package app.core;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.concurrent.atomic.AtomicLong;

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
        HashFunction hasher = Hashing.goodFastHash(Long.SIZE);
        HashCode idHash = hasher.newHasher()
                                .putLong(seed)
                                .hash();
        return idHash.asLong();

    }

    protected static long generateTimeHashID() {
        return generateLongHashID(System.currentTimeMillis());
    }
}
