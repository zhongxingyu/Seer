 package in.shekhar.forumapp.repository;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import in.shekhar.forumapp.config.ApplicationConfig;
 import in.shekhar.forumapp.domain.Message;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = ApplicationConfig.class)
 @ActiveProfiles("dev")
 @Transactional
 public class MessageRepositoryTest {
 
 	@Autowired
 	private MessageRepository messageRepository;
 	
 	@Test
 	public void testFindByMessageAuthor() {
 		
 		Message message = new Message("test_user", "hello world");
 		
 		messageRepository.save(message);
 		
 		List<Message> messages = messageRepository.findByMessageAuthor("test_user");
 		
 		assertEquals(1, messages.size());
 	}
 
 }
