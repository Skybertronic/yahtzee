import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

                                // administrates games
public class Administration implements Runnable {
    private final ArrayList<Thread> THREADS;
    private final List<Game> GAMES;
    private Thread lastAddedGameThread;

    public Administration() {
        THREADS = new ArrayList<>();
        GAMES = Collections.synchronizedList(new ArrayList<>());
    }

    public void addGame(Game game) {

        lastAddedGameThread = new Thread(game);
        lastAddedGameThread.start();
        THREADS.add(lastAddedGameThread);

        GAMES.add(game);
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

    private void closeGame() throws IOException {
        Game finishedGame = searchForFinishedGames();

        if (finishedGame!=null) {
            closePlayers(finishedGame);

            finishedGame.getSERVERSOCKET().close();
            GAMES.remove(finishedGame);
            THREADS.remove(finishedGame);
        }
    }

    private void closePlayers(Game game) throws IOException {

        System.out.println("server.Game " + game.getSERVERSOCKET() + " finished!");
        for (Player player: game.getPLAYERS()) {
            player.write("!endGame");
            player.getSocket().close();
            player.getUSER().setInGame(false);
        }
    }

    @Override
    public void run() {

        while (true) {

            try {
                closeGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}