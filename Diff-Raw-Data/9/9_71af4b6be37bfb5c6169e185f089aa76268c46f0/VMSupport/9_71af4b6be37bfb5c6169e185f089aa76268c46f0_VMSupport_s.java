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
 
 package org.dasein.cloud.terremark.compute;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.compute.AbstractVMSupport;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.VMLaunchOptions;
 import org.dasein.cloud.compute.VMLaunchOptions.NICConfig;
 import org.dasein.cloud.compute.VMScalingCapabilities;
 import org.dasein.cloud.compute.VMScalingOptions;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VirtualMachineProduct;
 import org.dasein.cloud.compute.VirtualMachineSupport;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.compute.VmStatistics;
 import org.dasein.cloud.compute.Volume;
 import org.dasein.cloud.identity.SSHKeypair;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.Firewall;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.NetworkInterface;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.terremark.EnvironmentsAndComputePools;
 import org.dasein.cloud.terremark.Layout;
 import org.dasein.cloud.terremark.Row;
 import org.dasein.cloud.terremark.Terremark;
 import org.dasein.cloud.terremark.TerremarkException;
 import org.dasein.cloud.terremark.TerremarkMethod;
 import org.dasein.cloud.terremark.TerremarkMethod.HttpMethodName;
 import org.dasein.cloud.terremark.identity.TerremarkKeypair;
 import org.dasein.cloud.terremark.network.FirewallRule;
 import org.dasein.cloud.terremark.network.TerremarkIpAddressSupport;
 import org.dasein.cloud.terremark.network.TerremarkNetworkSupport;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Megabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class VMSupport extends AbstractVMSupport {
 
 	// API Calls
 	public final static String VIRTUAL_MACHINES        = "virtualMachines";
 	public final static String LAYOUT                  = "layout";
 	public final static String LAYOUT_ROWS             = "layoutRows";
 	public final static String LAYOUT_GROUPS           = "layoutGroups";
 	public final static String POWER_ON                = "powerOn";
 	public final static String POWER_OFF               = "powerOff";
 	public final static String SHUTDOWN                = "shutdown";
 	public final static String REBOOT                  = "reboot";
 	public final static String COPY_IDENTICAL_VM       = "copyIdenticalVirtualMachine";
 
 	// Response Tags
 	public final static String VIRTUAL_MACHINE_TAG     = "VirtualMachine";
 	public final static String ROW_TAG                 = "Row";
 	public final static String GROUP_TAG               = "Group";
 
 	//Operation Names
 	public final static String CREATE_SERVER_OPERATION = "Create Server";
 	public final static String IMPORT_VM_OPERATION     = "Create Server from Catalog";
 	public final static String COPY_OPERATION          = "Copy Server";
 	public final static String POWER_ON_OPERATION      = "Power on Server";
 	public final static String SHUTDOWN_OPERATION      = "Shutdown Server";
 	public final static String POWER_OFF_OPERATION     = "Power off Server";
 	public final static String REBOOT_OPERATION        = "Restart Server";
 	public final static String DELETE_OPERATION        = "Delete Server";
 	public final static String CONFIGURE_OPERATION     = "Configure Server";
 
 	// Types
 	public final static String VIRTUAL_MACHINE_TYPE    = "application/vnd.tmrk.cloud.virtualMachine";
 	public final static String ROW_TYPE                = "application/vnd.tmrk.cloud.layoutRow";
 	public final static String GROUP_TYPE              = "application/vnd.tmrk.cloud.layoutGroup";	
 
 	//Layout Names
 	public final static String ROW_NAME                = "Dasein Cloud Row";
 	public final static String GROUP_NAME              = "Dasein Cloud Group";
 
 	public final static String DEFAULT_ROOT_USER       = "ecloud";
 
 	public final static long DEFAULT_SLEEP             = CalendarWrapper.SECOND * 20;
 	public final static long DEFAULT_TIMEOUT           = CalendarWrapper.MINUTE * 45;
 
 	static private final Logger logger = Logger.getLogger(VMSupport.class);
 
 	private Terremark provider = null;
 
 	static private final Random random = new Random();
 
 	static public String alphabet = "ABCEFGHJKMNPRSUVWXYZabcdefghjkmnpqrstuvwxyz0123456789#@()=+/{}[]<>,.?;':|-_!$%^&*~`";
 
 	static private Iterable<VirtualMachineProduct> products = null;
 
 	/**
 	 * Verifies that the following server name requirements have been met. Corrects invalid names.
 	 *  The name must begin with a letter
 	 *  The name may contain only letters, numbers, or hyphens
 	 *  The name must not exceed fifteen characters.
 	 * @param name the name you would like to use for a new server
 	 * @return a valid server name based on the input name
 	 */
 	private static String validateName(String name) {
 		name = name.replaceAll("_", "-");
 		name = name.replaceAll(" ", "-");
 		name = name.replaceAll("\\.","-");
 
 		StringBuilder str = new StringBuilder();
 		int charsAdded = 0;
 		int charNumber = 0;
 		while( charsAdded < name.length() && charsAdded < 15 ) {
 			char c = name.charAt(charNumber);
 
 			if (charNumber==0){
 				if (!Character.isLetter(c)){
 					str.append('a');
 					charsAdded++;
 				}
 			}
 			if( Character.isLetterOrDigit(c) || c == '-') {
 				str.append(c);
 				charsAdded++;
 			}
 			charNumber++;
 		}
 		return str.toString();
 	}
 
 	private HashMap<String,String> imageMap = new HashMap<String,String>();
 
 	public VMSupport(Terremark t) {
         super(t);
 		provider = t;
 	}
 
 	/**
 	 * Scales a virtual machine in accordance with the specified scaling options. Few clouds will support all possible
 	 * options. Therefore a client should check with the cloud's [VMScalingCapabilities] to see what can be scaled.
 	 * To support the widest variety of clouds, a client should be prepared for the fact that the returned virtual
 	 * machine will actually be different from the original. However, it isn't proper vertical scaling if the new VM
 	 * has a different state or if the old VM is still running. Ideally, it's just the same VM with it's new state.
 	 * @param vmId the virtual machine to scale
 	 * @param options the options governing how the virtual machine is scaled
 	 * @return a virtual machine representing the scaled virtual machine
 	 * @throws InternalException an internal error occurred processing the request
 	 * @throws CloudException an error occurred in the cloud processing the request
 	 */
 	@Override
 	public VirtualMachine alterVirtualMachine(String vmId, VMScalingOptions options) throws InternalException, CloudException {
 		String productString = options.getProviderProductId();
 		// product id format cpu:ram:disk
 		String cpuCount;
 		String ramSize;
 		String volumeSizes;
 		String[] productIds = productString.split(":");
 		if (productIds.length == 3) {
 			cpuCount = productIds[0];
 			ramSize = productIds[1];
 			volumeSizes = productIds[2].replace("[", "").replace("]", "");
 		}
 		else {
 			throw new InternalError("Invalid product id string. Product id format is cpu_count:ram_size:[disk_0_size,disk_1_size,disk_n_size]");
 		}
 		String[] diskSizes = volumeSizes.split(",");
 		String cpuOptions = "1,2,4,8";
 		if (!cpuOptions.contains(cpuCount)) {
 			throw new InternalException("Processor count must be 1, 2, 4, or 8");
 		}
 		int ramInt = Integer.parseInt(ramSize);
 		if(ramInt % 4 != 0) {
 			throw new InternalException("Memory size must be a multiple of four");
 		}
 		VirtualMachine vm = getVirtualMachine(vmId);
 		if (vm == null || vm.getCurrentState() == VmState.TERMINATED) {
 			throw new InternalException("Failed to find deployed vm: " + vmId);
 		}
 
 		Collection<Volume> volumes = provider.getComputeServices().getVolumeSupport().getVirtualMachineDisks(vmId);
 
 		Iterator<Volume> volumeItr = volumes.iterator();
 		for (String diskSize : diskSizes) {
 			int oldDiskSize = 0;
 			if (volumeItr.hasNext()) {
 				oldDiskSize = volumeItr.next().getSizeInGigabytes();
 			}
 			int newDiskSize = Integer.parseInt(diskSize);
 			if (newDiskSize > 512) {
 				throw new InternalException("Each disk size must be 512 GB or less");
 			}
 			if (oldDiskSize > 0 && newDiskSize < oldDiskSize) {
 				throw new InternalException("Disk capacity may not be reduced.");
 			}
 		}
 
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId + "/hardwareConfiguration";
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("HardwareConfiguration");
 
 			Element processorCountElement = doc.createElement("ProcessorCount");
 			processorCountElement.appendChild(doc.createTextNode(cpuCount));
 			rootElement.appendChild(processorCountElement);
 
 			Element memoryElement = doc.createElement("Memory");
 			Element memoryUnitElement = doc.createElement("Unit");
 			memoryUnitElement.appendChild(doc.createTextNode("MB"));
 			memoryElement.appendChild(memoryUnitElement);
 			Element memoryValueElement = doc.createElement("Value");
 			memoryValueElement.appendChild(doc.createTextNode(ramSize));
 			memoryElement.appendChild(memoryValueElement);
 			rootElement.appendChild(memoryElement);
 
 			Element disksElement = doc.createElement("Disks");
 			for (String diskSize : diskSizes) {
 				Element diskElement = doc.createElement("Disk");
 				Element diskSizeElement = doc.createElement("Size");
 
 				Element diskUnitElement = doc.createElement("Unit");
 				diskUnitElement.appendChild(doc.createTextNode("GB"));
 				diskSizeElement.appendChild(diskUnitElement);
 				Element diskValueElement = doc.createElement("Value");
 				diskValueElement.appendChild(doc.createTextNode(diskSize));
 				diskSizeElement.appendChild(diskValueElement);
 
 				diskElement.appendChild(diskSizeElement);
 				disksElement.appendChild(diskElement);
 			}
 			rootElement.appendChild(disksElement);
 
 			Element nicsElement = doc.createElement("Nics");
 			int nicCount = Integer.parseInt((String) vm.getTag("nic-count"));
 			for (int i=0; i<nicCount; i++) {
 				//Tag format nicNumber:nicNetworkHref:nicNetworkName:nicNetworkType
 				String[] nicInfo = ((String)vm.getTag("nic-" + i)).split(":");
 				String nicNumber = nicInfo[0];
 				String nicNetworkHref = nicInfo[1];
 				String nicNetworkName = nicInfo[2];
 				String nicNetworkType = nicInfo[3];
 				Element nicElement = doc.createElement("Nic");
 
 				Element unitNumberElement = doc.createElement("UnitNumber");
 				unitNumberElement.appendChild(doc.createTextNode(nicNumber));
 				nicElement.appendChild(unitNumberElement);
 
 				Element networkElement = doc.createElement("Network");
 				networkElement.setAttribute(Terremark.HREF, nicNetworkHref);
 				networkElement.setAttribute(Terremark.NAME, nicNetworkName);
 				networkElement.setAttribute(Terremark.TYPE, nicNetworkType);
 				nicElement.appendChild(networkElement);
 
 				nicsElement.appendChild(nicElement);
 			}
 			rootElement.appendChild(nicsElement);
 
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.PUT, url, null, body);
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, CONFIGURE_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 
 		return getVirtualMachine(vmId);
 	}
 
 	private void assignIpAddresses(String vmId, HashMap<String,List<String>> networksToAssign) throws CloudException, InternalException {
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId + "/assignedIps";
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("AssignedIpAddresses");
 
 			Element networksElement = doc.createElement("Networks");
 			String[] networks = networksToAssign.keySet().toArray(new String[0]);
 			for (int i=0; i<networks.length; i++) {
 				String networkId = networks[i];
 				VLAN network = provider.getNetworkServices().getVlanSupport().getVlan(networkId);
 				if (network != null) {
 					Element networkElement = doc.createElement("Network");
 					String networkHref = Terremark.DEFAULT_URI_PATH + "/" + TerremarkNetworkSupport.NETWORKS + "/" + networkId;
 					networkElement.setAttribute(Terremark.HREF, networkHref);
 					networkElement.setAttribute(Terremark.NAME, network.getName());
 					if (networkId.contains("ipv6")) {
 						networkElement.setAttribute(Terremark.TYPE, TerremarkNetworkSupport.NETWORK_IPV6_TYPE);
 					}
 					else {
 						networkElement.setAttribute(Terremark.TYPE, TerremarkNetworkSupport.NETWORK_TYPE);
 					}
 
 					Element ipAddressesElement = doc.createElement("IpAddresses");
 					List<String> ipAddressesToAssign = networksToAssign.get(networkId);
 					for (String ipAddressToAssign : ipAddressesToAssign) {
 						Element ipAddressElement = doc.createElement("IpAddress");
 						ipAddressElement.appendChild(doc.createTextNode(ipAddressToAssign));
 						ipAddressesElement.appendChild(ipAddressElement);
 					}
 
 					networkElement.appendChild(ipAddressesElement);
 					networksElement.appendChild(networkElement);
 				}
 			}
 
 			rootElement.appendChild(networksElement);	
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.PUT, url, null, body);
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, CONFIGURE_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	/**
 	 * Clones an existing virtual machine into a new copy.
 	 * @param vmId the ID of the server to be cloned
 	 * @param intoDcId the ID of the data center in which the new server will operate
 	 * @param name the name of the new server
 	 * @param description a description for the new server
 	 * @param powerOn power on the new server
 	 * @param firewallIds a list of firewall IDs to protect the new server
 	 * @return a newly deployed server cloned from the original
 	 * @throws InternalException an internal error occurred processing the request
 	 * @throws CloudException an error occurred in the cloud processing the request
 	 */
 	@Override
 	public VirtualMachine clone(@Nonnull String vmId, @Nonnull String intoDcId, @Nonnull String name, @Nonnull String description, boolean powerOn, @Nullable String ... firewallIds) throws InternalException, CloudException {
 		//TODO: Finish this.
 		VirtualMachine vmCopy = null;
 		String url = "/" + VIRTUAL_MACHINES + "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/" + intoDcId + "/" + Terremark.ACTION + "/" + COPY_IDENTICAL_VM;
 
 		String body = "";
 
 		Layout layout = getLayout(provider.getContext().getRegionId());
 		String rowId = null;
 		String groupId = null;
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("CopyIdenticalVirtualMachine");
 			rootElement.setAttribute(Terremark.NAME, name);
 
 			Element sourceElement = doc.createElement("Source");
 			sourceElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + VIRTUAL_MACHINES + "/" + vmId);
 			sourceElement.setAttribute(Terremark.TYPE, VIRTUAL_MACHINE_TYPE);
 			rootElement.appendChild(sourceElement);
 
 			Element layoutElement = doc.createElement("Layout");
 			if (layout.contains(ROW_NAME, GROUP_NAME)){
 				Row row = layout.getRowId(ROW_NAME, GROUP_NAME);
 				groupId = row.getGroupId(GROUP_NAME);
 				Element group = doc.createElement(GROUP_TAG);
 				group.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + LAYOUT_GROUPS + "/" + groupId);
 				group.setAttribute(Terremark.TYPE, GROUP_TYPE);
 				layoutElement.appendChild(group);
 
 			}
 			else if (layout.contains(ROW_NAME)){
 				rowId = layout.getRowId(ROW_NAME).getId();
 				Element row = doc.createElement(ROW_TAG);
 				row.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + LAYOUT_ROWS + "/" + rowId);
 				row.setAttribute(Terremark.TYPE, ROW_TYPE);
 				Element newGroup = doc.createElement("NewGroup");
 				newGroup.appendChild(doc.createTextNode(GROUP_NAME));
 				layoutElement.appendChild(row);
 				layoutElement.appendChild(newGroup);
 			}
 			else {
 				Element newRow = doc.createElement("NewRow");
 				Element newGroup = doc.createElement("NewGroup");
 				newRow.appendChild(doc.createTextNode(ROW_NAME));
 				newGroup.appendChild(doc.createTextNode(GROUP_NAME));
 				layoutElement.appendChild(newRow);
 				layoutElement.appendChild(newGroup);
 			}
 			rootElement.appendChild(layoutElement);
 
 			Element descriptionElement = doc.createElement("Description");
 			descriptionElement.appendChild(doc.createTextNode(description));
 			rootElement.appendChild(descriptionElement);
 
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, body);
 		Document doc = method.invoke();
 		if (doc != null) {
 			String newVmId = Terremark.hrefToId(doc.getElementsByTagName(VIRTUAL_MACHINE_TAG).item(0).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 			String taskHref = Terremark.getTaskHref(doc, COPY_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 			vmCopy = getVirtualMachine(newVmId);
 			if (powerOn && vmCopy.getCurrentState().equals(VmState.STOPPED)) {
 				start(newVmId);
 			}
 		}
 		return vmCopy;
 	}
 
 	/**
 	 * Describes the ways in which this cloud supports the vertical scaling of a virtual machine. A null response
 	 * means this cloud just doesn't support vertical scaling.
 	 * @return a description of how this cloud supports vertical scaling
 	 * @throws InternalException an internal error occurred processing the request
 	 * @throws CloudException an error occurred in the cloud processing the request
 	 */
 	@Override
 	public VMScalingCapabilities describeVerticalScalingCapabilities() throws CloudException, InternalException {
 		return VMScalingCapabilities.getInstance(false, true, Requirement.REQUIRED, Requirement.REQUIRED);
 	}
 
 	/**
 	 * Provides a number between 0 and 100 describing what percentage of the standard VM bill rate should be charged for
 	 * virtual machines in the specified state. 0 means that the VM incurs no charges while in the specified state, 100
 	 * means it incurs full charges, and a number in between indicates the percent discount that applies.
 	 * @param state the VM state being checked
 	 * @return the discount factor for VMs in the specified state
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public int getCostFactor(VmState state) throws InternalException, CloudException {
 		return (state.equals(VmState.STOPPED) ? 0 : 100);
 	}
 
 	/**
 	 * Returns the available layout for an environment/region.
 	 * Each layout has one or more rows. Each row has one or more groups.
 	 * @param environmentId the region id whose layout is being sought
 	 * @return the layout defined for the region
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	private Layout getLayout(String environmentId) throws CloudException, InternalException {
 		Layout layout = new Layout();
 		String url = "/" + LAYOUT + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + environmentId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		NodeList rowNodes = doc.getElementsByTagName(ROW_TAG);
 		for (int i=0; i<rowNodes.getLength();i++){
 			Node rowNode = rowNodes.item(i);
 			Row row = new Row();
 			NamedNodeMap rowAttrs = rowNode.getAttributes();
 			String rowHref = rowAttrs.getNamedItem(Terremark.HREF).getNodeValue();
 			String rowName = rowAttrs.getNamedItem(Terremark.NAME).getNodeValue();
 			row.setId(Terremark.hrefToId(rowHref));
 			row.setName(rowName);
 			NodeList rowChildren = rowNode.getChildNodes();
 			for (int j=0; j < rowChildren.getLength(); j++){
 				if (rowChildren.item(j).getNodeName().equals("Groups")){
 					NodeList groupNodes = rowChildren.item(j).getChildNodes();
 					for (int k=0; k<groupNodes.getLength();k++){
 						String groupHref = groupNodes.item(k).getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 						String groupName = groupNodes.item(k).getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 						row.addGroup(Terremark.hrefToId(groupHref), groupName);
 					}
 				}
 			}
 			layout.addRow(row);
 		}
 		return layout;
 	}
 
 	/**
 	 * Provides the maximum number of virtual machines that may be launched in this region for the current account.
 	 * @return the maximum number of launchable VMs or -1 for unlimited or -2 for unknown
 	 * @throws CloudException an error occurred fetching the limits from the cloud provider
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation determining the limits
 	 */
 	@Override
 	public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
 		return -2;
 	}
 
 	/**
 	 * Fetches the VM product associated with a specific product ID.
 	 * @param productId the virtual machine product ID (flavor, size, etc.)
 	 * @return the product represented by the specified product ID
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation fetching the product
 	 * @throws CloudException an error occurred fetching the product from the cloud
 	 */
 	@Override
 	public VirtualMachineProduct getProduct(@Nonnull String productId) throws InternalException, CloudException {
 		VirtualMachineProduct productMatch = null;
 		for( Architecture architecture : Architecture.values() ) {
 			for( VirtualMachineProduct product : listProducts(architecture) ) {
 				if( product.getProviderProductId().equals(productId) ) {
 					productMatch =  product;
 				}
 			}
 		}
 		if( productMatch == null && logger.isDebugEnabled() ) {
 			logger.debug("Unknown product ID for Terremark: " + productId);
 		}
 		return productMatch;
 	}
 
 	/**
 	 * Assists UIs by providing the cloud-specific term for a virtual server in the cloud.
 	 * @param locale the locale for which the term should be translated
 	 * @return the provider-specific term for a virtual server
 	 */
 	@Override
 	public String getProviderTermForServer(Locale locale) {
 		return "virtual machine";
 	}
 
 	/**
 	 * Generates a random password using the characters in the VMSupport.alphabet string with a length of 16 characters.
 	 * @return a 16 character password String
 	 */
 	public String getRandomPassword() {
 		StringBuilder password = new StringBuilder();
 		int rnd = random.nextInt();
 		int length = 16;
 
 		if( rnd < 0 ) {
 			rnd = -rnd;
 		}
 		length = length + (rnd%8);
 		while( password.length() < length ) {
 			char c;
 
 			rnd = random.nextInt();
 			if( rnd < 0 ) {
 				rnd = -rnd;
 			}
 			c = (char)(rnd%255);
 			if( alphabet.contains(String.valueOf(c)) ) {
 				password.append(c);
 			}
 		}
 		return password.toString();
 	}
 
 	/**
 	 * Provides the data from a specific virtual machine.
 	 * @param vmId the provider ID for the desired server
 	 * @return the data behind the target server
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public VirtualMachine getVirtualMachine(String vmId) throws InternalException, CloudException {
 		logger.trace("enter - getVirtualMachine(" + vmId + ")");
 		VirtualMachine vm = null;
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} catch (TerremarkException e) {
 			logger.warn("Failed to get vm " + vmId);
 		} catch (CloudException e) {
 			logger.warn("Failed to get vm " + vmId);
 		} catch (InternalException e) {
 			logger.warn("Failed to get vm " + vmId);
 		}
 		if (doc != null){
 			Node vmNode = doc.getElementsByTagName(VIRTUAL_MACHINE_TAG).item(0);
 			vm = toVirtualMachine(vmNode);
 		}
 		logger.trace("exit - getVirtualMachine(" + vmId + ")");
 		return vm;
 	}
 
 	/**
 	 * Provides hypervisor statistics for the specified server that fit within the defined time range.
 	 * For clouds that do not provide hypervisor statistics, this method should return an empty
 	 * {@link VmStatistics} object and NOT <code>null</code>.
 	 * @param vmId the unique ID for the target server 
 	 * @param from the beginning of the timeframe for which you want statistics
 	 * @param to the end of the timeframe for which you want statistics
 	 * @return the statistics for the target server
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public VmStatistics getVMStatistics(String vmId, long from, long to) throws InternalException, CloudException {
 		// TODO Implement this.
 		return new VmStatistics();
 	}
 
 	/**
 	 * Provides hypervisor statistics for the specified server that fit within the defined time range.
 	 * For clouds that do not provide hypervisor statistics, this method should return an empty
 	 * list.
 	 * @param vmId the unique ID for the target server 
 	 * @param from the beginning of the timeframe for which you want statistics
 	 * @param to the end of the timeframe for which you want statistics
 	 * @return a collection of discreet server statistics over the specified period
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public @Nonnull Iterable<VmStatistics> getVMStatisticsForPeriod(@Nonnull String vmId, @Nonnegative long from, @Nonnegative long to) throws InternalException, CloudException {
 		// TODO Implement this.
 		return Collections.emptyList();
 	}
 
 	private String guessImageId(String osDescription) throws CloudException, InternalException {
 		String searchString;
 		Architecture arch = null;
 		Platform platform = null;
 		String providerImageId = null;
 		if( osDescription == null ) {
 			return null;
 		}
 		if( imageMap.containsKey(osDescription) ) {
 			return imageMap.get(osDescription);
 		}
 		if( osDescription.contains("RHEL") || osDescription.contains("Red Hat") ) {
 			platform = Platform.RHEL;
 			searchString = "RHEL";
 		}
 		else if( osDescription.contains("CentOS") ) {
 			platform = Platform.CENT_OS;
 			searchString = "CentOS";
 		}
 		else if( osDescription.contains("Ubuntu") ) {
 			platform = Platform.UBUNTU;
 			searchString = "Ubuntu Server";
 		}
 		else if( osDescription.contains("Windows") && osDescription.contains("2003") ) {
 			platform = Platform.WINDOWS;
 			if( osDescription.contains("Enterprise") ) {
 				searchString = "Windows 2003 Enterprise";
 			}
 			else {
 				searchString = "Windows 2003 Standard";
 			}
 		}
 		else if( osDescription.contains("Windows") && osDescription.contains("2008") ) {
 			platform = Platform.WINDOWS;
 			if( osDescription.contains("Enterprise") ) {
 				searchString = "Windows Server 2008 Enterprise";
 			}
 			else if( osDescription.contains("Web") ) {
 				searchString = "Windows Web Server 2008";
 			}
 			else {
 				searchString = "Windows Server 2008 Standard";                
 			}
 		}
 		else {
 			searchString = osDescription;
 		}
 
 		if (osDescription.contains("32-bit") || osDescription.contains("32 bit")) {
 			arch = Architecture.I32;
 		}
 		else if (osDescription.contains("64-bit") || osDescription.contains("64 bit")) {
 			arch = Architecture.I64;
 		}
 		logger.debug("guessImageId(): Calling list machine images");
 		Iterable<MachineImage> images = provider.getComputeServices().getImageSupport().searchPublicImages(searchString, platform, arch, ImageClass.MACHINE);
 		for( MachineImage image : images) {
 			imageMap.put(osDescription, image.getProviderMachineImageId());
 			providerImageId = image.getProviderMachineImageId();
 			break;
 		}
 		return providerImageId;
 	}
 
 	/**
 	 * Identifies whether images of the specified image class are required for launching a VM. This method should
 	 * always return {@link Requirement#REQUIRED} when the image class chosen is {@link ImageClass#MACHINE}.
 	 * @param cls the desired image class
 	 * @return the requirements level of support for this image class
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 */
 	@Override
 	public Requirement identifyImageRequirement(ImageClass cls) throws CloudException, InternalException {
 		return (ImageClass.MACHINE.equals(cls) ? Requirement.REQUIRED : Requirement.NONE);
 	}
 
 	/**
 	 * Indicates the degree to which specifying a user name and password at launch is required for a Unix operating system.
 	 * @return the requirements level for specifying a user name and password at launch
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 * @deprecated Use {@link #identifyPasswordRequirement(Platform)}
 	 */
 	@Deprecated
 	@Override
 	public Requirement identifyPasswordRequirement() throws CloudException,InternalException {
 		return Requirement.REQUIRED;
 	}
 
 	/**
 	 * Indicates the degree to which specifying a user name and password at launch is required.
 	 * @param platform the platform for which password requirements are being sought
 	 * @return the requirements level for specifying a user name and password at launch
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 */
 	@Override
 	public Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException {
 		if (Platform.WINDOWS.equals(platform)) {
 			return Requirement.REQUIRED;
 		}
 		else {
 			return Requirement.NONE;
 		}
 	}
 
 	/**
 	 * Indicates whether or not a root volume product must be specified when launching a virtual machine.
 	 * @return the requirements level for a root volume product
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 */
 	@Override
 	public Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
 		return Requirement.NONE;
 	}
 
 	/**
 	 * Indicates the degree to which specifying a shell key at launch is required for a Unix operating system.
 	 * @return the requirements level for shell key support at launch
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 * @deprecated Use {@link #identifyShellKeyRequirement(Platform)}
 	 */
 	@Deprecated
 	@Override
 	public Requirement identifyShellKeyRequirement() throws CloudException, InternalException {
 		return Requirement.REQUIRED;
 	}
 
 	/**
 	 * Indicates the degree to which specifying a shell key at launch is required.
 	 * @param platform the target platform for which you are testing
 	 * @return the requirements level for shell key support at launch
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 */
 	@Override
 	public Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException {
 		if (Platform.WINDOWS.equals(platform)) {
 			return Requirement.NONE;
 		}
 		else {
 			return Requirement.REQUIRED;
 		}
 	}
 
 	/**
 	 * Indicates the degree to which static IP addresses are required when launching a VM.
 	 * @return the requirements level for static IP on launch
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 */
 	@Override
 	public Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
 		return Requirement.REQUIRED;
 	}
 
 	/**
 	 * Indicates whether or not specifying a VLAN in your VM launch options is required or optional.
 	 * @return the requirements level for a VLAN during launch
 	 * @throws CloudException an error occurred in the cloud identifying this requirement
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation identifying this requirement
 	 */
 	@Override
 	public Requirement identifyVlanRequirement() throws CloudException, InternalException {
 		return Requirement.REQUIRED;
 	}
 
 	/**
 	 * Indicates that the ability to terminate the VM via API can be disabled.
 	 * @return true if the cloud supports the ability to prevent API termination
 	 * @throws CloudException an error occurred in the cloud while determining this capability
 	 * @throws InternalException an error occurred in the Dasein Cloud implementation determining this capability
 	 */
 	@Override
 	public boolean isAPITerminationPreventable() throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Indicates whether or not this cloud provider supports basic analytics. Basic analytics are analytics
 	 * that are being gathered for every virtual machine without any intervention necessary to enable them. Extended
 	 * analytics implies basic analytics, so this method should always be true if {@link #isExtendedAnalyticsSupported()} 
 	 * is true (even if there are, in fact, only extended analytics).
 	 * @return true if the cloud provider supports the gathering of extended analytics
 	 * @throws CloudException an error occurred in the cloud provider determining extended analytics support
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation determining extended analytics support
 	 */
 	@Override
 	public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Indicates whether or not this cloud provider supports extended analytics. Extended analytics are analytics
 	 * that must be specifically enabled above and beyond any basic analytics the cloud provider is gathering.
 	 * @return true if the cloud provider supports the gathering of extended analytics
 	 * @throws CloudException an error occurred in the cloud provider determining extended analytics support
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation determining extended analytics support
 	 */
 	@Override
 	public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Indicates whether this account is subscribed to using virtual machines.
 	 * @return true if the subscription is valid for using virtual machines
 	 * @throws CloudException an error occurred querying the cloud for subscription info
 	 * @throws InternalException an error occurred within the implementation determining subscription state
 	 */
 	@Override
 	public boolean isSubscribed() throws CloudException, InternalException {
 		try {
 			listVirtualMachines();
 			return true;
 		}
 		catch(TerremarkException e ) {
 			logger.warn("Could not determine subscription status for VMs in Terremark for " + provider.getContext().getAccountNumber() + ": " + e.getMessage());
 			if( logger.isDebugEnabled() ) {
 				e.printStackTrace();
 			}
 			throw new CloudException(e);
 		}
 		catch( RuntimeException e ) {
 			logger.warn("Could not determine subscription status for VMs in Terremark for " + provider.getContext().getAccountNumber() + ": " + e.getMessage());
 			if( logger.isDebugEnabled() ) {
 				e.printStackTrace();
 			}
 			throw new InternalException(e);            
 		}
 	}
 
 	/**
 	 * Indicates whether or not the cloud allows bootstrapping with user data.
 	 * @return true of user-data bootstrapping is supported
 	 * @throws CloudException an error occurred querying the cloud for this kind of support
 	 * @throws InternalException an error inside the Dasein Cloud implementation occurred determining support
 	 */
 	@Override
 	public boolean isUserDataSupported() throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Preferred mechanism for launching a virtual machine in the cloud. This method accepts a rich set of launch
 	 * configuration options that define what the virtual machine should look like once launched. These options may
 	 * include things that behave very differently in some clouds. It is expected that the method will return 
 	 * immediately once Dasein Cloud as a trackable server ID, even if it has to spawn off a background thread
 	 * to complete follow on tasks (such as provisioning and attaching volumes).
 	 * @param withLaunchOptions the launch options to use in creating a new virtual machine
 	 * @return the newly created virtual machine
 	 * @throws CloudException the cloud provider errored out when launching the virtual machine
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 */
 	@Override
 	public VirtualMachine launch(VMLaunchOptions withLaunchOptions) throws CloudException, InternalException {
 		logger.trace("enter() - launch()");
 		ProviderContext ctx = provider.getContext();
 		if( ctx == null ) {
 			throw new CloudException("No context was established for this request");
 		}
 
 		//fromMachineImageId is of the form imageId:computePoolId:imageType
 		String imageId = null;
 		String imageType = null;
 		String imageDataCenterId = null;
 		if (withLaunchOptions.getMachineImageId().contains(":")){
 			String[] imageIds = withLaunchOptions.getMachineImageId().split(":");
 			imageId = imageIds[0];
 			imageDataCenterId = imageIds[1];
 			imageType = imageIds[2];
 		}
 		else {
 			throw new InternalException("Invalid image id: " + withLaunchOptions.getMachineImageId());
 		}
 
 		VirtualMachine vm = null;
 
 		if (imageType.equalsIgnoreCase(Template.ImageType.TEMPLATE.name())) {
 			if (withLaunchOptions.getDataCenterId() == null) {
 				withLaunchOptions.inDataCenter(imageDataCenterId);
 			}
 			if (!imageDataCenterId.equals(withLaunchOptions.getDataCenterId())) {
 				throw new InternalException("The requested data center " + withLaunchOptions.getDataCenterId() + " does not match the available data center for this image: " + imageDataCenterId);
 			}
 			vm = launchFromTemplate(imageId, withLaunchOptions.getStandardProductId(), withLaunchOptions.getDataCenterId(), withLaunchOptions.getFriendlyName(), withLaunchOptions.getDescription(), withLaunchOptions.getBootstrapKey(), withLaunchOptions.getBootstrapPassword(), withLaunchOptions.getVlanId(), withLaunchOptions.getNetworkInterfaces(), withLaunchOptions.getMetaData());
 		}
 		else if (imageType.equalsIgnoreCase(Template.ImageType.CATALOG_ENTRY.name())) {
 			vm = launchFromCatalogItem(imageId, withLaunchOptions.getStandardProductId(), withLaunchOptions.getDataCenterId(), withLaunchOptions.getFriendlyName(), withLaunchOptions.getDescription(), withLaunchOptions.getVlanId(), withLaunchOptions.getNetworkInterfaces(), withLaunchOptions.getMetaData());
 		}
 
 		return vm;
 	}
 
 	private @Nonnull VirtualMachine launchFromCatalogItem(@Nonnull String catalogId, @Nonnull String productString, @Nonnull String dataCenterId, @Nonnull String name, @Nonnull String description, @Nullable String inVlanId, @Nullable NICConfig[] nics, @Nullable Map<String, Object> tags)	throws InternalException, CloudException {
 		logger.trace("enter() - launchFromCatalogItem()");
 
 		final ProviderContext ctx = provider.getContext();
 		VirtualMachine server = null;
 		String vlanId = inVlanId;
 		if( vlanId == null ) {
 			vlanId = getFirstVlan();
 		}
 
 		VirtualMachineProduct product = parseProductString(productString);
 
 		Layout layout = getLayout(ctx.getRegionId());
 		MachineImage catalogEntry = null;
 
 		String url = "/" + VIRTUAL_MACHINES + "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/" + dataCenterId + "/action/importVirtualMachine";
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("ImportVirtualMachine");
 
 			addNameAttribute(rootElement, name);
 			addVmHardwareElements(doc, rootElement, product);
 			addLayoutElements(doc, rootElement, layout);
 			addDescriptionElement(doc, rootElement, description);
 			final String catalogHref = "/" + Terremark.ADMIN + "/" + Template.CATALOG + "/" + catalogId;
 			addTagsElement(doc, rootElement, catalogHref, tags);
 
 			catalogEntry = provider.getComputeServices().getImageSupport().getImage(catalogId+"::"+Template.ImageType.CATALOG_ENTRY.name());
 
 			if (catalogEntry == null) {
 				throw new CloudException("launchFromCatalogItem(): Failed to find machine image " + catalogId);
 			}
 
 			Element catalogEntryElement = doc.createElement("CatalogEntry");
 			catalogEntryElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + catalogHref);
 			rootElement.appendChild(catalogEntryElement);
 
 			Element networkMappingsElement = doc.createElement("NetworkMappings");
 
 			int networkMappingCount = 0;
 			try {
 				networkMappingCount = Integer.parseInt((String)catalogEntry.getTag("NetworkMappingCount"));
 				logger.debug("launchFromCatalogItem(): NetworkMappingCount = " + networkMappingCount);
 			}
 			catch (NumberFormatException e) {
 				throw new InternalException("launchFromCatalogItem(): Problem parsing NetworkMappingCount tag from catalog item.");
 			}
 
 			for (int i=0; i<networkMappingCount; i++) {
 				String nmName = (String)catalogEntry.getTag(Template.NETWORK_MAPPING_NAME + "-" + i);
 				logger.debug("launchFromCatalogItem(): Adding NetworkMappring for " + nmName);
 				Element networkMappingElement = doc.createElement("NetworkMapping");
 				networkMappingElement.setAttribute(Terremark.NAME, nmName);
 
 				addNetworkElement(doc, networkMappingElement, vlanId);
 
 				networkMappingsElement.appendChild(networkMappingElement);
 			}
 
 			rootElement.appendChild(networkMappingsElement);
 
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 
 		} catch (ParserConfigurationException e) {
 			if (logger.isDebugEnabled()) {
 				logger.warn("launchFromCatalogItem(): Error creating a new instance of a DocumentBuilder", e);
 			}
 			else {
 				logger.warn("launchFromCatalogItem(): Error creating a new instance of a DocumentBuilder");
 			}
 		} catch (TransformerException e) {
 			if (logger.isDebugEnabled()) {
 				logger.warn("launchFromCatalogItem(): Error transforming the xml", e);
 			}
 			else {
 				logger.warn("launchFromCatalogItem(): Error transforming the xml");
 			}
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, body);
 		Document responseDoc = method.invoke();
 
 		String vmId = Terremark.hrefToId(responseDoc.getElementsByTagName(VIRTUAL_MACHINE_TAG).item(0).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 
 		String taskHref = Terremark.getTaskHref(responseDoc, IMPORT_VM_OPERATION);
 		provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 
 		HashMap<String,List<String>> networkMap = new HashMap<String, List<String>>();
 
 		if (nics != null) {
 			for (NICConfig nicConfig : nics) {
 				NetworkInterface nic = provider.getNetworkServices().getVlanSupport().getNetworkInterface(nicConfig.nicId);
 				String nicVlanId = nic.getProviderVlanId();
 				if (networkMap.containsKey(nicVlanId)) {
 					List<String> networkIps = networkMap.get(nicVlanId);
 					for (RawAddress address : nic.getIpAddresses()) {
 						networkIps.add(address.getIpAddress());
 					}
 					networkMap.put(nicVlanId, networkIps);
 				}
 				else {
 					List<String> networkIps = new ArrayList<String>();
 					for (RawAddress address : nic.getIpAddresses()) {
 						networkIps.add(address.getIpAddress());
 					}
 					networkMap.put(nicVlanId, networkIps);
 				}
 			}
 		}
 		else {
 			String availableIpAddress = provider.getNetworkServices().getIpAddressSupport().getUnreservedAvailablePrivateIp(vlanId);
 			if (availableIpAddress == null) {
 				throw new CloudException("Failed to find an available private ip");
 			}
 			else {
 				List<String> networkIps = new ArrayList<String>();
 				networkIps.add(availableIpAddress);
 				networkMap.put(vlanId, networkIps);
 			}
 		}
 
 		assignIpAddresses(vmId, networkMap);
 
 		logger.debug("launchFromCatalogItem(): getting virtual machine " + vmId);
 		server = getVirtualMachine(vmId);
 		final String serverId = server.getProviderVirtualMachineId();
 
 		Thread t = new Thread() {
             public void run() {
                 provider.hold();
                 try {
                     try {
                     	waitForCatalogTask(serverId);
                     }
                     catch( Throwable t ) {
                         logger.error("launchFromCatalogItem(): Failed while polling launch task for server " + serverId + ": " + t.getMessage());
                         t.printStackTrace();
                     }
                 }
                 finally {
                     provider.release();
                 }
             }
         };
         t.setName("Wait for Terremark Catalog Item Launch " + server.getProviderVirtualMachineId());
         t.setDaemon(true);
         t.start();
 
 		if (server.getCurrentState().equals(VmState.STOPPED)) {
 			start(vmId);
 			server = getVirtualMachine(vmId);
 		}
 
 		logger.trace("exit() - launchFromCatalogItem()");
 		return server;
 	}
 	
 	private void waitForCatalogTask(String serverId) throws InternalException, CloudException {
 		final long catalogImportTimeout = CalendarWrapper.HOUR * 28;
 		long waitTime = 0;
 		long sleepTime;
 		VirtualMachine server = getVirtualMachine(serverId);
 		while (server.getCurrentState().equals(VmState.PENDING) && waitTime < catalogImportTimeout) {
 			try {
 				if (waitTime < DEFAULT_TIMEOUT) {
 					sleepTime = DEFAULT_SLEEP;
 				}
 				else {
 					sleepTime = CalendarWrapper.MINUTE * 10;
 				}
 				Thread.sleep(sleepTime);
 				waitTime += sleepTime;
 				server = getVirtualMachine(serverId);
 				if (server.getCurrentState().equals(VmState.TERMINATED)) {
 					break;
 				}
 			} catch (InterruptedException e) {
 				if (logger.isDebugEnabled()) {
 					logger.warn("waitForCatalogTask(): Thread was interrupted waiting for server to leave pending state.", e);
 				}
 				else {
 					logger.warn("waitForCatalogTask(): Thread was interrupted waiting for server to leave pending state.");
 				}
 			}
 		}
 	}
 
 	private void addNetworkElement(Document doc, Element networkParentElement, String vlanId) throws CloudException, InternalException {
 		VLAN network = provider.getNetworkServices().getVlanSupport().getVlan(vlanId);
 		Element networkElement = doc.createElement(TerremarkNetworkSupport.NETWORK_TAG);
 		networkElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + TerremarkNetworkSupport.NETWORKS + "/" + vlanId);
 		networkElement.setAttribute(Terremark.NAME, network.getName());
 		networkElement.setAttribute(Terremark.TYPE, TerremarkNetworkSupport.NETWORK_TYPE);
 		networkParentElement.appendChild(networkElement);
 	}
 
 	private void addTagsElement(Document doc, Element rootElement, String imageId, Map<String, Object> tags) {
 		Element tagsElement = doc.createElement("Tags");
 
 		//Add a tag with the image ID so we will know what image the server was launched from when we discover it
 		Element templateTagElement = doc.createElement("Tag");
 		templateTagElement.appendChild(doc.createTextNode(imageId));
 		tagsElement.appendChild(templateTagElement);
 		rootElement.appendChild(tagsElement);
 
 		for(String key: tags.keySet()){
 			Element tagElement = doc.createElement("Tag");
 			String tagValue = tags.get(key).toString();
 			String tag = key + "=" + tagValue;
 			tag = Terremark.removeCommas(tag);
 			tagElement.appendChild(doc.createTextNode(tag));
 			tagsElement.appendChild(tagElement);
 			rootElement.appendChild(tagsElement);
 		}
 	}
 
 	private void addDescriptionElement(Document doc, Element rootElement, String description) {
 		if( description.length() > 100 ) {
 			description = description.substring(0, 100);
 		}
 		Element descriptionElement = doc.createElement("Description");
 		descriptionElement.appendChild(doc.createTextNode(description));
 		rootElement.appendChild(descriptionElement);
 	}
 
 	private void addLayoutElements(Document doc, Element rootElement, Layout layout) {
 		Element layoutElement = doc.createElement("Layout");
 		if (layout.contains(ROW_NAME, GROUP_NAME)){
 			Row row = layout.getRowId(ROW_NAME, GROUP_NAME);
 			String groupId = row.getGroupId(GROUP_NAME);
 			Element group = doc.createElement(GROUP_TAG);
 			group.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + LAYOUT_GROUPS + "/" + groupId);
 			group.setAttribute(Terremark.TYPE, GROUP_TYPE);
 			layoutElement.appendChild(group);
 
 		}
 		else if (layout.contains(ROW_NAME)){
 			String rowId = layout.getRowId(ROW_NAME).getId();
 			Element row = doc.createElement(ROW_TAG);
 			row.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + LAYOUT_ROWS + "/" + rowId);
 			row.setAttribute(Terremark.TYPE, ROW_TYPE);
 			Element newGroup = doc.createElement("NewGroup");
 			newGroup.appendChild(doc.createTextNode(GROUP_NAME));
 			layoutElement.appendChild(row);
 			layoutElement.appendChild(newGroup);
 		}
 		else {
 			Element newRow = doc.createElement("NewRow");
 			Element newGroup = doc.createElement("NewGroup");
 			newRow.appendChild(doc.createTextNode(ROW_NAME));
 			newGroup.appendChild(doc.createTextNode(GROUP_NAME));
 			layoutElement.appendChild(newRow);
 			layoutElement.appendChild(newGroup);
 		}
 		rootElement.appendChild(layoutElement);		
 	}
 
 	private void addVmHardwareElements(Document doc, Element rootElement, VirtualMachineProduct product) {
 		Element processorCount = doc.createElement("ProcessorCount");
 		String cpuCount = Integer.toString(product.getCpuCount());
 		processorCount.appendChild(doc.createTextNode(cpuCount));
 		rootElement.appendChild(processorCount);
 
 		Element memory = doc.createElement("Memory");
 		Element unit = doc.createElement("Unit");
 		Element value = doc.createElement("Value");
 		unit.appendChild(doc.createTextNode("MB"));
 		String ramInMB = Integer.toString(product.getRamSize().intValue());
 		value.appendChild(doc.createTextNode(ramInMB));
 		memory.appendChild(unit);
 		memory.appendChild(value);
 		rootElement.appendChild(memory);
 	}
 
 	private @Nonnull VirtualMachine launchFromTemplate(@Nonnull String templateId, @Nonnull String productString, @Nonnull String dataCenterId, @Nonnull String name, @Nonnull String description, @Nullable String withKeypairId, @Nullable String withPassword, @Nullable String inVlanId, @Nullable NICConfig[] nics, @Nullable Map<String, Object> tags) throws InternalException, CloudException {
 		logger.trace("enter() - launchFromTemplate()");
 
 		ProviderContext ctx = provider.getContext();
 		VirtualMachine server = null;
 		if( inVlanId == null ) {
 			inVlanId = getFirstVlan();
 		}
 
 		VirtualMachineProduct product = parseProductString(productString);
 
 		Layout layout = getLayout(ctx.getRegionId());	
 		MachineImage template = null;
 
 		String url = "/" + VIRTUAL_MACHINES + "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/" + dataCenterId + "/action/createVirtualMachine";
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("CreateVirtualMachine");
 			addNameAttribute(rootElement, name);
 			addVmHardwareElements(doc, rootElement, product);
 			addLayoutElements(doc, rootElement, layout);
 			addDescriptionElement(doc, rootElement, description);
 			String templateHref = "/" + Template.TEMPLATES + "/" + templateId + "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/" + dataCenterId;
 			
 			Element tagsElement = doc.createElement("Tags");
 
 			//Add a tag with the image ID so we will know what image the server was launched from when we discover it
 			Element templateTagElement = doc.createElement("Tag");
 			templateTagElement.appendChild(doc.createTextNode(templateHref));
 			tagsElement.appendChild(templateTagElement);
 			rootElement.appendChild(tagsElement);
 
 			for(String key: tags.keySet()){
 				Element tagElement = doc.createElement("Tag");
 				String tagValue = tags.get(key).toString();
 				String tag = key + "=" + tagValue;
 				tag = Terremark.removeCommas(tag);
 				tagElement.appendChild(doc.createTextNode(tag));
 				tagsElement.appendChild(tagElement);
 			}
 			rootElement.appendChild(tagsElement);
 
 			String machineImageId = templateId + ":" + dataCenterId + ":" + Template.ImageType.TEMPLATE;
 			template = provider.getComputeServices().getImageSupport().getImage(machineImageId);
 
 			if (template == null) {
 				throw new CloudException("Failed to find machine image " + machineImageId);
 			}
 
 			ArrayList<NetworkInterface> nicsToAssign = new ArrayList<NetworkInterface>();
 
 			if (nics != null) {
 				int nicSize = 0;
 				while (nicSize < 4) {
 					for (NICConfig nicConfig : nics) {
 						NetworkInterface nic = provider.getNetworkServices().getVlanSupport().getNetworkInterface(nicConfig.nicId);
 						nicsToAssign.add(nic);
 						nicSize++;
 					}
 				}
 
 			}
 			else {
 				String availableIpAddress = provider.getNetworkServices().getIpAddressSupport().getUnreservedAvailablePrivateIp(inVlanId);
 				if (availableIpAddress == null) {
 					throw new CloudException("Failed to find an available private ip");
 				}
 				else {
 					NetworkInterface nic = new NetworkInterface();
 					RawAddress[] addresses = null;
 					RawAddress rawAddress = new RawAddress(availableIpAddress);
 
 
 					if (rawAddress.getVersion().equals(IPVersion.IPV6)) {
 						addresses = new RawAddress[2];
 						addresses[0] = rawAddress;
 
 						String ipV4Vlan = inVlanId.replace("/ipv6", "");
 						String availableIpAddressV4 = provider.getNetworkServices().getIpAddressSupport().getUnreservedAvailablePrivateIp(ipV4Vlan);
 						if (availableIpAddress != null) {
 							RawAddress rawAddressV4 = new RawAddress(availableIpAddressV4);
 							addresses[1] = rawAddressV4;
 						}
 					}
 					else {
 						addresses = new RawAddress[1];
 						addresses[0] = rawAddress;
 					}
 
 					nic.setIpAddresses(addresses);
 					nic.setProviderVlanId(inVlanId);
 					nicsToAssign.add(nic);
 				}
 			}
 
 			if (template.getPlatform().equals(Platform.WINDOWS)){
 				Element customiztionElement = doc.createElement("WindowsCustomization");
 
 				Element networkSettingsElement = doc.createElement("NetworkSettings");
 				Element networkAdapterSettingsElement = doc.createElement("NetworkAdapterSettings");
 				for (NetworkInterface nic : nicsToAssign) {
 					RawAddress[] nicAddresses = nic.getIpAddresses();
 					int numNicAddresses = nicAddresses.length;
 					boolean dualStack = false;
 					if (numNicAddresses == 2) {
 						if (!nicAddresses[0].getVersion().equals(nicAddresses[1].getVersion())) {
 							dualStack = true;
 						}
 						else {
 							logger.warn("Only one ip address of each version can be specified per network interface. Using the first ip in the array.");
 						}
 					}
 					else if (numNicAddresses > 2) {
 						logger.warn("Only one ip address of each version can be specified per network interface. Using the first ip in the array.");
 					}
 					if (dualStack) {
 						Element networkAdapterElement = doc.createElement("NetworkAdapter");
 
 						addNetworkElement(doc, networkAdapterElement, nic.getProviderVlanId());
 
 						String ipv4Address = "";
 						String ipv6Address = "";
 						for (RawAddress address : nicAddresses) {
 							if (address.getVersion().equals(IPVersion.IPV4)) {
 								ipv4Address = address.getIpAddress();
 							}
 							else if (address.getVersion().equals(IPVersion.IPV6)) {
 								ipv6Address = address.getIpAddress();
 							}
 						}
 
 						Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_TAG); 
 						ipAddressElement.appendChild(doc.createTextNode(ipv4Address));
 						networkAdapterElement.appendChild(ipAddressElement);
 
 						Element ipV6AddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_V6_TAG); 
 						ipV6AddressElement.appendChild(doc.createTextNode(ipv6Address));
 						networkAdapterElement.appendChild(ipV6AddressElement);
 
 						networkAdapterSettingsElement.appendChild(networkAdapterElement);
 					}
 					else {
 						Element networkAdapterElement = doc.createElement("NetworkAdapter");
 						addNetworkElement(doc, networkAdapterElement, nic.getProviderVlanId());
 
 						RawAddress address = nicAddresses[0];
 						if (address.getVersion().equals(IPVersion.IPV4)) {
 							Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_TAG); 
 							ipAddressElement.appendChild(doc.createTextNode(address.getIpAddress()));
 							networkAdapterElement.appendChild(ipAddressElement);
 						}
 						else if (address.getVersion().equals(IPVersion.IPV6)) {
 							Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_V6_TAG); 
 							ipAddressElement.appendChild(doc.createTextNode(address.getIpAddress()));
 							networkAdapterElement.appendChild(ipAddressElement);
 						}
 
 						networkAdapterSettingsElement.appendChild(networkAdapterElement);
 					}
 				}
 
 				networkSettingsElement.appendChild(networkAdapterSettingsElement);
 				//Optional DNS Settings Go Here
 				customiztionElement.appendChild(networkSettingsElement);
 
 				Element passwordElement = doc.createElement("Password");
 				if (withPassword == null) {
 					withPassword = getRandomPassword();
 				}
 				passwordElement.appendChild(doc.createTextNode(withPassword));
 				customiztionElement.appendChild(passwordElement);
 
 				rootElement.appendChild(customiztionElement);
 			}
 			else {
 				Element customiztionElement = doc.createElement("LinuxCustomization");
 
 				Element networkSettingsElement = doc.createElement("NetworkSettings");
 				Element networkAdapterSettingsElement = doc.createElement("NetworkAdapterSettings");
 				for (NetworkInterface nic : nicsToAssign) {
 					RawAddress[] nicAddresses = nic.getIpAddresses();
 					int numNicAddresses = nicAddresses.length;
 					boolean dualStack = false;
 					if (numNicAddresses == 2) {
 						if (!nicAddresses[0].getVersion().equals(nicAddresses[1].getVersion())) {
 							dualStack = true;
 						}
 						else {
 							logger.warn("Only one ip address of each version can be specified per network interface. Using the first ip in the array.");
 						}
 					}
 					else if (numNicAddresses > 2) {
 						logger.warn("Only one ip address of each version can be specified per network interface. Using the first ip in the array.");
 					}
 					if (dualStack) {
 						Element networkAdapterElement = doc.createElement("NetworkAdapter");
 						addNetworkElement(doc, networkAdapterElement, nic.getProviderVlanId());
 
 						String ipv4Address = "";
 						String ipv6Address = "";
 						for (RawAddress address : nicAddresses) {
 							if (address.getVersion().equals(IPVersion.IPV4)) {
 								ipv4Address = address.getIpAddress();
 							}
 							else if (address.getVersion().equals(IPVersion.IPV6)) {
 								ipv6Address = address.getIpAddress();
 							}
 						}
 
 						Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_TAG); 
 						ipAddressElement.appendChild(doc.createTextNode(ipv4Address));
 						networkAdapterElement.appendChild(ipAddressElement);
 
 						Element ipV6AddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_V6_TAG); 
 						ipV6AddressElement.appendChild(doc.createTextNode(ipv6Address));
 						networkAdapterElement.appendChild(ipV6AddressElement);
 
 						networkAdapterSettingsElement.appendChild(networkAdapterElement);
 					}
 					else {
 						Element networkAdapterElement = doc.createElement("NetworkAdapter");
 						addNetworkElement(doc, networkAdapterElement, nic.getProviderVlanId());
 
 						RawAddress address = nicAddresses[0];
 						if (address.getVersion().equals(IPVersion.IPV4)) {
 							Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_TAG); 
 							ipAddressElement.appendChild(doc.createTextNode(address.getIpAddress()));
 							networkAdapterElement.appendChild(ipAddressElement);
 						}
 						else if (address.getVersion().equals(IPVersion.IPV6)) {
 							Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_V6_TAG); 
 							ipAddressElement.appendChild(doc.createTextNode(address.getIpAddress()));
 							networkAdapterElement.appendChild(ipAddressElement);
 						}
 
 						networkAdapterSettingsElement.appendChild(networkAdapterElement);
 					}
 				}
 
 				networkSettingsElement.appendChild(networkAdapterSettingsElement);
 				//Optional DNS Settings Go Here
 				customiztionElement.appendChild(networkSettingsElement);
 
 				if( withKeypairId == null ) {
 					for( SSHKeypair k : provider.getIdentityServices().getShellKeySupport().list() ) {
 						withKeypairId = k.getProviderKeypairId();
 						break;
 					}
 					if (withKeypairId == null) {
 						throw new InternalException("Can't launch a Linux vm without a ssh keypair. Please generate a keypair before launching from this image.");
 					}
 				}
 				else {
 					SSHKeypair key = provider.getIdentityServices().getShellKeySupport().getKeypair(withKeypairId);
 					if (key == null) { //probably a name instead of an id
 						for( SSHKeypair k : provider.getIdentityServices().getShellKeySupport().list() ) {
 							if (k.getName().equals(withKeypairId)) {
 								withKeypairId = k.getProviderKeypairId();
 								break;
 							}
 						}
 					}
 				}
 
 				Element sshKeyElement = doc.createElement(TerremarkKeypair.SSH_KEY_TAG);
 				sshKeyElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/admin/" + TerremarkKeypair.SSH_KEYS + "/" + withKeypairId);
 				sshKeyElement.setAttribute(Terremark.TYPE, TerremarkKeypair.SSH_KEY_TYPE);
 				customiztionElement.appendChild(sshKeyElement);
 
 				rootElement.appendChild(customiztionElement);
 			}
 
 			Element poweredOnElement = doc.createElement("PoweredOn");
 			poweredOnElement.appendChild(doc.createTextNode("true"));
 			rootElement.appendChild(poweredOnElement);
 
 			Element templateElement = doc.createElement("Template");
 			templateElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + templateHref);
 			templateElement.setAttribute(Terremark.TYPE, Template.TEMPLATE_TYPE);
 			rootElement.appendChild(templateElement);
 
 
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 
 		} catch (ParserConfigurationException e) {
 			if (logger.isDebugEnabled()) {
 				logger.warn("launchFromTemplate(): Error creating a new instance of a DocumentBuilder", e);
 			}
 			else {
 				logger.warn("launchFromTemplate(): Error creating a new instance of a DocumentBuilder");
 			}
 		} catch (TransformerException e) {
 			if (logger.isDebugEnabled()) {
 				logger.warn("launchFromTemplate(): Error transforming the xml", e);
 			}
 			else {
 				logger.warn("launchFromTemplate(): Error transforming the xml");
 			}
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, body);
 		Document responseDoc = method.invoke();
 
 		String vmId = Terremark.hrefToId(responseDoc.getElementsByTagName(VIRTUAL_MACHINE_TAG).item(0).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 
 		String taskHref = Terremark.getTaskHref(responseDoc, CREATE_SERVER_OPERATION);
 		provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 
 		logger.debug("launchFromTemplate(): getting virtual machine " + vmId);
 		server = getVirtualMachine(vmId);	
 
 		if (server.getCurrentState().equals(VmState.STOPPED)) {
 			start(vmId);
 			server = getVirtualMachine(vmId);
 		}
 
 		if (template.getPlatform().equals(Platform.WINDOWS)){
 			server.setRootUser("Administrator");
 			server.setRootPassword(withPassword);
 		}
 		else {
 			server.setRootUser(DEFAULT_ROOT_USER);
 		}
 		logger.trace("exit - launchFromTemplate()");
 		return server;
 	}
 
 	private void addNameAttribute(Element rootElement, String name) {
 		name = validateName(name);
 		rootElement.setAttribute(Terremark.NAME, name);
 	}
 
 	private VirtualMachineProduct parseProductString(String productString) {
 		VirtualMachineProduct product = new VirtualMachineProduct();
 		//product string format cpu:ram
 		String[] productIds = productString.split(":");
 		if (productIds.length == 2 || productIds.length == 3) {
 			product.setCpuCount(Integer.parseInt(productIds[0]));
 			product.setRamSize(new Storage<Megabyte>(Integer.parseInt(productIds[1]), Storage.MEGABYTE));
 			if (productIds.length == 3) {
 				logger.warn("Provided disk size(s) will be ignored. Call alter vm to change disk sizes.");
 			}
 		}
 		else {
 			throw new InternalError("Invalid product id string");
 		}
 		return product;
 	}
 
 	private String getFirstVlan() throws CloudException, InternalException {
 		String inVlanId = null;
 		for( VLAN n : provider.getNetworkServices().getVlanSupport().listVlans() ) {
 			inVlanId = n.getProviderVlanId();
 			break;
 		}
 		return inVlanId;
 	}
 
 	/**
 	 * Provides a list of firewalls protecting the specified server. If firewalls are not supported
 	 * in this cloud, the list will be empty.
 	 * @param vmId the server ID whose firewall list is being sought
 	 * @return the list of firewalls protecting the target server
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public @Nonnull Iterable<String> listFirewalls(@Nonnull String vmId) throws InternalException, CloudException {
 		ArrayList<String> firewallproviderIds = new ArrayList<String>();
 		if (provider != null) {
 			FirewallRule support = provider.getNetworkServices().getFirewallSupport();
 
 			if( support == null ) {
 				return Collections.emptyList();
 			}
 			for (Firewall firewall : support.list()){
 				firewallproviderIds.add(firewall.getProviderFirewallId());
 			}
 		}
 		return firewallproviderIds;
 	}
 
 	/**
 	 * Provides a list of instance types, service offerings, or server sizes (however the underlying cloud
 	 * might describe it) for a particular architecture
 	 * @param architecture the desired architecture size offerings
 	 * @return the list of server sizes available for the specified architecture
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public Iterable<VirtualMachineProduct> listProducts(Architecture architecture) throws InternalException, CloudException {
 		if( products == null ) {
 			ArrayList<VirtualMachineProduct> sizes = new ArrayList<VirtualMachineProduct>();
 
 			for( int cpu : new int[] { 1, 2, 4, 8 } ) {
 				for( int ram : new int[] { 512, 1024, 2048, 4096, 8192, 16384, 32768 } ) {
 					VirtualMachineProduct product = new VirtualMachineProduct();
 					product.setProviderProductId(cpu + ":" + ram);
 					product.setName(cpu + " CPU, " + ram + "MB RAM");
 					product.setDescription(cpu + " CPU, " + ram + "MB RAM");
 					product.setCpuCount(cpu);
 					product.setRamSize(new Storage<Megabyte>(ram, Storage.MEGABYTE));
 					sizes.add(product);
 				}
 			}
 			products = Collections.unmodifiableList(sizes);
 		}
 		return products;
 	}
 
 	/**
 	 * Identifies what architectures are supported in this cloud.
 	 * @return a list of supported architectures
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation calculating supported architectures
 	 * @throws CloudException an error occurred fetching the list of supported architectures from the cloud
 	 */
 	@Override
 	public Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
 		Collection<Architecture> architectures = new ArrayList<Architecture>();
 		architectures.add(Architecture.I32);
 		architectures.add(Architecture.I64);
 		return architectures;
 	}
 
 	/**
 	 * Lists all virtual machines belonging to the account owner currently in the cloud.
 	 * @return all servers belonging to the account owner
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public Iterable<VirtualMachine> listVirtualMachines() throws InternalException, CloudException {
 		logger.trace("enter - listVirtualMachines()");
 		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
 		ProviderContext ctx = provider.getContext();
 		if( ctx == null ) {
 			throw new CloudException("No context was established for this request");
 		}
 		String regionId = ctx.getRegionId();
 		Document environmentDoc = provider.getDataCenterServices().getEnvironmentById(regionId);
 		NodeList vmNodes = environmentDoc.getElementsByTagName(VIRTUAL_MACHINE_TAG);
 		logger.trace("listVirtualMachines(): Found " + vmNodes.getLength() + " VMs in region");
 		for (int i=0; i < vmNodes.getLength(); i++){
 			String vmHref = vmNodes.item(i).getAttributes().item(0).getNodeValue();
 			VirtualMachine vm = getVirtualMachine(Terremark.hrefToId(vmHref));
 			vms.add(vm);
 		}
 		logger.trace("exit - listVirtualMachines()");
 		return vms;
 	}
 
 	/**
 	 * Lists the status for all virtual machines in the current region.
 	 * @return the status for all virtual machines in the current region
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation
 	 * @throws CloudException an error occurred with the cloud provider
 	 */
 	@Override
 	public Iterable<ResourceStatus> listVirtualMachineStatus() throws InternalException, CloudException {
 		logger.trace("enter - listVirtualMachineStatus()");
 		ArrayList<ResourceStatus> vms = new ArrayList<ResourceStatus>();
 		ProviderContext ctx = provider.getContext();
 		if( ctx == null ) {
 			throw new CloudException("No context was established for this request");
 		}
 		String regionId = ctx.getRegionId();
 		Document environmentDoc = provider.getDataCenterServices().getEnvironmentById(regionId);
 		NodeList vmNodes = environmentDoc.getElementsByTagName(VIRTUAL_MACHINE_TAG);
 		logger.trace("listVirtualMachineStatus(): Found " + vmNodes.getLength() + " VMs in region");
 		for (int i=0; i < vmNodes.getLength(); i++){
 			String vmId = Terremark.hrefToId(vmNodes.item(i).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 			String status = vmNodes.item(i).getFirstChild().getTextContent();
 			boolean poweredOn = vmNodes.item(i).getLastChild().getPreviousSibling().getTextContent().equals("true");
 			VmState state = null;
 			if (status != null) {
 				if (status.equalsIgnoreCase("Deployed") && poweredOn){
 					state = VmState.RUNNING;
 				}
 				else if (status.equalsIgnoreCase("Deployed") && !poweredOn){
 					state = VmState.STOPPED;
 				}
 				else if (status.equalsIgnoreCase("NotDeployed")){
 					state = VmState.TERMINATED;
 				}
 				else if (status.equalsIgnoreCase("Orphaned")){
 					state = VmState.TERMINATED;
 				}
 				else if (status.equalsIgnoreCase("TaskInProgress")){
 					state = VmState.PENDING;
 				}
 				else if (status.equalsIgnoreCase("CopyInProgress")){
 					state = VmState.PENDING;
 				}
 				else {
 					state = VmState.PENDING;
 				}
 				logger.debug("VM Status = " + status + " & PoweredOn = " + poweredOn + ", Setting current state to: " + state);
 			}
 			ResourceStatus vm = new ResourceStatus(vmId, state);
 			vms.add(vm);
 		}
 		logger.trace("exit - listVirtualMachineStatus()");
 		return vms;
 	}
 
 	/**
 	 * Power off stops the virtual machine without waiting for processes to complete.
 	 * @param vmId the provider ID for the server to power off
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	private void powerOff(@Nonnull String vmId) throws InternalException, CloudException {
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId + "/" + Terremark.ACTION + "/" + POWER_OFF;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, POWER_OFF_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	/**
 	 * Executes a virtual machine reboot for the target virtual machine.
 	 * @param vmId the provider ID for the server to reboot
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public void reboot(@Nonnull String vmId) throws CloudException, InternalException {
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId + "/" + Terremark.ACTION + "/" + REBOOT;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, REBOOT_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	/**
 	 * Shutdown requests the virtual machine to end all processes and turn itself off when all processes complete.
 	 * @param vmId the provider ID for the server to shutdown
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	private void shutdown(@Nonnull String vmId) throws InternalException, CloudException {
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId + "/" + Terremark.ACTION + "/" + SHUTDOWN;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, SHUTDOWN_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	/**
 	 * Starts up a virtual machine that was previously stopped (or a VM that is created in a {@link VmState#STOPPED} state).
 	 * @param vmId the virtual machine to boot up
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 * @throws OperationNotSupportedException starting/stopping is not supported for this virtual machine
 	 * @see #stop(String)
 	 */
 	@Override
 	public void start(String vmId) throws InternalException, CloudException {
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId + "/" + Terremark.ACTION + "/" + POWER_ON;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 		Document doc = method.invoke();
 		String taskHref = Terremark.getTaskHref(doc, POWER_ON_OPERATION);
 		provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 	}
 
 	/**
 	 * Shuts down a virtual machine with the capacity to boot it back up at a later time. The contents of volumes
 	 * associated with this virtual machine are preserved, but the memory is not.
 	 * @param vmId the virtual machine to be shut down
 	 * @param force whether or not to force a shutdown (kill the power)
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 * @throws OperationNotSupportedException starting/stopping is not supported for this virtual machine
 	 * @see #start(String)
 	 */
 	@Override
 	public void stop(String vmId, boolean force) throws InternalException, CloudException {
 		if (force) {
 			powerOff(vmId);
 		}
 		else {
 			shutdown(vmId);
 		}
 		VmState status = getVirtualMachine(vmId).getCurrentState();
 		if (!status.equals(VmState.STOPPED)){
 			throw new CloudException("Failed to stop server");
 		}
 	}
 
 	/**
 	 * Indicates whether the ability to start/stop a virtual machine is supported for the specified VM.
 	 * @param vm the virtual machine to verify
 	 * @return true if start/stop operations are supported for this virtual machine
 	 * @see #start(String)
 	 * @see #stop(String)
 	 * @see VmState#RUNNING
 	 * @see VmState#STOPPING
 	 * @see VmState#STOPPED
 	 */
 	@Override
 	public boolean supportsStartStop(VirtualMachine vm) {
 		return true;
 	}
 
 	/**
 	 * TERMINATES AND DESTROYS the specified virtual machine. If it is running, it will be stopped. Once it is
 	 * stopped, all of its data will be destroyed and it will no longer be usable. This is a very 
 	 * dangerous operation, especially in clouds with persistent servers.
 	 * @param vmId the provider ID of the server to be destroyed
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	@Override
 	public void terminate(@Nonnull String vmId) throws InternalException, CloudException {
 		VmState status = getVirtualMachine(vmId).getCurrentState();
 		if (!status.equals(VmState.STOPPED)){
 			powerOff(vmId);
 			status = getVirtualMachine(vmId).getCurrentState();
 		}
 		if (!status.equals(VmState.STOPPED)){
 			throw new CloudException("Failed to pause server");
 		}
 		String url = "/" + VIRTUAL_MACHINES + "/" + vmId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.DELETE, url, null, "");
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, DELETE_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	/**
 	 * Creates a VirtualMachine object from a virtual machine xml node
 	 * @param vmNode the xml node representing a virtual machine, identified by the tag VirtualMachine.
 	 * @return a VirtualMachine object based on the context and the xml node provided
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 * @throws CloudException an error occurred within the cloud provider
 	 */
 	private VirtualMachine toVirtualMachine(Node vmNode) throws CloudException, InternalException {
 		logger.trace("enter - toVirtualMachine");
 		if( vmNode == null ) {
 			logger.warn("vmNode is null");
 			return null;
 		}
 		VirtualMachine vm = new VirtualMachine();
 
 		ProviderContext ctx = provider.getContext();
 		if (ctx == null){
 			logger.warn("Context is null");
 			return null;
 		}
 		NamedNodeMap attributes = vmNode.getAttributes();
 		for (int i=0; i < attributes.getLength(); i++) {
 			Node node = attributes.item(i);
 			if (node.getNodeName().equals(Terremark.HREF)){
 				vm.setProviderVirtualMachineId(Terremark.hrefToId(node.getNodeValue()));
 			}
 			else if (node.getNodeName().equals(Terremark.NAME)){
 				vm.setName(node.getNodeValue());
 				logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Name = " + vm.getName());
 			}
 		}
 
 		vm.setProviderOwnerId(provider.getContext().getAccountNumber());
 		logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Owner = " + vm.getProviderOwnerId());
 		vm.setProviderRegionId(ctx.getRegionId());
 		logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Region = " + vm.getProviderRegionId());
 		vm.setPersistent(true);
 		vm.setRootPassword(null);
 
 		NodeList vmChildNodes = vmNode.getChildNodes();
 		String imageId = null;
 		String osName = null;
 		String status = null;
 		boolean poweredOn = false;
 		long created = System.currentTimeMillis();
 		long deployed = -1L;
 		long paused = -1L;
 		long powerOff = -1L;
 		long shutdown = -1L;
 		long terminated = -1L;
 		for (int i=0; i < vmChildNodes.getLength(); i++){
 			Node childNode = vmChildNodes.item(i);
 			if (childNode.getNodeName().equalsIgnoreCase("Links")){
 				NodeList linkNodes = childNode.getChildNodes();
 				for (int j=0; j < linkNodes.getLength(); j++) {
 					NamedNodeMap linkAttrs = linkNodes.item(j).getAttributes();
 					for (int k=0; k < linkAttrs.getLength(); k++) {
 						Node linkAttr = linkAttrs.item(k);
 						if (linkAttr.getNodeValue().contains(EnvironmentsAndComputePools.COMPUTE_POOLS.toLowerCase())){
 							vm.setProviderDataCenterId(Terremark.hrefToId(linkAttr.getNodeValue()));
 							logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " DC = " + vm.getProviderDataCenterId());
 						}
 					}
 				}
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("Tasks")) {
 				NodeList taskNodes = childNode.getChildNodes();
 				for (int j=0; j<taskNodes.getLength(); j++) {
 					Node task = taskNodes.item(j);
 					String operation = task.getFirstChild().getTextContent();
 					NodeList taskDetails = task.getChildNodes();
 					if (operation.equals(CREATE_SERVER_OPERATION)) {
 						for (int k=0; k<taskDetails.getLength(); k++) {
 							if (taskDetails.item(k).getNodeName().equals("StartTime")) {
 								String createdDateString = taskDetails.item(k).getTextContent();
 								Date createdDate = Terremark.parseIsoDate(createdDateString);
 								if (createdDate != null) {
 									created = createdDate.getTime();
 								}
 							}
 						}
 					}
 					else if (operation.equals(POWER_ON_OPERATION)) {
 						for (int k=0; k<taskDetails.getLength(); k++) {
 							if (taskDetails.item(k).getNodeName().equals("StartTime")) {
 								String deployedDateString = taskDetails.item(k).getTextContent();
 								Date deployedDate = Terremark.parseIsoDate(deployedDateString);
 								if (deployedDate != null) {
 									deployed = deployedDate.getTime();
 								}
 							}
 						}
 					}
 					else if (operation.equals(POWER_OFF_OPERATION)) {
 						for (int k=0; k<taskDetails.getLength(); k++) {
 							if (taskDetails.item(k).getNodeName().equals("StartTime")) {
 								String powerOffDateString = taskDetails.item(k).getTextContent();
 								Date powerOffDate = Terremark.parseIsoDate(powerOffDateString);
 								if (powerOffDate != null) {
 									powerOff = powerOffDate.getTime();
 								}
 							}
 						}
 					}
 					else if (operation.equals(SHUTDOWN_OPERATION)) {
 						for (int k=0; k<taskDetails.getLength(); k++) {
 							if (taskDetails.item(k).getNodeName().equals("StartTime")) {
 								String shutdownDateString = taskDetails.item(k).getTextContent();
 								Date shutdownDate = Terremark.parseIsoDate(shutdownDateString);
 								if (shutdownDate != null) {
 									shutdown = shutdownDate.getTime();
 								}
 							}
 						}
 					}
 					else if (operation.equals(DELETE_OPERATION)) {
 						for (int k=0; k<taskDetails.getLength(); k++) {
 							if (taskDetails.item(k).getNodeName().equals("StartTime")) {
 								String deleteDateString = taskDetails.item(k).getTextContent();
 								Date deleteDate = Terremark.parseIsoDate(deleteDateString);
 								if (deleteDate != null) {
 									terminated = deleteDate.getTime();
 								}
 							}
 						}
 					}
 				}
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("Description")){
 				vm.setDescription(childNode.getTextContent());
 				logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Description = " + vm.getDescription());
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("Tags")){
 				Map<String,String> properties = new HashMap<String,String>();
 				//When we launch servers we save "/templates/{template identifier}/computePools/{compute pool identifier}" in a tag, so we can identify the vm's machine image
 				NodeList tags = childNode.getChildNodes();
 				for (int j=0; j < tags.getLength(); j++){
 					String tagValue = tags.item(j).getTextContent();
 					if (Terremark.getTemplateIdFromHref(tagValue) != null){
 						imageId = Terremark.getTemplateIdFromHref(tagValue);
 						vm.setProviderMachineImageId(imageId);
 						logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Machine Image ID = " + vm.getProviderMachineImageId());
 					}
 					else if (Terremark.getCatalogIdFromHref(tagValue) != null){
 						imageId = Terremark.getCatalogIdFromHref(tagValue);
 						vm.setProviderMachineImageId(imageId);
 						logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Machine Image ID = " + vm.getProviderMachineImageId());
 					}
 					else {
 						String[] tag = tagValue.split("=");
 						if (tag.length == 2) properties.put(tag[0], tag[1]);
 					}
 				}
 				vm.setTags(properties);
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("Status")){
 				status = childNode.getTextContent();
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("PoweredOn")){
 				if (childNode.getTextContent().equalsIgnoreCase("true")){
 					poweredOn = true;
 				}
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("HardwareConfiguration")){
 				String processorCount = "0";
 				int mbRam = 0;
 				String diskSizes = "";
 				NodeList hcNodes = childNode.getChildNodes();
 				for (int j=0; j < hcNodes.getLength(); j++) {
 					Node hcNode = hcNodes.item(j);
 					if (hcNode.getNodeName().equalsIgnoreCase("ProcessorCount")){
 						processorCount = hcNode.getTextContent();
 					}
 					else if (hcNode.getNodeName().equalsIgnoreCase("Memory")){
 						String memUnit = hcNode.getChildNodes().item(0).getTextContent();
 						int memValue = Integer.parseInt(hcNode.getChildNodes().item(1).getTextContent());
 						if (memUnit.equalsIgnoreCase("MB")){ //API Doc says memory uses MB
 							mbRam = memValue;
 						}
 						else if (memUnit.equalsIgnoreCase("GB")){
 							mbRam = memValue * 1024;
 						}
 					}
 					else if (hcNode.getNodeName().equalsIgnoreCase("Disks")){
 						NodeList diskNodes = hcNode.getChildNodes();
 						for (int k=0; k<diskNodes.getLength(); k++) {
 							NodeList diskProperties = diskNodes.item(k).getChildNodes();
 							for (int l=0; l < diskProperties.getLength(); l++){
 								if (diskProperties.item(l).getNodeName().equalsIgnoreCase("Size")){
 									String diskUnit = diskProperties.item(l).getFirstChild().getTextContent();
 									String diskSize = diskProperties.item(l).getFirstChild().getNextSibling().getTextContent();
 									int gbDisk = 0;
 									if (diskUnit.equalsIgnoreCase("GB")){ // API Doc says disks use GB
 										gbDisk = Integer.parseInt(diskSize);
 									}
 									else if (diskUnit.equalsIgnoreCase("TB")){
 										gbDisk = (Integer.parseInt(diskSize) * 1024);
 									}
 									else if (diskUnit.equalsIgnoreCase("MB")){
 										gbDisk = (Integer.parseInt(diskSize) / 1024);
 									}
 									if (k == 0) {
 										vm.getTags().put("rootDiskSize", String.valueOf(gbDisk));
 										diskSizes += "[" + gbDisk;
 									}
 									else {
 										diskSizes += "," + gbDisk;
 									}
 								}
 							}
 						}
 						diskSizes += "]";
 					}
 					else if (hcNode.getNodeName().equalsIgnoreCase("Nics")){
 						NodeList nicNodes = hcNode.getChildNodes();
 						vm.setTag("nic-count", String.valueOf(nicNodes.getLength()));
 						for (int l=0; l < nicNodes.getLength(); l++){
 							NodeList nicProperties = nicNodes.item(l).getChildNodes();
 							String key = "nic-" + l;
 							String nicNumber = "";
 							String nicNetworkHref = "";
 							String nicNetworkName = "";
 							String nicNetworkType = "";
 							for (int m=0; m < nicProperties.getLength(); m++){
 								Node nicProperty = nicProperties.item(m);
 								if (nicProperty.getNodeName().equals("UnitNumber")) {
 									nicNumber = nicProperty.getTextContent();
 								}
 								else if (nicProperty.getNodeName().equals("Network")) {
 									nicNetworkHref = nicProperty.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 									nicNetworkName = nicProperty.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 									nicNetworkType = nicProperty.getAttributes().getNamedItem(Terremark.TYPE).getNodeValue();
 								}
 
 							}
 							String value = nicNumber + ":" + nicNetworkHref + ":" + nicNetworkName + ":" + nicNetworkType;
 							vm.setTag(key, value);
 						}
 					}
 				}
 				String str = processorCount + ":" + mbRam + ":" + diskSizes;
 				vm.setProductId(str);
 				logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " Product = " + vm.getProductId());
 			}
 			else if (childNode.getNodeName().equalsIgnoreCase("IpAddresses")){
 				Collection<String> addresses = new ArrayList<String>();
 				NodeList ipAddressesNodes = childNode.getChildNodes();
 				for (int j=0; j < ipAddressesNodes.getLength(); j++){
 					if (ipAddressesNodes.item(j).getNodeName().equals("AssignedIpAddresses")){
 						NodeList assignedAddressesNodes = ipAddressesNodes.item(j).getChildNodes();
 						for (int k=0; k < assignedAddressesNodes.getLength(); k++){
 							if (assignedAddressesNodes.item(k).getNodeName().equals("Networks")){
 								NodeList networksNodes = assignedAddressesNodes.item(k).getChildNodes();
 								for (int l=0; l < networksNodes.getLength(); l++){
 									Node networkNode = networksNodes.item(l);
 									NamedNodeMap networkAttrs = networkNode.getAttributes();
 									if (l == 0) {
 										String networkId = Terremark.hrefToNetworkId(networkAttrs.getNamedItem(Terremark.HREF).getNodeValue());
 										vm.setProviderVlanId(networkId);
 									}
 									NodeList ipAddressNodes = networkNode.getFirstChild().getChildNodes();
 									for (int n=0; n < ipAddressNodes.getLength(); n++){
 										String address = ipAddressNodes.item(n).getTextContent();
 										addresses.add(address);
 									}
 								}
 								break;
 							}
 						}
 						break;
 					}
 				}
 				if (addresses.size() > 0){
 					RawAddress[] privateIps = new RawAddress[addresses.size()];
 					int o = 0;
 
 					for( String addr : addresses ) {
 						if( o == 0 ) {
 							vm.setPrivateDnsAddress(addr); //Set to the first IP address id
 							logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " PrivateDnsAddress = " + vm.getPrivateDnsAddress());
 						}
 						privateIps[o++] = new RawAddress(addr);
 					}
 
 					vm.setPrivateAddresses(privateIps);
 					logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " PrivateIpAddress = " + Arrays.toString(vm.getPrivateAddresses()));
 				}
 				else {
 					vm.setPrivateAddresses(new RawAddress[0]);
 				}
 				vm.setPublicDnsAddress(null);
 				vm.setPublicAddresses(new RawAddress[0]);
 				vm.setProviderAssignedIpAddressId(null);
 			}	
 			else if (childNode.getNodeName().equalsIgnoreCase("OperatingSystem")){
 				NamedNodeMap osAttrs= childNode.getAttributes();
 				for(int j=0; j < osAttrs.getLength(); j++){
 					Node osAtt = osAttrs.item(j);
 					if (osAtt.getNodeName().equalsIgnoreCase(Terremark.NAME)){
 						osName=osAtt.getNodeValue();
 						vm.setPlatform(Platform.guess(osName));
 						logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " OS = " + osName + " Platform = " + vm.getPlatform());
 						if( osName == null || osName.indexOf("32 bit") != -1 || osName.indexOf("32-bit") != -1 ) {
 							vm.setArchitecture(Architecture.I32);            
 						}
 						else if( osName.indexOf("64 bit") != -1 || osName.indexOf("64-bit") != -1 ) {
 							vm.setArchitecture(Architecture.I64);
 						}
 						else {
 							vm.setArchitecture(Architecture.I64);
 						}
 						logger.debug("toVirtualMachine(): ID = " + vm.getProviderVirtualMachineId() + " OS = " + osName + " Architecture = " + vm.getArchitecture());
 					}
 				}
 			}
 		}
 		if (status != null) {
 			VmState state;
 			if (status.equalsIgnoreCase("Deployed") && poweredOn){
 				state = VmState.RUNNING;
 				vm.setPausable(true);
 				vm.setRebootable(true);
 			}
 			else if (status.equalsIgnoreCase("Deployed") && !poweredOn){
 				state = VmState.STOPPED;
 				vm.setImagable(true);
 				vm.setClonable(true);
 			}
 			else if (status.equalsIgnoreCase("NotDeployed")){
 				state = VmState.TERMINATED;
 			}
 			else if (status.equalsIgnoreCase("Orphaned")){
 				state = VmState.TERMINATED;
 			}
 			else if (status.equalsIgnoreCase("TaskInProgress")){
 				state = VmState.PENDING;
 			}
 			else if (status.equalsIgnoreCase("CopyInProgress")){
 				state = VmState.PENDING;
 			}
 			else {
 				state = VmState.PENDING;
 			}
 			logger.debug("VM Status = " + status + " & PoweredOn = " + poweredOn + ", Setting current state to: " + state);
 			vm.setCurrentState(state);
 		}
 		if (vm.getProviderMachineImageId() == null && osName != null){
 			logger.debug("toVirtualMachine(): Could not identify the template id, guessing based on OS name");
 			vm.setProviderMachineImageId(guessImageId(osName));
 		}
 
 		if (powerOff > 0) {
 			if (shutdown > 0) {
 				if (powerOff > shutdown) {
 					paused = powerOff;
 				}
 				else {
 					paused = shutdown;
 				}
 			}
 			else {
 				paused = powerOff;
 			}
 		}
 		else if (shutdown > 0) {
 			paused = shutdown;
 		}
 		if (deployed == -1L) {
 			deployed = created;
 		}
 		vm.setLastPauseTimestamp(paused);
 		vm.setLastBootTimestamp(deployed);
 		vm.setCreationTimestamp(created);
 		vm.setTerminationTimestamp(terminated);
 
 		logger.trace("exit - toVirtualMachine");
 		return vm;
 	}
 
 	/**
 	 * Updates meta-data for a virtual machine with the new values. It will not overwrite any value that currently
 	 * exists unless it appears in the tags you submit.
 	 * @param vmId the virtual machine to update
 	 * @param tags the meta-data tags to set
 	 * @throws CloudException an error occurred within the cloud provider
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 */
 	@Override
 	public void updateTags(String vmId, Tag... tags) throws CloudException, InternalException {
 		VirtualMachine vm = getVirtualMachine(vmId);
 		Map<String, String> tagsToAdd = vm.getTags();
 		for (int i=0; i<tags.length; i++) {
 			Tag tag = tags[i];
 			tagsToAdd.put(tag.getKey(), tag.getValue());
 		}
 
 		String url = VIRTUAL_MACHINES + "/" + vmId;
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("VirtualMachine");
 			rootElement.setAttribute(Terremark.NAME, vm.getName());
 			doc.appendChild(rootElement);
 
 			Element tagsElement = doc.createElement("Tags");
 
 			for(String key: tagsToAdd.keySet()){
 				if (!key.contains("nic-")) { // skip these because they are for internal communication only
 					Element tagElement = doc.createElement("Tag");
 					String tagValue = tagsToAdd.get(key).toString();
 					tagElement.appendChild(doc.createTextNode(key + "=" + tagValue));
 					tagsElement.appendChild(tagElement);	
 				}
 			}
 			rootElement.appendChild(tagsElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.PUT, url, null, body);
 		method.invoke();
 
 	}
 
 }
