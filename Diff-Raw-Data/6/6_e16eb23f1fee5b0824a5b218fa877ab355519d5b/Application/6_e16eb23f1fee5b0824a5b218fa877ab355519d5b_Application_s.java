 package controllers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import models.Answer;
 import models.Comment;
 import models.Notification;
 import models.Question;
 import models.Tag;
 import models.TimeTracker;
 import models.User;
 import models.database.Database;
 import models.helpers.Tools;
 import play.data.validation.Required;
 import play.i18n.Lang;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
 	private static final int entriesPerPage = 15;
 
 	@Before
 	static void setConnectedUser() {
 		if (controllers.Secure.Security.isConnected()) {
 			User user = Database.get().users().get(
 					controllers.Secure.Security.connected());
 			renderArgs.put("user", user);
 		}
 	}
 
 	/**
 	 * Leads to the index page at a given page of {@link Question}'s.
 	 * 
 	 * @param index
 	 *            the number of the page of {@link Question}'s.
 	 */
 	public static void index(int index) {
 		List<Question> questions = Database.get().questions().all();
 		int maxIndex = Tools.determineMaximumIndex(questions, entriesPerPage);
 		Collections.sort(questions, new Comparator<Question>() {
 			public int compare(Question q1, Question q2) {
 				return (q2.timestamp()).compareTo(q1.timestamp());
 			}
 		});
 		questions = Tools.paginate(questions, entriesPerPage, index);
 		render(questions, index, maxIndex);
 	}
 
 	/**
 	 * Leads to the detailed view of a {@link Question}.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void question(int id) {
 		Question question = Database.get().questions().get(id);
 		if (question == null) {
 			render();
 		} else {
 			List<Question> similarQuestions = (new ArrayList(question
 					.getSimilarQuestions()));
 			if (similarQuestions.size() > 5) {
 				similarQuestions = similarQuestions.subList(0, 5);
 			}
 			List<Answer> answers = question.answers();
 			render(question, answers, similarQuestions);
 		}
 	}
 
 	/**
 	 * Renders the detailed view of a {@link Question} and it's {@link Answer} 
 	 * 's.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void answerQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		List<Question> questions = Database.get().questions().all();
 		List<Answer> answers = question.answers();
 		int count = question.answers().size();
 		render(questions, question, answers, count);
 	}
 
 	/**
 	 * Leads to the detailed view of the {@link Question} and the field to
 	 * submit a comment.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void commentQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		List<Question> questions = Database.get().questions().all();
 		List<Comment> comments = question.comments();
 		int count = question.comments().size();
 		render(questions, question, comments, count);
 	}
 
 	/**
 	 * Leads to the detailed view of the {@link Answer} and the field to submit
 	 * a comment.
 	 * 
 	 * @param id
 	 *            the id of the {@link Answer}
 	 */
 	public static void commentAnswer(int questionId, int answerId) {
 		Question question = Database.get().questions().get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		List<Comment> comments = answer.comments();
 		render(answer, comments, question);
 	}
 
 	/**
 	 * Prompts the user to confirm the deletion of the {@link Question}.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void confirmDeleteQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		render(question);
 	}
 
 	public static void deleteuser() {
 		User showUser = Session.get().currentUser();
 		render(showUser);
 	}
 
 	public static void register() {
 		render();
 	}
 
 	/**
 	 * Lets the {@link User} sign up. Error-messages will be displayed, if the
 	 * information submitted by the {@link User} is wrong.
 	 * 
 	 * @param username
 	 *            the name the {@link User} has entered. This field is
 	 *            mandatory.
 	 * @param password
 	 *            the password the {@link User} chooses.
 	 * @param passwordrepeat
 	 *            the repeated password.
 	 */
 	public static void signup(@Required String username, String password,
 			String passwordrepeat) {
 
 		if (password.equals(passwordrepeat) && User.isAvailable(username)) {
 			Database.get().users().register(username, password);
 			// Mark user as connected
 			session.put("username", username);
 			index(0);
 		} else {
 			flash.keep("url");
 			if (!User.isAvailable(username)) {
 				flash.error("secure.usernameerror");
 			}
 			if (!password.equals(passwordrepeat)) {
 				flash.error("secure.passworderror");
 			}
 			params.flash();
 			register();
 		}
 	}
 
 	/**
 	 * Checks whether a {@link User} can edit a profile.
 	 * 
 	 * @param showUser
 	 *            the {@link User} who is the owner of the profile.
 	 * @return
 	 */
 	public static boolean userCanEditProfile(User showUser) {
 		User user = Session.get().currentUser();
 		if (user == null)
 			return false;
 		return user == showUser && !showUser.isBlocked() || user.isModerator();
 	}
 
 	/**
 	 * Leads to the view of a {@link User}'s profile.
 	 * 
 	 * @param userName
 	 *            the name of the {@link User} who is the owner of the profile.
 	 */
 	public static void showprofile(String userName) {
 		User showUser = Database.get().users().get(userName);
 		String biography = showUser.getBiography();
 		if (biography != null)
 			biography = Tools.markdownToHtml(biography);
 		boolean canEdit = userCanEditProfile(showUser);
 		render(showUser, biography, canEdit);
 	}
 
 	// TODO Add javadoc
 	public static void tags(String term, String content) {
 		String tagString = "";
 		for (Tag tag : Database.get().tags().all()) {
 			if (term == null || tag.getName().startsWith(term.toLowerCase())) {
 				tagString += tag.getName() + " ";
 			}
 		}
 		tagString += Tools.extractImportantWords(content);
 		// make sure not to return an array with a single empty string ([""])
 		String[] tags = tagString.split("\\s+");
 		if (tagString.length() == 0) {
 			tags = new String[0];
 		}
 		renderJSON(tags);
 	}
 
 	/**
 	 * Performs a search for the entered term. The view is displayed at the
 	 * given index.
 	 * 
 	 * @param term
 	 *            the term to be searched for.
 	 * @param index
 	 *            the page-number which will be displayed.
 	 */
 	public static void search(String term, int index) {
 		List<Question> results = Database.get().questions().searchFor(term);
 		int maxIndex = Tools.determineMaximumIndex(results, entriesPerPage);
 
 		results = Tools.paginate(results, entriesPerPage, index);
 		render(results, term, index, maxIndex);
 	}
 
 	// TODO Javadoc
 	public static void notifications(int content) {
 		User user = Session.get().currentUser();
 		if (user != null) {
 			List<Question> suggestedQuestions = user.getSuggestedQuestions();
 			List<Notification> notifications = user.getNotifications();
 			List<Question> questions = Database.get().questions().all();
 			ArrayList<Question> watchingQuestions = new ArrayList<Question>();
 			for (Question question : questions) {
 				if (question.hasObserver(user)) {
 					watchingQuestions.add(question);
 				}
 			}
 			render(notifications, watchingQuestions, suggestedQuestions,
 					content);
 		} else {
 			Application.index(0);
 		}
 	}
 
 	/**
 	 * Leads to the statistical overview. The statistical data is calculated via
 	 * the {@link TimeTracker}.
 	 */
 	public static void showStatisticalOverview() {
 		TimeTracker t = TimeTracker.getTimeTracker();
 		int numberOfUsers = Database.get().users().count();
 		int numberOfQuestions = Database.get().questions().count();
 		int numberOfAnswers = Database.get().questions().countAllAnswers();
 		int numberOfHighRatedAnswers = Database.get().questions()
 				.countHighRatedAnswers();
 		int numberOfBestAnswers = Database.get().questions()
 				.countBestRatedAnswers();
 		float questionsPerDay = (float) numberOfQuestions / t.getDays();
 		float questionsPerWeek = (float) numberOfQuestions / t.getWeeks();
 		float questionsPerMonth = (float) numberOfQuestions / t.getMonths();
 		float answersPerDay = (float) numberOfAnswers / t.getDays();
 		float answersPerWeek = (float) numberOfAnswers / t.getWeeks();
 		float answersPerMonth = (float) numberOfAnswers / t.getMonths();
 
 		render(numberOfQuestions, numberOfAnswers, numberOfUsers,
 				numberOfHighRatedAnswers, numberOfBestAnswers, questionsPerDay,
 				questionsPerWeek, questionsPerMonth, answersPerDay,
 				answersPerWeek, answersPerMonth);
 	}
 
 	/**
 	 * Leads to the admin page where a moderator can edit several options e.g.
 	 * clear the database.
 	 */
 	public static void admin() {
 		if (!Session.get().currentUser().isModerator()) {
 			flash.error("secure.moderatorerror");
 			Application.index(0);
 		}
 		render();
 	}
 
 	/**
 	 * Leads the the clearDB page.
 	 */
 	public static void clearDB() {
 		if (!Session.get().currentUser().isModerator()) {
 			flash.error("secure.moderatorerror");
 			Application.index(0);
 		}
 		render();
 	}
 
 	public static void selectLanguage(@Required String langId) {
 		if (langId != null) {
 			Lang.change(langId);
 			if (!Lang.get().equals(langId))
 				flash.error("Unknown language %s!", langId);
 		} else
 			flash.error("Wanna silence me? Try again!");
 		if (!CUser.redirectToCallingPage())
 			Application.index(0);
 	}
 }
