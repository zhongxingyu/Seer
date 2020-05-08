 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.framework;
 
 import java.beans.PropertyChangeListener;
 import java.net.URI;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanOperationInfo;
 import javax.xml.namespace.QName;
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.client.ServiceMixClient;
 import org.apache.servicemix.jbi.FaultException;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.MBeanInfoProvider;
 import org.apache.servicemix.jbi.management.OperationInfoHelper;
 import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
 import org.apache.servicemix.jbi.servicedesc.ExternalEndpoint;
 import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
 import org.apache.servicemix.jbi.servicedesc.LinkedEndpoint;
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.apache.servicemix.jbi.util.QNameUtil;
 
 import sun.security.action.GetLongAction;
 
 public class Endpoint implements EndpointMBean, MBeanInfoProvider {
 
     private static final Log log = LogFactory.getLog(Endpoint.class);
     
     private AbstractServiceEndpoint endpoint;
     private Registry registry;
     
     public Endpoint(AbstractServiceEndpoint endpoint, Registry registry) {
         this.endpoint = endpoint;
         this.registry = registry;
     }
 
     public String getEndpointName() {
         return endpoint.getEndpointName();
     }
 
     public QName[] getInterfaces() {
         return endpoint.getInterfaces();
     }
 
     public QName getServiceName() {
         return endpoint.getServiceName();
     }
     
     public String getReference() {
         try {
             return DOMUtil.asIndentedXML(endpoint.getAsReference(null));
         } catch (TransformerException e) {
             return null;
         }
     }
     
     public String getWSDL() {
         try {
             return DOMUtil.asXML(registry.getEndpointDescriptor(endpoint));
         } catch (Exception e) {
             return null;
         }
     }
 
     public String getComponentName() {
         if (endpoint.getComponentNameSpace() != null) {
             return endpoint.getComponentNameSpace().getName();
         } else {
             return null;
         }
     }
 
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "endpointName", "name of the endpoint");
         helper.addAttribute(getObjectToManage(), "serviceName", "name of the service");
         helper.addAttribute(getObjectToManage(), "componentName", "component name of the service unit");
         helper.addAttribute(getObjectToManage(), "interfaces", "interfaces implemented by this endpoint");
         return helper.getAttributeInfos();
     }
 
     public MBeanOperationInfo[] getOperationInfos() throws JMException {
         OperationInfoHelper helper = new OperationInfoHelper();
         helper.addOperation(getObjectToManage(), "getReference", "retrieve the endpoint reference");
         helper.addOperation(getObjectToManage(), "getWSDL", "retrieve the wsdl description of this endpoint");
         helper.addOperation(getObjectToManage(), "send", "send a simple message exchange to test this endpoint");
         return helper.getOperationInfos();
     }
 
     public Object getObjectToManage() {
         return this;
     }
 
     public String getName() {
         return endpoint.getServiceName() + endpoint.getEndpointName();
     }
 
     public String getType() {
         return "Endpoint";
     }
 
     public String getSubType() {
         if (endpoint instanceof InternalEndpoint) {
             return "Internal";
         } else if (endpoint instanceof LinkedEndpoint) {
             return "Linked";
         } else if (endpoint instanceof ExternalEndpoint) {
             return "External";
         }
         return null;
     }
 
     public String getDescription() {
         return null;
     }
 
     public void setPropertyChangeListener(PropertyChangeListener l) {
     }
 
     protected AbstractServiceEndpoint getEndpoint() {
         return endpoint;
     }
 
     public String send(String content, String operation, String mep) {
         ServiceMixClient client = null;
         try {
             client = registry.getContainer().getClientFactory().createClient();
             MessageExchange me = client.getExchangeFactory().createExchange(URI.create(mep));
             NormalizedMessage nm = me.createMessage();
             me.setMessage(nm, "in");
             nm.setContent(new StringSource(content));
             me.setEndpoint(endpoint);
             if (operation != null) {
                 me.setOperation(QNameUtil.parse(operation));
             }
             client.sendSync(me);
             if (me.getError() != null) {
                 throw me.getError();
             } else if (me.getFault() != null) {
                 throw FaultException.newInstance(me);
             } else if (me.getMessage("out") != null) {
                 return new SourceTransformer().contentToString(me.getMessage("out"));
             }
             return null;
         } catch (Exception e) {
            log.debug("Error proces test exchange", e);
             throw new RuntimeException(e);
         } finally {
             if (client != null) {
                 try {
                     client.close();
                 } catch (Exception e) {
                     // ignore
                 }
             }
         }
     }
 
 }
