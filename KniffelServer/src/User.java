import java.io.*;

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

class Player {
    private final User user;
    private final Points points;
    private final Dices dices;

    private final java.net.Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    public Player(User user, java.net.Socket socket) {
        this.user = user;
        this.socket = socket;
        this.points = new Points();
        this.dices = new Dices();

        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
    public User getUser() {
        return user;
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