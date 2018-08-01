package org.jusecase.bitnet.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

public class NetworkSender implements AutoCloseable {

    private DatagramChannel channel;

    public NetworkSender() throws IOException {
        channel = DatagramChannel.open(StandardProtocolFamily.INET6);
        channel.configureBlocking(true);
    }

    @Override
    public void close() throws IOException {
        channel.disconnect();
    }

    public void send(InetSocketAddress address, ByteBuffer packet) throws IOException {
        channel.send(packet, address);
        packet.rewind();
    }

    public void send(InetSocketAddress address, List<ByteBuffer> packets) throws IOException {
        for (ByteBuffer packet : packets) {
            send(address, packet);
        }
    }
}
