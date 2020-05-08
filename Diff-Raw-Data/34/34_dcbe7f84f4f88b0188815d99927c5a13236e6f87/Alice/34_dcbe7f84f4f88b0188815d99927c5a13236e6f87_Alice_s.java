 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Scanner;
 
 import twitter4j.IDs;
 import twitter4j.Paging;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import ec.util.MersenneTwisterFast;
 
 /**
  * Alice bot.
  * 
  * @author yappy
  */
 public class Alice {
 
	private static final boolean RANDOM_TWEET = false;
	private static final boolean AUTO_FOLLOW = false;
	private static final boolean AUTO_REPLY = false;
 
 	private static final String TWEET_FILE_NAME = "list.txt";
 
 	private static MersenneTwisterFast mt = new MersenneTwisterFast();
 	private static PrintWriter logOut;
 	private static Twitter twitter;
 	private static List<String> dataList;
 
 	private static List<String> loadList() throws IOException {
 		Scanner in = new Scanner(new File(TWEET_FILE_NAME), "UTF-8");
 		List<String> list = new ArrayList<String>();
 		while (in.hasNextLine()) {
 			String line = in.nextLine();
 			if (!line.equals("") && !line.startsWith("#")) {
 				list.add(line);
 			}
 		}
 		logOut.printf("List loaded (%d items)%n", list.size());
 		return list;
 	}
 
 	private static void randomTweet() {
 		try {
			int ind = mt.nextInt(dataList.size());

			String msg = dataList.get(ind);
 			twitter.updateStatus(msg);
 			logOut.println("tweet: " + msg);
 		} catch (TwitterException e) {
 			e.printStackTrace(logOut);
 		}
 	}
 
 	private static void autoFollow() {
 		try {
 			// friend: id who I am following
 			// follower: id who is following me
 			List<Long> friendList = new ArrayList<Long>();
 			List<Long> followerList = new ArrayList<Long>();
 
 			for (long cursor = -1; cursor != 0;) {
 				IDs ids = twitter.getFriendsIDs(cursor);
 				for (long id : ids.getIDs()) {
 					friendList.add(id);
 				}
 				cursor = ids.getNextCursor();
 			}
 			logOut.printf("Get friends: %d%n", friendList.size());
 			for (long cursor = -1; cursor != 0;) {
 				IDs ids = twitter.getFollowersIDs(cursor);
 				for (long id : ids.getIDs()) {
 					followerList.add(id);
 				}
 				cursor = ids.getNextCursor();
 			}
 			logOut.printf("Get followers: %d%n", followerList.size());
 			Collections.sort(friendList);
 			Collections.sort(followerList);
 
 			// follow
 			for (long id : followerList) {
 				if (Collections.binarySearch(friendList, id) < 0) {
 					User user = twitter.createFriendship(id);
 					logOut.printf("follow: %s%n", user.getName());
 				}
 			}
 			// unfollow
 			for (long id : friendList) {
 				if (Collections.binarySearch(followerList, id) < 0) {
 					User user = twitter.destroyFriendship(id);
 					logOut.printf("unfollow: %s%n", user.getName());
 				}
 			}
 		} catch (TwitterException e) {
 			e.printStackTrace(logOut);
 		}
 	}
 
 	/*
 	 * Notice: This method gets the latest tweet of myself. This method must be
 	 * called before tweeting anything.
 	 */
 	private static void autoReply() {
 		try {
 			// last tweet id
			List<Status> lastStatus = twitter.getUserTimeline(new Paging(1, 1));
			long lastId = lastStatus.isEmpty() ? 0 : lastStatus.get(0).getId();
 			// 200 recent mentions since last tweet
 			List<Status> mentions;
 			mentions = twitter.getMentions(new Paging(1, 200, lastId));
 			for (Status ms : mentions) {
 				logOut.printf("Find new mention: %s%n", ms.getText());
 
 				int ind = mt.nextInt(dataList.size());
 				String msg = "@" + ms.getUser().getScreenName() + " "
 						+ dataList.get(ind);
 				StatusUpdate update = new StatusUpdate(msg)
 						.inReplyToStatusId(ms.getId());
 				twitter.updateStatus(update);
 
 				logOut.printf("Reply: %s%n", msg);
 			}
 		} catch (TwitterException e) {
 			e.printStackTrace(logOut);
 		}
 	}
 
 	public static void main(String[] args) {
 		Date nowDate = new Date();
 		String nowStr = String.format("%1$tY%1$tm%1$td", nowDate);
 
 		File logDir = new File("log");
 		logDir.mkdir();
 		String logFileName = nowStr + ".log";
 		try {
 			Writer w = new OutputStreamWriter(new FileOutputStream(new File(
 					logDir, logFileName), true), "UTF-8");
 			logOut = new PrintWriter(w);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		logOut.printf("Start (%1$tF %1$tT)%n", nowDate);
 
 		try {
 			twitter = new TwitterFactory().getInstance();
 			dataList = loadList();
 			if (AUTO_REPLY) {
 				autoReply();
 			}
 			if (RANDOM_TWEET) {
 				randomTweet();
 			}
 			if (AUTO_FOLLOW) {
 				autoFollow();
 			}
 		} catch (Exception e) {
 			e.printStackTrace(logOut);
 		}
 
 		logOut.printf("End (%1$tF %1$tT)%n", System.currentTimeMillis());
 		logOut.println();
 		logOut.close();
 	}
 }
