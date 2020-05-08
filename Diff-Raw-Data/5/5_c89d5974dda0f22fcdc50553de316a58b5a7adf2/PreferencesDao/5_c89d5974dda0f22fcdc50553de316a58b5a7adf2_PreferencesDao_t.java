 /*
 Copyright (c) 2005, 2008 Lucas Holt
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
 
 package com.justjournal.db;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 /**
  * User: laffer1
  * Date: Jan 16, 2004
  * Time: 12:07:17 PM
  *
  * @author Lucas Holt
 * @version $Id: PreferencesDao.java,v 1.10 2008/08/01 11:41:11 laffer1 Exp $
  * @since 1.0
  *        <p/>
  *        1.3 Added show_avatar field select.
  *        1.2 Altered journal preferences query to include email address field.
  */
 public final class PreferencesDao {
     /**
      * Update the owner view only security feature.
      *
      * @param userId  userid of blog owner
      * @param ownerOnly  if the blog is private
      * @return true on success, false on any error
      */
     public static final boolean updateSec(int userId, boolean ownerOnly) {
         boolean noError = true;
         int records = 0;
         String ownerview = "N";
 
         if (ownerOnly)
             ownerview = "Y";
 
         final String sqlStmt = "Update user_pref SET owner_view_only='" + ownerview
                 + "' WHERE id='" + userId + "' LIMIT 1;";
 
         try {
             records = SQLHelper.executeNonQuery(sqlStmt);
         } catch (Exception e) {
             noError = false;
         }
 
         if (records != 1)
             noError = false;
 
 
         return noError;
     }
 
     /**
      * Retrieves the journal preferences for a certain user including
      * style information, and privacy settings.
      *
      * @param userName the user who needs their settings defined.
      * @return Preferences in cached rowset.
      * @throws Exception SQL exception
      */
     public static CachedRowSet ViewJournalPreferences(final String userName)
             throws Exception {
         CachedRowSet RS;
 
         if (userName == null)
             throw new IllegalArgumentException("Missing username.");
 
         if (userName.length() < 3)
             throw new IllegalArgumentException("Username must be at least 3 characters");
 
         String sqlStatement =
                 "SELECT user.name As name, user.id As id, user.since as since, up.style As style, up.allow_spider, " +
                         "up.owner_view_only, st.url as cssurl, st.doc as cssdoc, uc.email as email, " +
                         "up.show_avatar as show_avatar, up.journal_name as journal_name, ubio.content as bio FROM user, user_bio as ubio, user_pref As up, user_style as st, user_contact As uc " +
                        "WHERE user.username='" + userName + "' AND user.id = up.id AND user.id=st.id AND user.id=uc.id AND user.id = ubio.id LIMIT 1;";
 
         try {
             RS = SQLHelper.executeResultSet(sqlStatement);
         } catch (Exception e1) {
             throw new Exception("Couldn't get preferences: " + e1.getMessage());
         }
 
         return RS;
     }
 }
