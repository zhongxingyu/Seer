 package com.projectmaxwell.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.http.NameValuePair;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.projectmaxwell.model.User;
 import com.projectmaxwell.service.dao.impl.sendloop.SendloopBrowseListResponse;
 import com.projectmaxwell.service.dao.impl.sendloop.SendloopClient;
 import com.projectmaxwell.service.dao.impl.sendloop.SendloopImportResponse;
 import com.projectmaxwell.service.dao.impl.sendloop.SendloopSubscriber;
 
 public class SendloopClientTest {
 
 	@Test
 	@Ignore
 	public void testSendloopClientSubscribeAndUnsubscribe(){
 		SendloopClient client = new SendloopClient();
 		client.addBodyParam("ListID", "2");
 		client.addHeader("Accept-language", "en-US");
 		String gmailModified = String.valueOf(Math.random()).substring(2, 7);
 		client.addBodyParam("EmailAddress","galactoise+" + gmailModified + "@gmail.com");	
 		NameValuePair subscriptionParam = client.addBodyParam("SubscriptionIP","24.19.233.55");
 
 		//client.postRequest("Subscriber.Subscribe/json");
 		
 		client.removeBodyParam(subscriptionParam);
 		
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		client.addBodyParam("UnsubscriptionIP","24.19.233.55");
 		//client.postRequest("Subscriber.Unsubscribe/json");
 	}
 	
 	@Test
 	@Ignore
 	public void testSendloopClientImport(){
 		String gmailModified = String.valueOf(Math.random()).substring(2, 7);
 		SendloopClient client = new SendloopClient();
 /*		client.addBodyParam("ListID", "2");
 		client.addHeader("Accept-language", "en-US");
 		client.addBodyParam("Subscribers[0][EmailAddress]","galactoise+" + gmailModified + "@gmail.com");
 
 		client.postRequest("Subscriber.Import/json");*/
 		User u = new User();
 		u.setEmail("galactoise+" + gmailModified + "@gmail.com");
 		u.setFirstName("Dirt");
 		u.setLastName("McGirt");
 		SendloopImportResponse response = client.importUserToList(u, "2");
 		assertNotNull(response);
 		assertTrue(response.isSuccess());
 		assertEquals(response.getTotalDuplicate(),0);
 		assertEquals(response.getTotalFailed(),0);
 		assertEquals(response.getTotalImported(),1);
 		assertEquals(response.getErrorCode(),0);
 		assertNull(response.getErrorFields());
 		assertNull(response.getErrorMessage());
 	}
 	
 	@Test
 	public void testSendloopClientBrowse(){
 		SendloopClient client = new SendloopClient();
 
 		SendloopBrowseListResponse response = client.browseList("4");
 		assertNotNull(response);
 		assertTrue(response.isSuccess());
 		assertNotNull(response.getSubscribers());
 		assertFalse(response.getSubscribers().length == 0);
 		for(SendloopSubscriber subscriber : response.getSubscribers()){
 			System.out.println(subscriber.getEmailAddress());
 		}
 	}
 	
 }
