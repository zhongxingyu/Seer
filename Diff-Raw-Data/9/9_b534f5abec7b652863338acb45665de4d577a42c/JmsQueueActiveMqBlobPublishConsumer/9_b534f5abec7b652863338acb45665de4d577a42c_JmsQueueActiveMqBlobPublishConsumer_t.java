 /*
  * Copyright LGPL3
  * YES Technology Association
  * http://yestech.org
  *
  * http://www.opensource.org/licenses/lgpl-3.0.html
  */
 
 /*
  *
  * Author:  Artie Copeland
  * Last Modified Date: $DateTime: $
  */
 package org.yestech.publish.service;
 
 import org.apache.activemq.BlobMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yestech.publish.IPublishConstant;
 import org.yestech.publish.objectmodel.IArtifactMetaData;
 
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import java.io.InputStream;
 
 /**
  * @author Artie Copeland
  * @version $Revision: $
  */
public class JmsQueueActiveMqBlobPublishConsumer implements IPublishConsumer, MessageListener {
     final private static Logger logger = LoggerFactory.getLogger(JmsQueueActiveMqBlobPublishConsumer.class);
     private IPublishProcessor processor;
 
     public IPublishProcessor getProcessor() {
         return processor;
     }
 
     public void setProcessor(IPublishProcessor processor) {
         this.processor = processor;
     }
 
     public void onMessage(Message message) {
         if (message instanceof BlobMessage) {
             BlobMessage blobMessage = (BlobMessage) message;
             try {
                 IArtifactMetaData metaData = (IArtifactMetaData) blobMessage.getObjectProperty(IPublishConstant.META_DATA_IDENTIFIER);
                 InputStream artifact = blobMessage.getInputStream();
                 recieve(metaData, artifact);
             } catch (Exception e) {
                 logger.error(e.getMessage(), e);
             }
         }
     }
 
     public void recieve(IArtifactMetaData metaData, InputStream artifact) {
         processor.process(metaData, artifact);
     }
 }
