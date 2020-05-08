 package org.esgi.java.grabbergui.view.gui.lang;
 
 import java.util.HashMap;
 
 /**
  * This class is used to translate key String to a human
  * readable word.
  * For use this class, you don't need to instanciate it,
  * because all ots methods are static. But you must call
  * the INIT method, else the keys will not be translate.
  * 
  * @author Sebastien Manicon : SManicon@free.fr
  */
 public class TR {
 	private static HashMap<String, String> _translate;
 	
 	/**
 	 * This method must be called before try translate
 	 * key. it initialize all the key you will use in
 	 * the project.
 	 */
 	public static void INIT()
 	{
 		TR._translate = new HashMap<String, String>();
 		
 		// Translate all components of the MainView.
 		TR._translate.put("$MV_MENU_FILE"  , "Fichier");
 		TR._translate.put("$MV_MENU_NEW"   , "Nouveau Projet");
 		TR._translate.put("$MV_MENU_IMPORT", "Importer");
 		TR._translate.put("$MV_MENU_LEAVE" , "Quitter");
 		
 		// Translate all components of the ControlerPanelView
 		TR._translate.put("$CV_DISPLAY_ALL" , "Afficher tout");
 		TR._translate.put("$CV_DISPLAY_RUN" , "Afficher en cour");
		TR._translate.put("$CV_DISPLAY_STOP", "Afficher en pause");
		TR._translate.put("$CV_DISPLAY_END" , "Afficher terminé");
 		
 		// Translate all components of the ProjectsPanelView
 		TR._translate.put("$PV_TAB_NAME"  , "Nom du projet");
 		TR._translate.put("$PV_TAB_STATUS", "Status");
 		TR._translate.put("$PV_TAB_FILES" , "Fichiers téléchargés");
 		TR._translate.put("$PV_TAB_SIZE" , "téléchargés");
 		TR._translate.put("$PV_TAB_PROGRESS" , "progression");
 		
 		// Translate all components of the StatusPanelView
 		TR._translate.put("$SV_INFO_NAME"  , "Nom du projet : ");
 		TR._translate.put("$SV_INFO_URL"   , "URL du projet : ");
 		TR._translate.put("$SV_INFO_PATH"  , "Chemin du projet : ");
 		TR._translate.put("$SV_INFO_STATUS", "Status du projet : ");
 		TR._translate.put("$SV_TAB_URL"    , "URL aspiré");
 		TR._translate.put("$SV_TAB_NAME"   , "Nom du fichier");
 		TR._translate.put("$SV_TAB_PBAR"   , "Progression");
 		TR._translate.put("$SV_TAB_SIZE"   , "Taille du fichier");
 		
 		// Translate all components of the CreateProjectDialog
 		TR._translate.put("$CP_NAME"      , "Creer un nouveau Projet");
 		TR._translate.put("$CP_BTN_SUBMIT", "Créer");
 		TR._translate.put("$CP_BTN_CANCEL", "Annuler");
 		TR._translate.put("$CP_LBL_NAME"  , "Nom du projet :");
 		TR._translate.put("$CP_LBL_URL"   , "URL à aspirer :");
 		TR._translate.put("$CP_LBL_PATH"  , "Répertoire :");
 		TR._translate.put("$CP_RBT_FLIMIT"  , "Nombre de fichiers");
 		TR._translate.put("$CP_RBT_SLIMIT"  , "Taille téléchargé en Mo");
 		TR._translate.put("$CP_RBT_NOLIMIT"  , "Pas de Limitte");
 		TR._translate.put("$CP_LBL_CHOOSE_LIMIT", "fixer une limitte de Téléchargements :");
 		TR._translate.put("$CP_LBL_LIMITS", "limitte :");
 		
 		TR._translate.put("$PG_STATE_PAUSE", "en pause");
 		TR._translate.put("$PG_STATE_END", "terminé");
 		TR._translate.put("$PG_STATE_RUN", "en cours");
 		TR._translate.put("$PG_STATE_STOP", "stoppé");
 		
 		TR._translate.put("$PG_METHOD_NOLIMIT", " illimité");
 		/*
 		TR._translate.put("", "");
 		TR._translate.put("", "");
 		TR._translate.put("", "");
 		TR._translate.put("", "");
 		TR._translate.put("", "");
 		*/
 	}
 	
 	/**
 	 * This method translate key to the human readable
 	 * language. use ({@link TR#INIT()} before use this
 	 * method.<br>
 	 * If the INIT() wasn't called before, the key will 
 	 * be fired.<br>
 	 * If the key is unknown, "unknown $key" will be
 	 * fired.<br>
 	 * 
 	 * @param key : the key String
 	 * 
 	 * @return the translation
 	 * 
 	 * @see TR#INIT()
 	 */
 	public static String toString(String key)
 	{
 		if (TR._translate == null)
 			return key;
 		
 		if (! TR._translate.containsKey(key))
 			return "unknown " + key;
 		
 		return TR._translate.get(key);
 	}
 }
