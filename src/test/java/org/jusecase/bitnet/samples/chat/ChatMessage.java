package org.jusecase.bitnet.samples.chat;

import org.jusecase.bitnet.message.BitMessage;

public class ChatMessage extends BitMessage {
    public int playerId;
    public String text;

    public ChatMessage() {
    }

    @Override
    public String toString() {
        return "Player " + playerId + " says : " + text;
    }
}
