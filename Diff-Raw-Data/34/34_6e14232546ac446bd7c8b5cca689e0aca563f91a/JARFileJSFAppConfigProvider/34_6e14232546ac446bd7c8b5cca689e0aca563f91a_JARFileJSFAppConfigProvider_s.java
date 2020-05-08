 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.internal.provisional.jsfappconfig;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.util.FacesConfigResourceFactory;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.internal.emf.resource.EMF2SAXRendererFactory;
 
 /**
  * JARFileJSFAppConfigProvider provides the root element of an application
  * configuration model by loading the model from a /META-INF/faces-config.xml
  * entry in a JAR file.
  * 
  * @author Ian Trimble - Oracle
  */
 public class JARFileJSFAppConfigProvider extends AbstractJSFAppConfigProvider {
 
 	/**
 	 * Prefix required to turn filename into a JAR URI.
 	 */
 	public static final String JARFILE_URI_PREFIX = "jar:file:///";
 
 	/**
 	 * Suffix required to turn filename into a JAR URI.
 	 */
 	public static final String FACES_CONFIG_IN_JAR_SUFFIX = "!/META-INF/faces-config.xml";
 
 	/**
 	 * Name of a JAR file that contains a /META-INF/faces-config.xml entry.
 	 */
 	protected String filename = null;
 
 	/**
 	 * Cached {@link FacesConfigType} instance.
 	 */
 	protected FacesConfigType facesConfig = null;
 
 	/**
 	 * Creates an instance, storing the passed IProject instance and file name
 	 * String to be used for subsequent processing.
 	 * 
 	 * @param project IProject instance who's classpath contains this JAR file.
 	 * @param filename Name of a JAR file that contains a
 	 * /META-INF/faces-config.xml entry.
 	 */
 	public JARFileJSFAppConfigProvider(String filename) {
 		this.filename = filename;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.internal.provisional.jsfappconfig.IJSFAppConfigProvider#getFacesConfigModel()
 	 */
 	public FacesConfigType getFacesConfigModel() {
 		if (facesConfig == null) {
 			if (filename != null) {
 				StringBuffer sb = new StringBuffer();
 				sb.append(JARFILE_URI_PREFIX);
 				sb.append(filename);
 				sb.append(FACES_CONFIG_IN_JAR_SUFFIX);
 				URI jarFileURI = URI.createURI(sb.toString());
 				FacesConfigResourceFactory resourceFactory = new FacesConfigResourceFactory(EMF2SAXRendererFactory.INSTANCE);
 				Resource resource = resourceFactory.createResource(jarFileURI);
 				try {
 					resource.load(Collections.EMPTY_MAP);
 					if (resource != null) {
 						EList resourceContents = resource.getContents();
 						if (resourceContents != null && resourceContents.size() > 0) {
 							facesConfig = (FacesConfigType)resourceContents.get(0);
 							if (facesConfig != null) {
 								jsfAppConfigLocater.getJSFAppConfigManager().addFacesConfigChangeAdapter(facesConfig);
 							}
 						}
 					}
 				} catch(IllegalStateException ise) {
 					//log error
					JSFCorePlugin.log(
							IStatus.ERROR,
							NLS.bind(Messages.JARFileJSFAppConfigProvider_ErrorLoadingModel, JARFILE_URI_PREFIX + filename + FACES_CONFIG_IN_JAR_SUFFIX),
							ise);
 				} catch(IOException ioe) {
 					//log error
					JSFCorePlugin.log(
							IStatus.ERROR,
							NLS.bind(Messages.JARFileJSFAppConfigProvider_ErrorLoadingModel, JARFILE_URI_PREFIX + filename + FACES_CONFIG_IN_JAR_SUFFIX),
							ioe);
 				}
 			}
 		}
 		return facesConfig;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.internal.provisional.jsfappconfig.IJSFAppConfigProvider#releaseFacesConfigModel()
 	 */
 	public void releaseFacesConfigModel() {
 		jsfAppConfigLocater.getJSFAppConfigManager().removeFacesConfigChangeAdapter(facesConfig);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object otherObject) {
 		boolean equals = false;
 		if (otherObject != null && otherObject instanceof JARFileJSFAppConfigProvider) {
 			String otherFilename = ((JARFileJSFAppConfigProvider)otherObject).filename;
 			if (filename != null) {
 				equals = filename.equals(otherFilename);
 			} else {
 				equals = otherFilename == null;
 			}
 		}
 		return equals;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		return filename != null ? filename.hashCode() : 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		StringBuffer sb = new StringBuffer("JARFileJSFAppConfigProvider["); //$NON-NLS-1$
 		if (filename != null) {
 			sb.append(filename);
 		} else {
 			sb.append("null"); //$NON-NLS-1$
 		}
 		sb.append("]"); //$NON-NLS-1$
 		return sb.toString();
 	}
 
 }
