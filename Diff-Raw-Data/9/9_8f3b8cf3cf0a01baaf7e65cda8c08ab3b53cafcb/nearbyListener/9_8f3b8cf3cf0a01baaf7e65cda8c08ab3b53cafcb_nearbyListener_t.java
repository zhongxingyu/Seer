 package net.ednovak.nearby;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.packet.Message;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class nearbyListener implements MessageListener {
 	
 	private Context context;
 	private ArrayList<buffer> buffs;
 	
 	public nearbyListener(Context nContext, ArrayList<buffer> nBuffs){
 		context = nContext;
 		buffs = nBuffs;
 	}
 	
 	@Override
 	public void processMessage(Chat chat, Message message) {
 		//Log.d("xmpp", "Chat recieved in thread: " + message.getBody());
 		Log.d("xmpp", "Chat recieved in thread");
 		//Log.d("xmpp", "message: " + message.getBody().toString());
 		
 		buffer buff = parseIncoming(message);
 		
 		// Message stream over, time to process this buffer
 		if ( buff != null ) {
 			
 			//Log.d("receive", "buff.message: " + buff.message);
 			
 			// Initialize stuff
 			protocol p = new protocol();
 			//shareSingleton share = shareSingleton.getInstance();
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			
 			long end = System.currentTimeMillis();
 			long total_recTime = end - buff.start;
 			Log.d("stats", "It took: " + total_recTime + "ms to recieve all chunks");
 			
 			String sender = buff.sender;
 			String[] parts = buff.message.split(":");
 			int stage = Integer.valueOf(parts[0]);
 			
 			switch (stage){	
 				case 1: // The Polynomial (Case 1, stage 1)
 					if (parts[2].equals("where are you?")) {
 						// Bob only wants to share if Alice is near him so lets run the protocol
 						
 						// Initialize
 						int bits = Integer.valueOf(prefs.getString("bits", "1024"));
 						int policy = Integer.valueOf(prefs.getString("policy", "160000"));
 						int method = Integer.valueOf(prefs.getString("poly_method", "1"));
 						Log.d("test", "method: " + method + "  policy: " + policy + "  bits: " + bits);
 						
 						// Find span
 						int[] span = new int[3];
 						span = p.makeSpan(2, p.locSimple(context), policy);
 						
 						// Generate leaves
 						treeQueue leaves = p.genLeaves(span[0], span[2], span[1]);
 						Log.d("bob-stats", "The span size: " + leaves.length);
 						
 						// Generate root
 						tree root = p.buildUp(leaves);
 						Log.d("bob-stats", "The tree has " + root.count() + " nodes");
 						
 						// Find Wall
 						treeQueue wall = p.findWall(leaves.peek(0), leaves.peek(-1), root);
 						Log.d("bob-stats", "Wall size: " + wall.length);
 						
 						// Find Coefficients
 						BigInteger[] coefficients = p.makeCoefficients(wall, method);
 						Log.d("bob-stats", "Number of Coefficients: " + coefficients.length);
 						
 						// Encrypt Coefficients
 						BigInteger[] encCoe = p.encryptArray(coefficients, context);
 						//Log.d("test", "Bob's encrypted Coe's");
 						
 						// Put them in a string for sending
 						StringBuffer txt = new StringBuffer();
 						txt.append(encCoe[0].toString(16)); // The first one should not begin with ":"
 						for (int i = 1; i < encCoe.length; i++){
 							txt.append(":" + encCoe[i].toString(16));
 						}
 						
 						// Extra Stuff
 						BigInteger[] key = p.getKey(1024).publicKey();
 						txt.append(":" + policy);
 						txt.append(":" + bits);
 						txt.append(":" + key[0].toString(16)); // g
 						txt.append(":" + key[1].toString(16)); // n
 						txt.append(":" + method); // Poly method used
 						// Other party needs these values						
 
 						//Log.d("test", "Message: " + txt.toString());
 						
 						// Send to Alice
 						p.sendFBMessage(sender, txt.toString(), 3, buff.session, context);
 						break;
 					}
 				// End of case 1
 			
 				// This is Alice now, recieving the longitude wall from Bob and doing her computation
 				case 3: // The computation (case 3, stage 3)
 					// Initialize
 					int bits = Integer.valueOf( parts[parts.length - 4] );
 					int method = Integer.valueOf( parts[parts.length - 1]);
 					BigInteger g = new BigInteger( parts[parts.length - 3], 16 );
 					BigInteger n = new BigInteger( parts[parts.length - 2], 16 );
 					
 					// Make Span
 					//int[] span = new int[3];
 					int[] span = p.makeSpan(3, p.locSimple(context), 160934); // 160.934km = 100mi
 					
 					// Generate Leaves
 					//treeQueue leaves = p.genLeaves(span[0], span[2], span[1]);
 					
 					// Generate Alice's leaf.
 					String mapString = new StringBuffer(Integer.toBinaryString(span[1])).toString();
 					tree alice = new tree(span[1], mapString.toCharArray(), null, null, 0);
 					Log.d("stats-alice", "alice: " + alice.value);
 					
 					// Find Path
 					long pathStart = System.currentTimeMillis();
 					treeQueue path = p.findPath(alice, 13); // User's location leaf node
 					long pathEnd = System.currentTimeMillis();
 					Log.d("stats-alice", "Time to find path: " + (pathEnd - pathStart));
 					Log.d("stats-alice", "User's path length: " + path.length);
 					
 					// Pull out Encrypted Coefficients
 					//for(int i = 0; i < parts.length; i++){
 					//	Log.d("test", "parts[" + i + "]: " + parts[i]);
 					//}
 					BigInteger[] encCoe = new BigInteger[parts.length - 7];
 					for(int i = 0; i < encCoe.length; i++){
 						encCoe[i] = new BigInteger(parts[i+2], 16);
 						//Log.d("test", "encCoe[" + i + "]:" + encCoe[i]);
 					}
 					
 					
 					// Do the actual homomorphic computation
 					Log.d("test", "Homomorphic computation time");
 					long compStart = System.currentTimeMillis();
 					BigInteger[] results = p.computation(path, encCoe, bits, g, n, method);
 					long compEnd = System.currentTimeMillis();
 					Log.d("stats-alice", "Time to perform homomorphic encryption: " + (compEnd - compStart));
 					Log.d("stats-alice", "Number of results generated: " + results.length);
 					
 					// Put them in a string for sending
 					StringBuffer txt = new StringBuffer();
 					txt.append(results[0].toString(16));
 					for (int i = 1; i < results.length; i++){
 						txt.append(":" + results[i].toString(16));
 					}
 					
 					// Send that shit dawg
 					p.sendFBMessage(sender, txt.toString(), 4, buff.session, context);
 					break;
 				// End of stage 3 (case 3)
 					
 				case 4: // The Check (stage 4, case 4)
 					Log.d("test", "checking!");
 					
 					// Pull out the C values
 					String[] cValues = new String[parts.length - 2];
 					for(int i = 0; i < cValues.length; i++){
 						cValues[i] = parts[i+2];
 					}
 					
 					// Check the C values
 					bits = Integer.valueOf(prefs.getString("bits", "1024"));
 					shareSingleton share = shareSingleton.getInstance();
 					share.foundLon = p.check(cValues, context, bits);
 					Log.d("output", "Same location: " + share.foundLon);
 					
 					// not sure why bits and method give the 'already instantiated' error but policy does not.
 					// Also, if the uses changes their preferences (mid run) there is a problem
 					// BEGIN THE LATITUDE ROUND (stage 5)
 					bits = Integer.valueOf(prefs.getString("bits", "1024"));
 					int policy = Integer.valueOf(prefs.getString("policy", "160000"));
 					method = Integer.valueOf(prefs.getString("poly_method", "1"));
 					
 					// Find Span
 					span = p.makeSpan(5, p.locSimple(context), policy);
 					
 					// Generate Leaves
 					treeQueue leaves = p.genLeaves(span[0], span[2], span[1]);
 					
 					// Find Root
 					tree root = p.buildUp(leaves);
 					
 					// Find Wall
 					treeQueue wall = p.findWall(leaves.peek(0), leaves.peek(-1), root);
 					
 					// Find Coefficients (latitude now)
 					BigInteger[] coefficients = p.makeCoefficients(wall, method);
 					
 					// Encrypt Coefficients
 					encCoe = p.encryptArray(coefficients, context);
 					
 					// Put them in a string for sending
 					txt = new StringBuffer();
 					txt.append(encCoe[0].toString(16));
 					for (int i = 1; i < encCoe.length; i++){
 						txt.append(":" + encCoe[i].toString(16));
 					}
 					
 					// Extra Stuff
 					BigInteger[] key = p.getKey(1024).publicKey();
 					txt.append(":" + policy);
 					txt.append(":" + bits);
 					txt.append(":" + key[0].toString(16)); // g
 					txt.append(":" + key[1].toString(16)); // n
 					txt.append(":" + method); // Poly method used
 					// Other party needs these values
 					
 					// Send To Alice (latitude)
 					p.sendFBMessage(sender, txt.toString(), 6, buff.session, context);
 					break;
 				// End of stage 5 (case 4)
 				
 				// Alice does the latitude computation
 				case 6: //(stage 6, case 6)
 					// Initialize
 					bits = Integer.valueOf( parts[parts.length - 4] );
 					method = Integer.valueOf( parts[parts.length - 1]);
 					g = new BigInteger( parts[parts.length - 3], 16 );
 					n = new BigInteger( parts[parts.length - 2], 16 );
 					
 					// Make Span
 					//int[] span = new int[3];
 					span = p.makeSpan(6, p.locSimple(context), 160934); // 160.934km = 100mi
 					
 					// Generate Leaves
 					//treeQueue leaves = p.genLeaves(span[0], span[2], span[1]);
 					
 					// Generate Alice's leaf.
 					mapString = new StringBuffer(Integer.toBinaryString(span[1])).toString();
 					alice = new tree(span[1], mapString.toCharArray(), null, null, 0);
 					Log.d("checking", "alice: " + alice.value);
 					
 					// Find Path
 					path = p.findPath(alice, 13); // User's location leaf node
 					Log.d("stats", "User's path length: " + path.length);
 					
 					// Pull out Encrypted Coefficients
 					//for(int i = 0; i < parts.length; i++){
 					//	Log.d("test", "parts[" + i + "]: " + parts[i]);
 					//}
 					encCoe = new BigInteger[parts.length - 7];
 					for(int i = 0; i < encCoe.length; i++){
 						encCoe[i] = new BigInteger(parts[i+2], 16);
 						//Log.d("test", "encCoe[" + i + "]:" + encCoe[i]);
 					}
 					
 					
 					// Do the actual homomorphic computation
 					Log.d("test", "Homomorphic computation time");
 					results = p.computation(path, encCoe, bits, g, n, method); 
 					
 					
 					// Put them in a string for sending
 					txt = new StringBuffer();
 					txt.append(results[0].toString(16));
 					for (int i = 1; i < results.length; i++){
 						txt.append(":" + results[i].toString(16));
 					}
 					
 					// Send that shit dawg
 					p.sendFBMessage(sender, txt.toString(), 7, buff.session, context);
 					break;
 				// End of stage 6 (case 6)
 					
 				case 7: // Bob checks (case 7, stage 7) final stage
 					
 					Log.d("test", "checking!");
 					
 					// Pull out the C values
 					cValues = new String[parts.length - 2];
 					for(int i = 0; i < cValues.length; i++){
 						cValues[i] = parts[i+2];
 					}
 					
 					// Check the C values
 					bits = Integer.valueOf(prefs.getString("bits", "1024"));
 					boolean latResult = p.check(cValues, context, bits);
 					Log.d("output", "Same location: " + latResult);
 					
 					// Determine if near!
 					share = shareSingleton.getInstance();
 					boolean near = latResult && share.foundLon;
 
 					// Notify Bob (myself)
 					String contentTitle = sender + " queried you";
 					String contentText;
					Location l = p.locSimple(context);
					
 					if (near){
 						contentText = "Your location was shared";
 					}
 					else{
 						contentText = "Your location was not shared";
						l.setLatitude(0.0);
						l.setLongitude(0.0);
 					}
 					p.notification("Nearby Query Processed", contentTitle, contentText, context, MainActivity.class);
 					
 					
 					// Send current location to Alice
					p.sendFBMessage(sender, l.getLongitude() + ", " + l.getLatitude(), 8, buff.session, context);
 					break;
 					// End of stage 7 (case 7)
 					
 				// Alice learns Bob's location
 				case 8: // Stage 8 (case 8)
 					long totalEnd = System.currentTimeMillis();
 					Log.d("stats-alice", "Run is over: " + parts[2]);
 					share = shareSingleton.getInstance(); 
 					Log.d("stats-alice", "Entire protocol took: " + (totalEnd - share.start));
 					
 					String Title = sender + "'s Location";
 					// Notify Alice (myself)
 					p.notification("Nearby Query Processed", Title, parts[2], context, MainActivity.class);
 					
 			} // End of switch
 		}							
 	} // end of Process Chat
 	
 	// Searches for a buffer by session number
 	// This function avoids collisions by putting buffer objects into an arrayList of buffers
 	// Return buffer if found, returns null otherwise
 	private buffer searchBuff(String session){
 		for (buffer b : buffs){		
 			if (b.session.equals(session)){
 				return b;
 			}
 		}
 		return null;
 	}
 	
 	
 	// Clears a buffer (takes it out of the arrayList
 	// Also, takes the trailing "@@" off the buffer
 	// Returns the buffer object
 	private void clearBuff(buffer b){
 		for (int i = 0; i < buffs.size(); i++){
 			if ( buffs.get(i) == b ){
 				buffs.remove(i);
 			}
 		}
 		b.message = b.message.substring(0, b.message.length() - 2); // Remove trailing "@@"
 	}
 	
 	
 	// Puts packets into ArrayList<buffer>.  Returns a buffer when it is full, returns null otherwise
 	private buffer parseIncoming(Message packet){		
 		
 		boolean lastPacket = false;
 		if ( packet.getBody().substring(0, 2).equals("@@") ){
 			//String message = packet.getBody();
 			//int stage = Integer.valueOf(packet.getBody().substring(2,3));
 			String[] parts = packet.getBody().split(":");
 			String session = parts[1];
 			buffer buff;
 			buff = searchBuff(session);
 			if (buff == null){ // If the buffer is null we haven't seen this session yet and create a new buffer
 				String sender = xmppService.getRoster().getEntry(packet.getFrom().toString()).getName();
 				buff = new buffer(sender, System.currentTimeMillis(), session);
 				buffs.add(buff);
 				buff.append(packet.getBody().substring(2, 10));
 			}
 			String tmpMess = packet.getBody().substring(10); // Removing the beginning "@@X:sessi:" in message
 			buff.append(tmpMess);
 			lastPacket = packet.getBody().substring( packet.getBody().length() - 2 ).equals("@@"); // Packet ends in "@@"
 			if ( lastPacket ){
 				clearBuff(buff);
 				return buff;
 			}
 		}
 		// This is true of the most recent packet contained the end of sequence symbol at the end
 		// buff.message.substring(buff.message.length() - 2).equals("@@");
 		return null;
 	}
 }
