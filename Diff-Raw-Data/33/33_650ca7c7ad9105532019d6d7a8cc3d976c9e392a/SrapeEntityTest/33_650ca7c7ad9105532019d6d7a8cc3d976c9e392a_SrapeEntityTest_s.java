 package me.donnior.srape;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class SrapeEntityTest {
 
     @Test
     public void test_srape_entity_value_1() {
         final User user = domainUser();
 
         FieldExposerModule module = new AbstractFieldExposerModule() {
             public void config() {
                 expose(user).withNameAndType("user", SummaryUserEntity.class);
             }
         };
 
         System.out.println(build(module));
     }
 
     @Test
     public void test_srape_entity_value_2() {
         final User user = domainUser();
 
         FieldExposerModule module = new AbstractFieldExposerModule() {
             public void config() {
                 expose(user).withNameAndType("user", DetailedUserEntity.class);
             }
         };
 
         System.out.println(build(module));
     }
 
     private User domainUser() {
         User user = new User();
         user.name = "donny";
         user.age = 30;
         
         Profile p = new Profile();
         p.address = "Beijing China";
         user.profile = p;
         
         List<Post> posts = new ArrayList<Post>();
         posts.add(new Post("post 1"));
         posts.add(new Post("post 2"));
         posts.add(new Post("post 3"));
         user.posts = posts;
         return user;
     }
 
     private String build(FieldExposerModule module) {
        long start = System.currentTimeMillis();
        String result = new JSONBuilder(module).build();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        return result;
     }
 
 }
