 package edu.cmu.hcii.peer;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class MessageHandler {
 	private static String TAG = "MessageHandler";
 	
 	public static String MSG_TYPE_COMMAND = "command";
 	public static String MSG_TYPE_AUDIO_LEVEL = "audioLevel";
 	public static String MSG_TYPE_AUDIO_BUSY = "audioBusy";
 	
 	public static int COMMAND_NOT_FOUND = 0; 
 	public static int COMMAND_CONFIRMATION = 1;
 	public static int COMMAND_BACK = 2;
 	public static int COMMAND_NEXT = 3;
 	public static int COMMAND_SCROLL_UP = 4;
 	public static int COMMAND_SCROLL_DOWN = 5;
 	public static int COMMAND_MENU = 6;
 	public static int COMMAND_CLOSE = 7;
 	public static int COMMAND_MENU_OVERVIEW = 8;
 	public static int COMMAND_MENU_STOWAGE = 9;
 	public static int COMMAND_MENU_ANNOTATION = 10;
 	public static int COMMAND_MENU_GROUND = 11;
 	public static int COMMAND_GO_TO_STEP = 12;
 	public static int COMMAND_TIMER_START = 13;
 	public static int COMMAND_TIMER_RESET = 14;
 	public static int COMMAND_TIMER_STOP = 15;
 	public static int COMMAND_INPUT = 16;
 	public static int COMMAND_STEP_NUMBER = 17;
 	public static int COMMAND_CYCLE_NUMBER = 18;
 	public static int COMMAND_COND_EXPAND = 19;
 	public static int COMMAND_COND_COLLAPSE = 20;
 
 	
 	public static long COMMANDS_TIMEOUT_DURATION = 4000; // in millesconds
 	public static float MINIMUM_REFRESH_RMS = 7;
 	
 	private static long lastMessageTime;
 	private static boolean active;
 	
 	
 	/**
 	 * Sends a string broadcast message to be read by other classes
 	 * 
 	 * @param in message type
 	 * @param ctx Context
 	 * @param msg
 	 */
 	private static void sendBroadcastMsg(Context ctx, String in, String msg){
         Intent intent = new Intent(in);
         intent.putExtra("msg", msg);
         ctx.sendBroadcast(intent);
 	}
 	
 	/**
 	 * Sends an integer broadcast message to be read by other classes
 	 * Currently used for commands
 	 * 
 	 * @param in message type
 	 * @param ctx Context
 	 * @param msg
 	 */
 	private static void sendBroadcastMsg(Context ctx, String in, int msg){
         Intent intent = new Intent(in);
         intent.putExtra("msg", msg);
         ctx.sendBroadcast(intent);
 	}
 	
 	/**
 	 * Sends a broadcast message that includes an additional int
 	 * Currently used for commands
 	 * 
 	 * @param in message type
 	 * @param ctx Context
 	 * @param msg the command identifier
 	 * @param string an additional string
 	 */
 	private static void sendBroadcastMsg(Context ctx, String in, int msg, String str){
         Intent intent = new Intent(in);
         intent.putExtra("msg", msg);
         intent.putExtra("str",str);
         ctx.sendBroadcast(intent);
 	}
 	
 	/**
 	 * Parses an input JSON string
 	 * 
 	 * @param ctx Context
 	 * @param msg 
 	 */
 	public static void parseMsg(Context ctx, String msg){
 		JSONObject json;
 		
 		try {
 			json = new JSONObject(msg);
 			String type = json.getString("type");
 			String content = json.getString("content");	
 			
 			//Log.v(TAG, "type: "+type+", "+"content: "+content);
 			
 			if (type.equals(MSG_TYPE_COMMAND)){
 				if (System.currentTimeMillis() - lastMessageTime < COMMANDS_TIMEOUT_DURATION ||
 					content.equals("ready")){
 					handleCommand(ctx, type, content);
 					lastMessageTime = System.currentTimeMillis();
 					active = true;
 				}
 			}else if (type.equals(MSG_TYPE_AUDIO_LEVEL)){
 	    		if (Float.parseFloat(content) > MINIMUM_REFRESH_RMS && active)
 	    			lastMessageTime = System.currentTimeMillis();
	    		if (System.currentTimeMillis() - lastMessageTime > COMMANDS_TIMEOUT_DURATION)
	    			active = false;
	    			
 				sendBroadcastMsg(ctx, MSG_TYPE_AUDIO_LEVEL, content);
 			}else if (type.equals(MSG_TYPE_AUDIO_BUSY)){
 				sendBroadcastMsg(ctx, MSG_TYPE_AUDIO_BUSY, content);
 			}
 				
 				
 		
 		} catch (JSONException e) {
 			
 			e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * 
 	 */
 	
 	
 	/**
 	 * Reads an input command and returns a message for activities to see
 	 * This is where we convert verbal messages into commands to be executed
 	 * 
 	 * @param content
 	 * @return command
 	 */
 	private static void handleCommand(Context ctx, String type, String content){
 		Log.v(TAG, "type: "+type+", "+"command_msg: "+content);
 		
 		int commandIdentifier = COMMAND_NOT_FOUND;
 		
 		if (content.equals("ready")){
 			commandIdentifier = COMMAND_CONFIRMATION;
 		}else if (content.equals("back")){
 			commandIdentifier = COMMAND_BACK;
 		}else if (content.equals("next")){
 			commandIdentifier = COMMAND_NEXT;
 		}else if (content.equals("up")){
 			commandIdentifier = COMMAND_SCROLL_UP;
 		}else if (content.equals("down")){
 			commandIdentifier = COMMAND_SCROLL_DOWN;
 		}else if (content.equals("menu")){
 			commandIdentifier = COMMAND_MENU;
 		}else if (content.equals("close")){
 			commandIdentifier = COMMAND_CLOSE;
 		}else if (content.equals("overview")){
 			commandIdentifier = COMMAND_MENU_OVERVIEW;
 		}else if (content.equals("stowage")){
 			commandIdentifier = COMMAND_MENU_STOWAGE;
 		}else if (content.equals("annotations")){
 			commandIdentifier = COMMAND_MENU_ANNOTATION;
 		}else if (content.equals("ground")){
 			commandIdentifier = COMMAND_MENU_GROUND;
 		}else if (content.startsWith("step")){
 			commandIdentifier = COMMAND_GO_TO_STEP;
 			content = content.replaceFirst("step", "");	
 			sendBroadcastMsg(ctx, MSG_TYPE_COMMAND, commandIdentifier, content.trim());
 		}else if (content.equals("start")){
 			commandIdentifier = COMMAND_TIMER_START;
 		}else if (content.equals("reset")){
 			commandIdentifier = COMMAND_TIMER_RESET;
 		}else if (content.equals("pause")){
 			commandIdentifier = COMMAND_TIMER_STOP;
 		}else if (content.startsWith("step")){
 			commandIdentifier = COMMAND_STEP_NUMBER;
 			String str = handleStep("step",content);
 			sendBroadcastMsg(ctx, MSG_TYPE_COMMAND, commandIdentifier, str);
 		}else if (content.startsWith("cycle")){
 			commandIdentifier = COMMAND_CYCLE_NUMBER;
 			String str = handleStep("cycle",content);
 			sendBroadcastMsg(ctx, MSG_TYPE_COMMAND, commandIdentifier, str);
 		}else if (content.equals("expand")){
 			commandIdentifier = COMMAND_COND_EXPAND;
 		}else if (content.equals("collapse")){
 			commandIdentifier = COMMAND_COND_COLLAPSE;
 		}
 		
 				
 		sendBroadcastMsg(ctx, MSG_TYPE_COMMAND, commandIdentifier);
 	}
 	
 	
 	private static String handleStep(String startMsg, String content){
 		int inputNumber;
 		content = content.replaceFirst(startMsg, "");	
 		content = content.trim();
 		try{
 			inputNumber = Integer.parseInt(content);
 		} catch(NumberFormatException e){
 			inputNumber = -1;
 		}
 		
 		
 		return inputNumber+"";
 	}
 	
 }
