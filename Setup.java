/*
 * Version 2021.01.11
 * By Tim Miguletz
 * */

import socketio.ServerSocket;
import socketio.Socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Setup {
    private final Administration ADMINISTRATION;
    private final List<User> USERS;
    private static ServerSocket newestServerSocket;

    private static int localPort = 1000;

    public Setup() {
        USERS = new ArrayList<>();

        ADMINISTRATION = new Administration();
        Thread thread = new Thread(ADMINISTRATION);
        thread.start();
    }

                                //
    public void createNewSocket() throws IOException {

                                // Inizialisierung
        newestServerSocket = new ServerSocket(++localPort);

        System.out.println("Server " + localPort + " started!");
    }

    public Player[] connect() throws IOException {
        Player player;
        Socket loginSocket;
        ArrayList<Player> players = new ArrayList<>();

                                // User wird zum Spieler umgewandelt (add Socket)
        int i=0;
        do {
            loginSocket = newestServerSocket.accept();
            do {
                player = loginPlayer(loginSocket);
            } while (player == null);

                                //  Client Host oder Player setzen
            player.getSocket().write(setRoleClient(i) + "\n");

                                // Spieler hinzufügen
            players.add(player);

            players.get(0).getSocket().write(++i + ". Player added, add again?: \n");
        } while (players.get(0).getSocket().readLine().equalsIgnoreCase("yes"));

        return players.toArray(Player[]::new);
    }

    public Player loginPlayer(Socket loginSocket) throws IOException {
        String name, password;

        loginSocket.write("Name: \n");
        name = loginSocket.readLine();

        loginSocket.write("Password: \n");
        password = loginSocket.readLine();

        for (User user: USERS) {
            if (user.getName().equalsIgnoreCase(name)) {
                if (user.getPassword().equals(password) && !user.isIngame()) {
                    loginSocket.write("!loginSuccessful\n");
                    user.setIngame(true);
                    return new Player(user, loginSocket);
                }

                loginSocket.write("!loginFailed\n");
                return null;
            }
        }

        User user = new User(name, password);
        user.setIngame(true);
        USERS.add(user);

        loginSocket.write("!registered\n");
        return new Player(user, loginSocket);
    }

    public String setRoleClient(int i) {

        if (i==0) {
            return "!isHost";
        }
        else {
            return "!isPlayer";
        }
    }

    public void createGame(Player[] players) throws IOException {
        Game game = null;
        Thread gameThread;
        Chart chart = new Chart(players);

        System.out.println("Create game!");

                                // Host wählt Spieltyp
        String type = players[0].getSocket().readLine();

        switch (type) {
            case "linear" -> {
                game = new Game(newestServerSocket, chart, players);
                ADMINISTRATION.getGAMES().add(game);

                gameThread = new Thread(game);
                gameThread.start();
                ADMINISTRATION.getTHREADS().add(gameThread);
            }

            case "parallel" -> {
                Player[] playerGame;

                for (Player player : players) {
                    playerGame = new Player[1];
                    playerGame[0] = player;

                    game = new Game(newestServerSocket, chart, playerGame);
                    ADMINISTRATION.getGAMES().add(game);

                    gameThread = new Thread(game);
                    gameThread.start();
                    ADMINISTRATION.getTHREADS().add(gameThread);
                }
            }
        }

                                // Befreit Spieler aus Endlosschleife
        for (Player player: players) {
            player.getSocket().write("!startGame\n");
        }
        
        System.out.println("Game " + (game != null ? game.getSERVERSOCKET().toString() : null) + " created!");
    }

    public static void main(String[] args) {
        Setup setup = new Setup();

        try {
            while (true) {
                setup.createNewSocket();
                setup.createGame(setup.connect());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Administration implements Runnable {
    private final ArrayList<Thread> THREADS;
    private final List<Game> GAMES;

    public Administration() {
        THREADS = new ArrayList<>();
        GAMES = Collections.synchronizedList(new ArrayList<>());
    }

                                // Sucht nach Game, bei denen running false ist und leitet das Schließen ein
    public Game searchForFinishedGames() throws IOException {
        while (true) {
            for (Game game: GAMES) {
                if (!game.isRunning()) {
                    System.out.println("Game " + game.getSERVERSOCKET().toString() + " finished!");
                    for (Player player: game.getPLAYER()) {
                        player.getSocket().write("!endGame\n");
                        player.getUser().setIngame(false);
                        player.getSocket().close();
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

    public List<Game> getGAMES() {
        return GAMES;
    }

    public List<Thread> getTHREADS() {
        return THREADS;
    }
}