# -*- coding: utf-8 -*-
# nodes.py

import random
from packet import generate_packet


# node for 1-persistent CSMA + binary exponential backoff
class OnePersistentNode:

    def __init__(self, channel, rate, id, process_delay, propa_delay):

        random.seed()

        self.IDLE = 0                                   # state : node is not sending
        self.PROCESSING = 1                             # state : node received DATA packet and preparing ACK
        self.SENDING = 2                                # state : node sent DATA packet and waiting for ACK
        self.state = self.IDLE

        self.MAX_BACKOFF = 2**10

        self.id = id                                    # node identify number
        self.channel = channel                          # channel that node is connected
        self.rate = rate                                # packet generation rate ( per time unit )

        self.process_delay = process_delay
        self.propa_delay = propa_delay
        self.busy_sence_delay = self.propa_delay // 2   # for easy implementation
        self.timeout = self.propa_delay * 10           # set manually
        self.backoff_range = 1                          # backoff = uniform[1..backoff_range] * propa_delay
        self.backoff = 0

        self.last_send_time = 0                         # last data sent time ( for timeout check )
        self.last_receive_time = 0                      # last data receive time ( for ACK gen time )
        # self.last_collision_time = 0                    # last collision occur time ( for backoff check )

        self.packets = []                               # packets waiting for transmission

    # make random packet
    def generate_random_packet(self, curr_time, n_node, packet_size):
        if random.random() < (1 / self.propa_delay) and random.random() < self.rate:
            random_id = self.id
            while random_id == self.id:
                random_id = random.randint(0, n_node-1)

            self.packets.append(generate_packet(type='DATA', size=packet_size, time=curr_time,
                                                src=self.id, dest=random_id, mid='AP'))

    # node is not waiting for ACK and have packet to send
    def have_msg(self, curr_time):
        if self.state == self.IDLE and len(self.packets) > 0:
            return True
        elif self.state == self.PROCESSING and (curr_time - self.last_receive_time > self.process_delay - 1):
            return True

        return False

    # try sending packet
    def try_send(self, curr_time):

        # waiting for ACK
        if self.state == self.SENDING:
            return False

        # channel is busy
        if self.channel.is_busy(curr_time, self.busy_sence_delay):
            return False

        if self.backoff > 0:
            self.backoff -= 1
            return False

        # # waiting for backoff time
        # if curr_time - self.last_collision_time < self.backoff:
        #     return False

        self.channel.occupy(self.packets[0], curr_time)
        self.last_send_time = curr_time

        # if sending packet is ACK, just remove from list
        if self.packets[0]['type'] == 'ACK':
            self.clear_packet()
        # if sending packet is DATA, reserve if for timeout case
        elif self.packets[0]['type'] == 'DATA':
            self.state = self.SENDING

        return True

    # check if collision occurred
    def check_collision(self, curr_time):
        if self.state != self.SENDING:
            return False

        if (curr_time - self.last_send_time) >= self.timeout:
            self.update_backoff()
            self.state = self.IDLE
            # self.last_collision_time = curr_time

            return True

    # clear reserved packet
    def clear_packet(self):
        self.packets.pop(0)
        if len(self.packets) == 0:
            self.state = self.IDLE

    # receive packet from other node
    def receive_packet(self, packet, curr_time):
        if packet['type'] == 'DATA':

            for p in self.packets:
                # prevent duplicate packets
                if p['gentime'] == packet['gentime'] and p['dest'] == packet['src']:
                    return False

            # if node is waiting for other ACK
            if self.state == self.SENDING:
                # prevent deadlock
                if packet['src'] != self.packets[0]['dest']:
                    return False

            ack_packet = generate_packet(type='ACK', time=packet['gentime'], size=0, src=self.id, dest=packet['src'])
            self.packets.insert(0, ack_packet)

            self.state = self.PROCESSING
            self.last_receive_time = curr_time

            return True

        elif packet['type'] == 'ACK':

            # if node is not waiting for ACK
            if self.state != self.SENDING:
                return False

            # if packet is from strange node
            if packet['src'] != self.packets[0]['dest']:
                return False

            self.clear_packet()
            self.reset_backoff()
            return True

    def reset_backoff(self):
        self.backoff_range = 1
        self.backoff = 0

    def update_backoff(self):
        self.backoff_range = min(self.backoff_range*2, self.MAX_BACKOFF)
        self.backoff = random.randint(1, self.backoff_range) * self.propa_delay


