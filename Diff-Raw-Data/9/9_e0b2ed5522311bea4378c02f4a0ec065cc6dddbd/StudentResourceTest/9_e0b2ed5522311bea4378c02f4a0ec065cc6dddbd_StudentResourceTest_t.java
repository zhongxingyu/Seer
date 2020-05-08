 package com.tor.services;
 
 import org.junit.Test;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.core.Response;
 
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

 /**
  * User: tor
  * Date: 16.12.13
  * Time: 16:23
  * To change this template use File | Settings | File Templates.
  */
 public class StudentResourceTest {
     @Test
     public void testResourceStudent() throws Exception {
         Client client = ClientBuilder.newClient();
         try {
             System.out.println("*** Create a new Student ***");
 
             String xml = "<student>"
                     + "<first-name>Bill</first-name>"
                     + "<last-name>Burke</last-name>"
                     + "<middle-name>Claren</middle-name>"
                     + "</student>";
 
             Response response = client.target("http://localhost:8080/services/student")
                     .request().post(Entity.xml(xml));
            if (response.getStatus() != HTTP_CREATED) throw new RuntimeException("Failed to create");   //201
             String location = response.getLocation().toString();
             System.out.println("Location: " + location);
             response.close();
 
             System.out.println("*** GET Created Customer **");
             String customer = client.target(location).request().get(String.class);
             System.out.println(customer);
 
             String updateCustomer = "<student>"
                     + "<first-name>William</first-name>"
                     + "<last-name>Burke</last-name>"
                     + "<middle-name>Claren2</middle-name>"
                     + "</student>";
             response = client.target(location).request().put(Entity.xml(updateCustomer));
            if (response.getStatus() != HTTP_NO_CONTENT) throw new RuntimeException("Failed to update"); //204
             response.close();
             System.out.println("**** After Update ***");
             customer = client.target(location).request().get(String.class);
             System.out.println(customer);
         } finally {
             client.close();
         }
     }
 }
