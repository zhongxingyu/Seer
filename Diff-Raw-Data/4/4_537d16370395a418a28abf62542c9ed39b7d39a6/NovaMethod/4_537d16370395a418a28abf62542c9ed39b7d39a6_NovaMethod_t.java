 /**
  * Copyright (C) 2009-2013 Dell, Inc.
  * See annotations for authorship information
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
 
 package org.dasein.cloud.openstack.nova.os;
 
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.openstack.nova.os.ext.hp.cdn.HPCDN;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.HashMap;
 import java.util.Map;
 
 public class NovaMethod extends AbstractMethod {
     public NovaMethod(NovaOpenStack provider) { super(provider); }
     
     public void deleteServers(@Nonnull String resource, @Nonnull String resourceId) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getComputeUrl();
         
         if( endpoint == null ) {
             throw new CloudException("No compute endpoint exists");
         }
         delete(context.getAuthToken(), endpoint, resource + "/" + resourceId);
     }
 
     public void deleteNetworks(@Nonnull String resource, @Nonnull String resourceId) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getNetworkUrl();
 
         if( endpoint == null ) {
             throw new CloudException("No network endpoint exists");
         }
         delete(context.getAuthToken(), endpoint, resource + "/" + resourceId);
     }
     
     public @Nullable JSONObject getServers(@Nonnull String resource, @Nullable String resourceId, boolean suffix) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getComputeUrl();
         
         if( endpoint == null ) {
             throw new CloudException("No compute URL has been established in " + context.getMyRegion());
         }
         if( resourceId != null ) {
             resource = resource + "/" + resourceId;
         }
         else if( suffix ) {
             resource = resource + "/detail";
         }
         String response = getString(context.getAuthToken(), endpoint, resource);
 
         if( response == null ) {
             return null;
         }
         try {
             return new JSONObject(response);
         }
         catch( JSONException e ) {
             throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", response);
         }
     }
 
     public @Nullable JSONObject getNetworks(@Nonnull String resource, @Nullable String resourceId, boolean suffix) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getNetworkUrl();
 
         if( endpoint == null ) {
             throw new CloudException("No network URL has been established in " + context.getMyRegion());
         }
         if( resourceId != null ) {
             resource = resource + "/" + resourceId;
         }
         else if( suffix ) {
             resource = resource + "/detail";
         }

        if (resource != null && (!endpoint.endsWith("/") && !resource.startsWith("/"))) {
            endpoint = endpoint+"/";
        }
         String response = getString(context.getAuthToken(), endpoint, resource);
 
         if( response == null ) {
             return null;
         }
         try {
             return new JSONObject(response);
         }
         catch( JSONException e ) {
             throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", response);
         }
     }
 
     public @Nullable String postServersForString(@Nonnull String resource, @Nullable String resourceId, @Nonnull JSONObject body, boolean suffix) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
 
         if( resourceId != null ) {
             resource = resource + "/" + (suffix ? (resourceId + "/action") : resourceId);
         }
         String computeEndpoint = context.getComputeUrl();
 
         if( computeEndpoint == null ) {
             throw new CloudException("No compute endpoint exists");
         }
         return postString(context.getAuthToken(), computeEndpoint, resource, body.toString());
     }
 
     public @Nullable JSONObject postServers(@Nonnull String resource, @Nullable String resourceId, @Nonnull JSONObject body, boolean suffix) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
 
         if( resourceId != null ) {
             resource = resource + "/" + (suffix ? (resourceId + "/action") : resourceId);
         }
         String computeEndpoint = context.getComputeUrl();
 
         if( computeEndpoint == null ) {
             throw new CloudException("No compute endpoint exists");
         }
         String response = postString(context.getAuthToken(), computeEndpoint, resource, body.toString());
 
         if( response == null ) {
             return null;
         }
         try {
             return new JSONObject(response);
         }
         catch( JSONException e ) {
             throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", response);
         }
     }
 
     public @Nullable JSONObject postNetworks(@Nonnull String resource, @Nullable String resourceId, @Nonnull JSONObject body, boolean suffix) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
 
         if( resourceId != null ) {
             resource = resource + "/" + (suffix ? (resourceId + "/action") : resourceId);
         }
         String endpoint = context.getNetworkUrl();
 
         if( endpoint == null ) {
             throw new CloudException("No network endpoint exists");
         }
         String response = postString(context.getAuthToken(), endpoint, resource, body.toString());
 
         if( response == null ) {
             return null;
         }
         try {
             return new JSONObject(response);
         }
         catch( JSONException e ) {
             throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", response);
         }
     }
 
     public @Nullable String getHPCDN(@Nullable String resourceId) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getServiceUrl(HPCDN.SERVICE);
 
         if( endpoint == null ) {
             throw new CloudException("No CDN URL has been established in " + context.getMyRegion());
         }
         return getString(context.getAuthToken(), endpoint, resourceId == null ? "" : ("/" + resourceId));
     }
     
     public void putHPCDN(String container) throws CloudException, InternalException {
         Map<String,String> headers = new HashMap<String, String>();
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getServiceUrl(HPCDN.SERVICE);
 
         if( endpoint == null ) {
             throw new CloudException("No CDN URL has been established in " + context.getMyRegion());
         }
         if( container == null ) {
             throw new InternalException("No container was specified");
         }
         headers.put("X-TTL", "86400");
         putHeaders(context.getAuthToken(), endpoint, "/" + container, headers);
         
         headers = headResource(HPCDN.SERVICE, HPCDN.RESOURCE, container);
         if( headers == null ) {
             throw new CloudException("No container enabled");
         }
     }
 
     public void postHPCDN(String container, Map<String,String> headers) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getServiceUrl(HPCDN.SERVICE);
 
         if( endpoint == null ) {
             throw new CloudException("No CDN URL has been established in " + context.getMyRegion());
         }
         if( container == null ) {
             throw new InternalException("No container was specified");
         }
         postHeaders(context.getAuthToken(), endpoint, "/" + container, headers);
     }
     
     public void deleteHPCDN(String container) throws CloudException, InternalException {
         AuthenticationContext context = provider.getAuthenticationContext();
         String endpoint = context.getServiceUrl(HPCDN.SERVICE);
 
         if( endpoint == null ) {
             throw new CloudException("No CDN URL has been established in " + context.getMyRegion());
         }
         delete(context.getAuthToken(), endpoint, "/" + container);
     }
 }
 
