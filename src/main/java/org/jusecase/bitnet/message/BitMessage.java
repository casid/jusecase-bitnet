package org.jusecase.bitnet.message;

public abstract class BitMessage {
    private byte messageNumber;

    public byte getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(byte messageNumber) {
        this.messageNumber = messageNumber;
    }
}
