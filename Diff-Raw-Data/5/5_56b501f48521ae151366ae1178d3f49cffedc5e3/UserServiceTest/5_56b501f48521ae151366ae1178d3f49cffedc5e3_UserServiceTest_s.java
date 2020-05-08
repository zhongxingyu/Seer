 package com.mtt.service;
 
 import com.mtt.domain.entity.TestUtils;
 import com.mtt.domain.entity.User;
 import com.mtt.domain.exception.IncorrectPasswordException;
 import com.mtt.domain.exception.UserNotFoundException;
 import com.mtt.repository.UserRepository;
 import com.mtt.service.impl.UserServiceImpl;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class UserServiceTest {
 
     private UserService userService;
 
     private UserRepository userRepository;
 
     @Before
     public void init() {
         userService = new UserServiceImpl();
         userRepository = mock(UserRepository.class);
         ReflectionTestUtils.setField(userService, "userRepository", userRepository);
     }
 
     @Test(expected = UserNotFoundException.class)
     public void userNotFoundWithId() {
         when(userRepository.findOne(1L)).thenReturn(null);
 
         userService.find(1L);
     }
 
     @Test(expected = UserNotFoundException.class)
     public void userNotFoundWithUserName() {
         when(userRepository.findByUserName("mark")).thenReturn(null);
 
         userService.find("mark");
     }
 
     @Test
     public void userReturnedFoundById() {
         User userToReturn = TestUtils.createUser(1L);
         when(userRepository.findOne(1L)).thenReturn(userToReturn);
 
         User returnedUser = userService.find(1L);
 
         assertThat(returnedUser, equalTo(userToReturn));
     }
 
 
     @Test
     public void userReturnedFoundByUserName() {
         User userToReturn = TestUtils.createUser(1L);
         when(userRepository.findByUserName("mark")).thenReturn(userToReturn);
 
         User returnedUser = userService.find("mark");
 
         assertThat(returnedUser, equalTo(userToReturn));
     }
 
     @Test(expected = UserNotFoundException.class)
     public void testVerifyPasswordNoUserFound() {
         when(userRepository.findByUserName("mark")).thenReturn(null);
 
         userService.authenticate("mark", "password");
     }
 
     @Test(expected = IncorrectPasswordException.class)
     public void testVerifyPasswordIncorrectCredentials() {
         User userToReturn = TestUtils.createUser(1L);
         userToReturn.setPassword("nonHashedPasswordWillNeverMatch");
         when(userRepository.findByUserName("mark")).thenReturn(userToReturn);
 
         userService.authenticate("mark", "password");
     }
 
     @Test
     public void testCorrectCredentials() {
         User userToReturn = TestUtils.createUser(1L);
         userToReturn.setUsername("mark");
         userToReturn.setPassword("password");
         when(userRepository.findByUserName("mark")).thenReturn(userToReturn);
 
         User returned = userService.authenticate("mark", "password");
 
         assertThat(returned, equalTo(userToReturn));
     }
 }
