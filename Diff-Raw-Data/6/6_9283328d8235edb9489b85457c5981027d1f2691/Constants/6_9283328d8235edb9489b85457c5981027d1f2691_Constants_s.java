 package assistant;
 
 import sun.swing.plaf.synth.DefaultSynthStyle;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-13
  * Time: 14:07
  */
 public abstract class Constants {
 
     public abstract static class General {
 
         public static final String PERSISTENCE_NAME ="hyperactivity_persistence";
         public static final String SERVER_OS = "Linux";
         public static final String OS_PROPERTY = "os.name";
         public static final boolean IS_DEVELOPMENT_SERVER = (System.getProperty(OS_PROPERTY).equals(SERVER_OS)) ? false : true;
         public static final String LOCAL_SERVER_IP = "192.168.1.101";
         public static final String EXTERNAL_SERVER_IP = "89.253.85.33";
         public static final String HIBERNATE_PERSISTENCE_CONNECTION_NAME = "hibernate.connection.url";
         public static final String HIBERNATE_PERSISTENCE_CONNECTION_DEVELOPMENT_VALUE = "jdbc:mysql://89.253.85.33/Hyperactivity";
         public static final String HIBERNATE_PERSISTENCE_CONNECTION_VALUE = "jdbc:mysql://127.0.0.1/Hyperactivity";
     }
 
     public abstract class Method{
         public static final String LOGIN = "login";
         public static final String GET_PROFILE = "get_profile";
         public static final String UPDATE_PROFILE = "update_profile";
        public static final String GET_FORUM = "get_forum";
        public static final String GET_CATEGORY = "get_category";
        public static final String GET_THREAD = "get_thread";
         public static final String CREATE_THREAD = "create_thread";
         public static final String CREATE_REPLY = "create_reply";
     }
 
     public abstract class Param{
         public abstract class Name{
             public static final String TOKEN = "token";
             public static final String ACCOUNT = "account";
             public static final String ACCOUNT_ID = "account_id";
             public static final String PROFILE = "profile";
             public static final String DESCRIPTION = "description";
             public static final String SHOW_BIRTH_DATE = "show_birth_date";
             public static final String AVATAR = "avatar";
             public static final String TYPE = "type";
 
             public static final String CATEGORIES = "categories";
             public static final String THREAD_ID = "thread_id";
             public static final String TEXT = "text";
             public static final String SORT_TYPE = "sort_type";
             public static final String HEADLINE = "headline";
             public static final String CATEGORY_ID = "category_id";
             public static final String CATEGORY = "category";
             public static final String REPLY = "reply";
         }
         public abstract class Status{
             public static final String STATUS = "status";
             public static final String FIRST_LOGIN = "first_login";
             public static final String PROFILE_NOT_FOUND = "profile_not_found";
             public static final String SUCCESS = "success";
 
             public static final String CATEGORY_NOT_FOUND = "category_not_found";
             public static final String THREAD_NOT_FOUND = "thread_not_found";
         }
 
         public abstract class Value {
             public static final String PUBLIC = "public";
             public static final String PRIVATE = "private";
         }
     }
 
     public abstract class Json {
 
     }
 
     public abstract class Http {
         public static final int PORT = 12345;
         public static final String CONTENT_LENGTH = "Content-length";
 
     }
     public class Query {
 
         public static final String FACEBOOK_ID = "facebook_id";
         public static final String PARENT_CATEGORY_ID = "parent_category_id";
     }
 
     public class Database{
         public static final int NO_LIMIT_PER_DAY = 0;
         public static final boolean DEFAULT_SHOW_BIRTH_DATE = false;
         public static final boolean DEFAULT_USE_DEFAULT_COLORS = true;
     }
 
     public class Errors {
         public static final String BAD_HEADERS = "Bad headers";
         public static final String BAD_ID = "ID could not be converted to int";
         public static final String METHOD_NOT_FOUND = "Method not found";
         public static final String PARAM_NOT_FOUND = "A needed param is not found";
         public static final String PARAM_NOT_ALLOWED = "Some given params are not allowed";
         public static final String PARAM_VALUE_NOT_ALLOWED = "The given param value is not allowed";
     }
 }
