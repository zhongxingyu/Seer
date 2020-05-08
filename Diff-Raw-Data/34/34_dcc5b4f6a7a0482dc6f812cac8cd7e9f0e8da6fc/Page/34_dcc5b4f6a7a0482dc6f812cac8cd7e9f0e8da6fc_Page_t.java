 package Utilitaire;
 
 import java.util.*;
 
 public class Page
 {
 	private String nom;
 	private ArrayList<String> alTitre;
 	private ArrayList<String> alParagraphe;
 	private ArrayList<String> alImage;
 	
 	public Page(String nom)
 	{
 		this.nom = nom;
 		alTitre = new ArrayList<String>();
 		alParagraphe = new ArrayList<String>();
 		alImage = new ArrayList<String>();
 	}
 	
 	public String getNom()						{	return nom;				}
 	public ArrayList<String> getAlTitre()		{	return alTitre;			}
 	public ArrayList<String> getAlParagraphe()	{	return alParagraphe;	}
 	public ArrayList<String> getAlImage()		{	return alImage;			}
 
 	public void ajouterTitre(String titre)
 	{
 		alTitre.add(titre);
 	}
 	
 	public void modTitre(String titre, int indice) 
 	{
 		alTitre.remove(indice-1);
 		alTitre.add(indice-1, titre);
 	}
 	
 	public void ajouterParagraphe(String s)
 	{
	    alParagraphe.add(s);
 	}
 	
 	
 	public void modParagraphe(String s, int indice)
 	{
 	    alParagraphe.remove(indice-1);
		alParagraphe.add(indice-1, s);
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "Page [nom=" + nom + ", alTitre=" + alTitre + ", alParagraphe="
 				+ alParagraphe + ", alImage=" + alImage + "]";
 	}
 }
