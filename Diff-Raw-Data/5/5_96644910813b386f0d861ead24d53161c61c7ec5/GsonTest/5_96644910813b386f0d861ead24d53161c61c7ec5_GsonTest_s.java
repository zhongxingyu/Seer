 import java.util.LinkedList;
 import java.util.List;
 
 import models.Choice;
 import models.Poll;
 
 import org.junit.Test;
 
 import api.Request;
 import api.Request.CreateChoiceRequest;
 import api.Request.CreatePollRequest;
 import api.Response.CreatePollResponse;
 import api.entities.ChoiceJSON;
 import api.entities.PollJSON;
 import api.helpers.GsonHelper;
 
 import play.test.Fixtures;
 import play.test.UnitTest;
 
 public class GsonTest extends UnitTest {
 
 	@Test
     public void serializationTest() {
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("data.yml");
 		
 		List<Poll> polls = Poll.findAll();
     	assertEquals(polls.size(), 1);
     	
     	String json = GsonHelper.toJson(polls.get(0));
     	System.out.println(json);
     	
     	Poll p2 = GsonHelper.fromJson(json, Poll.class);
     	System.out.println(p2);
     	
     	assertEquals(polls.get(0).question, p2.question);
     	
     }
 	
 	@Test
     public void CreatePollRequestTest() {
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("data.yml");
 		
 		PollJSON p = new PollJSON();
 		p.question = "Strange question ...";
 		p.choices = new LinkedList<ChoiceJSON>();
 		
 		ChoiceJSON c1 = new ChoiceJSON();
 		c1.text = "Stupid answer.";
 		p.choices.add(c1);
 		
 		ChoiceJSON c2 = new ChoiceJSON();
 		c1.text = "... even worse";
 		p.choices.add(c2);
     	
     	CreatePollRequest r = new CreatePollRequest(p);
     	String json = GsonHelper.toJson(r);
     	
     	System.out.println();
     	System.out.println(json);
     	System.out.println();
     	
     }
 	
 	@Test
     public void CreatePollResponseTest() {
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("data.yml");
 		
 		Poll p = Poll.all().first();
		PollJSON pAsJson = Poll.toJson(p);
     	
		CreatePollResponse r = new CreatePollResponse(pAsJson);
     	String json = GsonHelper.toJson(r);
     	
     	System.out.println();
     	System.out.println(json);
     	System.out.println();
     	
     }
 	
 	@Test
     public void CreateChoiceRequestTest() {
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("data.yml");
 		
 		List<Choice> choices = Choice.findAll();
     	assertEquals(choices.size(), 2);
     	
     	Choice c = choices.get(0);
     	CreateChoiceRequest r = new CreateChoiceRequest(c);
     	String json = GsonHelper.toJson(r);
 
     	System.out.println();
     	System.out.println(json);
     	System.out.println();
     }
 
 
 	@Test
 	public void CreatePollRequestTest2() {
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("data.yml");
 
 		PollJSON p = new PollJSON();
 		p.question = "Strange question ...";
 		p.choices = new LinkedList<ChoiceJSON>();
 		
 		ChoiceJSON c1 = new ChoiceJSON();
 		c1.text = "Strange answer ..";
 		p.choices.add(c1);
 		
 		ChoiceJSON c2 = new ChoiceJSON();
 		c1.text = "... even worse";
 		p.choices.add(c2);
 		
 		CreatePollRequest r = new CreatePollRequest(p);
 		String json = GsonHelper.toJson(r);
 		
 		System.out.println(json);
 	}
 	/*
 	@Test
     public void CreatePollResponseTest2() {
         Response response = GET("/");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset("utf-8", response);
     }*/
 }
