 /*
  * Copyright (C) 2011 Raffaele Ragni <raffaele.ragni@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.jungle.portal.test;
 
 import com.jungle.portal.api.om.ForumComment;
 import com.jungle.portal.api.om.ForumPost;
 import org.junit.Test;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
 import com.jungle.portal.api.Forum;
 import com.jungle.portal.api.Portal;
 import com.jungle.portal.api.errors.DuplicateVillageCodeError;
 import com.jungle.portal.api.om.ForumCategory;
 import com.jungle.portal.api.om.UserProfile;
 import com.jungle.portal.api.om.Village;
 import com.jungle.portal.impl.ForumImpl;
 import com.jungle.portal.impl.PortalImpl;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import static org.junit.Assert.*;
 
 /**
  * A complete cycle test of a user registering, setting a forum, creating
  * categories, posting a pair of posts, editing and deleting them.
  * Deleting the account afterwards.
  * 
  * TODO: add tests for comments.
  * 
  * @author Raffaele Ragni <raffaele.ragni@gmail.com>
  */
 public class ForumTest
 {
     /**
      * The helper class for testing. Holds mock objects for, user service,
      * data store, etc.
      */
     private static final LocalServiceTestHelper helper =
         new LocalServiceTestHelper(new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());
     
     private static final Portal portal = new PortalImpl();
     
     private static final Forum forum = new ForumImpl();
 
     private static final String TEST_VILLAGE_CODE = "test";
     
     private static final String USER_ID_KEY = "com.google.appengine.api.users.UserService.user_id_key";
     
     /**
      * Initialization of tests.
      * 
      * Things to check:
      *  - ?
      */
     @BeforeClass
     public static void init()
     {
         Map<String, Object> attrs = new HashMap<String, Object>();
         attrs.put(USER_ID_KEY, "10");
         helper
             .setEnvAppId("jungle-portal")
             .setEnvVersionId("test")
             .setEnvEmail("test@test.com")
             .setEnvAuthDomain("test.com")
             .setEnvIsAdmin(false)
             .setEnvIsLoggedIn(true)
             .setEnvAttributes(attrs)
             .setUp();
     }
     
     /**
      * Destroying the tests, free resources, clean all data that shouldn't be
      * there.
      */
     @AfterClass
     public static void destroy()
     {
         helper.tearDown();
     }
     
     /**
      * First step: complete a registration for the user.
      */
     @Test
     public void t1Registration() throws Exception
     {
         UserProfile up = new UserProfile();
         // Set ID: the system should assign an automatic one, thus we'll check
         // that when retrieving it it must be different than this one
         up.setId("myid");
         up.setName("User Name");
         up.setDescription("My description profile");
         portal.saveMyUserProfile(up);
         // Check the variables we expect to get
         up = portal.getMyUserProfile();
         assertTrue(up != null);
             
         assertTrue(!"myid".equals(up.getId()));
         assertTrue("User Name".equals(up.getName()));
         assertTrue("My description profile".equals(up.getDescription()));
         
         // Test name
         assertTrue(portal.checkVillageCodeAvailability(TEST_VILLAGE_CODE));
         // check if registering the same name twice will throw an error
         portal.createVillage(TEST_VILLAGE_CODE, "Name");
         // Re-Test name after created village (negative)
         assertTrue(!portal.checkVillageCodeAvailability(TEST_VILLAGE_CODE));
         try
         {
             // Second try, this MUST throw the DuplicateVillageCodeError
             portal.createVillage(TEST_VILLAGE_CODE, "Name 2");
             // something wrong if we arrived here
             throw new Exception();
         }
         catch (DuplicateVillageCodeError e)
         {
             // All ok, the system recognized the duplicate code.
         }
         // Now retrieve the village and villages
         Village v = portal.getVillage(TEST_VILLAGE_CODE);
         List<Village> vs = new ArrayList<Village>(portal.getMyVillages());
         // And check for fields
         assertTrue(v != null);
         assertTrue(TEST_VILLAGE_CODE.equals(v.getCode()));
         assertTrue("Name".equals(v.getName()));
         // And that it appears in the list of 'my' villages
         assertTrue(vs != null);
         assertTrue(vs.contains(v));
     }
 
     /**
      * Create a decent category structure.
      */
     @Test
     public void t2MakeCategories()
     {
         Village v = portal.getVillage(TEST_VILLAGE_CODE);
         Long villageId = v != null ? v.getId() : null;
         // Configure a forum - create a simple category
         ForumCategory cat = new ForumCategory();
         cat.setName("Category");
         cat.setPublicReadable(true);
         cat.setPublicWriteable(true);
         List<String> villagers = new ArrayList<String>();
         villagers.add("1a");
         villagers.add("2b");
         cat.setVillagers(villagers);
         portal.createForumCategory(villageId, cat);
         // Check correctness of category
         List<ForumCategory> categories = forum.getForumCategories(villageId, 10, false, 0);
         assertEquals(categories.size(), 1);
         cat = categories.get(0);
         assertEquals(cat.isPublicReadable(), true);
         assertEquals(cat.isPublicWriteable(), true);
         assertEquals(cat.getVillagers().size(), 2);
         assertTrue(cat.getVillagers().contains("1a"));
         assertTrue(cat.getVillagers().contains("2b"));
         assertEquals(cat.getName(), "Category");
     } 
     
     /**
      * Make a post by poster1.
      */
     @Test
     public void t3Posts()
     {
         Village v = portal.getVillage(TEST_VILLAGE_CODE);
         Long villageId = v != null ? v.getId() : null;
         List<ForumCategory> categories = forum.getForumCategories(villageId, 10, false, 0);
         assertEquals(categories.size(), 1);
         Long categoryId = categories.get(0).getId();
         // Create a post
         forum.postNew(villageId, categoryId, "ASD");
         List<ForumPost> posts = forum.getForumPosts(villageId, categoryId, 10, false, 0);
         assertEquals(posts.size(), 1);
         assertEquals(posts.get(0).getText(), "ASD");
         Long postId = posts.get(0).getId();
         // Create a comment
         forum.comment(villageId, categoryId, postId, "COMMENT");
         posts = forum.getForumPosts(villageId, categoryId, 10, true, 10);
         assertEquals(posts.size(), 1);
         ForumPost post = posts.get(0);
         assertEquals(post.getComments().size(), 1);
         ForumComment comment = post.getComments().get(0);
         assertEquals(comment.getText(), "COMMENT");
     }
 
     /**
      * Modify a post.
      */
     @Test
     public void t4EditPosts()
     {
         Village v = portal.getVillage(TEST_VILLAGE_CODE);
         Long villageId = v != null ? v.getId() : null;
         List<ForumCategory> categories = forum.getForumCategories(villageId, 10, false, 0);
         assertEquals(categories.size(), 1);
         Long categoryId = categories.get(0).getId();
         List<ForumPost> posts = forum.getForumPosts(villageId, categoryId, 10, true, 10);
         assertEquals(posts.size(), 1);
         ForumPost post = posts.get(0);
         assertEquals(post.getComments().size(), 1);
         ForumComment comment = post.getComments().get(0);
         Long postId = posts.get(0).getId();
         Long commentId = comment.getId();
         // Modify a post
         forum.editPost(villageId, categoryId, postId, "ASD2");
         posts = forum.getForumPosts(villageId, categoryId, 10, false, 0);
         assertEquals(posts.size(), 1);
         assertEquals(posts.get(0).getText(), "ASD2");
         // Modify a comment
         forum.editComment(villageId, categoryId, postId, commentId, "COMMENT2");
         posts = forum.getForumPosts(villageId, categoryId, 10, true, 10);
         assertEquals(posts.size(), 1);
         post = posts.get(0);
         assertEquals(post.getComments().size(), 1);
         comment = post.getComments().get(0);
         assertEquals(comment.getText(), "COMMENT2");
     }
 
     /**
      * Delete a post.
      */
     @Test
     public void t5DeletePost()
     {
         Village v = portal.getVillage(TEST_VILLAGE_CODE);
         Long villageId = v != null ? v.getId() : null;
         List<ForumCategory> categories = forum.getForumCategories(villageId, 10, false, 0);
         assertEquals(categories.size(), 1);
         Long categoryId = categories.get(0).getId();
         List<ForumPost> posts = forum.getForumPosts(villageId, categoryId, 10, true, 10);
         assertEquals(posts.size(), 1);
         ForumPost post = posts.get(0);
         assertEquals(post.getComments().size(), 1);
         ForumComment comment = post.getComments().get(0);
         Long postId = posts.get(0).getId();
         Long commentId = comment.getId();
         // Delete the comment first
         forum.removeComment(villageId, categoryId, postId, commentId);
        posts = forum.getForumPosts(villageId, categoryId, 10, true, 10);
         assertEquals(posts.size(), 1);
        post = posts.get(0);
         assertEquals(post.getComments().size(), 0);
         // Remove the post
         forum.removePost(villageId, categoryId, postId);
        posts = forum.getForumPosts(villageId, categoryId, 10, true, 10);
         assertEquals(posts.size(), 0);
     }
     
     /**
      * Delete a category.
      */
     @Test
     public void t6DeleteCategory()
     {
         Village v = portal.getVillage(TEST_VILLAGE_CODE);
         Long villageId = v != null ? v.getId() : null;
         List<ForumCategory> categories = forum.getForumCategories(villageId, 10, false, 0);
         for (ForumCategory cat: categories)
             portal.deleteForumCategory(villageId, cat.getId());
         categories = forum.getForumCategories(villageId, 10, false, 0);
         assertTrue(categories.isEmpty());
     }
     
     /**
      * De-registration of the account.
      */
     @Test
     public void t7Unregister()
     {
         portal.deleteUserProfile();
         List<Village> villages = portal.getMyVillages();
         for (Village v: villages)
             portal.deleteVillage(v.getCode());
         
         UserProfile up = portal.getUserProfile("10");
         
         assertNull(up);
         assertTrue(portal.getMyVillages().isEmpty());
     }
 }
