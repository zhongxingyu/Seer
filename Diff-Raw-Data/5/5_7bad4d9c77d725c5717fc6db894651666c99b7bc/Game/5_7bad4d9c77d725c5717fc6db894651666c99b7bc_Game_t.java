 /**
  * Created with IntelliJ IDEA.
  * User: Notandi
  * Date: 12.11.2012
  * Time: 17:02
  */
 
 import java.util.Scanner;
 
 public class Game {
 
     public User player1;
     public User player2;
     public Board board;
 
     public Game() {
         setPlayers();
 
         board = new Board();
 
         loop();
     }
 
     public void setPlayers() {
         Scanner in = new Scanner(System.in);
         System.out.println("Enter name of player 1:");
 
         String name;
         name = in.nextLine();
         player1 = new User(name, 'X');
 
 
         System.out.println("Enter name of player 2:");
         name = in.nextLine();
         player2 = new User(name, 'O');
 
         //in.close();
     }
 
 
     public void loop() {
         boolean isPlayer1 = true;
         User user;
         int moveCount = 0;
         char winner = '.';
         Scanner in = new Scanner(System.in);
 
         while (winner == '.') {
 
             user = (isPlayer1) ? player1 : player2;
             int cell;
             boolean valid = true;
 
             do {
                 System.out.println(board);
 
                if (!valid){
                     System.out.println("Input not valid!");
                    moveCount--;
                }
                 System.out.println(user.getName() + " - pick a cell 1-9:");
 
                 cell = in.nextInt();
                 valid = board.updateBoard(user.getSign(), cell);
                 moveCount++;
                 winner = board.checkWinner(user.getSign(), cell, moveCount);
             } while (!valid);
 
 
             isPlayer1 = !isPlayer1;
         }
 
         System.out.println(board);
         if(winner == 'D')
             System.out.println("Draw");
         else
             System.out.println("Winner: " + winner);
 
         in.close();
     }
 
     public static void main(String[] args) {
         Game g = new Game();
     }
 }
