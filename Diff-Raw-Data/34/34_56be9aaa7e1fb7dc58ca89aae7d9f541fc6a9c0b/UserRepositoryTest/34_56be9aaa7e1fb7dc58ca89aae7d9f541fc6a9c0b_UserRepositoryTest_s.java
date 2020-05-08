 package hibernate;
 
 import static org.junit.Assert.assertNotNull;
 import hibernate.entities.EventEntitie;
 import hibernate.entities.UserEntitie;
 import hibernate.repositories.UserRepository;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import common.EventTypeE;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="classpath:META-INF/application-context.xml")
 //@ContextConfiguration(locations="classpath:META-INF/test-context.xml")
 public class UserRepositoryTest {
 
 	@Autowired
 	UserRepository userRepository;
 	
 	public UserRepository getUserRepository() {
 		return userRepository;
 	}
 
 	public void setUserRepository(UserRepository userRepository) {
 		this.userRepository = userRepository;
 	}
 
 	@Test
 	public void InsertUser() {
 		
 		UserEntitie user = new UserEntitie();
 		user.setName("Szymon");
 		user.setSurname("Stefan");
 		
 		userRepository.save(user);
 		
 		UserEntitie dbpost = userRepository.findOne(user.getUserId());
 		assertNotNull(dbpost);
 		System.out.println(dbpost.getUserId()+" "+dbpost.getName()+" "+dbpost.getSurname());
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Test
 	public void InsertUserEvent() {
 		UserEntitie user = new UserEntitie();
 		user.setName("Hakuna");
 		user.setSurname("Matata");
 		
 		Set<EventEntitie> events = new HashSet<EventEntitie>();
 		EventEntitie event = new EventEntitie();
 		event.setEventStart(new Date());
 		event.setEventStop(new Date());
 		event.setEventType(EventTypeE.bear);
 		event.setUser(user);
 		
 		events.add(event);
 		
 		user.setEvents(events);
 		
 		userRepository.save(user);
 		
 		UserEntitie dbpost = userRepository.findOne(user.getUserId());
 		assertNotNull(dbpost);
 		assertNotNull(dbpost.getEvents());
 		Assert.assertEquals(1, dbpost.getEvents().size());
 		System.out.println(dbpost.getUserId()+" "+dbpost.getName()+" "+dbpost.getSurname());
 	}
 
 }
