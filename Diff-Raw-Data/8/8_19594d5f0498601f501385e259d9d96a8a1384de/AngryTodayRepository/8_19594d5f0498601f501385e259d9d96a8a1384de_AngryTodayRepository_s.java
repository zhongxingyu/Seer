 package com.jayway.angrytoday.repository;
 
 import com.jayway.angrytoday.domain.AngryPost;
 import com.jayway.angrytoday.dto.AngryPostDTO;
 
 import java.util.*;
 
 public class AngryTodayRepository {
 
     public static AngryTodayRepository INSTANCE = new AngryTodayRepository();
 
     private AngryTodayRepository() {
        posts = new ArrayList<AngryPost>();
         created = new HashMap<AngryPost, Date>();
         initializeTestData();
     }
 
    private List<AngryPost> posts;
     private Map<AngryPost, Date> created;
 
     public void clear() {
         posts.clear();
         created.clear();
     }
 
     private void initializeTestData() {
         add( new AngryPost( "Test angry... Am I angry today????" ) );
         add( new AngryPost("A"));
         add( new AngryPost("B"));
         add( new AngryPost("C"));
         add( new AngryPost("D"));
         add( new AngryPost("E"));
         add( new AngryPost("F"));
 
         add( new AngryPost( "I was stuck for two hours in the traffic today. I'm SO ANGRY!!!!") );
         add( new AngryPost( "Just plain old ANGRY today. It's Monday and everything sucks ASS!" ));
         AngryPost post = new AngryPost("Train conductors makes me angry! Just got a 500 DKK fine because I forgot to stamp my card...");
         post.addComment("Yes! That is sooooo annoying. Happened to me last week. ");
         post.addTag( "public transportation");
         add( post );
 
         add( new AngryPost( "My car! My G** D*** car broke down. WTF! Now I need to get a new one or get it to an auto shop... I'm broke").addTag( "car") );
 
         add( new AngryPost("Choose the wrong line at the supermarket. Waited soo long...").addTag("waiting").addTag("supermarket"));
 
     }
 
     public void add( AngryPost post ) {
        posts.add(post);
         created.put( post, new Date() );
     }
 
     public List<AngryPost> posts() {
         return posts;
     }
 
     public AngryPostDTO toDto( AngryPost post ) {
         AngryPostDTO dto = new AngryPostDTO();
         dto.setId( post.getId() );
         dto.setMessage( post.getMessage() );
         dto.setComments( post.getComments() );
         dto.setTags( post.getTags() );
         dto.setCreated( created.get( post ));
         return dto;
     }
 
 }
