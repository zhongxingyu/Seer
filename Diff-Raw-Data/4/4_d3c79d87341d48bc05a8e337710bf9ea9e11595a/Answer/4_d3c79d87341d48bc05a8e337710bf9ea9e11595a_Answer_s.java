 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * The Class Answer delivers all functionality of the answers that other
  * votables don't have as well (those would be located in the class
  * Votable.java).
  */
 public class Answer extends Post {
 
 	/** All users having voted for answer. */
 	private ArrayList<User> userVotedForAnswer = new ArrayList<User>();
 
 	/** The ID of the question the answer belongs to. */
 	private int questionId;
 
 	/** The is best answer. */
 	private boolean isBestAnswer;
 
 	/**
 	 * Instantiates a new answer.
 	 * 
 	 * @param content
 	 *            - the content of the answer.
 	 * @param user
 	 *            - the user that is answering.
 	 * @param question
 	 *            - the question the answer belongs to.
 	 */
 	public Answer(String content, User user, Question question) {
 		this.questionId = question.getId();
 		this.content = content;
 		this.owner = user;
 		isBestAnswer = false;
 		date = new Date();
 		oldVersions= new ArrayList<Post>();
 		question.setLastChangedDate(new Date());
 		user.addActivity("Answered question <" + question.getContent()
 				+ "> by writing: <" + content + ">");
 		manager.addAnswer(this);
 	}
 
 	/**
 	 * Check if the @user already voted for the answer.
 	 * 
 	 * @param user
 	 *            -The user you want to check if he already voted.
 	 * @return - true if the user already voted and false if the user hasn't
 	 *         voted so far.
 	 */
 	public boolean checkUserVotedForAnswer(User user) {
 		for (int i = 0; i < userVotedForAnswer.size(); i++) {
 			if (user.getName().equals(userVotedForAnswer.get(i).getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds a user to the list of users that have voted for the answer.
 	 * 
 	 * @param user
 	 *            - The user that has voted.
 	 */
 	public void userVotedForAnswer(User user) {
 		userVotedForAnswer.add(user);
 	}
 
 	/**
 	 * Gets the ID of the question the answer belongs to.
 	 * 
 	 * @return - the ID of the question the answer belongs to.
 	 */
 	public int getQuestionId() {
 		return questionId;
 	}
 
 	/**
 	 * Checks if this is the best Answer to the question. The best answer is
 	 * selected by the user that asked the question.
 	 * 
 	 * @return - true if this answer is the best one and false if this is not
 	 *         the case.
 	 */
 	public boolean isBestAnswer() {
 		return isBestAnswer;
 	}
 
 	/**
 	 * Set the answer being the best answer.
 	 * 
 	 * @param status
 	 *            - true if the answer shall be the best or false if this isn't
 	 *            the case.
 	 */
 	public void markAsBestAnswer(boolean status) {
 		isBestAnswer = status;
 		score += isBestAnswer ? 5 : -5;
 	}
 
 	/**
 	 * Checks if the answer belongs to the question with a certain ID.
 	 * 
 	 * @param qid
 	 *            - The id of the question you want to check the belonging.
 	 * @return - true if the answer belongs to the question with the id qid or
 	 *         false if not.
 	 */
 	public boolean belongsToQuestion(int qid) {
 		return qid == questionId;
 	}
 
 	/**
 	 * Gets all Comments which belongs to this answer
 	 * 
 	 * @return - a sorted list of comments
 	 */
 	public ArrayList<Comment> getComments() {
 		return manager
 				.getAllCommentsByAnswerIdSortedByDate(this.getId());
 	}
 }
