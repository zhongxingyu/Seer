 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.ConnectException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import serveur.Duplication;
 
 /**
  * Cette classe a pour but de modéliser un noeud.
  * Elle contient des méthodes propres au client et propres au serveur.
  *
  * @author Marc Boisban
  * @author Kamel Gharout
  */
 public class Noeud implements Duplication {
 
   /****************************   Attributs   *********************************/
   private String adresse;
   private List<Fichier> listeFichiers;
   private List<NoeudConfiance> listeNoeudsConfiance;
   //private List<String> listeNomsFichiers;
 
   /****************************   Constructeur(s)   ***************************/
   /*
    * Initialise un noeud client avec sa propre adresse comme attribut "adresse"
    */
   public Noeud() throws RemoteException {
     try {
       InetAddress Ip = InetAddress.getLocalHost();
       adresse = Ip.getHostAddress();
       listeFichiers = new ArrayList<Fichier>();
       listeNoeudsConfiance = new ArrayList<NoeudConfiance>();
       File file = new File(".");
       File[] files = file.listFiles();
       for (int i = 0; i < files.length; i++) {
         if (files[i].isFile()) {
           listeFichiers.add(new Fichier(files[i].getName()));
         }
       }
     }
     catch (UnknownHostException uhe) {
       uhe.printStackTrace();
     }
   }
 
 
   /****************************   Accesseurs   ********************************/
   /**
    * Renvoie l'adresse du noeud client
    * 
    * @return l'adresse du noeud client
    */
   public String getAdresse() {
     return adresse;
   }
 
   /**
    * Affecte à l'attribut adresse la valeur passée en paramètre
    * 
    * @param adresse
    *        la nouvelle valeur de l'adresse
    */
   public void setAdresse(String adresse) {
     this.adresse = adresse;
   }
 
   /**
    * Renvoie la liste des fichiers du noeud client
    *
    * @return la liste des fichiers du noeud client
    */
   public List<Fichier> getListeFichiers() {
     return listeFichiers;
   }
 
   /**
    * Affecte à l'attribut listeFichiers la liste passée en paramètre
    *
    * @param listeFichiers
    *        la nouvelle valeur de l'attribut listeFichiers
    */
   public void setListeFichiers(List<Fichier> listeFichiers) {
     this.listeFichiers = listeFichiers;
   }
 
   /**
    * Renvoie la liste des noeuds de confiance du noeud client
    *
    * @return la liste des noeuds de confiance du noeud client
    */
   public List<NoeudConfiance> getListeNoeudsConfiance() {
     return listeNoeudsConfiance;
   }
 
   /**
    * Affecte à l'attribut listeFichiers la liste passée en paramètre
    *
    * @param listeNoeudsConfiance
    *        la nouvelle valeur de l'attribut listeFichiers
    */
   public void setListeNoeudsConfiance(List<NoeudConfiance> listeNoeudsConfiance) {
     this.listeNoeudsConfiance = listeNoeudsConfiance;
   }
 
   /**
    * Renvoie la liste des noms des fichiers du noeud serveur
    * 
    * @return la liste des noms des fichiers du noeud serveur
    */
   @Override
   public List<String> getListeNomsFichiers() {
     List<String> listeNomsFichiers = new ArrayList<String>();
 
     for (Fichier fichier : listeFichiers) {
       listeNomsFichiers.add(fichier.getNom());
     }
     return listeNomsFichiers;
   }
 
   /****************************   Autres méthodes   ***************************/
 
   
   /****************************   Méthodes client   ***************************/
 
   /**
    * Permet d'affecter un degré de confidentialité à un fichier. Ce degré a pour but
    * de déterminer les noeuds de confiance sur lesquels le fichier en question peut
    * être dupliqué.
    *
    * Il existe 4 degrés de confidentialité :
    * <ul>
    * <li> 1 : Peu confidentiel</li>
    * <li> 2 : Moyennent confidentiel</li>
    * <li> 3 : Très confidentiel</li>
    * <li> 4 : Non duplicable (affecté par défaut lors de la création d'un objet Fichier)</li>
    * </ul>
    * @param nomFichier
    *        Le nom du fichier auquel on souhaite affecter un degré de confidentialité
    *
    * @param niveauConfidentialite
    *        Le degré de confidentialité que l'ou souhaite affecter au fichier
    */
   public void assignerConfidentialite(String nomFichier, int niveauConfidentialite) {
 
     for (Fichier fichier : listeFichiers) {
       if (fichier.getNom().equals(nomFichier)) {
         fichier.setNiveauConfidentialite(niveauConfidentialite);
         break;
       }
     }
   }
 
   /**
    * Ajoute un noeud de confiance à la liste des noeuds de confiance.
    * Pour cela, on cherche l'interface de l'objet distant du noeud désiré et on
    * l'ajoute à l'attribut duplication du nouvel objet de type NoeudConfiance
    *
    * @param adresse
    *        L'adresse du noeud de confiance que l'on souhaite ajouter
    */
   public boolean ajouterNoeudConfiance(String adresse) throws NotBoundException, MalformedURLException, ConnectException, RemoteException {
     boolean succes = true;
     for (NoeudConfiance noeudConfiance : listeNoeudsConfiance) {
       if (noeudConfiance.getAdresse().equals(adresse)) {
         succes = false;
         break;
       }
     }
     if (succes) {
       Registry registry = LocateRegistry.getRegistry(adresse);
       Duplication duplication = (Duplication) registry.lookup("Noeud");
       NoeudConfiance noeudConfiance = new NoeudConfiance(adresse, duplication);
       listeNoeudsConfiance.add(noeudConfiance);
     }
 
     return succes;
   }
 
   /**
    * Cette méthode se charge d'affecter un degré de confiance à une machine distante, aussi appelée
    * noeud de confiance. Ce degré de confiance a pour but de déterminer quels fichier peuvent
    * être dupliqués sur ce noeud.
    *
    * Il existe 3 degrés de confiance :
    * <ul>
    * <li> 1 : Peu sûr </li>
    * <li> 2 : Moyennent sûr </li>
    * <li> 3 : Très sûr </li>
    * </ul>
    * @param adresse
    *        L'adresse du noeud auquel on souhaite affecter un degré de confiance
    *
    * @param niveauConfiance
    *        Le degré de confiance que l'on souhaite affecter au noeud
    */
   public void assignerConfiance(String adresse, int niveauConfiance) {
 
     for (NoeudConfiance noeudConfiance : listeNoeudsConfiance) {
       if (noeudConfiance.getAdresse().equals(adresse)) {
         noeudConfiance.setNiveauConfiance(niveauConfiance);
         break;
       }
     }
   }
 
   /**
    * Cette méthode est le traitement principal du projet, il permet la duplication
    * de fichiers sur des noeuds de confiance. Cette duplication se peut s'effectuer que
    * sous certaines conditions:
    *
    * <ul>
    * <li> les degrés de confidentialité des fichiers à dupliquer doivent être compatibles
    *   avec les degrés de confiance des noeuds </li>
    *
    * <li> un noeud de confiance peuvent être soumis à un traitement particulier pour
    *   certains fichiers. Dans ce cas précis, son degré de confiance n'est pas
    *   pris en compte pour la d�termination du droit de duplication pour ces fichiers
    *   en question. </li>
    *
    * <li> si le degré de confidentialité du fichier est 4 (non duplicable), il ne pourra
    *   de toute façon pas être dupliqué, il n'y aura donc pas de traitements à effectuer
    *   pour ceux-ci </li>
    * </ul>
    */
   public void dupliquerFichiers() {
     for (Fichier fichier : listeFichiers) {
       if (fichier.getNiveauConfidentialite() != 4) {
         for (NoeudConfiance noeudConfiance : listeNoeudsConfiance) {
           {
             FileInputStream fis = null;
             try {
               int offset;
               int nbOctetsLus;
               int tailleMax = 50000000;
               byte[] donnees = new byte[tailleMax];
               int cpt = 0;
               boolean debutFichier;
               if (fichier.getNoeudsConfianceMap().containsKey(noeudConfiance.getAdresse())) {
                 if (fichier.getNoeudsConfianceMap().get(noeudConfiance.getAdresse())) {
                   Duplication duplication = noeudConfiance.getDuplication();
                     File file = new File(fichier.getNom());
                     fis = new FileInputStream(file);
                     try {
                       fis = new FileInputStream(file);
                       offset = 0;
                       while ((nbOctetsLus = fis.read(donnees, tailleMax * cpt, tailleMax)) > 0) {
                         if (cpt == 0) {
                           debutFichier = true;
                         }
                         else {
                           debutFichier = false;
                         }
                         duplication.ecrireFichier(adresse, donnees, fichier.getNom(), offset, nbOctetsLus, debutFichier);
                         offset += nbOctetsLus;
                         cpt++;
                         if (nbOctetsLus < tailleMax) {
                           break;
                         }
                       }
                     }
                     catch (FileNotFoundException fnfe) {
                       fnfe.printStackTrace();
                     }
                     catch (IOException ioe) {
                       ioe.printStackTrace();
                     }
                 }
               }
               else {
                 if (noeudConfiance.getNiveauConfiance() >= fichier.getNiveauConfidentialite()) {
                   Duplication duplication = noeudConfiance.getDuplication();
                     File file = new File(fichier.getNom());
                     fis = new FileInputStream(file);
                     try {
                       fis = new FileInputStream(file);
                       offset = 0;
                       while ((nbOctetsLus = fis.read(donnees, tailleMax * cpt, tailleMax)) > 0) {
                         if (cpt == 0) {
                           debutFichier = true;
                         }
                         else {
                           debutFichier = false;
                         }
                         duplication.ecrireFichier(adresse, donnees, fichier.getNom(), offset, nbOctetsLus, debutFichier);
                         offset += nbOctetsLus;
                         cpt++;
                         if (nbOctetsLus < tailleMax) {
                           break;
                         }
                       }
                     }
                     catch (FileNotFoundException fnfe) {
                       fnfe.printStackTrace();
                     }
                     catch (IOException ioe) {
                       ioe.printStackTrace();
                     }
                 }
               }
             }
             catch (FileNotFoundException ex) {
               Logger.getLogger(Noeud.class.getName()).log(Level.SEVERE, null, ex);
             }
             finally {
               try {
                 if (fis != null) {
                   fis.close();
                 }
               }
               catch (IOException ex) {
                 Logger.getLogger(Noeud.class.getName()).log(Level.SEVERE, null, ex);
               }
             }
           }
         }
       }
     }
   }
 
   /**
    * Permet de supprimer un noeud de confiance de la liste des noeuds de confiance.
    * La suppression d'un noeud de confiance n'entraîne pas la suppression des fichiers
    * dupliqués sur celui-ci.
    *
    * @param adresse
    *        L'adresse du noeud de confiance à supprimer
    */
   public void supprimerNoeudConfiance(String adresse) {
 
     for (NoeudConfiance noeudConfiance : listeNoeudsConfiance) {
       if (noeudConfiance.getAdresse().equals(adresse)) {
         listeNoeudsConfiance.remove(noeudConfiance);
         for (Fichier fichier : listeFichiers) {
           fichier.getNoeudsConfianceMap().remove(noeudConfiance.getAdresse());
         }
         break;
       }
     }
   }
 
   /**
    * L'objectif de cette méthode est d'ajouter un noeud à la liste des noeuds
    * de confiance spécifique pour un fichier.
    *
    * @param nomFichier
    *        Le nom du fichier pour lequel on souhaite ajouter le noeud de confiance
    * @param adresse
    *        L'adresse du noeude de confiance à ajouter à la liste du fichier
    * @param mode
    *        Accepter la duplication du fichier sur le noeud de confiance
    *
    */
   public void ajouterNoeudConfianceFichier(String nomFichier, String adresse, boolean mode) {
 
     for (Fichier fichier : listeFichiers) {
       if (fichier.getNom().equals(nomFichier)) {
         fichier.getNoeudsConfianceMap().put(adresse, mode);
         break;
       }
     }
   }
 
   /**
    * L'objectif de cette méthode est de supprimer un noeud de la liste des noeuds
    * de confiance spécifique pour un fichier.
    * 
    * @param nomFichier
    *        Le nom du fichier pour lequel on souhaite supprimer le noeud de confiance
    * @param adresse
    *        L'adresse du noeude de confiance à supprimer de la liste du fichier
    */
   public void supprimerNoeudConfianceFichier(String nomFichier, String adresse) {
 
     for (Fichier fichier : listeFichiers) {
       if (fichier.getNom().equals(nomFichier)) {
        fichier.getNoeudsConfianceMap().put(adresse, false);
         break;
       }
     }
   }
 
   /**
    * Permet de supprimer sur les noeuds de confiance les fichiers dupliqués par le client
    * courant et qui ne figurent plus sur sa propre machine.
    *
    * <p>Pour savoir si les fichiers du noeud de confiance sont des fichiers dupliqués par le
    * noeud client, il suffit de vérifier que le nom du fichier commence par l'adresse de
    * celui-ci.</p>
    *
    * <p>Exemple : 10.54.65.81_fic.txt (si l'adresse du noeud client est 10.54.65.81)</p>
    */
   public void nettoyerFichiersDupliques() {
 
     boolean trouve;
     for (NoeudConfiance noeudConfiance : listeNoeudsConfiance) {
 
       try {
         List<String> listeFichiersServeur = noeudConfiance.getDuplication().getListeNomsFichiers();
 
         for (String nomFichier : listeFichiersServeur) {
           trouve = false;
           if (nomFichier.length() > adresse.length()
                   && nomFichier.substring(0, adresse.length()).equals(adresse)) {
 
             for (Fichier fichier : listeFichiers) {
               if (fichier.getNom().equals(nomFichier.substring(adresse.length() + 1, nomFichier.length()))) {
                 trouve = true;
                 break;
               }
             }
             if (!trouve) {
               noeudConfiance.getDuplication().supprimerFichier(nomFichier);
             }
           }
         }
       }
       catch (RemoteException re) {
         re.printStackTrace();
       }
     }
   }
 
   /**
    * Permet de récupérer les fichiers dupliqués sur les noeuds de confiance par le client
    * et qui ne figurent plus sur sa propre machine.
    *
    * <p>Pour savoir si les fichiers du noeud de confiance sont des fichiers dupliqués par le
    * noeud client, le principe est le même que pour la méthode nettoyerFichiersDupliques()</p>
    *
    * <p>Exemple : 10.54.65.81_fic.txt (si l'adresse du noeud client est 10.54.65.81)</p>
    *
    * @return la liste des noms des fichiers ayant étés récupérés
    */
   public List<String> recupererFichiersPerdus() {
     List<String> listeNomsFichiers = new ArrayList<String>();
 
     boolean trouve = false;
     for (NoeudConfiance noeudConfiance : listeNoeudsConfiance) {
 
       try {
         List<String> listeFichiersServeur = noeudConfiance.getDuplication().getListeNomsFichiers();
 
         for (String nomFichier : listeFichiersServeur) {
           trouve = false;
           if (nomFichier.length() > adresse.length()
                   && nomFichier.substring(0, adresse.length()).equals(adresse)) {
             String nouveauNomFichier = "";
             for (Fichier fichier : listeFichiers) {
               nouveauNomFichier = nomFichier.substring(adresse.length() + 1, nomFichier.length());
               if (fichier.getNom().equals(nouveauNomFichier)) {
                 trouve = true;
                 break;
               }
             }
 
             if (!trouve) {
               byte[] donnees = noeudConfiance.getDuplication().extraireDonnees(nomFichier);
               ecrireFichierPerdu(nouveauNomFichier, donnees);
               listeNomsFichiers.add(nouveauNomFichier);
               Fichier nouveauFichier = new Fichier(nouveauNomFichier);
               listeFichiers.add(nouveauFichier);
             }
           }
         }
       }
       catch (RemoteException re) {
         re.printStackTrace();
       }
     }
 
     return listeNomsFichiers;
   }
 
   /**
    * Permet d'écrire sur le client un fichier à partir des données récupérées d'un
    * noeud de confiance.
    *
    * @param nomFichier
    *        Le nom du fichier à créer sur la machine cliente
    *
    * @param donnees
    *        Les données à écrire côté client
    */
   public void ecrireFichierPerdu(String nomFichier, byte[] donnees) {
 
     String ligne;
     File file = new File(nomFichier);
     //On supprime le fichier s'il existe déjà pour l'écrire ensuite (cela évite d'écrire plusieurs fois les même données dans le fichier)
     file.delete();
 
     try {
       file.createNewFile();
       FileOutputStream fos = new FileOutputStream(file);
       fos.write(donnees);
       fos.flush();
       fos.close();
     }
     catch (IOException ioe) {
       ioe.printStackTrace();
     }
   }
 
   /****************************   Méthodes serveur  ***************************/
 
   /**
    * Permet d'écrire un fichier sur le noeud de confiance.
    * Cette méthode écrit le paramètre données dans un fichier
    * sur le serveur et est nommé avec le paramètre nom prefixé du paramètre adresse
    * à partir du nom du fichier en question.
    *
    * @param  adresse
    *         L'adresse de provenance du fichier
    *
    * @param  données
    *         Les données du fichier à dupliquer
    *
    * @param  nomFichier
    *         Le nom du fichier dont on souhaite écrire le contenu
    *
    * @param offset
    *        La position à partir de laquelle il faut ecrire les données dans le fichier
    *
    * @param nbOctetsLus
    *        Le nombre d'octets ayant été préalablement lus
    *
    * @param debutFichier
    *        Indique s'il s'agit du début du fichier (on créé le fichier en sortie s'il s'agit du début)
    *
    *
    */
   @Override
   public void ecrireFichier(String adresse, byte[] donnees, String nomFichier, int offset, int nbOctetsLus, boolean debutFichier) {
     boolean FichierCreer = false;
     File nouveauFichier = new File(adresse + "_" + nomFichier);
     nouveauFichier.delete();
     try {
       if (debutFichier) {
         FichierCreer = nouveauFichier.createNewFile();
       }
 
       FileWriter fw = new FileWriter(nouveauFichier, true);
 
       FileOutputStream fos = new FileOutputStream(nouveauFichier);
 
       byte[] donnes1 = new byte[nbOctetsLus];
 
       for (int i = 0; i < donnes1.length; i++) {
         donnes1[i] = donnees[i];
       }
       fos.write(donnes1, offset, donnes1.length);
       fos.close();
       fw.close();
       List<String> listeNomsFichiers = new ArrayList<String>();
       if (!listeNomsFichiers.contains(adresse + "_" + nomFichier));
       listeNomsFichiers.add(adresse + "_" + nomFichier);
     }
     catch (IOException ioe) {
       ioe.printStackTrace();
     }
   }
 
   /**
    * Permet de supprimer un fichier dont le nom est passé en paramètre.
    * Cette méthode sera appelée lors du nettoyage des fichiers supprimés.
    *
    * @param nomFichier
    *        Le nom du fichier à supprimer
    */
   @Override
   public void supprimerFichier(String nomFichier) {
     File file = new File(nomFichier);
     file.delete();
     for (Fichier fichier : listeFichiers) {
       if (fichier.getNom().equals(nomFichier)) {
         listeFichiers.remove(fichier);
         break;
       }
     }
   }
 
   /**
    * Permet d'extraire les données d'un fichier contenu sur le serveur
    * à partir du nom du fichier en question.
    * Cette méthode sera utile pour la fonctionalité de récupération de fichier
    * perdu sur le client.
    *
    *
    * @param  nomFichier
    *         Le nom du fichier dont on souhaite extraire  le contenu
    *
    * @return Les données extraites du fichier dont le nom est passé
    *         en paramètre
    */
   @Override
   public byte[] extraireDonnees(String nomFichier) {
     int nbOctetsLus, tailleMax = 50000000;
     byte[] donneesMax = new byte[tailleMax];
     byte[] donneesRetournees = new byte[0];
     
     FileInputStream fis = null;
     try {
       fis = new FileInputStream(nomFichier);
 
       while ((nbOctetsLus = fis.read(donneesMax)) > 0) {
         if(nbOctetsLus != -1)
           donneesRetournees = new byte[nbOctetsLus];
         if (nbOctetsLus < tailleMax) {
           break;
         } 
       }
       
       for (int i = 0; i < donneesRetournees.length - 1; i++) {
         donneesRetournees[i] = donneesMax[i];
       }
     }
     catch (FileNotFoundException ex) {
       Logger.getLogger(Noeud.class.getName()).log(Level.SEVERE, null, ex);
     }
     catch (IOException ex) {
       Logger.getLogger(Noeud.class.getName()).log(Level.SEVERE, null, ex);
     }
     finally {
       try {
         fis.close();
       }
       catch (IOException ex) {
         Logger.getLogger(Noeud.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
 
     return donneesRetournees;
   }
 }
