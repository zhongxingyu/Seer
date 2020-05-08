 package com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.impl;
 
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.Authentication;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.HardwareProfile;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.JCloudsWrapperService;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.OperatingSystemImage;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.VMMetadata;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.filter.Filter;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.filter.FilterBuilder;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.filter.NodeMetadataPredicateBuilder;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.filterimpl.FilterBuilderImpl;
 import com.comcast.tvx.xreapps.common.deployment.jcloudswrapper.filterimpl.NodeMetadataPredicateBuilderImpl;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.Iterables;
 
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.Hardware;
 import org.jclouds.compute.domain.Image;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.Template;
 import org.jclouds.compute.domain.TemplateBuilder;
 
 import org.jclouds.openstack.nova.v2_0.NovaApi;
 import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
 import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
 import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
 import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
 import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
 import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Concrete implementation of {@link JCloudsWrapperService} specific to Open Stack
  * @author mroowa000
  *
  */
 public class JCloudsWrapperServiceOpenStackImpl implements JCloudsWrapperService {   
 
 	private static final int SLEEP_TIME_IN_MILLISECONDS = 3000;
     private ComputeService computeService;
     private NovaApi novaApi;
     private Authentication authentication;  
     private FloatingIPApi floatingIPApi;
    
     public JCloudsWrapperServiceOpenStackImpl() {
         authentication = new Authentication();
         computeService = authentication.getComputeServiceOpenStack();
         novaApi = authentication.getOpenStackNovaApi();
         floatingIPApi = this.getFloatingIPApi();
     }  
     
    /*public JCloudsWrapperServiceOpenStackImpl(ComputeService computeService, FloatingIPApi floatingIPApi) {
     	this.computeService = computeService;
     	this.floatingIPApi = floatingIPApi;
     }*/
     
     /**
      * {@inheritDoc}
      */
     public VMMetadata getNodeMetadata(String id) {    	
     	return populateVMMetadata(computeService.getNodeMetadata(id));      
     }
     
     /**
      * {@inheritDoc}
      */
     /* TODO
     public void setVMUndeletable(Set<? extends NodeMetadata> nodeMetadata) {
     	for (NodeMetadata eachNodeMetadata : nodeMetadata) {
     		Map<String, String> nodeMap = eachNodeMetadata.getUserMetadata();
     		Set<String> keys = nodeMap.keySet();
     		boolean deleteKeyExists = false;
     		for (String eachKey : keys) {
     			if(eachKey.equals("delete")) {
     				nodeMap.put(eachKey, "false");
     				deleteKeyExists = true;
     			}      			
     		}
     		if (!deleteKeyExists) {
     			nodeMap.put("delete", "false");
     		}
     	}
     }*/
     
     /**
      * {@inheritDoc}
      */
     public List<String> allocateFloatingIPAddresses(Integer count) {
     	List<String> floatingIPAddresses = new ArrayList<String>();    	
     	for (int i = 0; i < count; i++) {
     		FloatingIP floatingIP = floatingIPApi.allocateFromPool(JCloudsWrapperServiceOpenStackConstants.FLOATING_IP_POOL_NAME);    		
     		floatingIPAddresses.add(floatingIP.getIp());
     	}    	
     	return floatingIPAddresses;    	
     }
     
     /**
      * {@inheritDoc}
      */    
     public void releaseFloatingIPAddresses(List<String> floatingIPAddresses) {
     	this.validateFloatingIPs(floatingIPAddresses);
     	List<String> floatingIds = new ArrayList<String>();
     	FluentIterable<? extends FloatingIP> floatingIPIterable = floatingIPApi.list();
     	
     	for (String eachFloatingIPAddress : floatingIPAddresses) {
     		for (FloatingIP eachFloatingIP : floatingIPIterable) {
     			if (eachFloatingIP.getInstanceId() == null) {
     				if (eachFloatingIPAddress.trim().equals(eachFloatingIP.getIp().trim())) {
     					floatingIds.add(eachFloatingIP.getId());
     					break;
     				}
     			}
     		}
     	}
 		
     	for (String eachFloatingId : floatingIds) {
     		floatingIPApi.delete(eachFloatingId);
     	}
     }
     
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public Set<VMMetadata> listNodesDetailsMatching(
         Filter filterExpression) {
     	Set<VMMetadata> vmMetadatas = new HashSet<VMMetadata>();
         Set<NodeMetadata> nodeMetadata = new HashSet<NodeMetadata>();
 
         if (filterExpression.getNodeMetadataPredicate() == null) {
             nodeMetadata = (Set<NodeMetadata>)
                 computeService.listNodesDetailsMatching(
                     filterExpression.getComputeMetadataPredicate());
         } else {
             Iterable<? extends NodeMetadata> result = Iterables.filter(
                     computeService.listNodesDetailsMatching(
                         filterExpression.getComputeMetadataPredicate()),
                     filterExpression.getNodeMetadataPredicate());
             Iterator<? extends NodeMetadata> iter = result.iterator();
 
             while (iter.hasNext()) {
                 nodeMetadata.add(iter.next());
             }
         }
         for (NodeMetadata eachNodeMetadata : nodeMetadata) {
         	vmMetadatas.add(this.populateVMMetadata(eachNodeMetadata));        	
         }
         return vmMetadatas;
     }
     
     /**
      * {@inheritDoc}
      */
 	public Set<VMMetadata> destroyNodesMatching(
 			Set<VMMetadata> vmMetadataSet) {
 		if ((!vmMetadataSet.isEmpty()) && (vmMetadataSet != null)) {
 			for (VMMetadata eachVmMetadata : vmMetadataSet) {
 				// TODO Add check for deletion flag and modify nodeMetadataSet
 				// if Necessary
 				destroyNode(eachVmMetadata.getNodeId());
 			}
 		} else {
 			throw new RuntimeException("NodeMetadata Set cannot be empty");
 		}
 		return vmMetadataSet;
 	}
     
     /**
      * {@inheritDoc}
      */
     public void destroyNode(String id) {
     	VMMetadata vmMetadata = getNodeMetadata(id);
     	Set<String> publicAddresses = vmMetadata.getPublicAddresses();
     	if (publicAddresses == null || publicAddresses.isEmpty()) {
     		computeService.destroyNode(id);
     	} else {  
     		for (String eachPublicAddress : publicAddresses) {
     			floatingIPApi.removeFromServer(eachPublicAddress.trim(), vmMetadata.getProviderId());
     		}
     		try {
 				Thread.sleep(SLEEP_TIME_IN_MILLISECONDS);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			computeService.destroyNode(id);
     	}        
     }
     
     /**
      * {@inheritDoc}
      */
     public Set<VMMetadata> provisionVM(
         OperatingSystemImage operatingSystemImage, HardwareProfile hardwareProfile,
         Integer count, String groupName, List<String> floatingIPAddresses, String script) { 
     	NovaTemplateOptions templateOptions;
         
     	Set<? extends NodeMetadata> nodeMetadata;  
     	Set<VMMetadata> vmMetadatas = new HashSet<VMMetadata>();
     	if (script != null) {
     		templateOptions = this.getNovaTemplateOptions(groupName, script);
     	} else {
     		templateOptions = this.getNovaTemplateOptions(groupName, null);
     	}       
 
         TemplateBuilder templateBuilder = this.getTemplateBuilder();
         Template template = templateBuilder.fromImage(this.getEquivalentOperatingSystemImage(operatingSystemImage)).fromHardware(this.getEquivalentHardware(hardwareProfile)).options(templateOptions).build();
 
         if (floatingIPAddresses == null) {
         	nodeMetadata = this.provisionVM(count, groupName, template);
         	for (NodeMetadata eachNodeMetadata : nodeMetadata) {
         		vmMetadatas.add(this.populateVMMetadata(eachNodeMetadata));
         	}
         } else {
         	if (floatingIPAddresses.size() != count.intValue()) {
         		throw new RuntimeException("Number of Floating IP Addresses does not match with the number of VMs to be provisioned");
         	}
         	this.validateFloatingIPs(floatingIPAddresses);
         	
         	nodeMetadata = this.provisionVM(count, groupName, template);              	
         	int counter = 0;
         	for (NodeMetadata eachNodeMetadata : nodeMetadata) {        		
         		floatingIPApi.addToServer(floatingIPAddresses.get(counter), eachNodeMetadata.getProviderId());
         		VMMetadata vmMetadata = this.populateVMMetadata(eachNodeMetadata);
         		Set<String> publicAddress = new HashSet<String>();
         		publicAddress.add(floatingIPAddresses.get(counter));
         		vmMetadata.setPublicAddresses(publicAddress);
         		vmMetadatas.add(vmMetadata);
         		counter++;        		
         	}        	
         }
         return vmMetadatas;
     }
     
     /**
      * {@inheritDoc}
      */
     public Set<VMMetadata> provisionVM(Integer count,
         String groupName, List<String> floatingIPAddresses, String script) {
         Set<? extends NodeMetadata> nodeMetadata;
         Set<VMMetadata> vmMetadatas = new HashSet<VMMetadata>();
         NovaTemplateOptions templateOptions;
         
         if (script != null) {
     		templateOptions = this.getNovaTemplateOptions(groupName, script);
     	} else {
     		templateOptions = this.getNovaTemplateOptions(groupName, null);
     	}       
                 
         TemplateBuilder templateBuilder = this.getTemplateBuilder();
         Template template = templateBuilder.fromImage(this.getDefaultLatestOperatingSystemImage()).fromHardware(this.getDefaultHardware()).options(templateOptions).build();
         
         if (floatingIPAddresses == null) {
         	nodeMetadata = this.provisionVM(count, groupName, template);
         	for (NodeMetadata eachNodeMetadata : nodeMetadata) {
         		vmMetadatas.add(this.populateVMMetadata(eachNodeMetadata));
         	}
         } else {
         	if (floatingIPAddresses.size() != count.intValue()) {
         		throw new RuntimeException("Number of Floating IP Addresses does not match with the number of VMs to be provisioned");
         	}
         	this.validateFloatingIPs(floatingIPAddresses);
         	
         	nodeMetadata = this.provisionVM(count, groupName, template); 	  	
         	int counter = 0;
         	for (NodeMetadata eachNodeMetadata : nodeMetadata) {
         		floatingIPApi.addToServer(floatingIPAddresses.get(counter), eachNodeMetadata.getProviderId());
         		VMMetadata vmMetadata = this.populateVMMetadata(eachNodeMetadata);
         		Set<String> publicAddress = new HashSet<String>();
         		publicAddress.add(floatingIPAddresses.get(counter));
         		vmMetadata.setPublicAddresses(publicAddress);
         		vmMetadatas.add(vmMetadata);
         		counter++;        		
         	}        	
         }
         return vmMetadatas;
     }    
         
     /**
      * {@inheritDoc}
      */
     public FilterBuilder getFilterBuilder() {
 
         return new FilterBuilderImpl();
     }
     
     /**
      * {@inheritDoc}
      */
     public NodeMetadataPredicateBuilder getNodeMetadataPredicateBuilder() {
 
         return new NodeMetadataPredicateBuilderImpl();
     }
     
     /**
      * {@inheritDoc}
      */
     public void close() {
 
         try {
             authentication.close();
             authentication.novaclose();
         } catch (IOException e) {
             throw new RuntimeException("Could not close the connection");
         }
     }   
     
     private Set<? extends Hardware> listHardwareProfiles() {
         return computeService.listHardwareProfiles();
     }
     
     private Set<? extends Image> listImages() {
         return computeService.listImages();
     }
     
     private Set<? extends NodeMetadata> provisionVM(Integer count, String groupName, Template template) {
     	Set<? extends NodeMetadata> nodeMetadata = null; 
     	try {
             nodeMetadata = computeService.createNodesInGroup(groupName, count,
                     template);
 
         } catch (RunNodesException e) {
             throw new RuntimeException("Could not provision VM " +
                 e.getMessage());
         }        
         return nodeMetadata;
     }
 
     private Set<String> getSecurityGroups() {
         Set<String> securityGroupSet = new HashSet<String>();
         Set<String> configuredZones = this.getConfiguredZones();
         String zone = null;
 
         if (configuredZones.isEmpty()) {
             throw new RuntimeException("No configured zones found");
         } else {
             Iterator<String> iter = configuredZones.iterator();
 
             if (iter.hasNext()) {
                 zone = iter.next();
             }
         }
 
         Optional<? extends SecurityGroupApi> securityGroupApi =
             novaApi.getSecurityGroupExtensionForZone(zone);
 
         if (securityGroupApi.isPresent()) {
             FluentIterable<? extends org.jclouds.openstack.nova.v2_0.domain.SecurityGroup> securityGroups =
                 securityGroupApi.get().list();
             Iterator<? extends org.jclouds.openstack.nova.v2_0.domain.SecurityGroup> iter =
                 securityGroups.iterator();
 
             while (iter.hasNext()) {
                 securityGroupSet.add(iter.next().getName());
             }
         }
 
         return securityGroupSet;
     }
 
     private Set<String> getConfiguredZones() {
         return novaApi.getConfiguredZones();
     }
     
     private boolean validateKeyPairName() {
     	 Set<String> configuredZones = this.getConfiguredZones();
     	 boolean keyPairExists = false;
          String zone = null;
 
          if (configuredZones.isEmpty()) {
              throw new RuntimeException("No configured zones found");
          } else {
              Iterator<String> iter = configuredZones.iterator();
 
              if (iter.hasNext()) {
                  zone = iter.next();
              }
          }
     	Optional<? extends KeyPairApi> keyPairApi = novaApi.getKeyPairExtensionForZone(zone);
     	if (keyPairApi.isPresent()) {
     		FluentIterable<? extends KeyPair> keyPairs = keyPairApi.get().list();
     		Iterator<? extends KeyPair> iter = keyPairs.iterator();
     		while(iter.hasNext()) {
     			if (iter.next().getName().equals(JCloudsWrapperServiceOpenStackConstants.KEY_PAIR_NAME)) {
     				keyPairExists = true;
     			}
     		}
     	}
     	return keyPairExists;
     }
     
     private NovaTemplateOptions getNovaTemplateOptions(String groupName, String script) {
     	 boolean defaultSecurityGroupFound = false;         
          NovaTemplateOptions templateOptions = new NovaTemplateOptions();
 
          Set<String> securityGroups = getSecurityGroups();
          Iterator<String> iter = securityGroups.iterator();
 
          while (iter.hasNext()) {
 
              if (iter.next().equals(JCloudsWrapperServiceOpenStackConstants.DEFAULT_SECURITY_GROUP)) {
                  defaultSecurityGroupFound = true;
 
                  break;
              }
          }
 
          if (!defaultSecurityGroupFound) {
              throw new RuntimeException("No default security group found");
          }
          
          if (!this.validateKeyPairName()) {
         	 throw new RuntimeException("No Key Pair found");
          }
          if (script != null) {        
 			return ((NovaTemplateOptions) templateOptions.userMetadata("name", groupName).userMetadata("delete",
 			     "true").securityGroupNames(
			     JCloudsWrapperServiceOpenStackConstants.DEFAULT_SECURITY_GROUP).runScript(script)).keyPairName(JCloudsWrapperServiceOpenStackConstants.KEY_PAIR_NAME).overrideLoginUser(JCloudsWrapperServiceOpenStackConstants.LOGIN_USER);
 		
          }          
 		return templateOptions.userMetadata("name", groupName).userMetadata("delete",
 			 "true").securityGroupNames(
 			     JCloudsWrapperServiceOpenStackConstants.DEFAULT_SECURITY_GROUP).keyPairName(JCloudsWrapperServiceOpenStackConstants.KEY_PAIR_NAME).overrideLoginUser(JCloudsWrapperServiceOpenStackConstants.LOGIN_USER);
 		
          
     }    
     
     private Image getDefaultLatestOperatingSystemImage() {       	   	
     	Image image = null;  
     	Double highestVersion = null;
     	
     	Pattern pattern = Pattern.compile(JCloudsWrapperServiceOpenStackConstants.OS_VERSION_REGEX); 
         Set<? extends Image> images = this.listImages();        
 
         for (Image eachImage : images) {
 
             if (eachImage.getOperatingSystem().getName().matches(JCloudsWrapperServiceOpenStackConstants.CENTOS_IMAGE_REGEX) &&
                     eachImage.getOperatingSystem().is64Bit()) {
             	
             	String imageName = eachImage.getOperatingSystem().getName();
             	try {
             		Matcher matcher = pattern.matcher(imageName);
             		if (matcher.find()) {
             			Double version = Double.valueOf(matcher.group());
             			if (highestVersion == null) {
             				highestVersion = version;
             				image = eachImage;
             			} else if (version > highestVersion){
             				highestVersion = version;
             				image = eachImage;
             			}
             		}
             	}	catch (IllegalArgumentException e) {
             		continue;
             	}            	
             }            
         }        
     	return image; 
     }
     
     private Image getEquivalentOperatingSystemImage(OperatingSystemImage operatingSystemImage) {
     	Image image = null;    	
         
     	if (operatingSystemImage.equals(OperatingSystemImage.CENTOS_LATEST_x86_64)) {
     		image = getDefaultLatestOperatingSystemImage();
     	} else if (operatingSystemImage.equals(OperatingSystemImage.CENTOS_58_x86_64)) {
     		image = this.matchOperatingSystemImage(5.8);
     	} else if (operatingSystemImage.equals(OperatingSystemImage.CENTOS_63_x86_64)) {
     		image = this.matchOperatingSystemImage(6.3);
     	}
     	return image;
     }
     
     private Image matchOperatingSystemImage(Double versionToMatch) {
     	Image image = null;
     	Pattern pattern = Pattern.compile(JCloudsWrapperServiceOpenStackConstants.OS_VERSION_REGEX); 
         Set<? extends Image> images = this.listImages();
         
         boolean imageFound = false;
 		for (Image eachImage : images) {
 			if (eachImage.getOperatingSystem().getName().matches(JCloudsWrapperServiceOpenStackConstants.CENTOS_IMAGE_REGEX) &&
 					eachImage.getOperatingSystem().is64Bit()) {
 				String imageName = eachImage.getOperatingSystem().getName();
 				try {
 					Matcher matcher = pattern.matcher(imageName);
 					if (matcher.find()) {
 						Double versionOfImage = Double.valueOf(matcher.group());
 						if (versionOfImage.equals(versionToMatch)) {
 							image = eachImage;
 							imageFound = true;
 							break;
 						}
 					}
 				} catch (IllegalArgumentException e) {
 					continue;
 				}
 			}
 		}
 		if (!imageFound) {
 			throw new RuntimeException("CENTOS " + versionToMatch + " 64 bit not found");
 		}
 		return image;
     }
     
     private Hardware getDefaultHardware() {
     	Hardware hardware = null;
     	
     	Set<? extends Hardware> allHardware = this.listHardwareProfiles();
     	for (Hardware eachHardware : allHardware) {
     		if (eachHardware.getRam() == JCloudsWrapperServiceOpenStackConstants.DEFAULT_RAM) {
     			hardware = eachHardware;
     			break;
     		}
     	}
     	return hardware;
     	
     }
     
     private Hardware getEquivalentHardware(HardwareProfile hardwareProfile) {
     	Hardware hardware = null;    	
     	
     	if (hardwareProfile.equals(HardwareProfile.TINY_RAM_512)) {
     		hardware = this.matchHardware(512);
     	} else if (hardwareProfile.equals(HardwareProfile.SMALL_RAM_2048)) {
     		hardware = this.matchHardware(2048);
     	} else if (hardwareProfile.equals(HardwareProfile.MEDIUM_RAM_4096)) {
     		hardware = this.matchHardware(4096);
     	} else if (hardwareProfile.equals(HardwareProfile.LARGE_RAM_8192)) {
     		hardware = this.matchHardware(8192);
     	} else if (hardwareProfile.equals(HardwareProfile.XLARGE_RAM_16384)) {
     		hardware = this.matchHardware(16384);
     	}
     	return hardware;
     }
     
     private Hardware matchHardware(int ram) {
     	Hardware hardware = null;
     	boolean hardwareFound = false;
     	Set<? extends Hardware> allHardware = this.listHardwareProfiles();
     	
     	for (Hardware eachHardware : allHardware) {
     		if (eachHardware.getRam() == ram) {
     			hardware = eachHardware;
     			hardwareFound = true;
     			break;
     		}
     	}
     	if (!hardwareFound) {
     		throw new RuntimeException("Hardware with RAM + " + ram + " not found");
     	}
     	return hardware;
     }
     
     private TemplateBuilder getTemplateBuilder() {
     	return computeService.templateBuilder();
     }   
     
     private VMMetadata populateVMMetadata(NodeMetadata nodeMetadata) {
     	VMMetadata vmMetadata = new VMMetadata();    	
         vmMetadata.setGroupName(nodeMetadata.getGroup());
         vmMetadata.setHostname(nodeMetadata.getHostname());
         vmMetadata.setNodeId(nodeMetadata.getId());
         vmMetadata.setProviderId(nodeMetadata.getProviderId());
         vmMetadata.setPublicAddresses(nodeMetadata.getPublicAddresses());
         vmMetadata.setPrivateAddresses(nodeMetadata.getPrivateAddresses());
         vmMetadata.setUserMetadata(nodeMetadata.getUserMetadata());
         return vmMetadata;
     }
     
     private FloatingIPApi getFloatingIPApi() {
     	Set<String> configuredZones = this.getConfiguredZones();   	 
         String zone = null;
         FloatingIPApi floatingIPApi = null;
 
         if (configuredZones.isEmpty()) {
             throw new RuntimeException("No configured zones found");
         } else {
             Iterator<String> iter = configuredZones.iterator();
 
             if (iter.hasNext()) {
                 zone = iter.next();
             }
         }
         Optional<? extends FloatingIPApi> optionalFloatingIPApi = novaApi.getFloatingIPExtensionForZone(zone);
         if (optionalFloatingIPApi.isPresent()) {
         	floatingIPApi = optionalFloatingIPApi.get();
         }
         return floatingIPApi;
     }
     
     private void validateFloatingIPs(List<String> floatingIPAddresses) {    	
     	List<String> floatingIPAddressesAllocated = new ArrayList<String>();
     	
     	FluentIterable<? extends FloatingIP> floatingIPIterable = floatingIPApi.list();
     	for (FloatingIP eachFloatingIP : floatingIPIterable) {
     		if (eachFloatingIP.getInstanceId() == null) {
     			floatingIPAddressesAllocated.add(eachFloatingIP.getIp());
     		}
     	}    	
     	for (String eachFloatingIPAddress : floatingIPAddresses) {
     		boolean ipFound = false;
     		for (String eachFloatingIPAddressAllocated : floatingIPAddressesAllocated) {
     			if (eachFloatingIPAddress.trim().equals(eachFloatingIPAddressAllocated.trim())) {
     				ipFound = true;
     				break;
     			}
     		}
     		if (!ipFound) {
     			throw new RuntimeException("IP Address " + eachFloatingIPAddress + " does not exist in the Floating IPs pool");
     		}
     	}
     }    
 }
