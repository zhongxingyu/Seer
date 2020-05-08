 
 import java.util.LinkedHashSet;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author jonathan
  */
 public class Playerlist extends FileLoader {
 	protected LinkedHashSet<String> players;
 	
 	public Playerlist() {
 		this.filename = "whitelist.txt";
 		players = new LinkedHashSet<String>();
 	}
 
 	public boolean addPlayer(String player) {
		return players.add(player.toLowerCase());
 	}
 
 	public boolean removePlayer(String player) {
		return players.remove(player.toLowerCase());
 	}
 
 	public boolean isWhitelisted(String player) {
		return players.contains(player.toLowerCase());
 	}
 
 	@Override
 	protected void beforeLoad() {
 		players.clear();
 	}
 
 	@Override
 	protected void loadLine(String line) {
 		this.addPlayer(line);
 	}
 
 	@Override
 	protected String saveString() {
 		String line="";
 		for( String p: players) {
 			line+=p;
 			line+="\r\n";
 		}
 		return line;
 	}
 
 }
