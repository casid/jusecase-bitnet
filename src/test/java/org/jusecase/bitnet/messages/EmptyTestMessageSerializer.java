package org.jusecase.bitnet.messages;

import org.jusecase.bitpack.BitWriter;
import org.jusecase.bitpack.BitSerializer;
import org.jusecase.bitpack.BitReader;

public class EmptyTestMessageSerializer implements BitSerializer<EmptyTestMessage> {
    @Override
    public EmptyTestMessage createObject() {
        return new EmptyTestMessage();
    }

    @Override
    public void serialize(BitWriter writer, EmptyTestMessage object) {
    }

    @Override
    public void deserialize(BitReader reader, EmptyTestMessage object) {
    }
}
