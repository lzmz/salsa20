package salsa20.salsa20;

public class Salsa20 {

    private final long MODULUS = 4294967295L;

    public int rotl(int value, int shift) {
        return (value << shift) | (value >>> (32 - shift));
    }

    public int[] quarterround(int y0, int y1, int y2, int y3) {
        int[] z = new int[4];

        z[1] = y1 ^ rotl((int) ((y0 + y3) & MODULUS), 7);
        z[2] = y2 ^ rotl((int) ((z[1] + y0) & MODULUS), 9);
        z[3] = y3 ^ rotl((int) ((z[2] + z[1]) & MODULUS), 13);
        z[0] = y0 ^ rotl((int) ((z[3] + z[2]) & MODULUS), 18);

        return z;
    }

    public int[] rowround(int[] y) {
        int[] z = new int[16];

        int[] quarterround = quarterround(y[0], y[1], y[2], y[3]);
        z[0] = quarterround[0];
        z[1] = quarterround[1];
        z[2] = quarterround[2];
        z[3] = quarterround[3];

        quarterround = quarterround(y[5], y[6], y[7], y[4]);
        z[5] = quarterround[0];
        z[6] = quarterround[1];
        z[7] = quarterround[2];
        z[4] = quarterround[3];

        quarterround = quarterround(y[10], y[11], y[8], y[9]);
        z[10] = quarterround[0];
        z[11] = quarterround[1];
        z[8] = quarterround[2];
        z[9] = quarterround[3];

        quarterround = quarterround(y[15], y[12], y[13], y[14]);
        z[15] = quarterround[0];
        z[12] = quarterround[1];
        z[13] = quarterround[2];
        z[14] = quarterround[3];

        return z;
    }

    public int[] columnround(int[] y) {
        int[] z = new int[16];

        int[] quarterround = quarterround(y[0], y[4], y[8], y[12]);
        z[0] = quarterround[0];
        z[4] = quarterround[1];
        z[8] = quarterround[2];
        z[12] = quarterround[3];

        quarterround = quarterround(y[5], y[9], y[13], y[1]);
        z[5] = quarterround[0];
        z[9] = quarterround[1];
        z[13] = quarterround[2];
        z[1] = quarterround[3];

        quarterround = quarterround(y[10], y[14], y[2], y[6]);
        z[10] = quarterround[0];
        z[14] = quarterround[1];
        z[2] = quarterround[2];
        z[6] = quarterround[3];

        quarterround = quarterround(y[15], y[3], y[7], y[11]);
        z[15] = quarterround[0];
        z[3] = quarterround[1];
        z[7] = quarterround[2];
        z[11] = quarterround[3];

        return z;
    }

    public int[] doubleround(int[] x) {
        return rowround(columnround(x));
    }

    public int littleendian(int b0, int b1, int b2, int b3) {
        return b0 + (b1 << 8) + (b2 << 16) + (b3 << 24);
    }

    public int[] invertLittleendian(int b) {
        int[] z = new int[4];

        z[0] = b & 0xFF;
        z[1] = b >>> 8 & 0xFF;
        z[2] = b >>> 16 & 0xFF;
        z[3] = b >>> 24 & 0xFF;

        return z;
    }

    public int[] salsa20Hash(int[] seq) {
        int[] x = new int[16];
        int[] w = new int[16];
        int[] hash = new int[64];

        for (int i = 0; i < 16; i++)
            x[i] = w[i] = littleendian(seq[4 * i], seq[4 * i + 1], seq[4 * i + 2], seq[4 * i + 3]);

        for (int i = 0; i < 10; i++)
            x = doubleround(x);

        int[] littleEndianInverted;
        for (int i = 0; i < 16; i++) {
            littleEndianInverted = invertLittleendian(x[i] + w[i]);
            hash[i * 4] = littleEndianInverted[0];
            hash[i * 4 + 1] = littleEndianInverted[1];
            hash[i * 4 + 2] = littleEndianInverted[2];
            hash[i * 4 + 3] = littleEndianInverted[3];
        }

        return hash;
    }

    public int[] salsa20Expand16(int[] k, int[] n) {
        int[] keystream = new int[64];

        int[][] tau = {
                { 101, 120, 112, 97 },
                { 110, 100, 32, 49 },
                { 54, 45, 98, 121 },
                { 116, 101, 32, 107 }
        };

        for (int i = 0; i < 64; i += 20)
            for (int j = 0; j < 4; j++)
                keystream[i + j] = tau[i / 20][j];

        for(int i = 0; i < 16; i++) {
            keystream[i + 4] = k[i];
            keystream[i + 24] = n[i];
            keystream[i + 44] = k[i];
        }

        return salsa20Hash(keystream);
    }

    public int[] salsa20Expand32(int[] k, int[] n) {
        int[] keystream = new int[64];

        int[][] sigma = {
                { 101, 120, 112, 97 },
                { 110, 100, 32, 51 },
                { 50, 45, 98, 121 },
                { 116, 101, 32, 107 }
        };

        for (int i = 0; i < 64; i += 20)
            for (int j = 0; j < 4; j++)
                keystream[i + j] = sigma[i / 20][j];

        for(int i = 0; i < 16; i++) {
            keystream[i + 4] = k[i];
            keystream[i + 24] = n[i];
            keystream[i + 44] = k[i + 16];
        }

        return salsa20Hash(keystream);
    }

    public int[] salsa20Encryption(int[] key, int[] nonce, int[] message, long streamIndex) {
        int[] n = new int[16];
        int[] keystream = new int[64];
        int[] encrypted = message.clone();
        int keyLength = key.length;

        if (keystream == null || key == null || nonce == null)
            return null;

        for(int i = 0; i < 8; i++)
            n[i] = nonce[i];

        if (Long.remainderUnsigned(streamIndex, 64) != 0) {
            getBlockNumber(Long.divideUnsigned(streamIndex, 64), n);

            if(keyLength == 16)
                keystream = salsa20Expand16(key, n);
            if(keyLength == 32)
                keystream = salsa20Expand32(key, n);
        }

        for(int i = 0; i < message.length; i++) {
            if(Long.remainderUnsigned(streamIndex + i, 64) == 0) {
                getBlockNumber(Long.divideUnsigned(streamIndex + i, 64), n);

                if(keyLength == 16)
                    keystream = salsa20Expand16(key, n);
                if(keyLength == 32)
                    keystream = salsa20Expand32(key, n);
            }

            encrypted[i] ^= keystream[(int) Long.remainderUnsigned(streamIndex + i, 64)];
        }

        return encrypted;
    }

    public void getBlockNumber(long b, int[] blockNumberArray) {
        blockNumberArray[8] = (int) (b & 0xFF);
        blockNumberArray[9] = (int) (b >>> 8 & 0xFF);
        blockNumberArray[10] = (int) (b >>> 16 & 0xFF);
        blockNumberArray[11] = (int) (b >>> 24 & 0xFF);
        blockNumberArray[12] = (int) (b >>> 32 & 0XFF);
        blockNumberArray[13] = (int) (b >>> 40 & 0XFF);
        blockNumberArray[14] = (int) (b >>> 48 & 0XFF);
        blockNumberArray[15] = (int) (b >>> 56 & 0XFF);
    }
}
