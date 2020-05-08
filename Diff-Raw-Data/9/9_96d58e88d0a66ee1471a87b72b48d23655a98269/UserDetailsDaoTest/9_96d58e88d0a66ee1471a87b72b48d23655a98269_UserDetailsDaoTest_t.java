 package org.deegree.securityproxy.authentication.repository;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import org.deegree.securityproxy.authentication.repository.UserDetailsDao;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:org/deegree/securityproxy/authentication/repository/UserDaoTestContext.xml" })
 public class UserDetailsDaoTest {
 
     private EmbeddedDatabase db;
 
     @Autowired
     private UserDetailsDao source;
 
     @Before
     public void setUp() {
         db = new EmbeddedDatabaseBuilder().build();
     }
 
     @Test
     public void testLoadUserDetailsFromDataSourceValidHeaderShouldReturnUserDetails() {
         UserDetails details = source.loadUserDetailsFromDataSource( "VALID_HEADER" );
         assertThat( details.getUsername(), is( "USER" ) );
         assertThat( details.getPassword(), is( "PASSWORD" ) );
     }
 
     @Test
     public void testLoadUserDetailsFromDataSourceInvalidHeaderShouldReturnNull() {
         UserDetails details = source.loadUserDetailsFromDataSource( "INVALID_HEADER" );
         assertThat( details, nullValue() );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testLoadUserDetailsFromDataSourceShouldThrowExceptionOnEmptyHeader() {
         source.loadUserDetailsFromDataSource( "" );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testLoadUserDetailsFromDataSourceShouldThrowExceptionOnNullArgument() {
         source.loadUserDetailsFromDataSource( null );
     }
 
     @After
     public void tearDown() {
         db.shutdown();
     }
 }
