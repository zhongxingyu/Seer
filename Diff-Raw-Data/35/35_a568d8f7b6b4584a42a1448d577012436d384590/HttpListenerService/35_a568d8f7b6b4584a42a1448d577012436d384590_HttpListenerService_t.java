 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.as.undertow.extension;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import io.undertow.server.HttpHandler;
 import io.undertow.server.HttpOpenListener;
 import io.undertow.server.HttpTransferEncodingHandler;
 import io.undertow.server.handlers.CookieHandler;
 import io.undertow.server.handlers.PathHandler;
 import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
 import io.undertow.servlet.api.ServletContainer;
 import org.jboss.as.network.SocketBinding;
 import org.jboss.msc.service.Service;
 import org.jboss.msc.service.StartContext;
 import org.jboss.msc.service.StartException;
 import org.jboss.msc.service.StopContext;
 import org.jboss.msc.value.InjectedValue;
 import org.xnio.BufferAllocator;
 import org.xnio.ByteBufferSlicePool;
 import org.xnio.ChannelListener;
 import org.xnio.ChannelListeners;
 import org.xnio.IoUtils;
 import org.xnio.OptionMap;
 import org.xnio.Options;
 import org.xnio.Xnio;
 import org.xnio.XnioWorker;
 import org.xnio.channels.AcceptingChannel;
 import org.xnio.channels.ConnectedStreamChannel;
 
 /**
  * @author Stuart Douglas
  */
 public class HttpListenerService implements Service<HttpListenerService> {
 
     private final InjectedValue<XnioWorker> worker = new InjectedValue<XnioWorker>();
     private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();
 
     private volatile HttpOpenListener openListener;
     private volatile AcceptingChannel<? extends ConnectedStreamChannel> server;
     private volatile Xnio xnio;
     private volatile PathHandler pathHandler = new PathHandler();
     private volatile ServletContainer servletContainer;
 
 
     public HttpHandler getRootHandler() {
         return openListener.getRootHandler();
     }
 
     public void setRootHandler(HttpHandler handler) {
         openListener.setRootHandler(handler);
     }
 
     public PathHandler getPathHandler() {
         return pathHandler;
     }
 
     public void setPathHandler(final PathHandler pathHandler) {
         this.pathHandler = pathHandler;
     }
 
     @Override
     public void start(final StartContext startContext) throws StartException {
         xnio = Xnio.getInstance("nio", HttpListenerService.class.getClassLoader());
         try {
             OptionMap serverOptions = OptionMap.builder()
                     .set(Options.WORKER_ACCEPT_THREADS, 1)
                     .set(Options.TCP_NODELAY, true)
                     .set(Options.REUSE_ADDRESSES, true)
                     .getMap();
             openListener = new HttpOpenListener(new ByteBufferSlicePool(BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, 8192, 8192 * 8192));
             ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> acceptListener = ChannelListeners.openListenerAdapter(openListener);
             final InetSocketAddress socketAddress = binding.getValue().getSocketAddress();
             server = worker.getValue().createStreamServer(socketAddress, acceptListener, serverOptions);
             server.resumeAccepts();
            SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(new InMemorySessionManager());
            sessionAttachmentHandler.setNext(pathHandler);
             final CookieHandler cookie = new CookieHandler();
            cookie.setNext(new SimpleErrorPageHandler(sessionAttachmentHandler));
             final HttpTransferEncodingHandler transferEncodingHandler = new HttpTransferEncodingHandler(cookie);
             openListener.setRootHandler(transferEncodingHandler);
             servletContainer  = ServletContainer.Factory.newInstance(pathHandler);
             UndertowMessages.MESSAGES.listenerStarted("Http listener", socketAddress);
         } catch (IOException e) {
             throw new StartException("Could not start http listener", e);
         }
     }
 
     @Override
     public void stop(final StopContext stopContext) {
         IoUtils.safeClose(server);
         server = null;
         xnio = null;
         servletContainer = null;
     }
 
     @Override
     public HttpListenerService getValue() throws IllegalStateException, IllegalArgumentException {
         return this;
     }
 
     public ServletContainer getServletContainer() {
         return servletContainer;
     }
 
     public InjectedValue<XnioWorker> getWorker() {
         return worker;
     }
 
     public InjectedValue<SocketBinding> getBinding() {
         return binding;
     }
 }
