 package parser;
 
 import java.util.ArrayList;
 
 import logging.ILogger;
 import ontology.IOntology;
 import ontology.IStandardEntry;
 import ontology.IStandardEntryConverter;
 import ontology.NumberCheck;
 import ontology.Ontology;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import s3filecontrol.FileIterator;
 import s3filecontrol.HeaderValueTuple;
 import s3filecontrol.IParsingFile;
 
 public class ParserFactory implements IParserFactory {
 	private ILogger logger;
 	private IOntology ontology;
 
 	public ParserFactory(ILogger logger) {
 		this.logger = logger;
 		ontology = new Ontology(logger);
 	}
 
 	public boolean initializeComponents() {
 		return ontology.readFromFile("standardizedValueHashMap2.json");
 	}
 	
 	public boolean parseFile(IParsingFile dataFile) {
 		try {
 			ArrayList<HeaderValueTuple> set = dataFile.getFirstSet();
 			ArrayList<IStandardEntryConverter> converters = parseSet(set);
 			if (converters == null)
 				return false;
 			
 			FileIterator iterator = dataFile.getFileIterator();
 			JSONArray array = new JSONArray();
 			
 			logger.logEvent("Starting conversion");
 			
 			while (iterator.hasNext()) {
 				int i = 0;
 				for (HeaderValueTuple tuple : iterator.next()) {
					JSONObject obj = new JSONObject();
 					
 					tuple = converters.get(i).convert(tuple);
 					
 					obj.put(tuple.getHeader(), tuple.getValue());
					array.add(obj);
 					i++;
 				}
 			}
 			
 			dataFile.setStandardJsonArray(array);
 			
 			logger.logEvent("Conversion complete");
 			
 			
 		} catch (Exception ex) {
 			logger.logEvent("AAHH");
 			logger.logException(ex);
 			return false;
 		}
 		return true;
 	}
 	
 	private ArrayList<IStandardEntryConverter> parseSet(ArrayList<HeaderValueTuple> set) {
 		ArrayList<IStandardEntry> standardEntryList = new ArrayList<IStandardEntry>();
 		ArrayList<IStandardEntryConverter> converters = new ArrayList<IStandardEntryConverter>();
 		int[] indexCounter = new int[set.size()];
 		
 		
 		for (int i = 0; i < set.size(); ++i) {
 			HeaderValueTuple tuple = set.get(i);
 		
 			IStandardEntry ret = ontology.getStandardEntryFor(tuple);
 			if (ret == null) {
 				logger.logError("Failed to find a match for " + tuple);
 				return null;
 			}
 			
 			if (standardEntryList.contains(ret)) { 
 				int index = standardEntryList.indexOf(ret);
 				IStandardEntryConverter converter = converters.get(index);
 				
 				if (!converter.hasIndexBeenSet())
 					converter.setIndex(1);
 				indexCounter[index]++;
 				
 				int currentIndex = indexCounter[i];
 				
 				if (NumberCheck.containsNumber(tuple.getHeader()))
 					converter = ret.getConverter(tuple);
 				else
 					converter = ret.getConverter(tuple, currentIndex);
 				converters.add(converter);
 			} else {
 				standardEntryList.add(ret);
 				converters.add(ret.getConverter(tuple));
 				indexCounter[i]++;
 			}
 		}
 		return converters;
 	}
 }
