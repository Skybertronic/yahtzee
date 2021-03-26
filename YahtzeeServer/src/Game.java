import java.io.IOException;
import java.util.ArrayList;

                                // administrates everything happening in game
public class Game implements Runnable {
    private boolean running;

    private final java.net.ServerSocket SERVERSOCKET;
    private final Chart CHART;
    private final Player[] PLAYER;

    public Game(java.net.ServerSocket serverSocket, Chart chart, Player[] players) {
        this.running = true;

        this.SERVERSOCKET = serverSocket;
        this.CHART = chart;
        this.PLAYER = players;
    }

                                // sends the chart to every player
    private void sendChartToEveryone() throws IOException {

        for (Player player: PLAYER) {
            CHART.sendChart(player);
        }
    }

                                // randomizes every dice
    private void randomizeDices(Player player) {

        for (int i=0; i<player.getDices().getValues().length; i++) {
            player.getDices().randomizeValue(i);
        }
    }

                                // returns the value of a dice
    private String printDices(Player player) {
        ArrayList<Integer> diceValues = new ArrayList<>();

        for (int value: player.getDices().getValues()) {
            diceValues.add(value);
        }

        return diceValues.toString();
    }

                                //  administrates the process of changing the dicing
    private void changeDices(Player player) throws IOException {
        String message;
        boolean wrongInput;
        ArrayList<Integer> changes = new ArrayList<>();

        randomizeDices(player);

        for (int i=0; i<3; i++) {
                                // sends the value of the dices to the active player
            do {
                wrongInput = false;
                changes.clear();

                player.write("!startPrintLn");
                player.write(printDices(player));
                player.write("!endPrintLn");

                // manages the input
                player.write("!startPrint");
                player.write("Which dices do you want to change?: ");
                player.write("!endPrint");

                player.write("!getInput");
                message = player.readLine();

                // player doesn't want to change any dices
                if (message.startsWith(",")) {
                    i=3;
                    break;
                }

                try {
                    for (String value: message.split(",")) {
                        changes.add(Integer.parseInt(value));
                    }
                } catch (NumberFormatException numberFormatException) {
                    wrongInput = true;

                    player.write("!startPrintLn");
                    player.write("Wrong input!");
                    player.write("!endPrintLn");
                }
            } while (wrongInput);

                                // extracts the different values
            for (int change: changes) {
                player.getDices().randomizeValue(change);
            }
            
        }

                                // sends the finals values to the player
        player.write("!startPrintLn");
        player.write(printDices(player));
        player.write("!endPrintLn");
    }

                                // manages the chart input
    private void setPoints(Player player) throws IOException {
        String section;
        int position = 0;

        do {
                                // manages the choosing of the upper or lower bracket
            player.write("!startPrint");
            player.write("Upper or lower bracket?: ");
            player.write("!endPrint");

            player.write("!getInput");
            section = player.readLine();

            if (section.toLowerCase().startsWith("u") || section.toLowerCase().startsWith("l")) {

                                // manages the choosing of the field
                CHART.sendPoints(player, section);

                player.write("!startPrint");
                player.write("ID: ");
                player.write("!endPrint");

                player.write("!getInput");
                position = Integer.parseInt(player.readLine())-1;
            }
        } while (!player.getPoints().setScore(section, position , player.getDices().getValues()));
    }

                                // manages if every player has completed their sheet
    private boolean endGame() {

        for (Player player: PLAYER) {
            for (boolean registered : player.getPoints().getRegistered()) {
                if (!registered) {
                    return true;
                }
            }
        }

        return false;
    }

                                // returns the status of the game
    public String printStatusResult() {

        for (Player player: CHART.getPlayers()) {
            for (boolean registered : player.getPoints().getRegistered()) {
                if (!registered) {
                    return "The game hasn't ended yet!";
                }
            }
        }

        return "The game has ended!";
    }

    @Override
    public void run() {

        try {
            while (endGame()) {
                for (Player player: PLAYER) {
                    player.write("!startTurn" + player.getUSER().getName());
                    sendChartToEveryone();
                    changeDices(player);
                    setPoints(player);
                    player.write("!endTurn" + player.getUSER().getName());
                }
            }

                                // Nach Spielende
            PLAYER[0].write("!waitingForResults");

            String status;
            do {
                PLAYER[0].write("!startPrintLn");
                PLAYER[0].write(printStatusResult());
                PLAYER[0].write("!endPrintLn");

                PLAYER[0].write("!startPrint");
                PLAYER[0].write("Do you want to print the results?: ");
                PLAYER[0].write("!endPrint");

                PLAYER[0].write("!getInput");
                status = PLAYER[0].readLine();
            } while (!status.equals("yes"));

            sendChartToEveryone();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        running = false;
    }

                                // get-/set- methods
    public Player[] getPLAYER() {
        return PLAYER;
    }

    public boolean isRunning() {
        return running;
    }

    public java.net.ServerSocket getSERVERSOCKET() {
        return SERVERSOCKET;
    }
}