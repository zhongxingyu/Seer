 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.deployer;
 
 import java.io.File;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
 
 import com.google.inject.AbstractModule;
 import com.meltmedia.cadmium.core.CadmiumModule;
 import com.meltmedia.cadmium.maven.ArtifactResolver;
 import com.meltmedia.cadmium.servlets.guice.CadmiumListener;
 
 /**
  * A Guice module that binds an instance of ArtifactResolver from the maven project.
  * 
  * @see <a href="http://code.google.com/p/google-guice/">Google Guice</a>
  * 
  * @author John McEntire
  *
  */
 @CadmiumModule
 public class DeployerModule extends AbstractModule {
   private final Logger logger = LoggerFactory.getLogger(getClass());
   
   /**
    * The System Property key to set the remote maven repository. <code>(com.meltmedia.cadmium.maven.repository)</code>
    */
   public static final String MAVEN_REPOSITORY = "com.meltmedia.cadmium.maven.repository";
   
   /**
    * Called to do all bindings for this module.
    * 
    * @see <a href="http://code.google.com/p/google-guice/">Google Guice</a>
    */
   @Override
   protected void configure() {
     try {
      InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
       File appRoot = new File(System.getProperty(CadmiumListener.BASE_PATH_ENV), "maven");
       FileUtils.forceMkdir(appRoot);
       String remoteMavenRepo = System.getProperty(MAVEN_REPOSITORY);
       ArtifactResolver resolver = new ArtifactResolver(remoteMavenRepo, appRoot.getAbsolutePath());
       bind(ArtifactResolver.class).toInstance(resolver);
     } catch(Exception e) {
       logger.error("Failed to initialize maven artifact resolver.", e);
     }
   }
 
 }
