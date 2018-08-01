package org.jusecase.bitnet.time;

public class CurrentTimeProviderStub extends CurrentTimeProvider {
    private final long fixedTime;

    public CurrentTimeProviderStub(long fixedTime) {
        this.fixedTime = fixedTime;
    }

    @Override
    public long getMillis() {
        return fixedTime;
    }
}