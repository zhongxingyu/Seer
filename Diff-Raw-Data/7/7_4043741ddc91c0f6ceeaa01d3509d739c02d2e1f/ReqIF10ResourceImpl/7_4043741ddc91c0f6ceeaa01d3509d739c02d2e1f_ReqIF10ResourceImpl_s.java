 /**
  * Copyright (c) 2013 itemis AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.reqif10.serialization;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.XMLSave;
 import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;
 import org.eclipse.rmf.internal.serialization.XMLPersistenceMappingSaveImpl;
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingResourceImpl;
 
 public class ReqIF10ResourceImpl extends XMLPersistenceMappingResourceImpl {
 
 	public ReqIF10ResourceImpl() {
 		super();
		initDefaultOptions();
 	}
 
 	public ReqIF10ResourceImpl(URI uri) {
 		super(uri);
		initDefaultOptions();
 	}
 
 	@Override
 	protected void init() {
 		super.init();
 		// enable id creation and maintenance
 		idToEObjectMap = new HashMap<String, EObject>();
 		eObjectToIDMap = new HashMap<EObject, String>();
 		Collection<EPackage> createIdForPackageSet = new HashSet<EPackage>();
 		createIdForPackageSet.add(ReqIF10Package.eINSTANCE);
 	}
 
 	@Override
 	public void initDefaultOptions() {
 		super.initDefaultOptions();
 		// ========= create options map===================
 		final XMLResource.XMLMap optionsMap = new XMLMapImpl();
 		optionsMap
 				.setIDAttributeName(ReqIF10Package.Literals.IDENTIFIABLE__IDENTIFIER
 						.getName().toUpperCase());
 
 		// ========= default save options ===================
 		Map<Object, Object> saveOptions = getDefaultSaveOptions();
 		Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
 		namespaceToPrefixMap.put(ReqIF10Package.eNS_URI, ""); //$NON-NLS-1$ 
 		namespaceToPrefixMap.put(XhtmlPackage.eNS_URI, "xhtml"); //$NON-NLS-1$ 
 		saveOptions.put(OPTION_NAMEPSACE_TO_PREFIX_MAP, namespaceToPrefixMap);
 		
 		// ========= default load options ===================
 		Map<Object, Object> loadOptions = getDefaultLoadOptions();
 		loadOptions.put(XMLResource.OPTION_XML_MAP, optionsMap);
 	}
 
 	/**
 	 * Return <code>true</code>.
 	 * 
 	 * @return <code>true</code>.
 	 * 
 	 * @see org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl#useUUIDs()
 	 */
 	@Override
 	protected boolean useUUIDs() {
 		return true;
 	}
 
 	/**
 	 * Return <code>false</code>.
 	 * 
 	 * @return <code>false</code>.
 	 * 
 	 * @see org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl#assignIDsWhileLoading()
 	 */
 	@Override
 	protected boolean assignIDsWhileLoading() {
 		return false;
 	}
 
 	/**
 	 * Sets the ID of the object. The default implementation will update the
 	 * {@link #eObjectToIDMap}. This behavior is override to set the ID in a
 	 * object's specific attribute to set the id in the
 	 * {@link Identifiable#setIdentifier(String)} and call the super method.
 	 * 
 	 * @param eObject
 	 *            : The object where the Id must be set.
 	 * @param id
 	 *            : The object's Id.
 	 * @see org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl#setID(org.eclipse.emf.ecore.EObject,
 	 *      java.lang.String)
 	 */
 	@Override
 	public void setID(final EObject eObject, final String id) {
 		final EAttribute idAttribute = eObject.eClass().getEIDAttribute();
 		if ((idAttribute != null) && (id != null)) {
 			eObject.eSet(idAttribute, id);
 		}
 		super.setID(eObject, id);
 	}
 
 	/**
 	 * Create a new XMLSave implementation, with our specific implementation to
 	 * avoid the save of the id of an {@link EObject} twice via the
 	 * {@link Identifiable#getIdentifier()} and the #idToEObjectMap.
 	 * 
 	 * @return The XMLSave created
 	 * 
 	 * @see org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl#createXMLSave()
 	 */
 	@Override
 	protected XMLSave createXMLSave() {
 		return new XMLPersistenceMappingSaveImpl(createXMLHelper()) {
 
 			@Override
 			protected void saveElementID(final EObject o) {
 				// As the identifier is an EStructuralFeture of the Identifiable
 				// class, so we ignore the save of the element id from the map.
 				this.saveFeatures(o);
 			}
 		};
 	}
 }
