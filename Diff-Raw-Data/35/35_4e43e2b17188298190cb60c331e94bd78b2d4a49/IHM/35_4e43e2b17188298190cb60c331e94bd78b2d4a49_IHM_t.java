 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.rmi.ConnectException;
 import java.rmi.ConnectIOException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.InputMismatchException;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 /**
  * Cette classe est chargée d'effectuer les affichages des menus et
  * les contrôles sur les saisies de l'utilisateur.
  *
  * @author Marc Boisban
  * @author Kamel Gharout
  */
 public class IHM {
 
   /****************************   Attributs   *********************************/
   private Noeud noeud;
   private Scanner scanner;
 
   /****************************   Constructeur(s)   ***************************/
   /**
    * Constructeur vide : instancie les attributs de la classe
    */
   public IHM() {
     super();
     try {
       noeud = new Noeud();
       scanner = new Scanner(System.in);
     }
     catch (RemoteException re) {
       re.printStackTrace();
     }
   }
 
   /**
    * Initialise les attributs de la classe avec les valeurs passées en paramètres
    *
    * @param noeud
    *        la nouvelle valeur de l'attribut noeud
    *
    * @param scanner
    *        la nouvelle valeur de l'attribut scanner
    */
   public IHM(Noeud noeud, Scanner scanner) {
     super();
     this.noeud = noeud;
     this.scanner = scanner;
   }
 
   /****************************   Accesseurs   ********************************/
   /**
    * Renvoie le noeud client de la classe
    *
    * @return le noeud client de la classe
    */
   public Noeud getNoeud() {
     return noeud;
   }
 
   /**
    * Affecte à l'attribut noeud la valeur passée en paramètre
    *
    * @param noeud
    *        la nouvelle valeur de l'attribut noeud
    */
   public void setNoeud(Noeud noeud) {
     this.noeud = noeud;
   }
 
   /****************************   Autres méthodes   ***************************/
   /**
    * Cette méthode affiche le menu principal.
    *
    * Contraintes sur les saisies :
    * <ul>
    * <li> Les valeurs saisies doivent être numériques </li>
    * <li> Les valeurs saisies doivent être comprises entre 1 et 8 </li>
    * </ul>
    */
   public void menu() {
     int choix = 0;
     do {
       System.out.println("Bienvenue");
       System.out.println("=========\n");
       System.out.println("   1. Afficher les fichiers du répertoire courant");
       System.out.println("   2. Assigner un degré de confidentialité à un fichier");
       System.out.println("   3. Ajouter un noeud à la liste des noeuds de confiance");
       System.out.println("   4. Enlever un noeud de la liste des noeuds de confiance");
       System.out.println("   5. Modifier la liste des noeuds de confiance pour un fichier");
       System.out.println("   6. Nettoyer les fichiers dupliqués et n’étant plus dans le répertoire");
       System.out.println("   7. Récupérer les données perdues");
       System.out.println("   8. Quitter");
       System.out.println("");
      
       choix = saisir("Veuillez saisir l’action désirée : ", 8);
 
       switch (choix) {
 
         case 1:
           listerFichiers();
           break;
         case 2:
           assignerConfidentialite();
           break;
         case 3:
           ajouterNoeudConfiance();
           break;
         case 4:
           supprimerNoeudConfiance();
           break;
         case 5:
           modifierListeNoeudsConfianceFichier();
           break;
         case 6:
           nettoyer();
           break;
         case 7:
           recuperer();
           break;
         case 8:
           break;
       }
     }
     while (choix != 8);
 
     System.out.println("Sortie du menu général.");
   }
 
   /**
    * Affiche la liste des fichiers que contient le noeud client (la machine
    * courante)
    */
   public void listerFichiers() {
     System.out.println("Liste des fichiers du répertoire courant");
     System.out.println("========================================\n");
     for (Fichier fichier : noeud.getListeFichiers()) {
       System.out.println("   " + fichier.getNom());
     }
     revenir();
   }
 
   /**
    * Ecran permettant d'assigner des degrés de confidentialité aux fichiers
    *
    * Contraintes sur les saisies :
    * <ul>
    * <li> Les valeurs saisies doivent être numériques </li>
    * <li> La valeur saisie pour le choix du fichier doit être comprise entre 1 et n+1 (n étant le nombre de fichiers de l'utilisateur </li>
    * <li> Le degré de confidentialité saisis doit être compris entre 1 et 4 </li>
    * </ul>
    */
   public void assignerConfidentialite() {
     int cpt;
     int choix, choix2 = 0;
 
     cpt = 1;
     System.out.println("Assigner un degré de confidentialité");
     System.out.println("====================================\n");
    
     for (Fichier fichier : noeud.getListeFichiers()) {
       System.out.println("   " + cpt + ". " + fichier.getNom());
       cpt = cpt + 1;
     }
 
     System.out.println("\n   " + cpt + ". Retourner au menu principal");
 
     choix = saisir("\nVeuillez sélectionner le fichier dont vous voulez assigner un degré de confidentialité :", cpt);
 
     if (choix < cpt) {
       choix2 = saisir("Quel degré de confidentialité souhaitez-vous lui assigner ?", 4);
       String nomFichier = noeud.getListeFichiers().get(choix - 1).getNom();
       noeud.assignerConfidentialite(nomFichier, choix2);
       System.out.println("Degré de confidentialité assigné avec succès.");
       revenir();
     }
    }
  
 
   /**
    * Permet d'ajouter un noeud de confiance
    *
    * Contraintes sur les saisies :
    * <ul>
    * <li> L'adresse réseau saisie doit être valide </li>
    * <li> Le degré de confiance saisi doit être compris entre 1 et 3</li>
    * </ul>
    */
   public void ajouterNoeudConfiance() {
     int choix, choix2;
     String adresse;
     System.out.println("Ajout d’un noeud de confiance");
     System.out.println("====================================\n");
 
     if (noeud.getListeNoeudsConfiance().isEmpty()) {
       System.out.println("Aucun noeud de confiance enregistré.");
     }
     for (NoeudConfiance noeudConfiance : noeud.getListeNoeudsConfiance()) {
       System.out.println("   " + noeudConfiance.getAdresse());
     }
     System.out.println("\n1. Retourner au menu principal\n");
     boolean succes;
 
     do{
       succes = true;
     System.out.println("Veuillez saisir l’adresse réseau du nouveau noeud de confiance :");
 
     scanner = new Scanner(System.in);
     adresse = scanner.next();
     
     if (!adresse.equals("1")) {
       try {
         
         System.out.println("Tentative de connexion à la machine " + adresse + " ...\n");
         if (noeud.ajouterNoeudConfiance(adresse)) {
           System.out.println("Connexion effectuée.");
          
           choix = saisir("Quel degré de confiance souhaitez-vous lui assigner ?", 3);
         
           noeud.assignerConfiance(adresse, choix);
           System.out.println("Noeud de confiance ajouté avec succès.\n");
           noeud.dupliquerFichiers();
         }
         else {
           System.out.println("Noeud de confiance déjà présent dans la liste.");
           System.out.println("Aucun ajout effectué.\n");
           System.out.println("Voulez-vous modifier le degrès de confiance du noeud de confiance?");
           System.out.println("1. Oui");
           System.out.println("2. Non");
           choix2 = saisir("", 2);
           if (choix2 == 1) {
             choix = saisir("Quel degré de confiance souhaitez-vous lui assigner ?", 3);
             noeud.assignerConfiance(adresse, choix);
             System.out.println("Degrès de confiance modifié avec succès.\n");
           }
            noeud.dupliquerFichiers();
         }
         revenir();
       }
       catch (MalformedURLException mue) {
         System.out.println("Erreur lors de l'ajout du noeud de confiance : adresse IP non valide.");
         succes = false;
       }
       catch (ConnectException ce) {
         System.out.println("Erreur lors de l'ajout du noeud de confiance : service de noms de la machine distante non trouvé.");
         succes = false;
       }
       catch (ConnectIOException ce) {
         System.out.println("Erreur lors de l'ajout du noeud de confiance : service de noms de la machine distante non trouvé.");
         succes = false;
       }
       catch (NotBoundException ex) {
         System.out.println("Erreur lors de l'ajout du noeud de confiance : objet distant non trouvé.");
         succes = false;
       }
       catch (UnknownHostException uhe) {
           System.out.println("Erreur lors de l'ajout du noeud de confiance : objet inconnu");
           succes = false;
       }
       catch (IOException ioe) {
         ioe.printStackTrace();
         succes = false;
       }
     }
     }while(!succes);
   }
 
   /**
    * Permet de supprimer un noeud de confiance
    *
    * Contraintes sur les saisies :
    * <ul>
    * <li> Le choix du noeud de confiance saisi doit être compris entre 1 et n+1 (n étant le nombre de noeud de confiance de la liste) </li>
    * <li> Le degré de confiance saisi doit être compris entre 1 et 3</li>
    * </ul>
    */
   public void supprimerNoeudConfiance() {
 
     int taille;
     int choix = 0;
   
     System.out.println("Suppression d’un noeud de confiance");
     System.out.println("====================================\n");
 
     if (noeud.getListeNoeudsConfiance().isEmpty()) {
       System.out.println("Aucun noeud de confiance enregistré.");
     }
     taille = noeud.getListeNoeudsConfiance().size();
     for (int i = 0; i < taille; i++) {
       System.out.println("   " + (i + 1) + ". " + noeud.getListeNoeudsConfiance().get(i).getAdresse());
     }
     if (taille > 0) {
       System.out.println("\n   " + (taille + 1) + ". Retourner au menu principal"); 
       choix = saisir("Veuillez choisir le noeud de confiance à supprimer :", taille + 1);
     }
 
     if (taille > 0 && choix <= taille) {
       noeud.supprimerNoeudConfiance(noeud.getListeNoeudsConfiance().get(choix - 1).getAdresse());
       System.out.println("\nNoeud de confiance supprimé avec succès.\n");
     }
     if (choix != 2) {
       revenir();
     }
   }
 
   /**
    * Permet de modifier la liste des noeuds de confiance spécifique à un fichier.
    *
    * Contrainte sur les saisies pour le premier écran:
    * <ul>
    * <li> Le choix d'ajout,de suppression ou de retour doit être compris entre 1 et 3 </li>
    * </ul>
    *
    * Contrainte sur les saisies pour les écrans d'ajout et de suppression:
    * <ul>
    * <li> Le choix du noeud de confiance saisi doit être compris entre 1 et n+1 (n étant le nombre de noeud de confiance de la liste) </li>
    * </ul>
    */
   public void modifierListeNoeudsConfianceFichier() {
 
     List<String> listeAutorise = new ArrayList<String>();
     int taille;
     int cpt2 = 1;
     int choix, choix2, choix3, choix4, choix5, cpt = 0;
     boolean autorise;
     
     System.out.println("Modification de la liste des noeuds de confiance");
     System.out.println("===================================================\n");
  
     taille = noeud.getListeFichiers().size() + 1;
     for (Fichier fichier : noeud.getListeFichiers()) {
       System.out.println("   " + cpt2 + ". " + fichier.getNom());
       cpt2 = cpt2 + 1;
     }
     System.out.println("   " + cpt2 + ". Retourner au menu principal");
     choix = saisir("\nVeuillez sélectionner le fichier dont vous voulez modifier la liste des noeuds :", cpt2);
    
     if (choix < taille) {
       do {
         HashMap<String, Boolean> noeudFichierMap;
         System.out.println(" 1. Ajouter un noeud de confiance à un fichier");
         System.out.println(" 2. Enlever un noeud de confiance à un fichier");
         System.out.println(" 3. Retourner au menu principal");
       
         choix2 = saisir("Veuillez sélectionner la modification que vous souhaitez réaliser :", 3);
         
         if (choix2 == 2) {
         
           cpt = 0;
           System.out.println("Suppression d'un noeud de confiance pour le fichier");
           System.out.println("===================================================");
           System.out.println("Liste des noeuds de confiance dont la duplication est autorisée");
          noeudFichierMap = noeud.getListeFichiers().get(choix - 1).getNoeudsConfianceMap();
 
           int niveauConfidentialite = noeud.getListeFichiers().get(choix - 1).getNiveauConfidentialite();
 
           for (NoeudConfiance noeudConfiance : noeud.getListeNoeudsConfiance()) {
             if (noeudConfiance.getNiveauConfiance() >= niveauConfidentialite) {
               listeAutorise.add(noeudConfiance.getAdresse());
             }
           }
 
           for (Entry<String, Boolean> currentEntry : noeudFichierMap.entrySet()) {
             if (currentEntry.getValue()) {
               if (!listeAutorise.contains(currentEntry.getKey())) {
                 listeAutorise.add(currentEntry.getKey());
               }
             }
             else {
               if (listeAutorise.contains(currentEntry.getKey())) {
                 listeAutorise.remove(currentEntry.getKey());
               }
             }
           }
 
           if (listeAutorise.isEmpty()) {
             System.out.println("\nAucun noeud de confiance autorisé spécifiquement pour ce fichier.");
             revenir();
           }
           else {
             for (String adresse : listeAutorise) {
               cpt += 1;
               System.out.println("   " + cpt + ". " + adresse);
             }
 
             cpt += 1;
            System.out.println("\n   " + cpt + ". Retourner au menu précédent.");
 
             choix3 = saisir("\nVeuillez selectionner le noeud de confiance à supprimer pour le fichier :", cpt);
             if (choix3 < cpt) {
              //noeudFichierMap.put(listeAutorise.get(choix3 - 1), false);
              noeud.supprimerNoeudConfianceFichier(noeud.getListeFichiers().get(choix - 1).getNom(), listeAutorise.get(choix3 -1));
               System.out.println("Le noeud a été supprimé");
               if (choix3 != cpt) {
                 revenir();
               }
             }
           }
         }
         else if (choix2 == 1) {
 
           cpt = 0;
           System.out.println("Ajout d'un noeud de confiance pour le fichier");
           System.out.println("===================================================\n");
           System.out.println("Liste des noeuds de confiance dont la duplication n'est pas autorisée");
          noeudFichierMap = noeud.getListeFichiers().get(choix - 1).getNoeudsConfianceMap();
 
           int niveauConfidentialite = noeud.getListeFichiers().get(choix - 1).getNiveauConfidentialite();
 
           for (NoeudConfiance noeudConfiance : noeud.getListeNoeudsConfiance()) {
             if (noeudConfiance.getNiveauConfiance() < niveauConfidentialite) {
               if (!listeAutorise.contains(noeudConfiance.getAdresse())) {
                 listeAutorise.add(noeudConfiance.getAdresse());
               }
             }
           }
 
           for (Entry<String, Boolean> currentEntry : noeudFichierMap.entrySet()) {
             if (currentEntry.getValue() == false) {
               if (!listeAutorise.contains(currentEntry.getKey())) {
                 listeAutorise.add(currentEntry.getKey());
               }
             }
             else {
               if (listeAutorise.contains(currentEntry.getKey())) {
                 listeAutorise.remove(currentEntry.getKey());
               }
             }
           }
           if (listeAutorise.isEmpty()) {
             System.out.println("\nAucun noeud de confiance non-autorisé spécifiquement pour ce fichier.");
             revenir();
           }
           else {
             for (String adresse : listeAutorise) {
               cpt += 1;
               System.out.println("   " + cpt + ". " + adresse);
             }
 
             cpt += 1;
            System.out.println("\n   " + cpt + ". Retourner au menu précédent.");
             choix4 = saisir("Veuillez selectionner le noeud de confiance à ajouter pour le fichier :", cpt);
        
             if (choix4 < cpt) {
              noeud.ajouterNoeudConfianceFichier(noeud.getListeFichiers().get(choix - 1).getNom(), listeAutorise.get(choix4 -1), true);
              //noeudFichierMap.put(listeAutorise.get(choix4 - 1), true);
               System.out.println("Le noeud a été ajouté.");
               if (choix4 != cpt) {
                 revenir();
               }
             }
           }
         }
        listeAutorise.clear();
       }
      while (choix2 != 3);
     }
   }
 
   /**
    * Permet de nettoyer les fichiers dupliqués n'étant plus présent sur le client.
    *
    */
   public void nettoyer() {
     noeud.nettoyerFichiersDupliques();
     System.out.println("Nettoyage des fichiers effectué");
     revenir();
   }
 
   /**
    * Permet de récupérer les fichiers perdus qui ont été dupliqués sur les noeuds de confiance.
    *
    */
   public void recuperer() {
     List<String> listeNomsFichier = noeud.recupererFichiersPerdus();
 
     if (listeNomsFichier.isEmpty()) {
       System.out.println("Aucun fichier récupéré");
     }
     else {
       System.out.println("Fichiers récupérés");
       System.out.println("------------------");
       //afficher les fichier recuperer
       for (String nomFichier : listeNomsFichier) {
         System.out.println("   " + nomFichier);
       }
     }
     revenir();
   }
 
   /**
    * Permet de récupérer la saisie au clavier de l'utilisateur. Un contrôle
    * est aussi sur cette saisie par l'appel de la méthode controler()
    *
    * @param message
    *        le message à afficher pour l'invite de commandes
    *
    * @param max
    *        la valeur maximale de la saisie
    *
    * @return la valeur saisie par l'utilisateur
    */
   public int saisir(String message, int max) {
 
     int saisie = 0;
     int controle = -1;
 
     do {
       try {
         System.out.println(message);
         saisie = scanner.nextInt();
         controle = controler(saisie, max);
       }
       catch (InputMismatchException ime) {
         System.out.println("Erreur de saisie: la valeur saisie doit être numérique.");
         scanner = new Scanner(System.in);
       }
     }
     while (controle == -1);
 
     return saisie;
   }
 
   /**
    * Effectue le contrôle de la valeur passée en paramètre par rapport à la valeur
    * max, elle aussi passée en paramètre. Le contrôle échoue si la valeur saisie
    * est inférieur à 1 ou supérieure à la valeur max. Si le contrôle échoue, la valeur
    * -1 est retournée
    *
    * @param saisie
    *        la valeur saisie par l'utilisateur
    *
    * @param max
    *        la valeur maximale de la saisie
    *
    * @return la valeur saisie par l'utilisateur si le controle passe avec succès,
    *         renvoie -1 sinon
    */
   public int controler(int valeur, int max) {
     if (valeur < 1 || valeur > max) {
       System.out.println("Erreur de saisie: la valeur saisie doit être comprise entre 1 et " + max + ". Valeur trouvée : " + valeur + ".");
       scanner = new Scanner(System.in);
       valeur = -1;
     }
     return valeur;
   }
 
   /**
    * Permet de revenir au menu précédent en appuyant sur la touche entrée
    *
    */
   public void revenir() {
     System.out.println("\nAppuyer sur entrée revenir au menu principal.");
     try {
       System.in.read();
     }
     catch (IOException ioe) {
       ioe.printStackTrace();
     }
   }
 }
