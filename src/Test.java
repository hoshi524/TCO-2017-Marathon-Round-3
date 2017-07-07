import java.util.*;

public class Test {

    public static final void main(String args[]) {
        new Test().solve();
    }

    void solve() {
        {
            long start = System.currentTimeMillis();
            debug(calcWide(1000, 8, 20, 3));
            debug("time", System.currentTimeMillis() - start);
        }
    }

    class WidePair {
        int wide;
        double expected;

        public String toString() {
            return "wide = " + wide + " , expected = " + expected;
        }
    }

    private static final int MAX_BOTTLES = 10000;
    private static final int MAX_STRIPS = 20;
    private static final int MAX_ROUNDS = 10;
    private WidePair[][][] memo = new WidePair[MAX_BOTTLES + 1][MAX_STRIPS + 1][MAX_ROUNDS + 1];

    WidePair calcWide(int bottles, int poison, int strips, int rounds) {
        if (memo[bottles][strips][rounds] != null)
            return memo[bottles][strips][rounds];
        WidePair result = new WidePair();
        if (rounds == 0 || strips == 0) return memo[bottles][strips][rounds] = result;
        int min = 1;
        int max = bottles / strips;
        if (max < 1) max = 1;
        if (max > 1500) max = 1500;
        while (max - min >= 3) {
            int d = (max - min) / 3;
            int r = min + d;
            int l = max - d;
            if (expected(bottles, poison, strips, rounds, r) < expected(bottles, poison, strips, rounds, l)) {
                min = r;
            } else {
                max = l;
            }
        }
        for (int wide = min; wide <= max; ++wide) {
            double e = expected(bottles, poison, strips, rounds, wide);
            if (result.expected < e) {
                result.expected = e;
                result.wide = wide;
            }
        }
        return memo[bottles][strips][rounds] = result;
    }

    double expected(int bottles, int poison, int strips, int rounds, int wide) {
        double x = 0;
        double[] probability = probability(bottles, poison, strips, wide);
        for (int badStrips = 0; badStrips < probability.length; ++badStrips) {
            if (probability[badStrips] < 1e-5) continue;
            int remove = removeValue(bottles, poison, strips, wide, badStrips);
            WidePair child = calcWide(bottles - remove, poison, strips - badStrips, rounds - 1);
            x += probability[badStrips] * (remove + child.expected);
        }
        return x;
    }

    int removeValue(int bottles, int poison, int strips, int wide, int bad) {
        int x = wide * (strips - bad);
        if (poison == bad) {
            x += bottles - wide * strips;
        }
        return x;
    }

    double[] probability(int bottles, int poison, int strips, int wide) {
        double dp[][][] = new double[strips + 1][strips + 1][poison + 1];
        dp[0][0][0] = 1;
        double p[][] = new double[poison + 1][poison + 1];
        for (int i = 0; i < strips; ++i) {
            for (int k = 0; k < poison + 1; ++k) {
                for (int m = k; m < poison + 1; ++m) {
                    int t = poison - k;
                    int s = wide - (m - k);
                    int f = m - k;
                    if (s < 0 || f < 0 || f > t) {
                        p[k][m] = 0;
                    } else {
                        p[k][m] = remove(bottles - i * wide, t, s, f);
                    }
                }
            }
            for (int j = 0; j <= i; ++j) {
                for (int k = 0; k < poison + 1; ++k) {
                    if (dp[i][j][k] < 1e-5) continue;
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

    private Map<Long, Double> removeMemo = new HashMap<>();

    double remove(int b, int p, int s, int f) {
        if (p == 0) return 1;
        long key = ((long) b << 27) | ((long) p << 19) | ((long) s << 8) | ((long) f);
        Double t = removeMemo.get(key);
        if (t != null) return t;
        double x = 1;
        if (s == 0) {
            for (int i = 0; i < f; ++i) {
                x *= p - i;
                x /= b - i;
            }
        } else if (f == 0) {
            for (int i = 0; i < s; ++i) {
                x *= b - p - i;
                x /= b - i;
            }
        } else {
            x = ((double) (b - p) / b) * remove(b - 1, p, s - 1, f) + ((double) p / b) * remove(b - 1, p - 1, s, f - 1);
        }
        removeMemo.put(key, x);
        return x;
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}
