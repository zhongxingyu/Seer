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
 package org.apache.servicemix.jbi.audit;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.MessageExchange;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanOperationInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.event.ExchangeListener;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.BaseSystemService;
 import org.apache.servicemix.jbi.management.OperationInfoHelper;
 import org.apache.servicemix.jbi.management.ParameterHelper;
 
 /**
  * Base class for ServiceMix auditors implementations.
  * 
  * @author Guillaume Nodet (gnt)
  * @since 2.1
  * @version $Revision$
  */
 public abstract class AbstractAuditor extends BaseSystemService implements AuditorMBean, ExchangeListener {
 
     protected final Log log = LogFactory.getLog(getClass());
     
     private boolean asContainerListener = true;
 
     public JBIContainer getContainer() {
         return container;
     }
 
     public void setContainer(JBIContainer container) {
         this.container = container;
     }
     
     protected Class getServiceMBean() {
         return AuditorMBean.class;
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.management.LifeCycleMBean#start()
      */
     public void start() throws javax.jbi.JBIException {
         super.start();
         doStart();
         if (isAsContainerListener())
         	this.container.addListener(this);
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.management.LifeCycleMBean#stop()
      */
     public void stop() throws javax.jbi.JBIException {
         this.container.removeListener(this);
         doStop();
         super.stop();
     }
     
     protected void doStart() throws JBIException {
     }
 
     protected void doStop() throws JBIException {
     }
 
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.management.MBeanInfoProvider#getAttributeInfos()
      */
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         // TODO: this should not be an attribute, as it can require access to database
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "exchangeCount", "number of exchanges");
         return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.management.MBeanInfoProvider#getOperationInfos()
      */
     public MBeanOperationInfo[] getOperationInfos() throws JMException {
         // TODO: add other operations infos
         OperationInfoHelper helper = new OperationInfoHelper();
         ParameterHelper ph = helper.addOperation(getObjectToManage(), "getExchanges", 2, "retrieve a bunch messages");
         ph.setDescription(0, "fromIndex", "lower index of message (start from 0)");
         ph.setDescription(1, "toIndex", "upper index of message (exclusive, > fromIndex)");
         ph = helper.addOperation(getObjectToManage(), "getExchange", 1, "retrieve an exchange given its id");
         ph.setDescription(0, "id", "id of the exchange to retrieve");
         return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeCount()
      */
     public abstract int getExchangeCount() throws AuditorException;
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeId(int)
      */
     public String getExchangeId(int index) throws AuditorException {
         if (index < 0) {
             throw new IllegalArgumentException("index should be greater or equal to zero");
         }
         return getExchangeIds(index, index + 1)[0];
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeIds()
      */
     public String[] getExchangeIds() throws AuditorException {
         return getExchangeIds(0, getExchangeCount());
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeIds(int, int)
      */
     public abstract String[] getExchangeIds(int fromIndex, int toIndex)  throws AuditorException;
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchange(int)
      */
     public MessageExchange getExchange(int index) throws AuditorException {
         if (index < 0) {
             throw new IllegalArgumentException("index should be greater or equal to zero");
         }
         return getExchanges(index, index + 1)[0];
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchange(java.lang.String)
      */
     public MessageExchange getExchange(String id) throws AuditorException {
         if (id == null || id.length() == 0) {
             throw new IllegalArgumentException("id should be non null and non empty");
         }
         return getExchanges(new String[] { id })[0];
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges()
      */
     public MessageExchange[] getExchanges() throws AuditorException {
         return getExchanges(0, getExchangeCount());
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges(int, int)
      */
     public MessageExchange[] getExchanges(int fromIndex, int toIndex) throws AuditorException {
         return getExchanges(getExchangeIds(fromIndex, toIndex));
     }
 
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges(java.lang.String[])
      */
     public abstract MessageExchange[] getExchanges(String[] ids) throws AuditorException;
 
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges()
      */
     public int deleteExchanges() throws AuditorException {
         return deleteExchanges(0, getExchangeCount());
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchange(int)
      */
     public boolean deleteExchange(int index) throws AuditorException {
         if (index < 0) {
             throw new IllegalArgumentException("index should be greater or equal to zero");
         }
         return deleteExchanges(index, index + 1) == 1;
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchange(java.lang.String)
      */
     public boolean deleteExchange(String id) throws AuditorException {
         return deleteExchanges(new String[] { id }) == 1;
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges(int, int)
      */
     public int deleteExchanges(int fromIndex, int toIndex) throws AuditorException {
         return deleteExchanges(getExchangeIds(fromIndex, toIndex));
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges(java.lang.String[])
      */
     public abstract int deleteExchanges(String[] ids) throws AuditorException;
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.jbi.audit.AuditorMBean#resendExchange(javax.jbi.messaging.MessageExchange)
      */
     public void resendExchange(MessageExchange exchange) throws JBIException {
         container.resendExchange(exchange);
     }
 
 	/**
      * Test if Auditor should be included as a container listener
      * 
      * @return Returns the addToContainer.
      */
     public boolean isAsContainerListener() {
         return asContainerListener;
     }
 
     /**
      * Set if Auditor should be included as a container listener.
      * 
      * @param addToContainer
      *            The addToContainer to set.
      */
     public void setAsContainerListener(boolean addToContainer) {
         this.asContainerListener = addToContainer;
     }
 }
