import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

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
        while (true) {
            int bottles = random.nextInt(9000) + 1000;
            int poison = random.nextInt(100) + 100;
            int strips = random.nextInt(10) + 10;
            int rounds = 2;
            {
                long start = System.currentTimeMillis();
                QuerySize querySize = new Optimize().solve(bottles, poison, strips, rounds);
                long time = System.currentTimeMillis() - start;
                debug("bottles", bottles, "poison", poison, "strips", strips, "rounds", rounds, querySize, "time", time);
            }
        }
    }

    double[][] combination(int n, int m) {
        double comb[][] = new double[n][m];
        for (int i = 0; i < n; i++) {
            comb[i][0] = 1;
            if (i != 0) {
                for (int j = 1; j < m; j++) {
                    comb[i][j] = comb[i - 1][j - 1] + comb[i - 1][j];
                }
            }
        }
        return comb;
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

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }
}
