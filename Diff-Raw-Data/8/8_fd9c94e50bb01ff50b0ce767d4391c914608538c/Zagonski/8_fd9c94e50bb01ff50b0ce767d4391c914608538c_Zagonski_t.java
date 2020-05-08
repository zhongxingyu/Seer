 /*
  * Za zaključek implementirajte še testni razred Zagonski. V zagonski metodi omenjenega 
 razreda omogočite, da bo uporabnik lahko vnesel podatke o oddihih ter vnesene oddihe 
 izpisal na zaslon.
  */
 package si.unimb.ruk.prijatelj.logika;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Scanner;
 
 import si.unimb.ruk.prijatelj.logika.oddihi.Destinacija;
 import si.unimb.ruk.prijatelj.logika.oddihi.Izlet;
 import si.unimb.ruk.prijatelj.logika.oddihi.Oddih;
 import si.unimb.ruk.prijatelj.logika.oddihi.Paket;
 import si.unimb.ruk.prijatelj.logika.osebe.Vodic;
 
 /**
  * @author Joze
  *
  */
 public class Zagonski {
 	public static void main(String args[]) {
 		
 		List<Oddih> oddihi = new ArrayList<Oddih>();
 
 		Scanner in = new Scanner(System.in);
 		int x=0;
 		do {
 			System.out.println("Menu");
 			System.out.println("--------------------------");
 			System.out.println("0: vnos podatkov o oddihih");
 			System.out.println("1: izpis podatkov o oddihih");
 			System.out.println("2: Izhod");
 			System.out.println("--------------------------");
 			
 			x=in.nextInt();
 			switch(x) {
 			case 0:
 				Scanner sc = new Scanner(System.in);
 				System.out.println("Vnesi naziv oddiha: ");
 				String tmp = sc.nextLine();
 				
 				Izlet pocitnice = new Izlet(tmp);
 				
 				System.out.println("Vnesi ime: ");
 				String ime = sc.nextLine();
 				
 				System.out.println("Vnesi priimek: ");
 				String priimek = sc.nextLine();
 				
 				Vodic vodic = new Vodic(ime,priimek);
 				
 				System.out.println("Vnesi eposto: ");
 				String eposta = sc.nextLine();
 				
 				vodic.setEposta(eposta);
 				
 				System.out.println("Vnesi datum rojstva: leto, mesec, dan.");
 				int letoR = sc.nextInt();
 				int mesecR = sc.nextInt()-1;
 				int danR = sc.nextInt();
 				GregorianCalendar rd = new GregorianCalendar(letoR, mesecR, danR);
 				
 				vodic.setRojstniDatum(rd);
 				
 				System.out.println("Vnesi st prostih mest: ");
 				int prostaMesta = sc.nextInt();
 				
 				System.out.println("Vnesi ceno: ");
 				double cena = sc.nextInt();
 				
 				System.out.println("Vnesi datum zacetka oddiha - leto, mesec, dan");
 				int letoZ = sc.nextInt();
 				int mesecZ = sc.nextInt()-1;
 				int danZ = sc.nextInt();
 				GregorianCalendar datumZacetka = new GregorianCalendar(letoZ,mesecZ, danZ);
 			
 				System.out.println("Vnesi koncni datum oddiha - leto, mesec, dan");
 				int letoK = sc.nextInt();
 				int mesecK = sc.nextInt()-1;
 				int danK = sc.nextInt();
 				GregorianCalendar datumKonca = new GregorianCalendar(letoK,mesecK, danK);
				sc.nextLine(); // This line consumes the \n character
 			
 				System.out.println("Vnesi naziv destinacije: ");
 				String nazivD = sc.nextLine();
 				
 				Destinacija cilj = new Destinacija(nazivD);
 				
 				List<Paket> seznamPaketov = new ArrayList<Paket>();
 				Paket paket = new Paket(cena, datumZacetka, datumKonca, prostaMesta);
 				seznamPaketov.add(paket);
 				
 				System.out.println("Izracunaj ceno za X potnikov: ");
 				int stPotnikov = sc.nextInt();
 				System.out.println("Cena za "+stPotnikov+" potnikov = "+paket.izracunajCeno(stPotnikov));
 				
 				System.out.println("Izracunaj ceno za X odraslih in Y otrok: ");
 				int stOtrok = sc.nextInt();
 				int stOdraslih = sc.nextInt();
 				
 				System.out.println("Cena za "+stOtrok+" otrok in "+stOdraslih+" odraslih = "+paket.izracunajCeno(stOdraslih, stOtrok));
 				
 				pocitnice.setPonudbaPaketov(seznamPaketov);
 				pocitnice.setVodic(vodic);
 				pocitnice.setDestinacija(cilj);
 				oddihi.add(pocitnice);
 				
 				break;
 			case 1:
 				System.out.println("Izpis podatkov: ");
 				for (int i = 0; i < oddihi.size(); i++) {					
 					System.out.println(oddihi.get(i).toString());
 				}
 				break;
 			}
 		}
 		while(x!=2);
 	}
 
 }
