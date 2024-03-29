import java.security.*;
import java.util.Arrays;
import java.util.List;

public class PoisonedWineVis {

    private int numBottles;
    private int testStrips, testStrips_;
    private int testRounds, testRounds_;
    private int numPoison;
    private boolean[] bottles;
    private boolean failure = false;

    private void generateTestCase(long seed) {
        try {
            SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
            r.setSeed(seed);

            numBottles = r.nextInt(9951) + 50;
            testStrips = testStrips_ = r.nextInt(16) + 5;
            testRounds = testRounds_ = r.nextInt(10) + 1;
            numPoison = r.nextInt(numBottles / 50) + 1;

            bottles = new boolean[numBottles];
            int remain = numPoison;
            while (remain > 0) {
                int x = r.nextInt(numBottles);
                if (bottles[x]) continue;
                bottles[x] = true;
                remain--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPoison(List<Integer> list) {
        int x = 0;
        for (Integer b : list) {
            if (bottles[b]) ++x;
        }
        return x;
    }

    public int[] useTestStrips(String[] tests) {
        if (tests.length > testStrips) {
            debug("testWine() called with " + tests.length + " tests when only " + testStrips + " strips remain");
            failure = true;
            return new int[0];
        }
        if (testRounds <= 0) {
            debug("testWine() called too many times");
            failure = true;
            return new int[0];
        }
        int[] ret = new int[tests.length];
        for (int i = 0; i < tests.length; i++) {
            boolean poison = false;
            String[] s = tests[i].split(",");
            for (int j = 0; j < s.length; j++) {
                int x = -1;
                try {
                    x = Integer.parseInt(s[j]);
                } catch (Exception e) {
                    debug("Invalid value " + s[j] + " found in a test request");
                    failure = true;
                    return new int[0];
                }
                if (x < 0 || x >= numBottles) {
                    debug("Invalid value " + x + " found in a test request");
                    failure = true;
                    return new int[0];
                }
                poison |= bottles[x];
            }
            if (poison) {
                ret[i] = 1;
                testStrips--;
            }
        }
        testRounds--;
        return ret;
    }

    public double runTest(long seed) {
        generateTestCase(seed);
        PoisonTest.vis = this;
        int[] ret = new PoisonedWine().testWine(numBottles, testStrips, testRounds, numPoison);
        if (failure) {
            return 0;
        }
        for (int i = 0; i < ret.length; i++) {
            if (ret[i] < 0 || ret[i] >= numBottles) {
                throw new RuntimeException("Invalid return value: " + ret[i]);
            }
            bottles[ret[i]] = false;
        }
        for (int i = 0; i < bottles.length; i++) {
            if (bottles[i]) {
                throw new RuntimeException("A poisoned bottle remained. bottles = " + i);
            }
        }
        double pct = (double) (numBottles - ret.length) / (numBottles - numPoison);
        double score = pct * pct;
        debug(String.format("seed = %4d , numBottles = %4d , testStrips = %2d , testRounds = %2d , numPoison = %3d , ret = %4d , Score = %f", seed, numBottles, testStrips_, testRounds_, numPoison, ret.length, score));
        return score;
    }

    public double runTest_(long seed) {
        generateTestCase(seed);
        PoisonTest.vis = this;
        int[] ret = new PoisonedWineTest0().testWine(numBottles, testStrips, testRounds, numPoison);
        if (failure) {
            return 0;
        }
        for (int i = 0; i < ret.length; i++) {
            if (ret[i] < 0 || ret[i] >= numBottles) {
                throw new RuntimeException("Invalid return value: " + ret[i]);
            }
            bottles[ret[i]] = false;
        }
        for (int i = 0; i < bottles.length; i++) {
            if (bottles[i]) {
                throw new RuntimeException("A poisoned bottle remained. bottles = " + i);
            }
        }
        double pct = (double) (numBottles - ret.length) / (numBottles - numPoison);
        double score = pct * pct;
        debug(String.format("seed = %4d , numBottles = %4d , testStrips = %2d , testRounds = %2d , numPoison = %3d , ret = %4d , Score = %f", seed, numBottles, testStrips_, testRounds_, numPoison, ret.length, score));
        return score;
    }

    void execute() {
        long size = 20000;
        double s1 = 0, s2 = 0;
        for (long seed = 1, end = seed + size; seed < end; ++seed) {
            s1 += new PoisonedWineVis().runTest(seed);
            s2 += new PoisonedWineVis().runTest_(seed);
            long n = seed - end + size + 1;
            debug(s1 / n, s2 / n);
        }
        debug(String.format("average = %f", s1 / size));
    }

    public static void main(String[] args) {
        new PoisonedWineVis().execute();
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}
