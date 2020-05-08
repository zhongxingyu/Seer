 /*
  * Copyright (C) 2012  Pauli Kauppinen
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package org.javnce.vnc.server;
 
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.javnce.eventing.Event;
 import org.javnce.eventing.EventLoop;
 import org.javnce.eventing.EventSubscriber;
 import org.javnce.rfb.messages.*;
 import org.javnce.rfb.types.*;
 import org.javnce.vnc.common.FbEncodingsEvent;
 import org.javnce.vnc.common.FbFormatEvent;
 import org.javnce.vnc.common.FbRequestEvent;
 import org.javnce.vnc.common.FbUpdateEvent;
 import org.javnce.vnc.common.KeyEvent;
 import org.javnce.vnc.common.MessageDispatcher;
 import org.javnce.vnc.common.PointerEvent;
 import org.javnce.vnc.common.ReceiveMessageFactory;
 import org.javnce.vnc.common.ReceivedMsgEvent;
 import org.javnce.vnc.server.platform.FramebufferDevice;
 import org.javnce.vnc.server.platform.PlatformController;
 
 /**
  * The Class for handling communication with client.
  */
 class ServerProtocolHandler implements EventSubscriber, ReceiveMessageFactory {
 
     /**
      * The Constant version.
      */
     static final private Version version = new Version(3, 8);
     /**
      * The message handler.
      */
     final private MessageDispatcher messageHandler;
     /**
      * The server name.
      */
     static final private String name = "JaVNCe - a Java VNC";
     /**
      * The format.
      */
     private PixelFormat format;
     /**
      * The size.
      */
     private Size size;
     /**
      * The event loop.
      */
     final private EventLoop eventLoop;
     /**
      * The receive message list.
      */
     final private ArrayList<Message> receiveMessages;
     /**
      * The factory mode.
      */
     private boolean factoryMode;
 
     /**
      * Instantiates a new server protocol handler.
      *
      * @param eventLoop the event loop
      * @param channel the channel
      */
     ServerProtocolHandler(EventLoop eventLoop, SocketChannel channel) {
         this.eventLoop = eventLoop;
         FramebufferDevice dev = PlatformController.instance().getPlatformManager().getFramebufferDevice();
 
         this.format = dev.format();
         this.size = dev.size();
         receiveMessages = new ArrayList<>();
         receiveMessages.add(new MsgProtocolVersion());
         factoryMode = false;
 
         eventLoop.subscribe(ReceivedMsgEvent.eventId(), this);
         messageHandler = new MessageDispatcher(eventLoop, channel, this);
         eventLoop.publish(new ReceivedMsgEvent(null));
     }
 
     /* (non-Javadoc)
      * @see org.javnce.eventing.EventSubscriber#event(org.javnce.eventing.Event)
      */
     @Override
     public void event(Event event) {
         if (ReceivedMsgEvent.eventId().equals(event.Id())) {
             process(((ReceivedMsgEvent) event).get());
         } else if (FbUpdateEvent.eventId().equals(event.Id())) {
             event((FbUpdateEvent) event);
         } else {
             EventLoop.fatalError(this, new UnsupportedOperationException("Unsubscribed event " + event.getClass().getName()));
         }
     }
 
     /**
      * Framebuffer change event handler.
      *
      * @param event the event
      */
     private void event(FbUpdateEvent event) {
         Framebuffer[] fb = event.get();
         messageHandler.send(new MsgFramebufferUpdate(format, fb));
     }
 
     /**
      * Client message processing method..
      *
      * @param msg the client message
      */
     private void process(Message msg) {
         if (null == msg) {
             messageHandler.send(new MsgProtocolVersion(version));
         } else {
             switch (msg.getId()) {
 
                 case ProtocolVersion:
                     handle((MsgProtocolVersion) msg);
                     break;
                 case SelectedSecurityType:
                     handle((MsgSelectedSecurityType) msg);
                     break;
                 case SecurityResult:
                     error("Got a SecurityResult message");
                     break;
                 case ClientInit:
                     handle((MsgClientInit) msg);
                     break;
                 case ServerInit:
                     error("Got a ServerInit message");
                     break;
                 case SetEncodings:
                     handle((MsgSetEncodings) msg);
                     break;
                 case SetPixelFormat:
                     handle((MsgSetPixelFormat) msg);
                     break;
                 case FramebufferUpdateRequest:
                     handle((MsgFramebufferUpdateRequest) msg);
                     break;
                 case KeyEvent:
                     handle((MsgKeyEvent) msg);
                     break;
                 case PointerEvent:
                     handle((MsgPointerEvent) msg);
                     break;
                 case Factory:
                     process(((MsgServerFactory) msg).get());
                     break;
                 case Unknown:
                     error("Unknown message " + msg);
                     break;
                 default:
                     error("Unsupported message " + msg);
                     break;
             }
         }
     }
 
     /**
      * Protocol error handler.
      *
      * @param text the text
      */
     private void error(String text) {
         Logger logger = Logger.getLogger(this.getClass().getName());
         logger.warning(text);
         messageHandler.close();
     }
 
     /**
      * ProtocolVersion message handler.
      *
      * @param msg the message
      */
     private void handle(MsgProtocolVersion msg) {
         if (version.equals(msg.get())) {
             messageHandler.send(new MsgSecurityTypeList(new SecurityType[]{SecurityType.None}));
            receiveMessages.add(new MsgSelectedSecurityType());
         } else {
             messageHandler.send(new MsgSecurityTypeList("Protocol version " + msg.get() + " not supported"));
             error("Protocol version " + msg.get() + " not supported");
         }
     }
 
     /**
      * SecurityType message handler.
      *
      * @param msg the message
      */
     private void handle(MsgSelectedSecurityType msg) {
         SecurityType type = msg.get();
 
         if (SecurityType.None == type) {
             messageHandler.send(new MsgSecurityResult(true));
            receiveMessages.add(new MsgClientInit());
         } else {
             messageHandler.send(new MsgSecurityResult("Security type " + type + " not supported"));
             error("Security type " + type + " not supported");
         }
     }
 
     /**
      * End of handshaking handler.
      */
     private void endHandshake() {
         eventLoop.subscribe(FbUpdateEvent.eventId(), this);
         factoryMode = true;
     }
 
     /**
      * ClientInit message handler.
      *
      * @param msg the message
      */
     private void handle(MsgClientInit msg) {
         endHandshake();
         messageHandler.send(new MsgServerInit(format, size, name));
     }
 
     /**
      * SetEncodings message handler.
      *
      * @param msg the message
      */
     private void handle(MsgSetEncodings msg) {
         eventLoop.publish(new FbEncodingsEvent(msg.get()));
     }
 
     /**
      * SetPixelFormat message handler.
      *
      * @param msg the message
      */
     private void handle(MsgSetPixelFormat msg) {
         eventLoop.publish(new FbFormatEvent(msg.get()));
     }
 
     /**
      * FramebufferUpdateRequest message handler.
      *
      * @param msg the message
      */
     private void handle(MsgFramebufferUpdateRequest msg) {
         eventLoop.publish(new FbRequestEvent(msg.getIncremental(), msg.getRect()));
     }
 
     /**
      * KeyEvent message handler
      *
      * @param msg the message
      */
     private void handle(MsgKeyEvent msg) {
         eventLoop.publish(new KeyEvent(msg.getDown(), msg.getKey()));
     }
 
     /**
      * PointerEvent message handler
      *
      * @param msg the message
      */
     private void handle(MsgPointerEvent msg) {
         eventLoop.publish(new PointerEvent(msg.getMask(), msg.getPoint()));
     }
 
     @Override
     public Message nextReceiveMessage() {
 
         Message msg = null;
         if (factoryMode) {
             msg = new MsgServerFactory();
         } else if (!receiveMessages.isEmpty()) {
             msg = receiveMessages.remove(0);
         }
         return msg;
     }
 }
