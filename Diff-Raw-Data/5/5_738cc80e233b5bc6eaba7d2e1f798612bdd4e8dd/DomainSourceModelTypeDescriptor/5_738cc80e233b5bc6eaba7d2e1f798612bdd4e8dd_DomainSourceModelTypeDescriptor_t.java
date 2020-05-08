 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.internal;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 
 /**
  * Implementation of a {@link IDomainSourceModelType} descriptor.   
  * Responsible for producing instances of {@link IDomainSourceModelType}.  
  * Also responsible for creating the {@link IMetaDataTranslator} descriptors from the 
  * <code>com.eclipse.jst.jsf.common.domainSourceModelTypeTranslators</code> ext-pt
  */
 public class DomainSourceModelTypeDescriptor {
 	private static final String TRANSLATORS_EXTENSION_POINT_ID = "domainSourceModelTypeTranslators";
 	private static final String STANDARD_FILE_NULL_TRANSLATOR = "org.eclipse.jst.jsf.common.metadata.internal.StandardAnnotationFilesTranslator";
 	private String domain;
 	private String domainSourceModelTypeId;
 	private String locatorClassName;
 	private Set translatorDescriptors;
 	private String bundleId;
 	private int ordinal;
 	
 	/**
 	 * Constructor
 	 * @param domain
 	 * @param domainSourceModelTypeId
 	 * @param locatorClassName
 	 * @param bundleId
 	 * @param ordinal
 	 */
 	public DomainSourceModelTypeDescriptor(String domain, String domainSourceModelTypeId, String locatorClassName, String bundleId, int ordinal){
 		this.domain = domain;
 		this.locatorClassName = locatorClassName;
 		this.domainSourceModelTypeId = domainSourceModelTypeId;
 		this.bundleId = bundleId;
 		this.ordinal = ordinal;
 		init();
 	}
 
 	private synchronized void init() {
 		translatorDescriptors = new HashSet();
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 		IExtensionPoint point = extensionRegistry.getExtensionPoint(JSFCommonPlugin.PLUGIN_ID, TRANSLATORS_EXTENSION_POINT_ID );
 		if (point != null) {
 			IConfigurationElement[] elements = point.getConfigurationElements();
 			for (int i = 0; i < elements.length; i++) {
 				IConfigurationElement element = elements[i];
 				String srcHdlrId = element.getAttribute("domainSourceModelTypeId");
 				if (srcHdlrId.equals(domainSourceModelTypeId))
 					addTranslatorDescriptor(element);
 			}
 		}
 	}
 	
 	private void addTranslatorDescriptor(IConfigurationElement element) {
 		String translator = element.getAttribute("class");
 		DomainSourceModelTranslatorDescriptor d = new DomainSourceModelTranslatorDescriptor(translator, element.getContributor().getName());
 		getTranslatorDescriptors().add(d);
 	}
 
 	private Set getTranslatorDescriptors(){
 		if (translatorDescriptors == null){
 			translatorDescriptors = new HashSet();
 		}
 		return translatorDescriptors;
 	}
 
 	/**
 	 * @return domain
 	 */
 	public String getDomain() { 
 		return domain;
 	}
 	 
 	/**
 	 * @return new instance of {@link IDomainSourceModelType} 
 	 */
 	public IDomainSourceModelType newInstance(){		
 
 		return new DomainSourceModelTypeImpl();
 	}
 	
 	class DomainSourceModelTypeImpl implements IDomainSourceModelType{
 
 		private Set translators;
 		private IMetaDataLocator locator;
 
 		DomainSourceModelTypeImpl(){
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jst.jsf.common.metadata.internal.IDomainSourceModelType#getDomain()
 		 */
 		public String getDomain() {
			return domain;
 		}
 		
 		/**
 		 * @return value of ordinal defined by the ext-pt used for ordering source types for a domain
 		 */
 		public int getOrdinal(){
 			return ordinal;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jst.jsf.common.metadata.internal.IDomainSourceModelType#getLocator()
 		 */
 		public IMetaDataLocator getLocator() {
 			if (locator == null){
 				Class klass = JSFCommonPlugin.loadClass(locatorClassName, bundleId);
 				try {
 					locator = (IMetaDataLocator)klass.newInstance();
 				} catch (InstantiationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 								
 			return locator;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jst.jsf.common.metadata.internal.IDomainSourceModelType#getTranslators()
 		 */
 		public Set getTranslators() {
 			if (translators == null){				
 				translators = createTranslatorInstances();
 			}
 			return translators;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString(){
			StringBuffer buf = new StringBuffer("DomainSourceModelTypeImpl");
 			buf.append("(domain = ");
 			buf.append(getDomain());
 			buf.append(", locator = ");
 			buf.append(getLocator());
 			buf.append(")");
 			return buf.toString();
 		}
 		
 		private Set createTranslatorInstances() {
 			translators = new HashSet/*<IMetaDataTranslator>*/();
 			if (translatorDescriptors.size() == 0){
 				//add Null Translator for now....
 				//we could/should raise exception
 				Class klass = JSFCommonPlugin.loadClass(STANDARD_FILE_NULL_TRANSLATOR, JSFCommonPlugin.PLUGIN_ID);
 				try {
 					translators.add((IMetaDataTranslator)klass.newInstance());
 					return translators;
 				} catch (InstantiationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 			
 			Iterator/*<DomainSourceModelTranslatorDescriptor>*/it = translatorDescriptors.iterator();
 			while (it.hasNext()){
 				DomainSourceModelTranslatorDescriptor d = (DomainSourceModelTranslatorDescriptor)it.next();
 				Class klass = JSFCommonPlugin.loadClass(d.translator, d.bundleId);
 				try {
 					translators.add((IMetaDataTranslator)klass.newInstance());
 				} catch (InstantiationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			return translators;
 		}
 
 	}
 	
 	/**
 	 * Implements a descriptor for DomainSourceModelTranslators
 	 */
 	class DomainSourceModelTranslatorDescriptor {
 
 		private String translator;
 		private String bundleId;
 
 		public DomainSourceModelTranslatorDescriptor(String translator, String bundleId) {
 			this.translator = translator;
 			this.bundleId = bundleId;
 		}
 		
 	}
 
 }
