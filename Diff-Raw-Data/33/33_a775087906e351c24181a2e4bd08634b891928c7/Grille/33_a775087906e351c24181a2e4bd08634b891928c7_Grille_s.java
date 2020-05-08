 package gomoku.jeu;
 
 import java.util.Set;
 import java.util.HashSet;
 import gomoku.regles.Variante;
 
 /**
  * Cette classe implémente l'interface Plateau
  */
 public class Grille implements Plateau {
 
   private int jeu[][];
   private int largeur;
   private int hauteur;
 
   public Grille(Variante jeu) {
     this.largeur = jeu.largeur();
     this.hauteur = jeu.hauteur();
     this.jeu = new int[this.largeur][this.hauteur];
     this.initialisation();
   }
 
   public void initialisation() {
     for (int i = 0; i < this.largeur; i++)
       for (int j = 0; i < this.hauteur; j++)
         this.placer(new CoordonneesPoint(i, j), Joueur.VIDE);
   }
 
   /** Largeur du jeu */
   public int largeur() {
    return this.largeur();
   }
 
   /** Hauteur du jeu */
   public int hauteur() {
    return this.hauteur();
   }
 
   /** Donne la couleur de la pierre située à la position spécifiée.
    * @param c coordonnées de la position à tester
    * @return Joueur.NOIR, Joueur.BLANC ou Joueur.VIDE */
   public int contenu(Coordonnees c) {
     return jeu[c.abscisse()][c.ordonnee()];
   }
 
   /** Place une pierre de la couleur spécifiée à la position
    * indiquée. Ne fait rien si les coordonnées sont incorrectes.
    * @param c coordonnées de la position à modifier
    * @param couleur couleur de la pierre (Joueur.NOIR ou
    * Joueur.BLANC ; on peut aussi utiliser Joueur.VIDE pour
    * supprimer une pierre) */
   public void placer(Coordonnees c, int couleur) {
    if ((c.abscisse() <= this.largeur()) &&
        (c.ordonnee() <= this.hauteur()))
     jeu[c.abscisse()][c.ordonnee()] = couleur;
   }
 
   /** Calcule les positions voisines de la position spécifiée,
    * jusqu'à la distance spécifiée
    * @param c coordonnées de la position dont on veut calculer les voisines
    * @param dist distance maximale pour calculer le voisinage
    * @return l'ensemble des coordonnées des positions voisines */
   public Set<Coordonnees> voisins(Coordonnees c, int dist) {
     Set<Coordonnees> coorPosibles = new HashSet<Coordonnees>();
     //for (int i = c.abscisse() ; ; )
     return null;
   }
 
   /** Calcule, pour le joueur de la couleur spécifiée, l'ensemble
    * des alignements de pierres de ce joueur qui ont
    * <b>exactement</b> la taille indiquée.
    * @param couleur la couleur des pierres à tester
    * @param taille le nombre de pierres qui doivent être alignées
    * @return l'ensemble des alignements touvés */
   public Set<Alignement> rechercherAlignements(int couleur, int taille) {
     Set<Alignement> alignTrouvees = new HashSet<Alignement>();
     return null;
   }
 
   /** Calcule, pour le joueur de la couleur spécifiée, l'ensemble
    * des positions où il est autorisé à jouer.
    * @param couleur la couleur du joueur
    * @return un ensemble de cases libres où le joueur peut poser une
    * pierre */
   public Set<Coordonnees> casesJouables(int couleur) {
     Set<Coordonnees> coorPosibles = new HashSet<Coordonnees>();
     return null;
   }
 
   public String toString() {
     String ret = "";
     for (int i = 0; i < this.largeur(); i++)
       for (int j = 0; j < this.hauteur(); j++)
         ret += "" + this.jeu[i][j];
     return ret;
   }
 }
