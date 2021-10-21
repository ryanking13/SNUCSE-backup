# accessPoint.py

from packet import generate_packet


class AP:

    def __init__(self, channel):
        self.channel = channel

    def is_busy(self, curr_time):
        return self.channel.is_busy(curr_time, 0)

    def receive_packet(self, packet, curr_time):
        self.send_packet(packet, curr_time)

    def send_packet(self, packet, curr_time):

        new_packet = generate_packet(type=packet['type'], size=packet['size'], time=packet['gentime'],
                                     src=packet['src'], dest=packet['dest'])

        if not self.is_busy(curr_time):
            self.channel.occupy(new_packet, curr_time)