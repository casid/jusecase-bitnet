package org.jusecase.bitnet.samples.chat;

public class ChatPlayer2Tester {
    public static void main(String[] args) {
        Chat chat = new Chat(Chat.ADDRESS2, 2);
        chat.start();
    }
}
