 package org.sonar.plugins.xmpp.channel;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.api.notifications.Notification;
 import org.sonar.plugins.xmpp.config.IncompleteXmppConfigurationException;
 import org.sonar.plugins.xmpp.config.ServerXmppConfiguration;
 import org.sonar.plugins.xmpp.config.UserXmppConfiguration;
 import org.sonar.plugins.xmpp.config.XmppConfigurationFinder;
 import org.sonar.plugins.xmpp.gateway.XmppGateway;
 import org.sonar.plugins.xmpp.gateway.XmppGatewayFactory;
 import org.sonar.plugins.xmpp.message.XmppMessageContent;
 import org.sonar.plugins.xmpp.message.XmppMessageFactory;
 
 import static org.mockito.Mockito.*;
 
 public class XmppNotificationChannelTest {
 
     public static final String USER = "user";
     XmppConfigurationFinder configFinder;
     XmppMessageFactory messageFactory;
     XmppGatewayFactory gatewayFactory;
     Notification notification;
     ServerXmppConfiguration serverConfiguration;
     XmppGateway gateway;
     XmppMessageContent message;
 
     @Before
     public void init() {
         configFinder = mock(XmppConfigurationFinder.class);
         messageFactory = mock(XmppMessageFactory.class);
         gatewayFactory = mock(XmppGatewayFactory.class);
 
         notification = mock(Notification.class);
         gateway = mock(XmppGateway.class);
         message = mock(XmppMessageContent.class);
     }
 
     @Test
     public void shouldNotSendAnythingIfAddressNotConfiguredForUser() throws IncompleteXmppConfigurationException {
 
         when(configFinder.getUserConfiguration(anyString())).thenThrow(new IncompleteXmppConfigurationException("msg"));
 
         XmppNotificationChannel channel = new XmppNotificationChannel(configFinder, messageFactory, gatewayFactory);
         channel.deliver(notification, USER);
 
         verify(configFinder, times(1)).getUserConfiguration(USER);
         verify(messageFactory, never()).create(any(Notification.class));
         verify(gatewayFactory, never()).create(any(ServerXmppConfiguration.class));
     }
 
     @Test
     public void shouldSendOneMessageForUserWithConfiguredAddress() throws IncompleteXmppConfigurationException {
         UserXmppConfiguration configuration = new UserXmppConfiguration("receiver@qwe.qwe");
 
         when(configFinder.getUserConfiguration(anyString())).thenReturn(configuration);
         serverConfiguration = new ServerXmppConfiguration("sonar", "sonar123", "jabber.com");
         when(configFinder.getServerConfiguration()).thenReturn(serverConfiguration);
         when(gatewayFactory.create(serverConfiguration)).thenReturn(gateway);
         when(messageFactory.create(notification)).thenReturn(message);
         XmppNotificationChannel channel = new XmppNotificationChannel(configFinder, messageFactory, gatewayFactory);
         channel.deliver(notification, USER);
 
         verify(configFinder, times(1)).getUserConfiguration(USER);
         verify(messageFactory, times(1)).create(notification);
         verify(gatewayFactory, times(1)).create(serverConfiguration);
         verify(gateway, times(1)).send(configuration, message);
     }
 
 
 }
