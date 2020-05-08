 package com.lateralthoughts.devinlove.repository;
 
import static com.lateralthoughts.devinlove.domain.ProfoundIdentity.DEVELOPER;
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.lateralthoughts.devinlove.domain.Mascot;
 import com.lateralthoughts.devinlove.domain.Person;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:META-INF/applicationContext.xml")
 @Transactional
 public class PersonRepositoryTest {
 
 	@Autowired
 	private PersonRepository personRepository;
 	private Person person;
 
 	@Before
 	public void setup() {
 		person = new Person();
 		person.setFirstName("Florent");
 		person.setLastName("Biville");
 		person.setShoeSize(42);
 		person.setProfoundIdentity(DEVELOPER);
 		person.setFavoriteColor("Blue");
 		Mascot mascot = new Mascot();
 		mascot.setName("Tux");
 		person.setMascot(mascot);
 	}
 
 	@Test
 	public void when_inserting_then_retrievable() {
 		assertThat(personRepository).isNotNull();
 		personRepository.save(person);
 		assertThat(person.getId()).isGreaterThan(0L);
 	}
 }
