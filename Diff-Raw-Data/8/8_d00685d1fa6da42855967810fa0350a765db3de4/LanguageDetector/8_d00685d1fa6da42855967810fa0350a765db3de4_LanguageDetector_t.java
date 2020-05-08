 package es.weso.acota.core.utils.lang;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 
 import com.cybozu.labs.langdetect.Detector;
 import com.cybozu.labs.langdetect.DetectorFactory;
 import com.cybozu.labs.langdetect.LangDetectException;
 
 import es.weso.acota.core.business.enhancer.SeedConfiguration;
 import es.weso.acota.core.exceptions.AcotaConfigurationException;
 
 /**
  * Language Detection Util-Class, this class perform the language detection
  * and also contains the ISO 639 Codes supported by ACOTA. 
 * @see <a href="http://en.wikipedia.org/wiki/ISO_639">http://en.wikipedia.org/wiki/ISO_639</a>
  * 
  * @author César Luis Alvargonzález
  */
 public class LanguageDetector {
 	public static final String UTF_8 = "utf-8";
 	public static final String ISO_639_GERMAN = "de";
 	public static final String ISO_639_ENGLISH = "en";
 	public static final String ISO_639_SPANISH = "es";
 	public static final String ISO_639_FRENCH = "fr";
 	public static final String ISO_639_PORTUGESE = "pt";
 	public static final String ISO_639_UNDEFINED = "undefined";
 	
 	public static LanguageDetector LANGUAGE_UTIL_INSTANCE;
 
 	protected String profilesPath;
 	protected String[] profiles;
 	
 	/**
 	 * 
 	 * @param configuration
 	 * @throws AcotaConfigurationException
 	 */
 	private LanguageDetector(SeedConfiguration configuration) throws AcotaConfigurationException{
 		super();
 		loadConfiguration(configuration);
 	}
 	
 	/**
 	 * 
 	 * @param configuration
 	 * @return
 	 * @throws AcotaConfigurationException
 	 */
 	public static LanguageDetector getInstance(SeedConfiguration configuration) throws AcotaConfigurationException{
 		if(LANGUAGE_UTIL_INSTANCE==null)
 			LanguageDetector.LANGUAGE_UTIL_INSTANCE = new LanguageDetector(configuration);
 		return LANGUAGE_UTIL_INSTANCE;
 	}
 	
 	/**
 	 * 
 	 * @param configuration
 	 * @throws AcotaConfigurationException
 	 */
 	public void loadConfiguration(SeedConfiguration configuration) throws AcotaConfigurationException{
 		if(configuration == null)
 			configuration = new SeedConfiguration();
 		this.profilesPath = configuration.getLanguageProfilesPath();
 		this.profiles = configuration.getLanguageProfiles();
 	}
 	
 	/**
 	 * Detects the language of the supplied text
 	 * @param text The text to detect
 	 * @return ISO 639 Language Code
 	 * @throws AcotaConfigurationException Any exception that occurs 
 	 * while initializing a Configuration object
 	 */
 	public String detect(String text) throws AcotaConfigurationException{
 		Detector detector = null;
 		
 		if(text.isEmpty() || NumberUtils.isNumber(text))
 			return ISO_639_UNDEFINED;
 		try{
 			if(DetectorFactory.getLangList().isEmpty()){
 				loadProfilesAsJson();
 			}
 			detector = DetectorFactory.create();
 			detector.append(text);
 			return detector.detect();
 		}catch(LangDetectException e){
 			throw new AcotaConfigurationException("Failed langdetec initialization: "+text, e);
 		}	
 	}
 	
 	/**
 	 * Loads the languages profiles from resources folder
 	 * @throws AcotaConfigurationException Any exception that occurs 
 	 * while initializing a Configuration object
 	 */
 	private void loadProfilesAsJson() throws AcotaConfigurationException {
 		try{
 			InputStream input = null;
 			StringWriter writer = null;
 			List<String> languages = new LinkedList<String>();
 			for(String language : profiles){
 				input = LanguageDetector.class.getClassLoader().getResourceAsStream(profilesPath+"/"+language);
 				writer = new StringWriter();
 				IOUtils.copy(input, writer, UTF_8);
 				languages.add(writer.toString());
 			}
 			DetectorFactory.loadProfile(languages);
 		}catch(IOException e1){
 			throw new AcotaConfigurationException(e1);
 		} catch (LangDetectException e2) {
 			throw new AcotaConfigurationException(e2);
 		}
 	}
 		
 }
