 package main;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.TreeMap;
 import race.Race;
 import race.StageRace;
 import race.LapRace;
 
 
 /**
  * The main class which initiates the Sorter program.
  * 
  * @author Team08
  * 
  */
 public class Enduro {
 	protected TreeMap<Integer, Driver> register;
 	private Race race;
 	private String start = "defaultStart";
 	private String stop = "defaultStop";
 	private String name = "defaultName";
 	private String result = "defaultResult";
 	private String sorted = "defaultSorted";
 	private String raceTime = "";
 	private String raceType = "";
 	private int laps = 0;
 	private String startType = "";
 	private String attributeString = "";
 	private ArrayList<String> driverAttributes = new ArrayList<String>();
 	private String specialDistances;
 	private int factor;
 	private GenerateConfig genCon;
 
 	/**
 	 * The constructor which creates an Enduro object. Enter a config file in
 	 * order to make the sorter function as wanted. E.g. if there has been a lap
 	 * race or a stage race
 	 * 
 	 * @param config
 	 *            file which the user creates before starting the sorter program
 	 *            in order for it to produce the wanted result
 	 */
 	public Enduro(String[] args) {
 
 		if (args.length == 0) {
 			Properties configFile = new Properties();
 			genCon = new GenerateConfig(configFile);
 			try {
 				configFile.load(new FileInputStream("config.properties"));
 				if(genCon.checkKey()){
 					readConfigKeys(configFile);
 					createRace();
 				}else{
 					System.err.println("Misslyckades med att lÃ€sa konfigurationsfilen");
 				}
 
 			} catch (FileNotFoundException e1) {
 				System.err.println("Misslyckades med att lÃ€sa konfigurationsfilen, en ny har autogenerats.");
 				System.err.println("Var god och fyll i config.properties filen och starta om programmet.");
 				genCon.autogenerateConfig();
 				System.exit(1);
 			} catch (IOException e1) {				
 				e1.printStackTrace();
 			}
 
 		} else {
 			try {
 				readKeysfromArgs(args);
 				if (raceType.equals("varv")) {
 					raceTime = args[6];
 				}
 				createRace();
 			} catch (Exception e) {
 				System.err.println("Error: Du måste skriva in alla argument");
 
 			}}
 	}
 
 
 	private void readKeysfromArgs(String[] args) {
 		start = args[0];
 		stop = args[1];
 		name = args[2];
 		result = args[3];
 		sorted = args[9];
 		raceType = args[4];
 		startType = args[5];
 		startType = startType.toLowerCase();
 		raceType = raceType.toLowerCase();
 		laps = Integer.parseInt(args[7]);
 		attributeString = args[8];
 		String[] attributeArray = args[8].split("; ");
 
 		for (int i = 0; i < attributeArray.length; i++) {
 			driverAttributes.add(attributeArray[i]);
 		}
 		
 		specialDistances = args[9]; 
 		factor = Integer.parseInt(args[10]);
 	}
 
 
 	private void readConfigKeys(Properties configFile) {
 		start = configFile.getProperty("STARTFILE").trim();
 		stop = configFile.getProperty("STOPFILE").trim();
 		name = configFile.getProperty("NAMEFILE").trim();
 		result = configFile.getProperty("RESULTFILE").trim();
 		sorted = configFile.getProperty("SORTEDFILE").trim();
 		raceTime = configFile.getProperty("RACETIME").trim();
		raceType = configFile.getProperty("RACETYPE").trim();	
		startType = configFile.getProperty("STARTTYPE").trim();				
 		laps = Integer.parseInt(configFile.getProperty("LAPS").trim());	
 		attributeString = configFile.getProperty("DRIVER_ATTRIBUTES").trim();
    if(raceType.equals("etapp")){	
 		specialDistances = configFile.getProperty("SPECIAL_DISTANCES").trim();
 		factor = Integer.parseInt(configFile.getProperty("FACTOR").trim());
    }
 		String[] attributeArray = attributeString.split("; ");
 		for (int i = 0; i < attributeArray.length; i++) {
 		driverAttributes.add(attributeArray[i]);
 		}
 		
 	}
 	
 	
 	private void createRace(){
 		if (raceType.equals("varv")) {
 			race = new LapRace(start, stop, name, result, sorted, raceTime, laps, startType, driverAttributes);
 		}else if (raceType.equals("etapp")){
 			race = new StageRace(start, stop, name, result, laps, startType, driverAttributes, specialDistances, factor);
 		}
 		race.computeTotalTime();
 		System.out.println("Programmet har genererat: " + result);
 		if(!sorted.equals("")){
 		System.out.println("Programmet har genererat: " + sorted);
 		}
 	}
 	
 
 	/**
 	 * Main program that initiates the Sorter program.
 	 * 
 	 * @param the
 	 *            config file
 	 */
 
 	public static void main(String[] args) {
 		new Enduro(args);
 	}
 }
