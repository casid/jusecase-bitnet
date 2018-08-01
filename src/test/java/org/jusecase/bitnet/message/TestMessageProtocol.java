package org.jusecase.bitnet.message;

import org.jusecase.bitnet.message.BitMessageProtocol;
import org.jusecase.bitnet.messages.*;

public class TestMessageProtocol extends BitMessageProtocol {

    public static final int MAX_PACKET_BYTES = 32; // A very low value to test packet splitting
    public static final int MAX_PACKETS_PER_MESSAGE = 10;

    public TestMessageProtocol() {
        super(MAX_PACKET_BYTES, MAX_PACKETS_PER_MESSAGE);
        register(1, new EmptyTestMessageSerializer());
        register(2, new SimpleTestMessageSerializer());
        register(3, new TooBigTestMessageSerializer());
        register(4, new BiggestAllowedTestMessageSerializer());
        register(5, new BigTestMessageSerializer());
    }
}
