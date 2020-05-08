 package main;
 
 import io.printer.*;
 import io.reader.CvsReader;
 import io.reader.Parser;
 import io.reader.Parser.FileIdentifier;
 import io.reader.ParserException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import members.Competitor;
 import members.Sorter;
 
 /**
  * @author tf08vo5
  * 
  */
 public class ResultCompilerMain {
 
 	private static final String RACETYPE = "racetype";
 	public static final String STARTTIMES = "start";
 	public static final String NAMEFILE = "namn";
 	public static final String FINISHTIMES = "slut";
 	public static final String RESULTFILE = "resultat";
 	public static final String SORTRESULTFILE = "sortresultat";
 	public static final String EXTENSION = ".txt";
 	private final static String LAP_RACE = "laprace";
 	private final static String STANDARD = "standard";
 	private final static String BINARY_LAPS = "etapprace";
 	public final static String YES = "yes";
 	public final static String NO = "no";
 	public static final String NUMBER_BINARY = "antalstationer";
 
 	/**
 	 * 
 	 * Read input files and create list with competitors, and call printResults
 	 * to print the results to the output file.
 	 * 
 	 * @throws URISyntaxException
 	 * @throws FileNotFoundException
 	 * 
 	 */
 	public static void main(String[] args) throws URISyntaxException {
 		Properties prop = new Properties();
 
 		try {
 			// load properties from config.properties
 			prop.load(new FileInputStream("config.properties"));
 
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 
 		// Detects the location of the executable. Keep as comment if needed
 		// later.
 		//
 		// CodeSource codeSource =
 		// ResultCompilerMain.class.getProtectionDomain()
 		// .getCodeSource();
 		// File jarFile = new File(codeSource.getLocation().toURI().getPath());
 		// String jarDir = jarFile.getParentFile().getPath();
 		//
 
 		try {
 			List<FileHeader> inputFiles = getInputFiles(prop);
 			Map<Integer, Competitor> map = null;
 			map = parseInputFiles(inputFiles, prop);
 
 			ArrayList<Competitor> list = new ArrayList<Competitor>(map.values());
 			printResults(prop, list);
 		} catch (FileNotFoundException e) {
 			errorMessage(e.getMessage());
 			System.exit(-1);
 		} catch (ParserException e) {
 			errorMessage(e.getMessage());
 			System.exit(-1);
 		} catch (IOException e) {
 			errorMessage(e.getMessage());
 			System.exit(-1);
 		}
 	}
 
 	/**
 	 * Reads the filenames of the files with names or times in the config-file
 	 * and puts them in a list
 	 * 
 	 * @param prop
 	 *            contains the values in the config file
 	 * @return HashMap with filenames as keys and type of file as value
 	 * @throws IOException
 	 */
 	private static List<FileHeader> getInputFiles(Properties prop)
 			throws IOException {
 
 		List<FileHeader> inputFiles = new ArrayList<FileHeader>();
 
 		addInputFile(prop, "namefiles", inputFiles,
 				Parser.FileIdentifier.name_file, Competitor.NO_STATION);
 		
 		
 		if (prop.containsKey(NUMBER_BINARY)
 				&& prop.getProperty(RACETYPE).equalsIgnoreCase(BINARY_LAPS)) {
 			addBinaryLapInfo(prop, inputFiles);
 
 		} else {
 			addDefaultInfo(prop, inputFiles);
 		}
 
 		return inputFiles;
 	}
 
 	/**
 	 * Adds the filenames to the list
 	 * 
 	 * @param prop
 	 *            contains the values in the config file property the filename
 	 *            we are currently adding inputFiles the list of file and
 	 *            headers fileIdentity type of the file
 	 * @return ArrayList with files and headers
 	 * @throws IOException
 	 *             if the config property is missing
 	 */
 	private static void addInputFile(Properties prop, String property,
 			List<FileHeader> inputFiles, FileIdentifier fileIdentity,
 			int stationNr) throws IOException {
 
 		String startPath = "";
 		if (prop.containsKey(property)) {
 			startPath = prop.getProperty(property);
 		} else {
 			throw new IOException("Property " + property + " not found.");
 		}
 		String[] startFiles = startPath.split(" ");
 		for (String s : startFiles) {
 			if (!new File(s).exists())
 				throw new FileNotFoundException("Filepath: " + s);
 
 			inputFiles.add(new FileHeader(s, stationNr, fileIdentity));
 		}
 	}
 
 	private static void addBinaryLapInfo(Properties prop,
 			List<FileHeader> inputFiles) throws IOException {
 		try {
 			int e = 1;
 			e = Integer.parseInt(prop.getProperty(NUMBER_BINARY));
 			for (int i = 1; i <= e; i++) {
 				addInputFile(prop, "startfiles" + "_" + i, inputFiles,
 						Parser.FileIdentifier.start_file, i);
 				addInputFile(prop, "finishfiles" + "_" + i, inputFiles,
 						Parser.FileIdentifier.finish_file, i);
 			}
 		} catch (NumberFormatException e1) {
 		}
 	}
 
 	private static void addDefaultInfo(Properties prop,
 			List<FileHeader> inputFiles) throws IOException {
 		addInputFile(prop, "startfiles", inputFiles,
 				Parser.FileIdentifier.start_file, Competitor.NO_STATION);
 		addInputFile(prop, "finishfiles", inputFiles,
 				Parser.FileIdentifier.finish_file, Competitor.NO_STATION);
 	}
 
 	/**
 	 * Parses the content of each file in the list of inputfiles to the list of
 	 * competitors.
 	 * 
 	 * @param inputFiles
 	 *            list of inputfiles nad their headers
 	 * @param prop
 	 * @return a hashmap with the competitors information
 	 * @throws FileNotFoundException
 	 * @throws ParserException
 	 */
 	private static Map<Integer, Competitor> parseInputFiles(
 			List<FileHeader> inputFiles, Properties prop)
 			throws FileNotFoundException, ParserException {
 
 		Map<Integer, Competitor> map = new HashMap<Integer, Competitor>();
 
 		Parser p = new Parser();
 		for (FileHeader header : inputFiles) {
 			p.setStationNr(header.station);
 			map = p.parse(read(header.file), map, header.id);
 
 		}
 		return map;
 	}
 
 	/**
 	 * Reads the content of the file
 	 * 
 	 * @param file
 	 * @return an arraylist of strings with all the lines in file
 	 * @throws FileNotFoundException
 	 */
 	private static ArrayList<ArrayList<String>> read(String file)
 			throws FileNotFoundException {
 		CvsReader reader = new CvsReader(file);
 
 		return reader.readAll();
 	}
 
 	/**
 	 * Return a printer. Which type depends on the status of 'racetype' in the
 	 * config file. If 'racetype' status does not exist in config file, a
 	 * StdPrinter is returned. Else if 'racetype' status is set to 'laprace', a
 	 * LapPrinter is returned.
 	 * 
 	 * @param prop
 	 *            properties to get status in config file
 	 * @return a printer, which type depends on the status in the config file
 	 */
 	private static Printer getPrinter(Properties prop) {
 		Printer printer = null;
 
 		String printerType = prop.getProperty(RACETYPE);
 
 		if (printerType.equals(STANDARD)) {
 			printer = new StdPrinter();
 		} else if (printerType.equals(LAP_RACE)) {
 			printer = new LapPrinter();
 		} else if (printerType.equals(BINARY_LAPS)) {
 			printer = new BinaryLapPrinter();
 		}
 		return printer;
 	}
 
 	/**
 	 * Return a Sortprinter. Which type depends on the status of 'racetype' in
 	 * the config file. If 'racetype' status does not exist in config file, a
 	 * SortStdPrinter is returned. Else if 'racetype' status is set to
 	 * 'laprace', a SortLapPrinter is returned.
 	 * 
 	 * @param prop
 	 *            properties to get status in config file
 	 * @return a Sortprinter, which type depends on the status in the config
 	 *         file
 	 */
 	private static Printer getSortPrinter(Properties prop) {
 		Printer printer = null;
 
 		String printerType = prop.getProperty(RACETYPE);
 
 		if (printerType.equals(STANDARD)) {
 			printer = new SortStdPrinter();
 		} else if (printerType.equals(LAP_RACE)) {
 			printer = new SortLapPrinter();
 		} else if (printerType.equals(BINARY_LAPS)) {
 			printer = new SortBinaryLapPrinter();
 		}
 		return printer;
 	}
 
 	/**
 	 * If an error is caught during the parsing, a message is shown. See
 	 * ParserException class for information of what errors or
 	 * FileNotFoundException.
 	 * 
 	 * @param e
 	 *            exception
 	 */
 	private static void errorMessage(String e) {
 		JFrame frame = new JFrame();
 		JOptionPane.showMessageDialog(frame, e, "FEL",
 				JOptionPane.ERROR_MESSAGE);
 		frame.dispose();
 	}
 
 	/**
 	 * Prints the result to the file specified in the config file under
 	 * 'resultfile'. If 'sorted' in config file is set to 'yes' is also prints a
 	 * sorted result to the file specified in 'sortedresultfile' in the config
 	 * file.
 	 * 
 	 * @param prop
 	 *            contains the values in the config file
 	 * @param competitors
 	 *            the list of competitors to print
 	 */
 	private static void printResults(Properties prop,
 			ArrayList<Competitor> competitors) {
 		String filepath = prop.getProperty("resultfile");
 		Printer printer = getPrinter(prop);
 		Sorter sorter = new Sorter();
 		sorter.sortList(false, competitors, "");
 		printer.printResults(competitors, filepath);
 		
 		print(prop, "sorted", "sortedresultfile", competitors, sorter,
 				new NullConverter());
 		print(prop, "html", "htmlresultfile", competitors, sorter,
 				new HtmlConverter());
 	}
 
 	private static void print(Properties prop, String printType,
 			String resultfile, ArrayList<Competitor> competitors,
 			Sorter sorter, Converter conv) {
 		Printer printer;
 		if (prop.containsKey(printType) && prop.get(printType).equals(YES)
 				&& prop.containsKey(resultfile)) {
 			String resultfilepath = prop.getProperty(resultfile);
 			printer = getSortPrinter(prop);
 			sorter.sortList(true, competitors, prop.getProperty("racetype"));
 			printer.printResults(competitors, resultfilepath, conv);
 		}
 	}
 
 	/**
 	 * @author Henrik A class for holding several variables for use in this
 	 *         class.
 	 */
 	private static class FileHeader {
 		@Override
 		public String toString() {
 			return "FileHeader [file=" + file + ", id=" + id + ", station="
 					+ station + "]";
 		}
 
 		public FileHeader(String filePath, int stationNr,
 				FileIdentifier fileIdentity) {
 			id = fileIdentity;
 			station = stationNr;
 			file = filePath;
 		}
 
 		String file;
 		FileIdentifier id;
 		int station;
 	}
 
 }
