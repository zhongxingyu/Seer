 package org.intellij.plugins.junitgen;
 
 import com.intellij.openapi.components.*;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.project.Project;
 import com.intellij.util.xmlb.XmlSerializerUtil;
 import com.intellij.util.xmlb.annotations.Transient;
 import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This is the settings wrapper. We persist settings to disk in various ways from
  * this class.
  *
  * @author Jon Osborn
  * @since 1/4/12 2:56 PM
  */
 public class JUnitGeneratorSettings implements PersistentStateComponent<JUnitGeneratorSettings> {
 
     private static final Logger log = Logger.getInstance(JUnitGeneratorSettings.class);
 
     @State(
             name = "JUnitGeneratorSettings",
             storages = {
                     @Storage(id = "app-default", file = "$APP_CONFIG$/junitgenerator-settings.xml")})
     public static class App extends JUnitGeneratorSettings {
         public App() {
            //load the default state from the application state
             loadDefaultState(this);
         }
     }
 
     @State(
             name = "JUnitGeneratorSettings",
             storages = {
                     @Storage(id = "prj-default", file = "$PROJECT_FILE$"),
                     @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/junitgenerator-settings.xml", scheme = StorageScheme.DIRECTORY_BASED)})
     public static class Prj extends JUnitGeneratorSettings {
         public Prj() {
             //load the default state from the application state
             loadDefaultState(this);
         }
     }
 
     /**
      * Return an instance from the service manager
      *
      * @return the settings instance
      */
     public static JUnitGeneratorSettings getInstance() {
         return ServiceManager.getService(JUnitGeneratorSettings.class);
     }
 
     /**
      * Return the proper settings based on the configuration of the project. If the project settings
      * are not used, this method returns the global settings instance
      *
      * @param project the project
      * @return the settings instance
      */
     public static JUnitGeneratorSettings getInstance(Project project) {
         JUnitGeneratorSettings settings = getProjectInstance(project);
         if (!settings.isUseProjectSettings()) {
             if (log.isDebugEnabled()) {
                 log.debug("Project is configured for global settings, so we return the global settings");
             }
             settings = getInstance();
         }
         return settings;
     }
 
     /**
      * Return the project settings, regardless of the configuration
      *
      * @param project the project
      * @return the settings instance
      */
     public static JUnitGeneratorSettings getProjectInstance(Project project) {
         return ServiceManager.getService(project, JUnitGeneratorSettings.class);
     }
 
     @Override
     public JUnitGeneratorSettings getState() {
         return this;
     }
 
     @Override
     public void loadState(JUnitGeneratorSettings settings) {
         XmlSerializerUtil.copyBean(settings, this);
     }
 
     /**
      * Load the default state
      *
      * @param settings the settings to load into
      */
     public static void loadDefaultState(JUnitGeneratorSettings settings) {
         log.debug("setting up default configuration");
         settings.setOutputFilePattern(JUnitGeneratorUtil.getProperty("junit.generator.outputPath"));
         settings.setGenerateForOverloadedMethods(true);
         settings.setListOverloadedMethodsBy("paramName");
         settings.setCombineGetterAndSetter(false);
         final List<String> names = Arrays.asList(JUnitGeneratorUtil.getDelimitedProperty("junit.generator.vm.names", ","));
         final List<String> templates = JUnitGeneratorUtil.getPropertyList("junit.generator.vm");
         if (names.size() != templates.size()) {
             throw new IllegalArgumentException("template names and definitions must be equal");
         }
         final Map<String, String> map = new HashMap<String, String>(names.size());
         for (int i = 0; i < names.size(); i++) {
             map.put(names.get(i), templates.get(i));
         }
         settings.setVmTemplates(map);
         settings.setUseProjectSettings(false);
     }
 
     private String outputFilePattern;
     private boolean generateForOverloadedMethods;
     private String listOverloadedMethodsBy;
     private boolean combineGetterAndSetter;
     private Map<String, String> vmTemplates = new HashMap<String, String>();
     private String selectedTemplateKey;
     private boolean useProjectSettings;
 
     public String getOutputFilePattern() {
         return outputFilePattern;
     }
 
     public void setOutputFilePattern(String outputFilePattern) {
         this.outputFilePattern = outputFilePattern;
     }
 
     public String getListOverloadedMethodsBy() {
         return listOverloadedMethodsBy;
     }
 
     public void setListOverloadedMethodsBy(String listOverloadedMethodsBy) {
         this.listOverloadedMethodsBy = listOverloadedMethodsBy;
     }
 
     public boolean isGenerateForOverloadedMethods() {
         return generateForOverloadedMethods;
     }
 
     public void setGenerateForOverloadedMethods(boolean generateForOverloadedMethods) {
         this.generateForOverloadedMethods = generateForOverloadedMethods;
     }
 
     public boolean isCombineGetterAndSetter() {
         return combineGetterAndSetter;
     }
 
     public void setCombineGetterAndSetter(boolean combineGetterAndSetter) {
         this.combineGetterAndSetter = combineGetterAndSetter;
     }
 
     public boolean isUseProjectSettings() {
         return useProjectSettings;
     }
 
     public void setUseProjectSettings(boolean useProjectSettings) {
         this.useProjectSettings = useProjectSettings;
     }
 
     public Map<String, String> getVmTemplates() {
         return vmTemplates;
     }
 
     public void setVmTemplates(Map<String, String> vmTemplates) {
         this.vmTemplates = vmTemplates;
     }
 
     public String getSelectedTemplateKey() {
         return selectedTemplateKey;
     }
 
     public void setSelectedTemplateKey(String selectedTemplateKey) {
         this.selectedTemplateKey = selectedTemplateKey;
     }
 
     @Transient
     public String getTemplate(String key) {
         if (key != null) {
             return this.vmTemplates.get(key);
         }
         return null;
     }
 }