# node for IEEE 802.11 CSMA-CA DCF
class DcfNode:

    def __init__(self, channel, rate, id, propa_delay, sifs, difs):

        self.IDLE = 0                                   # state : node is not sending
        self.WAITING = 1                                # state : waiting for IFS
        self.DELAY = 2                                  # state : waiting for backoff
        self.PROCESSING = 3                             # state : node received DATA packet and preparing ACK
        self.SENDING = 4                                # state : node sent DATA packet and waiting for ACK
        self.state = self.IDLE

        self.MAX_BACKOFF = 2 ** 10

        self.id = id                                    # node identify number
        self.channel = channel                          # channel that node is connected
        self.rate = rate                                # packet generation rate ( per time unit )

        self.process_delay = 0
        self.propa_delay = propa_delay
        self.timeout = self.propa_delay * 10             # set manually
        self.backoff_range = 1                          # backoff = uniform[1..backoff_range] * propa_delay
        self.backoff = 0

        self.sifs = sifs
        self.difs = difs

        self.last_send_time = 0                         # last data sent time ( for timeout check )
        self.last_sense_time = 0                        # last channel sense time ( for IFS check )
        self.last_receive_time = 0                      # last data receive time ( for ACK gen time )
        # self.last_collision_time = 0                    # last collision occur time ( for backoff check )

        self.packets = []                               # packets waiting for transmission

    # make random packet
    def generate_random_packet(self, curr_time, n_node, packet_size):
        if random.random() < (1 / self.propa_delay) and random.random() < self.rate:
            random_id = self.id
            while random_id == self.id:
                random_id = random.randint(0, n_node-1)

            self.packets.append(generate_packet(type='DATA', size=packet_size, time=curr_time,
                                                src=self.id, dest=random_id))

    # node is not waiting for ACK and have packet to send
    def have_msg(self, curr_time):
        if self.state == self.IDLE and len(self.packets) > 0:
            return True
        elif self.state == self.PROCESSING and (curr_time - self.last_receive_time > self.process_delay - 1):
            return True

        return False

    # node is waiting for ifs
    def is_waiting(self):
        return self.state == self.WAITING

    # node if waiting for backoff
    def is_delaying(self):
        return self.state == self.DELAY

    # done waiting ifs
    def is_done_ifs(self, curr_time):
        ifs = None
        if self.packets[0]['type'] == 'ACK':
            ifs = self.sifs
        elif self.packets[0]['type'] == 'DATA':
            ifs = self.difs

        if curr_time - self.last_sense_time >= ifs:
            self.state = self.DELAY
            return True

        return False

    # sense channel
    def sense_channel(self, curr_time):
        # if channel is busy, return false
        if self.channel.is_busy(curr_time, 0):
            return False

        # if channel is idl, start waiting ifs and return true
        else:
            self.state = self.WAITING
            self.last_sense_time = curr_time
            return True

    # try sending packet
    def try_send(self, curr_time):

        # waiting for ACK
        if self.state == self.SENDING:
            return False

        # channel is busy
        if self.channel.is_busy(curr_time, 0):
            return False

        if self.backoff > 0:
            self.backoff -= 1
            return False

        self.channel.occupy(self.packets[0], curr_time)
        self.last_send_time = curr_time

        # if sending packet is ACK, just remove from list
        if self.packets[0]['type'] == 'ACK':
            self.clear_packet()
        # if sending packet is DATA, reserve if for timeout case
        elif self.packets[0]['type'] == 'DATA':
            self.state = self.SENDING

        return True

    # check if collision occurred
    def check_collision(self, curr_time):
        if self.state != self.SENDING:
            return False

        if curr_time - self.last_send_time >= self.timeout:
            self.update_backoff()
            self.state = self.IDLE
            # self.last_collision_time = curr_time

            return True

    # clear reserved packet
    def clear_packet(self):
        self.packets.pop(0)
        if len(self.packets) == 0:
            self.state = self.IDLE

    # receive packet from other node
    def receive_packet(self, packet, curr_time):
        if packet['type'] == 'DATA':

            for p in self.packets:
                # prevent duplicate packets
                if p['gentime'] == packet['gentime'] and p['dest'] == packet['src']:
                    return False

            # if node is waiting for other ACK
            if self.state == self.SENDING:
                # prevent deadlock
                if packet['src'] != self.packets[0]['dest']:
                    return False

            ack_packet = generate_packet(type='ACK', time=packet['gentime'], size=0,
                                         src=self.id, dest=packet['src'], mid='AP')
            self.packets.insert(0, ack_packet)

            self.state = self.PROCESSING
            self.last_receive_time = curr_time

            return True

        elif packet['type'] == 'ACK':

            # if node is not waiting for ACK
            if self.state != self.SENDING:
                return False

            # if packet is from strange node
            if packet['src'] != self.packets[0]['dest']:
                return False

            self.clear_packet()
            self.reset_backoff()
            return True

    def reset_backoff(self):
        self.backoff_range = 1
        self.backoff = 0

    def update_backoff(self):
        self.backoff_range = min(self.backoff_range*2, self.MAX_BACKOFF)
        self.backoff = random.randint(1, self.backoff_range) * self.propa_delay
