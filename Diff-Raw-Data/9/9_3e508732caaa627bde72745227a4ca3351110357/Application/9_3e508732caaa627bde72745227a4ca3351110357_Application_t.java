 package controllers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Answer;
 import models.Notification;
 import models.Question;
 import models.SystemInformation;
 import models.Tag;
 import models.TimeTracker;
 import models.User;
 import models.database.Database;
 import models.helpers.Tools;
 import notifiers.Mails;
 import play.cache.Cache;
 import play.data.validation.Required;
 import play.i18n.Lang;
 import play.libs.Codec;
 import play.libs.Images;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
 	private static final int entriesPerPage = 15;
 
 	@Before
 	static void setConnectedUser() {
 		if (Secure.Security.isConnected()) {
 			User user = Database.get().users().get(Secure.Security.connected());
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
 		List<Question> questions = (List<Question>) Cache
 				.get("index.questions");
 		if (questions == null) {
 			questions = Database.get().questions().all();
 			Collections.sort(questions, new Comparator<Question>() {
 				public int compare(Question q1, Question q2) {
 					return q2.timestamp().compareTo(q1.timestamp());
 				}
 			});
 			Cache.set("index.questions", questions, "10mn");
 		}
 		int maxIndex = Tools.determineMaximumIndex(questions, entriesPerPage);
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
 			List<Question> similarQuestions = (List<Question>) Cache
 					.get("question." + id + ".similar");
 			if (similarQuestions == null) {
 				similarQuestions = new ArrayList(question.getSimilarQuestions());
 				if (similarQuestions.size() > 5) {
 					// the Cache chokes on sublists!
					similarQuestions = new ArrayList<Question>(similarQuestions
							.subList(0, 5));
 				}
 				Cache.set("question." + id + ".similar", similarQuestions,
 						"10mn");
 			}
 			List<Answer> answers = question.answers();
 			render(question, answers, similarQuestions);
 		}
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
 		render(question);
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
 		render(answer, question);
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
 
 	/**
 	 * Prompts the user to mark this {@link Question} as Spam.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}
 	 */
 	public static void confirmMarkSpam(int id) {
 		Question question = Database.get().questions().get(id);
 		render(question);
 	}
 
 	public static void deleteuser() {
 		User showUser = Session.get().currentUser();
 		render(showUser);
 	}
 
 	public static void register() {
 		String randomID = Codec.UUID();
 		render(randomID);
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
 			@Required String email, String passwordrepeat,
 			@Required String code, String randomID) {
 		boolean isUsernameAvailable = Database.get().users().isAvailable(
 				username);
 		validation.equals(code, Cache.get("captcha." + randomID));
 		validation.equals(code, Cache.get(randomID));
 		if (validation.hasErrors()) {
 			flash.error("captcha.invalid");
 			params.flash();
 			register();
 		}
 		if (password.equals(passwordrepeat) && isUsernameAvailable) {
 			User user = Database.get().users().register(username, password,
 					email);
 			boolean success = Mails.welcome(user);
 			if (success) {
 				flash.success("secure.mail.success");
 				index(0);
 			} else {
 				user.delete();
 				flash.error("secure.mail.error");
 				register();
 			}
 		} else {
 			flash.keep("url");
 			if (!isUsernameAvailable) {
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
 		if (biography != null) {
 			biography = Tools.markdownToHtml(biography);
 		}
 		boolean canEdit = userCanEditProfile(showUser);
 		render(showUser, biography, canEdit);
 	}
 
 	/**
 	 * Renders a JSON list combining all the currently used tags starting with a
 	 * given term and the most often used words in a given (question's) content.
 	 * This list can be used for implementing client-side tag autocompletion.
 	 * 
 	 * @param term
 	 *            the part of the tag a user has already entered and that is
 	 *            supposed to be auto-completed
 	 * @param content
 	 *            the content of e.g. a question to search through for often
 	 *            occurring words that might also be useful as tags
 	 */
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
 	 * given index. Prevents a user from searching something different too soon
 	 * or anonymous users from searching at all - with the exception of the
 	 * search for a single tag which remains unrestricted.
 	 * 
 	 * @param term
 	 *            the term to be searched for.
 	 * @param index
 	 *            the page-number which will be displayed.
 	 */
 	public static void search(String term, int index) {
 		List<Question> results = (List<Question>) Cache.get("search." + term);
 		User user = Session.get().currentUser();
 		boolean isPureTagSearch = term.matches("^tag:\\S+$");
 
 		if (results != null) {
 			// we've already done this search lately, so we can
 			// let the user do it with hardly any additional cost
 		} else if (isPureTagSearch) {
 			// we currently allow the search for a single tag for all
 			// users all the time
 		} else if (user == null) {
 			flash.error("search.notloggedin");
 			// don't redirect to the calling page, as that might be a search
 			// page which would lead to an infinite loop of failing
 			index(0);
 		} else if (!user.canSearchFor(term)) {
 			flash.error("search.hastowait");
 			// don't redirect to the calling page, as that might be a search
 			// page which would lead to an infinite loop of failing
 			index(0);
 		}
 
 		if (results == null) {
 			results = Database.get().questions().searchFor(term);
 			Cache.set("search." + term, results, "5mn");
 		}
 		int maxIndex = Tools.determineMaximumIndex(results, entriesPerPage);
 		results = Tools.paginate(results, entriesPerPage, index);
 		if (user != null && !isPureTagSearch) {
 			user.setLastSearch(term, SystemInformation.get().now());
 		}
 		render(results, term, index, maxIndex);
 	}
 
 	// TODO Javadoc
 	public static void notifications(int content) {
 		User user = Session.get().currentUser();
 		if (user != null) {
 			List<Notification> spamNotification = new LinkedList();
 			List<Question> suggestedQuestions = user.getSuggestedQuestions();
 			List<Notification> notifications = user.getNotifications();
 			List<Question> questions = Database.get().questions().all();
 			ArrayList<Question> watchingQuestions = new ArrayList<Question>();
 			for (Question question : questions) {
 				if (question.hasObserver(user)) {
 					watchingQuestions.add(question);
 				}
 			}
 			if (user.isModerator()) {
 				spamNotification.addAll(Database.get().users()
 						.getModeratorMailbox().getNewNotifications());
 			}
 			render(notifications, watchingQuestions, suggestedQuestions,
 					spamNotification, content);
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
 		User user = Session.get().currentUser();
 		if (user == null || !user.isModerator()) {
 			flash.error("secure.moderatorerror");
 			Application.index(0);
 		}
 		render();
 	}
 
 	/**
 	 * Leads the the clearDB page.
 	 */
 	public static void clearDB() {
 		User user = Session.get().currentUser();
 		if (user == null || !user.isModerator()) {
 			flash.error("secure.moderatorerror");
 			Application.index(0);
 		}
 		render();
 	}
 
 	/**
 	 * Changes the language of the user interface.
 	 * 
 	 * @param langId
 	 *            an ISO-631-like language code (e.g. en, de, fr)
 	 */
 	public static void selectLanguage(@Required String langId) {
 		if (langId != null) {
 			Lang.change(langId);
 			if (!Lang.get().equals(langId)) {
 				flash.error("Unknown language %s!", langId);
 			}
 		} else {
 			flash.error("Wanna silence me? Try again!");
 		}
 		if (!CUser.redirectToCallingPage()) {
 			index(0);
 		}
 	}
 
 	/**
 	 * Leads to the edit-view of the {@link User}'s profile
 	 * 
 	 * @param userName
 	 *            the name of the {@link User} who owns the profile
 	 */
 	public static void editProfile(@Required String userName) {
 		User showUser = Database.get().users().get(userName);
 		if (!userCanEditProfile(showUser)) {
 			showprofile(userName);
 		}
 		render(showUser);
 	}
 
 	/**
 	 * Confirm a {@link User}'s profile if they clicked on the right link
 	 * 
 	 * @param username
 	 *            of the {@link User}
 	 * @param key
 	 *            for the Confirmation
 	 */
 	public static void confirmUser(@Required String username, String key) {
 		User user = Database.get().users().get(username);
 		boolean existsUser = Database.get().users().isAvailable(username);
 		if (!existsUser && key.equals(user.getConfirmKey())) {
 			user.confirm();
 			flash.success("user.confirm.success");
 			try {
 				Secure.login();
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 		} else {
 			flash.error("user.confirm.error");
 			index(0);
 		}
 	}
 
 	/**
 	 * Generates a random captcha-image.
 	 * 
 	 * @param id
 	 */
 	public static void captcha(String id) {
 		Images.Captcha captcha = Images.captcha();
 		String code = captcha.getText("#ff8400");
 		Cache.set(id, "capthca." + code, "3mn");
 		renderBinary(captcha);
 	}
 
 }
