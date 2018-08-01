package org.jusecase.bitnet.messages;

import org.jusecase.bitpack.BitWriter;
import org.jusecase.bitpack.BitSerializer;
import org.jusecase.bitpack.BitReader;

public class BigTestMessageSerializer implements BitSerializer<BigTestMessage> {
    @Override
    public BigTestMessage createObject() {
        return new BigTestMessage();
    }

    @Override
    public void serialize(BitWriter writer, BigTestMessage object) {
        for (int i = 0; i < BigTestMessage.maxBytes; ++i) {
            writer.writeInt8(object.data[i]);
        }
    }

    @Override
    public void deserialize(BitReader reader, BigTestMessage object) {
        for (int i = 0; i < BigTestMessage.maxBytes; ++i) {
            object.data[i] = (byte) reader.readInt8();
        }
    }
}
