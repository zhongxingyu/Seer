 /**
  * EasySOA Registry
  * Copyright 2012 Open Wide
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact : easysoa-dev@googlegroups.com
  */
 
 package org.easysoa.registry.monitoring.rest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import org.apache.log4j.Logger;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.integration.EndpointStateServiceImpl;
 import org.easysoa.registry.integration.SimpleRegistryServiceImpl;
 import org.easysoa.registry.rest.integration.EndpointInformation;
 import org.easysoa.registry.rest.integration.EndpointStateService;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicator;
 import org.easysoa.registry.types.Endpoint;
 import org.easysoa.registry.types.InformationService;
 import org.easysoa.registry.types.SubprojectNode;
 import org.easysoa.registry.types.ids.SoaNodeId;
 import org.easysoa.registry.utils.ContextData;
 import org.easysoa.registry.utils.EasysoaModuleRoot;
 import org.easysoa.registry.utils.NXQLQueryHelper;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.ecm.webengine.model.Template;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
 import org.nuxeo.runtime.api.Framework;
 
 
 /**
  * Indicators
  * 
  * impl : TODO see ServiceDocumentationController, MatchingDashboard...
  * 
  * @author mdutoo, jguillemotte
  * 
  */
 @WebObject(type = "monitoring")
 @Path("easysoa/monitoring")
 public class EndpointStateController extends EasysoaModuleRoot {
 
     private static final String SERVICE_LIST_PROPS = "*"; // "ecm:title"
     
     @SuppressWarnings("unused")
     private static Logger logger = Logger.getLogger(EndpointStateController.class);
     
     /**
      * Default constructor
      */
     public EndpointStateController() {
         
     }
     
     @GET
     @Produces(MediaType.TEXT_HTML)
     public Object doGetHTML(@QueryParam("subprojectId") String subProjectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
         List<String> envs = new ArrayList<String>();
         
         // Get the environments
         DocumentModelList environments = session.query("SELECT DISTINCT " + Endpoint.XPATH_ENDP_ENVIRONMENT + " FROM " + Endpoint.DOCTYPE);
         // Fill the envs list
         for(DocumentModel model : environments){
             envs.add((String)model.getPropertyValue(Endpoint.XPATH_ENDP_ENVIRONMENT));
         }
         
         //EndpointStateService endpointStateService = Framework.getService(EndpointStateService.class);
         //endpointStateService.getSlaOrOlaIndicatorsByEnv(environment, projectId, periodStart, periodEnd, pageSize, pageStart);
         
         // Get endpoints
         //SimpleRegistryService simpleRegistryService = Framework.getService(SimpleRegistryService.class);
         //SimpleRegistryService simpleRegistryService = new SimpleRegistryServiceImpl();
        
         // Get the deployed services
         // Get the service
         //String serviceId = endpoint.getProperty("impl:providedInformationService").getValue(String.class);
         String query = "SELECT * FROM " + InformationService.DOCTYPE;
         String subProjectPathCriteria = NXQLQueryHelper.buildSubprojectPathCriteria(session, subProjectId, visibility);
         if(!"".equals(subProjectPathCriteria)){
             query = query + DocumentService.NXQL_WHERE + subProjectPathCriteria;
         }
  
         DocumentModelList services = docService.query(session, query, true, false);
         Map<String, DocumentModelList> endpoints = new HashMap<String, DocumentModelList>();
         
         for(DocumentModel service : services){
             // Get the endpoint
            query = "SELECT * FROM " + Endpoint.DOCTYPE + DocumentService.NXQL_WHERE
                    + " impl:providedInformationService = '" + service.getId() + "'";
            if(!"".equals(subProjectPathCriteria)){
                query = query + DocumentService.NXQL_AND + subProjectPathCriteria;
            }
            
            DocumentModelList endpointsList = docService.query(session, query, true, false);
             endpoints.put(service.getName(), endpointsList);
         }
         
         return getView("dashboard")
                 .arg("envs", envs) // TODO later by (sub)project
                 .arg("endpoints", endpoints)
                 .arg("subprojectId", subProjectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subProjectId));
     }
     
     @GET
     @Path("envIndicators/{endpointId:.+}") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doGetByPathHTML(@PathParam("endpointId") String endpointId, @QueryParam("subprojectId") String subProjectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
         
         // Check there is at least on environment
         if(endpointId == null || "".equals(endpointId)){
             throw new IllegalArgumentException("At least one endpointID must be specified");
         }
         
         // Get the enpoints associated with the environment
         //DocumentModelList endpointsModel = session.query("SELECT * FROM " + Endpoint.DOCTYPE + " WHERE " + Endpoint.XPATH_ENDP_ENVIRONMENT + " = " + envName);
 
         // Get the endpoint
         DocumentModel endpoint = docService.query(session, "SELECT * FROM " + Endpoint.DOCTYPE + " WHERE ecm:uuid = '" + endpointId + "'" , true, false).get(0);
         // Get the service
         String serviceId = endpoint.getProperty("impl:providedInformationService").getValue(String.class);
         DocumentModel service = docService.query(session, "SELECT * FROM " + InformationService.DOCTYPE + " WHERE ecm:uuid = '" + serviceId + "'" , true, false).get(0);
         String slaOrOlaSubprojectId = (String) service.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT);
         
         //String serviceFolder = session.getDocument(service.getParentRef()).getPathAsString();
         
         // Get the indicators for the endpoint in the Nuxeo store
         EndpointStateService endpointStateService = new EndpointStateServiceImpl();
         List<SlaOrOlaIndicator> indicators =  endpointStateService.getSlaOrOlaIndicators(endpointId, "", null, null, 10, 0).getSlaOrOlaIndicatorList();
 
         // TODO Complete the returned indicators 
         // Complete each indicator with the description / services ...
         for(SlaOrOlaIndicator indicator : indicators){
             indicator.setDescription("Aucune description");
             
             DocumentModel slaOrOla;
             // Get only the first par of the subprojectID
             /*String subPath = "";
             if(subProjectId != null && !"".equals(subProjectId)){
                 subPath = subProjectId.substring(0, subProjectId.lastIndexOf("/"));
             }*/
             // Get the SLA or OLA indicator in the Nuxeo registry
             slaOrOla = docService.findSoanode(session, new SoaNodeId(slaOrOlaSubprojectId, org.easysoa.registry.types.SlaOrOlaIndicator.SLA_DOCTYPE, indicator.getSlaOrOlaName()), true);
             if(slaOrOla == null){
                 slaOrOla = docService.findSoanode(session, new SoaNodeId(slaOrOlaSubprojectId, org.easysoa.registry.types.SlaOrOlaIndicator.OLA_DOCTYPE, indicator.getSlaOrOlaName()), true);    
             }
             
             // Set additionnal indicator informatons
             if(slaOrOla != null){
                 indicator.setDescription(slaOrOla.getProperty(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_SLA_OR_OLA_DESCRIPTION).getValue(String.class));
                 indicator.setPath(slaOrOla.getPathAsString());
             }
         }
         
         Template view = getView("envIndicators"); // TODO see services.ftl, dashboard/*.ftl...
         if (indicators != null) {
             view = view.arg("indicators", indicators);
         }
         view.arg("subprojectId", subProjectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subProjectId))
                 .arg("service", service.getName())
                 .arg("servicePath", service.getPathAsString())
                 .arg("endpoint", endpoint);
         
         return view; 
     }
     
 }
