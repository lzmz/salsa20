package salsa20Test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import salsa20.salsa20.Salsa20;
import salsa20.salsa20.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class VectorTest {

    @Test
    public void vector128Test() throws IOException {
        Salsa20 salsa20 = new Salsa20();

        String path = Utils.testCasesGenerator("src/test/resources/test_vectors.128.txt", true);

        File folder = new File(path);
        File[] testsCases = folder.listFiles();

        int[] message = new int[512];

        runTestCases(salsa20, testsCases, message);
    }

    @Test
    public void vector256Test() throws IOException {
        Salsa20 salsa20 = new Salsa20();

        String path = Utils.testCasesGenerator("src/test/resources/test_vectors.256.txt", false);

        File folder = new File(path);
        File[] testsCases = folder.listFiles();

        int[] message = new int[512];

        runTestCases(salsa20, testsCases, message);
    }

    private void runTestCases(Salsa20 salsa20, File[] testsCases, int[] message) throws IOException {
        int[] key;
        int[] nonce;
        int[] stream0;
        int[] stream1;
        int[] stream2;
        int[] stream3;
        int[] streamXor;
        int[] crypt;
        int[] streamIndex;

        for (int i = 0; i < testsCases.length; i++) {
            ArrayList<ArrayList<Integer>> data = Utils.parseTestCase(testsCases[i]);
            key = Utils.toPrimitive(data.get(0));
            nonce = Utils.toPrimitive(data.get(1));
            stream0 = Utils.toPrimitive(data.get(2));
            stream1 = Utils.toPrimitive(data.get(3));
            stream2 = Utils.toPrimitive(data.get(4));
            stream3 = Utils.toPrimitive(data.get(5));
            streamXor = Utils.toPrimitive(data.get(6));
            streamIndex = Utils.toPrimitive(data.get(7));

            crypt = salsa20.salsa20Encryption(key, nonce, message, streamIndex[0] - 0);
            Assertions.assertArrayEquals(stream0, Arrays.copyOfRange(crypt, 0, 64));

            crypt = salsa20.salsa20Encryption(key, nonce, message, streamIndex[1] - 192);
            Assertions.assertArrayEquals(stream1, Arrays.copyOfRange(crypt, 192, 256));

            crypt = salsa20.salsa20Encryption(key, nonce, message, streamIndex[2]- 256);
            Assertions.assertArrayEquals(stream2, Arrays.copyOfRange(crypt, 256, 320));

            crypt = salsa20.salsa20Encryption(key, nonce, message, streamIndex[3] - 448);
            Assertions.assertArrayEquals(stream3, Arrays.copyOfRange(crypt, 448, 512));
        }
    }
}
