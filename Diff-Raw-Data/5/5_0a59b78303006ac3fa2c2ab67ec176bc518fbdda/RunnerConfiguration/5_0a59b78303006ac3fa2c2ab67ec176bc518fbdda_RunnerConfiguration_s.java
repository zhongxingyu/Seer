 package org.chtijbug.drools.runner;
 
 import org.chtijbug.drools.guvnor.GuvnorConnexionConfiguration;
 
 import java.io.File;
 
 /**
  * Created with IntelliJ IDEA.
  * User: samuel
  * Date: 03/10/12
  * Time: 17:26
  */
 public class RunnerConfiguration {
     protected static final String WORKSPACE_FOLDER = "/tmp/chtijbug";
     /** The string used to separator packages */
     public static final String PACKAGE_SEPARATOR = ".";
 
     /** Guvnor connexion configuration bean */
     private GuvnorConnexionConfiguration configuration;
    /** The business package name */
    private String packageName;
     /** Class name to be used as an argument */
     private String inputClassName;
     /** Class name to be used as a result */
     private String outputClassName;
 
     public RunnerConfiguration(GuvnorConnexionConfiguration configuration, String inputClassName, String outputClassName) {
         this.configuration = configuration;
         this.inputClassName = inputClassName;
         this.outputClassName = outputClassName;
     }
 
     protected GuvnorConnexionConfiguration getConfiguration() {
         return configuration;
     }
 
     public String getPackageName() {
        return packageName;
     }
 
     public String getInputClassName() {
         return inputClassName;
     }
 
     public String getOutputClassName() {
         return outputClassName;
     }
 
     public String getInputClassShortName() {
         return stripPackageName(getInputClassName());
     }
 
     public String getOutputClassShortName() {
         return stripPackageName(getOutputClassName());
     }
 
     public static String stripPackageName(final String classname) {
         int idx = classname.lastIndexOf(PACKAGE_SEPARATOR);
         if (idx != -1)
             return classname.substring(idx + 1, classname.length());
         return classname;
     }
 
     public String getWebappName() {
         return getPackageName().concat("-rules-service");  //To change body of created methods use File | Settings | File Templates.
     }
 
     public String getProjectRootPath() {
         return WORKSPACE_FOLDER.concat("/").concat(getWebappName());
     }
 
     // TODO Ajouter un JUNIT
     public String getPomFilePath() {
         return getProjectRootPath().concat(File.separator).concat("pom.xml");
     }
 
     // TODO Ajouter un JUNIT
     public String getWarFileFile() {
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append(getProjectRootPath()).append(File.separator);
         stringBuffer.append("target").append(File.separator);
         stringBuffer.append(getWebappName()).append(".war");
         return stringBuffer.toString();
     }
 }
