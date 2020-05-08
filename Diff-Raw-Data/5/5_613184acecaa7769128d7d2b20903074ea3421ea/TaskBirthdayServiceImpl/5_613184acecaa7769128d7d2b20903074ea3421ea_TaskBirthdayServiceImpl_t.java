 package ch.hszt.mdp.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.springframework.scheduling.quartz.QuartzJobBean;
 import org.springframework.stereotype.Service;
 
 import ch.hszt.mdp.dao.TaskBirthdayDao;
 import ch.hszt.mdp.domain.Friendship;
 import ch.hszt.mdp.domain.User;
 
 @Service
 public class TaskBirthdayServiceImpl extends QuartzJobBean{
 	
 	private TaskBirthdayDao taskBirthdayDao;
 	private String email;
 	private UserService service;
 	
 	
 	public TaskBirthdayServiceImpl(TaskBirthdayDao taskBirthdayDao, String email){
 		this.taskBirthdayDao = taskBirthdayDao;
 		this.email=email;
 	}
 
 
 	protected void executeInternal(JobExecutionContext arg0)
 			throws JobExecutionException {
		User emailUser = service.getUserByEmail(email);
 		
		List<Friendship> friends= service.getAccepteFriendships(emailUser);
 		List<User> users = new ArrayList<User>();
 		
 		for (Friendship friend:friends){
 			if (friend.getSecondaryUser().getBirthdate().isEqualNow()){
 				User user = service.getUser(friend.getSecondaryUser().getId());
 				users.add(user);
 			}
 		}
 		
 		taskBirthdayDao.postHappyBirthday(users);
 		
 	}
 }
