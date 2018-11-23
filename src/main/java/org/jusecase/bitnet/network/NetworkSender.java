package org.jusecase.bitnet.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

public class NetworkSender implements AutoCloseable {

    private DatagramChannel channel;

    public NetworkSender() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(true);
    }

    @Override
    public void close() throws IOException {
        channel.disconnect();
    }

    public void setBroadcast(boolean broadcast) throws IOException {
        channel.socket().setBroadcast(broadcast);
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
