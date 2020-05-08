 package com.ideas;
 
 import com.ideas.api.client.services.ideas.IdeasService;
 import com.ideas.entities.ideas.Idea;
 import com.ideas.entities.ideas.Ideas;
 import org.junit.Test;
 
 import java.util.UUID;
 
 import static org.junit.Assert.*;
 
 /**
  * Test the ideas service endpoints.
  */
 public class TestIdeasService
 {
 
     /**
      * Test getting an idea by ID
      *
      * @throws Exception
      */
     @Test
     public void testGetIdeaById ()
     {
         IdeasService service = new IdeasService();
         Idea idea = service.getIdeaById(1);
 
         assertEquals(
                 "Asserting that the ID of the returned idea is equal to the ID requested.",
                 1L,
                 idea.getId()
         );
     }
 
     /**
      * Test the idea text of a created idea.
      */
     @Test
     public void testIdeaTextOfCreatedIdea ()
     {
         UUID uuid = UUID.randomUUID();
         String ideaText = "Generated Idea Text : " + uuid.toString();
 
         IdeasService service = new IdeasService();
         Idea idea = new Idea();
         idea.setTitle("TestIdea");
         idea.setIdeaText(ideaText);
 
         Idea createdIdea = service.createIdea(idea);
 
         assertEquals(
                 "Asserting that the idea text that was created matches the idea text that was supposed to be created.",
                 idea.getIdeaText(),
                 createdIdea.getIdeaText()
         );
 
     }
 
     /**
      * Test the title of a created idea.
      */
     @Test
     public void testTitleOfCreatedIdea ()
     {
         UUID uuid = UUID.randomUUID();
         String ideaText = "Generated Idea Text : " + uuid.toString();
 
         IdeasService service = new IdeasService();
         Idea idea = new Idea();
         idea.setTitle("TestIdea");
         idea.setIdeaText(ideaText);
 
         Idea createdIdea = service.createIdea(idea);
 
         assertEquals(
                 "Asserting that the idea title that was created matches the idea title that was supposed to be created.",
                 idea.getTitle(),
                 createdIdea.getTitle()
         );
 
     }
 
    /**
     * Test getting latest ideas.
     */
     @Test
    public void testGetLatestIdeas()
     {
        UUID uuid = UUID.randomUUID();
        String ideaText = "Generated Idea Text : " + uuid.toString();

         IdeasService service = new IdeasService();
        Idea idea = new Idea();
        idea.setTitle("TestIdea");
        idea.setIdeaText(ideaText);
 
        service.createIdea(idea);

        Ideas ideas = service.getLatest(5);
 
         assertEquals(
                "Asserting that the latest idea is the latest one added by the API.",
                ideaText,
                ideas.getIdeaList().get(0).getIdeaText()
         );
     }
 }
