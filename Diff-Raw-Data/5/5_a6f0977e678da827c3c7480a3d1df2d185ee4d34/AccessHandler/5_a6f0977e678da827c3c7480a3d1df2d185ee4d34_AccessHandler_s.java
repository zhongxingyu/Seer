 /**
  * This file is part of RibbonServer application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package ribbonserver;
 
 import java.util.Arrays;
 
 /**
  * Access control class and handler
  * @author Stanislav Nepochatov
  * @since RibbonServer a2
  */
 public final class AccessHandler {
     
     private static String LOG_ID = "ДОСТУП";
     
     /**
      * User storage list.
      * @since RibbonServer a2
      */
     private static java.util.ArrayList<UserClasses.UserEntry> userStore;
     
     /**
      * Group storage list.
      * @since RibbonServer a2
      */
     private static java.util.ArrayList<UserClasses.GroupEntry> groupStore;
     
     /**
      * Init this component;
      * @since RibbonServer a2
      */
     public static void init() {
         AccessHandler.groupStore = IndexReader.readGroups();
         AccessHandler.groupStore.add(new UserClasses.GroupEntry("{ADM},{Службова група адміністраторів системи \"Стрічка\"}"));
         RibbonServer.logAppend(LOG_ID, 3, "індекс груп опрацьвано (" + groupStore.size() + ")");
         AccessHandler.userStore = IndexReader.readUsers();
         RibbonServer.logAppend(LOG_ID, 3, "індекс користувачів опрацьвано (" + userStore.size() + ")");
     }
     
     /**
      * Check access to directory with specified mode;<br>
      * <br>
      * <b>Modes:</b><br>
      * 0 - attempt to read directory;<br>
      * 1 - attempt to release messege in directory;<br>
      * 2 - attempt to admin directory;
      * @param givenName user name which attempt to perform some action
      * @param givenDir directory path 
      * @param givenMode mode of action (read, write or admin)
      * @return result of access checking
      * @since RibbonServer a2
      */
     public static Boolean checkAccess(String givenName, String givenDir, Integer givenMode) {
         java.util.ListIterator<UserClasses.UserEntry> userIter = AccessHandler.userStore.listIterator();
         UserClasses.UserEntry findedUser = null;
         while (userIter.hasNext()) {
             UserClasses.UserEntry currUser = userIter.next();
             if (currUser.USER_NAME.equals(givenName)) {
                 findedUser = currUser;
                 break;
             }
         }
         String[] keyArray = Arrays.copyOf(findedUser.GROUPS, findedUser.GROUPS.length + 1);
         keyArray[keyArray.length - 1] = findedUser.USER_NAME;
         Boolean findedAnswer = false;
         DirClasses.DirPermissionEntry fallbackPermission = null;
         DirClasses.DirPermissionEntry[] dirAccessArray = Directories.getDirAccess(givenDir);
        if (dirAccessArray == null) {
             for (Integer keyIndex = 0; keyIndex < keyArray.length; keyIndex++) {
                 for (Integer dirIndex = 0; dirIndex < dirAccessArray.length; dirIndex++) {
                     if (keyArray[keyIndex].equals("ADM") && !keyIndex.equals(keyArray.length - 1)) {
                         return true;    //ADM is root-like group, all permission will be ignored
                     }
                     if (dirAccessArray[dirIndex].KEY.equals("ALL")) {
                         fallbackPermission = dirAccessArray[dirIndex];
                         continue;
                     }
                     if (dirAccessArray[dirIndex].KEY.equals(keyArray[keyIndex])) {
                         findedAnswer = dirAccessArray[dirIndex].checkByMode(givenMode);
                         if (findedAnswer == true) {
                             return findedAnswer;
                         }
                     }
                 }
             }
         } else {
             for (Integer keyIndex = 0; keyIndex < keyArray.length; keyIndex++) {
                 if (keyArray[keyIndex].equals("ADM") && !keyIndex.equals(keyArray.length - 1)) {
                     if (keyArray[keyIndex].equals("ADM")) {
                         return true;    //ADM is root-like group, all permission will be ignored
                     }
                 }
             }
         }
         if (fallbackPermission == null) {
            fallbackPermission = new DirClasses.DirPermissionEntry("ALL:" + RibbonServer.ACCESS_ALL_MASK);
         }
         if (findedAnswer == false) {
             findedAnswer = fallbackPermission.checkByMode(givenMode);
         }
         return findedAnswer;
     }
     
     /**
      * Check access to directories with specified mode;<br>
      * <br>
      * <b>Modes:</b><br>
      * 0 - attempt to read directory;<br>
      * 1 - attempt to release messege in directory;<br>
      * 2 - attempt to admin directory;
      * @param givenName user name which attempt to perform some action
      * @param givenDirs array with directories which should be checked
      * @return null if success or array index which checking failed
      * @since RibbonServer a2
      */
     public static Integer checkAccessForAll(String givenName, String[] givenDirs, Integer givenMode) {
         for (Integer dirIndex = 0; dirIndex < givenDirs.length; dirIndex++) {
             if (AccessHandler.checkAccess(givenName, givenDirs[dirIndex], givenMode) == false) {
                 return dirIndex;
             }
         }
         return null;
     }
     
     /**
      * Find out is user is member of specified group.
      * @param givenName name to search
      * @param givenGroup name of group
      * @return result of checking
      * @since RibbonServer a2
      */
     public static Boolean isUserIsMemberOf(String givenName, String givenGroup) {
         java.util.ListIterator<UserClasses.UserEntry> userIter = AccessHandler.userStore.listIterator();
         UserClasses.UserEntry findedUser = null;
         while (userIter.hasNext()) {
             UserClasses.UserEntry currUser = userIter.next();
             if (currUser.USER_NAME.equals(givenName)) {
                 findedUser = currUser;
                 break;
             }
         }
         if (findedUser == null) {
             return false;
         }
         for (String groupItem : findedUser.GROUPS) {
             if (groupItem.equals("ADM")) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Login user or return error.
      * @param givenName name of user which is trying to login
      * @param givenHash md5 hash of user's password
      * @return null or error message
      * @since RibbonServer a2
      */
     public static String PROC_LOGIN_USER(String givenName, String givenHash) {
         UserClasses.UserEntry findedUser = null;
         java.util.ListIterator<UserClasses.UserEntry> usersIter = userStore.listIterator();
         while (usersIter.hasNext()) {
             UserClasses.UserEntry currUser = usersIter.next();
             if (currUser.USER_NAME.equals(givenName)) {
                 findedUser = currUser;
                 break;
             }
         }
         if (findedUser != null) {
             if (findedUser.H_PASSWORD.equals(givenHash)) {
                 if (!findedUser.IS_ENABLED) {
                     return "Користувач " + givenName + " заблоковано!";
                 } else {
                     return null;
                 }
             } else {
                 return "Невірний пароль!";
             }
         } else {
             return "Користувача " + givenName + " не знайдено!";
         }
     }
     
     /**
      * Resume user or return error.
      * @param givenEntry session entry of user which trying to resume;
      * @return null or error message;
      * @since RibbonServer a2
      */
     public static String PROC_RESUME_USER(SessionManager.SessionEntry givenEntry) {
         UserClasses.UserEntry findedUser = null;
         java.util.ListIterator<UserClasses.UserEntry> usersIter = userStore.listIterator();
         while (usersIter.hasNext()) {
             UserClasses.UserEntry currUser = usersIter.next();
             if (currUser.USER_NAME.equals(givenEntry.SESSION_USER_NAME)) {
                 findedUser = currUser;
                 break;
             }
         }
         if (findedUser != null) {
             if (!findedUser.IS_ENABLED) {
                 return "Користувач " + givenEntry.SESSION_USER_NAME + " заблоковано!";
             } else {
                 givenEntry.useEntry();
                 return null;
             }
         } else {
             return "Користувача " + givenEntry.SESSION_USER_NAME + " не знайдено!";
         }
     }
     
     /**
      * Return all users in short csv form to client or control application.
      * @param includAdm include ADM members in final result;
      * @return list with all users;
      * @since RibbonServer a2
      */
     public static String PROC_GET_USERS_UNI(Boolean includAdm) {
         StringBuffer userBuf = new StringBuffer();
         java.util.ListIterator<UserClasses.UserEntry> userIter = userStore.listIterator();
         while (userIter.hasNext()) {
             UserClasses.UserEntry currEntry = userIter.next();
             userBuf.append("{");
             userBuf.append(currEntry.USER_NAME);
             userBuf.append("},{");
             userBuf.append(currEntry.COMM);
             userBuf.append("}\n");
         }
         userBuf.append("END:");
         return userBuf.toString();
     }
     
     /**
      * Find out if there is group with given name
      * @param givenGroupName given name to search
      * @return true if group existed/false if not
      * @since RibbonServer a2
      */
     public static Boolean isGroupExisted(String givenGroupName) {
         java.util.ListIterator<UserClasses.GroupEntry> groupIter = groupStore.listIterator();
         while (groupIter.hasNext()) {
             if (groupIter.next().GROUP_NAME.equals(givenGroupName)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Find user entry by user name.
      * @param givenName name to search;
      * @return finded entry or null;
      */
     public static UserClasses.UserEntry getEntryByName(String givenName) {
         UserClasses.UserEntry result = null;
         java.util.ListIterator<UserClasses.UserEntry> userIter = AccessHandler.userStore.listIterator();
         while (userIter.hasNext()) {
             UserClasses.UserEntry curr = userIter.next();
             if (curr.USER_NAME.equals(givenName)) {
                 result = curr;
                 break;
             }
         }
         return result;
     }
     
 }
