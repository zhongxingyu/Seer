 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.digt.service;
 
 import com.digt.model.UserAccount;
 import com.digt.model.impl.UserAccountImpl;
 import com.digt.util.PwdUtils;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import org.apache.rave.model.PageType;
 import org.apache.rave.model.Person;
 import org.apache.rave.model.User;
 import org.apache.rave.portal.model.impl.AuthorityImpl;
 import org.apache.rave.portal.model.util.SearchResult;
 import org.apache.rave.portal.repository.PageLayoutRepository;
 import org.apache.rave.portal.repository.PageRepository;
 import org.apache.rave.portal.repository.PageTemplateRepository;
 import org.apache.rave.portal.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.security.openid.OpenIDAuthenticationToken;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import com.digt.repository.UserAccountRepository;
 import com.digt.util.HttpUtils;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.rave.model.Page;
 import org.apache.rave.portal.model.impl.PersonPropertyImpl;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.springframework.context.MessageSource;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Value;
 
 /**
  *
  * @author
  * wasa
  */
 @Service(value = "userAccountService")
 public class UserAccountServiceImpl implements UserAccountService {
 
     private final UserRepository userRepository;
     private final PageRepository pageRepository;
     private final PageTemplateRepository pageTemplateRepository;
     private final PageLayoutRepository pageLayoutRepository;
     private final UserAccountRepository uacRepository;
     private final MessageSource messages;
     
     private final String cmisBaseUrl;
     
     private static final Logger LOG = Logger.getLogger(UserAccountServiceImpl.class.getName());
     
     @Autowired
     public UserAccountServiceImpl(UserRepository userRepository, PageRepository pageRepository, 
                        PageTemplateRepository pageTemplateRepository, 
                        PageLayoutRepository pageLayoutRepository,
                        UserAccountRepository uacRepository, MessageSource messages,
                        @Value("${cmis.baseurl}") String cmisBaseUrl) {
      
         this.userRepository = userRepository;
         this.pageLayoutRepository = pageLayoutRepository;
         this.pageRepository = pageRepository;
         this.pageTemplateRepository = pageTemplateRepository;
         this.uacRepository = uacRepository;
         this.messages = messages;
         this.cmisBaseUrl = cmisBaseUrl;
     }
     
     @Override
     public User getUserByAccountId(String accId, String type) {
         return uacRepository.getUserByAccountId(accId, type);
     }
     
     @Transactional
     @Override
     public UserAccount registerNewAccount(User user, String type) 
     {
         HashSet auth = new HashSet();
         String accountId = user.getUsername();
         auth.add(new AuthorityImpl("ROLE_USER"));
         user.setAuthorities(auth);
         user.setDefaultPageLayout(pageLayoutRepository.getByPageLayoutCode("columns_1"));
         User managedUser = userRepository.save(user);
         createDefaultPages(managedUser);
 
         UserAccount uac = new UserAccountImpl();
         uac.setUser(managedUser);
         uac.setAccountId(accountId);
         uac.setAccountType(type);
 
         return uacRepository.save(uac);
     }
     
     @Override
     public User getUserByEmail(String email) {
         return userRepository.getByUserEmail(email);
     }
     
     @Transactional
     @Override
     public void registerNewUser(User user) {
         HashSet auth = new HashSet();
         auth.add(new AuthorityImpl("ROLE_USER"));
         user.setAuthorities(auth);
         user.setDefaultPageLayout(pageLayoutRepository.getByPageLayoutCode("columns_1"));
         try {
             user.setPassword(new String(PwdUtils.cryptPassword(user.getPassword().toCharArray())));
         } catch (NoSuchAlgorithmException ex) {
             throw new RuntimeException(ex);
         }
         User managedUser = userRepository.save(user);
         createDefaultPages(managedUser);
     }
     
     private void createDefaultPages(User managedUser) {
         // Create profile page
         pageRepository.createPageForUser(managedUser, 
              pageTemplateRepository.getDefaultPage(
                  PageType.PERSON_PROFILE));
         // Create default page set
         try {
             JSONArray obj = new JSONArray(messages.getMessage("user.pages.json", null, null, null));
             for (int i = 0; i < obj.length(); i++) {
                 JSONObject o = obj.getJSONObject(i);
                 
                 Page page = pageRepository.createPageForUser(managedUser, 
                      pageTemplateRepository.getDefaultPage(
                          PageType.USER));
                 page.setName(o.getString("pName"));
                 page.setPageLayout(pageLayoutRepository.getByPageLayoutCode(o.getString("layout")));
                 pageRepository.save(page);
             }
         } catch (JSONException ex) {
             LOG.log(Level.SEVERE, null, ex);
         }
         
         String storageUrl = cmisBaseUrl + "/" + HttpUtils.generateId(managedUser.getUsername());
         PersonPropertyImpl pp = new PersonPropertyImpl();
        pp.setType("urls");
        pp.setValue("storageUrl: " + storageUrl);
         
         managedUser.getProperties().add(pp);
     }
 
     @Override
     public User getAuthenticatedUser() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void setAuthenticatedUser(String userId) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void clearAuthenticatedUser() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public User getUserByUsername(String userName) {
         return userRepository.getByUsername(userName);
     }
 
     @Override
     public User getUserById(String id) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void updateUserProfile(User user) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public SearchResult<User> getLimitedListOfUsers(int offset, int pageSize) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public SearchResult<User> getUsersByFreeTextSearch(String searchTerm, int offset, int pageSize) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void deleteUser(String userId) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public List<Person> getAllByAddedWidget(String widgetId) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void sendPasswordReminder(User user) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void sendUserNameReminder(User user) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Transactional
     @Override
     public void updatePassword(User user) {
         userRepository.save(user);
     }
 
     @Override
     public boolean isValidReminderRequest(String forgotPasswordHash, int nrOfMinutesValid) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public boolean addFriend(String friendUsername, String username) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void removeFriend(String friendUsername, String username) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public HashMap<String, List<Person>> getFriendsAndRequests(String username) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public List<Person> getFriendRequestsReceived(String username) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public boolean acceptFriendRequest(String friendUsername, String username) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public User getUserByOpenId(String openId) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public SearchResult<Person> getLimitedListOfPersons(int offset, int pageSize) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public SearchResult<Person> getPersonsByFreeTextSearch(String searchTerm, int offset, int pageSize) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public UserDetails loadUserByUsername(String string) throws UsernameNotFoundException {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public UserDetails loadUserDetails(OpenIDAuthenticationToken t) throws UsernameNotFoundException {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
