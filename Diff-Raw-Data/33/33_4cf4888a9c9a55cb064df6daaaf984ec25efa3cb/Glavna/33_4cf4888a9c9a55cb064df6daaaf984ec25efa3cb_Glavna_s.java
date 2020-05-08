 //korak 10 - Kreirati paket
 package hr.tvz.programiranje.java.glavna;
 
 import hr.tvz.programiranje.java.banka.Racun;
 import hr.tvz.programiranje.java.banka.Transakcija;
 import hr.tvz.programiranje.java.osoba.Osoba;
 import java.math.BigDecimal;
 import java.util.Scanner;
 
 //korak 11 - reirati klasu 
 public class Glavna {
 
 	/**
 	 * @param args
 	 */
 	//korak 11 - main metoda 
 	public static void main(String[] args) {
 	
 		//set scanner
 		Scanner unos = new Scanner(System.in);
 		
 		//korak 11 - potrebno zatražiti unošenje podataka o vlasniku
 		//prvog računa (ime, prezime i OIB) i postaviti ih u objekt klase Osoba
 		System.out.println("Unesite ime vlasnika prvog računa: ");
 		String imePrviVlasnik = unos.next();
 		System.out.println("Unesite prezime vlasnika prvog računa: ");
 		String prezimePrviVlasnik = unos.next();
		System.out.println("\nUnesite OIB vlasnika prvog računa: ");
 		String oibPrviVlasnik = unos.next();
 		//ponoviti unos u slučaju nije 11-znamenkasti broj
 		while (oibPrviVlasnik.length() != 11 || oibPrviVlasnik.matches("[^0-9]")) {
			System.out.println("\nPogrešan unos. OIB mora imati 11 brojeva.");
			System.out.println("Ponovo unesite OIB vlasnika prvog računa: ");
 			oibPrviVlasnik = unos.next();
 		}
 		//karak 11 - Nakon toga potrebno je zatražiti unos iznosa na prvom računu
		System.out.println("\nUnesite stanje prvog računa: ");
 		BigDecimal iznosPrvogVlasnikaRacuna = unos.nextBigDecimal();
 		
 		//korak 11 - postaviti ih u objekt klase Osoba
 		Osoba vlasnikPrvogRacuna = new Osoba(imePrviVlasnik, prezimePrviVlasnik, oibPrviVlasnik);
 		
 		//korak 11 - kreirati objekt klase Racun
 		Racun prviRacun = new Racun(vlasnikPrvogRacuna, iznosPrvogVlasnikaRacuna);
 		
 		//korak 11 - Na sličan način kreirati i sve navedene podatke za 
 		//vlasnika drugog računa i iznos za drugi račun
		System.out.println("\nUnesite ime vlasnika drugog računa: ");
 		String imeDrugiVlasnik = unos.next();
		System.out.println("\nUnesite prezime vlasnika drugog računa: ");
 		String prezimeDrugiVlasnik = unos.next();
		System.out.println("\nUnesite OIB vlasnika drugog računa: ");
 		String oibDrugiVlasnik = unos.next();
 		//ponoviti unos u slučaju nije 11-znamenkasti broj
 		while (oibDrugiVlasnik.length() != 11 || oibDrugiVlasnik.matches("[^0-9]")) {
			System.out.println("\nPogrešan unos. OIB mora imati 11 brojeva.");
			System.out.println("Ponovo unesite OIB vlasnika drugog računa: ");
 			oibDrugiVlasnik = unos.next();
 		}
 		Osoba vlasnikDrugogRacuna = new Osoba(imeDrugiVlasnik, prezimeDrugiVlasnik, oibDrugiVlasnik);
		System.out.println("\nUnesite stanje drugog računa: ");
 		BigDecimal iznosDrugogVlasnikaRacuna = unos.nextBigDecimal();
 		Racun drugiRacun = new Racun(vlasnikDrugogRacuna, iznosDrugogVlasnikaRacuna);
		System.out.println("\nUnesite iznos transakcije s prvog na drugi račun: ");
 		BigDecimal iznosTransakcije = unos.nextBigDecimal();
 		
 		//korak 12 - kreirati objekt klase Transakcija koja će prebacivati sredstva
 		Transakcija transakcija = new Transakcija(prviRacun, drugiRacun, iznosTransakcije);
 		transakcija.provediTransakciju();
 		
 		//korak 12 - Na kraju je još potrebno ispisati nova stanja na računu
 		System.out.println("\nStanje prvog računa nakon transakcije: " + prviRacun.getStanjeRacunaHRK());
 		System.out.println("\nStanje drugog računa nakon transakcije: " + drugiRacun.getStanjeRacunaHRK());
				
 		//zatvori skener
 		unos.close();
 	
 	}
 	
 }
