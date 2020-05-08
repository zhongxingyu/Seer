 package epfl.sweng.globals;
 
 /**
  * Static global variables
  */
 public final class Globals {
 
 	public final static String RANDOM_QUESTION_URL = "https://sweng-quiz.appspot.com/quizquestions/random";
 	public final static String SUBMIT_QUESTION_URL = "https://sweng-quiz.appspot.com/quizquestions/";
 	public final static String QUIZSERVER_LOGIN_URL = "https://sweng-quiz.appspot.com/login";
 	public final static String AUTHSERVER_LOGIN_URL = "https://tequila.epfl.ch/cgi-bin/tequila/login";
 	
 	public final static int MAX_NUMBER_OF_ANSWERS = 10;
 	public final static boolean LOG_QUIZSERVER_REQUESTS = true;
 	public static final String LOGTAG_QUIZSERVER_COMMUNICATION = "QuizServer";
 	public static final boolean LOG_AUTH_REQUESTS = true;
 	public static final String LOGTAG_AUTH_COMMUNICATION = "Auth";
 	public static final int STATUSCODE_AUTHSUCCESSFUL = 302;
	public static final String PREFS_NAME = "user_session";
 	public final static int AUTHENTICATION_REQUEST_CODE = 1;
 	
 		
 }
