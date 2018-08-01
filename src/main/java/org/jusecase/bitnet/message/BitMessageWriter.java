package org.jusecase.bitnet.message;

import org.jusecase.bitnet.checksum.MessageChecksum;
import org.jusecase.bitpack.buffer.BufferBitWriter;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitMessageWriter {

    private final BitMessageProtocol protocol;
    private final int clientId;
    private final BufferBitWriter packer;
    private final PacketSplitter splitter = new PacketSplitter();
    private MessageChecksum messageChecksum;
    private byte nextMessageNumber;

    public BitMessageWriter(BitMessageProtocol protocol, int clientId) {
        this.protocol = protocol;
        this.clientId = clientId;
        this.packer = new BufferBitWriter(protocol, ByteBuffer.allocateDirect(protocol.getMaxMessageBytes()));
        this.messageChecksum = protocol.getMessageChecksum();
    }

    /**
     * @param message the {@link BitMessage} to send
     * @return A list of packets containing this message, ready to be sent over the wire
     */
    public List<ByteBuffer> write(BitMessage message) {
        try {
            packer.reset();
            ByteBuffer data = writeInOnePacket(message);
            if (data.limit() <= protocol.getMaxPacketBytes()) {
                return Collections.singletonList(data);
            }

            return writeInMultiplePackets(data);
        } finally {
            increaseNextMessageNumber();
        }
    }

    private void increaseNextMessageNumber() {
        ++nextMessageNumber;
    }

    private ByteBuffer writeInOnePacket(BitMessage message) {
        try {
            ByteBuffer data = packer.getBuffer();
            data.putInt(clientId);
            data.put(nextMessageNumber);

            int type = protocol.getTypeForMessage(message);
            data.put((byte) type);
            packer.writeObjectNonNull(message);
            packer.flush();

            data.limit(data.position());
            data.rewind();
            int checksum = messageChecksum.calculateChecksum(data);

            data.putInt(0, checksum);
            data.rewind();
            return data;
        } catch (BufferOverflowException e) {
            throw new InvalidBitMessageException("Maximum message size of " + protocol.getMaxMessageBytes() + " bytes exceeded");
        }
    }

    private List<ByteBuffer> writeInMultiplePackets(ByteBuffer data) {
        return splitter.split(data);
    }

    public void setMessageChecksum(MessageChecksum messageChecksum) {
        this.messageChecksum = messageChecksum;
    }

    public byte getNextMessageNumber() {
        return nextMessageNumber;
    }

    public void setNextMessageNumber(byte nextMessageNumber) {
        this.nextMessageNumber = nextMessageNumber;
    }

    private final class PacketSplitter {

        private ByteBuffer data;
        private List<ByteBuffer> packets;
        private int dataBytes;
        private int effectiveBytesForFirstPacket;
        private int effectiveBytesForAdditionalPacket;
        private int packetCount;

        public List<ByteBuffer> split(ByteBuffer data) {
            init(data);

            ByteBuffer packet1 = protocol.createPacket();
            writePacketPart(0, packet1);
            packets.add(packet1);

            for (int packetNumber = 1; packetNumber < packetCount; ++packetNumber) {
                ByteBuffer packet = protocol.createPacket();
                writePacketPart(packetNumber, packet);
                packets.add(packet);
            }

            return packets;
        }

        private void init(ByteBuffer data) {
            packets = new ArrayList<>();
            this.data = data;
            dataBytes = data.limit();
            data.rewind();
            effectiveBytesForFirstPacket = protocol.getEffectiveBytesForFirstPacket();
            effectiveBytesForAdditionalPacket = protocol.getEffectiveBytesForAdditionalPacket();
            packetCount = calculatePacketCount();
        }

        private int calculatePacketCount() {
            int remainingBytesAfterFirstPacket = dataBytes - effectiveBytesForFirstPacket;
            int packetCount = (remainingBytesAfterFirstPacket / effectiveBytesForAdditionalPacket) + 1;
            if (remainingBytesAfterFirstPacket % effectiveBytesForAdditionalPacket != 0) {
                ++packetCount;
            }

            return packetCount;
        }

        private void writePacketPart(int packetNumber, ByteBuffer packetPart) {
            packetPart.putInt(clientId);
            packetPart.put(nextMessageNumber);
            packetPart.put(BitMessageProtocol.MULTI_MESSAGE_TYPE);
            packetPart.put((byte) packetNumber);
            if (packetNumber == 0) {
                packetPart.put((byte) packetCount);
                data.limit(effectiveBytesForFirstPacket);
            } else {
                data.limit(Math.min(data.position() + effectiveBytesForAdditionalPacket, dataBytes));
            }

            packetPart.put(data);
            packetPart.limit(packetPart.position());

            packetPart.rewind();
            int packetChecksum = messageChecksum.calculateChecksum(packetPart);
            packetPart.putInt(0, packetChecksum);

            packetPart.rewind();
        }
    }
}
