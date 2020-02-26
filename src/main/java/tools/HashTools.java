package tools;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

@SuppressWarnings("UnstableApiUsage")
public class HashTools {

    public static String getSha256(String string) {
        HashFunction hasher = Hashing.sha256();
        HashCode sha256 = hasher.newHasher()
                                .putUnencodedChars(string)
                                .hash();
        return sha256.toString();
    }

    public static long goodFastHash(long seed) {
        HashFunction hasher = Hashing.goodFastHash(Long.SIZE);
        HashCode idHash = hasher.newHasher()
                                .putLong(seed)
                                .hash();
        return idHash.asLong();
    }
}
