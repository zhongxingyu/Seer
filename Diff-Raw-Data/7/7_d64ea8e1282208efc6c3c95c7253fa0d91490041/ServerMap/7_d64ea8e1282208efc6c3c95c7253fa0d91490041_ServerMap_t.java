 package javachallenge.server;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 import javachallenge.common.BlockType;
 import javachallenge.common.Map;
 import javachallenge.common.Point;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class ServerMap extends Map {
 	private static final long serialVersionUID = 1028425069786631534L;
 	
 	private transient Agent[][] agents;
 	private transient ArrayList<Flag> flags;
 	
 	private void init() {
 		agents = new Agent[getWid()][getHei()];
 		//------------------------------------
 		flags = new ArrayList<Flag>() ;
 		for(int i = 0 ; i < this.getFlagLocations().size() ; i++){
 			flags.add(new Flag(this.getFlagLocations().get(i), i)) ;
 		}
 	}
 	
 	public Flag getFlag(int id){
 		return flags.get(id) ;
 	}
 	
 	public ServerMap(int wid, int hei, int teamCount,
 			ArrayList<Point> spawnLocations, ArrayList<Point> flagLocations) {
 		super(wid, hei, teamCount, spawnLocations, flagLocations);
 		// Put your code in init not constructor
 		init();
 	}
 	
 	public Agent getAgent(Point p){
 		if (!isInsideMap(p))
 			return null ;
 		return agents[p.x][p.y];
 	}
 	
 	public void spawnAgent(Agent a){
 		Point p = a.getLocation() ;
 		agents[p.x][p.y] = a ;
 	}
 	
 	public void moveAgent(Agent a, Point p, Point p2){
 		agents[p.x][p.y] = null ;
 		agents[p2.x][p2.y] = a ;
 	}
 	
 	private static ServerMap sampleMap;
 	
 	static {
 		ArrayList<Point> spawnLoc = new ArrayList<Point>();
 		spawnLoc.add(new Point(2, 2));
 		ArrayList<Point> flagLoc = new ArrayList<Point>();
 		flagLoc.add(new Point(5, 1));
 		flagLoc.add(new Point(4, 4));
 		sampleMap = new ServerMap(18, 10, 1, spawnLoc, flagLoc);
 		sampleMap.setBlockType(new Point(8, 8), BlockType.GROUND);
 		sampleMap.setBlockType(new Point(8, 9), BlockType.GROUND);
 		sampleMap.setBlockType(new Point(9, 9), BlockType.RIVER);
 		sampleMap.setBlockType(new Point(9, 8), BlockType.RIVER);
 		sampleMap.setBlockType(new Point(9, 7), BlockType.RIVER);
 	}
 	
 	public void save(String filename) throws IOException {
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		String json = gson.toJson(this);
 		
 		OutputStreamWriter out = null;
 		try {
 			out = new OutputStreamWriter(new FileOutputStream(filename));
 			out.write(json);
 		} finally {
			if (out != null)
				out.close();
 		}
 	}
 	
 	public static ServerMap load(String filename) throws IOException {
 		BufferedReader f = null;
 		StringBuilder sb = new StringBuilder();
 		try {
 			f = new BufferedReader(new FileReader(filename));
 			String line = f.readLine();
 			while (line != null) {
 				sb.append(line);
 				sb.append("\n");
 				line = f.readLine();
 			}
 		} finally {
			if (f != null)
				f.close();
 		}
 		
 		String json = sb.toString();
 		Gson gson = new Gson();
 		ServerMap obj = gson.fromJson(json, ServerMap.class);
 		obj.init();
 		System.out.println(obj.agents);
 		
 		return obj;
 	}
 
 	public boolean hasFlag(Point dest) {
 		return this.getFlagLocations().contains(dest) ;
 	}
 
 	public Flag getFlag(Point dest) {
 		for (Flag flag : flags) {
 			if (flag.getLocation().x == dest.x && flag.getLocation().y == dest.y )
 				return flag ;
 		}
 		return null ;
 	}
 
 	public ArrayList<Flag> getFlags() {
 		return flags;
 	}
 }
