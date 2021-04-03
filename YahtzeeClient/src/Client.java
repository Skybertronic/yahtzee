import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    private java.net.Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private final Scanner scanner = new Scanner(System.in);

    public Client() {

        System.out.println("Client v2.2.0 by Skybertronic");
        printRules();
    }

                                // prints rules
    public void printRules() {

        System.out.printf("%n%s%n", "Rules/Recommendations:");
        System.out.println("1. The game follows the standard rules of Yahtzee");
        System.out.println("2. Please don´t disconnect, until you have finished your game");
        System.out.println("3. Don't use spaces, it will break the code");
        System.out.println("4. The dice positions are structured like an array {0,1,2,3,4}");
        System.out.println("5. If you don't want to change dices, start your input with an \",\"");
        System.out.println("6. If you want to navigate between brackets, you have to input an non existing ID");
        System.out.println("7. I don´t know how many players the game can support, it depends on the length of the names :/");
        System.out.println("8. Due to number 6, the maximum name length is 12 characters");
        System.out.println("9. This is the first multiplayer version I created, so please try not to make inputs at the same time.. idk what will happen. ty! :D");
    }

                                // handles receiving a message
    public String read() throws IOException {
        return bufferedReader.readLine();
    }

                                // handles sending a message
    public void write(String message) {
        printWriter.println(message);
        printWriter.flush();
    }

                                // connects to the server
    public boolean connect() {
        int port;

                                // choose ip
            System.out.printf("%n%s", "IP: ");
            String ip = scanner.next();

                                // choose port/lobby
            try {
                System.out.print("Lobby: ");
                port = scanner.nextInt();

            } catch (InputMismatchException inputMismatchException) {
                System.out.printf("%n%n%s%n", "Lobby/Port is always a number!");
                return false;
            }

                                // creates connection
        try {
            socket = new java.net.Socket(ip, port);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ioException) {
            System.out.printf("%n%s%n%s%n", "Not able to connect!", "Please use the IP written in the first line of the server-log or localhost!");
            return false;
        }

        return true;
    }

    public void inGame() throws IOException {
        String receive;

        do {
            receive = read();
            switch (receive) {

                                // login commands
                case "!reconnect" -> System.out.printf("%n%s%n", "Reconnecting!");
                case "!name" -> System.out.printf("%n%s", "Name: ");
                case "!password" -> System.out.printf("%s", "Password: ");
                case "!registered" -> System.out.printf("%n%s", "New Account created!");
                case "!loginSuccessful" -> System.out.printf("%n%s", "Login successful!");
                case "!loginFailed" -> System.out.printf("%n%s%n", "Login failed, try again!");

                                // host commands
                case "!isHost" -> System.out.printf("%n%s%n", "You are the host of this game!");
                case "!addAgain" -> {
                    System.out.printf("%n%s", read() + " joined the game, add again?: ");
                    if (scanner.next().toLowerCase().contains("yes")) {
                        write("!yes");
                    }
                    else {
                        write("!no");
                    }
                }
                case "!assignType" -> System.out.printf("%n%s", "Linear or Parallel?: ");
                case "!printFinalResults" -> System.out.printf("%n%s%n%s", "The game hasn't finished yet!", "Do you want to end the game?: ");

                                // player commands
                case "!isPlayer" -> {
                    System.out.printf("%n%s", "Waiting for the game to start!");
                    while(read().equals("!startGame"));
                }

                                // common commands
                case "!startTurn" -> System.out.printf("%n%s%n", "It´s your turn!");
                case "!endTurn" -> System.out.printf("%n%s%n", "Turn finished!");
                case "!changeDices" -> System.out.printf("%n%s", "Which dices do you want to change?: ");
                case "!chooseBracket" -> System.out.printf("%n%s", "Upper or lower bracket?: ");
                case "!chooseID" -> System.out.printf("%n%s", "ID: ");
                case "!getInput" -> write(scanner.next());
                case "!wrongInput" -> System.out.printf("%n%s%n", "Wrong input!");
                case "!startPrint" -> {
                    receive = read();
                    while (!receive.equals("!endPrint")) {
                        System.out.printf("%n%s", receive);
                        receive = read();
                    }
                }
            }
        } while (!receive.equals("!endGame"));
    }

                                // closes the socket
    public void closeClient() throws IOException {

        socket.close();
        System.out.println("Socket closed!");
    }

    public static void main(String[] args) {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);

        do {
            while (!client.connect());

            try {
                client.inGame();
                client.closeClient();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            System.out.printf("%n%s", "Play again?: ");
        } while (scanner.next().toLowerCase().contains("yes"));

    }
}