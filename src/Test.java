import java.util.*;

public class Test {

    public static final void main(String args[]) {
        new Test().solve();
    }

    void solve() {
        class XorShift {
            int x = 123456789;
            int y = 362436069;
            int z = 521288629;
            int w = (int) System.currentTimeMillis();

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
        XorShift random = new XorShift();
        MySQL database = new MySQL();
        while (true) {
            int poison = random.nextInt(20) + 1;
            int bottles = random.nextInt(1000) + poison;
            int strips = random.nextInt(20) + 1;
            int rounds = random.nextInt(10) + 1;
            long start = System.currentTimeMillis();
            Opt.QuerySize querySize = new Opt().solve(bottles, poison, strips, rounds);
            long time = System.currentTimeMillis() - start;
            debug("bottles", bottles, "poison", poison, "strips", strips, "rounds", rounds, querySize, "time", time);
            database.insert(bottles, poison, strips, rounds, querySize.size, querySize.expect, (int) time);
        }
    }

    class Opt {
        class QuerySize {
            int size;
            double expect;

            public String toString() {
                return String.format("size = %d, expect = %f", size, expect);
            }
        }

        private static final int MAX_BOTTLES = 10000;
        private static final int MAX_STRIPS = 20;
        private static final int MAX_ROUNDS = 10;
        private QuerySize[][][] memo;

        QuerySize solve(int bottles, int poison, int strips, int rounds) {
            memo = new QuerySize[MAX_BOTTLES + 1][MAX_STRIPS + 1][MAX_ROUNDS + 1];
            return solve(bottles, bottles, poison, strips, rounds);
        }

        private QuerySize solve(int bottles, int remain, int poison, int strips, int rounds) {
            if (memo[remain][strips][rounds] != null)
                return memo[remain][strips][rounds];
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
                if (expect(bottles, remain, poison, strips, rounds, r) < expect(bottles, remain, poison, strips, rounds, l)) {
                    min = r;
                } else {
                    max = l;
                }
                // debug(max, min);
            }
            for (int size = min; size <= max; ++size) {
                double e = expect(bottles, remain, poison, strips, rounds, size);
                if (result.expect < e) {
                    result.expect = e;
                    result.size = size;
                }
            }
            return memo[remain][strips][rounds] = result;
        }

        double expect(int bottles, int remain, int poison, int strips, int rounds, int size) {
            double expect = 0;
            double[] probability = probability(remain, poison, Math.min(remain, strips), size);
            for (int badStrips = 0; badStrips < probability.length; ++badStrips) {
                if (probability[badStrips] < 1e-4 || Double.isNaN(probability[badStrips])) continue;
                int remove = size * (strips - badStrips) + (poison == badStrips ? remain - size * strips : 0);
                expect += probability[badStrips] * solve(bottles, remain - remove, poison, strips - badStrips, rounds - 1).expect;
            }
            return expect;
        }

        double[] probability(int bottles, int poison, int strips, int size) {
            double dp[][][] = new double[strips + 1][strips + 1][poison + 1];
            dp[0][0][0] = 1;
            double p[][] = new double[poison + 1][poison + 1];
            for (int i = 0; i < strips; ++i) {
                {
                    int b = bottles - i * size;
                    for (int k = 0; k < poison + 1; ++k) {
                        int t = poison - k;
                        for (int m = k; m < poison + 1; ++m) {
                            int f = m - k;
                            int s = size - f;
                            if (s < 0 || f < 0 || f > t) {
                                p[k][m] = 0;
                            } else {
                                p[k][m] = remove(b, t, s, f);
                            }

                        }
                    }
                }
                for (int j = 0; j <= i; ++j) {
                    for (int k = 0; k < poison + 1; ++k) {
                        for (int m = k; m < poison + 1; ++m) {
                            dp[i + 1][j + (m == k ? 0 : 1)][m] += dp[i][j][k] * p[k][m];
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

        private Map<Long, Double> removeMemo = new TreeMap<>();

        double remove(int b, int p, int s, int f) {
            long key = ((long) b << 27) | ((long) p << 19) | ((long) s << 8) | ((long) f);
            Double t = removeMemo.get(key);
            if (t != null) return t;
            double x = 1;
            if (s == 0) {
                for (int i = 0; i < f; ++i) {
                    x = x * (p - i) / (b - i);
                }
            } else if (f == 0) {
                for (int i = 0; i < s; ++i) {
                    x = x * (b - p - i) / (b - i);
                }
            } else {
                x = ((double) (b - p) / b) * remove(b - 1, p, s - 1, f) + ((double) p / b) * remove(b - 1, p - 1, s, f - 1);
            }
            removeMemo.put(key, x);
            return x;
        }

        private void debug(Object... o) {
            System.out.println(Arrays.deepToString(o));
        }
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}
