package org.jusecase.bitnet.checksum;

import java.nio.ByteBuffer;

public class MessageChecksumStub implements MessageChecksum {

    private int checksum;

    public MessageChecksumStub() {
        this(0);
    }

    public MessageChecksumStub(int checksum) {
        this.checksum = checksum;
    }

    @Override
    public int calculateChecksum(ByteBuffer data) {
        return checksum;
    }
}