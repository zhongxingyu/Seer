 // 
 //
 //  @ Projet : Project Gammon
 //  @ Fichier : Session.java
 //  @ Date : 19/12/2012
 //  @ Auteurs : DONG Chuan, BONNETTO Benjamin, FRANCON Adrien, POTHELUNE Jean-Michel
 //
 //
 
 
 
 package fr.ujm.tse.info4.pgammon.models;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 public class GestionDeSession {
 	public List<Session> listSession = new ArrayList<Session>();
 	private static GestionDeSession gestionDeSession;
 
 	private GestionDeSession(){
 	
 	}
 	
 	/**
 	 * Constructeur : Singleton
 	 * @return gestionDeSession la liste de Sessions
 	 * @throws IOException
 	 * @throws JDOMException
 	 */
 	public static GestionDeSession getGestionDeSession() throws IOException, JDOMException{
 		
 		if(gestionDeSession == null){
 			gestionDeSession = new GestionDeSession();  
 			gestionDeSession.charger();
 		} 
 		return gestionDeSession; 
 	}
 	/**
 	 * sauvegarder tous les sessions dans le dossier "sauvegardeSessions" par XML
 	 */
 	public void sauvegarder(){
 		try{
 			for(int i=0;i<listSession.size();i++){
 				
 				String tmpNom = "Session"+String.valueOf(listSession.get(i).getIdSession());
 				Element racine = new Element(tmpNom);
 				Document document = new Document(racine);
 				listSession.get(i).sauvegarder(racine);
 				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
 				
 				File path = new File("sauvegardeSessions");
 				if(!path.exists()) 
 					path.mkdirs();
 				
 				String tmpPath = "sauvegardeSessions/"+tmpNom+".xml"; 
 				sortie.output(document, new FileOutputStream(tmpPath));
 				
 				
 				
 			}
 	    }catch(Exception e){
 				System.out.println("Erreur d'enregistrement");
 			}
 	}
 	/**
 	 * Charger les fichiers XML dans le dossier "sauvegardeSessions"
 	 * @throws JDOMException
 	 * @throws IOException
 	 */
 	public void charger() throws JDOMException, IOException{
 		
		File files[]; 
 		File path = new File("sauvegardeSessions");
 		files = path.listFiles();
		System.out.println(path);
 		if(files == null)
 			return;
 		Arrays.sort(files);
 		
 		SAXBuilder builder = new SAXBuilder();
 		
 		for (int i = 0; i < files.length; i++) {
 			
 			String tmpPath = files[i].toString();
 			Document document = builder.build(tmpPath);
 			Element racine = document.getRootElement();
 			listSession.add(new Session());
 			// si le chargement c'est mal passer on supprime la session
 			if (!listSession.get(i).charger(racine))
 				supprimerSession(listSession.get(i).getIdSession());
 
 		}
 	}	 
 	/**
 	 * Getter
 	 * @return la liste de Sessions
 	 */
 	public List<Session> getListSession() {
 		return listSession;
 	}
 	/**
 	 * Mettre la valeur de listSession
 	 * @param listSession une liste contenant tous les sessions 
 	 */
 	public void setListSession(List<Session> listSession) {
 		this.listSession = listSession;
 	}
 	/**
 	 * supprimer une session dans la liste par ID de cette session
 	 * @param idSession ID pour chaque session
 	 */
 	public void supprimerSession(int idSession){
 		
 		supprimerFichierSession(idSession);
 		for(int i=0;i<listSession.size();i++){
 			if(listSession.get(i).getIdSession()==idSession){
 				listSession.remove(i);
 			}
 			else
 				return;
 		}
 	}
 	/**
 	 * supprimer le fichier de Session XML par ID de cette session 
 	 * @param idSession ID pour chaque session
 	 */
 	public void supprimerFichierSession(int idSession){
 		File files[]; 
 		File path = new File("sauvegardeSessions");
 		files = path.listFiles();
 		
 		if(files == null)
 			return;
 
 		for (int i = 0; i < files.length; i++) {
 			String tmpPath = files[i].toString();
 			if(tmpPath.equals("sauvegardeSessions\\Session"+String.valueOf(idSession)+".xml")){
 				files[i].delete();
 			}
 		
 		}
 	}
 
 }
