 package models;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * An {@link Item} which has a content and can be voted up and down.
  * 
  * @author Simon Marti
  * @author Mirco Kocher
  */
 public abstract class Entry extends Item implements Comparable<Entry> {
 
 	private final String content;
 	private HashMap<String, Vote> votes;
 
 	/**
 	 * Create an <code>Entry</code>.
 	 * 
 	 * @param owner
 	 *            the {@link User} who owns the <code>Entry</code>
 	 * @param content
 	 *            the content of the <code>Entry</code>
 	 */
 	public Entry(User owner, String content) {
 		super(owner);
 		this.content = content;
 		votes = new HashMap<String, Vote>();
 	}
 
 	/**
 	 * Unregisters a deleted {@link Comment} to its {@link Entry}.
 	 * 
 	 * @param comment
 	 *            the <code> Comment </code> to be unregistered
 	 */
 	public abstract void unregister(Comment comment);
 
 	/**
 	 * Unregisters the <code>Entry</code> if it gets deleted.
 	 */
 	@Override
 	public void unregister() {
 		unregisterVotes();
 		unregisterUser();
 	}
 
 	/**
 	 * Delete all {@link Vote}s if the <code>Entry</code> gets deleted.
 	 */
 	protected void unregisterVotes() {
 		Collection<Vote> votes = this.votes.values();
 		this.votes = new HashMap();
 		for (Vote vote : votes) {
 			vote.unregister();
 		}
 	}
 
 	/**
 	 * Unregisters a deleted {@link Vote}.
 	 * 
 	 * @param vote
 	 *            the {@link Vote} to unregister
 	 */
 	public void unregister(Vote vote) {
 		votes.remove(vote.owner().getName());
 	}
 
 	/**
 	 * 
 	 * Get the content of an <code>Entry</code>.
 	 * 
 	 * @return the content of the <code>Entry</code>
 	 */
 	public String content() {
 		return content;
 	}
 
 	/**
 	 * Count all positive {@link Vote}s on an <code>Entry</code>.
 	 * 
 	 * @return number of positive {@link Vote}s
 	 */
 	public int upVotes() {
 		return countVotes(true);
 	}
 
 	/**
 	 * Count all negative {@link Vote}s on an <code>Entry</code>.
 	 * 
 	 * @return number of negative {@link Vote}s
 	 */
 	public int downVotes() {
 		return countVotes(false);
 	}
 
 	/**
 	 * Get the current rating of the <code>Entry</code>.
 	 * 
 	 * @return rating as an <code>Integer</code>
 	 */
 	public int rating() {
 		return upVotes() - downVotes();
 	}
 
 	/**
 	 * Compares this <code>Entry</code> with another one with respect to their
 	 * ratings (or their age, if they've got identical ratings).
 	 * 
 	 * @return comparison result (-1 = this Entry has more upVotes)
 	 */
 	public int compareTo(Entry e) {
 		int diff = e.rating() - this.rating();
 		if (diff == 0)
 			// compare by ID instead of - potentially identical - timestamp
 			// for a guaranteed stable sorting (makes testing easier)
 			return this.getID() - e.getID();
 		return diff;
 	}
 
 	/**
 	 * Counts the number of <code>Votes</code> of an <code>Entry</code>.
 	 * 
 	 * @param up
 	 *            boolean whether there is a <code>Vote</code> to this
 	 *            <code>Entry</code> or not
 	 * @return counter number of <code>Votes</code>
 	 */
 	private int countVotes(boolean up) {
 		int counter = 0;
 		for (Vote vote : votes.values())
 			if (vote.up() == up) {
 				counter++;
 			}
 		return counter;
 	}
 
 	/**
 	 * Vote an <code>Entry</code> up.
 	 * 
 	 * @param user
 	 *            the {@link User} who voted
 	 * @return the {@link Vote}
 	 */
 	public Vote voteUp(User user) {
 		return vote(user, true);
 	}
 
 	/**
 	 * Vote an <code>Entry</code> down.
 	 * 
 	 * @param user
 	 *            the {@link User} who voted
 	 * @return the {@link Vote}
 	 */
 	public Vote voteDown(User user) {
 		return vote(user, false);
 	}
 
 	/**
 	 * Let an <code>User</code> vote for an <code>Entry</code>.
 	 * 
 	 * @param user
 	 *            who is voting
 	 * @return vote of the <code>User</code>
 	 */
 	private Vote vote(User user, boolean up) {
 		if (user == owner())
 			return null;
 		if (votes.containsKey(user.getName())) {
 			votes.get(user.getName()).unregister();
 		}
 		Vote vote = new Vote(user, this, up);
 		votes.put(user.getName(), vote);
 		return vote;
 	}
 
 	/**
 	 * Turns this Entry into an anonymous (user-less) one.
 	 */
 	public void anonymize() {
 		unregisterUser();
 	}
 
 	/**
 	 * Produces a one-line summary of an Entry: the first 35 to 45 characters,
 	 * if possible cut off at a word boundary, and an ellipsis, if the content
 	 * is longer.
 	 * 
 	 * @return a one-line summary of an <code>Entry</code>.
 	 * */
 	public String summary() {
 		return content.replaceAll("\\s+", " ").replaceFirst(
 				"^(.{35}\\S{0,9} ?).{5,}", "$1...");
 	}
 
 	/**
 	 * Get all <code>Votes</code>.
 	 * 
 	 * @return votes
 	 */
 	public Collection<Vote> getVotes() {
 		return votes.values();
 	}
 
 	public String toString() {
		return "Entry(" + summary() + ")";
 	}
 
 }
