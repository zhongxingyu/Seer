 package gomoku.cli;
 
 import gomoku.regles.*;
 import gomoku.jeu.*;
 
 public class GomokuCLI {
 
   public GomokuCLI(JoueurAbstrait j1, JoueurAbstrait j2,String type) {
   
     Plateau plateau;
 
     if(type == "Morpion")
       plateau = new Grille(new Morpion());
     else
       plateau = new Grille(new Gomoku());
 
     Partie partie = new Partie(j1, j2, plateau, new VisuelCLI(plateau));
   }
 }
