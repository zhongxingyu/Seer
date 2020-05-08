 package Others;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.lang.Character;
 
 public class Outils {
 
 	private static final String String = null;
 	private BufferedReader br;
 	private String[] listes;
 
 	public Outils() {
 
 	}
 
 	public String leMotEstDansLaListe(String mot, String texte, char lettre) {
 		if (rechercheDuMotDansLaListe(mot, texte, lettre)) {
 			return "Le mot est dans la liste +1 ! ";
 		} else {
 			return " Le mot n'est pas bon mais voici un exemple :"+ motPossible(texte, lettre);
 		}
 	}
 
 	public boolean rechercheDuMotDansLaListe(String mot, String texte, char lettre) {
 		String fichier = texte;
 		if (Character.toUpperCase(mot.charAt(0)) != Character.toUpperCase(lettre)) {
 			return false;
 		}
 		// lecture du fichier texte
 		try {
 			InputStream ips = new FileInputStream(fichier);
 			InputStreamReader ipsr = new InputStreamReader(ips);
 			br = new BufferedReader(ipsr);
 			String ligne;
 			String motMaj = mot.toUpperCase();
 			while ((ligne = br.readLine()) != null) {
 				String ligneMaj = ligne.toUpperCase();
 				if (motMalTape(motMaj, ligneMaj)) {
 					return true;
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 		return false;
 	}
 
 	// Gere la possibilit que la personne est mal tap
 	public boolean motMalTape(String motTape, String motReference) {
 		/*
 		 * if(motTape.length()!= motReference.length()){ return false; }
 		 */
 		int nbReference;
 		if (motReference.length() > motTape.length()) {
 			nbReference = motTape.length();
 		} else {
 			nbReference = motReference.length();
 		}
 		int nbCharJuste = 0;
 		for (int i = 0; i < nbReference; i++) {
 			if (String.valueOf(motTape.charAt(i)).compareTo(
 					String.valueOf(motReference.charAt(i))) == 0) {
 				nbCharJuste++;
 			}
 		}
 		int nbTemp = nbReference - nbCharJuste;
 		if (nbTemp == 0 || nbTemp == 1) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String motPossible(String texte, char lettre) {
 		try {
 			InputStream ips = new FileInputStream(texte);
 			InputStreamReader ipsr = new InputStreamReader(ips);
 			br = new BufferedReader(ipsr);
 			String ligne;
 			while ((ligne = br.readLine()) != null) {
 				if (ligne.toUpperCase().charAt(0) == Character.toUpperCase(lettre)) {
 					return ligne;
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 		return "le mot n'existe pas en : " + lettre;
 	}
 	
 	/**
 	 * 
 	 * @param texte		Le nom du fichier a l'interieur du quel rechercher le mot
 	 * @param lettre	La premiere lettre du mot a rechercher
 	 * @return 			Mot aleatoire commencant par la lettre donnee
 	 */
 	public String motPossibleAlea(String texte, char lettre){
 
 		int nbLignes = nbLigneTexte(texte);
 		int premLigne=1;
 		
 		// Une premiere boucle pour trouver la premiere ligne de l'occurrence de la lettre
 		while(premLigne <= nbLignes && (getMotDeLaLigne(texte, premLigne).charAt(0) != lettre)){
 		premLigne+=1;
 		}
 		
 		String motTmp = getMotDeLaLigne(texte, premLigne);
 		if (premLigne == nbLignes && (motTmp.toUpperCase().charAt(0) != Character.toUpperCase(lettre))){
 			return "Error";
 		}
 		
 		int derLigne = premLigne;
 		// Une seconde boucle pour trouver la derniere ligne de l'occurrence de la lettre
 		while(derLigne <= nbLignes && (getMotDeLaLigne(texte, derLigne).charAt(0) == lettre)){
 		derLigne+=1;
 		}
 		// Le nombre de lignes commencant par la lettre donnee
 		int nbMotCommencantParLettre = derLigne - premLigne;
 		
 		// Ligne aleatoire entre la premiere et derniere occurrence de la lettre
 		int nbLigneMot = (int)( Math.random()*( nbMotCommencantParLettre ) ) + premLigne;
 		
 		
 		String mot = getMotDeLaLigne(texte, nbLigneMot);		
 		if(mot.toUpperCase().charAt(0) == Character.toUpperCase(lettre)){
 			return mot;
 		}
 		else{
 		return "Error";
 		}
 	}
 	
 	
 	public int nbLigneTexte(String texte) {
 		String fichier = texte;
		int count=0;
 		// lecture du fichier texte
 		try {
 			InputStream ips = new FileInputStream(fichier);
 			InputStreamReader ipsr = new InputStreamReader(ips);
 			br = new BufferedReader(ipsr);
 			String ligne;
 			while ((ligne = br.readLine()) != null) {
 				count++;
 				
 			}
 			br.close();
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 		return count;
 	}
 	
 	
 	public String getMotDeLaLigne(String texte, int nb){
 		String fichier = texte;
 		// lecture du fichier texte
 		try {
 			InputStream ips = new FileInputStream(fichier);
 			InputStreamReader ipsr = new InputStreamReader(ips);		
 			br = new BufferedReader(ipsr);
 			String ligne;
 			while ((ligne = br.readLine()) != null) {
 				for(int i = 0; i!= nb;i++) {
 					ligne = br.readLine();
 				}
 				return ligne;
 			}
 			br.close();
 		}catch (Exception e) {
 			System.out.println(e.toString());
 		}
 		return "Error";
 	}
 	
 	public char choisirLettre(String texte){
 		//texte="ListeFruits.txt";
 		int nb = nbLigneTexte(texte);
 		int alea = (int)Math.floor(Math.random()*nb);
 		String mot = getMotDeLaLigne(texte, alea);
 		char lettre = mot.charAt(0);
 		return lettre;
 	}
 	
 	//Utilise random pour choisir une liste
 	//Les liste dans un tableau ?
 	public String choisirListe(){
 		listes = new String[8];
 		listes[0]= "..\\ressources\\ListeSports.txt";
 		listes[1]= "..\\ressources\\ListeFruits.txt";
 		listes[2]= "..\\ressources\\ListeMetiers.txt";
 		listes[3]= "..\\ressources\\ListeVillesDeFrance.txt";
 		listes[4]= "..\\ressources\\ListePays.txt";
 		listes[5]= "..\\ressources\\ListeMammiferes.txt";
 		listes[6]= "..\\ressources\\ListePrenoms.txt";
 		listes[7]= "..\\ressources\\ListePlanetes.txt";
 
 		int i = (int)Math.floor(Math.random()*8);
 		return listes[i];
 	}
 	
 	public String printListe(String texte){
 		return texte.substring(19,texte.length()-1); 
 	}
 	
 	public String laQuestionEst(){
 		String liste= choisirListe();
 		return printListe(liste) + " en " + choisirLettre(liste);
 	}
 }
 
