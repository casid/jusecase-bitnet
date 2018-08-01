package org.jusecase.bitnet.network;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface NetworkReceiverListener {
    void onPacketReceived(ByteBuffer packet, String senderHost, int senderPort);
    void onErrorReceived(IOException e);
}
