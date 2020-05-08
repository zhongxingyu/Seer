 package modele;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Achat {
 	private Map<String,Object> map = new HashMap<String,Object>();
 	private Personne personne;
 
 	
 	/********** Constructeurs ************/
 	/**
 	 * Constructeur d'un achat deja present dans la bdd, 
 	 * La map contient l'ensemble des attributs de l'objet ainsi que les id Billet et Personne. 
 	 * @param map
 	 * @param personne
 	 */
 	public Achat(Map<String,Object> map, Personne perso) {
 		this.map = map;
 		this.personne = perso;
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 * Lorsqu'une commande a ete validee, cette methode permet d'enregistrer chaque achat 
 	 * dans la liste des achats d'une personne ainsi que dans la bdd.
 	 * Elle repercute les modifs sur le billet
 	 */
 	public void ajoute() {
 		// Enregistre l'achat dans la bdd
 		try {
 			personne.getBilleterie().getBdd().ajoutBDD("achat", map); //NOM BDD
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		// Ajoute l'achat à la liste d'achats de la personne
 		personne.getAchats().ajoutAchat(this);
 		
 		// Repercute l'achat sur la liste des billets
 		this.repercuter();
 	}
 	
 	/**
 	 *  Cette methode modifie un Achat dans la bdd
 	 *  @param map
 	 */
 	private void enregistre() {
 		// Enregistre l'achat dans la bdd
 		try {
 			personne.getBilleterie().getBdd().enregistreBDD("achat", map); //NOM BDD
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Cette fonction modifie le billet en diminuant sa quantite
 	 */
 	private void repercuter() {
 		try {
			personne.getBilleterie().getListeBillets().getBillet((Integer) map.get("id_billet")).modifieQt((Integer) map.get("quantite"), (Boolean) map.get("subventionne"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/********** Setters ************/
 	/**
 	 * Modifie l'attribut "paye" de l'achat 
 	 * Cela signifie que les billets ont ete payes
 	 */
 	public void setPayer(boolean bl) {
 		map.put("paye", bl);
 		this.enregistre();
 	}
 	
 	/**
 	 * Modifie l'attribut "donne" de l'achat 
 	 * Cela signifie que les billets ont ete donnes a la personne
 	 */
 	public void setDonner(boolean bl) {
 		map.put("donne", bl);
 		this.enregistre();
 	}
 	
 	/**
 	 * Modifie l'attribut "subventionne" de l'achat 
 	 * Cela signifie indique si un billet est subventionne
 	 */
 	public void setSubventionne(boolean bl) {
 		map.put("subventionne", bl);
 		this.enregistre();
 	}
 	
 	
 	
 	/********** Getters ************/
 	public boolean getPaye() {
 		return (Boolean) Boolean.valueOf(map.get("paye").toString());
 	}
 	public boolean getDonne() {
 		return (Boolean) Boolean.valueOf(map.get("donne").toString());
 	}
 	public boolean getSubventionne() {
 		return (Boolean) Boolean.valueOf(map.get("subventionne").toString());
 	}
 	public Billet getBillet() {
 		try {
			return personne.getBilleterie().getListeBillets().getBillet((Integer) map.get("id_billet"));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	public int getQt() {
 		return (Integer) map.get("quantite");
 	}
 	public int getPrixUnitaire() {
 		return (Integer) map.get("prix_unitaire");
 	}
 	public double getPrixTotal() {
 		return (Double) map.get("prix_total");
 	}
 	public Personne getPersonne() {
 		return personne;
 	}
 	public String toString () {
 		return map +"\n";
 	}
 }
