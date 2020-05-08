 package chameleon.workspace;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.rejuse.junit.Revision;
 
 import chameleon.core.language.Language;
 
 public class LanguageRepository {
 
 	public Language get(String name, Revision revision) {
 		Map<Revision,Language> tmp = _languageMap.get(name.toUpperCase());
 		if(tmp != null) {
 			Language result = tmp.get(revision);
 			if(result != null) {
 				return result;
 			} else {
 				throw new IllegalArgumentException("Revision "+revision.toString()+ " of language "+name+" is not registered.");
 			}
 		}
 		throw new IllegalArgumentException("No language with name "+name+" is registered.");
 	}
 	
 	public void add(Language lang) {
 		String name = lang.name();
 		Revision rev = lang.version();
 		Map<Revision,Language> m = _languageMap.get(name.toUpperCase());
 		if(m == null) {
 			m = new HashMap<Revision, Language>();
 			_languageMap.put(name.toUpperCase(), m);
 		}
 		m.put(rev, lang);
 	}
 	
 	/**
 	 * Return the default version of the language with the given name. When an
 	 * explicit default version is set, that version is returned. Otherwise, the
 	 * highest version (as defined by {@link Revision#compareTo(Revision)}) is returned.
 	 * @param name
 	 * @return
 	 */
 	public Language get(String name) {
 		String n = name.toUpperCase();
 		Revision rev = _defaultRevisions.get(n);
 		if(rev == null) {
 			Map<Revision, Language> tmp = _languageMap.get(n);
 			// Doesn't want to compile with types in place.
			if(tmp != null) {
				Set revisions = tmp.keySet();
				rev = (Revision)Collections.max(revisions); // Command line compiler requires the cast, Eclipse doesn't :|
			}
		}
		if(name == null || rev == null) {
			throw new IllegalArgumentException("No language with name "+name +" is registered in this chameleon workspace.");
 		}
 		// If rev == null, {@link LanguageRepository#get(String,Revision)} will throw an exception.
 		return get(name, rev);
 	}
 	
 	public void setDefaultVersion(String languageName, Revision version) {
 		if(version == null || languageName == null) {
 			throw new IllegalArgumentException();
 		}
 		_defaultRevisions.put(languageName.toUpperCase(), version);
 	}
 	
 	
 	public void clearDefaultVersion(String languageName) {
 		_defaultRevisions.remove(languageName);
 	}
 	
 	public List<Language> languages() {
 		List<Language> result = new ArrayList<Language>();
 		for(Map<Revision,Language> map: _languageMap.values()) {
 			result.addAll(map.values());
 		}
 		return result;
 	}
 	
 	private Map<String,Map<Revision,Language>> _languageMap = new HashMap<String,Map<Revision,Language>>();
 	
 	private Map<String, Revision> _defaultRevisions = new HashMap<String, Revision>();
 }
