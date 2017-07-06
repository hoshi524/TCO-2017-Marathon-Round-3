import java.security.*;

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
            bottles = new boolean[numBottles];
            testStrips = testStrips_ = r.nextInt(16) + 5;
            testRounds = testRounds_ = r.nextInt(10) + 1;
            numPoison = r.nextInt(numBottles / 50) + 1;
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

    public int[] useTestStrips(String[] tests) {
        if (tests.length > testStrips) {
            addFatalError("testWine() called with " + tests.length + " tests when only " + testStrips + " strips remain");
            failure = true;
            return new int[0];
        }
        if (testRounds <= 0) {
            addFatalError("testWine() called too many times");
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
                    addFatalError("Invalid value " + s[j] + " found in a test request");
                    failure = true;
                    return new int[0];
                }
                if (x < 0 || x >= numBottles) {
                    addFatalError("Invalid value " + x + " found in a test request");
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

    public double runTest(long seed, Solver solver) {
        try {
            generateTestCase(seed);
            PoisonTest.vis = this;
            int[] ret = solver.testWine(numBottles, testStrips, testRounds, numPoison);
            if (failure) {
                return 0;
            }
            for (int i = 0; i < ret.length; i++) {
                if (ret[i] < 0 || ret[i] >= numBottles) {
                    addFatalError("Invalid return value: " + ret[i]);
                    return 0;
                }
                bottles[ret[i]] = false;
            }
            for (int i = 0; i < bottles.length; i++) {
                if (bottles[i]) {
                    addFatalError("A poisoned bottle remained.");
                    return 0;
                }
            }
            double pct = (double) (numBottles - ret.length) / (numBottles - numPoison);
            double score = pct * pct;
            System.out.println(String.format("seed = %4d , numBottles = %4d , testStrips = %2d , testRounds = %2d , numPoison = %3d , ret = %4d , Score = %f", seed, numBottles, testStrips_, testRounds_, numPoison, ret.length, score));
            return score;
        } catch (Exception e) {
            System.err.println("An exception occurred while trying to get your program's results.");
            e.printStackTrace();
            return 0;
        }
    }

    interface Solver {
        int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison);
    }

    class Current implements Solver {
        public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
            return new PoisonedWine().testWine(numBottles, testStrips, testRounds, numPoison);
        }
    }

    class Next implements Solver {
        public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
            return new PoisonedWineNext().testWine(numBottles, testStrips, testRounds, numPoison);
        }
    }

    void execute() {
        long size = 5000;
        double sum1 = 0, sum2 = 0;
        for (long seed = 1, end = seed + size; seed < end; ++seed) {
            double s1 = new PoisonedWineVis().runTest(seed, new Current());
            double s2 = new PoisonedWineVis().runTest(seed, new Next());
            double max = Math.max(s1, s2);
            sum1 += s1 / max;
            sum2 += s2 / max;
            System.out.println();
        }
        System.out.println(String.format("sum1 = %f , sum2 = %f", sum1, sum2));
    }

    public static void main(String[] args) {
        new PoisonedWineVis().execute();
    }

    void addFatalError(String message) {
        System.out.println(message);
    }
}
