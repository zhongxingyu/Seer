 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import service.MorphiaObject;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 
 @Entity
 public class Post {
 	
 	@Id
 	public ObjectId id;
 	public String url;
 	public List<String> to = new ArrayList<String>();
 	public String from;
 	public String fromName;
 	
 	public Post(List<String> to, User from, String url) {
 		this.to.addAll(to);
 		this.from = from.uid;
 		this.url  = url;
 	}
 	
 	public static List<Post> all() {
 		return MorphiaObject.datastore.find(Post.class).asList();
 	}
 
 	public static Post findById(String id){
 		return MorphiaObject.datastore.find(Post.class).field("_id").equal(new ObjectId(id)).get();
 	}
 	
 	@Override
 	public String toString() {
 		return "Post [url=" + url + ", to=" + to + ", from=" + from
 				+ ", fromName=" + fromName + "]";
 	}
 }
