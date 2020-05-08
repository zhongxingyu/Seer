 package net.minecraft.src;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Properties;
 
 import ws.slide.minecraft.mod_IntelliChat.GuiSettings;
 import ws.slide.minecraft.mod_IntelliChat.ICChatCallback;
 import ws.slide.minecraft.mod_IntelliChat.ChatFilter;
 import ws.slide.minecraft.mod_IntelliChat.ChatFilterProfile;
 import ws.slide.minecraft.mod_IntelliChat.ChatTab;
 import ws.slide.minecraft.mod_IntelliChat.ChatTabProfile;
 import ws.slide.minecraft.mod_IntelliChat.CommandClear;
 import ws.slide.minecraft.mod_IntelliChat.CommandHighlight;
 import ws.slide.minecraft.mod_IntelliChat.CommandIgnore;
 import ws.slide.minecraft.mod_IntelliChat.CommandUnIgnore;
 import ws.slide.minecraft.mod_IntelliChat.Settings;
 import ws.slide.minecraft.mod_IntelliChat.SettingsHighlight;
 import ws.slide.minecraft.mod_IntelliChat.TabConfig;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.server.MinecraftServer;
 import org.lwjgl.opengl.GL11;
 
 public class mod_IntelliChat extends BaseMod
 {
 	private Minecraft mc;
 	private static mod_IntelliChat instance = null;
 	private Settings settings = new Settings();
 	private TabConfig tabConfig;
 	private int tabActive = 0;
     private List<List<ChatLine>> historyWrapped = new ArrayList<List<ChatLine>>();
     private byte chatHeightCurrent;
     private int chatWidthCurrent;
     private int scrollOffset = 0;
 
     public mod_IntelliChat()
     {
     	instance = this;
     }
 
 	public String getPriorities()
 	{
 		return "required-before:mod_ClientCommands";
 	}
 
 	public void load()
 	{
 		mc = ModLoader.getMinecraftInstance();
 		
 		loadSettings();
 		loadTabConfig(settings.getConfigDefault());
 
 		// Add chat callback
 		mod_ClientCommands.getInstance().addChatCallback(new ICChatCallback());
 
 		// Register new commands: ignore, clear
 		mod_ClientCommands.getInstance().registerCommand(new CommandIgnore());
 		mod_ClientCommands.getInstance().registerCommand(new CommandUnIgnore());
 		mod_ClientCommands.getInstance().registerCommand(new CommandClear());
 		mod_ClientCommands.getInstance().registerCommand(new CommandHighlight());
 
 		// Register localizations
 		ModLoader.addLocalization("ic.menu.retunToChat", "Return to Chat");
 		
 		// Register key binding for switching tabs
 	}
 
 	public void unload()
 	{
 		saveSettings();
 	}
 
 	public String getVersion()
 	{
 		return "v0.9 by slide23 for Minecraft v1.3.2";
 	}
 
 	public static mod_IntelliChat getInstance()
 	{
 		return instance;
 	}
 
 	public Settings getSettings() { return settings; }
 
     public void loadSettings()
     {
 		try {
 	        File fileSettings = new File(this.mc.getAppDir("minecraft"), "mods" + File.separator + "IntelliChat" + File.separator + "Settings.json");
 			InputStream inputStream = new FileInputStream(fileSettings);
 	        Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
 	        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
 			settings = gson.fromJson(new FileReader(fileSettings), Settings.class);
 			reader.close();
 
 			chatHeightCurrent = settings.getChatHeight();
 			chatWidthCurrent = settings.getChatWidth();
 		}
 		catch (Exception e)
 		{
 			settings = new Settings();
 			saveSettings();
 			saveTabConfig(createBaseConfig());
 		}
     }
 
     public void saveSettings()
     {
 		try {
 			File modPath = new File(this.mc.getAppDir("minecraft"), "mods" + File.separator + "IntelliChat");
 	        modPath.mkdirs();
 	        File fileSettings = new File(modPath + File.separator + "Settings.json");
 			OutputStream outputStream = new FileOutputStream(fileSettings);
 	        Writer writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
 	        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
 	        writer.write(gson.toJson(settings));
 	        writer.close();
 		}
 		catch (Exception e)
 		{
 			
 		}
     }
 
 	public TabConfig loadTabConfig(String name)
 	{
         Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
         TabConfig config = null;
 
 		try
 		{
 	        File fileConfig = new File(this.mc.getAppDir("minecraft"), "mods" + File.separator + "IntelliChat" + File.separator + "Configs" + File.separator + name + ".json");
 			InputStream inputStream = new FileInputStream(fileConfig);
 	        Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
 			config = gson.fromJson(reader, TabConfig.class); // new TypeToken<ICConfig>(){}.getType()
 			reader.close();
 
 			if (config != null)
 			{
 				for (ChatTab tab : config.getTabs())
 				{
 					for (ChatTabProfile tabProfile : config.getTabProfiles())
 					{
 						if (tab.getProfileName().equals(tabProfile.getName()))
 						{
 							tab.setProfile(tabProfile);
 							break;
 						}
 					}
 				}
 		
 				for (ChatFilterProfile filterProfile : config.getFilterProfiles())
 				{
 					for (ChatTabProfile tabProfile : config.getTabProfiles())
 					{
 						if (filterProfile.getProfileName() != null)
 						{
 							if (filterProfile.getProfileName().equals(tabProfile.getName()))
 							{
 								filterProfile.setProfile(tabProfile);
 								break;
 							}
 						}
 						else if (filterProfile.getProfile() != null && filterProfile.getProfile().getParentName() != null && filterProfile.getProfile().getParentName().equals(tabProfile.getName()))
 						{
 							filterProfile.getProfile().setParent(tabProfile);
 							break;
 						}
 					}
 
 					for (ChatFilter filter : config.getFilters())
 					{
 						if (filterProfile.getFilterName() != null && filterProfile.getFilterName().equals(filter.getName()))
 						{
 							filterProfile.setFilter(filter);
 							break;
 						}
 					}
 				}
 			}
 
 			tabConfig = config;
 	    	for (ChatTab tab : tabConfig.getTabs())
 	    		historyWrapped.add(tabConfig.getTabs().indexOf(tab), new ArrayList<ChatLine>());
 		}
 		catch (Exception e)
 		{
 			config = createBaseConfig();
 		}
 
 		return config;
 	}
 
     public void saveTabConfig(TabConfig config)
     {
 		try {
 			File modPath = new File(this.mc.getAppDir("minecraft"), "mods" + File.separator + "IntelliChat" + File.separator + "Configs");
 	        modPath.mkdirs();
 	        File file = new File(modPath + File.separator + "Default.json");
 			OutputStream outputStream = new FileOutputStream(file);
 	        Writer writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
 	        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
 	        writer.write(gson.toJson(config));
 	        writer.close();
 		}
 		catch (Exception e)
 		{
 			
 		}
     }
 
 	public TabConfig createBaseConfig()
 	{
 		TabConfig config = new TabConfig();
 
         ChatTabProfile tabProfileAll = new ChatTabProfile();
         tabProfileAll.setName("All Chat");
         config.getTabProfiles().addAll(Arrays.asList(tabProfileAll));
 
         ChatFilterProfile fpAll = new ChatFilterProfile(new ChatFilter("(.*)"), tabProfileAll);
         config.getFilterProfiles().addAll(Arrays.asList(fpAll));
 
         ChatTab tabAll = new ChatTab(tabProfileAll);
         config.getTabs().addAll(Arrays.asList(tabAll));
 
         return config;
 	}
 
     public ChatTab getActiveChatTab()
     {
     	return tabConfig.getTabs().get(tabActive);
     }
 
     public void doScroll(int par1)
     {
         this.scrollOffset += par1;
         int bufferSize = this.historyWrapped.get(this.tabActive).size();
 
        if (this.scrollOffset > bufferSize - chatHeightCurrent)
         {
            this.scrollOffset = bufferSize - chatHeightCurrent;
         }
 
         if (this.scrollOffset <= 0)
         {
             this.scrollOffset = 0;
         }
     }
 
     public Boolean displayChat(int par1)
     {
         if (this.mc.gameSettings.chatVisibility != 2 && tabConfig.getTabs().size() > 0)
         {
             ScaledResolution scaledSize = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
 
         	if (mod_IntelliChat.getInstance().getSettings().getExpandToFillScreen())
         	{
         		chatHeightCurrent = (byte) ((scaledSize.getScaledHeight() - 50 - 3) / (this.mc.fontRenderer.FONT_HEIGHT));
         		int chatWidthCurrentNew = scaledSize.getScaledWidth() - 9;
 
         		if (chatWidthCurrent != chatWidthCurrentNew)
         		{
         			chatWidthCurrent = chatWidthCurrentNew;
         			wrapTabsToWidth();
         		}
         	}
 
             boolean showGui = false;
             byte lineCountDisplay = settings.getChatOverlayHeight();
             if (mc.ingameGUI.getChatGUI().func_73760_d())
             {
             	lineCountDisplay = chatHeightCurrent;
             	showGui = true;
     			((GuiChat)this.mc.currentScreen).inputField.setMaxStringLength(tabConfig.getTabs().get(tabActive).getProfile().getInputLimit());
             }
 
             float backgroundColorDefault = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
             int backgroundColor = (int)((float)255 * backgroundColorDefault);
 
             if (showGui || mod_IntelliChat.getInstance().getSettings().getShowTabIndicators())
             {
             	int offset = 3;
             	int y = lineCountDisplay * -this.mc.fontRenderer.FONT_HEIGHT - 3;
 
 //            	System.out.println(mod_IntelliChat.getInstance().getSettings().getExpandToFillScreen());
             	if (mod_IntelliChat.getInstance().getSettings().getExpandToFillScreen() || (!showGui && mod_IntelliChat.getInstance().getSettings().getShowTabIndicators()))
             		y = -scaledSize.getScaledHeight() + 50;
 
                 // Draw ? button
             	int textWidth = this.mc.fontRenderer.getStringWidth("?");
             	Gui.drawRect(offset, y, offset + 1 + textWidth + 1, y + this.mc.fontRenderer.FONT_HEIGHT, backgroundColor / 2 << 24);
                 GL11.glEnable(GL11.GL_BLEND);
             	this.mc.fontRenderer.drawStringWithShadow("?", offset + 1, y + 1, 0xFFFFFF + (backgroundColor << 24));
 
             	if (mod_IntelliChat.getInstance().getSettings().isEnabled())
             	{
 	                // Draw Tabs
 	            	offset += offset + 1 + textWidth + 1;
 	            	for (ChatTab tab : tabConfig.getTabs())
 	            	{
 	            		String tabText = tab.getTitle();
 	            		if (tab.getProfile().getCloseable())
 	            			tabText += " \u00D7";
 	
 	            		textWidth = this.mc.fontRenderer.getStringWidth(tabText);
 	            		Gui.drawRect(offset, y, offset + 3 + textWidth + 3, y + this.mc.fontRenderer.FONT_HEIGHT, backgroundColor / 2 << 24);
 	                    GL11.glEnable(GL11.GL_BLEND);
 	
 	                    int tabColor = 0x666666;
 	                    if (tabConfig.getTabs().indexOf(tab) == tabActive)
 	                    	tabColor = 0xFFFFFF;
 	                    else if (tab.hasActivity())
 	                    	tabColor = 0x00CC00;
 	
 	                	this.mc.fontRenderer.drawStringWithShadow(tabText, offset + 3, y + 1, tabColor + (backgroundColor << 24));
 	
 	                	offset += this.mc.fontRenderer.getStringWidth(tabText) + 3 + 3 + 3;
 	            	}
             	}
             }
 
             if (mod_IntelliChat.getInstance().getSettings().isEnabled())
 	        {
 	            int lineNumber = 0;
 	            int lineAddedTime;
 	            List<ChatLine> buffer = historyWrapped.get(tabActive);
 	            // Draw Chat Buffer
 	            synchronized (historyWrapped.get(tabActive))
 	            {
 	            	for (lineNumber = 0; lineNumber + scrollOffset < buffer.size() && lineNumber < lineCountDisplay; lineNumber++)
 		            {
 		                ChatLine line = buffer.get(lineNumber + scrollOffset);
 		
 	            		byte x = 3;
 	                    int y = -lineNumber * this.mc.fontRenderer.FONT_HEIGHT;
 	                	if (showGui)
 	                		Gui.drawRect(x, y, x + chatWidthCurrent + 4, y + this.mc.fontRenderer.FONT_HEIGHT, backgroundColor / 2 << 24);
 	
 	                    lineAddedTime = par1 - line.getUpdatedCounter();
 	
 	                    if (lineAddedTime < 200 || showGui)
 	                    {
 	                        double var10 = (double)lineAddedTime / 200.0D;
 	                        var10 = 1.0D - var10;
 	                        var10 *= 10.0D;
 	
 	                        if (var10 < 0.0D)
 	                            var10 = 0.0D;
 	
 	                        if (var10 > 1.0D)
 	                            var10 = 1.0D;
 	
 	                        var10 *= var10;
 	                        backgroundColor = (int)(255.0D * var10);
 	
 	                        if (showGui)
 	                            backgroundColor = 255;
 	
 	                        backgroundColor = (int)((float)backgroundColor * backgroundColorDefault);
 	
 	                        if (backgroundColor > 3)
 	                        {
 	                        	if (!showGui)
 	                        		Gui.drawRect(x, y, x + chatWidthCurrent + 4, y + this.mc.fontRenderer.FONT_HEIGHT, backgroundColor / 2 << 24);
 	
 	                        	GL11.glEnable(GL11.GL_BLEND);
 	                            this.mc.fontRenderer.drawStringWithShadow(line.getChatLineString(), x, y + 1, 0xFFFFFF + (backgroundColor << 24));
 	                        }
 	                    }
 		            }
 	
 	                int bufferSize = historyWrapped.get(tabActive).size();
 		            // Draw Chat History Scroll bars
 		            if (showGui && bufferSize > 0 && bufferSize > chatHeightCurrent)
 		            {
 		                int lineHeight = this.mc.fontRenderer.FONT_HEIGHT;
 		                GL11.glTranslatef(0.0F, (float)lineHeight, 0.0F);
 		                int bufferHeight = bufferSize * lineHeight;
 		                int displayHeight = chatHeightCurrent * lineHeight;
 		                int y = scrollOffset * displayHeight / bufferSize;
 		                int var11 = displayHeight * displayHeight / bufferHeight;
 		
 		                if (bufferHeight >= displayHeight)
 		                {
 		                    backgroundColor = y > 0 ? 170 : 96;
 		                    int var18 = 13382451; // this.field_73769_e ? 13382451 : 3355562;
 		                    Gui.drawRect(0, -y, 2, -y - var11, var18 + (backgroundColor << 24));
 		                    Gui.drawRect(2, -y, 1, -y - var11, 13421772 + (backgroundColor << 24));
 		                }
 		            }
 	            }
 	        }
         }
 
     	return mod_IntelliChat.getInstance().getSettings().isEnabled();
     }
 
     private void wrapTabsToWidth()
     {
         synchronized (historyWrapped)
         {
 			historyWrapped = new ArrayList<List<ChatLine>>();
 			for (ChatTab tab : tabConfig.getTabs())
 			{
 				List<ChatLine> tabHistoryWrapped = new ArrayList<ChatLine>();
 	    		for (ChatLine line : tab.getHistory())
 	    		{
 	                String text = line.getChatLineString();
 	
 	                if (mod_IntelliChat.getInstance().getSettings().isTimestampEnabled())
 	                {
 	                	SimpleDateFormat timestampFormat = new SimpleDateFormat(mod_IntelliChat.getInstance().getSettings().getTimestampFormat());
 	                	String timestampColor = mod_IntelliChat.getInstance().getSettings().getTimestampColorAsString();
 	                	text = "" + timestampColor + timestampFormat.format(line.getTimestamp()) + "r " + text;
 	                }
 	
 	                if (!this.mc.gameSettings.chatColours)
 	                    text = StringUtils.stripControlCodes(text);
 	
 	                for (String lineText : (List<String>)Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, chatWidthCurrent))
 	                	tabHistoryWrapped.add(new ChatLine(line.getUpdatedCounter(), lineText, line.getChatLineID()));
 	    		}
 	    		historyWrapped.add(tabConfig.getTabs().indexOf(tab), tabHistoryWrapped);
 			}
         }
     }
 
     public void printChatMessage(String message)
     {
         boolean var3 = mc.ingameGUI.getChatGUI().func_73760_d();
         boolean var4 = true;
 
         for (ChatFilterProfile fp : tabConfig.getFilterProfiles())
         {
         	ChatFilter filter = fp.getFilter();
         	if (filter.matches(message))
         	{
         		Boolean ignore = false;
         		for (String ignoreValue : mod_IntelliChat.getInstance().getSettings().getIgnores())
         		{
         			String variableValue = filter.getVariables(message).get(filter.getIgnoreVariableName());
         			if (variableValue != null && variableValue.equals(ignoreValue))
         				return;
         		}
         	}
         }
 
         for (ChatFilterProfile fp : tabConfig.getFilterProfiles())
         {
         	ChatFilter filter = fp.getFilter();
         	if (filter.matches(message))
         	{
         		ChatTab tab = new ChatTab(fp.getProfile());
         		tab.updateVariables(filter.getVariables(message));
             	tab.addLine((filter.getOutput() == null) ? (message) : (filter.getOutput()));
 
         		if (tabConfig.getTabs().contains(tab))
         		{
         			tab = tabConfig.getTabs().get(tabConfig.getTabs().indexOf(tab));
             		tab.updateVariables(filter.getVariables(message));
                 	tab.addLine((filter.getOutput() == null) ? (message) : (filter.getOutput()));
         		}
         		else
         		{
         			tabConfig.getTabs().add(tab);
                     historyWrapped.add(tabConfig.getTabs().indexOf(tab), new ArrayList<ChatLine>());
                     if (tab.getProfile().getFocusOnOpen())
                 	{
         				getActiveChatTab().clearActivity();
                 		this.tabActive = tabConfig.getTabs().indexOf(tab);
                 	}
         		}
 
         		ChatLine line = tab.getHistory().get(0);
                 String text = line.getChatLineString();
 
                 if (mod_IntelliChat.getInstance().getSettings().isTimestampEnabled())
                 {
                 	SimpleDateFormat timestampFormat = new SimpleDateFormat(mod_IntelliChat.getInstance().getSettings().getTimestampFormat());
                 	String timestampColor = mod_IntelliChat.getInstance().getSettings().getTimestampColorAsString();
                 	text = "" + timestampColor + timestampFormat.format(line.getTimestamp()) + "r " + text;
                 }
 
                 if (!this.mc.gameSettings.chatColours)
                     text = StringUtils.stripControlCodes(text);
 
                 List<ChatLine> tabHistoryWrapped = historyWrapped.get(tabConfig.getTabs().indexOf(tab));
                 for (String lineText : (List<String>)Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, chatWidthCurrent))
                 	tabHistoryWrapped.add(0, new ChatLine(line.getUpdatedCounter(), lineText, line.getChatLineID()));
 
             	if (this.tabActive == -1 || tab.getProfile().getFocusOnActivity())
             	{
             		getActiveChatTab().clearActivity();
             		this.tabActive = tabConfig.getTabs().indexOf(tab);
             	}
 
         		if (filter.getConsume())
         			break;
         	}
         }
     }
 
     public ChatClickData doClick(int clickX, int clickY)
     {
         if (mc.ingameGUI.getChatGUI().func_73760_d() & tabConfig.getTabs().size() >= 0)
         {
             ScaledResolution scaledSize = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
             int scaleFactor = scaledSize.getScaleFactor();
             int scaledX = clickX / scaleFactor - 3;
             int scaledY = clickY / scaleFactor - 40;
             boolean showGui = mc.ingameGUI.getChatGUI().func_73760_d();
 
         	int offset = 3;
 
     		int x1 = offset - 2;
     		int x2 = offset + 1 + this.mc.fontRenderer.getStringWidth("?") + 1;
 
             byte lineCountDisplay = chatHeightCurrent;
         	int y = lineCountDisplay * this.mc.fontRenderer.FONT_HEIGHT + 3;
 
         	if (mod_IntelliChat.getInstance().getSettings().getExpandToFillScreen() || (!showGui && mod_IntelliChat.getInstance().getSettings().getShowTabIndicators()))
         		y = scaledSize.getScaledHeight() - 50 - 2;
 
     		if (x1 < scaledX && scaledX < x2 && y < scaledY && scaledY < y + this.mc.fontRenderer.FONT_HEIGHT)
     		{
     			ModLoader.openGUI(this.mc.thePlayer, new GuiSettings());
     			return null;
     		}
 
     		if (mod_IntelliChat.getInstance().getSettings().isEnabled())
     		{
 	            ListIterator tabIterator = tabConfig.getTabs().listIterator();
 	        	while (tabIterator.hasNext())
 	        	{
 	        		int tabIndex = tabIterator.nextIndex();
 	        		ChatTab tab = (ChatTab) tabIterator.next();
 	
 	        		String tabText = tab.getTitle();
 	        		if (tab.getProfile().getCloseable())
 	        			tabText += " \u00D7";
 	
 	        		int textWidth = this.mc.fontRenderer.getStringWidth(tabText);
 	        		x1 = offset;
 	        		x2 = offset + 3 + textWidth + 3;
 	
 	
 	        		if (x1 < scaledX && scaledX < x2 && y < scaledY && scaledY < y + this.mc.fontRenderer.FONT_HEIGHT)
 	        		{
 	        			if (tab.getProfile().getCloseable() && (x2 - this.mc.fontRenderer.getStringWidth(" \u00D7") - 6) < scaledX && scaledX < x2)
 	        			{
 	        				tabIterator.remove();
 	        				historyWrapped.remove(tabIndex);
 	        				if (this.tabActive == tabIndex)
 	        					Math.min(this.tabActive--, 0);
 	        			}
 	        			else
 	        			{
 	        				getActiveChatTab().clearActivity();
 	        				this.tabActive = tabConfig.getTabs().indexOf(tab);
 	        			}
 	        		}
 	
 	            	offset = x2 + 3;
 	        	}
 	
 	            int var7 = Math.min(20, historyWrapped.get(tabActive).size());
 	            if (0 <= scaledX && scaledX <= 320 && 0 <= scaledY && scaledY < this.mc.fontRenderer.FONT_HEIGHT * var7 + var7)
 	            {
 	                int var8 = scaledY / (this.mc.fontRenderer.FONT_HEIGHT); // + this.field_73768_d;
 	                if (historyWrapped.get(tabActive).size() < var8)
 	                	return new ChatClickData(this.mc.fontRenderer, historyWrapped.get(tabActive).get(var8), scaledX, scaledY - (var8 - mc.ingameGUI.getChatGUI().getScrollOffset()) * this.mc.fontRenderer.FONT_HEIGHT + var8);
 	            }
     		}
         }
         return null;
     }
 }
