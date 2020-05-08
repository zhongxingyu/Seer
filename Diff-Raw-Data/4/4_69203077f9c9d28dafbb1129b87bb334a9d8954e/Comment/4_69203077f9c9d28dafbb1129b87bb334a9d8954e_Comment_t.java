 package org.smartsnip.core;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.smartsnip.persistence.IPersistence;
 import org.smartsnip.shared.Pair;
 import org.smartsnip.shared.XComment;
 
 public class Comment {
 	/** The owner (user) of the message */
 	public final String owner;
 	/** The snippet id where the comment belongs is fixed */
 	public final Long snippet;
 	/** Comment message. */
 	private String message = "";
 
 	/** Hash id of the comment */
 	private long id;
 
 	/** Last change time */
 	private Date time = null;
 
 	/** Positive votes are stored as chocolates */
 	private int chocolates = 0;
 	/** Negative votes are stored as lemons */
 	private int lemons = 0;
 
 	/**
 	 * Creates a new comment. If one of the arguments if null a new
 	 * {@link NullPointerException} will be thrown, and if the message is empty
 	 * a new {@link IllegalArgumentException} will be thrown
 	 * 
 	 * @param owner
 	 *            of the comment
 	 * @param snippet
 	 *            of the comment
 	 * @param message
 	 *            of the comment
 	 */
 	Comment(String owner, long snippetid, String message, long id, Date time,
 			int posVotes, int negVotes) {
 		if (owner == null || message == null)
 			throw new NullPointerException();
 		if (owner.isEmpty())
 			throw new IllegalArgumentException(
 					"Snippet owner cannot be empty username");
 		if (message.length() == 0)
 			throw new IllegalArgumentException(
 					"Cannot create empty comment box");
 		if (time == null)
 			throw new IllegalArgumentException(
 					"Cannot create comment with not creation time");
 
 		this.owner = owner;
 		this.snippet = snippetid;
 		this.message = message;
 		this.time = time;
 		this.id = id;
 
 		this.chocolates = posVotes;
 		this.lemons = negVotes;
 	}
 
 	/**
 	 * Creates a new comment. If one of the arguments if null a new
 	 * {@link NullPointerException} will be thrown, and if the message is empty
 	 * a new {@link IllegalArgumentException} will be thrown
 	 * 
 	 * @param owner
 	 *            of the comment
 	 * @param snippet
 	 *            of the comment
 	 * @param message
 	 *            of the comment
 	 */
 	private Comment(String owner, long snippet_id, String message) {
 		if (owner == null || message == null)
 			throw new NullPointerException();
 		if (owner.isEmpty())
 			throw new IllegalArgumentException(
 					"Cannot create comment with empty username as owner");
 		if (message.length() == 0)
 			throw new IllegalArgumentException(
 					"Cannot create empty comment box");
 
 		this.owner = owner;
 		this.snippet = snippet_id;
 		this.message = message;
 		setCurrentSystemTime();
 
 		chocolates = 0;
 		lemons = 0;
 	}
 
 	/**
 	 * Creates a comment and adds this comment to the snippet
 	 * 
 	 * If one of the arguments if null a new {@link NullPointerException} will
 	 * be thrown, and if the message is empty a new
 	 * {@link IllegalArgumentException} will be thrown
 	 * 
 	 * @param owner
 	 *            of the comment
 	 * @param snippet
 	 *            of the comment
 	 * @param message
 	 *            of the comment
 	 * @return the newly created comment if success
 	 */
 	public static Comment createComment(String owner, long snippet_id,
 			String message) throws IOException {
 		Comment comment = new Comment(owner, snippet_id, message);
 		Snippet snippet = Snippet.getSnippet(snippet_id);
 		if (snippet == null)
 			throw new IllegalArgumentException(
 					"Invalid snippet id: No such snippet found");
		
		// add the comment to the according snippet and persist it
 		snippet.addComment(comment);
 		comment.setCurrentSystemTime();
 
 		snippet.refreshDB();
 
 		return comment;
 	}
 
 	/**
 	 * The given user wants to rate the comment positive. If the user has
 	 * already voted, this call will be ignored
 	 * 
 	 * @param user
 	 *            that wants to vote
 	 */
 	public synchronized void votePositive(User user) {
 		if (user == null)
 			return;
 		try {
 			Persistence.instance.votePositive(user, this,
 					IPersistence.DB_DEFAULT);
 			updateVotes();
 		} catch (IOException e) {
 			System.err.println("IOException during votePositive("
 					+ user.getUsername() + ") " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * The given user wants to rate the comment negative. If the user has
 	 * already voted, this call will be ignored
 	 * 
 	 * @param user
 	 *            that wants to vote
 	 */
 	public synchronized void voteNegative(User user) {
 		if (user == null)
 			return;
 		try {
 			Persistence.instance.voteNegative(user, this,
 					IPersistence.DB_DEFAULT);
 			updateVotes();
 		} catch (IOException e) {
 			System.err.println("IOException during voteNegative("
 					+ user.getUsername() + ") " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 
 	}
 
 	/**
 	 * Unvotes the vote of the given user. If the user has no vote given,
 	 * nothing happens.
 	 * 
 	 * @param user
 	 *            that wants to unvote
 	 */
 	public synchronized void unvote(User user) {
 		if (user == null)
 			return;
 		try {
 			Persistence.instance.unVote(user, this, IPersistence.DB_DEFAULT);
 			updateVotes();
 		} catch (IOException e) {
 			System.err.println("IOException during unvote("
 					+ user.getUsername() + ") " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * @return the negative votes of the comment
 	 */
 	public synchronized int getNegativeVotes() {
 		updateVotes();
 		return lemons;
 	}
 
 	/**
 	 * @return the positive votes of the comment
 	 */
 	public synchronized int getPositiveVotes() {
 		updateVotes();
 		return chocolates;
 	}
 
 	/**
 	 * @return the total votes of the comment
 	 */
 	public synchronized int getTotalVotes() {
 		updateVotes();
 		return lemons + chocolates;
 	}
 
 	/**
 	 * @return the comments message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @return the last time the comment was edited
 	 */
 	public Date getTime() {
 		return time;
 	}
 
 	/**
 	 * Sets the time to the current system time
 	 */
 	private void setCurrentSystemTime() {
 		Calendar cal = Calendar.getInstance();
 		this.time = cal.getTime();
 	}
 
 	@Override
 	public String toString() {
 		return message;
 	}
 
 	/**
 	 * Deletes this comment out of the database
 	 */
 	public void delete() {
 		Snippet snippet = Snippet.getSnippet(this.snippet);
 		if (snippet == null)
 			return; // if owner does not exists, this is in the nirwana - do
 					// nothing
 
 		snippet.removeComment(this);
 
 		try {
 			Persistence.instance.removeComment(this, IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			System.err.println("IOException during delete of comment (id="
 					+ getHashID() + "): " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Edits the current message to a new message. If the given message is null
 	 * or empty, the method returns without effect.
 	 * 
 	 * @param newMessage
 	 */
 	public void edit(String newMessage) {
 		if (newMessage == null || newMessage.isEmpty())
 			return;
 
 		// TODO: Message change history
 
 		this.message = newMessage;
 		setCurrentSystemTime();
 	}
 
 	/**
 	 * The hash code of the comment object
 	 */
 	@Override
 	public int hashCode() {
 		return Long.valueOf(id).hashCode();
 	}
 
 	/**
 	 * @return The internal unique hash code of the comment
 	 */
 	public long getHashID() {
 		return id;
 	}
 
 	/**
 	 * @return The internal unique hash code of the comment
 	 * @deprecated Use getHashID due to name conventions
 	 */
 	@Deprecated
 	public long getHash() {
 		return id;
 	}
 
 	/**
 	 * Updates the votes
 	 */
 	private void updateVotes() {
 		Pair<Integer, Integer> votes;
 		try {
 			votes = Persistence.instance.getVotes(this);
 			chocolates = votes.first;
 			lemons = votes.second;
 		} catch (IOException e) {
 			System.err.println("IOException during getVotes() "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Comment))
 			return false;
 
 		Comment comment = (Comment) obj;
 		if (!comment.message.equals(this.message))
 			return false;
 		if (!comment.owner.equals(this.owner))
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * writes a given comment out to the persistence layer. The database is
 	 * told, not to overwrite an existing object.
 	 * 
 	 * If the comment is null, nothing happens
 	 * 
 	 * @param comment
 	 *            to be written
 	 * @throws IOException
 	 *             if occuring in the persistence
 	 */
 	synchronized static void addToDB(Comment comment) throws IOException {
 		if (comment == null)
 			return;
 
 		comment.id = Persistence.instance.writeComment(comment,
 				IPersistence.DB_NEW_ONLY);
 	}
 
 	/**
 	 * @return created serialisable comment object
 	 */
 	public XComment toXComment() {
 		return new XComment(owner, getHashID(), snippet, this.message,
 				this.chocolates, this.lemons, this.time);
 	}
 
 	/**
 	 * Gets a comment object identified by the unique id
 	 * 
 	 * @param commentID
 	 *            id of the comment object to get
 	 * @return the found comment object or null, if not found
 	 */
 	public static Comment getComment(long commentID) {
 		try {
 			return Persistence.getInstance().getComment(commentID);
 		} catch (IOException e) {
 			System.err.println("IOException during getComment(" + commentID
 					+ "): " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 
 	/**
 	 * @return the owner of the comment (User).
 	 */
 	public User getOwner() {
 		User user = User.getUser(owner);
 		return user;
 	}
 
 	/**
 	 * @return the owner of the comment (User).
 	 */
 	public String getOwnerName() {
 		return this.owner;
 	}
 
 	/**
 	 * @return the owner snippet of this comment
 	 */
 	public Snippet getSnippet() {
 		Snippet snippet = Snippet.getSnippet(this.snippet);
 		return snippet;
 	}
 
 	/**
 	 * @return the snippet id
 	 */
 	public Long getSnippetId() {
 		return this.snippet;
 	}
 
 	/**
 	 * Manually sets the id of this comment. This call does not writes out into
 	 * the db.
 	 * 
 	 * DO NOT USE THIS METHOD UNLESS YOU KNOW WHAT YOUR ARE DOING
 	 * 
 	 * @param key
 	 *            to be set
 	 */
 	public void setID(long key) {
 		this.id = key;
 	}
 }
