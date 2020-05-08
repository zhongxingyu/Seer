 package org.jarvisland.level.level0;
 
 import org.jarvisland.InventoryManager;
 import org.jarvisland.LevelManager;
 import org.jarvisland.levels.room.AbstractRoom;
 import org.jarvisland.levels.room.Room;
 import org.jarvisland.levels.room.RoomNotAccessibleException;
 
 
 /**
  * La pièce tutoriel du niveau 0
  * 
  * Il faut ouvrir un coffre, prendre la clé, utiliser la clé
  * sur la porte et sortir de la pièce. 
  * 
  * @author niclupien
  *
  */
 public class TutorialRoom extends AbstractRoom {
 	boolean coffreOuvert = false;
 	boolean isPorteOuverte = false;
 	
 	public String execute(String s) {
 		if (s.matches("OUVRIR.* COFFRE")) {
 			if (coffreOuvert)
 				return "Le coffre est déjà ouvert.";
 			
 			coffreOuvert = true;
 			return "Le coffre s'ouvre et un épais nuage de poussière en sort. Il y a une clé dans le fond.";
		} else if (s.matches("PRENDRE.* CLÉ")) {
 			if (coffreOuvert && !InventoryManager.getInstance().hasItem("Clé")) {
 				InventoryManager.getInstance().addItem("Clé");
 				return "Vous ramassez une clé.";
 			}
		} else if (s.matches("UTILISER.* CLÉ.* PORTE")) {
 			if (InventoryManager.getInstance().hasItem("Clé")) {
 				isPorteOuverte = true;
 				InventoryManager.getInstance().removeItem("Clé");
 				return "La porte est maintenant ouverte.";
 			}
 		}
 		
 		return null;
 	}
 
 	public String look() {
 		return "Vous vous trouvez dans une petite pièce sombre.\n" +
 				"Vous n'avez aucune idée où vous êtes mais vous voyez\n" +
 				"un coffre dans un coin à côté d'ossements humains.";
 	}
 	
 
 	public Room north() throws RoomNotAccessibleException {
 		if (!isPorteOuverte)
 			throw new RoomNotAccessibleException("Vous voyez une porte métallique.\n" +
 					"Elle est verrouillée et il n'y a aucun moyen de la défoncer.");
 		LevelManager.getInstance().notifyCurrentLevel("outOfFirstRoomEvent");
 		return null;
 	}
 	
 	public Room south() throws RoomNotAccessibleException {
 		throw new RoomNotAccessibleException("Vous voyez un mur de pierre couvert de taches de sang.");
 	}
 	
 	public Room east() throws RoomNotAccessibleException {
 		throw new RoomNotAccessibleException("Vous voyez un mur de pierre.");
 	}
 	
 	public Room west() throws RoomNotAccessibleException {
 		throw new RoomNotAccessibleException("Vous voyez un mur de pierre.");
 	}
 }
