package app.core;

import java.util.concurrent.atomic.AtomicLong;
import tools.HashTools;

public abstract class Identifiable {

    protected final AtomicLong ID = new AtomicLong(-1);

    public Identifiable() {
        setSessionID(generateTimeHashID());
    }

    public long getSessionID() {
        return ID.get();
    }

    public synchronized void setSessionID(long sessionID) {
        this.ID.set(sessionID);
    }

    //Static Generators

    @SuppressWarnings("UnstableApiUsage")
    protected static long generateLongHashID(long seed) {
        return HashTools.goodFastHash(seed);
    }

    protected static long generateTimeHashID() {
        return generateLongHashID(System.currentTimeMillis());
    }
}
