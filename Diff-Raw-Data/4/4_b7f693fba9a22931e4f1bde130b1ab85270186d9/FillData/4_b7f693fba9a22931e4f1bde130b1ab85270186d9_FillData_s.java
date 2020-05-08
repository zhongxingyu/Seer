/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package evaluationserver.server.execution;
 
 import evaluationserver.server.entities.Category;
 import evaluationserver.server.entities.Competition;
 import evaluationserver.server.entities.File;
 import evaluationserver.server.entities.Language;
 import evaluationserver.server.entities.Role;
 import evaluationserver.server.entities.Solution;
 import evaluationserver.server.entities.SystemReply;
 import evaluationserver.server.entities.Task;
 import evaluationserver.server.entities.User;
 import evaluationserver.server.entities.UserGroup;
 import java.util.Date;
 import javax.persistence.EntityManager;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 
 /**
  *
  * @author danrob
  */
 public class FillData {
 	public static void main(String[] args) {
 
 		String program = "#include <stdio.h>\nint main()\n{\nprintf(\"Hello World!\");\nreturn 0;\n}";
 		
 		Resource resource = new FileSystemResource("configuration.xml");
 		BeanFactory factory = new XmlBeanFactory(resource);
 		
 		// get server instance
 		EntityManager em = factory.getBean(EntityManager.class);
 		
 		Category category1 = new Category(null, "Testing category");
 		Category category2 = new Category(null, "Soutez category");
 		
 		Role roleAdmin = new Role(null, "admin", "admin");
 		Role roleUser = new Role(null, "user", "user");
 		
 		User userAdmin = new User(null, "admin", "admin", true, new Date()).setRole(roleAdmin);
 		User userUser = new User(null, "admin", "admin", true, new Date()).setRole(roleUser).setCreator(userAdmin);
 		
 		Language langC = new Language(null, "C", "c", "c");
 		Language langCpp = new Language(null, "C++", "cpp", "cpp");
 		
 		UserGroup group = new UserGroup(null, "group", true, "skupina").addUser(userUser);
 		
 		SystemReply srAccept = new SystemReply(null, "Accepted", true, "accept");
 		SystemReply srError = new SystemReply(null, "Error", false, "error");
 		
 		File fileResultResolver = new File(null, "diff", 10000).setData("diff program".getBytes());
 
 		Task task = new Task(null, "Task", "...", 3000, 50000, 5000000, 5000000, new Date()).setCategory(category1).setCreator(userAdmin).setResultResolver(fileResultResolver);
 		
 		Competition competition = new Competition(null, "Competition", 0, true, new Date()).addTask(task).setCreator(userAdmin).addUserGroup(group);
 		
 		File fileSolution = new File(null, "solution.c", 10000).setData(program.getBytes());
 		File fileSolution2 = new File(null, "solution2.c", 10000).setData(program.getBytes());
 		Solution solution = new Solution(null, new Date()).setCompetition(competition).setLanguage(langC).setTask(task).setFile(fileSolution).setUser(userUser);
 		Solution solution2 = new Solution(null, new Date()).setCompetition(competition).setLanguage(langC).setTask(task).setFile(fileSolution2).setUser(userUser);
 		
 		Object[] entities = {
 			category1,
 			category2,
 			roleAdmin,
 			roleUser,
 			userAdmin,
 			userUser,
 			langC,
 			langCpp,
 			group,
 			srAccept,
 			srError,
 			fileResultResolver,
 			task,
 			competition,
 			fileSolution,
 			solution,
 			fileSolution2,
 			solution2
 		};
 		
 		em.getTransaction().begin();
 		for(Object o : entities)
 			em.persist(o);
 		em.flush();
 		em.getTransaction().commit();
 	}	
 }
