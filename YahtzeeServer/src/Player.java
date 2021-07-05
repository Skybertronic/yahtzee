import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

// saves all the data needed to play a game and handles the connection
public class Player {
    private final User USER;
    private final Points POINTS;

    private final BufferedReader BUFFEREDREADER;
    private final PrintWriter PRINTWRITER;

    private java.net.Socket socket;

    public Player(User user, java.net.Socket socket, BufferedReader BUFFEREDREADER, PrintWriter PRINTWRITER) {
        this.USER = user;
        this.POINTS = new Points();

        this.BUFFEREDREADER = BUFFEREDREADER;
        this.PRINTWRITER = PRINTWRITER;

        this.socket = socket;
    }

    public void writeMultipleParagraphs(String message) {

        this.write("!startPrint");
        this.write(message);
        this.write("!endPrint");
    }

    public void write(String message) {

        PRINTWRITER.println(message);

        try {
            Thread.sleep(10);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        PRINTWRITER.flush();
    }

    public String read() throws IOException {
        write("!getInput");

        return BUFFEREDREADER.readLine();
    }

    public void startTurn() {
        write("!startTurn");
    }

    public void endTurn() {
        write("!endTurn");
    }

    // manages if the player's chart is full
    public boolean hasFinished() {
        for (boolean registered : getPOINTS().getRegistered()) {
            if (!registered) {
                return false;
            }
        }
        return true;
    }

    // get-/ set- methods
    public User getUSER() {
        return USER;
    }

    public Points getPOINTS() {
        return POINTS;
    }

    public Socket getSocket() {
        return socket;
    }
}
