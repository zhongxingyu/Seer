 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.ws.notification.consumer;
 
 import java.security.cert.X509Certificate;
 import javax.xml.ws.BindingProvider;
 import net.lin_k.safe_online.notification.consumer.*;
 import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType.Message;
 import net.link.safeonline.sdk.api.exception.WSClientTransportException;
 import net.link.safeonline.sdk.api.ws.notification.consumer.client.NotificationConsumerClient;
 import net.link.safeonline.ws.notification.NotificationConsumerServiceFactory;
 import net.link.util.ws.AbstractWSClient;
 import net.link.util.ws.security.WSSecurityConfiguration;
 import net.link.util.ws.security.WSSecurityHandler;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
 
 
 /**
  * Implementation of the WS-Notification consumer interface. This class is using JAX-WS, secured via WS-Security and server-side SSL.
  *
  * @author wvdhaute
  */
 public class NotificationConsumerClientImpl extends AbstractWSClient<NotificationConsumerPort> implements NotificationConsumerClient {
 
     private static final Log LOG = LogFactory.getLog( NotificationConsumerClientImpl.class );
 
     private static final String TOPIC_DIALECT_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";
 
     private final String location;
 
     /**
      * Main constructor.
      *
      * @param location       the location (host:port) of the attribute web service.
      * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
      * @param configuration  The WS-Security configuration.
      */
     public NotificationConsumerClientImpl(String location, X509Certificate sslCertificate, WSSecurityConfiguration configuration) {
 
         super( NotificationConsumerServiceFactory.newInstance().getNotificationConsumerPort(), sslCertificate );
         getBindingProvider().getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = location );
 
         WSSecurityHandler.install( getBindingProvider(), configuration );
     }
 
     @Override
     public void sendNotification(String topic, String destination, String subject, String content)
             throws WSClientTransportException {
 
         LOG.debug( "send notification to " + location + " for topic: " + topic + ", destination:" + destination + ", subject:" + subject
                    + ", content:" + content );
 
         TopicExpressionType topicExpression = new TopicExpressionType();
         topicExpression.setDialect( TOPIC_DIALECT_SIMPLE );
         topicExpression.getContent().add( topic );
 
         Notify notifications = new Notify();
         NotificationMessageHolderType notification = new NotificationMessageHolderType();
         notification.setTopic( topicExpression );
         Message notificationMessage = new Message();
         notificationMessage.setDestination( destination );
         notificationMessage.setSubject( subject );
         notificationMessage.setContent( content );
         notification.setMessage( notificationMessage );
         notifications.getNotificationMessage().add( notification );
 
         try {
             getPort().notify( notifications );
         }
        catch (Exception e) {
             LOG.debug( "Failed to send notification" );
             throw new WSClientTransportException( getBindingProvider(), e );
         }
     }
 }
