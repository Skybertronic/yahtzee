import java.util.ArrayList;

public class Chart {
    private final String[] DESIGNATIONS = {"ACES", "TWOS", "THREES", "FOURS", "FIVES", "SIXES", "UPPER SUM", "BONUS", "WITH BONUS", "THREE OF A KIND", "FOUR OF A KIND", "FULL HOUSE", "SMALL STRAIGHT", "BIG STRAIGHT", "YAHTZEE", "CHANCE", "TOTAL LOWER POINTS", "TOTAL UPPER POINTS", "TOTAL"};
    private final Player[] players;
    private final int LONGESTDESIGNATION, LONGESTNAME;

    public Chart(Player[] players) {
        this.players = players;
        this.LONGESTDESIGNATION = longestString(DESIGNATIONS);
        ArrayList<String> playersNames = new ArrayList<>();

        for (Player player: players) {
            playersNames.add(player.getUser().getName());
        }
        this.LONGESTNAME =  longestString(playersNames.toArray(String[]::new));
    }

                                // Gibt die Länge+1 der längsten Strings zurück
    public int longestString(String[] strings) {
        int max = 0, length;

        for (String string: strings) {
            length = string.length();
            if (length > max) {
                max = length;
            }
        }

        return max+1;
    }

                                // Gibt ein Chart mit allen Spielern und Punkten zurück
    public String getPrintChart() {
        int space = 3;
        String printPoints;
        ArrayList<Integer[]> playersPoints = new ArrayList<>();
        ArrayList<boolean[]> playersRegistered = new ArrayList<>();

                                // Punkte zwischenspeichern
        for (Player player: players) {
            playersPoints.add(player.getPoints().getUpdatedSinglePoints());
            playersRegistered.add(player.getPoints().getRegistered());
        }

                                // Namen
        StringBuilder returnString = new StringBuilder(String.format("%-" + (LONGESTDESIGNATION + space) + "s", "NAMES:"));
        for (Player player : players) {
            returnString.append(String.format("%-" + (LONGESTNAME + space) + "s", player.getUser().getName()));
        }

        for (int i=0; i<playersPoints.get(0).length; i++) {

                                // Leere Zeilen
            if (i == 0 || i == 6 || i == 9 || i == 16) {
                returnString.append("\n");
            }

                                // Bezeichnungen
            returnString.append(String.format("%n%-" + (LONGESTDESIGNATION + space) + "s", DESIGNATIONS[i] + ":"));

                                // Punkte
            for (int j=0; j<playersPoints.size(); j++) {
                if (playersRegistered.get(j)[i]) {
                    printPoints = playersPoints.get(j)[i].toString();
                }
                else {
                    printPoints = "-";
                }
                returnString.append(String.format("%-" + (LONGESTNAME + space) + "s", printPoints));
            }
        }

        returnString.append("\n");

        return returnString.toString();
    }

                                // Gibt detaillierte Felder aus, wenn man oben oder unten seine Punkte eintragen will
    public String printPoints(Player player, String section) {
        String[] designation = {"ID", "DESIGNATION", "NOT PLAYED", "POINTS"};
        StringBuilder returnString = new StringBuilder();

                                // Spalten Bezeichnung
        returnString.append(String.format("%n%-" + 3 + "s", designation[0]));
        returnString.append(String.format("%-" + LONGESTDESIGNATION + "s", designation[1]));
        returnString.append(String.format("%-"+ LONGESTDESIGNATION + "s", designation[2]));
        returnString.append(String.format("%s%n", designation[3]));

                                // 1-6 (Obere Sektion)
        if (section.toLowerCase().startsWith("u")) {

            for (int i=0; i<6; i++) {

                returnString.append(String.format("%n%-" + 3 + "d", (i+1)));
                returnString.append(String.format("%-" + LONGESTDESIGNATION + "s", DESIGNATIONS[i]));
                returnString.append(String.format("%-" + LONGESTDESIGNATION + "B", !player.getPoints().getRegistered()[i]));
                returnString.append(String.format("%d", player.getPoints().pointsU(i, player.getDices().getValues())));
            }
        }

                                // Untere Sektion
        else {

            for (int i=0; i<7; i++) {

                returnString.append(String.format("%n%-" + 3 + "d", (i+1)));
                returnString.append(String.format("%-" + LONGESTDESIGNATION + "s", DESIGNATIONS[i+9]));
                returnString.append(String.format("%-" + LONGESTDESIGNATION + "B", !player.getPoints().getRegistered()[i+9]));
                if (player.getPoints().condition(i, player.getDices().getValues())) {
                    returnString.append(String.format("%d", player.getPoints().pointsL(i, player.getDices().getValues())));
                }
                else {
                    returnString.append(String.format("%d", 0));
                }
            }
        }

        return returnString.toString();
    }

    public Player[] getPlayers() {
        return players;
    }
}
