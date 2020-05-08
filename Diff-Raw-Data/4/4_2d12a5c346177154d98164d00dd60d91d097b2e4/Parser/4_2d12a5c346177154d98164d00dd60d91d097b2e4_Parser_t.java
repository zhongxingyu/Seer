 /*
  * Claudia Project
  * http://claudia.morfeo-project.org
  *
  * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
  * S.A.Unipersonal (Telefonica I+D)
  *
  * See CREDITS file for info about members and contributors.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the Affero GNU General Public License (AGPL) as 
  * published by the Free Software Foundation; either version 3 of the License, 
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the Affero GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * If you want to use this software an plan to distribute a
  * proprietary application in any way, and you are not licensing and
  * distributing your source code under AGPL, you probably need to
  * purchase a commercial license of the product. Please contact
  * claudia-support@lists.morfeo-project.org for more information.
  */
 
 package com.telefonica.claudia.slm.maniParser;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.dmtf.schemas.ovf.envelope._1.ContentType;
 import org.dmtf.schemas.ovf.envelope._1.DiskSectionType;
 import org.dmtf.schemas.ovf.envelope._1.EnvelopeType;
 import org.dmtf.schemas.ovf.envelope._1.FileType;
 import org.dmtf.schemas.ovf.envelope._1.MsgType;
 import org.dmtf.schemas.ovf.envelope._1.NetworkSectionType;
 import org.dmtf.schemas.ovf.envelope._1.ObjectFactory;
 import org.dmtf.schemas.ovf.envelope._1.ProductSectionType;
 import org.dmtf.schemas.ovf.envelope._1.RASDType;
 import org.dmtf.schemas.ovf.envelope._1.ReferencesType;
 import org.dmtf.schemas.ovf.envelope._1.StartupSectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualDiskDescType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualHardwareSectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemCollectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemType;
 import org.dmtf.schemas.ovf.envelope._1.ProductSectionType.Property;
 import org.dmtf.schemas.ovf.envelope._1.StartupSectionType.Item;
 
 import com.abiquo.ovf.OVFEnvelopeUtils;
 import com.abiquo.ovf.OVFEnvironmentUtils;
 import com.abiquo.ovf.OVFReferenceUtils;
 import com.abiquo.ovf.exceptions.DNSServerNotFoundException;
 import com.abiquo.ovf.exceptions.EmptyEnvelopeException;
 import com.abiquo.ovf.exceptions.GatewayNotFoundException;
 import com.abiquo.ovf.exceptions.IPNotFoundException;
 import com.abiquo.ovf.exceptions.IdAlreadyExistsException;
 import com.abiquo.ovf.exceptions.InvalidSectionException;
 import com.abiquo.ovf.exceptions.NetmaskNotFoundException;
 import com.abiquo.ovf.exceptions.NotEnoughIPsInPoolException;
 import com.abiquo.ovf.exceptions.PoolNameNotFoundException;
 import com.abiquo.ovf.exceptions.PrecedentTierEntryPointNotFoundException;
 import com.abiquo.ovf.exceptions.SectionNotPresentException;
 import com.abiquo.ovf.exceptions.XMLException;
 import com.abiquo.ovf.section.OVFDiskUtils;
 import com.abiquo.ovf.section.OVFNetworkUtils;
 import com.abiquo.ovf.section.OVFProductUtils;
 import com.abiquo.ovf.xml.OVFSerializer;
 import com.telefonica.claudia.ovf.AffinityScopeType;
 import com.telefonica.claudia.ovf.AffinitySectionType;
 import com.telefonica.claudia.ovf.AffinityType;
 import com.telefonica.claudia.ovf.AntiAffinityType;
 import com.telefonica.claudia.ovf.AreaType;
 import com.telefonica.claudia.ovf.AvailabilitySectionType;
 import com.telefonica.claudia.ovf.CountryType;
 import com.telefonica.claudia.ovf.DeploymentSectionType;
 import com.telefonica.claudia.ovf.ElasticArraySectionType;
 import com.telefonica.claudia.ovf.KPISectionType;
 import com.telefonica.claudia.ovf.PositionType;
 import com.telefonica.claudia.ovf.RestrictionType;
 import com.telefonica.claudia.ovf.RuleType;
 import com.telefonica.claudia.ovf.TimezoneType;
 import com.telefonica.claudia.ovf.WindowUnitType;
 import com.telefonica.claudia.ovf.KPISectionType.KPI;
 import com.telefonica.claudia.slm.common.SMConfiguration;
 import com.telefonica.claudia.slm.deployment.Customer;
 import com.telefonica.claudia.slm.deployment.GeographicDomain;
 import com.telefonica.claudia.slm.deployment.Rule;
 import com.telefonica.claudia.slm.deployment.ServiceApplication;
 import com.telefonica.claudia.slm.deployment.ServiceKPI;
 import com.telefonica.claudia.slm.deployment.VEE;
 import com.telefonica.claudia.slm.deployment.hwItems.CPUConf;
 import com.telefonica.claudia.slm.deployment.hwItems.DiskConf;
 import com.telefonica.claudia.slm.deployment.hwItems.MemoryConf;
 import com.telefonica.claudia.slm.deployment.hwItems.NICConf;
 import com.telefonica.claudia.slm.deployment.hwItems.Network;
 import com.telefonica.claudia.slm.lifecyclemanager.DeploymentException;
 
 
 public class Parser {
 
 	/* The Service Application */
 	private ServiceApplication sa;
 
 	private EnvelopeType envelope = null;
 
 	/* Class logger */
 	private static Logger logger = Logger.getLogger(Parser.class);
 
 	static {
 		Logger.getLogger("com.telefonica.claudia.slm.maniParser").setLevel(Level.DEBUG);
 		Logger.getLogger("com.telefonica.claudia.slm.maniParser").addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t] %c{2}: %m%n"), "System.out"));
 	}
 
 	/**
 	 * Class constructor.
 	 * 
 	 * @param xml
 	 *            file location
 	 */
 
 	public Parser(String filename, Customer cus, String serviceName) throws ManiParserException {
 
 		try {
 			/* Build the XMLBeans with the input file */
 			File xmlFile = new File(filename);
 			OVFSerializer ovfSerializer = OVFSerializer.getInstance();
 			ovfSerializer.setValidateXML(false);
 			envelope = ovfSerializer.readXMLEnvelope(new FileInputStream(xmlFile));
 
 			sa = new ServiceApplication(serviceName, cus);
 			sa.setXmlFile(filename);
 
 		} catch (XMLException e) {
 			logger.error(e.getMessage());
 			throw new ManiParserException(e.getMessage());
 		} catch (IOException e) {
 			logger.error(e.getMessage());
 			throw new ManiParserException(e.getMessage());
 		} catch (JAXBException e) {
 			logger.error(e.getMessage());
 			throw new ManiParserException(e.getMessage());
 		}
 	}
 
 
 	public boolean parse() throws ManiParserException {
 
 		// Build networks
 		buildNetworks(sa);
 
 		// Build the VEE vector
 		buildVEEVector(sa);
 
 		// Build the KPI vector
 		buildKPIVector(sa);
 
 		// Build the rule vector
 		buildRuleVector(sa);
 
 		return true;
 	}
 
 	private void buildNetworks (ServiceApplication sa) {
 
 		/* Parse the Network Section in the SManif in order to create the Network objects
 		 * associated to the ServiceApplication */
 		NetworkSectionType ns = null;
 		try {
 			ns = OVFEnvelopeUtils.getSection(envelope, NetworkSectionType.class);
 			List<NetworkSectionType.Network> networks = ns.getNetwork();
 			for (Iterator<NetworkSectionType.Network> iteratorN = networks.iterator(); iteratorN.hasNext();) {
 				NetworkSectionType.Network netEl = (NetworkSectionType.Network) iteratorN.next();
 				Network net = new Network(netEl.getName(),sa);
 
 				QName publicAtt = new QName("http://schemas.telefonica.com/claudia/ovf","public");
 				Map<QName, String> attributes = netEl.getOtherAttributes();
 
 				net.setPrivateNet(!Boolean.parseBoolean(attributes.get(publicAtt)));
 				sa.registerNetwork(net);
 
 				logger.debug("network: " + net.getName() + ", private: " + net.getPrivateNet());
 
 			}
 		} catch (SectionNotPresentException e) {					
 			logger.error(e);
 		} catch (InvalidSectionException e) {					
 			logger.error(e);
 		}	
 	}
 
 	public HashMap<String,Integer> getNumberOfIpsPerNetwork(String vsId)  {
 		VirtualSystemCollectionType topVsc;
 		try {
 			topVsc = (VirtualSystemCollectionType) OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 			VirtualSystemType vs = (VirtualSystemType) OVFEnvelopeUtils.getContentTypeByString(topVsc, vsId);
 			return OVFEnvelopeUtils.getRequiredIPByNetwork(envelope, vs);
 
 		} catch (EmptyEnvelopeException e) {
 			logger.warn("Empty envelope detected, service deployment may not be completed.");
 		}
 
 		return new HashMap<String, Integer>();
 	}
 	
 	public String getStaticIpProperty(String vsId)  {
 		VirtualSystemCollectionType topVsc;
 		try {
 			topVsc = (VirtualSystemCollectionType) OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 			VirtualSystemType vs = (VirtualSystemType) OVFEnvelopeUtils.getContentTypeByString(topVsc, vsId);
 			
 			//return OVFEnvelopeUtils.getRequiredIPByNetwork(envelope, vs);
 			
 			String staticip = getPropertyFromVirtualSystem(vs, "STATIC_IP");
 			return staticip;
 
 		} catch (EmptyEnvelopeException e) {
 			logger.warn("Empty envelope detected, service deployment may not be completed.");
 		}
 
 		return null;
 	}
 	
 
 	public String generateEnvironments(String vsId, int contInstanceNumber, 
 			HashMap<String,ArrayList<String>> ips,
 			HashMap<String,String> netmask,
 			HashMap<String,String> dnsServers,
 			HashMap<String,String> gateways,
 			HashMap<String, HashMap<String, String>> eps) throws DeploymentException
 			{
 		ContentType entityInstance = null;
 		try {
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 		} catch (EmptyEnvelopeException e) {
 			e.printStackTrace();
 		}
 		if (entityInstance instanceof VirtualSystemType){
 			// TODO
 			return null;
 		} else if (entityInstance instanceof VirtualSystemCollectionType)  {
 
 			//VirtualSystemCollectionType virtualSystemCollection = (VirtualSystemCollectionType) entityInstance;
 			//List<ProductSectionType> listProductSection = OVFEnvelopeUtils.getProductSections(virtualSystemCollection);
 
 			/* Accordingly to the OVF Environment document production rules in DSP0243 1.0.0 lines 
 			 * 1322-1325, all the VirtualSystems in a given VirtualSystemCollection get the 
 			 * Properties defined in ProductSections placed *at parent* VirtualSystemCollection level: 
 			 * "The PropertySection contains the key/value pairs defined for all the properties specified 
 			 * in the OVF descriptor for the current virtual machine, as well as properties specified 
 			 * for the immediate parent VirtualSystemCollection, if one exists".
 			 */
 
 			VirtualSystemCollectionType topVsc;
 			try {
 				topVsc = (VirtualSystemCollectionType)OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 				VirtualSystemType vs = (VirtualSystemType) OVFEnvelopeUtils.getContentTypeByString(topVsc, vsId);
 
 				if (vs == null) 
 					throw new DeploymentException ("VirtualSystem " + vsId + " can not be found in SManif", vsId);
 
 				/* Get the number of IPs */
 				HashMap<String,Integer> ipNumbers = OVFEnvelopeUtils.getRequiredIPByNetwork(envelope, vs);
 				for ( Iterator<String> i = ipNumbers.keySet().iterator(); i.hasNext() ; ) {
 					String net = i.next();
 					logger.debug("net '" + net + "' needs " + ipNumbers.get(net).toString() + " IP addresses in VEE type '" + vsId + "'");
 				}
 
 				try {
 					ByteArrayOutputStream output = new ByteArrayOutputStream();
 					OVFEnvironmentUtils.createOVFEnvironment(topVsc, 
 							vs,
 							contInstanceNumber,
 							SMConfiguration.getInstance().getDomainName(),
 							sa.getFQN().toString(),
 							SMConfiguration.getInstance().getMonitoringAddress(), 
 							ips,
 							netmask,
 							dnsServers,
 							gateways,
 							eps,
 							output,
 							SMConfiguration.getInstance().isOvfEnvEntityGen());
 
 					logger.debug("OVF Environment file for VEE [" + vsId + "] " + output.toString());
 					return output.toString();
 
 					//					ByteArrayOutputStream output = new ByteArrayOutputStream();
 					//					OVFEnvelopeUtils.inEnvolopeMacroReplacement(envelope, 
 					//							vs,
 					//							contInstanceNumber,
 					//							SMConfiguration.getInstance().getDomainName(),
 					//							sa.getFQN().toString(),
 					//							SMConfiguration.getInstance().getMonitoringAddress(), 
 					//							ips,
 					//							netmask,
 					//							dnsServers,
 					//							gateways,
 					//							eps,
 					//							output
 					//					);
 					//					
 					//					logger.debug("OVF Envelope file for VEE [" + vsId + "] " + output.toString());
 					//					return output.toString();
 
 				} catch (IPNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (DNSServerNotFoundException e) {
 					// TODO Auto-generated catch block				
 					e.printStackTrace();
 				} catch (NetmaskNotFoundException e) {
 					// TODO Auto-generated catch block				
 					e.printStackTrace();
 				} catch (GatewayNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (PrecedentTierEntryPointNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NotEnoughIPsInPoolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (PoolNameNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} catch (EmptyEnvelopeException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 		}
 
 		return null;
 
 			}
 
 	/**
 	 * builds the VEE vector
 	 */
 	private void buildVEEVector(ServiceApplication sa) throws ManiParserException {
 
 		/*
 		 * Get the <VirtualSystemCollection> and process each child
 		 * <VirtualSystem>
 		 */
 
 		// First of all, get the OVF Documents of its child VS to add them to the
 		// VEEs
 		HashMap<String, String> ovfDocuments=null;
 		try {
 			ovfDocuments = splitOvf();
 		} catch (Exception e1) {
 			logger.error("Could not split OVF due to the following error: " + e1.getMessage());
 			throw new ManiParserException("Could not split OVF due to the following error: " + e1.getMessage());
 		}
 
 		ContentType entityInstance = null;
 		try {
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 		} catch (EmptyEnvelopeException e) {
 			logger.error(e);
 		}
 		if (entityInstance instanceof VirtualSystemType) {
 
 			//TODO
 
 		} else if (entityInstance instanceof VirtualSystemCollectionType) {
 			VirtualSystemCollectionType virtualSystemCollectionType = (VirtualSystemCollectionType) entityInstance;
 
 			for (VirtualSystemType vs : OVFEnvelopeUtils.getVirtualSystems(virtualSystemCollectionType)) {
 
 				VirtualHardwareSectionType vh = null;
 				try {
 					vh = OVFEnvelopeUtils.getSection(vs, VirtualHardwareSectionType.class);
 				} catch (SectionNotPresentException e) {					
 					logger.error(e);
 				} catch (InvalidSectionException e) {					
 					logger.error(e);
 				}
 
 				VEE vee = new VEE(vs.getId(), sa);
 				logger.debug("creating VEE name: " + vs.getId());
 
 				vee.setOvfRepresentation(ovfDocuments.get(vs.getId()));
 
 				/* Elasticity bounds and default instances value */
 				/*
 				 * FIXME: not able to access min, max or initial throguh
 				 * XMLBeans methods in vs. By the moment, using the DOM model
 				 * (through getDomNode). Maybe the rsrvr:min, rsrvr:max,
 				 * rsrvr:init has not been defined in reservoir_manifest.xsd?
 				 */
 
 				//TODO esta forma de recoger valores usando el otherattributes estoy por no usarlo
 				// ya que puedes acceder directamente a vs.getMin() !!!!  
 
 				Map<QName, String> attributes = vs.getOtherAttributes();
 				// rdPort = attributes.get(VirtualMachineConfiguration.remoteDesktopQname);
 				QName minAtt = new QName("http://schemas.telefonica.com/claudia/ovf","min");
 				QName maxAtt = new QName("http://schemas.telefonica.com/claudia/ovf","max");
 				QName initialAtt = new QName("http://schemas.telefonica.com/claudia/ovf","initial");
 				QName uuidAtt = new QName("http://schemas.telefonica.com/claudia/ovf","uuid");
 
 				int min = Integer.parseInt(attributes.get(minAtt));
 				int max = Integer.parseInt(attributes.get(maxAtt));
 				int initial = Integer.parseInt(attributes.get(initialAtt));
 				String uuid = attributes.get(uuidAtt);
 
 				logger.debug("min= " + min + ", max= " + max + ", initial= " + initial);
 				if (uuid != null) {
 					logger.debug("uuid = " + uuid);
 				}
 				vee.setInitReplicas(initial);
 				vee.setMinReplicas(min);
 				vee.setMaxReplicas(max);
 				vee.setUUID(uuid);
 
 
 				/* Set deployment order */
 				StartupSectionType stup = null;
 				try {
 					stup = OVFEnvelopeUtils.getSection(virtualSystemCollectionType, StartupSectionType.class);
 				} catch (SectionNotPresentException e) {
 					logger.error(e);
 				} catch (InvalidSectionException e) {
 					logger.error(e);
 				}
 				if (stup != null) {
 					List<Item> items = stup.getItem();
 					for (Iterator<Item> iteratorIm = items.iterator(); iteratorIm.hasNext();) {
 						Item it = iteratorIm.next();
 						it.getOrder();
 						if (it.getId().equals(vs.getId())) {
 							vee.setDeploymentOrder(it.getOrder());
 							logger.debug("deployment order: " + it.getOrder());
 							break;
 						}
 					}
 				} else {
 					logger.warn("no startup section was defined");
 				}
 
 				/* Virtual system type */
 				String type = vh.getSystem().getVirtualSystemType().getValue();
 				logger.debug("virt-tech = " + type);
 
 				/*
 				 * CIM_VirtualSystemSettingData VirtualSystemType property does
 				 * not define the actual text identifies, so we based on OVF
 				 * documentation examples or making it up
 				 */
 				if (type.startsWith("vmx")) {
 					/* vmx-4 is based on OVF spec 1.0.0d line 611 */
 					vee.setVirtType(VEE.VirtualizationTechnologyType.VMWARE);
 				} else if (type.startsWith("xen")) {
 					/* xen-3 is based on OVF spec 1.0.0d line 611 */
 					vee.setVirtType(VEE.VirtualizationTechnologyType.XEN);
 				} else if (type.startsWith("kvm")) {
 					/* kvm is made up */
 					vee.setVirtType(VEE.VirtualizationTechnologyType.KVM);
 				} else {
 					throw new ManiParserException("unrecognized virtual system type: " + type);
 				}
 
 				/*
 				 * Processing VirtualHardware / /* Note we get <Item> directly,
 				 * assuming that the only can appear within
 				 * <VirtualHardwareSection>. This is how it works with the
 				 * current OVF spec, however, if this change a previous step to
 				 * get <VirtualHardwareSection> would be used
 				 */
 
 				char sdaId = 'a';
 				List<RASDType> items = vh.getItem();
 				for (Iterator<RASDType> iteratorRASD = items.iterator(); iteratorRASD.hasNext();) {
 					RASDType item = (RASDType) iteratorRASD.next();
 
 					/* Get the resource type and process it accordingly */
 					int rsType = new Integer(item.getResourceType().getValue());
 					logger.debug("hw type: " + rsType);
 
 					int quantity = 1;
 					if (item.getVirtualQuantity() != null) {
 						quantity = item.getVirtualQuantity().getValue().intValue();
 					}
 
 					switch (rsType) {
 					case OVFEnvelopeUtils.ResourceTypeCPU:
 
 						for (int k = 0; k < quantity; k++) {
 							/*
 							 * VirtualQuantity in this scope means the numver of
 							 * virtual CPUs /
 							 * 
 							 * // FIXME: how to deal with CPU 'capacity'? Use
 							 * Reservation attribute? What happens if the
 							 * attribute is not present?
 							 */
 							CPUConf cpuC = new CPUConf(0);
 
 							logger.debug("cpu");
 							vee.addCPUConf(cpuC);
 						}
 
 						break;
 					case OVFEnvelopeUtils.ResourceTypeDISK:
 						/*
 						 * The rasd:HostResource will follow the pattern
 						 * 'ovf://disk/<id>' where id is the ovf:diskId of some
 						 * <Disk>
 						 */
 						String hostRes = item.getHostResource().get(0).getValue();
 						logger.debug("hostresource: " + hostRes);
 						StringTokenizer st = new StringTokenizer(hostRes, "/");
 
 						/*
 						 * Only ovf:/<file|disk>/<n> format is valid, accodring
 						 * OVF spec
 						 */
 						if (st.countTokens() != 3) {
 							throw new ManiParserException("malformed HostResource value (" + hostRes + ")");
 						}
 						if (!(st.nextToken().equals("ovf:"))) {
 							throw new ManiParserException("HostResource must start with ovf: (" + hostRes + ")");
 						}
 						String hostResType = st.nextToken();
 						if (!(hostResType.equals("disk") || hostResType.equals("file"))) {
 							throw new ManiParserException("HostResource type must be either disk or file: (" + hostRes + ")");
 						}
 						String hostResId = st.nextToken();
 
 						String fileRef = null;
 						String capacity = null;
 						if (hostResType.equals("disk")) {
 							/* This type involves an indirection level */
 							DiskSectionType ds = null;
 							try {
 								ds = OVFEnvelopeUtils.getSection(envelope, DiskSectionType.class);
 							} catch (SectionNotPresentException e) {
 								logger.error(e);
 							} catch (InvalidSectionException e) {
 								logger.error(e);
 							}
 							List<VirtualDiskDescType> disks = ds.getDisk();
 							for (Iterator<VirtualDiskDescType> iteratorDk = disks.iterator(); iteratorDk.hasNext();) {
 								VirtualDiskDescType disk = iteratorDk.next();
 
 								String diskId = disk.getDiskId();
 								if (diskId.equals(hostResId)) {
 
 									/*
 									 * Using the fileRef to find the actual URL
 									 * among <File> tags
 									 */
 									fileRef = disk.getFileRef();
 
 									/* Using the capacity attribute in disk */
 									capacity = disk.getCapacity();
 
 									break;
 								}
 							}
 						} else {
 							fileRef = hostResId;
 						}
 
 						/* Throw exceptions in the case of missing information */
 						if (fileRef == null) {
 							throw new ManiParserException("file reference can not be found for disk: " + hostRes);
 						}
 
 						URL url = null;
 						String digest = null;
 
 						ReferencesType ref = envelope.getReferences();
 						List<FileType> files = ref.getFile();
 
 						for (Iterator<FileType> iteratorFl = files.iterator(); iteratorFl.hasNext();) {
 							FileType fl = iteratorFl.next();
 							if (fl.getId().equals(fileRef)) {
 								try {
 									url = new URL(fl.getHref());
 								} catch (MalformedURLException e) {
 									throw new ManiParserException("problems parsing disk href: " + e.getMessage());
 								}
 
 								/*
 								 * If capacity was not set using ovf:capacity in
 								 * <Disk>, try to get it know frm <File>
 								 * ovf:size
 								 */
 								if (capacity == null && fl.getSize() != null) {
 									capacity = fl.getSize().toString();
 								}
 
 								/* Try to get the digest */
 								Map<QName, String> attributesFile = fl.getOtherAttributes();
 								QName digestAtt = new QName("http://schemas.telefonica.com/claudia/ovf","digest");
 								digest = attributesFile.get(digestAtt);
 
 								break;
 							}
 						}
 
 						/* Throw exceptions in the case of missing information */
 						if (capacity == null) {
 							throw new ManiParserException("capacity can not be set for disk " + hostRes);
 						}
 						if (url == null) {
 							throw new ManiParserException("url can not be set for disk " + hostRes);
 						}
 
 						if (digest == null) {
 							logger.debug("md5sum digest was not found for disk " + hostRes);
 						}
 
 						// FIXME: assuming a "hardwired" disk pattern, due to
 						// OpenNebula way of doing
 						// FIXME: unit conversion to MB?
 						File filesystem = new File("sd" + sdaId++);
 
 						String urlDisk = url.toString();
 
 						if (urlDisk.contains("file:/"))
 							urlDisk = urlDisk.replace("file:/", "file:///");
 
 						logger.debug("disk: capacity = " + toMB(capacity) + ", url = " + urlDisk + ", filesystem = " + filesystem + ", digest=" + digest);
 						DiskConf diskC = new DiskConf(toMB(capacity), url, filesystem);
 						diskC.setDigest(digest);
 
 						vee.addDiskConf(diskC);
 
 						break;
 					case OVFEnvelopeUtils.ResourceTypeMEMORY:
 
 						// FIXME: units conversion to MB? check
 						// rasd:AllocationUnits
 
 						if (vee.getMemoryConf() == null) {
 							MemoryConf memC = new MemoryConf(quantity);
 
 							logger.debug("memory: capacity = " + quantity);
 							vee.setMemoryConf(memC);
 						} else {
 							logger.warn("exceding memory declarations in VEE hardware configuration: ignoring");
 						}
 
 						break;
 					case OVFEnvelopeUtils.ResourceTypeNIC:
 
 						String netName = item.getConnection().get(0).getValue();
 
 						Network net = sa.getNetworkByName(netName);
 
 						if (net != null) {
 
 							NICConf nicC = new NICConf(net);
 
 							logger.debug("vnic: network: " + net.getName() + ", private: " + net.getPrivateNet());
 							vee.addNICConf(nicC);
 						} else {
 							throw new ManiParserException("network named " + netName + " is not found within NetworkSection");
 						}
 
 						break;
 					default:
 						throw new ManiParserException("unknown hw type: " + rsType);
 					}
 				}
 
 				try {
 					AvailabilitySectionType availabilitySec = OVFEnvelopeUtils.getSection(vs, AvailabilitySectionType.class);
 
 					WindowUnitType wut = availabilitySec.getWindow().getUnit();
 					BigInteger big = availabilitySec.getWindow().getValue();
 
 					vee.setAvailabilityWindow(wut.toString(), big.longValue());
 					vee.setAvailabilityValue(availabilitySec.getPercentile().doubleValue());
 
 				} catch (SectionNotPresentException e) {
 					/* This is not actually a bug: it is just the case in which no
 					 * deployment restriction has been specified for that VM */
 					logger.debug("no AvailabilitySection found");
 				} catch (InvalidSectionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				try {
 					DeploymentSectionType deploymentSec = OVFEnvelopeUtils.getSection(vs, DeploymentSectionType.class);
 
 					for (RestrictionType restr: deploymentSec.getRestriction()) {
 
 						GeographicDomain gd = new GeographicDomain();
 
 						// Insert the areas in the Restriction section
 						for (AreaType at: restr.getArea()) {
 							gd.addAreas(at.getValue(), at.isInside());
 						}
 
 						// Insert the countries in the Restrction section
 						for (CountryType ct :restr.getCountry()) {
 							gd.addCountries(ct.getValue().toString(), ct.isInside());
 						}
 
 						// Insert the contries in the Restrction section
 						for (TimezoneType tz: restr.getTimezone()) {
 							gd.addTimezone(tz.getValue(), tz.isInside());
 						}
 
 						// Insert the locations in the Restrction section
 						for (PositionType pos: restr.getPosition()) {
 							gd.addLocations(new GeographicDomain.Location(pos.getLocation().getLatitude(), pos.getLocation().getLongitude(), 
 									pos.getDistance().getUnit().toString(), pos.getDistance().getValue().longValue()), pos.isInside());
 						}
 
 						vee.addAllowedDomain(gd);
 					}
 
 
 				} catch (SectionNotPresentException e) {
 					/* This is not actually a bug: it is just the case in which no
 					 * deployment restriction has been specified for that VM */
 					logger.debug("no DeploymentSection found");
 
 				} catch (InvalidSectionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 
 				try {
 
 					AffinitySectionType affSec = OVFEnvelopeUtils.getSection(envelope, AffinitySectionType.class);
 
 					affSec.getOtherAttributes();
 
 					AffinityType afType= affSec.getAffinity();
 					AntiAffinityType antiAfType = affSec.getAntiAffinity();
 
 					List<String> vees = afType.getVirtualSystemId();
 
 					for (String veeAffinity: vees) {
 						if (!veeAffinity.equals(vee.getVEEName()))
 							if (afType.getScope().equals(AffinityScopeType.PHYSICAL))
 								sa.addHostAffinity(veeAffinity);
 							else if (afType.getScope().equals(AffinityScopeType.DATACENTER))
 								sa.addSiteAffinity(veeAffinity);
 							else if (afType.getScope().equals(AffinityScopeType.DOMAIN))
 								sa.addDomainAffinity(veeAffinity);
 					}
 
 					vees = antiAfType.getVirtualSystemId();
 
 					for (String veeAffinity: vees) {
 						if (!veeAffinity.equals(vee.getVEEName()))
 							if (antiAfType.getScope().equals(AffinityScopeType.PHYSICAL))
 								sa.addHostAntiAffinity(veeAffinity);
 							else if (afType.getScope().equals(AffinityScopeType.DATACENTER))
 								sa.addSiteAntiAffinity(veeAffinity);
 							else if (afType.getScope().equals(AffinityScopeType.DOMAIN))
 								sa.addDomainAntiAffinity(veeAffinity);						
 					}
 
 				} catch (InvalidSectionException e) {
 					logger.debug("no Affinity Section found");
 				} catch (SectionNotPresentException e) {
 					logger.debug("no Affinity Section found");
 				}
 
 
 				/* Finally, register the vee */
 				sa.registerVEE(vee);
 			}//End for
 		} //End else-if
 
 	}
 
 	/**
 	 * builds the rule vector
 	 */
 	private void buildRuleVector(ServiceApplication sa) {
 
 		ElasticArraySectionType elastSec = null;
 
 		ContentType entityInstance = null;
 		try {
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 		} catch (EmptyEnvelopeException e) {
 
 			logger.error(e);
 		}
 		if (entityInstance instanceof VirtualSystemType) {
 
 			//TODO
 
 		} else if (entityInstance instanceof VirtualSystemCollectionType) {
 			VirtualSystemCollectionType virtualSystemCollectionType = (VirtualSystemCollectionType) entityInstance;
 
 			for (VirtualSystemType vs : OVFEnvelopeUtils.getVirtualSystems(virtualSystemCollectionType)) {
 				try {
 
 					elastSec = OVFEnvelopeUtils.getSection(vs, ElasticArraySectionType.class);
 
 					if (elastSec != null) {
 
 						List<RuleType> rules = elastSec.getRule();
 
 						for (RuleType rule: rules) {
 							Rule sRule = new Rule(sa);
 
 							sRule.setName(rule.getKPIName());
 
 							double frequency = rule.getFrequency().doubleValue();
 							logger.debug("rule-frequency = " + frequency);
 							sRule.setFrequency(frequency);
 
 							double window = rule.getWindow().getValue().doubleValue();
 							logger.debug("rule-window = " + window);
 							sRule.setWindow(window);
 
 							double quota = rule.getQuota().doubleValue();
 							logger.debug("rule-quota = " + quota);
 							sRule.setQuota(quota);
 
 							BigDecimal toleranceBD = rule.getTolerance();
 							logger.debug("rule-tolerance = " + toleranceBD);
 							double tolerance=0.0;
 							if (toleranceBD!=null) tolerance = toleranceBD.doubleValue();
 							sRule.setTolerance(tolerance);
 
 							logger.info("PONG rule.getKPIType = " + (rule.getKPIType()));
 
							if (rule.getKPIType()==null){
								rule.setKPIType("AGENT");
							}
							
 							if (rule.getKPIType().equals("VEEHW")){
 								sRule.setKPIName(sa + ".vees." + vs.getId()+ ".replicas.1"+ ".kpis." + rule.getKPIName());
 								sRule.setEventType("VeeHwMeasureEvent");
 
 								Set<ServiceKPI> kpis = sa.getServiceKPIs();
 
 								for (Iterator<ServiceKPI> iterator = kpis.iterator(); iterator.hasNext();) {
 									
 									ServiceKPI skpi = iterator.next();
 									if(skpi.getKPIName().equals(rule.getKPIName())){
 										logger.info("PONG found KPIName equal= " + rule.getKPIName());
 										skpi.setKPIType("VEEHW");
 									}
 									
 								}
 							}
 							else {
 								sRule.setKPIName(sa + ".kpis." + rule.getKPIName());
 								sRule.setEventType("AgentMeasureEvent");
 							}
 							logger.info("PONG srule.getKPIName = " + (sRule.getKPIName()));
 
 
 							//sRule.setKPIName(sa + ".kpis." + rule.getKPIName());
 							//sRule.setEventType("AgentMeasureEvent");
 							sRule.setKPIType(rule.getKPIType());
 							sRule.setAssociatedVee(sa + ".vees." + vs.getId());
 
 							sa.registerServiceRule(sRule);
 							// grnet.customers.tid.services.ds1.vees.workernode.replicas.1.kpis.memory
 						}
 					} else {
 						logger.info("no elasticity rules were defined");
 					}
 				} catch (SectionNotPresentException e) {
 					logger.info(e);
 				} catch (InvalidSectionException e) {
 					logger.error(e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * builds the KPI vector
 	 */
 	private void buildKPIVector(ServiceApplication sa) {
 
 		KPISectionType kpisSec = null;
 		try {
 			kpisSec = OVFEnvelopeUtils.getSection(envelope, KPISectionType.class);
 			if (kpisSec != null) {
 				List<KPI> kpis = kpisSec.getKPI();
 				for (Iterator<KPI> iterator = kpis.iterator(); iterator.hasNext();) {
 					KPI kpi = iterator.next();
 					String id = kpi.getKPIname();
 					String type = kpi.getKPItype();
 					String name = kpi.getKPIVmname();
 					logger.debug("kpi-name = " + id);
 					ServiceKPI skpi = new ServiceKPI(sa, id);
 					skpi.setKPIType(type);
 					skpi.setKPIVmname(name);
 					
 					sa.registerServiceKPI(skpi);
 				}
 			} else {
 				logger.info("no kpis were defined");
 			}
 		} catch (SectionNotPresentException e) {
 			logger.error(e);
 		} catch (InvalidSectionException e) {
 			logger.error(e);
 		}
 
 	}
 
 	private static String getPropertyFromVirtualSystem(VirtualSystemType virtualSystem, String property)
 	{
 		String propValue = null;
 
 		ProductSectionType productSection;
 		try
 		{
 			productSection = OVFEnvelopeUtils.getSection(virtualSystem, ProductSectionType.class);
 			Property prop = OVFProductUtils.getProperty(productSection, property);
 			propValue = prop.getValue().toString();
 		}
 		catch (Exception e) 
 		{
 			//TODO throw PropertyNotFoundException
 			logger.error(e);
 		}
 
 		return propValue;
 	}
 
 
 	/**
 	 * 
 	 * returns the Service Application encapsulating the Service
 	 * 
 	 */
 	public ServiceApplication getServiceApplication() {
 		return sa;
 	}
 
 
 	public void setServiceApplication(ServiceApplication sa) {
 		this.sa= sa;
 	}
 
 	/**
 	 * 
 	 * @param net
 	 * @return true if net exists in the NetworksSection of the manifest
 	 */
 	private boolean haveNetwork(String netName) {
 
 		/*
 		 * Note that we are using NetworkSectionType.Network instead of Network
 		 * in order to avoid collision with class Network in
 		 * es.tid.reservoir.serviceManager.deployment.hwItems
 		 */
 		NetworkSectionType ns = null;
 		try {
 			ns = OVFEnvelopeUtils.getSection(envelope, NetworkSectionType.class);
 			List<NetworkSectionType.Network> networks = ns.getNetwork();
 			for (Iterator<NetworkSectionType.Network> iteratorN = networks.iterator(); iteratorN.hasNext();) {
 				NetworkSectionType.Network net = (NetworkSectionType.Network) iteratorN.next();
 				if (netName.equals(net.getName()))
 					return true;
 			}
 		} catch (SectionNotPresentException e) {					
 			logger.error(e);
 		} catch (InvalidSectionException e) {					
 			logger.error(e);
 		}	
 
 		return false;
 	}
 
 
 	/**
 	 * covert a size string (eg. '100GB') megabytes (MB). Supported suffix
 	 * units:
 	 * 
 	 * (none) MB GB
 	 * 
 	 */
 	private int toMB(String s) throws NumberFormatException {
 		if (s.endsWith("MB")) {
 			return Integer.parseInt(s.substring(0, s.length() - 2));
 		} else if (s.endsWith("GB")) {
 			return Integer.parseInt(s.substring(0, s.length() - 2)) * 1024;
 		} else {
 			return Integer.parseInt(s);
 		}
 	}
 
 	public HashMap<String, String> splitOvf() throws Exception {
 
 		HashMap<String, String> results = new HashMap<String, String>();
 		HashMap<String, EnvelopeType> ovfEnvelopes = splitOvf(envelope);
 
 		for (String vs: ovfEnvelopes.keySet()) {
 
 			ByteArrayOutputStream sob = new ByteArrayOutputStream();
 
 			try {
 				OVFSerializer.getInstance().writeXML(ovfEnvelopes.get(vs), sob);
 			} catch (XMLException e) {
 				logger.error("OVF could not be serialized: " + e.getMessage());
 				continue;
 			}
 
 			try {
 				results.put(vs, sob.toString(Charset.defaultCharset().toString()));
 			} catch (UnsupportedEncodingException e) {
 				logger.error("Unsuported encoding...");
 			}
 		}
 
 		return results;
 	}
 
 	/**
 	 * Split the given Service OVF file into as many files as VirtualSystems are found in it. 
 	 * 
 	 * @param env
 	 * 		A service OVF file. It has to contain a VirtualSystemCollection item.
 	 * 
 	 * @return
 	 * 		A list of OVF Envelopes, each one representing the information of a Virtual System.
 	 * @throws Exception 
 	 */
 	protected HashMap<String, EnvelopeType> splitOvf(EnvelopeType env) throws Exception {
 
 		HashMap<String, EnvelopeType> results  = new HashMap<String, EnvelopeType>();
 
 		ContentType entityInstance = null;
 		try {
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 		} catch (EmptyEnvelopeException e) {
 
 			logger.error("Empty envelope found: " + e);
 		}
 		if (entityInstance instanceof VirtualSystemType) {
 
 			// If the entity is already a VirtualSystem, return a list with the virtual system itself.
 			results.put(entityInstance.getId(), env);
 
 		} else if (entityInstance instanceof VirtualSystemCollectionType) {
 			VirtualSystemCollectionType virtualSystemCollectionType = (VirtualSystemCollectionType) entityInstance;
 
 			List<ProductSectionType> productSections=null;
 
 			productSections = OVFEnvelopeUtils.getProductSections(virtualSystemCollectionType);
 
 			for (VirtualSystemType vs : OVFEnvelopeUtils.getVirtualSystems(virtualSystemCollectionType)) {
 
 				// Create the envelope
 				ObjectFactory ovfFactory = new ObjectFactory();
 				EnvelopeType vsEnv = ovfFactory.createEnvelopeType();
 
 				// Add the VirtualSystem to the envelope
 				try {
 					ReferencesType references = ovfFactory.createReferencesType();
 					vsEnv.setReferences(references);
 					DiskSectionType diskSection = ovfFactory.createDiskSectionType();
 					diskSection.setInfo(new MsgType());
 
 					NetworkSectionType networkSection = ovfFactory.createNetworkSectionType();
 					networkSection.setInfo(new MsgType());
 
 					if (productSections!=null)
 						for (ProductSectionType ps: productSections) {
 							try {
 								OVFEnvelopeUtils.addSection(vs, ps);
 							} catch (Exception snpe) {
 								logger.warn("Product section not found: " + snpe.getMessage());
 							}
 						}
 
 					ProductSectionType restMonitoringURL = new ProductSectionType();
 
 					OVFEnvelopeUtils.addSection(vs, restMonitoringURL);
 
 					OVFEnvelopeUtils.addSection(vsEnv, diskSection);
 					OVFEnvelopeUtils.addSection(vsEnv, networkSection);
 
 					// Search for the disks and add them to the DiskSection and ReferenceSection
 					// of the child VirtualSystem
 					VirtualHardwareSectionType vh = null;
 
 					try {
 						vh = OVFEnvelopeUtils.getSection(vs, VirtualHardwareSectionType.class);
 
 						List<RASDType> items = vh.getItem();
 						for (Iterator<RASDType> iteratorRASD = items.iterator(); iteratorRASD.hasNext();) {
 							RASDType item = (RASDType) iteratorRASD.next();
 
 							/* Get the resource type and process it accordingly */
 							int rsType = new Integer(item.getResourceType().getValue());
 							logger.debug("hw type: " + rsType);
 
 							switch (rsType) {
 							case OVFEnvelopeUtils.ResourceTypeDISK:
 								String hostRes = item.getHostResource().get(0).getValue();
 								logger.debug("hostresource: " + hostRes);
 								StringTokenizer st = new StringTokenizer(hostRes, "/");
 
 								if (st.countTokens() != 3) {
 									throw new Exception("malformed HostResource value (" + hostRes + ")");
 								}
 								if (!(st.nextToken().equals("ovf:"))) {
 									throw new Exception("HostResource must start with ovf: (" + hostRes + ")");
 								}
 								String hostResType = st.nextToken();
 								if (!(hostResType.equals("disk") || hostResType.equals("file"))) {
 									throw new Exception("HostResource type must be either disk or file: (" + hostRes + ")");
 								}
 								String hostResId = st.nextToken();
 
 								if (hostResType.equals("disk")) {
 									/* This type involves an indirection level */
 									DiskSectionType ds = null;
 									String fileRef = null;
 
 									try {
 										ds = OVFEnvelopeUtils.getSection(envelope, DiskSectionType.class);
 									} catch (SectionNotPresentException e) {
 										logger.error(e);
 									} catch (InvalidSectionException e) {
 										logger.error(e);
 									}
 
 									// Fill the disk section
 									List<VirtualDiskDescType> disks = ds.getDisk();
 									for (Iterator<VirtualDiskDescType> iteratorDk = disks.iterator(); iteratorDk.hasNext();) {
 										VirtualDiskDescType disk = iteratorDk.next();
 
 										fileRef = disk.getFileRef();
 										String diskId = disk.getDiskId();
 										if (diskId.equals(hostResId)) {
 											OVFDiskUtils.addDisk(diskSection, disk);
 											break;
 										}
 									}
 
 									// Fille the Reference section
 									if (fileRef == null) {
 										throw new ManiParserException("File reference can not be found for disk: " + hostRes);
 									}
 
 									ReferencesType ref = envelope.getReferences();
 									List<FileType> files = ref.getFile();
 
 									for (Iterator<FileType> iteratorFl = files.iterator(); iteratorFl.hasNext();) {
 										FileType fl = iteratorFl.next();
 										if (fl.getId().equals(fileRef)) {
 											OVFReferenceUtils.addFile(references, fl);
 										}
 									}
 								}
 
 								break;
 
 							case OVFEnvelopeUtils.ResourceTypeNIC:
 
 								String netName = item.getConnection().get(0).getValue();
 
 								NetworkSectionType ns = OVFEnvelopeUtils.getSection(envelope, NetworkSectionType.class);
 								List<NetworkSectionType.Network> networks = ns.getNetwork();
 								for (Iterator<NetworkSectionType.Network> iteratorN = networks.iterator(); iteratorN.hasNext();) {
 									org.dmtf.schemas.ovf.envelope._1.NetworkSectionType.Network netInSection = iteratorN.next();
 
 									if (netInSection.getName().equals(netName)) 
 										OVFNetworkUtils.addNetwork(networkSection, netInSection);
 								}
 
 								break;
 							}
 						}
 
 					} catch (SectionNotPresentException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					} catch (InvalidSectionException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 
 					OVFEnvelopeUtils.addVirtualSystem(vsEnv, vs);
 
 				} catch (IdAlreadyExistsException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}	
 
 				// Add the new Envelope to the results list
 				results.put(vs.getId(), vsEnv);
 			}
 		}
 
 		return results;
 	}
 
 	public String inEnvolopeMacroReplacement(String ovf,    
 			int instanceNumber, 
 			String domain, 
 			String serviceId, 
 			String monitoringChannel, 
 			HashMap<String,ArrayList<String>> ips, 
 			HashMap<String, String> netmasks, 
 			HashMap<String, String> dnsServers, 
 			HashMap<String, String> gateways,
 			HashMap<String, HashMap<String, String> > entryPoints) throws IOException {
 
 
 		// Parse the ovf String
 		OVFSerializer ovfSerializer = OVFSerializer.getInstance();
 		try {
 			ovfSerializer.setValidateXML(false);
 
 			EnvelopeType envVee = ovfSerializer.readXMLEnvelope(new ByteArrayInputStream(ovf.getBytes()));
 
 			ContentType entityInstance = null;
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envVee);
 
 			if (entityInstance instanceof VirtualSystemCollectionType) {
 				return "";
 			} else if (entityInstance instanceof VirtualSystemType) {
 
 				OVFEnvelopeUtils.inEnvolopeMacroReplacement(envVee, (VirtualSystemType) entityInstance, instanceNumber, domain, serviceId, monitoringChannel, ips,
 						netmasks, dnsServers, gateways, entryPoints);	
 				// Serialize the ovf
 				ByteArrayOutputStream sob = new ByteArrayOutputStream();
 
 				OVFSerializer.getInstance().writeXML(envVee, sob);
 
 				return sob.toString(Charset.defaultCharset().toString());
 			} else {
 				throw new IllegalArgumentException("Virtual System not found.");
 			}
 		} catch (EmptyEnvelopeException e) {
 			logger.error("Empty envelope found: " + e);
 			throw new IOException("Empty envelope found: " + e.getMessage());
 		}catch (JAXBException e) {
 			logger.error("Unknown JAXB error");
 			throw new IOException("Unexpected errors in macro replacement." + e.getMessage());
 		} catch (XMLException e) {
 			logger.error("OVF could not be serialized: " + e.getMessage());
 			throw new IOException("Unexpected errors in macro replacement." + e.getMessage());
 		} catch (IPNotFoundException e) {
 			throw new IllegalArgumentException("No IP found: " + e.getMessage());
 		} catch (DNSServerNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("No DNS found: " + e.getMessage());
 		} catch (NetmaskNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("No Netmask found: " + e.getMessage());
 		} catch (GatewayNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("No Gateway found: " + e.getMessage());
 		} catch (PrecedentTierEntryPointNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("No Precedent Tier found: " + e.getMessage());
 		} catch (NotEnoughIPsInPoolException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("Not enough IPs found: " + e.getMessage());
 		} catch (PoolNameNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("Pool Name not found: " + e.getMessage());
 		}
 	}
 }
