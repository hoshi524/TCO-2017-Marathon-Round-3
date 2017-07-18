import java.util.*;
import java.util.stream.Collectors;

public class PoisonedWineTest0 {
    private XorShift random = new XorShift();
    private int bottles;
    private int strips;
    private int rounds;
    private int poison;
    private boolean[] safe;

    public int[] testWine(int numBottles, int testStrips, int testRounds, int numPoison) {
        {// init
            this.bottles = numBottles;
            this.strips = testStrips;
            this.rounds = testRounds;
            this.poison = numPoison;
            this.safe = new boolean[numBottles];
        }
        return solve();
    }

    int[] solve() {
        Optimize optimize = new Optimize();
        List<Test> tests = new ArrayList<>();
        for (int r = 0; r < rounds && strips > 0; ++r) {
            State state = bottles(tests, false);
            List<Integer> b = state.bottles;
            int size = b.size();
            int poison = state.poison;
            int n;
            if (rounds - r < 2) {
                n = optimize.solve(size, poison, strips, rounds - r).size;
            } else {
                n = Math.max((int) Math.min(1. * size / this.poison * (r + 1) / rounds, 1. * size / strips), 1);
            }
            TestRound round = new TestRound();
            for (int s = 0; s < strips; ++s) {
                Test test = new Test();
                for (int i = 0; i < n; ++i) {
                    if (b.isEmpty()) break;
                    test.bottles.add(b.remove(random.nextInt(b.size())));
                }
                if (test.bottles.size() > 0) {
                    round.tests.add(test);
                }
            }
            int bad = round.execute();
            tests.addAll(round.tests);
            // int s = getSafe();
            // debug("rounds", rounds - r, rounds, this.bottles, this.poison, (double) this.bottles / this.poison, "strips", this.strips, "bottles", size, this.bottles - s, "wide", n, "safe", s, (double) s / this.bottles, "bad", bad, round.tests.size());
        }
        return to(bottles(tests, true).bottles);
    }

    class QuerySize {
        int size = 1;
        double expect;

        public String toString() {
            return String.format("size = %d, expect = %f", size, expect);
        }
    }

    class Optimize {

        private static final int MAX_BOTTLES = 10000;
        private static final int MAX_STRIPS = 20;
        private static final int MAX_ROUNDS = 10;
        private int bottles;
        private int poison;
        private QuerySize[][][] memo;
        private double[][] comb = combination(MAX_BOTTLES + 1, 1501);

        QuerySize solve(int bottles, int poison, int strips, int rounds) {
            this.bottles = bottles;
            this.poison = poison;
            this.memo = new QuerySize[MAX_BOTTLES + 1][MAX_STRIPS + 1][MAX_ROUNDS + 1];
            return solve(bottles, strips, rounds);
        }

        private QuerySize solve(int remain, int strips, int rounds) {
            if (memo[remain][strips][rounds] != null) {
                return memo[remain][strips][rounds];
            }
            QuerySize result = new QuerySize();
            if (remain == poison || rounds == 0 || strips == 0) {
                double expect = (double) (bottles - remain) / (bottles - poison);
                result.expect = expect * expect;
                return memo[remain][strips][rounds] = result;
            }
            int min = 1;
            int max = remain / strips;
            if (max < 1) max = 1;
            if (max > 1500) max = 1500;
            while (max - min >= 3) {
                int d = (max - min) / 3;
                int r = min + d;
                int l = max - d;
                if (expect(remain, strips, rounds, r) < expect(remain, strips, rounds, l)) {
                    min = r;
                } else {
                    max = l;
                }
            }
            for (int size = min; size <= max; ++size) {
                double e = expect(remain, strips, rounds, size);
                if (result.expect < e) {
                    result.expect = e;
                    result.size = size;
                }
            }
            return memo[remain][strips][rounds] = result;
        }

        double expect(int remain, int strips, int rounds, int size) {
            double expect = 0;
            int r = remain;
            int s = size;
            double[] prob;
            while (true) {
                prob = probability(r, poison, Math.min(r, strips), s);
                if (!isNaN(prob)) break;
                r /= 2;
                s /= 2;
            }
            for (int bad = 0; bad < prob.length; ++bad) {
                if (prob[bad] < 1e-4) continue;
                int remove = size * (strips - bad) + (poison == bad ? remain - size * strips : 0);
                expect += prob[bad] * solve(Math.max(remain - remove, poison), strips - bad, rounds - 1).expect;
            }
            return expect;
        }

        boolean isNaN(double[] x) {
            for (double d : x) if (Double.isNaN(d)) return true;
            return false;
        }

        double[] probability(int bottles, int poison, int strips, int size) {
            double dp[][][] = new double[strips + 1][strips + 1][poison + 1];
            dp[0][0][0] = 1;
            for (int i = 0; i < strips; ++i) {
                int b = bottles - i * size;
                for (int k = 0; k < poison + 1; ++k) {
                    int p = poison - k;
                    for (int m = k; m < poison + 1; ++m) {
                        int f = m - k;
                        int s = size - f;
                        if (!(s < 0 || p < f || b - p < s || b < s + f)) {
                            double prob = comb[b - p][s] * comb[p][f] / comb[b][s + f];
                            for (int j = 0; j <= i; ++j) {
                                dp[i + 1][j + (m == k ? 0 : 1)][m] += dp[i][j][k] * prob;
                            }
                        }
                    }
                }
            }
            double[] sum = new double[strips + 1];
            for (int i = 0; i < strips + 1; ++i) {
                for (int j = 0; j < poison + 1; ++j) {
                    sum[i] += dp[strips][i][j];
                }
            }
            return sum;
        }
    }

