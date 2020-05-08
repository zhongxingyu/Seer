 package no.niths.application.rest;
 
 import no.niths.application.rest.interfaces.RoleController;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.infrastructure.interfaces.StudentRepository;
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 @Transactional
 @TransactionConfiguration(
         transactionManager = TestAppConfig.TRANSACTION_MANAGER)
 public class RoleControllerTest {
 	
 	@Autowired
 	private RoleController roleController;
 	
 	@Autowired
 	private StudentRepository studRepo;
 	
 	@Test
 	public void testAddAndRemoveRolesToAndFromStudent(){
 		Student stud = new Student("stud@nith.no");
 		studRepo.create(stud);
 		
 		Student fetched = studRepo.getById(stud.getId());
 		assertEquals(stud, fetched);
 		
 		assertEquals(true, fetched.getRoles().isEmpty());
 		
		int roleSize = roleController.getAll(null).size();
		
 		//Test of add
 		Role role = new Role();
 		role.setRoleName("ROLE_TEST");
 		
 		roleController.create(role);
 		
		assertEquals(roleSize + 1, roleController.getAll(null).size());
 		
 		roleController.addStudentRole(stud.getId(), role.getId());
 		
 		assertEquals(1, stud.getRoles().size());
 		
 		Role role2 = new Role();
 		role.setRoleName("ROLE_TEST2");
 		roleController.create(role2);
 		
 		roleController.addStudentRole(stud.getId(), role2.getId());
 		assertEquals(2, stud.getRoles().size());
 		
 		//Test of remove
 		roleController.removeStudentRole(stud.getId(), role.getId());
 		assertEquals(1, stud.getRoles().size());
 		
 		roleController.removeAllRolesFromStudent(stud.getId());
 		assertEquals(true, stud.getRoles().isEmpty());
 
 		
 	}
 
 }
