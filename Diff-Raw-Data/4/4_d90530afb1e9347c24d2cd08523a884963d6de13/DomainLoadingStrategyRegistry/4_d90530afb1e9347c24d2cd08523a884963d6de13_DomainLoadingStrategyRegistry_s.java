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
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 
 
 /**
  * Registry of strategies used to load domains of metadata.
  * Uses the <code>org.eclipse.jst.jsf.common.domainLoadingStrategies</code> ext-pt to load.  
  * 
  *@see <code>org.eclipse.jst.jsf.common.domainLoadingStrategies</code> ext-pt 
  */
 public class DomainLoadingStrategyRegistry{
 	private static DomainLoadingStrategyRegistry INSTANCE;
 	
 	private HashMap/*<String, DomainLoadingStrategyDescriptorImpl>*/ domainLoadingStrategyDescriptors;
 //	private HashMap/*<String, IDomainLoadingStrategy>*/ domainLoadingStrategies;
 	
 	public static final String TAGLIB_DOMAIN ="TagLibraryDomain"; //this does *not* belong here.  FIX ME
 //	public static final String TAGLIB_DOMAIN_SOURCE_HANDLER_ID = TAGLIB_DOMAIN + "SourceHandler";
 //	public static final String TAGLIB_DOMAIN_TRANSLATOR = "com.foo.translators."+TAGLIB_DOMAIN + "Translator";
 
 	private static final String EXTENSION_POINT_ID = "domainLoadingStrategies";
 
 	private DomainLoadingStrategyRegistry(){
 		init();
 	}
 	
 	/**
 	 * @return singelton instance of the DomainLoadingStrategyRegistry
 	 */
 	public synchronized static DomainLoadingStrategyRegistry getInstance() {
 		if (INSTANCE == null){
 			INSTANCE = new DomainLoadingStrategyRegistry();
 		}
 		return INSTANCE;
 	}
 	
 	/**
 	 * Loads registry with descriptors from the domainLoadingStrategies ext-pt.    
 	 */
 	synchronized void  init(){
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 		IExtensionPoint point = extensionRegistry.getExtensionPoint(JSFCommonPlugin.PLUGIN_ID, EXTENSION_POINT_ID );
 		if (point != null) {
 			IConfigurationElement[] elements = point.getConfigurationElements();
 			for (int i = 0; i < elements.length; i++) {
 				IConfigurationElement element = elements[i];
 				DomainLoadingStrategyDescriptorImpl dls = new DomainLoadingStrategyDescriptorImpl(element);
 				addDomainLoadingStrategyDescriptor(dls);
 			}
 		}
 	}
 
 	protected void addDomainLoadingStrategyDescriptor(DomainLoadingStrategyDescriptorImpl strategy){
 		getDescriptors().put(strategy.getDomain(), strategy);
 	}
 	
 	/**
 	 * @param domain
 	 * @return an instance of an <code>IDomainLoadingStrategy</code> for the given domain
 	 */
 	public IDomainLoadingStrategy getLoadingStrategy(String domain){
 		DomainLoadingStrategyDescriptorImpl strategy = (DomainLoadingStrategyDescriptorImpl)getDescriptors().get(domain);
 		if (strategy == null){
 			return createDefaultLoadingStrategy();
 		}
		else {			
			return createLoadingStrategy(domain);
		}
 	}
 
 	
 	/**
 	 * @return strategy that will only use standard metadata files
 	 */
 	private IDomainLoadingStrategy createDefaultLoadingStrategy() {
 		return new DomainLoadingStrategy(null);
 	}
 
 	private IDomainLoadingStrategy createLoadingStrategy(String domain){
 //		System.out.println("createLoadingStrategy"); //debug
 		return ((DomainLoadingStrategyDescriptorImpl)getDescriptors().get(domain)).newInstance();			
 	}
 	
 	private Map/*<String, DomainLoadingStrategyDescriptorImpl>*/ getDescriptors(){
 		if (domainLoadingStrategyDescriptors == null){
 			domainLoadingStrategyDescriptors = new HashMap/*<String, DomainLoadingStrategyDescriptorImpl>*/();			
 		}
 		return domainLoadingStrategyDescriptors;
 	}
 
 	/**
 	 * Implementation of a DomainLoadingStrategy descriptor that is responsible for creating instances of the IDomainLoadingStrategy
 	 */
 	private class DomainLoadingStrategyDescriptorImpl {
 		String domain;
 		String loadingStrategyClassName;
 		String bundleId;
 		Class strategy;
 		IConfigurationElement element;
 		
 		DomainLoadingStrategyDescriptorImpl(IConfigurationElement element){
 			this.element = element;
 			this.init();
 		}
 
 		private void init() {
 			domain = element.getAttribute("domainId");
 			bundleId = element.getContributor().getName();
 			loadingStrategyClassName = element.getAttribute("domainLoadingStrategy");			
 		}
 
 		public String getDomain() {	
 			return domain;
 		}
 		
 		/**
 		 * @return new instance of IDomainLoadingStrategy
 		 */
 		public IDomainLoadingStrategy newInstance(){
 			//TODO err handling
 			try {
 //				Class[] parameterTypes = new Class[]{MetaDataModelManager.class, String.class};
 //				Object[] initargs = new Object[]{mdr, domain};
 				Class[] parameterTypes = new Class[]{String.class};
 				Object[] initargs = new Object[]{domain};
 				Object loader = this.getLoadingStrategy().getConstructor(parameterTypes).newInstance(initargs);
 				if (loader instanceof IDomainLoadingStrategy)
 					return (IDomainLoadingStrategy)loader;
 			} catch (InstantiationException e) {
 				// TODO log
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO log
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		private Class getLoadingStrategy() {		
 			if (strategy == null){
 				strategy = JSFCommonPlugin.loadClass(loadingStrategyClassName, bundleId);
 			}
 			return strategy;
 		}
 		
 	}
 
 }
