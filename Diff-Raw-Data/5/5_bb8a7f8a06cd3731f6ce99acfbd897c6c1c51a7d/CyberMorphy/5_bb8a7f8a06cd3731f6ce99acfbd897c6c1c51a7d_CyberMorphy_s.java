 package de.morphyum.cybermorphy;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import javax.xml.crypto.Data;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.JoinEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 
 public class CyberMorphy extends ListenerAdapter {
 	int capesmorphy = 0;
 	int soldiersmorphy = 0;
 	int capesdeth = 0;
 	int capesarte = 0;
 	int bonksmorphy = 0;
 	int orbsgotarte = 0;
 	int orbsfailedarte = 0;
 
 	boolean greetmorphy = true;
 	boolean greetarte = false;
 
 	public void onJoin(JoinEvent event) throws Exception {
 		if (event.getUser().getNick().contentEquals("cybermorphy")) {
 			event.getBot().sendMessage(event.getChannel(), "Yay! I'm back, type !help to get to know me.");
 		} else if (event.getChannel().getName().contentEquals("#morphyum") && greetmorphy) {
 			event.getBot().sendMessage(event.getChannel(), "Hi " + event.getUser().getNick() + ", Welcome to the stream! <3");
 		} else if (event.getChannel().getName().contentEquals("#artegaomega") && greetarte) {
 			event.getBot().sendMessage(event.getChannel(), "Hi " + event.getUser().getNick() + ", Welcome to the stream! <3");
 		}
 	}
 
 	public void onMessage(MessageEvent event) throws Exception {
 
 		if (event.getMessage().equalsIgnoreCase("!bestmanever")) {
 			event.getBot().sendMessage(event.getChannel(), "Its Bloody the man of the man!");
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("teh urn")) {
 			event.getBot().sendMessage(event.getChannel(), "It's TEH URN!!");
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("what is this")) {
 			event.getBot().sendMessage(event.getChannel(), "That's called a Stream, open your eyes? o.O");
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("what it is?")) {
 			event.getBot().sendMessage(event.getChannel(), "That's what it do!");
 		} else if (event.getMessage().equalsIgnoreCase("!smwwiki")) {
 			event.getBot().sendMessage(event.getChannel(), "http://www.smwwiki.com");
 		} else if (event.getMessage().equalsIgnoreCase("!japanese")) {
 			event.getBot().sendMessage(event.getChannel(),
 					"The japanese version has less signs for the text, which makes it 20.8 seconds faster over the whole run.");
 		} else if (event.getMessage().equalsIgnoreCase("!wingdupe")) {
 			event.getBot().sendMessage(event.getChannel(),
 					"Blocks can be duplicated to the side, upwards, or upwards-diagonally by throwing a item at it while Mario is close to it.");
 			Thread.sleep(3000);
 			event.getBot()
 					.sendMessage(
 							event.getChannel(),
 							"When turn blocks are duplicated over half of a dragon coin and Mario collects the remaining half, the duplicated turn block will change into a key/wings/balloon/shell block based on horizontal position. And the Wings end the Level if collected with Yoshi!");
 		} else if ((event.getMessage().toLowerCase()).contains("where am i")) {
 			event.getBot().sendMessage(event.getChannel(), "In a chat, ofcourse.");
 		}
 
 		else if (event.getMessage().equalsIgnoreCase("!yoshi")) {
 			event.getBot().sendMessage(event.getChannel(), "A Moment of Silence please for yoshi, that died to make this run possible!");
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("!wr")) {
 			event.getBot().sendMessage(event.getChannel(), getWR(event.getMessage().toLowerCase().substring(4)));
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("http://www.youtube.com/watch?v=")) {
 
 			String[] texte = event.getMessage().split(" ");
 			for (int i = 0; i < texte.length; i++) {
 				if (texte[i].toLowerCase().contains("http://www.youtube.com/watch?v=")) {
 					System.out.println(texte[i]);
 					String link = texte[i];
 					event.getBot().sendMessage(event.getChannel(), getYoutube(link));
 				}
 			}
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("!goto") && event.getUser().getNick().equalsIgnoreCase("morphyum")) {
 			String[] message = event.getMessage().toLowerCase().split(" ");
 			String channel = message[1];
 			event.getBot().joinChannel("#" + channel);
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("!pb")) {
 			String[] message = event.getMessage().toLowerCase().split(" ");
 			String category = "";
 			for (int i = 1; i < message.length - 1; i++) {
 				if (i > 1)
 					category = category + " " + message[i];
 				else
 					category = message[i];
 			}
 			event.getBot().sendMessage(event.getChannel(), getPB(category, message[message.length - 1]));
 		}
 
 		else if (event.getMessage().equalsIgnoreCase("!king")) {
 			event.getBot().sendMessage(event.getChannel(), "Oh, the King has been transformed ! Please find the Magic Wand so we can change him back.");
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("!cape")) {
 			capes(event);
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("!soldier")) {
 			soldiers(event);
 		}
 
 		else if ((event.getMessage().toLowerCase()).contains("!bonk")) {
 			bonks(event);
 		}
 
 		else if (event.getMessage().equalsIgnoreCase("!race")) {
 			event.getBot().sendMessage(event.getChannel(), "To see all contestent of a race go to http://speedrunslive.com/races/#!/live !");
 		}
 
 		else if (event.getMessage().equalsIgnoreCase("!help")) {
 			event.getBot().sendMessage(event.getChannel(), "http://pastebin.com/9LxubXzA");
 
 		} else if (event.getMessage().toLowerCase().contains("!season")) {
 			if (event.getMessage().equalsIgnoreCase("!season")) {
 				season(event);
 				event.getBot().sendMessage(event.getChannel(), "The Rest of the Leaderboard can be found here: http://speedrunslive.com/races/seasons/1/1/4");
 			} else {
 				seasonsearch(event, event.getMessage().toLowerCase().substring(8));
 			}
 		}
 
 		else if (event.getMessage().equalsIgnoreCase("!join")) {
 
 			event.getBot().joinChannel("#" + event.getUser().getNick());
 
 		}
 
 		else if (event.getMessage().equalsIgnoreCase("!advertise")) {
 			advertise(event);
 		}
 
 		else if (event.getMessage().toLowerCase().contains("!xcom")) {
 			event.getBot().sendMessage(event.getChannel(), "Morphyum plays XCOM on Classic Difficulty in Ironman mode!");
 			Thread.sleep(1000);
 			event.getBot().sendMessage(event.getChannel(), "Join the Squad!");
 			Thread.sleep(1000);
 			event.getBot().sendMessage(event.getChannel(),
 					"https://docs.google.com/spreadsheet/ccc?key=0AkFJymkKRhIzdHhIWWVaX0xpX3FTNTVFS1pFMFVXRnc&usp=sharing");
 
 		}
 
 		else if (event.getMessage().toLowerCase().contains("!orb")) {
 			if (event.getMessage().equalsIgnoreCase("!orb")) {
 				event.getBot().sendMessage(event.getChannel(), "OrbOrbOrbOrbOrb!");
 			} else {
 				orbcount(event);
 			}
 		}
 
 		else if (event.getMessage().toLowerCase().contains("!greet")) {
 			greeting(event);
 		}
 
 		else if (event.getMessage().toLowerCase().contains("!leave")) {
 			if (event.getChannel().isOp(event.getUser()) || event.getUser().getNick().equalsIgnoreCase("morphyum")) {
 				event.getBot().sendMessage(event.getChannel(), "Fine i leave :(");
 				event.getBot().partChannel(event.getChannel());
 			}
 		}
 		Thread.sleep(3000);
 	}
 
 	private static String getYoutube(String link) {
 		String id = link.substring(31);
 		String video = getHTML("https://www.googleapis.com/youtube/v3/videos?part=snippet&id=" + id + "&key=AIzaSyDOqZtT01vX5FyWTPzalHIfq-wbOXIju2w");
 		JSONObject jsonobject = new JSONObject(video);
 		JSONArray items = jsonobject.getJSONArray("items");
 		jsonobject = items.getJSONObject(0).getJSONObject("snippet");
 		String response = "Oh thanks! A link to the video: " + jsonobject.getString("title") + " uploaded by " + jsonobject.getString("channelTitle");
 		return response;
 
 	}
 
 	private void season(MessageEvent event) {
 		String leaderboard = getHTML("http://api.speedrunslive.com/seasongoal/4?callback=renderLeaderboard");
 		leaderboard = leaderboard.substring(18, leaderboard.length() - 1);
 		JSONObject jsonobj = new JSONObject(leaderboard);
 		jsonobj = jsonobj.getJSONObject("season_goal");
 		JSONArray jsonarray = jsonobj.getJSONArray("leaders");
 		String leader = "";
 		for (int i = 0; i < 10; i++) {
 			leader += "#" + (i + 1) + " " + jsonarray.getJSONObject(i).getString("name") + " " + jsonarray.getJSONObject(i).getInt("trueskill") + " --- ";
 		}
 		event.getBot().sendMessage(event.getChannel(), leader);
 	}
 
 	private void seasonsearch(MessageEvent event, String name) {
 		String leaderboard = getHTML("http://api.speedrunslive.com/seasongoal/4?callback=renderLeaderboard");
 		leaderboard = leaderboard.substring(18, leaderboard.length() - 1);
 		JSONObject jsonobj = new JSONObject(leaderboard);
 		jsonobj = jsonobj.getJSONObject("season_goal");
 		JSONArray jsonarray = jsonobj.getJSONArray("leaders");
 		Boolean match = false;
 		for (int i = 0; i < jsonarray.length(); i++) {
 			if (jsonarray.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
 				event.getBot().sendMessage(
 						event.getChannel(),
 						name + " is currently ranked " + "#" + jsonarray.getJSONObject(i).getInt("rank") + " with "
 								+ jsonarray.getJSONObject(i).getInt("trueskill") + " Points");
 				match = true;
 			}
 		}
 		if (!match) {
 			event.getBot().sendMessage(event.getChannel(), name + " is not ranked yet.");
 		}
 		match = false;
 
 	}
 
 	private void greeting(MessageEvent event) {
 
 		if (event.getChannel().isOp(event.getUser())) {
 			if (event.getChannel().getName().contentEquals("#morphyum")) {
 				if (event.getMessage().equalsIgnoreCase("!greet on")) {
 					greetmorphy = true;
 					event.getBot().sendMessage(event.getChannel(), "Greeting activated");
 				} else if (event.getMessage().equalsIgnoreCase("!greet off")) {
 					greetmorphy = false;
 					event.getBot().sendMessage(event.getChannel(), "Greeting deactivated");
 				}
 			} else if (event.getChannel().getName().contentEquals("#artegaomega")) {
 				if (event.getMessage().equalsIgnoreCase("!greet on")) {
 					greetarte = true;
 					event.getBot().sendMessage(event.getChannel(), "Greeting activated");
 				} else if (event.getMessage().equalsIgnoreCase("!greet off")) {
 					greetarte = false;
 					event.getBot().sendMessage(event.getChannel(), "Greeting deactivated");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(),
 						"Sadly the greeting isn't available in this channel, if u own this channel please contact Morphyum to make it available");
 			}
 		}
 
 	}
 
 	private void advertise(MessageEvent event) {
 		if (event.getChannel().getName().contentEquals("#morphyum")) {
 			event.getBot().sendMessage(event.getChannel(), "Morphyum's Youtube Channel is http://www.youtube.com/TheMorphyum");
 			event.getBot().sendMessage(event.getChannel(), "Morphyum's Facebook Site is http://www.facebook.com/TheMorphyum");
 			event.getBot().sendMessage(event.getChannel(), "Morphyum's Twitter Page is https://twitter.com/morphyum");
 		} else {
 			event.getBot().sendMessage(event.getChannel(), "I never heared of another page owned by this streamer. If he has one tell Morphyum about it!");
 		}
 
 	}
 
 	private void capes(MessageEvent event) {
 		if (event.getChannel().getName().contentEquals("#morphyum")) {
 			if (event.getChannel().isOp(event.getUser())) {
 				if (event.getMessage().equalsIgnoreCase("!capes reset")) {
 					this.capesmorphy = 0;
 					event.getBot().sendMessage(event.getChannel(), "Cape Counter reset!");
 				} else if (event.getMessage().equalsIgnoreCase("!capes +")) {
 					capesmorphy++;
 					event.getBot().sendMessage(event.getChannel(), "Morphyum lost " + capesmorphy + " Capes in this Run!");
 				}
 
 				else if (event.getMessage().equalsIgnoreCase("!capes -")) {
 					if (capesmorphy != 0) {
 						capesmorphy--;
 						event.getBot().sendMessage(event.getChannel(), "Morphyum lost " + capesmorphy + " Capes in this Run!");
 					} else {
 						event.getBot().sendMessage(event.getChannel(), "-1 Capes? that doesnt make any sense, lets stop at 0 Kappa");
 					}
 				} else {
 					event.getBot().sendMessage(event.getChannel(),
 							"Morphyum lost " + capesmorphy + " Capes in this Run! To increase or decrease number type !capes [+/-]");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(), "Morphyum lost " + capesmorphy + " Capes in this Run!");
 			}
 		} else if (event.getChannel().getName().contentEquals("#dethwing")) {
 			if (event.getChannel().isOp(event.getUser())) {
 				if (event.getMessage().equalsIgnoreCase("!capes reset")) {
 					this.capesdeth = 0;
 					event.getBot().sendMessage(event.getChannel(), "Cape Counter reset!");
 				} else if (event.getMessage().equalsIgnoreCase("!capes +")) {
 					capesdeth++;
 					event.getBot().sendMessage(event.getChannel(), "Dethwing lost " + capesdeth + " Capes in this Run!");
 				}
 
 				else if (event.getMessage().equalsIgnoreCase("!capes -")) {
 					if (capesdeth != 0) {
 						capesdeth--;
 						event.getBot().sendMessage(event.getChannel(), "Dethwing lost " + capesdeth + " Capes in this Run!");
 					} else {
 						event.getBot().sendMessage(event.getChannel(), "-1 capes? that doesnt make any sense, lets stop at 0 Kappa");
 					}
 				} else {
 					event.getBot().sendMessage(event.getChannel(),
 							"Dethwing lost " + capesdeth + " Capes in this Run! To increase or decrease number type !capes [+/-]");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(), "Dethwing lost " + capesdeth + " Capes in this Run!");
 			}
 		} else if (event.getChannel().getName().contentEquals("#artegaomega")) {
 			if (event.getChannel().isOp(event.getUser())) {
 				if (event.getMessage().equalsIgnoreCase("!capes reset")) {
 					this.capesarte = 0;
 					event.getBot().sendMessage(event.getChannel(), "Cape Counter reset!");
 				} else if (event.getMessage().equalsIgnoreCase("!capes +")) {
 					capesarte++;
 					event.getBot().sendMessage(event.getChannel(), "Artega lost " + capesarte + " Capes in this Run!");
 				}
 
 				else if (event.getMessage().equalsIgnoreCase("!capes -")) {
 					if (capesarte != 0) {
 						capesarte--;
 						event.getBot().sendMessage(event.getChannel(), "Artega lost " + capesarte + " Capes in this Run!");
 					} else {
 						event.getBot().sendMessage(event.getChannel(), "-1 capes? that doesnt make any sense, lets stop at 0 Kappa");
 					}
 				} else {
 					event.getBot().sendMessage(event.getChannel(),
 							"Artega lost " + capesarte + " Capes in this Run! To increase or decrease number type !capes [+/-]");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(), "Artega lost " + capesdeth + " Capes in this Run!");
 			}
 		} else {
 			event.getBot().sendMessage(event.getChannel(),
 					"Sadly the cape counter isn't available in this channel, if u own this channel please contact Morphyum to make it available");
 		}
 
 	}
 
 	private void soldiers(MessageEvent event) {
 		if (event.getChannel().getName().contentEquals("#morphyum")) {
 			if (event.getChannel().isOp(event.getUser())) {
 				if (event.getMessage().equalsIgnoreCase("!soldiers reset")) {
 					this.soldiersmorphy = 0;
 					event.getBot().sendMessage(event.getChannel(), "Soldier Counter reset!");
 				} else if (event.getMessage().equalsIgnoreCase("!soldiers +")) {
					capesmorphy++;
 					event.getBot().sendMessage(event.getChannel(), "Morphyum lost " + soldiersmorphy + " Soldiers in this Run!");
 				}
 
 				else if (event.getMessage().equalsIgnoreCase("!soldiers -")) {
 					if (soldiersmorphy != 0) {
 						soldiersmorphy--;
 						event.getBot().sendMessage(event.getChannel(), "Morphyum lost " + soldiersmorphy + " Soldiers in this Run!");
 					} else {
 						event.getBot().sendMessage(event.getChannel(), "-1 soldiers? that doesnt make any sense, lets stop at 0 Kappa");
 					}
 				} else {
 					event.getBot().sendMessage(event.getChannel(),
 							"Morphyum lost " + soldiersmorphy + " Soldiers in this Run! To increase or decrease number type !capes [+/-]");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(), "Morphyum lost " + soldiersmorphy + " Soldiers in this Run!");
 			}
 		} else {
 			event.getBot().sendMessage(event.getChannel(),
 					"Sadly the soldier counter isn't available in this channel, if u own this channel please contact Morphyum to make it available");
 		}
 
 	}
 
 	private void bonks(MessageEvent event) {
 		if (event.getChannel().getName().contentEquals("#morphyum")) {
 			if (event.getChannel().isOp(event.getUser())) {
 				if (event.getMessage().equalsIgnoreCase("!bonks reset")) {
 					this.bonksmorphy = 0;
 					event.getBot().sendMessage(event.getChannel(), "BONK Counter reset!");
 				} else if (event.getMessage().equalsIgnoreCase("!bonks +")) {
 					bonksmorphy++;
 					event.getBot().sendMessage(event.getChannel(), "Morphyum BONKed " + bonksmorphy + " times in this Run!");
 				}
 
 				else if (event.getMessage().equalsIgnoreCase("!bonks -")) {
 					if (bonksmorphy != 0) {
 						bonksmorphy--;
 						event.getBot().sendMessage(event.getChannel(), "Morphyum BONKed " + bonksmorphy + " times in this Run!");
 					} else {
 						event.getBot().sendMessage(event.getChannel(), "-1 BONKs? that doesnt make any sense, lets stop at 0 Kappa");
 					}
 				} else {
 					event.getBot().sendMessage(event.getChannel(),
 							"Morphyum BONKed " + bonksmorphy + " times in this Run! To increase or decrease number type !bonks [+/-]");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(), "Morphyum BONKed " + bonksmorphy + " times in this Run!");
 			}
 		} else {
 			event.getBot().sendMessage(event.getChannel(),
 					"Sadly the bonk counter isn't available in this channel, if u own this channel please contact Morphyum to make it available");
 		}
 
 	}
 
 	private void orbcount(MessageEvent event) {
 		if (event.getChannel().getName().contentEquals("#artegaomega")) {
 			if (event.getMessage().equalsIgnoreCase("!orbcount")) {
 				event.getBot().sendMessage(event.getChannel(),
 						"ArtegaOmega got " + orbsgotarte + " Orbs this session and failed it " + orbsfailedarte + " times!");
 			}
 			if (event.getChannel().isOp(event.getUser())) {
 				if (event.getMessage().equalsIgnoreCase("!orbreset")) {
 					this.orbsfailedarte = 0;
 					this.orbsgotarte = 0;
 					event.getBot().sendMessage(event.getChannel(), "Orb Counter Counter reset!");
 				} else if (event.getMessage().equalsIgnoreCase("!orbgot")) {
 					orbsgotarte++;
 					event.getBot().sendMessage(event.getChannel(),
 							"ArtegaOmega got " + orbsgotarte + " Orbs this session and failed it " + orbsfailedarte + " times!");
 				} else if (event.getMessage().equalsIgnoreCase("!orbfailed")) {
 					orbsfailedarte++;
 					event.getBot().sendMessage(event.getChannel(),
 							"ArtegaOmega got " + orbsgotarte + " Orbs this session and failed it " + orbsfailedarte + " times!");
 
 				} else {
 					event.getBot().sendMessage(
 							event.getChannel(),
 							"ArtegaOmega got " + orbsgotarte + " Orbs this session and failed it " + orbsfailedarte
 									+ " times! To increase it use [!orbfailed/!orbgot] to reset it use [!orbreset]");
 				}
 			} else {
 				event.getBot().sendMessage(event.getChannel(),
 						"ArtegaOmega got " + orbsgotarte + " Orbs this session and failed it " + orbsfailedarte + " times!");
 			}
 		} else {
 			event.getBot().sendMessage(event.getChannel(),
 					"Sadly the orb counter isn't available in this channel, if u own this channel please contact Morphyum to make it available");
 		}
 
 	}
 
 	public static String getHTML(String urlToRead) {
 		URL url;
 		HttpURLConnection conn;
 		BufferedReader rd;
 		String line;
 		String result = "";
 		try {
 
 			url = new URL(urlToRead);
 			conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			while ((line = rd.readLine()) != null) {
 				result += line;
 			}
 			rd.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	private String getWR(String category) {
 		String name = "ArtegaOmega";
 		name = name.toLowerCase();
 
 		category = category.toLowerCase();
 
 		String categories = getHTML("http://www.deanyd.net/smw/api.php?action=parse&page=leaderboards&format=json&prop=sections");
 		JSONObject jsonobject2 = new JSONObject(categories);
 		jsonobject2 = jsonobject2.getJSONObject(("parse"));
 		JSONArray sections = jsonobject2.getJSONArray("sections");
 		boolean catfound = false;
 		for (int h = 0; h < sections.length(); h++) {
 			if (sections.getJSONObject(h).getString("line").toLowerCase().contentEquals(category)) {
 				catfound = true;
 				String section = sections.getJSONObject(h).getString("index");
 				String wikitext = getHTML("http://www.deanyd.net/smw/api.php?action=parse&page=leaderboards&format=json&section=" + section);
 				JSONObject jsonobject = new JSONObject(wikitext);
 				jsonobject = jsonobject.getJSONObject(("parse"));
 				jsonobject = jsonobject.getJSONObject(("text"));
 				wikitext = jsonobject.getString("*");
 				String[] wrtime = wikitext.split("<td>");
 				String[] wrname = wrtime[1].split(">");
 
 				return ("The World Record for " + category + " is " + wrtime[2].replace("</td>", "").trim() + " by " + wrname[1].replace("</a", "").trim());
 			}
 		}
 		if (!catfound) {
 			return ("Category " + category + " was not found");
 		}
 		catfound = false;
 		return "Error";
 	}
 
 	private String getPB(String category, String name) {
 
 		name = name.toLowerCase();
 
 		category = category.toLowerCase();
 
 		String categories = getHTML("http://www.deanyd.net/smw/api.php?action=parse&page=leaderboards&format=json&prop=sections");
 		JSONObject jsonobject2 = new JSONObject(categories);
 		jsonobject2 = jsonobject2.getJSONObject(("parse"));
 		JSONArray sections = jsonobject2.getJSONArray("sections");
 		boolean catfound = false;
 		for (int h = 0; h < sections.length(); h++) {
 			if (sections.getJSONObject(h).getString("line").toLowerCase().contentEquals(category)) {
 				catfound = true;
 				String section = sections.getJSONObject(h).getString("index");
 				String wikitext = getHTML("http://www.deanyd.net/smw/api.php?action=parse&page=leaderboards&format=json&section=" + section);
 				JSONObject jsonobject = new JSONObject(wikitext);
 				jsonobject = jsonobject.getJSONObject(("parse"));
 				jsonobject = jsonobject.getJSONObject(("text"));
 				wikitext = jsonobject.getString("*");
 				String[] pbtext = wikitext.split("<td>");
 				int ranking = -1;
 				boolean playerfound = false;
 				for (int i = 0; i < pbtext.length; i++) {
 					if (pbtext[i].contains("title")) {
 						String[] pbhelp = pbtext[i].split(">");
 						ranking++;
 
 						if (pbhelp[1].replace("</a", "").trim().toLowerCase().contentEquals(name)) {
 							playerfound = true;
 							return (pbhelp[1].replace("</a", "").trim() + " is currently ranked #" + ranking + " on the " + category
 									+ " Leaderboard with a time of " + pbtext[i + 1].replace("</td>", "").trim());
 
 						}
 					}
 
 				}
 				if (!playerfound) {
 					return (name + " was not found on the leaderboard for the category " + category);
 				}
 				playerfound = false;
 				break;
 			}
 		}
 		if (!catfound) {
 			return ("Category " + category + " was not found");
 		}
 		return "Error";
 
 	}
 
 }
