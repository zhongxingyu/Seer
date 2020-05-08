 /**
  * AuthPlayer: AuthInfo.java
  * @author Alex Riebs
  * Created Jul 8, 2011
  */
 package archangel.authplayer;
 
 import java.io.Serializable;
 import java.net.InetAddress;
 
 import org.bukkit.entity.Player;
 
 /**
  * @author Alex "Arcalyth" Riebs
  * License: Don't steal my shit. Give credit if you modify and/or redistribute any of my code.
  * Otherwise, you're free to do whatever else you want with it.
  * 
  * Also, the Bukkit license applies.
  */
 public class AuthInfo implements Serializable {
 	private static final long serialVersionUID = 6082897226190340978L;
 	
 	private String password;
 	private InetAddress ip;
 	private boolean registered;
 	
 	public AuthInfo(Player player, String password) {
 		this.password = password;
 		this.ip = player.getAddress().getAddress();
 		
 		registered = false;
 	}
 	
 	public InetAddress getAddress() {
 		return ip;
 	}
 	
 	public boolean authenticate(String password) {
 		registered = true;
 		return password.equals(this.password);
 	}
 
 	public boolean isRegistered() {
 		return registered;
 	}
 }
