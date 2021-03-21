import java.io.IOException;
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

                                // returns the (length + 1) of the longest string
    public int longestString(String[] strings) {
        int max = 0, length;

        for (String string: strings) {

            length = string.length();

            if (length > max) {

                max = length;
            }
        }

        return max + 1;
    }

                                // sends the chart to the active player
    public void sendChart(Player receivingPlayer) throws IOException {
        final int space = 3;
        ArrayList<Integer[]> playersPoints = new ArrayList<>();
        ArrayList<boolean[]> playersRegistered = new ArrayList<>();
        StringBuilder row = new StringBuilder();

                                // caches points
        for (Player player: players) {
            playersPoints.add(player.getPoints().getUpdatedScore());
            playersRegistered.add(player.getPoints().getRegistered());
        }

        receivingPlayer.write("!startPrintLn");

                                // appends all the names to the to be send row
        row.append(String.format("%-" + (LONGESTDESIGNATION + space) + "s", "NAMES:"));
        for (Player player : players) {
            row.append(String.format("%-" + (LONGESTNAME + space) + "s", player.getUser().getName()));
        }

                                // sends row and deletes the cache
        receivingPlayer.write(row.toString());
        row.delete(0, row.length());

        for (int i=0; i<playersPoints.get(0).length; i++) {

                                // formatting
            if (i == 0 || i == 6 || i == 9 || i == 16) {
                row.append("\n");
            }

                                //  appends all the designations to the to be send row
            row.append(String.format("%-" + (LONGESTDESIGNATION + space) + "s", DESIGNATIONS[i] + ":"));

                                // appends all the values to the to be send row
            for (int j=0; j<playersPoints.size(); j++) {
                if (playersRegistered.get(j)[i]) {
                    row.append(String.format("%-" + (LONGESTNAME + space) + "s", playersPoints.get(j)[i].toString()));
                }
                else {
                    row.append(String.format("%-" + (LONGESTNAME + space) + "s", "-"));
                }
            }

                                // sends row and deletes the cache
            receivingPlayer.write(row.toString());
            row.delete(0, row.length());
        }

        receivingPlayer.write("!endPrintLn");
    }

                                // sends the point-sheet
    public void sendPoints(Player player, String section) throws IOException {
        String[] description = {"ID", "DESIGNATION", "NOT PLAYED", "POINTS"};
        StringBuilder row = new StringBuilder();

        player.write("!startPrintLn");

                                // prints designations
        row.append(String.format("%-3s", description[0]));
        row.append(String.format("%-" + LONGESTDESIGNATION + "s", description[1]));
        row.append(String.format("%-"+ LONGESTDESIGNATION + "s", description[2]));
        row.append(String.format("%s", description[3]));

        player.write(row.toString());
        player.write("\n");
        row.delete(0, row.length());

        if (section.toLowerCase().startsWith("u")) {

                                // sends upper section (row 1-6)
            for (int i=0; i<6; i++) {

                row.append(String.format("%-3d", (i+1)));
                row.append(String.format("%-" + LONGESTDESIGNATION + "s", DESIGNATIONS[i]));
                row.append(String.format("%-" + LONGESTDESIGNATION + "B", !player.getPoints().getRegistered()[i]));
                row.append(String.format("%d", player.getPoints().scoreUpperSection(i, player.getDices().getValues())));

                                // sends line and deletes the cache
                player.write(row.toString());
                row.delete(0, row.length());
            }
        }

        else {

                                // appends lower section (chart row 7-16)
            for (int i=0; i<7; i++) {

                                // one formatted row
                row.append(String.format("%-3d", (i+1)));
                row.append(String.format("%-" + LONGESTDESIGNATION + "s", DESIGNATIONS[i+9]));
                row.append(String.format("%-" + LONGESTDESIGNATION + "B", !player.getPoints().getRegistered()[i+9]));
                if (player.getPoints().condition(i, player.getDices().getValues())) {
                    row.append(String.format("%d", player.getPoints().scoreLowerSection(i, player.getDices().getValues())));
                }
                else {
                    row.append(String.format("%d", 0));
                }

                                // sends everything and deletes the cache
                player.write(row.toString());
                row.delete(0, row.length());
            }
        }

        player.write("!endPrintLn");
    }

                                // get-/set- methods
    public Player[] getPlayers() {
        return players;
    }
}