public class Dices {
    private final int[] values;

    public Dices() {
        values = new int[5];
    }

                                // randomizes a dice
    public void randomizeValue(int i) {
        values[i] = (int) (Math.random()*6+1);
    }

                                // get-/set- methods
    public int[] getValues() {
        return values;
    }
}