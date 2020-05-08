 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.match.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.compare.MatchPlugin;
 import org.eclipse.emf.compare.match.MatchModel;
 import org.eclipse.emf.compare.match.api.MatchEngine;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * Service facade for matching models
  * 
  * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public class MatchService {
 	// The shared instance
 	private static MatchService service;
 
 	private Map engines = new HashMap();
 
 	/**
 	 * The constructor
 	 */
 	public MatchService() {
 		service = this;
 		parseExtensionMetadata();
 	}
 
 	private void parseExtensionMetadata() {
 		IExtension[] extensions = Platform.getExtensionRegistry()
 				.getExtensionPoint(MatchPlugin.PLUGIN_ID, "engine")
 				.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IConfigurationElement[] configElements = extensions[i]
 					.getConfigurationElements();
 			for (int j = 0; j < configElements.length; j++) {
 				EngineDescriptor desc = parseEngine(configElements[j]);
 				storeEngineDescriptor(desc);
 			}
 		}
 
 	}
 
 	private void storeEngineDescriptor(EngineDescriptor desc) {
 		if (!engines.containsKey(desc.getFileExtension())) {
 			engines.put(desc.getFileExtension(), new ArrayList());
 		}
 		List set = (List) engines.get(desc.getFileExtension());
 		set.add(desc);
 	}
 
 	private EngineDescriptor getBestDescriptor(String extension) {
 
 		if (engines.containsKey(extension)) {
 			return getHighestDescriptor((List) (engines.get(extension)));
 		}
 		if (engines.containsKey(ALL_EXTENSIONS)) {
 			return getHighestDescriptor((List) (engines.get(ALL_EXTENSIONS)));
 		}
 		return null;
 	}
 
 	private EngineDescriptor getHighestDescriptor(List set) {
 		Collections.sort(set, Collections.reverseOrder());
 		if (set.size() > 0)
 			return (EngineDescriptor) set.get(0);
 		return null;
 	}
 
 	private static final String TAG_ENGINE = "engine";
 
 	private EngineDescriptor parseEngine(IConfigurationElement configElements) {
 		if (!configElements.getName().equals(TAG_ENGINE))
 			return null;
 		EngineDescriptor desc = new EngineDescriptor(configElements);
 		return desc;
 	}
 
 	/**
 	 * Return the singleton instance
 	 * 
 	 * @return the singleton instance
 	 */
 	public static MatchService getInstance() {
 		if (service == null)
 			service = new MatchService();
 		return service;
 	}
 
 	/**
 	 * Match two models and return the coresponding matching model
 	 * 
 	 * @param leftRoot :
 	 *            left model
 	 * @param rightRoot :
 	 *            right model
 	 * @param monitor
 	 * @return matching model
 	 * @throws InterruptedException
 	 */
 	public MatchModel doMatch(EObject leftRoot, EObject rightRoot,
 			IProgressMonitor monitor) throws InterruptedException {
 		MatchModel result = null;
 		String extension = "ecore"; //$NON-NLS-1$
 		if (leftRoot.eResource().getURI() != null)
 			leftRoot.eResource().getURI().fileExtension();
 		if (extension == null && rightRoot.eResource() != null)
 			extension = rightRoot.eResource().getURI().fileExtension();
 		EngineDescriptor desc = getBestDescriptor(extension);
 		MatchEngine currentEngine = desc.getEngineInstance();
 		result = currentEngine.modelMatch(leftRoot, rightRoot, monitor);
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param leftRoot :
 	 *            left model
 	 * @param rightRoot :
 	 *            right model
 	 * @param ancestor :
 	 *            common ancestor model
 	 * @return the match model
 	 */
 	public MatchModel doMatch(EObject leftRoot, EObject rightRoot,
 			EObject ancestor) {
 		MatchModel result = null;
 		// TODOCBR code 3 Way match
 		return result;
 	}
 
 	/**
 	 * Return the best match engine from a file extension
 	 * 
 	 * @param extension :
 	 *            file extension
 	 * @return best match engine
 	 */
 	public MatchEngine getBestMatchEngine(String extension) {
 		EngineDescriptor desc = getBestDescriptor(extension);
 		return desc.getEngineInstance();
 	}
 
 	private static final String ALL_EXTENSIONS = "*";
 }
