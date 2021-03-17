import java.util.ArrayList;
import java.util.Arrays;

public class Points {
    private final int[] STANDARDPOINTS = {35, 25, 30, 40, 60};

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

                                // Aktualisiert Bonus und totalPoints
    public void updateTotalPoints() {
        int sumPoints = 0;

                                // Oben ohne Bonus
        for (Integer point: upperPoints) {
            sumPoints += point;
        }
        totalPoints[0] = sumPoints;

                                // Bonus
        if (totalPoints[0]>=63) {
            bonus = STANDARDPOINTS[0];
        }

                                // Oben mit Bonus
        totalPoints[1] = sumPoints + bonus;

                                // Unten
        sumPoints = 0;
        for (int points: lowerPoints) {
            sumPoints += points;
        }
        totalPoints[2] = sumPoints;

        totalPoints[3] = totalPoints[1] + totalPoints[2];
    }

    public Integer[] getUpdatedSinglePoints() {
        updateTotalPoints();
        ArrayList<Integer> points = new ArrayList<>();

                                // Oben
        for (Integer point: upperPoints) {
            points.add(point);
        }
        points.add(totalPoints[0]);
        points.add(bonus);
        points.add(totalPoints[1]);

                                // Unten
        for (Integer point: lowerPoints) {
            points.add(point);
        }

                                // Gesamt
        points.add(totalPoints[2]);
        points.add(totalPoints[1]);
        points.add(totalPoints[3]);

        return points.toArray(new Integer[0]);
    }

    public boolean setPoints(String section, int position, int[] diceValues) {
        if (section.toLowerCase().startsWith("u") && position>=0 && position<=5 && !registered[position]) {

            upperPoints[position] = pointsU(position, diceValues);
            registered[position] = true;

            return true;
        }
        else if (section.toLowerCase().startsWith("l") && position>=0 && position<=6 && !registered[position+9]) {
            if (condition(position, diceValues)) {
                lowerPoints[position] = pointsL(position, diceValues);
            }

            registered[position+9] = true;
            return true;
        }

        return false;
    }

    public boolean condition(int position, int[] diceValues) {

        int[] values = diceValues.clone();
        Arrays.sort(values);

        int amount, firstValue = values[0];

        switch (position) {
                                // Dreierpasch
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

                                // Viererpasch
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

                                // Full-House
            case 2:
                return (values[0] == values[1] && values[0] == values[2] && values[3] == values[4]) || (values[0] == values[1] && values[4] == values[3] && values[4] == values[2]);

                                // Kleine Straße
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

                                // Große Straße
            case 4:
                for (int i = 0; i<values.length-1; i++) {
                    if (values[i] != values[i+1]-1){
                        return false;
                    }
                }
                return true;

                                // Kniffel
            case 5:

                for (int value: values) {
                    if (value != firstValue) {
                        return false;
                    }
                }
                return true;

                                // Chance
            case 6:
                return true;
        }

        return false;
    }

    public int pointsU(int position, int[] diceValues) {
        int returnPoints = 0;

        for (int value: diceValues) {
            if (value == position+1) {
                returnPoints += value;
            }
        }

        return returnPoints;
    }

                                // Wie Condition, nur die passenden Punkte
    public int pointsL(int position, int[] diceValues)  {
        int returnPoints = 0;

        switch (position) {
            case 0:
            case 1:
            case 6:
                for (int value: diceValues) {
                    returnPoints += value;
                }
                break;

            case 2:
                return STANDARDPOINTS[1];

            case 3:
                return STANDARDPOINTS[2];

            case 4:
                return STANDARDPOINTS[3];

            case 5:
                return STANDARDPOINTS[4];
        }

        return returnPoints;
    }

    public boolean[] getRegistered() {
        return registered;
    }
}
