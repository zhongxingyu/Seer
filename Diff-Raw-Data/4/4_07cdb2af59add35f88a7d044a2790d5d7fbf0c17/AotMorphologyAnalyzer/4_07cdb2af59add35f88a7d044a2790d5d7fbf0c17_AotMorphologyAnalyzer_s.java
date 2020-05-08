 package dart.blackcat.talker.aot;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import dart.blackcat.talker.aot.dao.AotDBDao;
 import dart.blackcat.talker.domain.MorphologyAnalysis;
 import dart.blackcat.talker.domain.MorphologyAnalyzer;
 
 /**
  * {@link MorphologyAnalysis} factory
  * @author pvyazankin
  *
  */
 public class AotMorphologyAnalyzer implements MorphologyAnalyzer {
 	
 	private static final Log log = LogFactory.getLog(AotMorphologyAnalyzer.class);
 	
	private AotDBDao aotDao;
 	private Set<String> prefixes;
 
 	public void setAotDao(AotDBDao aotDao) {
 		this.aotDao = aotDao;
 	}
 	
 	public void init() {
 		prefixes = aotDao.getPrefixes();
 	}
 
 	/**
 	 * {@link MorphologyAnalysis} factory method
 	 * @param word word to analyze
 	 * @return {@link Set} of {@link MorphologyAnalysis}es. Can be empty.
 	 * @throws AotException if there is any problem
 	 */
 	@Override
 	public Set<MorphologyAnalysis> analyze(String word) throws AotException  {
 		word = word.toUpperCase();
 		log.debug(word + " - analyzing");
 		Set<MorphologyAnalysis> result = new HashSet<MorphologyAnalysis>();
 		
 		if ( ! word.isEmpty()) {
 			result = analyze0(word);
 			
 			if (result.isEmpty()) {
 				word = cutPrefix(word);
 				if (word != null) {
 					result = analyze0(word);
 				}
 			}
 		}
 		
 		log.debug(word + " " + result.size() + " results found.");
 		return result;
 	}
 	
 	protected Set<MorphologyAnalysis> analyze0(String word) throws AotException {
 		Set<MorphologyAnalysis> result = new HashSet<MorphologyAnalysis>();
 		int length = word.length();
 
 		while (result.isEmpty() && length >= 0) {
 			result = aotDao.findWord(word.substring(0, length), word.substring(length));
 			length--;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * cut prefix or return null if unable
 	 */
 	protected String cutPrefix(String word) {
 		for (Iterator<String> i = prefixes.iterator(); i.hasNext();) {
 			String prefix = i.next();
 			if (word.startsWith(prefix)) {
 				return word.substring(prefix.length());
 			}
 		}
 		return null;
 	}
 	
 }
