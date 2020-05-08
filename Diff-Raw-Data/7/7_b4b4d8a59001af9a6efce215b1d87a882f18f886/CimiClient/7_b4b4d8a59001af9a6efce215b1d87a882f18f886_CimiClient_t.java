 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 
 package org.ow2.sirocco.apis.rest.cimi.sdk;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.ws.rs.core.MediaType;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCloudEntryPoint;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiJob;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 import com.sun.jersey.api.json.JSONConfiguration;
 
 /**
  * Root handle representing a session with a CIMI provider and through which all
  * operations can be invoked on CIMI entities
  */
 public class CimiClient {
     public static final MediaType DEFAULT_MEDIA_TYPE = MediaType.APPLICATION_JSON_TYPE;
 
     private static final String CIMI_QUERY_EXPAND_KEYWORD = "$expand";
 
     private static final String CIMI_QUERY_FILTER_KEYWORD = "$filter";
 
     private static final String CIMI_QUERY_FIRST_KEYWORD = "$first";
 
     private static final String CIMI_QUERY_LAST_KEYWORD = "$last";
 
     private static final String CIMI_QUERY_SELECT_KEYWORD = "$select";
 
     private static final String CIMICLIENT_AUTH_PLUGIN_CLASS_PROP = "CIMICLIENT_AUTH_PLUGIN_CLASS";
 
     private static final String DEFAULT_CIMICLIENT_AUTH_PLUGIN_CLASS = "org.ow2.sirocco.apis.rest.cimi.sdk.auth.BasicAuthPlugin";
 
     /**
      * Contains options for connecting to a CIMI provider
      */
     public static class Options {
         private boolean debug;
 
         private MediaType mediaType;
 
         private Options() {
 
         }
 
         /**
          * Returns a new set of default options
          * 
          * @return
          */
         public static Options build() {
             return new Options();
         }
 
         /**
         * Turns on or off logging of HTTP messages on standard output
          * 
          * @param debug true if logging is desired
          * @return Options object
          */
         public Options setDebug(final boolean debug) {
             this.debug = debug;
             return this;
         }
 
         /**
          * Sets the media type used for HTTP requests and responses
          * 
          * @param mediaType either XML or JSON
          */
        public Options setMediaType(final MediaType mediaType) {
             this.mediaType = mediaType;
            return this;
         }
 
     }
 
     static class CimiResult<E> {
         final CimiJob job;
 
         final E resource;
 
         public CimiResult(final CimiJob job, final E resource) {
             super();
             this.job = job;
             this.resource = resource;
         }
 
         public CimiJob getJob() {
             return this.job;
         }
 
         public E getResource() {
             return this.resource;
         }
 
     }
 
     private WebResource webResource;
 
     private MediaType mediaType = CimiClient.DEFAULT_MEDIA_TYPE;
 
     private String cimiEndpointUrl;
 
     CimiCloudEntryPoint cloudEntryPoint;
 
     private String userName;
 
     private String password;
 
     private LoggingFilter loggingFilter = new LoggingFilter();
 
     private Map<String, String> authenticationHeaders;
 
     String extractPath(final String href) {
         if (href.startsWith("http")) {
             return href.substring(this.cloudEntryPoint.getBaseURI().length());
         } else {
             return href;
         }
     }
 
     String getMachinesPath() {
         return this.extractPath(this.cloudEntryPoint.getMachines().getHref());
     }
 
     String getMachineImagesPath() {
         return this.extractPath(this.cloudEntryPoint.getMachineImages().getHref());
     }
 
     String getMachineTemplatesPath() {
         return this.extractPath(this.cloudEntryPoint.getMachineTemplates().getHref());
     }
 
     String getMachineConfigurationsPath() {
         return this.extractPath(this.cloudEntryPoint.getMachineConfigs().getHref());
     }
 
     String getVolumesPath() {
         return this.extractPath(this.cloudEntryPoint.getVolumes().getHref());
     }
 
     String getVolumeImagesPath() {
         return this.extractPath(this.cloudEntryPoint.getVolumeImages().getHref());
     }
 
     String getVolumeTemplatesPath() {
         return this.extractPath(this.cloudEntryPoint.getVolumeTemplates().getHref());
     }
 
     String getVolumeConfigurationsPath() {
         return this.extractPath(this.cloudEntryPoint.getVolumeConfigs().getHref());
     }
 
     String getCredentialsPath() {
         return this.extractPath(this.cloudEntryPoint.getCredentials().getHref());
     }
 
     String getCredentialTemplatesPath() {
         return this.extractPath(this.cloudEntryPoint.getCredentialTemplates().getHref());
     }
 
     String getJobsPath() {
         return this.extractPath(this.cloudEntryPoint.getJobs().getHref());
     }
 
     String getSystemsPath() {
         return this.extractPath(this.cloudEntryPoint.getSystems().getHref());
     }
 
     String getSystemTemplatesPath() {
         return this.extractPath(this.cloudEntryPoint.getSystemTemplates().getHref());
     }
 
     private void handleResponseStatus(final ClientResponse response) throws CimiProviderException {
         if (response.getStatus() == 400) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException(message);
         } else if (response.getStatus() == 401) {
             throw new CimiProviderException("Unauthorized");
         } else if (response.getStatus() == 403) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException("Forbidden: " + message);
         } else if (response.getStatus() == 404) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException("Resource not found: " + message);
         } else if (response.getStatus() == 409) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException(message);
         } else if (response.getStatus() == 503) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException("Service unavailable: " + message);
         } else if (response.getStatus() == 500) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException("Internal error: " + message);
         } else if (response.getStatus() == 501) {
             String message = response.getEntity(String.class);
             throw new CimiProviderException("Not implemented: " + message);
         }
     }
 
     private void initAuthenticationHeaders(final String userName, final String password) throws CimiClientException {
         String authPluginClassName = java.lang.System.getProperty(CimiClient.CIMICLIENT_AUTH_PLUGIN_CLASS_PROP);
         if (authPluginClassName == null) {
             authPluginClassName = CimiClient.DEFAULT_CIMICLIENT_AUTH_PLUGIN_CLASS;
         }
         Class<?> authPluginClazz = null;
         try {
             authPluginClazz = Class.forName(authPluginClassName);
         } catch (ClassNotFoundException ex) {
             throw new CimiClientException("Cannot find auth pluging class " + authPluginClassName);
         }
         AuthPlugin authPlugin = null;
         try {
             authPlugin = (AuthPlugin) authPluginClazz.newInstance();
         } catch (Exception ex) {
             throw new CimiClientException("Cannot create auth plugin " + authPluginClassName + " " + ex.getMessage());
         }
         this.authenticationHeaders = authPlugin.authenticate(userName, password);
     }
 
     private WebResource.Builder addAuthenticationHeaders(final WebResource resource) {
         WebResource.Builder builder = resource.getRequestBuilder();
         for (Entry<String, String> header : this.authenticationHeaders.entrySet()) {
             builder = builder.header(header.getKey(), header.getValue());
         }
         return builder;
     }
 
     private CimiClient(final String cimiEndpointUrl, final String userName, final String password, final Options... optionList)
         throws CimiClientException, CimiProviderException {
         this.cimiEndpointUrl = cimiEndpointUrl;
         this.userName = userName;
         this.password = password;
         this.initAuthenticationHeaders(userName, password);
         ClientConfig config = new DefaultClientConfig();
         config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
         Client client = Client.create(config);
         for (Options options : optionList) {
             if (options.debug) {
                 client.addFilter(this.loggingFilter);
             }
             if (options.mediaType != null) {
                 this.mediaType = options.mediaType;
             }
         }
         try {
             WebResource cepWebResource = client.resource(cimiEndpointUrl).path("/");
 
             ClientResponse response = this.addAuthenticationHeaders(cepWebResource).accept(this.mediaType)
                 .get(ClientResponse.class);
             this.handleResponseStatus(response);
             this.cloudEntryPoint = response.getEntity(CimiCloudEntryPoint.class);
 
             this.webResource = client.resource(this.cloudEntryPoint.getBaseURI());
         } catch (ClientHandlerException e) {
             String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
             throw new CimiClientException(message, e);
         }
     }
 
     /**
      * Changes the media type used for HTTP requests and responses
      * 
      * @param mediaType either XML or JSON
      */
     public void setMediaType(final MediaType mediaType) {
         this.mediaType = mediaType;
     }
 
     /**
      * Login to a CIMI provider with some credentials
      * 
      * @param cimiEndpointUrl URL of the CIMI provider endpoint
      * @param userName user name
      * @param password password
      * @param options options
      * @return
      * @throws CimiClientException raised if login operation fails
      */
     public static CimiClient login(final String cimiEndpointUrl, final String userName, final String password,
         final Options... options) throws CimiClientException {
         return new CimiClient(cimiEndpointUrl, userName, password, options);
     }
 
     <U> U getRequest(final String path, final Class<U> clazz, final QueryParams... queryParams) throws CimiClientException {
         WebResource service = this.webResource.path(path);
         if (queryParams.length > 0) {
             if (queryParams[0].getExpand() != null) {
                 service = service.queryParam(CimiClient.CIMI_QUERY_EXPAND_KEYWORD, queryParams[0].getExpand());
             }
             if (queryParams[0].getSelect() != null) {
                 service = service.queryParam(CimiClient.CIMI_QUERY_SELECT_KEYWORD, queryParams[0].getSelect());
             }
             if (queryParams[0].getFilter() != null) {
                 service = service.queryParam(CimiClient.CIMI_QUERY_FILTER_KEYWORD, queryParams[0].getFilter());
             }
             if (queryParams[0].getFirst() != null) {
                 service = service.queryParam(CimiClient.CIMI_QUERY_FIRST_KEYWORD, Integer.toString(queryParams[0].getFirst()));
             }
             if (queryParams[0].getLast() != null) {
                 service = service.queryParam(CimiClient.CIMI_QUERY_LAST_KEYWORD, Integer.toString(queryParams[0].getLast()));
             }
         }
         try {
             ClientResponse response = this.addAuthenticationHeaders(service).accept(this.mediaType).get(ClientResponse.class);
             this.handleResponseStatus(response);
             U cimiObject = response.getEntity(clazz);
             return cimiObject;
         } catch (ClientHandlerException e) {
             throw new CimiClientException(e.getMessage(), e);
         }
     }
 
     <U> CimiJob actionRequest(final String href, final U input) throws CimiClientException {
         WebResource service = this.webResource.path(this.extractPath(href));
         try {
             ClientResponse response = this.addAuthenticationHeaders(service).accept(this.mediaType)
                 .entity(input, this.mediaType).post(ClientResponse.class);
             this.handleResponseStatus(response);
             if (response.getStatus() == 202) {
                 if (response.getLength() > 0
                     || (response.getType().equals(MediaType.APPLICATION_XML_TYPE) || response.getType().equals(
                         MediaType.APPLICATION_JSON_TYPE))) {
                     return response.getEntity(CimiJob.class);
                 }
             }
         } catch (ClientHandlerException e) {
             throw new CimiClientException(e.getMessage(), e);
         }
         return null;
     }
 
     <U, V> CimiResult<V> postCreateRequest(final String ref, final U input, final Class<V> outputClazz)
         throws CimiClientException {
         WebResource service = this.webResource.path(this.extractPath(ref));
         try {
             ClientResponse response = this.addAuthenticationHeaders(service).accept(this.mediaType)
                 .entity(input, this.mediaType).post(ClientResponse.class);
             this.handleResponseStatus(response);
             CimiResult<V> createResult = null;
             if (response.getStatus() == 201) {
                 V resource = null;
                 if (response.getLength() > 0
                     || (response.getType().equals(MediaType.APPLICATION_XML_TYPE) || response.getType().equals(
                         MediaType.APPLICATION_JSON_TYPE))) {
                     resource = response.getEntity(outputClazz);
                 }
                 createResult = new CimiResult<V>(null, resource);
             } else if (response.getStatus() == 202) {
                 CimiJob job = response.getEntity(CimiJob.class);
                 createResult = new CimiResult<V>(job, null);
             }
             return createResult;
         } catch (ClientHandlerException e) {
             throw new CimiClientException(e.getMessage(), e);
         }
     }
 
     <V> CimiResult<V> partialUpdateRequest(final String href, final V input, final String attributes)
         throws CimiClientException {
         WebResource service = this.webResource.path(this.extractPath(href));
         service = service.queryParam(CimiClient.CIMI_QUERY_SELECT_KEYWORD, attributes);
         try {
             ClientResponse response = this.addAuthenticationHeaders(service).accept(this.mediaType)
                 .entity(input, this.mediaType).put(ClientResponse.class);
             this.handleResponseStatus(response);
             CimiResult<V> updateResult = null;
             if (response.getStatus() == 200) {
                 V resource = null;
                 if (response.getLength() > 0) {
                     resource = (V) response.getEntity(input.getClass());
                 }
                 updateResult = new CimiResult<V>(null, resource);
             } else if (response.getStatus() == 202) {
                 CimiJob job = response.getEntity(CimiJob.class);
                 updateResult = new CimiResult<V>(job, null);
             }
             return updateResult;
         } catch (ClientHandlerException e) {
             throw new CimiClientException(e.getMessage(), e);
         }
     }
 
     CimiJob deleteRequest(final String id) throws CimiClientException {
         WebResource service = this.webResource.path(this.extractPath(id));
         try {
             ClientResponse response = this.addAuthenticationHeaders(service).accept(this.mediaType)
                 .delete(ClientResponse.class);
             this.handleResponseStatus(response);
             if (response.getStatus() == 202) {
                 CimiJob job = response.getEntity(CimiJob.class);
                 return job;
             } else {
                 return null;
             }
         } catch (ClientHandlerException e) {
             throw new CimiClientException(e.getMessage(), e);
         }
     }
 
     <U> U getCimiObjectByReference(final String ref, final Class<U> clazz, final QueryParams... queryParams)
         throws CimiClientException {
         return this.getRequest(this.extractPath(ref), clazz, queryParams);
     }
 
 }
