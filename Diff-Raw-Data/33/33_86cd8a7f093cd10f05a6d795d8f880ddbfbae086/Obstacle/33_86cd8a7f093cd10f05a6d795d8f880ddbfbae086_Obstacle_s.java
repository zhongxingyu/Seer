 package chalmers.dax021308.ecosystem.model;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Obstacle Class.
  * 
  * @author Henrik
  * 
  */
 public class Obstacle implements IObstacle {
 
 	private String obstacleType;
 	private List<Pair>[] obstacles;
 
 	public Obstacle(String type, List<Pair>[] obstacles) {
 		obstacleType = type;
 		this.obstacles = obstacles;
 	}
 	
 	public Obstacle(String filename) {
 		this.obstacles = fileToObstacle(filename);
 	}
 
 	@Override
 	public boolean insideObstacle(Position p) {
 		// TODO proper calculation depending on the type of obstacleRange
 		return false;
 	}
 
 	@Override
 	public String getObstacleType() {
 		return obstacleType;
 	}
 	
 	/**
 	 * @author Sebastian
 	 * @param filePath a path to the file which to read ascii obstacle from.
 	 * @return an array containing lists with start/stop x-values for the obstacle.
 	 */
 	
 	private List<Pair>[] fileToObstacle(String filePath){
 		try{
 			FileInputStream fstream = new FileInputStream(filePath);
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line;
			//TODO: How to do here correct???
			List<Pair>[] o = new List[1000]; 
 			int yPos = 0;
 			
 			//Read line by line (every y-position)
 			while ((line = br.readLine()) != null) {
 				List<Pair> pl = new ArrayList<Pair>();
 				boolean started = false; //If first position of obstacle has been found or not.
 				int lastPos = -1;
 				int startPos = -1;
 				for(int i=0;i<line.length();i++) {
					if(line.charAt(i)!= ' ') {
 						if(!started){
 							startPos = i;
 							started = true;
						} else if(i+1<line.length() && (line.charAt(i+1)==' ')) {
 							lastPos = i;
 							pl.add(new Pair(startPos,lastPos));
 							started = false;
//							System.out.println("ASDF");
						} else if(i==line.length()-1) {
 							lastPos = i;
 							pl.add(new Pair(startPos,lastPos));
 							started = false;
							
 						}
					} 
 				}
 				o[yPos] = pl;
 				System.out.println(pl);
 				yPos++;
 			}
 			
 			in.close();
		    }catch (Exception e){//Catch exception if any
		    	System.err.println("Error: " + e.getMessage());
		    }
//		    
	    return null;
 	}
 }
