 /*******************************************************************************
  * Copyright (c) 2014 itemis AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mark Broerkens (itemis AG) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.rmf.tests.serialization.model.nodes.serialization;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
import org.eclipse.rmf.serialization.IdAdapter;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingResourceImpl;
 import org.eclipse.rmf.tests.serialization.model.nodes.NodesPackage;
 
 public class NodesResourceImpl extends XMLPersistenceMappingResourceImpl {
 
 	public NodesResourceImpl() {
 		super();
 	}
 
 	public NodesResourceImpl(URI uri) {
 		super(uri);
 	}
 
 	@Override
 	protected void init() {
 		super.init();
 		// enable id creation and maintenance
 		idToEObjectMap = new HashMap<String, EObject>();
 		eObjectToIDMap = new HashMap<EObject, String>();
 		Collection<EPackage> createIdForPackageSet = new HashSet<EPackage>();
 		createIdForPackageSet.add(NodesPackage.eINSTANCE);
		eAdapters().add(new IdAdapter(idToEObjectMap, eObjectToIDMap, createIdForPackageSet));
 	}
 
 	@Override
 	public void initDefaultOptions() {
 		super.initDefaultOptions();
 		// ========= default save options ===================
 		Map<Object, Object> saveOptions = getDefaultSaveOptions();
 		Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
 		namespaceToPrefixMap.put(NodesPackage.eNS_URI, ""); //$NON-NLS-1$ 
 		saveOptions.put(OPTION_NAMEPSACE_TO_PREFIX_MAP, namespaceToPrefixMap);
 	}
 }
