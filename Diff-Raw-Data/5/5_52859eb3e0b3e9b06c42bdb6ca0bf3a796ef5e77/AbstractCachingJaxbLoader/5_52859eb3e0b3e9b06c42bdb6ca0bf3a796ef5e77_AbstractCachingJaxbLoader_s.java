 /**
  * Copyright 2012, Board of Regents of the University of
  * Wisconsin System. See the NOTICE file distributed with
  * this work for additional information regarding copyright
  * ownership. Board of Regents of the University of Wisconsin
  * System licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package edu.wisc.mum.status.dao;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.List;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.UnmarshalException;
 import javax.xml.bind.Unmarshaller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.io.Resource;
 import org.springframework.util.Assert;
 
 /**
  * Base logic for loading unmarshalling an XML document via JAXB and only reloading the cached object model when needed.
  * The class attempts to monitor the lastModified date of the {@link Resource} to determine when to reload. If that fails
  * the resource is reloaded periodically as specified by the {@link #setNoLastModifiedReloadPeriod(long)} property.
  * 
  * The class determines the return type and the base package to use for the {@link JAXBContext#newInstance(String)} call
  * via the loadedType parameter provided to the constructor.
  * 
  * @author Eric Dalquist
  * @version $Revision: 297 $
  */
 public abstract class AbstractCachingJaxbLoader<T> implements InitializingBean, DisposableBean {
     protected final Logger logger = LoggerFactory.getLogger(this.getClass());
     private final JAXBContext jaxbContext;
     private final Class<T> loadedType;
     
     private final ReadWriteLock lock = new ReentrantReadWriteLock();
     
     private Resource mappedXmlResource;
     private WatchService watchService;
     
     private T unmarshalledObject;
     
     protected AbstractCachingJaxbLoader(Class<T> loadedType) {
         Assert.notNull(loadedType, "loadedType can not be null");
         this.loadedType = loadedType;
         
         final String filterDisplayPackage = this.loadedType.getPackage().getName();
         try {
             jaxbContext = JAXBContext.newInstance(filterDisplayPackage);
         }
         catch (JAXBException e) {
             throw new RuntimeException("Failed to create " + JAXBContext.class + " to unmarshal " + this.loadedType, e);
         }
     }
     
 
     public Resource getMappedXmlResource() {
         return mappedXmlResource;
     }
     
     /**
      * The XML resource to load.
      */
     public void setMappedXmlResource(Resource mappedXmlResource) {
         this.mappedXmlResource = mappedXmlResource;
     }
     
     
     @Override
     public void afterPropertiesSet() throws Exception {
         //Setup file watcher
         final URI xmlUri = this.mappedXmlResource.getURI();
         final Path xmlParentPath = Paths.get(xmlUri).getParent();
         watchService = FileSystems.getDefault().newWatchService();
         xmlParentPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
     }
     
     @Override
     public void destroy() throws Exception {
         watchService.close();        
     }
 
     /**
      * Loads and unmarshalls the XML as needed, returning the unmarshalled object
      */
     protected final T getUnmarshalledObject() {
         final WatchKey changedKey = watchService.poll();
         try {
             if (changedKey != null || this.unmarshalledObject == null) {
                 //Read all pending events
                final List<WatchEvent<?>> watchEvents = changedKey.pollEvents();
                 
                 if (this.logger.isDebugEnabled()) {
                    if (this.unmarshalledObject == null) {
                        this.logger.debug("Performing first load of {}", this.loadedType.getName());
                    }
                    else {
                        this.logger.debug("Load of {} triggered by WatchKey {} for events {}", this.loadedType.getName(), changedKey, watchEvents);
                    }
                 }
                 
                 this.lock.writeLock().lock();
                 try {
                     this.unmarshalledObject = readAndUnmarshall();
                     return this.unmarshalledObject;
                 }
                 finally {
                     this.lock.writeLock().unlock();
                 }
             }
             else {
                 this.lock.readLock().lock();
                 try {
                     this.logger.trace("Returning cached instance of {}", this.loadedType.getName());
                     return this.unmarshalledObject;
                 }
                 finally {
                     this.lock.readLock().unlock();
                 }
             }
         }
         finally {
             if (changedKey != null) {
                 changedKey.reset();
             }
         }
     }
     
     private final T readAndUnmarshall() {
         final long start = System.currentTimeMillis();
         
         final Unmarshaller unmarshaller;
         try {
             unmarshaller = jaxbContext.createUnmarshaller();
         }
         catch (JAXBException e) {
             throw new RuntimeException("Failed to create " + Unmarshaller.class + " to unmarshal " + this.loadedType, e);
         }
         
         try (final InputStream xmlInputStream = this.mappedXmlResource.getInputStream()) {
             @SuppressWarnings("unchecked")
             final T unmarshalledObject = (T)unmarshaller.unmarshal(xmlInputStream);
             
             this.postProcessUnmarshalling(unmarshalledObject);
             
             this.logger.debug("Loaded {} in {}ms", this.loadedType.getName(), System.currentTimeMillis() - start);
             return unmarshalledObject;
         } catch (IOException e) {
             throw new RuntimeException("Failed to read XML from " + this.mappedXmlResource, e);
         }
         catch (UnmarshalException e) {
             throw new RuntimeException("Failed to unmarshal XML in " + this.mappedXmlResource + " to " + this.loadedType, e);
         }
         catch (JAXBException e) {
             throw new RuntimeException("Unexpected JAXB error while unmarshalling  " + this.mappedXmlResource, e);
         }
     }
     
     /**
      * Allow sub-classes to do specific handling of of the unmarshalled object before it is returned by a call to
      * {@link #getUnmarshalledObject()} that triggered a reload. If this method throws an exception the reload will
      * fail, the object will not be cached, and the exception will be propagated to the caller of {@link #getUnmarshalledObject()}.
      * 
      * This method is called within the synchronization block of {@link #getUnmarshalledObject()}.
      */
     protected void postProcessUnmarshalling(T unmarshalledObject) {
     }
 }
