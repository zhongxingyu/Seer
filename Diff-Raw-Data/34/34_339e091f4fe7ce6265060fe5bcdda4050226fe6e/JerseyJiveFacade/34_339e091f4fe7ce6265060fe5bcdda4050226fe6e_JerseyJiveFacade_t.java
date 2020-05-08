 
 package org.mule.modules.jive;
 
 import org.mule.modules.jive.api.EntityType;
 import org.mule.modules.jive.api.JiveIds;
 import org.mule.modules.jive.api.JiveUris;
 import org.mule.modules.jive.api.xml.XmlMapper;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.WebResource.Builder;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Map;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 
 public class JerseyJiveFacade implements JiveFacade
 {
     /** The userID. */
     private Long userID;
     private String username;
     /** The password. */
     private String password;
     private String gatewayUri;
     private final XmlMapper mapper = new XmlMapper();
     private Client client = createClient();
     private Object initUserId = new Object();
 
 
     @Override
     public final Map<String, Object> get(final EntityType entityType, final String id)
     {
         return entityType.get(id, mapper, getGateway());
     }
 
     @Override
     public final Map<String, Object> execute(final CustomOp op, final Map<String, Object> entity,  final String id)
     {
         final String response;
         final Builder partialRequest = this.getGateway().path(getUri(op, id)).type(
             MediaType.APPLICATION_FORM_URLENCODED).header("content-type", "text/xml");
 
         if (op.getMethod().equals("POST"))
         {
             response = partialRequest.post(String.class, toXml(op, entity));
         }
         else if (op.getMethod().equals("GET"))
         {
             response = partialRequest.get(String.class);
         }
         else if (op.getMethod().equals("PUT"))
         {
             response = partialRequest.put(String.class, toXml(op, entity));
         }
         else 
         { 
             Validate.isTrue(op.getMethod().equals("DELETE"));
             response = partialRequest.delete(String.class);
         }
         return xml2map(new StringReader(response));
     }
 
 
     private String toXml(final CustomOp op, final Map<String, Object> entity)
     {
         final Writer writer = new StringWriter();
         map2xml(op.getRootTagElementName(), entity, writer);
         return writer.toString();
     }
 
     private String getUri(final CustomOp op, final String id)
     {
        return JiveUris.getOperationUri(op.getBaseOperationUri(), id);
     }
 
 
     @Override
     public final Map<String, Object> create(final EntityType type, final Map<String, Object> entity)
     {
         Validate.notNull(type);
         Validate.notNull(entity);
         return type.create(type, entity, mapper, getGateway());
     }
 
     /**
      * @param xmlRootTag
      * @param entity
      * @param writer
      * @see org.mule.modules.jive.api.xml.XmlMapper#map2xml(java.lang.String,
      *      java.util.Map, java.io.Writer)
      */
     public final void map2xml(final String xmlRootTag, final Map<String, Object> entity, final Writer writer)
     {
         mapper.map2xml(xmlRootTag, entity, writer);
     }
 
     /**
      * @param reader
      * @return
      * @see org.mule.module(s.jive.api.xml.XmlMapper#xml2map(java.io.Reader)
      */
     public final Map<String, Object> xml2map(final Reader reader)
     {
         return mapper.xml2map(reader);
     }
 
     @Override
     /**{@inheritDoc}*/
     public final Map<String, Object> delete(final EntityType type, final String id)
     {
         Validate.notNull(type);
         Validate.notEmpty(id);
         return type.delete(type, id, mapper, getGateway());
     }
 
     @Override
     public Map<String, Object> update(EntityType entityType, Map<String, Object> entity)
     {
         Validate.notNull(entity);
         Validate.notNull(entity);
         return entityType.put(entityType, entity, mapper, getGateway());
     }
 
     /**
      * Call the get count service.
      * 
      * @return The count as a {@link Long}
      * @param type The service type
      */
     public final Long count(final EntityType type)
     {
         Validate.notNull(type);
         return type.count(type, mapper, getGateway());
     }
 
     /**
      * Creates the client.
      * 
      * @return The jersey client
      */
     private Client createClient()
     {
         final ClientConfig config = new DefaultClientConfig();
         return Client.create(config);
     }
 
     /**
      * Creates the webresource.
      * 
      * @param gatewayUri The resource uri
      * @param client The jersey client
      * @return 
      */
     private WebResource createGateway(final String gatewayUri)
     {
         Validate.notNull(client, "Client cannot be empty");
         Validate.notEmpty(gatewayUri, "Gateway cannot be empty");
         client.addFilter(new HTTPBasicAuthFilter(this.getUser(), this.getPassword()));
         return client.resource(gatewayUri);
     }
 
     /**
      * @param userID the userID to set
      */
     public final void setUserID(final Long userID)
     {
         this.userID = userID;
     }
 
     /**
      * @return the userID
      */
     public final Long getUserID()
     {
         if (userID == null)
         {
             synchronized (initUserId)
             {
                 if (userID == null)
                 {
                     final String response = getGateway().path("/userService/users/" + this.getUser()).type(
                         MediaType.APPLICATION_FORM_URLENCODED).get(String.class);
                     this.setUserID(Long.parseLong(StringUtils.substringBetween(response, "<ID>", "</ID>")));
                 }
             }
         }
         
         return userID;
     }
 
     /**
      * @param user the user to set
      */
     public final void setUser(final String user)
     {
         this.username = user;
     }
 
     /**
      * @return the user
      */
     public final String getUser()
     {
         return username;
     }
 
     /**
      * @param pass the pass to set
      */
     public final void setPass(final String pass)
     {
         this.password = pass;
     }
 
     /**
      * @return the pass
      */
     public final String getPassword()
     {
         return password;
     }
 
     @Override
     public final void setGatewayUri(final String gatewayUri)
     {
         this.gatewayUri = gatewayUri;
     }
 
     @Override
     public final void setUsername(final String user)
     {
         this.username = user;
     }
 
     @Override
     public final void setPassword(final String pass)
     {
         this.password = pass;
     }
 
     protected WebResource getGateway()
     {
         return createGateway(gatewayUri);
     }
 
 }
