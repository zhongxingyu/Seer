 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.arquillian.test;
 
 import java.io.File;
 import java.net.URL;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.drone.api.annotation.Drone;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 /**
  *
  * @author Marcin Wisniewski
  */
 @RunWith(Arquillian.class)
 public class IndexTest {
     
     @ArquillianResource
     static URL url;
 
     @Drone
     WebDriver browser;
     
    @Deployment
     public static WebArchive createDeployment() {
         WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
                 .addAsWebResource(new File("src/main/webapp", "index.jsp"));
         return war;
     }
     
     @Test
     public void shouldRenderHelloWorld() {
         browser.get(url.toString());
         final WebElement h1 = browser.findElement(By.tagName("h1"));
         Assert.assertNotNull("Unable to find h1", h1);
         Assert.assertEquals("Hello World!", h1.getText());
     }
     
 }
