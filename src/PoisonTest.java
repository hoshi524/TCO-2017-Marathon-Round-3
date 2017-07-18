import java.util.List;

class PoisonTest {
    public static PoisonedWineVis vis = null;
    public static int[] useTestStrips(String[] tests) {
        return vis.useTestStrips(tests);
    }
    public static int getPoison(List<Integer> list) {
        return vis.getPoison(list);
    }
}