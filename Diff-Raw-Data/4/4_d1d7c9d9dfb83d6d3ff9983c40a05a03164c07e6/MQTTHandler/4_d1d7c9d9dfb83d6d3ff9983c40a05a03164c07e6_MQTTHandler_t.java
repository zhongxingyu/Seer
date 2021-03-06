 package org.dna.mqtt.moquette.server;
 
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.dna.mqtt.moquette.messaging.spi.IMessaging;
import org.dna.mqtt.moquette.proto.Utils;
 import org.dna.mqtt.moquette.proto.messages.AbstractMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.dna.mqtt.moquette.proto.messages.AbstractMessage.*;
 
 /**
  * MINA MQTT Handler used to route messages to protocol logic
  *
  * @author andrea
  */
 public class MQTTHandler extends IoHandlerAdapter {
 
     private static final Logger LOG = LoggerFactory.getLogger(MQTTHandler.class);
     private IMessaging m_messaging;
 
     @Override
     public void messageReceived(IoSession session, Object message) throws Exception {
         AbstractMessage msg = (AbstractMessage) message;
        LOG.info(String.format("Received a message of type %s", Utils.msgType2String(msg.getMessageType())));
         try {
             switch (msg.getMessageType()) {
                 case CONNECT:
                 case SUBSCRIBE:
                 case UNSUBSCRIBE:
                 case PUBLISH:
                 case PINGREQ:
                 case DISCONNECT:
                     m_messaging.handleProtocolMessage(session, msg);
                     break;
             }
         } catch (Exception ex) {
             LOG.error("Bad error in processing the message", ex);
         }
     }
 
     @Override
     public void sessionIdle(IoSession session, IdleStatus status) {
         session.close(false);
     }
 
     public void setMessaging(IMessaging messaging) {
         m_messaging = messaging;
     }
 
 }
