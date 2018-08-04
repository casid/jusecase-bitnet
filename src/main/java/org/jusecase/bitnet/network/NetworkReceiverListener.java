package org.jusecase.bitnet.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface NetworkReceiverListener {
    void onPacketReceived(ByteBuffer packet, InetSocketAddress address);
    void onErrorReceived(IOException e);
}
