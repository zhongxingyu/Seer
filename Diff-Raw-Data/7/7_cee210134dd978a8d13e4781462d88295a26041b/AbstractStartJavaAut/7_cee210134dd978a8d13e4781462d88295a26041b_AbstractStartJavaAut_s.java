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
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jubula.autagent.monitoring.IMonitoring;
 import org.eclipse.jubula.autagent.monitoring.MonitoringDataStore;
 import org.eclipse.jubula.autagent.monitoring.MonitoringUtil;
 import org.eclipse.jubula.communication.message.StartAUTServerStateMessage;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.CommandConstants;
 import org.eclipse.jubula.tools.constants.MonitoringConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.jarutils.MainClassLocator;
 import org.eclipse.osgi.service.datalocation.Location;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 9, 2007
  */
 public abstract class AbstractStartJavaAut extends AbstractStartToolkitAut {
     /**
      * <code>JAVA_OPTIONS_INTRO</code>
      */
     protected static final String JAVA_OPTIONS_INTRO = "_JAVA_OPTIONS="; //$NON-NLS-1$
 
     /**
      * <code>JAVA_LANGUAGE_PROPERTY</code>
      */
     private static final String JAVA_LANGUAGE_PROPERTY = "-Duser.language="; //$NON-NLS-1$
 
     /**
      * <code>JAVA_COUNTRY_PROPERTY</code>
      */
     private static final String JAVA_COUNTRY_PROPERTY = "-Duser.country="; //$NON-NLS-1$
 
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(AbstractStartJavaAut.class);
      
     
     /**
      * {@inheritDoc}
      */
     protected String createBaseCmd(Map parameters) throws IOException {
         String executableFileName = (String)parameters.get(
                 AutConfigConstants.EXECUTABLE);
         if (executableFileName != null && executableFileName.length() > 0) {
             // Use the given executable, prepending the working directory if
             // the filename is relative
             File exe = new File(executableFileName);
             if (!exe.isAbsolute()) {
                 exe = new File(
                     (String)parameters.get(AutConfigConstants.WORKING_DIR), 
                     executableFileName);
             }
             if (exe.isFile() && exe.exists()) {
                 return exe.getCanonicalPath();
             }
             // else
             String errorMsg = 
                 executableFileName 
                 + " does not point to a valid executable."; //$NON-NLS-1$
             LOG.warn(errorMsg);
             return executableFileName;
         }
     
         // Use java if no executable file is defined
         String java = StringConstants.EMPTY;
         String jre = (String)parameters.get(AutConfigConstants.JRE_BINARY);
         if (jre == null) {
             jre = StringConstants.EMPTY;
         }
         File jreFile = new File(jre);
         if (jre.length() == 0) {
             java = "java"; //$NON-NLS-1$
         } else {
             if (!jreFile.isAbsolute()) {
                 jreFile = new File(getWorkingDir(parameters), jre);
             }
             if (jreFile.isFile() && jreFile.exists()) {
                 java = jreFile.getCanonicalPath();
             } else {
                 String errorMsg = 
                     jreFile + " does not point to a valid JRE executable."; //$NON-NLS-1$
                 LOG.error(errorMsg);
                 throw new FileNotFoundException(errorMsg);
             }
         }
         return java; 
     }
 
     /**
      * determines the main class for the aut. <br>
      * If a main class was transmitted, use it. Otherwise scan the jar.
      * 
      * @param parameters The parameters for starting the AUT.
      * @return the main class or null if no main class was found. In this case
      *         m_errorMesssage contains a detailed message to send back.
      */
     protected String getAUTMainClass(Map parameters) {
         final String autClassName = (String)parameters.get(
                 AutConfigConstants.CLASSNAME);
         if (autClassName != null && autClassName.length() > 0) {
             // use the supplied information
             return autClassName;
         }
         final String jarFile = (String)parameters.get(
                 AutConfigConstants.JAR_FILE);
         String mainClass = getMainClassFromManifest(parameters);
         if (mainClass != null) {
             return mainClass;
         }
         if (LOG.isInfoEnabled()) {
             LOG.info("neither main class transmitted nor found in the manifest, " //$NON-NLS-1$
                     + "searching in jar: '" //$NON-NLS-1$
                     + String.valueOf(jarFile) + "'"); //$NON-NLS-1$
         }
         if (jarFile != null && jarFile.length() > 0) {
             try {
                 List mains = MainClassLocator.getMainClass(new File(jarFile));
                 if (mains.size() == 0) {
                     String message = "no main class found in '" //$NON-NLS-1$
                             + jarFile + "'"; //$NON-NLS-1$
                     LOG.error(message);
                     setErrorMessage(new StartAUTServerStateMessage(
                         StartAUTServerStateMessage.AUT_MAIN_NOT_FOUND_IN_JAR,
                         message));
                     return null;
                 }
                 if (mains.size() != 1) {
                     // the jar must contain exact one main class
                     // HERE send back a list of main classes
                     String message = "more than on main class found"; //$NON-NLS-1$
                     LOG.error(message);
 
                     setErrorMessage(new StartAUTServerStateMessage(
                         StartAUTServerStateMessage.AUT_MAIN_NOT_DISTINCT_IN_JAR,
                         message));
                     return null;
                 }
                 return ((String)mains.get(0)).replace('/', '.');
             } catch (NullPointerException npe) {
                 // from new File()
                 String message = "no jar given as classpath"; //$NON-NLS-1$ 
                 LOG.error(message, npe);
                 setErrorMessage(new StartAUTServerStateMessage(
                     StartAUTServerStateMessage.NO_JAR_AS_CLASSPATH, message));
                 return null;
             } catch (IOException ioe) {
                 String message = "scanning '" //$NON-NLS-1$
                         + String.valueOf(jarFile) + "' for main class failed"; //$NON-NLS-1$
                 LOG.error(message, ioe);
 
                 setErrorMessage(new StartAUTServerStateMessage(
                     StartAUTServerStateMessage.SCANNING_JAR_FAILED, message));
                 return null;
             }
         }
         return null;
     }
 
     /**
      * Gets the main Class of the AUT jar
      * 
      * @param parameters The parameters for starting the AUT.
      * @return the main class
      */
     private String getMainClassFromManifest(Map parameters) {
         String jarFile = createAbsoluteJarPath(parameters);
         return getAttributeFromManifest("main-class", jarFile); //$NON-NLS-1$
     }
     
     /**
      * @param attributeName the attribute name in the manifest
      * @param jarFile the path and name of the jar file to examine
      * @return the String value of the specified attribute name, or null if
      *         not found.
      */
     private String getAttributeFromManifest(
         String attributeName, String jarFile) {
         
         if (jarFile == null || jarFile.length() < 1) {
             return null;
         }
         String attribute = null;
         try {
             JarFile jar = new JarFile(jarFile);
             Manifest manifest = jar.getManifest();
             if (manifest != null) {
                 attribute = manifest.getMainAttributes().getValue(
                         attributeName);
             }
         } catch (FileNotFoundException e) {
             LOG.error("File not found: " + jarFile, e); //$NON-NLS-1$
         } catch (IOException e) {
             LOG.error("Error reading jar file: " + jarFile, e); //$NON-NLS-1$
         }
         return attribute;
     }
 
 
     /**
      * 
      * @param parameters The parameters for starting the AUT.
      * @return the absolute path to the AUT jar file or null.
      */
     private String createAbsoluteJarPath(Map parameters) {
         File workingDir = getWorkingDir(parameters);
         String jarPath = (String)parameters.get(AutConfigConstants.JAR_FILE);
         if (jarPath != null && jarPath.length() > 0) {
             if (workingDir != null) {
                 File jarFile = new File(jarPath);
                 if (!jarFile.isAbsolute()) {
                     jarPath = workingDir + FILE_SEPARATOR + jarPath;
                 }
             }
         }
         return jarPath;
     }
     
     /**
      * @return the name of the main class for the AUT server.
      */
     protected abstract String getServerClassName();
     
     /**
      * @param cmds The command List. May <b>not</b> be <ocde>null</code>.
      * @param locale The <code>Locale</code> for the AUT. 
      *               May be <code>null</code> if no locale was specified.
      */
     protected void addLocale(List cmds, Locale locale) {
         if (locale != null) {
             if (locale.getCountry() != null 
                 && locale.getCountry().length() > 0) {
                 cmds.add(JAVA_COUNTRY_PROPERTY + locale.getCountry());
             }
             if (locale.getLanguage() != null 
                 && locale.getLanguage().length() > 0) {
                 cmds.add(JAVA_LANGUAGE_PROPERTY + locale.getLanguage());
             }
         }
     }
 
     /**
      * Gets the classPath from the manifest of the given jar.
      * 
      * @param parameters The parameters for starting the AUT.
      * @return the classpath separated with OS-specific path separators 
      * or an empty String if the given jar is null or if there is no manifest
      * in the jar.
      */
     protected String getClassPathFromManifest(Map parameters) {
         String jarFile = createAbsoluteJarPath(parameters);
         String classPath = getAttributeFromManifest("class-path", jarFile); //$NON-NLS-1$
         if (classPath == null) {
             return StringConstants.EMPTY;
         }
         classPath = classPath.trim();
         return classPath.replace(' ', PATH_SEPARATOR.charAt(0));
     }
 
     /**
      * Workaround to make the given classpath, which uses a specific path 
      * separator, usable on the current platform.
      * @param clientPath The classpath to convert.
      * @return the converted classpath
      */
     protected String convertClientSeparator(String clientPath) {
         return clientPath.replaceAll(CLIENT_PATH_SEPARATOR, PATH_SEPARATOR);
     }
 
     /**
      * {@inheritDoc}
      */
     protected abstract String[] createCmdArray(String baseCmd, Map parameters);
     
     /**
      * @param parameters The parameters for starting the AUT.
      * @return <code>true</code> if the AUT is being started via an executable
      *         file or script. Otherwise, <code>false</code>.
      */
     protected boolean isRunningFromExecutable(Map parameters) {
         return parameters.containsKey(AutConfigConstants.EXECUTABLE);
     }
     
     /**
      * @param parameters The parameters for starting the AUT.
      * @return <code>true</code> if the AUT is being started with a 
      * monitoring agent. Otherwise, <code>false</code>.
      */
     protected boolean isRunningWithMonitoring(Map parameters) {
         
         String monitoringId = (String)parameters.get(
                 AutConfigConstants.MONITORING_AGENT_ID);        
         if (!StringUtils.isEmpty(monitoringId)) { 
             return true;
         }                 
         return false;         
     }
     
     /**
      * Sets -javaagent and JRE arguments as SUN environment variable.
      * @param parameters The parameters for starting the AUT
      * @return the _JAVA_OPTIONS environment variable including -javaagent
      * and jre arguments
      */
     protected String setJavaOptions(Map parameters) {
         StringBuffer sb = new StringBuffer();
         if (isRunningFromExecutable(parameters)) {
             Locale locale = (Locale)parameters.get(IStartAut.LOCALE);
             // set agent and locals
             
             sb.append(JAVA_OPTIONS_INTRO);
             if (isRunningWithMonitoring(parameters)) {
                 sb.append(getMonitoringAgent(parameters))
                     .append(StringConstants.SPACE);
             }                 
             sb.append(StringConstants.QUOTE)
                 .append("-javaagent:") //$NON-NLS-1$
                 .append(getAbsoluteAgentJarPath())
                 .append(StringConstants.QUOTE);
                
             if (locale != null) {
                 sb.append(StringConstants.SPACE)
                     .append(JAVA_COUNTRY_PROPERTY)
                     .append(locale.getCountry());
                 sb.append(StringConstants.SPACE)
                     .append(JAVA_LANGUAGE_PROPERTY)
                     .append(locale.getLanguage());
             }
         } else {
             if (isRunningWithMonitoring(parameters)) {
                 sb.append(JAVA_OPTIONS_INTRO)
                     .append(getMonitoringAgent(parameters));
             }
         }
 
         return sb.toString();
     }
     
     /**
      * Gets the absolute path of the org.eclipse.jubula.rc.common.agent.jar file.
      * @return the absolute path
      */
     protected String getAbsoluteAgentJarPath() {
         return AbstractStartToolkitAut.getClasspathForBundleId(
                 CommandConstants.RC_COMMON_AGENT_BUNDLE_ID);
     }
     
     /**
      * Searches for *.jar files in the server extension directory (ext). 
      * @return the extensions (jarfiles) as URL's
      */
     private URL[] getExtensions() {
         
         final File extDir = getExtDir();
         final File[] extJars = extDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".jar"); //$NON-NLS-1$
             }
         });
         URL[] urls;
        if (extJars != null) {           
            LOG.error("jars are " + extJars); //$NON-NLS-1$
             urls = new URL[extJars.length];
             for (int i = 0; i < extJars.length; i++) {
                 try {                          
                     urls[i] = extJars[i].toURI().toURL();
                 } catch (MalformedURLException e) {                   
                     LOG.error("URL is malformed", e); //$NON-NLS-1$
                 }                  
             }
         } else {
            LOG.error("no ext jars"); //$NON-NLS-1$
             urls = new URL[0];
         }
         return urls;
     }
 
     /**
      * @return A File with the directory where the monitoring extensions
      * may be found. Note that the directory may not exist!
      */
     public static File getExtDir() {
         File installDir = getInstallDir();
         final File extDir = new File(installDir, 
                 CommandConstants.EXT_JARS_PATH);
         return extDir;
     }
 
     /**
      * @return the AUTAgent installation directory
      */
     public static File getInstallDir() {
         Location installLoc = Platform.getInstallLocation();
         String installDir = installLoc.getURL().getFile();
         return new File(installDir);
     }
     
     /**
      * This method will load the class which implements the {@link IMonitoring} 
      * interface, and will invoke the "getAgent" method. 
      * @param parameters The AutConfigMap
      * @return agentString The agent string 
      */        
     protected String getMonitoringAgent(Map parameters) {
         
         String monitoringAgentClass = (String)parameters.get(
                     MonitoringConstants.AGENT_CLASS); 
         
         String autId = (String)parameters.get(
                 AutConfigConstants.AUT_ID);         
        
         MonitoringDataStore mds = MonitoringDataStore.getInstance();
         boolean duplicate = MonitoringUtil.checkForDuplicateAutID(autId);
         if (!duplicate) {            
             mds.putConfigMap(autId, parameters); 
         }      
         String agentString = null;       
         if (isRunningWithMonitoring(parameters)) {
             try {  
                 ClassLoader loader = new URLClassLoader(
                         getExtensions(), getClass().getClassLoader());
                 Class<?> monitoringClass = 
                     loader.loadClass(monitoringAgentClass);     
                 Constructor<?> constructor = monitoringClass.getConstructor();
                 IMonitoring agentInstance = 
                     (IMonitoring)constructor.newInstance();
                 agentInstance.setAutId(autId);
                 agentInstance.setInstallDir(getInstallDir());
                 agentString = agentInstance.createAgent();
                 if (!duplicate) {
                     mds.putMonitoringAgent(autId, agentInstance);  
                 } 
             } catch (ClassNotFoundException e) {
                 String errorMsg = "The monitoring class could not be loaded via reflection "; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
                
             } catch (InstantiationException e) {
                 String errorMsg = "The instantiation of the monitoring class failed "; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
                
             } catch (IllegalAccessException e) {
                 String errorMsg = "Access to the monitoring class failed "; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
                 
             } catch (SecurityException e) {
                 String errorMsg = "Access to the monitoring class failed "; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
                
             } catch (NoSuchMethodException e) {
                 String errorMsg = "A method in the monitoring class could not be found"; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
                 
             } catch (IllegalArgumentException e) {
                 String errorMsg = "A argument which is passed to monitoring class is invalide"; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
                 
             } catch (InvocationTargetException e) {
                 String errorMsg = "The method call of 'getAgent' failed, you have to implement the interface 'IMonitoring"; //$NON-NLS-1$
                 LOG.error(errorMsg, e);
             }     
           
         }
         return agentString;        
     }
     
 }
