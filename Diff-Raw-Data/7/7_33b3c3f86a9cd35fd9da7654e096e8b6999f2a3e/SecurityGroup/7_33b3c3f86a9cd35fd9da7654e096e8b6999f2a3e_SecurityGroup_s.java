 /**
  * Copyright (C) 2011-2012 enStratus Networks Inc
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
 
 package org.dasein.cloud.opsource.network;
 
 import java.io.StringWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.*;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.*;
 import org.dasein.cloud.opsource.CallCache;
 import org.dasein.cloud.opsource.OpSource;
 import org.dasein.cloud.opsource.OpSourceMethod;
 import org.dasein.cloud.opsource.Param;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 /**
  * There is no concept of firewall group in OpSource,
  * but it supports the idea of firewall rule.
  * Therefore, in this implementation, the firewall group idea would be
  * the same with the network group. 
  * The create and delete firewall group will not be supported.
  * But list and get firewall would be list and get network.
  *
  */
 public class SecurityGroup implements FirewallSupport {
     static private final Logger logger = Logger.getLogger(SecurityGroup.class);
     
     static public final String AUTHORIZE_SECURITY_GROUP_INGRESS = "authorizeSecurityGroupIngress";
     static public final String CREATE_SECURITY_GROUP            = "createSecurityGroup";
     static public final String DELETE_SECURITY_GROUP            = "deleteSecurityGroup";
     static public final String LIST_SECURITY_GROUPS             = "listSecurityGroups";
     static public final String REVOKE_SECURITY_GROUP_INGRESS    = "revokeSecurityGroupIngress";
     
     private OpSource provider;
     
     SecurityGroup(OpSource provider) { this.provider = provider; }
 
     @Override
     public @Nonnull String authorize(@Nonnull String securityGroupId, @Nonnull String cidr, @Nonnull Protocol protocol, int startPort, int endPort) throws CloudException, InternalException {
         return authorize(securityGroupId, Direction.INGRESS, cidr, protocol, startPort, endPort);
     }
 
     @Override
     @Deprecated
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         return authorize(firewallId, direction, Permission.ALLOW, cidr, protocol, RuleTarget.getGlobal(firewallId), beginPort, endPort);
     }
 
     @Override
     @Deprecated
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         return authorize(firewallId, direction, permission, cidr, protocol, RuleTarget.getGlobal(firewallId), beginPort, endPort);
     }
 
     @Override
     @Deprecated
     public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String cidr, @Nonnull Protocol protocol, @Nonnull RuleTarget destination, int beginPort, int endPort) throws CloudException, InternalException {
         String positionId = getFirstAvaiablePositionForInsertRule(firewallId);
         int precedence = -1;
         try{
             precedence = Integer.parseInt(positionId);
         }
         catch(NumberFormatException ex){
             throw new CloudException("Cannot find available position to add rule");
         }
         return authorize(firewallId, direction, permission, RuleTarget.getCIDR(cidr), protocol, destination, beginPort, endPort, precedence);
     }
 
     @Nonnull
     @Override
     public String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull RuleTarget sourceRuleTarget, @Nonnull Protocol protocol, @Nonnull RuleTarget destinationRuleTarget, int beginPort, int endPort, @Nonnegative int precedence) throws CloudException, InternalException {
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
         parameters.put(0, param);
 
         param = new Param(firewallId, null);
         parameters.put(1, param);
 
         param = new Param("aclrule", null);
         parameters.put(2, param);
 
         /** Create post body */
         Document doc = provider.createDoc();
         Element aclRule = doc.createElementNS("http://oec.api.opsource.net/schemas/network", "AclRule");
         Element nameElmt = doc.createElement("name");
         if(direction.equals(Direction.INGRESS)) nameElmt.setTextContent(sourceRuleTarget.getCidr());
         else nameElmt.setTextContent(destinationRuleTarget.getCidr());
 
         Element positionElmt = doc.createElement("position");
         String positionId = precedence + "";
        if(positionId.equals("-1"))positionId = getFirstAvaiablePositionForInsertRule(firewallId);
         if(positionId == null){
             throw new CloudException("Can not add firewall Rule because no position availabe to insert the current rule !!!");
         }else{
             positionElmt.setTextContent(positionId);
         }
 
         Element actionElmt = doc.createElement("action");
         //<!-- mandatory, string, one of (PERMIT,DENY) -->
         actionElmt.setTextContent(permission.equals(Permission.ALLOW) ? "PERMIT" : "DENY");
 
         Element protocolElmt = doc.createElement("protocol");
         String protocolType = "";
         if(protocol.equals(Protocol.IPSEC))protocolType = "IP";//Filthy hack to allow IP type as a protocol
         else protocolType = protocol.name();
         protocolElmt.setTextContent(protocolType);
 
         String sourceIp="0.0.0.0";
         String sourceMask = null;
         boolean singleIp = false;
 
         if(sourceRuleTarget.getCidr() != null){
             if(sourceRuleTarget.getCidr().contains("/")){
                 String[] ipInfo = sourceRuleTarget.getCidr().split("/");
                 sourceIp = ipInfo[0];
                 if(ipInfo.length >1){
                     sourceMask = convertNetMask(ipInfo[1]);
                 }
             }
             else{
                 sourceIp = sourceRuleTarget.getCidr();
                 singleIp = true;
             }
         }
 
         Element sourceIpRange = doc.createElement("sourceIpRange");
         Element sourceIpAddress = doc.createElement("ipAddress");
         sourceIpAddress.setTextContent(sourceIp);
         sourceIpRange.appendChild(sourceIpAddress);
 
         /** OpSource can not accept cidr style as IP/255.255.255.255, therefore when it is only one IP, ignore */
         if(!singleIp && sourceMask != null && !sourceMask.equals("255.255.255.255") ){
             Element sourceNetMask = doc.createElement("netmask");
             sourceNetMask.setTextContent(sourceMask);
             sourceIpRange.appendChild(sourceNetMask);
         }
 
         String destinationIp = "0.0.0.0";
         String destinationMask = null;
         singleIp = false;
 
         if(destinationRuleTarget.getCidr() != null){
             if(destinationRuleTarget.getCidr().contains("/")){
                 String[] ipInfo = destinationRuleTarget.getCidr().split("/");
                 destinationIp = ipInfo[0];
                 if(ipInfo.length > 1){
                     destinationMask = convertNetMask(ipInfo[1]);
                 }
             }
             else{
                 destinationIp = destinationRuleTarget.getCidr();
                 singleIp = true;
             }
         }
 
         Element destinationIpRange = doc.createElement("destinationIpRange");
         Element destinationIpAddress = doc.createElement("ipAddress");
         destinationIpAddress.setTextContent(destinationIp);
         destinationIpRange.appendChild(destinationIpAddress);
 
         if(!singleIp && destinationMask != null && !destinationMask.equals("255.255.255.255") ){
             Element destinationNetMask = doc.createElement("netmask");
             destinationNetMask.setTextContent(destinationMask);
             destinationIpRange.appendChild(destinationNetMask);
         }
 
         Element portRange = doc.createElement("portRange");
         Element portRangeType = doc.createElement("type");
         if(!protocol.equals(Protocol.IPSEC)){
             portRangeType.setTextContent("EQUAL_TO");
 
             Element port = doc.createElement("port1");
             port.setTextContent(String.valueOf(beginPort));
             portRange.appendChild(portRangeType);
             portRange.appendChild(port);
         }
         else{
             portRangeType.setTextContent("ALL");
             portRange.appendChild(portRangeType);
         }
 
         Element type = doc.createElement("type");
         type.setTextContent(direction.equals(Direction.INGRESS) ? "OUTSIDE_ACL" : "INSIDE_ACL");
 
         aclRule.appendChild(nameElmt);
         aclRule.appendChild(positionElmt);
         aclRule.appendChild(actionElmt);
         aclRule.appendChild(protocolElmt);
         //aclRule.appendChild(protocolElmt);
         if(!sourceIp.equals("0.0.0.0") && !sourceRuleTarget.getRuleTargetType().equals(RuleTargetType.GLOBAL)) aclRule.appendChild(sourceIpRange);
         if(!destinationIp.equals("0.0.0.0") && !destinationRuleTarget.getRuleTargetType().equals(RuleTargetType.GLOBAL))aclRule.appendChild(destinationIpRange);
         aclRule.appendChild(portRange);
         aclRule.appendChild(type);
         doc.appendChild(aclRule);
 
         System.out.println("Sending this rule:");
         try{
             TransformerFactory transfac = TransformerFactory.newInstance();
             Transformer trans = transfac.newTransformer();
             trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
             trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
             StringWriter sw = new StringWriter();
             StreamResult result = new StreamResult(sw);
             DOMSource source = new DOMSource(doc);
             trans.transform(source, result);
             String xmlString = sw.toString();
             System.out.println(xmlString);
         }
         catch(Exception ex){
             ex.printStackTrace();
         }
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl(null,true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
         Document responseDoc = method.invoke();
 
         System.out.println("Response:");
         try{
             TransformerFactory transfac = TransformerFactory.newInstance();
             Transformer trans = transfac.newTransformer();
             trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
             trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
             StringWriter sw = new StringWriter();
             StreamResult result = new StreamResult(sw);
             DOMSource source = new DOMSource(responseDoc);
             trans.transform(source, result);
             String xmlString = sw.toString();
             System.out.println(xmlString);
         }
         catch(Exception ex){
             ex.printStackTrace();
         }
 
         Node item = responseDoc.getDocumentElement();
         String sNS = "";
         try{
             sNS = item.getNodeName().substring(0, item.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList matches = item.getChildNodes();
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
                 if(node.getNodeName().equals(sNS + "id") && node.getFirstChild().getNodeValue() != null ){
                     System.out.println("Returning this value: " + node.getFirstChild().getNodeValue() + ":" + positionId);
                     return node.getFirstChild().getNodeValue() + ":" + positionId;
                 }
 
             }
         }
         throw new CloudException("Fails to authorize firewall rule without explaination.");
     }
 
     public String convertNetMask(String mask){
     	if(mask == null){
     		return "255.255.255.255";
     	}
     	if(mask.contains(".")){
     		return mask;
     	}
     	int prefix;
     	try  
     	{  
     		prefix = Integer.parseInt(mask);
     		
     	}catch(NumberFormatException nfe)  
     	{  
     		prefix = 0;  
     	 }
     	
     	int maskValue = 0xffffffff << (32 - prefix);
     	int value = maskValue;
     	byte[] bytes = new byte[]{ 
     	            (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
 
     	InetAddress netAddr;
 		
     	try {
 			netAddr = InetAddress.getByAddress(bytes);
 	    	return netAddr.getHostAddress();
 		} catch (UnknownHostException e) {
 			return "255.255.255.255";
 		}
     }
    
     // 
     private String convertCidr(String cidr){
     	String mask = "255.255.255.255";
 		if(cidr != null){
 			String[] ipInfo = cidr.split("/");
 			String ipAddress = ipInfo[0];
 			if(ipInfo.length >1){
 				mask = convertNetMask(ipInfo[1]);
 			}
 			return ipAddress +"/" + mask;
 		}		
     	return null;
     }
     
     public String getNetMask(String mask){
     	if(mask == null){
     		return "255.255.255.255";
     	}
     	if(mask.contains(".")){
     		return mask;
     	}
     	int prefix;
     	try{  
     		prefix = Integer.parseInt(mask);
     	}catch(NumberFormatException nfe){  
     		prefix = 0;  
     	}
     	
     	int maskValue = 0xffffffff << (32 - prefix);
     	int value = maskValue;
     	byte[] bytes = new byte[]{ 
     	            (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
 
     	InetAddress netAddr;
 		try {
 			netAddr = InetAddress.getByAddress(bytes);
 	    	return netAddr.getHostAddress();
 		} catch (UnknownHostException e) {
 			return "255.255.255.255";
 		}
     }
 
     @Override
     public String create(String name, String description) throws InternalException, CloudException {
     	/** Does not support create Firewall */
     	throw new CloudException("No Op");
     }
 
     @Override
     public String createInVLAN(String name, String description, String providerVlanId) throws InternalException, CloudException {
     	/** Does not support create Firewall */
     	throw new CloudException("No Op");
     }
     
     @Override
     public void delete(String firewallId) throws InternalException, CloudException {
     	/** Does not support delete Firewall */
     	throw new CloudException("No Op");
     }
 
     /** Equal to network(VLAN) */
     @Override
     public Firewall getFirewall(String firewallId) throws InternalException, CloudException {
      	//Firewall id is the same as network id
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
     	parameters.put(0, param);
     	param = new Param(firewallId, null);
     	parameters.put(1, param); 
     	
     	OpSourceMethod method = new OpSourceMethod(provider, 
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
       	Document doc = method.invoke();
      
       	Node item = doc.getDocumentElement();
         String sNS = "";
         try{
             sNS = item.getNodeName().substring(0, item.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
       	if(item.getNodeName().equals(sNS + "Network")){
       		return toFirewall(item);   		
       	}
       	return null;        
     }
     
     private String getFirstAvaiablePositionForInsertRule(String firewallId) throws InternalException, CloudException{
     	ArrayList<FirewallRule> list = (ArrayList<FirewallRule>) getRules(firewallId);
     	if(list == null){
     		return null;
     	}
    	
     	for(int i = 100;i<= 500;i ++){
     		String position = String.valueOf(i);
     		boolean isExist = false;
     		for(FirewallRule rule: list){

                 if(position.equals(getFirewallPositionIdFromDaseinRuleId(rule.getProviderRuleId()))){
     				isExist = true;
     				break;
     			}       		
         	}
     		if(!isExist){
     			return position;
     		}
     	}
     	return null;
     }
 
     private String getFirewallPositionIdFromDaseinRuleId(String daseinRuleId){
         if(daseinRuleId.contains(":")){
             return daseinRuleId.substring(daseinRuleId.indexOf(":")+1);
         }else{
             return null;
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForFirewall(@Nonnull Locale locale) {
         return "Network group";
     }
 
     @Override
     public @Nonnull Collection<FirewallRule> getRules(@Nonnull String firewallId) throws InternalException, CloudException {
      	/** In OpSource firewallId is the same as networkId */
  
     	ArrayList<FirewallRule> list = new ArrayList<FirewallRule>();
      
       	HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
     	parameters.put(0, param);
     	        	
     	param = new Param(firewallId, null);
       	parameters.put(1, param);
       	
       	param = new Param("aclrule", null);
       	parameters.put(2, param);
 
     	OpSourceMethod method = new OpSourceMethod(provider, 
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
       	Document doc = method.invoke();
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 
         NodeList matches = doc.getElementsByTagName(sNS + "AclRule");
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);            
                 FirewallRule rule = toRule(firewallId,node);
                 if( rule != null ) {
                 	list.add(rule);
                 }
             }
         }
         return list;
     }
 
     @Nonnull
     @Override
     public Requirement identifyPrecedenceRequirement(boolean inVLan) throws InternalException, CloudException {
         return Requirement.REQUIRED;
     }
 
     public boolean isSubscribed() throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public boolean isZeroPrecedenceHighest() throws InternalException, CloudException {
         return true;
     }
 
     @Override
     public Collection<Firewall> list() throws InternalException, CloudException {
       	//List the network information
     	ArrayList<Firewall> list = new ArrayList<Firewall>();
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param("networkWithLocation", null);
     	parameters.put(0, param);
 
     	param = new Param(provider.getDefaultRegionId(), null);
       	parameters.put(1, param);
 
     	OpSourceMethod method = new OpSourceMethod(provider,
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
       	Document doc = method.invoke();
         //Document doc = CallCache.getInstance().getAPICall("networkWithLocation", provider, parameters);
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList matches = doc.getElementsByTagName(sNS + "network");
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);            
                 Firewall firewall = toFirewall(node);                
                 if( firewall != null ) {
                 	list.add(firewall);
                 }
             }
         }
         return list;
     }
 
     @Nonnull
     @Override
     public Iterable<ResourceStatus> listFirewallStatus() throws InternalException, CloudException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public Iterable<RuleTargetType> listSupportedDestinationTypes(boolean inVlan) throws InternalException, CloudException {
         Collection<RuleTargetType> destTypes = new ArrayList<RuleTargetType>();
         if (!inVlan) {
             destTypes.add(RuleTargetType.CIDR);
             destTypes.add(RuleTargetType.GLOBAL);
         }
         return destTypes;
     }
 
     @Override
     public Iterable<RuleTargetType> listSupportedSourceTypes(boolean inVlan) throws InternalException, CloudException{
         Collection<RuleTargetType> sourceTypes = new ArrayList<RuleTargetType>();
         if (!inVlan) {
             sourceTypes.add(RuleTargetType.CIDR);
             sourceTypes.add(RuleTargetType.GLOBAL);
         }
         return sourceTypes;
     }
 
     @Override
     public Iterable<Direction> listSupportedDirections(boolean inVlan) throws InternalException, CloudException{
         Collection<Direction> directions = new ArrayList<Direction>();
         if (!inVlan) {
             directions.add(Direction.EGRESS);
             directions.add(Direction.INGRESS);
         }
         return directions;
     }
 
     @Nonnull
     @Override
     public Iterable<Permission> listSupportedPermissions(boolean inVlan) throws InternalException, CloudException {
         Collection<Permission> permissions = new ArrayList<Permission>();
         if (!inVlan) {
             permissions.add(Permission.ALLOW);
             permissions.add(Permission.DENY);
         }
         return permissions;
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     @Override
     public void revoke(@Nonnull String firewallRuleId) throws InternalException, CloudException {
         String firewallId = "";
         ArrayList<Firewall> firewalls = (ArrayList<Firewall>)list();
         for(Firewall firewall : firewalls){
             ArrayList<FirewallRule> rules = (ArrayList<FirewallRule>)getRules(firewall.getProviderFirewallId());
             for(FirewallRule rule : rules){
                 if(rule.getProviderRuleId().equals(firewallRuleId)){
                     firewallId = rule.getFirewallId();
                     break;
                 }
             }
         }
 
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
         parameters.put(0, param);
 
         param = new Param(firewallId, null);
         parameters.put(1, param);
 
         param = new Param("aclrule", null);
         parameters.put(2, param);
 
         if(firewallRuleId.indexOf(":") > 0)firewallRuleId = firewallRuleId.substring(0, firewallRuleId.indexOf(":"));
         param = new Param(firewallRuleId, null);
         parameters.put(3, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl("delete",true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
         method.parseRequestResult("Revoking firewall rule",method.invoke(), "result", "resultDetail");
     }
 
     @Override
     public void revoke(@Nonnull String firewallId, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         revoke(firewallId, Direction.INGRESS, cidr, protocol, beginPort, endPort);
     }
 
     @Override
     public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
         FirewallRule rule = null;
         String opSourceCidr = this.convertCidr(cidr);
 
         for( FirewallRule r : getRules(firewallId) ) {
             if(opSourceCidr == null &&  !r.getCidr().equals(opSourceCidr) ) {
                 continue;
             }
             if( !(r.getStartPort() == beginPort) ) {
                 continue;
             }
             if( !(r.getEndPort() == endPort) ) {
                 continue;
             }
             if( r.getProtocol() != null &&  !r.getProtocol().equals(protocol) ) {
                 continue;
             }
 
             rule = r;
             break;
 
         }
         if( rule == null ) {
             logger.warn("No such rule for " + firewallId + ": " + cidr + "/" + protocol + "/" + beginPort + "/" + endPort);
             return;
         }
 
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
         parameters.put(0, param);
 
         param = new Param(firewallId, null);
         parameters.put(1, param);
 
         param = new Param("aclrule", null);
         parameters.put(2, param);
 
         String ruleId = rule.getProviderRuleId();
         if(ruleId.indexOf(":") > 0)ruleId = ruleId.substring(0, ruleId.indexOf(":"));
         param = new Param(ruleId, null);
         parameters.put(3, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl("delete",true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
         method.parseRequestResult("Revoking firewall rule",method.invoke(), "result", "resultDetail");
     }
 
     @Override
     public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String source, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException{
         //TODO: Implement for 2013.01
     }
 
     @Override
     public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull String source, @Nonnull Protocol protocol, @Nonnull RuleTarget target, int beginPort, int endPort) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Override
     public boolean supportsRules(@Nonnull Direction direction, @Nonnull Permission permission, boolean inVlan) throws CloudException, InternalException{
         return true;
     }
 
     @Override
     public boolean supportsFirewallSources() throws CloudException, InternalException {
         return true;
     }
 
     private Firewall toFirewall(Node node) {
         if( node == null ) {
             return null;
         }
         String sNS = "";
         try{
             sNS = node.getNodeName().substring(0, node.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 
         NodeList attributes = node.getChildNodes();
         Firewall firewall = new Firewall();
         
         firewall.setActive(true);
         firewall.setAvailable(true);
         firewall.setRegionId(provider.getContext().getRegionId());
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName().toLowerCase();
             String value;
             
             if( attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();                
             }
             else {
                 value = null;
             }
             if( name.equalsIgnoreCase(sNS + "id") ) {
             	firewall.setProviderFirewallId(value);
             	/** The firewall Id is the same as vlan Id */
             	firewall.setProviderVlanId(value);
             }
             else if( name.equalsIgnoreCase(sNS + "name") ) {
             	firewall.setName("enstratus security group for VLan " + value);
             }
             else if( name.equalsIgnoreCase(sNS + "description") ) {
             	firewall.setDescription("enstratus security group for VLan "+ value);
             }
             else if( name.equalsIgnoreCase(sNS + "location") && value != null ) {
             	firewall.setRegionId(value);
             }
            
         }
         if( firewall.getProviderFirewallId() == null ) {
             logger.warn("Discovered firewall " + firewall.getProviderFirewallId() + " with an empty firewall ID");
             return null;
         }
         if( firewall.getName() == null ) {
             firewall.setName(firewall.getProviderFirewallId());
         }
         if( firewall.getDescription() == null ) {
             firewall.setDescription(firewall.getName());
         }
         return firewall;
     }
     
     private FirewallRule toRule(String firewallId, Node node) {
         if( node == null) {
             return null;
         }
 
         String sNS = "";
         try{
             sNS = node.getNodeName().substring(0, node.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList attributes = node.getChildNodes();
 
         Permission permission = Permission.ALLOW;
         Direction direction = Direction.INGRESS;
         Protocol protocol = null;
         int startPort = -1;
         int endPort = -1;
         String providerRuleId = "";
         RuleTarget source = null;
         RuleTarget destination = null;
         int positionId = -1;
         
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName();
             String value;
 
             if( attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();                
             }
             else {
                 value = null;
             }
 
             if( name.equalsIgnoreCase(sNS + "id") ) {
             	providerRuleId = value;
             }
             else if( name.equalsIgnoreCase(sNS + "position") ) {
                positionId = Integer.parseInt(value);
             }
             else if( name.equalsIgnoreCase(sNS + "action") ) {
             	
             	if(value.equalsIgnoreCase("deny")){
             		permission = Permission.DENY;
             	}else{
             		permission = Permission.ALLOW;
             	}                      	
             }
             else if( name.equalsIgnoreCase(sNS + "protocol") ) {
             	
             	if(value.equalsIgnoreCase("TCP")){
             		protocol = Protocol.TCP;
             	}else if (value.equalsIgnoreCase("UPD")){
             		protocol = Protocol.UDP;
             	}else if (value.equalsIgnoreCase("ICMP") ){
             		protocol = Protocol.ICMP;
             	}
                 else if (value.equalsIgnoreCase("IP")){
                     protocol = Protocol.IPSEC;
                 }
                 else{
                     //OpSource has a rule with an odd protocol by default that we don't want to add or display
                     return null;
                 }
             	
             }
             else if( name.equalsIgnoreCase(sNS + "sourceIpRange") ) {
         		String networkMask = null;
             	NodeList ipAddresses = attribute.getChildNodes();
             	for(int j = 0 ;j < ipAddresses.getLength(); j ++){
             		Node ip = ipAddresses.item(j);
             		if(ip.getNodeType() == Node.TEXT_NODE) continue;
    
             		if(ip.getNodeName().equals(sNS + "ipAddress") && ip.getFirstChild().getNodeValue() != null){
                         source = RuleTarget.getCIDR(ip.getFirstChild().getNodeValue());
             		}
             	}
             }
             else if( name.equalsIgnoreCase(sNS + "destinationIpRange") ) {
                 String networkMask = null;
                 NodeList ipAddresses = attribute.getChildNodes();
                 for(int j = 0 ;j < ipAddresses.getLength(); j ++){
                     Node ip = ipAddresses.item(j);
                     if(ip.getNodeType() == Node.TEXT_NODE) continue;
 
                     if(ip.getNodeName().equals(sNS + "ipAddress") && ip.getFirstChild().getNodeValue() != null){
                         destination = RuleTarget.getCIDR(ip.getFirstChild().getNodeValue());
                     }
                 }
             }
             else if( name.equalsIgnoreCase(sNS + "portRange") ) {
             	NodeList portAttributes  = attribute.getChildNodes();
             	String portType = null;
            		for(int j=0;j<portAttributes.getLength();j++ ){
 	           		Node portItem = portAttributes.item(j);
 	           		if( portItem.getNodeName().equalsIgnoreCase(sNS + "type") && portItem.getFirstChild().getNodeValue() != null ) {
 	           			portType = portItem.getFirstChild().getNodeValue();	           			
 	                }
 	                else if( portItem.getNodeName().equalsIgnoreCase(sNS + "port1") && portItem.getFirstChild().getNodeValue() != null ) {
 	                	startPort = Integer.valueOf(portItem.getFirstChild().getNodeValue());
 	                	if(portType.equalsIgnoreCase("EQUAL_TO")){
 	                    	endPort = Integer.valueOf(portItem.getFirstChild().getNodeValue());
 	                    }
 	                }
 	                else if( portItem.getNodeName().equalsIgnoreCase(sNS + "port2") && portItem.getFirstChild().getNodeValue() != null ) {
 	                	endPort = Integer.valueOf(portItem.getFirstChild().getNodeValue());
 	                }	                                     
            		}              	      
             }
             else if( name.equalsIgnoreCase(sNS + "type") ) {
             	if(value != null){
             		if(value.equalsIgnoreCase("INSIDE_ACL")){
             			direction = Direction.EGRESS;
             		}else if (value.equalsIgnoreCase("OUTSIDE_ACL")){
             			direction = Direction.INGRESS;
             		}else{
             			direction = Direction.INGRESS;
             		}
             	}
             }
         }
 
         if(protocol == null){
             //OpSource has a rule with an odd protocol by default that we don't want to add or display
             return null;
         }
         if(source == null)source = RuleTarget.getGlobal(firewallId);
         if(destination == null)destination = RuleTarget.getGlobal(firewallId);
 
         System.out.println("Provider Rule ID: " + providerRuleId + ":" + positionId);
 
         FirewallRule rule = FirewallRule.getInstance(providerRuleId + ":" + positionId, firewallId, source, direction, protocol, permission, destination, startPort, endPort);
         rule = rule.withPrecedence(positionId);
         return rule;
     }
 }
