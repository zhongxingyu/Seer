 package org.lastbamboo.common.ice;
 
 import java.net.InetSocketAddress;
 
 import org.littleshoot.mina.common.IoHandler;
 import org.littleshoot.mina.common.IoHandlerAdapter;
 import org.littleshoot.mina.common.IoSession;
 import org.littleshoot.mina.filter.codec.ProtocolCodecFactory;
 import org.junit.Assert;
 import org.junit.Test;
 import org.lastbamboo.common.ice.transport.IceConnector;
 import org.lastbamboo.common.ice.transport.IceUdpConnector;
 import org.lastbamboo.common.ice.transport.IceUdpStunChecker;
 import org.lastbamboo.common.stun.client.StunClientMessageVisitorFactory;
 import org.littleshoot.stun.stack.StunDemuxableProtocolCodecFactory;
 import org.littleshoot.stun.stack.StunIoHandler;
 import org.littleshoot.stun.stack.message.BindingRequest;
 import org.littleshoot.stun.stack.message.BindingSuccessResponse;
 import org.littleshoot.stun.stack.message.StunMessage;
 import org.littleshoot.stun.stack.message.StunMessageVisitorFactory;
 import org.littleshoot.stun.stack.transaction.StunTransactionTracker;
 import org.littleshoot.stun.stack.transaction.StunTransactionTrackerImpl;
 import org.littleshoot.util.mina.DemuxableProtocolCodecFactory;
 import org.littleshoot.util.mina.DemuxingIoHandler;
 import org.littleshoot.util.mina.DemuxingProtocolCodecFactory;
 
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
             //new InetSocketAddress("stun01.sipphone.com", 3478);
            new InetSocketAddress("stun.xten.com", 3478);
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
