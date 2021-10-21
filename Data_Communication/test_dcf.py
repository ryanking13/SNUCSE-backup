# test_dcf.py

import sys
import random
from nodes import DcfNode
from accessPoint import AP
from channel import Channel

ALL = False

ONE_SEC = 10**6
PACKET_SIZE = 8192              # 8 * 1024 bit
TRANSMISSION_DELAY = 20
SIFS = 10
DIFS = 50
PROCESS_DELAY = SIFS

transmission_cnt = 0        # total data packet send count
success_cnt = 0     # total success count
collision_cnt = 0   # total collision count
total_delay = 0     # total transmission delay ( ACK packet arrive - DATA packet gen )


# check if there is finished transmission
def check_transmission_done(channel, nodes, ap, curr_time):

    global success_cnt
    global total_delay

    packet = channel.check_transmission_done(curr_time)

    # if there is completed transmission
    if packet is not None:
        # print('packet arrived!')

        # clear channel to idle
        channel.clear_channel()

        if ALL:
            print("%-10s gentime: %7d currtime: %d type: %5s src-dest: %s-%s"
                  % ("SUCCESS", packet['gentime'], curr_time, packet['type'], packet['src'], packet['dest']))

        # packet if to AP
        if 'mid' in packet:
            ap.receive_packet(packet, curr_time)
            return

        received = nodes[packet['dest']].receive_packet(packet, curr_time)

        # update variables for analysis
        if received and packet['type'] == 'ACK':
            success_cnt += 1
            total_delay += curr_time - packet['gentime']

# check each nodes transmission, collision
def check_nodes(nodes, curr_time, n_node):

    global collision_cnt
    global transmission_cnt

    for node in nodes:
        collision = node.check_collision(curr_time)

        if collision:
            if ALL:
                print("%-10s time: %d src: %s" % ("COLLISION", curr_time, node.id))
            collision_cnt += 1

        node.generate_random_packet(curr_time, n_node, PACKET_SIZE)

        # if node have msg to transmit
        if node.have_msg(curr_time):
            node.sense_channel(curr_time)

        if node.is_waiting():
            node.is_done_ifs(curr_time)

        if node.is_delaying():
            sended = node.try_send(curr_time)
            if sended:
                transmission_cnt += 1


# initialize nodes
def init_nodes(nodes, channel, n_node, arrival_rate):
    for i in range(n_node):
        nodes.append(DcfNode(channel=channel, rate=arrival_rate, id=i, propa_delay=TRANSMISSION_DELAY,
                             sifs=SIFS, difs=DIFS))


def main():

    global transmission_cnt
    global success_cnt
    global collision_cnt
    global total_delay
    global ALL

    random.seed()

    curr_time = 0
    n_node = int(sys.argv[1])
    arrival_rate = float(sys.argv[2])

    if '-all' in sys.argv:
        ALL = True

    nodes = []
    channel = Channel(propa_delay=TRANSMISSION_DELAY)
    ap = AP(channel=channel)

    init_nodes(nodes, channel, n_node, arrival_rate)

    test_time = ONE_SEC // 2
    while curr_time < test_time:

        check_transmission_done(channel, nodes, ap, curr_time)
        check_nodes(nodes, curr_time, n_node)

        curr_time += 1

    # print('transmission_cnt :', transmission_cnt)
    print('success_cnt :', success_cnt)
    print('collision_cnt :', collision_cnt)
    # print('total_delay :', total_delay)

    print('throughput :', success_cnt / (test_time / ONE_SEC))
    print('mean packet delay :', total_delay / success_cnt)
    print('transmission collision probability :', collision_cnt / (success_cnt + collision_cnt))


if __name__ == '__main__':
    main()