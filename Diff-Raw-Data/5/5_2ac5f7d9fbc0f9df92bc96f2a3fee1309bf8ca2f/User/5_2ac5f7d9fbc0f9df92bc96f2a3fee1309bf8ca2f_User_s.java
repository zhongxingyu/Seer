 /*
 Copyright (c) 2003-2006, Lucas Holt
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
 
 package com.justjournal;
 
 import com.justjournal.db.PreferencesDao;
 import com.justjournal.db.UserDao;
 import com.justjournal.db.UserTo;
 import sun.jdbc.rowset.CachedRowSet;
 
 /**
  * Represents a user's basic credentals including userId and
  * userName.
  *
  * @author Lucas Holt
 * @version $Id: User.java,v 1.12 2006/11/22 02:46:08 laffer1 Exp $
  *          Date: Jan 4, 2004
  *          Time: 9:59:35 PM
  * @since 1.0
  */
 public final class User {
     private String userName = "*";
     private int userId = 0;
     private int type = 0;
     private String firstName = ""; // real name
     private int startYear = 2003;  // starting year for account
 
     private boolean allowSpider = false;
     private boolean privateJournal = false;  // journal viewable only by owner.
     private int styleId = 1;  // theme of journal?
     private String styleDoc = "";
     private String styleUrl = "";
     private int emoticon = 1;  // default emoticon theme
 
     private String emailAddress = "";
     private boolean showAvatar = false;
 
     private String journalName = "";
 
     public User(final String userName) throws Exception {
         setUserName(userName);
 
         UserTo ut = UserDao.view(userName);
 
         setUserId(ut.getId());
         setFirstName(ut.getName());
 
         try {
             CachedRowSet RS = PreferencesDao.ViewJournalPreferences(userName);
 
             if (RS.next()) {
                 this.styleId = RS.getInt("style");
 
                 if (RS.getString("cssdoc") == null)
                     this.styleDoc = "";
                 else
                     this.styleDoc = RS.getString("cssdoc");
 
                 if (RS.getString("cssurl") == null)
                     this.styleUrl = "";
                 else
                     this.styleUrl = RS.getString("cssurl");
 
                 this.emailAddress = RS.getString("email");
 
                 if (RS.getString("journal_name") == null)
                     this.journalName = "";
                 else
                     this.journalName = RS.getString("journal_name");
 
                 if (RS.getInt("since") > 2003)
                     startYear = RS.getInt("since");
 
                 if (RS.getString("allow_spider").equals("Y")) {
                     this.allowSpider = true;
                 } else {
                     this.allowSpider = false;
                 }
 
                 if (RS.getString("owner_view_only").equals("Y")) {
                     this.privateJournal = true;
                 } else {
                     this.privateJournal = false;
                 }
 
                 if (RS.getString("show_avatar").equals("Y")) {
                     this.showAvatar = true;
                 } else {
                     this.showAvatar = false;
                 }
             }
 
             RS.close();
         } catch (Exception ePrefs) {
             throw new Exception("Error loading user information", ePrefs);
         }
     }
 
     public User(int userId) throws Exception {
         setUserId(userId);
 
         UserTo ut = UserDao.view(userId);
 
         setUserName(ut.getUserName());
         setFirstName(ut.getName());
 
         try {
             CachedRowSet RS = PreferencesDao.ViewJournalPreferences(userName);
 
             if (RS.next()) {
                 this.styleId = RS.getInt("style");
 
                 if (RS.getString("cssdoc") == null)
                     this.styleDoc = "";
                 else
                     this.styleDoc = RS.getString("cssdoc");
 
                 if (RS.getString("cssurl") == null)
                     this.styleUrl = "";
                 else
                     this.styleUrl = RS.getString("cssurl");
 
                 this.emailAddress = RS.getString("email");
 
                 if (RS.getString("journal_name") == null)
                     this.journalName = "";
                 else
                     this.journalName = RS.getString("journal_name");
 
                 if (RS.getInt("since") > 2003)
                     startYear = RS.getInt("since");
 
                 if (RS.getString("allow_spider").equals("Y")) {
                     this.allowSpider = true;
                 } else {
                     this.allowSpider = false;
                 }
 
                 if (RS.getString("owner_view_only").equals("Y")) {
                     this.privateJournal = true;
                 } else {
                     this.privateJournal = false;
                 }
 
                 if (RS.getString("show_avatar").equals("Y")) {
                     this.showAvatar = true;
                 } else {
                     this.showAvatar = false;
                 }
             }
 
             RS.close();
         } catch (Exception ePrefs) {
             throw new Exception("Error loading user information", ePrefs);
         }
     }
 
     public User(Integer userId) {
         setUserId(userId);
     }
 
     public User() {
 
     }
 
     /**
      * Retrieve the username associated with this user.
      *
      * @return the username
      */
     public String getUserName() {
         return this.userName;
     }
 
     /**
      * Set the username associated with this user. Maximum size
      * is 15 characters.
      *
      * @param userName The username associated with this account.
      */
     public void setUserName(final String userName) {
         if (userName == null)
             throw new IllegalArgumentException("username can not be null.");
 
         if (userName.length() > 15)
             throw new IllegalArgumentException("username can not be longer than 15 characters");
 
         if (userName.length() < 3)
             throw new IllegalArgumentException("username must be at least 3 characters.");
 
         this.userName = userName;
     }
 
     /**
      * Get the user id associated with this account.  The user id
      * is the unique identifier. It is an int where n >= 0.
      *
      * @return unique userid used with this account.
      */
     public int getUserId() {
         return this.userId;
     }
 
     /**
      * Set the unique user id associated with this user account.
      * This unique identifier is an int where n >= 0.
      *
      * @param userId A unique user id to set for the account.
      */
     public void setUserId(final int userId) {
         if (userId < 0)
             throw new IllegalArgumentException("user id must be greater than or equal to zero.");
 
         this.userId = userId;
     }
 
     /**
      * Sets the unique user id associated with this user account.
      * This version takes an integer version of the user id.
      *
      * @param userId Integer user id where n >= 0
      */
     public void setUserId(final Integer userId) {
         if (userId == null)
             throw new IllegalArgumentException("user id can not be null.");
 
         setUserId(userId.intValue());
     }
 
     /**
      * Retrieve the type of user account.  Default account type
      * is 0.  type will always be >= 0
      *
      * @return User account type.
      */
     public int getType() {
         return this.type;
     }
 
     /**
      * Set the type of user account as an int representation
      * greater than or equal to zero.
      *
      * @param type Account type >= 0
      */
     public void setType(final int type) {
         if (type < 0)
             throw new IllegalArgumentException("type must be greater than or equal to zero.");
         this.type = type;
     }
 
     /**
      * Get the Users first name as specified during
      * the signup process. Name can not be longer than
      * 20 characters.
      *
      * @return user's first name
      */
     public String getFirstName() {
         return this.firstName;
     }
 
     /**
      * Set the first name of the user.  Name length is limited
      * to 20 characters.
      *
      * @param firstName user's first name.
      */
     public void setFirstName(final String firstName) {
         if (firstName == null)
            throw new IllegalArgumentException("first name can not bell null.");
 
         if (firstName.length() > 20)
             throw new IllegalArgumentException("first name can not be longer than 20 characters.");
 
         if (firstName.length() < 2)
             throw new IllegalArgumentException("first name must be at least 2 characters.");
 
         this.firstName = firstName;
     }
 
     /**
      * Retrieve the year when this user account was
      * created.
      * <p/>
      * n >= 2003
      *
      * @return Year account was created.
      */
     public int getStartYear() {
         return this.startYear;
     }
 
     /**
      * Set the year when the user account was created.
      * <p/>
      * n >= 2003
      *
      * @param since Year account was created.
      */
     public void setStartYear(final int since) {
         if (since < 2003)
             throw new IllegalArgumentException("Start Year must be at least the year 2003.");
 
         this.startYear = since;
     }
 
     public boolean isSpiderAllowed() {
         return this.allowSpider;
     }
 
     public boolean isSpiderAllowed(boolean allowSpider) {
         this.allowSpider = allowSpider;
         return allowSpider;
     }
 
     public boolean isPrivateJournal() {
         return this.privateJournal;
     }
 
     public boolean isPrivateJournal(boolean privateJournal) {
         this.privateJournal = privateJournal;
         return privateJournal;
     }
 
     public int getEmoticon() {
         return emoticon;
     }
 
     public void setEmoticon(int value) {
         this.emoticon = value;
     }
 
     public int getStyleId() {
         return this.styleId;
     }
 
     public void setStyleId(int styleId) {
         this.styleId = styleId;
     }
 
     public String getStyleDoc() {
         return this.styleDoc;
     }
 
     public void setStyleDoc(String doc) {
         this.styleDoc = doc;
     }
 
     public String getStyleUrl() {
         return this.styleUrl;
     }
 
     public void setStyleUrl(String url) {
         this.styleUrl = url;
     }
 
     public String getEmailAddress() {
         return this.emailAddress;
     }
 
     public void setEmailAddress(String emailAddress) {
         this.emailAddress = emailAddress;
     }
 
     public boolean showAvatar() {
         return showAvatar;
     }
 
     public void showAvatar(boolean showAvatar) {
         this.showAvatar = showAvatar;
     }
 
     public String toString() {
         return Integer.toString(userId) + "," + userName + "," + type +
                 "," + firstName + "," + startYear + "," + journalName;
     }
 
     /**
      * Fetch the User's Title for their journal.
      *
      * @return journal title
      */
     public String getJournalName() {
         if (journalName.compareTo("") == 0)
             journalName = firstName + "'s Journal";
 
         return journalName;
     }
 
     public void recycle() {
         firstName = "";
         userId = 0;
         styleId = 1;
         allowSpider = false;
         privateJournal = false;
         styleDoc = "";
         styleUrl = "";
         startYear = 2003;
         emailAddress = "";
         journalName = "";
     }
 }
