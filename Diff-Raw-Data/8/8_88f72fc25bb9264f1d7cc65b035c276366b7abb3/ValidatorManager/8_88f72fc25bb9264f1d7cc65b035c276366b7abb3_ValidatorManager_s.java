 package org.eclipse.dltk.validators.internal.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.validators.core.IValidatorType;
 
 public class ValidatorManager {
 
 	private final static String LANGUAGE_EXTPOINT = ValidatorsCore.PLUGIN_ID
 			+ ".validator";
 
 	private final static String NATURE_ATTR = "nature";
 
 	// Contains list of validators for selected nature.
 	private static Map validators;
 	
 	private static Map idToValidatorType = null;
 	
 	public static IValidatorType getValidatorTypeFromID( String id ) {
 		if( idToValidatorType == null ) {
 			idToValidatorType = new HashMap();
 			try {
 				IValidatorType[] allValidatorTypes = getAllValidatorTypes();
 				for (int i = 0; i < allValidatorTypes.length; i++) {
 					idToValidatorType.put(allValidatorTypes[i].getID(), allValidatorTypes[i] );
 				}
 			} catch (CoreException e) {
 				idToValidatorType = null;
 				return null;
 			}
 		}
 		return (IValidatorType)idToValidatorType.get(id);
 	}
 
 	private static void initialize() {
 		if (validators != null) {
 			return;
 		}
 
 		validators = new HashMap(5);
 		IConfigurationElement[] cfg = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(LANGUAGE_EXTPOINT);
 
 		for (int i = 0; i < cfg.length; i++) {
 			String nature = cfg[i].getAttribute(NATURE_ATTR);
 			if (validators.get(nature) != null) {
 				List elements = (List)validators.get(nature);
 				elements.add(cfg[i]);
 				continue;
 			}
			List elements = new ArrayList();
			elements.add( cfg[i] );
			validators.put( nature, elements );
 		}
 	}
 	/**
 	 * Return merged with all elements with nature #
 	 * @param natureId
 	 * @return
 	 * @throws CoreException
 	 */
 	public static IValidatorType[] getValidators(String natureId)
 			throws CoreException {
 		initialize();
 
 		List results = new ArrayList();
 		processNature(natureId, results);
 		// Add from # nature.
 		processNature( "#", results );
 		return (IValidatorType[])results.toArray(new IValidatorType[results.size()]);
 	}
 	private static void processNature(String natureId, List results)
 			throws CoreException {
 		Object ext = validators.get(natureId);
 		
 		if (ext != null) {
 			if( ext instanceof IValidatorType[]) {
 				IValidatorType[] b = (IValidatorType[])ext;
 				for (int i = 0; i < b.length; i++) {
 					if( !results.contains(b[i])) {
 						results.add(b[i]);
 					}
 				}
 			}
 			else if ( ext instanceof List ) {
 				List elements = (List)ext;
 				IValidatorType[] result = new IValidatorType[elements.size()];
 				for( int i = 0; i < elements.size(); ++i ) {
 					Object e = elements.get(i);
 					if( e instanceof IValidatorType ) {
 						result[i] = (IValidatorType)e;
 					}
 					else {
 						IConfigurationElement cfg = (IConfigurationElement) e;
 						IValidatorType builder = (IValidatorType) cfg
 								.createExecutableExtension("class");
 						result[i] = builder;
 					}
 				}
 				validators.put(natureId, result) ;
 				for (int i = 0; i < result.length; i++) {
 					if( !results.contains(result[i])) {
 						results.add(result[i]);
 					}
 				}
 			}
 		}
 	}
 
 	public static IValidatorType[] getAllValidatorTypes() throws CoreException {
 		
 		initialize();
 		List result = new ArrayList();
 		Iterator iterator = validators.keySet().iterator();
 		while( iterator.hasNext() ) {
 			String nature = (String)iterator.next();
 			IValidatorType[] b = getValidators(nature);
 			for( int i = 0; i < b.length; ++i ) {
 				result.add(b[i]);
 			}
 		}
 		return (IValidatorType[])result.toArray(new IValidatorType[result.size()]);
 	}
 }
