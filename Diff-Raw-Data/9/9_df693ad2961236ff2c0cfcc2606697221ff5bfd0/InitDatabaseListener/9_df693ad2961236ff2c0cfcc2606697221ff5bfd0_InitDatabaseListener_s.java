 package quizsite.util;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 
 import quizsite.controllers.Util;
 import quizsite.models.*;
 import quizsite.models.messages.*;
 import quizsite.models.questions.CheckboxQuestion;
 import quizsite.models.questions.ResponseQuestion;
 
 /**
  * Application Lifecycle Listener implementation class InitDatabaseListener
  *
  */
 @WebListener
 public class InitDatabaseListener implements ServletContextListener {
 
 	/**
 	 * Default constructor. 
 	 */
 	public InitDatabaseListener() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
 	 */
 	public void contextInitialized(ServletContextEvent arg0) {
 		//By Instantiating each of these objects we will instantiate empty tables
 		//if the tables do not exist already. We do not save any of the models so
 		//the tables will remain empty
 		try {
 			DatabaseConnection.switchModeTo(Util.readMode());
 			dropTables();
 			createTables();
 			initTestData();
 		} catch (Exception e) {
 			System.err.println("QuizSite : Error : While dropping and creating tables");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
 	 */
 	public void contextDestroyed(ServletContextEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@SuppressWarnings("unused")
 	private void createTables() throws SQLException {
 		User user0 				= new User(null, null, null, null);
 		System.out.println("User Init");
 		Friendship friendship0	= new Friendship(null, null);
 		System.out.println("Friendship Init");
 		Message message0		= new Note(null, null, null);
 		System.out.println("Message Init");
		Quiz quiz0				= new Quiz(false, false, false, false, 0);
 		System.out.println("Quiz Init");
 		Set<String> filler = new HashSet<String>();
 		Question question0		= new CheckboxQuestion(filler, null, 0, new ArrayList<String>());
 		System.out.println("Question Init");
 		Attempt attempt0		= new Attempt(null, null , 0);
 		System.out.println("Attempt Init");
 		Achievement ach0 = new Achievement(null, null);
 	}
 
 
 	private void dropTables() throws SQLException {
 		DatabaseConnection.dropTablesIfExist(Achievement.TABLE_NAME, Attempt.TABLE_NAME, Question.TABLE_NAME, Quiz.TABLE_NAME, Message.TABLE_NAME, Friendship.TABLE_NAME, User.TABLE_NAME);
 	}
 
 	private void initTestData() throws SQLException {
 		final int TOTAL_TEST_USERS = 5;
 		final int TOTAL_TEST_FS = 6;
 		String[] userNames = {"Vighnesh","Bruce","Logan", "Makar", "John"};
 		String[] emails = {"vig@rege.com", "bru@wayne.com", "wol@x.com", "mak@ar.com", "john@ys.com"};
 		String[] passwords = {"pass", "pass","pass","pass","pass"};
 
 		// Save users
 		User[] users = new User[TOTAL_TEST_USERS];
 		for (int i = 0; i < TOTAL_TEST_USERS; i++) {
 			users[i] = User.get(User.registerNewUser(userNames[i], emails[i], passwords[i]) );
 		}
 
 		// Send some friend requests
 		Friendship[] fs = new Friendship[TOTAL_TEST_FS];
 		FriendRequest[] fr = new FriendRequest[TOTAL_TEST_FS];
 
 		fs[0] = Friendship.get((fr[0] = new FriendRequest(users[0], users[1], "")).send());
 		fs[1] = Friendship.get((fr[1] = new FriendRequest(users[3], users[1], "")).send());
 		fs[2] = Friendship.get((fr[2] = new FriendRequest(users[4], users[1], "")).send());
 		fs[3] = Friendship.get((fr[3] = new FriendRequest(users[0], users[2], "")).send());
 		fs[4] = Friendship.get((fr[4] = new FriendRequest(users[3], users[2], "")).send());
 		fs[5] = Friendship.get((fr[5] = new FriendRequest(users[4], users[2], "")).send());
 
 		// Accept some, reject some, keep rest pending
 		fs[0].accept();
 		fs[1].accept();
 		fs[2].reject();
 		fs[3].reject();
 
 
 		// create some quizzes
 		Quiz[] quizzes = new Quiz[3];
		(quizzes[0] = new Quiz(false, true, false, true, users[0].getId())).save();
		(quizzes[1] = new Quiz(true, true, false, true, users[1].getId())).save();
		(quizzes[2] = new Quiz(false, false, false, true, users[2].getId())).save();
 
 		// Send some challenges
 		Challenge[] challenges = new Challenge[3];
 		(challenges[0] = new Challenge(users[0], users[1], quizzes[0])).save();
 		(challenges[1] = new Challenge(users[1], users[2], quizzes[1])).save();
 		(challenges[2] = new Challenge(users[2], users[3], quizzes[2])).save();
 
 		// Send some notes
 		Note[] notes = new Note[4];
 		(notes[0] = new Note(users[0], users[1], "0-1")).save();
 		(notes[1] = new Note(users[1], users[2], "1-2")).save();
 		(notes[2] = new Note(users[0], users[2], "0-2")).save();
 		(notes[3] = new Note(users[4], users[3], "4-3")).save();
 
 		// Set of answers
 		Set<String> ans = new HashSet<String>();
 		ans.add("oye");
 		ans.add("answer");
 		ans.add("booyah");
 
 		// Create some questions
 		Question[] questions = new Question[15];
 		(questions[0] = new ResponseQuestion(ans, "1:What is the answer? One of: oye, answer, booyah", quizzes[0].getId())).save();
 		(questions[1] = new ResponseQuestion(ans, "2:What is the answer? One of: oye, answer, booyah", quizzes[0].getId())).save();
 		(questions[2] = new ResponseQuestion(ans, "3:What is the answer? One of: oye, answer, booyah", quizzes[0].getId())).save();
 		(questions[3] = new ResponseQuestion(ans, "4:What is the answer? One of: oye, answer, booyah", quizzes[0].getId())).save();
 		(questions[4] = new ResponseQuestion(ans, "5:What is the answer? One of: oye, answer, booyah", quizzes[0].getId())).save();
 
 		(questions[5] = new ResponseQuestion(ans, "1:What is ze answer? One of: oye, answer, booyah", quizzes[1].getId())).save();
 		(questions[6] = new ResponseQuestion(ans, "2:What is ze answer? One of: oye, answer, booyah", quizzes[1].getId())).save();
 		(questions[7] = new ResponseQuestion(ans, "3:What is ze answer? One of: oye, answer, booyah", quizzes[1].getId())).save();
 		(questions[8] = new ResponseQuestion(ans, "4:What is ze answer? One of: oye, answer, booyah", quizzes[1].getId())).save();
 		(questions[9] = new ResponseQuestion(ans, "5:What is ze answer? One of: oye, answer, booyah", quizzes[1].getId())).save();
 
 		(questions[10] = new ResponseQuestion(ans, "1:Where is the answer? One of: oye, answer, booyah", quizzes[2].getId())).save();
 		(questions[11] = new ResponseQuestion(ans, "2:Where is the answer? One of: oye, answer, booyah", quizzes[2].getId())).save();
 		(questions[12] = new ResponseQuestion(ans, "3:Where is the answer? One of: oye, answer, booyah", quizzes[2].getId())).save();
 		(questions[13] = new ResponseQuestion(ans, "4:Where is the answer? One of: oye, answer, booyah", quizzes[2].getId())).save();
 		(questions[14] = new ResponseQuestion(ans, "5:Where is the answer? One of: oye, answer, booyah", quizzes[2].getId())).save();
 
 		// Save some attempts
 		Attempt[] attempts = new Attempt[20];
 		(attempts[0] = new Attempt(quizzes[0], users[0], 21)).save();
 		(attempts[1] = new Attempt(quizzes[0], users[0], 22)).save();
 		(attempts[2] = new Attempt(quizzes[0], users[2], 23)).save();
 		(attempts[3] = new Attempt(quizzes[0], users[3], 24)).save();
 		(attempts[4] = new Attempt(quizzes[1], users[0], 20)).save();
 		(attempts[5] = new Attempt(quizzes[1], users[1], 30)).save();
 		(attempts[6] = new Attempt(quizzes[1], users[2], 40)).save();
 		(attempts[7] = new Attempt(quizzes[1], users[3], 50)).save();
 		(attempts[8] = new Attempt(quizzes[1], users[4], 60)).save();
 		(attempts[9] = new Attempt(quizzes[1], users[4], 70)).save();
 		(attempts[10] = new Attempt(quizzes[2], users[4], 10)).save();
 		(attempts[11] = new Attempt(quizzes[2], users[4], 22)).save();
 		(attempts[12] = new Attempt(quizzes[2], users[3], 30)).save();
 		(attempts[13] = new Attempt(quizzes[2], users[3], 40)).save();
 		(attempts[14] = new Attempt(quizzes[2], users[2], 23)).save();
 		(attempts[15] = new Attempt(quizzes[2], users[2], 23)).save();
 		(attempts[16] = new Attempt(quizzes[2], users[1], 40)).save();
 		(attempts[17] = new Attempt(quizzes[2], users[1], 50)).save();
 		(attempts[18] = new Attempt(quizzes[2], users[0], 24)).save();
 		(attempts[19] = new Attempt(quizzes[2], users[0], 24)).save();
 	}
 }
