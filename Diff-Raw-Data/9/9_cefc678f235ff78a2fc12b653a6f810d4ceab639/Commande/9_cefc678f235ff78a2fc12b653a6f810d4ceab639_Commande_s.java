 package modele;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Fonctionnement des commandes
  * Lorque l'on clique sur "Passer commande"
  * 1 L'ecran Achat billet apparait, il permet de : 
  * 		1 Choisir un billet à partir d'un tableau -> fait
  * 		2 Choisir la quantite
  * 		3 Choisir subventionne (permet de savoir si le billet est subventionne)
  * 		4 Choisir donne (permet de savoir si le billet a ete remis a la personne)
  * 		5 Choisir paye (permet de savoir si le billet a ete paye par la personne)
  * 		6 Deux boutons : Terminer la commande ou Continuer la commande
  * 			-> Le bouton "Continuer la Commande" fait appel aux fonctions 
  * 					1 ajoutCommande(param) (cf 2.)
  * 					2 affiche un nouvel ecran Achat Billet
  * 			-> Le bouton "Terminer la Commande" fait appel aux fonctions 
  * 					1 ajoutCommande(param) (cf 2.)
  * 					2 affiche la fenetre "Fin de la commande" (cf 3.)
  *
  * 2 Fonction ajoutCommande : La fonction verifie si l'achat est possible (pas suffisament de billet sub par ex)
  * 		-> Si c'est possible, l'achat est automatiquement valide (ajout a la liste des achats de la personne)
  * 		-> Si ce n'est pas possible, une erreur est lance par ajoutCommande
  * 			=> une fenêtre de "forçage" doit être proposés
  * 				1 Si on valide, cella appelle la focntion valider() : on force le processus même si il y a des erreurs 
  * 				2 Si on annule, cella appelle la focntion annuler() : la commande n'est pas prise en compte
  *
  * 3 Si "Terminer la commande"
  * 		2 Cette fenetre presente un recapitulatif des billets achetes ainsi que le montant à payer
  * 		3 Un unique bouton qui affiche "Terminer"
  */
 
 public class Commande {
 	private Personne personne;
 	private int nbArticle;
 	private float prixTotal;
 	private List<Achat> listeValidee = new ArrayList<Achat>();
 	private Achat achatEnCours;
 	
 	
 	public Commande(Personne personne) {
 		this.personne = personne;
 		this.nbArticle = 0;
 		this.prixTotal = 0;
 		if(!personne.isAchatEnMem()){
 			personne.getAchats().metEnMemoire();
 		}
 	}
 	
 	/********** Methodes ************/
 	/**
 	 * Realise un achat si cela est possible
 	 * @param billet
 	 * @param qt
 	 * @param paye
 	 * @param donne
 	 * @param subventionne
 	 * @return vrai si l operation a reussie
 	 * @throws AchatException 
 	 */
 	public void ajoutCommande(Billet billet, int qt, boolean paye, boolean donne, boolean subventionne) throws AchatException {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("quantite", qt);
 		map.put("paye", paye);
 		map.put("donne", donne);
 		map.put("subventionne", subventionne);
 		if (subventionne) {
 			map.put("prix_unitaire", billet.getPrixRed());
 			map.put("prix_total", billet.getPrixRed() * qt);
 		} else {
 			map.put("prix_unitaire", billet.getPrixNor());
 			map.put("prix_total", billet.getPrixNor() * qt);
 		}
 		achatEnCours = new Achat(map, personne, billet);
 		
 		if (this.achatPossible(billet, qt, subventionne)) {
 			valider();
 		}
 	}
 	
 	/**
 	 * Si le message d'erreur quota depasse est lance par la commande ajoutCommande. 
 	 * Une boite de dialogue est affichee pour valider cette action normalement non autorisee
 	 * Cette methode valide la derniere commande
 	 */
 	public void valider() {
 		listeValidee.add(achatEnCours);
 		achatEnCours.ajoute();
 		prixTotal += achatEnCours.getPrixTotal();
 		achatEnCours = null;
 		nbArticle++;
 	}
 	
 	/**
 	 * Si le message d'erreur quota depasse est lance par la commande ajoutCommande. 
 	 * Une boite de dialogue est affichee pour valider cette action normalement non autorisee
 	 * Cette methode annule la derniere commande en l'enlevant de la liste
 	 */
 	public void annuler() {
 		achatEnCours = null;
 	}
 	
 	/**
 	 * Vide la listeValidee :  clorture une commande
 	 */
 	public void cloturer() {
 		listeValidee.clear();
 	}
 	
 	/**
 	 * Verifie si un achat est possible
 	 * Si ce n'est pas possible, cette methode envoie des exceptions
 	 * @param billet
 	 * @param qt
 	 * @param subventionne
 	 * @return
 	 * @throws AchatException
 	 */
 	public boolean achatPossible(Billet billet, int qt, boolean subventionne) throws AchatException{
 		if (billet.getNbPlace() >= qt) {
 			if(subventionne) {
 				if (billet.getNbPlaceSub() >= qt) {
 					if (billet.getNbPlacePerso() - personne.nbBilletsAchete(billet) >= qt) {
 						
 					} else {
 						throw new AchatException(0);
 					}
 				} else {
 					throw new AchatException(2);
 				}
 			}
 		} else {
 			throw new AchatException(1);
 		}
 		return true;
 	}
 	
 	
 	/********** Getters ************/
 	public int getNbArticle() {
 		return nbArticle;
 	}
 	public float getPrixTotal() {
 		return prixTotal;
 	}
 
 	
 	/********** Methodes de base ************/
 	public String toString() {
		return "Mon panier : " + nbArticle + " articles | " + prixTotal + "€";
 	}
 }
