import java.util.*;
import java.util.stream.Collectors;

public class PoisonedWineNext {
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
        List<Test> tests = new ArrayList<>();
        for (int r = 0; r < rounds && strips > 0; ++r) {
            State state = bottles(tests, false);
            List<Integer> b = state.bottles;
            int size = b.size();
            int poison = state.poison;
            int n = Math.max((int) Math.min(1. * size / poison * (r + 1) / rounds, 1. * size / strips), 1);
            TestRound round = new TestRound();
            for (int s = 0; s < strips; ++s) {
                Test test = new Test();
                for (int i = 0; i < n; ++i) {
                    if (b.isEmpty()) break;
                    test.bottles.add(b.remove(random.nextInt(b.size())));
                }
                if (test.bottles.size() > 0) {
                    Collections.sort(test.bottles);
                    round.tests.add(test);
                }
            }
            round.execute();
            tests.addAll(round.tests);
        }
        return to(bottles(tests, true).bottles);
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
            for (int j = i + 1; j < tests.size(); ++j) {
                if (t.equals(tests.get(j))) {
                    tests.remove(j);
                    --j;
                }
            }
        }
        List<Integer> remain = new ArrayList<>();
        for (int i = 0; i < bottles; ++i) {
            if (!safe[i]) remain.add(i);
        }
        double prob[] = new double[bottles];
        double average = (double) this.poison / remain.size();
        for (Integer b : remain) {
            prob[b] = average;
        }
        if (tests.size() > 0) {
            class Node {
                Test test;
                List<Node> link = new ArrayList<>();
            }
            List<Node> exclusion = new ArrayList<>();
            for (Test test : tests) {
                Node n = new Node();
                n.test = test;
                exclusion.add(n);
                double p = 1.0 / test.bottles.size();
                if (average < p) {
                    for (Integer b : test.bottles) {
                        if (prob[b] < p) {
                            prob[b] = p;
                        }
                    }
                }
            }
            if (exclusion.size() >= poison) {
                for (int i = 0; i < exclusion.size(); ++i) {
                    Node ni = exclusion.get(i);
                    for (int j = i; j < exclusion.size(); ++j) {
                        Node nj = exclusion.get(j);
                        add:
                        for (Integer bi : ni.test.bottles) {
                            for (Integer bj : nj.test.bottles) {
                                if (bi.equals(bj)) {
                                    ni.link.add(nj);
                                    nj.link.add(ni);
                                    break add;
                                }
                            }
                        }
                    }
                }
                boolean recursion = false;
                for (int i = 0; i < 100; ++i) {
                    List<Node> x = new ArrayList<>(exclusion);
                    List<Test> select = new ArrayList<>();
                    while (x.size() > 0 && select.size() < poison) {
                        Node n = x.remove(random.nextInt(x.size()));
                        select.add(n.test);
                        x.removeAll(n.link);
                    }
                    if (select.size() == poison) {
                        Set<Integer> set = new HashSet<>();
                        for (Test t : select) {
                            set.addAll(t.bottles);
                        }
                        for (Integer b : remain) {
                            if (!set.contains(b) && safe[b] == false) {
                                safe[b] = true;
                                recursion = true;
                            }
                        }
                    }
                }
                if (recursion) return bottles(tests, last);
            }
        }
        if (last) return new State(remain, 0);
        List<Integer> res = new ArrayList<>();
        for (Integer b : remain) {
            if (prob[b] < average + 1e-5) {
                res.add(b);
            }
        }
        return new State(res, poison);
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

        boolean equals(Test t) {
            if (inPoison != t.inPoison) return false;
            if (bottles.size() != t.bottles.size()) return false;
            for (int i = 0; i < bottles.size(); ++i) {
                if (!bottles.get(i).equals(t.bottles.get(i))) return false;
            }
            return true;
        }
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