 package net.dovemq.transport.link;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.management.MalformedObjectNameException;
 
 import net.dovemq.transport.common.CAMQPTestTask;
 import net.dovemq.transport.common.JMXProxyWrapper;
 import net.dovemq.transport.session.SessionCommand;
 
 public class LinkTestMultipleLinksNoSharing
 {
     private static final String source = "src";
 
     private static final String target = "target";
 
     private static int NUM_THREADS = 5;
 
     private static class LinkTestMessageSender extends CAMQPTestTask implements Runnable
     {
         private volatile CAMQPLinkSender linkSender = null;
         CAMQPLinkSender getLinkSender()
         {
             return linkSender;
         }
 
         private final int numMessagesToSend;
         public LinkTestMessageSender(CountDownLatch startSignal,
                 CountDownLatch doneSignal, int numMessagesToSend)
         {
             super(startSignal, doneSignal);
             this.numMessagesToSend = numMessagesToSend;
         }
 
         @Override
         public void run()
         {
             String linkSource = String.format("%s%d", source, Thread.currentThread().getId());
             String linkTarget = String.format("%s%d", target, Thread.currentThread().getId());
             
             CAMQPLinkSender sender = CAMQPLinkFactory.createLinkSender(brokerContainerId, linkSource, linkTarget);
             linkSender = sender;
             System.out.println("Sender Link created between : " + linkSource + "  and: " + linkTarget);
             
             String linkName = linkSender.getLinkName();
             
             mbeanProxy.registerTarget(linkSource, linkTarget);
             mbeanProxy.issueLinkCredit(linkName, 10);
             
             LinkTestUtils.sendMessagesOnLink(linkSender, numMessagesToSend);
             waitForReady();
             linkSender.destroyLink();
             done();
         }
     }
     
     private static String brokerContainerId ;
     private static LinkCommandMBean mbeanProxy;
     
     public static void main(String[] args) throws InterruptedException, IOException, MalformedObjectNameException
     {
         /*
          * Read args
          */
         String publisherName = args[0];
         String brokerIp = args[1];
         String jmxPort = args[2];
         
         JMXProxyWrapper jmxWrapper = new JMXProxyWrapper(brokerIp, jmxPort);
         
         NUM_THREADS = Integer.parseInt(args[3]);
         int numMessagesToSend = Integer.parseInt(args[4]);
           
         brokerContainerId = String.format("broker@%s", brokerIp);
         CAMQPLinkManager.initialize(false, publisherName);
         
         SessionCommand localSessionCommand = new SessionCommand();
         localSessionCommand.sessionCreate(brokerContainerId);
         
         mbeanProxy = jmxWrapper.getLinkBean();
         
         ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
         CountDownLatch startSignal = new CountDownLatch(1);
         CountDownLatch doneSignal = new CountDownLatch(NUM_THREADS);
         
         LinkTestMessageSender[] senders = new LinkTestMessageSender[NUM_THREADS];
         for (int i = 0; i < NUM_THREADS; i++)
         {
             LinkTestMessageSender sender = new LinkTestMessageSender(startSignal, doneSignal, numMessagesToSend);
             senders[i] = sender;
             executor.submit(sender);
         }
 
         Random randomGenerator = new Random();
         int iterator = 0;
         while (true)
         {
             int randomInt = randomGenerator.nextInt(50);
             long messagesReceived = mbeanProxy.getNumMessagesReceived();
             System.out.println("got messages: " + messagesReceived + " issuing link credit: " + randomInt);
             if (messagesReceived == numMessagesToSend * NUM_THREADS)
             {
                 break;
             }
             Thread.sleep(randomGenerator.nextInt(50) + 50);
 
             LinkTestMessageSender sender = senders[iterator % NUM_THREADS];
             iterator++;
             /*
              * Receiver-driven link-credit
              */
            mbeanProxy.issueLinkCredit(sender.getLinkSender().getLinkName(), randomInt);            
         }
         
         startSignal.countDown();
         
         doneSignal.await();
         Thread.sleep(2000);
         
         assertTrue(mbeanProxy.getNumMessagesReceived() == numMessagesToSend * NUM_THREADS);
         executor.shutdown();
         
         CAMQPLinkManager.shutdown();
         mbeanProxy.reset();
         jmxWrapper.cleanup();
     }
 }
