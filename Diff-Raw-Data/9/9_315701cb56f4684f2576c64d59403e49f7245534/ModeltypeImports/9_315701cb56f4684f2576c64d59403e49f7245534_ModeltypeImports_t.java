 /**
  * Copyright (c) 2008 Borland Software Corp.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Alexander Shatalin (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.migration;
 
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.eclipse.emf.ecore.EPackage;
 
 class ModeltypeImports extends AbstractImportsManager {
 
	private static final String DELIMITER = "_";

 	private static final String DEFAULT_ALIAS = "metaModelRef";
 
 	private boolean injectUnusedImports;
 
 	private Set<String> modeltypeAliases = new HashSet<String>();
 
 	private Map<String, String> nsURI2Aliases = new LinkedHashMap<String, String>();
 
 	private Set<String> usedNsURIs = new HashSet<String>();
 
 	ModeltypeImports(StringBuilder stringBuilder, boolean injectUnusedImports) {
 		super(stringBuilder);
 		this.injectUnusedImports = injectUnusedImports;
 	}
 	
 	ModeltypeImports(int placeholder, boolean injectUnusedImports) {
 		super(placeholder);
 		this.injectUnusedImports = injectUnusedImports;
 	}
 
 	void setInjectUnusedImports(boolean injectUnusedImports) {
 		this.injectUnusedImports = injectUnusedImports;
 	}
 
 	Map<String, String> getModelTypes() {
 		Map<String, String> result = new LinkedHashMap<String, String>();
 		for (Entry<String, String> entry : nsURI2Aliases.entrySet()) {
 			if (!injectUnusedImports && !usedNsURIs.contains(entry.getKey())) {
 				// Skipping the entry if it was not used
 				continue;
 			}
 			result.put(entry.getKey(), entry.getValue());
 		}
 		return result;
 	}
 
 	void registerModeltype(String nsURI) {
 		assert nsURI.length() > 0;
 		String modeltypeAlias = getUniqueModeltypeAlias(nsURI);
 		nsURI2Aliases.put(nsURI, modeltypeAlias);
 	}
 
 	String getModeltypeAlias(EPackage ePackage) {
 		String nsURI = ePackage.getNsURI();
 		// Now we are registering models even if they was not imported before..
 		if (!nsURI2Aliases.containsKey(nsURI)) {
 			registerModeltype(nsURI);
 		}
 		usedNsURIs.add(nsURI);
 		return nsURI2Aliases.get(nsURI);
 	}
 
 	private String getUniqueModeltypeAlias(String nsURI) {
 		char[] charArray = nsURI.toCharArray();
 		int i = charArray.length - 1;
 		for (; i > -1 && Character.isJavaIdentifierPart(charArray[i]); i--) {
 		}
 		String aliasProposal;
 		if (i < charArray.length - 1) {
 			char[] alias = new char[charArray.length - 1 - i];
 			System.arraycopy(charArray, i + 1, alias, 0, charArray.length - 1 - i);
 			alias[0] = Character.toLowerCase(alias[0]);
 			aliasProposal = new String(alias);
			if (!Character.isJavaIdentifierStart(alias[0])) {
				aliasProposal = DELIMITER + aliasProposal;
			}
 		} else {
 			aliasProposal = DEFAULT_ALIAS;
 		}
 		String actualAlias = aliasProposal;
 		for (int suffix = 1; modeltypeAliases.contains(actualAlias); suffix++) {
			actualAlias = aliasProposal + DELIMITER + suffix;
 		}
 		modeltypeAliases.add(actualAlias);
 		return actualAlias;
 	}
 
 }
