 package org.strategoxt.imp.testing.strategies;
 
 import java.util.Map;
 import java.util.WeakHashMap;
 
import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.spoofax.interpreter.library.IOAgent;
 import org.strategoxt.imp.runtime.Environment;
 import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
 import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
 import org.strategoxt.imp.runtime.services.StrategoObserver;
 import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
 import org.strategoxt.lang.Context;
 
 /** 
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class ObserverCache {
 	
 	private static final ObserverCache instance = new ObserverCache();
 	
 	private static final Map<Descriptor, StrategoObserver> asyncCache =
 		new WeakHashMap<Descriptor, StrategoObserver>();
 
 	private ObserverCache() {}
 	
 	public static ObserverCache getInstance() {
 		return instance;
 	}
 
 	public StrategoObserver getObserver(Context context, String languageName) throws BadDescriptorException {
		Language language = LanguageRegistry.findLanguage(languageName);
		if (language == null) throw new BadDescriptorException("No language known with the name " + languageName);
		Descriptor descriptor = Environment.getDescriptor(language);
 		if (descriptor == null) throw new BadDescriptorException("No language known with the name " + languageName);
 		
 		return getObserver(context, descriptor);
 	}
 
 	private synchronized StrategoObserver getObserver(Context context, Descriptor descriptor) throws BadDescriptorException {
 		StrategoObserver result = asyncCache.get(descriptor);
 
 		if (result == null)
 			result = descriptor.createService(StrategoObserver.class, null);
 		
 		IOAgent ioAgent = context.getIOAgent();
 		if (ioAgent instanceof EditorIOAgent) {
 			// Make the console visible to users
 			((EditorIOAgent) ioAgent).getDescriptor().setDynamicallyLoaded(true);
 			asyncCache.put(descriptor, result);
 		}
 		result.getRuntime().setIOAgent(ioAgent);
 		return result;
 	}
 }
