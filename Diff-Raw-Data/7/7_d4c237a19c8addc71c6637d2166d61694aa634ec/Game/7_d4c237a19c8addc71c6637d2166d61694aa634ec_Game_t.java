 package concubattles;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import ar.edu.unq.tpi.pconc.Channel;
 import ar.edu.unq.tpi.pconc.Utils;
 
 public class Game {
 	
 	private int count;
 	private Channel<String> input;
 	private Channel<String> output;
 	public ArrayList<Place> places; 
 	public HashMap<Place, ArrayList<Integer> > connected;
 	public ArrayList<Castle> castles;
 	
 	public int minID;
 	
 	public int getNextChannelID(){
 		int next = this.count;
 		this.increaseCount();
 		return next;
 	}
 	public void increaseCount(){
 		this.count++;
 	}
 	public void increaseID(){
 		this.minID++;
 	}
 	public int getNextID(){
 		int next = this.minID;
 		this.increaseID();
 		return next;
 	}
 	
 	
 	public Game(Channel<String> input, Channel<String> output){
 		this.count = 10;
 		this.input = input;
 		this.output = output;
 		this.places = new ArrayList<Place>();
 		this.minID = 10;
 		this.connected = new HashMap<Place, ArrayList<Integer>>();
 		this.castles= new ArrayList<Castle>();
 	}
 	
 	public void ensureID(int id){
 		while(this.minID <= id){
 			this.minID++;
 		}
 	}
 	
 	public Castle createCastle(int castleID){
		System.out.println("Castle created with Id : "+castleID);
 		this.ensureID(castleID);
 		Castle castle =  new Castle(new Channel<String>(this.getNextChannelID()), castleID);
 		this.places.add(castle);
 		this.castles.add(castle);
 		return castle;
 		
 	}
 	
 	public City createCity(int ciryID){
 		System.out.println("Ciudad creado con id: "+ciryID);
 		this.ensureID(ciryID);
 		City city = new City(new Channel<String>(this.getNextChannelID()), ciryID);
 		this.places.add(city);
 		return city;
 	
 	}
 	
 	private Way createway(int id){
 		this.ensureID(id);
 		Way way = new Way(new Channel<String>(this.getNextChannelID()), id);
 		this.places.add(way);
 		return way;
 	}
 	
 	public void addOrCreate(Place key, Place value){
 		if(!this.connected.containsKey(key)){
 			this.connected.put(key, new ArrayList<Integer>());
 		}
 		this.connected.get(key).add(key.id);
 	}
 	
 	public void connect(Place place1, Place place2){
 		Way way = this.createway(this.getNextID());
 		place1.getRoads().add(way);
 		place2.getRoads().add(way);			
 		way.getRoads().add(place1);
 		way.getRoads().add(place2);
 		this.addOrCreate(place1, place2);
 		this.addOrCreate(place2, place1);
 	}
 	
 	public void connectbyID(int id1, int id2){
 		Place place1 = this.getPlaceById(id1);
 	    Place place2 = this.getPlaceById(id2);
 		if(isConnectedTo(place1, place2)){
 			return;
 		}else{
 			this.connect(place1, place2);
 		}
 	}
 	
 	public Place getPlaceById(int id){
 		for(Place place: this.places){
 			if(place.id == id){
 				return place;
 			}
 		}
 		return null;
 	}
 	
 	public boolean isConnectedTo(Place place1, Place place2){
 		if(this.connected.containsKey(place1)){
 			return this.connected.get(place1).contains(place2.id);
 		}
 		return false;
 	}
 	
 	public void createMap(){
 		String map = this.input.receive();
 		String[] cities = map.split("\n");
 		int numCities = cities.length - 1;
 		for (int i = 2; i <= numCities; i++) {
 			this.createCity(i);
 		}
 			this.createCastle(numCities + 1);
 			this.createCastle(1);	
 		
 		for(String city: cities){
 			String[] citiesID = city.split(" ");
 			String id1 = citiesID[1];
 			for(String id: Arrays.copyOfRange(citiesID, 2, citiesID.length)){
 				this.connectbyID(Utils.parseInt(id1), Utils.parseInt(id));
 			}
 			
 		}
 		
 	}
 	
 	public void moveSilver(int i){
 		this.output.send("silver1 "+i);
 	}
 	
 	public void startGame(){
 		for(Castle c: this.castles){
 			c.createSoldier();
 		}
 	}
 	
 	public static void main(String[] args) {
 		
 		Game game = new Game(new Channel<String>(1002), new Channel<String>(1001));
 		
 		game.createMap();
 		game.startGame();
		game.output.send("silver0 1");
		game.output.send("golden0 5");
 
 	}
 
 }
