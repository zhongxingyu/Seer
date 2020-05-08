 package si.unimb.ruk.prijatelj.logika.oddihi;
 
 import java.util.*;
 
 
 
 /**
  * @author Joze
  * Razred Paket izpeljan iz razreda Oddih
  */
 public class Paket extends Oddih{
 		
 	private double cena;
 	private GregorianCalendar datum_zacetka;
 	private GregorianCalendar datum_konca;
 	private int stevilo_mest;
 	
 	private final int minPopustPotnikov = 10;
 	private final int popustVelikaSkupina = 12;
 	private final int popustSkupinaProcent = 5;
 	private final double popustOtrociProcent = 0.5;
 	
 	
 	/**
 	 * Konstruktor paket 
 	 * 
 	 * @param cena
 	 * @param datum_zacetka
 	 * @param datum_konca
 	 * @param stevilo_mest
 	 */
 	public Paket(double cena, GregorianCalendar datum_zacetka, GregorianCalendar datum_konca, int stevilo_mest) {
 		this.cena = cena;
 		this.datum_zacetka = datum_zacetka;
 		this.datum_konca = datum_konca;
 		this.stevilo_mest = stevilo_mest;
 	}
 	
 	//getter in setter metode
 	
 	/**
 	 * @return
 	 */
 	public double getCena() {
 		return cena;
 	}
 	/**
 	 * @param cena
 	 */
 	public void setCena(double cena) {
 		this.cena = cena;
 	}
 	/**
 	 * @return
 	 */
 	public GregorianCalendar getDatum_zacetka() {
 		return datum_zacetka;
 	}
 	/**
 	 * @param datum_zacetka
 	 */
 	public void setDatum_zacetka(GregorianCalendar datum_zacetka) {
 		this.datum_zacetka = datum_zacetka;
 	}
 	/**
 	 * @return
 	 */
 	public GregorianCalendar getDatum_konca() {
 		return datum_konca;
 	}
 	/**
 	 * @param datum_konca
 	 */
 	public void setDatum_konca(GregorianCalendar datum_konca) {
 		this.datum_konca = datum_konca;
 	}
 	/**
 	 * @return
 	 */
 	public int getStevilo_mest() {
 		return stevilo_mest;
 	}
 	/**
 	 * @param stevilo_mest
 	 */
 	public void setStevilo_mest(int stevilo_mest) {
 		this.stevilo_mest = stevilo_mest;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString(){
 		return "Cena: " + cena + ", Datum zacetka: " + datum_zacetka.get(Calendar.DAY_OF_MONTH)+ "."+ datum_zacetka.get(Calendar.MONTH)+ "." + datum_zacetka.get(Calendar.YEAR) + ", Datum konca: " + datum_konca.get(Calendar.DAY_OF_MONTH)+ "."+ datum_konca.get(Calendar.MONTH)+ "." + datum_konca.get(Calendar.YEAR) + ", Stevilo mest: " + stevilo_mest + "\n";
 	}
 	
 	/**
 	 * @return
 	 */
 	public double izracunajCeno(){
 		return cena;
 	}
 	
 	//Ce se popotniki na oddih prijavijo kot skupina(vsaj 10 clanov) prejmejo na vsakega clana 5% popusta, 
	//zmanj�ajo se tudi skupni administracijski stroski v visini 12eur.
 	/**
 	 * @param velikostSkupine
 	 * @return
 	 */
 	public double izracunajCeno(int velikostSkupine){
 		if(velikostSkupine >= minPopustPotnikov){
 			final double cena_paketa = cena*velikostSkupine - velikostSkupine*((popustSkupinaProcent*cena)/100) - popustVelikaSkupina;
 			return cena_paketa;
 		}
 		else return cena*velikostSkupine;
 	}
 	//izracuna ceno paketa za druzine, pri cemer placajo druzine za vsakega otroka 50% cene odraslega.
 	/**
 	 * @param steviloOdraslih
 	 * @param steviloOtrok
 	 * @return
 	 */
 	public double izracunajCeno(int steviloOdraslih, int steviloOtrok){
 		if(steviloOtrok > 0){
 			final double cena_druzine = steviloOdraslih*cena + (steviloOtrok*(cena*popustOtrociProcent));
 			return cena_druzine;
 		}
 		else return cena*steviloOdraslih;		
 	}
 }
