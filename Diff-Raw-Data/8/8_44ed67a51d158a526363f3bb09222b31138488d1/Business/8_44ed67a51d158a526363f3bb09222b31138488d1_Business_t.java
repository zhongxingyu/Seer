 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageBusiness;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import packageAccess.AccessDB;
 import packageException.ConnexionException;
 import packageException.InscriptionException;
 import packageException.ListAlbumException;
 import packageModel.Album;
 import packageModel.Utilisateur;
 
 /**
  *
  * @author Emilien
  */
 public class Business {
     AccessDB ac = new AccessDB();    
     
     public Utilisateur connexion(String login, String pass) throws ConnexionException
     {
         try 
         {
             MessageDigest mdEnc = MessageDigest.getInstance("MD5");
             mdEnc.update(pass.getBytes(),0,pass.length());
             String md5Pass = new BigInteger(1,mdEnc.digest()).toString(16);
             return ac.connexion(login, md5Pass);
         }
         catch(Exception e)
         {
             throw new ConnexionException(e.toString());
         }
         
     }    
     
     public void ajoutUtilisateur(String nom,String prenom,String rue,String numero,String boite,String localite,String codePostal, String email,String motDePasse,String mdpConf,String numTel) throws InscriptionException
     {
         if(nom.isEmpty() || prenom.isEmpty() || rue.isEmpty() || numero.isEmpty() || boite.isEmpty() || localite.isEmpty() || codePostal.isEmpty() || email.isEmpty() && motDePasse.isEmpty() && mdpConf.isEmpty() && numTel.isEmpty())
        {
            throw new InscriptionException("errorField");
        }
         
 
         
        if(Pattern.matches("[a-zA-Z]",prenom)==false)
         {
             throw new InscriptionException("errorFirstName");
         }
         
         if(Pattern.matches("[^a-zA-Z]",nom))
         {
             throw new InscriptionException("errorName");
         }
         
         if(Pattern.matches("[^a-zA-Z0-9]{2,50}",rue))
         {
             throw new InscriptionException("errorStreet");
         } 
         
         if(Pattern.matches("[^0-9]$", numero))
         {
             throw new InscriptionException("errorNumero");
         }
         
         if(Pattern.matches("[^a-zA-Z0-9]{0,15}",boite))
         {
             throw new InscriptionException("errorBox");
         }
         
         if(Pattern.matches("[^a-zA-Z0-9]{2,50}",localite))
         {
             throw new InscriptionException("errorLocality");
         }
         
         if(Pattern.matches("[^a-zA-Z0-9]{4,4}",codePostal))
         {            
             throw new InscriptionException("errorPostalCode");
         }
         
         if(Pattern.matches("[^0-9/+//]{8,50}",numTel))
         {
             throw new InscriptionException("errorPhone");
         }
         
         
        if (Pattern.matches("[^_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$", email))
         {
             throw new InscriptionException("errorMail");
         }
         
        if(Pattern.matches("/^([a-zA-Z0-9_\\.\\-]{6,20})+$/",motDePasse))
         {
             throw new InscriptionException("errorPw");
         }
 
         
         if(motDePasse.compareTo(mdpConf)!= 0 )
         {
             throw new InscriptionException("errorPwNotMatch");
         }
         
         
 
         // Continuer les vérifications
         try
         {
             int num = Integer.parseInt(numero);
             int cp= Integer.parseInt(codePostal);
             MessageDigest mdEnc = MessageDigest.getInstance("MD5");
             mdEnc.update(motDePasse.getBytes(),0,motDePasse.length());
             String md5 = new BigInteger(1,mdEnc.digest()).toString(16);
             Utilisateur newUtilisateur = new Utilisateur(nom,prenom,rue,num,boite,localite,cp,email,md5,numTel);
             ac.ajoutUtilisateur(newUtilisateur);
         
         }
         catch(Exception ex)
         {
             throw new InscriptionException(ex.toString());
         }
                 
        
     } 
 
     
     public ArrayList<Album> getLastAlbums() throws ListAlbumException
     {
         return ac.getLastAlbums();
     }
 
     public Album getAlbum(int idAlbum) throws ListAlbumException
     {
         return ac.getAlbum(idAlbum);
     }
     
 }
