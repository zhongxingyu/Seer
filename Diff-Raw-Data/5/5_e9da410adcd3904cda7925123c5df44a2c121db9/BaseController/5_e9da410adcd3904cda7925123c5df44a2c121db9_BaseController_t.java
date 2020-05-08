 package controllers;
 
 import models.Answer;
 import models.Comment;
 import models.Entry;
 import models.Question;
 import models.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http;
 
 /**
  * Some basic methods that should be available to all controllers.
  */
 public abstract class BaseController extends Controller {
 
 	/**
 	 * Makes the connected user available to all views as 'user'.
 	 */
 	@Before
 	static void setConnectedUser() {
 		if (Security.isConnected()) {
 			User user = Database.users().get(Security.connected());
 			renderArgs.put("user", user);
 		}
 	}
 
 	/**
 	 * Redirects to the calling page.
 	 * 
 	 * @return true, if a redirect header was found; false, if the caller should
 	 *         call an explicit Controller action instead.
 	 */
 	protected static boolean redirectToCallingPage() {
 		Http.Header referer = request.headers.get("referer");
 		if (referer == null)
 			return false;
 		redirect(referer.value());
 		return true;
 	}
 
 	/**
 	 * Gets an answer to a question or <code>null</code> if it doesn't exist.
 	 * 
 	 * @param questionId
 	 *            the question's id
 	 * @param answerId
 	 *            the answer's id
 	 * @return the answer, if it exists
 	 */
 	protected static Answer getAnswer(int questionId, int answerId) {
 		Question question = Database.questions().get(questionId);
 		if (question == null)
 			return null;
 		return question.getAnswer(answerId);
 	}
 
 	/**
 	 * Gets a comment to a question or an answer, or <code>null</code> if it
 	 * doesn't exist.
 	 * 
 	 * @param questionId
 	 *            the question's id
 	 * @param answerId
 	 *            the answer's id or an invalid ID for getting a question's
 	 *            comment
 	 * @param commentId
 	 *            the comment's id
 	 * @return the answer, if it exists
 	 */
 	protected static Comment getComment(int questionId, int answerId,
 			int commentId) {
 		Question question = Database.questions().get(questionId);
 		if (question == null)
 			return null;
 		Answer answer = question.getAnswer(answerId);
 		if (answer == null)
 			return question.getComment(commentId);
 		return answer.getComment(commentId);
 	}
 
 	/**
 	 * Checks whether a {@link User} can edit a profile.
 	 * 
 	 * @param showUser
 	 *            the {@link User} who is the owner of the profile.
 	 * @return
 	 */
 	protected static boolean userCanEditProfile(User showUser) {
 		User user = Session.user();
 		if (user == null)
 			return false;
 		return user == showUser && !showUser.isBlocked() || user.isModerator();
 	}
 
 	/**
 	 * Marks a given entry as possibly spam (ordinary user) or as definitively
 	 * spam (moderators). In the first case, a notification is sent to all
 	 * moderators in their spam report; otherwise the entry is deleted and the
 	 * entry's owner is blocked as a spammer.
 	 * 
 	 * @param spam
 	 *            the Entry that might be spam
 	 */
 	protected static void markSpam(Entry spam) {
 		User user = Session.user();
 		if (spam != null && user != null) {
 			if (!user.isModerator()) {
 				spam.markSpam(Database.users().getModeratorMailbox());
				flash.success("spam.thx.user");
 			} else {
 				spam.confirmSpam();
				flash.success("spam.thx.mod");
 			}
 		}
 	}
 }
