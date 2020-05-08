 //You may not release this source under any condition, it must be linked to this page
 //You may recompile and publish as long as skipperguy12 and Guru_Fraser are given credit
 //You may not claim this to be your own
 //You may not remove these comments
 package net.minecraft.src;
 
 import java.util.ArrayList;
 import java.math.*;
 
 import net.minecraft.client.Minecraft;
 
 public static class mod_Ares extends BaseMod {
   private static mod_Ares pa;
 
 	public boolean onPA = false;
 
 	private String[] FPS;
 	private String fps;
 
 	private int friendCount;
 	public static final String serverDomain = "oc.tc";
 	private ArrayList<String> friends;
 	protected double kills;
 	protected double deaths;
 	protected double killed;
 	public String map;
 	private String IP;
 	private String serverName;
 	public String[] wool;
 	public String[] redWool;
 	public String[] blueWool;
 	private String team;
 	private boolean showGUI;
 	protected int killStreak;
 	public static World world;
 	protected String username = "Not_Found";
 	protected Minecraft mc;
 	//public int height = mc.displayHeight;
 	//public int width = mc.displayWidth;
 	
 	AresCustomMethods Methods;
 
 	public KeyBinding o = new KeyBinding("togglegui", 24);
 
 
 	@MLProp(name="showFPS", info="true = Show FPS in Gui, false = Doesn't show it.")
 	public static String showFPS = "true";
 
 	@MLProp(name="showKills", info="true = Show Kills in Gui, false = Doesn't show it.")
 	public static String showKills = "true";
 
 	@MLProp(name="showDeaths", info="true = Show Deaths in Gui, false = Doesn't show it.")
 	public static String showDeaths = "true";
 
 	@MLProp(name="showKilled", info="true = Show Times killed via PVP not PVE in Gui, false = Doesn't show it")
 	public static String showKilled = "true";
 
 	@MLProp(name="showServer", info="true = Show what server you are on in Gui, false = Doesn't show it.")
 	public static String showServer = "true";
 
 	@MLProp(name="showTeam", info="true = Show what team your on in Gui, false = Doesn't show it.")
 	public static String showTeam = "true";
 
 	@MLProp(name="showKD", info="true = Show your Kill Death Ratio in Gui, false = Doesn't show it.")
 	public static String showKD = "true";
 
 	@MLProp(name="showKK", info="true = Show your Kill Killed Ratio in Gui, false = Doesn't show it.")
 	public static String showKK = "true";
 
 	@MLProp(name="showFriends", info="true = Show Friends Online in Gui, false = Doesn't show it.")
 	public static String showFriends = "true";
 
 	@MLProp(name="showMap", info="true = Show the current map you are playing in Gui, false = Doesn't show it.")
 	public static String showMap = "true";
 
 	@MLProp(name="showStreak", info="true = Show your kill streak in Gui, false = Doesn't show it.")
 	public static String showStreak = "true";
 	EntityPlayer player = null;
 	//What is this for? ^
 
 	// TODO: Check for captured wools
 
 
 	@Override
 	public String getVersion()
 	{
 		return "1.4.7";
 	}
 
 	@Override
 	public void load()
 	{
 		// TODO Auto-generated method stub
 		ModLoader.setInGUIHook(this, true, false);
 		ModLoader.setInGameHook(this, true, false);
 
 		this.friends = new ArrayList();
 		this.friendCount = 0;
 		this.wool = new String[0];
 		this.redWool = new String[] {"Not Obtained", "Not Obtained", "Not Obtained"};
 		this.blueWool = new String[] {"Not Obtained", "Not Obtained", "Not Obtained"};
 		this.showGUI = true;
 		this.killStreak = 0;
 		ModLoader.registerKey(this, o, false);
 		ModLoader.addLocalization("o","Toggle Gui");
 		//Will look at this Server data stuff when i get home
 		ServerData sd = new ServerData(null, this.serverDomain);
 		Methods = new AresCustomMethods();
 	}
 
 
 	public void clientChat(String var1)
 	{
 		Minecraft mc = ModLoader.getMinecraftInstance();
 		World world = mc.theWorld; //get the current world.
 		EntityPlayer player = mc.thePlayer; //get the player entity.
 		String message = StringUtils.stripControlCodes(var1);
 		// Minecraft.getMinecraft().thePlayer.addChatMessage("Hai");
 		if (this.onPA)
 		{
 			username = mc.thePlayer.username.toString();
 			if(username == null)
 			{
 				return;
 			}
 			if (!message.startsWith("<") && message.contains(" joined the game")) 
 			{
 				message = message.replace(" joined the game", "");
 
 				if (!this.friends.contains(message))
 				{
 					this.friends.add(message.toString());
 					++this.friendCount;
 					// this.mc.thePlayer.b("One of your friends just logged in, "
 					// + message + ". There are now " + this.friendCount +
 					// " online");
 					if(this.showFriends.toString().equals("true"))
 					{
 						player.addChatMessage("One of your friends just logged in, "
 								+ message
 								+ ". There are now "
 								+ this.friendCount + " online");
 					}
 
 				}
 			}
 
 			else if (!message.startsWith("<") && message.contains("You are currently on"))
 			{
 				this.serverName = message.replace("You are currently on ", "");
 			}
 
 			else if (!message.startsWith("<") && message.contains(" placed ") && message.contains("for the"))
 			{
 				message = message.replace("placed ", "");
 				message = message.replace("for the ", "");
 				message = message.replace(" WOOL", "WOOL");
 				this.wool = message.split(" ");
 
 				if (this.wool[2].contains("Red"))
 				{
 					if (this.redWool[0].equals("Not Obtained"))
 					{
 						this.redWool[0] = this.wool[1];
 					}
 					else if (!this.redWool[0].equals("Not Obtained") && this.redWool[1].contains("Not Obtained"))
 					{
 						this.redWool[1] = this.wool[1];
 					}
 					else
 					{
 						this.redWool[2] = this.wool[1];
 					}
 				}
 				else if (this.wool[2].contains("Blue"))
 				{
 					if (this.blueWool[0].equals("Not Obtained"))
 					{
 						this.blueWool[0] = this.wool[1];
 					}
 					else if (!this.blueWool[0].equals("Not Obtained") && this.blueWool[1].contains("Not Obtained"))
 					{
 						this.blueWool[1] = this.wool[1];
 					}
 					else
 					{			
 						this.blueWool[2] = this.wool[1];
 					}
 				}
 			}
 			else if (!message.startsWith("<") && message.contains("left the game"))
 			{
 				message = message.replace(" left the game", "");
 				if (this.friends.contains(message))
 				{
 					--this.friendCount;
 					this.friends.remove(message);
 					if(this.showFriends.toString().equals("true"))
 					{	
 						player.addChatMessage("One of your friends just left, " + message + ". There are now " + this.friendCount + " online");
 					}
 				}
 			}
			else if (message.startsWith("<") || !message.contains("was shot by " + username) && !message.contains("was blown up by " + username) && !message.contains("was slain by " + username))
 			{
 				if (!message.startsWith("<") && message.startsWith(username + " was"))
 				{
 					++this.killed;
 					++this.deaths;
 					Methods.endKillStreak();
 				}
 				else if (!message.startsWith("<") && message.startsWith(username))
 				{
 					Methods.endKillStreak();
 					++this.deaths;
 				}
 			}
			else if(message.startsWith("<") || message.contains("was shot by " + username) && message.contains("was blown up by " + username) && message.contains("was slain by " + username))
 			{
 				this.kills++;
 				this.killStreak++;
 			}
 			else if (!message.startsWith("<") && var1.contains("You joined the"))
 			{
 				this.kills = 0.0D;
 				this.killed = 0.0D;
 				this.deaths = 0.0D;
 				this.team = message.replace("You joined the ", "");
 			}
 
 			else if (!message.startsWith("<") && message.contains("Game Over!"))
 			{
 				player.addChatMessage("Your final stats!");
 				player.addChatMessage("Kills: " + this.kills);
 				player.addChatMessage("Deaths: " + this.deaths);
 				player.addChatMessage("K/D: " + Methods.getKD());
 				this.kills = 0.0D;
 				this.killed = 0.0D;
 				this.deaths = 0.0D;
 				this.team = "Observers";
 				this.redWool[0] = "Not Obtained";
 				this.redWool[1] = "Not Obtained";
 				this.redWool[2] = "Not Obtained";
 				this.blueWool[0] = "Not Obtained";
 				this.blueWool[1] = "Not Obtained";
 				this.blueWool[2] = "Not Obtained";
 			}
 			else if(!message.startsWith("<") && message.contains("Match starting in 25 seconds"))
 			{
 				Methods.getMap(mc);
 			}
 		}
 	}
 	
 	public boolean onTickInGame(float time, Minecraft mc)
 	{
 
 		world = mc.theWorld;
 
 		this.FPS = mc.debug.split(",");
 		this.fps = this.FPS[0];
 
 		// Minecraft.getMinecraft().thePlayer.addChatMessage("Hai");
 		// System.out.println(this.fps);
 
 		if(showGUI)
 		{
 			if(this.showFPS.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow(this.fps, 2, 2, 0xffff);
 			}
 		}
 
 		if (onPA == true && showGUI == true)
 		{
 
 			// Their on PA
 			// FPS Display:
 
 			// mc.fontRenderer.drawStringWithShadow("Your playing oc.tc!", 2,
 			// 10, 0xffff);
 
 
 			//Server display
 			if(this.showServer.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Server: \u00A76"
 						+ this.serverName, 2, 10, 16777215);	
 			}
 
 			//Team display
 			if(this.showTeam.toString().equals("true"))
 			{
 
 
 				if(this.team.equalsIgnoreCase("red team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x990000);	
 				}
 				else if(this.team.equalsIgnoreCase("blue team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x0033FF);	
 				}
 				else if(this.team.equalsIgnoreCase("purple team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x9933CC);	
 				}
 				else if(this.team.equalsIgnoreCase("cyan team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x00FFFF);	
 				}
 				else if(this.team.equalsIgnoreCase("lime team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x00FF00);	
 				}
 				else if(this.team.equalsIgnoreCase("yellow team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0xFFFF00);
 				}
 				else if(this.team.equalsIgnoreCase("orange team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x006600);
 				}
 				else if(this.team.equalsIgnoreCase("orange team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0xFF9900);
 				}
 				else if(this.team.equalsIgnoreCase("Observers"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ this.team, 2, 18, 0x00FFFF);
 				}
 			}
 			// Friend display:
 			if(this.showFriends.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Friends Online: \u00A73"
 						+ this.friendCount, 2, 34, 16777215);
 			}
 
 
 			// Map fetcher:
 			if(this.showMap.toString().equals("true"))
 			{
 				if (this.map != null)
 				{
 					mc.fontRenderer.drawStringWithShadow(
 							"Current Map: \u00A7d" + this.map, 2, 42, 16777215);
 				}
 				else
 				{
 					this.map = "Fetching...";
 					mc.fontRenderer.drawStringWithShadow(
 							"Current Map: \u00A78" + this.map, 2, 42, 16777215);
 				}
 			}
 			// Kills, deaths, K/D and KK
 			if(this.showKD.equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("K/D: \u00A73" + Methods.getKD(), 2, 58,
 						16777215);
 			}
 			if(this.showKK.equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("K/K: \u00A73" + Methods.getKK(), 2, 66,
 						16777215);
 			}
 
 
 			if(this.showKills.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Kills: \u00A7a" + this.kills, 2, 82,
 						16777215);
 			}
 			if(this.showDeaths.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Deaths: \u00A74" + this.deaths, 2, 90,
 						16777215);
 			}
 
 			if(this.showStreak.toString().equals("true"))
 			{
 
 				//Kill Streak display
 				mc.fontRenderer.drawStringWithShadow("Current Killstreak: \u00A75"
 						+ this.killStreak, 2, 98, 16777215);	
 			}
 			// mc.displayGuiScreen(new GuiStart(world,this, mc, this.fps));
 		}
 		return true;
 	}
 	
 	public void clientConnect(NetClientHandler var1)
 	{
 		this.team = "Observers";
 		System.out.println("Client successfully connected to "
 				+ var1.getNetManager().getSocketAddress().toString());
 
 		if (var1.getNetManager().getSocketAddress().toString().contains(serverDomain))
 		{
 			
 			this.showGUI = true;
 			// The domain contains oc.tc!!
 			System.out.println("Ares Mod has detected that this client is on an oc.tc domain! Mod has now been activated!");
 			// Their on PA now...
 			// world = minecraft.theWorld;
 			this.team = "Observers";
 			onPA = true;
 			if(var1.getNetManager().getSocketAddress().toString().contains("alpha"))
 				this.serverName="Alpha";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("beta"))
 				this.serverName="Beta";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("gamma"))
 				this.serverName="Gamma";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("delta"))
 				this.serverName="Delta";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("epsilon"))
 				this.serverName="Epsilon";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("theta"))
 				this.serverName="Theta";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("eta"))
 				this.serverName="Eta";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("iota"))
 				this.serverName="Iota";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("kappa"))
 				this.serverName="Kappa";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("lambda"))
 				this.serverName="Lambda";
 			else if(var1.getNetManager().getSocketAddress().toString().contains("nostalgia"))
 				this.serverName="Nostalgia";
 			// System.out.println(world + this.getName());
 		}
 		else
 		{
 			this.showGUI = false;
 		}
 
 	}
 
 
 	public void onDisconnect(NetClientHandler handler) 
 	{
 		this.team = "Observers";
 		if (onPA)
 			onPA = false;
 		this.showGUI = false;
 		this.team = "Observers";
 		Methods.endKillStreak();
 		this.map = "Attempting to fetch map...";
 	}
 	
 	public void keyboardEvent(KeyBinding keybinding)
 	{
 		Minecraft mc = ModLoader.getMinecraftInstance();
 		World world = mc.theWorld;
 		EntityPlayerSP player = mc.thePlayer;
 		if(!(mc.currentScreen instanceof GuiChat))
 		{
 			if (keybinding == o)
 			{
 				if(showGUI==true)
 				{
 					showGUI = false;
 				}
 				else
 				{
 					showGUI = true;
 				}
 			}
 		}
 	}
 }
