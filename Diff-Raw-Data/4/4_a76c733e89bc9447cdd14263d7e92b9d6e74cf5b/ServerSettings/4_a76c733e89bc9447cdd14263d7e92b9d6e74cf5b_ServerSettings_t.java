 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.stormfront.settings.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.settings.IHighlightString;
 import cc.warlock.core.configuration.ConfigurationUtil;
 import cc.warlock.core.script.ScriptEngineRegistry;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.settings.StormFrontServerSettings;
 import cc.warlock.core.stormfront.settings.skin.IStormFrontSkin;
 import cc.warlock.core.stormfront.xml.StormFrontDocument;
 import cc.warlock.core.stormfront.xml.StormFrontElement;
 
 @Deprecated
 public class ServerSettings implements Comparable<ServerSettings>
 {
 	public static final String WINDOW_MAIN = "smain";
 	public static final String WINDOW_INVENTORY = "sinv";
 	public static final String WINDOW_SPELLS = "sSpells";
 	public static final String WINDOW_DEATHS = "sdeath";
 	public static final String WINDOW_THOUGHTS = "sthoughts";
 	public static final String WINDOW_ROOM = "sroom";
 	public static final String WINDOW_NEWS = "snews";
 	public static final String WINDOW_CHAR_SHEET = "scharsheet";
 	public static final String WINDOW_FAMILIAR = "sfamiliar";
 	public static final String WINDOW_CONTAINER_STOW = "cstow";
 	
 	public static final String SETTING_UPDATE_PREFIX = "<stgupd>";
 	public static final String IGNORES_TEXT = "<<m><ignores disable=\"n\"></ignores><ignores disable=\"n\"></ignores></<m>";
 	
 	private IStormFrontClient client;
 	private String playerId, clientVersion, crc;
 	private int majorVersion;
 	private StormFrontDocument document;
 	protected Palette palette;
 	
 	protected HashMap<String, WindowSettings> windowSettings = new HashMap<String,WindowSettings>();
 	protected CommandLineSettings commandLineSettings;
 	protected HashMap<String, Preset> presets = new HashMap<String,Preset>();
 	protected ArrayList<HighlightPreset> highlightStrings = new ArrayList<HighlightPreset>();
 	protected HashMap<String, String> variables = new HashMap<String, String>();
 	protected ArrayList<ArrayList<MacroKey>> macroSets = new ArrayList<ArrayList<MacroKey>>();
 	protected HashMap<String, ServerScript> scripts = new HashMap<String, ServerScript>();
 	protected ArrayList<IgnoreSetting> ignores = new ArrayList<IgnoreSetting>();
 	
 	protected HashMap<HighlightPreset, HighlightPreset> deletedHighlightStrings = new HashMap<HighlightPreset, HighlightPreset>();
 	protected ArrayList<String> deletedVariables = new ArrayList<String>();
 	protected ArrayList<IServerSettingsListener> listeners = new ArrayList<IServerSettingsListener>();
 	
 	protected ServerScriptProvider scriptProvider;
 	
 	private StormFrontElement streamElement, paletteElement, presetsElement, stringsElement, namesElement, ignoresElement;
 	
 	public ServerSettings (IStormFrontClient client)
 	{
 		this.client = client;
 		
 		scriptProvider = new ServerScriptProvider(client);
 		ScriptEngineRegistry.addScriptProvider(scriptProvider);
 	}
 	
 	public static StormFrontDocument getDocument (String playerId)
 	{
 		try {
 			FileInputStream stream = new FileInputStream(ConfigurationUtil.getConfigurationFile("serverSettings_" + playerId + ".xml"));
 			
 			StormFrontDocument document = new StormFrontDocument(stream);
 			
 			stream.close();
 			return document;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static String getCRC (StormFrontDocument document)
 	{
 		if (document != null)
 		{
 			return document.getRootElement().attributeValue("crc");
 		}
 		return null;
 	}
 	
 	public static Integer getMajorVersion (StormFrontDocument document)
 	{
 		if (document != null)
 		{
 			String value = document.getRootElement().attributeValue("major");
 			if (value != null)
 			{
 				return Integer.parseInt(value);
 			}
 		}
 		return null;
 	}
 	
 	public void addServerSettingsListener (IServerSettingsListener listener)
 	{
 		listeners.add(listener);
 	}
 	
 	public void removeServerSettingsListener (IServerSettingsListener listener)
 	{
 		if (listeners.contains(listener))
 			listeners.remove(listener);
 	}
 	
 	public void load (String playerId, InputStream stream)
 	{
 		this.playerId = playerId;
 		
 		try {
 			document = new StormFrontDocument(stream);
 			
 			loadPalette();
 			
			String majorVersion = document.getRootElement().attributeValue("major");
			if (majorVersion != null)
				this.majorVersion = Integer.parseInt(majorVersion);
 			clientVersion = document.getRootElement().attributeValue("client");
 			crc = document.getRootElement().attributeValue("crc");
 			
 			commandLineSettings = new CommandLineSettings(this, document.getRootElement().element("cmdline"), palette);
 			loadWindowSettings();
 			loadPresets();
 			loadHighlightStrings();
 			loadVariables();
 			loadMacros();
 			loadScripts();
 			loadIgnores();
 			
 			// initalize before we call the viewers
 //			client.getStormFrontSkin().loadDefaultPresets(this, presets);
 			
 //			for (IWarlockClientViewer v : client.getViewers())
 //			{
 //				IStormFrontClientViewer viewer = (IStormFrontClientViewer) v;
 //				viewer.loadServerSettings(this);
 //			}
 			
 			stream.close();
 			incrementMajorVersion();
 			
 			for (IServerSettingsListener listener : listeners) {
 				try {
 					listener.serverSettingsLoaded(this);
 				} catch (Throwable t) { }
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadPalette ()
 	{
 		paletteElement = document.getRootElement().element("palette");
 		palette = new Palette(this, paletteElement);
 	}
 	
 	private void loadWindowSettings()
 	{
 		streamElement = document.getRootElement().element("stream");
 		
 		for (StormFrontElement wElement : streamElement.elements())
 		{
 			WindowSettings windowSettings = new WindowSettings(this, wElement, palette);
 			
 			// we take the first declaration as precedence (same as what stormfront does)
 			if (!this.windowSettings.containsKey(wElement.attributeValue("id")))
 			{
 				this.windowSettings.put(wElement.attributeValue("id"), windowSettings);
 			}
 		}
 	}
 	
 	private void loadPresets ()
 	{
 		presetsElement = document.getRootElement().element("presets");
 		if (presetsElement != null)
 		{
 			for (StormFrontElement pElement : presetsElement.elements())
 			{
 				String presetId = pElement.attributeValue("id");
 				
 				presets.put(presetId, new Preset(this, pElement, palette));
 			}
 		}
 	}
 	
 	private void loadHighlightStrings()
 	{
 		stringsElement = document.getRootElement().element("strings");
 		if (stringsElement != null)
 		{
 			for (StormFrontElement hElement : stringsElement.elements())
 			{
 				String text = hElement.attributeValue("text");
 				
 				if(text != null)
 					highlightStrings.add(new HighlightPreset(this, hElement, palette));
 			}
 		}
 		
 		namesElement = document.getRootElement().element("names");
 		if (namesElement != null)
 		{
 			for (StormFrontElement hElement : namesElement.elements())
 			{
 				String text = hElement.attributeValue("text");
 				
 				HighlightPreset highlight = new HighlightPreset(this, hElement, palette);
 				highlight.setIsName(true);
 				highlightStrings.add(highlight);
 			}
 		}
 	}
 	
 	private void loadVariables()
 	{
 		StormFrontElement varsElement = document.getRootElement().element("vars");
 		if (varsElement != null)
 		{
 			for (StormFrontElement varElement : varsElement.elements())
 			{
 				variables.put(varElement.attributeValue("name"), varElement.attributeValue("value"));
 			}
 		}
 	}
 	
 	private void loadMacros ()
 	{
 		StormFrontElement macrosElement = document.getRootElement().element("macros");
 		if (macrosElement != null)
 		{
 			for (StormFrontElement keysElement : macrosElement.elements())
 			{
 				ArrayList<MacroKey> keys = new ArrayList<MacroKey>();
 				macroSets.add(keys);
 				
 				for (StormFrontElement kElement : keysElement.elements())
 				{	
 					keys.add(new MacroKey(this, kElement.attributeValue("key"), kElement.attributeValue("action")));
 				}
 			}
 		}
 	}
 	
 	private void loadScripts ()
 	{
 		StormFrontElement scriptsElement = document.getRootElement().element("scripts");
 		if (scriptsElement != null)
 		{
 			for (StormFrontElement sElement : scriptsElement.elements())
 			{
 				if(sElement != null) {
 					String name = sElement.attributeValue("name");
 					if(name != null) {
 						ServerScript script = new ServerScript(this, sElement);
 						scripts.put(name, script);
 						
 						scriptProvider.scriptContentsUpdated(script);
 					}
 				}
 			}
 		}
 	}
 	
 	protected void loadIgnores ()
 	{
 		ignoresElement = document.getRootElement().element("ignores");
 		if (ignoresElement != null)
 		{
 			for (StormFrontElement hElement : ignoresElement.elements())
 			{
 				if (hElement != null)
 				{
 					ignores.add(new IgnoreSetting(this, hElement));
 				}
 			}
 		}
 	}
 	
 	protected void incrementMajorVersion ()
 	{
 		// Needed so our settings are validated by other Stormfront clients
 		
 		try {
 			client.getConnection().send(SETTING_UPDATE_PREFIX +
 					ServerSetting.UPDATE_PREFIX +
 					"<settings client=\"" + clientVersion + "\" major=\"" + majorVersion + "\"></settings>" +
 					"<settings client=\"" + clientVersion + "\" major=\"" + (++majorVersion) + "\"></settings>" +
 					ServerSetting.UPDATE_SUFFIX + "\n");
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Palette getPalette ()
 	{
 		return palette;
 	}
 	
 	public Collection<Preset> getPresets()
 	{
 		return presets.values();
 	}
 	
 	public Preset getPreset (String presetId)
 	{
 		return presets.get(presetId);
 	}
 	
 	public HighlightPreset getHighlightString (int index)
 	{
 		if (highlightStrings == null) return null;
 		
 		return highlightStrings.get(index);
 	}
 	
 	public IWarlockStyle getNamedStyle(String name) {
 		return presets.get(name).getStyle();
 	}
 	
 	public List<? extends IHighlightString> getHighlightStrings ()
 	{	
 		return highlightStrings;
 	}
 	
 	public Collection<HighlightPreset> getHighlightPresets() {
 		return highlightStrings;
 	}
 	
 	public void clearHighlightStrings ()
 	{
 		highlightStrings.clear();
 	}
 	
 	public void updateHighlightString (HighlightPreset string)
 	{
 		if (!highlightStrings.contains(string))
 		{
 			string.setNew(true);
 		}
 		
 		for (ListIterator<HighlightPreset> iter = highlightStrings.listIterator(); iter.hasNext(); )
 		{
 			HighlightPreset highlight = iter.next();
 			if (highlight.equals(string))
 			{
 				iter.remove();
 				iter.add(string);
 			}
 		}
 	}
 	
 	public void updatePreset (Preset preset)
 	{
 		presets.put(preset.getName(), preset);
 	}
 	
 	public void updateWindowSettings (WindowSettings settings)
 	{
 		windowSettings.put(settings.getId(), settings);
 	}
 	
 	public void deleteHighlightString (HighlightPreset string)
 	{
 		HighlightPreset toDelete =
 			string.getOriginalHighlightString() == null ? string : string.getOriginalHighlightString();
 		
 		if (highlightStrings.contains(toDelete))
 		{
 			int index = highlightStrings.indexOf(toDelete);
 			HighlightPreset next = null;
 			if (index < highlightStrings.size() - 1)
 			{
 				next = highlightStrings.get(index+1);
 			}
 			
 			
 			deletedHighlightStrings.put(toDelete, next);
 			highlightStrings.remove(toDelete);
 		}
 	}
 	
 	protected void saveHighlights(boolean saveNames)
 	{
 		StringBuffer stringsAddMarkup = new StringBuffer();
 		StringBuffer stringsUpdateMarkup = new StringBuffer();
 		StringBuffer stringsDeleteMarkup = new StringBuffer();
 		
 		String paletteMarkup = "";
 		if (palette.needsUpdate())
 		{
 			paletteMarkup = palette.toStormfrontMarkup();
 		}
 		
 		for (HighlightPreset string : highlightStrings)
 		{
 			if (string.isName() == saveNames) {
 				if (string.needsUpdate())
 				{
 					if (!string.isNew())
 					{
 						stringsUpdateMarkup.append(saveNames ?
 								HighlightPreset.NAMES_PREFIX :
 									HighlightPreset.STRINGS_PREFIX);
 						stringsUpdateMarkup.append(ServerSetting.UPDATE_PREFIX);
 						
 						if (string.getOriginalHighlightString() != null)
 							stringsUpdateMarkup.append(string.getOriginalHighlightString().toStormfrontMarkup());
 						
 						stringsUpdateMarkup.append(string.toStormfrontMarkup());
 						stringsUpdateMarkup.append(ServerSetting.UPDATE_SUFFIX);
 						stringsUpdateMarkup.append(saveNames ?
 								HighlightPreset.NAMES_SUFFIX :
 									HighlightPreset.STRINGS_SUFFIX);
 						
 						string.saveToDOM();
 						string.setNeedsUpdate(false);
 					} else {
 						stringsAddMarkup.append(string.toStormfrontAddMarkup());
 					}
 				}
 			}
 		}
 		
 		for (Map.Entry<HighlightPreset, HighlightPreset> entry: deletedHighlightStrings.entrySet())
 		{
 			HighlightPreset string = entry.getKey();
 			// don't send the delete command if it was re-added after it was marked for deletion
 			if (highlightStrings.contains(string)) continue;
 			
 			if (saveNames == string.isName()) {
 				String markup = string.toStormfrontMarkup();
 				String updateMarkup = ServerSetting.updateMarkup(markup+markup);
 				
 				stringsDeleteMarkup.append(string.surroundMarkup(updateMarkup));
 				stringsDeleteMarkup.append(string.surroundMarkup(updateMarkup));
 				stringsDeleteMarkup.append(string.surroundMarkup(string.deleteMarkup()));
 				
 				if (deletedHighlightStrings.get(string) != null)
 				{
 					HighlightPreset next = entry.getValue();
 					markup = next.toStormfrontMarkup();
 					updateMarkup = ServerSetting.updateMarkup(markup+markup);
 					
 					stringsDeleteMarkup.append(next.surroundMarkup(updateMarkup));
 				}
 				
 				string.deleteFromDOM();
 			}
 		}
 		
 		if (stringsDeleteMarkup.length() > 0)
 		{
 			sendSettingsUpdate(SETTING_UPDATE_PREFIX, stringsDeleteMarkup, IGNORES_TEXT + paletteMarkup);
 			
 			deletedHighlightStrings.clear();
 		}
 		
 		if (stringsAddMarkup.length() > 0)
 		{
 			sendSettingsUpdate(SETTING_UPDATE_PREFIX, stringsAddMarkup, IGNORES_TEXT + paletteMarkup);
 		}
 		
 		if (stringsUpdateMarkup.length() > 0)
 		{
 			sendSettingsUpdate(SETTING_UPDATE_PREFIX, stringsUpdateMarkup, IGNORES_TEXT + paletteMarkup);
 		}
 		
 		saveLocalXml();
 	}
 	
 	public void saveHighlightStrings ()
 	{
 		saveHighlights(false);
 	}
 	
 	public void saveHighlightNames ()
 	{
 		saveHighlights(true);
 	}
 	
 	public void savePresets ()
 	{
 		StringBuffer presetUpdateMarkup = new StringBuffer();
 		
 		for (Preset preset : presets.values())
 		{
 			if (preset.needsUpdate())
 			{
 				presetUpdateMarkup.append(preset.getOriginalPreset().toStormfrontMarkup());
 				presetUpdateMarkup.append(preset.toStormfrontMarkup());
 				preset.saveToDOM();
 				preset.setNeedsUpdate(false);
 			}
 		}
 				
 		if (presetUpdateMarkup.length() > 0)
 		{
 			sendSettingsUpdate(
 				SETTING_UPDATE_PREFIX +
 				Preset.STORMFRONT_MARKUP_PREFIX +
 				ServerSetting.UPDATE_PREFIX,
 				presetUpdateMarkup,
 				ServerSetting.UPDATE_SUFFIX +
 				Preset.STORMFRONT_MARKUP_SUFFIX);
 		}
 		
 		saveLocalXml();
 	}
 	
 	public void saveWindowSettings ()
 	{
 		StringBuffer windowUpdateMarkup = new StringBuffer();
 		
 		for (WindowSettings settings: windowSettings.values())
 		{
 			if (settings.needsUpdate())
 			{
 				settings.saveToDOM();
 				windowUpdateMarkup.append(settings.getOriginalWindowSettings().toStormfrontMarkup(false));
 				windowUpdateMarkup.append(settings.toStormfrontMarkup());
 				settings.setNeedsUpdate(false);
 			}
 		}
 		
 		String paletteMarkup = "";
 		if (palette.needsUpdate())
 		{
 //			palette.saveToDOM();
 			paletteMarkup = palette.toStormfrontMarkup();
 		}
 		
 		if (windowUpdateMarkup.length() > 0)
 		{
 			String updatePrefix =
 				SETTING_UPDATE_PREFIX +
 				WindowSettings.STORMFRONT_MARKUP_PREFIX +
 				ServerSetting.R_PREFIX;
 			
 			String updateSuffix =
 				ServerSetting.R_SUFFIX +
 				WindowSettings.STORMFRONT_MARKUP_SUFFIX +
 				paletteMarkup;
 			
 //			System.out.println(updatePrefix + windowUpdateMarkup + updateSuffix);
 			sendSettingsUpdate(updatePrefix, windowUpdateMarkup, updateSuffix);
 		}
 		
 		saveLocalXml();
 	}
 	
 	public void saveScript (ServerScript script)
 	{
 		// Just save locally and increment major version, forcing a server settings push to Simu on next connection
 		
 		StormFrontElement scriptElement = script.getElement();
 		scriptElement.setText(ServerScript.convertScriptToTokens(script.getScriptContents()));
 		
 		majorVersion++;
 		document.getRootElement().setAttribute("major", ""+majorVersion);
 		
 		saveLocalXml();
 	}
 	
 	protected void saveLocalXml ()
 	{
 		try {
 			FileWriter writer = new FileWriter(ConfigurationUtil.getConfigurationFile("serverSettings_" + playerId + ".xml"));
 			document.saveTo(writer, true);
 			writer.close();
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 		
 	private void sendSettingsUpdate (String prefix, StringBuffer markup, String suffix)
 	{
 		if (markup.length() > 0)
 		{
 			System.out.println("[test-settings-update]\n\n" + prefix + markup.toString() + suffix);
 			
 			try {
 				client.getConnection().send(prefix + markup.toString() + suffix + "\n");
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void sendAllSettings ()
 	{
 		StormFrontServerSettings.sendSettingsDocument(this.client, this.document);
 	}
 	
 	public Preset createPreset ()
 	{
 		return Preset.createPresetFromParent(this, presetsElement);
 	}
 	
 	public HighlightPreset createHighlightString (boolean isName)
 	{
 		HighlightPreset string = null;
 		if (isName)
 			string = HighlightPreset.createHighlightStringFromParent(this, namesElement);
 		else
 			string = HighlightPreset.createHighlightStringFromParent(this, stringsElement);
 		
 		string.setIsName(isName);
 		return string;
 	}
 	
 	public int compareTo(ServerSettings o) {
 		if (this == o) return 0;
 		return -1;
 	}
 	
 	public boolean containsVariable (String name)
 	{
 		return variables.containsKey(name);
 	}
 	
 	public String getVariable (String name)
 	{
 		return variables.get(name);
 	}
 	
 	public Collection<String> getVariableNames ()
 	{
 		return variables.keySet();
 	}
 	
 	public List<MacroKey> getMacroSet (int set)
 	{
 		if (macroSets.size() > set) {
 			return macroSets.get(set);
 		} else {
 			List<MacroKey> macros = Collections.emptyList();
 			return macros;
 		}
 	}
 	
 	public boolean containsServerScript (String scriptName)
 	{
 		return scripts.containsKey(scriptName);
 	}
 	
 	public ServerScript getServerScript (String scriptName)
 	{
 		return scripts.get(scriptName);
 	}
 	
 	public Collection<ServerScript> getAllServerScripts ()
 	{
 		return scripts.values();
 	}
 
 	public int getMajorVersion() {
 		return majorVersion;
 	}
 
 	public void setMajorVersion(int majorVersion) {
 		this.majorVersion = majorVersion;
 	}
 
 	public String getClientVersion() {
 		return clientVersion;
 	}
 
 	public void setClientVersion(String clientVersion) {
 		this.clientVersion = clientVersion;
 	}
 
 	public IStormFrontSkin getDefaultSkin() {
 		return client.getStormFrontSkin();
 	}
 	
 	public List<IgnoreSetting> getIgnores ()
 	{
 		return ignores;
 	}
 	
 	public Collection<WindowSettings> getAllWindowSettings ()
 	{
 		return windowSettings.values();
 	}
 	
 	public WindowSettings getWindowSettings (String windowId)
 	{
 		if (windowSettings.containsKey(windowId)) {
 			return windowSettings.get(windowId);
 		}
 		return null;
 	}
 	
 	public WindowSettings getMainWindowSettings ()
 	{
 		return getWindowSettings(WINDOW_MAIN);
 	}
 	
 	public WindowSettings getThoughtsWindowSettings ()
 	{
 		return getWindowSettings(WINDOW_THOUGHTS);
 	}
 	
 	public WindowSettings getDeathsWindowSettings ()
 	{
 		return getWindowSettings(WINDOW_DEATHS);
 	}
 	
 	public WindowSettings getInventoryWindowSettings ()
 	{
 		return getWindowSettings(WINDOW_INVENTORY);
 	}
 	
 	public WindowSettings getSpellsWindowSettings ()
 	{
 		return getWindowSettings(WINDOW_SPELLS);
 	}
 	
 	public CommandLineSettings getCommandLineSettings ()
 	{
 		return commandLineSettings;
 	}
 
 	public String getCrc() {
 		return crc;
 	}
 
 	public IStormFrontClient getClient() {
 		return client;
 	}
 }
