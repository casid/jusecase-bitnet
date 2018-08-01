package org.jusecase.bitnet.message;

import org.junit.Before;
import org.junit.Test;
import org.jusecase.bitnet.checksum.MessageChecksumStub;
import org.jusecase.bitnet.messages.BiggestAllowedTestMessage;
import org.jusecase.bitnet.messages.EmptyTestMessage;
import org.jusecase.bitnet.messages.SimpleTestMessage;
import org.jusecase.bitnet.messages.TooBigTestMessage;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BitMessageTest {
    BitMessageProtocol protocol;
    BitMessageWriter writer;
    BitMessageReader reader;

    BitMessage message;
    List<ByteBuffer> packets;
    BitMessage messageReceived;

    @Before
    public void setUp() {
        protocol = new TestMessageProtocol();

        writer = new BitMessageWriter(protocol, 0x12340001);
        reader = new BitMessageReader(protocol, 0x12340001);
    }

    @Test
    public void emptyMessage() {
        givenMessage(new EmptyTestMessage());
        whenMessageIsWrittenAndRead();
        thenReadMessageIsCorrect();
    }

    @Test
    public void messageContainsMessageNumber() {
        givenMessage(new EmptyTestMessage());
        writer.setNextMessageNumber((byte) 48);

        whenMessageIsWrittenAndRead();

        assertThat(messageReceived.getMessageNumber()).isEqualTo((byte) 48);
    }

    @Test
    public void nextMessageNumberIsIncreased() {
        givenMessage(new EmptyTestMessage());
        writer.setNextMessageNumber((byte) 48);

        whenMessageIsWrittenAndRead();

        assertThat(writer.getNextMessageNumber()).isEqualTo((byte) 49);
    }

    @Test
    public void nextMessageNumberCycles() {
        givenMessage(new EmptyTestMessage());
        writer.setNextMessageNumber(Byte.MAX_VALUE);

        whenMessageIsWrittenAndRead();

        assertThat(writer.getNextMessageNumber()).isEqualTo(Byte.MIN_VALUE);
    }

    @Test
    public void singlePacketStartsAtZero() {
        givenMessage(new EmptyTestMessage());
        whenMessageIsWritten();
        assertThat(packets.get(0).position()).isEqualTo(0);
    }

    @Test
    public void multiPacketsStartAtZero() {
        givenMessage(new BiggestAllowedTestMessage());
        whenMessageIsWritten();
        for (ByteBuffer packet : packets) {
            assertThat(packet.position()).isEqualTo(0);
        }
    }

    @Test
    public void simpleMessage() {
        SimpleTestMessage message = new SimpleTestMessage();
        message.amountOfPlayers = 120;
        message.timeFrame = 13;
        givenMessage(message);

        whenMessageIsWrittenAndRead();

        thenReadMessageIsCorrect();
    }

    @Test
    public void clientIdMismatch() {
        writer = new BitMessageWriter(protocol, 0xcdcdcdce);
        reader = new BitMessageReader(protocol, 0xcdcdcdcd);
        givenMessage(new SimpleTestMessage());

        whenMessageIsWritten();

        assertThatThrownBy(this::whenMessageIsRead).isInstanceOf(InvalidBitMessageException.class);
    }

    @Test
    public void checksumMismatch() {
        writer.setMessageChecksum(new MessageChecksumStub(0xabababab));
        reader.setMessageChecksum(new MessageChecksumStub(0xabababac));
        givenMessage(new SimpleTestMessage());

        whenMessageIsWritten();

        assertThatThrownBy(this::whenMessageIsRead).isInstanceOf(InvalidBitMessageException.class);
    }

    @Test
    public void biggestAllowedMessage() {
        givenMessage(new BiggestAllowedTestMessage());
        whenMessageIsWrittenAndRead();
        thenReadMessageIsCorrect();
    }

    @Test
    public void biggestAllowedMessage_splitIntoChunks() {
        givenMessage(new BiggestAllowedTestMessage());
        whenMessageIsWrittenAndRead();
        assertThat(packets).hasSize(TestMessageProtocol.MAX_PACKETS_PER_MESSAGE + 3); // Slight overhead of packeting will result in additional packets with those mini test packets
    }

    @Test
    public void tooBigMessage() {
        givenMessage(new TooBigTestMessage());
        assertThatThrownBy(this::whenMessageIsWritten).isInstanceOf(InvalidBitMessageException.class);
    }

    private void givenMessage(BitMessage message) {
        this.message = message;
    }

    private void whenMessageIsWrittenAndRead() {
        whenMessageIsWritten();
        whenMessageIsRead();
    }

    private void whenMessageIsWritten() {
        packets = writer.write(message);
    }

    private void whenMessageIsRead() {
        for (ByteBuffer packet : packets) {
            messageReceived = reader.read(packet);
        }
    }

    private void thenReadMessageIsCorrect() {
        assertThat(messageReceived).isEqualToComparingFieldByField(message);
    }
}
