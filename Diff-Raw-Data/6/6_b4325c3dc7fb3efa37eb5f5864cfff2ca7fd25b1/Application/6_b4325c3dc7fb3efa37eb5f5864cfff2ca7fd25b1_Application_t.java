 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
	@Before
	static void addDefaults() {
		renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
		renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
	}

     public static void index() {
     	Post frontPost = Post.find("order by postedAt desc").first();
     	List<Post> olderPosts = Post.find(
     		"order by PostedAt desc"
     	).from(1).fetch(10);
         render(frontPost, olderPosts);
     }
 
 }
