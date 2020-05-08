 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.messaging;
 
 import org.apache.servicemix.jbi.jaxp.BytesSource;
 import org.apache.servicemix.jbi.jaxp.ResourceSource;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 
 import javax.activation.DataHandler;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.security.auth.Subject;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamSource;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Represents a JBI NormalizedMessage.
  *
  * @version $Revision$
  */
 public class NormalizedMessageImpl implements NormalizedMessage, Externalizable {
     
     private static final long serialVersionUID = 9179194301410526549L;
     
     protected transient MessageExchangeImpl exchange;
    private transient Source content;
    private Subject securitySubject;
    private Map properties;
    private Map attachments;
 
     private static SourceTransformer transformer = new SourceTransformer();
 
     /**
      * Constructor
      *
      */
     public NormalizedMessageImpl() {
     }
 
 
     /**
      * Constructor
      * @param exchange
      */
     public NormalizedMessageImpl(MessageExchangeImpl exchange) {
         this.exchange = exchange;
     }
 
     
 
 
     /**
      * @return the content of the message
      */
     public synchronized Source getContent() {
         return content;
     }
 
     /**
      * set the content fo the message
      *
      * @param source
      */
     public synchronized void setContent(Source source) {
         this.content = source;
     }
 
     /**
      * @return the security subject from the message
      */
     public synchronized Subject getSecuritySubject() {
         return securitySubject;
     }
 
     /**
      * set the security subject
      *
      * @param securitySubject
      */
     public synchronized void setSecuritySubject(Subject securitySubject) {
         this.securitySubject = securitySubject;
     }
 
     /**
      * get a named property
      *
      * @param name
      * @return a property from the message
      */
     public synchronized Object getProperty(String name) {
         if (properties != null) {
             return properties.get(name);
         }
         return null;
     }
 
     /**
      * @return an iterator of property names
      */
     public synchronized Set getPropertyNames() {
         if (properties != null) {
             return Collections.unmodifiableSet(properties.keySet());
         }
         return Collections.EMPTY_SET;
     }
 
     /**
      * set a property
      *
      * @param name
      * @param value
      */
     public synchronized void setProperty(String name, Object value) {
         if (value == null) {
             if (properties != null) {
                 properties.remove(name);
             }
         } else {
             getProperties().put(name, value);
         }
     }
 
     /**
      * Add an attachment
      *
      * @param id
      * @param content
      */
     public synchronized void addAttachment(String id, DataHandler content) {
         getAttachments().put(id, content);
     }
 
     /**
      * Get a named attachement
      *
      * @param id
      * @return the specified attachment
      */
     public synchronized DataHandler getAttachment(String id) {
         if (attachments != null) {
             return (DataHandler) attachments.get(id);
         }
         return null;
     }
 
     /**
      * @return a list of identifiers for atachments
      */
     public synchronized Iterator listAttachments() {
         if (attachments != null) {
             return attachments.keySet().iterator();
         }
         return Collections.EMPTY_LIST.iterator();
     }
 
     /**
      * remove an identified attachment
      *
      * @param id
      */
     public synchronized void removeAttachment(String id) {
         if (attachments != null) {
             attachments.remove(id);
         }
     }
     
     /** Returns a list of identifiers for each attachment to the message.
      *  @return iterator over String attachment identifiers
      */
     public Set getAttachmentNames(){
         if (attachments != null){
             return Collections.unmodifiableSet(attachments.keySet());
         }
         return Collections.EMPTY_SET;
     }
 
 
     public String toString() {
         return super.toString() + "{properties: " + getProperties() + "}";
     }
     
     // Scripting helper methods to add expressive power
     // when using languages like Groovy, Velocity etc
     //-------------------------------------------------------------------------
 
     public Object getBody() throws MessagingException {
         return getMarshaler().unmarshal(exchange, this);
     }
 
     public void setBody(Object body) throws MessagingException {
         getMarshaler().marshal(exchange, this, body);
     }
 
     public String getBodyText() throws TransformerException {
         return transformer.toString(getContent());
     }
 
     public void setBodyText(String xml) {
         setContent(new StringSource(xml));
     }
 
     public  PojoMarshaler getMarshaler() {
         return exchange.getMarshaler();
     }
 
 
     // Implementation methods
     //-------------------------------------------------------------------------
     protected Map getProperties() {
         if (properties == null) {
             properties = createPropertiesMap();
         }
         return properties;
     }
 
     protected Map getAttachments() {
         if (attachments == null) {
             attachments = createAttachmentsMap();
         }
         return attachments;
     }
 
     protected void setAttachments(Map attachments) {
         this.attachments = attachments;
     }
 
     protected void setProperties(Map properties) {
         this.properties = properties;
     }
 
     protected Map createPropertiesMap() {
         // Normalized exchanges do not need to be thread-safe
         return new HashMap();
     }
 
     protected Map createAttachmentsMap() {
         // Normalized exchanges do not need to be thread-safe
         return new HashMap();
     }
 
     /**
      * Write to a Stream
      * @param out
      * @throws IOException
      */
     public void writeExternal(ObjectOutput out) throws IOException {
         try {
             out.writeObject(attachments);
             out.writeObject(properties);
             String src = transformer.toString(content);
             out.writeObject(src);
             // We have read the source
             // so now, ensure that it can be re-read
             if ((content instanceof StreamSource ||
                     content instanceof SAXSource) &&
                     !(content instanceof StringSource) &&
                     !(content instanceof BytesSource) &&
                     !(content instanceof ResourceSource)) {
                 content = new StringSource(src); 
             }
         } catch (TransformerException e) {
             throw (IOException) new IOException("Could not transform content to string").initCause(e);
         }
     }
 
     /**
      * Read from a stream
      * 
      * @param in
      * @throws IOException
      * @throws ClassNotFoundException
      */
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
         attachments = (Map) in.readObject();
         properties = (Map) in.readObject();
         String src = (String) in.readObject();
         if (src != null) {
             content = new StringSource(src);
         }
     }
 
 }
 
