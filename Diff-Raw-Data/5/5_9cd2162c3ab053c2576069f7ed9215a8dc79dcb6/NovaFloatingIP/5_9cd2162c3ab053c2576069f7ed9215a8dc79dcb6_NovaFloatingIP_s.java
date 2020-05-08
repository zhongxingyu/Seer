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
 
 package org.dasein.cloud.openstack.nova.os.network;
 
 import org.apache.http.HttpStatus;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.AddressType;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.IpAddressSupport;
 import org.dasein.cloud.network.IpForwardingRule;
 import org.dasein.cloud.network.Protocol;
 import org.dasein.cloud.openstack.nova.os.NovaException;
 import org.dasein.cloud.openstack.nova.os.NovaMethod;
 import org.dasein.cloud.openstack.nova.os.NovaOpenStack;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * IP addresses services for Dasein Cloud to access OpenStack Nova floating IPs.
  * @author George Reese (george.reese@imaginary.com)
  * @since 2011.10
  * @version 2011.10
  * @version 2012.04.1 Added some intelligence around features Rackspace does not support
  * @version 2013.02 updated to 2013.02 model
  */
 public class NovaFloatingIP implements IpAddressSupport {
     static private final Logger logger = NovaOpenStack.getLogger(NovaFloatingIP.class, "std");
 
     static public final String QUANTIUM_TARGET = "/floating-ips";
     static public final String NOVA_TARGET     = "/os-floating-ips";
 
     private NovaOpenStack provider;
     
     NovaFloatingIP(NovaOpenStack cloud) {
         provider = cloud;
     }
 
     private String getEndpoint() {
         return NOVA_TARGET;
     }
 
     @Override
     public void assign(@Nonnull String addressId, @Nonnull String serverId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.assign");
         try {
             HashMap<String,Object> json = new HashMap<String,Object>();
             HashMap<String,Object> action = new HashMap<String,Object>();
             IpAddress addr = getIpAddress(addressId);
             
             if( addr == null ) {
                 throw new CloudException("No such IP address: " + addressId);
             }
             //action.put("server", serverId);
             action.put("address",addr.getRawAddress().getIpAddress());
             json.put("addFloatingIp", action);
 
             NovaMethod method = new NovaMethod(provider);
 
             method.postServers("/servers", serverId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void assignToNetworkInterface(@Nonnull String addressId, @Nonnull String nicId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("Network interfaces are not currently supported");
     }
 
     @Override
     public @Nonnull String forward(@Nonnull String addressId, int publicPort, @Nonnull Protocol protocol, int privatePort, @Nonnull String onServerId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("IP forwarding is not currently supported");
     }
 
     @Override
     public @Nullable IpAddress getIpAddress(@Nonnull String addressId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.getIpAddress");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             JSONObject ob = method.getServers(getEndpoint(), addressId, false);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("floating_ip") ) {
                     JSONObject json = ob.getJSONObject("floating_ip");
                     IpAddress addr = toIP(ctx, json);
 
                     if( addr != null ) {
                         return addr;
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("getIpAddress(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for IP address");
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForIpAddress(@Nonnull Locale locale) {
         return "floating IP";
     }
 
     @Override
     public @Nonnull Requirement identifyVlanForVlanIPRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public boolean isAssigned(@Nonnull AddressType type) {
        return type.equals(AddressType.PUBLIC);
     }
 
     @Override
     public boolean isAssigned(@Nonnull IPVersion version) throws CloudException, InternalException {
         return getVersions().contains(version);
     }
 
     @Override
     public boolean isAssignablePostLaunch(@Nonnull IPVersion version) throws CloudException, InternalException {
         return getVersions().contains(version);
     }
 
     @Override
     public boolean isForwarding() {
         return false;
     }
 
     @Override
     public boolean isForwarding(IPVersion version) throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isRequestable(@Nonnull AddressType type) {
        return type.equals(AddressType.PUBLIC);
     }
 
     @Override
     public boolean isRequestable(@Nonnull IPVersion version) throws CloudException, InternalException {
         return getVersions().contains(version);
     }
 
     private boolean verifySupport() throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.verifySupport");
         try {
             NovaMethod method = new NovaMethod(provider);
 
             try {
                 method.getServers(getEndpoint(), null, false);
                 return true;
             }
             catch( CloudException e ) {
                 if( e.getHttpCode() == 404 ) {
                     return false;
                 }
                 throw e;
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @SuppressWarnings("SimplifiableIfStatement")
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(provider, "IpAddress.isSubscribed");
         try {
             if( provider.getMajorVersion() > 1 && provider.getComputeServices().getVirtualMachineSupport().isSubscribed() ) {
                 return verifySupport();
             }
             if( provider.getMajorVersion() == 1 && provider.getMinorVersion() >= 1  &&  provider.getComputeServices().getVirtualMachineSupport().isSubscribed() ) {
                 return verifySupport();
             }
             return false;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<IpAddress> listPrivateIpPool(boolean unassignedOnly) throws InternalException, CloudException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<IpAddress> listPublicIpPool(boolean unassignedOnly) throws InternalException, CloudException {
         return listIpPool(IPVersion.IPV4, unassignedOnly);
     }
 
     @Override
     public @Nonnull Iterable<IpAddress> listIpPool(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.listIpPool");
         try {
             if( !getVersions().contains(version) ) {
                 return Collections.emptyList();
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             JSONObject ob = method.getServers(getEndpoint(), null, false);
             ArrayList<IpAddress> addresses = new ArrayList<IpAddress>();
 
             try {
                 if( ob != null && ob.has("floating_ips") ) {
                     JSONArray list = ob.getJSONArray("floating_ips");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject json = list.getJSONObject(i);
 
                         try {
                             IpAddress addr = toIP(ctx, json);
 
                             if( addr != null ) {
                                 if( !unassignedOnly || addr.getServerId() == null ) {
                                     addresses.add(addr);
                                 }
                             }
                         }
                         catch( JSONException e ) {
                             logger.error("Invalid JSON from cloud: " + e.getMessage());
                             throw new CloudException("Invalid JSON from cloud: " + e.getMessage());
                         }
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("list(): Unable to identify expected values in JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for floating IP in " + ob.toString());
             }
             return addresses;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listIpPoolStatus(@Nonnull IPVersion version) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.listIpPoolStatus");
         try {
             if( !getVersions().contains(version) ) {
                 return Collections.emptyList();
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             JSONObject ob = method.getServers(getEndpoint(), null, false);
             ArrayList<ResourceStatus> addresses = new ArrayList<ResourceStatus>();
 
             try {
                 if( ob != null && ob.has("floating_ips") ) {
                     JSONArray list = ob.getJSONArray("floating_ips");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject json = list.getJSONObject(i);
 
                         try {
                             ResourceStatus addr = toStatus(json);
 
                             if( addr != null ) {
                                 addresses.add(addr);
                             }
                         }
                         catch( JSONException e ) {
                             throw new CloudException("Invalid JSON from cloud: " + e.getMessage());
                         }
                     }
                 }
             }
             catch( JSONException e ) {
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for floating IP in " + ob.toString());
             }
             return addresses;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<IpForwardingRule> listRules(@Nonnull String addressId) throws InternalException, CloudException {
         return Collections.emptyList();
     }
 
     static private volatile List<IPVersion> versions;
 
     private Collection<IPVersion> getVersions() {
         if( versions == null ) {
             ArrayList<IPVersion> tmp = new ArrayList<IPVersion>();
 
             tmp.add(IPVersion.IPV4);
             //tmp.add(IPVersion.IPV6);     TODO: when there's API support for IPv6
             versions = Collections.unmodifiableList(tmp);
         }
         return versions;
     }
 
     @Override
     public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
         return getVersions();
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     @Override
     public void releaseFromPool(@Nonnull String addressId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.releaseFromPool");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             long timeout = System.currentTimeMillis() + CalendarWrapper.HOUR;
 
             do {
                 try {
                     method.deleteServers(getEndpoint(), addressId);
                     return;
                 }
                 catch( NovaException e ) {
                     if( e.getHttpCode() != HttpStatus.SC_CONFLICT ) {
                         throw e;
                     }
                 }
                 try { Thread.sleep(CalendarWrapper.MINUTE); }
                 catch( InterruptedException e ) { /* ignore */ }
             } while( System.currentTimeMillis() < timeout );
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.releaseFromServer");
         try {
             HashMap<String,Object> json = new HashMap<String,Object>();
             HashMap<String,Object> action = new HashMap<String,Object>();
             IpAddress addr = getIpAddress(addressId);
 
             if( addr == null ) {
                 throw new CloudException("No such IP address: " + addressId);
             }
             String serverId = addr.getServerId();
             
             if( serverId == null ) {
                 throw new CloudException("IP address " + addressId + " is not attached to a server");
             }
             //action.put("server", serverId);
             action.put("address", addr.getRawAddress().getIpAddress());
             json.put("removeFloatingIp", action);
 
             NovaMethod method = new NovaMethod(provider);
 
             method.postServers("/servers", serverId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     private Iterable<String> listPools() throws CloudException, InternalException {
         NovaMethod method = new NovaMethod(provider);
         JSONObject ob = method.getServers("/os-floating-ip-pools", null, false);
         ArrayList<String> pools = new ArrayList<String>();
         ArrayList<String> tmp = new ArrayList<String>();
 
 
         try {
             if( ob != null && ob.has("floating_ip_pools") ) {
                 JSONArray list = ob.getJSONArray("floating_ip_pools");
 
                 for( int i=0; i<list.length(); i++ ) {
                     JSONObject p = list.getJSONObject(i);
 
                     if( p.has("name") ) {
                         String n = p.getString("name");
 
                         if( n.equals("default") ) {
                             pools.add(n);
                         }
                         else {
                             tmp.add(n);
                         }
                     }
                 }
                 pools.addAll(tmp);
             }
         }
         catch( JSONException e ) {
             throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for IP address");
         }
         return pools;
     }
 
     @Override
     public @Nonnull String request(@Nonnull AddressType typeOfAddress) throws InternalException, CloudException {
         if( typeOfAddress.equals(AddressType.PRIVATE) ) {
             throw new OperationNotSupportedException("Requesting private IP addresses is not supported by OpenStack");
         }
         return request(IPVersion.IPV4);
     }
 
     @Override
     public @Nonnull String request(@Nonnull IPVersion version) throws InternalException, CloudException {
         try {
             return request(version, null);
         }
         catch( NovaException e ) {
             if( e.getHttpCode() == 404 ) {
                 for( String pool : listPools() ) {
                     try {
                         return request(version, pool);
                     }
                     catch( CloudException ignore ) {
                         // ignore
                     }
                 }
             }
             throw e;
         }
     }
 
     private @Nonnull String request(@Nonnull IPVersion version, @Nullable String pool) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.request");
         try {
             if( !getVersions().contains(version) ) {
                 throw new OperationNotSupportedException("Cannot request an IPv6 IP address at this time");
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
 
             if( pool != null ) {
                 wrapper.put("pool", pool);
             }
 
             NovaMethod method = new NovaMethod(provider);
 
             if( pool != null ) {
                 wrapper.put("pool", pool);
             }
             JSONObject result = method.postServers(getEndpoint(), null, new JSONObject(wrapper), false);
 
             if( result != null && result.has("floating_ip") ) {
                 try {
                     JSONObject ob = result.getJSONObject("floating_ip");
                     IpAddress addr = toIP(ctx, ob);
 
                     if( addr != null ) {
                         return addr.getProviderIpAddressId();
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("create(): Unable to understand create response: " + e.getMessage());
                     if( logger.isTraceEnabled() ) {
                         e.printStackTrace();
                     }
                     throw new CloudException(e);
                 }
             }
             logger.error("create(): No IP address was created by the create attempt, and no error was returned");
             throw new CloudException("No IP address was created");
 
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String requestForVLAN(@Nonnull IPVersion version) throws InternalException, CloudException {
         throw new OperationNotSupportedException(provider.getCloudName() + " does not support static IP addresses for VLANs");
     }
 
     @Override
     public @Nonnull String requestForVLAN(@Nonnull IPVersion version, @Nonnull String vlanId) throws InternalException, CloudException {
         throw new OperationNotSupportedException(provider.getCloudName() + " does not support static IP addresses for VLANs");
     }
 
     @Override
     public void stopForward(@Nonnull String ruleId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("Forwarding not supported");
     }
 
     @Override
     public boolean supportsVLANAddresses(@Nonnull IPVersion ofVersion) throws InternalException, CloudException {
         return false;
     }
 
     private IpAddress toIP(ProviderContext ctx, JSONObject json) throws JSONException {
         if(json == null ) {
             return null;
         }
         String regionId = ctx.getRegionId();
 
         IpAddress address = new IpAddress();
 
         if( regionId != null ) {
             address.setRegionId(regionId);
         }
         address.setServerId(null);
         address.setProviderLoadBalancerId(null);
         address.setAddressType(AddressType.PUBLIC);
 
         String id = ((json.has("id") && !json.isNull("id")) ? json.getString("id") : null);
         String ip = ((json.has("ip") && !json.isNull("ip")) ? json.getString("ip") : null);
         String server = ((json.has("instance_id") && !json.isNull("instance_id")) ? json.getString("instance_id") : null);
         if( id != null ) {
             address.setIpAddressId(id);
         }
         if( server != null ) {
             address.setServerId(server);
         }
         if( ip != null ) {
             address.setAddress(ip);
         }
         if( id == null || ip == null ) {
             return null;
         }
         address.setVersion(IPVersion.IPV4);
         return address;
     }
 
     private ResourceStatus toStatus(JSONObject json) throws JSONException {
         if( json == null ) {
             return null;
         }
         Boolean available = null;
         String id;
 
         id = (json.has("id") ? json.getString("id") : null);
         if( json.has("instance_id") ) {
             String instance = json.getString("instance_id");
 
             available = !(instance != null && instance.length() > 0);
         }
         if( id == null ) {
             return null;
         }
         return new ResourceStatus(id, available == null ? true : available);
     }
 }
