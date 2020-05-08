 package microcontroller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import microcontroller.interfaces.Consumer;
 import microcontroller.interfaces.Database;
 import microcontroller.space.Door;
 import osn.Tweet;
 
 public class DefaultConsumer implements Consumer {
 	
 	protected Map<Pattern, Integer> methodMap;
 	protected BlockingQueue<String> messagesToPublish;
 	protected Database db;
 	protected List<String> nearbyUsers;
 	protected Semaphore nearbyUsersSemaphore;
 	protected Door door;
 
 	public DefaultConsumer(BlockingQueue<String> messagesToPublish,
 			Database db,
 			List<String> nearbyUsers,
 			Semaphore nearbyUsersSemaphore,
 			Door door,
 			String secret) {
 		
 		this.methodMap = new HashMap<Pattern, Integer>();
 		this.messagesToPublish = messagesToPublish;
 		this.db = db;
 		this.nearbyUsers = nearbyUsers;
 		this.nearbyUsersSemaphore = nearbyUsersSemaphore;
 		this.door = door;
 		
 		if(secret == null)
 			secret = "@LokiUMD";
 		
 		// "Who is in?" pattern
 		String regex = String.format("%s,?\\s+[Ww]ho\\s+is\\s+in\\s*\\??\\s*", secret);
 		this.methodMap.put(Pattern.compile(regex), 0);
 
 		// "Open door" pattern
 		regex = String.format("%s,?\\s+[Oo]pen(?:\\s+the)?\\s+door\\s*\\.?\\s*", secret);
 		this.methodMap.put(Pattern.compile(regex), 1); 
 	
 		// "Is @x in?" pattern
 		regex = String.format("%s,?\\s+[Ii]s\\s+@([\\p{Alnum}_]+)\\s+in\\s*\\??\\s*", secret);
 		this.methodMap.put(Pattern.compile(regex), 2); 
 		
 		// "Is door open?" pattern
 		regex = String.format("%s,?\\s+[Ii]s(?:\\s+the)?\\s+door\\s+open\\s*\\??\\s*", secret);
 		this.methodMap.put(Pattern.compile(regex), 3); 
 	}
 	
 	protected void respondToWhoIsIn(Matcher m, Tweet msg) {
 		
 		while (true) {
 			try {
 				this.nearbyUsersSemaphore.acquire();
 				StringBuffer usernames = new StringBuffer(200);
 				usernames.append(String.format("@%s, ", msg.getUser().getScreenName()));
 				int counter = 0;
 				for(String deviceId: this.nearbyUsers) {
 					String username = this.db.getTwitterNameFromDeviceId(deviceId);
 					if(username != null) {
 						usernames.append(String.format("@%s ", username));
 						counter++;
 					}
 				}
 				
 				if(counter > 1)
 					usernames.append("are in.");
 				else if(counter == 1)
 					usernames.append("is in.");
 				else if(counter == 0)
 					usernames.append("There is no one in the room.");
 				
 				this.messagesToPublish.add(usernames.toString());
 				this.nearbyUsersSemaphore.release();
 				break;
 			} catch (InterruptedException e) {
 				
 			}
 		}
 	}
 	
 	protected void respondToOpenDoor(Matcher m, Tweet msg) {
 		String username = msg.getUser().getScreenName();
 		this.door.open_door();
 		try {
 			this.nearbyUsersSemaphore.acquire();
 			this.messagesToPublish.add(String.format("@%s opened the door.", username));
 			String deviceId = this.db.getDeviceIdFromUsername(username);
 			if(!this.nearbyUsers.contains(deviceId)) 
 				this.nearbyUsers.add(deviceId);
 			this.nearbyUsersSemaphore.release();
 		} catch (InterruptedException e) {
 			
 		}
 	}
 	
 	protected void respondToIsIn(Matcher m, Tweet msg) {
 		String username = msg.getUser().getScreenName();
 		String deviceId = this.db.getDeviceIdFromUsername(m.group(1));
 		System.out.println(String.format("[DEVICE ID]: %s -> %s", m.group(1), deviceId));
 		while(true) {
 			try {
 				this.nearbyUsersSemaphore.acquire();
 				System.out.print("[IDs IN]:");
 				for(String d: this.nearbyUsers) {
 					System.out.println(String.format("%s ", d));
 				}
 				if(this.nearbyUsers.contains(deviceId))
					this.messagesToPublish.add(String.format("@s, @%s is in the room.", username, m.group(1)));
 				else
					this.messagesToPublish.add(String.format("@s, @%s is not in the room.", username, m.group(1)));
 				this.nearbyUsersSemaphore.release();
 				break;
 			} catch (InterruptedException e) {
 				
 			}
 		}
 	}
 	
 	protected void respondToIsDoorOpen(Matcher m, Tweet msg) {
 		String username = msg.getUser().getScreenName();
 		String doorState = this.door.get_door_state_str();
 		this.messagesToPublish.add(String.format("@%s, door is %s.", username, doorState.toLowerCase()));
 	}
 	
 	@Override
 	public void consume(Tweet msg) {
 		/*
 		 * Get the message content
 		 */
 		String msgContent = msg.getTweet();
 		
 		/*
 		 * Find the pattern matches to tweet's content.
 		 */
 		Matcher m = null;
 		int methodType = -1;
 		for(Map.Entry<Pattern, Integer> entry: this.methodMap.entrySet()) {
 			if(m == null)
 				m = entry.getKey().matcher(msg.getTweet());
 			else {
 				m.usePattern(entry.getKey());
 				m.reset(msg.getTweet());
 			}
 			
 			if(m.matches()) {
 				methodType = entry.getValue().intValue();
 				break;
 			}
 		}
 
 		/*
 		 * Execute the method this pattern maps.
 		 * If there is no match, print the message content.
 		 */
 		switch(methodType) {
 		case 0:	
 			this.respondToWhoIsIn(m, msg);
 			break;
 		case 1:
 			this.respondToOpenDoor(m, msg);
 			break;
 		case 2:
 			this.respondToIsIn(m, msg);
 			break;
 		case 3:
 			this.respondToIsDoorOpen(m, msg);
 			break;
 		default: System.out.println(String.format("No pattern matches to --> %s", msgContent));
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String regex = "\\s*[Ww]ho\\s+is\\s+in\\s*\\??\\s*";
 		System.out.println(Pattern.compile(regex).matcher("Who is in?").matches());
 		System.out.println(Pattern.compile(regex).matcher("Who      is in").matches());
 		System.out.println(Pattern.compile(regex).matcher("Open the door.").matches());
 		
 		regex = "\\s*[Oo]pen(?:\\s+the)?\\s+door\\s*\\.?\\s*";
 		System.out.println(Pattern.compile(regex).matcher("Open the door.").matches());
 		System.out.println(Pattern.compile(regex).matcher("open  door  ").matches());
 		System.out.println(Pattern.compile(regex).matcher("Open the door.1").matches());
 		
 		regex = "\\s*[Ii]s\\s+@(\\p{Alnum}+)\\s+in\\s*\\??\\s*";
 		Matcher m = Pattern.compile(regex).matcher("Is @bs in?");
 		System.out.println(m.matches());
 		System.out.println(m.group(1));
 		m.reset("is    @Osman in");
 		System.out.println(m.matches());
 		System.out.println(m.group(1));
 		m.reset("is    #Osman i");
 		System.out.println(m.matches());
 		
 		regex = "\\s*[Ii]s(?:\\s+the)?\\s+door\\s+open\\s*\\??\\s*";
 		System.out.println(Pattern.compile(regex).matcher("is door open").matches());
 		System.out.println(Pattern.compile(regex).matcher("is   the     door open?").matches());
 		System.out.println(Pattern.compile(regex).matcher("Open the door.").matches());
 	}
 }
