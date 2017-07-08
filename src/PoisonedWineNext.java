import java.util.*;
import java.util.stream.Collectors;

public class PoisonedWineNext {
    private XorShift random = new XorShift();
    private int bottles;
    private int strips;
    private int rounds;
    private int poison;

    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        {// init
            this.bottles = numBottles;
            this.strips = testStrips;
            this.rounds = testRounds;
            this.poison = numPoison;
        }
        return solve();
    }

    int[] solve() {
        List<TestRound> prevRounds = new ArrayList<>();
        for (int r = 0; r < rounds && strips > 0; ++r) {
            List<Integer> b = bottles(prevRounds, false);
            int size = b.size();
            int n = Math.max(Math.min((size / poison) * (r + 1) / rounds, (size / strips)), 1);
            TestRound round = new TestRound();
            for (int s = 0; s < strips; ++s) {
                Test test = new Test();
                for (int i = 0; i < n; ++i) {
                    if (b.isEmpty()) break;
                    test.bottles.add(b.remove(random.nextInt(b.size())));
                }
                if (test.bottles.size() > 0) round.tests.add(test);
            }
            round.execute();
            prevRounds.add(round);
        }
        return to(bottles(prevRounds, true));
    }

    List<Integer> bottles(List<TestRound> rounds, boolean last) {
        double prob[] = new double[bottles];
        for (TestRound round : rounds) {
            for (Test test : round.tests) {
                if (test.inPoison) {
                } else {
                    for (Integer b : test.bottles) {
                        prob[b] = -1;
                    }
                }
            }
        }
        List<Integer> remain = new ArrayList<>();
        for (int i = 0; i < bottles; ++i) {
            if (prob[i] >= 0) remain.add(i);
        }
        if (last) return remain;
        double average = (double) poison / remain.size();
        for (Integer b : remain) {
            prob[b] = average;
        }
        for (TestRound round : rounds) {
            for (Test test : round.tests) {
                if (test.inPoison) {
                    for (int i = 0; i < test.bottles.size(); ++i) {
                        if (prob[test.bottles.get(i)] < 0) {
                            test.bottles.remove(i);
                            --i;
                        }
                    }
                    double p = 1.0 / test.bottles.size();
                    if (average < p) {
                        for (Integer b : test.bottles) {
                            if (prob[b] < p) {
                                prob[b] = p;
                            }
                        }
                    }
                } else {
                }
            }
        }
        List<Integer> res = new ArrayList<>();
        for (Integer b : remain) {
            if (prob[b] > 0 && prob[b] < average * 1.2) {
                res.add(b);
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

        void execute() {
            int[] res = PoisonTest.useTestStrips(tests.stream().map(i -> i.query()).collect(Collectors.toList()).toArray(new String[0]));
            for (int i = 0; i < tests.size(); ++i) {
                if (res[i] == 1) {
                    tests.get(i).inPoison = true;
                    --strips;
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