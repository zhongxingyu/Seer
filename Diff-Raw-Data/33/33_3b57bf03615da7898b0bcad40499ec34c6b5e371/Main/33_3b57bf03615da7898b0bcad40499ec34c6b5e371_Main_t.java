 package ru.pondohva.main;
 
 import ru.pondohva.game.*;
 import java.util.Scanner;
 
 public class Main {
 
     public static void main(String[] args) {
         Game newGame;
         System.out.println("1. Computer vs. Computer");
         System.out.println("2. Player vs. Computer");
         System.out.println("3. Player vs. Player (1 computer)");
         System.out.println("4. Player vs. Player (over LAN)");
         System.out.print("Enter 1-4 to play the game: ");
         Scanner sc = new Scanner(System.in);
         int choose = sc.nextInt();
         switch (choose) {
             case 1:
                 newGame = new Game("Computer", "Computer", 3);
                 break;
             case 2:
                 newGame = new Game("Local", "Computer", 3);
                 break;
             case 3:
                 newGame = new Game("Local", "Local", 3);
                 break;
             case 4:
                 newGame = new Game("Local", "Remote", 3);
                 break;
             default:
                 return;
         }
         newGame.startGame();
     }
 }
