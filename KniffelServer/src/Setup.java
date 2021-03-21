import java.io.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

                                // creates games
public class Setup {
    private final Administration ADMINISTRATION;
    private final List<User> USERS;
    private static java.net.ServerSocket latestServerSocket;

    private static int latestLocalPort = 1000;

    public Setup() {
        System.out.println("Server v2.0.1 by Skybertronic");

        USERS = new ArrayList<>();

        ADMINISTRATION = new Administration();

        Thread thread = new Thread(ADMINISTRATION);
        thread.start();

                                // prints ip-address to ease the connection
        try {
            System.out.println("IP-Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

                                // creates the server socket for the different games
    public void createNewSocket() throws IOException {

        latestServerSocket = new java.net.ServerSocket(++latestLocalPort);

        System.out.println("Lobby " + latestLocalPort + " is open to join!");
    }

                                // basically the lobby
    public Player[] connectToLobby() throws IOException {
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
            player.write(setRoleClient(isHost));
            isHost = false;

                                // adds player to the lobby
            players.add(player);

                                // host decides how many people are able to join the lobby
            players.get(0).write(player.getUser().getName() + " joined the game, add again?: ");
        } while (players.get(0).readLine().equalsIgnoreCase("yes"));

        System.out.println("Lobby " + latestLocalPort + " is closed!");

        return players.toArray(Player[]::new);
    }

                                // client gets linked to a player
    public Player loginPlayer(Socket loginSocket) throws IOException {
        String name, password;
        BufferedReader bufferedReader;
        PrintWriter printWriter;

                                // temporary connection
        bufferedReader = new BufferedReader(new InputStreamReader(loginSocket.getInputStream()));
        printWriter = new PrintWriter(new OutputStreamWriter(loginSocket.getOutputStream()));

                                // input name
        printWriter.println("Name: ");
        printWriter.flush();
        name = bufferedReader.readLine();

                                // input password
        printWriter.println("Password: ");
        printWriter.flush();
        password = bufferedReader.readLine();

                                // compares name and password with every existing user
        for (User user: USERS) {

            if (user.getName().equalsIgnoreCase(name)) {

                if (user.getPassword().equals(password) && !user.isInGame()) {

                                // login successful
                    printWriter.println("!loginSuccessful");
                    printWriter.flush();
                    user.setInGame(true);

                    return new Player(user, loginSocket);
                }

                                // wrong password
                printWriter.println("!loginFailed");
                printWriter.flush();

                return null;
            }
        }

                                // player didn't exist
        User user = new User(name, password);
        user.setInGame(true);
        USERS.add(user);

        printWriter.println("!registered");
        printWriter.flush();

        return new Player(user, loginSocket);
    }

                                // returns the command to assign a role
    public String setRoleClient(boolean isHost) {

        if (isHost) {
            return "!isHost";
        }
        else {
            return "!isPlayer";
        }
    }

    public void createGame(Player[] players) throws IOException {
        Game game;
        Thread gameThread;
        Chart chart = new Chart(players);

                                // host chooses game-type
        String type = players[0].readLine();

        System.out.println("Game " + latestLocalPort + " starts!");

        switch (type) {
                                // players roll dices one at a time
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
        }

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

                                // administrates games
class Administration implements Runnable {
    private final ArrayList<Thread> THREADS;
    private final List<Game> GAMES;

    public Administration() {
        THREADS = new ArrayList<>();
        GAMES = Collections.synchronizedList(new ArrayList<>());
    }

                                // closes the socket of games, where running equals false
    public Game searchForFinishedGames() throws IOException {
        while (true) {
            for (Game game: GAMES) {
                if (!game.isRunning()) {
                    System.out.println("server.Game " + game.getSERVERSOCKET() + " finished!");
                    for (Player player: game.getPLAYER()) {
                        player.write("!endGame");

                        player.getSocket().close();
                        player.getUser().setInGame(false);
                    }

                    return game;
                }
            }
        }
    }

    @Override
    public void run() {
        Game finishedGame;

        try {
            while (true) {
                finishedGame = searchForFinishedGames();

                finishedGame.getSERVERSOCKET().close();
                GAMES.remove(finishedGame);
                THREADS.remove(finishedGame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

                                // get-/set- methods
    public List<Game> getGAMES() {
        return GAMES;
    }

    public List<Thread> getTHREADS() {
        return THREADS;
    }
}