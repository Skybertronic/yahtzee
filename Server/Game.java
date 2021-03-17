import socketio.ServerSocket;

import java.io.IOException;
import java.util.ArrayList;

public class Game implements Runnable {
    private boolean running;

    private final ServerSocket SERVERSOCKET;
    private final Chart CHART;
    private final Player[] PLAYER;

    public Game(ServerSocket serverSocket, Chart chart, Player[] players) {
        this.running = true;
        this.SERVERSOCKET = serverSocket;
        this.CHART = chart;
        this.PLAYER = players;
    }

                                // Sendet das Chart an alle Spieler eines Games
    public void sendChart() throws IOException {

        for (Player player: PLAYER) {
            player.getSocket().write("!startPrintLn\n");
            player.getSocket().write(CHART.getPrintChart());
            player.getSocket().write("!endPrintLn\n");
        }
    }

                                // Weist allen Würfeln eines Spielers einen neuen Wert zu
    public void randomizeDices(Player player) {

        for (int i=0; i<player.getDices().getValues().length; i++) {
            player.getDices().randomizeValue(i);
        }
    }

                                // Gibt die Würfel eines Spielers, in Form einen Strings, zurück
    public String printDices(Player player) {
        ArrayList<Integer> diceValues = new ArrayList<>();

        for (int value: player.getDices().getValues()) {
            diceValues.add(value);
        }

        return diceValues.toString();
    }

                                // Verwaltet das ändern der Würfel
    public void changeDices(Player player) throws IOException {
        String changes;

        randomizeDices(player);

        for (int i=0; i<3; i++) {
            player.getSocket().write("!startPrintLn\n");
            player.getSocket().write(printDices(player) + "\n");
            player.getSocket().write("!endPrintLn\n");

            player.getSocket().write("!startPrint\n");
            player.getSocket().write("Which dices do you want to change?: \n");
            player.getSocket().write("!endPrint\n");

            player.getSocket().write("!getInput\n");
            changes = player.getSocket().readLine();

            if (changes.split("").length<2 || !changes.split("")[1].equals(",")) break;

            for (String changing: changes.split(",")) {
                player.getDices().randomizeValue(Integer.parseInt(changing));
            }
        }

        player.getSocket().write("!startPrintLn\n");
        player.getSocket().write(printDices(player) + "\n");
        player.getSocket().write("!endPrintLn\n");
    }

                                // Verwaltet das Setzen der Punkte
    public void setPoints(Player player) throws IOException {
        String section;
        int position;

        do {
            position = 0;
            player.getSocket().write("!startPrint\n");
            player.getSocket().write("Upper or lower bracket?: \n");
            player.getSocket().write("!endPrint\n");

            player.getSocket().write("!getInput\n");
            section = player.getSocket().readLine();

            if (section.toLowerCase().startsWith("u") || section.toLowerCase().startsWith("l")) {
                player.getSocket().write("!startPrintLn\n");
                player.getSocket().write( CHART.printPoints(player, section) + "\n");
                player.getSocket().write("!endPrintLn\n");

                player.getSocket().write("!startPrint\n");
                player.getSocket().write("ID: \n");
                player.getSocket().write("!endPrint\n");

                player.getSocket().write("!getInput\n");
                position = Integer.parseInt(player.getSocket().readLine())-1;
            }
        } while (!player.getPoints().setPoints(section, position , player.getDices().getValues()));
    }

                                // Überprüft, ob alle Spieler eines Games ihre Felder ausgefüllt haben
    public boolean endGame() {

        for (Player player: PLAYER) {
            for (boolean registered : player.getPoints().getRegistered()) {
                if (!registered) {
                    return true;
                }
            }
        }

        return false;
    }

                                // Gibt verschiedene Nachrichten aus, je nachdem ob alle Felder eines Charts ausgefüllt sind
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
                    player.getSocket().write("!startTurn" + player.getUser().getName() + "\n");
                    sendChart();
                    changeDices(player);
                    setPoints(player);
                    player.getSocket().write("!endTurn" + player.getUser().getName() + "\n");
                }
            }

                                // Nach Spielende
            PLAYER[0].getSocket().write("!waitingForResults\n");

            String status;
            do {
                PLAYER[0].getSocket().write("!startPrintLn\n");
                PLAYER[0].getSocket().write(printStatusResult() + "\n");
                PLAYER[0].getSocket().write("!endPrintLn\n");

                PLAYER[0].getSocket().write("!startPrint\n");
                PLAYER[0].getSocket().write("Do you want to print the results?: \n");
                PLAYER[0].getSocket().write("!endPrint\n");

                PLAYER[0].getSocket().write("!getInput\n");
                status = PLAYER[0].getSocket().readLine();
            } while (!status.equals("yes"));

            sendChart();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        running = false;
    }

    public Player[] getPLAYER() {
        return PLAYER;
    }

    public boolean isRunning() {
        return running;
    }

    public ServerSocket getSERVERSOCKET() {
        return SERVERSOCKET;
    }
}
