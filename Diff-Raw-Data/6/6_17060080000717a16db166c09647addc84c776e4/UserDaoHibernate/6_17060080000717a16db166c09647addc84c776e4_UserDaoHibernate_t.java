 package net.continuumsecurity.dao.hibernate;
 
 import net.continuumsecurity.dao.UserDao;
 import net.continuumsecurity.model.User;
 import org.hibernate.Query;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.RecoverableDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.orm.hibernate4.SessionFactoryUtils;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Repository;
 
 import javax.persistence.Table;
 import java.sql.*;
 import java.util.List;
 
 /**
  * This class interacts with Hibernate session to save/delete and
  * retrieve User objects.
  *
  * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
  *   Modified by <a href="mailto:dan@getrolling.com">Dan Kibler</a>
  *   Extended to implement Acegi UserDetailsService interface by David Carter david@carter.net
  *   Modified by <a href="mailto:bwnoll@gmail.com">Bryan Noll</a> to work with
  *   the new BaseDaoHibernate implementation that uses generics.
  *   Modified by jgarcia (updated to hibernate 4)
 */
 @Repository("userDao")
 public class UserDaoHibernate extends GenericDaoHibernate<User, Long> implements UserDao, UserDetailsService {
 
     /**
      * Constructor that sets the entity to User.class.
      */
     public UserDaoHibernate() {
         super(User.class);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public List<User> getUsers() {
         Query qry = getSession().createQuery("from User u order by upper(u.username)");
         return qry.list();
     }
 
     /**
      * {@inheritDoc}
      */
     public User saveUser(User user) {
         if (log.isDebugEnabled()) {
             log.debug("user's id: " + user.getId());
         }
         getSession().saveOrUpdate(user);
         // necessary to throw a DataIntegrityViolation and catch it in UserManager
         getSession().flush();
         return user;
     }
 
     /**
      * Overridden simply to call the saveUser method. This is happening
      * because saveUser flushes the session and saveObject of BaseDaoHibernate
      * does not.
      *
      * @param user the user to save
      * @return the modified user (with a primary key set if they're new)
      */
     @Override
     public User save(User user) {
         return this.saveUser(user);
     }
 
     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         List users = getSession().createCriteria(User.class).add(Restrictions.eq("username", username)).list();
         if (users == null || users.isEmpty()) {
             throw new UsernameNotFoundException("user '" + username + "' not found...");
         } else {
             return (UserDetails) users.get(0);
         }
     }
 
     /**
      * {@inheritDoc}
     */
     public UserDetails loadUserDetailsByUsername(String username) throws UsernameNotFoundException {
         String dbUrl = "jdbc:mysql://localhost/spring_mvc_example";
         String dbClass = "com.mysql.jdbc.Driver";
         String query = "Select * from app_user where username='"+username+"'";
         String dbusername = "root";
         String password = "4goodL0ngpassw0rd$";
         User user = null;
         try {
             Class.forName(dbClass);
             Connection connection = DriverManager.getConnection(dbUrl,
                     dbusername, password);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query);
             log.debug(rs);
             if (rs.next()) {
                 user = new User();
                 user.setFirstName(rs.getString("first_name"));
                 user.setLastName(rs.getString("last_name"));
                 user.setEmail(rs.getString("email"));
                 user.setPassword(rs.getString("password"));
                 user.setAccountExpired(rs.getBoolean("account_expired"));
                 user.setAccountLocked(rs.getBoolean("account_locked"));
                 user.getAddress().setAddress(rs.getString("address"));
                 user.getAddress().setCity(rs.getString("city"));
                 user.getAddress().setPostalCode(rs.getString("postal_code"));
                 user.getAddress().setProvince(rs.getString("province"));
                 user.setCredentialsExpired(rs.getBoolean("credentials_expired"));
                 user.setId(rs.getLong("id"));
                 user.setPasswordHint(rs.getString("password_hint"));
                 log.debug("Created user with firstname: "+user.getFirstName());
             }
             connection.close();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (SQLException e) {
            throw new RecoverableDataAccessException(e.getMessage());
         }
         if (user == null) throw new UsernameNotFoundException("user '" + username + "' not found...");
         return user;
     }
 
     /**
      * {@inheritDoc}
     */
     public String getUserPassword(Long userId) {
         JdbcTemplate jdbcTemplate =
                 new JdbcTemplate(SessionFactoryUtils.getDataSource(getSessionFactory()));
         Table table = AnnotationUtils.findAnnotation(User.class, Table.class);
         return jdbcTemplate.queryForObject(
                 "select password from " + table.name() + " where id=?", String.class, userId);
     }
 }
