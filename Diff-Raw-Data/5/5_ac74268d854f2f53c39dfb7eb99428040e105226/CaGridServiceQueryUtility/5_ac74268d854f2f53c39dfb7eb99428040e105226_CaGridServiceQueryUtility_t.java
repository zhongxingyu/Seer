 /*******************************************************************************
  * Copyright (C) 2007 The University of Manchester   
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 package net.sf.taverna.cagrid.ui.servicedescriptions;
 
 import gov.nih.nci.cagrid.discovery.client.DiscoveryClient;
 import gov.nih.nci.cagrid.metadata.MetadataUtils;
 import gov.nih.nci.cagrid.metadata.ServiceMetadata;
 import gov.nih.nci.cagrid.metadata.ServiceMetadataServiceDescription; 
 import gov.nih.nci.cagrid.metadata.common.PointOfContact;
 import gov.nih.nci.cagrid.metadata.common.UMLClass;
 import gov.nih.nci.cagrid.metadata.exceptions.QueryInvalidException;
 import gov.nih.nci.cagrid.metadata.exceptions.RemoteResourcePropertyRetrievalException;
 import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;
 import gov.nih.nci.cagrid.metadata.service.Operation; 
 import gov.nih.nci.cagrid.metadata.service.ServiceContext; 
 import gov.nih.nci.cagrid.metadata.service.ServiceContextOperationCollection;
 import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 
 import javax.swing.JOptionPane;
 
 import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;
 import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;
 
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.log4j.Logger;
 
 /**
  * An agent to query Index Service to determine the available 
  * services and add them to Tavern's Service Panel.
  * 
  * @author Wei Tan
  * @author Alex Nenadic
  * 
  */
 
 public class CaGridServiceQueryUtility {
 
 	private static Logger logger = Logger.getLogger(CaGridServiceQueryUtility.class);
 
 	/**
 	 *  Load services & operations by caGrid discovery service API and 
 	 *  add the found services (i.e. their operations) to the Service Panel.
 	 */
 	public static void loadServices(String caGridName, String indexServiceURL, CaGridServiceQuery[] sq,
 			FindServiceDescriptionsCallBack callBack) throws Exception {
 		
 		if (sq.length == 0){
 			logger.info("Searching for all services in " + caGridName);
 		}
 		else{
 			logger.info("Searching for services in " + caGridName + ". Using " + sq.length + " search criteria.");
 		}
 		EndpointReferenceType[] servicesList = null;
 		servicesList = getEPRListByServiceQueryArray(indexServiceURL, sq);
 		
 		logger.info("caGrid DiscoveryClient loaded and EPRs to services returned.");
 		
 		if (servicesList == null || servicesList.length==0){ // servicesList is returned as null and not empty array for some reason, but check anyway
             JOptionPane.showMessageDialog(null,
                     "caGrid services search did not find any matching services", 
                     "caGrid services search",
                     JOptionPane.INFORMATION_MESSAGE);  
 			logger.error("caGrid search: resulting caGrid service list returned is null (empty).");
 		}
 		else{
 			logger.info("Discovered "+ servicesList.length + " caGrid EPRs.");
 			
 			// Counter of operations added to Taverna's Service Panel as services
 			int serviceCounter = 0;
 			
 			for (EndpointReferenceType epr : servicesList) {
 				String serviceAddress = epr.getAddress().toString();					
 				String wsdlURL = serviceAddress + "?wsdl";
 					
 				// Find serviceName from the URI
 				URI uri = URI.create(epr.getAddress().toString());
 				URI parentURI = uri.resolve(".");
 				URI relativeURI = parentURI.relativize(uri);
 				String serviceName = relativeURI.getPath();		
 				
 				URI wsdlURI = URI.create(wsdlURL);
 
 				logger.info("Discovered caGrid service: "+ wsdlURL);
 					
 				// We'll just parse the wsdl here as using the commented out method below
 				// we get the operations that are not from this wsdl but from the related
 				// job resource
 				try {
 					ServiceMetadata serviceMetadata = MetadataUtils.getServiceMetadata(epr);	
 					ServiceMetadataServiceDescription serviceMetadataDesc = serviceMetadata.getServiceDescription();
 					ServiceServiceContextCollection srvContxCol = serviceMetadataDesc.getService().getServiceContextCollection();						
 					ServiceContext[] srvContxs = srvContxCol.getServiceContext();
 					List<CaGridServiceDescription> serviceDescriptions = new ArrayList<CaGridServiceDescription>();
 					for (ServiceContext srvcontx : srvContxs) {
 						ServiceContextOperationCollection operationCollection = srvcontx.getOperationCollection();
 						String srvcontxServiceName = srvcontx.getName();
 						if (operationCollection != null){
 							Operation[] ops = srvcontx.getOperationCollection().getOperation();
 							for (Operation op : ops) {
 								// Add an operation as Taverna's ServiceDescription in Service Panel
 								CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
 								serviceDesc.setOperation(op.getName());
 								serviceDesc.setUse(op.getName());
 								//CaGrid services are all DOCUMENT style
 								serviceDesc.setStyle("document");
 								serviceDesc.setURI(URI.create(wsdlURL));
 								serviceDesc.setResearchCenter(serviceMetadata
 										.getHostingResearchCenter().getResearchCenter()
 										.getDisplayName());	
 								serviceDesc.setCaGridName(caGridName);
 								serviceDesc.setIndexServiceURL(indexServiceURL);
 								if (!srvcontxServiceName.equals(serviceName)){
 									// This is a helper service
 									serviceDesc.setHelperService(true);
 									serviceDesc.setHelperServiceName(srvcontxServiceName);
 									// The helper service has its own wsdl URL different from the one
 									// of the master service
 									serviceDesc.setURI(parentURI.resolve(srvcontxServiceName + "?wsdl"));
 									serviceDesc.setMasterURI(wsdlURI);
 								}
 								logger.info("Adding operation "+ op.getName()+" under caGrid service "+ wsdlURL);
 								serviceDescriptions.add(serviceDesc);
 								serviceCounter++;
 							}
 						}
 					}
 					callBack.partialResults(serviceDescriptions);
 				}
 				catch (Exception e) {
 					// This service probably did not have the getResourceProperty method defined
 					// so getServiceMetadata failed - do the old fashioned wsdl parsing
 					
 					WSDLParser parser = null;
 					try{
 						parser = new WSDLParser(wsdlURL);
 						List<CaGridServiceDescription> serviceDescriptions = new ArrayList<CaGridServiceDescription>();
 						List<javax.wsdl.Operation> operations = parser.getOperations();
 						for (javax.wsdl.Operation operation : operations) {
 							CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
 							serviceDesc.setOperation(operation.getName());
 							serviceDesc.setUse(operation.getName());
 							//CaGrid services are all DOCUMENT style
 							serviceDesc.setStyle("document");
 							serviceDesc.setURI(URI.create(wsdlURL));
 							serviceDesc.setCaGridName(caGridName);
 							serviceDesc.setIndexServiceURL(indexServiceURL);
 							
 							WSDLParser.flushCache(wsdlURL);
 
 							// We cannot discover the helper services from the wsdl document
 							// so we are not setting them here at all
 							
 							// Security properties of the service will be set later
 							// at the time of invoking the activity
 							
 							logger.info("Adding operation "+ operation.getName() +" under caGrid service "+ wsdlURL);
 							serviceDescriptions.add(serviceDesc);
 							serviceCounter++;
 						}
 						callBack.partialResults(serviceDescriptions);
 					}
 					catch (Exception ex){
 						// Ignore - skip to the next EPR
 					}
 				}
 
 				// This was the original code for obtaining operations of a wsdl service from
 				// service metadata etc, now replaced by the code above
 				//CaGridService service = new CaGridService(serviceAddress + "?wsdl", serviceAddress);
 				/*try {
 					ServiceMetadata serviceMetadata = MetadataUtils
 							.getServiceMetadata(epr);
 					ServiceMetadataServiceDescription serviceDes = serviceMetadata
 							.getServiceDescription();
 						// ServiceContextOperationCollection s =
 					// serviceDes.getService().getServiceContextCollection().getServiceContext(0).getOperationCollection();
 						ServiceServiceContextCollection srvContxCol = serviceDes
 							.getService().getServiceContextCollection();
 					ServiceContext[] srvContxs = srvContxCol
 							.getServiceContext();
 						service.setResearchCenterName(serviceMetadata
 							.getHostingResearchCenter().getResearchCenter()
 							.getDisplayName());
 					for (ServiceContext srvcontx : srvContxs) {
 						ServiceContextOperationCollection operationCollection = srvcontx.getOperationCollection();
 						if (operationCollection != null){
 							Operation[] ops = srvcontx
 									.getOperationCollection()
 									.getOperation();
 								// TODO: portType is no longer needed??
 							for (Operation op : ops) {
 								// add an operation node
 								// print out the name of an operation
 								String operationName = op.getName();
 								// OperationInputParameterCollection opp =
 								// op.getInputParameterCollection();
 									service.addOperation(operationName);
 								// System.out.println(operationName);
 							}
 						}
 						}
 					services.add(service);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}*/
 			}
 			logger.info("Added " + serviceCounter + " caGrid services to Service Panel.");
 	    	callBack.finished();
 		}
 	}
 
 	public static EndpointReferenceType[] getEPRListByServiceQuery(
 			String indexURL, CaGridServiceQuery sq) {
 		EndpointReferenceType[] servicesList  = null;
 		DiscoveryClient client = null;
 		try {
 			client = new DiscoveryClient(indexURL);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (sq == null) {
 			logger.info("Retrieving all caGrid services from the Index Service: "
 							+ indexURL);
 			try {
 				servicesList = client.getAllServices(true);
 			} catch (RemoteResourcePropertyRetrievalException e) {
 				logger.error("Error retrieving all caGrid services from the Index Service", e);
 				e.printStackTrace();
 			} catch (QueryInvalidException e) {
 				logger.error("Error retrieving all caGrid services from the Index Service", e);
 				e.printStackTrace();
 			} catch (ResourcePropertyRetrievalException e) {
 				logger.error("Error retrieving all caGrid services from the Index Service", e);
 				e.printStackTrace();
 			}
 		} else {
 
 			// semanticQueryingClause = indexURL.substring(n1+2);
 
 			logger.info("caGrid service query criteria: " + sq.queryCriteria + "  == "
 					+ sq.queryValue);
 			
 			// TODO: semantic based service searching
 			// query by Search String
 			if (sq.queryCriteria.equals("Search String")) {
 				logger.info("Searching by 'Search Sting' criteria.");
 				try {
 					servicesList = client
							.discoverServicesBySearchString(sq.queryValue);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Search String'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Search String'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Search String'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Research Center Name
 			else if (sq.queryCriteria.equals("Research Center")) {
 				logger.info("Searching by 'Research Center' criteria.");
 				try {
 					servicesList = client
 							.discoverServicesByResearchCenter(sq.queryValue);
 				}catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Research Center'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Research Center'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Research Center'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Point of Contact
 			else if (sq.queryCriteria.equals("Point Of Contact")) {
 				logger.info("Searching by 'Point Of Contact' criteria.");
 				PointOfContact poc = new PointOfContact();
 				int n3 = sq.queryValue.indexOf(" ");
 				String firstName = sq.queryValue.substring(0, n3);
 				String lastName = sq.queryValue.substring(n3 + 1);
 				poc.setFirstName(firstName);
 				poc.setLastName(lastName);
 				try {
 					servicesList = client.discoverServicesByPointOfContact(poc);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Point of Contact'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Point of Contact'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Point of Contact'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Service Name
 			else if (sq.queryCriteria.equals("Service Name")) {
 				logger.info("Searching by 'Service Name' criteria.");
 				try {
 					servicesList = client.discoverServicesByName(sq.queryValue);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Service Name'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Service Name'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Service Name'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Operation Name
 			else if (sq.queryCriteria.equals("Operation Name")) {
 				logger.info("Searching by 'Operation Name' criteria.");
 				try {
 					servicesList = client
 							.discoverServicesByOperationName(sq.queryValue);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Name'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Name'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Name'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Operation Input
 			else if (sq.queryCriteria.equals("Operation Input")) {
 				logger.info("Searching by 'Operation Input' criteria.");
 				UMLClass umlClass = new UMLClass();
 				umlClass.setClassName(sq.queryValue);
 				try {
 					servicesList = client
 							.discoverServicesByOperationInput(umlClass);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Input'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Input'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Input'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Operation Output
 			else if (sq.queryCriteria.equals("Operation Output")) {
 				logger.info("Searching by 'Operation Output' criteria.");
 				UMLClass umlClass = new UMLClass();
 				umlClass.setClassName(sq.queryValue);
 				try {
 					servicesList = client
 							.discoverServicesByOperationOutput(umlClass);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Output'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Output'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Output'", e);
 					e.printStackTrace();
 				}
 			}
 			// query by Operation Class
 			else if (sq.queryCriteria.equals("Operation Class")) {
 				logger.info("Searching by 'Operation Class' criteria.");
 				UMLClass umlClass = new UMLClass();
 				umlClass.setClassName(sq.queryValue);
 				try {
 					servicesList = client
 							.discoverServicesByOperationClass(umlClass);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Class'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Class'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Class'", e);
 					e.printStackTrace();
 				}
 			}
 			// discoverServicesByConceptCode("C43418")
 			else if (sq.queryCriteria.equals("Concept Code")) {
 				logger.info("Searching by 'Concept Code' criteria.");
 				try {
 					servicesList = client
							.discoverDataServicesByModelConceptCode(sq.queryValue);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Concept Code'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Concept Code'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Concept Code'", e);
 					e.printStackTrace();
 				}
 			}
 			// discoverServicesByOperationConceptCode
 			// discoverServicesByDataConceptCode
 			// discoverServicesByPermissibleValue
 			// getAllDataServices
 			// discoverDataServicesByDomainModel("caCore")
 			else if (sq.queryCriteria.equals("Domain Model for Data Services")) {
 				logger.info("Searching by 'Domain Model for Data Services' criteria.");
 				try {
 					servicesList = client
 							.discoverDataServicesByDomainModel(sq.queryValue);
 				} catch (RemoteResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Domain Model for Data Services'", e);
 					e.printStackTrace();
 				} catch (QueryInvalidException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Domain Model for Data Services'", e);
 					e.printStackTrace();
 				} catch (ResourcePropertyRetrievalException e) {
 					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Domain Model for Data Services'", e);
 					e.printStackTrace();
 				}
 			}
 			// discoverDataServicesByModelConceptCode
 			// discoverDataServicesByExposedClass
 			// discoverDataServicesByPermissibleValue
 			// discoverDataServicesByAssociationsWithClass
 			// discoverByFilter
 		}
 		return servicesList;
 
 	}
 
 	public static EndpointReferenceType[] getEPRListByServiceQueryArray(
 			String indexURL, CaGridServiceQuery sq[]) {
 		EndpointReferenceType[] servicesList = null;
 		if ((sq == null) || (sq.length==0)) { // null or empty service query list
 			return getEPRListByServiceQuery(indexURL, null);
 		} else if (sq.length == 1) {
 			return getEPRListByServiceQuery(indexURL, sq[0]);
 		}
 		// sq holds more than 1 queries
 		else if (sq.length > 1) {
 			EndpointReferenceType[][] tempEPRList = new EndpointReferenceType[sq.length][];
 			for (int i = 0; i < sq.length; i++) {
 				tempEPRList[i] = getEPRListByServiceQuery(indexURL, sq[i]);
 			}
 			return CombineEPRList(tempEPRList);
 		}
 		return servicesList;
 
 	}
 
 	public static EndpointReferenceType[] CombineEPRList(
 			EndpointReferenceType[][] tempEPRList) {
 		EndpointReferenceType[] servicesList = null;
 		String[][] addressList = new String[tempEPRList.length][];
 		for (int i = 0; i < tempEPRList.length; i++) {
 			addressList[i] = new String[tempEPRList[i].length];
 			for (int j = 0; j < tempEPRList[i].length; j++) {
 				addressList[i][j] = tempEPRList[i][j].getAddress().toString();
 			}
 		}
 		List<String> alist = new ArrayList<String>(Arrays.asList(addressList[0]));
 		for (int i = 1; i < tempEPRList.length; i++) {
 			alist.retainAll(Arrays.asList(addressList[i]));
 		}
 
 		int count = 0;
 		int[] flag = new int[tempEPRList[0].length];
 		for (int i = 0; i < tempEPRList[0].length; i++) {
 			if (alist.contains(tempEPRList[0][i].getAddress().toString())) {
 				count++;
 				flag[i] = 1;
 			}
 		}
 		servicesList = new EndpointReferenceType[count];
 		int j = 0;
 		for (int i = 0; i < tempEPRList[0].length; i++) {
 
 			if (flag[i] == 1) {
 				servicesList[j++] = tempEPRList[0][i];
 			}
 		}
 		return servicesList;
 	}
 
 }
 
 class ServiceMetaData {
 	String[] serviceAddress = null;
 	String[][] operationName = null;
 
 	ServiceMetaData() {
 		//String[] serviceAddress = null;
 		//String[][] operationName = null;
 	}
 
 }
