 package daos;
 
 import play.*;
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.db.ebean.Model.*;
 import play.data.format.*;
 import play.data.validation.*;
 import models.*;
 
 public class PostDAO extends AbstractDAO<Post>{
 	public PostDAO(){
 		super(Post.class);
 	}
 
 	public Post removeTag(Post post, Tag tag){
 		post.tags.remove(tag);
 		return super.save(post);
 	}
 
 	@Override
 	public Post get(Long id){
 		return super.find
 			.fetch("tags")
 			.fetch("author")
 			.where()
 			.eq("id", id)
 			.findUnique();
 	}
 
	@Override
	public Post save(Post post){
		post.tags = post.tags;
		super.save(post);
		Logger.info("saving many to many");
		post.saveManyToManyAssociations("tags");
		return post;
	}
 }
