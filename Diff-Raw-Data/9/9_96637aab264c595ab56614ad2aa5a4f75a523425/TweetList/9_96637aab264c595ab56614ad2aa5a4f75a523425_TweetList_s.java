 package org.twitterNotifier.ui;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import twitter4j.Status;
 
 /**
  * Contains the list of tweets to show
  * @author pyppo
  *
  */
 public class TweetList extends JPanel {
 
	private List<Status> shownStatus = new LinkedList<>(); 
 
 	JTextArea area = new JTextArea();
 	
 	public TweetList(){
 		add(area);
 	}
 	
 	/**
 	 * Adds some tweets to the list.
 	 * @param tweets
 	 */
 	public void addTweets(List<Status> tweets){
 		if (tweets != null && tweets.size() > 0){
 			ListIterator<Status> it = tweets.listIterator(tweets.size());
 			while (it.hasPrevious()){
 				Status tweet = it.previous();
 				shownStatus.add(0, tweet);
 			}
 		}
 		//normalize the list to 20
 		if (shownStatus.size() > 20){
 			Iterator<Status> it2 = shownStatus.listIterator(20);
 			while (it2.hasNext()){
 				it2.next();
 				it2.remove();
 			}
 		}
 		printOutput();
 	}
 	
 	private void printOutput(){
 		StringBuilder b = new StringBuilder();
 		for (Status tweet : shownStatus){
 			b.append(tweet.getCreatedAt().toString());
 			b.append(" - ");
 			b.append(tweet.getUser().getName());
 			b.append(" - ");
 			b.append(tweet.getText());
 			b.append('\n');
 		}
 		area.setText(b.toString());
 		
 	}
 }
