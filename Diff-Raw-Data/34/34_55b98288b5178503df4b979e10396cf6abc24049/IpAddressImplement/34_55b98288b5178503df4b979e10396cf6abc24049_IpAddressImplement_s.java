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
 
 import java.util.*;
 import javax.annotation.Nonnull;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.*;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.AddressType;
 
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.IpAddressSupport;
 import org.dasein.cloud.network.IpForwardingRule;
 import org.dasein.cloud.network.LoadBalancer;
 import org.dasein.cloud.network.Protocol;
 import org.dasein.cloud.network.Subnet;
 
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.opsource.CallCache;
 import org.dasein.cloud.opsource.OpSource;
 import org.dasein.cloud.opsource.OpSourceMethod;
 import org.dasein.cloud.opsource.Param;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class IpAddressImplement implements IpAddressSupport { 
 
     
     private OpSource provider;
     
     final int Maximum_Static_Public_IP = 8;
     final int Minimum_Static_Public_IP = 1;
     
     static public final Logger logger = Logger.getLogger(IpAddressImplement.class);
     
     
     public IpAddressImplement(OpSource provider) {
         this.provider = provider;
     }
     
     
     @Override
     public void assign(String addressId, String toServerId) throws InternalException, CloudException {
     	VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(toServerId);
     	String privateIp = null;
     	if(vm != null){
     		String[] privateIpAddress = vm.getPrivateIpAddresses();
     		if(privateIpAddress != null){
     			//Return the first one
     			privateIp = privateIpAddress[0];  			
     		}
     	}else{
     		throw new CloudException("Server to assign IP is null");
     	}  	
     
         if(privateIp == null){
         	throw new CloudException("Can not assign a server without private IP");
         }
         
         //Create post body
         Document doc = provider.createDoc();
         Element natRule = doc.createElementNS("http://oec.api.opsource.net/schemas/network", "ns4:NatRule");
         
         Element nameElmt = doc.createElement("ns4:name");
         nameElmt.setTextContent(privateIp);
        
         Element sourceIpElmt = doc.createElement("ns4:sourceIp");
         sourceIpElmt.setTextContent(privateIp);
                 
         natRule.appendChild(nameElmt);
         natRule.appendChild(sourceIpElmt);
         doc.appendChild(natRule);
 
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
     	parameters.put(0, param);
         param = new Param(vm.getProviderVlanId(), null);
     	parameters.put(1, param);
     	
     	param = new Param("natrule", null);
       	parameters.put(2, param);
      
     	OpSourceMethod method = new OpSourceMethod(provider,
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
     	
     	method.parseRequestResult("Assign Ip",method.invoke(), "result", "resultDetail");
     }
 
     @Override
     public void assignToNetworkInterface(@Nonnull String addressId, @Nonnull String nicId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("No support for NICs");
     }
 
     public String getPrivateIPFromServerId(String serverId) throws InternalException, CloudException{
     	VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(serverId);
     	if(vm != null){
     		String[] privateIpAddress = vm.getPrivateIpAddresses();
     		if(privateIpAddress != null){
     			//Return the first one
     			return privateIpAddress[0];  			
     		}
     	}
     	return null;
     }
     
     public String forward(String addressId, int publicPort, Protocol protocol, int privatePort, String onServerId) throws InternalException, CloudException {
     	throw new OperationNotSupportedException("Not support");
     }
   
       
     public IpAddress getIpAddress(String addressId) throws InternalException, CloudException {
     	ArrayList<IpAddress> addresses = (ArrayList<IpAddress>) listPublicIpPool(false);
     	for(IpAddress ip : addresses){
     		if(ip.getProviderIpAddressId().equals(addressId)){
     			return ip;    			
     		}    		
     	}
     	addresses = (ArrayList<IpAddress>) listPrivateIpPool(false);
     	for(IpAddress ip : addresses){
     		if(ip.getProviderIpAddressId().equals(addressId)){
     			return ip;    			
     		}    		
     	}
     	return null;
     }
     
     public NatRule getNatRule(String ip, String networkId) throws CloudException, InternalException{
     	
     	ArrayList<NatRule> list = null;
     	if(networkId == null){
     		list = (ArrayList<NatRule>) this.listNatRule();
     	}else{
     		list = (ArrayList<NatRule>) this.listNatRule(networkId);
     	}
     	
     	if(list == null){
     		return null;
     	}
     	for(NatRule rule : list){
     		if(rule.getNatIp().equals(ip) || rule.getSourceIp().equals(ip)){
     			return rule;    			
     		}  		
     	}    	
     	return null;
     }
     
     public String getProviderTermForIpAddress(Locale locale) {
         return "IP address";
     }
 
     @Override
     public Requirement identifyVlanForVlanIPRequirement() throws CloudException, InternalException{
         return null;
     }
 
     @Override
     @Deprecated
     public boolean isAssigned(AddressType type) {
         return type.equals(AddressType.PUBLIC);
     }
 
     @Override
     public boolean isAssigned(@Nonnull IPVersion version) throws CloudException, InternalException {
         return version.equals(IPVersion.IPV4);
     }
 
     @Override
     public boolean isAssignablePostLaunch(@Nonnull IPVersion version) throws CloudException, InternalException{
         return true;
     }
 
     @Override
     @Deprecated
     public boolean isForwarding() {
         return false;
     }
 
     @Override
     public boolean isForwarding(IPVersion version) throws CloudException, InternalException {
         return false;
     }
 
     @Override
     @Deprecated
     public boolean isRequestable(AddressType type) {
     	return type.equals(AddressType.PUBLIC);
     }
 
     @Override
     public boolean isRequestable(@Nonnull IPVersion version) throws CloudException, InternalException {
         return version.equals(IPVersion.IPV4);
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
     	return true;
     }
     /**
      * https://<Cloud API URL>/oec/0.9/{org-id}/networkWithLocation/{networkid}/
 config
      */
 
     @Override
     @Deprecated
     public Iterable<org.dasein.cloud.network.IpAddress> listPrivateIpPool(boolean unassignedOnly) throws InternalException, CloudException {
     	return Collections.emptyList();
     	//As the private IP can not be assigned to a specific server, no need to list
     	/*     
     	ArrayList<org.dasein.cloud.network.IpAddress> addresses = new ArrayList<org.dasein.cloud.network.IpAddress>();
         
         ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
         if(networkList == null){
         	return Collections.emptyList();
         }        
         for(VLAN network : networkList){
         	
         	HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param("networkWithLocation", null);
             
            	parameters.put(0, param);
            	
            	param = new Param(network.getProviderVlanId(), null);
             
            	parameters.put(1, param);
            	
            	param = new Param("config", null);
             
            	parameters.put(2, param);
             	
            	OpSourceMethod method = new OpSourceMethod(provider, 
            			provider.buildUrl(null,true, parameters),
            			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
              	
            	Document doc = method.invoke();
             NodeList matches = doc.getElementsByTagName("ns4:privateIps");
 
             if(matches != null){
                 for( int i=0; i<matches.getLength(); i++ ) {
                     Node item = matches.item(i); 
                     if(item.getNodeType() == Node.TEXT_NODE) continue;
                 
                 	ArrayList<org.dasein.cloud.network.IpAddress> list = (ArrayList<org.dasein.cloud.network.IpAddress>) toPrivateAddress(item);
                 	if(list == null) continue;
                 	
             		ArrayList<VirtualMachine> vms = (ArrayList<VirtualMachine>) provider.getComputeServices().getVirtualMachineSupport().listVirtualMachines();
             		if(vms == null) addresses.addAll(list);
             		for(org.dasein.cloud.network.IpAddress ip: list){
             			String addressId = ip.getAddress();
             			
             			if(unassignedOnly){
             				boolean isAssign = false;
                 			for( VirtualMachine vm : vms ) {
                     			String[] addrs = vm.getPrivateIpAddresses();		                    			
                     			if( addrs != null ) {
                     				for( String addr : addrs ) {
                     					if( addr.equals(addressId) ) {
                     						isAssign = true;
                     						break;		                    						
                     					}
 	                    	        }
                     			}
 	                    	}
                 			if(!isAssign){
                 				addresses.add(ip);
                 			}
             			}else{                    				
                 			for( VirtualMachine vm : vms ) {
                     			String[] addrs = vm.getPrivateIpAddresses();
                     			boolean isAssign = false;
                     			if( addrs != null ) {
                     				for( String addr : addrs ) {
                     					if( addr.equals(addressId) ) {
                     						ip.setServerId(vm.getProviderVirtualMachineId());
                     						isAssign = true;
                     						break;		                    						
                     					}
 	                    	        }
                     			}
                     			if(isAssign){
                     				break;                    				
                     			}
 	                    	}	                    			
                 			addresses.add(ip);
                 		}                    			
             		}
             	 }
             }
           
         }
         return addresses;*/
        
     }
     
     
 	@Override
     @Deprecated
 	public @Nonnull Iterable<IpAddress> listPublicIpPool(boolean unassignedOnly) throws InternalException, CloudException {
 		  
 		ArrayList<IpAddress> addresses = new ArrayList<IpAddress>();
 	    
 		ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
 	    
 	    for(VLAN network : networkList){
 	    	addresses.addAll((ArrayList<IpAddress>)listPublicIpPool(unassignedOnly, network.getProviderVlanId()));
         }
         return addresses;
     }
 
     @Override
     public @Nonnull Iterable<IpAddress> listIpPool(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
         if( version.equals(IPVersion.IPV4) ) {
             return listPublicIpPool(unassignedOnly);
         }
         return Collections.emptyList();
     }
 

     public Iterable<IpAddress> listPublicIpPool(boolean unassignedOnly, String networkId) throws InternalException, CloudException {
 		  
 		ArrayList<IpAddress> addresses = new ArrayList<IpAddress>();
    	
     	HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param("networkWithLocation", null);
        
         parameters.put(0, param);
         
         param = new Param(networkId, null);
            
         parameters.put(1, param);
            	
        	param = new Param("config", null);
        
        	parameters.put(2, param);
         	
        	OpSourceMethod method = new OpSourceMethod(provider,
        			provider.buildUrl(null,true, parameters),
        			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
          	
        	Document doc = method.invoke();
         //Document doc = CallCache.getInstance().getAPICall("networkWithLocation", provider, parameters);
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList matches = doc.getElementsByTagName(sNS + "publicIps");
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node item = matches.item(i); 
                 if(item.getNodeType() == Node.TEXT_NODE) continue;
                 NodeList ipblocks = item.getChildNodes();
                 for(int j = 0;j <ipblocks.getLength(); j++ ){
                 	 Node node = ipblocks.item(j); 
                 	 if(node.getNodeType() == Node.TEXT_NODE) continue;
                 	 if(node.getNodeName().equals(sNS + "IpBlock")){
                     	ArrayList<IpAddress> list = (ArrayList<IpAddress>) toPublicAddress(node, networkId, sNS);
                     	if(list == null) continue;         	
            		
                 		for(org.dasein.cloud.network.IpAddress ip: list){
                 			
                 			if(unassignedOnly && (ip.getProviderLoadBalancerId() != null || ip.getServerId() != null)){
                 				continue;           
                 			}
                 			addresses.add(ip);
                 		}
                 	 }
                 }
             }
         }
         return addresses;
     }
 
     @Override
     public Iterable<ResourceStatus> listIpPoolStatus(IPVersion version){//TODO: Implement
         return Collections.emptyList();
     }
     
     
 	public @Nonnull Collection<IpForwardingRule> listRules(String addressId) throws InternalException, CloudException {
     	return  Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     public void releaseFromPool(String addressId) throws InternalException, CloudException {
     	throw new OperationNotSupportedException("Not support for releasing IP for pool");
     }
     
     public Collection<NatRule> listNatRule() throws CloudException, InternalException{
     	ArrayList<NatRule> list = new ArrayList<NatRule>();
     	ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
         if(networkList == null){
         	return Collections.emptyList();
         }
         for(VLAN network : networkList){        	
     	
         	ArrayList<NatRule> natList = (ArrayList<NatRule>) this.listNatRule(network.getProviderVlanId());
         	if(natList != null){
         		list.addAll(natList);
         	}
         }
 		return list;   	
     }
     
     public Collection<NatRule> listNatRule(String networkId) throws CloudException, InternalException{
     	ArrayList<NatRule> list = new ArrayList<NatRule>();    	
     	
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
     	parameters.put(0, param);
         param = new Param(networkId, null);
     	parameters.put(1, param);
     	
     	param = new Param("natrule", null);
       	parameters.put(2, param);
      
     	OpSourceMethod method = new OpSourceMethod(provider,
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
     	
     	Document doc =  method.invoke();
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
     	NodeList matches = doc.getElementsByTagName(sNS + "NatRule");
     	if(matches != null){
     		for(int i = 0; i< matches.getLength();i++){
     			Node node = matches.item(i);
     			NatRule rule = this.toNatRule(node, sNS);
     			if(rule != null){
     				rule.setVlanId(networkId);
     				list.add(rule);
     			}
     		}  		
     	}       
 		return list;   	
     }
     
     public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {
     	NatRule rule = this.getNatRule(addressId, null);
     	
     	if(rule == null){
     		throw new CloudException("This address is not associated to a server");
     	}       
      
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
     	parameters.put(0, param);
         param = new Param(rule.getVlanId(), null);
     	parameters.put(1, param);
     	
     	param = new Param("natrule", null);
       	parameters.put(2, param);
       	
       	param = new Param(rule.getId(), null);
       	parameters.put(3, param);
      
     	OpSourceMethod method = new OpSourceMethod(provider,
     			provider.buildUrl("delete",true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
     	
     	method.requestResult("Release Ip from server",method.invoke());
     }
     
     /**
      * Opsource does not support reserve one IP address
      * But it supports to create a subnet of ip addresses
      */
 
     @Override
     @Deprecated
     public @Nonnull String request(@Nonnull AddressType addressType) throws InternalException, CloudException {
     	
     	if(addressType.equals(AddressType.PRIVATE)){    		
     		throw new OperationNotSupportedException("OpSource does not support request private IP");        	
     	}
     	    	
     	//Check currant available IP lists on default network 	
     	ArrayList<IpAddress> list = (ArrayList<IpAddress>) listPublicIpPool(true, provider.getDefaultVlanId());
     	
     	int size = list.size();
     	if(size >= this.Minimum_Static_Public_IP){
     		return list.get(0).getProviderIpAddressId();
     	}else{
         	//Create a subnet of public IP
         	
     		String defaultVlan = provider.getDefaultVlanId();
         	Network network = new Network(provider);
         	Subnet subnet = network.createSubnet(defaultVlan);
         	//Return first IP of the subnet
         	return subnet.getTags().get("baseIp");
     	}  	
     }
 
     @Override
     public @Nonnull String request(@Nonnull IPVersion version) throws InternalException, CloudException {
         if( version.equals(IPVersion.IPV4) ) {
             return request(AddressType.PUBLIC);
         }
         throw new OperationNotSupportedException("No support for " + version);
     }
 
     @Nonnull
     @Override
     public String requestForVLAN(@Nonnull IPVersion ipVersion, @Nonnull String s) throws InternalException, CloudException {   //TODO: Implement
         if(ipVersion.equals(IPVersion.IPV4)){
             throw new OperationNotSupportedException("Not yet implemented");
         }
         throw new OperationNotSupportedException("No support for " + ipVersion);
     }
 
     @Override
     public @Nonnull String requestForVLAN(IPVersion version) throws InternalException, CloudException {
         if( version.equals(IPVersion.IPV4) ) {
             throw new OperationNotSupportedException("This stuff is not yet supported because Dasein Cloud is too stupid to mention what vlan!");
         }
         throw new OperationNotSupportedException("No support for " + version);
     }
 
     public void stopForward(String ruleId) throws InternalException, CloudException {
     	throw new OperationNotSupportedException("Not support for stopForwarding for OpSource");
     }
 
     @Override
     public boolean supportsVLANAddresses(@Nonnull IPVersion ofVersion) throws InternalException, CloudException {
         return false; // TODO: make true when Dasein Cloud changes
     }
 
     private NatRule toNatRule(Node node, String nameSpace){
     	if(node == null){
     		return null;
     	}
     	NatRule rule = new NatRule();
     	
 		NodeList attributes = node.getChildNodes();
 	            
 	    for( int i=0; i<attributes.getLength(); i++ ) {
 	         Node n = attributes.item(i);
 	         String name = n.getNodeName();
 	         String value;
 	         
 	         if( n.getChildNodes().getLength() > 0 ) {
 	             value = n.getFirstChild().getNodeValue();
 	         }
 	         else {
 	             continue;
 	         }
 	         if( name.equalsIgnoreCase(nameSpace + "id") ) {
 	             rule.setId(value);
 	         }
 	         else if( name.equalsIgnoreCase(nameSpace + "natIp") ) {
 	        	 rule.setNatIp(value);
 	         }
 	         else if( name.equalsIgnoreCase(nameSpace + "sourceIp") ) {
 	        	 rule.setSourceIp(value);
 	         }
 	     }
 	     if(rule != null && rule.getId() != null 
 	    		 && rule.getNatIp() != null 
 	    		 && rule.getSourceIp() != null){
 	    	 return rule;            	 
 	     }else{
 	    	 return null; 
 	     }	    
     }
  
     private Collection<IpAddress> toPublicAddress(Node node, String networkId, String nameSpace) throws InternalException, CloudException {
         ArrayList<IpAddress> list = new ArrayList<IpAddress> ();
                  
     	IpAddress address = null;
         NodeList attributes = node.getChildNodes();      
 
         String baseIp = null;
         int ipSize = 0;
         
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node n = attributes.item(i);
             String name = n.getNodeName().toLowerCase();
             String value;
             
             if( n.getChildNodes().getLength() > 0 ) {
                 value = n.getFirstChild().getNodeValue();
             }
             else {
                 continue;
             }
             if( name.equalsIgnoreCase(nameSpace + "id") ) {
             	//
             }
             else if(name.equalsIgnoreCase(nameSpace + "baseIp") ) {
                 baseIp = value;
             }
             else if(name.equalsIgnoreCase(nameSpace + "subnetSize") ) {
             	ipSize = Integer.valueOf(value);
             }
             else if( name.equalsIgnoreCase(nameSpace + "networkDefault") ) {
             	//
             } 
         }    
         
         if(baseIp != null && ipSize >0 ){
         	
         	String prefix = baseIp.substring(0, baseIp.lastIndexOf(".")+1);
         	int lastValue = Integer.parseInt(baseIp.substring(baseIp.lastIndexOf(".")+1));
         	        	
         	LoadBalancers lbSupport = new LoadBalancers(provider);
         	ArrayList<LoadBalancer> lbs =  (ArrayList<LoadBalancer>) lbSupport.listLoadBalancers(networkId);
             
         	ArrayList<VirtualMachine> vms =  (ArrayList<VirtualMachine>) provider.getComputeServices().getVirtualMachineSupport().listVirtualMachines();
 
         	for(int i=0;i < ipSize ; i++){
         		int lastSection = lastValue + i;
         		String addressId = prefix + lastSection;
         		address =  new IpAddress();
 
                 address.setAddressType(AddressType.PUBLIC);
         		address.setRegionId(provider.getContext().getRegionId());
 
         		address.setIpAddressId(addressId);
         		address.setAddress(addressId);
         		
         		//Set LB Id
                 for(LoadBalancer lb : lbs){
               	   if(lb.getAddress().equals(addressId)){
               		 address.setProviderLoadBalancerId(lb.getProviderLoadBalancerId());
               		 break;
               	   }
                  }                
                 
                 // Set server Id
                 for( VirtualMachine vm : vms ) {
                 	
         			String[] addrs = vm.getPublicIpAddresses();	
         			if(addrs == null) continue;
         			boolean findServer = false;
         			for(String publicIp: addrs){
         				if( publicIp.equals(addressId)) {
         					address.setServerId(vm.getProviderVirtualMachineId());
         					findServer = true;
         					break;
             	        }
         			}
         			if(findServer) break;
             	}    
         		list.add(address);      		
         	}        	       	
         }
         return list;
     }
 
     
       
  /*   private Collection<org.dasein.cloud.network.IpAddress> toPrivateAddress(Node node) throws InternalException, CloudException {
         ArrayList<org.dasein.cloud.network.IpAddress> list = new ArrayList<org.dasein.cloud.network.IpAddress> ();
                 
     	org.dasein.cloud.network.IpAddress address = new org.dasein.cloud.network.IpAddress();
         NodeList attributes = node.getChildNodes();
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node n = attributes.item(i);
             String name = n.getNodeName().toLowerCase();
             String value;
             
             if( n.getChildNodes().getLength() > 0 ) {
                 value = n.getFirstChild().getNodeValue();
             }
             else {
                 continue;
             }
             if( name.equalsIgnoreCase("ns4:subnetSize") ) {
             	
             }
             else if( name.equalsIgnoreCase("ns4:Ip") ) {
             	address = new org.dasein.cloud.network.IpAddress();
                 address.setRegionId(provider.getContext().getRegionId());
                 address.setServerId(null);
                 address.setProviderLoadBalancerId(null);
                 address.setAddressType(AddressType.PRIVATE);
          		address.setIpAddressId(value);
         		address.setAddress(value);      		
 
         		list.add(address);      	
             } 
         }
         
         return list;
     }*/
     
  /*   private String getServerId(String privateIp) throws InternalException, CloudException{
         for( VirtualMachine vm : provider.getComputeServices().getVirtualMachineSupport().listVirtualMachines() ) {
             String[] addrs = vm.getPrivateIpAddresses();
             
             if( addrs == null ) {
             	continue;
             }
             for( String addr : addrs ) {
                 if( addr.equals(privateIp) ) {
                    return vm.getProviderVirtualMachineId();
                 }
             }           
         }
         return null;    	
     }*/
  
   
     
     /*
     private boolean hasRules(String address) throws InternalException, CloudException {
         return (provider.getNetworkServices().getLoadBalancerSupport().getLoadBalancer(address) != null);
         HttpClient client = new HttpClient();
         GetMethod get = new GetMethod();
         Document doc;
         int code;
         
         try {
             method.get(method.buildUrl(LoadBalancers.LIST_LOAD_BALANCER_RULES, new Param[] { new Param("publicIp", address) }));
             get.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
         }
         catch( SignatureException e ) {
             throw new InternalException("Unable to generate a valid signature: " + e.getMessage());
         }
         try {
             code = client.executeMethod(get);
         }
         catch( HttpException e ) {
             throw new InternalException("HttpException during GET: " + e.getMessage());
         }
         catch( IOException e ) {
             throw new CloudException("IOException during GET: " + e.getMessage());
         }
         if( code != HttpStatus.SC_OK ) {
             if( code == 401 ) {
                 throw new CloudException("Unauthorized user");
             }
             else if( code == 430 ) {
                 throw new InternalException("Malformed parameters");
             }
             else if( code == 431 ) {
                 throw new InternalException("Invalid parameters");
             }
             else if( code == 530 || code == 570 ) {
                 throw new CloudException("Server error in cloud (" + code + ")");
             }
             throw new CloudException("Received error code from server: " + code);
         }
         try {
             doc = provider.parseResponse(get.getResponseBodyAsStream());
         }
         catch( IOException e ) {
             throw new CloudException("IOException getting stream: " + e.getMessage());
         }
         
         NodeList rules = doc.getElementsByTagName("loadbalancerrule");
         return (rules.getLength() > 0);
     }
         */
     
     public class NatRule{
     	String id;
        	String natIp;
     	String sourceIp;
     	String vlanId;
     	
     	NatRule(){}
     	
     	NatRule(String id, String natIp, String sourceIp){
     		this.id = id;
     		this.natIp = natIp;
     		this.sourceIp = sourceIp;    		
     	}
     	public String getId(){
     		return id;
     	}
     	public String getNatIp(){
     		return this.natIp;
     	}
     	public String getSourceIp(){
     		return this.sourceIp;
     	}
     	public void setId(String id){
     		this.id = id;
     	}
     	public void setNatIp(String natIp){
     		this.natIp = natIp;
     	}
     	public void setSourceIp(String sourceIp){
     		this.sourceIp = sourceIp;
     	}
     	public void setVlanId(String vlanId){
     		this.vlanId = vlanId;
     	}
     	public String getVlanId(){
     		return vlanId;
     	}  
     }
 }
