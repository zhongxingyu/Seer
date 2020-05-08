 package userManagement;
 
 import java.util.Vector;
 
 import javax.servlet.http.HttpSession;
 
 import user.User;
 
 import logger.Log;
 
 /**
  * Verwaltet alle eingelogten User.
  */
 public abstract class LoggedInUsers {
 
 	/**
 	 * Statische Variable für den Logger
 	 */
 	private static Log log = Log.getInstance();
 
 	/**
 	 * Vector mit den eingelogten Usern des Systems.
 	 */
 	private static Vector<User> users = new Vector<User>();
 
 	/**
 	 * Gibt den Vector mit den eingeloggten Usern zurueck.
 	 * 
 	 * @return Vector mit eingeloggten Usern
 	 */
 	public static Vector<User> getUsers() {
 		return users;
 	}
 
 	/**
 	 * Fuegt einen User zum Vektor der eingeloggten User hinzu und behebt
 	 * eventuelle Konflikte. Versucht man einen User einzuloggen der bereits
 	 * eine Session im System hat, so wird die alte Session beendet.Versucht man
 	 * sich als ein User anzumelden,der bereits im System angemeldet ist, so
 	 * wird der bereits vorhandene Kandidat abgemeldet.
 	 * 
 	 * @param u
 	 *            hinzuzufuegender User
 	 * 
 	 * 
 	 */
 	static public void addUser(User u) {
 		User tempuserbyname = getUserByUsername(u.getUserData().getUsername());
 		User tempuserbysession = getUserBySession(u.getUserData().getSession());
 		if (tempuserbyname == null && tempuserbysession == null) {
 			users.add(u);
 			log.write("LoggedInUsers", u.getUserData().getUsername()
 					+ " has logged in.");
			log.write("LoggedInUsers", LoggedInUsers.getUsers().size()
					+ " user are currently logged in.");
 
 		} else if (tempuserbysession == null && tempuserbyname != null) {
			log.write("LoggedInUsers", "Killed User: "+tempuserbyname.getUserData().getUsername());
 			tempuserbyname.invalidate();
 			users.add(u);
 			log.write("LoggedInUsers", u.getUserData().getUsername()
 					+ " has logged in.");
			log.write("LoggedInUsers", LoggedInUsers.getUsers().size()
					+ " user are currently logged in.");
 
 		}
 		else if (tempuserbyname == null && tempuserbysession!=null) {
			log.write("LoggedInUsers", "Killed User: "+tempuserbysession.getUserData().getUsername());
 			removeUserBySession(u.getUserData().getSession());
 			users.add(u);
 			log.write("LoggedInUsers", u.getUserData().getUsername()
 					+ " has logged in.");
			log.write("LoggedInUsers", LoggedInUsers.getUsers().size()
					+ " user are currently logged in.");
 		}
 		else if(tempuserbyname !=null && tempuserbysession!=null){
 			if(tempuserbyname==tempuserbysession)return;
			log.write("LoggedInUsers", "Killed User: "+tempuserbysession.getUserData().getUsername());
 			removeUserBySession(u.getUserData().getSession());
			log.write("LoggedInUsers", "Killed User: "+tempuserbyname.getUserData().getUsername());
 			tempuserbyname.invalidate();
 			users.add(u);
 			log.write("LoggedInUsers", u.getUserData().getUsername()
 					+ " has logged in.");
			log.write("LoggedInUsers", LoggedInUsers.getUsers().size()
					+ " user are currently logged in.");
 		}
 
 	}
 
 	/**
 	 * Gibt ein User-Objekt mit dem übergebenen Namen zurueck.
 	 * 
 	 * @param username
 	 *            zu suchender Benutzername
 	 * @return Das gefundene User-Objekt wird zurueckgegeben, ansonsten
 	 *         <code>NULL</code>
 	 * @see User
 	 */
 	public static User getUserByUsername(String username) {
 		for (int i = 0; i < users.size(); i++)
 			if (users.get(i).getUserData().getUsername().equals(username))
 				return users.get(i);
 		return null;
 	}
 
 	/**
 	 * Gibt ein User-Objekt mit der übergebenen Session zurueck
 	 * 
 	 * @param session
 	 *            zu suchende Session
 	 * @return Das gefundene User-Objekt wird zurueckgegeben, ansonsten
 	 *         <code>Null</code>
 	 **/
 	public static User getUserBySession(HttpSession session) {
 		for (int i = 0; i < users.size(); i++) {
 			if (users.get(i).getUserData().getSession() == session) {
 				return users.get(i);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Entfernt das User-Objekt mit der ID der angegebenen Session aus der Liste
 	 * der eingeloggten User. Ist die Session unbenannt, so wird nichts
 	 * entfernt.
 	 * 
 	 * @param session
 	 *            Session des zu loeschenden <code>User</code>-Objekts
 	 * 
 	 */
 	static void removeUserBySession(HttpSession session) {
 		// userName==null keine aktion
		String caller = Thread.currentThread().getStackTrace()[2].getMethodName();
 		if (session.getAttribute("userName") == null)
 			return;
 		for (int i = 0; i < users.size(); i++) {
 			if (users.get(i).getUserData().getSession() == session) {
 				String name = users.get(i).getUserData().getUsername();
 				users.remove(i);
				if(caller.equals("sessionDestroyed")){
					log.write("LoggedInUsers", name+ " has logged out.");
					log.write("LoggedInUsers", LoggedInUsers.getUsers().size()
							+ " user are currently logged in.");
				}
 				break;
 			}
 		}
 	}
 }
