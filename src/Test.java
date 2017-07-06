import java.util.*;

public class Test {

    public static final void main(String args[]) {
        new Test().solve();
    }

    void solve() {
        {
            long start = System.currentTimeMillis();
            debug(calcWide(1000, 20, 20, 1));
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
        if (max > 1000) max = 1000;
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
            if (probability[badStrips] < 1e-4) continue;
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
        int bad = Math.min(poison, wide);
        double dp[][][] = new double[strips + 1][strips + 1][bad + 1];
        dp[0][0][0] = 1;
        for (int i = 0; i < strips; ++i) {
            for (int j = 0; j < strips; ++j) {
                for (int k = 0; k < bad + 1; ++k) {
                    if (dp[i][j][k] < 1e-4) continue;
                    for (int m = k; m < bad + 1; ++m) {
                        dp[i + 1][j + (m == k ? 0 : 1)][m] += dp[i][j][k] * remove(bottles - i * wide, poison - k, wide - (m - k), m - k);
                    }
                }
            }
        }
        double[] sum = new double[strips + 1];
        for (int i = 0; i < strips + 1; ++i) {
            for (int j = 0; j < bad + 1; ++j) {
                sum[i] += dp[strips][i][j];
            }
        }
        return sum;
    }

    private Map<Long, Double> removeMemo = new HashMap<>();

    double remove(int b, int p, int s, int f) {
        if (p == 0) return 1;
        long key = ((long) b << 26) | ((long) p << 18) | ((long) s << 8) | ((long) f);
        Double t = removeMemo.get(key);
        if (t != null) return t;
        if (s == 0) {
            double x = 1;
            for (int i = 0; i < f; ++i) {
                x *= p - i;
                x /= b - i;
            }
            return x;
        }
        if (f == 0) {
            double x = 1;
            for (int i = 0; i < s; ++i) {
                x *= b - p - i;
                x /= b - i;
            }
            return x;
        }
        double x = ((double) (b - p) / b) * remove(b - 1, p, s - 1, f) + ((double) p / b) * remove(b - 1, p - 1, s, f - 1);
        removeMemo.put(key, x);
        return x;
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}
