 package org.lastbamboo.common.sip.proxy;
 
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.management.NotificationBroadcasterSupport;
 
 import org.apache.mina.common.IoSession;
 import org.lastbamboo.common.sip.stack.message.Register;
 import org.lastbamboo.common.sip.stack.message.SipMessageFactory;
 import org.lastbamboo.common.sip.stack.message.SipMessageUtils;
 import org.lastbamboo.common.sip.stack.message.SipResponse;
 import org.lastbamboo.common.sip.stack.message.header.SipHeader;
 import org.lastbamboo.common.sip.stack.message.header.SipHeaderNames;
 import org.lastbamboo.common.sip.stack.transport.SipTcpTransportLayer;
 import org.lastbamboo.common.util.MapUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Registrar for SIP clients.
  * 
  * TODO: Also create a map of reader/writers to SIP URIs for more efficient
  * removals??
  */
 public class SipRegistrarImpl extends NotificationBroadcasterSupport 
     implements SipRegistrar, SipRegistrarImplMBean
     {
 
     private final Logger m_log = 
         LoggerFactory.getLogger(SipRegistrarImpl.class);
     
     private final SipMessageFactory m_messageFactory;
 
     private final SipTcpTransportLayer m_transportLayer;
     
     private final Map<URI, IoSession> m_registrations = 
         new ConcurrentHashMap<URI, IoSession>();
 
     private final Collection<RegistrationListener> m_registrationListeners =
         new LinkedList<RegistrationListener>();
 
     /**
      * Keep track of the maximum number of registrations we've seen.
      */
     private int m_maxSize = 0;
     
     /**
      * Creates a new registrar.
      * 
      * @param factory The factory for creating messages for responding to
      * register requests.
      * @param transportLayer The transport layer for actually sending data.
      */
     public SipRegistrarImpl(final SipMessageFactory factory, 
         final SipTcpTransportLayer transportLayer)
         {
         this.m_messageFactory = factory;
         this.m_transportLayer = transportLayer;
         }
 
     public void handleRegister(final Register register, final IoSession session)
         {
         m_log.debug("Processing registration...");
         
         // We also need to add a mapping according to the URI.
         final SipHeader fromHeader = register.getHeader(SipHeaderNames.FROM);
         final URI uri = SipMessageUtils.extractUri(fromHeader);
         if (this.m_registrations.containsKey(uri))
             {
             final IoSession existingSession = this.m_registrations.get(uri);
             m_log.warn("We already have a registration for URI: " + 
                 uri+" with value: "+existingSession+"...closing");
            //existingSession.close();
             }
         this.m_registrations.put(uri, session);
         
         // Keep stats on the maximum number of registrations we've seen.
         if (m_registrations.size() > m_maxSize)
             {
             m_maxSize = m_registrations.size();
             }
         
         final SipResponse response = 
             this.m_messageFactory.createRegisterOk(register);
         
         final InetSocketAddress remoteAddress = 
             (InetSocketAddress) session.getRemoteAddress();
         m_log.debug("Writing OK response to SIP client...");
         
         this.m_transportLayer.writeResponse(remoteAddress, response);
         notifyListeners(uri, true);
         }
 
     public IoSession getIoSession(final URI uri)
         {
         return this.m_registrations.get(uri);
         }
 
     public boolean hasRegistration(final URI uri)
         {
         return this.m_registrations.containsKey(uri);
         }
 
     public void sessionClosed(final IoSession session) 
         {
         final URI uri = 
             (URI) MapUtils.removeFromMapValues(this.m_registrations, 
                 session);
         if (uri != null)
             {
             notifyListeners(uri, false);
             }
         else
             {
             // Maybe we've received duplicate close events?  This could also
             // happen if the client connected but never actually registered,
             // and we're getting a session closed for a client we never knew
             // about.
             m_log.warn("Could not locate URI for reader/writer: " + 
                 session + " in:\n" + this.m_registrations.values());
             }
         }
     
     private void notifyListeners(final URI uri, final boolean registered)
         {
         // Note we're still on the selector thread, so none of the listeners
         // can do anything time consuming at all.  If they do, they need to
         // do it on a separate thread.
         synchronized (this.m_registrationListeners)
             {
             for (final RegistrationListener rl : this.m_registrationListeners)
                 {
                 if (registered)
                     {
                     rl.onRegistered(uri);
                     }
                 else
                     {
                     rl.onUnregistered(uri);
                     }
                 }
             }
         }
 
     public void addRegistrationListener(final RegistrationListener listener)
         {
         m_log.debug("Adding registration listener...");
         this.m_registrationListeners.add(listener);
         }
 
     public int getSipNumRegistered()
         {
         return this.m_registrations.size();
         }
 
     public int getSipMaxRegistered()
         {
         return this.m_maxSize;
         }
     
     public Collection<URI> getRegistered()
         {
         final Collection<URI> registered = new HashSet<URI>();
         synchronized (this.m_registrations)
             {
             final Set<URI> keySet = this.m_registrations.keySet();
             registered.addAll(keySet);
             }
         return registered;
         }
 
     @Override
     public String toString()
         {
         return getClass().getSimpleName();
         }
     }
