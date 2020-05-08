 /*
  * Copyright 2011 Martin Dobmeier
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.martido.genny.ant;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 
 import de.martido.genny.GeneratorDefinition;
 import de.martido.genny.Genny;
 import de.martido.genny.GennyConfiguration;
 import de.martido.genny.provider.PropertyFileProvider;
 
 /**
  * An Ant task for Genny.
  * 
  * @author Martin Dobmeier
  */
 public class GennyTask extends Task {
 
   /** The target class to generate. */
   private String targetClass;
 
   /** The base directory of {@link #targetClass}. */
   private String baseDirectory;
 
   /** A list of files from which to generate the {@link #targetClass}. */
   private final List<FileSet> fileSets = new ArrayList<FileSet>();
 
   /** An optioanl fully qualified class name of a custom {@link GennyConfiguration}. */
   private String configurationClass;
 
   /** Turns verbose logging on/off. */
   private boolean verbose;
 
   public void setTargetClass(String targetClass) {
     this.targetClass = targetClass;
   }
 
   public void setBaseDirectory(String baseDirectory) {
     this.baseDirectory = baseDirectory;
   }
 
   public void addFileSet(FileSet fileSet) {
     this.fileSets.add(fileSet);
   }
 
   public void setConfigurationClass(String configurationClass) {
     this.configurationClass = configurationClass;
   }
 
   public void setVerbose(boolean verbose) {
     this.verbose = verbose;
   }
 
   /**
    * Visible for testing.
    */
   protected boolean isValid() {
     if (this.targetClass == null ||
         this.baseDirectory == null ||
         this.fileSets.isEmpty()) {
       return false;
     } else {
       return true;
     }
   }
 
   /**
    * Reflectively creates an instance of {@code className} which must be assignable to a reference
    * of type {@link GennyConfiguration}.
    * 
    * @param className
    *          <i>mandatory</i> - the name of the Java class to instantiate.
    * @return A {@link GennyConfiguration}.
    * @throws BuildException
    *           If the configuration class could not be created.
    */
   private GennyConfiguration createConfiguration(String className) throws BuildException {
     try {
       Class<?> clazz = Class.forName(className);
       if (!GennyConfiguration.class.isAssignableFrom(clazz)) {
         throw new BuildException("A configuration class must implement "
             + GennyConfiguration.class.getSimpleName());
       }
       return (GennyConfiguration) clazz.newInstance();
     } catch (Exception ex) {
       throw new BuildException(ex);
     }
   }
 
   /**
    * Creates a default {@link GennyConfiguration}.
    * 
    * @return A {@link GennyConfiguration}.
    */
   private GennyConfiguration createConfiguration() {
 
     return new GennyConfiguration() {
       public GeneratorDefinition configure(GeneratorDefinition def) {
         def.setFieldProvider(PropertyFileProvider.forFiles(def.getInputFiles()).build());
         return def;
       }
     };
   }
 
   @Override
   public void execute() throws BuildException {
 
     if (!this.isValid()) {
       throw new BuildException(
           "You have to specify the 'targetClass' and 'baseDirectory' attributes " +
               "as well as a set of source files. If you need custom behaviour, " +
               "implement a GennyConfiguration and add the 'configurationClass' attribute");
     }
 
     final List<String> inputFiles = new ArrayList<String>();
     for (FileSet fs : this.fileSets) {
       DirectoryScanner ds = fs.getDirectoryScanner(this.getProject());
      for (String fileName : ds.getIncludedFiles()) {
        inputFiles.add(fileName);
       }
     }
 
     GennyConfiguration conf;
     if (this.configurationClass != null) {
       conf = this.createConfiguration(this.configurationClass);
     }
     else {
       conf = this.createConfiguration();
     }
 
     try {
       GeneratorDefinition def = new GeneratorDefinition(
           this.targetClass, this.baseDirectory, inputFiles);
       new Genny(this.verbose).generateFrom(conf, def);
     } catch (Exception ex) {
       throw new BuildException(ex);
     }
   }
 
 }
