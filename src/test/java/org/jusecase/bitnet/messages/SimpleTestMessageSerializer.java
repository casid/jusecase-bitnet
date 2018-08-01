package org.jusecase.bitnet.messages;

import org.jusecase.bitpack.BitWriter;
import org.jusecase.bitpack.BitSerializer;
import org.jusecase.bitpack.BitReader;

public class SimpleTestMessageSerializer implements BitSerializer<SimpleTestMessage> {
    @Override
    public SimpleTestMessage createObject() {
        return new SimpleTestMessage();
    }

    @Override
    public void serialize(BitWriter writer, SimpleTestMessage object) {
        writer.writeInt12(object.timeFrame);
        writer.writeInt8(object.amountOfPlayers);
    }

    @Override
    public void deserialize(BitReader reader, SimpleTestMessage object) {
        object.timeFrame = reader.readInt12();
        object.amountOfPlayers = reader.readInt8();
    }
}
