 package io.appery.tester.utils;
 
 /**
  * @author Daniel Lukashevich
  */
 public interface Constants {
 
     public interface API {
         public static final String LOGIN = "/app/rest/user/login";
 
         public static final String GET_PROJECTLIST = "/app/rest/user/projects";
 
         public static final String GET_PROJECT_RESOURCE = "/app/rest/user/project/%1$d/" + FILENAME_ZIP;
 
        public static final String LOGOUT = "/app/logout";
 
         public static final String GET_USER_ID = "/app/rest/user/user-id";
     }
 
     public interface DIALOGS {
 
         public static final int PROGRESS = 1;
 
         public static final int SIGN_IN = 2;
 
         public static final int COMPLETE_ACTION = 3;
 
         public static final int PROJECT_ACTION = 4;
 
         public static final int SORT = 6;
     }
 
     public interface MENU_OPTIONS {
 
         public static final int REFRESH = 2;
 
         public static final int SORT = 3;
 
         public static final int FILTER = 4;
 
         public static final int SORT_BY_NAME =5 ;
         public static final int SORT_BY_CREATE =6 ;
         public static final int SORT_BY_EDIT =7 ;
         public static final int SELECT_FOLDER =8 ;
         public static final int LOGOUT =9 ;
     }
 
     public interface PREFERENCES {
         public static final String BASE_URL = "base_url";
 
         public static final String USERNAME = "username";
 
         public static final String PASSWORD = "password";
 
         public static final String FIX_VERSION = "fix_version";
 
     }
 
     public interface EXTRAS {
         public static final String SORT_BY = "SORT_BY";
 
         public static final String PROJECTS_LIST = "PROJECTS_LIST";
 
         public static final String SELECTED_PROJECT = "SELECTED_PROJECT";
 
         public static final String USER_ID = "USER_ID";
 
     }
 
     public interface SERVER_CONSTANTS {
         public static final String OLD_APPERY_URL_HTTP = "http://project.tiggzi.com";
 
         public static final String OLD_APPERY_URL_HTTPS = "https://project.tiggzi.com";
 
         public static final String NEW_APPERY_URL_HTTPS = "https://appery.io";
 
     }
 
     public static final String FILENAME_APK = "app.apk";
 
     public static final String FILENAME_ZIP = "project.zip";
 
 }
