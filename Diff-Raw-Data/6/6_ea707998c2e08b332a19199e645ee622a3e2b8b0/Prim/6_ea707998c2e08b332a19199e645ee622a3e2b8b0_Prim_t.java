 import java.util.Arrays;
 
 public class Prim
 {
 	public static void main(String[] s) throws Exception{
 		int g = 0;
 		try{
 			arguments(s);
			g = new Integer(Integer.parseInt(s[0]));
 		}
 		catch(java.lang.ArrayIndexOutOfBoundsException e){
 			System.out.println("Wenn sie ein paar Primzahlen wollen, dann sind sie hier genau richtig.");
 			System.out.print("Aber zuerst brauchen wir eine oberer Grenze: ");
 			g = ein();
 		}
 		catch(java.lang.NumberFormatException e){
 			System.out.println("Das ist zwar ganz schön, aber wir brauchen eine Zahl.");
 			System.out.print("Bitte geben sie eine Zahl >1 ein: ");
 			g = ein();
 		}
 		while(g < 2){
 			System.out.println("Die eingegebene Nummer ist leider nicht gültig.");
 			System.out.print("Bitte geben sie eine Zahl >1 ein: ");
 			g = ein();
 		}
 		new Prim(g);
 	}
 
 	public Prim(int i){
 		System.out.println(primzahlen(i));
 	}
 
 	//handeld schlechten input
 
 	public static int ein()throws Exception{
 		try{
 			return EA.einInt();
 		}
 		catch(java.lang.NumberFormatException e){
 			System.out.println("Das ist zwar ganz schön, aber wir brauchen eine Zahl.");
 			System.out.print("Bitte geben sie eine Zahl >1 ein: ");
 			return ein();
 		}
 	}
 
 	//dekrementiert die übergebene Ganzzahl. 
 
 	public int dek(int i){
 		return --i;
 	}
 
 	//sagt, ob die zweite übergebene Ganzzahl ein Vielfaches der ersten übergebenen Ganzzahl ist.
 
 	public boolean vielfach(int a, int b){
 		if(a == 0 || a == 0){
 			return false;
 		}
 
 		int div     = b / a;
 		double divd = (double)b / (double)a;
 		double divi = (double)div;
 
 		if(divd == divi) {
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	//wirft alle Ganzzahlen, die die übergebene Bedingung erfüllen, aus der Liste hinaus.
 
 	public IntListe werfen(int i, IntListe is){
 		if(is.istLeer() == true){
 			return is;
 		}
 		else{
 			IntListe ret = new IntListe();
 			while(is.istLeer() != false){
 				if(vielfach(i, is.ende())){
 					IntListenSteuerung.loeschen(is.ende(), is);
 				}
 				else{
 					IntListenSteuerung.anfuegen(is.ende(), ret);
 					IntListenSteuerung.loeschen(is.ende(), is);
 				}
 			}
 			return ret;
 		}
 	}
 
 	/*
 	Vorstufe für das Sieb des Eratosthenes: Aus der übergebenen Liste werden
 	das vorderste Element und alle weiteren Elemente, die keine Vielfachen
 	des vordersten Elements sind, in die zurück gegebene Ergebnis-Liste übernommen.
 	*/
 
 	public IntListe sieb(IntListe is){
 		if(is.istLeer() == true){
 			return is;
 		}
 		else{
 			int k = is.kopf();
 			is    = werfen(k, is);
 			return IntListenSteuerung.anfuegen(k, is);
 		}
 	}
 
 	//Sieb des Eratosthenes.
 
 	public IntListe prims(IntListe is){
 		if(is.istLeer() == true){
 			return is;
 		}
 		else{
 			is           = sieb(is);
 			int i        = 2;
 			IntListe tmp = new IntListe();
 			while(i > is.ende()){
 				if(i == is.kopf()){
 					sieb(is);
 					IntListenSteuerung.anfuegen(is.kopf(), tmp);
 					IntListenSteuerung.loeschen(is.kopf(), is);
 				}
 				else{
 					werfen(i, is);
 				}
 				i++;
 			}
 			IntListe ret = new IntListe();
 			while(tmp.istLeer() != false){
 				IntListenSteuerung.anfuegen(tmp.kopf(), ret);
 				IntListenSteuerung.loeschen(tmp.kopf(), tmp);
 			}
 			return ret;
 		}
 	}
 
 	//gibt eine Liste mit allen Primzahlen bis zur übergebenen
 
 	public IntListe primzahlen(int i){
 		return prims(IntListenSteuerung.liste(i));
 	}
 
 	//Uninteressant: Hilfe
 
 	public static void printHelp(){
 		System.out.println("Usage: java -jar 'Primzahlen.jar' [<Obere Grenze>] <command>\n");
 		System.out.println("The commands are:");
 		System.out.println("	help    Prints this help.\n");
 	}
 
 	public static void arguments(String[] s){
 		String[] help = {"-h", "--help", "help"};
 		if(multcomp(s, help)){
 			printHelp();
 			System.exit(0);
 		}
 	}
 
 	public static boolean multcomp(String[] s, String[] r){
 		int i = 0;
		while(i <= s.length){
 			if(Arrays.asList(s).contains(r[i])){
 				return true;
 			}
 			i++;
 		}
 		return false;
 	}
 }
