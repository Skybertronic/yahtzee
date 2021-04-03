import java.io.*;
import java.net.Socket;

// saves name and password
public class User {
    private final String name, password;
    private boolean inGame;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

                                // get-/ set- methods
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
}

                                // saves all the data needed to play a game and handles the connection
class Player {
    private final User USER;
    private final Points POINTS;

    private final BufferedReader BUFFEREDREADER;
    private final PrintWriter PRINTWRITER;

    private java.net.Socket socket;
    private boolean leftGame;

    public Player(User user, java.net.Socket socket, BufferedReader BUFFEREDREADER, PrintWriter PRINTWRITER) {
        this.USER = user;
        this.POINTS = new Points();

        this.BUFFEREDREADER = BUFFEREDREADER;
        this.PRINTWRITER = PRINTWRITER;

        this.socket = socket;
        this.leftGame = false;
    }

    public void writeMultipleParagraphs(String message) {

        PRINTWRITER.println("!startPrint");
        this.send();
        PRINTWRITER.println(message);
        this.send();
        PRINTWRITER.println("!endPrint");
        this.send();
    }

                                    // sends stuff
    public void write(String message) {

        PRINTWRITER.println(message);
        this.send();
    }

    public void send() {

        try {
            Thread.sleep(10);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        PRINTWRITER.flush();
    }

    public String read() throws IOException {

        PRINTWRITER.println("!getInput");
        send();

        return BUFFEREDREADER.readLine();
    }

    public void startTurn() {

        PRINTWRITER.println("!startTurn");
        send();
    }

    public void endTurn() {

        PRINTWRITER.println("!endTurn");
        send();
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

    public void setSOCKET(Socket socket) {
        this.socket = socket;
    }

    public boolean hasLeftGame() { return leftGame; }

    public void setLeftGame(boolean leftGame) { this.leftGame = leftGame; }
}