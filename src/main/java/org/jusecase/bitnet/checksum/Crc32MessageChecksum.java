package org.jusecase.bitnet.checksum;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

// TODO before doing anything productive with this, switch to CRC32C (http://download.java.net/java/jdk9/docs/api/java/util/zip/CRC32C.html)
public class Crc32MessageChecksum implements MessageChecksum {

    @Override
    public int calculateChecksum(ByteBuffer data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);

        return (int)crc32.getValue();
    }
}
