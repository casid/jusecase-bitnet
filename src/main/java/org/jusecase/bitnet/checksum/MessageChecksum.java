package org.jusecase.bitnet.checksum;

import java.nio.ByteBuffer;

public interface MessageChecksum {
    int calculateChecksum(ByteBuffer data);
}
