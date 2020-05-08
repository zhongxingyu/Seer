 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.user.application;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.Before;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.*;
 import static com.globant.katari.user.OrderedUsers.*;
 import static com.globant.katari.user.NamesContain.*;
 
 import com.globant.katari.user.SpringTestUtils;
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.UserFilter;
 import com.globant.katari.user.domain.UserRepository;
 import com.globant.katari.user.domain.filter.ContainsFilter;
 import com.globant.katari.user.domain.filter.Paging;
 import com.globant.katari.user.domain.filter.Sorting;
 
 /* This class represents a TestCase of the user filter command. In this class
  * we will test all the features of the user filter command.
  */
 public class UserFilterCommandTest {
 
   /* The command to be tested.
    */
   private UserFilterCommand userFilterCommnad;
 
   /* The user repository.
    */
   private UserRepository userRepository;
 
   /* This is a set up method of this TestCase.
    */
   @Before
   public final void setUp() {
     userRepository = (UserRepository) SpringTestUtils.getBean(
         "user.userRepository");
     addUsers();
     userFilterCommnad = new UserFilterCommand(userRepository);
   }
 
   /* Adds a pair of users to be used in the tests.
    */
   private void addUsers() {

     //  Removes the unneeded users.
     while (userRepository.getUsers(new UserFilter()).size() != 0) {
       for (User user : userRepository.getUsers(new UserFilter())) {
         userRepository.remove(user);
       }
     }
 
     // Add users.
     User user = new User("admin", "admin@none");
     user.changePassword("admin");
     userRepository.save(user);
 
     user = new User("nico", "nico@none");
     user.changePassword("pass");
     userRepository.save(user);
 
     user = new User("nicanor", "nicanor@none");
     user.changePassword("pass");
     userRepository.save(user);
 
     user = new User("juan", "juan@none");
     user.changePassword("pass");
     userRepository.save(user);
 
     user = new User("ramon", "ramon@none");
     user.changePassword("pass");
     userRepository.save(user);
   }
 
   /* Test Execute.
    */
   @Test
   public final void testExecute() {
     List<User> users = userFilterCommnad.execute();
     assertThat(users, notNullValue());
     assertThat(users.size(), is(5));
   }
 
   /* Test Execute Sorting.
    */
   @Test
  public final void testExecute_Sorting() {
 
     // Verify the ascending order.
     Sorting sorting = new Sorting();
     sorting.setAscendingOrder(true);
     sorting.setColumnName("name");
     userFilterCommnad.setSorting(sorting);
     List<User> users = userFilterCommnad.execute();
     assertThat(users, notNullValue());
     assertThat(users.size(), is(5));
     assertThat(users, inAscendingOrder());
 
     // Verify the descending order.
     sorting.setAscendingOrder(false);
     userFilterCommnad.setSorting(sorting);
     users = userFilterCommnad.execute();
     assertThat(users, notNullValue());
     assertThat(users.size(), is(5));
     assertThat(users, inDescendingOrder());
   }
 
   /* Test Execute Contains.
    */
   @Test
   public final void testExecute_Contains() {
     ContainsFilter containsFilter = new ContainsFilter();
     containsFilter.setColumnName("name");
     String value = "nic";
     containsFilter.setValue(value);
     userFilterCommnad.setContainsFilter(containsFilter);
     List<User> users = userFilterCommnad.execute();
     assertThat(users, notNullValue());
     assertThat(users.size(), is(2));
     assertThat(users, namesContain(value));
 
     assertThat(users, notNullValue());
     assertThat(users.size(), is(2));
     assertThat(users, inDescendingOrder());
   }
 
   /* Test Execute Paging.
    */
   @Test
   public final void testExecute_Paging() {
 
     // First Page.
     Paging paging = new Paging();
     paging.setPageNumber(0);
     paging.setPageSize(4);
     userFilterCommnad.setPaging(paging);
     List<User> users = userFilterCommnad.execute();
     assertThat(users, notNullValue());
     assertThat(users.size(), is(4));
 
     // Second Page.
     paging.setPageNumber(1);
     paging.setPageSize(4);
     userFilterCommnad.setPaging(paging);
     users = userFilterCommnad.execute();
     assertThat(users, notNullValue());
     assertThat(users.size(), is(1));
 
     // Sets the default pagination.
     userFilterCommnad.setPaging(new Paging());
   }
 }
