package org.jusecase.bitnet.messages;

import org.jusecase.bitpack.BitWriter;
import org.jusecase.bitpack.BitSerializer;
import org.jusecase.bitpack.BitReader;

public class BiggestAllowedTestMessageSerializer implements BitSerializer<BiggestAllowedTestMessage> {
    @Override
    public BiggestAllowedTestMessage createObject() {
        return new BiggestAllowedTestMessage();
    }

    @Override
    public void serialize(BitWriter writer, BiggestAllowedTestMessage object) {
        for (int i = 0; i < BiggestAllowedTestMessage.maxBytes; ++i) {
            writer.writeInt8(object.data[i]);
        }
    }

    @Override
    public void deserialize(BitReader reader, BiggestAllowedTestMessage object) {
        for (int i = 0; i < BiggestAllowedTestMessage.maxBytes; ++i) {
            object.data[i] = (byte) reader.readInt8();
        }
    }
}
