 package org.notificationengine.persistance;
 
 import static org.junit.Assert.*;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.junit.Before;
 import org.junit.Test;
 import org.notificationengine.domain.DecoratedNotification;
 import org.notificationengine.domain.RawNotification;
 import org.notificationengine.domain.Recipient;
 import org.notificationengine.domain.Topic;
 import org.notificationengine.persistance.Persister;
 
 import com.mongodb.ServerAddress;
 
 public class TestPersister {
 
 	private Persister persister;
 	
 	@Before
 	public void init() {
 		
		persister = new Persister(Boolean.TRUE, null);
 		
 		persister.cleanRawNotifications();
 		
 		persister.cleanDecoratedNotifications();
 	}
 	
 	@Test
 	public void testCreateDecoratedNotification() {
 		
 		RawNotification rawNotification = new RawNotification();
 		rawNotification.set_id(new ObjectId());
 		rawNotification.setProcessed(Boolean.FALSE);
 		rawNotification.setTopic(new Topic("facturation.societe1"));
 		
 		Recipient recipient = new Recipient("email1@societe1.com");
 		
 		DecoratedNotification decoratedNotification = new DecoratedNotification(rawNotification, recipient);
 		
 		persister.createDecoratedNotification(decoratedNotification);
 		
 		assertEquals(1, persister.getDecoratedNotifications().count());
 	}
 	
 	@Test
 	public void testCreateRawNotification() {
 		
 		RawNotification rawNotification1 = new RawNotification();
 		rawNotification1.set_id(new ObjectId());
 		rawNotification1.setProcessed(Boolean.FALSE);
 		rawNotification1.setTopic(new Topic("facturation.societe1"));
 		
 		persister.createRawNotification(rawNotification1);
 		
 		
 		RawNotification rawNotification2 = new RawNotification();
 		rawNotification2.set_id(new ObjectId());
 		rawNotification2.setProcessed(Boolean.FALSE);
 		rawNotification2.setTopic(new Topic("facturation.societe2"));
 		
 		persister.createRawNotification(rawNotification2);
 		
 		
 		assertEquals(2, persister.getRawNotifications().count());
 	}
 
 	@Test
 	public void testRetrieveNotProcessedRawNotificationsForTopic() {
 		
 		RawNotification rawNotification1 = new RawNotification();
 		rawNotification1.set_id(new ObjectId());
 		rawNotification1.setProcessed(Boolean.FALSE);
 		rawNotification1.setTopic(new Topic("facturation.societe1"));
 		
 		persister.createRawNotification(rawNotification1);
 		
 		
 		RawNotification rawNotification2 = new RawNotification();
 		rawNotification2.set_id(new ObjectId());
 		rawNotification2.setProcessed(Boolean.FALSE);
 		rawNotification2.setTopic(new Topic("facturation.societe2"));
 		
 		persister.createRawNotification(rawNotification2);
 		
 		
 		RawNotification rawNotification3 = new RawNotification();
 		rawNotification3.set_id(new ObjectId());
 		rawNotification3.setProcessed(Boolean.TRUE);
 		rawNotification3.setTopic(new Topic("facturation.societe1"));
 		
 		persister.createRawNotification(rawNotification3);
 		
 		
 		RawNotification rawNotification4 = new RawNotification();
 		rawNotification4.set_id(new ObjectId());
 		rawNotification4.setProcessed(Boolean.FALSE);
 		rawNotification4.setTopic(new Topic("facturation"));
 		
 		persister.createRawNotification(rawNotification4);
 		
 		
 
 		RawNotification rawNotification5 = new RawNotification();
 		rawNotification5.set_id(new ObjectId());
 		rawNotification5.setProcessed(Boolean.FALSE);
 		rawNotification5.setTopic(new Topic("facturationDifferente"));
 		
 		persister.createRawNotification(rawNotification5);
 		
 		
 		Collection<RawNotification> rawNotifications = persister.retrieveNotProcessedRawNotificationsForTopic(new Topic("facturation"));
 		
 		assertEquals(3, rawNotifications.size());
 	}
 	
 	@Test
 	public void testRetrieveNotSentDecoratedNotificationsForTopic() {
 		
 		DecoratedNotification decoratedNotification1 = new DecoratedNotification();
 		decoratedNotification1.set_id(new ObjectId());
 		decoratedNotification1.setSent(Boolean.FALSE);
 		decoratedNotification1.setRawNotification(new RawNotification(new Topic("facturation.societe1")));
 		
 		persister.createDecoratedNotification(decoratedNotification1);
 		
 		
 		DecoratedNotification decoratedNotification2 = new DecoratedNotification();
 		decoratedNotification2.set_id(new ObjectId());
 		decoratedNotification2.setSent(Boolean.FALSE);
 		decoratedNotification2.setRawNotification(new RawNotification(new Topic("facturation.societe2")));
 		
 		persister.createDecoratedNotification(decoratedNotification2);
 		
 		
 		DecoratedNotification decoratedNotification3 = new DecoratedNotification();
 		decoratedNotification3.set_id(new ObjectId());
 		decoratedNotification3.setSent(Boolean.TRUE);
 		decoratedNotification3.setRawNotification(new RawNotification(new Topic("facturation.societe1")));
 		
 		persister.createDecoratedNotification(decoratedNotification3);
 		
 		
 		DecoratedNotification decoratedNotification4 = new DecoratedNotification();
 		decoratedNotification4.set_id(new ObjectId());
 		decoratedNotification4.setSent(Boolean.FALSE);
 		decoratedNotification4.setRawNotification(new RawNotification(new Topic("facturation")));
 		
 		persister.createDecoratedNotification(decoratedNotification4);
 		
 		
 
 		DecoratedNotification rawNotification5 = new DecoratedNotification();
 		rawNotification5.set_id(new ObjectId());
 		rawNotification5.setSent(Boolean.FALSE);
 		rawNotification5.setRawNotification(new RawNotification(new Topic("facturationDifferente")));
 		
 		persister.createDecoratedNotification(rawNotification5);
 		
 		
 		Collection<DecoratedNotification> notSentDecoratedNotifications = persister.retrieveNotSentDecoratedNotificationsForTopic(new Topic("facturation"));
 		
 		assertEquals(3, notSentDecoratedNotifications.size());
 	}
 	
 	@Test
 	public void testRetrieveRawNotificationById() {
 		
 		RawNotification rawNotification = new RawNotification();
 		rawNotification.set_id(new ObjectId());
 		rawNotification.setProcessed(Boolean.FALSE);
 		rawNotification.setTopic(new Topic("facturation.societe1"));
 		
 		persister.createRawNotification(rawNotification);
 		
 		rawNotification = persister.retrieveRawNotificationById(rawNotification.get_id());
 		
 		assertEquals("facturation.societe1", rawNotification.getTopic().getName());
 	}
 
 	@Test
 	public void testMarkRawNotificationAsProcessed() {
 		
 		RawNotification rawNotification = new RawNotification();
 		rawNotification.set_id(new ObjectId());
 		rawNotification.setProcessed(Boolean.FALSE);
 		rawNotification.setTopic(new Topic("facturation.societe1"));
 		
 		persister.createRawNotification(rawNotification);
 		
 		persister.markRawNotificationAsProcessed(rawNotification);
 		
 		rawNotification = persister.retrieveRawNotificationById(rawNotification.get_id());
 		
 		assertTrue(rawNotification.getProcessed());
 	}
 	
 	@Test
 	public void testRetrieveDecoratedNotificationById() {
 		
 		DecoratedNotification decoratedNotification = new DecoratedNotification();
 		decoratedNotification.set_id(new ObjectId());
 		decoratedNotification.setSent(Boolean.FALSE);
 		decoratedNotification.setRawNotification(new RawNotification(new Topic("facturation.societe1")));
 		
 		persister.createDecoratedNotification(decoratedNotification);
 		
 		decoratedNotification = persister.retrieveDecoratedNotificationById(decoratedNotification.get_id());
 		
 		assertEquals("facturation.societe1", decoratedNotification.getRawNotification().getTopic().getName());
 	}
 
 	@Test
 	public void testMarkDecoratedNotificationAsSent() {
 		
 		DecoratedNotification decoratedNotification = new DecoratedNotification();
 		decoratedNotification.set_id(new ObjectId());
 		decoratedNotification.setSent(Boolean.FALSE);
 		
 		persister.createDecoratedNotification(decoratedNotification);
 		
 		persister.markDecoratedNotificationAsSent(decoratedNotification);
 		
 		decoratedNotification = persister.retrieveDecoratedNotificationById(decoratedNotification.get_id());
 		
 		assertTrue(decoratedNotification.getSent());
 	}
 }
