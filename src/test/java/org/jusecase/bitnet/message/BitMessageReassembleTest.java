package org.jusecase.bitnet.message;

import org.jusecase.bitnet.messages.BigTestMessage;
import org.jusecase.bitnet.messages.BiggestAllowedTestMessage;
import org.jusecase.bitnet.messages.SimpleTestMessage;
import org.jusecase.bitnet.time.CurrentTimeProviderStub;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class BitMessageReassembleTest {

    BitMessageProtocol protocol;
    BitMessageWriter writer;
    BitMessageReader reader;

    @Before
    public void setUp() {
        protocol = new TestMessageProtocol();

        writer = new BitMessageWriter(protocol, 0x12340001);
        reader = new BitMessageReader(protocol, 0x12340001);
    }

    @Test
    public void packetsInReverseOrder() {
        BiggestAllowedTestMessage message = new BiggestAllowedTestMessage();
        List<ByteBuffer> bigMessagePackets = writer.write(message);

        for (int i = bigMessagePackets.size() - 1; i > 0; --i) {
            assertThat(reader.read(bigMessagePackets.get(i))).isNull();
        }
        assertThat(reader.read(bigMessagePackets.get(0))).isEqualToComparingFieldByField(message);
    }

    @Test
    public void packetsInRandomOrder() {
        BiggestAllowedTestMessage message = new BiggestAllowedTestMessage();
        List<ByteBuffer> bigMessagePackets = writer.write(message);
        Collections.shuffle(bigMessagePackets, new Random(0));

        for (int i = bigMessagePackets.size() - 1; i > 0; --i) {
            assertThat(reader.read(bigMessagePackets.get(i))).isNull();
        }
        assertThat(reader.read(bigMessagePackets.get(0))).isEqualToComparingFieldByField(message);
    }

    @Test
    public void packetsMixedWithInvalidPacket() {

    }

    @Test
    public void packetsMixedWithSimplePacket() {
        BiggestAllowedTestMessage message = prepareMessage(new BiggestAllowedTestMessage());
        List<ByteBuffer> bigMessagePackets = writer.write(message);

        SimpleTestMessage simpleMessage = prepareMessage(new SimpleTestMessage());
        ByteBuffer simpleMessagePacket = writer.write(simpleMessage).get(0);

        for (int i = 0; i < 7; ++i) {
            assertThat(reader.read(bigMessagePackets.get(i))).isNull();
        }
        assertThat(reader.read(simpleMessagePacket)).isEqualToComparingFieldByField(simpleMessage);
        for (int i = 7; i < bigMessagePackets.size() - 1; ++i) {
            assertThat(reader.read(bigMessagePackets.get(i))).isNull();
        }
        assertThat(reader.read(bigMessagePackets.get(bigMessagePackets.size() - 1))).isEqualToComparingFieldByField(message);
    }

    @Test
    public void packetsInterleaved() {
        BiggestAllowedTestMessage message1 = prepareMessage(new BiggestAllowedTestMessage());
        List<ByteBuffer> message1Packets = writer.write(message1);

        BiggestAllowedTestMessage message2 = prepareMessage(new BiggestAllowedTestMessage());
        List<ByteBuffer> message2Packets = writer.write(message2);

        for (int i = 0; i < message1Packets.size() - 1; ++i) {
            assertThat(reader.read(message1Packets.get(i))).isNull();
            assertThat(reader.read(message2Packets.get(i))).isNull();
        }
        assertThat(reader.read(message1Packets.get(message1Packets.size() - 1))).isEqualToComparingFieldByField(message1);
        assertThat(reader.read(message2Packets.get(message2Packets.size() - 1))).isEqualToComparingFieldByField(message2);
    }

    @Test
    public void messageNumberRecycled() {
        BiggestAllowedTestMessage message1 = prepareMessage(new BiggestAllowedTestMessage());
        List<ByteBuffer> message1Packets = writer.write(message1);

        writer.setNextMessageNumber((byte)0); // Force same message number

        BigTestMessage message2 = prepareMessage(new BigTestMessage());
        List<ByteBuffer> message2Packets = writer.write(message2);

        // First message is read
        thenMessageIsReadCorrectly(message1Packets, message1);

        // ... some time may pass, then a message with the same message number is read
        thenMessageIsReadCorrectly(message2Packets, message2);
    }

    @Test
    public void messageNumberRecycled_previousMessageWasCorrupt() {
        BiggestAllowedTestMessage message1 = prepareMessage(new BiggestAllowedTestMessage());
        List<ByteBuffer> message1Packets = writer.write(message1);
        message1Packets.get(6).putInt(0, 0x1234); // destroy checksum in packet

        writer.setNextMessageNumber((byte)0); // Force same message number

        BigTestMessage message2 = prepareMessage(new BigTestMessage());
        List<ByteBuffer> message2Packets = writer.write(message2);

        // First message fails to be read
        givenCurrentTime(10000L);
        thenMessageCannotBeRead(message1Packets);

        // ... some time may pass, then a message with the same message number is read and MUST succeed!
        givenCurrentTime(10000L + protocol.getPacketTimeoutInMilliseconds());
        thenMessageIsReadCorrectly(message2Packets, message2);
    }

    private <T extends BitMessage> T prepareMessage(T message) {
        message.setMessageNumber(writer.getNextMessageNumber());
        return message;
    }

    private void givenCurrentTime(long millis) {
        reader.setCurrentTimeProvider(new CurrentTimeProviderStub(millis));
    }

    private void thenMessageIsReadCorrectly(List<ByteBuffer> packets, BitMessage expected) {
        for (int i = 0; i < packets.size() - 1; ++i) {
            assertThat(reader.read(packets.get(i))).isNull();
        }
        assertThat(reader.read(packets.get(packets.size() - 1))).isEqualToComparingFieldByField(expected);
    }

    private void thenMessageCannotBeRead(List<ByteBuffer> packets) {
        for (ByteBuffer packet : packets) {
            try {
                assertThat(reader.read(packet)).isNull();
            } catch (InvalidBitMessageException e) {
                // Ignore!
            }
        }
    }
}
