public class Dices {
    private final int[] values;

    public Dices() {
        values = new int[5];
    }

    public void randomizeValue(int i) {
        values[i] = (int) (Math.random()*6+1);
    }

    public int[] getValues() {
        return values;
    }
}
