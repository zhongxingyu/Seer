 package org.eclipse.dltk.internal.core.builder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 
 public class ScriptBuilderManager {
 
 	private final static String LANGUAGE_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".builder";
 
 	private final static String NATURE_ATTR = "nature";
 
 	// Contains list of builders for selected nature.
 	private static Map builders;
 
 	private static void initialize() {
 		if (builders != null) {
 			return;
 		}
 
 		builders = new HashMap(5);
 		IConfigurationElement[] cfg = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(LANGUAGE_EXTPOINT);
 
 		for (int i = 0; i < cfg.length; i++) {
 			String nature = cfg[i].getAttribute(NATURE_ATTR);
 			if (builders.get(nature) != null) {
 				List elements = (List)builders.get(nature);
 				elements.add(cfg[i]);
 			}
			List elements = new ArrayList();
			elements.add( cfg[i] );
			builders.put(nature, elements);
 		}
 	}
 	/**
 	 * Return merged with all elements with nature #
 	 * @param natureId
 	 * @return
 	 * @throws CoreException
 	 */
 	public static IScriptBuilder[] getScriptBuilders(String natureId)
 			throws CoreException {
 		initialize();
 
 		List results = new ArrayList();
 		processNature(natureId, results);
 		// Add from # nature.
 		processNature( "#", results );
 		return (IScriptBuilder[])results.toArray(new IScriptBuilder[results.size()]);
 	}
 	private static void processNature(String natureId, List results)
 			throws CoreException {
 		Object ext = builders.get(natureId);
 		
 		if (ext != null) {
 			if( ext instanceof IScriptBuilder[]) {
 				IScriptBuilder[] b = (IScriptBuilder[])ext;
 				for (int i = 0; i < b.length; i++) {
 					if( !results.contains(b[i])) {
 						results.add(b[i]);
 					}
 				}
 			}
 			else if ( ext instanceof List ) {
 				List elements = (List)ext;
 				IScriptBuilder[] result = new IScriptBuilder[elements.size()];
 				for( int i = 0; i < elements.size(); ++i ) {
 					Object e = elements.get(i);
 					if( e instanceof IScriptBuilder ) {
 						result[i] = (IScriptBuilder)e;
 					}
 					else {
 						IConfigurationElement cfg = (IConfigurationElement) e;
 						IScriptBuilder builder = (IScriptBuilder) cfg
 								.createExecutableExtension("class");
 						result[i] = builder;
 					}
 				}
 				builders.put(natureId, result) ;
 				for (int i = 0; i < result.length; i++) {
 					if( !results.contains(result[i])) {
 						results.add(result[i]);
 					}
 				}
 			}
 		}
 	}
 
 	public static IScriptBuilder[] getAllScriptBuilders() throws CoreException {
 		
 		initialize();
 		List result = new ArrayList();
 		Iterator iterator = builders.keySet().iterator();
 		while( iterator.hasNext() ) {
 			String nature = (String)iterator.next();
 			IScriptBuilder[] b = getScriptBuilders(nature);
 			for( int i = 0; i < b.length; ++i ) {
 				result.add(b[i]);
 			}
 		}
 		return (IScriptBuilder[])result.toArray(new IScriptBuilder[result.size()]);
 	}
 }
