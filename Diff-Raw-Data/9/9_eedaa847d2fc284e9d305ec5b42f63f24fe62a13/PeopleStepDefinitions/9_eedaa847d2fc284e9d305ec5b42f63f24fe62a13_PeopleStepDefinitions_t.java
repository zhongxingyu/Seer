 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import net.gree.asdk.api.GreePlatform;
 import net.gree.asdk.api.GreeUser;
 import net.gree.asdk.api.GreeUser.GreeIgnoredUserListener;
 import net.gree.asdk.api.GreeUser.GreeUserListener;
 import net.gree.asdk.core.Core;
 import net.gree.asdk.core.Session;
 import net.gree.asdk.core.auth.AuthorizerCore;
 import net.gree.asdk.core.auth.OAuthStorage;
 import net.gree.asdk.core.request.OnResponseCallback;
 import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 import org.apache.http.HeaderIterator;
 
 import util.Consts;
 import android.util.Log;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Given;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 import com.openfeint.qa.core.util.CredentialStorage;
 
 public class PeopleStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "People_Steps";
 
     private final static String PEOPLE_LIST = "friendList";
 
     private final static String MYSELF = "me";
 
     private final static String HAS_APP = "hasApp";
 
     private final static String FRIEND = "friend";
 
     private final static String IGNORE_LIST = "ignoreUsers";
 
     private final static String IGNORE_USER = "specificIgnoreUser";
 
     private static String login_result;
 
     @Given("I logged in with email (.+) and password (\\w+)")
     public void setCurrentLoginUser(String email, String password) {
         HashMap<String, String> credential = getCredential(email, password);
         String user_id = credential.get(CredentialStorage.KEY_USERID);
         if (GreePlatform.getLocalUser() == null
                 || !GreePlatform.getLocalUser().getId().equals(user_id)) {
             hackLogin(credential);
         } else {
             Log.i(TAG, "Already login with email " + email);
         }
     }
 
     @And("I switch to user (.+) with password (\\w+)")
     @After("I switch to user (.+) with password (\\w+)")
     public void switchLoginUser(String email, String password) {
         Log.d(TAG, "current user is : " + GreePlatform.getLocalUser().getNickname());
         HashMap<String, String> credential = getCredential(email, password);
         hackLogin(credential);
     }
 
     private HashMap<String, String> getCredential(String email, String password) {
         CredentialStorage credentialStorage = CredentialStorage.getInstance();
         String key = email + "&" + password;
         HashMap<String, String> credential = credentialStorage.getCredentialByKey(key);
         if (credential == null) {
             fail("Can not get credential with key:" + key);
         }
         return credential;
     }
 
     private void hackLogin(HashMap<String, String> credential) {
         login_result = "success";
         // Get token & secret of the user
         String user_id = credential.get(CredentialStorage.KEY_USERID);
         String token = credential.get(CredentialStorage.KEY_TOKEN);
         String secret = credential.get(CredentialStorage.KEY_SECRET);
         Log.i(TAG, "Try Login Userid: " + user_id + "\nToken: " + token + "\nand secret: " + secret);
 
         AuthorizerCore core = AuthorizerCore.getInstance();
         try {
             // get mOAuth field of AuthorizerCore
             Field oAuth_field = core.getClass().getDeclaredField("mOAuth");
             oAuth_field.setAccessible(true);
             // get OAuth class
             Class<?> OAuthClass = Class.forName("net.gree.asdk.core.auth.OAuth");
             // get consumer by invoke getConsumer method of OAuth class
             final CommonsHttpOAuthConsumer consumer = (CommonsHttpOAuthConsumer) OAuthClass
                     .getMethod("getConsumer").invoke(oAuth_field.get(core));
             // Set token & secret
             consumer.setTokenWithSecret(token, secret);
 
             // get mOAuthStorage field of AuthorizerCore
             Field mOAuthStorage_field = core.getClass().getDeclaredField("mOAuthStorage");
             mOAuthStorage_field.setAccessible(true);
             // set user_id, token & secret
             OAuthStorage oAuth_storage = (OAuthStorage) mOAuthStorage_field.get(core);
             oAuth_storage.setUserId(user_id);
             oAuth_storage.setToken(token);
             oAuth_storage.setSecret(secret);
 
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
 
         // Update session & local user
         Log.d(TAG, "Updating session...");
         new Session().refreshSessionId(GreePlatform.getContext(), new OnResponseCallback<String>() {
             @Override
             public void onSuccess(int responseCode, HeaderIterator headers, String response) {
                 Log.i(TAG, "Update session success!");
                 notifyAsyncInStep();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 login_result = "Update session failed!";
                 notifyAsyncInStep();
             }
         });
         waitForAsyncInStep();
         if (!"success".equals(login_result))
             fail(login_result);
         Log.d(TAG, "Updating local user...");
         Core.getInstance().updateLocalUser(new GreeUserListener() {
             @Override
             public void onSuccess(int index, int count, GreeUser[] users) {
                 Log.i(TAG, "Update local user to: " + users[0].getNickname());
                 notifyAsyncInStep();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 login_result = "Update local user failed!";
                 notifyAsyncInStep();
             }
         });
         waitForAsyncInStep();
         if (!"success".equals(login_result))
             fail(login_result);
     }
 
     @When("I see my info from native cache")
     public void getUserInfoFromCache() {
         if (GreePlatform.getLocalUser() == null) {
             fail("No login yet！");
         } else {
             getBlockRepo().put(MYSELF, GreePlatform.getLocalUser());
             Log.i(TAG, "Logined as user: " + GreePlatform.getLocalUser().getNickname());
         }
     }
 
     @Then("my info (.+) should be (.+)")
     public void verifyUserInfo(String column, String value) {
         GreeUser me = (GreeUser) getBlockRepo().get(MYSELF);
 
         if ("displayName".equals(column)) {
             assertEquals("userName", value, me.getNickname());
         } else if ("id".equals(column)) {
             assertEquals("userId", value, me.getId());
         } else if ("userGrade".equals(column)) {
             assertEquals("userGrade", value, String.valueOf(me.getUserGrade()));
         } else if ("region".equals(column)) {
             assertEquals("userRegion", value, me.getRegion());
         } else if ("subregion".equals(column)) {
             assertEquals("subregion", value, me.getSubregion());
         } else if ("birthday".equals(column)) {
             assertEquals("birthday", value, me.getBirthday());
         } else if ("aboutMe".equals(column)) {
             assertEquals("aboutMe", value, me.getAboutMe());
         } else if ("language".equals(column)) {
             assertEquals("language", value, me.getLanguage());
         } else if ("bloodType".equals(column)) {
             assertEquals("bloodType", value, me.getBloodType());
         } else if ("age".equals(column)) {
             assertEquals("age", value, me.getAge());
         } else if ("timezone".equals(column)) {
             assertEquals("timezone", value, me.getTimezone());
         } else if ("has the application".equals(column)) {
             assertEquals("has the application", value, String.valueOf(me.getHasApp()));
         } else {
             fail("Unknown column of user info!");
         }
     }
 
     @When("I see my info from server")
     public void getUserInfoFromServer() {
         notifyStepWait();
         GreeUser.loadUserWithId("@me", new GreeUserListener() {
             @Override
             public void onSuccess(int index, int count, GreeUser[] people) {
                 Log.d(TAG, "Get people success!");
                 getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                 if (people != null) {
                     Log.i(TAG, "Get " + people.length + " people");
                     Log.i(TAG, "Adding people datas");
                     getBlockRepo().put(MYSELF, people[0]);
                     Log.i(TAG, "User info get from server: " + people[0]);
                 }
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get people failed!");
                 notifyStepPass();
             }
         });
     }
 
     private void getCurrentUserFriends(int pageSize) {
         notifyStepWait();
         GreeUser me = GreePlatform.getLocalUser();
         me.loadFriends(Consts.STARTINDEX_1, pageSize, new GreeUserListener() {
             @Override
             public void onSuccess(int index, int count, GreeUser[] people) {
                 Log.d(TAG, "Get people success!");
                 getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                 if (people != null) {
                     Log.i(TAG, "Get " + people.length + " people");
                     Log.i(TAG, "Adding people datas");
                     ((ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST)).addAll(Arrays
                             .asList(people));
                 }
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                 Log.e(TAG, "Get people failed!");
                 notifyStepPass();
             }
         });
     }
 
     @When("I check my friend list")
     public void getAllFriends() {
         getCurrentUserFriends(Consts.PAGESIZE_ALL);
     }
 
     @When("I check my friend list first page")
     public void getFriendsOfFirstPage() {
         getCurrentUserFriends(Consts.PAGESIZE_FIRSTPAGE);
     }
 
     @Then("friend list should be size of (\\d+)")
     public void verifyFriendNumber(int num) {
         Log.d(TAG, "Checking friend number...");
         ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
         assertEquals("friend count", num, l.size());
     }
 
     private boolean isUserExists(String friendName) {
         ArrayList<GreeUser> friends = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
         Log.d(TAG, "Check if " + friendName + " is in the friend list");
 
         for (GreeUser friend : friends) {
             if (friendName.equals(friend.getNickname())) {
                 Log.d(TAG, "Got the user " + friendName);
                 assertTrue("user in friend list", true);
                 getBlockRepo().put(FRIEND, friend);
                 return true;
             }
         }
         Log.d(TAG, "Do not find the user " + friendName);
         return false;
     }
 
     @Then("friend list should have (.+)")
     public void verifyFriendExists(String friendName) {
         assertEquals("user " + friendName + " in friend list", true, isUserExists(friendName));
     }
 
     @Then("friend list should not have (.+)")
     public void verifyFriendNotExists(String friendName) {
         assertEquals("user " + friendName + " in friend list", false, isUserExists(friendName));
     }
 
     @Then("userid of (.+) should be (\\w+) and grade should be (\\w+)")
     public void verifyFriendInfo(String name, String id, String grade) {
         GreeUser friend = (GreeUser) getBlockRepo().get(FRIEND);
 
         if (friend == null)
             fail("Don't get the friend " + name + " in the list");
 
         Log.d(TAG, "Checking friend info...");
         assertEquals("userId", id, friend.getId());
         assertEquals("userGrade", grade, String.valueOf(friend.getUserGrade()));
     }
 
     // This step is not using now, seems the sdk is not allow to get another
     // user instance except the current login user
     @When("I check friend list of user (\\w+)")
     public void getSpecificUserFriends(String userId) {
         notifyStepWait();
         GreeUser.loadUserWithId(userId, new GreeUserListener() {
             @Override
             public void onSuccess(int index, int count, GreeUser[] people) {
                 Log.d(TAG, "Get people success!");
                 getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                 if (people != null) {
                     Log.i(TAG, "Get " + people.length + " people");
                     Log.i(TAG, "Adding people datas");
                     ((ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST)).addAll(Arrays
                             .asList(people));
                 }
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                 Log.e(TAG, "Get people failed!");
                 notifyStepPass();
             }
         });
     }
 
     @Given("I make sure my ignore list (\\w+) user (\\w+)")
     @After("I make sure my ignore list (\\w+) user (\\w+)")
     public void checkUserInIgnoreListAsCondition(String isIncludeMark, String userId) {
         // Log.i(TAG, "Expect user " + userId + " is in my ignore list...");
         // GreeUser me = GreePlatform.getLocalUser();
         // me.isIgnoringUserWithId(userId, ignoredUserListener);
         // waitForAsyncInStep();
         //
         // fail("Did not get the ignore list!");
         // ArrayList<String> ignoreList = (ArrayList<String>)
         // getBlockRepo().get(IGNORE_LIST);
         // if ("INCLUDE".equals(isIncludeMark)) {
         // if (ignoreList.size() == 0 || !userId.equals(ignoreList.get(0)))
         // fail("User " + userId
         // + " is not in the ignore list, please add it from sb.gree.net");
         // }
         // if ("NOTINCLUDE".equals(isIncludeMark)) {
         // if (ignoreList.size() > 0 && userId.equals(ignoreList.get(0))) {
         // fail("User " + userId +
         // "is in the ignore list, please remove it from sb.gree.net");
         // }
         // }
     }
 
     private void getIgnoreUser(int pageSize) {
         notifyStepWait();
         GreeUser me = GreePlatform.getLocalUser();
         getBlockRepo().put(IGNORE_LIST, new ArrayList<String>());
         me.loadIgnoredUserIds(Consts.STARTINDEX_1, pageSize, new GreeIgnoredUserListener() {
             @Override
             public void onSuccess(int index, int count, String[] list) {
                 Log.d(TAG, "Get ignore list success!");
                 Log.i(TAG, "Ignore list size: " + count);
                 getBlockRepo().put(IGNORE_LIST, new ArrayList<String>());
                 if (list != null) {
                     ((ArrayList<String>) getBlockRepo().get(IGNORE_LIST)).addAll(Arrays
                             .asList(list));
                     for (int i = 0; i < list.length; i++) {
                         Log.i(TAG, "Block user " + (i + 1) + ": " + list[i]);
                     }
                 } else {
                     Log.d(TAG, "The list returned is null!");
                 }
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get ignore list failed!");
                 getBlockRepo().put(IGNORE_LIST, new ArrayList<String>());
                 notifyStepPass();
             }
         });
     }
 
     @When("I load my ignore list")
     public void getAllIgnoreUser() {
         getIgnoreUser(Consts.PAGESIZE_ALL);
     }
 
     @When("I load first page of my ignore list")
     public void getIgnoreUserOfFirstPage() {
         getIgnoreUser(Consts.PAGESIZE_FIRSTPAGE);
     }
 
     @Then("my ignore list should be size of (\\d+)")
     public void verifyIgnoreUserCount(int count) {
         if (getBlockRepo().get(IGNORE_LIST) == null)
             fail("Do not have user in the ignore list!");
         assertEquals("ingore user count", count,
                 ((ArrayList<String>) getBlockRepo().get(IGNORE_LIST)).size());
     }
 
     private boolean transIsIncludeMark(String isIncludeMark) {
         if ("INCLUDE".equals(isIncludeMark))
             return true;
         else if ("NOTINCLUDE".equals(isIncludeMark))
             return false;
         else {
             fail("Got invalid isIncludeMark!");
             return false;
         }
     }
 
     @Then("my ignore list should (\\w+) user (\\w+)")
     public void checkUserInIgnoreList(String isIncludeMark, String userId) {
         if (!"INCLUDE".equals(isIncludeMark) && !"NOTINCLUDE".equals(isIncludeMark))
             fail("Not a valid isIncludeMark!");
         boolean isExists = false;
         if (getBlockRepo().get(IGNORE_LIST) == null)
             fail("Did not get the ignore list!");
         ArrayList<String> ignoreUsers = (ArrayList<String>) getBlockRepo().get(IGNORE_LIST);
         for (String id : ignoreUsers) {
             if (userId.equals(id)) {
                 Log.i(TAG, "Find user: " + userId + " in the list");
                 isExists = true;
                 break;
             }
         }
         assertEquals("user in ignore list", transIsIncludeMark(isIncludeMark), isExists);
     }
 
     @When("I check user from my ignore list with id (\\w+)")
     public void verifyBlockedUser(final String userId) {
         getBlockRepo().put(IGNORE_USER, "");
         GreePlatform.getLocalUser().isIgnoringUserWithId(userId, new GreeIgnoredUserListener() {
             @Override
             public void onSuccess(int index, int count, String[] ignoredUsers) {
                 Log.i(TAG, "Check ignore user success!");
                 if (ignoredUsers != null && ignoredUsers.length > 0) {
                     getBlockRepo().put(IGNORE_USER, ignoredUsers[0]);
                 } else {
                     Log.d(TAG, "The list returned is null!");
                 }
                 notifyAsyncInStep();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Check ignore user failed!");
                 notifyAsyncInStep();
             }
         });
        waitForAsyncInStep();
     }
 
     @Then("status of (\\w+) in my ignore list should be TRUE")
     public void verifyUserBlocked(String userId) {
         String returnUser = (String) getBlockRepo().get(IGNORE_USER);
         if ("".equals(returnUser))
             fail("Not ignore user return from server!");
         assertEquals("user is blocked", userId, returnUser);
     }
 }
