 package iaws.covoiturage.services.impl;
 
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
 		return null;
 	}
 
 }
