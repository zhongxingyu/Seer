 package uk.ac.susx.mlcl.parser;
 
 
 import com.beust.jcommander.Parameter;
 import org.maltparser.core.exception.MaltChainedException;
 import org.maltparser.core.syntaxgraph.DependencyStructure;
 import org.maltparser.core.syntaxgraph.edge.Edge;
 import uk.ac.susx.mlcl.featureextraction.IndexToken;
 import uk.ac.susx.mlcl.featureextraction.InvalidEntryException;
 import uk.ac.susx.mlcl.featureextraction.Sentence;
 import uk.ac.susx.mlcl.featureextraction.Token;
 import uk.ac.susx.mlcl.featureextraction.annotations.Annotations;
 import uk.ac.susx.mlcl.featureextraction.featurefactory.FeatureFactory;
 import uk.ac.susx.mlcl.featureextraction.featurefunction.DependencyFeatureFunction;
 import uk.ac.susx.mlcl.util.IntSpan;
 
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  * @author jp242
  */
 public class StanPoSMaltDepParser extends StanfordParser {
 
 	private static final Logger LOG =
 	Logger.getLogger(AbstractParser.class.getName());
 
 	public class StanMaltConfig extends StanConfig {
 
 		private static final long serialVersionUID = 1L;
 
 		@Parameter(names = {"-dr", "--depRels"},
 		description = "Array of dependancy relations to extract.")
 		private List<String> depList = new ArrayList<String>();
 
 		@Parameter(names = {"-hdr", "--headDepRels"},
 		description = "Array of head dependancy relations to extract.")
 		private List<String> hDepList = new ArrayList<String>();
 
 		@Parameter(names = {"-modN", "--modelName"},
 		description = "Name of the dependancy parser model")
 		private String modN = "";
 
 		public List depList() {
 			return depList;
 		}
 
 		public List hDepList() {
 			return hDepList;
 		}
 
 		public String modelName() {
 			return modN;
 		}
 	}
 
 	private StanMaltConfig config;
 	protected BlockingQueue<MaltParserWrapper> parsers;// protected for testing
 
 	@Override
 	protected List<Sentence> annotate(Map<Object, Object> map) {
 		//entry must represent document, consisting of a set of sentences, separated by newLineDelim()
 		List<Sentence> annotatedSentences = new ArrayList<Sentence>();
 
 		if (!config().isUseTokenAsBase() && !config().isUseChunkAsBase()) {
 			throw new RuntimeException(
 			"useTokenAsBase is off, there must be base entries!");
 		}
 
 		if (config.depList == null && config().hDepList == null) {
 			throw new RuntimeException(
 			"No dependancy relations specified!");
 		}
 
 
 		for (Object sentString : map.keySet()) {
 			Sentence sentObject = annotateWithTokenInfo((String) sentString);
 			if (sentObject.size() < 1) {
 				throw new InvalidEntryException("empty sentence!");
 			}
 			try {
 				sentObject = annotateWithDependencies((DependencyStructure) map.get(sentString), sentObject);
 			} catch (MaltChainedException e) {
 				e.printStackTrace();
 			}
 			applyFeatureFactory(getFeatureFactory(), sentObject);
 			annotatedSentences.add(sentObject);
 		}
 		return annotatedSentences;
 	}
 
 	Sentence annotateWithTokenInfo(String sentenceStr) {
 		//sentenceStr must be a single sentence
 		Sentence annotatedSent = new Sentence();
 		String[] splitSent = sentenceStr.split(getTokenDelim());
 		for (int i = 0; i < splitSent.length; i++) {
 			Token t = new Token();
 			String[] tokPos = splitSent[i].split(getPosDelim());
 			String pos = config.isUseCoarsePos() ? Token.coarsifyPoSTag(tokPos[2]) : tokPos[2];
 			String lemma = tokPos[1];
 			t.setAnnotation(Annotations.OriginalTokenAnnotation.class, tokPos[0]);
 			String token = config.isUseLemma() ? lemma : tokPos[0];
 			t.setAnnotation(Annotations.TokenAnnotation.class, token + getPosDelim() + pos);
 			t.setAnnotation(Annotations.PoSAnnotation.class, pos);
 			if (config.depList() != null) {
 				t.setAnnotation(Annotations.DependencyListAnnotation.class, new HashMap<String, ArrayList<String>>());
 			}
 			if (config.hDepList() != null) {
 				t.setAnnotation(Annotations.DependencyHeadListAnnotation.class, new HashMap<String, ArrayList<String>>());
 			}
 			annotatedSent.add(t);
 			IndexToken<CharSequence> key = new IndexToken<CharSequence>(new IntSpan(i, i), Annotations.TokenAnnotation.class);
 			annotatedSent.addKey(key);
 		}
 
 		return annotatedSent;
 	}
 
 	Sentence annotateWithDependencies(DependencyStructure graph, Sentence annotatedSent) throws MaltChainedException {
 		if (graph != null) {
 			for (Edge edge : graph.getEdges()) {
 				if (MaltParserWrapper.getHeadIndex(edge) - 1 >= 0 &&
 				MaltParserWrapper.getDependantIndex(edge) - 1 >= 0) {
 
 					ArrayList<String> feats = (ArrayList<String>) annotatedSent.
 					get(MaltParserWrapper.getHeadIndex(edge) - 1).
 					getAnnotation(Annotations.DependencyHeadListAnnotation.class).
 					get(MaltParserWrapper.getDepRel(edge, graph));
 
 					if (config.hDepList() != null) {
 						if (feats == null) {
 							feats = new ArrayList<String>();
 							feats.add(MaltParserWrapper.getDependant(edge, graph));
 							annotatedSent.get(MaltParserWrapper.getHeadIndex(edge) - 1).
 							getAnnotation(Annotations.DependencyHeadListAnnotation.class).
 							put(MaltParserWrapper.getDepRel(edge, graph), feats);
 						} else {
 							feats.add(MaltParserWrapper.getDependant(edge, graph));
 						}
 					}
 					if (config.depList() != null) {
 						feats = (ArrayList<String>) annotatedSent.
 						get(MaltParserWrapper.getDependantIndex(edge) - 1).
 						getAnnotation(Annotations.DependencyListAnnotation.class).
 						get(MaltParserWrapper.getDepRel(edge, graph));
 
 						if (feats == null) {
 							feats = new ArrayList<String>();
 							feats.add(MaltParserWrapper.getHead(edge, graph));
 							annotatedSent.get(MaltParserWrapper.getDependantIndex(edge) - 1).
 							getAnnotation(Annotations.DependencyListAnnotation.class).
 							put(MaltParserWrapper.getDepRel(edge, graph), feats);
 						} else {
							feats.add(MaltParserWrapper.getDependant(edge, graph));
 						}
 					}
 				}
 			}
 		} else {
 			System.err.println("Dependency graph is null");
 		}
 		return annotatedSent;
 	}
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 		StanPoSMaltDepParser sp = new StanPoSMaltDepParser();
 		sp.init(args);
 		long start = System.currentTimeMillis();
 		sp.parse();
 		System.out.println("Time (ms) = " + (System.currentTimeMillis() - start));
 	}
 
 	@Override
 	protected FeatureFactory buildFeatureFactory() {
 		FeatureFactory featurefactory = super.buildFeatureFactory();
 		for (String dep : config.depList) {
 			DependencyFeatureFunction fn = new DependencyFeatureFunction(dep, true);
 			fn.setPrefix(dep + "-DEP:");
 			featurefactory.addFeature("Dependency feature function (dep)" + dep, fn);
 		}
 
 		for (String dep : config.hDepList) {
 			DependencyFeatureFunction fn = new DependencyFeatureFunction(dep, false);
 			fn.setPrefix(dep + "-HEAD:");
 			featurefactory.addFeature("Dependency feature function (head)" + dep, fn);
 		}
 
 		return featurefactory;
 	}
 
 	@Override
 	public void init(String[] args) {
 		config = new StanMaltConfig();
 		config.load(args);
 		int n = config.getNumCores();
 		this.parsers = new LinkedBlockingDeque<MaltParserWrapper>(n);
 		if (config.modelName().length() > 0) {
 			//only load PoS tagger and dep. parser models if they have been requested
 			super.initPreProcessor();
 			for (int i = 0; i < n; i++) {
 				MaltParserWrapper maltPar = new MaltParserWrapper(super.getPosDelim(), config.modelName(), i);
 				parsers.add(maltPar);
 			}
 		}
 	}
 
 	protected StanMaltConfig config() {
 		return config;
 	}
 
 	@Override
 	public String getOutPath() {
 		String outpath = super.getOutPath();
 
 		if (config().depList() != null) {
 			for (String dep : config.depList) {
 				outpath += "-" + dep;
 			}
 		}
 		if (config().hDepList() != null) {
 			for (String dep : config.hDepList) {
 				outpath += "-" + dep;
 			}
 		}
 
 		if (config().depList() != null) {
 			for (String dep : config.depList) {
 				outpath += "-" + dep;
 			}
 		}
 		if (config().hDepList() != null) {
 			for (String dep : config.hDepList) {
 				outpath += "-" + dep;
 			}
 		}
 
 		return outpath;
 	}
 
 	@Override
 	protected Map<Object, Object> loadPreparsedEntry(String entry) {
 		throw new IllegalStateException("Loading pre-parsed text not implemented for this type");
 	}
 
 	/**
 	 * Returns a map between PoS tagged, tokenized, lemmatized sentences and their corresponding parse trees
 	 *
 	 * @param text
 	 * @throws ModelNotValidException
 	 */
 	@Override
 	protected Map<Object, Object> rawTextParse(final CharSequence text) {
 		//sentence segment, tokenize, lemmatize and PoS tag
 		//returns a map of id to PoS tagged, tokenized, lemmatized sentences
 		Map<Object, Object> ret = super.rawTextParse(text);
 
 
 		MaltParserWrapper maltPar = getNextAvailableParser();
 		Map<Object, Object> parseTrees = new LinkedHashMap<Object, Object>();//maintain sentence order
 
 		for (Object intID : ret.keySet()) {
 			String line = (String) ret.get(intID);
 			if (config().depList() != null && config().hDepList() != null) {
 				//do not parse if dependency features have not been requested
 				String[] sentence = maltPar.formatSentenceForMaltParser(line.split(getTokenDelim()));
 				try {
 					DependencyStructure graph = maltPar.parse(sentence);
 					parseTrees.put(line, graph);
 				} catch (MaltChainedException ex) {
 					Logger.getLogger(StanPoSMaltDepParser.class.getName()).log(Level.SEVERE, null, ex);
 					System.err.println("Sentence is: \n" + Arrays.toString(sentence));
 //                  System.err.println("Parser is: \n" + maltPar);
 				}
 			} else {
 				parseTrees.put(line, null);
 			}
 		}
 
 		returnParser(maltPar);
 		return parseTrees;
 	}
 
 	MaltParserWrapper getNextAvailableParser() {
 		MaltParserWrapper maltPar = null;
 		try {
 			maltPar = parsers.take();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return maltPar;
 	}
 
 	void returnParser(MaltParserWrapper maltPar) {
 		parsers.offer(maltPar); //free up the parser, it is no longer required
 	}
 }
