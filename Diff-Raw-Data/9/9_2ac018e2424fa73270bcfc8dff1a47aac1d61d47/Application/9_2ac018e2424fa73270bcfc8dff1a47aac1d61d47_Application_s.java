 package controllers;
 
 import play.*;
 import play.cache.Cache;
 import play.data.validation.Required;
 import play.libs.Codec;
 import play.libs.Images;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 	private static int HOME_POST_COUNT = 10;
 
 	@Before
 	static void addDefaults() {
 	    renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
 	    renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
 	}
 	
 	public static void index() {
     	System.out.println("Rendering Index page");
     	
     	Post frontPost = Post.find("order by postedAt desc").first();
     	List<Post> olderPosts = Post.find("order by postedAt desc").from(1).fetch(HOME_POST_COUNT);
         
     	render(frontPost, olderPosts);
     }
 	
 	public static void showPost(Long id) {
 		Post post = Post.findById(id);
 		String randomID = Codec.UUID();
 		
 		System.out.println("Showing post with tags: " + post.tags.toString());
 		
 		System.out.println("Generated captcha id: " + randomID);
 		
		render(post, randomID);
 	}
 	
 	public static void postComment(Long postId, 
 	        @Required(message="Author is required") String author, 
 	        @Required(message="A message is required") String content, 
 	        @Required(message="Please type the code") String code, 
 	        String randomID) {
 	    Post post = Post.findById(postId);
 	    
 		if (post == null) {
 			index();
 			return;
 		}
 		
 		validation.equals(
 	        code, Cache.get(randomID)
 	    ).message("Invalid code. Please type it again");
 
 		Cache.delete(randomID);
 		
 	    if (validation.hasErrors()) {
	    	showPost(postId);
 	    }
 	    
 	    post.addComment(author, content);
 	    flash.success("Thanks for posting %s", author);	    
 	    showPost(postId);
 	}
 	
 	public static void removeComment(Long id) {
 		Comment comment = Comment.findById(id);
 		
 		if (comment == null) {
 			index();
 			return;
 		}
 		
 		Post post = comment.post;
 		
 		comment.delete();
 		flash.success("Comment was deleted.");
 		
 		System.out.println("Removing comment " + comment.id);
 		showPost(post.id);
 	}
 	
 	public static void captcha(String id) {
 		Images.Captcha captcha = Images.captcha();
 	    String code = captcha.getText("#E4EAFD");
 	    Cache.set(id, code, "10mn");
 	    renderBinary(captcha);
 	}
 	
 	public static void listTagged(String tag) {
 	    List<Post> posts = Post.findTaggedWith(tag);
 	    render(tag, posts);
 	}
 	
 }
