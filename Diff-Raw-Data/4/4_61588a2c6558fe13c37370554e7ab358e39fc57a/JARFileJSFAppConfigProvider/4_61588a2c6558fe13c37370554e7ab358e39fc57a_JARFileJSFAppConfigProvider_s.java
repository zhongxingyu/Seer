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
 package org.eclipse.jst.jsf.core.jsfappconfig;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collections;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.util.FacesConfigResourceFactory;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * JARFileJSFAppConfigProvider provides the root element of an application
  * configuration model by loading the model from a /META-INF/faces-config.xml
  * entry in a JAR file.
  * 
  * <p><b>Provisional API - subject to change</b></p>
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
 	 * Flag to track if load error has been logged at least once.
 	 */
 	protected boolean loadErrorLogged = false;
 	
 	/**
 	 * Creates an instance, storing the passed IProject instance and file name
 	 * String to be used for subsequent processing.
 	 * 
 	 * @param filename Name of a JAR file that contains a
 	 * /META-INF/faces-config.xml entry.
 	 */
 	public JARFileJSFAppConfigProvider(String filename) {
 		this.filename = filename;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigProvider#getFacesConfigModel()
 	 */
 	public FacesConfigType getFacesConfigModel() {
 	    // TODO: should this job be pushed into the model?
 		if (facesConfig == null && filename != null) 
 		{
 			facesConfig = getFacesConfig();
 			
 			if (facesConfig != null)
 			{
 				jsfAppConfigLocater.getJSFAppConfigManager().addFacesConfigChangeAdapter(facesConfig);
 			}
 		}
 		return facesConfig;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigProvider#releaseFacesConfigModel()
 	 */
 	public void releaseFacesConfigModel() {
 		jsfAppConfigLocater.getJSFAppConfigManager().removeFacesConfigChangeAdapter(facesConfig);
 	}
 
 	/**
 	 * Called to log a load error; load error will be logged once only per
 	 * instance, per VM session.
 	 * 
 	 * @param ex Throwable instance to be logged.
 	 */
 	protected void logLoadError(Throwable ex) {
 		if (!loadErrorLogged) {
 			JSFCorePlugin.log(
 					IStatus.ERROR,
 					NLS.bind(Messages.JARFileJSFAppConfigProvider_ErrorLoadingModel, JARFILE_URI_PREFIX + filename + FACES_CONFIG_IN_JAR_SUFFIX),
 					ex);
 			loadErrorLogged = true;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object otherObject) {
 		boolean equals = false;
 		if (otherObject instanceof JARFileJSFAppConfigProvider) {
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
 
 	private FacesConfigType getFacesConfig()
 	{
 		JarFile 		jarFile = null;
 		File    		tempFile = null;
 		OutputStream	tempFileStream = null;
 
 		try
 		{
 			jarFile = new JarFile(filename);
 			ZipEntry entry = jarFile.getEntry("META-INF/faces-config.xml");
 
 			if (entry != null)
 			{
 				InputStream stream = jarFile.getInputStream(entry);
 	
 				tempFile = File.createTempFile("tempfile", ".xml");
 				tempFileStream = new FileOutputStream(tempFile);
 	
 				int read = 0;
 				byte[]  buffer = new byte[4096];
 	
 				while ((read = stream.read(buffer)) != -1)
 				{
 					tempFileStream.write(buffer, 0, read);
 				}
 	
 				tempFileStream.close();
 				tempFileStream = null;
 	
 				FacesConfigResourceFactory factory = FacesConfigResourceFactory.createResourceFactoryForJar();
 				//FacesConfigResourceFactory.register(tempFile.toURI().toString());
 				Resource resource = factory.createResource(URI.createFileURI(tempFile.getAbsolutePath()));
 				
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
 					logLoadError(ise);
 				} catch(IOException ioe) {
 					//log error
 					logLoadError(ioe);
 				}
 			}
 
 			return facesConfig;
 		}
 		catch (IOException ioe)
 		{
 			logLoadError(ioe);
 			return null;
 		}
 		finally
 		{
 			if (jarFile != null)
 			{
 				try
 				{
 					jarFile.close();
 				}
 				catch (IOException ioe)
 				{
 					logLoadError(ioe);
 				}
 			}
 
 			if (tempFileStream != null)
 			{
 				try
 				{
 					tempFileStream.close();
 				}
 				catch(IOException ioe)
 				{
 					logLoadError(ioe);
 				}
 			}
 
 			if (tempFile != null && tempFile.exists())
 			{
 				if (!tempFile.delete())
 				{
 					tempFile.deleteOnExit();
 				}
 			}
 		}
 	}
 }
