 package com.dottydingo.hyperion.service.endpoint;
 
 import com.dottydingo.hyperion.api.ApiObject;
 import com.dottydingo.hyperion.exception.AuthorizationException;
 import com.dottydingo.hyperion.exception.BadRequestException;
 import com.dottydingo.hyperion.exception.HyperionException;
 import com.dottydingo.hyperion.exception.NotFoundException;
 import com.dottydingo.hyperion.service.context.RequestContext;
 import com.dottydingo.hyperion.service.context.RequestContextBuilder;
 import com.dottydingo.hyperion.service.marshall.EndpointMarshaller;
 import com.dottydingo.hyperion.service.persistence.QueryResult;
 import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
 import com.dottydingo.hyperion.service.configuration.EntityPlugin;
 import com.dottydingo.hyperion.service.configuration.ServiceRegistry;
 
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
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
     private RequestContextBuilder requestContextBuilder;
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
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             requestContext = buildRequestContext(entity,fields,HttpMethod.GET,
                     plugin.getApiVersionRegistry().getPluginForVersion(version));
 
             QueryResult<C> queryResult = plugin.getPersistenceOperations().query(query, start, limit, sort, requestContext);
 
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
 
             requestContext = buildRequestContext(entity,fields,HttpMethod.GET,
                     plugin.getApiVersionRegistry().getPluginForVersion(version));
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             List<ID> ids = plugin.getKeyConverter().covertKeys(id);
             List<C> converted = plugin.getPersistenceOperations().findByIds(ids, requestContext);
 
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
             requestContext = buildRequestContext(entity,fields,HttpMethod.GET,apiVersionPlugin);
 
             endpointAuthorizationChecker.checkAuthorization(requestContext);
 
             C clientObject = endpointMarshaller.unmarshall(httpServletRequest,apiVersionPlugin.getApiClass());
             Set<String> fieldSet = requestContext.getRequestedFields();
             if(fieldSet != null)
                 fieldSet.add("id");
 
             C saved = plugin.getPersistenceOperations().createItem(clientObject, requestContext);
             if(saved != null)
             {
                 httpServletResponse.setStatus(201);
                 httpServletResponse.setHeader("Location",URI.create(saved.getId().toString()).toString());
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
             requestContext = buildRequestContext(entity,fields,HttpMethod.GET,apiVersionPlugin);
 
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
             checkMethodAllowed(plugin,HttpMethod.DELETE);
            requestContext = buildRequestContext(entity,null,HttpMethod.GET,null);
 
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
                                                  ApiVersionPlugin apiVersionPlugin)
     {
         return requestContextBuilder.buildRequestContext(uriInfo,httpServletRequest,httpServletResponse,
                 entity,fields,apiVersionPlugin,method);
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
