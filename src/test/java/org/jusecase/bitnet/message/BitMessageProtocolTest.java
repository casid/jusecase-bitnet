package org.jusecase.bitnet.message;

import org.junit.Test;
import org.jusecase.bitnet.message.BitMessageProtocol;

public class BitMessageProtocolTest {
    @Test(expected = IllegalArgumentException.class)
    public void tooBigMaxPacketsPerMessage() {
        new BitMessageProtocol(1024, BitMessageProtocol.MAX_PACKETS_PER_MESSAGE + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallMaxPacketsPerMessage() {
        new BitMessageProtocol(1024, -1);
    }
}