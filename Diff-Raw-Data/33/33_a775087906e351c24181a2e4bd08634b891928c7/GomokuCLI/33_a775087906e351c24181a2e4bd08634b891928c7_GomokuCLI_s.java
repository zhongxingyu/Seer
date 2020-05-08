 package gomoku.cli;
 
 import gomoku.jeu.Grille;
 import gomoku.regles.Gomoku;
 
 public class GomokuCLI {
 
   public static void main (String[] args) {
     Grille g = new Grille(new Gomoku());
    System.out.println(g.toString());
   }
 }
