import java.io.IOException;
import java.util.ArrayList;

public class Game implements Runnable {
    private boolean running;

    private final java.net.ServerSocket SERVERSOCKET;
    private final Chart CHART;
    private final Player[] PLAYERS;
    private final Dices DICES;

    public Game(java.net.ServerSocket serverSocket, Chart chart, Player[] players) {
        this.running = true;

        this.SERVERSOCKET = serverSocket;
        this.CHART = chart;
        this.PLAYERS = players;
        this.DICES = new Dices();
    }

                                // sends the chart to every player
    private void sendChartToEveryone() throws IOException {

        for (Player player: PLAYERS) {
            CHART.sendChart(player);
        }
    }

                                // randomizes every dice
    private void randomizeDices() throws ArrayIndexOutOfBoundsException {

        for (int i=0; i<DICES.getValues().length; i++) {
            DICES.randomizeValue(i);
        }
    }

                                // returns the value of a dice
    private String printDices() {
        ArrayList<Integer> diceValues = new ArrayList<>();

        for (int value: DICES.getValues()) {
            diceValues.add(value);
        }

        return diceValues.toString();
    }

                                //  administrates the process of changing the dicing
    private void changeDices(Player player) throws IOException, ArrayIndexOutOfBoundsException {
        randomizeDices();

        Integer[] changes;
        for (int i=0; i<3; i++) {

            changes = inputChangeDices(player);
            if (changes == null) break;

            for (int change: changes) {
                DICES.randomizeValue(change);
            }
        }

                                // sends the finals values to the player
        player.write(printDices());
    }

    private Integer[] inputChangeDices(Player player) throws IOException {
        ArrayList<Integer> changes = new ArrayList<>();
        boolean wrongInput;
        String input;

        do {
            wrongInput = false;

            player.writeMultipleParagraphs(printDices());

                                // manages the input
            player.write("!changeDices");
            input = player.read();

                                // player doesn't want to change any dices
            if (input.startsWith(",")) return null;

                                // checks, if the input is valid
            try {
                for (String value: input.split(",")) {
                    changes.add(Integer.parseInt(value));
                }
            }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
                wrongInput = true;
                changes.clear();
                player.write("!wrongInput");
            }
        } while (wrongInput);

        return changes.toArray(new Integer[0]);
    }

                                // manages the chart input
    private void setPoints(Player player) throws IOException {
        String section;
        boolean wrongInput;
        int position = 0;

        do {
            wrongInput = false;
                                // manages the choosing of the upper or lower bracket
            player.write("!chooseBracket");
            section = player.read();

            if (section.toLowerCase().startsWith("u") || section.toLowerCase().startsWith("l")) {

                                // manages the choosing of the field
                try {
                    CHART.sendPoints(player, section, DICES);
                    player.write("!chooseID");
                    position = Integer.parseInt(player.read())-1;

                } catch (NumberFormatException numberFormatException) {
                    wrongInput = true;
                    player.write("!wrongInput");
                }
            }
        } while (wrongInput || !player.getPOINTS().setScore(section, position , DICES.getValues()));
    }
                                // manages if every player has completed their sheet
    private boolean isFinished(Player[] players) {

        for (Player player: players) {
            for (boolean registered : player.getPOINTS().getRegistered()) {
                if (!registered) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean finishGame() throws IOException {

        PLAYERS[0].write("!printFinalResults");
        return !PLAYERS[0].read().equalsIgnoreCase("yes");
    }

    @Override
    public void run() {

            while (!isFinished(PLAYERS)) {
                for (Player player: PLAYERS) {
                    player.startTurn();

                    try {
                        sendChartToEveryone();
                    } catch (IOException ignored) {}

                    try {
                        changeDices(player);
                        setPoints(player);
                    } catch (IOException ioException) {
                        if (!player.hasLeftGame()) {
                            System.out.printf("%n%s%n", player.getUSER().getName() + " has left"); // DEBUG
                            player.setLeftGame(true);   // DELETE
                        }
                    }

                    player.endTurn();
                }
            }
            try {

                // Only relevant for parallel
                while (!isFinished(CHART.getPlayers()) && finishGame()) {
                    finishGame();
                    sendChartToEveryone();
                }

                // Final chart
                sendChartToEveryone();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        running = false;
    }

                                // get-/set- methods
    public Player[] getPLAYERS() {
        return PLAYERS;
    }

    public boolean isRunning() {
        return running;
    }

    public java.net.ServerSocket getSERVERSOCKET() {
        return SERVERSOCKET;
    }
}