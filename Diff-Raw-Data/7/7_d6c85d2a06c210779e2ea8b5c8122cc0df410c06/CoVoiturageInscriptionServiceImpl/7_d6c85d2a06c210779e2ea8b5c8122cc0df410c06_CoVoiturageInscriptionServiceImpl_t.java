 package iaws.covoiturage.services.impl;
 
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Session;

 import iaws.covoiturage.services.CoVoiturageInscriptionService;
 
 /**
  * @author Axel robert
  */
 public class CoVoiturageInscriptionServiceImpl implements CoVoiturageInscriptionService{
 
 	public String inscrirePersonnel(String nom, String prenom, String mail,
 			String adresse) {
 		return addPersonnel(nom, prenom, mail, adresse);
 	}
 
 	private String addPersonnel(String nom, String prenom, String mail,
 			String adresse) {
		/*Session s = new Session("localhost",5984);
		Database db = s.getDatabase("covoituragedb");*/
		
 		return null;
 	}
 
 }
