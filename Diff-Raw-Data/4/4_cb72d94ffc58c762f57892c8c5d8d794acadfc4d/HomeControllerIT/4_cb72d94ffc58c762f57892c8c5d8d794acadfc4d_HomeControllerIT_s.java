 package com.clickconcepts.web.controller;
 
import com.dynacrongroup.test.util.WebClientFactory;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import org.junit.Before;
 import org.junit.Test;
 
 import static java.lang.String.format;

 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.startsWith;
 
 /**
  * Integration test for Home Controller (runs with failsafe-plugin and requires container to be running
  */
 public class HomeControllerIT {
 
     private static final String BASE_HOST = System.getProperty("integration.server.host", "localhost");
     private static final String BASE_PORT = System.getProperty("integration.server.port", "8080");
     private static final String BASE_URL = format("http://%s:%s", BASE_HOST, BASE_PORT);
     private WebClient webClient;
 
     @Before
     public void setup() {
         webClient = WebClientFactory.getWebClient();
     }
 
     @Test
     public void testShowHomePage() throws Exception {
         String url = format("%s%s", BASE_URL, "/webapp/home");
 
         HtmlPage page = (HtmlPage) webClient.getPage(url);
 
         assertThat(page.getTitleText(), startsWith("Welcome to Sample Webapp"));
         assertThat(page.getByXPath("//*[@id='header']/h1"), notNullValue());
     }
 
 }
