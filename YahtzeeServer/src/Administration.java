import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

                                // administrates games
public class Administration implements Runnable {
    private final ArrayList<Thread> THREADS;
    private final List<Game> GAMES;

    public Administration() {
        THREADS = new ArrayList<>();
        GAMES = Collections.synchronizedList(new ArrayList<>());
    }

                                // closes the socket of games, where running equals false
    private Game searchForFinishedGames() throws IOException {
        while (true) {
            for (Game game: GAMES) {
                if (!game.isRunning()) {
                    System.out.println("server.Game " + game.getSERVERSOCKET() + " finished!");
                    for (Player player: game.getPLAYER()) {
                        player.write("!endGame");

                        player.getSocket().close();
                        player.getUSER().setInGame(false);
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