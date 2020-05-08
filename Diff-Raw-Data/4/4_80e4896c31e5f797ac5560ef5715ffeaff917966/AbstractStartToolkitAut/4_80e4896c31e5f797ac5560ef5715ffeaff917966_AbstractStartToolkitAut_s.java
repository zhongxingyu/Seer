 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.autagent.commands;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jubula.autagent.AutStarter;
 import org.eclipse.jubula.autagent.monitoring.MonitoringDataStore;
 import org.eclipse.jubula.autagent.monitoring.MonitoringUtil;
 import org.eclipse.jubula.communication.message.StartAUTServerStateMessage;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.AutEnvironmentConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.eclipse.jubula.tools.utils.ZipUtil;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 10, 2007
  *
  */
 public abstract class AbstractStartToolkitAut implements IStartAut {
     
     /** the logger */
     private static Logger log = 
         LoggerFactory.getLogger(AbstractStartToolkitAut.class);
 
     /**
      * the message to send back if the command for starting the AUTServer could
      * not created
      */
     private StartAUTServerStateMessage m_errorMessage;
 
     /** true if executable file and -javaagent are set */
     private boolean m_isAgentSet = false;
 
     /**
      *
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public StartAUTServerStateMessage startAut(Map parameters)
         throws IOException {
         StartAUTServerStateMessage envCheckMsg = validateEnvironment();
         if (envCheckMsg == null) {
             if (!MonitoringUtil.checkForDuplicateAutID(String.valueOf(
                     parameters.get(AutConfigConstants.AUT_ID)))) {
                 MonitoringDataStore cm = MonitoringDataStore.getInstance();
                 cm.putConfigMap((String)parameters.get(
                       AutConfigConstants.AUT_ID), parameters);
             }
             File workingDir = getWorkingDir(parameters);
             String java = createBaseCmd(parameters);
             String[] cmdArray = createCmdArray(java, parameters);
             String[] envArray = createEnvArray(parameters, m_isAgentSet);
             // if no environment variables set
             if ((envArray == null) && log.isInfoEnabled()) {
                 log.info("envArray: NULL"); //$NON-NLS-1$
             }
             if (log.isInfoEnabled()) {
                 StringBuffer logMessage = new StringBuffer();
                 for (int i = 0; i < cmdArray.length; i++) {
                     logMessage.append(cmdArray[i]
                         + IStartAut.WHITESPACE_DELIMITER);
                 }
                 log.info("starting AUT with command: " //$NON-NLS-1$
                     + logMessage.toString());
             }
             return executeCommand(cmdArray, envArray, workingDir);
         }
         return envCheckMsg;
     }
 
     /**
      * Validates the runtime environment. This method should be overridden
      * in subclasses which need a specific environment.
      * @return null if the environment is OK or a message with a specific
      * error code.
      */
     protected StartAUTServerStateMessage validateEnvironment() {
         return null;
     }
 
     /**
      *
      * @param parameters startup parameters for the AUT.
      * @return the working directory for the AUT, or <code>null</code> if no
      *         working directory was defined.
      */
     protected File getWorkingDir(Map parameters) {
         String autWorkDir = 
             (String)parameters.get(AutConfigConstants.WORKING_DIR);
         if (autWorkDir == null) {
             autWorkDir = StringConstants.EMPTY;
         }
         File workingDir = new File(autWorkDir);
         if (!workingDir.isDirectory() || !workingDir.exists()) {
             if (log.isInfoEnabled()) {
                 log.info("Working dir: invalid"); //$NON-NLS-1$
             }
             workingDir = null;
         }
 
         return workingDir;
     }
 
     /**
      * Creates the environment variables for starting the AUTServer.
      * @param parameters startup parameters for the AUT.
      * @param isAgentSet true if executable file and agent are set.
      * @return the environment settings as array.
      */
     protected String[] createEnvArray(Map parameters, boolean isAgentSet) {
         m_isAgentSet = isAgentSet;
         final String environment =
             (String)parameters.get(AutConfigConstants.ENVIRONMENT);
         final boolean generate = ((Boolean)parameters.get(
                 AutConfigConstants.NAME_TECHNICAL_COMPONENTS)).booleanValue();
         
         Properties oldProp = EnvironmentUtils.getProcessEnvironment();
         String[] newEnvArray = null;
         
         if (generate) {
             Properties generateProperty = new Properties();
             generateProperty.setProperty(
                         AutEnvironmentConstants.GENERATE_COMPONENT_NAMES, 
                     String.valueOf(generate));
             oldProp = EnvironmentUtils
                     .setEnvironment(oldProp, generateProperty);
             newEnvArray = EnvironmentUtils.propToStrArray(
                     oldProp, IStartAut.PROPERTY_DELIMITER);
         }
         
         if ((environment != null) && (environment.trim().length() != 0)) {
             String[] envArray = EnvironmentUtils.strToStrArray(environment, "\r\n"); //$NON-NLS-1$
             Properties newProp = EnvironmentUtils.strArrayToProp(
                     envArray, IStartAut.PROPERTY_DELIMITER);
             newProp = EnvironmentUtils.setEnvironment(oldProp, newProp);
             newEnvArray = EnvironmentUtils.propToStrArray(
                     newProp, IStartAut.PROPERTY_DELIMITER);
         }
         
         return newEnvArray;
     }
 
     /**
      *
      * @param parameters startup parameters for the AUT.
      * @return a <code>String</code> that represents a
      * call to an executable. Ex. "java" or "/opt/java1.6/java".
      */
     protected abstract String createBaseCmd(Map parameters) throws IOException;
 
     /**
      *
      * @param baseCmd The base command to execute. For example, "java".
      * @param parameters startup parameters for the AUT.
      * @return an <code>Array</code> of <code>String</code>s representing
      *         a command line.
      */
     protected abstract String [] createCmdArray(String baseCmd, Map parameters);
 
     /**
      * Executes the given command in the given environment with the
      * given working directory.
      * @param cmdArray The command line to execute.
      * @param envArray The execution environment.
      * @param workingDir The working directory.
      * @return a <code>StartAutServerStateMessage</code> which either describes an error
      * condition or just tells the originator that the AUT was started correctly.
      */
     protected StartAUTServerStateMessage executeCommand(
         String [] cmdArray, String [] envArray, File workingDir)
         throws IOException {
 
         final AutStarter autStarter = AutStarter.getInstance();
         Process process = Runtime.getRuntime().exec(cmdArray, envArray,
             workingDir);
         if (isErrorMessage()) {
             System.out.println("AbstractStartToolkitAut - executeCommand: " //$NON-NLS-1$
                     + getErrorMessage());
             return getErrorMessage();
         }
         // give the process to AutStarter to watch
         if (!autStarter.watchAutServer(process, m_isAgentSet)) {
             process.destroy(); // new AUTServer could not be watched
             return createBusyMessage();
         }
         return new StartAUTServerStateMessage(StartAUTServerStateMessage.OK);
     }
 
     /**
      * Internal: generate a return message with the information about the problem. This
      * is used in other methods to propagate errors.
      *
      * @param errorMessage The errorMessage to store.
      */
     protected void setErrorMessage(StartAUTServerStateMessage errorMessage) {
         m_errorMessage = errorMessage;
     }
 
     /**
      * Internal: get a return message with the information about a problem. This
      * is used in other methods to propagate errors.
      *
      * @return Returns the errorMessage.
      */
     protected StartAUTServerStateMessage getErrorMessage() {
         if (m_errorMessage == null) {
             m_errorMessage = new StartAUTServerStateMessage(
                     StartAUTServerStateMessage.ERROR, "Unexpected error, no detail available."); //$NON-NLS-1$
         }
         return m_errorMessage;
     }
 
     /**
      * Internal: checks whether there is currently an error message.
      *
      * @return <code>true</code> if an error has occurred and there is an
      *         error message available. Otherwise <code>false</code>.
      */
     protected boolean isErrorMessage() {
         return m_errorMessage != null;
     }
 
     /**
      * Creates a <code>StartAUTServerStateMessage</code> with an
      * <code>ERROR</code> state and a description that the server is already running.
      * This message will eventually be returned be <code>execute()</code>.
      *
      * @return a new <code>StartAUTServerStateMessage</code>
      */
     protected StartAUTServerStateMessage createBusyMessage() {
         return new StartAUTServerStateMessage(StartAUTServerStateMessage.ERROR,
             "AUTServer is already running"); //$NON-NLS-1$
     }
 
     /**
      * 
      * @param bundleId The ID of the bundle to search for classpath entries.
      * @return classpath entries contained within the bundle with the given ID.
      *         If
      *         <ul> 
      *           <li>the bundle cannot be resolved to a file, or</li> 
      *           <li>the bundle is not a JAR file and the bundle's directory contains no JAR files</li>
      *         </ul>
      *         an empty array will be returned.
      */
     public static String[] getClasspathEntriesForBundleId(String bundleId) {
         Bundle bundle = Platform.getBundle(bundleId);
         
         if (bundle == null) {
             log.error("No bundle found for ID '" + bundleId + "'."); //$NON-NLS-1$//$NON-NLS-2$
             return new String[0];
         }
         
         List<String> classpathEntries = new ArrayList<String>();
         try {
             File bundleFile = FileLocator.getBundleFile(bundle);
             if (bundleFile.isFile()) {
                 // bundle file is not a directory, so we assume it's a JAR file
                 classpathEntries.add(bundleFile.getAbsolutePath());
 
                 // since the classloader cannot handle nested JARs, we need to extract
                 // all known nested JARs and add them to the classpath
                 try {
                     // assuming that it's a JAR/ZIP file
                     File[] createdFiles = ZipUtil.unzipTempJars(bundleFile);
                     for (int i = 0; i < createdFiles.length; i++) {
                         classpathEntries.add(createdFiles[i].getAbsolutePath());
                     }
                 } catch (IOException e) {
                     log.error("An error occurred while trying to extract nested JARs from " + bundleId, e); //$NON-NLS-1$
                 }
             } else {
                 Enumeration<URL> e = bundle.findEntries(
                        "/", "*.jar", true);
                 if (e != null) {
                    //$NON-NLS-1$//$NON-NLS-2$
                     while (e.hasMoreElements()) {
                         URL jarUrl = e.nextElement();
                         classpathEntries.add(
                                 new File(bundleFile + jarUrl.getFile())
                                 .getAbsolutePath());
                     }
                 }
             }
         } catch (IOException ioe) {
             log.error("Bundle with ID '" + bundleId + "' could not be resolved to a file.", ioe); //$NON-NLS-1$//$NON-NLS-2$
         }
 
         return classpathEntries.toArray(new String[classpathEntries.size()]);
     }
     
     /**
      * 
      * @param bundleId The ID of the bundle to search for a classpath.
      * @return the classpath contained within the bundle with the given ID.
      *         If
      *         <ul> 
      *           <li>the bundle cannot be resolved to a file, or</li> 
      *           <li>the bundle is not a JAR file and the bundle's directory contains no JAR files</li>
      *         </ul>
      *         an empty String will be returned.
      */
     public static String getClasspathForBundleId(String bundleId) {
         StringBuilder pathBuilder = new StringBuilder();
         for (String entry : getClasspathEntriesForBundleId(bundleId)) {
             pathBuilder.append(entry).append(PATH_SEPARATOR);
         }
         
         return pathBuilder.substring(
                 0, pathBuilder.lastIndexOf(PATH_SEPARATOR));
     }
     
 }
