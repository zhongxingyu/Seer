 package tazzernator.cjc.timeshift;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.bukkit.World;
 
 public class TimeShiftFileReaderWriter {
 
 	public static ArrayList<String> readLines(String filename) throws IOException {
 		// Method to read our numbers in the startup file
 		TimeShift.data.clear();
 		FileReader fileReader = new FileReader(filename);
 		BufferedReader bufferedReader = new BufferedReader(fileReader);
 		String line = null;
 		while ((line = bufferedReader.readLine()) != null) {
			TimeShift.data.add(line);
 		}
 		bufferedReader.close();
 		//returns a list of lines, each line contains settings for a world.
 		return TimeShift.data;
 	}
 
 	private static void initializeFile() {
 		//writes out a dummy file. 
 		FileWriter fstream;
 		try {
 			fstream = new FileWriter(TimeShift.path);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("world=-1");
 			out.close();
 			// input it
 			readSettings();
 		} catch (IOException f) {
 			System.out.println("Could not create file for " + TimeShift.name);
 		}
 	}
 	// method to take those numbers, parse them, and input them into memory
 	public static void readSettings() {
 		try {
 			try {
 				//get list of world settings
 				readLines(TimeShift.path);
 			} catch (IOException e) {
 				// create a file if unreadable
 				initializeFile();
 				try {
 					//try to list settings again
 					readLines(TimeShift.path);
 				} catch (Exception p) {
 					//still errored, blah
 					System.out.println(TimeShift.name + " had a ton of trouble with its settings file.");
 					p.printStackTrace();
 				}
 			}
 			// iterate through strings, splitting at =
 
 			for (String d : TimeShift.data) {
 				//string length is at least l=0
 				if (d.length() >= 3) {
 					String[] sets = d.split("=");
 					int setting = Integer.parseInt(sets[1]); //setting on right
 					String world = sets[0];//world name on left
 					if (sets.length == 2) { // if there were two keys
 						try {
 							//add the setting to our table
 							TimeShift.settings.put(world, setting);
 						} catch (Exception e) {
 							//error putting
 							System.out.println("Error parsing " + TimeShift.name + "'s settings file.");
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			//error parsing
 			initializeFile();
 			System.out.println("There was a problem parsing " + TimeShift.name + "'s data. World startup states have been reset.");
 		}
 	}
 
 	// build and write string to file for persistent settings.
 	public static void persistentWriter(int setting, World w) {
 		String output = "";
 		
 		//read in file
 		//modify correct setting
 		//output to file
 		try {
 			readLines(TimeShift.path);
 		} catch (Exception e) {
 		}
 
 		Boolean isSet = false;
 		for (String d : TimeShift.data) {
 			String[] sets = d.split("=");
 
 			if (sets.length == 2) {
 				if (sets[0].equals(w.getName())) {
 					//world in file, modify setting
 					isSet = true;
 					output = output + sets[0] + "=" + setting + "\n";
 				} else {
 					//not the world, add back to output
 					output = output + sets[0] + "=" + sets[1] + "\n";
 				}
 			}
 		}
 		//if world wasn't already in the file, and therefore modified above, add it.
 		if (!isSet) {
 			output = output + w.getName() + "=" + setting + "\n";
 		}
 		
 		//write out the output.
 		FileWriter fstream;
 		try {
 			fstream = new FileWriter(TimeShift.path);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(output);
 			out.close();
 		} catch (IOException e) {
 		}
 	}
 }
