 package Projet.Jeu;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 import org.fusesource.jansi.*;
 
 import Projet.Liste.*;
 import Projet.Carte.*;
 import Projet.Joueur.*;
 import Projet.Couleur.*;
 import Projet.Plateau.*;
 import Projet.Cube.*;
 
 import java.io.*;
 
 public class Jeu
 {
     /*********************/
     /***** ATTRIBUTS *****/
     /*********************/
     
     // Les cartes Trophes
     private ArrayList<Trophee> listeTrophee = new ArrayList<Trophee>();
 
     private Pioche pioche       = new Pioche();
     private Defausse defausse   = new Defausse();
     private Sac sac             = new Sac();
     private Plateau plateau     = new Plateau();
 	private Joueur joueur1;
     private Joueur joueur2;
     
     public final int NB_TUILE = 4;
     public final int NB_CARTE_PAR_JOUEUR = 8;
     
     private int[][] tabBallon = {   { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },         // rouge
                                     { 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1 },         // jaune
                                     { 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1 },         // vert
                                     { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 },         // bleu
                                     { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 },         // gris
                                 };
 
 	/********************/
 	/*** CONSTRUCTEUR ***/
 	/********************/
 	
 	public Jeu( String nom1, String nom2 )	
 	{
 		joueur1= new Joueur(nom1);
 		joueur2= new Joueur(nom2);
 	}
 	
 	/*****************/
 	/*** ACCESSEUR ***/
 	/*****************/
 	
 	public Joueur getJoueur1() 		{	return joueur1; 	}
 	public Joueur getJoueur2() 		{	return joueur2; 	}
 	public Plateau getPlateau()		{	return plateau;		}
 	public Pioche getPioche()		{	return pioche;		}
 	public Defausse getDefausse()	{	return defausse;	}
 	public Sac getSac()				{	return sac;			}
 	public ArrayList<Trophee> getListeTrophee()    {    return listeTrophee;    }
 			  
     public void initialiserJeu()
     {
         // initialisation de la pioche
         pioche.initialiserPioche(tabBallon);
         pioche.melangerPioche();
 		
         // initialisation du sac
         sac.initialiserSac(tabBallon);
         sac.melangerSac();
         
         // initialisation de la liste des trophes
         initialiserListeTrophee();
         
         // donne des cartes au joueurs
         donnerCarteAuxJoueurs();	  
 	       
         // on initialise le plateau
         initialiserPlateau();
         initialiserCubeSurTuile();
     }
     
     /*********************************/
     /*** METHODES POUR INITIALISER ***/
     /*********************************/
     
     public void donnerCarteAuxJoueurs()
     {
         for (int i = 0; i < NB_CARTE_PAR_JOUEUR; i++)
         {
             pioche.distribuerCarte(joueur1);
             pioche.distribuerCarte(joueur2);
         }
     }
     
     public void initialiserPlateau()
     {
         for (int i = 0; i < NB_TUILE; i++)
         {
             if ( i % 2 == 0)
                 plateau.ajouterTuile(new Tuile(new Paysage("Plaine", "Montagne")));
             else
                 plateau.ajouterTuile(new Tuile(new Paysage("Montagne", "Plaine")));
         }
     }
     
     public void initialiserCubeSurTuile()
     {
         for (int i = 0; i < NB_TUILE; i++)
         {
 			for (int j = 0; j < plateau.getTuile(i).getAttribut(); ++j)
 			{
 				plateau.getTuile(i).ajouterCubeSurPaysage(sac.getDernierElement());
 				sac.supprimerDernierElement();
 			}
         }
     }
     
     public void afficherVosCartes(Joueur joueur)
     {
         joueur.afficherMain();
     }
     
     public void DefausseVersPioche()
     {       
         ArrayList<Ballon> inter = new ArrayList<Ballon> (defausse.getDefausse());
         for (Ballon b: inter)
             pioche.ajouterElement(b);
 			
 		pioche.melangerPioche();    
     }
     
 	public boolean PeutMettreUneCarte(char cote, int choixTuile)
 	{
 		if (plateau.getTuile(choixTuile).getElementJeu(cote) == plateau.getTuile(choixTuile).getAttribut())
 			return false;
 		
 		return true;
 	}
     
 	public void carteVersTuile(Joueur joueur, int choixBallon, char cote, int choixTuile)
 	{
 		if (PeutMettreUneCarte(cote, choixTuile))
 		{
 			plateau.getTuile(choixTuile).ajouterBallon(joueur.getBallon(choixBallon), cote);
 			joueur.supprimerBallon(choixBallon);
 		}
 	}
 	
 	public int getAttribut(Couleur couleur)
 	{
 		for (Trophee t: listeTrophee)
 			if (t.getCouleur().equals(couleur.getLibelle()))
 				return t.getNumero();
 
 		return 0;
 	} 
 
 	public Trophee getTrophee(Couleur couleur)
 	{
 		for (Trophee t: listeTrophee)
 			if (t.getCouleur().equals(couleur.getLibelle()))
 				return t;
 
 		return null;
 	}
     
     
 	/*********************************/
 	/*** INITIALISATION DES LISTES ***/
 	/*********************************/
     
 	public void initialiserListeTrophee()
 	{
 		listeTrophee.add( new Trophee(3, Couleur.GRIS.getLibelle()));
 		listeTrophee.add( new Trophee(4, Couleur.BLEU.getLibelle()));
 		listeTrophee.add( new Trophee(5, Couleur.VERT.getLibelle()));
 		listeTrophee.add( new Trophee(6, Couleur.JAUNE.getLibelle()));
 		listeTrophee.add( new Trophee(7, Couleur.ROUGE.getLibelle()));
 	}
     
 	/***********/
 	/** AUTRE **/
 	/***********/
     
 	public void donnerCubeAuJoueur(Joueur joueur, int choixTuile)
 	{
 		for (int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			joueur.ajouterCube(plateau.getTuile(choixTuile).getPaysage().getDernierElement());
 			plateau.getTuile(choixTuile).getPaysage().supprimerDernierElement();
 		}
 	}
     
 	public boolean PeutAvoirUnTrophee(Joueur joueur,Couleur couleur)
 	{
 		for (Trophee t: listeTrophee)
 			if (t.getCouleur().equals(couleur.getLibelle()))
 				if (t.getNumero() == joueur.getNbCube(couleur))
 					return true;
 
 		return false;
 	}       
     
 	public void CubeEnTrophee(Joueur joueur, Couleur couleur)
 	{
 		joueur.ajouterTrophee(getTrophee(couleur));
 		joueur.supprimerCube(couleur.getLibelle());
 		sac.ajouterElement(getAttribut(couleur), couleur);
 	}
 	
 	public int choisirBallon()
 	{
 		int choixBallon = 0;
 		do
 		{
 			System.out.println("Choissisez une carte a placer sur la tuile : ");
 			Scanner sc = new Scanner(System.in);
 			choixBallon = sc.nextInt();
 			--choixBallon;
 		} while (choixBallon < 0 || choixBallon >= 8);
 		
 		return choixBallon;
 	}
 	
 	public char choisirCote()
 	{
 		char choixCote = ' ';
 		do
 		{
 			System.out.println("Choissisez un cote pour mettre votre carte : ");
 			Scanner sc = new Scanner(System.in);
 			String str = sc.nextLine();
 			choixCote = str.charAt(0);
 		} while (choixCote != 'g' && choixCote != 'd' &&choixCote != 'D' && choixCote != 'G');
 		
 		return choixCote;
 	}
 	
 	public int choisirTuile()
 	{
 		int choixTuile = 0;
 		do
 		{
 			System.out.println("Choissisez une tuile : ");
 			Scanner sc = new Scanner(System.in);
 			choixTuile = sc.nextInt();
 			choixTuile--;
 		} while ( choixTuile > 4 || choixTuile < 0);
 		
 		return choixTuile;
 	}
 	
 	public boolean cotePlein(int choixTuile, char choixCote)
 	{
 		if (plateau.getTuile(choixTuile).estPleine(choixCote))
 			return true;
 		
 		return false;
 	}
 	
 	public boolean cubeUtilise(int choixTuile, char choixCote, String couleur)
 	{
 		Cube cube = null;
 		for (int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			// on recupere le cube
 			cube = plateau.getTuile(choixTuile).getPaysage().getElement(i);
 			// si la couleur est egale a la couleur du cube
 			if (couleur.equals(cube.getCouleur()))
 			{
 				// si le cube est "deja" utilise
 				if (plateau.getTuile(choixTuile).getPaysage().estUtilise(cube, choixCote))
 				{
 					System.out.println(choixCote + "\t test");
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean verifcouleur(int choixTuile, char choixCote, String couleur)
 	{
 		Cube cube = null;
 		for (int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			// on recupere le cube
 			cube = plateau.getTuile(choixTuile).getPaysage().getElement(i);
 			if (couleur.equals(cube.getCouleur()))
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean impossibleDeJoueur(Joueur joueur)
 	{
 		Cube cube = null;
 		String couleur = "";
 		for (int i = 0; i < NB_TUILE; i++)
 		{
 			for (int j = 0; j < plateau.getTuile(i).getAttribut(); j++)
 			{
 				cube = plateau.getTuile(i).getPaysage().getElement(j);
 				for(int k = 0; k < NB_CARTE_PAR_JOUEUR; k++)
 				{
 					couleur = joueur.getBallon(k).getCouleur();
 					if (couleur.equals(cube.getCouleur()))
 						return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	public void ajouterCubeUtilise(int choixTuile, char choixCote, String couleur)
 	{
 		Cube cube = null;
 		for (int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			cube = plateau.getTuile(choixTuile).getPaysage().getElement(i);
 			if (couleur.equals(cube.getCouleur()))
 			{
 				plateau.getTuile(choixTuile).getPaysage().ajouterCubeUtilise(cube, choixCote);
 				break;
 			}
 		}
 	}
 	
 	public void piocher(Joueur joueur)
 	{
 		pioche.distribuerCarte(joueur);
 	}
 	
 	public Joueur quiAGagne(Joueur dernierJoueur, char[][] coteJoueur, int choixTuile)
 	{
 		// on compte les points des points
 		int compteDroit = plateau.getTuile(choixTuile).getResultat('D');
 		int compteGauche = plateau.getTuile(choixTuile).getResultat('G');
 		
 		char cote;
 		// On regarde quel cote a gagne
 		if (compteDroit > compteGauche)
 		{	
			if (plateau.getTuile(choixTuile).getPaysage().getRecto().equals("Plaine"))
 				cote = 'G';
 			else
 				cote = 'D';
 		}
 		else if (compteDroit < compteGauche)
 		{
			if (plateau.getTuile(choixTuile).getPaysage().getRecto().equals("Plaine"))
 				cote = 'D';
 			else
 				cote = 'G';
 		}
 		else
 			return dernierJoueur;
 		
 		for (int i = 0; i < coteJoueur.length; i++)
 		{
 			// on regarde le cote assiocier au joueur
 			if (coteJoueur[1][i] == cote)
 			{
 				if (coteJoueur[0][i] == '1')
 					return joueur1;
 				else
 					return joueur2;
 			}
 		}
 		System.out.println("error : return null");
 		// normalement impossible
 		return null;
 	}
 	
 	public void distribuerCube(Joueur joueur, int choixTuile)
 	{
 		for(int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			Cube cube = plateau.getTuile(choixTuile).getPaysage().getElement(i);
 			joueur.ajouterCube(cube);
 			
 		}
 		plateau.getTuile(choixTuile).getPaysage().supprimerTousLesElements();
     }
 	
 	public void tuileVersDefausse(int choixTuile)
 	{
 		for (int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			// partie gauche
 			defausse.ajouterElement(plateau.getTuile(choixTuile).getBallon(i, 'G'));
 			// partie droite
 			defausse.ajouterElement(plateau.getTuile(choixTuile).getBallon(i, 'D'));
 		}
 		plateau.getTuile(choixTuile).supprimerToutesLesCartes();
 	}
 	
 	public void inverserTuile()
 	{
 		for (int i = 0; i < NB_TUILE; i++)
 			plateau.getTuile(i).getPaysage().inverserPaysage();
 	}
 	
 	public void supprimerCubeDejaUtilise(int choixTuile)
 	{
 		plateau.getTuile(choixTuile).getPaysage().supprimerCubeDejaUtilise();
 	}
 	
 	
     public String toString()
     {
 		String s;
 	
 		//affichage plateau entier
 		s = "Joueur 1 : " + joueur1.getNomJoueur() +"\t" + "Joueur 2 : " + joueur2.getNomJoueur() +"\n\n";   
 		s += plateau.toString();
 		s += "\n";
 	
 		return s;
     }
 	
 	public boolean AUneTuilePleine()
 	{
 		for (int i = 0; i < NB_TUILE; i++)
 			if (plateau.getTuile(i).estEntierementPleine())
 				return true;
 				
 		return false;
 	}
 	public int quelTuilePleine()
 	{
 		for (int i = 0; i < NB_TUILE; i++)
 			if (plateau.getTuile(i).estEntierementPleine())
 				return i;
 		
 		return -1;
 	}
 	
 	public void inverserLaTuile(int choixTuile)
 	{
 		plateau.getTuile(choixTuile).getPaysage().inverserPaysage();
 	}
 	
 	public void ajouterCube(int choixTuile)
 	{
 		for (int i = 0; i < plateau.getTuile(choixTuile).getAttribut(); i++)
 		{
 			plateau.getTuile(choixTuile).ajouterCubeSurPaysage(sac.getDernierElement());
 			sac.supprimerDernierElement();
 		}
 	}
 	
 	public boolean peutAcheterTrophee(Joueur joueur)
 	{
 		int cptG=0;
 		int cptB=0;
 		int cptV=0;
 		int cptJ=0;
 		int cptR=0;
 		
 		for ( Cube c : joueur.getListeCube() )
 		{
 			if ( c.getCouleur().equals("Gris") )
 				cptG++;
 			if ( c.getCouleur().equals("Vert") )
 				cptV++;
 			if ( c.getCouleur().equals("Jaune") )
 				cptJ++;
 			if ( c.getCouleur().equals("Bleu") )
 				cptB++;
 			if ( c.getCouleur().equals("Rouge") )
 				cptR++;
 
 		}
 		
 		if ( cptG >= 3 || cptB >= 4 || cptB >= 4 || cptB >= 4 || cptB >= 4)
 			return true;
 			
 			
 		return false;
 	}
 	
 	public String couleurDispo( Joueur j )
 	{
 		String s ="Achat de Trophees.\nAchat Possibles :  ";
 		
 		int cptG=0;
 		int cptB=0;
 		int cptV=0;
 		int cptJ=0;
 		int cptR=0;
 		
 		for ( Cube b : j.getListeCube() )
 		{
 			if ( b.getCouleur().equals("Gris") )
 				cptG++;
 			if ( b.getCouleur().equals("Vert") )
 				cptV++;
 			if ( b.getCouleur().equals("Jaune") )
 				cptJ++;
 			if ( b.getCouleur().equals("Bleu") )
 				cptB++;
 			if ( b.getCouleur().equals("Rouge") )
 				cptR++;
 
 		}
 		
 		if ( cptG >= 3 )
 			s += "Gris ";
 		if ( cptB >= 4 )
 			s += "Bleu ";
 		if ( cptV >= 5 )
 			s += "Vert ";
 		if ( cptJ >= 6 )
 			s += "Jaune ";
 		if ( cptR >= 7 )
 			s += "Rouge ";
 			
 		s += "\nRapel : P=Passer, G=Gris, B=Bleu, V=Vert, R=Rouge, J=Jaune.\nChoississez : ";
 			
 		return s;
 	}
 	
 	public boolean prendreCubeImpossible(Joueur joueur, char choixCouleur)
 	{
 		int cpt = 0;
 		for (Cube b : joueur.getListeCube())
 			if ( b.getCouleur().equals(choixCouleur))
 				cpt++;
 	
 		int attribut = 0;
 		for (Trophee t: listeTrophee)
 			if (choixCouleur == t.getCouleur().charAt(0))
 				attribut = t.getNumero();
 				
 		if (cpt >= attribut && attribut > 0)
 			return false;
 			
 		return true;
 	}
 	
 	public void acheterTrophee( Joueur j, Trophee t  )
 	{
 		j.supprimerCube(t.getCouleur());
 		j.ajouterTrophee(t);
 	}
 	
 	public void supprimerTrophee( Trophee t )
 	{
 		int cpt =0;
 		
 		for ( Trophee t2 : getListeTrophee() )
 			if ( t2.getCouleur().equals(t.getCouleur()) )
 				cpt=listeTrophee.indexOf(t2);
 			
 			
 		listeTrophee.remove(cpt);
 	
 	}
 	
 	/************/
 	/*** MAIN ***/
 	/************/
     
 	public static void main (String[] args)
 	{
 		//Initialisation des Joueurs
 		Scanner entree = new Scanner(System.in);
 		System.out.print("Veuillez entrer le nom du joueur 1 : ");
 		String nom1 = entree.nextLine();
 		
 		entree = new Scanner(System.in);
 		System.out.print("Veuillez entrer le nom du joueur 2 : ");
 		String nom2 = entree.nextLine();
 		
 		System.out.println();
 		// initialisation des couleurs
 		AnsiConsole.systemInstall();
 	  
 		//Nouveau Jeu
 		Jeu jeu = new Jeu(nom1 , nom2);
 		jeu.initialiserJeu();
 		
 		//affichage du Jeu ( plateau )
 		System.out.println(jeu);
 		
 		char choixCoteJ1, choixCoteJ2;
 		do
 		{
 			System.out.println("Choisissez votre cote (G/D): ");
 			entree = new Scanner(System.in);
 			String str = entree.nextLine();
 			choixCoteJ1 = str.charAt(0);
 			
 		} while (choixCoteJ1 != 'G' && choixCoteJ1 != 'D');
 		
 		if ( choixCoteJ1 == 'G')		choixCoteJ2 = 'D';
 		else							choixCoteJ2 = 'G';
 		
 		// permet de savoir quel joueur gagne 
 		char[][] coteJoueur = { {     '1'    ,     '2'     },
 		                        { choixCoteJ1, choixCoteJ2 },
 							  };
 			
 		Joueur[] tabJoueur = { jeu.getJoueur1(), jeu.getJoueur2() };
 			
 		int choixTuile = 0;
 		int choixBallon = 0;
 		int dernierJoueur = 0;
 		char choixCote = ' ';
 		char choixCouleur = ' ';
 		
 		//Boucle de Jeu
 		while (jeu.getJoueur1().getTrophee() != 3 || jeu.getJoueur2().getTrophee() != 3)
 		{
 			while (!jeu.AUneTuilePleine())
 			{
 				for (int j = 0; j < tabJoueur.length; j++)
 				{
 					System.out.println("\n" + jeu.getPlateau().toString() + "\n");
 					System.out.println("Joueur : " + tabJoueur[j].getNomJoueur() + "\n");
 					System.out.println(tabJoueur[j] + "\n");
 					
 					boolean pass = true;
 					boolean impossibleDeJouer = false;
 					do
 					{
 						pass = true;
 						// si le joueur ne peut poser aucune carte alors il passe
 						if (jeu.impossibleDeJoueur(tabJoueur[j]))
 						{
 							System.out.println("Vous ne pouvez pas jouer :( ");
 							impossibleDeJouer = true;
 							break;
 						}
 						choixBallon = jeu.choisirBallon();
 						choixTuile = jeu.choisirTuile();
 						choixCote = jeu.choisirCote();
 					
 						if (jeu.cotePlein(choixTuile, choixCote))
 							pass = false;
 						if (jeu.cubeUtilise(choixTuile, choixCote, tabJoueur[j].getBallon(choixBallon).getCouleur()))
 							pass = false;
 						if (!jeu.verifcouleur(choixTuile, choixCote, tabJoueur[j].getBallon(choixBallon).getCouleur()))
 							pass = false;		
 					} while (!pass);
 					
 					if (impossibleDeJouer)
 						continue;
 						
 					// permet de ne plus "utilise" le cube de la couleur de la carte
 					jeu.ajouterCubeUtilise(choixTuile, choixCote, tabJoueur[j].getBallon(choixBallon).getCouleur());
 					// on place la carte choisi sur le jeu
 					jeu.carteVersTuile(tabJoueur[j], choixBallon, choixCote, choixTuile);
 					// si la pioche est vide alors on remet des cartes
 					if (jeu.getPioche().estVide())
 					jeu.DefausseVersPioche();
 					// lorsque le joueur joue il pioche une carte
 					jeu.piocher(tabJoueur[j]);
 					
 					if (jeu.peutAcheterTrophee(tabJoueur[j]))
 					{
 						entree = new Scanner(System.in);
 						System.out.println(" Voulez-vous acheter un Trophee ? ");
 						boolean achete = entree.hasNextBoolean();
 						
 						if (achete)
 						{
 							do
 							{
 								entree = new Scanner(System.in);
 								System.out.println(jeu.couleurDispo(tabJoueur[j]));
 								String str = entree.nextLine();
 								choixCouleur = str.charAt(0);
 							} while (choixCouleur != 'P' || jeu.prendreCubeImpossible(tabJoueur[j], choixCouleur));
 							
 							if ( choixCouleur != 'P' )
 							{	
 								Trophee trophee = null;
 								
 								int i = 0;
 								for (Trophee t: jeu.getListeTrophee())
 								{
 									if (choixCouleur == jeu.getListeTrophee().get(i).getCouleur().charAt(0))
 										trophee = jeu.getListeTrophee().get(i);
 										
 									i++;
 								}
 									
 								jeu.acheterTrophee( jeu.getJoueur1(), trophee);
 
 								System.out.println("Transaction Effectue !");
 								jeu.supprimerTrophee(trophee);
 
 							}
 							else
 								System.out.println("Aucun Achat Effectue !");
 						}
 					}
 					dernierJoueur = j;
 				}
 			}
 			int tuilePleine = jeu.quelTuilePleine();
 			// on regarde quel joueur a gagne
 			Joueur joueur = jeu.quiAGagne(tabJoueur[dernierJoueur], coteJoueur, tuilePleine);
 			// on distribue les cubes au gagnant et on supprime les cubes de la tuile
 			jeu.distribuerCube(joueur, tuilePleine);
 
 			// on met les cartes de la tuile fini dans la defausse
 			jeu.tuileVersDefausse(tuilePleine);
 			// on supprime les cube deja utilise
 			jeu.supprimerCubeDejaUtilise(tuilePleine);
 			jeu.inverserLaTuile(tuilePleine);
 			jeu.ajouterCube(tuilePleine);
 		}
 	}
 }       