    int getSafe() {
        int x = 0;
        for (int i = 0; i < bottles; ++i) if (safe[i]) ++x;
        return x;
    }

    class State {
        List<Integer> bottles;
        int poison;

        State(List<Integer> bottles, int poison) {
            this.bottles = bottles;
            this.poison = poison;
        }
    }

    State bottles(List<Test> tests, boolean last) {
        for (int i = 0; i < tests.size(); ++i) {
            Test t = tests.get(i);
            if (!t.inPoison) {
                for (Integer b : t.bottles) {
                    safe[b] = true;
                }
                tests.remove(i);
                --i;
            }
        }
        for (int i = 0; i < tests.size(); ++i) {
            Test t = tests.get(i);
            for (int j = 0; j < t.bottles.size(); ++j) {
                if (safe[t.bottles.get(j)]) {
                    t.bottles.remove(j);
                    --j;
                }
            }
        }
        for (int i = 0; i < tests.size(); ++i) {
            Test t = tests.get(i);
            for (int j = 0; j < tests.size(); ++j) {
                if (tests.get(j).subset(t)) {
                    tests.remove(j);
                    --j;
                }
            }
        }
        if (tests.size() >= poison && new TestInfo(tests).safeBottles.size() > 0) return bottles(tests, last);

        List<Integer> remain = new ArrayList<>();
        for (int i = 0; i < bottles; ++i) {
            if (!safe[i]) remain.add(i);
        }
        if (last) return new State(remain, 0);

        double prob[] = new double[bottles];
        double average = (double) this.poison / remain.size();
        for (Integer b : remain) {
            prob[b] = average;
        }
        List<Test> exclusion = new ArrayList<>();
        for (Test test : tests) {
            double p = 1.0 / test.bottles.size();
            if (average < p) {
                exclusion.add(test);
                for (Integer b : test.bottles) {
                    if (prob[b] < p) {
                        prob[b] = p;
                    }
                }
            }
        }
        List<Integer> res = new ArrayList<>();
        for (Integer b : remain) {
            if (prob[b] < average + 1e-5) {
                res.add(b);
            }
        }
        return new State(res, poison - new TestInfo(exclusion).minPoison);
    }

    class TestInfo {
        int minPoison;
        Set<Integer> safeBottles;

        TestInfo(List<Test> tests) {
            minPoison = 0;
            safeBottles = new HashSet<>();
            if (tests.isEmpty()) return;
            class Node {
                Test test;
                List<Node> link = new ArrayList<>();
            }
            List<Node> list = new ArrayList<>();
            for (Test test : tests) {
                Node n = new Node();
                n.test = test;
                list.add(n);
            }
            for (int i = 0; i < list.size(); ++i) {
                Node ni = list.get(i);
                for (int j = i; j < list.size(); ++j) {
                    Node nj = list.get(j);
                    for (Integer bj : nj.test.bottles) {
                        if (ni.test.bottles.contains(bj)) {
                            ni.link.add(nj);
                            nj.link.add(ni);
                            break;
                        }
                    }
                }
            }
            for (int i = 0; i < 100; ++i) {
                List<Node> x = new ArrayList<>(list);
                List<Test> select = new ArrayList<>();
                while (x.size() > 0 && select.size() < poison) {
                    Node n = x.remove(random.nextInt(x.size()));
                    select.add(n.test);
                    x.removeAll(n.link);
                }
                if (minPoison < select.size()) minPoison = select.size();
                if (select.size() == poison) {
                    Set<Integer> set = new HashSet<>();
                    for (Test t : select) {
                        set.addAll(t.bottles);
                    }
                    for (int b = 0; b < bottles; ++b) {
                        if (!set.contains(b) && safe[b] == false) {
                            safe[b] = true;
                            safeBottles.add(b);
                        }
                    }
                }
            }
        }
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

        int execute() {
            int bad = 0;
            int[] res = PoisonTest.useTestStrips(tests.stream().map(i -> i.query()).collect(Collectors.toList()).toArray(new String[0]));
            for (int i = 0; i < tests.size(); ++i) {
                if (res[i] == 1) {
                    tests.get(i).inPoison = true;
                    ++bad;
                }
            }
            strips -= bad;
            return bad;
        }
    }

    class Test {
        List<Integer> bottles = new ArrayList<>();
        boolean inPoison = false;

        String query() {
            return String.join(",", bottles.stream().map(i -> i.toString()).collect(Collectors.toList()).toArray(new String[0]));
        }

        boolean subset(Test t) {
            if (this == t) return false;
            return bottles.containsAll(t.bottles);
        }
    }

    double[][] combination(int n, int m) {
        double comb[][] = new double[n][m];
        for (int i = 0; i < n; i++) {
            comb[i][0] = 1;
            if (i != 0) {
                for (int j = 1; j < m; j++) {
                    comb[i][j] = (comb[i - 1][j - 1] + comb[i - 1][j]);
                }
            }
        }
        return comb;
    }

    private class XorShift {
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
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}