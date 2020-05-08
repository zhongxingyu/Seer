 package es.uc3m.inf.kr.oslcrm.services;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.eclipse.lyo.core.query.ParseException;
 import org.eclipse.lyo.core.query.Properties;
 import org.eclipse.lyo.core.query.QueryUtils;
 import org.eclipse.lyo.oslc4j.core.OSLC4JConstants;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcDialog;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcDialogs;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
 import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
 import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
 
 import es.uc3m.inf.kr.common.utils.ApplicationContextLocator;
 import es.uc3m.inf.kr.dao.VocabularyDAO;
 import es.uc3m.inf.kr.oslcrm.Constants;
 import es.uc3m.inf.kr.oslcrm.appserv.RequirementApplicationService;
 import es.uc3m.inf.kr.oslcrm.to.Person;
 import es.uc3m.inf.kr.oslcrm.to.RequirementTO;
 import es.uc3m.inf.kr.oslcrm.to.VocabularyTO;
 
 @OslcService(Constants.REQUIREMENTS_MANAGEMENT_DOMAIN)
@Path("rm")
 public class RequirementManagementService {
 
 	@Context private HttpServletRequest httpServletRequest;
 	@Context private HttpServletResponse httpServletResponse;
 	@Context private UriInfo uriInfo;
 	RequirementApplicationService appServ;
 
 	public RequirementManagementService(){
 		super();
 		this.appServ = new RequirementApplicationService();
 
 	}
 
 	@OslcDialogs(
 			{
 				@OslcDialog
 				(
 						title = "Requirement Management Selection Dialog",
 						label = "Requirement Management Selection Dialog",
						uri = "/rm/selector",
 						hintWidth = "525px",
 						hintHeight = "325px",
 						resourceTypes = {Constants.TYPE_REQUIREMENT_REQUEST},
 						usages = {OslcConstants.OSLC_USAGE_DEFAULT}
 						)
 
 			})
 	@OslcQueryCapability
 	(
 			title = "Requirement Management Query Capability",
 			label = "Requirement Management Catalog Query",
 			resourceShape = OslcConstants.PATH_RESOURCE_SHAPES + "/" + Constants.PATH_VOCABULARY_REQUEST,
 			resourceTypes = {Constants.TYPE_REQUIREMENT_REQUEST},
 			usages = {OslcConstants.OSLC_USAGE_DEFAULT}
 			)
 
 	@GET
 	@Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
 	public List<RequirementTO> getRequirements() throws IOException, ServletException {
 		return this.appServ.getRequirements();
 
 
 	}
 	
 
 
 	@GET
 	@Path("get/{requirementID}")
 	@Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
 	public RequirementTO getRequirement(@PathParam("requirementID") final String requirementId) throws IOException, ServletException {
 		 return this.appServ.getRequirement(requirementId);
 	}
 	
 
 
     @GET
     @Path("query")
     @Produces({OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON})
     public List<RequirementTO> getRequirementsByQuery       (@PathParam("requirementId")         final String requirementId,
     		                                 	     	 @QueryParam("oslc.where")       final String where,
     		                                 		     @QueryParam("oslc.select")      final String select,
     		                                 		     @QueryParam("oslc.prefix")      final String prefix,
     		                                             @QueryParam("page")             final String pageString,
     		                                             @QueryParam("oslc.orderBy")     final String orderBy,
     		                                             @QueryParam("oslc.searchTerms") final String searchTerms,
     		                                             @QueryParam("oslc.paging")      final String paging,
     		                                             @QueryParam("oslc.pageSize")    final String pageSize) throws IOException, ServletException
     {
         boolean isPaging = false;
         
         if (paging != null) {
             isPaging = Boolean.parseBoolean(paging);
         }
         
     	int page=0;
     	
         if (null != pageString) {
             page = Integer.parseInt(pageString);
         }
         
     	int limit=10;
     	
     	if (isPaging && pageSize != null) {
     	    limit = Integer.parseInt(pageSize);
     	}
     	
     	Map<String, String> prefixMap;
     	
         try {
             prefixMap = QueryUtils.parsePrefixes(prefix);
         } catch (ParseException e) {
            throw new IOException(e);
         }
         
         addDefaultPrefixes(prefixMap);
         
         Properties properties;
         
         if (select == null) {
             properties = QueryUtils.WILDCARD_PROPERTY_LIST;
         } else {
             try {
                 properties = QueryUtils.parseSelect(select, prefixMap);
             } catch (ParseException e) {
                 throw new IOException(e);
             }
         }
         
         Map<String, Object> propMap =
             QueryUtils.invertSelectedProperties(properties);
         
         List<RequirementTO> results;
 		results = this.appServ.getRequirements();
 
         
         Object nextPageAttr = httpServletRequest.getAttribute(Constants.NEXT_PAGE);
         
         if (! isPaging && nextPageAttr != null) {
             
             String location = 
                 uriInfo.getBaseUri().toString() + uriInfo.getPath() + '?' +
                 (where != null ? ("oslc.where=" + URLEncoder.encode(where, "UTF-8") + '&') : "") +
                 (select != null ? ("oslc.select=" + URLEncoder.encode(select, "UTF-8") + '&') : "") +
                 (prefix != null ? ("oslc.prefix=" + URLEncoder.encode(prefix, "UTF-8") + '&') : "") +
                 (orderBy != null ? ("oslc.orderBy=" + URLEncoder.encode(orderBy, "UTF-8") + '&') : "") +
                 (searchTerms != null ? ("oslc.searchTerms=" + URLEncoder.encode(searchTerms, "UTF-8") + '&') : "") +
                 "oslc.paging=true&oslc.pageSize=" + limit;
                 
             try {
                 throw new WebApplicationException(Response.temporaryRedirect(new URI(location)).build());
             } catch (URISyntaxException e) {
                 // XXX - Can't happen
                 throw new IllegalStateException(e);
             }
         }
         
         httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_SELECTED_PROPERTIES,
                                         propMap);
         
         if (nextPageAttr != null) {
             
             String location = 
                 uriInfo.getBaseUri().toString() + uriInfo.getPath() + '?' +
                 (where != null ? ("oslc.where=" + URLEncoder.encode(where, "UTF-8") + '&') : "") +
                 (select != null ? ("oslc.select=" + URLEncoder.encode(select, "UTF-8") + '&') : "") +
                 (prefix != null ? ("oslc.prefix=" + URLEncoder.encode(prefix, "UTF-8") + '&') : "") +
                 (orderBy != null ? ("oslc.orderBy=" + URLEncoder.encode(orderBy, "UTF-8") + '&') : "") +
                 (searchTerms != null ? ("oslc.searchTerms=" + URLEncoder.encode(searchTerms, "UTF-8") + '&') : "") +
                 "oslc.paging=true&oslc.pageSize=" + limit + "&page=" + nextPageAttr;
                 
             httpServletRequest.setAttribute(OSLC4JConstants.OSLC4J_NEXT_PAGE,
                                             location);
 
         }
 
         return results;
     }
 
     private static void addDefaultPrefixes(final Map<String, String> prefixMap)  {
         //recursivelyCollectNamespaceMappings(prefixMap, BugzillaChangeRequest.class);
     }  
     
     
 
 }
