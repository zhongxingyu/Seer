 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 public class Repertoire implements Serializable
 {
 	//private ArrayList<Personne> repert;
 	
 	private HashMap<Integer,Personne> repert;
 	private ArrayList<Integer> lesCles;
 
 	public Repertoire()
 	{
 		repert = new HashMap<Integer, Personne>();
 		lesCles = new ArrayList<Integer>();
 	}
 
 
 	public void afficher()
 	{
 		
 		Collection<Personne> lesPersonnes = repert.values(); 
 		for(Personne p : lesPersonnes)
 		{
 			p.afficher();
 		}
 	}
 
 
 	public Personne rechercher(String nom)
 	{
 		Collection<Personne> lesPersonnes = repert.values(); 
 		for(Personne p : lesPersonnes)
 		{
 			if(p.getNom().equals(nom))
 			{
 				return p;
 			}
 		}
 
 		return null;
 	}
 	
 	
 	
 	public Personne rechercher(String nom, String prenom)
 	{
 		Collection<Personne> lesPersonnes = repert.values(); 
 		for(Personne p : lesPersonnes)
 		{
 			if(p.getNom().equals(nom) && p.getPrenom().equals(prenom))
 			{
 				return p;
 			}
 		}		
 		return null;
 	}
 	
 	
 	public void ajouter(int id, Personne personne)
 	{
 		repert.put(id, personne);
 		lesCles.add(id);
 	}
 	
 	public Personne recherche_personne(int index)
 	{
 		return repert.get(lesCles.get(index));
 	}
 	
 	public void supprimer(int index)
 	{
 		repert.remove(lesCles.get(index));
 		lesCles.remove(index);
 	}
 	
 	/*
 	public void ajouter(String nom, String prenom, String adresse, String tel)
 	{
 		String[] infoPersonne=new String[4];
 		infoPersonne[0]=nom;
 		infoPersonne[1]=prenom;
 		infoPersonne[2]=adresse;
 		infoPersonne[3]=tel;
 		repert.add(new Personne(infoPersonne));
 	}
 	*/
 	
 	public void ajouter(int id, String nom, String prenom, Adresse adresse, String tel)
 	{
 		String[] infoPersonne=new String[7];
 		infoPersonne[0]=nom;
 		infoPersonne[1]=prenom;
 		infoPersonne[2]=adresse.getNumRue();
 		infoPersonne[3]=adresse.getRue();
		infoPersonne[4]=adresse.getCP();
		infoPersonne[5]=adresse.getVille();
 		infoPersonne[6]=tel;
 		repert.put(id, new Personne(infoPersonne));
 		lesCles.add(id);
 	}
 	
 	public int getTaille()
 	{
 		return repert.size();
 	}
 	
 	public Collection<Personne> getPersonnes()
 	{
 		return repert.values();
 	}
 
 
 	public int getCle(int index) 
 	{
 		return lesCles.get(index);
 	}
 }
