package org.jusecase.bitnet.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NetworkReceiver {
    private final InetSocketAddress host;
    private final NetworkReceiverListener listener;
    private final ByteBuffer packet;

    private DatagramChannel channel;
    private volatile boolean running;

    public NetworkReceiver(InetSocketAddress host, int packetCapacity, NetworkReceiverListener listener) {
        this.host = host;
        this.listener = listener;
        this.packet = ByteBuffer.allocateDirect(packetCapacity);
    }

    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("network receiver is already running");
        }

        openChannel();

        running = true;

        Thread thread = new Thread(this::listen);
        thread.setName("network-receiver");
        thread.start();
    }

    public void stop() throws IOException {
        running = false;
        closeChannel();
    }

    private void listen() {
        while (running) {
            receive();
        }
    }

    private void receive() {
        SocketAddress address = null;
        try {
            address = channel.receive(packet);
        } catch (IOException e) {
            if (!running) {
                return; // stop has been called, just return
            } else {
                listener.onErrorReceived(e);
            }
        }

        if (address instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) address;

            packet.limit(packet.position());
            packet.position(0);

            listener.onPacketReceived(packet, inetSocketAddress);

            packet.clear();
        }
    }

    private void openChannel() throws IOException {
        channel = DatagramChannel.open();
        channel.socket().bind(host);
    }

    private void closeChannel() throws IOException {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }
}
