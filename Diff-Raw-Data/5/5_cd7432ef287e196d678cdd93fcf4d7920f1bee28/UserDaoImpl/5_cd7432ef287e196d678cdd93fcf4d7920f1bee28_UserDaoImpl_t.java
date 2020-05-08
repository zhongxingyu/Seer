 package epam.cdp.spring.task1.dao.impl.db;
 
 import static epam.cdp.spring.task1.dao.impl.db.util.RowMapper.userMapper;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import epam.cdp.spring.task1.bean.User;
 import epam.cdp.spring.task1.dao.UserDao;
 
 @Repository
 public class UserDaoImpl implements UserDao {
 
 	private JdbcTemplate template;
 
	private static final String FIND_USER_BY_LOGIN = "SELECT * FROM user WHERE login = ? and password = ?";
 
 	private static final String INSERT_USER = "INSERT INTO User VALUES (?, ?)";
 
 	@Autowired
 	public void setTemplate(JdbcTemplate template) {
 		this.template = template;
 	}
 
 	@Override
 	public boolean isUserExists(String login) {
 		List<User> users = template.query(FIND_USER_BY_LOGIN, userMapper(), login);
 		if (users.isEmpty()) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public User login(String login, String password) {
		List<User> users = template.query(FIND_USER_BY_LOGIN, userMapper(), login, password);
 		if (users.isEmpty()){
 			return null;
 		}
 		return users.get(0);
 	}
 
 	@Override
 	public User register(User user) {
 		template.update(INSERT_USER, user.getLogin(), user.getPassword());
 		return user;
 	}
 }
