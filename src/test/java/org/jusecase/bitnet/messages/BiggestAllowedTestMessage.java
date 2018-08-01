package org.jusecase.bitnet.messages;

import org.jusecase.bitnet.message.BitMessage;
import org.jusecase.bitnet.message.TestMessageProtocol;

import java.util.Random;

public class BiggestAllowedTestMessage extends BitMessage {

    // = Max message bytes - checksum(4 byte) - messageType(1 byte)
    public static final int maxBytes = TestMessageProtocol.MAX_PACKET_BYTES * TestMessageProtocol.MAX_PACKETS_PER_MESSAGE - 4 - 10;

    public byte[] data = new byte[maxBytes];

    public BiggestAllowedTestMessage() {
        Random random = new Random(0);
        random.nextBytes(data);
    }
}
