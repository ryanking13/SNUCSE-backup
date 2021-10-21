# channel.py

class Channel:

    def __init__(self, propa_delay):
        self.BUSY = 1           # channel is busy
        self.EMPTY = 0          # channel is empty

        self.propa_delay = propa_delay
        self.last_occupied_time = 0
        self.last_busy_time = 0

        self.state = self.EMPTY
        self.is_on_collision = False

        self.carrying_packet = []

    # if channel is busy and node can sense it
    def is_busy(self, curr_time, busy_sense_delay):
        if (self.state == self.BUSY) and (curr_time - self.last_busy_time >= busy_sense_delay):
            return True

        return False

    # node try to occupy channel
    def occupy(self, packet, curr_time):

        self.last_occupied_time = curr_time
        self.carrying_packet.append(packet)

        if len(self.carrying_packet) > 1:
            self.is_on_collision = True
            # print('collision', self.carrying_packet)
        else:
            self.last_busy_time = curr_time

        self.state = self.BUSY

    # check transmission is done
    # if so, return transmitted packet
    def check_transmission_done(self, curr_time):
        if self.state == self.EMPTY:
            return None
        else:
            if curr_time - self.last_occupied_time >= self.propa_delay:
                if self.is_on_collision:
                    self.clear_channel()
                    return None
                else:
                    return self.carrying_packet[0]
            else:
                return None

    # clear channel after transmission
    def clear_channel(self):
        self.carrying_packet = []
        self.state = self.EMPTY
        self.is_on_collision = False

