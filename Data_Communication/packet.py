# packet.py

def generate_packet(type, size, src, dest, time, mid=None):

    if type not in ['ACK', 'DATA']:
        print("PACKET TYPE ERROR")
        return None

    packet = dict()
    packet['type'] = type

    if type == 'ACK':
        packet['size'] = 0  # ignore ACK packet size ( for throughput analysis )
    else:
        packet['size'] = size

    packet['src'] = src
    packet['dest'] = dest
    packet['gentime'] = time

    # if packet goes through AP
    if mid is not None:
        packet['mid'] = mid

    return packet
