 package leen.meij;
 
 import java.util.Date;
 
 import leen.meij.utilities.*;
 
 public class Klant extends Entity
 {
 
 	private int klantID;
 
 	private int klantNummer;
 
 	private String voornaam;
 
 	private String tussenvoegsel;
 
 	private String achternaam;
 
 	private String bedrijfsnaam;
 
 	private String kvknummer;
 
 	private String straat;
 
 	private String huisNummer;
 
 	private String postcode;
 
 	private String woonplaats;
 
 	private String provincie;
 
 	private String land;
 
 	private String telefoonnummer;
 
 	private String mobielnummer;
 
 	private String emailadres;
 
 	private java.util.Date geboorteDatum;
 
 	private byte[] kopiePaspoort;
 
 	private byte[] kopieRijbewijs;
 
	public String toString(){
		return voornaam + " " + achternaam;
	}

 	public Klant()
 	{
 	}
 
 	public Klant(int klantID, String voornaam)
 	{
 		this.klantID = klantID;
 		this.voornaam = voornaam;
 	}
 
 	public int getKlantID()
 	{
 		return this.klantID;
 	}
 
 	/**
 	 * 
 	 * @param klantID
 	 */
 	public void setKlantID(int klantID)
 	{
 		this.klantID = klantID;
 	}
 
 	public int getKlantNummer()
 	{
 		return this.klantNummer;
 	}
 
 	/**
 	 * 
 	 * @param klantNummer
 	 */
 	public void setKlantNummer(int klantNummer)
 	{
 		this.klantNummer = klantNummer;
 	}
 
 	public String getVoornaam()
 	{
 		return this.voornaam;
 	}
 
 	/**
 	 * 
 	 * @param voornaam
 	 */
 	public void setVoornaam(String voornaam)
 	{
 		this.voornaam = voornaam;
 	}
 
 	public String getTussenvoegsel()
 	{
 		return this.tussenvoegsel;
 	}
 
 	/**
 	 * 
 	 * @param tussenvoegsel
 	 */
 	public void setTussenvoegsel(String tussenvoegsel)
 	{
 		this.tussenvoegsel = tussenvoegsel;
 	}
 
 	public String getAchternaam()
 	{
 		return this.achternaam;
 	}
 
 	/**
 	 * 
 	 * @param achternaam
 	 */
 	public void setAchternaam(String achternaam)
 	{
 		this.achternaam = achternaam;
 	}
 
 	public String getBedrijfsnaam()
 	{
 		return this.bedrijfsnaam;
 	}
 
 	/**
 	 * 
 	 * @param bedrijfsnaam
 	 */
 	public void setBedrijfsnaam(String bedrijfsnaam)
 	{
 		this.bedrijfsnaam = bedrijfsnaam;
 	}
 
 	public String getKvknummer()
 	{
 		return this.kvknummer;
 	}
 
 	/**
 	 * 
 	 * @param kvknummer
 	 */
 	public void setKvknummer(String kvknummer)
 	{
 		this.kvknummer = kvknummer;
 	}
 
 	public String getStraat()
 	{
 		return this.straat;
 	}
 
 	/**
 	 * 
 	 * @param straat
 	 */
 	public void setStraat(String straat)
 	{
 		this.straat = straat;
 	}
 
 	public String getHuisNummer()
 	{
 		return this.huisNummer;
 	}
 
 	/**
 	 * 
 	 * @param huisNummer
 	 */
 	public void setHuisNummer(String huisNummer)
 	{
 		this.huisNummer = huisNummer;
 	}
 
 	public String getPostcode()
 	{
 		return this.postcode;
 	}
 
 	/**
 	 * 
 	 * @param postcode
 	 */
 	public void setPostcode(String postcode)
 	{
 		this.postcode = postcode;
 	}
 
 	public String getWoonplaats()
 	{
 		return this.woonplaats;
 	}
 
 	/**
 	 * 
 	 * @param woonplaats
 	 */
 	public void setWoonplaats(String woonplaats)
 	{
 		this.woonplaats = woonplaats;
 	}
 
 	public String getProvincie()
 	{
 		return this.provincie;
 	}
 
 	/**
 	 * 
 	 * @param provincie
 	 */
 	public void setProvincie(String provincie)
 	{
 		this.provincie = provincie;
 	}
 
 	public String getLand()
 	{
 		return this.land;
 	}
 
 	/**
 	 * 
 	 * @param land
 	 */
 	public void setLand(String land)
 	{
 		this.land = land;
 	}
 
 	public String getTelefoonnummer()
 	{
 		return this.telefoonnummer;
 	}
 
 	/**
 	 * 
 	 * @param telefoonnummer
 	 */
 	public void setTelefoonnummer(String telefoonnummer)
 	{
 		this.telefoonnummer = telefoonnummer;
 	}
 
 	public String getMobielnummer()
 	{
 		return this.mobielnummer;
 	}
 
 	/**
 	 * 
 	 * @param mobielnummer
 	 */
 	public void setMobielnummer(String mobielnummer)
 	{
 		this.mobielnummer = mobielnummer;
 	}
 
 	public String getEmailadres()
 	{
 		return this.emailadres;
 	}
 
 	/**
 	 * 
 	 * @param emailadres
 	 */
 	public void setEmailadres(String emailadres)
 	{
 		this.emailadres = emailadres;
 	}
 
 	public java.util.Date getGeboorteDatum()
 	{
 		return this.geboorteDatum;
 	}
 
 	/**
 	 * 
 	 * @param date
 	 */
 	public void setGeboorteDatum(Date date)
 	{
 		this.geboorteDatum = date;
 	}
 
 	public byte[] getKopiePaspoort()
 	{
 		return this.kopiePaspoort;
 	}
 
 	/**
 	 * 
 	 * @param kopiePaspoort
 	 */
 	public void setKopiePaspoort(byte[] kopiePaspoort)
 	{
 		this.kopiePaspoort = kopiePaspoort;
 	}
 
 	public byte[] getKopieRijbewijs()
 	{
 		return this.kopieRijbewijs;
 	}
 
 	/**
 	 * 
 	 * @param kopieRijbewijs
 	 */
 	public void setKopieRijbewijs(byte[] kopieRijbewijs)
 	{
 		this.kopieRijbewijs = kopieRijbewijs;
 	}
 
 	public void validateFields()
 	{
 		isValid = true;
 	}
 
 	public String getVolledigeNaam()
 	{
 		String naam = "";
 		if (voornaam != null)
 		{
 			naam += voornaam;
 		}
 		if (achternaam != null)
 		{
 			naam += achternaam;
 		}
 		return naam;
 	}
 
 }
