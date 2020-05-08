 package org.servicemix.jbi.nmr;
 
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

 import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
 import javax.xml.namespace.QName;
 
 import junit.framework.TestCase;
 
import org.servicemix.components.util.EchoComponent;
 import org.servicemix.jbi.container.ActivationSpec;
 import org.servicemix.jbi.container.JBIContainer;
 import org.servicemix.jbi.container.SubscriptionSpec;
 import org.servicemix.jbi.resolver.SubscriptionFilter;
 import org.servicemix.tck.ReceiverComponent;
 import org.servicemix.tck.SenderComponent;
 
 public class PubSubTest extends TestCase {
 
     private SenderComponent sender;
     private JBIContainer container;
 
     protected void setUp() throws Exception {
         container = new JBIContainer();
         container.setUseMBeanServer(true);
         container.setCreateMBeanServer(false);
         container.setFlowName("seda");
         container.init();
         container.start();
         
         sender = new SenderComponent();
         ActivationSpec as = new ActivationSpec("source",sender);
         as.setService(new QName("http://www.test.com","source"));
         as.setFailIfNoDestinationEndpoint(false);
         container.activateComponent(as);
     }
 
     protected void tearDown() throws Exception {
         container.shutDown();
     }
     
     public void testPubSub() throws Exception {
     	ReceiverComponent recListener = new ReceiverComponent();
         container.activateComponent(createReceiverAS("receiver",recListener));
         sender.sendMessages(1);
         recListener.getMessageList().assertMessagesReceived(1);
     }
     
     public void testPubSubFiltered() throws Exception {
     	ReceiverComponent recListener = new ReceiverComponent();
         container.activateComponent(createReceiverASFiltered("receiver",recListener));
         sender.sendMessages(1, false);
         recListener.getMessageList().assertMessagesReceived(1);
     }
 
     private ActivationSpec createReceiverAS(String id, Object component) {
         ActivationSpec as = new ActivationSpec(id, component);
         SubscriptionSpec ss = new SubscriptionSpec();
         ss.setService(new QName("http://www.test.com","source"));
         as.setSubscriptions(new SubscriptionSpec[] { ss });
         as.setFailIfNoDestinationEndpoint(false);
         return as;
     }
 
     private ActivationSpec createReceiverASFiltered(String id, Object component) {
         ActivationSpec as = new ActivationSpec(id, component);
         SubscriptionSpec ss = new SubscriptionSpec();
         ss.setService(new QName("http://www.test.com","source"));
         ss.setFilter(new Filter());
         as.setSubscriptions(new SubscriptionSpec[] { ss });
         as.setFailIfNoDestinationEndpoint(false);
         return as;
     }
 
     public static class Filter implements SubscriptionFilter {
 
         public boolean matches(MessageExchange arg0) {
             System.out.println("Matches");
             return true;
         }
         
     }
 }
