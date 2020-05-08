 /**
  * Copyright (C) 2009-2012 enStratus Networks Inc
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
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.Direction;
 import org.dasein.cloud.network.Firewall;
 import org.dasein.cloud.network.FirewallRule;
 import org.dasein.cloud.network.FirewallSupport;
 import org.dasein.cloud.network.Permission;
 import org.dasein.cloud.network.Protocol;
 import org.dasein.cloud.network.RuleTarget;
 import org.dasein.cloud.network.RuleTargetType;
 import org.dasein.cloud.openstack.nova.os.NovaException;
 import org.dasein.cloud.openstack.nova.os.NovaMethod;
 import org.dasein.cloud.openstack.nova.os.NovaOpenStack;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 
 /**
  * Support for OpenStack security groups.
  * @author George Reese (george.reese@imaginary.com)
  * @since 2011.10
  * @version 2011.10
  * @version 2012.04.1 Added some intelligence around features Rackspace does not support
  */
 public class NovaSecurityGroup implements FirewallSupport {
     private NovaOpenStack provider;
 
     NovaSecurityGroup(NovaOpenStack cloud) {
         provider = cloud;
     }
 
     @Override
     public @Nonnull String authorize(@Nonnull String firewallId, @Nullable String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         if( cidr == null ) {
             cidr = "0.0.0.0/0";
         }
         return authorize(firewallId, Direction.INGRESS, cidr, protocol, beginPort, endPort);
     }
 
     @Override
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         Logger logger = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".authorize(" + firewallId + "," + direction + "," + cidr + "," + protocol + "," + beginPort + "," + endPort + ")");
         }
         try {
             if( direction.equals(Direction.EGRESS) ) {
                 throw new OperationNotSupportedException(provider.getCloudName() + " does not support egress rules.");
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
             HashMap<String,Object> json = new HashMap<String,Object>();
             NovaMethod method = new NovaMethod(provider);
 
             json.put("ip_protocol", protocol.name().toLowerCase());
             json.put("from_port", beginPort);
             json.put("to_port", endPort);
             json.put("parent_group_id", firewallId);
             json.put("cidr", cidr);
             wrapper.put("security_group_rule", json);
             JSONObject result = method.postServers("/os-security-group-rules", null, new JSONObject(wrapper), false);
 
             if( result != null && result.has("security_group_rule") ) {
                 try {
                     JSONObject rule = result.getJSONObject("security_group_rule");
 
                     return rule.getString("id");
                 }
                 catch( JSONException e ) {
                     logger.error("Invalid JSON returned from rule creation: " + e.getMessage());
                     throw new CloudException(e);
                 }
             }
             logger.error("authorize(): No firewall rule was created by the create attempt, and no error was returned");
             throw new CloudException("No firewall rule was created");
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT: " + NovaSecurityGroup.class.getName() + ".authorize()");
             }
         }
     }
 
     @Override
     @Deprecated
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String source, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         RuleTarget sourceTarget = RuleTarget.getCIDR(source);
 
         if( direction.equals(Direction.INGRESS) ) {
             return authorize(firewallId, direction, permission, sourceTarget, protocol, RuleTarget.getGlobal(firewallId), beginPort, endPort, 0);
         }
         else {
             return authorize(firewallId, direction, permission, RuleTarget.getGlobal(firewallId), protocol, sourceTarget, beginPort, endPort, 0);
         }
     }
 
     @Override
     @Deprecated
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String source, @Nonnull Protocol protocol, @Nonnull RuleTarget target, int beginPort, int endPort) throws CloudException, InternalException {
         RuleTarget sourceTarget = RuleTarget.getCIDR(source);
 
         if( direction.equals(Direction.INGRESS) ) {
             return authorize(firewallId, direction, permission, sourceTarget, protocol, target, beginPort, endPort, 0);
         }
         else {
             return authorize(firewallId, direction, permission, target, protocol, sourceTarget, beginPort, endPort, 0);
         }
     }
 
     @Override
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull RuleTarget sourceEndpoint, @Nonnull Protocol protocol, @Nonnull RuleTarget destinationEndpoint, int beginPort, int endPort, @Nonnegative int precedence) throws CloudException, InternalException {
         Logger logger = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".authorize(" + firewallId + "," + direction + "," + permission + "," + sourceEndpoint + "," + protocol + "," + destinationEndpoint + "," + beginPort + "," + endPort + "," + precedence + ")");
         }
         try {
             if( direction.equals(Direction.EGRESS) ) {
                 throw new OperationNotSupportedException(provider.getCloudName() + " does not support egress rules.");
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
             HashMap<String,Object> json = new HashMap<String,Object>();
             NovaMethod method = new NovaMethod(provider);
 
             json.put("ip_protocol", protocol.name().toLowerCase());
             json.put("from_port", beginPort);
             json.put("to_port", endPort);
             json.put("parent_group_id", firewallId);
             switch( sourceEndpoint.getRuleTargetType() ) {
                 case CIDR: json.put("cidr", sourceEndpoint.getCidr()); break;
                 case VLAN: throw new OperationNotSupportedException("Cannot target VLANs with firewall rules");
                 case VM: throw new OperationNotSupportedException("Cannot target virtual machines with firewall rules");
                 case GLOBAL:
                     HashMap<String,Object> g = new HashMap<String, Object>();
                     Firewall targetGroup = getFirewall(sourceEndpoint.getProviderFirewallId());
 
                     if( targetGroup == null ) {
                         throw new CloudException("No such source endpoint firewall: " + sourceEndpoint.getProviderFirewallId());
                     }
                     g.put("tenant_id", ctx.getAccountNumber());
                     g.put("name", targetGroup.getName());
                     json.put("group", g);
                     break;
             }
 
             wrapper.put("security_group_rule", json);
             JSONObject result = method.postServers("/os-security-group-rules", null, new JSONObject(wrapper), false);
 
             if( result != null && result.has("security_group_rule") ) {
                 try {
                     JSONObject rule = result.getJSONObject("security_group_rule");
 
                     return rule.getString("id");
                 }
                 catch( JSONException e ) {
                     logger.error("Invalid JSON returned from rule creation: " + e.getMessage());
                     throw new CloudException(e);
                 }
             }
             logger.error("authorize(): No firewall rule was created by the create attempt, and no error was returned");
             throw new CloudException("No firewall rule was created");
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT: " + NovaSecurityGroup.class.getName() + ".authorize()");
             }
         }
     }
 
     @Override
     public @Nonnull String create(@Nonnull String name, @Nonnull String description) throws InternalException, CloudException {
         Logger logger = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".create(" + name + "," + description + ")");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 logger.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
             HashMap<String,Object> json = new HashMap<String,Object>();
             NovaMethod method = new NovaMethod(provider);
 
             json.put("name", name);
             json.put("description", description);
             wrapper.put("security_group", json);
             JSONObject result = method.postServers("/os-security-groups", null, new JSONObject(wrapper), false);
 
             if( result != null && result.has("security_group") ) {
                 try {
                     JSONObject ob = result.getJSONObject("security_group");
                     Firewall fw = toFirewall(ctx, ob);
 
                     if( fw != null ) {
                         String id = fw.getProviderFirewallId();
                         
                         if( id != null ) {
                             return id;
                         }
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
             logger.error("create(): No firewall was created by the create attempt, and no error was returned");
             throw new CloudException("No firewall was created");
 
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT: " + NovaSecurityGroup.class.getName() + ".create()");
             }
         }
     }
 
     @Override
     public @Nonnull String createInVLAN(@Nonnull String name, @Nonnull String description, @Nonnull String providerVlanId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("VLAN security groups are not currently supported");
     }
 
     @Override
     public void delete(@Nonnull String firewallId) throws InternalException, CloudException {
         Logger std = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( std.isTraceEnabled() ) {
             std.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".delete(" + firewallId + ")");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 std.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             long timeout = System.currentTimeMillis() + CalendarWrapper.HOUR;
 
             do {
                 try {
                     method.deleteServers("/os-security-groups", firewallId);
                     return;
                 }
                 catch( NovaException e ) {
                     if( e.getHttpCode() != HttpServletResponse.SC_CONFLICT ) {
                         throw e;
                     }
                 }
                 try { Thread.sleep(CalendarWrapper.MINUTE); }
                 catch( InterruptedException e ) { /* ignore */ }
             } while( System.currentTimeMillis() < timeout );
         }
         finally {
             if( std.isTraceEnabled() ) {
                 std.trace("EXIT: " + NovaSecurityGroup.class.getName() + ".delete()");
             }
         }
     }
 
     @Override
     public @Nullable Firewall getFirewall(@Nonnull String firewallId) throws InternalException, CloudException {
         Logger std = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( std.isTraceEnabled() ) {
             std.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".getFirewall(" + firewallId + ")");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 std.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             JSONObject ob = method.getServers("/os-security-groups", firewallId, false);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("security_group") ) {
                     JSONObject json = ob.getJSONObject("security_group");
                     Firewall fw = toFirewall(ctx, json);
 
                     if( fw != null ) {
                         return fw;
                     }
                 }
             }
             catch( JSONException e ) {
                 std.error("getRule(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for security group");
             }
             return null;
         }
         finally {
             if( std.isTraceEnabled() ) {
                 std.trace("EXIT: " + NovaSecurityGroup.class.getName() + ".getFirewall()");
             }
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForFirewall(@Nonnull Locale locale) {
         return "security group";
     }
 
     @Override
     public @Nonnull Collection<FirewallRule> getRules(@Nonnull String firewallId) throws InternalException, CloudException {
         Logger std = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( std.isTraceEnabled() ) {
             std.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".getFirewall(" + firewallId + ")");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 std.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             JSONObject ob = method.getServers("/os-security-groups", firewallId, false);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("security_group") ) {
                     JSONObject json = ob.getJSONObject("security_group");
 
                     if( !json.has("rules") ) {
                         return Collections.emptyList();
                     }
                     ArrayList<FirewallRule> rules = new ArrayList<FirewallRule>();
                     JSONArray arr = json.getJSONArray("rules");
                     Iterable<Firewall> myFirewalls = null;
 
                     for( int i=0; i<arr.length(); i++ ) {
                         JSONObject rule = arr.getJSONObject(i);
                         int startPort = -1, endPort = -1;
                         Protocol protocol = null;
                         String ruleId = null;
 
                         if( rule.has("id") ) {
                             ruleId = rule.getString("id");
                         }
                         if( ruleId == null ) {
                             continue;
                         }
                         RuleTarget sourceEndpoint = null;
 
                         if( rule.has("ip_range") ) {
                             JSONObject range = rule.getJSONObject("ip_range");
 
                             if( range.has("cidr") ) {
                                 sourceEndpoint = RuleTarget.getCIDR(range.getString("cidr"));
                             }
                         }
                         if( rule.has("group") ) {
                             JSONObject g = rule.getJSONObject("group");
                             String id = (g.has("id") ? g.getString("id") : null);
 
                             if( id != null ) {
                                 sourceEndpoint = RuleTarget.getGlobal(id);
                             }
                             else {
                                 String o = (g.has("tenant_id") ? g.getString("tenant_id") : null);
 
                                 if( ctx.getAccountNumber().equals(o) ) {
                                     String n = (g.has("name") ? g.getString("name") : null);
 
                                     if( n != null ) {
                                         if( myFirewalls == null ) {
                                             myFirewalls = list();
                                         }
                                         for( Firewall fw : myFirewalls ) {
                                             if( fw.getName().equals(n) ) {
                                                 sourceEndpoint = RuleTarget.getGlobal(fw.getProviderFirewallId());
                                                 break;
                                             }
                                         }
 
                                     }
                                 }
                             }
                         }
                         if( sourceEndpoint == null ) {
                             continue;
                         }
 
                         if( rule.has("from_port") ) {
                             startPort = rule.getInt("from_port");
                         }
                         if( rule.has("to_port") ) {
                             endPort = rule.getInt("to_port");
                         }
                         if( startPort == -1 && endPort != -1 ) {
                             startPort = endPort;
                         }
                         else if( endPort == -1 && startPort != -1 ) {
                             endPort = startPort;
                         }
                         if( startPort > endPort ) {
                             int s = startPort;
 
                             startPort = endPort;
                             endPort = s;
                         }
                         if( rule.has("ip_protocol") ) {
                             protocol = Protocol.valueOf(rule.getString("ip_protocol").toUpperCase());
                         }
                         if( protocol == null ) {
                             protocol = Protocol.TCP;
                         }
 
                         rules.add(FirewallRule.getInstance(ruleId, firewallId, sourceEndpoint, Direction.INGRESS, protocol, Permission.ALLOW, RuleTarget.getGlobal(firewallId), startPort, endPort));
                     }
                     return rules;
                 }
             }
             catch( JSONException e ) {
                 std.error("getRules(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for security groups");
             }
             return null;
         }
         finally {
             if( std.isTraceEnabled() ) {
                 std.trace("EXIT: " + NovaSecurityGroup.class.getName() + ".getFirewall()");
             }
         }
     }
 
     @Override
     public @Nonnull Requirement identifyPrecedenceRequirement(boolean inVlan) throws InternalException, CloudException {
         return Requirement.NONE;
     }
 
     private boolean verifySupport() throws InternalException, CloudException {
         NovaMethod method = new NovaMethod(provider);
 
         try {
             method.getServers("/os-security-groups", null, false);
             return true;
         }
         catch( CloudException e ) {
             if( e.getHttpCode() == 404 ) {
                 return false;
             }
             throw e;
         }
     }
     
     @SuppressWarnings("SimplifiableIfStatement")
     @Override
     public boolean isSubscribed() throws InternalException, CloudException {
         if( provider.getMajorVersion() > 1 && provider.getComputeServices().getVirtualMachineSupport().isSubscribed() ) {
             return verifySupport();
         }
         if( provider.getMajorVersion() == 1 && provider.getMinorVersion() >= 1  &&  provider.getComputeServices().getVirtualMachineSupport().isSubscribed() ) {
             return verifySupport();
         }
         return false;
     }
 
     @Override
     public boolean isZeroPrecedenceHighest() throws InternalException, CloudException {
         return true;  // nonsense since no precedence is supported
     }
 
     @Override
     public @Nonnull Collection<Firewall> list() throws InternalException, CloudException {
         Logger std = NovaOpenStack.getLogger(NovaSecurityGroup.class, "std");
 
         if( std.isTraceEnabled() ) {
             std.trace("ENTER: " + NovaSecurityGroup.class.getName() + ".list()");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 std.error("No context exists for this request");
                 throw new InternalException("No context exists for this request");
             }
             NovaMethod method = new NovaMethod(provider);
             JSONObject ob = method.getServers("/os-security-groups", null, false);
             ArrayList<Firewall> firewalls = new ArrayList<Firewall>();
 
             try {
                 if( ob != null && ob.has("security_groups") ) {
                     JSONArray list = ob.getJSONArray("security_groups");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject json = list.getJSONObject(i);
 
                         try {
                             Firewall fw = toFirewall(ctx, json);
     
                             if( fw != null ) {
                                 firewalls.add(fw);
                             }
                         }
                         catch( JSONException e ) {
                             std.error("Invalid JSON from cloud: " + e.getMessage());
                             throw new CloudException("Invalid JSON from cloud: " + e.getMessage());
                         }
                     }
                 }
             }
             catch( JSONException e ) {
                 std.error("list(): Unable to identify expected values in JSON: " + e.getMessage());                e.printStackTrace();
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for security groups in " + ob.toString());
             }
             return firewalls;
         }
         finally {
             if( std.isTraceEnabled() ) {
                 std.trace("exit - " + NovaSecurityGroup.class.getName() + ".list()");
             }
         }
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listFirewallStatus() throws InternalException, CloudException {
 
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new InternalException("No context exists for this request");
         }
         NovaMethod method = new NovaMethod(provider);
         JSONObject ob = method.getServers("/os-security-groups", null, false);
         ArrayList<ResourceStatus> firewalls = new ArrayList<ResourceStatus>();
 
         try {
             if( ob != null && ob.has("security_groups") ) {
                 JSONArray list = ob.getJSONArray("security_groups");
 
                 for( int i=0; i<list.length(); i++ ) {
                     JSONObject json = list.getJSONObject(i);
 
                     try {
                         ResourceStatus fw = toStatus(json);
 
                         if( fw != null ) {
                             firewalls.add(fw);
                         }
                     }
                     catch( JSONException e ) {
                         throw new CloudException("Invalid JSON from cloud: " + e.getMessage());
                     }
                 }
             }
         }
         catch( JSONException e ) {
             throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for security groups in " + ob.toString());
         }
         return firewalls;
 
     }
 
     @Override
     public @Nonnull Iterable<RuleTargetType> listSupportedDestinationTypes(boolean inVlan) throws InternalException, CloudException {
         if( inVlan ) {
             return Collections.emptyList();
         }
         return Collections.singletonList(RuleTargetType.GLOBAL);
     }
 
     @Override
     public @Nonnull Iterable<Direction> listSupportedDirections(boolean inVlan) throws InternalException, CloudException {
         if( inVlan ) {
             return Collections.emptyList();
         }
         return Collections.singletonList(Direction.INGRESS);
     }
 
     @Override
     public @Nonnull Iterable<Permission> listSupportedPermissions(boolean inVlan) throws InternalException, CloudException {
         if( inVlan ) {
             return Collections.emptyList();
         }
         return Collections.singletonList(Permission.ALLOW);
     }
 
     @Override
     public @Nonnull Iterable<RuleTargetType> listSupportedSourceTypes(boolean inVlan) throws InternalException, CloudException {
         if( inVlan ) {
             return Collections.emptyList();
         }
         ArrayList<RuleTargetType> list= new ArrayList<RuleTargetType>();
 
         list.add(RuleTargetType.CIDR);
         list.add(RuleTargetType.GLOBAL);
         return list;
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     @Override
     public void revoke(@Nonnull String providerFirewallRuleId) throws InternalException, CloudException {
         NovaMethod method = new NovaMethod(provider);
         long timeout = System.currentTimeMillis() + CalendarWrapper.HOUR;
 
         do {
             try {
                 method.deleteServers("/os-security-group-rules", providerFirewallRuleId);
                 return;
             }
             catch( NovaException e ) {
                 if( e.getHttpCode() != HttpServletResponse.SC_CONFLICT ) {
                     throw e;
                 }
             }
             try { Thread.sleep(CalendarWrapper.MINUTE); }
             catch( InterruptedException e ) { /* ignore */ }
         } while( System.currentTimeMillis() < timeout );
     }
 
     @Override
     @Deprecated
     public void revoke(@Nonnull String firewallId, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         revoke(firewallId, Direction.INGRESS, Permission.ALLOW, cidr, protocol, RuleTarget.getGlobal(firewallId), beginPort, endPort);
     }
 
     @Override
     @Deprecated
     public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         revoke(firewallId, direction, Permission.ALLOW, cidr, protocol, RuleTarget.getGlobal(firewallId), beginPort, endPort);
     }
 
     @Override
     @Deprecated
     public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String source, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         revoke(firewallId, direction, permission, source, protocol, RuleTarget.getGlobal(firewallId), beginPort, endPort);
     }
 
     @Override
     public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String source, @Nonnull Protocol protocol, @Nonnull RuleTarget target, int beginPort, int endPort) throws CloudException, InternalException {
         if( direction.equals(Direction.EGRESS) ) {
             throw new OperationNotSupportedException(provider.getCloudName() + " does not support egress rules.");
         }
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new InternalException("No context exists for this request");
         }
         FirewallRule targetRule = null;
 
         for( FirewallRule rule : getRules(firewallId) ) {
             RuleTarget t = rule.getSourceEndpoint();
 
             if( t.getRuleTargetType().equals(RuleTargetType.CIDR) && source.equals(t.getCidr()) ) {
                 RuleTarget rt = rule.getDestinationEndpoint();
 
                 if( target.getRuleTargetType().equals(rt.getRuleTargetType()) ) {
                     boolean matches = false;
 
                     switch( rt.getRuleTargetType() ) {
                         case CIDR:
                             //noinspection ConstantConditions
                             matches = target.getCidr().equals(rt.getCidr());
                             break;
                         case GLOBAL:
                             //noinspection ConstantConditions
                             matches = target.getProviderFirewallId().equals(rt.getProviderFirewallId());
                             break;
                         case VLAN:
                             //noinspection ConstantConditions
                             matches = target.getProviderVlanId().equals(rt.getProviderVlanId());
                             break;
                         case VM:
                             //noinspection ConstantConditions
                             matches = target.getProviderVirtualMachineId().equals(rt.getProviderVirtualMachineId());
                             break;
                     }
                     if( matches && rule.getProtocol().equals(protocol) && rule.getPermission().equals(permission) && rule.getDirection().equals(direction) ) {
                         if( rule.getStartPort() == beginPort && rule.getEndPort() == endPort ) {
                             targetRule = rule;
                             break;
                         }
                     }
 
                 }
             }
         }
         if( targetRule == null ) {
             throw new CloudException("No such firewall rule");
         }
         revoke(targetRule.getProviderRuleId());
     }
 
     @Override
     public boolean supportsRules(@Nonnull Direction direction, @Nonnull Permission permission, boolean inVlan) throws CloudException, InternalException {
         return (!inVlan && Direction.INGRESS.equals(direction) && Permission.ALLOW.equals(permission));
     }
 
     @Override
     public boolean supportsFirewallSources() throws CloudException, InternalException {
         return true;
     }
 
     private @Nullable Firewall toFirewall(@Nonnull ProviderContext ctx, @Nonnull JSONObject json) throws JSONException {
         Firewall fw = new Firewall();
         String id = null, name = null;
         
         fw.setActive(true);
         fw.setAvailable(true);
         fw.setProviderVlanId(null);
         String regionId = ctx.getRegionId();
         fw.setRegionId(regionId == null ? "" : regionId);
         if( json.has("id") ) {
             id = json.getString("id");
         }
         if( json.has("name") ) {
             name = json.getString("name");
         }
         if( json.has("description") ) {
             fw.setDescription(json.getString("description"));
         }
         if( id == null ) {
             return null;
         }
         fw.setProviderFirewallId(id);
         if( name == null ) {
             name = id;
         }
         fw.setName(name);
         if( fw.getDescription() == null ) {
             fw.setDescription(name);
         }
         return fw;
     }
 
     private @Nullable ResourceStatus toStatus(@Nonnull JSONObject json) throws JSONException {
         String id = null;
 
         if( json.has("id") ) {
             id = json.getString("id");
         }
         if( id == null ) {
             return null;
         }
         return new ResourceStatus(id, true);
     }
 }
