import java.util.ArrayList;
import java.util.Arrays;

                                // calculates the score and checks the conditions
public class Points {
    private final int[] FIXEDPOINTS = {35, 25, 30, 40, 60};

    private final int[] upperPoints, lowerPoints, totalPoints;
    private int bonus;
    private final boolean[] registered;

    public Points() {
        registered = new boolean[19];
        upperPoints = new int[6];
        bonus = 0;
        lowerPoints = new int[7];
        totalPoints = new int[4];

        int[] alwaysTrues = {6, 7, 8, 16, 17, 18};
        for (int alwaysTrue: alwaysTrues) {
            for (int i=0; i<registered.length; i++) {
                if (alwaysTrue == i) registered[i] = true;
            }
        }
    }

                                // updates the total points and the bonus
    public void updateTotalScore() {
        int sumPoints = 0;

                                // upper bracket without bonus
        for (Integer point: upperPoints) {
            sumPoints += point;
        }
        totalPoints[0] = sumPoints;

                                // bonus
        if (totalPoints[0]>=63) {
            bonus = FIXEDPOINTS[0];
        }

                                // upper bracket with bonus
        totalPoints[1] = sumPoints + bonus;

                                // lower bracket
        sumPoints = 0;
        for (int points: lowerPoints) {
            sumPoints += points;
        }
        totalPoints[2] = sumPoints;

        totalPoints[3] = totalPoints[1] + totalPoints[2];
    }

                                // returns the updated score of a player (formatted)
    public Integer[] getUpdatedScore() {
        ArrayList<Integer> points = new ArrayList<>();

        updateTotalScore();

                                // adds upper bracket
        for (Integer point: upperPoints) {
            points.add(point);
        }
        points.add(totalPoints[0]);
        points.add(bonus);
        points.add(totalPoints[1]);

                                // adds lower bracket
        for (Integer point: lowerPoints) {
            points.add(point);
        }

                                // adds total score
        points.add(totalPoints[2]);
        points.add(totalPoints[1]);
        points.add(totalPoints[3]);

        return points.toArray(new Integer[0]);
    }

                                // checks if score is already set and assigns the value of it
    public boolean setScore(String section, int position, int[] diceValues) {

        if (section.toLowerCase().startsWith("u") && position>=0 && position<=5 && !registered[position]) {

            upperPoints[position] = scoreUpperSection(position, diceValues);
            registered[position] = true;

            return true;
        }

        else if (section.toLowerCase().startsWith("l") && position>=0 && position<=6 && !registered[position+9]) {

            if (condition(position, diceValues)) {

                lowerPoints[position] = scoreLowerSection(position, diceValues);
            }

            registered[position+9] = true;
            return true;
        }

        return false;
    }

                                // checks, if the dice fulfills the condition if the field
    public boolean condition(int position, int[] diceValues) {

        int[] values = diceValues.clone();
        Arrays.sort(values);

        int amount, firstValue = values[0];

        switch (position) {
                                // tree of a kind
            case 0:
                for (int fstValue: values) {
                    amount = 0;

                    for (int sndValue: values) {
                        if (sndValue == fstValue) {
                            if (++amount>=3) {
                                return true;
                            }
                        }
                    }
                }
                break;

                                // four of a kind
            case 1:
                for (int fstValue: values) {
                    amount = 0;

                    for (int sndValue: values) {
                        if (sndValue == fstValue) {
                            if (++amount>=4) {
                                return true;
                            }
                        }
                    }
                }
                break;

                                // full-house
            case 2:
                return (values[0] == values[1] && values[0] == values[2] && values[3] == values[4]) || (values[0] == values[1] && values[4] == values[3] && values[4] == values[2]);

                                // small straight
            case 3:
                for (int i=0;i<values.length-1; i++) {
                    if (values[i]==values[i+1]) {
                        values[i] = 0;
                    }
                }
                Arrays.sort(values);

                if (values[0] != 0) {
                    for (int i = 0; i<values.length-2; i++) {
                        if (values[i] != values[i+1]-1){
                            return false;
                        }
                    }
                    return true;
                }

                else if (values[1] != 0) {
                    for (int i = 1; i<values.length-1; i++) {
                        if (values[i] != values[i+1]-1){
                            return false;
                        }
                    }
                    return true;
                }
                return false;

                                // big straight
            case 4:
                for (int i = 0; i<values.length-1; i++) {
                    if (values[i] != values[i+1]-1){
                        return false;
                    }
                }
                return true;

                                // yahtzee
            case 5:

                for (int value: values) {
                    if (value != firstValue) {
                        return false;
                    }
                }
                return true;

                                // chance
            case 6:
                return true;
        }

        return false;
    }

                                // computes the score for fields part of the upper section
    public int scoreUpperSection(int position, int[] diceValues) {
        int returnPoints = 0;

        for (int value: diceValues) {
            if (value == position+1) {
                returnPoints += value;
            }
        }

        return returnPoints;
    }

                                // computes the score for fields part of the lower section | compare position to condition
    public int scoreLowerSection(int position, int[] diceValues)  {
        int returnPoints = 0;

        switch (position) {
            case 0:
            case 1:
            case 6:
                for (int value: diceValues) {
                    returnPoints += value;
                }
                break;

            case 2: return FIXEDPOINTS[1];
            case 3: return FIXEDPOINTS[2];
            case 4: return FIXEDPOINTS[3];
            case 5: return FIXEDPOINTS[4];
        }

        return returnPoints;
    }

                                // get-/set- methods
    public boolean[] getRegistered() {
        return registered;
    }
}