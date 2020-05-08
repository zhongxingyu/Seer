 package org.sonar.plugins.xmpp.channel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.notifications.Notification;
 import org.sonar.api.notifications.NotificationChannel;
 import org.sonar.plugins.xmpp.config.IncompleteXmppConfigurationException;
 import org.sonar.plugins.xmpp.config.ServerXmppConfiguration;
 import org.sonar.plugins.xmpp.config.UserXmppConfiguration;
 import org.sonar.plugins.xmpp.config.XmppConfigurationFinder;
 import org.sonar.plugins.xmpp.gateway.XmppGateway;
 import org.sonar.plugins.xmpp.gateway.XmppGatewayFactory;
 import org.sonar.plugins.xmpp.message.XmppMessageContent;
 import org.sonar.plugins.xmpp.message.XmppMessageFactory;
 
 public class XmppNotificationChannel extends NotificationChannel {
     private static final Logger LOG = LoggerFactory.getLogger(XmppNotificationChannel.class);
 
     private XmppConfigurationFinder configurationFinder;
     private XmppMessageFactory messageFactory;
     private XmppGatewayFactory gatewayFactory;
 
     public XmppNotificationChannel(XmppConfigurationFinder configurationFinder, XmppMessageFactory messageFactory, XmppGatewayFactory gatewayFactory) {
         this.configurationFinder = configurationFinder;
         this.messageFactory = messageFactory;
         this.gatewayFactory = gatewayFactory;
     }
 
     @Override
     public void deliver(Notification notification, String userName) {
 
         UserXmppConfiguration userConfiguration;
         ServerXmppConfiguration serverConfiguration;
         try {
             userConfiguration = configurationFinder.getUserConfiguration(userName);
             serverConfiguration = configurationFinder.getServerConfiguration();
         } catch (IncompleteXmppConfigurationException exception) {
             LOG.debug("Could not send XMPP notification", exception);
             return;
         }
         LOG.info("XMPP notification for  " + userName);
         sendNotification(notification, serverConfiguration, userConfiguration);

     }
 
     private void sendNotification(Notification notification, ServerXmppConfiguration serverConfiguration, UserXmppConfiguration userConfiguration) {
         XmppGateway gateway = gatewayFactory.create(serverConfiguration);
         XmppMessageContent message = messageFactory.create(notification);
         gateway.send(userConfiguration, message);
        gateway.close();
     }
 
 }
