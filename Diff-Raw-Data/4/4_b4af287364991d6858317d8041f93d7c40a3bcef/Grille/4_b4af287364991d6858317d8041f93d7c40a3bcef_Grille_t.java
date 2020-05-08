 package gomoku.jeu;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Iterator;
 import gomoku.regles.Variante;
 import gomoku.jeu.Alignement;
 
 /**
  * Cette classe implémente l'interface Plateau
  */
 public class Grille implements Plateau {
 
   private int jeu[][];
   private int largeur;
   private int hauteur;
   private Variante v;
 
   public Grille(Variante jeu) {
     this.v = jeu;
     this.largeur = jeu.largeur();
     this.hauteur = jeu.hauteur();
     this.jeu = new int[this.largeur][this.hauteur];
   }
 
   /** Largeur du jeu */
   public int largeur() {
     return this.largeur;
   }
 
   /** Hauteur du jeu */
   public int hauteur() {
     return this.hauteur;
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
     if (c.abscisse() < this.largeur &&
         c.ordonnee() < this.hauteur)
     jeu[c.abscisse()][c.ordonnee()] = couleur;
   }
 
   /** Calcule les positions voisines de la position spécifiée,
    * jusqu'à la distance spécifiée
    * @param c coordonnées de la position dont on veut calculer les voisines
    * @param dist distance maximale pour calculer le voisinage
    * @return l'ensemble des coordonnées des positions voisines */
   public Set<Coordonnees> voisins(Coordonnees c, int dist) {
     Set<Coordonnees> coorVoisines = new HashSet<Coordonnees>();
    for (int i = c.abscisse() - dist; i <= c.abscisse() + dist; i++)
      for (int j = c.ordonnee() - dist; j <= c.ordonnee() + dist; j++)
         if ((i >= 0 && i < this.largeur) && (j >= 0 && j < this.hauteur))
           coorVoisines.add(new PierreCoordonnees(i, j));
     return coorVoisines;
   }
 
   /** Calcule, pour le joueur de la couleur spécifiée, l'ensemble
    * des alignements de pierres de ce joueur qui ont
    * <b>exactement</b> la taille indiquée.
    * @param couleur la couleur des pierres à tester
    * @param taille le nombre de pierres qui doivent être alignées
    * @return l'ensemble des alignements touvés */
   public Set<Alignement> rechercherAlignements(int couleur, int taille) {
     Set<Alignement> alignTrouvees = new HashSet<Alignement>();
     alignTrouvees = this.concatenerLesHashSet(alignTrouvees,
         this.rechercherAlignementsVertical(couleur, taille));
     alignTrouvees = this.concatenerLesHashSet(alignTrouvees,
       this.rechercherAlignementsHorizontal(couleur, taille));
     alignTrouvees = this.concatenerLesHashSet(alignTrouvees,
       this.rechercherAlignementsDiagonalDes(couleur, taille));
     alignTrouvees = this.concatenerLesHashSet(alignTrouvees,
       this.rechercherAlignementsDiagonalAsc(couleur, taille));
     return alignTrouvees;
   }
 
   private Set<Alignement> concatenerLesHashSet(Set<Alignement> englobante, Set<Alignement> englobee) {
     Iterator<Alignement> it = englobee.iterator();
     while (it.hasNext())
       englobante.add(it.next());
     return englobante;
   }
 
   public Set<Alignement> rechercherAlignementsVertical(int couleur, int taille) {
     int cpt, x, y;
     Set<Alignement> alignV = new HashSet<Alignement>();
     for (x = 0; x < this.largeur; x++) {
       cpt = 0;
       for (y = 0; y < this.hauteur; y++) {
         if (this.contenu(new PierreCoordonnees(x, y)) == couleur) {
           cpt++;
         } else {
           cpt = 0;
         }
       }
       if (cpt == taille) {
         alignV.add(new VecteurAlignement(new PierreCoordonnees(x, y - taille),
               new PierreCoordonnees(x, y),
               this.v));
       }
     }
     return alignV;
   }
 
   public Set<Alignement> rechercherAlignementsHorizontal(int couleur, int taille) {
     int cpt, x, y;
     Set<Alignement> alignH = new HashSet<Alignement>();
     for (y = 0; y < this.hauteur; y++) {
       cpt = 0;
       for (x = 0; x < this.largeur; x++) {
         if (this.contenu(new PierreCoordonnees(x, y)) == couleur) {
           cpt++;
         } else {
           cpt = 0;
         }
       }
       if (cpt == taille) {
         alignH.add(new VecteurAlignement(new PierreCoordonnees(x - taille, y),
                           new PierreCoordonnees(x, y),
                           this.v));
       }
     }
     return alignH;
   }
 
   public Set<Alignement> rechercherAlignementsDiagonalDes(int couleur, int taille) {
 
     int cpt = 0;
     Coordonnees coor[] = new Coordonnees[taille];
     Set<Alignement> alignD = new HashSet<Alignement>();
 
     // parcourt de la première moitiée
     for (int x = 0; x < this.largeur; x++)
       for (int y = 0; y < x+1; y++)
         if (this.contenu(new PierreCoordonnees(x-y, y)) == couleur) {
           cpt++;
           coor[cpt] = new PierreCoordonnees(x-y, y);
           if (cpt == taille) {
             alignD.add(new VecteurAlignement(coor[0], coor[cpt], this.v));
           } else {
             cpt = 0;
             for (int i = 0; i < coor.length; i++)
               coor[i] = null;
           }
         }
 
     // parcourt de la deuxieme moitiée
     for (int y = 0; y < this.hauteur-2; y++)
       for (int x = this.largeur-1; x > y; x--)
         if (this.contenu(new PierreCoordonnees(x, this.largeur - (x - y))) == couleur) {
           cpt++;
           coor[cpt] = new PierreCoordonnees(x, this.largeur - (x - y));
           if (cpt == taille) {
             alignD.add(new VecteurAlignement(coor[0], coor[cpt], this.v));
           } else {
             cpt = 0;
             for (int i = 0; i < coor.length; i++)
               coor[i] = null;
           }
         }
     return alignD;
   }
 
   public Set<Alignement> rechercherAlignementsDiagonalAsc(int couleur, int taille) {
 
     int cpt = 0;
     Coordonnees coor[] = new Coordonnees[taille];
     Set<Alignement> alignD = new HashSet<Alignement>();
 
     // parcourt de la première moitiée
     for (int y = 0; y < this.largeur; y++)
       for (int x = 0; x < y+1; x++)
         if (this.contenu(new PierreCoordonnees(y-x, x)) == couleur) {
           cpt++;
           coor[cpt] = new PierreCoordonnees(y-x, x);
           if (cpt == taille) {
             alignD.add(new VecteurAlignement(coor[0], coor[cpt], this.v));
           } else {
             cpt = 0;
             for (int i = 0; i < coor.length; i++)
               coor[i] = null;
           }
         }
 
     // parcourt de la deuxieme moitiée
     for (int x = 0; x < this.hauteur-2; x++)
       for (int y = this.largeur-1; y > x; y--)
         if (this.contenu(new PierreCoordonnees(y, this.largeur - (y - x))) == couleur) {
           cpt++;
           coor[cpt] = new PierreCoordonnees(y, this.largeur - (y - x));
           if (cpt == taille) {
             alignD.add(new VecteurAlignement(coor[0], coor[cpt], this.v));
           } else {
             cpt = 0;
             for (int i = 0; i < coor.length; i++)
               coor[i] = null;
           }
         }
 
     return alignD;
   }
 
   /** Calcule, pour le joueur de la couleur spécifiée, l'ensemble
    * des positions où il est autorisé à jouer.
    * @param couleur la couleur du joueur
    * @return un ensemble de cases libres où le joueur peut poser une
    * pierre */
   public Set<Coordonnees> casesJouables(int couleur) {
     int contenuCouleur;
     Set<Coordonnees> coorJouables = new HashSet<Coordonnees>();
     for (int x = 0; x < this.largeur; x++)
       for (int y = 0; y < this.hauteur; y++)
         if (this.contenu(new PierreCoordonnees(x, y)) == couleur) {
           Set<Coordonnees> coorVoisines = this.voisins(new PierreCoordonnees(x, y), couleur);
           Iterator<Coordonnees> it = coorVoisines.iterator();
           while (it.hasNext())
             if (this.contenu(new PierreCoordonnees(x, y)) == Joueur.VIDE)
               coorJouables.add(new PierreCoordonnees(x, y));
         }
     return coorJouables;
   }
 
   /**
    * Methode toString.
    * Affiche le tableau dans la console
    */
   public String toString() {
     String ret = "";
     for (int x = 0; x < this.largeur(); x++) {
       ret += " ----------------------------------------" +
         "-------------------------------------\n";
       for (int y = 0; y < this.hauteur(); y++) {
         ret += " | " + this.jeu[x][y];
       }
       ret +=  " | \n";
     }
     ret += " ----------------------------------------" +
       "-------------------------------------\n";
     return ret;
   }
 
   public Variante getVariante() {
     return v;
   }
 }
