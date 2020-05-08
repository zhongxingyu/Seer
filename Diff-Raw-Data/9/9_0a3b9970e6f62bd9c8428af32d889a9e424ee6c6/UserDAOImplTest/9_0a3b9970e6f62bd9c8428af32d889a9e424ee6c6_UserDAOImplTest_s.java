 package uk.org.cobaltdevelopment.test.db.dao;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Set;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import uk.org.cobaltdevelopment.test.db.AbstractDbUnitTestCase;
 import uk.org.cobaltdevelopment.test.db.dataset.DataSetContextBuilder;
 import uk.org.cobaltdevelopment.test.db.dataset.DataSets;
 import uk.org.cobaltdevelopment.test.db.domain.Role;
 import uk.org.cobaltdevelopment.test.db.domain.User;
 import uk.org.cobaltdevelopment.test.db.domain.UserBuilder;
 
 /**
  * Integration test for <code>UserDAO</code>
  * 
  * @author "Christopher Edgar"
  * 
  */
 @ContextConfiguration
 @Transactional
 public class UserDAOImplTest extends AbstractDbUnitTestCase {
 
 	@Autowired
 	private UserDAO userDAO;
 
 	/**
 	 * Tests the default behaviour of loading a DBUNit data set named
 	 * UserDAOImplTest-dataset.xml within the current classpath. No
 	 * assertDataSet attribute is specified here therefore not assertion is
 	 * executed after JUnit returns from test method.
 	 * 
 	 */
 	@Test
 	@DataSets
 	public void test_find() {
 		User user = userDAO.find(1l);
 		assertThat(user.getId(), is(equalTo(1l)));
 		assertThat(user.getUserName(), is(equalTo("jtbrown")));
 	}
 
 	/**
 	 * Tests using default setup <code>DataSet</code> as in
 	 * <code>test_find</code> above and explicit specification of an assertion
 	 * <code>DataSet</code> also loaded from classpath.
 	 */
 	@Test
 	@DataSets(assertDataSet = "UserDAOImplTestAfterAdd-dataset.xml")
 	public void test_add() {
 
 		User newUser = UserBuilder.create().user("cmedgar")
 				.dateRegistered(createDate(2012, Calendar.AUGUST, 21))
 				.role("ADMIN").build();
 
 		long id = userDAO.add(newUser);
 		assertThat(id, is(not(equalTo(0l))));
 		assertThat(newUser.getId(), is(equalTo(id)));
 
 		dataSetContext = DataSetContextBuilder.create()
 				.add("ID", newUser.getId()).build();
 
 	}
 
 	/**
 	 * Illustrates a DBUnit idiom using a special data set that empties tables
 	 * before an insert in order to focus and simplify the assertion phase of
 	 * the test.
 	 */
 	@Test
 	@DataSets(setUpDataSet = "empty.xml", assertDataSet = "UserDAOImplTestAfterAddWithEmpty-dataset.xml")
 	public void test_add_with_empty() {
 
 		User user = UserBuilder.create().user("cmedgar")
 				.dateRegistered(createDate(2012, Calendar.AUGUST, 21))
 				.role("ADMIN").build();
 
 		long id = userDAO.add(user);
 		assertThat(id, is(not(equalTo(0l))));
 		assertThat(user.getId(), is(equalTo(id)));
 
 		// create a valid context for DataSet execution listener
 		dataSetContext = DataSetContextBuilder.create().add("ID", user.getId())
 				.build();
 
 	}
 
 	@Test
 	@DataSets
 	public void test_roles() {
 
 		Set<Role> roles = userDAO.roles(1);
 		assertThat(roles, is(not(nullValue())));
 		assertThat(roles.isEmpty(), is(false));
 		assertThat(roles.size(), is(equalTo(2)));
 	}
 
 	/**
 	 * Create a Date ignoring any time component
 	 */
 	private Date createDate(int year, int month, int date) {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.HOUR, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.YEAR, year);
 		cal.set(Calendar.DATE, date);
 		cal.set(Calendar.MONTH, month);
 		return cal.getTime();
 	}
 }
