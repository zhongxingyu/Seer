 package uk.org.cobaltdevelopment.test.db.domain;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.junit.Test;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import uk.org.cobaltdevelopment.test.db.AbstractDbUnitTestCase;
 import uk.org.cobaltdevelopment.test.db.dataset.DataSetContextBuilder;
 import uk.org.cobaltdevelopment.test.db.dataset.DataSets;
 
 /**
  * 
  * @author "Christopher Edgar"
  *
  */
 @ContextConfiguration
 public class UserTest extends AbstractDbUnitTestCase {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	/**
 	 * Illustrates <code>DataSets</code> defaults - by convention a set up data
 	 * set will be searched for on the classpath that is names
 	 * ${TestClass}-dataset.xml. No explciit assertion attribute defined here
 	 * which is normal when testing finder methods.
 	 */
 	@Test
 	@DataSets
 	public void test_set_up() {
 		Long totalUsers = countUsers();
 
 		assertThat(totalUsers, is(equalTo(2l)));
 
 		User user = em.find(User.class, 1);
 		assertThat(user.getId(), is(equalTo(1)));
 		assertThat(user.getUserName(), is(equalTo("crsedgar")));
 
 		assertThat(user.getRoles(), is(notNullValue()));
 		assertThat(user.getRoles().size(), is(equalTo(2)));
 		assertThat(user.getDateRegistered(), is(nullValue()));
 
 		User secondUser = em.find(User.class, 2);
 		assertThat(secondUser.getDateRegistered(), is(notNullValue()));
 	}
 
 	/**
 	 * Illustrates explicit specification of both setup and assertion
 	 * <code>DataSets</code> attributes. The empty.xml is a DBUnit idioem for
 	 * clearing down a repository so you can focus on assertion just the data
 	 * that is the focus of the test method. This also ensures the assertion
 	 * data set file is kept small and therefore more manageable.
 	 */
 	@Test
 	@Transactional
 	@DataSets(setUpDataSet = "empty.xml", assertDataSet = "UserTestAfterAdd-dataset.xml")
 	public void test_assert_with_user_add() {
 
 		//@formatter:off
 
 		User userSmith = UserBuilder.create()
 				.user("msmith")
 				.role("ADMIN")
 				.role("MODERATOR")
 				.build();
 		
 		em.persist(userSmith);
 
 		User userBrown = UserBuilder.create()
 				.user("tbrown")
 				.dateRegistered(
 						DateBuilder.create()
 							.date(19)
 							.month(Calendar.FEBRUARY)
 							.year(2010)
 							.build())
 				.role("ADMIN")
 				.build();
 		
 		//@formatter:on
 
 		em.persist(userBrown);
 
 		em.flush();
 
 		// populate all token values in DataSetContext ensuring that they are
 		// replaced before
 		// execution of the data set assertion.
 		dataSetContext = DataSetContextBuilder.create()
 				.add("ID1", userSmith.getId())
 				.add("ROLE_ID1", userSmith.getRoleByName("ADMIN").getId())
 				.add("ROLE_ID2", userSmith.getRoleByName("MODERATOR").getId())
 				.add("ID2", userBrown.getId())
 				.add("ROLE_ID3", userBrown.getRoleByName("ADMIN").getId())
 				.build();
 
 	}
 
 	@Test
 	@DataSets(assertDataSet = "UserTestAfterUpdate-dataset.xml")
 	@Transactional
 	public void test_assert_with_user_update() {
 
 		User user = em.find(User.class, 1);
 		user.setDateRegistered(DateBuilder.create().date(2)
 				.month(Calendar.AUGUST).year(2012).build());
 		user.setUserName("rsmith");
 
 		em.merge(user);
 		em.flush();
 
 	}
 
 	private static class UserBuilder {
 		private User content;
 
 		public static UserBuilder create() {
 			UserBuilder userBuilder = new UserBuilder();
 			userBuilder.content = new User();
 			return userBuilder;
 		}
 
 		public UserBuilder user(String username) {
 			content.setUserName(username);
 			return this;
 		}
 
 		public UserBuilder dateRegistered(Date dateRegistered) {
 			content.setDateRegistered(dateRegistered);
 			return this;
 		}
 
 		public UserBuilder role(String name) {
 			Role role = new Role();
 			role.setName(name);
 			content.getRoles().add(role);
 			return this;
 		}
 
 		public User build() {
 			return content;
 		}
 	}
 
 	/**
 	 * Builder for creating a Date excluding any time fragment
 	 * 
 	 * @author "Christopher Edgar"
 	 * 
 	 */
 	private static class DateBuilder {
 		private Calendar content;
 
 		public static DateBuilder create() {
 			DateBuilder db = new DateBuilder();
 			db.content = Calendar.getInstance();
 			db.content.set(Calendar.MILLISECOND, 0);
			db.content.set(Calendar.HOUR, 12);
 			db.content.set(Calendar.MINUTE, 0);
 			db.content.set(Calendar.SECOND, 0);
 			return db;
 		}
 
 		public DateBuilder year(int year) {
 			content.set(Calendar.YEAR, year);
 			return this;
 		}
 
 		public DateBuilder date(int date) {
 			content.set(Calendar.DATE, date);
 			return this;
 		}
 
 		public DateBuilder month(int month) {
 			content.set(Calendar.MONTH, month);
 			return this;
 		}
 
 		public Date build() {
 			return content.getTime();
 		}
 	}
 
 	private Long countUsers() {
 		Long totalUsers;
 		TypedQuery<Long> createQuery = em.createQuery(
 				"SELECT COUNT(u.id) FROM User u", Long.class);
 		totalUsers = createQuery.getSingleResult();
 		return totalUsers;
 	}
 }
