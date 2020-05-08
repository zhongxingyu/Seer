 /*
  * Copyright 2014 Sorcersoft.com S.A.
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
 package sorcer.rmi;
 
 import org.rioproject.resolver.Resolver;
 import org.rioproject.resolver.ResolverException;
 import org.rioproject.resolver.ResolverHelper;
 import org.rioproject.url.artifact.ArtifactURLConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sun.rmi.server.LoaderHandler;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.rmi.server.RMIClassLoader;
 import java.rmi.server.RMIClassLoaderSpi;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author Rafał Krupiński
  */
 public class ArtifactRmiLoader extends RMIClassLoaderSpi {
     /**
      * A table of artifacts to derived codebases. This improves performance by resolving the classpath once per
      * artifact.
      */
     private final Map<String, String> artifactToCodebase = new ConcurrentHashMap<String, String>();
     /**
      * A table of classes to artifact: codebase. This will ensure that if the annotation is requested for a class that
      * has it's classpath resolved from an artifact, that the artifact URL is passed back instead of the resolved
      * (local) classpath.
      */
     private final Map<String, String> classAnnotationMap = new ConcurrentHashMap<String, String>();
     private static final Logger logger = LoggerFactory.getLogger(ArtifactRmiLoader.class);
     private static final RMIClassLoaderSpi loader = RMIClassLoader.getDefaultProviderInstance();
 
     private static ArtifactClassLoader artifactClassLoader;
 
     static {
         try {
             Resolver resolver = ResolverHelper.getResolver();
             artifactClassLoader = new ArtifactClassLoader(ClassLoader.getSystemClassLoader(), resolver);
             LoaderHandler.registerCodebaseLoader(artifactClassLoader);
         } catch (ResolverException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public Class<?> loadClass(final String codebase,
                               final String name,
                               final ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
         if (logger.isTraceEnabled()) {
             logger.trace("codebase: {}, name: {}, defaultLoader: {}",
                     codebase, name, defaultLoader == null ? "NULL" : defaultLoader.getClass().getName());
         }

         if (codebase != null && !codebase.contains(" "))
             try {
                 artifactClassLoader.addURI(new URI(codebase));
             } catch (URISyntaxException e) {
                 logger.warn("Error while resolving {} with codebase {}", name, codebase, e);
                 throw new MalformedURLException(e.getMessage());
             }
 
         return loader.loadClass(codebase, name, defaultLoader);
     }
 
     @Override
     public Class<?> loadProxyClass(final String codebase,
                                    final String[] interfaces,
                                    final ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
         if (logger.isTraceEnabled()) {
             logger.trace("codebase: {}, interfaces: {}, defaultLoader: {}",
                     codebase, Arrays.toString(interfaces), defaultLoader == null ? "NULL" : defaultLoader.getClass().getName());
         }
         if (codebase != null && !codebase.contains(" "))
             try {
                 artifactClassLoader.addURI(new URI(codebase));
             } catch (URISyntaxException e) {
                 logger.warn("Error while resolving {} with codebase {}", interfaces, codebase, e);
                 throw new MalformedURLException(e.getMessage());
             }
 
         return loader.loadProxyClass(codebase, interfaces, defaultLoader);
     }
 
     @Override
     public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
         return loader.getClassLoader(codebase);
     }
 
     @Override
     public String getClassAnnotation(final Class<?> aClass) {
         return loader.getClassAnnotation(aClass);
     }
 }
