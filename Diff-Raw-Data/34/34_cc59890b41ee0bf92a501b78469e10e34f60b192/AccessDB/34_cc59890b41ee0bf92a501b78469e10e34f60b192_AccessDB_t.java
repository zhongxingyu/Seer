 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageAccess;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
 import java.util.ArrayList;
 import javax.naming.Context;
 import javax.naming.InitialContext;
import javax.naming.NamingException;
 import javax.sql.DataSource;
 import packageException.ConnexionException;
 import packageException.InscriptionException;
 import packageException.ListAlbumException;
 import packageModel.Album;
 import packageModel.Utilisateur;
 
 /**
  * @author BAVAY Emilien - THILLAYE Joachim
  */
 public class AccessDB {
     private Connection connexion;
     
     public Utilisateur connexion(String login, String pass) throws ConnexionException 
     {
         Utilisateur user = new Utilisateur();
         user.setMail("");
         
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource)ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();        
             
             String requeteSQL = "Select password from Utilisateur where mail = ?";
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setString(1, login);
             ResultSet donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 if (donnees.getString(1).compareTo(pass) != 0)
                     throw new ConnexionException("incorrectPass");
                 else
                     user.setMail(login);  
             }    
             
             connexion.close();
         }
         catch (SQLException e)
         {
             throw new ConnexionException("connexionException");
         }
         catch (Exception e) 
         {
             throw new ConnexionException("sqlException");
         }
                     
         return user;
     }
     
     public ArrayList<Album> getLastAlbums() throws ListAlbumException
     {
         ArrayList<Album> arrayAlbum = new ArrayList<Album>();
         
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
 
             String requeteSQL = "SELECT Album.idAlbum, Album.titre, Album.prix, Album.image, Artiste.nom "
                     + "FROM Album, Artiste_Album, Artiste "
                     + "WHERE Artiste_Album.idAlbum = Album.idAlbum AND Artiste_Album.idArtiste = Artiste.idArtiste";
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setMaxRows(10);
             ResultSet donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 Album album = new Album();
                 album.setId(donnees.getInt(1));
                 album.setTitre(donnees.getString(2));
                 album.setPrix(donnees.getDouble(3));
                 album.setImage(donnees.getString(4));
                 album.setArtiste(donnees.getString(5));
                 arrayAlbum.add(album);
             }
             
             if (arrayAlbum.isEmpty() == true) // Envoi erreur si aucune album
             {    
                 throw new ListAlbumException("emptyListAlbum");
             }            
             connexion.close();
         }
         catch (SQLException e)
         {
             throw new ListAlbumException("listAlbumException");
         }
         catch (Exception e) 
         {
             throw new ListAlbumException("sqlException");
         }
         
         return arrayAlbum;
     }
 
     public Album getAlbum(int idAlbum) throws ListAlbumException
     {
         Album album = new Album();
         
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
 
             String requeteSQL = "SELECT Album.idAlbum, Album.titre, Album.prix, Album.image, Artiste.nom, Label.Nom, Label.Image "
                     + "FROM Album, Artiste_Album, Artiste, Label "
                     + "WHERE Album.idAlbum = " + idAlbum
                         + " AND Artiste_Album.idAlbum = Album.idAlbum AND Artiste_Album.idArtiste = Artiste.idArtiste "
                         + " AND Album.idLabel = Label.idLabel ";
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setMaxRows(10);
             ResultSet donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 album.setId(donnees.getInt(1));
                 album.setTitre(donnees.getString(2));
                 album.setPrix(donnees.getDouble(3));
                 album.setImage(donnees.getString(4));
                 album.setArtiste(donnees.getString(5));
                 album.setLabel(donnees.getString(6));
                 album.setLabelImg(donnees.getString(7));
             }
             
             if (album == null) // Envoi erreur si aucune album
             {    
                 throw new ListAlbumException("albumNotExist");
             }            
             connexion.close();
         }
         catch (SQLException e)
         {
             throw new ListAlbumException("getAlbumException");
         }
         catch (Exception e) 
         {
             throw new ListAlbumException("sqlException");
         }
         
         return album;
     }
     
     public void ajoutUtilisateur(Utilisateur util) throws InscriptionException
     {
         try
         {
             Context cont = new InitialContext();
             DataSource source = (DataSource)cont.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
             String requeteSQL = "INSERT INTO UTILISATEUR" + 
                                 "(NOM, PRENOM, ADR_RUE, ADR_NUMERO, ADR_BOITE, ADR_CODEPOSTAL, ADR_LOCALITE,MAIL,MOTDEPASSE,NUMTEL)"
                                 + "VALUES(?,?,?,?,?,?,?,?,?,?)";
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setString(1, util.getNom());
             prepStat.setString(2,util.getPrenom());
             prepStat.setString(3,util.getRue());
             prepStat.setInt(4,util.getNumero());
             prepStat.setString(5, util.getBoite());
             prepStat.setInt(6,util.getCodepostal() );  
             prepStat.setString(7, util.getLocalite());
             prepStat.setString(8,util.getMail() );
             prepStat.setString(9, util.getPassword());
             prepStat.setString(10, "071/713862");
             
             prepStat.executeUpdate();
             
             connexion.close();
             
         }
        catch(NamingException ex)
         {
            throw new InscriptionException("errorNaming");
        }
        catch(SQLIntegrityConstraintViolationException ex)
        {
            throw new InscriptionException("errorMailUsed");
        }
        catch(SQLException ex)
        {
            throw new InscriptionException("errorSQL");
        }
     }
 }
