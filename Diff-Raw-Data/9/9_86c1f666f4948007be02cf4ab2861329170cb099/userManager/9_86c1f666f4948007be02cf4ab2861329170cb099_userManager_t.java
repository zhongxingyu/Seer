 package server.cooproject.itk.hu;
 
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 import org.jwebsocket.api.WebSocketConnector;
 import org.jwebsocket.token.Token;
 
 
 public class userManager {
 
 	private HashMap<String, WebSocketConnector> _users;
 	private static Logger log = Logger.getLogger(userManager.class.getName());
 	private coopMessageListener _coopMessageListener;
 	public userManager(coopMessageListener parent) {
 		this._coopMessageListener = parent;
 		this._users = new HashMap<String, WebSocketConnector>();
 	}
 
 	/**
 	 * Megnezi, hogy a user benne van-e a usermanagerben. Ha nincs hozzaadja
 	 * (login=false -al!) Ha benne van, de eltero username-el, akkor usernevet
 	 * valt!
 	 * 
 	 * @param c
 	 *            A connector
 	 * @param username
 	 *            A csatlakozo user neve
 	 */
 	private void checkUser(WebSocketConnector c, String username) {
 		/* Ha nincs benne */
 		if (!_users.containsKey(c.getSession().getSessionId())) {
 			c.setUsername(username);
 			c.setBoolean("login", false);// meg nem loginolt!
 			_users.put(c.getSession().getSessionId(), c);
 			log.info("New record in _users map : "
 					+ c.getSession().getSessionId() + " - " + c.getUsername());
 
 			// Ha benne van, es nevet valtott
 		} else {
 			if (_users.get(c.getSession().getSessionId()).getUsername() != username) {
 				log.info("_users map updated with "
 						+ c.getSession().getSessionId() + " - " + username);
 				c.setUsername(username);
 			}
 		}
 
 	}
 
 	public String getUsername(WebSocketConnector c) {
 		//TODO:RemoveMe
 		if(c.getUsername() != null){
 			return c.getUsername();
 		}else{
 			return c.getSession().getSessionId();
 		}
 	}
 
 	/**
 	 * Login esemeny kezelese
 	 * 
 	 * @param c
 	 *            A logint kuldo connectorja
 	 * @param username
 	 *            A usernev
 	 */
 	public void handleLogin(WebSocketConnector c, String username) {
		if (_users.containsKey(c.getSession().getSessionId())) {
 			c.setBoolean("login", true);
 			this._coopMessageListener.userJoined(c,username);
 		}
 	}
 
 	/**
 	 * User be van loggolva?
 	 * 
 	 * @param c
 	 *            A socket connector
 	 * @param aToken
 	 *            a kuldott uzenet
 	 * @return true ha be van loggolva, false ha nem
 	 */
 	public boolean isLoggedIn(WebSocketConnector c, Token aToken) {
 		this.checkUser(c, getUsernameFromMessage(aToken));
 		if (!_users.containsKey(c.getSession().getSessionId())) {
 			return c.getBoolean("login");
 		} else {
 			return false;
 		}
 	}
 
 	private String getUsernameFromMessage(Token aToken) {
 		return aToken.getString("sender");
 	}
 	//
 
 	public void userLeft(String sessionId) {
 		if (_users.containsKey(sessionId)){
 			_users.remove(sessionId);
 		} 
 		
 	}
 
 }
