 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.summit.i18nhelper;
 
 import java.io.File;
 import java.util.List;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.shared.model.fileset.FileSet;
 
 /**
  *
  * @goal gen
 * @phase compile
  * @threadSafe false
  * @execute phase="process-resources"
  * @author justin
  */
 public class GenerateMojo extends AbstractMojo {
 
     /**
      * @parameter 
      *  expression="${i18n.gen.input}"
      *  default-value="${project.build.directory}/i18nHelper-translated.properties"
      * 
      * @required
      */
     File genInputFile;
     /**
      * Languages codes to include in the pull, these will be added to the generated file.
      * 
      * File output will be a java properties file
      * 
      * @parameter 
      */
     private List<String> languageCodes;
     /**
      * Places to look for bundles in the source tree, this is so we use what is 
      * explicitely written to source, instead of something external.
      * @parameter
      * @required 
      */
     private FileSet[] bundleLocations;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         File genDir = new File("target/classes/");
         if (!genDir.exists()) {
             genDir.mkdirs();
         }
         BundleGenerator bundleGenerator = new BundleGenerator();
         try {
             bundleGenerator.generateResources(genDir, genInputFile, languageCodes, getLog());
         } catch (Exception ex) {
             throw new MojoFailureException(ex, ex.getLocalizedMessage(), "Error generating resources.");
         }
     }
 }
