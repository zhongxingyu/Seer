 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.common.test;
 
 import com.sun.appserv.security.ProgrammaticLogin;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import javax.ejb.embeddable.EJBContainer;
 import javax.naming.NamingException;
 import javax.transaction.UserTransaction;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 /**
  * Parent class for EJB Test classes.
  * <p>
  * Encapsulates setup and configuration of the embedded Glassfish container for the purposes of
  * testing EJBs. To use this class, create a JUnit 4 test class for your EJB, then extend
  * that class with this one. This class defines @BeforeClass and @AfterClass annotations
  * to configure the embedded Glassfish server and to start it running.
  * These annotations should be removed from descendent test classes to avoid overriding the
  * methods in this class. 
  * </p>
  * <p>
  * Note whenever a change to the Glassfish configuration occurs, the CONFIG_VERSION_NUMBER
  * constant should be updated accordingly. This is used to prevent unnecessary downloads of the
  * configuration data to the temp directory. Old versions of the configuration will be removed. 
  * </p>
  * @see SOLA Wiki <a href="http://www.flossola.org/wiki/Unit_testing_with_Embedded_Glassfish">
  * Unit Testing with Embedded Glassfish</a>
  * 
  * @author soladev
  * @since 15-May-2011
  *
  */
 public abstract class AbstractEJBTest {
 
     /**
      * The container application name
      */
     protected static String CONTAINER_APP_NAME = "solatest";
     /**
      * The Glassfish resource name in the TestUtilities Jar
      */
     protected static String GLASSFISH_RESOURCE_NAME = "glassfish";
     private static String configVersion = "v001";
     private static EJBContainer container = null;
     private static Map properties = null;
     private static Boolean skipTests = null;
     private ProgrammaticLogin pgLogin;
 
     /**
      * Checks the SOLA_OPTS environment variable to determine if the Integration Tests such as
      * those using Embedded Glassfish should be skipped or not. 
      * <p> To set this environment variable in Ubuntu, add the following export to the 
      * ~/.gnomerc file: {@code export SOLA_OPTS=SkipIntTests} You may need to create
      * the ~/.gnomerc file if it doesn't exist.</p><p>
      * This variable is used by Bamboo to avoid running Integration tests during the automated build process. 
      * @return 
      */
     protected static boolean skipIntegrationTest() {
         if (skipTests == null) {
             skipTests = false;
             String solaBuildOptions = System.getenv("SOLA_OPTS");
             if (solaBuildOptions != null && solaBuildOptions.matches(".*SkipIntTests.*")) {
                 // Skip running the tests
                 skipTests = true;
             }
         }
         return skipTests;
     }
 
     private static void setContainer(EJBContainer container) {
         AbstractEJBTest.container = container;
     }
 
     private static EJBContainer createContainer() {
         reconfigureContainerProperties();
         return EJBContainer.createEJBContainer(getProperties());
     }
 
     /**
      * Deletes all files and subdirectories under dir.
      * <p>
      * If a deletion fails, the method stops and returns false.
      * </p>
      * @param dir
      * @return true if all deletions were successful.
      */
     private static boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         // The directory is now empty so delete it
         return dir.delete();
     }
 
     /**
      * Returns the EJBContainer
      * @return
      */
     protected static EJBContainer getContainer() {
         return container;
     }
 
     /**
      * Obtains the properties to use for the embedded Glassfish container.
      * <p>
      * When creating the properties Map, the installation.root and APP_NAME properties are 
      * set. To add or alter these properties before the container is instantiated override
      * the reconfigureContainerProperties method in descendent classes. 
      * </p>
      * @return The Map of properties. 
      */
     protected static Map getProperties() {
         if (properties == null) {
             properties = new HashMap();
             properties.put("org.glassfish.ejb.embedded.glassfish.installation.root",
                     getGlassfishConfig());
             properties.put(EJBContainer.APP_NAME, CONTAINER_APP_NAME);
             //properties.put(InitialContext.SECURITY_PRINCIPAL, "joe");
             //properties.put(InitialContext.SECURITY_CREDENTIALS, "cool");
         }
         return properties;
     }
 
     /**
      * Empty method that allows the container properties to be reconfigured.
      * <p>
      * If further configuration of the container properties is required, override this method 
      * in descendent classes and update the Properties map directly
      * e.g. <pre><code>getProperties().put(PropertyName, PropertyValue);</code></pre>
      * </p>
      * <p>
      * By default embedded Glassfish will search for and load any EJB's in the jar that contains 
      * the test class as well as any jar this jar is dependent on. Other EJB's can also be 
      * loaded by specifying the MODULES property. 
      * </p>
      * <p>
      * e.g. To load EJB's from the ShoppingCart and OnlineCatalog jars
      * <pre><code>
      * File[] ejbModules = new File[2];
      * ejbModules[0] = new File("/home/myusername/ejbs/ShoppingCartEJB.jar");
      * ejbModules[1] = new File("/home/myusername/ejbs/OnlineCatalogEJB.jar");
      * getProperties().put(EJBContainer.MODULES, ejbModules);
      * </code></pre>
      * </p>
      * <p>
      * Note that loading modules in this manner will probably require explicitly loading the jar's 
      * containing the test class as well.
      * </p>
      */
     protected static void reconfigureContainerProperties() {
     }
 
     /**
      * Gets the Glassfish configuration location. 
      * <p>
      * Determines where the Glassfish configuration is located. If its not in a temp location
      * this method extracts the Glassfish configuration from the TestUtilities JAR file and saves it
      * there.
      * </p>
      * <p>
      * If the Glassfish configuration is updated, change the name of the temp directory version 
      * (e.g. TestUtilities/v001/ to TestUtilities/v002/) to ensure the new configuration is used by
      * everyone.
      * </p>
      * 
      * @return The location of the glassfish configuration
      */
     protected static String getGlassfishConfig() {
 
         ResourceBundle bundle = ResourceBundle.getBundle("sola-test-common");
         if (bundle != null) {
             String tempProp = bundle.getString("configuration.version");
             if (tempProp != null && !tempProp.isEmpty()) {
                 configVersion = tempProp;
             }
         }
         String tempDir = System.getProperty("java.io.tmpdir") + "/TestUtilities/";
         String tempDirVersion = tempDir + configVersion + "/";
         String installRootDir = tempDirVersion + GLASSFISH_RESOURCE_NAME;
        installRootDir = installRootDir.replace("%20", " ");
         File root = new File(installRootDir);
         File temp = new File(tempDir);
 
         if (!root.exists()) {
 
             // Remove any old versions of the configuration data but don't delete the
             // temp directory
             if (temp.exists()) {
                 String[] children = temp.list();
                 for (int i = 0; i < children.length; i++) {
                     deleteDir(new File(temp, children[i]));
                 }
             }
 
             // Determine the location of the TestUtilities JAR file (typically in the maven repository)
             // by getting the path to the /glassfish resource
             String jarLocation = AbstractEJBTest.class.getResource("/" + GLASSFISH_RESOURCE_NAME).getPath();
             // Remove the file: and !/glassfish from the file path
             int first = jarLocation.indexOf(":") + 1;
             int last = jarLocation.lastIndexOf("!");
             if (last <= 0) {
                 // This isn't a jar file (as no ! in path), so just return the location as is 
                 // because it should be an exploded folder
                 installRootDir = jarLocation;
             } else {
 
                 jarLocation = jarLocation.substring(first, last);
                 try {
                     // Extract the entire contents of the TestUtilities Jar to the tempVersion directory
                     UnZip extract = new UnZip();
                     extract.unZip(jarLocation, tempDirVersion);
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return installRootDir;
     }
 
     /**
      * Obtains a reference to the named EJB class running in the embedded Glassfish container.
      * <p>
      * This method assumes the EJB being requested is in the same jar as the test class and uses
      * "classes" as the module name. If the "classes" module name results in a NamingException, the
      * lookup is retried with the "classesejb" module name. This appears to be the module name used
      * when trying to debug the test. 
      * </p>
      * @param ejbClassName The class name of the EJB to get
      * @return A reference to the EJB or an exception
      * @throws NamingException If the EJB name cannot be found
      */
     protected Object getEJBInstance(String ejbClassName) throws Exception {
         return getEJBInstance("classes", ejbClassName);
     }
 
     /**
      * Obtains a reference to the name EJB class running in the embedded Glassfish container.
      * <p>
      * If the EJB is not in the same Jar as the test class, it will be necessary to specify the
      * module name for the EJB. This should be the name of the Jar containing the target EJB. Check
      * the JNDI names in the Output window for the EJB when it is loaded into the embedded 
      * Glassfish container.
      * </p>
      * <p>
      * Note this method appends the name parts together as follows
      * <pre><code>"java:global/solatest/" + moduleName + "/" + ejbClassName;</code></pre>
      * </p>
      * @param moduleName The name of the module prefixing the EJB
      * @param ejbClassName The class name of the EJB to get
      * @return A reference to the EJB or an exception
      * @throws NamingException If the EJB name cannot be found
      */
     protected Object getEJBInstance(String moduleName, String ejbClassName) throws Exception {
 
         List<String> moduleNames = new ArrayList<String>();
         moduleNames.add(moduleName);
         moduleNames.add("sola-" + ejbClassName.substring(0, (ejbClassName.length() - 3)).toLowerCase());
         moduleNames.add("classesejb");
         if (!moduleName.equals("classes")) {
             moduleNames.add("classes");
         }
 
         Object result = null;
         for (String modName : moduleNames) {
             try {
                 result = getContainer().getContext().lookup(buildLookupName(modName, ejbClassName));
                 break;
             } catch (NamingException ex) {
                 System.out.println("INFO: Failed lookup of " + ejbClassName + " using JNDI name "
                         + buildLookupName(modName, ejbClassName) + ". Will retry with alternative name.");
             }
         }
         if (result == null) {
             throw new Exception("ERROR: Unable to locate EJB for " + ejbClassName
                     + " using JNDI lookup. Verify the moduleName to use by referring to the "
                     + "'Portable JNDI names for EJB' listed in the output above and retry");
         }
         return result;
     }
 
     private String buildLookupName(String moduleName, String ejbClassName) {
         return "java:global/" + CONTAINER_APP_NAME + "/" + moduleName + "/" + ejbClassName;
     }
 
     /**
      * Obtains a UserTransaction object from the embedded Glassfish server.
      * <p>
      * Obtains the object by performing a JNDI name lookup. The transaction
      * object must then be managed manually by the calling code. 
      * </p>
      * @return The UserTransaction
      * @throws NamingException If the JNDI name for the UserTransaction is not recognized
      */
     protected UserTransaction getUserTransaction() throws NamingException {
         return (UserTransaction) getContainer().getContext().lookup("java:comp/UserTransaction");
     }
 
     /**
      * Logs the named user into Glassfish.
      * @param userName Username used to login
      * @param password User password
      * @return
      * @throws Exception 
      */
     protected boolean login(String userName, String password) throws Exception {
         if(pgLogin==null){
             pgLogin = new ProgrammaticLogin();
         }else{
             pgLogin.logout();
         }
         return pgLogin.login(userName, password.toCharArray(), "SolaRealm", true);
     }
     
     /**
      * Logs out user.
      * @return
      * @throws Exception 
      */
     protected boolean logout() throws Exception {
         if(pgLogin!=null){
             return pgLogin.logout();
         }
         return true;
     }
 
     /** 
      * Checks if the specified exception type exists in the Cause
      * list for the exception. 
      * <p>
      * When an exception occurs in an EJB, it is wrapped by an EJB rollback exception or 
      * equivalent. This method can be used to determine if the exception was caused by
      * a specific exception (e.g. OptimisiticLockException). 
      * </p>
      * @param t The exception that is being checked
      * @param causeType The type of exception to check for. 
      * @return true if the causeType is found in the list of causes for the exception. 
      */
     protected boolean hasCause(Throwable t, Class causeType) {
         boolean result = false;
         if (t.getClass() == causeType) {
             result = true;
         } else {
             if (t.getCause() != null) {
                 result = hasCause(t.getCause(), causeType);
             }
         }
         return result;
     }
 
     /** 
      * @BeforeClass test method that creates the embedded Glassfish container
      * @throws Exception 
      */
     @BeforeClass
     public static void setUpClass() throws Exception {
         if (skipIntegrationTest()) {
             System.out.println("Skipping GF Tests as SOLA_OPTS.SkipGFTests environment "
                     + "variable is set.");
         } else {
             System.out.println("Current User Directory = " + System.getProperty("user.dir"));
             setContainer(createContainer());
         }
     }
 
     /**
      * @AfterClass test method that closes the embedded Glassfish
      * @throws Exception 
      */
     @AfterClass
     public static void tearDownClass() throws Exception {
         if (getContainer() != null) {
             getContainer().close();
         }
     }
 }
