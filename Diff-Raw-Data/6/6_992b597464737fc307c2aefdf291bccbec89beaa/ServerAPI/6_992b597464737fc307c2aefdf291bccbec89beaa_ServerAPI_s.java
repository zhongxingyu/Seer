 package uk.co.thomasc.wordmaster.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.game.Achievements;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;
 import uk.co.thomasc.wordmaster.objects.User;
 import uk.co.thomasc.wordmaster.util.GameHelper;
 
 public class ServerAPI {
 
 	private static final String BASE_URL = "https://thomasc.co.uk/wm/";
 	private static String playerid = "";
 
 	/**
 	 * Calls the getMatches function of the server API. Retrieves
 	 * a list of all the games a player is involved in.
 	 * 
 	 * @param playerID – the Google+ ID of the player to retrieve games for
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @param listener – a GetMatchesRequestListener to be notified when the request finishes 
 	 */
 	public static void getMatches(final String playerID, final BaseGame activityReference, final GetMatchesRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("getMatches", activityReference);
 				if (json != null) {
 					int errorCode = ((Long) json.get("error")).intValue();
 					if (errorCode == 0) {
 						JSONArray response = (JSONArray) json.get("response");
 						Game[] games = new Game[response.size()];
 						for (int i = 0; i < response.size(); i++) {
 							JSONObject gameObject = (JSONObject) response.get(i);
 							String gameID = (String) gameObject.get("gameid");
 							boolean needsWord = (Boolean) gameObject.get("needword");
 							int playerScore = ((Long) gameObject.get("pscore")).intValue();
 							boolean playersTurn = (Boolean) gameObject.get("turn");
 							int alpha = gameObject.containsKey("alpha") ? ((Long) gameObject.get("alpha")).intValue() : 0;
 							
 							User opp = null;
 							int opponentScore = 0;
 							if (gameObject.containsKey("oppid")) {
 								opp = User.getUser((String) gameObject.get("oppid"), activityReference);
 								opponentScore = ((Long) gameObject.get("oscore")).intValue();
 							}
 							
 							Game game = Game.getGame(gameID, User.getUser(playerID, activityReference), opp);
 							game.setPlayersTurn(playersTurn);
 							game.setNeedsWord(needsWord);
 							game.setScore(playerScore, opponentScore);
 							if (gameObject.containsKey("updated")) {
 								long updated = (Long) gameObject.get("updated");
 								game.setLastUpdateTimestamp(updated);
 							}
 							game.setAlpha(alpha);
 							games[i] = game;
 						}
 						listener.onRequestComplete(games);
 						return;
 					}
 				}
 				listener.onRequestFailed();
 			}
 		};
 		t.start();
 	}
 
 	/**
 	 * Calls the getTurns function on the server API. Returns an array
 	 * containing all the turns taken in the game.
 	 * 
 	 * @param gameID – the game ID of the game to retrieve turns from
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @param listener – a GetTurnsRequestListener to be notified when the request finishes 
 	 */
 	public static void getTurns(final String gameID, final BaseGame activityReference, final GetTurnsRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("getTurns", gameID, activityReference);
 				if (json != null) {
 					List<Turn> turns = ServerAPI.getTurns(json, activityReference);
 					if (turns != null) {
 						listener.onRequestComplete(turns);
 						return;
 					}
 				}
 				listener.onRequestFailed();
 			}
 		};
 		t.start();
 	}
 
 	/**
 	 * Calls the getTurns function on the server API. Starting from a 'pivot'
 	 * turn, retrieves a given number of turns from before or after this point.
 	 * Returns an array containing the turns.
 	 * 
 	 * @param gameID – the game ID of the game to retrieve turns from
 	 * @param turnID – the turn ID of the 'pivot' turn 
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @param listener – a GetTurnsRequestListener to be notified when the request finishes 
 	 */
 	public static void getTurns(final String gameID, final int turnID, final BaseGame activityReference, final GetTurnsRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("getTurns", gameID, String.valueOf(turnID), activityReference);
 				if (json != null) {
 					List<Turn> turns = ServerAPI.getTurns(json, activityReference);
 					if (turns != null) {
 						listener.onRequestComplete(turns);
 						return;
 					}
 				}
 				listener.onRequestFailed();
 			}
 		};
 		t.start();
 	}
 
 	/**
 	 * Calls the getTurns function on the server API. Starting from a 'pivot'
 	 * turn, retrieves a given number of turns from before or after this point.
 	 * Returns an array containing the turns.
 	 * 
 	 * @param gameID – the game ID of the game to retrieve turns from
 	 * @param turnID – the turn ID of the 'pivot' turn
 	 * @param number – the number of turns to retrieve (negative for less recent, positive for more recent) 
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @param listener – a GetTurnsRequestListener to be notified when the request finishes 
 	 */
 	public static void getTurns(final String gameID, final int turnID, final int number, final BaseGame activityReference, final GetTurnsRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("getTurns", gameID, String.valueOf(turnID), Integer.toString(number), activityReference);
 				if (json != null) {
 					List<Turn> turns = ServerAPI.getTurns(json, activityReference);
 					if (turns != null) {
 						listener.onRequestComplete(turns);
 						return;
 					}
 				}
 				listener.onRequestFailed();
 			}
 		};
 		t.start();
 	}
 
 	private static List<Turn> getTurns(JSONObject json, BaseGame activityReference) {
 		int errorCode = ((Long) json.get("error")).intValue();
 		if (errorCode == 0) {
 			JSONArray response = (JSONArray) json.get("response");
 			List<Turn> turns = new ArrayList<Turn>();
 			for (int i = 0; i < response.size(); i++) {
 				JSONObject turnObject = (JSONObject) response.get(i);
 				int id = ((Long) turnObject.get("turnid")).intValue();
 				int num = ((Long) turnObject.get("turnnum")).intValue();
 				String playerID = (String) turnObject.get("playerid");
 				String guess = (String) turnObject.get("guess");
 				long when = (Long) turnObject.get("when");
 				int correct = ((Long) turnObject.get("correct")).intValue();
 				int displaced = ((Long) turnObject.get("displaced")).intValue();
 
 				Turn turn;
 				if (correct == 4) {
 					String opponentWord = (String) turnObject.get("oppword");
 					turn = new Turn(id, num, new Date(when), User.getUser(playerID, activityReference), guess, correct, displaced, opponentWord);
 				} else {
 					turn = new Turn(id, num, new Date(when), User.getUser(playerID, activityReference), guess, correct, displaced);
 				}
 				turns.add(turn);
 			}
 			return turns;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Calls the takeTurn function on the server API. Makes a guess for the player
 	 * and updates the state of the game on the server. Returns two booleans to
 	 * represent the outcome of the call.
 	 * 
 	 * @param playerID – the Google+ ID of the player taking the turn
 	 * @param gameID – the game ID of the game the turn is from
 	 * @param word – the guess the player has made
 	 * @param listener – a TakeTurnRequestListener to be notified when the request finishes 
 	 */
 	public static void takeTurn(final String playerID, final String gameID, final String word, final BaseGame activityReference, final TakeTurnRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("takeTurn", gameID, word, activityReference);
 
 				int errorCode = ((Long) json.get("error")).intValue();
 				listener.onRequestComplete(errorCode);
 			}
 		};
 		t.start();
 	}
 
 	/**
 	 * Calls the createGame function of the server API. Creates a new game
 	 * involving the given players. If no opponent is specified, the player
 	 * is put into the automatch pool.
 	 * 
 	 * @param playerID – the Google+ ID of the user
 	 * @param opponentID – the Google+ ID of the opponent (null for automatch)
 	 * @param activityReference – reference to the BaseGame activity, used to get avatars from Google+
 	 * @param listener – a CreateGameRequestListener to be notified when the request finishes 
 	 */
 	public static void createGame(final String playerID, final String opponentID, final BaseGame activityReference, final CreateGameRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json;
 				if (opponentID == null) {
 					json = ServerAPI.makeRequest("createGame", activityReference);
 				} else {
 					json = ServerAPI.makeRequest("createGame", opponentID, activityReference);
 				}
 				int errorCode = ((Long) json.get("error")).intValue();
 				if (errorCode == 0) {
 					JSONObject response = (JSONObject) json.get("response");
 					String gameID = (String) response.get("gameid");
 					
 					User opp = null;
 					if (opponentID != null) {
 						opp = User.getUser(opponentID, activityReference);
 					}
 					
 					Game game = Game.getGame(gameID, User.getUser(playerID, activityReference), opp);
 					listener.onRequestComplete(game);
 				} else {
 					listener.onRequestFailed(errorCode);
 				}
 			}
 		};
 		t.start();
 	}
 
 	/**
 	 * Calls the setWord function of the server API. If no word has been set
 	 * for the given player in the given game, their word is updated. Returns
 	 * two booleans representing the response from the server.
 	 * 
 	 * @param playerID – the Google+ ID of the player whose word is being set
 	 * @param gameID – the game ID of the game the player is involved in
 	 * @param word – the word the player wishes to use
 	 * @param listener – a SetWordRequestListener to be notified when the request finishes 
 	 */
 	public static void setWord(final String playerID, final String gameID, final String word, final BaseGame activityReference, final SetWordRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("setWord", gameID, word, activityReference);
 				int errorCode = -3;
 				if (json != null) {
 					errorCode = ((Long) json.get("error")).intValue();
 				}
 				
 				listener.onSetWordComplete(errorCode);
 			}
 		};
 		t.start();
 	}
 
 	public static void registerGCM(final String playerID, final String regid, final BaseGame activityReference) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				ServerAPI.makeRequest("registerGCM", regid, activityReference);
 			}
 		};
 		t.start();
 	}
 
 	public static void upgradePurchased(final String token, final BaseGame activityReference) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				ServerAPI.makeRequest("upgradePurchased", token, activityReference);
 			}
 		};
 		t.start();
 	}
 
 	public static void updateAlpha(final String gameID, final int alpha, final BaseGame activityReference, final UpdateAlphaRequestListener listener) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.makeRequest("updateAlpha", gameID, String.valueOf(alpha), activityReference);
				int errorCode = ((Long) json.get("error")).intValue();
 
 				listener.onRequestComplete(errorCode, activityReference);
 			}
 		};
 		t.start();
 	}
 
 	public static void identify(final String authToken, final BaseGame activityReference, final GameHelper gameHelper) {
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				JSONObject json = ServerAPI.doRequest(ServerAPI.BASE_URL + "identify" + "/" + authToken, activityReference);
 				JSONObject response = (JSONObject) json.get("response");
 				playerid = (String) response.get("key");
 				activityReference.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						gameHelper.onConnected(null);
 					}
 				});
 			}
 		};
 		t.start();
 	}
 
 	private static JSONObject makeRequest(String iface, String param1, String param2, String param3, BaseGame activityReference) {
 		return ServerAPI.doRequest(ServerAPI.BASE_URL + iface + "/" + playerid + "/" + param1 + "/" + param2 + "/" + param3, activityReference);
 	}
 
 	private static JSONObject makeRequest(String iface, String param1, String param2, BaseGame activityReference) {
 		return ServerAPI.doRequest(ServerAPI.BASE_URL + iface + "/" + playerid + "/" + param1 + "/" + param2, activityReference);
 	}
 
 	private static JSONObject makeRequest(String iface, String param1, BaseGame activityReference) {
 		return ServerAPI.doRequest(ServerAPI.BASE_URL + iface + "/" + playerid + "/" + param1, activityReference);
 	}
 	
 	private static JSONObject makeRequest(String iface, BaseGame activityReference) {
 		return ServerAPI.doRequest(ServerAPI.BASE_URL + iface + "/" + playerid, activityReference);
 	}
 
 	private static JSONObject doRequest(String url, final BaseGame activityReference) {
 		String jsonText = "";
 		System.out.println(url);
 
 		try {
 			InputStream is = new URL(url).openStream();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 			String inputLine;
 			while ((inputLine = reader.readLine()) != null) {
 				jsonText += inputLine;
 			}
 			is.close();
 
 			JSONParser parser = new JSONParser();
 			JSONObject jsonObject = (JSONObject) parser.parse(jsonText);
 			ServerAPI.processAchievements(jsonObject, activityReference);
 			return jsonObject;
 		} catch (IOException ex) {
 			return null;
 		} catch (ParseException ex) {
 			return null;
 		}
 	}
 
 	private static void processAchievements(JSONObject json, BaseGame activityReference) {
 		JSONObject achievements = (JSONObject) json.get("achievements");
 		for (Object key : achievements.keySet()) {
 			int sid = Integer.parseInt((String) key);
 			Achievements achievement = Achievements.forServerId(sid);
 			if (achievement != null) {
 				int increment = ((Long) achievements.get(key)).intValue();
 				activityReference.unlockAchievement(achievement, increment);
 			}
 		}
 	}
 
 }
