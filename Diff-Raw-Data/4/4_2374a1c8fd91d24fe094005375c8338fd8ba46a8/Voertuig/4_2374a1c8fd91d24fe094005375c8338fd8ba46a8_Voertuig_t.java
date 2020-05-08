 
 package leen.meij;
 
 import java.util.ArrayList;
 
 import leen.meij.utilities.*;
 
 public class Voertuig extends Entity
 {
 
 	private ArrayList<Onderhoud> onderhoud = new ArrayList<Onderhoud>();
 
 	private int voertuigID;
 
 	private String categorie;
 
 	private String merk;
 
 	private String type;
 
 	private String kleur;
 
 	private String beschrijving;
 
 	private boolean verhuurbaar;
 	
 	public Voertuig(){}
 	
 	public Voertuig(int VoertuigID,String merk)
 	{
 		this.voertuigID = voertuigID;
 		this.merk = merk;
 	}
 	
 
 	public ArrayList<Onderhoud> getOnderhoud()
 	{
 		return this.onderhoud;
 	}
 
 	/**
 	 * 
 	 * @param onderhoud
 	 */
 	public void setOnderhoud(ArrayList<Onderhoud> onderhoud)
 	{
 		this.onderhoud = onderhoud;
 	}
 
 	public int getVoertuigID()
 	{
 		return this.voertuigID;
 	}
 
 	/**
 	 * 
 	 * @param voertuigID
 	 */
 	public void setVoertuigID(int voertuigID)
 	{
 		this.voertuigID = voertuigID;
 	}
 
 	public String getCategorie()
 	{
 		return this.categorie;
 	}
 
 	/**
 	 * 
 	 * @param categorie
 	 */
 	public void setCategorie(String categorie)
 	{
 		this.categorie = categorie;
 	}
 
 	public String getMerk()
 	{
 		return this.merk;
 	}
 	
 	
 	/**
 	 * 
 	 * @param merk
 	 */
 	public void setMerk(String merk)
 	{
 		this.merk = merk;
 	}
 
 	public String getType()
 	{
 		return this.type;
 	}
 
 	/**
 	 * 
 	 * @param type
 	 */
 	public void setType(String type)
 	{
 		this.type = type;
 	}
 
 	public String getKleur()
 	{
 		return this.kleur;
 	}
 
 	/**
 	 * 
 	 * @param kleur
 	 */
 	public void setKleur(String kleur)
 	{
 		this.kleur = kleur;
 	}
 
 	public String getBeschrijving()
 	{
 		return this.beschrijving;
 	}
 
 	/**
 	 * 
 	 * @param beschrijving
 	 */
 	public void setBeschrijving(String beschrijving)
 	{
 		this.beschrijving = beschrijving;
 	}
 
 	public boolean getVerhuurbaar()
 	{
 		return this.verhuurbaar;
 	}
 
 	/**
 	 * 
 	 * @param verhuurbaar
 	 */
 	public void isVerhuurbaar(boolean verhuurbaar)
 	{
 		// TODO - implement {class}.{operation}
 		throw new UnsupportedOperationException();
 	}
 
 	public void validateFields()
 	{
 		// TODO - implement {class}.{operation}
 		isValid = true;
 	}
 
 }
