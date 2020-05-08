 package org.diveintojee.codestory.steps;
 
 import com.google.common.collect.ImmutableMap;
 import com.sun.jersey.api.client.ClientResponse;
 import org.diveintojee.codestory2013.QuestionsResource;
 import org.diveintojee.codestory2013.ScalaskelResource;
 import org.hamcrest.Matchers;
 import org.jbehave.core.annotations.Named;
 import org.jbehave.core.annotations.Pending;
 import org.jbehave.core.annotations.Then;
 import org.jbehave.core.annotations.When;
 import org.jbehave.core.model.ExamplesTable;
 import org.jbehave.core.model.OutcomesTable;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author louis.gueye@gmail.com
  */
 public class ScalaskelSteps extends BackendBaseSteps {
     public ScalaskelSteps(Exchange exchange) {
         super(exchange);
     }
     @When("the server is asked the scalaskel change for value \"$id\"")
     public void getChange(@Named("id") String id) {
         this.exchange.getRequest().setType("*/*");
         this.exchange.getRequest().setRequestedType(MediaType.APPLICATION_JSON);
        final String uri = UriBuilder.fromResource(ScalaskelResource.class).path("/change").path(id) .build().toString();
         this.exchange.getRequest().setUri(uri);
         this.exchange.sendGetRequest();
     }
 
     @Override
     public Map<String, String> actualRow(ClientResponse clientResponse) {
         ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
         builder.put("code", String.valueOf(clientResponse.getStatus()));
         builder.put("body", String.valueOf(clientResponse.getEntity(String.class)));
         return builder.build();
     }
 
 }
