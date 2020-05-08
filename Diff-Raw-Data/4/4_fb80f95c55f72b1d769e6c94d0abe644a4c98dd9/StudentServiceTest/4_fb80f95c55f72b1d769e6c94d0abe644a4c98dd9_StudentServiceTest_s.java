 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Committee;
 import no.niths.domain.Course;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.services.interfaces.CommitteeService;
 import no.niths.services.interfaces.CourseService;
 import no.niths.services.interfaces.RoleService;
 import no.niths.services.interfaces.StudentService;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 //@Transactional
 //@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
 public class StudentServiceTest {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(StudentServiceTest.class);
 
 	@Autowired
 	private StudentService studService;
 	
 	@Autowired
 	private CourseService courseService;
 	
 	@Autowired
 	private RoleService roleService;
 	
 	@Autowired
 	private CommitteeService comService;
 
 	@Test
 	@Rollback(true)
 	public void testCRUD() {
 
 		ArrayList<Student> students = (ArrayList<Student>) studService.getAll(null);
 		for (int i = 0; i < students.size(); i++) {
 			studService.hibernateDelete(students.get(i).getId());
 		}
 
 		
 		
 		assertEquals(true, studService.getAll(null).isEmpty());
 
 		// Testing create
 		Student s = new Student("John", "Doe");
 		Student x = new Student("Vera", "Fine");
 		s.setEmail("mail@mil.com");
 		x.setEmail("mail2@mal.com");
 
 		studService.create(s);
 		studService.create(x);
 
 		// Testing get
 		assertEquals(2, studService.getAll(null).size());
 		assertEquals(s, studService.getById(s.getId()));
 
 		// Testing update
 
 		assertEquals("mail@mil.com", studService.getById(s.getId()).getEmail());
 
 		s.setEmail("john@doe.com");
 		studService.update(s);
 		assertEquals("john@doe.com", studService.getById(s.getId()).getEmail());
 
 		// Testing delete //Don't work in same transaction
 		studService.hibernateDelete(s.getId());
 
 		//assertTrue(isDeleted);
 		assertEquals(1, studService.getAll(null).size());
 		
 		studService.hibernateDelete(x.getId());
 		assertEquals(true, studService.getAll(null).isEmpty());
 	}
 	
 	@Test
 	public void testStudentCoursesRelationship(){
 		Course c = new Course("Navn", "Desc");
 		courseService.create(c);
 		Course c2 = new Course("Navaa", "Descen");
 		courseService.create(c2);
 		
 		Student s = new Student("xxxx@nith.no");
 		s.getCourses().add(c);
 		s.getCourses().add(c2);
 		studService.create(s);
 		assertEquals(2, studService.getById(s.getId()).getCourses().size());
 		
 		//Delete one course
 		courseService.hibernateDelete(c.getId());
 		//Course should be deleted on student
 		assertEquals(1, studService.getById(s.getId()).getCourses().size());
 		//Delete the student
 		studService.hibernateDelete(s.getId());
 		//Course should still be there
 		assertEquals(c2, courseService.getById(c2.getId()));
 		
 		courseService.hibernateDelete(c2.getId());
 		
 	}
 	
 	@Test
 	public void testStudentRoleRelationship(){
 		Student s = new Student("xxxxx@nith.no");
 		studService.create(s);
 		Student fetched = studService.getById(s.getId());
 		assertEquals(s, fetched);
 		int studentRoles = fetched.getRoles().size();
 		int numOfRoles = roleService.getAll(null).size();
 		
		Role r1 = new Role("ROLE_TEST");
		Role r2 = new Role("ROLE_TEST2");
 		roleService.create(r1);
 		roleService.create(r2);
 		assertEquals(numOfRoles + 2,  roleService.getAll(null).size());
 		
 		fetched.getRoles().add(r1);
 		fetched.getRoles().add(r2);
 		studService.update(fetched);
 		fetched = studService.getById(s.getId());
 		assertEquals(studentRoles + 2, fetched.getRoles().size());
 		
 		//Delete a ROLE
 		roleService.hibernateDelete(r1.getId());
 		//Should be removed from student!
 		fetched = studService.getById(s.getId());
 		assertEquals(studentRoles + 1, fetched.getRoles().size());
 		
 		//Delete the student
 		studService.hibernateDelete(s.getId());
 		//Role should still exist!
 		Role r2Copy = roleService.getById(r2.getId());
 		assertEquals(r2, r2Copy);
 		
 		//Cleanup
 		roleService.hibernateDelete(r2.getId());
 		
 		assertEquals(numOfRoles,  roleService.getAll(null).size());
 	}
 	
 	@Test
 	public void testStudentCommitteesRelationship(){
 		Student s = new Student("xxx@nith.no");
 		studService.create(s);
 		int comSize = comService.getAll(null).size();
 		Committee c1 = new Committee();
 		Committee c2 = new Committee();
 		comService.create(c1);
 		comService.create(c2);
 		assertEquals(comSize + 2, comService.getAll(null).size());
 		
 		Student fetched = studService.getById(s.getId());
 		fetched.getCommittees().add(c1);
 		fetched.getCommittees().add(c2);
 		studService.update(fetched);
 		fetched = studService.getById(s.getId());
 		assertEquals(2, fetched.getCommittees().size());
 		
 		//Delete a committee
 		comService.hibernateDelete(c1.getId());
 		assertEquals(comSize + 1, comService.getAll(null).size());
 		//Should be removed from student
 		fetched = studService.getById(s.getId());
 		assertEquals(1, fetched.getCommittees().size());
 		
 		//Delete student
 		studService.hibernateDelete(s.getId());
 		//Course should still exist
 		assertEquals(c2, comService.getById(c2.getId()));
 		
 		comService.hibernateDelete(c2.getId());
 		assertEquals(comSize, comService.getAll(null).size());
 	}
 }
