 package eu.trentorise.opendata.ckanalyze.analyzers.resources;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.trentorise.opendata.nlprise.identifiers.date.DateIdentifier;
 import eu.trentorise.opendata.ckanalyze.exceptions.CKAnalyzeException;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * This class performs analysis of CSV files. You can use the analyze() method
  * to start the analysis
  * 
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  * @since Last modified by azanella On 17/lug/2013
  */
 public class CSVAnalyzer {
 	private static Logger logger = LoggerFactory.getLogger(CSVAnalyzer.class);
 	private static final int MAX_DATE_SIZE = 30; 
 	/**
 	 * Defines supported data types
 	 * 
 	 * @author Alberto Zanella <a.zanella@trentorise.eu> Last modified by
 	 *         azanella On 11/lug/2013
 	 */
 	public enum Datatype {
 		INT, FLOAT, DATE, STRING, GEOJSON, EMPTY;
 	}
 
 	private Map<Long, Long> stringLengthDistribution;
 	private File file;
 	private String id;
 	private double stringLengthAvg;
 	private List<Character> alternativeSeparators;
 	private int rowCount = 0;
 	private int columnCount = 0;
 	private Map<Datatype, Integer> colsPerType;
 
 	/**
 	 * 
 	 * @param filename
 	 *            -- File to be analyzed
 	 * @param id
 	 *            -- Id of the Dataset Resource
 	 */
 	public CSVAnalyzer(String filename, String id) {
 		this.id = id;
 		this.stringLengthDistribution = new HashMap<Long, Long>();
 		this.alternativeSeparators = new ArrayList<Character>();
 		alternativeSeparators.add(';');
 		alternativeSeparators.add('\t');
 		alternativeSeparators.add('|');
 		colsPerType = new HashMap<CSVAnalyzer.Datatype, Integer>();
 		this.file = new File(filename);
 		try {
 			if (!file.exists()) {
 				throw new FileNotFoundException();
 			}
 		} catch (FileNotFoundException e) {
 			logger.error("CSVAnalyzer :: Initialization error : File not Found");
 		}
 	}
 
 	/**
 	 * Performs the analysis of the resource
 	 */
 	public void analyze() throws CKAnalyzeException {
 		CSVReader reader = null;
 		try {
 			// Read CSV file
 			reader = new CSVReader(new FileReader(file));
 			List<String[]> retval = reader.readAll();
 			ArrayList<ColumnType> colsTypes = new ArrayList<ColumnType>();
 			if ((retval.size()) <= 1) {
 				reader.close();
 				throw new CKAnalyzeException("Empty resource");
 			}
 			// Initialize colsTypes to the column dimension and tries to
 			// identify data types from column headers
 			processHeader(retval.remove(0), colsTypes);
 
 			// If only one column is identified, tries with other separators
 			while ((!moreThanOneColumn(retval))
 					&& (alternativeSeparators.isEmpty())) {
 				reader = new CSVReader(new FileReader(file),
 						alternativeSeparators.remove(0));
 				retval = reader.readAll();
 			}
 			// Extract rows and columns number (without heading line)
 			rowCount = retval.size();
 			columnCount = colsTypes.size();
 			// Analyze the file field by field
 			for (String[] row : retval) {
 				for (int i = 0; i < row.length; i++) {
 					if (i >= columnCount) {
 						logger.warn("Malformed CSV file");
 						break;
 					}
 					String field = row[i];
 					analyzeCell(field, i, colsTypes);
 				}
 			}
 			// stores what columns are discovered as String
 			HashSet<Integer> strColumnsPos = new HashSet<Integer>();
 			computeTypeOfColumn(colsTypes, strColumnsPos);
 			computeStringMetrics(retval, strColumnsPos);
 			reader.close();
 		} catch (FileNotFoundException e) {
 			throw new CKAnalyzeException("File Not Found",e);
 		} catch (IOException e) {
 			throw new CKAnalyzeException("Can't read resource file",e);
 		} catch (IndexOutOfBoundsException e) {
 			throw new CKAnalyzeException("Malformed CSV file",e);
 		}
 
 	}
 
 	private void analyzeCell(String field, int cellIndex,
 			List<ColumnType> colsTypes) {
 		if (!colsTypes.get(cellIndex).isGuessByHeader()) {
 			processColsWhithoutHeader(field, cellIndex, colsTypes, 1);
 		} else {
 			processColsWithHeaders(field, cellIndex, colsTypes);
 		}
 
 	}
 
 	/**
 	 * Process columns where type is not inferred by header analysis.
 	 * 
 	 * @param field
 	 * @param cellIndex
 	 * @param colsTypes
 	 * @param toAdd
 	 */
 	private void processColsWhithoutHeader(String field, int cellIndex,
 			List<ColumnType> colsTypes, int toAdd) {
 		Datatype cellVal = parse(field);
 		if (!colsTypes.get(cellIndex).getConfidenceTypes().containsKey(cellVal)) {
 			colsTypes.get(cellIndex).getConfidenceTypes().put(cellVal, 1);
 		}
 		// Treat multiple EMPTY types differently. If a column
 		// is totally empty value 1 is sufficient. If it is not
 		// completely empty the desired type is not EMPTY
 		else if (!cellVal.equals(Datatype.EMPTY)) {
 			colsTypes
 					.get(cellIndex)
 					.getConfidenceTypes()
 					.put(cellVal,
 							colsTypes.get(cellIndex).getConfidenceTypes()
 									.get(cellVal)
 									+ toAdd);
 		}
 	}
 
 	/**
 	 * Process column where a type was inferred by the header analysis
 	 * 
 	 * @param field
 	 * @param cellIndex
 	 * @param colsTypes
 	 */
 	private void processColsWithHeaders(String field, int cellIndex,
 			List<ColumnType> colsTypes) {
 		// Convention: DATE = 2 guessedbyHeader means date
 		// identification not sure.
 		if (colsTypes.get(cellIndex).getConfidenceTypes()
 				.containsKey(Datatype.DATE)
 				&& (colsTypes.get(cellIndex).getConfidenceTypes()
 						.get(Datatype.DATE) > 1)) {
 			Datatype cellVal = parse(field);
 			int toadd = 1;
 			if (cellVal.equals(Datatype.DATE)) {
 				toadd = 2;
 			}
 			processColsWhithoutHeader(field, cellIndex, colsTypes, toadd);
 		}
 	}
 
 	private void computeStringMetrics(List<String[]> file, Set<Integer> strPos) {
 		int numOfStrings = 0;
 		double sum = 0;
 		for (String[] row : file) {
 			for (int i = 0; i < row.length; i++) {
 				// Column recognized to be a String column
 				if (strPos.contains(i)) {
 					numOfStrings = numOfStrings + 1;
 					long length = row[i].length();
 					if (!stringLengthDistribution.containsKey(length)) {
 						stringLengthDistribution.put(length, new Long(1));
 					} else {
 						stringLengthDistribution.put(length,
 								stringLengthDistribution.get(length) + 1);
 					}
 				}
 			}
 		}
 		for (Long length : stringLengthDistribution.keySet()) {
 			sum = sum + (length * stringLengthDistribution.get(length));
 		}
 		if (!stringLengthDistribution.isEmpty()) {
 			stringLengthAvg = sum / numOfStrings;
 		} else {
 			stringLengthAvg = 0;
 		}
 	}
 
 	/**
 	 * Process header line trying to identify types
 	 * 
 	 * @param headers
 	 *            heading line
 	 * @param cols
 	 *            ArrayList of ColumnType
 	 */
 	private void processHeader(String[] headers, List<ColumnType> cols) {
 		for (String hd : headers) {
 			ColumnType ct = new ColumnType();
 			String tp = hd.toLowerCase().trim();
 			// different heuristics for sure date identification
			if ((tp.equals("anno")) || (tp.equals("anni"))
					|| (tp.contains("year")) || (tp.equals("mese"))
					|| (tp.equals("mesi")) || (tp.contains("month"))) {
 				ct.setGuessByHeader(true);
 				ct.getConfidenceTypes().put(Datatype.DATE, 1);
 			}
 			// the word Data is ambiguous (different means for ita and eng)
 			else if (tp.contains("data")) {
 				ct.setGuessByHeader(true);
 				ct.getConfidenceTypes().put(Datatype.DATE, 2);
 			}
 			// geojson identification
 			else if (tp.equals("geojson")) {
 				ct.setGuessByHeader(true);
 				ct.getConfidenceTypes().put(Datatype.GEOJSON, 1);
 			}
 			// Despite of column identification add an element foreach column
 			cols.add(ct);
 		}
 	}
 
 	// Compute the colsPerType variable
 	private void computeTypeOfColumn(List<ColumnType> metrics,
 			Set<Integer> strPos) {
 		for (int i = 0; i < metrics.size(); i++) {
 			ColumnType ct = metrics.get(i);
 			Map<Datatype, Integer> hashMap = ct.getConfidenceTypes();
 			int max = 0;
 			Datatype retval = null;
 			for (Datatype d : hashMap.keySet()) {
 				int value = hashMap.get(d);
 				logger.debug(d + ": \t" + value);
 				if (value > max) {
 					max = value;
 					retval = d;
 				} else if ((value == max) && (retval != null)
 						&& (!retval.equals(Datatype.EMPTY))) {
 					max = value;
 					retval = d;
 				}
 			}
 			logger.debug("%%%%%%%%%%%");
 			if (retval.equals(Datatype.STRING)) {
 				strPos.add(i);
 			}
 			if (!colsPerType.containsKey(retval)) {
 				colsPerType.put(retval, 1);
 			} else {
 				colsPerType.put(retval, colsPerType.get(retval) + 1);
 			}
 		}
 	}
 
 	// Try to parse the field and returns a Datatype value
 	private Datatype parse(String str) {
 		if (checkEmpty(str)) {
 			return Datatype.EMPTY;
 		}
 		if (checkInt(str)) {
 			return Datatype.INT;
 		}
 		if (checkFloat(str)) {
 			return Datatype.FLOAT;
 		}
 		if (checkDate(str)) {
 			return Datatype.DATE;
 		}
 		return Datatype.STRING;
 	}
 
 	private boolean checkEmpty(String str) {
 		String tocheck = str.replaceAll(" ", "");
 		return tocheck.isEmpty();
 	}
 
 	private boolean checkInt(String str) {
 		try {
 			Long.parseLong(str);
 			return true;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 
 	private boolean checkFloat(String str) {
 		try {
 			String toCheck = str;
 			if (toCheck.contains(".") && (toCheck.contains(","))
 					&& (toCheck.lastIndexOf(',') < toCheck.lastIndexOf('.'))) {
 				{
 					toCheck = toCheck.replace(",", "");
 				}
 			}
 			Double.parseDouble(toCheck);
 			return true;
 		} catch (NumberFormatException e) {
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 
 	}
 
 	private boolean checkDate(String str) {
 		if (str.length() > MAX_DATE_SIZE) {
 			return false;
 		}
 		String toCheck = str.replaceAll("[^\\w\\s]", "");
 		return (DateIdentifier.isADate(toCheck).getResult());
 	}
 
 	private boolean moreThanOneColumn(List<String[]> check) {
 		return check.get(0).length > 1;
 	}
 
 	// Getters methods
 	public File getFile() {
 		return file;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public int getRowCount() {
 		return rowCount;
 	}
 
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 	public Map<Datatype, Integer> getColsPerType() {
 		return colsPerType;
 	}
 
 	public Map<Long, Long> getStringLengthDistribution() {
 		return stringLengthDistribution;
 	}
 
 	public double getStringLengthAvg() {
 		return stringLengthAvg;
 	}
 
 	/**
 	 * This class represent a Column in type identification.
 	 * 
 	 * @author Alberto Zanella <a.zanella@trentorise.eu>
 	 * 
 	 */
 	class ColumnType {
 		/**
 		 * For each Datatype identified in column it contains a counter. At the
 		 * end the type associated to the highest counter is considered the type
 		 * for the column.
 		 */
 		public boolean isGuessByHeader() {
 			return guessByHeader;
 		}
 
 		public void setGuessByHeader(boolean guessByHeader) {
 			this.guessByHeader = guessByHeader;
 		}
 
 		/**
 		 * Indicates if the type was guessed also by header line alaysis
 		 */
 		public Map<Datatype, Integer> getConfidenceTypes() {
 			return confidenceTypes;
 		}
 
 		private Map<Datatype, Integer> confidenceTypes;
 		private boolean guessByHeader;
 
 		public ColumnType() {
 			this.confidenceTypes = new HashMap<Datatype, Integer>();
 		}
 	}
 }
