package org.jusecase.bitnet.messages;

import org.jusecase.bitnet.message.BitMessage;
import org.jusecase.bitnet.message.TestMessageProtocol;

import java.util.Random;

public class BigTestMessage extends BitMessage {

    public static final int maxBytes = TestMessageProtocol.MAX_PACKET_BYTES * 3;

    public byte[] data = new byte[maxBytes];

    public BigTestMessage() {
        Random random = new Random(0);
        random.nextBytes(data);
    }


}
