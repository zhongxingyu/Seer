 package com.gentics.cr.lucene.indexer.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.CharArraySet;
 import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.util.Version;
 
 import com.gentics.cr.CRConfigFileLoader;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.LuceneVersion;
 import com.gentics.cr.lucene.analysis.ReverseAnalyzer;
 import com.gentics.cr.lucene.indexer.IndexerUtil;
 
 /**
  * TODO javadoc.
  * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
  * @version $Revision: 131 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public final class LuceneAnalyzerFactory {
 
 	/**
 	 * Private constructor.
 	 */
 	private LuceneAnalyzerFactory() {
 	}
 
 	/**
 	 * Log4j Logger for error and debug messages.
 	 */
 	protected static final Logger LOGGER = Logger.getLogger(LuceneAnalyzerFactory.class);
 
 	/**
 	 * Stop word config key.
 	 */
 	private static final String STOP_WORD_FILE_KEY = "STOPWORDFILE";
 
 	/**
 	 * Analyzer config key.
 	 */
 	private static final String ANALYZER_CONFIG_KEY = "ANALYZERCONFIG";
 
 	/**
 	 * Analyzer class key.
 	 */
 	private static final String ANALYZER_CLASS_KEY = "ANALYZERCLASS";
 
 	/**
 	 * Field name.
 	 */
 	private static final String FIELD_NAME_KEY = "FIELDNAME";
 
 	/**
 	 * Reveres attributes key.
 	 */
 	private static final String REVERSE_ATTRIBUTES_KEY = "REVERSEATTRIBUTES";
 
 	/**
 	 * 	Reverse Attribute suffix.
 	 */
 	public static final String REVERSE_ATTRIBUTE_SUFFIX = "_REVERSE";
 
 	/**
 	 * This Map stores the same information as the PerFieldAnalyzerWrapper, 
 	 * makes the used Analyzer class names (canonical names) per field accessible.
 	 * filled in the createAnalyzer method
 	 */
 	private static Map<String, String> configuredAnalyzerMap = new HashMap<String, String>();
 
 	/**
 	 * TODO javadoc.
 	 * @param config TODO javadoc
 	 * @return TODO javadoc
 	 */
 	public static List<String> getReverseAttributes(final GenericConfiguration config) {
 		GenericConfiguration analyzerConfig = loadAnalyzerConfig(config);
 		if (analyzerConfig != null) {
 			String reverseAttributeString = (String) analyzerConfig.get(REVERSE_ATTRIBUTES_KEY);
 			return IndexerUtil.getListFromString(reverseAttributeString, ",");
 		}
 		return null;
 	}
 
 	/**
 	 * Creates an analyzer from the given config.
 	 * @param config TODO javadoc
 	 * @return TODO javadoc
 	 */
 	public static Analyzer createAnalyzer(final GenericConfiguration config) {
 		// Caching the analyzer instances is not possible as those do not implement Serializable
 		// TODO: cache the config (imho caching should be implemented in the config itself)
 
 		PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(createDefaultAnalyzer(config));
 		configuredAnalyzerMap.clear();
 
 		//Load analyzer config
 		GenericConfiguration analyzerConfig = loadAnalyzerConfig(config);
 		if (analyzerConfig != null) {
 			ArrayList<String> addedReverseAttributes = new ArrayList<String>();
 			List<String> reverseAttributes = getReverseAttributes(config);
 			Map<String, GenericConfiguration> subconfigs = analyzerConfig.getSortedSubconfigs();
 			if (subconfigs != null) {
 				for (Map.Entry<String, GenericConfiguration> entry : subconfigs.entrySet()) {
 					GenericConfiguration analyzerconfig = entry.getValue();
 					String fieldname = analyzerconfig.getString(FIELD_NAME_KEY);
 					String analyzerclass = analyzerconfig.getString(ANALYZER_CLASS_KEY);
 
 					Analyzer analyzerInstance = createAnalyzer(analyzerclass, analyzerconfig);
 					analyzerWrapper.addAnalyzer(fieldname, analyzerInstance);
 					configuredAnalyzerMap.put(fieldname, analyzerInstance.getClass().getCanonicalName());
 
 					//ADD REVERSE ANALYZERS
 					if (reverseAttributes != null && reverseAttributes.contains(fieldname)) {
 						addedReverseAttributes.add(fieldname);
 
 						ReverseAnalyzer reverseAnalyzer = new ReverseAnalyzer(analyzerInstance);
 						analyzerWrapper.addAnalyzer(fieldname + REVERSE_ATTRIBUTE_SUFFIX, reverseAnalyzer);
 
 						configuredAnalyzerMap.put(fieldname + REVERSE_ATTRIBUTE_SUFFIX, reverseAnalyzer.getClass()
 								.getCanonicalName());
 					}
 				}
 			}
 			//ADD ALL NON CONFIGURED REVERSE ANALYZERS
 			if (reverseAttributes != null && reverseAttributes.size() > 0) {
 				for (String att : reverseAttributes) {
 					if (!addedReverseAttributes.contains(att)) {
 						ReverseAnalyzer reverseAnalyzer = new ReverseAnalyzer(null);
 						analyzerWrapper.addAnalyzer(att + REVERSE_ATTRIBUTE_SUFFIX, reverseAnalyzer);
 						configuredAnalyzerMap.put(att + REVERSE_ATTRIBUTE_SUFFIX, reverseAnalyzer.getClass()
 								.getCanonicalName());
 					}
 				}
 			}
 		}
 		return analyzerWrapper;
 	}
 
 	/**
 	 * TODO javadoc.
 	 * @param config TODO javadoc
 	 * @return TODO javadoc
 	 */
 	private static GenericConfiguration loadAnalyzerConfig(final GenericConfiguration config) {
 		if (config.hasSubConfig(ANALYZER_CONFIG_KEY)) {
 			return config.getSubConfig(ANALYZER_CONFIG_KEY);
 		} else {
 			GenericConfiguration analyzerConfig = null;
 			String confpath = config.getString(ANALYZER_CONFIG_KEY);
 			if (confpath != null) {
 				analyzerConfig = new GenericConfiguration();
 				try {
 					CRConfigFileLoader.loadConfiguration(analyzerConfig, confpath, null);
 				} catch (IOException e) {
 					LOGGER.error("Could not load analyzer configuration from " + confpath + ". Using default config.");
 					analyzerConfig = new GenericConfiguration();
 					analyzerConfig.set("content.analyzerclass","com.gentics.cr.lucene.autocomplete.AutocompleteAnalyzer");
 					analyzerConfig.set("content.fieldname","grammedwords");
 				}
 			}
 			return analyzerConfig;
 		}
 	}
 
 	/**
 	 * TODO javadoc.
 	 * @param analyzerclass TODO javadoc
 	 * @param config TODO javadoc
 	 * @return TODO javadoc
 	 */
 	private static Analyzer createAnalyzer(final String analyzerclass, final GenericConfiguration config) {
 		Analyzer a = null;
 		try {
 			//First try to create an Analyzer that takes a config object
 			a = (Analyzer) Class.forName(analyzerclass).getConstructor(new Class[] { GenericConfiguration.class })
 					.newInstance(config);
 		} catch (Exception e1) {
 			try {
 				//IF FIRST FAILS TRY SIMPLE CONSTRUCTOR
 				a = (Analyzer) Class.forName(analyzerclass).getConstructor().newInstance();
 			} catch (Exception e2) {
 				//IF SIMPLE FAILS, PROBABLY DID NOT FIND CONSTRUCTOR,
 				//TRYING WITH VERSION ADDED
				LOGGER.error("Configured Analyzer fails in the default constructor or it does not have a default constructor.", e2);
 				try {
 					a = (Analyzer) Class.forName(analyzerclass).getConstructor(new Class[] { Version.class })
 							.newInstance(LuceneVersion.getVersion());
 				} catch (Exception e3) {
 					LOGGER.error("Could not instantiate Analyzer with class " + analyzerclass
 							+ ". Do you use some special" + " Analyzer? Or do you need to use a Wrapper?", e3);
 				}
 			}
 		}
 		return a;
 	}
 
 	/**
 	 * TODO javadoc.
 	 * @param config TODO javadoc
 	 * @return TODO javadoc
 	 */
 	private static Analyzer createDefaultAnalyzer(final GenericConfiguration config) {
 		//Update/add Documents
 		Analyzer analyzer;
 		File stopWordFile = IndexerUtil.getFileFromPath((String) config.get(STOP_WORD_FILE_KEY));
 		if (stopWordFile != null) {
 			//initialize Analyzer with stop words
 			try {
 				analyzer = new StandardAnalyzer(LuceneVersion.getVersion(), stopWordFile);
 				return analyzer;
 			} catch (IOException ex) {
 				LOGGER.error("Could not open stop words file. " + "Will create standard " + "analyzer.", ex);
 			}
 		}
 
 		analyzer = new StandardAnalyzer(LuceneVersion.getVersion(), CharArraySet.EMPTY_SET);
 		return analyzer;
 	}
 
 	/**
 	 * Return a map of all used analyzers (per field).
 	 * This method calls createAnalyzer(config) so it is quite expensive.
 	 * The config parameter is needed for the call to createAnalyzer as this method
 	 * reads the analyzer configuration everytime!
 	 * Key: fieldname
 	 * Value: canonical class name
 	 * @param config needed for listing all analyzers.
 	 * @return Map of analyzers per field.
 	 */
 	public static Map<String, String> getConfiguredAnalyzers(final GenericConfiguration config) {
 		createAnalyzer(config);
 		return configuredAnalyzerMap;
 	}
 }
