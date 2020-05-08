 package com.dottydingo.hyperion.service.pipeline.phase;
 
 import com.dottydingo.hyperion.exception.BadRequestException;
 import com.dottydingo.hyperion.exception.HyperionException;
 import com.dottydingo.hyperion.exception.NotFoundException;
 import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
 import com.dottydingo.hyperion.service.configuration.EntityPlugin;
 import com.dottydingo.hyperion.service.configuration.ServiceRegistry;
 import com.dottydingo.hyperion.service.endpoint.HttpMethod;
 import com.dottydingo.hyperion.service.pipeline.AuthorizationChecker;
 import com.dottydingo.hyperion.service.pipeline.UriParser;
 import com.dottydingo.hyperion.service.pipeline.UriRequestResult;
 import com.dottydingo.hyperion.service.configuration.HyperionEndpointConfiguration;
 import com.dottydingo.hyperion.service.context.HyperionContext;
 import com.dottydingo.hyperion.service.context.NullUserContextBuilder;
 import com.dottydingo.hyperion.service.context.UserContextBuilder;
 import com.dottydingo.service.endpoint.context.EndpointRequest;
 import com.dottydingo.service.endpoint.pipeline.AbstractEndpointPhase;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  */
 public class EndpointValidationPhase extends AbstractEndpointPhase<HyperionContext>
 {
     private Logger logger = LoggerFactory.getLogger(EndpointValidationPhase.class);
 
     private ServiceRegistry serviceRegistry;
     private HyperionEndpointConfiguration hyperionEndpointConfiguration;
     private AuthorizationChecker<HyperionContext> authorizationChecker;
     private UserContextBuilder<HyperionContext> userContextBuilder = new NullUserContextBuilder();
     private UriParser uriParser = new UriParser();
 
     public void setServiceRegistry(ServiceRegistry serviceRegistry)
     {
         this.serviceRegistry = serviceRegistry;
     }
 
     public void setHyperionEndpointConfiguration(HyperionEndpointConfiguration hyperionEndpointConfiguration)
     {
         this.hyperionEndpointConfiguration = hyperionEndpointConfiguration;
     }
 
    public void setUserContextBuilder(UserContextBuilder<HyperionContext> userContextBuilder)
    {
        this.userContextBuilder = userContextBuilder;
    }

     public void setAuthorizationChecker(AuthorizationChecker<HyperionContext> authorizationChecker)
     {
         this.authorizationChecker = authorizationChecker;
     }
 
     @Override
     protected void executePhase(HyperionContext phaseContext) throws Exception
     {
         EndpointRequest request = phaseContext.getEndpointRequest();
 
         UriRequestResult uriRequestResult = uriParser.parseRequestUri(request.getRequestUri());
 
         if(uriRequestResult == null)
             throw new NotFoundException(String.format("%s is not recognized.",request.getRequestUri()));
 
         String entityName = uriRequestResult.getEndpoint();
 
         EntityPlugin plugin = serviceRegistry.getPluginForName(entityName);
         if(plugin == null)
             throw new NotFoundException(String.format("%s is not a valid entity.",entityName));
 
         phaseContext.setEntityPlugin(plugin);
 
         HttpMethod httpMethod = null;
         String requestMethod = getEffectiveMethod(request);
         try
         {
             httpMethod = HttpMethod.valueOf(requestMethod);
         }
         catch (IllegalArgumentException e)
         {
             throw new HyperionException(405,String.format("%s is not allowed.", requestMethod));
         }
 
         if(!plugin.isMethodAllowed(httpMethod))
             throw new HyperionException(405,String.format("%s is not allowed.",httpMethod));
 
         phaseContext.setHttpMethod(httpMethod);
 
         String version = request.getFirstParameter(hyperionEndpointConfiguration.getVersionParameterName());
         if(version == null || version.length() == 0)
             version = request.getFirstHeader(hyperionEndpointConfiguration.getVersionHeaderName());
 
         if(hyperionEndpointConfiguration.isRequireVersion() && httpMethod != HttpMethod.DELETE &&
                 (version == null || version.length()==0))
             throw new BadRequestException(String.format("The %s parameter must be specified",hyperionEndpointConfiguration.getVersionParameterName()));
 
         if(version != null)
         {
             try
             {
                 phaseContext.setVersion(Integer.parseInt(version));
             }
             catch(NumberFormatException e)
             {
                 throw new BadRequestException(String.format("%s is not a valid value for version.",version));
             }
         }
 
         if(!validateMethod(httpMethod,uriRequestResult))
             throw new HyperionException(405,"Not allowed.");
 
         phaseContext.setId(uriRequestResult.getId());
         phaseContext.setHistory(uriRequestResult.isHistory());
 
         ApiVersionPlugin versionPlugin = plugin.getApiVersionRegistry().getPluginForVersion(phaseContext.getVersion());
         phaseContext.setVersionPlugin(versionPlugin);
 
         if(authorizationChecker != null)
             authorizationChecker.checkAuthorization(phaseContext);
 
         phaseContext.setUserContext(userContextBuilder.buildUserContext(phaseContext));
 
         logRequestInformation(phaseContext);
 
     }
 
     protected void logRequestInformation(HyperionContext context)
     {
         if(!logger.isDebugEnabled())
             return;
 
         EndpointRequest request = context.getEndpointRequest();
 
         logger.debug("Correlation ID: {}",context.getCorrelationId());
         logger.debug("Request URL: {}",request.getRequestUrl());
         logger.debug("Base URL: {}",request.getBaseUrl());
         logger.debug("Request URI: {}",request.getRequestUri());
         logger.debug("Request Query String: {}",request.getQueryString());
         logger.debug("Request Method: {}",request.getRequestMethod());
         logger.debug("Effective Method: {}",context.getHttpMethod());
         logger.debug("Endpoint Name: {}",context.getEntityPlugin().getEndpointName());
         logger.debug("ContentType: {}",request.getContentType());
         logger.debug("User Identifier: {}",context.getUserContext().getUserIdentifier());
         logger.debug("Id: {}",context.getId());
         logger.debug("Audit: {}",context.isHistory());
 
         for (String name : request.getHeaderNames())
         {
             logger.debug("Header name: {} value:{}",name,request.getHeader(name));
         }
 
         for (String name : request.getParameterNames())
         {
             logger.debug("Parameter name: {} value:{}",name,request.getParameter(name));
         }
 
         logger.debug("Request Version: {}",context.getVersion());
     }
 
     protected String getEffectiveMethod(EndpointRequest request)
     {
         if(request.getRequestMethod().equalsIgnoreCase("POST") && request.getContentType() != null &&
                 request.getContentType().contains("application/x-www-form-urlencoded"))
         {
             return "GET";
         }
 
         return request.getRequestMethod();
     }
 
     protected boolean validateMethod(HttpMethod method,UriRequestResult requestResult)
     {
         switch (method)
         {
             case DELETE:
                 return (requestResult.getId() != null && !requestResult.isHistory());
             case POST:
                 return (requestResult.getId() == null && !requestResult.isHistory());
             case PUT:
                 return (!requestResult.isHistory());
             case GET:
                 return (requestResult.isHistory() && requestResult.getId() != null) || !requestResult.isHistory();
             default:
                 return false;
         }
 
     }
 }
