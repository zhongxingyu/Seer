 package org.osiam.client.integration;
 
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.AbstractIntegrationTestBase;
 import org.osiam.client.query.Query;
 import org.osiam.resources.scim.SCIMSearchResult;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
 @DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
 public class SearchByExtensionIT extends AbstractIntegrationTestBase {
 
 
     @Test
    @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/extensions.xml")
     public void searching_by_multiple_fields_works() {
         Query query = new Query.Builder(User.class).setFilter("userName co \"existing\" AND extension.gender eq \"male\" AND extension.birthday pr").build();
 
         SCIMSearchResult<User> result = oConnector.searchUsers(query, accessToken);
 
         assertThat(result.getTotalResults(), is(1L));
    }
    
    @Test
    @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/search_by_extensions_with_not.xml")
    public void search_user_with_extension_and_not_fail() {
        String query = getCompletUserQueryString();
        SCIMSearchResult<User> queryResult = oConnector.searchUsers("filter=" + query, accessToken);
        assertEquals(1L, queryResult.getTotalResults());
    }
 
    private String getCompletUserQueryString() {
        return encodeExpected("not (extension.gender eq \"male\")");
     }
 }
