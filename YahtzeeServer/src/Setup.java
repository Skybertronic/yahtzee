import java.io.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// creates games
public class Setup {
    private final Administration ADMINISTRATION;
    private final List<User> USERS;

    private static java.net.ServerSocket latestServerSocket;
    private static int latestLocalPort = 1000;

    public Setup() {
        new Database();

        System.out.println("Server v2.1.4 by Skybertronic");

        USERS = new ArrayList<>();

        ADMINISTRATION = new Administration();
        Thread thread = new Thread(ADMINISTRATION);
        thread.start();

        System.out.print("Do you want to print your IP-address?: ");

                                // prints ip-address to ease the connection
        if (new Scanner(System.in).next().equalsIgnoreCase("yes")) {
            try {
                System.out.println("IP-address: " + InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException unknownHostException) {
                unknownHostException.printStackTrace();
            }
        }

    }

    public void write(PrintWriter printWriter, String message) {

        System.out.println("TestSend " + message);
        printWriter.println(message);
        printWriter.flush();

        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public String read(BufferedReader bufferedReader) throws IOException {
        String message = bufferedReader.readLine();
        System.out.println(message);
        return message;
    }

                                // creates the server socket for the different games
    private void createNewSocket() throws IOException {
        latestServerSocket = new java.net.ServerSocket(++latestLocalPort);
        System.out.println("Lobby " + latestLocalPort + " is open to join!");
    }

                                // basically the lobby
    private Player[] connectToLobby() throws IOException {
        Socket loginSocket;
        Player player;
        ArrayList<Player> players = new ArrayList<>();

        boolean isHost = true;
        do {
                                // client connects to lobby
            loginSocket = latestServerSocket.accept();

                                // checks for a valid name
            do {
                player = loginPlayer(loginSocket);
            } while (player == null);

                                //  client becomes the host or stays a player | relevant for the client
            write(player.getPrintWriter(), setRoleClient(isHost));
            isHost = false;

                                // adds player to the lobby
            players.add(player);

                                // host decides how many people are able to join the lobby
            write(players.get(0).getPrintWriter(), player.getUSER().getName());
        } while (players.get(0).read().equalsIgnoreCase("yes"));

        System.out.println("Lobby " + latestLocalPort + " is closed!");

        return players.toArray(Player[]::new);
    }

                                // client gets linked to a player
    private Player loginPlayer(Socket loginSocket) throws IOException {
        final int MAX_PLAYER_NAME_LENGTH = 12;
        String name, password;


        BufferedReader bufferedReader;
        PrintWriter printWriter;

                                // temporary connection
        bufferedReader = new BufferedReader(new InputStreamReader(loginSocket.getInputStream()));
        printWriter = new PrintWriter(new OutputStreamWriter(loginSocket.getOutputStream()));

                                // input name
        boolean wrongInput;
        do {
            wrongInput = false;

            name = read(bufferedReader);

            if (name.length()>MAX_PLAYER_NAME_LENGTH) {
                wrongInput = true;

                write(printWriter, "" + MAX_PLAYER_NAME_LENGTH);
            }
        } while (wrongInput);
        write(printWriter, "!acceptedInput");

                                // input password
        password = read(bufferedReader);

                                // compares name and password with every existing user
        for (User user: USERS) {

            if (user.getName().equalsIgnoreCase(name)) {

                if (user.getPassword().equals(password) && !user.isInGame()) {

                                // login successful
                    write(printWriter, "!loginSuccessful");
                    user.setInGame(true);

                    return new Player(user, loginSocket, bufferedReader, printWriter);
                }

                                // wrong password
                write(printWriter, "!loginFailed");

                return null;
            }
        }

                                // user didn't exist
        User user = new User(name, password);
        user.setInGame(true);
        USERS.add(user);

        write(printWriter, "!registered");

        return new Player(user, loginSocket, bufferedReader, printWriter);
    }

                                // returns the command to assign a role
    private String setRoleClient(boolean isHost) {

        if (isHost) {
            return "!isHost";
        }
        else {
            return "!isPlayer";
        }
    }

                                // last settings, before the game starts
    private void createGame(Player[] players) throws IOException {
        Game game;
        boolean wrongInput;
        Thread gameThread;
        Chart chart = new Chart(players);

                                // host chooses game-type
        do {
            wrongInput = false;

            switch (players[0].read()) {

                case "linear" -> {

                    game = new Game(latestServerSocket, chart, players);
                    ADMINISTRATION.getGAMES().add(game);

                    // allows multiple games to run at the same time
                    gameThread = new Thread(game);
                    gameThread.start();
                    ADMINISTRATION.getTHREADS().add(gameThread);
                }

                // players play there games separately and the chart gets synchronized
                case "parallel" -> {

                    for (Player player : players) {

                        game = new Game(latestServerSocket, chart, new Player[]{player});
                        ADMINISTRATION.getGAMES().add(game);

                        // allows multiple games to run at the same time
                        gameThread = new Thread(game);
                        gameThread.start();
                        ADMINISTRATION.getTHREADS().add(gameThread);
                    }
                }
                default -> {

                    wrongInput = true;
                    players[0].write("!wrongInput");
                }
            }
        } while (wrongInput);

        players[0].write("!acceptedInput");

        System.out.println("Game " + latestLocalPort + " starts!");

                                // sends the command to start the game to every player part of the lobby
        for (Player player: players) {
            player.write("!startGame");
        }
    }

    public static void main(String[] args) {
        Setup setup = new Setup();

        try {
                                // creates infinite lobbies and transforms them into games games
            while (true) {

                setup.createNewSocket();
                setup.createGame(setup.connectToLobby());
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}