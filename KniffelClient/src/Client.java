import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    private java.net.Socket socket;
    private String name;

    public Client() {
        boolean wrongInput;
        int port = 0;
        Scanner scanner;

        System.out.println("Client v2.1.0 by Skybertronic");
        printRules();

        do {
            wrongInput = false;

                                // choose ip
            scanner = new Scanner(System.in);
            System.out.print("IP: ");
            String ip = scanner.next();

                                // choose port/lobby
            try {
                scanner = new Scanner(System.in);
                System.out.print("Lobby: ");
                port = scanner.nextInt();
                System.out.println();

            } catch (InputMismatchException inputMismatchException) {
                wrongInput = true;
                System.out.printf("%n%s%n%n", "Lobby/Port is always a number!");
            }

                    // creates socket based on ip and port
            if (!wrongInput) {
                try {
                    socket = new java.net.Socket(ip, port);
                } catch (IOException e) {
                    wrongInput = true;
                    System.out.println("Not able to connect!\nPlease use the IP written in the first line of the server-log or localhost\n");
                }

            }
        } while (wrongInput);

    }


                                // prints rules
    public void printRules() {

        System.out.println("\nRules/Recommendations:");
        System.out.println("1. The game follows the standard rules of Yahtzee");
        System.out.println("2. Please don´t disconnect, until you have finished your game");
        System.out.println("3. Don't use spaces, it will break the code");
        System.out.println("4. The dice positions are structured like 0,1,2,3,4");
        System.out.println("5. If you don't want to change dices start your input with an \",\"");
        System.out.println("6. If you want to navigate between brackets, you have to input an non existing ID");
        System.out.println("7. I don´t know how many players the game can support, it depends on the length of the names :/");
        System.out.println("8. This is the first multiplayer version I created, so please try not to make inputs at the same time.. ty :D\n");
    }

                                // handles receiving a message
    public String readLine() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return bufferedReader.readLine();
    }

                                // handles sending a message
    public void write(String message) throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        printWriter.println(message);
        printWriter.flush();
    }

                                // login process | loops until name and password are valid
    public boolean login() throws IOException {
        String receive;
        Scanner scanner1 = new Scanner(System.in), scanner2 = new Scanner(System.in);

         do {
                                // nickname
             System.out.print(readLine());
             name = scanner1.next();
             write(name);

                                // password
             System.out.print(readLine());
             write(scanner2.next());

                                // login message
             receive = readLine();
             switch (receive) {
                 case "!registered" -> System.out.println("New Account created!");
                 case "!loginSuccessful" -> System.out.println("Login successful!");
                 case "!loginFailed" -> System.out.println("Login failed, try again!");
             }

        } while (receive.equals("!loginFailed"));

                                // assigns role | host or not host
        receive = readLine();
        return receive.equals("!isHost");
    }

                                // host allows other people to join
    public void fillLobby() throws IOException {
        String send;
        Scanner scanner = new Scanner(System.in);

        System.out.printf("%n%s%n%n", "You are the host of this game!");

        do  {
            System.out.print(readLine());
            send = scanner.nextLine();
            write(send);
        } while (send.equalsIgnoreCase("yes"));
    }

                                // host assigns type of game
    public void assignGameType() throws IOException {
        String message;
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        do {
            System.out.print("Linear or parallel?: ");
            message = scanner.next();
        } while (!message.equalsIgnoreCase("linear") && !message.equalsIgnoreCase("parallel"));

        write(message);
    }

                                // loops until the host assigned a game type
    public void waitingForHost() throws IOException {

        System.out.printf("%n%s%n", "Waiting for host!");
        if (!readLine().equals("!startGame")) {
            while (!readLine().equals("!startGame"));
        }
    }

                                // the game itself
    public void inGame() throws IOException {
        String receive;
        boolean writeable = false;
        Scanner scanner = new Scanner(System.in);

        try {
            do {
                receive = readLine();

                                // prints full lines
                if (receive.equals("!startPrintLn")) {
                    System.out.println();
                    receive = readLine();
                    while (!receive.equals("!endPrintLn")) {
                        System.out.println(receive);
                        receive = readLine();
                    }
                }
                                // prints open lines
                else if (receive.equals("!startPrint")) {
                    System.out.println();
                    receive = readLine();
                    while (!receive.equals("!endPrint")) {
                        System.out.print(receive);
                        receive = readLine();
                    }
                }
                                // allows input
                else if (receive.equals("!getInput") && writeable) {
                    write(scanner.next());
                }
                                // indicates the start of the turn
                else if (receive.equals("!startTurn" + name)) {
                    System.out.printf("%n%s%n", "It´s your turn!");
                    writeable = true;
                }
                                // indicates the end of the turn
                else if (receive.equals("!endTurn" + name)) {
                    System.out.printf("%n%s%n", "Turn finished!");
                    writeable = false;
                }
                                // after the game ended
                else if (receive.equals("!waitingForResults")) {
                    writeable = true;
                }

            } while (!receive.equals("!endGame"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

                                // closes the socket
    public void end() throws IOException {

        socket.close();
        System.out.println("Socket closed!");
    }

    public static void main(String[] args) {
        Client client = new Client();

        try {
                                // login returns host (true) or not host (false)
            if (client.login()) {
                client.fillLobby();
                client.assignGameType();
            }
            else {
                client.waitingForHost();
            }

            client.inGame();

            client.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}