 package no.niths.infrastructure;
 
 import static org.junit.Assert.assertEquals;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Committee;
 import no.niths.domain.Event;
 import no.niths.domain.Student;
 import no.niths.infrastructure.interfaces.CommitteeRepositorty;
 import no.niths.infrastructure.interfaces.StudentRepository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes= { TestAppConfig.class, HibernateConfig.class})
 @Transactional
 @TransactionConfiguration(transactionManager = "transactionManager")  
 public class CommitteeRepositoryTest {
 
 	@Autowired
 	private CommitteeRepositorty committeeRepo;
 	
 	@Autowired
 	private StudentRepository studentRepo;
 	
 	
 	@Test
 	public void testAddLeader(){
 		Student s1 = new Student("John", "Doe");
		s1.setEmail("mail@mail.com");
 		Student s2 = new Student("Jane", "Doe");
		s2.setEmail("mail2@mail.com");
 		studentRepo.create(s1);
 		studentRepo.create(s2);
 		
 		Committee c1 = new Committee("Linux", "1337");
 		c1.getLeaders().add(s1);
 		committeeRepo.create(c1);
 		
 		assertEquals(1, c1.getLeaders().size());
 		
 		Student s3 = studentRepo.getById(s1.getId());
 		assertEquals(0, s3.getCommittees().size());
 		
 	}
 			
 	@Test
 	public void testCRUD() {
 		int size = committeeRepo.getAll(null).size();
 		Committee committee = new Committee("LUG", "Linux");
 
 		committee.setId(committeeRepo.create(committee));
 		assertEquals(size + 1, committeeRepo.getAll(null).size());
 		assertEquals(committee, committeeRepo.getById(committee.getId()));
 	
 		committee.setName("LINUXs");
 		committeeRepo.update(committee);
 
 		assertEquals(committee, committeeRepo.getById(committee.getId()));
 		
 		committeeRepo.delete(committee.getId());
 		
 		assertEquals(size, committeeRepo.getAll(null).size());
 	}
 	
 
 	@Test
 	public void testGetAllWithCreateCritera(){
 		
 		Committee c1 = new Committee("LUG", "23");
 		Committee c2 = new Committee("LAG", "Linux");
 		Committee c3 = new Committee("ads", "Linux");
 		
 		committeeRepo.create(c1);
 		committeeRepo.create(c2);
 		committeeRepo.create(c3);
 		
 		
 		c1.setDescription(null);
 		assertEquals(1, committeeRepo.getAll(c1).size());
 	
 		c3.setName(null);
 		assertEquals(2, committeeRepo.getAll(c3).size());
 	}
 	
 	
 	@Test
 	public void testEventJoin(){
 		
 		Event event = new Event();
 		event.setName("Joe");
 		Committee committee = new Committee("LUG", "Linux");
 		committee.getEvents().add(event);
 		
 		committeeRepo.create(committee);
 		Committee temp = committeeRepo.getById(committee.getId());
 		assertEquals(committee, temp);
 		
 		assertEquals(1, temp.getEvents().size());
 		
 	}
 }
