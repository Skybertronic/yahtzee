import java.io.*;
import java.net.Socket;

public class Lobby implements Runnable {
    private final Game GAME;
    private final Player PLAYER;

    public Lobby(Game game, Player player) {
        this.GAME = game;
        this.PLAYER = player;
    }

    public void write(PrintWriter printWriter, String message) {

        System.out.println("LOBBY: " + message);

        printWriter.println(message);   // DEBUG
        printWriter.flush();

        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public String read(PrintWriter printWriter, BufferedReader bufferedReader) throws IOException {

        try {
            Thread.sleep(10);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        printWriter.println("!getInput");
        printWriter.flush();

        return bufferedReader.readLine();
    }

    private boolean reconnect() throws IOException {
        final int MAX_PLAYER_NAME_LENGTH = 12;
        String name, password;

        System.out.println("ACCEPT");   // DEBUG

        Socket socket = GAME.getSERVERSOCKET().accept();

        BufferedReader bufferedReader;
        PrintWriter printWriter;

                                // temporary connection
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                                // input name
        boolean wrongInput;
        write(printWriter, "!reconnect");

        do {
            wrongInput = false;

            write(printWriter, "!name");
            name = read(printWriter, bufferedReader);

            if (name.length() > MAX_PLAYER_NAME_LENGTH) {
                write(printWriter, "!wrongInput");
                wrongInput = true;
            }
        } while (wrongInput);

                                // input password
        write(printWriter, "!password");
        password = read(printWriter, bufferedReader);

        if (PLAYER.getUSER().getName().equalsIgnoreCase(name)){

            if (PLAYER.getUSER().getPassword().equals(password)) {

                                 // login successful
                PLAYER.setSOCKET(socket);
                System.out.println("Login Successful!");
                PLAYER.write("!loginSuccessful");
                PLAYER.setLeftGame(false);
                return true;
            }

                                // wrong password
            write(printWriter, "!loginFailed");
        }

        return false;
    }

    public Game getGAME() {
        return GAME;
    }

    public Player getPLAYER() {
        return PLAYER;
    }

    @Override
    public void run() {
        try {

            while (reconnect());

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
