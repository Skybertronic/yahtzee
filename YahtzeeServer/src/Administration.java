import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

                                // administrates games
public class Administration implements Runnable {
    private final ArrayList<Thread> THREADS;
    private final List<Game> GAMES;
    private final List<Lobby> LOBBIES;

    public Administration() {
        THREADS = new ArrayList<>();
        GAMES = Collections.synchronizedList(new ArrayList<>());
        LOBBIES = Collections.synchronizedList(new ArrayList<>());
    }

                                // closes the socket of games, where running equals false
    private Game searchForFinishedGames() {

        for (Game game: GAMES) {
            if (!game.isRunning()) {
                return game;
            }
        }
        return null;
    }

    private void closePlayers(Game game) throws IOException {

        System.out.println("server.Game " + game.getSERVERSOCKET() + " finished!");
        for (Player player: game.getPLAYERS()) {
            player.write("!endGame");

            player.getSocket().close();
            player.getUSER().setInGame(false);
        }
    }

    private void closeLobbies(Game game) {

        for (Lobby lobby: LOBBIES) {
            if (lobby.getGAME() == game || !lobby.getPLAYER().hasLeftGame()) {
                System.out.println("Remove Lobby"); // DEBUG

                LOBBIES.remove(lobby);
                THREADS.remove(lobby);
            }
        }
    }

    private void closeGame() throws IOException {
        Game finishedGame = searchForFinishedGames();

        if (finishedGame!=null) {
            closePlayers(finishedGame);
            closeLobbies(finishedGame);

            finishedGame.getSERVERSOCKET().close();
            GAMES.remove(finishedGame);
            THREADS.remove(finishedGame);
        }
    }

    private void administrateReconnecting() {
        Lobby newLobby = createNewLobby();

        if (newLobby != null) {
            System.out.printf("%n%s%n%s%n", "Game " + newLobby.getGAME().getSERVERSOCKET().getLocalPort(), newLobby.getPLAYER().getUSER().getName() + " isn't connected!");
            LOBBIES.add(newLobby);
            Thread thread = new Thread(newLobby);
            thread.start();
            THREADS.add(thread);
        }
    }

    private Lobby createNewLobby() {

        for (Game game: GAMES) {
            System.out.println("TEST");
            for (Player player: game.getPLAYERS()) {
                System.out.println(player.hasLeftGame());
                if (player.hasLeftGame()) {
                    System.out.printf("%n%s%n", "NEW LOBBY");
                    Lobby lobby = new Lobby(game, player);
                    if (!lobbyAlreadyExists(lobby)) return lobby;
                }
            }
        }

        return null;
    }

    private boolean lobbyAlreadyExists(Lobby newLobby) {

        if (newLobby != null) {
            for (Lobby lobby: LOBBIES) {
                if (lobby.getPLAYER() == newLobby.getPLAYER()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void run() {

        while (true) {

            try {
                closeGame();
            } catch (IOException e) {
                e.printStackTrace();
            }

            administrateReconnecting();
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