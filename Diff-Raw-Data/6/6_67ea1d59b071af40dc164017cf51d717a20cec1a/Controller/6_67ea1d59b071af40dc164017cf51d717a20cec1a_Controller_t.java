 /*
  * Copyright (c) 2011, Sho SHIMIZU
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.galibier.controller.core;
 
 import org.galibier.controller.event.MessageListener;
 import org.galibier.controller.event.SwitchListener;
 import org.galibier.netty.OpenFlowServerPipelineFactory;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.openflow.protocol.OFMessage;
 import org.openflow.protocol.OFType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.*;
 
 public class Controller {
     private static final Logger log = LoggerFactory.getLogger(Controller.class);
     private static int DEFAULT_PORT = 6633;
 
     private final int portNumber;
     private ChannelFactory factory;
     private Channel channel;
     private Map<OFType, List<MessageListener>> messageListeners =
             new ConcurrentHashMap<OFType, List<MessageListener>>();
     private Set<SwitchListener> switchListeners =
             new CopyOnWriteArraySet<SwitchListener>();
 
     public Controller() {
         this(DEFAULT_PORT);
     }
 
     public int getDefaultPortNumber() {
         return DEFAULT_PORT;
     }
 
     public Controller(int portNumber) {
         this.portNumber = portNumber;
     }
 
     public void bind() {
         factory = new NioServerSocketChannelFactory(
                 Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
         ServerBootstrap bootstrap = new ServerBootstrap(factory);
 
         bootstrap.setPipelineFactory(new OpenFlowServerPipelineFactory(this));
         bootstrap.setOption("reuseAddress", true);
 
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
 
         channel = bootstrap.bind(new InetSocketAddress(this.portNumber));
         log.info("Controller started: {}", channel.getLocalAddress());
     }
 
     public synchronized void addMessageListener(OFType type, MessageListener listener) {
         List<MessageListener> listeners;
         if (messageListeners.containsKey(type)) {
             listeners = messageListeners.get(type);
         } else {
             listeners = new CopyOnWriteArrayList<MessageListener>();
         }
         listeners.add(listener);
         messageListeners.put(type, listeners);
     }
 
     public synchronized void removeMessageListener(OFType type, MessageListener listener) {
         List<MessageListener> oldListeners = messageListeners.get(type);
        if (oldListeners.size() > 0) {
            oldListeners.remove(listener);
        } else {
            messageListeners.remove(type);
        }
     }
 
     public void addSwitch(Switch client) {
         for (SwitchListener listener: switchListeners) {
             listener.switchConnected(client);
         }
     }
 
     public void removeSwitch(Switch client) {
         for (SwitchListener listener: switchListeners) {
             listener.switchDisconnected(client);
         }
     }
 
     public void invokeMessageListener(Switch client, OFMessage message) {
         List<MessageListener> listeners = messageListeners.get(message.getType());
         
         if (listeners == null) {
             return;
         }
 
         for (MessageListener listener: listeners) {
             listener.receive(client, message);
         }
     }
 }
