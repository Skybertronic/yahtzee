import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    private java.net.Socket socket;
    BufferedReader bufferedReader;

    public Client() {

        System.out.println("Client v2.1.1 by Skybertronic");
        printRules();
        while (!connect());
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
    public String read() throws IOException {
        String message = bufferedReader.readLine();
        return message;
    }

                                // handles sending a message
    public void write(String message) throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        printWriter.println(message);
        printWriter.flush();
    }

    public boolean connect() {
        int port;
        Scanner scanner= new Scanner(System.in);;

                                // choose ip
            System.out.print("IP: ");
            String ip = scanner.next();

                                // choose port/lobby
            try {
                System.out.print("Lobby: ");
                port = scanner.nextInt();
                System.out.println();

            } catch (InputMismatchException inputMismatchException) {
                System.out.printf("%n%s%n%n", "Lobby/Port is always a number!");
                return false;
            }

                                // creates socket based on ip and port
        try {
            socket = new java.net.Socket(ip, port);
        } catch (IOException e) {
            System.out.println("Not able to connect!\nPlease use the IP written in the first line of the server-log or localhost\n");
            return false;
        }

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return true;
    }

                                // login process | loops until name and password are valid
    public boolean login() throws IOException {
        String receive = "";
        Scanner scanner = new Scanner(System.in);
        boolean wrongInput = false;

         do {
                                // nickname
             do {
                 if (wrongInput) {
                     System.out.println("The maximal length is: " + receive);
                 }

                 System.out.print(read());
                 String name = scanner.next();
                 this.write(name);

                 receive = this.read();
                 wrongInput = !receive.equals("!acceptedInput");
             } while (wrongInput);

                                // password
             System.out.print(read());
             this.write(scanner.next());

                                // login message
             receive = read();
             switch (receive) {
                 case "!registered" -> System.out.println("New Account created!");
                 case "!loginSuccessful" -> System.out.println("Login successful!");
                 case "!loginFailed" -> System.out.println("Login failed, try again!");
             }

        } while (receive.equals("!loginFailed"));

                                // assigns role | host or not host
        receive = read();
        System.out.println(receive);
        return receive.equals("!isHost");
    }

                                // host allows other people to join
    public void hostFillsLobby() throws IOException {
        String send;
        Scanner scanner = new Scanner(System.in);

        System.out.printf("%n%s%n%n", "You are the host of this game!");

        do  {
            System.out.print(read() + " joined the game, add again?: ");
            send = scanner.nextLine();
            write(send);
        } while (send.equalsIgnoreCase("yes"));
    }

                                // host assigns type of game
    public void hostAssignGameType() throws IOException {
        String message;
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.printf("%n%s", "Linear or parallel?: ");
            message = scanner.next();
        } while (!message.equalsIgnoreCase("linear") && !message.equalsIgnoreCase("parallel"));

        write(message);
    }

                                // loops until the host assigned a game type
    public void waitingForHost() throws IOException {

        System.out.printf("%n%s%n", "Waiting for host!");
        if (!read().equals("!startGame")) {
            while (!read().equals("!startGame"));
        }
    }

                                // the game itself
    public void inGame() throws IOException {
        String receive;
        Scanner scanner = new Scanner(System.in);

            do {
                receive = read();

                                // prints full lines
                switch (receive) {
                    case "!startTurn" -> System.out.printf("%n%s%n", "It´s your turn!");
                    case "!endTurn" -> System.out.printf("%n%s%n", "Turn finished!");
                    case "!getInput" -> write(scanner.next());
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
    public void end() throws IOException {

        socket.close();
        System.out.println("Socket closed!");
    }

    public static void main(String[] args) {
        Client client = new Client();

        try {
                                // login returns host (true) or not host (false)
            if (client.login()) {
                client.hostFillsLobby();
                client.hostAssignGameType();
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