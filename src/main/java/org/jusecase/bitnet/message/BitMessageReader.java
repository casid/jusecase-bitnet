package org.jusecase.bitnet.message;

import org.jusecase.bitnet.checksum.MessageChecksum;
import org.jusecase.bitnet.time.CurrentTimeProvider;
import org.jusecase.bitpack.buffer.BufferBitReader;

import java.nio.ByteBuffer;

public class BitMessageReader {
    private final BitMessageProtocol protocol;
    private final int clientId;
    private final DataForMessageNumber[] dataForMessageNumber;
    private MessageChecksum messageChecksum;
    private CurrentTimeProvider currentTimeProvider;


    public BitMessageReader(BitMessageProtocol protocol, int clientId) {
        this.protocol = protocol;
        this.clientId = clientId;
        this.dataForMessageNumber = new DataForMessageNumber[256];
        for (int i = 0; i < dataForMessageNumber.length; ++i) {
            dataForMessageNumber[i] = new DataForMessageNumber();
        }
        this.messageChecksum = protocol.getMessageChecksum();
        this.currentTimeProvider = new CurrentTimeProvider();
    }

    /**
     * @param packet a data packet received over the network
     * @return an {@link BitMessage} in case there is enough data to read the entire message, otherwise null
     */
    public BitMessage read(ByteBuffer packet) throws InvalidBitMessageException {
        packet.rewind();
        BufferBitReader unpacker = new BufferBitReader(protocol, packet);

        ByteBuffer data = unpacker.getBuffer();

        int receivedChecksum = data.getInt();
        byte messageNumber = data.get();
        data.putInt(0, clientId);
        data.rewind();
        int calculatedChecksum = messageChecksum.calculateChecksum(data);
        if (calculatedChecksum != receivedChecksum) {
            throw new InvalidBitMessageException("Invalid checksum, expected " + calculatedChecksum + " received " + receivedChecksum);
        }
        data.position(5);

        int messageType = unpacker.readInt8();
        if (messageType == BitMessageProtocol.MULTI_MESSAGE_TYPE) {
            return readMultipartMessagePacket(packet, messageNumber);
        }

        Class<?> messageClass = protocol.getBitTypes().getClassForType(messageType);

        BitMessage message = (BitMessage) unpacker.readObjectNonNull(messageClass);
        message.setMessageNumber(messageNumber);

        return message;
    }

    private BitMessage readMultipartMessagePacket(ByteBuffer packet, byte messageNumber) {
        int index = (int) messageNumber - Byte.MIN_VALUE;
        DataForMessageNumber data = dataForMessageNumber[index];
        return data.readMultipartMessagePacket(packet);
    }

    public void setMessageChecksum(MessageChecksum messageChecksum) {
        this.messageChecksum = messageChecksum;
    }

    public void setCurrentTimeProvider(CurrentTimeProvider currentTimeProvider) {
        this.currentTimeProvider = currentTimeProvider;
    }

    private class DataForMessageNumber {
        private ByteBuffer multiPartData;
        private int multiPartDataPackets;
        private int multiPartDataPacketsReceived;
        private int multiPartDataSize;
        private long timestampOfLastPacketReceived;

        public BitMessage readMultipartMessagePacket(ByteBuffer packet) {
            checkTimeout();
            extractPacketContent(packet);
            return tryToReadMessage();
        }

        private void checkTimeout() {
            long now = currentTimeProvider.getMillis();
            if (timestampOfLastPacketReceived != 0 && now - timestampOfLastPacketReceived >= protocol.getPacketTimeoutInMilliseconds()) {
                reset();
            }
            timestampOfLastPacketReceived = now;
        }

        private void extractPacketContent(ByteBuffer packet) {
            if (multiPartData == null) {
                this.multiPartData = protocol.createMultiPartData();
            }

            int packetNumber = packet.get();
            if (packetNumber == 0) {
                multiPartDataPackets = packet.get();
            }

            multiPartData.position(protocol.getPacketPosition(packetNumber));
            multiPartData.put(packet);
            multiPartDataSize = Math.max(multiPartDataSize, multiPartData.position());
        }

        private BitMessage tryToReadMessage() {
            ++multiPartDataPacketsReceived;
            if (multiPartDataPackets == 0 || multiPartDataPacketsReceived != multiPartDataPackets) {
                return null;
            }

            try {
                multiPartData.limit(multiPartDataSize);
                return read(multiPartData);
            } finally {
                releaseData();
            }
        }

        private void reset() {
            multiPartData.clear();
            multiPartDataPackets = 0;
            multiPartDataPacketsReceived = 0;
            multiPartDataSize = 0;
            timestampOfLastPacketReceived = 0;
        }

        private void releaseData() {
            reset();
            multiPartData = null;
        }
    }
}
