/*
* Version 2021.01.11
* By Tim Miguletz
* */

import socketio.Socket;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    private final String HOST = "localhost" ;
    private final int PORT;
    private Socket socket;

    private String name;

    public Client() {

                                // Port auswählen
        Scanner scanner = new Scanner(System.in);
        System.out.print("Port: ");
        PORT = scanner.nextInt();

                                // Initialisierung Port
        try {
            socket = new Socket(HOST, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.connect();
    }

                                // Sorgt für den Login und gibt zurück, ob der Client der Host seines Spiels ist
    public boolean login() throws IOException {
        String send, receive;
        Scanner scanner1 = new Scanner(System.in), scanner2 = new Scanner(System.in);

         do {
                                // Nickname
             receive = socket.readLine();
             System.out.print(receive);
             name = scanner1.next();
             socket.write(name + "\n");

                                // Passwort
             receive = socket.readLine();
             System.out.print(receive);
             send = scanner2.nextLine();
             socket.write(send + "\n");

                                // Antwort
             receive = socket.readLine();
             switch (receive) {
                 case "!registered" -> System.out.println("New Account created!");
                 case "!loginSuccessful" -> System.out.println("Logged in!");
                 case "!loginFailed" -> System.out.println("Login failed, try again!");
             }

        } while (receive.equals("!loginFailed"));

                                // Host / Player
        receive = socket.readLine();
        return receive.equals("!isHost");
    }

                                // Wird nur als Host aufgerufen. Steuert das wiederaufrufen der loginPlayer Methode (in Setup)
    public void fillLobby() throws IOException {
        String send;
        Scanner scanner = new Scanner(System.in);

        System.out.printf("%n%s%n%n", "You are the host of this game!");

        do  {
            System.out.print(socket.readLine());
            send = scanner.nextLine();
            socket.write(send + "\n");
        } while (send.equalsIgnoreCase("yes"));
    }

                                // Wird nur als Host aufgerufen. Weist Game einen Typ zu.
    public void assignGameType() throws IOException {
        String message;
        Scanner scanner = new Scanner(System.in);

        System.out.println();     // Formatierung
        do {
            System.out.print("Linear or parallel?: ");
            message = scanner.next();
        } while (!message.equalsIgnoreCase("linear") && !message.equalsIgnoreCase("parallel"));

        socket.write(message + "\n");
    }

                                // Wird nur als Player aufgerufen
    public void waitingForHost() throws IOException {

        System.out.printf("%n%s%n", "Waiting for host!");
        if (!socket.readLine().equals("!startGame")) {
            while (!socket.readLine().equals("!startGame"));
        }
    }

                                // Hilft dabei, dass keine Fehler passieren
    public void printRules() {

        System.out.println("\nRules/Recommendations:");
        System.out.println("1. Don't use spaces");
        System.out.println("2. The dice position is structured like an array {0,1,2,3,4}");
        System.out.println("3. If you don't want to change dices, you have to conform the position with an \",\"");
    }

    public void ingame() throws IOException {
        String receive;
        boolean writeable = false;
        Scanner scanner = new Scanner(System.in);

        try {
            do {
                receive = socket.readLine();

                                // Gibt ganze Zeilen aus
                if (receive.equals("!startPrintLn")) {
                    System.out.println();     // Formatierung
                    receive = socket.readLine();
                    while (!receive.equals("!endPrintLn")) {
                        System.out.println(receive);
                        receive = socket.readLine();
                    }
                }
                                // Gibt eine Zeile aus (für Eingaben)
                else if (receive.equals("!startPrint")) {
                    System.out.println();     // Formatierung
                    receive = socket.readLine();
                    while (!receive.equals("!endPrint")) {
                        System.out.print(receive);
                        receive = socket.readLine();
                    }
                }
                                // Die eigentliche Eingabe
                else if (receive.equals("!getInput") && writeable) {
                    socket.write(scanner.next() + "\n");
                }
                                // Beginnt den Zug des Spielers
                else if (receive.equals("!startTurn" + name)) {
                    System.out.printf("%n%s%n", "It´s your turn!");
                    writeable = true;
                }
                                // Beendet den Zug des Spielers
                else if (receive.equals("!endTurn" + name)) {
                    System.out.printf("%n%s%n", "Turn finished!");
                    writeable = false;
                }
                else if (receive.equals("!waitingForResults")) {
                    writeable = true;
                }

            } while (!receive.equals("!endGame"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void end() throws IOException {

        socket.close();
        System.out.println("Socket closed!");
    }

    public static void main(String[] args) {
        Client client = new Client();

        try {
            if (client.login()) {
                client.fillLobby();
                client.assignGameType();
            }
            else {
                client.waitingForHost();
            }

            client.printRules();

            client.ingame();

            client.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
