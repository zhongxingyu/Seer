 package strain.sound;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import strain.file.CsvParser;
 
 /**
  * Maps indexes to sound file names. The sound database is loaded from disk and
  * stored in a map. The map can then be used to get a sound file name for a
  * specific sound event index.
  */
 class SoundDatabase {
 	
 	/**
 	 * The name of the sound data file.
 	 */
 	public static final String DATA_FILE = "sounds.csv";
 	
 	/**
 	 * The location of the sound data file in the package structure.
 	 */
	public static final String RESOURCE_LOCATION = "/strain/resource/data/";
 	
 	/**
 	 * The map containing sound event indexes and sound file names.
 	 */
 	private HashMap<String, String> database;
 	
 	/**
 	 * The parser for the CSV data.
 	 */
 	private CsvParser parser;
 	
 	/**
 	 * Creates a new SoundDatabase. The data file is immediately loaded from
 	 * disk.
 	 */
 	public SoundDatabase() {
 		database = new HashMap<String, String>();
 		readSoundDefinitions();
 		parser.close();
 	}
 	
 	/**
 	 * Gets the sound file for an index.
 	 * 
 	 * @param index The index to get the sound file for.
 	 * 
 	 * @return The sound file for the given index.
 	 */
 	public String getSound(String index) {
 		return database.get(index);
 	}
 	
 	/**
 	 * Reads the definitions from the parser.
 	 */
 	private void readParser() throws IOException {
 		String[][] records = parser.read();
 		for (String[] r: records) {
 			String index = r[0];
 			String soundFile = r[1];
 			database.put(index, soundFile);
 		}
 	}
 	
 	/**
 	 * Loads the sound definitions into memory.
 	 */
 	private void readSoundDefinitions() {
 		String fileLocation = RESOURCE_LOCATION + DATA_FILE;
 		InputStream file = getClass().getResourceAsStream(fileLocation);
 		parser = new CsvParser(file, '\n', ',', '"');
 		try {
 			readParser();
 		} catch (IOException e) {
 			System.err.println("Sound file format error");
 		}
 	}
 	
 }
