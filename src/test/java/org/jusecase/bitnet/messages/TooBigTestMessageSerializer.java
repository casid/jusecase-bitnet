package org.jusecase.bitnet.messages;

import org.jusecase.bitnet.message.TestMessageProtocol;
import org.jusecase.bitpack.BitWriter;
import org.jusecase.bitpack.BitSerializer;
import org.jusecase.bitpack.BitReader;

public class TooBigTestMessageSerializer implements BitSerializer<TooBigTestMessage> {
    @Override
    public TooBigTestMessage createObject() {
        return new TooBigTestMessage();
    }

    @Override
    public void serialize(BitWriter writer, TooBigTestMessage object) {
        int tooMuchInts = (TestMessageProtocol.MAX_PACKET_BYTES * TestMessageProtocol.MAX_PACKETS_PER_MESSAGE) / 4;
        for (int i = 0; i < tooMuchInts; ++i) {
            writer.writeInt32(i);
        }
    }

    @Override
    public void deserialize(BitReader reader, TooBigTestMessage object) {
    }
}
