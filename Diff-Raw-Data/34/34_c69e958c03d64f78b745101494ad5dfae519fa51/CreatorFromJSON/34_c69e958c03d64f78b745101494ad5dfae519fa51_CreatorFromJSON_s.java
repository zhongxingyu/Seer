 package be.artesis.timelog.model;
 
 //import be.artesis.timelog.model.JSONOverdrager;
 import be.artesis.timelog.view.Gebruiker;
 import be.artesis.timelog.view.Opdrachtgever;
 import be.artesis.timelog.view.Project;
 import be.artesis.timelog.view.Taak;
 import be.artesis.timelog.view.Tijdspanne;
 import java.io.IOException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class CreatorFromJSON
 {
 
 
         public static Gebruiker createGebruiker(String sessionKey) throws JSONException, IOException {
		JSONObject Jgebruiker = JSONOverdrager.getGebruiker(sessionKey);

 		Gebruiker gebruiker = new Gebruiker(Jgebruiker.getString("naam"), Jgebruiker.getString("voornaam"), Jgebruiker.getString("gebruikersnaam"), Jgebruiker.getString("email"), Jgebruiker.getInt("ID"));
 
 		return gebruiker;
 
 	}
 
 	public static Project[] createProjecten(String sessionKey) throws JSONException, IOException {
             //een project maken vanuit de database
 		JSONObject[] Jproject = JSONOverdrager.getProjectenVanGebruiker(sessionKey);
 
 		Project[] project = new Project[Jproject.length];
 		
		//if(!Jproject[0].getString("error").equals("Geen projecten")) {
 		if(!Jproject[0].has("error")) {
 	        int i = 0;
 	        for(JSONObject Jobject: Jproject)
 	        {
 	        	project[i++] =  new Project(Jobject.getString("naam"), Jobject.getInt("opdrachtgevers_ID"), Jobject.getLong("start_datum"),Jobject.getLong("eind_datum"), Jobject.getInt("ID"));
 	    	}
 		}

 		return project;
 	}
 
 
 
 	public static Opdrachtgever[] createOpdrachtgevers(String sessionKey) throws JSONException, IOException {
 		JSONObject[] Jopdrachtgever = JSONOverdrager.getOpdrachtgeverVanGebruiker(sessionKey);
 
 		Opdrachtgever[] opdrachtgever = new Opdrachtgever[Jopdrachtgever.length];
 		
 		if(!Jopdrachtgever[0].has("error")) {
 	        int i = 0;
 	        for (JSONObject Jobject : Jopdrachtgever){
 	                opdrachtgever[i++] = new Opdrachtgever(Jobject.getString("naam"),Jobject.getString("voornaam"),Jobject.getString("bedrijfsnaam"),Jobject.getString("email"),Jobject.getString("telefoonnummer"),Jobject.getInt("ID"));
 	        }
 		}
         return opdrachtgever;
 	}
 
 	public static Taak[] createTaken(String sessionKey,int projectID) throws JSONException, IOException {
 		JSONObject Jtaken[] = JSONOverdrager.getTakenVanProject(sessionKey,projectID);
 
 		Taak[] taken = new Taak[Jtaken.length];
 		int i = 0;
 		for (JSONObject Jobject : Jtaken){
 			taken[i++] = new Taak(Jobject.getString("naam"), Jobject.getLong("begin_tijd"), Jobject.getLong("verwacht_eind"), Jobject.getString("commentaar"),Jobject.getInt("ID"));
 		}
 
 
 		return taken;
 
 	}
 
 	public static Tijdspanne[] createTijdspannes(String sessionKey, int taakID, boolean pauze) throws JSONException, IOException {
 		JSONObject[] Jtijdspannes = JSONOverdrager.getTijdspannesVanTaak(sessionKey, taakID, pauze);
 
 		Tijdspanne[] tijdspannes = new Tijdspanne[Jtijdspannes.length];
 
         int i = 0;
         for (JSONObject Jobject : Jtijdspannes){
             tijdspannes[i++] = new Tijdspanne(Jobject.getLong("begin_uur"),Jobject.getLong("eind_uur"),Jobject.getInt("ID"),Jobject.getBoolean("pauze"));
         }
 
 		return tijdspannes;
 	}
 
 	/*public static Tijdspanne createPauze(String sessionKey){
 		JSONObject Jtijdspanne = JSONOverdrager.getGeregistreerdePauze(sessionKey);
 
 		Tijdspanne tijdspanne = null;
 
 		try {
 			tijdspanne = new Tijdspanne(Jtijdspanne.getLong("begintijd"),Jtijdspanne.getLong("eindtijd"),Jtijdspanne.getInt("id"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return tijdspanne;
 	}
 
          public static Project createProject(String sessionKey,Opdrachtgever opdrachtgever){
             //een project object maken met en lokaal opdrachtgever object
 		JSONObject Jproject = JSONOverdrager.getProject(sessionKey,);
 
 		Project project = null;
 
 		//Project project =  new Project(Jproject.getString("naam"),Jproject.getString("startdatum"),Jproject.getString("einddatum"))
 		try {
 			project =  new Project(Jproject.getString("naam"),opdrachtgever,Jproject.getLong("startdatum"),Jproject.getLong("einddatum"),Jproject.getInt("id"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return project;
 	}
         */
 
 }
