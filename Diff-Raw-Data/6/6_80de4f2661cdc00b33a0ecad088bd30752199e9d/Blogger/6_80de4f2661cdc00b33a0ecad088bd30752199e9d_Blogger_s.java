 /*
 Copyright (c) 2007, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.google;
 
 import com.justjournal.User;
 import com.justjournal.WebLogin;
 import com.justjournal.db.DateTime;
 import com.justjournal.db.DateTimeBean;
 import com.justjournal.db.EntryDAO;
 import com.justjournal.db.EntryTo;
 import com.justjournal.utility.StringUtil;
 import org.apache.log4j.Category;
 
 import java.util.*;
 import java.io.Serializable;
 
 /**
  * User: laffer1
  * Date: Dec 3, 2007
  * Time: 4:21:42 PM
 * $Id: Blogger.java,v 1.11 2007/12/13 20:57:03 laffer1 Exp $
  * <p/>
  * A blogger 1 compatible interface exposed by XML-RPC
  * <p/>
  * http://www.blogger.com/developers/api/1_docs/
  * <p/>
  * Methods defined in Blogger 1.0 API:
  * blogger.newPost: Makes a new post to a designated blog. Optionally, will publish the blog after making the post.
  * blogger.editPost: Edits a given post. Optionally, will publish the blog after making the edit.
  * blogger.getUsersBlogs: Returns information on all the blogs a given user is a member of.
  * blogger.getUserInfo: Authenticates a user and returns basic user info (name, email, userid, etc.)
  * blogger.getTemplate: Returns the main or archive index template of a given blog.
  * blogger.setTemplate: Edits the main or archive index template of a given blog.
  */
 @SuppressWarnings({"UnusedParameters"})
 public class Blogger {
 
     private static final Category log = Category.getInstance(Blogger.class.getName());
 
     /**
      * Fetch the users personal information including their username, userid,
      * email address and name.
      * <p/>
      * The Blogger API defines nickname, userid, url, email, firstname and lastname here.  We don't track
      * last name.
      *
      * @param appkey   Not used but required by Blogger API
      * @param username username to authenticate/check
      * @param password the account secret
      * @return A HashMap of the users personal info or an error code as a hashmap
      */
     public HashMap<String, Serializable> getUsersInfo(String appkey, String username, String password) {
         int userId;
         boolean blnError = false;
         HashMap<String, Serializable> s = new HashMap<String, Serializable>();
 
 
         if (!StringUtil.lengthCheck(username, 3, 15)) {
             blnError = true;
         }
 
         if (!StringUtil.lengthCheck(password, 5, 18)) {
             blnError = true;
         }
 
         userId = WebLogin.validate(username, password);
         if (userId < 1)
             blnError = true;
 
         if (!blnError)
             try {
                 User user = new User(userId);
 
                 s.put("nickname", user.getUserName());
                 s.put("userid", userId);
                 s.put("url", "http://www.justjournal.com/users/" + user.getUserName());
                 s.put("email", user.getEmailAddress());
                 s.put("firstname", user.getFirstName());
             } catch (Exception e) {
                 blnError = true;
                 log.debug(e.getMessage());
             }
 
         if (blnError) {
             s.clear();
             s.put("faultCode", 4);
             s.put("faultString", "User authentication failed: " + username);
         }
 
 
         return s;
     }
 
 
     /**
      * Fetch the users blogs.  JJ only supports 1 blog per user right now.  Still this must be a list
      * of all blogs.
      * <p/>
      * The Blogger API defines url, blogid and blogName.
      *
      * @param appkey   Not used but required by Blogger API
      * @param username username to authenticate/check
      * @param password the account secret
      * @return A list of hash maps for each blog
      */
     public ArrayList<HashMap<Object, Serializable>> getUsersBlogs(String appkey, String username, String password) {
         int userId;
         boolean blnError = false;
         ArrayList<HashMap<Object, Serializable>> a = new ArrayList<HashMap<Object, Serializable>>();
         HashMap<Object, Serializable> s = new HashMap<Object, Serializable>();
 
 
         if (!StringUtil.lengthCheck(username, 3, 15)) {
             blnError = true;
         }
 
         if (!StringUtil.lengthCheck(password, 5, 18)) {
             blnError = true;
         }
 
         userId = WebLogin.validate(username, password);
         if (userId < 1)
             blnError = true;
 
         if (!blnError)
             try {
                 User user = new User(userId);
 
                 s.put("url", "http://www.justjournal.com/users/" + user.getUserName());
                 s.put("blogid", userId);
                 s.put("blogName", user.getJournalName());
             } catch (Exception e) {
                 blnError = true;
                 log.debug(e.getMessage());
             }
 
         if (blnError) {
             s.clear();
             s.put("faultCode", 4);
             s.put("faultString", "User authentication failed: " + username);
         }
 
         a.add(s);
 
         return a;
     }
 
 
     /**
      * Create a new blog entry using the limited feautres of the blogger api.
      *
      * @param appkey   Ignored but required
      * @param blogid   Ignored since JJ supports 1 blog per user.  Just the UID anyway.
      * @param username Username of blog owner.
      * @param password password of blog owner.
      * @param content  The body/content of the blog (no subjects)
      * @param publish  A boolean representing the publish state of the entry.  JJ does not support drafts yet.
      * @return The entry id of the entry as a string or an error.
      */
     public Serializable newPost(String appkey, String blogid, String username, String password, String content, Boolean publish) {
         String result = "";
         int userId;
         boolean blnError = false;
         final EntryTo et = new EntryTo();
         EntryTo et2;
         HashMap<String, Serializable> s = new HashMap<String, Serializable>();
 
         if (!StringUtil.lengthCheck(username, 3, 15)) {
             blnError = true;
         }
 
         if (!StringUtil.lengthCheck(password, 5, 18)) {
             blnError = true;
         }
 
         userId = WebLogin.validate(username, password);
         if (userId < 1)
             blnError = true;
 
         if (!blnError)
             try {
                 User user = new User(userId);
                 et.setUserId(userId);
                 DateTime d = new DateTimeBean();
                 d.set(new java.util.Date());
                 et.setDate(d);
                 //   et.setSubject(StringUtil.replace(request.getParameter("subject"), '\'', "\\\'"));
                 et.setBody(StringUtil.replace(content, '\'', "\\\'"));
                 //et.setMusic(StringUtil.replace(music, '\'', "\\\'"));
                 et.setSecurityLevel(2);   // public
                 et.setLocationId(0); // not specified
                 et.setMoodId(12);    // not specified
                 et.setAutoFormat(true);
                 et.setAllowComments(true);
                 et.setEmailComments(true);
                 et.setUserId(userId);
                 et.setUserName(user.getUserName());
 
                 EntryDAO.add(et);
                 et2 = EntryDAO.viewSingle(et);
                 result = Integer.toString(et2.getId());
 
             } catch (Exception e) {
                 blnError = true;
                 log.debug(e.getMessage());
             }
 
         if (blnError) {
             s.clear();
             s.put("faultCode", 4);
             s.put("faultString", "User authentication failed: " + username);
             return s;
         }
 
         return result;
     }
 
     /**
      * Modify a blog entry using the limited feautres of the blogger api.
      *
      * @param appkey   Ignored but required
      * @param postid   entry id as a string
      * @param username Username of blog owner.
      * @param password password of blog owner.
      * @param content  The body/content of the blog (no subjects)
      * @param publish  A boolean representing the publish state of the entry.  JJ does not support drafts yet.
      * @return The entry id of the entry as a string or an error.
      */
     public Serializable editPost(String appkey, String postid, String username, String password, String content, Boolean publish) {
         int userId;
         boolean blnError = false;
         HashMap<String, Serializable> s = new HashMap<String, Serializable>();
 
         int eid = 0;
 
         if (!StringUtil.lengthCheck(username, 3, 15)) {
             blnError = true;
         }
 
         if (!StringUtil.lengthCheck(password, 5, 18)) {
             blnError = true;
         }
 
         userId = WebLogin.validate(username, password);
         if (userId < 1)
             blnError = true;
 
         try {
             eid = Integer.parseInt(postid);
         } catch (IllegalFormatException ex) {
             blnError = true;
         }
 
         if (!blnError && eid > 0) {
             try {
                 /* we're just updating the content aka body as this is the
            only thing the protocol supports. */
                 EntryTo et2 = EntryDAO.viewSingle(eid, userId);
                 et2.setBody(StringUtil.replace(content, '\'', "\\\'"));
                 EntryDAO.update(et2);
             } catch (Exception e) {
                 blnError = true;
                 log.debug(e.getMessage());
             }
         }
 
         if (eid < 1) {
             s.put("faultCode", 4);
             s.put("faultString", "Invalid entry id " + postid);
 
         } else if (blnError) {
             s.clear();
             s.put("faultCode", 4);
             s.put("faultString", "User authentication failed: " + username);
 
         } else {
             return !blnError; /* ie true per spec */
         }
 
         return s;
     }
 
     /**
      * Retrieve recent posts from the blog in chronological order.
      * <p/>
      * Sample request:
      * <p/>
      * <p/>
      * <?xml version="1.0" encoding="UTF-8"?>
      * <methodCall>
      * <methodName>metaWeblog.getRecentPosts</methodName>
      * <params>
      * <param><value><int>178663</int></value></param>
      * <param><value><string>yourUsername</string></value></param>
      * <param><value><string>somePassword</string></value></param>
      * <param><value><int>4</int></value></param>
      * </params>
      * </methodCall>
      * <p/>
      * Sample response:
      * <p/>
      * <?xml version="1.0" encoding="UTF-8"?>
      * 2	<methodResponse>
      * 3	  <params>
      * 4	    <param><value><array><data><value>
      * 5	      <struct>
      * 6	        <member>
      * 7	          <name>link</name>
      * 8	          <value><string>http://typekeytest111.typepad.com/my_weblog/2005/07/one_more.html</string></value>
      * 9	        </member>
      * 10	        <member>
      * 11	          <name>permaLink</name>
      * 12	          <value><string>http://typekeytest111.typepad.com/my_weblog/2005/07/one_more.html</string></value>
      * 13	        </member>
      * 14	        <member>
      * 15	          <name>userid</name>
      * 16	          <value><string>28376</string></value>
      * 17	        </member>
      * 18	        <member>
      * 19	          <name>mt_allow_pings</name>
      * 20	          <value><int>0</int></value>
      * 21	        </member>
      * 22	        <member>
      * 23	          <name>mt_allow_comments</name>
      * 24	          <value><int>1</int></value>
      * 25	        </member>
      * 26	        <member>
      * 27	          <name>description</name>
      * 28	          <value><string/></value>
      * 29	        </member>
      * 30	        <member>
      * 31	          <name>mt_convert_breaks</name>
      * 32	          <value><string>0</string></value>
      * 33	        </member>
      * 34	        <member>
      * 35	          <name>postid</name>
      * 36	          <value><string>5423957</string></value>
      * 37	        </member>
      * 38	        <member>
      * 39	          <name>mt_excerpt</name>
      * 40	          <value><string/></value>
      * 41	        </member>
      * 42	        <member>
      * 43	          <name>mt_keywords</name>
      * 44	          <value><string/></value>
      * 45	        </member>
      * 46	        <member>
      * 47	          <name>title</name>
      * 48	          <value><string>One more!</string></value>
      * 49	        </member>
      * 50	        <member>
      * 51	          <name>mt_text_more</name>
      * 52	          <value><string/></value>
      * 53	        </member>
      * 54	        <member>
      * 55	          <name>dateCreated</name>
      * 56	          <value><dateTime.iso8601>2005-07-02T02:37:04Z</dateTime.iso8601></value>
      * 57	        </member>
      * 58	      </struct></value></data></array></value>
      * 59	    </param>
      * 60	  </params>
      * 61	</methodResponse>
      * <p/>
      * URI of example: http://www.sixapart.com/developers/xmlrpc/blogger_api/bloggergetrecentposts.html
      *
      * @param appkey        Unused, but required by Blogger API
      * @param blogid        Unused, but required by Blogger API
      * @param username      user to check
      * @param password      password for user
      * @param numberOfPosts The max number of posts to get.  (we should match this exactly)
      * @return An arraylist of posts or a hashmap of error info.
      */
     public Cloneable getRecentPosts(String appkey, String blogid, String username, String password, int numberOfPosts) {
         ArrayList<HashMap<Object, Serializable>> arr = new ArrayList<HashMap<Object, Serializable>>(numberOfPosts);
         Collection<EntryTo> total;
         Boolean blnError = false;
         int userId;
         HashMap<String, Serializable> s = new HashMap<String, Serializable>();
 
         if (!StringUtil.lengthCheck(username, 3, 15)) {
             blnError = true;
         }
 
         if (!StringUtil.lengthCheck(password, 5, 18)) {
             blnError = true;
         }
 
         userId = WebLogin.validate(username, password);
         if (blnError || userId < 1) {
             s.put("faultCode", 4);
             s.put("faultString", "User authentication failed: " + username);
             return s;
         }
 
         total = EntryDAO.viewAll(username, true);
 
         Iterator<EntryTo> it = total.iterator();
 
         for (int i = 0; i < numberOfPosts; i++)
             if (it.hasNext()) {
                 HashMap<Object, Serializable> entry = new HashMap<Object, Serializable>();
                 EntryTo e = it.next();
                 entry.put("link", "http://www.justjournal.com/users/" + e.getUserName() + "/entry/" + e.getId());
                 entry.put("permaLink", "http://www.justjournal.com/users/" + e.getUserName() + "/entry/" + e.getId());
                 entry.put("userid", Integer.toString(e.getUserId()));
                 entry.put("mt_allow_pings", 0);     /* TODO: on or off? */
                 entry.put("mt_allow_comments", 1);  /* TODO: on or off? */
                 entry.put("description", e.getBodyWithoutHTML());  /* TODO: change format? */
                 entry.put("content", e.getBodyWithoutHTML());  /* TODO: change format? */
                 entry.put("mt_convert_breaks", 0);                 /* TODO: research what these are... */
                 entry.put("postid", Integer.toString(e.getId()));
                 entry.put("mt_excerpt", e.getBodyWithoutHTML());
                 entry.put("mt_keywords", "");
                 entry.put("title", e.getSubject());
                 entry.put("mt_text_more", "");
                 entry.put("dateCreated", e.getDate().toDate()); /* TODO: needs to be iso8601 */
                 arr.add(entry);
             }
 
         return arr;
     }
 
     /**
      * Get a single blog entry by the post id (aka entry id)
      *
      * @param appkey   For google compatibility.. not used
      * @param postid   the entry id to fetch
      * @param username the username of the entry
      * @param password the password of the entry
      * @return a signle entry as a hashmap for consumption by xml-rpc
      */
     public HashMap<Object, Serializable> getPost(String appkey, String postid, String username, String password) {
         Boolean blnError = false;
         int userId;
         HashMap<Object, Serializable> s = new HashMap<Object, Serializable>();
         HashMap<Object, Serializable> entry = new HashMap<Object, Serializable>();
         EntryTo e;
 
         if (!StringUtil.lengthCheck(username, 3, 15)) {
             blnError = true;
         }
 
         if (!StringUtil.lengthCheck(password, 5, 18)) {
             blnError = true;
         }
 
         userId = WebLogin.validate(username, password);
         if (blnError || userId < 1) {
             s.put("faultCode", 4);
             s.put("faultString", "User authentication failed: " + username);
             return s;
         }
 
         e = EntryDAO.viewSingle(Integer.parseInt(postid), true);
 
         entry.put("link", "http://www.justjournal.com/users/" + e.getUserName() + "/entry/" + e.getId());
         entry.put("permaLink", "http://www.justjournal.com/users/" + e.getUserName() + "/entry/" + e.getId());
         entry.put("userid", Integer.toString(e.getUserId()));
         entry.put("mt_allow_pings", 0);     /* TODO: on or off? */
         entry.put("mt_allow_comments", 1);  /* TODO: on or off? */
         entry.put("description", e.getBodyWithoutHTML());  /* TODO: change format? */
         entry.put("content", e.getBodyWithoutHTML());  /* TODO: change format? */
         entry.put("mt_convert_breaks", 0);                 /* TODO: research what these are... */
         entry.put("postid", Integer.toString(e.getId()));
         entry.put("mt_excerpt", e.getBodyWithoutHTML());
         entry.put("mt_keywords", "");
         entry.put("title", e.getSubject());
         entry.put("mt_text_more", "");
         entry.put("dateCreated", e.getDate().toDate()); /* TODO: needs to be iso8601 */
 
         return entry;
     }
 }
