import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
