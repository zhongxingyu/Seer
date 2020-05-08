 package com.dottydingo.hyperion.service.endpoint;
 
 import com.dottydingo.hyperion.api.ApiObject;
 import com.dottydingo.hyperion.exception.AuthorizationException;
 import com.dottydingo.hyperion.exception.BadRequestException;
 import com.dottydingo.hyperion.exception.HyperionException;
 import com.dottydingo.hyperion.exception.NotFoundException;
 import com.dottydingo.hyperion.service.context.DefaultRequestContextBuilder;
 import com.dottydingo.hyperion.service.context.RequestContext;
 import com.dottydingo.hyperion.service.context.RequestContextBuilder;
 import com.dottydingo.hyperion.service.context.WriteContext;
 import com.dottydingo.hyperion.service.marshall.EndpointMarshaller;
 import com.dottydingo.hyperion.service.model.PersistentObject;
 import com.dottydingo.hyperion.service.persistence.QueryResult;
 import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
 import com.dottydingo.hyperion.service.configuration.EntityPlugin;
 import com.dottydingo.hyperion.service.configuration.ServiceRegistry;
 
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriInfo;
 import java.io.Serializable;
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 
 /**
  */
 public class BaseDataServiceEndpoint<C extends ApiObject,ID extends Serializable>
 {
     private ServiceRegistry serviceRegistry;
     private RequestContextBuilder requestContextBuilder = new DefaultRequestContextBuilder();
     private EndpointAuthorizationChecker endpointAuthorizationChecker = new EmptyAuthorizationChecker();
     private EndpointMarshaller endpointMarshaller = new EndpointMarshaller();
     private EndpointExceptionHandler endpointExceptionHandler = new DefaultEndpointExceptionHandler();
 
     @Context
     protected UriInfo uriInfo;
     @Context
     protected HttpServletRequest httpServletRequest;
     @Context
     protected HttpServletResponse httpServletResponse;
 
 
     public void setServiceRegistry(ServiceRegistry serviceRegistry)
     {
         this.serviceRegistry = serviceRegistry;
     }
 
     public void setRequestContextBuilder(RequestContextBuilder requestContextBuilder)
     {
         this.requestContextBuilder = requestContextBuilder;
     }
 
     public void setEndpointAuthorizationChecker(EndpointAuthorizationChecker endpointAuthorizationChecker)
     {
         this.endpointAuthorizationChecker = endpointAuthorizationChecker;
     }
 
     public void setEndpointMarshaller(EndpointMarshaller endpointMarshaller)
     {
         this.endpointMarshaller = endpointMarshaller;
     }
 
     public void setEndpointExceptionHandler(EndpointExceptionHandler endpointExceptionHandler)
     {
         this.endpointExceptionHandler = endpointExceptionHandler;
     }
 
     @GET()
     public void queryData(@PathParam("entity") String entity,
                               @QueryParam("fields") String fields,
                               @QueryParam("start")  Integer start,
                               @QueryParam("limit")  Integer limit,
                               @QueryParam("query") String query,
                               @QueryParam("sort") String sort,
                               @QueryParam("version")  Integer version)
     {
         RequestContext requestContext = null;
 
         try
         {
             EntityPlugin<C,?,ID> plugin = getEntityPlugin(entity);
             checkMethodAllowed(plugin,HttpMethod.GET);
 
             if(start != null && start < 1)
                 throw new BadRequestException("The start parameter must be greater than zero.");
 
             if(limit != null && limit < 1)
                 throw new BadRequestException("The limit parameter must be greater than zero.");
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             ApiVersionPlugin<C, ? extends PersistentObject> versionPlugin =
                     plugin.getApiVersionRegistry().getPluginForVersion(version);
 
             requestContext = buildRequestContext(entity,fields,HttpMethod.GET, plugin, versionPlugin);
 
             QueryResult<C> queryResult = plugin.getPersistenceOperations().query(query, start, limit, sort, requestContext);
 
             addVersionHeader(versionPlugin.getVersion());
             EntityResponse<C> entityResponse = new EntityResponse<C>();
             entityResponse.setEntries(queryResult.getItems());
             entityResponse.setResponseCount(queryResult.getResponseCount());
             entityResponse.setStart(start);
             entityResponse.setTotalCount(queryResult.getTotalCount());
 
             httpServletResponse.setStatus(200);
             endpointMarshaller.marshall(httpServletResponse,entityResponse);
 
         }
         catch (Throwable t)
         {
             endpointExceptionHandler.handleException(t,endpointMarshaller,httpServletResponse);
         }
 
     }
 
     @POST()
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public void queryDataPost(@PathParam("entity") String entity,
                           @FormParam("fields") String fields,
                           @FormParam("start")  Integer start,
                           @FormParam("limit")  Integer limit,
                           @FormParam("query") String query,
                           @FormParam("sort") String sort,
                           @FormParam("version")  Integer version)
     {
         queryData(entity, fields, start, limit, query, sort, version);
     }
 
     @GET()
     @Path("{id}")
     public void getItem(@PathParam("entity") String entity,
                             @PathParam("id") String id,
                             @QueryParam("fields") String fields,
                             @QueryParam("version")  Integer version)
 
     {
         RequestContext requestContext = null;
 
         try
         {
             EntityPlugin<C,?,ID> plugin = getEntityPlugin(entity);
             checkMethodAllowed(plugin,HttpMethod.GET);
 
             ApiVersionPlugin<C, ? extends PersistentObject> versionPlugin =
                     plugin.getApiVersionRegistry().getPluginForVersion(version);
             requestContext = buildRequestContext(entity,fields,HttpMethod.GET,
                     plugin, versionPlugin);
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             List<ID> ids = plugin.getKeyConverter().covertKeys(id);
             List<C> converted = plugin.getPersistenceOperations().findByIds(ids, requestContext);
 
             addVersionHeader(versionPlugin.getVersion());
             EntityResponse<C> entityResponse = new EntityResponse<C>();
             entityResponse.setEntries(converted);
             entityResponse.setResponseCount(converted.size());
             entityResponse.setStart(1);
             entityResponse.setTotalCount(new Long(converted.size()));
 
             httpServletResponse.setStatus(200);
             endpointMarshaller.marshall(httpServletResponse,entityResponse);
 
         }
         catch (Throwable t)
         {
             endpointExceptionHandler.handleException(t,endpointMarshaller,httpServletResponse);
         }
 
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     public void createItem(@PathParam("entity") String entity,
                                @QueryParam("fields") String fields,
                                @QueryParam("version")  Integer version                               )
     {
         RequestContext requestContext = null;
         try
         {
             EntityPlugin<C,?,ID> plugin = getEntityPlugin(entity);
             checkMethodAllowed(plugin,HttpMethod.POST);
             ApiVersionPlugin<C,?> apiVersionPlugin = plugin.getApiVersionRegistry().getPluginForVersion(version);
            requestContext = buildRequestContext(entity,fields,HttpMethod.GET, plugin, apiVersionPlugin);
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             C clientObject = endpointMarshaller.unmarshall(httpServletRequest,apiVersionPlugin.getApiClass());
             Set<String> fieldSet = requestContext.getRequestedFields();
             if(fieldSet != null)
                 fieldSet.add("id");
 
             C saved = plugin.getPersistenceOperations().createOrUpdateItem(clientObject, requestContext);
             if(saved != null)
             {
                 addVersionHeader(apiVersionPlugin.getVersion());
                 if(requestContext.getWriteContext() == WriteContext.create)
                 {
                     httpServletResponse.setStatus(201);
                     httpServletResponse.setHeader("Location",URI.create(saved.getId().toString()).toString());
                 }
                 else
                 {
                     httpServletResponse.setStatus(200);
                 }
                 endpointMarshaller.marshall(httpServletResponse,saved);
             }
             else
                 httpServletResponse.setStatus(304);
 
 
         }
         catch (Throwable t)
         {
             endpointExceptionHandler.handleException(t,endpointMarshaller,httpServletResponse);
         }
 
     }
 
     @PUT
     @Path("{id}")
     public void updateItem(@PathParam("entity") String entity,
                            @PathParam("id") String id,
                                @QueryParam("fields") String fields,
                                @QueryParam("version")  Integer version)
     {
         RequestContext requestContext = null;
         try
         {
             EntityPlugin<C,?,ID> plugin = getEntityPlugin(entity);
             checkMethodAllowed(plugin,HttpMethod.PUT);
             ApiVersionPlugin<C,?> apiVersionPlugin = plugin.getApiVersionRegistry().getPluginForVersion(version);
            requestContext = buildRequestContext(entity,fields,HttpMethod.GET, plugin, apiVersionPlugin);
 
             C clientObject = endpointMarshaller.unmarshall(httpServletRequest,apiVersionPlugin.getApiClass());
 
             List<ID> ids = plugin.getKeyConverter().covertKeys(id);
             if(ids.size() != 1)
                 throw new BadRequestException("A single id must be provided for an update.");
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             Set<String> fieldSet = requestContext.getRequestedFields();
             if(fieldSet != null)
                 fieldSet.add("id");
 
             C saved = plugin.getPersistenceOperations().updateItem(ids, clientObject, requestContext);
             if(saved != null)
             {
                 addVersionHeader(apiVersionPlugin.getVersion());
                 httpServletResponse.setStatus(200);
                 endpointMarshaller.marshall(httpServletResponse,saved);
             }else
                 httpServletResponse.setStatus(304);
 
         }
         catch (Throwable t)
         {
             endpointExceptionHandler.handleException(t,endpointMarshaller,httpServletResponse);
         }
     }
 
     @DELETE()
     @Path("{id}")
     public void deleteItem(@PathParam("entity") String entity,
                             @PathParam("id") String id)
     {
         RequestContext requestContext = null;
 
         try
         {
             EntityPlugin<C,?,ID> plugin = getEntityPlugin(entity);
             ApiVersionPlugin<C,?> apiVersionPlugin = plugin.getApiVersionRegistry().getPluginForVersion(null);
             checkMethodAllowed(plugin,HttpMethod.DELETE);
            requestContext = buildRequestContext(entity,null,HttpMethod.GET, plugin, apiVersionPlugin);
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
             List<ID> ids = plugin.getKeyConverter().covertKeys(id);
             int deleted = plugin.getPersistenceOperations().deleteItem(ids, requestContext);
 
             DeleteResponse response = new DeleteResponse();
             response.setCount(deleted);
 
             httpServletResponse.setStatus(200);
             endpointMarshaller.marshall(httpServletResponse,response);
 
 
         }
         catch (Throwable t)
         {
             endpointExceptionHandler.handleException(t,endpointMarshaller,httpServletResponse);
         }
 
     }
 
 
     protected RequestContext buildRequestContext(String entity, String fields, HttpMethod method,
                                                  EntityPlugin entityPlugin, ApiVersionPlugin apiVersionPlugin)
     {
         return requestContextBuilder.buildRequestContext(uriInfo,httpServletRequest,httpServletResponse,
                 entity,fields, entityPlugin, apiVersionPlugin,method);
     }
 
     private void addVersionHeader(Integer version)
     {
         httpServletResponse.addHeader("HyperionEntityVersion",version.toString());
     }
 
     private EntityPlugin<C,?,ID> getEntityPlugin(String entity)
     {
         EntityPlugin<C,?,ID> plugin = serviceRegistry.getPluginForName(entity);
         if(plugin == null)
             throw new NotFoundException(String.format("%s is not a valid entity.",entity));
 
         return plugin;
     }
 
     private void checkMethodAllowed(EntityPlugin plugin, HttpMethod httpMethod)
     {
         if(!plugin.isMethodAllowed(httpMethod))
             throw new HyperionException(405,String.format("%s is not allowed.",httpMethod));
     }
 
 
     private class EmptyAuthorizationChecker implements EndpointAuthorizationChecker
     {
         @Override
         public void checkAuthorization(RequestContext requestContext) throws AuthorizationException {}
     }
 }
