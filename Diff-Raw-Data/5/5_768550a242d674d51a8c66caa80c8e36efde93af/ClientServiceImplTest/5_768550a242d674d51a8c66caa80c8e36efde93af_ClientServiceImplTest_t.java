 package com.dbpractice.realestate.service;
 
 import com.dbpractice.realestate.dao.UserDAO;
 import com.dbpractice.realestate.domain.Client;
 import com.dbpractice.realestate.domain.User;
 import junit.framework.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: moncruist
  * Date: 01.12.11
  * Time: 10:47
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"classpath:data.xml"})
 public class ClientServiceImplTest {
 
     @Autowired
     private ClientService clientService;
 
     @Autowired
     private UserService userService;
 
     @Test
     public void createClientTest() {
         Client newClient = new Client();
         newClient.setClientId(3);
         newClient.setFirstName("Test");
         newClient.setLastName("Test");
         newClient.setPhoneNumber("1234567890");
         newClient.setUser(userService.getUser(1));
 
         clientService.createClient(newClient);
         Assert.assertNotNull(newClient.getClientId());
     }
 
     @Test
     public void getClientTest() {
        Assert.assertNotNull(clientService.getClientById(0));
     }
 
     @Test
     public void getUserFromClientTest() {
        Assert.assertNotNull(clientService.getClientById(0).getUser().getUserId());
     }
 }
