 package edu.wheaton.simulator.statistics;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 
 public class Loader {
 
 	private Grid grid; 
 
 	/**
 	 * Map of all PrototypeSnapshots for the simulation
 	 * Since PrototypeSnapshots are immutable, this collection is the same for each step
 	 */
 	private Set<Prototype> prototypes; 
 
 	public Loader(){
 		this.prototypes = new HashSet<Prototype>(); 
 	}
 
 	/**
 	 * Load the contents of a file
 	 * Code based on http://stackoverflow.com/questions/15906640/
 	 * @param fileName The name of the file to load
 	 */
 	public void load(String fileName){
 		File file = new File(fileName);
 		BufferedReader reader = null;
 		
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			String readLine = reader.readLine();
 			
 			while (readLine != null) {
 				
 				
 				if(readLine.equals("AgentSnapshot")){ 				
 					//Find the appropriate prototype
 					Prototype parent = getPrototype(reader.readLine());  
 					
 					//Create the Agent
 					Agent agent = new Agent(grid, parent);
 					
 					//Get the Agent's position on the Grid
 					int xpos = Integer.parseInt(reader.readLine()); 
 					int ypos = Integer.parseInt(reader.readLine()); 
 					
 					//Add the agent's default fields
 					readLine = reader.readLine(); 
 					while(readLine.substring(0,  13).equals("FieldSnapshot")){
 						String[] tokens = readLine.split(" ");
 						try {
 							agent.addField(tokens[1], tokens[2]);
 						} catch (ElementAlreadyContainedException e) {
 							System.out.println("Field already exists"); 
 							e.printStackTrace();
 						}
 					}
 
 					grid.addAgent(agent, xpos, ypos);  					
 				}
 				else if(readLine.equals("PrototypeSnapshot")){
 					//Parse the required prototype data
 					String name = reader.readLine(); 
 					Color color = new Color(Integer.parseInt(reader.readLine()));
 					byte[] design = createByteArray(reader.readLine());
 					
 					//Create the prototype
 					Prototype proto = new Prototype(grid, color, design, name);
 					
 					//Add the prototype's default fields
 					readLine = reader.readLine(); 
 					while(readLine.substring(0,  13).equals("FieldSnapshot")){
 						String[] tokens = readLine.split(" ");
 						try {
 							proto.addField(tokens[1], tokens[2]);
 						} catch (ElementAlreadyContainedException e) {
 							System.out.println("Field already exists"); 
 							e.printStackTrace();
 						}
 					}
 					prototypes.add(proto); 
 				}
 				else if(readLine.equals("Globals")){
 					
 				}
 				else{
 					reader.readLine(); 
 				}
 			}
 		}
 		catch (FileNotFoundException e) {
 			throw new RuntimeException("Could not find file: " + file.getAbsolutePath(), e);
 		}
 		catch (IOException e) {
 			throw new RuntimeException("Could not read file: " + file.getAbsolutePath(), e);
 		} finally {
 			try {
 				reader.close();
 			}
 			catch (IOException e) {
 				throw new RuntimeException("Could not close stream", e);
 			}
 		}
 	}
 	
 	/**
 	 * Create a byte array from a string
 	 * @param s String representing a byte array in the form "010111000"
 	 * @return The create byte array
 	 */
	private byte[] createByteArray(String s){
 		byte[] ret = new byte[s.length()]; 
 		
 		for(int i = 0; i < s.length(); i++)
 			ret[i] = (byte) s.charAt(i); 
 		
 		return ret; 
 	}
 	
 	/**
 	 * Get the Prototype in this class's internal list with the supplied name
 	 * @param name The name of the prototype to retrieve
 	 * @return The prototype with the supplied name
 	 */
 	private Prototype getPrototype(String name){
 		Prototype ret = null; 
 		
 		for(Prototype p : prototypes)
 			if(p.getName().equals(name))
 				ret = p; 
 		
 		if(ret == null) 
 			System.out.println("Parent Not Found");
 		
 		return ret; 
 	}
 
 
 }
