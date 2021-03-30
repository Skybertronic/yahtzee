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
    private final Points points;

    private final java.net.Socket socket;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public Player(User user, java.net.Socket socket, BufferedReader bufferedReader, PrintWriter printWriter) {
        this.USER = user;
        this.socket = socket;
        this.points = new Points();

        this.bufferedReader = bufferedReader;
        this.printWriter = printWriter;
    }

    public String read() throws IOException {
        return bufferedReader.readLine();
    }

    public void writeMultipleParagraphs(String message) {

        printWriter.println("!startPrint");
        this.send();
        printWriter.println(message);
        this.send();
        printWriter.println("!endPrint");
        this.send();
    }

                                    // sends stuff
    public void write(String message) {

        System.out.println("PlayerTestSend "+ message);     // DEBUG

        printWriter.println(message);
        this.send();
    }

    public void send() {

        try {
            Thread.sleep(10);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        printWriter.flush();
    }

    public String getInput() throws IOException {

        printWriter.println("!getInput");
        send();

        return read();
    }

    public void startTurn() {

        printWriter.println("!startTurn");
        send();
    }

    public void endTurn() {

        printWriter.println("!endTurn");
        send();
    }

                                    // get-/ set- methods
    public User getUSER() {
        return USER;
    }

    public Points getPoints() {
        return points;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }
}