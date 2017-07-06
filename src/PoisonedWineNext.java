import java.util.*;
import java.util.stream.Collectors;

public class PoisonedWineNext {
    private XorShift random = new XorShift();
    private int numBottles;
    private int testStrips;
    private int testRounds;
    private int numPoison;
    private boolean[] ok;

    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        {// init
            this.numBottles = numBottles;
            this.testStrips = testStrips;
            this.testRounds = testRounds;
            this.numPoison = numPoison;
            this.ok = new boolean[numBottles];
        }
        return solve();
    }

    int[] solve() {
        for (int r = 0; r < testRounds && testStrips > 0; ++r) {
            List<Integer> bottles = remainBottles();
            int size = bottles.size();
            int n = Math.max(Math.min((int) (size / numPoison) * (r + 1) / testRounds, (int) (size / testStrips)), 1);
            // debug(r, size, n, testStrips);
            TestRound round = new TestRound();
            for (int s = 0; s < testStrips; ++s) {
                Test test = new Test();
                for (int i = 0; i < n; ++i) {
                    if (bottles.isEmpty()) break;
                    int x = random.nextInt(bottles.size());
                    test.bottles.add(bottles.get(x));
                    bottles.remove(x);
                }
                if (test.bottles.size() > 0) round.tests.add(test);
            }
            round.execute();
        }
        return to(remainBottles());
    }

    List<Integer> remainBottles() {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < numBottles; ++i) {
            if (ok[i] == false) {
                res.add(i);
            }
        }
        return res;
    }

    int[] to(List<Integer> list) {
        int x[] = new int[list.size()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = list.get(i);
        }
        return x;
    }

    class TestRound {
        List<Test> tests = new ArrayList<>();
        private boolean executed = false;

        void execute() {
            if (executed) throw new RuntimeException();
            executed = true;
            int[] res = PoisonTest.useTestStrips(tests.stream().map(i -> i.query()).collect(Collectors.toList()).toArray(new String[0]));
            int bad = 0;
            for (int i = 0; i < tests.size(); ++i) {
                if (res[i] == 1) {
                    tests.get(i).inPoison = true;
                    --testStrips;
                    ++bad;
                } else {
                    for (Integer b : tests.get(i).bottles) {
                        ok[b] = true;
                    }
                }
            }
            if (bad == numPoison) {
                boolean ex[] = new boolean[numBottles];
                for (Test test : tests) {
                    for (Integer b : test.bottles) {
                        ex[b] = true;
                    }
                }
                for (int b = 0; b < numBottles; ++b) {
                    if (ex[b] == false) {
                        ok[b] = true;
                    }
                }
            }
        }
    }

    class Test {
        List<Integer> bottles = new ArrayList<>();
        boolean inPoison = false;

        String query() {
            return String.join(",", bottles.stream().map(i -> i.toString()).collect(Collectors.toList()).toArray(new String[0]));
        }
    }

    private final class XorShift {
        int x = 123456789;
        int y = 362436069;
        int z = 521288629;
        int w = 88675123;

        int nextInt(int n) {
            final int t = x ^ (x << 11);
            x = y;
            y = z;
            z = w;
            w = (w ^ (w >>> 19)) ^ (t ^ (t >>> 8));
            final int r = w % n;
            return r < 0 ? r + n : r;
        }

        int nextInt() {
            final int t = x ^ (x << 11);
            x = y;
            y = z;
            z = w;
            return w = (w ^ (w >>> 19)) ^ (t ^ (t >>> 8));
        }
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}