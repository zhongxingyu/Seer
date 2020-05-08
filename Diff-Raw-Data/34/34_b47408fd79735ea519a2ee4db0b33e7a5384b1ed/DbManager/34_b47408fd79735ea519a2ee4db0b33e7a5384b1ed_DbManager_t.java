 package models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import models.comparators.ChangedDateComparator;
 import models.comparators.DateComparator;
 import models.comparators.ScoreComparator;
 
 /**
  * The Class UserQuestionAnswerManager delivers functionality to coordinate the
  * different entities of the application.
  */
 public class DbManager {
 
 	/** All questions stored. */
 	private static HashMap<Integer, Question> questions;
 
 	/** All answers stored. */
 	private static HashMap<Integer, Answer> answers;
 
 	/** All comments stored. */
 	private static ArrayList<Comment> comments;
 
 	/** All registered users. */
 	private static ArrayList<User> users;
 
 	/** All registered email-adresses */
 	private static ArrayList<String> emailAddresses= new ArrayList<String>();
 
 	/** All tags that have been used so far. */
 	private static ArrayList<String> tags;
 
 	/** All likes have been set by users on different comments. */
 	private static ArrayList<Like> likes;
 	
 	/** All admin warning. */
 	private static ArrayList<Warning> warnings;
 
 	/** 4 Counters for the Id's */
 	private int userCounterIdCounter;
 	private int questionIdCounter;
 	private int answerIdCounter;
 	private int commentIdCounter;
 	private int warningIdCounter;
 
 	private static final DbManager INSTANCE = new DbManager();
 
 	/**
 	 * Delivers the only instance of this class.
 	 * 
 	 * @return - single instance of UserQuestionAnswerManager
 	 */
 	public static DbManager getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Instantiates a new UserQuestionAnswerManager.
 	 */
 	private DbManager() {
 		questions = new HashMap<Integer, Question>();
 
 		answers = new HashMap<Integer, Answer>();
 		comments = new ArrayList<Comment>();
 		users = new ArrayList<User>();
 		tags = new ArrayList<String>();
 		likes = new ArrayList<Like>();
 		warnings = new ArrayList<Warning>();
 		this.userCounterIdCounter = 0;
 		this.questionIdCounter = 0;
 		this.answerIdCounter = 0;
 		this.commentIdCounter = 0;
 	}
 
 	/**
 	 * Checks if a username is already occupied.
 	 * 
 	 * @param name
 	 *            - the username you want to check
 	 * @return - true, if the username is already occupied or false otherwise.
 	 */
 	public boolean checkUserNameIsOccupied(String name) {
 		if (users.size() != 0) {
 			for (int i = 0; i < users.size(); i++) {
 				if (name.equals(users.get(i).getName())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if there's already a question with a certain content.
 	 * 
 	 * @param - content to be checked
 	 * @return - true if there already exists a question with this content and
 	 *         false otherwise.
 	 */
 	public boolean checkQuestionDuplication(String content) {
 		if (!questions.isEmpty()) {
 			Iterator it = questions.values().iterator();
 			while (it.hasNext()) {
 				Question cur = (Question) it.next();
 				if (content.equals(cur.getContent())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Deletes a user and all entries he or she wrote. Posts which the user
 	 * edited are anonymized.
 	 * 
 	 * @param - The username of the user to be deleted as a string object.
 	 */
 	public void deleteUser(String username) {
 		if (!DbManager.users.contains(this.getUserByName(username))) {
 			throw new NoSuchElementException();
 		}
 
 		User deleteUser = getUserByName(username);
 
 		// Delete the user's email-address from the list of registered addresses.
 		emailAddresses.remove(deleteUser.getEmail());
 
 		if (!checkUserNameIsOccupied("anonymous"))
 			new User("anonymous", "a@nonymous", "anonymous");
 
 		ArrayList<Comment> updatedComments = new ArrayList<Comment>();
 		users.remove(deleteUser);
 
 		// Delete all questions a user added
 		Iterator it = questions.values().iterator();
 		Question toDelete = null;
 		while (it.hasNext()) {
 			Question cur = (Question) it.next();
 			if (cur.getOwner().equals(deleteUser)) {
 				toDelete = cur;
 			}
 
 		}
 		if (toDelete != null) {
 		deleteQuestion(toDelete);
 		}
 
 		// Anonymize all questions a user edited
 		Iterator it1 = questions.values().iterator();
 		while (it1.hasNext()) {
 			Question p = (Question) it1.next();
 			if (p.getEditor() != p.getOwner()
 					|| p.getEditor().equals(deleteUser))
 				p.setEditor("anonymous");
 		}
 
 		// Delete all answers a user added
 		Iterator it2 = answers.values().iterator();
 		while (it2.hasNext()) {
 			Answer p = (Answer) it2.next();
 			if (p.getOwner().equals(deleteUser)) {
 				deleteAnswer(p);
 			}
 		}
 
 		// Anonymize all answers a user edited
 		Iterator it3 = answers.values().iterator();
 		while (it3.hasNext()) {
 			Answer p = (Answer) it3.next();
 			if (p.getEditor() != p.getOwner()
 					&& p.getEditor().equals(deleteUser)) {
 				p.setEditor("anonymous");
 			}
 		}
 
 		// Delete all comments a user added
 		for (Comment c : DbManager.comments) {
 			if (!c.getOwner().equals(deleteUser))
 				updatedComments.add(c);
 		}
 		comments.clear();
 		comments.addAll(updatedComments);
 	}
 
 	/**
 	 * Deletes a certain question and all its answers
 	 * 
 	 * @param question
 	 */
 	public void deleteQuestion(Question question) {
 		for (int i = 0; i <= question.getComments().size() - 1; i++) {
 			if (question.getComments().contains(this.comments.get(i))) {
 				this.comments.remove(this.comments.get(i));
 			}
 		}
 
 		Iterator it = answers.values().iterator();
 		while (it.hasNext()) {
 			Answer a = (Answer) it.next();
 			if (question.getAnswers().contains(a)) {
 				for (int i = 0; i <= a.getComments().size() - 1; i++) {
 					this.comments.remove(i);
 				}
 			}
 		}
 
 		for (Answer a : question.getAnswers()) {
 			deleteAnswer(a);
 		}
 		questions.remove(question.getId());
 	}
 
 	/**
 	 * Deletes a certain answer
 	 * 
 	 * @param answer
 	 */
 	public void deleteAnswer(Answer answer) {
 		for (int i = 0; i <= comments.size() - 1; i++) {
 			if (this.getAllCommentsByAnswerIdSortedByDate(answer.getId())
 					.contains(comments.get(i)))
 				this.comments.remove(comments.get(i));
 		}
 
 		answers.remove(answer.getId());
 	}
 
 	/**
 	 * Deletes a certain comment
 	 * 
 	 * @param comment
 	 */
 	public void deleteComment(Comment comment) {
 		comments.remove(comment);
 	}
 
 	/**
 	 * Adds a tag to the list of all tags that have been used.
 	 * 
 	 * @param singleTag
 	 *            - the tag that has to be added.
 	 */
 	public void addTag(String singleTag) {
 		if (!this.tags.contains(singleTag))
 			this.tags.add(singleTag);
 	}
 
 	public boolean isEmailRegistered(String email) {
 		return this.emailAddresses.contains(email);
 	}
 
 	/**
 	 * Gets a user by his name.
 	 * 
 	 * @param name
 	 *            - the name of the user you are looking for. the name
 	 * @return - the user with the username 'name'.
 	 */
 	public User getUserByName(String name) {
 		for (User user : users) {
 			if (user.getName().equals(name))
 				return user;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a user by his id.
 	 * 
 	 * @param uid
 	 *            - the id of the user you are looking for.
 	 * @return - the user with the user id 'uid'.
 	 */
 	public User getUserById(int uid) {
 		for (User user : users) {
 			if (user.getId() == uid)
 				return user;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the all answers to the question with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the question you want to get the answers of.
 	 * @return - all answers to the question with the id 'id'.
 	 */
 	public ArrayList<Answer> getAllAnswersByQuestionId(int id) {
 		ArrayList<Answer> questionAnswers = new ArrayList<Answer>();
 		Iterator it = answers.values().iterator();
 		while (it.hasNext()) {
 			Answer answer = (Answer) it.next();
 			if (answer.getQuestionId() == id) {
 				questionAnswers.add(answer);
 			}
 		}
 		return questionAnswers;
 	}
 
 	/**
 	 * Gets the question with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the question you are looking for.
 	 * @return - the question with the id #id.
 	 */
 	public Question getQuestionById(int id) {
 		return questions.get(id);
 	}
 
 
 	/**
 	 * Gets the answer with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the answer you are looking for.
 	 * @return - the answer with the id #id.
 	 */
 	public Answer getAnswerById(int id) {
 		return answers.get(id);
 	}
 
 	/**
 	 * Get the comment with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the comment you are looking for
 	 * @return - the comment with the id #id
 	 */
 	public Comment getCommentById(int id) {
 		for (Comment c_comment : comments)
 			if (c_comment.getId() == id)
 				return c_comment;
 		return null;
 	}
 
 	/**
 	 * Gets all questions sorted by score.
 	 * 
 	 * @return - the questions sorted by score
 	 */
 	public ArrayList<Question> getQuestionsSortedByScore() {
 		ArrayList<Question> sortedQuestions = new ArrayList<Question>();
 		sortedQuestions.addAll(questions.values());
 
 		Collections.sort(sortedQuestions,
 				Collections.reverseOrder(new ScoreComparator()));
 
 		return sortedQuestions;
 	}
 
 	/**
 	 * Gets the answers to a certain question sorted by their score.
 	 * 
	 * @param userId
 	 *            - the id of the question you are looking for the answers to.
 	 * @return - all answers sorted by their score.
 	 */
	public ArrayList<Answer> getAnswersSortedByScore(int userId) {
		ArrayList<Answer> sortedAnswers = this.getAllAnswersByQuestionId(userId);
 
 		Collections.sort(sortedAnswers, Collections
 				.reverseOrder(new ScoreComparator()));
 
 		return sortedAnswers;
 	}
 
 	/**
 	 * Gets all answers sorted by date.
 	 * 
 	 * @return - all answers sorted by date.
 	 */
 	public ArrayList<Answer> getAnswersSortedByDate() {
 		ArrayList<Answer> sortedAnswers = this.getAnswers();
 
 		Collections.sort(sortedAnswers, new DateComparator());
 
 		return sortedAnswers;
 	}
 
 	/**
	 * Gets all answers sorted by date and then sorted by score.
	 * 
	 * @return - all answers sorted by date and then by score.
	 */
	public ArrayList<Answer> getAnswersSortedByDateByScore(int userId) {
		ArrayList<Answer> sortedAnswers = this
				.getAllAnswersByQuestionId(userId);

		Collections.sort(sortedAnswers,
				Collections.reverseOrder(new DateComparator()));
		Collections.sort(sortedAnswers,
				Collections.reverseOrder(new ScoreComparator()));

		return sortedAnswers;
	}


	/**
 	 * Gets all answers from a specific user sorted by Date
 	 * 
 	 * @param userId
 	 *            - the users id
 	 * 
 	 * @return - sorted Answers
 	 */
 	public ArrayList<Answer> getAnswersByUserIdSortedByDate(int userId) {
 		ArrayList<Answer> usersAnswers = new ArrayList<Answer>();
 		ArrayList<Answer> sortedAnswers = this.getAnswersSortedByDate();
 		for (Answer currentAnswer : sortedAnswers) {
 			if (currentAnswer.ownerIs(userId))
 				usersAnswers.add(currentAnswer);
 		}
 		return usersAnswers;
 	}
 
 	/**
 	 * Gets all questions sorted by date.
 	 * 
 	 * @return - all questions sorted by date.
 	 */
 	public ArrayList<Question> getQuestionsSortedByDate() {
 		ArrayList<Question> sortedQuestions = new ArrayList<Question>();
 		sortedQuestions.addAll(questions.values());
 
 		Collections.sort(sortedQuestions, new DateComparator());
 
 		return sortedQuestions;
 	}
 
 	/**
 	 * Gets all question sorted by the date of their latest change (means adding
 	 * of answers, comments, votes)
 	 */
 	public ArrayList<Question> getQuestionsSortedByLastChangedDate() {
 		ArrayList<Question> sortedQuestions = new ArrayList<Question>();
 		sortedQuestions.addAll(questions.values());
 
 		Collections.sort(sortedQuestions, new ChangedDateComparator());
 
 		return sortedQuestions;
 	}
 
 	/**
 	 * Gets all questions from a specific user sorted by Date
 	 * 
 	 * @param userId
 	 *            - the id of the user, the questions are from
 	 * 
 	 * @return - sorted Questions
 	 */
 	public ArrayList<Question> getQuestionsByUserIdSortedByDate(int userId) {
 		ArrayList<Question> usersQuestions = new ArrayList<Question>();
 		ArrayList<Question> sortedQuestions = this.getQuestionsSortedByDate();
 		for (Question currentQuestion : sortedQuestions) {
 			if (currentQuestion.ownerIs(userId))
 				usersQuestions.add(currentQuestion);
 		}
 		return usersQuestions;
 		}
 
 	/**
 	 * Gets all comments to a certain question sorted by date.
 	 * 
 	 * @param id
 	 *            - the id of the question
 	 * 
 	 * @return - comments sorted by date
 	 */
 	public ArrayList<Comment> getAllCommentsByQuestionIdSortedByDate(
 			int questionId) {
 		ArrayList<Comment> sortedComments = new ArrayList<Comment>();
 		Post currentQuestion = this.getQuestionById(questionId);
 		for (Comment currentComment : this.getComments()) {
 			if (currentComment.getCommentedPost().equals(currentQuestion))
 				sortedComments.add(currentComment);
 		}
 		Collections.sort(sortedComments, new DateComparator());
 		return sortedComments;
 	}
 
 	/**
 	 * Gets all comments to a certain answer sorted by date.
 	 * 
 	 * @param id
 	 *            - the id of the answer
 	 * 
 	 * @return - comments sorted by date
 	 */
 	public ArrayList<Comment> getAllCommentsByAnswerIdSortedByDate(int answerId) {
 		ArrayList<Comment> sortedComments = new ArrayList<Comment>();
 		Answer currentAnswer = this.getAnswerById(answerId);
 		for (Comment currentComment : this.getComments()) {
 			if (currentComment.getCommentedPost().equals(currentAnswer))
 				sortedComments.add(currentComment);
 		}
 		Collections.sort(sortedComments, new DateComparator());
 		return sortedComments;
 	}
 
 	/**
 	 * Set a comment as liked for a given user.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param commentId
 	 *            the comment id
 	 * @return true, if successful
 	 */
 	public boolean likeComment(User user, int commentId) {
 		Comment comment = getCommentById(commentId);
 		if (userCanLikeComment(user, commentId)) {
 			this.likes.add(new Like(user, comment));
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Get the like object of a user for a given commentId from the like list.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param commentId
 	 *            the comment id
 	 * @return the like object
 	 */
 	public Like getLike(User user, int commentId) {
 
 		Comment comment = getCommentById(commentId);
 
 		return getLike(user, comment);
 	}
 
 	/**
 	 * Get the like object of a user for a given comment from the like list.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param comment
 	 *            the comment
 	 * @return the like
 	 */
 	public Like getLike(User user, Comment comment) {
 		for (Like l : likes) {
 			if (l.getUser() == user && l.getComment() == comment)
 				return l;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns if a user is allowed to like a comment.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param commentId
 	 *            the comment id
 	 * @return true, if allowed
 	 */
 	public boolean userCanLikeComment(User user, int commentId) {
 
 		Comment comment = getCommentById(commentId);
 
 		if (getLike(user, comment) != null)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Delete a given like from a user to a comment in the list.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param commentId
 	 *            the comment id
 	 */
 	public void unlikeComment(User user, int commentId) {
 
 		Comment comment = getCommentById(commentId);
 		Like like = getLike(user, comment);
 		this.likes.remove(like);
 	}
 
 	/**
 	 * Gets all likes to a given commentId.
 	 * 
 	 * @param commentId
 	 *            the comment id
 	 * @return the likes
 	 */
 	public List<Like> getLikes(int commentId) {
 
 		List<Like> likeList = new ArrayList<Like>();
 		Comment comment = getCommentById(commentId);
 		for (Like l : likes) {
 			if (l.getComment().equals(comment)) {
 				likeList.add(l);
 			}
 		}
 
 		return likeList;
 	}
 
 	/**
 	 * Number of like for a given commentId.
 	 * 
 	 * @param commentId
 	 *            the comment id
 	 * @return the int
 	 */
 	public int numberOfLike(int commentId) {
 
 		return getLikes(commentId).size();
 	}
 
 	/**
 	 * Gets the user log of a certain user by his/her username.
 	 * 
 	 * @param username
 	 *            - the username of the user you are looking for the log of.
 	 * @return - the user log
 	 */
 	public ArrayList<String> getUserLog(String username) {
 		return this.getUserByName(username).getActivities();
 	}
 
 	/**
 	 * Gets all Object of the type votable a certain user created.
 	 * 
 	 * @param userId
 	 *            - the id of the user
 	 * @return - all votables the user with the id #userId created.
 	 */
 	public ArrayList<Post> getVotablesByUserId(int userId) {
 		ArrayList<Post> usersVotables = new ArrayList<Post>();
 		Iterator it = questions.values().iterator();
 
 		while (it.hasNext()) {
 			Question cur = (Question) it.next();
 			if (cur.getOwner().getId() == userId) {
 				usersVotables.add(cur);
 			}
 		}
 		Iterator it1 = answers.values().iterator();
 		while (it1.hasNext()) {
 			Answer currentAnswer = (Answer) it1.next();
 			if (currentAnswer.getOwner().getId() == userId) {
 				usersVotables.add(currentAnswer);
 			}
 		}
 		return usersVotables;
 	}
 
 	/**
 	 * Gets the #count newest questions in the knowledgeBase.
 	 * 
 	 * @param count
 	 *            - the number of questions.
 	 * @return - the newest questions in the KB. The size of the array equals
 	 *         'count'.
 	 */
 	public ArrayList<Question> getRecentQuestionsByNumber(int count) {
 		// The list of which the newest questions are picked out.
 		ArrayList allQuestions = getQuestionsSortedByLastChangedDate();
 		ArrayList recentQuestions = new ArrayList<String>();
 		int size = allQuestions.size();
 
 		// Pick last '#count' questions out of the list sorted by date.
 		for (int i = size - 1; i >= size - count && i >= 0; i--) {
 			recentQuestions.add((Post) allQuestions.get(i));
 		}
 		return recentQuestions;
 	}
 
 	/**
 	 * Ads a new created user to user ArrayList, sets the id in the user and
 	 * increments the userIdCounter.
 	 * 
 	 * @param user
 	 */
 	public void addUser(User user) {
 		user.setId(userCounterIdCounter);
 		users.add(user);
 		emailAddresses.add(user.getEmail());
 		userCounterIdCounter++;
 	}
 
 	/**
 	 * Ads a new created question to question ArrayList, sets the id in the
 	 * question and increments the questionIdCounter.
 	 * 
 	 * @param question
 	 */
 	public void addQuestion(Question question) {
 		question.setId(questionIdCounter);
 		questions.put(question.getId(), question);
 		questionIdCounter++;
 	}
 
 	public void addQuestion(Question question, int id) {
 		question.setId(id);
 		questions.put(id, question);
 	}
 
 	/**
 	 * Ads a new created answer to answer ArrayList, sets the id in the answer
 	 * and increments the answerIdCounter.
 	 * 
 	 * @param answer
 	 */
 	public void addAnswer(Answer answer) {
 		answer.setId(answerIdCounter);
 		answers.put(answer.getId(), answer);
 		answerIdCounter++;
 		this.getQuestionById(answer.getQuestionId()).notifyChange();
 	}
 
 	/**
 	 * Ads a new created comment to comment ArrayList, sets the id in the
 	 * comment and increments the commentIdCounter.
 	 * 
 	 * @param comment
 	 */
 	public void addComment(Comment comment) {
 		comment.setId(commentIdCounter);
 		comments.add(comment);
 		commentIdCounter++;
 	}
 
 	/**
 	 * Adds a eMail-address to the list of registered eMail-addresses.
 	 * 
 	 * @param email
 	 *            - the email-address to add
 	 */
 	public void addEMail(String email) {
 		this.emailAddresses.add(email);
 	}
 
 	public void addWarning(Warning newWarning) {
 		boolean isContained = false;
 		for (Warning currentWarning : warnings) {
 			if (currentWarning.getInappropriatePost().equals(newWarning.getInappropriatePost())) {
 				currentWarning.incrementCounter();
 				isContained = true;
 			}
 		}
 		if (!isContained){
 			warnings.add(newWarning);
 			newWarning.setId(warningIdCounter);
 			warningIdCounter++;
 		}
 	}
 	
 	public ArrayList<Warning> getWarnings(){
 		Collections.sort(warnings);
 		return warnings;
 	}
 	
 	public Warning getWarningById(int wid) {
 		for (Warning currentWarning : warnings) {
 			if (currentWarning.getId() == wid)
 				return currentWarning;
 		}
 		return null;
 	}
 	
 	public void deleteWarning(Warning deletedWarning) {
 		warnings.remove(deletedWarning);
 	}
 	
 	/**
 	 * Resets all counters to 0
 	 */
 	public void resetAllIdCounts() {
 		this.userCounterIdCounter = 0;
 		this.questionIdCounter = 0;
 		this.answerIdCounter = 0;
 		this.commentIdCounter = 0;
 	}
 
 	/**
 	 * Saves a reputation from a specific user and day
 	 */
 	public void addReputation(User user, int reputation, String votingUser) {
 		user.addReputation(votingUser, reputation);
 	}
 
 	/**
 	 * Gets the reputation from a user to a specific date.
 	 * 
 	 * @param user
 	 *            - the specific user.
 	 * @param date
 	 *            - the specific date.
 	 * 
 	 * @return - the reputation as an integer.
 	 */
 	@SuppressWarnings("deprecation")
 	public int getReputationByUserAndDate(User user, Date date) {
 		ArrayList<Integer> allReputations = user.getReputations();
 		int result = 0;
 		int counter = 1;
 		Date currentDate = new Date();
 		while ((counter <= allReputations.size())
 				&& ((date.getYear() != currentDate.getYear())
 						|| (date.getMonth() != currentDate.getMonth()) || (date
 						.getDate() != currentDate.getDate()))) {
 			currentDate = new Date(currentDate.getTime() - 86400000);
 			counter++;
 		}
 		if (counter <= allReputations.size())
 			result = allReputations.get(counter - 1);
 		return result;
 	}
 
 	/**
 	 * Gets all reputations from a user in a specific time range.
 	 * 
 	 * @param user
 	 *            - the specific user.
 	 * @param days
 	 *            - the number of day the reputations are from.
 	 * 
 	 * @return - a list of reputations as integers sorted by date
 	 */
 	public ArrayList<Integer> getReputations(User user, int days) {
 		ArrayList<Integer> reputations = user.getReputations();
 		while (reputations.size() < days) {
 			reputations.add(0);
 		}
 		while (reputations.size() > days) {
 			reputations.remove(reputations.size() - 1);
 		}
 		return reputations;
 	}
 
 	/**
 	 * Updates the reputations of an user
 	 * 
 	 * @param reputatedUser
 	 */
 
 	public void updateReputation(User reputatedUser) {
 		reputatedUser.updateReputation();
 	}
 
 	/** Getters */
 	public ArrayList<User> getUsers() {
 		return users;
 	}
 
 	public ArrayList<Question> getQuestions() {
 		ArrayList<Question> returnedQuestions = new ArrayList<Question>();
 		returnedQuestions.addAll(questions.values());
 		return returnedQuestions;
 	}
 
 	public ArrayList<Answer> getAnswers() {
 		ArrayList<Answer> returnedAnswers = new ArrayList<Answer>();
 		returnedAnswers.addAll(answers.values());
 		return returnedAnswers;
 	}
 
 	public ArrayList<Comment> getComments() {
 		return comments;
 	}
 
 	public static ArrayList<String> getTagList() {
 		return tags;
 	}
 
 	private ArrayList<Like> getLikeList() {
 		return likes;
 	}
 
 	/** Count Methods */
 	public int countOfUsers() {
 		return this.getUsers().size();
 	}
 
 	public int countOfQuestions() {
 		return this.getQuestions().size();
 	}
 
 	public int countOfAnswers() {
 		return this.getAnswers().size();
 	}
 
 	public int countOfComments() {
 		return this.getComments().size();
 	}
 
 	public int countOfTags() {
 		return this.getTagList().size();
 	}
 
 	public int countOfLikes() {
 		return this.getLikeList().size();
 	}
 
 	/** Clear Methods for Testing */
 	public void clearQuestionsMap() {
 		questions.clear();
 	}
 
 	public void clearAnswerMap() {
 		answers.clear();
 	}
 	
 	public void clearWarnings() {
 		warnings.clear();
 	}
 
 }
