import java.io.*;
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

        System.out.println("Server v2.2.0 by Skybertronic");

        USERS = new ArrayList<>();

        ADMINISTRATION = new Administration();
        Thread thread = new Thread(ADMINISTRATION);
        thread.start();

        try {
            printIPAddress();
        } catch (UnknownHostException unknownHostException) {
            unknownHostException.printStackTrace();
        }
    }

                                // prints ip-address to ease the connection
    private void printIPAddress() throws UnknownHostException {

        System.out.printf("%n%s", "Do you want to print your IP-address?: ");

        if (new Scanner(System.in).next().toLowerCase().contains("yes")) System.out.println("IP-address: " + InetAddress.getLocalHost().getHostAddress());
    }

    public void write(PrintWriter printWriter, String message) {

        printWriter.println(message);
        printWriter.flush();

        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public String read(PrintWriter printWriter, BufferedReader bufferedReader) throws IOException {

        try {
            Thread.sleep(10);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        printWriter.println("!getInput");
        printWriter.flush();

        String message = bufferedReader.readLine();
        return message;
    }

                                // creates the server socket for the different games
    private void createNewSocket() throws IOException {
        latestServerSocket = new java.net.ServerSocket(++latestLocalPort);
        System.out.printf("%n%s", "Lobby " + latestLocalPort + " is open to join!");
    }

                                // basically the lobby
    private Player[] connectToGame() throws IOException {
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
            player.write(getCommandRoleClient(isHost));
            isHost = false;

                                // adds player to the lobby
            players.add(player);

        } while (addAgain(players.get(0), player));

        System.out.printf("%n%s", "Lobby " + latestLocalPort + " is closed!");

        return players.toArray(Player[]::new);
    }

    // client gets linked to a player
    public Player loginPlayer(Socket loginSocket) throws IOException {
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

            write(printWriter, "!name");
            name = read(printWriter, bufferedReader);

            if (name.length()>MAX_PLAYER_NAME_LENGTH) {
                write(printWriter, "!wrongInput");
                wrongInput = true;
            }
        } while (wrongInput);

        // input password
        write(printWriter,"!password");
        password = read(printWriter, bufferedReader);

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

                                // host decides if additional clients are able to join the lobby
    private boolean addAgain(Player host, Player player) throws IOException {

        host.write("!addAgain");
        host.write(player.getUSER().getName());

        host.read().equals("yes");

        return new BufferedReader(new InputStreamReader(host.getSocket().getInputStream())).readLine().equals("!yes");
    }



                                // returns the command to assign a role
    private String getCommandRoleClient(boolean isHost) {

        if (isHost) {
            return "!isHost";
        }
        else {
            return "!isPlayer";
        }
    }

                                // last settings, before the game starts
    private void createGame(Player[] players) throws IOException {
        boolean wrongInput;
        Chart chart = new Chart(players);
        ArrayList<Game> games = new ArrayList<>();

                                // host chooses game-type
        do {
            wrongInput = false;

            players[0].write("!assignType");
            switch (players[0].read()) {
                case "linear" -> games.add(new Game(latestServerSocket, chart, players));

                                // players play there games separately and the chart gets synchronized
                case "parallel" -> {
                    for (Player player : players) {
                        games.add(new Game(latestServerSocket, chart, new Player[]{player}));
                    }
                }

                default -> {
                    wrongInput = true;
                    players[0].write("!wrongInput");
                }
            }
        } while (wrongInput);

                                // sends the command to start the game to every player part of the lobby
        for (Player player: players) {
            player.write("!startGame");
        }

                                // adds the game(s) to administration
        for (Game game: games) {
            ADMINISTRATION.addGame(game);
        }

        System.out.printf("%n%s%n", "Game " + latestLocalPort + " starts!");
    }

    public static void main(String[] args) {
        Setup setup = new Setup();

        try {
                                // creates infinite lobbies and transforms them into games games
            while (true) {

                setup.createNewSocket();
                setup.createGame(setup.connectToGame());
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}