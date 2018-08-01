package org.jusecase.bitnet.message;

import org.jusecase.bitnet.checksum.Crc32MessageChecksum;
import org.jusecase.bitnet.checksum.MessageChecksum;
import org.jusecase.bitpack.AbstractBitProtocol;

import java.nio.ByteBuffer;

public class BitMessageProtocol extends AbstractBitProtocol {

    public static final byte MULTI_MESSAGE_TYPE = 0;
    public static final int MAX_PACKETS_PER_MESSAGE = 256;

    private final int maxPacketBytes;
    private final int maxPacketsPerMessage;
    private MessageChecksum messageChecksum = new Crc32MessageChecksum();

    public BitMessageProtocol() {
        this(1024, 32);
    }

    public BitMessageProtocol(int maxPacketBytes, int maxPacketsPerMessage) {
        if (maxPacketsPerMessage <= 0 || maxPacketsPerMessage > MAX_PACKETS_PER_MESSAGE) {
            throw new IllegalArgumentException("This protocol cannot handle more than " + MAX_PACKETS_PER_MESSAGE + " packets per message.");
        }

        this.maxPacketBytes = maxPacketBytes;
        this.maxPacketsPerMessage = maxPacketsPerMessage;
    }

    public int getTypeForMessage(BitMessage message) {
        return getBitTypes().getTypeForInstance(message);
    }

    public int getMaxPacketBytes() {
        return maxPacketBytes;
    }

    public int getEffectiveBytesForFirstPacket() {
        int byteOverheadForFirstPacket = 4 + 1 + 1 + 1 + 1; // 4 (checksum) 1 messageNumber 1 (multipart messageType) 1 (packetNumber) 1 (packet count)
        return maxPacketBytes - byteOverheadForFirstPacket;
    }

    public int getEffectiveBytesForAdditionalPacket() {
        int byteOverheadForAdditionalPackets = 4 + 1 + 1 + 1; // 4 (checksum) 1 messageNumber 1 (multipart messageType) 1 (packetNumber)
        return maxPacketBytes - byteOverheadForAdditionalPackets;
    }

    public int getPacketPosition(int packetNumber) {
        if (packetNumber == 0) {
            return 0;
        }

        return getEffectiveBytesForFirstPacket() + (packetNumber - 1) * getEffectiveBytesForAdditionalPacket();
    }

    public MessageChecksum getMessageChecksum() {
        return messageChecksum;
    }

    public void setMessageChecksum(MessageChecksum messageChecksum) {
        this.messageChecksum = messageChecksum;
    }

    public int getMaxMessageBytes() {
        return maxPacketBytes * maxPacketsPerMessage;
    }

    public ByteBuffer createPacket() {
        return ByteBuffer.allocateDirect(getMaxPacketBytes());
    }

    public ByteBuffer createMultiPartData() {
        return ByteBuffer.allocateDirect(getMaxMessageBytes());
    }

    public int getPacketTimeoutInMilliseconds() {
        return 5000; // Approx. (one message / 30ms with 256 cycling message numbers would be 7680ms, we can live with less)
    }
}
