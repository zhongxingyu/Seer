 package gomoku.jeu ;
 
 /** Implémentation de l'interface coordonnées */
 public class CoordonneesPoint implements Coordonnees {
 
  //coordonnées point
  private abs, ord;
 
   public CoordonneesPoint(int x, int y) {
     this.abs = x;
     this.ord = y;
   }
 
   public int abscisse () {
     return this.abs;
   }
 
   public int ordonnee () {
     return this.ord;
   }
 	
 }
