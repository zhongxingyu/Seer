 package de.geotweeter;
 
 import java.security.AccessControlException;
 import java.util.List;
 
 import android.os.AsyncTask;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.apiconn.TwitterApiAccess;
 import de.geotweeter.exceptions.TweetAccessException;
 import de.geotweeter.timelineelements.DirectMessage;
 import de.geotweeter.timelineelements.ErrorMessageDisguisedAsTweet;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 
 /**
  * Retrieves a conversation based on a given endpoint
  * 
  * @author Lutz Krumme (@el_emka)
  *
  */
 public class Conversation {
 
 	private TimelineElementAdapter tea;
 	private TwitterApiAccess api;
 	private boolean backwards;
 	private MessageHashMap dm_conversations;
 	
 	/**
 	 * Creates the conversation object and starts the retrieval task
 	 * 
 	 * @param tea The timeline containing the conversation endpoint as the only element
 	 * @param current_account The timeline owning account
 	 * @param backwards If true the conversation is shown beginning at its end point
 	 * @param onStack If true the timeline is put on the shown timeline stack
 	 */
 	public Conversation(TimelineElementAdapter tea, Account current_account, boolean backwards, boolean onStack) {
 		this.tea = tea;
 		this.backwards = backwards;
 		api = current_account.getApi();
 		dm_conversations = current_account.getDMConversations();
 		if (onStack) {
 			current_account.pushTimeline(tea);
 		}
 		if (!tea.isEmpty()) {
 			new LoadConversationTask().execute(tea.getItem(0));
 		}
 	}
 	
 	/**
 	 * Loads the actual conversation
 	 */
 	private class LoadConversationTask extends AsyncTask<TimelineElement, TimelineElement, Void> {
 
 		@Override
 		/**
 		 * Builds the conversation from memory and in case of tweets it fetches
 		 * missing ones from the API
 		 * 
 		 * @param params The conversation endpoint as first element. Other content will be ignored
 		 */
 		protected Void doInBackground(TimelineElement... params) {
 			if (params == null) {
 				throw new NullPointerException("Conversation Task parameters are null");
 			}
 			if (params[0] == null) {
 				throw new NullPointerException("Conversation Task parameter is null");
 			}
 			TimelineElement current_element = params[0];
 			if (current_element.getClass() != Tweet.class) {
 				List<DirectMessage> messages = dm_conversations.getConversation(getRespondent(current_element));
 				if (messages != null) {
 					for (DirectMessage msg : messages) {
 						publishProgress(msg);
 					}
 				}
 				return null;
 			}
 			Tweet current = (Tweet) current_element;
 			while (current.in_reply_to_status_id != 0) {
 				long predecessor_id = current.in_reply_to_status_id;
 				try {
 					current = (Tweet) TimelineActivity.availableTweets.get(predecessor_id);
 				} catch (NullPointerException e) {
 					current = null;
 				}
 				if (current == null) {
 					try {
 						current = api.getTweet(predecessor_id);
 					} catch (TweetAccessException e) {
 						publishProgress(new ErrorMessageDisguisedAsTweet(R.string.error_tweet_protected));
 						break;
 					} catch (Exception e) {
 						e.printStackTrace();
 						publishProgress(new ErrorMessageDisguisedAsTweet(R.string.error_tweet_loading_failed));
 						break;
 					}
 				}				
 				publishProgress(current);
 			}
 			return null;
 		}
 
 		/**
 		 * Gets the respondent of a direct message conversation
 		 * 
 		 * @param current_element Element of the conversation
 		 * @return Twitter id of the respondent
 		 */
 		private long getRespondent(TimelineElement current_element) {
 			assert (current_element.getClass() == DirectMessage.class);
 			DirectMessage current_msg = (DirectMessage) current_element;
 			if (current_msg.sender.id == dm_conversations.getOwnerId()) {
 				return current_msg.recipient.id;
 			} else if (current_msg.recipient.id == dm_conversations.getOwnerId()) {
 				return current_msg.sender.id;
 			} else {
 				/* Shouldn't actually happen */ 
 				throw new AccessControlException("Message does not belong to given user's timeline");
 			}
 		}
 
 		/**
 		 * Pushes the timeline element to the conversation timeline
 		 */
 		protected void onProgressUpdate(TimelineElement... params) {
 			if (params[0] == null) {
 				return;
 			}
 			if (tea.getItem(0).getID() == params[0].getID()) {
 				return;
 			}
 			if (backwards) {
 				tea.add(params[0]);
 			} else {
 				tea.addAsFirst(params[0]);
 			}
 		}
 		
 	}
 	
 }
