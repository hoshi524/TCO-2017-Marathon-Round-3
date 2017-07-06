import java.util.*;

public class Test {

    private XorShift random = new XorShift();

    void solve() {
        debug(0, remove(100, 2, 25, 0));
        debug(1, remove(100, 2, 24, 1));
        debug(2, remove(100, 2, 23, 2));
        new AAA(100, 2).probability(25);
        probability(100, 2, 2, 25);
    }

    class AAA {
        boolean bottles[];
        int poison;

        AAA(int n, int p) {
            poison = p;
            bottles = new boolean[n];
            List<Integer> remain = indexList();
            for (int i = 0; i < p; ++i) {
                bottles[remain.remove(random.nextInt(remain.size()))] = true;
            }
        }

        List<Integer> indexList() {
            List<Integer> x = new ArrayList<>();
            for (int i = 0; i < bottles.length; ++i) {
                x.add(i);
            }
            return x;
        }

        int test(int n) {
            int x = 0;
            List<Integer> remain = indexList();
            for (int i = 0; i < n; ++i) {
                if (bottles[remain.remove(random.nextInt(remain.size()))]) {
                    ++x;
                }
            }
            return x;
        }

        void probability(int wide) {
            int testcase = 0xffffff;
            int count[] = new int[poison + 1];
            for (int i = 0; i < testcase; ++i) {
                ++count[test(wide)];
            }
            for (int i = 0; i < count.length; ++i) {
                debug(i, (double) count[i] / testcase);
            }
        }
    }

    double[] probability(int bottles, int poison, int strips, int wide) {
        double dp[][][] = new double[strips + 1][strips + 1][poison + 1];
        dp[0][0][0] = 1;
        for (int i = 0; i < strips; ++i) {
            for (int j = 0; j < strips + 1; ++j) {
                for (int k = 0; k < poison + 1; ++k) {
                    if (dp[i][j][k] < 1e-5) continue;
                    for (int m = k; m < poison + 1; ++m) {
                        if (m == k) {
                            dp[i + 1][j][m] += dp[i][j][k] * remove(bottles - i * wide, poison - k, wide, 0);
                        } else {
                            dp[i + 1][j + 1][m] += dp[i][j][k] * remove(bottles - i * wide, poison - k, wide - (m - k), m - k);
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
            debug(i, sum[i]);
        }
        return sum;
    }

    double remove(int b, int p, int s, int f) {
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
        return ((double) (b - p) / b) * remove(b - 1, p, s - 1, f) + ((double) p / b) * remove(b - 1, p - 1, s, f - 1);
    }

    void debug(Object... o) {
        System.out.println(Arrays.deepToString(o));
    }

    public static final void main(String args[]) {
        new Test().solve();
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
}
