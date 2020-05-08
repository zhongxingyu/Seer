 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import annotations.Unused;
 
 /**
  * The Class Question delivers all functionality of the questions that other
  * votables don't have (those would be located in the class @see Votable.java.
  */
 public class Question extends Post {
 
 	/** The best answer to this question. */
 	private Answer bestAnswer;
 
 	/** The time when the best answer has been defined. */
 	private Date bestAnswerSetTime;
 
 	/** The tags of this question. */
	private ArrayList<String> tags;
 
 	private Date lastChangedDate;
 
 	/**
 	 * Instantiates a new question.
 	 * 
 	 * @param content
 	 *            - The content of the question.
 	 * @param questionOwner
 	 *            - The user who asked the question.
 	 * @param addQuestionToList
 	 *            - true if the new Question shall be added to the Answer list
 	 *            and false otherwise.
 	 */
 	public Question(Boolean addQuestionToList, String content,
 			User questionOwner) {
 		this.owner = questionOwner;
 		this.content = content;
 		this.score = 0;
 
 		date = new Date();
 		lastChangedDate = new Date();
		tags = new ArrayList<String>();
 		if (addQuestionToList) {
 			oldVersions = new ArrayList<Post>();
 			manager.addQuestion(this);
 			questionOwner.addActivity("Asked question <" + content + ">");
 		}
 	}
 
 	/**
 	 * Instantiates a new question and adds it to the question list.
 	 * 
 	 * @param content
 	 *            - The content of the question
 	 * @param questionOwner
 	 *            - The user who asked the question.
 	 */
 	public Question(String content, User questionOwner) {
 		new Question(true, content, questionOwner);
 	}
 
 	/**
 	 * Set an answer as the best answer to this question.
 	 * 
 	 * @param answer
 	 *            - the answer that is best answering this question.
 	 */
 	public void setBestAnswer(Answer answer) {
 		if (bestAnswerChangeable() && answer.belongsToQuestion(id)) {
 			if (hasBestAnswer()) {
 				bestAnswer.markAsBestAnswer(false);
 			}
 
 			answer.markAsBestAnswer(true);
 			bestAnswerSetTime = new Date();
 			bestAnswer = answer;
 		}
 	}
 
 	/**
 	 * Checks if an answer has been selected as the best answer.
 	 * 
 	 * @return - true if the best answer has been selected of false if no answer
 	 *         is set as best one.
 	 */
 	public boolean hasBestAnswer() {
 		return bestAnswer != null;
 	}
 
 	/**
 	 * Checks if you can still change the choice of the best answer. This is
 	 * possible in a timespan of 30 min after the first choice.
 	 * 
 	 * @return - true if best answer was set less than 30 min ago or no best
 	 *         answer is set yet, false otherwise.
 	 */
 	public boolean bestAnswerChangeable() {
 		long now = new Date().getTime();
 		long then = hasBestAnswer() ? bestAnswerSetTime.getTime() : now;
 		long diff = now - then; // time difference in ms
 
 		return (diff / (1000 * 60)) < 30;
 	}
 
 	/**
 	 * Adds tags to the question separated by spaces and also adds new Tags to
 	 * the tag-List in the manager.
 	 * 
 	 * @param tags
 	 *            - A string containing all tags separated by spaces
 	 */
 	public void addTags(String tags) {
 		String delimiter = "[ ]+";
 		tags = tags.toLowerCase();
 		for (String newTag : tags.split(delimiter)) {
 			if (!this.tags.contains(newTag))
 				this.tags.add(newTag);
 			if (!manager.getTagList().contains(newTag.toLowerCase()))
 				manager.addTag(newTag.toLowerCase());
 		}
 	}
 
 	@Unused
 	public void addTags(ArrayList<String> tags) {
 		this.tags.addAll(tags);
 	}
 	
 	public void setTags(String tags){
 		this.tags=new ArrayList<String>();
 		this.addTags(tags);
 	}
 
 	/**
 	 * Checks whether some tags from the parameter 'tags' are similar to some
 	 * that have already been entered previously using the Levenshtein-distance.
 	 * 
 	 * @param tags
 	 *            - the String of tags - separated by spaces - that are to be
 	 *            checked.
 	 * @return - A string containing all tags that already exist, separated by a
 	 *         space and each starting with a '#'.
 	 */
 	public static String checkTags(String tags) {
 		String delimiter = "[ ]+";
 		// The minimum Levenshtein distance two strings need to have.
 		int minDistance = 2;
 		String storeTags = "" + tags;
 		tags = tags.toLowerCase();
 		String existingTags = new String();
 		for (String newTag : tags.split(delimiter)) {
 			for (String existingTag : manager.getTagList()) {
 				if (models.algorithms.Levenshtein.getLevenshteinDistance(newTag
 						.toLowerCase(), existingTag) <= minDistance
 						&& !manager.getTagList().contains(newTag)) {
 					existingTags = existingTags + "#" + existingTag + " ";
 				}
 			}
 		}
 		tags = storeTags;
 		return existingTags;
 	}
 
 	/*
 	 * Getter methods
 	 */
 	public String getTagByIndex(int i) {
 		return tags.get(i);
 	}
 
 	public Answer getBestAnswer() {
 		return bestAnswer;
 	}
 
 	public ArrayList<String> getTags() {
 		return this.tags;
 	}
 
 	public String getTagsString() {
 		StringBuffer result = new StringBuffer();
 		for (String tag : tags) {
 			result.append(tag + " ");
 		}
 		return result.toString();
 	}
 
 	/**
 	 * Gets all Comments which belongs to this question
 	 * 
 	 * @return - a sorted list of comments
 	 */
 	public ArrayList<Comment> getComments() {
 		return manager.getAllCommentsByQuestionIdSortedByDate(this.getId());
 	}
 
 	/**
 	 * Gets the date of the last change (means adding of answer, comment, vote)
 	 * 
 	 */
 	public Date getLastChangedDate() {
 		return this.lastChangedDate;
 	}
 
 	/**
 	 * Setter methods
 	 * 
 	 * @param date
 	 *            - the date when the answer has been changed.
 	 */
 	public void setLastChangedDate(Date date) {
 		this.lastChangedDate = date;
 	}
 
 	public void setContent(String content, String uname) {
 		// this.oldVersions.add(0, new Question(false, this.content,
 		// this.owner));
 		super.setContent(content, uname);
 		// manager.getUserByName(uname).addActivity(
 		// "Edited Question " + this.id + " by writing: <" + content + ">.");
 	}
 
 	/**
 	 * Edits a question and adds the actual contents and tags to the list of
 	 * older versions.
 	 * 
 	 * @param content
 	 *            - the new content of the question
 	 * @param tags
 	 *            - the new tags
 	 * @param uname
 	 *            - the name of the user who adds the new version.
 	 */
 	public void addVersion(String content, String tags, String uname) {
 		Question question = new Question(false, this.content, this.owner);
		question.addTags(tags);
 		this.oldVersions.add(0, question);
 		this.editedBy.add(manager.getUserByName(uname));
 		super.setContent(content, uname);
 		this.setTags(""+tags);
 		manager.getUserByName(uname).addActivity(
 				"Edited Question " + this.id + " by writing: <" + content
 						+ ">.");
 		this.setLastChanged(getDate());
 	}
 
 	public void restoreOldVersion(String oldContent, String oldTags,
 			String uname) {
 		addVersion(oldContent, oldTags, uname);
 	}
 
 }
