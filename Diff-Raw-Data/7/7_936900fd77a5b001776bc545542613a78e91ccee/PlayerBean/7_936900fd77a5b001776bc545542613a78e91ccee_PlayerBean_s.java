 package at.ac.tuwien.big.ewa.ue3;
 
 import java.util.Date;
 
 import at.ac.tuwien.big.easyholdem.player.Player;
 import at.ac.tuwien.big.easyholdem.player.Player.Gender;
 
 public class PlayerBean {
 
	private Player player = new Player();
 
 	public Date getDateOfBirth() {
 		return player.getDateOfBirth();
 	}
 
 	public String getFirstName() {
 		return player.getFirstName();
 	}
 
 	public Gender getGender() {
 		return player.getGender();
 	}
 
 	public long getId() {
 		return player.getId();
 	}
 
 	public String getLastName() {
 		return player.getLastName();
 	}
 
 	public String getPassword() {
 		return player.getPassword();
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getStack() {
 		return player.getStack();
 	}
 
 	public String getUserName() {
 		return player.getUserName();
 	}
 
 	public void setDateOfBirth(Date dateOfBirth) {
 		player.setDateOfBirth(dateOfBirth);
 	}
 
 	public void setFirstName(String firstName) {
 		player.setFirstName(firstName);
 	}
 
 	public void setGender(Gender gender) {
 		player.setGender(gender);
 	}
 
 	public void setId(long id) {
 		player.setId(id);
 	}
 
 	public void setLastName(String lastName) {
 		player.setLastName(lastName);
 	}
 
 	public void setPassword(String password) {
 		player.setPassword(password);
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	public void setStack(int stack) {
 		player.setStack(stack);
 	}
 
 	public void setUserName(String userName) {
 		player.setUserName(userName);
 	}
 
 }
