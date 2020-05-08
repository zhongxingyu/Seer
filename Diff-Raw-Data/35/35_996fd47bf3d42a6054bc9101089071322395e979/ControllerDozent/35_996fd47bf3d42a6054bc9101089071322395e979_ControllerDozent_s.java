 ﻿package control;
 
 import gui.ModulEditCreate;
 import gui.ModulReview;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import objects.Modul;
 import objects.ModulKu;
 import objects.Nachricht;
 
 
 
 public class ControllerDozent extends Controller{
 
 	//öffnet Datensatz zum Bearbeiten in neuem Fenster
	public void ändernModul(int modul){
 		if (modul == 0){
 		Modul neu = new Modul(modulDatabase.getNewId(),  null, 0, null,
 				 null, 0, null, userid , 
 				 null, null, null,
 				 null, null,null, null,
 				 null, null, null, null);
 		
 		ModulEditCreate b = new ModulEditCreate(neu);		
 		}else {
 			
 		 Modul test = modulDatabase.loadModule(modul);			
 		 if(modulDatabase.getSperr(test.getid())== true){
			 System.out.println("Modul bereits geänder warten auf bestätigung");
 			 /* NOCH FEHLT POP FENSTER*/
 			 
 		 } else {
 
 			 ModulEditCreate gg = new ModulEditCreate(test);
 		 }
 		}
 		
 	}
 	
 	//gibt Änderungsantrag aus
 	public void ausgebenModulList(int userid){
 		LinkedList<ModulKu> list = modulDatabase.loadModuleList(userid);
 		if (blarghs.getRangStell(userid)){
 			LinkedList<ModulKu> stellList = modulDatabase.loadModuleList(blarghs.getStellID(userid));
 			for(int i=0;i<stellList.size();i++)list.add(stellList.get(i));
 		}	
 		
 		ModulReview a = new ModulReview(list);
 		
 	}
 	
 	/*public void showMessages(LoginApplication aa){
 		//Annahme: Messaging ist GUI-Klasse
 		Messaging m = new Messaging(aa);
 	}*/
 	
 	//Methode zum Speichern des Moduls im Puffer
 	public void speichernModul(Modul modul){
 		modulPufferData.writeBufferModule(modul);
 	    modulDatabase.setSperr(modul.getid(), true);
 
 		Nachricht test = new Nachricht(nachrichtenData.getNewId(),"", "",modul.getresponsibleid(), modul.getid());
 		nachrichtenData.newNachricht(test);
 		//nachrichtenData.newBenachrichtigung(modul);
 	}
 }
