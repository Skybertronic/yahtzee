import java.io.*;

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
    private final Dices dices;

    private final java.net.Socket socket;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public Player(User user, java.net.Socket socket, BufferedReader bufferedReader, PrintWriter printWriter) {
        this.USER = user;
        this.socket = socket;
        this.points = new Points();
        this.dices = new Dices();

        this.bufferedReader = bufferedReader;
        this.printWriter = printWriter;
    }

                                    // reads stuff
    public String readLine() throws IOException {
        return bufferedReader.readLine();
    }

                                    // sends stuff
    public void write(String message) throws IOException {
        printWriter.println(message);
        printWriter.flush();

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

                                    // get-/ set- methods
    public User getUSER() {
        return USER;
    }

    public java.net.Socket getSocket() {
        return socket;
    }

    public Points getPoints() {
        return points;
    }

    public Dices getDices() {
        return dices;
    }
}