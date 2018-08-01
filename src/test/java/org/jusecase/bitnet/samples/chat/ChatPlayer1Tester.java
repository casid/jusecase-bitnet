package org.jusecase.bitnet.samples.chat;

public class ChatPlayer1Tester {
    public static void main(String[] args) {
        Chat chat = new Chat(Chat.ADDRESS1, 1);
        chat.start();
    }
}
