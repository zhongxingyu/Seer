 // 
 // Copyright (c) 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 // 
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 // 
 // * Redistributions of source code must retain the above copyright notice,  
 //       this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 //       this list of conditions and the following disclaimer in the documentation  
 //       and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 //       nor the names of its contributors may be used to endorse or promote products  
 //       derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 // 
 
 package net.cyklotron.cms.ngodatabase;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 
 import net.cyklotron.bazy.organizations.OrganizationResource;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.codehaus.jackson.annotate.JsonSubTypes;
 import org.codehaus.jackson.annotate.JsonSubTypes.Type;
 import org.codehaus.jackson.annotate.JsonTypeInfo;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.MapperConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.PropertyNamingStrategy;
 import org.codehaus.jackson.map.introspect.AnnotatedField;
 import org.codehaus.jackson.map.introspect.AnnotatedMethod;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.ValueRequiredException;
 
 import com.fasterxml.jackson.dataformat.xml.XmlMapper;
 import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
 import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
 
 public class BazyngoMessageListener
     implements MessageListener, ExceptionListener
 {
     private final Logger logger;
 
     private final NgoDatabaseServiceImpl ngoDatabaseServiceImpl;
 
     private final CoralSessionFactory coralSessionFactory;
 
     public BazyngoMessageListener(Logger logger, NgoDatabaseServiceImpl ngoDatabaseServiceImpl,
         CoralSessionFactory coralSessionFactory)
         throws Exception
     {
         this.logger = logger;
         this.ngoDatabaseServiceImpl = ngoDatabaseServiceImpl;
         this.coralSessionFactory = coralSessionFactory;
     }
 
     @Override
     public void onMessage(Message message)
     {
         if(message instanceof TextMessage)
         {
             CoralSession coralSession = coralSessionFactory.getRootSession();
             TextMessage textMessage = (TextMessage)message;
             try
             {
                 BazyngoMessage bazyngoMessage = getMessageObject(textMessage);
                 
                 if(bazyngoMessage != null)
                 {
                     if(bazyngoMessage instanceof CategoriesTreeBazyngoMessage)
                     {
                         // do your job.
                         logger.info("get category tree message: " + bazyngoMessage.toString());
 
                     }
                     else if(bazyngoMessage instanceof OrganizationBazyngoMessage)
                     {
                         // do your job.
                         logger.info("get organization message: " + bazyngoMessage.toString());
                         OrganizationBazyngoMessage organizationMessage = (OrganizationBazyngoMessage)bazyngoMessage;
                         OrganizationResource organization = ngoDatabaseServiceImpl
                            .getOrganizationResource(coralSession, organizationMessage.getexternalIdentifier());
                         if(organization != null)
                         {
                             synchronized(organization)
                             {
                                 organization.setOrganizationName(organizationMessage.getOrganizationName());
                                 organization.setData(organizationMessage.getData());
                                 organization.update();
                                 logger.info("organization resource updated: "
                                     + organization.getIdString());
                             }
                         }
                     }
                     else
                     {
                         logger.info("get message: " + bazyngoMessage.toString());
                     }
                 }
             }
             catch(JMSException ex)
             {
                 logger.error("Error reading message: " + ex);
             }
             catch(JsonParseException ex)
             {
                 logger.error("Error reading message: " + ex);
             }
             catch(JsonMappingException ex)
             {
                 logger.error("Error reading message: " + ex);
             }
             catch(IOException ex)
             {
                 logger.error("Error reading message: " + ex);
             }
             catch(InvalidResourceNameException ex)
             {
                 logger.error("Error reading message: " + ex);
             }
             catch(ValueRequiredException ex)
             {
                 logger.error("Error reading message: " + ex);
             }
             finally
             {
                 if(coralSession != null)
                 {
                     coralSession.close();
                 }
             }
         }
         else
         {
             logger.info("Received: " + message);
         }
     }
 
     @Override
     public void onException(JMSException e)
     {
         logger.error("JMS Exception: ", e);
     }
 
     /**
      * Converts json message data to Cyklotron CiviCrmMessage object
      * 
      * @param textMessage
      * @return CiviCrmMessage
      * @throws JsonParseException
      * @throws JsonMappingException
      * @throws IOException
      * @throws JMSException
      */
     public BazyngoMessage getPolymorphicMessageObject(TextMessage textMessage)
         throws JsonParseException, JsonMappingException, IOException, JMSException
     {
         ObjectMapper mapper = new ObjectMapper();
         mapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());
         mapper.getDeserializationConfig().addMixInAnnotations(BazyngoMessage.class,
             PolymorphicBazyngoMessageMixIn.class);
         mapper.getSerializationConfig().addMixInAnnotations(BazyngoMessage.class,
             PolymorphicBazyngoMessageMixIn.class);
         return mapper.readValue(textMessage.getText(), BazyngoMessage.class);
     }
 
     /**
      * Converts json message data to Cyklotron OrganizationBazyngoMessage object
      * 
      * @param textMessage
      * @return CiviCrmMessage
      * @throws JsonParseException
      * @throws JsonMappingException
      * @throws IOException
      * @throws JMSException
      */
     public BazyngoMessage getMessageObject(TextMessage textMessage)
         throws JsonParseException, JsonMappingException, IOException, JMSException
     {
         ObjectMapper mapper = new ObjectMapper();
         mapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());
 
         // JSON2XML
         Map<String,Object> mapMessage = mapper.readValue(textMessage.getText(), Map.class);        
         XmlMapper xmlMapper = new XmlMapper();
         xmlMapper.configure( ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true );
         String messageMXL = xmlMapper.writeValueAsString(mapMessage);
         
         // Cast to bazyngoMessage
         BazyngoMessage bazyngoMessage = mapper.readValue(textMessage.getText(), OrganizationBazyngoMessage.class);
         bazyngoMessage.setData(messageMXL);
         
         logger.info("message JSON2XML: " + bazyngoMessage.getData());
         
         return bazyngoMessage;
     }
     
     
     /**  
      * Converts standard CamelCase field and method names to   
      * typical JSON field names having all lower case characters   
      * with an underscore separating different words.  For   
      * example, all of the following are converted to JSON field   
      * name "some_name":  
      *   
      * Java field name "someName"    
      * Java method name "getSomeName"  
      * Java method name "setSomeName"  
      *   
      * Typical Use:  
      *  
      * String jsonString = "{\"foo_name\":\"fubar\"}";  
      * ObjectMapper mapper = new ObjectMapper();  
      * mapper.setPropertyNamingStrategy(  
      *     new CamelCaseNamingStrategy());  
      * Foo foo = mapper.readValue(jsonString, Foo.class);  
      * System.out.println(mapper.writeValueAsString(foo));  
      * // prints {"foo_name":"fubar"}
      *   
      * class Foo  
      * {  
      *   private String fooName;  
      *   public String getFooName() {return fooName;}  
      *   public void setFooName(String fooName)   
      *   {this.fooName = fooName;}  
      * }  
      */ 
     public class CamelCaseNamingStrategy
         extends PropertyNamingStrategy
     {
         @Override
         public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method,
             String defaultName)
         {
             return translate(defaultName);
         }
 
         @Override
         public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method,
             String defaultName)
         {
             return translate(defaultName);
         }
 
         @Override
         public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
         {
             return translate(defaultName);
         }
 
         private String translate(String defaultName)
         {
             char[] nameChars = defaultName.toCharArray();
             StringBuilder nameTranslated = new StringBuilder(nameChars.length * 2);
             for(char c : nameChars)
             {
                 if(Character.isUpperCase(c))
                 {
                     nameTranslated.append("_");
                     c = Character.toLowerCase(c);
                 }
                 nameTranslated.append(c);
             }
             return nameTranslated.toString();
         }
     }
 
     @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "message_type")
     @JsonSubTypes({ @Type(value = CategoriesTreeBazyngoMessage.class, name = "categories_tree"),
                     @Type(value = OrganizationBazyngoMessage.class, name = "organization") })
     public abstract static class PolymorphicBazyngoMessageMixIn
     {
 
     }
 
     /**
      * Abstract BazyngoMessage 
      * OrganizationBazyngoMessage JSON 
      * CategoriesTreeBazyngoMessage JSON {"type":"categories_tree", "data":"categories tree"}
      * 
      * @author lukasz
      */
     @JsonIgnoreProperties(ignoreUnknown = true)
     public abstract static class BazyngoMessage
     {
         @JsonProperty
         private String messageType;
 
         @JsonProperty
         private String data;
 
         public String getMessageType()
         {
             return messageType;
         }
 
         public void setMessageType(String messageType)
         {
             this.messageType = messageType;
         }
 
         public String getData()
         {
             return data;
         }
 
         public void setData(String data)
         {
             this.data = data;
         }
         
     }
 
     public static class CategoriesTreeBazyngoMessage
         extends BazyngoMessage
     {
 
     }
     
     @JacksonXmlRootElement( localName = "organization", namespace = "" )
     public static class OrganizationBazyngoMessage
         extends BazyngoMessage
     {
         @JsonProperty("external_identifier")
         private String externalIdentifier;
 
         @JsonProperty("organization_name")
         private String organizationName;
 
        public String getexternalIdentifier()
         {
             return externalIdentifier;
         }
 
        public void setId(String externalIdentifier)
         {
             this.externalIdentifier = externalIdentifier;
         }
 
         public String getOrganizationName()
         {
             return organizationName;
         }
 
         public void setName(String organizationName)
         {
             this.organizationName = organizationName;
         }
     }
 
 }
