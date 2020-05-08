 package cz.cvut.fel.bupro.service;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fel.bupro.dao.ProjectRepository;
 import cz.cvut.fel.bupro.dao.SubjectRepository;
 import cz.cvut.fel.bupro.dao.UserRepository;
 import cz.cvut.fel.bupro.model.Comment;
 import cz.cvut.fel.bupro.model.Enrolment;
 import cz.cvut.fel.bupro.model.EnrolmentType;
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.model.Subject;
 import cz.cvut.fel.bupro.model.User;
 
 @Service
 public class InitDevelService {
 
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private UserRepository userRepository;
 	@Autowired
 	private ProjectRepository projectRepository;
 	@Autowired
 	private SubjectRepository subjectRepository;
 
 	@PostConstruct
 	@Transactional
 	public void initDevelData() {
 		log.info("Init Devel data");
 		User user1 = new User();
 		user1.setFirstName("Frantisek");
 		user1.setLastName("Vomacka");
 		user1.setEmail("vomacka@exmple.com");
 		user1.setUsername("vomacka");
 		userRepository.save(user1);
 
 		User user2 = new User();
 		user2.setFirstName("Venca");
 		user2.setLastName("Rohlik");
 		user2.setEmail("rohlik@exmple.com");
 		user2.setUsername("rohlik");
 		userRepository.save(user2);
 
 		Subject subject = new Subject();
 		subject.setName("X7 - Happy Subject");
 		Enrolment enrolment = new Enrolment();
 		enrolment.setEnrolmentType(EnrolmentType.TEACHER);
 		enrolment.setUser(user1);
 		enrolment.setSubject(subject);
 		subject.getEnrolments().add(enrolment);
 		subjectRepository.save(subject);
 
 		Project p1 = new Project();
 		p1.setName("Test 1");
		p1.setOwner(user1);		
 		p1.setSubject(subject);
 		Comment comment = new Comment();
 		comment.setUser(user1);
 		comment.setTitle("Some extra notes");
 		comment.setText("Dont forget to get extra reward for this project");
 		p1.add(comment);
 
 		projectRepository.save(p1);
 
		subject.getProjects().add(p1);
		p1.setSubject(subject);
		subjectRepository.save(subject);
		projectRepository.save(p1);

 		Project p2 = new Project();
 		p2.setName("Test 2");
 		p2.setOwner(user2);
 		p2.setSubject(subject);
 
 		projectRepository.save(p2);
 	}
 
 }
