package ru.spbau.mit.oquechy.stress.types;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

public class Trinket {
    public Trinket(int queries, Trinket writingTrinket) {
        this.queries = queries;
        this.writingTrinket = writingTrinket;
    }

    private int queries;
    private final Trinket writingTrinket;

    public void setWritingTrinket(int serializedSize, ByteBuffer messageBuffer) {
        messageBuffer.flip();
        synchronized (writingTrinket) {
            writingTrinket.getSizeBuffer().putInt(serializedSize);
            writingTrinket.getSizeBuffer().flip();
            writingTrinket.setMessageBuffer(messageBuffer);
        }
    }

    public void queryDone() {
        queries--;
    }

    public boolean isFinished() {
        return queries == 0;
    }

    @Getter
    @Setter
    private ByteBuffer messageBuffer = null;

    @Getter
    private ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
}
