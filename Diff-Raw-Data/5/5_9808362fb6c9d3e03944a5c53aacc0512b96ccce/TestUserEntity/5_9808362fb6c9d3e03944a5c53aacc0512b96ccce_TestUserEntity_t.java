 
 package test.axirassa.domain;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import axirassa.ioc.IocIntegrationTestRunner;
 import axirassa.model.UserEntity;
 import axirassa.model.exception.NoSaltException;
 
 @RunWith(IocIntegrationTestRunner.class)
 public class TestUserEntity {
 
 	@Inject
 	private Session database;
 
 
 	@Test
 	public void userPassword () throws NoSaltException {
 		database.beginTransaction();
 
 		UserEntity user = new UserEntity();
		user.createPassword("blah");
 
 		assertTrue(user.matchPassword("blah"));
 		assertFalse(user.matchPassword("blah "));
 		assertFalse(user.matchPassword("blah123"));
 		assertFalse(user.matchPassword("tweedle"));
 
 		database.getTransaction().commit();
 	}
 
 
 	@Test
 	public void userAutomaticSalting () throws NoSaltException {
 		database.beginTransaction();
 		UserEntity user = new UserEntity();
 		long start = System.currentTimeMillis();
 		user.createPassword("password");
 		System.out.println("PASSWORD IN: " + (System.currentTimeMillis() - start));
		database.persist(user);
 		database.getTransaction().commit();
 	}
 
 }
