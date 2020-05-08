 package com.insightfullogic.vertx.promises.plugin;
 
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
 
 import java.io.File;
import java.util.Arrays;
import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
 
 import com.insightfullogic.vertx.promises.codegen.Api;
 import com.insightfullogic.vertx.promises.codegen.ClassGenerator;
 import com.insightfullogic.vertx.promises.codegen.ClassInspector;
 import com.insightfullogic.vertx.promises.codegen.LanguageModule;
import com.insightfullogic.vertx.promises.codegen.java.JavaLanguageModule;
 
 /**
  * Goal which touches a timestamp file.
  * 
  * @goal generate
  * 
  * @phase validate
  */
 public class CodeGenerationMojo extends AbstractMojo {
     
     /**
      * Location of the file.
      * 
      * @parameter expression="${project.build.directory}"
      * @required
      */
     private File outputDirectory;
 
     /**
      * @parameter
      * @required
      */
     private String languageModule;
 
     public void execute() throws MojoExecutionException {
         ensureDirectoryExists();
         LanguageModule languageModule = getLanguageModule();
         ClassGenerator generator = languageModule.makeGenerator(outputDirectory);
         for (Class<?> klass : Api.INST.classes) {
             ClassInspector inspector = new ClassInspector(klass, generator);
             inspector.inspect();
         }
         generator.generate();
     }
 
     @SuppressWarnings("unchecked")
     private LanguageModule getLanguageModule() throws MojoExecutionException {
         try {
             Class<LanguageModule> moduleClass = (Class<LanguageModule>) Class.forName(languageModule);
             return moduleClass.newInstance();
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 
     private void ensureDirectoryExists() {
         if (!outputDirectory.exists()) {
             outputDirectory.mkdirs();
         }
     }
 
 }
