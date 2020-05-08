 package name.felixbecker.brokenaop.services;
 
 import java.util.List;
 

 import name.felixbecker.brokenaop.entities.TweetBO;
 
import org.springframework.transaction.annotation.Transactional;


 public interface TweetService {
 
	@Transactional
 	public List<TweetBO> getAllTweets();
 }
