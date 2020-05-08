 package Utilitaire;
 
 import java.util.*;
 
 public class Projet
 {
 	private String nom;
 	private ArrayList<Page> alPage;
 	private Page pageSelectionne;
 	
 	public Projet(String nom)
 	{
 		this.nom = nom;
 		alPage = new ArrayList<Page>();
 	}
 	
 	public void setPageSelectionne(Page p)
 	{
 		pageSelectionne = p;
 	}
 	
 	public ArrayList<Page> getAlPage()
 	{
 		return alPage;
 	}
 	
 	public Page getPageSelectionne()
 	{
 		return pageSelectionne;
 	}
 	
 	public Page getPage(String nom)
 	{
 		for (Page p : alPage)
 			if (nom.equals(p.getNom()))
 				return p;
 		
 		return null;
 	}
 	
 	public Page getPage(Page page)
 	{
 		for (Page p : alPage)
 			if (page.equals(p))
 				return p;
 		
 		return null;
 	}
 
	public void ajouterPage(Page p)
	{
		alPage.add(p);
	}
 }
