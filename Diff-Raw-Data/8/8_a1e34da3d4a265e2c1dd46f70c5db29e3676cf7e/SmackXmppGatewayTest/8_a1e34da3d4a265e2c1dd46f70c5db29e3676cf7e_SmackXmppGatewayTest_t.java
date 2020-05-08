 package org.sonar.plugins.xmpp.gateway.smack;
 
 import org.jivesoftware.smack.*;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.sonar.plugins.xmpp.config.ServerXmppConfiguration;
 import org.sonar.plugins.xmpp.config.UserXmppConfiguration;
 import org.sonar.plugins.xmpp.gateway.XmppConnectionException;
 import org.sonar.plugins.xmpp.message.XmppMessage;
 
 import static org.mockito.Mockito.*;
 
 public class SmackXmppGatewayTest {
 
     private static final String AUTHENTICATION_EXCEPTION = "Authentication exception";
     private static final String SENDING_EXCEPTION = "Sending exception";
     private static final String CONNECTION_EXCEPTION = "Connection exception";
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
 
     @Test
     public void throwsExceptionOnConnectionError() throws XMPPException {
 
         XMPPConnection connection = mock(XMPPConnection.class);
         doThrow(new XMPPException(CONNECTION_EXCEPTION)).when(connection).connect();
 
         ServerXmppConfiguration configuration = mock(ServerXmppConfiguration.class);
         thrown.expect(XmppConnectionException.class);
         thrown.expectMessage(CONNECTION_EXCEPTION);
 
         UserXmppConfiguration userConfiguration = mock(UserXmppConfiguration.class);
         when(userConfiguration.getAddress()).thenReturn("receiver@server.com");
         XmppMessage message = mock(XmppMessage.class);
 
         new SmackXmppGateway(connection, configuration).send(userConfiguration, message);
 
         verify(connection).disconnect();
     }
 
     @Test
     public void throwsExceptionOnAuthenticationError() throws XMPPException {
 
         XMPPConnection connection = mock(XMPPConnection.class);
        doThrow(new XMPPException(AUTHENTICATION_EXCEPTION)).when(connection).login(anyString(), anyString(), anyString());
 
         ServerXmppConfiguration configuration = mock(ServerXmppConfiguration.class);
        when(configuration.getUserName()).thenReturn("user");
        when(configuration.getPassword()).thenReturn("password");
         thrown.expect(XmppConnectionException.class);
         thrown.expectMessage(AUTHENTICATION_EXCEPTION);
 
         UserXmppConfiguration userConfiguration = mock(UserXmppConfiguration.class);
         when(userConfiguration.getAddress()).thenReturn("receiver@server.com");
         XmppMessage message = mock(XmppMessage.class);
 
         new SmackXmppGateway(connection, configuration).send(userConfiguration, message);
 
         verify(connection).disconnect();
     }
 
     @Test
     public void throwsExceptionOnSendingError() throws XMPPException {
 
         XMPPConnection connection = mock(XMPPConnection.class);
         ChatManager chatManager = mock(ChatManager.class);
         when(connection.getChatManager()).thenReturn(chatManager);
         Chat chat = mock(Chat.class);
         when(chatManager.createChat(anyString(), any(MessageListener.class))).thenReturn(chat);
         doThrow(new XMPPException(SENDING_EXCEPTION)).when(chat).sendMessage(anyString());
         ServerXmppConfiguration configuration = mock(ServerXmppConfiguration.class);
         thrown.expect(XmppConnectionException.class);
         thrown.expectMessage(SENDING_EXCEPTION);
 
         XmppMessage message = mock(XmppMessage.class);
         when(message.getText()).thenReturn("text");
         UserXmppConfiguration userConfiguration = mock(UserXmppConfiguration.class);
         when(userConfiguration.getAddress()).thenReturn("receiver@server.com");
 
         new SmackXmppGateway(connection, configuration).send(userConfiguration, message);
        verify(connection).disconnect();
     }
 
     @Test
     public void shouldSendCorrectMessage() throws XMPPException {
 
         XMPPConnection connection = mock(XMPPConnection.class);
         ChatManager chatManager = mock(ChatManager.class);
         when(connection.getChatManager()).thenReturn(chatManager);
         Chat chat = mock(Chat.class);
         when(chatManager.createChat(anyString(), any(MessageListener.class))).thenReturn(chat);
         ServerXmppConfiguration configuration = mock(ServerXmppConfiguration.class);
 
         XmppMessage message = mock(XmppMessage.class);
         when(message.getText()).thenReturn("text");
         UserXmppConfiguration userConfiguration = mock(UserXmppConfiguration.class);
         when(userConfiguration.getAddress()).thenReturn("receiver@server.com");
 
         new SmackXmppGateway(connection, configuration).send(userConfiguration, message);
 
         verify(chat).sendMessage("text");
         verify(connection).disconnect();
     }
 
 }
