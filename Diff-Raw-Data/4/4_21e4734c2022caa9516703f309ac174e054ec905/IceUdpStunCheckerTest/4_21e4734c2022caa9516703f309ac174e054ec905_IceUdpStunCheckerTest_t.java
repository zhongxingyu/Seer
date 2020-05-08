 package org.lastbamboo.common.ice;
 
 import java.net.InetSocketAddress;
 
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.junit.Assert;
 import org.junit.Test;
 import org.lastbamboo.common.ice.transport.IceConnector;
 import org.lastbamboo.common.ice.transport.IceUdpConnector;
 import org.lastbamboo.common.ice.transport.IceUdpStunChecker;
 import org.lastbamboo.common.stun.client.StunClientMessageVisitorFactory;
 import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
 import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
 import org.lastbamboo.common.util.mina.DemuxingIoHandler;
 import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
 
 /**
  * Test for the ICE connectivity checker. 
  */
 public class IceUdpStunCheckerTest
     {
 
     /**
      * Basic test for the STUN checker.
      * 
      * @throws Exception If any unexpected error occurs.
      */
     @Test public void testStunChecker() throws Exception
         {
         final DemuxableProtocolCodecFactory stunCodecFactory =
             new StunDemuxableProtocolCodecFactory();
         final DemuxableProtocolCodecFactory otherCodecFactory =
             new StunDemuxableProtocolCodecFactory();
         final ProtocolCodecFactory codecFactory =
             new DemuxingProtocolCodecFactory(stunCodecFactory, 
                 otherCodecFactory);
         final IoHandler clientIoHandler = new IoHandlerAdapter();
         
         final InetSocketAddress remoteAddress =
            new InetSocketAddress("stun01.sipphone.com", 3478);
         
         final StunTransactionTracker<StunMessage> tracker = 
             new StunTransactionTrackerImpl();
         final StunMessageVisitorFactory visitorFactory = 
             new StunClientMessageVisitorFactory<StunMessage>(tracker);
         final StunIoHandler<StunMessage> stunIoHandler =
             new StunIoHandler<StunMessage>(visitorFactory);
         
         final IoHandler demuxingIoHandler =
             new DemuxingIoHandler<StunMessage, Object>(
                 StunMessage.class, stunIoHandler, Object.class, 
                 clientIoHandler);
         final IceConnector connector = 
             new IceUdpConnector(codecFactory, demuxingIoHandler, true);
         final IoSession ioSession = 
             connector.connect(new InetSocketAddress(4932), remoteAddress);
         final IceUdpStunChecker checker = 
             new IceUdpStunChecker(ioSession, tracker);
         
         final BindingRequest bindingRequest = new BindingRequest();
         final long rto = 20;
         final StunMessage response = checker.write(bindingRequest, rto);
         
         Assert.assertTrue(response instanceof BindingSuccessResponse);
         }
     }
