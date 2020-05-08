 package Storage;
 
 import java.io.*;
 import java.util.*;
 import java.lang.String;
 
 /**
  * The Storage class reads and writes to the database file
  *  to store player information
  *  
  *  realName is an optional attribute and has default value "noName"
  *  age is an optional attribute and has default value -1
  * 
  * @author Gabriel
  *
  */
 
 
 public class Storage implements StorageInterface{
 	
 	private String playerName;
 	private String password;
 	private String realName;
 	private int age;
 	private int numChips;
 	private int numWins;
 	private int numLosses;
 	private int numGames;
	private ArrayList<Storage> database = new ArrayList();
 
 	// Empty constructor
 	Storage() {
 	}
 	
 	Storage(String playerName, String password, String realName, int age) {
 		this.playerName = playerName;
 		this.password = password;
 		this.realName = realName;
 		this.age = age;
 	}
 	
 	Storage(String playerName, String password) {
 		this(playerName, password, "noName", -1);
 	}
 	
 	Storage(String line) {
 		String[] fields;
 		fields = line.split(",");
 		this.playerName = fields[0];
 		this.password = fields[1];
 		this.realName = fields[2];
 		this.age = Integer.parseInt(fields[3]);
 		this.numChips = Integer.parseInt(fields[4]);
 		this.numWins = Integer.parseInt(fields[5]);
 		this.numLosses = Integer.parseInt(fields[6]);
 		this.numGames = numWins + numLosses;	
 	}
 	
	private void loadDatabase() throws IOException {
 		Storage storedPlayer;
 		Scanner reader = new Scanner(new File("database.csv"));
 		String line;
 		
 		while (reader.hasNextLine()) {
 			line = reader.nextLine();
 			storedPlayer = new Storage(line);
 			database.add(storedPlayer);
 		}
 		reader.close();
 	}
 	
	private void saveDatabase() throws IOException {
 		FileWriter fstream = new FileWriter("database.csv");
 		BufferedWriter writer = new BufferedWriter(fstream);
 		
 		for ( Storage player : database ) {
 			writer.write(player.playerName + ","
 					+ player.password + ","
 					+ player.realName + ","
 					+ player.age + ","
 					+ player.numChips + ","
 					+ player.numWins + ","
 					+ player.numLosses);
 			writer.write("\r\n");
 		}
 		database.clear();
 		writer.close();
 	}
 	
	public void addNewPlayer(String inputPlayerName, String inputPassword) throws IOException {
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(inputPlayerName)) {
 				saveDatabase();
 				return;
 			}
 		}
 		Storage newPlayer = new Storage(inputPlayerName, inputPassword);
 		database.add(newPlayer);
 		saveDatabase();
 	}
 	
 	public void setPassword(String inputPassword) throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.password = inputPassword;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public void changePlayerName(String newPlayerName) throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.playerName = newPlayerName;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public void setRealName(String inputRealName) throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.realName = inputRealName;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public String getRealName() throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				return player.realName;
 			}
 		}
 		return "Player Not Found";
 	}
 	
 	public void setAge(int inputAge) throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.age = inputAge;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public int getAge() throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				return player.age;
 			}
 		}
 		return -1;
 	}
 	
 	public void setNumChips(int betValue) throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.numChips += betValue;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public int getNumChips() throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				return player.numChips++;
 			}
 		}
 		return -1;
 	}
 	
 	public void setWin() throws IOException {
 		loadDatabase();		
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.numWins++;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public int getNumWins() throws IOException {
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				return player.numWins;
 			}
 		}
 		return -1;
 	}
 	
 	public void setLoss() throws IOException {
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
 				player.numLosses++;
 			}
 		}
 		saveDatabase();
 	}
 	
 	public int getNumLosses() throws IOException {
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName)) {
				return player.numLosses++;
 			}
 		}
 		return -1;
 	}
 
 	public boolean validatePassword(String inputPassword) throws IOException{
 		loadDatabase();
 		for ( Storage player : database ) {
 			if (player.playerName.equals(this.playerName) &&
 				inputPassword.equals(player.password)) {
 				return true;
 			}
 		}
 		return false;
 	}	
 }
