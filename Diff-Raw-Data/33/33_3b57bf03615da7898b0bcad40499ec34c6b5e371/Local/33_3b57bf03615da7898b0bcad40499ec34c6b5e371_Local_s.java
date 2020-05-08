 package ru.pondohva.player;
 
 import ru.pondohva.game.Field;
 import ru.pondohva.game.Game;
 
import java.util.Scanner;

 public class Local extends Player {
 
     private static Field newPole;
     private static Game newGame;
     private char[][] field;
     private char sign;
 
     public Local(Field pole, Game game, char sign) {
         this.newPole = pole;
         this.newGame = game;
         this.sign = sign;
     }
 
     public void step () {
         System.out.print("Enter i and j: ");
         Scanner sc = new Scanner(System.in);
         int x = sc.nextInt();
         int y = sc.nextInt();
         newPole.setVal(x, y, sign);
     }
 }
