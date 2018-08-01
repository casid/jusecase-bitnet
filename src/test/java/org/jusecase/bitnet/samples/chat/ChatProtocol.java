package org.jusecase.bitnet.samples.chat;

import org.jusecase.bitnet.message.BitMessageProtocol;

public class ChatProtocol extends BitMessageProtocol {
    public static final int MAX_PACKET_BYTES = 1400;
    public static final int MAX_PACKETS_PER_MESSAGE = 10;

    public ChatProtocol() {
        super(MAX_PACKET_BYTES, MAX_PACKETS_PER_MESSAGE);
        register(new ChatMessageSerializer());
    }
}
