 package org.lastbamboo.common.ice.stubs;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 
 import org.apache.commons.lang.math.RandomUtils;
 import org.apache.mina.common.IoServiceListener;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.util.NetworkUtils;
 
 public class StunClientStub implements StunClient
     {
 
     private final InetAddress m_stunServerAddress;
     private final InetSocketAddress m_hostAddress;
     private final InetSocketAddress m_serverReflexiveAddress;
     private final InetSocketAddress m_relayAddress;
 
     public static StunClient newClient()
         {
         try
             {
             return new StunClientStub();
             }
         catch (final UnknownHostException e)
             {
             throw new IllegalArgumentException("Can't resolve host", e);
             }
         }
 
     public static StunClient newClient(final InetSocketAddress serverReflexive, 
         final int hostPort)
         {
         try
             {
             final StunClient client = 
                 new StunClientStub(InetAddress.getByName("32.34.3.2"),
                 new InetSocketAddress(NetworkUtils.getLocalHost(), hostPort),
                 serverReflexive);
             return client;
             }
         catch (final UnknownHostException e)
             {
             throw new IllegalArgumentException("Can't resolve host", e);
             }
         }
 
     public StunClientStub(final InetAddress stunServerAddress) 
         throws UnknownHostException 
         {
         this(stunServerAddress, 
             new InetSocketAddress(NetworkUtils.getLocalHost(), getRandomPort()));
         }
 
     public StunClientStub() throws UnknownHostException
         {
         this(InetAddress.getByName("32.34.3.2"), 
            new InetSocketAddress(NetworkUtils.getLocalHost(), getRandomPort()));
         }
 
     public StunClientStub(final InetAddress stunServerAddress, 
         final InetSocketAddress hostAddress)
         {
         this(stunServerAddress, hostAddress, 
             new InetSocketAddress("76.24.52.2", 4820));
         }
     
     public StunClientStub(final InetSocketAddress serverReflexiveAddress,
         final int hostPort) throws UnknownHostException
         {
         this(InetAddress.getByName("32.34.3.2"),
             new InetSocketAddress(NetworkUtils.getLocalHost(), hostPort),
             serverReflexiveAddress);
         }
     
     private StunClientStub(final InetAddress stunServerAddress, 
         final InetSocketAddress hostAddress, 
         final InetSocketAddress serverReflexiveAddress)
         {
         m_stunServerAddress = stunServerAddress;
         m_hostAddress = hostAddress; 
         m_serverReflexiveAddress = serverReflexiveAddress;
         
         // Use a random port to avoid bind conflicts for tests that use
         // multiple stubs.
         m_relayAddress = new InetSocketAddress("98.242.12.7", getRandomPort());
         }
     
 
     private static int getRandomPort()
         {
         final int relayPort = 
             1024 + RandomUtils.nextInt() % (Short.MAX_VALUE*2 - 1025);
         return relayPort;
         }
 
     public InetSocketAddress getHostAddress()
         {
         return this.m_hostAddress;
         }
 
     public InetSocketAddress getServerReflexiveAddress()
         {
         return m_serverReflexiveAddress;
         }
 
     public InetAddress getStunServerAddress()
         {
         return this.m_stunServerAddress;
         }
 
     public InetSocketAddress getRelayAddress()
         {
         return this.m_relayAddress;
         }
 
     public StunMessage write(BindingRequest request, InetSocketAddress remoteAddress)
         {
         // TODO Auto-generated method stub
         return null;
         }
 
     public StunMessage write(BindingRequest request, InetSocketAddress remoteAddress, long rto)
         {
         // TODO Auto-generated method stub
         return null;
         }
 
     public void addIoServiceListener(IoServiceListener serviceListener)
         {
         // TODO Auto-generated method stub
         
         }
 
     public void connect()
         {
         // TODO Auto-generated method stub
         
         }
 
     public void close()
         {
         // TODO Auto-generated method stub
         
         }
     }
