package org.jusecase.bitnet.samples.chat;

import org.jusecase.bitpack.BitReader;
import org.jusecase.bitpack.BitSerializer;
import org.jusecase.bitpack.BitWriter;

public class ChatMessageSerializer implements BitSerializer<ChatMessage> {
    @Override
    public ChatMessage createObject() {
        return new ChatMessage();
    }

    @Override
    public void serialize(BitWriter writer, ChatMessage object) {
        writer.writeInt8(object.playerId);
        writer.writeStringNonNull(object.text);
    }

    @Override
    public void deserialize(BitReader reader, ChatMessage object) {
        object.playerId = reader.readInt8();
        object.text = reader.readStringNonNull();
    }
}
