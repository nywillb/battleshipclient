import javax.xml.bind.SchemaOutputResolver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.InputMismatchException;

public class BattleshipClient {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Battleship!");
        System.out.println("Please select a server...");
        String server = scanner.nextLine();
        try {
            Socket skt = new Socket(server, 2424);
            skt.setTcpNoDelay(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            PrintWriter out = new PrintWriter(skt.getOutputStream(), true);

            boolean playing = true;
            while(playing) {
                System.out.println("1. HOST GAME | Hosting a game means that you go first. There can only be 1 host per game");
                System.out.println("2. JOIN GAME | Join a game if someone is already hosting it");
                System.out.println("3. EXIT      | Stop playing battleship");
                int mode;
                while(true) {
                    try {
                        mode = scanner.nextInt();
                        break;
                    } catch (InputMismatchException e) {
                        scanner.next(); //need so no infinite loop
                    }
                }
                if(mode == 1) {
                    out.println("GETGAMECODE");
                    String gamecode = in.readLine();
                    System.out.println("Give this gamecode to the person you want to play against: " + gamecode.split(" ")[1]);
                } else if (mode ==2 ){
                    System.out.println("Please type your gamecode.");
                    String gamecode = scanner.next();
                    System.out.println("Joining game...");
                    out.println("JOIN " + gamecode);
                    String joinStatus = in.readLine();
                    if(joinStatus.equals("JOIN fail")){
                        System.out.println("That doesn't seem to be a valid gamecode. Please try again.");
                        continue;
                    } else {
                        System.out.println("Game joined!");
                    }
                } else if (mode == 3) {
                    System.out.println("Goodbye :(");
                    playing = false;
                    break;
                }
                System.out.println("Let's get your board setup!");
                System.out.println("• | Water");
                System.out.println("S | Ship");
                System.out.println("You can place 5 ships.");
                String[][] myBoard = {
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"}
                };
                String sendboard = "SENDBOARD";
                for(int i = 1; i<=5; i++){
                    System.out.println("Here is your board:");
                    printBoard(myBoard);
                    System.out.println("Place ship number " + i + ":");
                    int x= getNum("x");
                    sendboard = sendboard + " " + x;
                    int y = getNum("y");
                    sendboard = sendboard + " " + y;
                    myBoard[x][y] = "S";
                }
                out.println(sendboard);
                System.out.println("Waiting for opponent...");
                String[][] opponentBoard = {
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"},
                        {"•", "•", "•", "•", "•"}
                };
                String first = in.readLine();
                if(first.equals("MOVEFIRST no")){
                    System.out.println("It is your opponent's turn! Waiting for them to make their move...");
                    String strike = in.readLine();
                    System.out.println(strike);
                    int strikeX = Integer.parseInt(strike.split(" ")[2]);
                    int strikeY = Integer.parseInt(strike.split(" ")[3]);
                    if(myBoard[strikeX][strikeY].equals("S")){
                        System.out.println("YOUR SHIP HAS BEEN HIT!");
                        myBoard[strikeX][strikeY] = "H";
                        System.out.println("Here are your ships:");
                        printBoard(myBoard);
                    } else {
                        System.out.println("MISS!");
                        System.out.println("Here are your ships:");
                        printBoard(myBoard);
                    }
                }
                boolean gameActive = true;
                while(gameActive) {
                    System.out.println("It's your turn! Here are your opponent's waters:");
                    printBoard(opponentBoard);
                    System.out.println("Where would you like to attack?");
                    int attackX = getNum("x");
                    int attackY = getNum("y");
                    out.println("MOVE " + attackX + " " + attackY);
                    String moveResult = in.readLine();
                    if(moveResult.equals("MOVE hit " + attackX + attackY)){
                        System.out.println("HIT!");
                        opponentBoard[attackX][attackY] = "H";
                    } else {
                        System.out.println("MISS!");
                        opponentBoard[attackX][attackY] = "M";
                    }
                    System.out.println("It is your opponent's turn! Waiting for them to make their move...");
                    String strike = in.readLine();
                    if (strike.equals("GAMEEND win")){
                        System.out.println("Congrats! You won!");
                        break;
                    } else if(strike.equals("GAMEEND lose")){
                        System.out.println("Sorry, you lost.");
                        break;
                    }
                    if(gameActive == true) {
                        int strikeX = Integer.parseInt(strike.split(" ")[2]);
                        int strikeY = Integer.parseInt(strike.split(" ")[3]);
                        if (myBoard[strikeX][strikeY].equals("S")) {
                            System.out.println("YOUR SHIP HAS BEEN HIT!");
                            myBoard[strikeX][strikeY] = "H";
                            printBoard(myBoard);
                        }
                    }
                }
            }
        } catch(java.io.IOException e){
            System.out.println("Uh oh.. (IOException)");
        }
    }
    private static void printBoard(String[][] board) {
        System.out.println(" |1|2|3|4|5|");
        int currentrow = 1;
        for(String[] row : board) {
            System.out.println("- ----------");
            System.out.print(currentrow + "|");
            for (String slot : row) {
                System.out.print(slot + "|");
            }
            System.out.println();
            currentrow++;

        }
        System.out.println("- ----------");
    }
    private static int getNum(String prefixQuery) {
        Scanner scanner = new Scanner(System.in);
        int exitValue;
        while (true){
            try {
                System.out.print(prefixQuery + " = ");
                exitValue = scanner.nextInt();
            } catch (InputMismatchException e) { // catch for if the user doesn't type an integer
                scanner.next(); // reset scanner token by moving to the next line so we don't end up with an
                // infinite loop.
                System.out.println("You must type an integer between 1 and 5 here.");
                continue;
            }
            if (exitValue > 5 || exitValue < 1) {
                System.out.println("You must type an integer between 1 and 5 here.");
            } else {
                exitValue--;
                return exitValue;
            }
        }
    }
}
