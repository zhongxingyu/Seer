 package com.maistora.spring.demo.repositories;
 
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.maistora.spring.demo.AbstractTest;
 import com.maistora.spring.demo.database.entities.UserEntity;
 import com.maistora.spring.demo.database.repositories.UserEntityRepository;
 
 public class UserEntityRepositoryTest extends AbstractTest {
 	
 	@Autowired
 	private UserEntityRepository repo;
 	
 	@Test
 	public void testUserPersistence() {
 		final UserEntity user = new UserEntity();
 		user.setName("Test Name");
 		user.setUsername("Test username");
 		user.setEmail("myemail@mail.com");
 		user.setPassword("asdfasdf");
 		
 		repo.save(user);
 		
 		// here we have 'user' persisted in the DB and it has an ID set already.
 		final UserEntity dbUser = repo.findOne(user.getUserId());
 		assertNotNull(dbUser);
 		
 		System.out.println(dbUser);
 	}
 	
 }
