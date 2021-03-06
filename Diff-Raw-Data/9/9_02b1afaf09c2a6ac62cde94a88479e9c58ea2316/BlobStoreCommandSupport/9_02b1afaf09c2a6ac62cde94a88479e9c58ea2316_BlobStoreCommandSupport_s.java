 /**
  * Copyright (C) 2011, the original authors
  *
  * ====================================================================
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
  * ====================================================================
  */
 package org.jclouds.karaf.commands.blobstore;
 
 import com.google.common.base.Strings;
 import com.google.common.io.ByteStreams;
 import org.apache.felix.gogo.commands.Option;
 import org.apache.karaf.shell.console.AbstractAction;
 import org.jclouds.ContextBuilder;
 import org.jclouds.apis.ApiMetadata;
 import org.jclouds.blobstore.BlobStore;
 import org.jclouds.blobstore.BlobStoreContext;
 import org.jclouds.blobstore.domain.Blob;
 import org.jclouds.blobstore.util.BlobStoreUtils;
 import org.jclouds.karaf.cache.BasicCacheProvider;
 import org.jclouds.karaf.cache.CacheProvider;
 import org.jclouds.karaf.utils.EnvHelper;
 import org.jclouds.karaf.utils.blobstore.BlobStoreHelper;
 import org.jclouds.providers.ProviderMetadata;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author iocanel
  */
 public abstract class BlobStoreCommandSupport extends AbstractAction {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreCommandSupport.class);
 
     public static final String PROVIDERFORMAT = "%-24s %-12s %-12s";
 
     private List<BlobStore> services = new ArrayList<BlobStore>();
 
 
     protected CacheProvider cacheProvider = new BasicCacheProvider();
 
     @Option(name = "--provider", description = "The provider to use.")
     protected String provider;
 
     @Option(name = "--api", description = "The api to use.")
     protected String api;
 
     @Option(name = "--identity", description = "The identity to use for creating a blob store.")
     protected String identity;
 
     @Option(name = "--credential", description = "The credential to use for a blob store.")
     protected String credential;
 
     @Option(name = "--endpoint", description = "The endpoint to use for a blob store.")
     protected String endpoint;
 
     public void setBlobStoreServices(List<BlobStore> services) {
         this.services = services;
     }
 
     protected List<BlobStore> getBlobStoreServices() {
         if (provider == null && api == null) {
             return services;
         } else {
             return Collections.singletonList(getBlobStore());
         }
     }
 
     protected BlobStore getBlobStore() {
         if (services != null && services.size() == 1) {
             return services.get(0);
         }
         BlobStore blobStore = null;
         String providerValue = EnvHelper.getBlobStoreProvider(provider);
         String apiValue = EnvHelper.getBlobStoreApi(api);
         String identityValue = EnvHelper.getBlobStoreIdentity(identity);
         String credentialValue = EnvHelper.getBlobStoreCredential(credential);
         String endpointValue = EnvHelper.getBlobStoreEndpoint(endpoint);
 
        boolean canCreateService = (!Strings.isNullOrEmpty(providerValue) || !Strings.isNullOrEmpty(providerValue))
                 && !Strings.isNullOrEmpty(identityValue) && !Strings.isNullOrEmpty(credentialValue);
 
         String providerOrApiValue = !Strings.isNullOrEmpty(providerValue) ? providerValue : apiValue;
 
         try {
             blobStore = BlobStoreHelper.getBlobStore(providerOrApiValue, services);
         } catch (Throwable t) {
             if (!canCreateService) {
                 throw new RuntimeException(t.getMessage());
             }
         }
         if (blobStore == null && canCreateService) {
             ContextBuilder builder = ContextBuilder.newBuilder(providerOrApiValue).credentials(identityValue, credentialValue);
             if (!Strings.isNullOrEmpty(endpointValue)) {
                 builder = builder.endpoint(endpoint);
             }
             BlobStoreContext context = builder.build(BlobStoreContext.class);
             blobStore = context.getBlobStore();
         }
         return blobStore;
     }
 
     /**
      * Reads an Object from the blob store.
      *
      * @param containerName
      * @param blobName
      * @return
      */
     public Object read(String containerName, String blobName) {
         Object result = null;
         ObjectInputStream ois = null;
 
         BlobStore blobStore = getBlobStore();
         blobStore.createContainerInLocation(null, containerName);
 
         InputStream is = blobStore.getBlob(containerName, blobName).getPayload().getInput();
 
         try {
             ois = new ObjectInputStream(is);
             result = ois.readObject();
         } catch (IOException e) {
             LOGGER.error("Error reading object.", e);
         } catch (ClassNotFoundException e) {
             LOGGER.error("Error reading object.", e);
         } finally {
             if (ois != null) {
                 try {
                     ois.close();
                 } catch (IOException e) {
                 }
             }
 
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                 }
             }
         }
         return result;
     }
 
 
     /**
      * Returns an InputStream to a {@link Blob}.
      *
      * @param containerName
      * @param blobName
      * @return
      */
     public InputStream getBlobInputStream(BlobStore blobStore, String containerName, String blobName) throws Exception {
         if (blobStore.blobExists(containerName, blobName)) {
             return getBlobStore().getBlob(containerName, blobName).getPayload().getInput();
         } else {
             throw new Exception("Blob " + blobName + " does not exist in conatiner " + containerName + ".");
         }
     }
 
     /**
      * Writes to the {@link Blob} by serializing an Object.
      *
      * @param containerName
      * @param blobName
      * @param object
      */
     public void write(String containerName, String blobName, Object object) {
         BlobStore blobStore = getBlobStore();
         Blob blob = blobStore.blobBuilder(blobName).build();
         blob.setPayload(toBytes(object));
         blobStore.putBlob(containerName, blob);
     }
 
     /**
      * Writes to the {@link Blob} using an InputStream.
      *
      * @param bucket
      * @param blobName
      * @param is
      */
     public void write(String bucket, String blobName, InputStream is) {
         BlobStore blobStore = getBlobStore();
         try {
             if (blobName.contains("/")) {
                 String directory = BlobStoreUtils.parseDirectoryFromPath(blobName);
                 if (!Strings.isNullOrEmpty(directory)) {
                     blobStore.createDirectory(bucket, directory);
                 }
             }
 
             Blob blob = blobStore.blobBuilder(blobName).payload(ByteStreams.toByteArray(is)).build();
             blobStore.putBlob(bucket, blob);
             is.close();
         } catch (Exception ex) {
             LOGGER.warn("Error closing input stream.", ex);
         }
     }
 
     public byte[] toBytes(Object object) {
         byte[] result = null;
 
         if (object instanceof byte[]) {
             return (byte[]) object;
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = null;
 
         try {
             oos = new ObjectOutputStream(baos);
             oos.writeObject(object);
             result = baos.toByteArray();
         } catch (IOException e) {
             LOGGER.error("Error while writing blob", e);
         } finally {
             if (oos != null) {
                 try {
                     oos.close();
                 } catch (IOException e) {
                 }
             }
 
             if (baos != null) {
                 try {
                     baos.close();
                 } catch (IOException e) {
                 }
             }
         }
         return result;
     }
 
     /**
      * Reads a bye[] from a URL.
      *
      * @param url
      * @return
      */
     public byte[] readFromUrl(URL url) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataInputStream dis = null;
         try {
             dis = new DataInputStream(url.openStream());
             int size = 0;
             while ((size = dis.available()) > 0) {
                 byte[] buffer = new byte[size];
                 baos.write(buffer);
             }
             return baos.toByteArray();
         } catch (IOException e) {
             LOGGER.warn("Failed to read from stream.", e);
         } finally {
             if (dis != null) {
                 try {
                     dis.close();
                 } catch (Exception e) {
                     //Ignore
                 }
             }
 
             if (baos != null) {
                 try {
                     baos.close();
                 } catch (Exception e) {
                     //Ignore
                 }
             }
 
         }
         return new byte[0];
     }
 
     protected void printBlobStoreProviders(Map<String, ProviderMetadata> providers, List<BlobStore> blobStores, String indent, PrintStream out) {
         out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
         for (String provider : providers.keySet()) {
             boolean registered = false;
             for (BlobStore blobStore : blobStores) {
                 if (blobStore.getContext().unwrap().getId().equals(provider)) {
                     registered = true;
                     break;
                 }
             }
             out.println(String.format(PROVIDERFORMAT, provider, "blobstore", registered));
         }
     }
 
     protected void printBlobStoreApis(Map<String, ApiMetadata> apis, List<BlobStore> blobStores, String indent, PrintStream out) {
         out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
         for (String provider : apis.keySet()) {
             boolean registered = false;
             for (BlobStore blobStore : blobStores) {
                 if (blobStore.getContext().unwrap().getId().equals(provider)) {
                     registered = true;
                     break;
                 }
             }
             out.println(String.format(PROVIDERFORMAT, provider, "blobstore", registered));
         }
     }
 
     public CacheProvider getCacheProvider() {
         return cacheProvider;
     }
 
     public void setCacheProvider(CacheProvider cacheProvider) {
         this.cacheProvider = cacheProvider;
     }
 }
