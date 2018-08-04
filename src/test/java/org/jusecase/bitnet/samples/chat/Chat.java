package org.jusecase.bitnet.samples.chat;

import org.jusecase.bitnet.message.BitMessage;
import org.jusecase.bitnet.message.BitMessageReader;
import org.jusecase.bitnet.message.BitMessageWriter;
import org.jusecase.bitnet.message.InvalidBitMessageException;
import org.jusecase.bitnet.network.NetworkReceiver;
import org.jusecase.bitnet.network.NetworkReceiverListener;
import org.jusecase.bitnet.network.NetworkSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Chat implements NetworkReceiverListener {

    public static InetSocketAddress ADDRESS1 = new InetSocketAddress("localhost", 60387);
    public static InetSocketAddress ADDRESS2 = new InetSocketAddress("localhost", 60388);

    private final InetSocketAddress address;
    private final int playerId;

    private final ChatProtocol protocol = new ChatProtocol();
    private final BitMessageReader reader = new BitMessageReader(protocol, 0x12340001);
    private final BitMessageWriter writer = new BitMessageWriter(protocol, 0x12340001);

    private NetworkReceiver networkReceiver;
    private NetworkSender networkSender;

    private List<InetSocketAddress> addresses = Arrays.asList(ADDRESS1, ADDRESS2);

    public Chat(InetSocketAddress address, int playerId) {
        this.address = address;
        this.playerId = playerId;
    }

    public void start() {
        try {
            networkReceiver = new NetworkReceiver(address, ChatProtocol.MAX_PACKET_BYTES, this);
            networkReceiver.start();

            networkSender = new NetworkSender();
        } catch (IOException e) {
            System.err.println("Failed to start chat, will terminate now");
            e.printStackTrace();
            return;
        }

        System.out.println("Welcome to the test chat. Type a message to send it, or 'exit' to terminate");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            if ("exit".equals(message)) {
                System.out.println("Shutting down");
                stop();
                return;
            }

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.text = message;
            chatMessage.playerId = playerId;

            System.out.println(chatMessage);
            sendToOtherPlayers(chatMessage);
        }
    }

    public void stop() {
        try {
            networkSender.close();
        } catch (IOException e) {
            System.err.println("IO error while closing network sender");
            e.printStackTrace();
        }

        try {
            networkReceiver.stop();
        } catch (IOException e) {
            System.err.println("IO error while stopping network receiver");
            e.printStackTrace();
        }
    }

    @Override
    public void onPacketReceived(ByteBuffer packet, InetSocketAddress address) {
        try {
            BitMessage message = reader.read(packet);
            if (message == null) {
                return;
            }

            System.out.println(message.toString());
        } catch (InvalidBitMessageException e) {
            System.err.println("invalid bit message received");
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorReceived(IOException e) {
        System.err.println("IO error while listening for incoming packets");
        e.printStackTrace();
    }

    public void sendToOtherPlayers(BitMessage message) {
        List<ByteBuffer> packets = writer.write(message);

        for (InetSocketAddress address : addresses) {
            if (!address.equals(this.address)) {
                try {
                    networkSender.send(address, packets);
                } catch (IOException e) {
                    System.out.println("IO error while sending message to " + address);
                    e.printStackTrace();
                }
            }
        }
    }
}
