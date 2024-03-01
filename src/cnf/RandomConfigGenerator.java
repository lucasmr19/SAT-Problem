package cnf;

import java.util.Random;

public class RandomConfigGenerator {
    private static final Random random = new Random();

    public static int[] getRandomConfig(int n) {
        int[] vals = new int[n];
        for (int i = 0; i < n; i++) {
            vals[i] = random.nextBoolean() ? 1 : -1; // Only values 1 (True) or -1 (False) in the array
        }
        return vals;
    }
}

