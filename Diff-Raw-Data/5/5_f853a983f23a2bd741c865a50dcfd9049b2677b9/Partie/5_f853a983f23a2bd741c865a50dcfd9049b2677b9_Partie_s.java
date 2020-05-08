 import java.lang.*;
 import java.util.*;
 
 
 /**
 * Une partie est simplement un serveur qui attend des connexions puis attend les ordres de la part de joueurs et les redistribue a tous les clients (joueurs, ia, spectateurs) Le createur de la partie possede donc le serveur de la partie. Le serveur de partie possede son propre plateau de jeu sur lequel il effectue les modifications.
 */
 public class Partie {
 	protected ArrayList<Joueur> joueurs; // permet de gerer plus de 2 joueurs.
 	/** 61 cases contenant soit une bille soit null (empty)*/
 	public Plateau plateau; 
 	/** Stocke le numero joueur qui doit jouer (1 = NOIR, 2 = BLANC) */
 	protected int joueurActuel;
 /**
  * terminee est effectivement utile car on peut changer les conditions de victoire a l'interieur d'une partie.	
  */
 	protected boolean terminee;
 	protected String variante;
 	/** Le numero du coup*/
 	protected int numCoup;
 	protected final byte NB_BILLES_EJECTER = 1; 
 	/** Score actuel de chaque joueur*/
 	protected int[] score;
 	/** MODE SANS SERVEUR*/
 	public FenetreJeu f;
 	/** Joueur 1 : le noir*/
 	public final static int NOIR = 1;
 	/** Joueur 2: le blanc*/
 	public final static int BLANC = 2;
 	/** Le gagnant de la partie, a 0 si personne n'a encore gagne*/
 	protected int gagnant = 0;
 	//
 	public ClickAction listener;
 	
 	public Partie() {
 		joueurActuel = 1;
 		numCoup = 1;
 		terminee = false;
 		score = new int[2];
 		score[NOIR-1] = 0;
 		score[BLANC-1] = 0;
 		plateau = new Plateau(); // initialise les valeurs des vecteurs
 		listener = new ClickAction(this);
 		f = new FenetreJeu(plateau,listener);
 	}
 	//--------------------------------------ACCESSEURS-----------------------------------
 			/** Renvoie le numero du joueur en cour
 			* @return le numero du jouer actuel (1 pour le J1 NOIR et 2 pour le J2 BLANC)
 			*/
 			public int getJoueurActuel(){
 				return joueurActuel;
 			}
 			/** Change le jouer actuel pour savoir qui doit jouer*/
 			public void setJoueur(){
 				if(joueurActuel == NOIR)
 					joueurActuel = BLANC;
 				else
 					joueurActuel = NOIR;
 			}
 	
 			/** Renvoie le score du joueur demande
 			* @param numJ le numero du joueur dont l'on veut connaitre le score
 			* @return le score du joueur demande
 			*/
 			public int getScore(int numJ){
 				if(numJ == NOIR)
 					return score[NOIR-1];
 				else
 					return score[BLANC-1];
 			}
 			/** Vérifie le plateau pour savoir si une bille est tombé au dernier coup et incremente le score*/
 			public void setScore(){
 				//Des qu'une bille est tombee incremente le score
 				//Si une bille blanche est dans le trou, on incrémente le score du joueur noir
 				if(this.plateau.cases[Plateau.TROU].getContenu() == Bille.BLANC) {
					if((this.score[NOIR] += 1) == this.NB_BILLES_EJECTER) {
 						this.terminee = true;
 						this.gagnant = NOIR;
 					}
 							
 				}else	if(this.plateau.cases[Plateau.TROU].getContenu() == Bille.NOIR) {
						if((this.score[BLANC] += 1) == this.NB_BILLES_EJECTER) {
 							this.terminee = true;
 							this.gagnant = BLANC;
 						}
 
 					}
 				
 				//On revide la case trou comme le score à été pris en compte
 				this.plateau.cases[Plateau.TROU].setContenu(Case.NEANT);
 			}
 		
 			/** Renvoie le numero du coup en cour*/
 			public int getNumCoup(){
 				return numCoup;
 			}
 			/** Ajoute un coup au compteur*/
 			public void setNumCoup(){
 				numCoup++;
 			}
 	//------------------------------------------------------------------------------------------------
 	
 	/** Un joueur abandonne la partie, la partie est terminee et l'autre joueur gagne
 	* @param numJ le numero du joueur qui abandonne (1 ou 2)
 	*/
 	public void abandonner(int numJ){
 		//La partie est finie
 		this.terminee = true;
 		
 		//On indique le gagnant de la partie
 		if(numJ == NOIR){this.gagnant = BLANC;}
 		else if(numJ == BLANC){this.gagnant =  NOIR;}
 		
 	}		
 			
 	
 	/** Affiche une partie en console avec le System*/
 	public String toString(){
 		String afficher="";
 		
 		afficher += "Joueur Actuel : "+joueurActuel+"\n";
 		afficher += "Numero du coup : "+numCoup+"\n";
 		afficher += "Score du joueur 1 : "+score[NOIR-1]+"\n";
 		afficher += "Score du joueur 2 : "+score[BLANC-1]+"\n";
 		afficher += "Gagnant : "+gagnant;
 		
 		if(terminee)
 			afficher += "Partie terminée\n";
 		
 		return afficher;
 	}
 }
