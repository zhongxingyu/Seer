 /**
  * EasySOA Proxy
  * Copyright 2011-2013 Open Wide
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
 
 package org.easysoa.registry.context.rest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.types.Project;
 import org.easysoa.registry.types.Subproject;
 import org.easysoa.registry.utils.ContextData;
 import org.easysoa.registry.utils.EasysoaModuleRoot;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.ecm.webengine.model.Template;
 import org.nuxeo.ecm.webengine.model.WebObject;
 
 /**
  *
  * @author jguillemotte
  */
 @WebObject(type = "context")
 @Path("easysoa/context")
 public class ContextController extends EasysoaModuleRoot {
 
     // Logger
     private static Logger logger = Logger.getLogger(ContextController.class);
     
     /**
      * Returns the context view
      * @return The context view
      * @throws Exception If a problem occurs
      */
     @GET
     @Produces(MediaType.TEXT_HTML)    
     public Template doGetHtml(@QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         Template view = getView("context");
 
         // Get the projects
         DocumentModelList projectsList = session.query("SELECT * FROM " + Project.DOCTYPE);
 
         HashMap<String, HashMap<String, List<DocumentModel>>> projectIdToSubproject = new HashMap<String, HashMap<String, List<DocumentModel>>>();
         // For each project, get the subprojects
         for(DocumentModel project : projectsList){
             HashMap<String, List<DocumentModel>> liveAndVersions = new HashMap<String, List<DocumentModel>>();
             // Get the live for the project
             List<DocumentModel> lives = new ArrayList<DocumentModel>();
             String nxqlRequest = DocumentService.NXQL_SELECT_FROM + Subproject.DOCTYPE + DocumentService.NXQL_WHERE_NO_PROXY
                     + DocumentService.NXQL_AND + "spnode:subproject STARTSWITH '" + project.getPathAsString() + "'"
                    + DocumentService.NXQL_AND + DocumentService.NXQL_IS_NOT_VERSIONED + "' ORDER BY dc:title ASC";
             DocumentModelList liveList = session.query(nxqlRequest);
             for(DocumentModel live : liveList){
                 lives.add(live);
             }
             liveAndVersions.put("live", lives);
             
             // Get the versions for the project
             nxqlRequest = DocumentService.NXQL_SELECT_FROM + Subproject.DOCTYPE + DocumentService.NXQL_WHERE_NO_PROXY
                     + DocumentService.NXQL_AND + "spnode:subproject STARTSWITH '" + project.getPathAsString() + "'"
                     + DocumentService.NXQL_AND + DocumentService.NXQL_IS_VERSIONED
                    + " ORDER BY dc:title ASC, major_version DESC, minor_version DESC";
             DocumentModelList versionList = session.query(nxqlRequest);
             List<DocumentModel> versions = new ArrayList<DocumentModel>();
             for(DocumentModel version : versionList){
                 versions.add(version);
             }
             
             liveAndVersions.put("versions", versions);
             
             // Pass it to the view for display
             projectIdToSubproject.put((String)project.getPropertyValue(Project.XPATH_TITLE), liveAndVersions);
         }
         
         // Pass projects map in the view
         view.arg("projectIdToSubproject", projectIdToSubproject)
             .arg("subprojectId", subprojectId)
             .arg("visibility", visibility)
             .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
         
         return view;
     }
     
 }
