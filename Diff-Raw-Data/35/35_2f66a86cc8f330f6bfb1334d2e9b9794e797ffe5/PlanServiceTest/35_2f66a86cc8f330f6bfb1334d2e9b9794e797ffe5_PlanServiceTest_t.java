 package com.socialcomputing.wps.server.webservices.maker;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Rule;
 import org.junit.Test;
 
 import com.socialcomputing.junit.rules.GuiceWebResource;
 import com.socialcomputing.junit.rules.JettyServletContainerRule;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 
 public final class PlanServiceTest {
     
     @Rule
     public JettyServletContainerRule jettyRule = new JettyServletContainerRule(9999);
     
     @Test
     public void getPlanAsJSON() {
         Client client = Client.create();
         WebResource wr = client.resource("http://localhost:9999/")
                                .path("services/plan/Solr_sample")
                                .queryParam("analysisProfile", "GlobalProfile")
                                .queryParam("q", "salaire")
                                .queryParam("maxResults", "30");
         System.out.println(wr.get(String.class));
         assertTrue(true);
     }
 }
