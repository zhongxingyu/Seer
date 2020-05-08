 package Modele;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /*
  * This is the main class where you can launch the application
  * 
  */
 
 public class Billeterie {
 	/********** Attributs ************/
 	private ListePersonnes listePersonnes;
 	private ListeBillets listeBillets;
 	private SQLInterface bdd;
 		
 	/* TODO ajouter une map<String, type> pour avoir les attributs de chaque objet (sans les id)
 	 * La methode doit pouvoir marcher meme si la bdd est vide
 	 * Elle enverra des exception si certains para necessaire ne sont pas presents (para de base)
 	 */
 	
 	public Billeterie (String nomBdd) {
 		try{
 			bdd = new SQLiteImpl(nomBdd);
 		} catch (Exception e) {
 			//TODO
 		}
 		listePersonnes = new ListePersonnes(this);
 		listeBillets = new ListeBillets(this);
 		
 		listeBillets.metEnMemoire();	// La mise en memoire n'est pas presente 2 fois ? (dans  
 		listePersonnes.metEnMemoire();	// les constructeurs des Liste)
 	}
 	
 	/********** Methodes ************/
 	
 	
 	
 	/********** Methodes de base ************/
 	public ListePersonnes getListePersonnes() {
 		return listePersonnes;
 	}
 	public void setListePersonnes(ListePersonnes listePersonnes) {
 		this.listePersonnes = listePersonnes;
 	}
 	public ListeBillets getListeBillets() {
 		return listeBillets;
 	}
 	public void setListeBillets(ListeBillets listeBillets) {
 		this.listeBillets = listeBillets;
 	}
 	public SQLInterface getBdd() {
 		return bdd;
 	}
 	public void setBdd(SQLInterface bdd) {
 		this.bdd = bdd;
 	}
 
 	public static void main (String[] args){
 		long start; 
 		start = System.nanoTime();
 		
 		Billeterie bill = new Billeterie("database.sqlite");
 		System.out.println(bill.getListePersonnes() + "\n\n\n"); 
 		System.out.println(bill.getListeBillets() + "\n\n\n"); 
 		
		System.out.println(bill.getListeBillets().recherche("cin")); 
		System.out.println(bill.getListePersonnes().recherche("bon"));
 		
 		long duree = System.nanoTime() - start;
 		System.out.println(duree);
 	}
 }
