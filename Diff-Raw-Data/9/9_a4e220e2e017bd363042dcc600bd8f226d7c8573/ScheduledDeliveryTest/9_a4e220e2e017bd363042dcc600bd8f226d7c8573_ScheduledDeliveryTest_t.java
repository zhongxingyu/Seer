 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
 package org.jboss.test.messaging.jms;
 
 import javax.jms.Connection;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.jboss.messaging.core.message.impl.MessageImpl;
 
 
 /**
  * 
  * A ScheduledDeliveryTest
  *
  * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
  * @version <tt>$Revision$</tt>
  *
  * $Id$
  *
  */
 public class ScheduledDeliveryTest extends JMSTestCase
 {
    // Constants -----------------------------------------------------
 
    // Static --------------------------------------------------------
 
    // Attributes ----------------------------------------------------
 
    // Constructors --------------------------------------------------
 
    public ScheduledDeliveryTest(String name)
    {
       super(name);
    }
 
    // Public --------------------------------------------------------
    
    public void testScheduledDeliveryTX() throws Exception
    {
    	scheduledDelivery(true);
    }
 
    public void testScheduledDeliveryNoTX() throws Exception
    {
    	scheduledDelivery(false);
    }
 
    public void testScheduledDeliveryWithRestart() throws Exception
    {
       Connection conn = null;
 
       try
       {
          conn = cf.createConnection();
 
          Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
          Queue queue1 = (Queue) getInitialContext().lookup("/queue/testQueue");
          MessageProducer prod = sess.createProducer(queue1);
 
          //Send one scheduled
 
          long now = System.currentTimeMillis();
 
          TextMessage tm1 = sess.createTextMessage("testScheduled1");
          tm1.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 29000);
          prod.send(tm1);
 
          //First send some non scheduled messages
 
          TextMessage tm2 = sess.createTextMessage("testScheduled2");
          prod.send(tm2);
 
          TextMessage tm3 = sess.createTextMessage("testScheduled3");
          prod.send(tm3);
 
          TextMessage tm4 = sess.createTextMessage("testScheduled4");
          prod.send(tm4);
 
 
          //Now send some more scheduled messages
          
          //These numbers have to be large with Hudson, since restart can take some time
 
          TextMessage tm5 = sess.createTextMessage("testScheduled5");
          tm5.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 27000);
          prod.send(tm5);
 
          TextMessage tm6 = sess.createTextMessage("testScheduled6");
          tm6.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 26000);
          prod.send(tm6);
 
          TextMessage tm7 = sess.createTextMessage("testScheduled7");
          tm7.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 25000);
          prod.send(tm7);
 
          TextMessage tm8 = sess.createTextMessage("testScheduled8");
          tm8.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 28000);
          prod.send(tm8);
 
          //And one scheduled with a -ve number
 
          TextMessage tm9 = sess.createTextMessage("testScheduled9");
          tm8.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), -3);
          prod.send(tm9);
 
          //Now stop the server and restart it
 
          conn.close();
 
          stop();
 
          start();
 
          // Messaging server restart implies new ConnectionFactory lookup
          deployAndLookupAdministeredObjects();
 
          conn = cf.createConnection();
 
          conn.start();
 
          sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
          MessageConsumer cons = sess.createConsumer(queue1);
 
          forceGC();
 
          //First the non scheduled messages should be received
 
          TextMessage rm1 = (TextMessage)cons.receive(250);
          assertNotNull(rm1);
          assertEquals(tm2.getText(), rm1.getText());
 
          TextMessage rm2 = (TextMessage)cons.receive(250);
          assertNotNull(rm2);
          assertEquals(tm3.getText(), rm2.getText());
 
          TextMessage rm3 = (TextMessage)cons.receive(250);
          assertNotNull(rm3);
          assertEquals(tm4.getText(), rm3.getText());
 
          //Now the one with a scheduled with a -ve number
          TextMessage rm5 = (TextMessage)cons.receive(250);
          assertNotNull(rm5);
          assertEquals(tm9.getText(), rm5.getText());
 
          //Now the scheduled
          TextMessage rm6 = (TextMessage)cons.receive(25500);
          assertNotNull(rm6);
          assertEquals(tm7.getText(), rm6.getText());
 
          long now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 3000);
 
 
          TextMessage rm7 = (TextMessage)cons.receive(26500);
          assertNotNull(rm7);
          assertEquals(tm6.getText(), rm7.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 4000);
 
 
          TextMessage rm8 = (TextMessage)cons.receive(27500);
          assertNotNull(rm8);
          assertEquals(tm5.getText(), rm8.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 5000);
 
 
          TextMessage rm9 = (TextMessage)cons.receive(28500);
          assertNotNull(rm9);
          assertEquals(tm8.getText(), rm9.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 6000);
 
 
          TextMessage rm10 = (TextMessage)cons.receive(29500);
          assertNotNull(rm10);
          assertEquals(tm1.getText(), rm10.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 7000);
       }
       finally
       {
          removeAllMessages("testQueue", true);
          if (conn != null)
          {
             conn.close();
          }
       }
    }
 
    public void testDelayedRedelivery() throws Exception
    {
       String qName = "testDelayedRedeliveryDefaultQ";
    	try
    	{
          long delay = 3000;
          addAddressSettings(qName, delay);
          createQueue(qName);
          queue1 = (Queue) getInitialContext().lookup("/queue/" + qName);
 
 
 	      this.delayedRedeliveryDefaultOnClose(delay);
 
 	      this.delayedRedeliveryDefaultOnRollback(delay);
    	}
    	finally
    	{
          removeAddressSettings(qName);
          this.destroyQueue(qName);
       }
    }
 
 
    // Package protected ---------------------------------------------
 
    // Protected -----------------------------------------------------
 
    // Private -------------------------------------------------------
 
    private void delayedRedeliveryDefaultOnClose(long delay) throws Exception
    {
       Connection conn = null;
 
       try
       {
          conn = cf.createConnection();
 
          Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
          MessageProducer prod = sess.createProducer(queue1);
 
          final int NUM_MESSAGES = 5;
 
          forceGC();
 
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = sess.createTextMessage("message" + i);
 
             prod.send(tm);
          }
 
          Session sess2 = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
 
          MessageConsumer cons = sess2.createConsumer(queue1);
 
          conn.start();
 
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = (TextMessage)cons.receive(500);
 
             assertNotNull(tm);
 
             assertEquals("message" + i, tm.getText());
          }
 
          //Now close the session
          //This should cancel back to the queue with a delayed redelivery
 
          long now = System.currentTimeMillis();
 
          sess2.close();
 
          Session sess3 = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
          MessageConsumer cons2 = sess3.createConsumer(queue1);
 
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = (TextMessage)cons2.receive(delay + 1000);
 
             assertNotNull(tm);
 
             long time = System.currentTimeMillis();
 
             assertTrue(time - now >= delay);
 
             //Hudson can introduce a large degree of indeterminism
             assertTrue((time - now)  + ">" + (delay + 1000), time - now < delay + 1000);
          }
       }
       finally
       {
          if (conn != null)
          {
             conn.close();
          }
       }
    }
 
    private void delayedRedeliveryDefaultOnRollback(long delay) throws Exception
    {
    	Connection conn = null;
 
    	try
    	{
    		conn = cf.createConnection();
 
    		Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
    		MessageProducer prod = sess.createProducer(queue1);
 
    		final int NUM_MESSAGES = 5;
 
    		for (int i = 0; i < NUM_MESSAGES; i++)
    		{
    			TextMessage tm = sess.createTextMessage("message" + i);
 
    			prod.send(tm);
    		}
 
    		Session sess2 = conn.createSession(true, Session.SESSION_TRANSACTED);
 
    		MessageConsumer cons = sess2.createConsumer(queue1);
 
    		conn.start();
 
    		for (int i = 0; i < NUM_MESSAGES; i++)
    		{
    			TextMessage tm = (TextMessage)cons.receive(500);
 
    			assertNotNull(tm);
 
    			assertEquals("message" + i, tm.getText());
    		}
 
    		//Now rollback
 
    		long now = System.currentTimeMillis();
 
    		sess2.rollback();
 
    		//This should redeliver with a delayed redelivery
 
    		for (int i = 0; i < NUM_MESSAGES; i++)
    		{
    			TextMessage tm = (TextMessage)cons.receive(delay + 1000);
 
    			assertNotNull(tm);
 
    			long time = System.currentTimeMillis();
 
    			assertTrue(time - now >= delay);
 
             //Hudson can introduce a large degree of indeterminism
             assertTrue((time - now)  + ">" + (delay + 1000), time - now < delay + 1000);
          }
 
    		sess2.commit();
    	}
    	finally
    	{
    		if (conn != null)
    		{
    			conn.close();
    		}
    	}
    }
 
    private void scheduledDelivery(boolean tx) throws Exception
    {
       Connection conn = null;
 
       try
       {
          conn = cf.createConnection();
 
          Session sess = conn.createSession(tx, tx ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
 
          MessageProducer prod = sess.createProducer(queue1);
 
          MessageConsumer cons = sess.createConsumer(queue1);
 
          conn.start();
 
          //Send one scheduled
 
          long now = System.currentTimeMillis();
 
          TextMessage tm1 = sess.createTextMessage("testScheduled1");
          tm1.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 7000);
          prod.send(tm1);
 
          //First send some non scheduled messages
 
          TextMessage tm2 = sess.createTextMessage("testScheduled2");
          prod.send(tm2);
 
          TextMessage tm3 = sess.createTextMessage("testScheduled3");
          prod.send(tm3);
 
          TextMessage tm4 = sess.createTextMessage("testScheduled4");
          prod.send(tm4);
 
 
          //Now send some more scheduled messages
 
          TextMessage tm5 = sess.createTextMessage("testScheduled5");
          tm5.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 5000);
          prod.send(tm5);
 
          TextMessage tm6 = sess.createTextMessage("testScheduled6");
          tm6.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 4000);
          prod.send(tm6);
 
          TextMessage tm7 = sess.createTextMessage("testScheduled7");
          tm7.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 3000);
          prod.send(tm7);
 
          TextMessage tm8 = sess.createTextMessage("testScheduled8");
          tm8.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), now + 6000);
          prod.send(tm8);
 
          //And one scheduled with a -ve number
 
          TextMessage tm9 = sess.createTextMessage("testScheduled9");
         tm9.setLongProperty(MessageImpl.HDR_SCHEDULED_DELIVERY_TIME.toString(), -3);
          prod.send(tm9);
 
          if (tx)
          {
          	sess.commit();
          }
 
          //First the non scheduled messages should be received
          forceGC();
 
 
          TextMessage rm1 = (TextMessage)cons.receive(250);
          assertNotNull(rm1);
          assertEquals(tm2.getText(), rm1.getText());
 
          TextMessage rm2 = (TextMessage)cons.receive(250);
          assertNotNull(rm2);
          assertEquals(tm3.getText(), rm2.getText());
 
          TextMessage rm3 = (TextMessage)cons.receive(250);
          assertNotNull(rm3);
          assertEquals(tm4.getText(), rm3.getText());
 
          //Now the one with a scheduled with a -ve number
          TextMessage rm5 = (TextMessage)cons.receive(250);
          assertNotNull(rm5);
          assertEquals(tm9.getText(), rm5.getText());
 
          //Now the scheduled
          TextMessage rm6 = (TextMessage)cons.receive(3250);
          assertNotNull(rm6);
          assertEquals(tm7.getText(), rm6.getText());
 
          long now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 3000);
 
 
          TextMessage rm7 = (TextMessage)cons.receive(1250);
          assertNotNull(rm7);
          assertEquals(tm6.getText(), rm7.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 4000);
 
 
          TextMessage rm8 = (TextMessage)cons.receive(1250);
          assertNotNull(rm8);
          assertEquals(tm5.getText(), rm8.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 5000);
 
 
          TextMessage rm9 = (TextMessage)cons.receive(1250);
          assertNotNull(rm9);
          assertEquals(tm8.getText(), rm9.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 6000);
 
 
          TextMessage rm10 = (TextMessage)cons.receive(1250);
          assertNotNull(rm10);
          assertEquals(tm1.getText(), rm10.getText());
 
          now2 = System.currentTimeMillis();
 
          assertTrue(now2 - now >= 7000);
 
          if (tx)
          {
          	sess.commit();
          }
       }
       finally
       {
          if (conn != null)
          {
             conn.close();
          }
       }
    }
 
    // Inner classes -------------------------------------------------
    
 }
