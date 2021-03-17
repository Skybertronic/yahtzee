import socketio.Socket;

public class User {
    private String name, password;
    private boolean ingame;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

                                // Get-/ Set- Methoden
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean isIngame() {
        return ingame;
    }

    public void setIngame(boolean ingame) {
        this.ingame = ingame;
    }
}

class Player {
    private User user;
    private Socket socket;

    private Points points;
    private Dices dices;

    public Player(User user, Socket socket) {
        this.user = user;
        this.socket = socket;
        points = new Points();
        dices = new Dices();
    }
                                // Get-/ Set- Methoden
    public User getUser() {
        return user;
    }

    public Socket getSocket() {
        return socket;
    }

    public Points getPoints() {
        return points;
    }

    public Dices getDices() {
        return dices;
    }
}