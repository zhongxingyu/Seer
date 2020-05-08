 package by.itransition.fanfic.service.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import by.itransition.fanfic.dao.CommentDao;
 import by.itransition.fanfic.dao.FanficDao;
 import by.itransition.fanfic.dao.RoleDao;
 import by.itransition.fanfic.dao.UnregisteredUserDao;
 import by.itransition.fanfic.dao.UserDao;
 import by.itransition.fanfic.dao.VoteDao;
 import by.itransition.fanfic.domain.Comment;
 import by.itransition.fanfic.domain.Fanfic;
 import by.itransition.fanfic.domain.UnregisteredUser;
 import by.itransition.fanfic.domain.User;
 import by.itransition.fanfic.domain.Vote;
 import by.itransition.fanfic.service.UserService;
 
 /**
  * Class that represent implementation of UserService. 
  */
 @Service
 public class UserServiceImpl implements UserService {
 
 	@Autowired
 	private UserDao userDao;
 
 	@Autowired
 	private UnregisteredUserDao unregisteredUserDao;
 	
 	@Autowired
 	private RoleDao roleDao;
 	
 	@Autowired
 	private CommentDao commentDao;
 	
 	@Autowired
 	private FanficDao fanficDao;
 	
 	@Autowired
 	private VoteDao voteDao;
 
 	@Override
 	@Transactional
 	public User login(String name, String password) {
 		User user = userDao.getUserByName(name);
 		if (null != user && user.getPassword().equals(password)) {
 			return user;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	@Transactional
 	public int register(User user) {
 		UnregisteredUser unregisteredUser = new UnregisteredUser();
 		unregisteredUser.setEmail(user.getEmail());
 		unregisteredUser.setPassword(user.getPassword());
 		unregisteredUser.setName(user.getUsername());
 		unregisteredUserDao.save(unregisteredUser);
 		return unregisteredUser.getId();
 	}
 
 	@Override
 	@Transactional
 	public void confirmRegistration(int id) {
 		UnregisteredUser unregisteredUser = unregisteredUserDao.getUnregisteredUserById(id);
 		if (null != unregisteredUser) {
 			unregisteredUserDao.remove(unregisteredUser);
 			User user = new User();
 			user.setUsername(unregisteredUser.getName());
 			user.setEmail(unregisteredUser.getEmail());
 			user.setPassword(unregisteredUser.getPassword());
 			user.addRole(roleDao.getRoleByName("ROLE_USER"));
 			userDao.save(user);
 		}
 	}
 
 	@Override
 	@Transactional
 	public List<User> getAllUsers() {
 		return userDao.getAllUsers();
 	}
 
 	@Override
 	@Transactional
 	public void save(User user) {
 		userDao.save(user);
 	}
 
 	@Override
 	@Transactional
 	public void remove(User user) {
		List<Fanfic> fanfics = user.getFanfics();
 		for (Fanfic fanfic : fanfics) {
 			removeFanfic(user, fanfic);
 		}
 		for (Comment comment : user.getComments()) {
 			comment.setAuthor(null);
 			Fanfic fanfic = comment.getFanfic();
 			fanfic.getComments().remove(comment);
 			comment.setFanfic(null);
 			commentDao.save(comment);
 			fanficDao.save(fanfic);
 		}
 		List<Comment> comments = user.getComments();
 		user.setComments(null);
 		for (Comment comment : comments) {
 			commentDao.remove(comment);
 		}
 		for (Vote vote : user.getVotes()) {
 			vote.setUser(null);
 			Fanfic fanfic = vote.getFanfic();
 			fanfic.getVotes().remove(vote);
 			vote.setFanfic(null);
 			voteDao.save(vote);
 			fanficDao.save(fanfic);
 		}
 		List<Vote> votes = user.getVotes();
 		user.setVotes(null);
 		for (Vote vote : votes) {
 			voteDao.remove(vote);
 		}
 		userDao.save(user);
 		userDao.remove(user);
 	}
 
 	@Override
 	@Transactional
 	public User getUserById(int id) {
 		return userDao.getUserById(id);
 	}
 
 	@Override
 	@Transactional
 	public User getUserByName(String name) {
 		return userDao.getUserByName(name);
 	}
 
 	@Override
 	@Transactional
 	public void removeUserById(int id) {
 		remove(getUserById(id));
 	}
 
 	@Override
 	@Transactional
 	public boolean isRegistered(String name, String password) {
 		User user = userDao.getUserByName(name);
 		return null != user && user.getPassword().equals(password);
 	}
 
 	@Override
 	@Transactional
 	public boolean isRegistered(String name) {
 		return null != userDao.getUserByName(name);
 	}
 	
 	@Override
 	@Transactional
 	public List<Integer> getStatistics() {
 		List<Integer> answer = new ArrayList<Integer>();
 		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
 		Calendar calendar = Calendar.getInstance();
 		calendar.clear(Calendar.MINUTE);
 		calendar.clear(Calendar.SECOND);
 		calendar.clear(Calendar.MILLISECOND);
 		for (int hoursBeforeNow = 0; hoursBeforeNow < 10; ++hoursBeforeNow) {
 			for (User user : userDao.getAllUsers()) {
 				Date userDate = user.getDateOfRegistration();
 				Calendar userCalendar = Calendar.getInstance();
 				userCalendar.setTime(userDate);
 				userCalendar.clear(Calendar.MINUTE);
 				userCalendar.clear(Calendar.SECOND);
 				userCalendar.clear(Calendar.MILLISECOND);
 				if (userCalendar.getTime().equals(calendar.getTime())) {
 					if (null == map.get(hoursBeforeNow)) {
 						map.put(hoursBeforeNow, 1);
 					} else {
 						map.put(hoursBeforeNow, map.get(hoursBeforeNow) + 1);
 					}
 				}
 			}
 			calendar.add(Calendar.HOUR, -1);
 		}
 		for (int hoursBeforeNow = 0; hoursBeforeNow < 10; ++hoursBeforeNow) {
 			if (null != map.get(hoursBeforeNow)) {
 				answer.add(map.get(hoursBeforeNow));
 			} else {
 				answer.add(0);
 			}
 		}
 		return answer;
 	}
 
 	private void removeFanfic(User user, Fanfic fanfic) {
 		user.getFanfics().remove(fanfic);
 		userDao.save(user);
 		for (Comment comment : fanfic.getComments()) {
 			User author = comment.getAuthor();
 			author.getComments().remove(comment);
 			comment.setAuthor(null);
 			comment.setFanfic(null);
 			userDao.save(author);
 			commentDao.save(comment);
 		}
 		List<Comment> comments = fanfic.getComments();
 		fanfic.setComments(null);
 		for (Comment comment : comments) {
 			commentDao.remove(comment);
 		}
 		for (Vote vote : fanfic.getVotes()) {
 			User author = vote.getUser();
 			author.getVotes().remove(vote);
 			vote.setUser(null);
 			vote.setFanfic(null);
 			userDao.save(author);
 			voteDao.save(vote);
 		}
 		List<Vote> votes = fanfic.getVotes();
 		fanfic.setComments(null);
 		for (Vote vote : votes) {
 			voteDao.remove(vote);
 		}
 		fanficDao.save(fanfic);
 		fanficDao.removeFanficById(fanfic.getId());
 	}
 	
 	
 }
