 /*
  * Copyright 2012-2013 The Open Source Electronic Health Record Agent
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.osehra.vista.camel.rpc.service;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.logging.InternalLoggerFactory;
 import org.jboss.netty.logging.Slf4JLoggerFactory;
 
 
 public class VistaRequestTracer extends VistaServerSupport {
     public static void main(String... args) {
         new VistaRequestTracer().run();
     }
 
     @Override
     protected void startInternal() throws RuntimeException {
         super.startInternal();
 
         try {
             startRpcListener();
         } catch (Exception e) {
             LOG.error("Failed to start RPC server", e);
             throw new RuntimeException("Failed to start RPC server", e);
         }
     }
 
     protected void startRpcListener() throws Exception {
         InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
 
         ServerBootstrap bootstrap = new ServerBootstrap(
             new NioServerSocketChannelFactory(
                 Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool()));
 
         bootstrap.setPipelineFactory(new TracerPipelineFactory());
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
 
         // Bind and start to accept incoming connections.
         // TODO: make port configurable
        bootstrap.bind(new InetSocketAddress(9220));
     }
 
 }
