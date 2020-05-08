 package org.easysoa.registry.integration;
 
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.SubprojectServiceImpl;
 import org.easysoa.registry.facets.WsdlInfoFacet;
 import org.easysoa.registry.rest.integration.EndpointInformations;
 import org.easysoa.registry.rest.integration.ServiceInformation;
 import org.easysoa.registry.rest.integration.ServiceInformations;
 import org.easysoa.registry.rest.integration.SimpleRegistryService;
 import org.easysoa.registry.types.SoaNode;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * Simple service registry implementation
  * 
  * @author jguillemotte
  *
  */
 @Path("easysoa/simpleRegistryService")
 public class SimpleRegistryServiceImpl implements SimpleRegistryService {
 
     //TODO : How to get nuxeo base url ?
     public final static String NUXEO_BASE_URL = "http://localhost:8080/nuxeo/";
     
     //
     @Context HttpServletRequest request;
     
     /**
      * 
      */
     @Override
     public ServiceInformations queryWSDLInterfaces(String search, String subProjectId, String visibility) throws Exception {
         
         CoreSession documentManager = SessionFactory.getSession(request);
         DocumentService documentService = Framework.getService(DocumentService.class);
         
         boolean searchParamIncluded = false;
         
         ArrayList<String> parameters = new ArrayList<String>();
         StringBuilder query = new StringBuilder(); 
         query.append("SELECT * FROM InformationService ");
         // Search parameter
         if(search != null && !"".equals(search)){
             String searchParam = "%"+search+"%";
             query.append("WHERE (dc:title like '?' " +
                     "OR " + SoaNode.XPATH_SOANAME + " like '?' " +
                             "OR dc:description like '?' " +
                                     "OR " + WsdlInfoFacet.XPATH_WSDL_PORTTYPE_NAME + " like '?')");
             parameters.add(searchParam);
             parameters.add(searchParam);
             parameters.add(searchParam);
             parameters.add(searchParam);
             searchParamIncluded = true;
         }
         // Project ID parameter
         if(subProjectId != null && !"".equals(subProjectId)){ 
             // TODO : not implemented yet in Nuxeo registry
             //[and projectId=appGivenProjectId || projectId in (allProjectIdsReferredByFraSCAtiStudioProject)]
             String subProjectCriterias = "";
             DocumentModel subProjectModel = SubprojectServiceImpl.getSubprojectById(documentManager, subProjectId);
             if(subProjectModel != null){
               subProjectCriterias  = SubprojectServiceImpl.buildCriteriaSeenFromSubproject(subProjectModel/*, visibility*/);
                if(searchParamIncluded){
                    query.append(" AND ").append(subProjectCriterias);    
                } else {
                    query.append(" WHERE ").append(subProjectCriterias);
                }
             } else {
                 throw new Exception("subProjectId '" + subProjectId + "' is not a valid subproject !");
             }
        }
         
         // LATER
         // Platform parameter
         //if(platformId != null && !"".equals(platformId)){
             // TODO Add implementation for condition, would allow to search only in its own services
             // what acts as platformId for : FraSCAti Studio (base url and not Cloud(s) url), Talend (talend repository url ?)
             // for now only platform:deliverableRepositoryUrl exists that could act as platform id, later maybe :
             // [and is/impl:platformId='fraSCAtiStudioPlatformId']
         //}
 
         // LATER
         // Platform service standard parameter
         //if(platformServiceStandard != null && !"".equals(platformServiceStandard)){
             // LATER Add doc model & implementation for condition (FraSCAti & CXF only support WSDL11), something like :
             // platform:wsdlVersion / WsdlInfo.XPATH_WSDL_VERSION='1.1'            
         //}        
         
         // LATER
         // WS binding transport(_type) parameter
         //if(wsBindingTransport != null && !"".equals(wsBindingTransport)){
             // LATER Add doc model & implementation for condition (for now only HTTP), something like :
             // [and platform:serviceTransport or endpointServiceTransport='http://schemas.xmlsoap.org/soap/http']
         //}
         
         // LATER other platform parameters (see ServiceMatchingListener.findIServicesForImpl()) ?
 
         // Execute query
         String nxqlQuery = NXQLQueryBuilder.getQuery(query.toString(), parameters.toArray(), false, true);
         //DocumentModelList soaNodeModelList = documentManager.query(nxqlQuery);
         DocumentModelList soaNodeModelList = documentService.query(documentManager, nxqlQuery, true, false);
         
         // Write response
         ServiceInformations serviceInformations = new ServiceInformations();
         for (DocumentModel soaNodeModel : soaNodeModelList) {
             serviceInformations.addServiceInformation(SoaNodeInformationToWSDLInformationMapper.mapToServiceInformation(soaNodeModel, NUXEO_BASE_URL, documentManager));
         }
         return serviceInformations;
     }
 
     /**
      * 
      */
     //@Override
     public EndpointInformations queryEndpoints(String search, String subProjectId, String visibility) throws Exception {
         CoreSession documentManager = SessionFactory.getSession(request);
         return SimpleRegistryServiceImpl.queryEndpoints(documentManager, search, subProjectId, visibility);
     }
 
     // TODO : make a service with this method
     public static EndpointInformations queryEndpoints(CoreSession documentManager, String search, String subProjectId, String visibility) throws Exception {
         DocumentService documentService = Framework.getService(DocumentService.class);
         
         boolean searchParamIncluded = false;
         
         // Fetch SoaNode list
         ArrayList<String> parameters = new ArrayList<String>(); 
         StringBuilder query = new StringBuilder(); 
         query.append("SELECT * FROM Endpoint ");
 
         // Search parameter
         if(search != null && !"".equals(search)){
             String searchParam = "%"+search+"%";
             query.append("WHERE dc:title like '?' " +
                     "OR " + SoaNode.XPATH_SOANAME + " like '?' " +
                             "OR dc:description like '?' " +
                                     "OR " + WsdlInfoFacet.XPATH_WSDL_PORTTYPE_NAME + " like '?'");
             parameters.add(searchParam);
             parameters.add(searchParam);
             parameters.add(searchParam);
             parameters.add(searchParam);
             searchParamIncluded = true;
         }
         // Project ID parameter
         if(subProjectId != null && !"".equals(subProjectId)){
             // TODO : not implemented yet in Nuxeo registry
             //[and projectId=appGivenProjectId || projectId in (allProjectIdsReferredByFraSCAtiStudioProject)]
             String subProjectCriterias = "";
             DocumentModel subProjectModel = SubprojectServiceImpl.getSubprojectById(documentManager, subProjectId);
             if(subProjectModel != null){
               subProjectCriterias  = SubprojectServiceImpl.buildCriteriaSeenFromSubproject(subProjectModel/*, visibility*/);
                if(searchParamIncluded){
                    query.append(" AND ").append(subProjectCriterias);    
                } else {
                    query.append(" WHERE ").append(subProjectCriterias);
                }
             } else {
                 throw new Exception("subProjectId '" + subProjectId + "' is not a valid subproject !");
             }            
         }        
         
         String nxqlQuery = NXQLQueryBuilder.getQuery(query.toString(), parameters.toArray(), false, true);
         //DocumentModelList soaNodeModelList = documentManager.query(nxqlQuery);
         DocumentModelList soaNodeModelList = documentService.query(documentManager, nxqlQuery, true, false);
         
         // Write response
         EndpointInformations endpointInformations = new EndpointInformations();
         for (DocumentModel soaNodeModel : soaNodeModelList) {
             endpointInformations.addEndpointInformation(SoaNodeInformationToWSDLInformationMapper.mapToEndpointInformation(soaNodeModel, NUXEO_BASE_URL));
         }
         return endpointInformations;
     }
     
     //@Override
     public ServiceInformations queryServicesWithEndpoints(String search, String subProjectId, String visibility) throws Exception {
         
         // Get services 
         ServiceInformations serviceInformations = this.queryWSDLInterfaces(search, subProjectId, visibility);
 
         // For each service, get the corresponding endpoints
         for(ServiceInformation serviceInformation : serviceInformations.getServiceInformationList()){
             EndpointInformations endpoints = this.queryServiceEndpoints(serviceInformation.getNuxeoID());
             serviceInformation.setEndpoints(endpoints);
         }
 
         return serviceInformations;
     }
     
     /**
      * Returns the endpoints associated with the service
      * @param serviceId The service Nuxeo UUID
      * @throws Exception If a problem occurs
      */
     private EndpointInformations queryServiceEndpoints(String serviceId) throws Exception {
         CoreSession documentManager = SessionFactory.getSession(request);
 
         EndpointInformations endpoints = new EndpointInformations();
         
         // Fetch SoaNode list
         ArrayList<String> parameters = new ArrayList<String>(); 
         StringBuilder query = new StringBuilder(); 
         query.append("SELECT * FROM Endpoint WHERE impl:providedInformationService = '?'");
         parameters.add(serviceId);
 
         String nxqlQuery = NXQLQueryBuilder.getQuery(query.toString(), parameters.toArray(), false, true);
         DocumentModelList soaNodeModelList = documentManager.query(nxqlQuery);        
     
         for (DocumentModel soaNodeModel : soaNodeModelList) {
             endpoints.addEndpointInformation(SoaNodeInformationToWSDLInformationMapper.mapToEndpointInformation(soaNodeModel, NUXEO_BASE_URL));
         }        
     
         return endpoints;
     }
     
 }
