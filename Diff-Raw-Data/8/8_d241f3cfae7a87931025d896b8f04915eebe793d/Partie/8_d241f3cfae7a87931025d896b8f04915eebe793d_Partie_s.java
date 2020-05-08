 package gomoku.jeu;
 
 import java.util.Set;
 import gomoku.regles.Variante;
 import gomoku.regles.RegleCoup;
 import gomoku.regles.RegleAlignement;
 import gomoku.jeu.Plateau;
 import gomoku.jeu.Grille;
 import gomoku.jeu.PierreCoordonnees;
 
 public class Partie {
 
   private JoueurAbstrait jNoir;
   private JoueurAbstrait jBlanc;
   private Plateau plateau;
   private boolean premierCoup = true;
   private int doisJouer = Joueur.NOIR;
   private int gagnant;
   private String CLIouGUI;
   private Visuel visualiser;
 
   public Partie(JoueurAbstrait jNoir, JoueurAbstrait jBlanc, Plateau plateau,
       Visuel visualiser) {
     this.jNoir = jNoir;
     this.jBlanc = jBlanc;
     this.plateau = plateau;
     this.visualiser = visualiser;
   }
 
   public int getGagnant() {
     return this.gagnant;
   }
 
 
   public void jouer(Coordonnees c) {
     if (c == null)
       this.CLIouGUI = "CLI";
     else
       this.CLIouGUI = "GUI";
     String str;
     if (this.coupAjouer()) {
       if(this.demanderDeJouer(c))
         this.visualiser.afficherLaGrille();
       if (this.gagnant != 0) {
         if (this.getGagnant() == Joueur.NOIR)
           str = this.jNoir.couleurIntToString();
         else
           str = this.jBlanc.couleurIntToString();
         this.visualiser.leJoueurAGagne(str);
       } else {
         if (this.CLIouGUI.equals("CLI"))
           this.jouer(null);
       }
     } else {
       this.visualiser.laPartieEstNulle();
     }
   }
 
  /** Cette méthode vérifie s'il reste des coups à jouer
    * @return true ou false */
   public boolean coupAjouer() {
     if (!(this.jNoir.getNbCoups() == 0
           && this.jBlanc.getNbCoups() == 0))
       return true;
     return false;
   }
 
 /** Cette méthode demande les coordonnées
   *  au joueur qui a la main.
   * @return une coordonnées */
   public Coordonnees demanderCoor() {
     if (this.aLaMain(Joueur.NOIR))
       return this.jNoir.demanderCoorJoueur(this);
     else
       return this.jBlanc.demanderCoorJoueur(this);
   }
 
 /** Permet de savoir si le joueur a la main
   * @param la couleur du joueur
   * @return true si la couleur correspond au joueur qui à la main
   * false sinon.
    */
   public boolean aLaMain(int couleur) {
     return couleur == this.doisJouer ? true : false;
   }
 
   public boolean demanderDeJouer(Coordonnees c) {
     Variante v = ((Grille)plateau).getVariante();
     RegleCoup r = v.verifCoup();
     if (this.premierCoup) {
       this.premierCoup = false;
       if (c == null)
         c = this.demanderCoor();
       this.joueurJoue(c);
       return true;
     } else {
       if (c == null)
         c = this.demanderCoor();
       if (r.estValide(c, plateau) &&
         this.plateau.contenu(c) == Joueur.VIDE)
       {
         this.joueurJoue(c);
         return true;
       }
     }
     return false;
   }
 
   public void joueurJoue(Coordonnees c) {
     if (this.aLaMain(Joueur.NOIR)) {
       this.plateau.placer(c, this.jNoir.couleur());
       this.jNoir.joueUnePierre();
     }
     else {
       if (c == null)
        this.visualiser.laPartieEstNulle();
       this.plateau.placer(c, this.jBlanc.couleur());
       this.jBlanc.joueUnePierre();
     }
     this.verifierCoupGagnant();
   }
 
 /** Permet de donner la main au joueur suivant
    */
   public void donnerLaMain() {
     this.doisJouer = this.aLaMain(Joueur.NOIR) ?
       Joueur.BLANC : Joueur.NOIR;
   }
 
   public void verifierCoupGagnant() {
     Coordonnees c;
     Variante v = ((Grille)this.plateau).getVariante();
     RegleAlignement regle = v.verifAlignement();
     Set<Alignement> align = plateau.rechercherAlignements(this.doisJouer,
         regle.tailleMin());
     for (Alignement a: align) {
       if (regle.estGagnant(a, this.plateau))
         this.gagnant = this.doisJouer;
     }
     if (this.gagnant == 0) {
       this.donnerLaMain();
       if (this.aLaMain(Joueur.BLANC)) {
         if (this.blancEstUneIA()) {
           c = this.demanderCoor();
           this.joueurJoue(c);
         }
       }
     }
   }
 
 /** Permet de savoir si le joueur est une Intelligence
   * Artificielle.
   * @return true si oui, false sinon
   */
   private boolean blancEstUneIA() {
     return this.jBlanc.getClass().getName() ==
       "gomoku.jeu.JoueurCybernetique" ? true : false;
   }
 
 /** Permet de récupérer le plateau
   * @return le plateau correspondant à la partie.
    */
   public Plateau getPlateau() {
     return this.plateau;
   }
 }
