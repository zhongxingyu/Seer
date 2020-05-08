 package org.bloodtorrent.repository;
 
 import org.bloodtorrent.dto.BloodRequest;
 import org.bloodtorrent.testing.unitofwork.UnitOfWorkRule;
 import org.junit.Before;
 import org.junit.Rule;
 import org.bloodtorrent.dto.User;
 import org.junit.Test;
 
 import java.sql.Date;
 
 import static junit.framework.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 26
  * Time: 오전 11:54
  * To change this template use File | Settings | File Templates.
  */
 public class UsersRepositoryTest {
     @Rule
     public UnitOfWorkRule unitOfWorkRule = new UnitOfWorkRule("integration-test-configuration.json", User.class);
 
     private UsersRepository repository;
 
     @Before
     public void setup(){
         repository = new UsersRepository(unitOfWorkRule.getSessionFactory());
     }
 
     @Test
    public void testIsExistentId() throws Exception {
        boolean existAdminId = repository.isExistentId("Administrator@bloodtorrent.org");
        assertTrue(existAdminId);
    }

    @Test
     public void testInsert() throws Exception {
         User user = new User();
         user.setId("123");
         user.setBirthDay("2013-03-30");
         user.setAddress("address, city, state, India");
         user.setAnonymous(false);
         repository.insert(user);
     }
 
     @Test
     public void testGet() throws Exception {
         String id = "Administrator@bloodtorrent.org";
         repository.get(id);
     }
 }
