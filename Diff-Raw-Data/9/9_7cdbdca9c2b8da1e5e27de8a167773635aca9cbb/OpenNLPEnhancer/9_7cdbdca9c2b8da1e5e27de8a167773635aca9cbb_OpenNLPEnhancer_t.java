 package es.weso.acota.core.business.enhancer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 
 import es.weso.acota.core.CoreConfiguration;
 import es.weso.acota.core.business.enhancer.EnhancerAdapter;
 import es.weso.acota.core.business.enhancer.analyzer.opennlp.EnglishOpenNLPAnalyzer;
 import es.weso.acota.core.business.enhancer.analyzer.opennlp.OpenNLPAnalyzer;
 import es.weso.acota.core.business.enhancer.analyzer.opennlp.SpanishOpenNLPAnalyzer;
 import es.weso.acota.core.entity.ProviderTO;
 import es.weso.acota.core.entity.TagTO;
 import es.weso.acota.core.exceptions.AcotaConfigurationException;
 import es.weso.acota.core.utils.LanguageUtil;
 
 import static es.weso.acota.core.utils.LanguageUtil.ISO_639_SPANISH;
 
 /**
  * OpenNLPEnhancer is an {@link Enhancer} specialized in modifying
  * the weights, of the sets of{@link TagTO}s, depending on its morphosyntactic type.
  * 
  * @author César Luis Alvargonzález
  * @author Weena Jimenez
  */
 public class OpenNLPEnhancer extends EnhancerAdapter implements Configurable {
 	
 	protected static Logger logger;
 
 	protected Set<String> nouns;
 	protected Set<String> verbs;
 	protected Set<String> numbers;
 	
 	protected EnglishOpenNLPAnalyzer englishOpenNlpAnalyzer;
 	protected SpanishOpenNLPAnalyzer spanishOpenNlpAnalyzer;
 	
 	protected OpenNLPAnalyzer openNlpAnalyzer;
 	
 	protected CoreConfiguration configuration;
 	
 	/**
 	 * Zero-argument default constructor
 	 * @throws AcotaConfigurationException Any exception that occurs while initializing 
 	 * a Configuration object
 	 */
 	public OpenNLPEnhancer() throws AcotaConfigurationException {
 		super();
 		OpenNLPEnhancer.logger = Logger.getLogger(OpenNLPEnhancer.class);
 		OpenNLPEnhancer.provider = new ProviderTO("OpenNPL Enhancer");
 		
 		this.nouns = new HashSet<String>();
 		this.verbs = new HashSet<String>();
 		this.numbers = new HashSet<String>();
 		
 		loadConfiguration(configuration);
 	}
 	
 	/**
 	 * One-argument default constructor
 	 * @param configuration acota-core's configuration class
 	 * @throws AcotaConfigurationException Any exception that occurs while initializing 
 	 * a Configuration object
 	 */
 	public OpenNLPEnhancer(CoreConfiguration configuration) throws AcotaConfigurationException {
 		super();
 		OpenNLPEnhancer.logger = Logger.getLogger(OpenNLPEnhancer.class);
 		OpenNLPEnhancer.provider = new ProviderTO("OpenNPL tagger");
 		
 		this.nouns = new HashSet<String>();
 		this.verbs = new HashSet<String>();
 		this.numbers = new HashSet<String>();
 		
 		loadConfiguration(configuration);
 	}
 	
 	@Override
 	public void loadConfiguration(CoreConfiguration configuration) throws AcotaConfigurationException{
 		if(configuration==null)
 			configuration = new CoreConfiguration();
 		this.configuration = configuration;
 		
 		if(spanishOpenNlpAnalyzer == null)
 			this.spanishOpenNlpAnalyzer = new SpanishOpenNLPAnalyzer(configuration);
 		else
 			spanishOpenNlpAnalyzer.loadConfiguration(configuration);
 		
 		if(englishOpenNlpAnalyzer == null)
 			this.englishOpenNlpAnalyzer = new EnglishOpenNLPAnalyzer(configuration);
 		else
 			englishOpenNlpAnalyzer.loadConfiguration(configuration);
 	}
 	
 	@Override
 	protected void preExecute() throws Exception {
 		this.suggest = request.getSuggestions();
 		this.tags = suggest.getTags();
 		suggest.setResource(request.getResource());
 		
 		nouns.clear();
 		verbs.clear();
 		numbers.clear();
 	}
 	
 	@Override
 	protected void execute() throws Exception {
 		analyseLabelTerms();
 		analyseDescriptionTerms();
 	}
 
 	@Override
 	protected void postExecute() throws Exception {
 		logger.debug("Add providers to request");
 		this.request.getTargetProviders().add(provider);
 		logger.debug("Add suggestons to request");
 		this.request.setSuggestions(suggest);
 	}
 
 	/**
 	 * Makes an Analysis of the label terms
 	 * @throws IOException Any exception that occurs while reading OpenNLP's files
 	 * @throws AcotaConfigurationException Any exception that occurs while initializing 
 	 * a Configuration object
 	 */
 	protected void analyseLabelTerms() throws IOException, AcotaConfigurationException {
 		analysisOfTerms(request.getResource().getLabel());
 	}
 
 	/**
 	 * Makes an Analysis of the descriptions terms
 	 * @throws IOException Any exception that occurs while reading OpenNLP's files
 	 * @throws AcotaConfigurationException Any exception that occurs while initializing 
 	 * a Configuration object
 	 */
 	protected void analyseDescriptionTerms() throws IOException, AcotaConfigurationException {
 		analysisOfTerms(request.getResource().getDescription());
 	}
 
 	/**
 	 * Makes an Analysis of a text's terms
 	 * @param text Text to make the analysis
 	 * @throws IOException Any exception that occurs while reading OpenNLP's files
 	 * @throws AcotaConfigurationException Any exception that occurs while initializing 
 	 * a Configuration object
 	 */
 	public void analysisOfTerms(String text) throws IOException, AcotaConfigurationException {
 		this.openNlpAnalyzer = loadAnalyzer(text);
 
		String sentences[] = openNlpAnalyzer.sentDetect(text);
 		for (String sentence : sentences) {
 			String[] textTokenized = openNlpAnalyzer.tokenize(sentence);
 			processSetence(openNlpAnalyzer.tag(textTokenized), textTokenized);
 		}
 
 		findAndChangeNouns();
 		findAndChangeVerbs();
 		findAndChangeNumbers();
 	}
 
 	/**
 	 * Processes a set of terms and saves them depending on its morphosyntactic type
 	 * @param tags OpenNLP Tags related to the Tokenized Text
 	 * @param tokenizedText Tokenized Text
 	 * @throws AcotaConfigurationException 
 	 */
 	protected void processSetence(String[] tags, String[] tokenizedText) throws AcotaConfigurationException {
 		for (int i = 0; i < tokenizedText.length; i++) {
 			if (openNlpAnalyzer.isDispenasble(tags[i])) {
 				findAndRemove(tokenizedText[i]);
 			} else if (openNlpAnalyzer.isNoun(tags[i])) {
 				nouns.add(tokenizedText[i].toLowerCase());
 			} else if (openNlpAnalyzer.isVerb(tags[i])) {
 				verbs.add(tokenizedText[i].toLowerCase());
 			} else if (openNlpAnalyzer.isNumber(tags[i])) {
 				numbers.add(tokenizedText[i].toLowerCase());
 			}
 		}
 	}
 
 	/**
 	 * Calculates the maximum value of the tags Map
 	 * @return The maximum value of the tags Map
 	 */
 	protected double calculateMaxValue() {
 		List<TagTO> list = new ArrayList<TagTO>(tags.values());
 		Collections.sort(list);
 		double value = 0d;
 		if(list.size()>0)
 			value = list.get(0).getValue();
 		return value;
 	}
 
 	/**
 	 * Removes a word from the tags Map
 	 * @param label Label of the tag to remove
 	 */
 	protected void findAndRemove(String label) {
 		logger.debug("Remove some tags");
 		if (tags.containsKey(label.toLowerCase())) {
 			tags.remove(label.toLowerCase());
 		}
 	}
 
 	/**
 	 * Modifies Nouns' weight. Adds the half of the maximum weight.
 	 */
 	protected void findAndChangeNouns() {
 		double sum = calculateMaxValue() / 2;
 
 		for (Entry<String, TagTO> label : tags.entrySet()) {
 			if (nouns.contains(label.getKey())) {
 				label.getValue().addValue(sum);
 			}
 		}
 	}
 
 	/**
 	 * Modifies Verbs' weight. Subtracts the half of the maximum weight.
 	 */
 	protected void findAndChangeVerbs() {
 		double sum = calculateMaxValue() / 2;
 
 		for (Entry<String, TagTO> label : tags.entrySet()) {
 			if (verbs.contains(label.getKey())) {
 				label.getValue().subValue(sum);
 			}
 		}
 	}
 
 	/**
 	 * Modifies Numbers' weight. Subtracts the maximum weight.
 	 */
 	protected void findAndChangeNumbers() {
 		double value = calculateMaxValue();
 		for (Entry<String, TagTO> label : tags.entrySet()) {
 			if (numbers.contains(label.getKey())) {
 				label.getValue().subValue(value);
 			}
 		}
 	}
 	
 	/**
 	 * Loads a language analyzer (English or Spanish)
 	 * @param text Text to analyze
 	 * @return OpenNLP's {@link Analyzer}
 	 * @throws AcotaConfigurationException Any exception that occurs while initializing 
 	 * a Configuration object 
 	 */
 	protected OpenNLPAnalyzer loadAnalyzer(String text) throws AcotaConfigurationException {
 		String language = LanguageUtil.detect(text);
 		OpenNLPAnalyzer analyzer = null;
 		if (language.equals(ISO_639_SPANISH)) {
 			if(spanishOpenNlpAnalyzer==null)
 				this.spanishOpenNlpAnalyzer = new SpanishOpenNLPAnalyzer(configuration);
 			analyzer = spanishOpenNlpAnalyzer;
 		} else {
 			if(englishOpenNlpAnalyzer==null)
 				this.englishOpenNlpAnalyzer = new EnglishOpenNLPAnalyzer(configuration);
 			analyzer = englishOpenNlpAnalyzer;
 		}
 		return analyzer;
 	}
 
 }
