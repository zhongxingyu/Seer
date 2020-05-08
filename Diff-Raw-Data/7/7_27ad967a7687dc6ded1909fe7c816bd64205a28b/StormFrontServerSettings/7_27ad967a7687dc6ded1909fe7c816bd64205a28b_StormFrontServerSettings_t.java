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
 package cc.warlock.core.stormfront.settings;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import cc.warlock.core.client.IWarlockClientViewer;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockColor;
 import cc.warlock.core.client.WarlockFont;
 import cc.warlock.core.client.internal.WarlockStyle;
 import cc.warlock.core.client.settings.IHighlightString;
 import cc.warlock.core.client.settings.internal.ClientConfigurationProvider;
 import cc.warlock.core.client.settings.internal.ClientSettings;
 import cc.warlock.core.client.settings.internal.HighlightString;
 import cc.warlock.core.client.settings.internal.Ignore;
 import cc.warlock.core.client.settings.internal.Variable;
 import cc.warlock.core.client.settings.internal.WindowSettings;
 import cc.warlock.core.configuration.ConfigurationUtil;
 import cc.warlock.core.configuration.WarlockConfiguration;
 import cc.warlock.core.script.configuration.ScriptConfiguration;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.client.IStormFrontClientViewer;
 import cc.warlock.core.stormfront.client.StormFrontColor;
 import cc.warlock.core.stormfront.settings.internal.StormFrontClientSettings;
 import cc.warlock.core.stormfront.settings.server.HighlightPreset;
 import cc.warlock.core.stormfront.settings.server.IgnoreSetting;
 import cc.warlock.core.stormfront.settings.server.MacroKey;
 import cc.warlock.core.stormfront.settings.server.Preset;
 import cc.warlock.core.stormfront.settings.server.ServerScript;
 import cc.warlock.core.stormfront.settings.server.ServerSettings;
 import cc.warlock.core.stormfront.xml.StormFrontDocument;
 import cc.warlock.core.stormfront.xml.StormFrontElement;
 
 
 /**
  * The purpose of this class is to import/export (and eventually merge) StormFront's server settings
  * 
  * We keep a log of the imported settings version as well as the CRC so we can merge correctly later.
  * 
  * @author marshall
  *
  */
 @SuppressWarnings("deprecation")
 public class StormFrontServerSettings extends ClientConfigurationProvider {
 	
 	protected String clientVersion, majorVersion, crc;
 	protected IStormFrontMacroImporter macroImporter;
 	
 	public StormFrontServerSettings ()
 	{
 		super("stormfront-settings");
 	}
 	
 	public static String readStream (InputStream stream)
 	{
 		try {
 			InputStreamReader reader = new InputStreamReader(stream);
 			
 			char chunk[] = new char[1024];
 			StringBuffer buffer = new StringBuffer();
 			
 			while (reader.ready())
 			{
 				int read = reader.read(chunk);
 				buffer.append(chunk, 0, read);
 			}
 			stream.close();
 			
 			return buffer.toString();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static StormFrontDocument getInitialServerSettings()
 	{
 		try {
 			URL initialServerSettingsURL = StormFrontServerSettings.class.getClassLoader().getResource(
 				"cc/warlock/core/stormfront/settings/initialServerSettings.xml");
 			InputStream stream = initialServerSettingsURL.openStream();
 			
 			StormFrontDocument document = new StormFrontDocument(stream);
 			stream.close();
 			
 			return document;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static void sendInitialServerSettings (IStormFrontClient client)
 	{
 		StormFrontDocument initialDoc = getInitialServerSettings();
 		sendSettingsDocument(client, initialDoc);
 	}
 	
 	public static void sendInitialStreamWindows (IStormFrontClient client)
 	{
 		try {
 			URL initialStreamWindowsURL = StormFrontServerSettings.class.getClassLoader().getResource(
 				"cc/warlock/core/stormfront/settings/initialStreamWindows.xml");
 			InputStream stream = initialStreamWindowsURL.openStream();
 			
 			String initialStreamWindows = readStream(stream);
 			initialStreamWindows += "\n";
 			
 			client.getConnection().send(initialStreamWindows);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void sendSettingsDocument (IStormFrontClient client, StormFrontDocument document)
 	{
 		StormFrontElement settingsElement = document.getRootElement();
 		if (settingsElement.attribute("crc") != null)
 			settingsElement.removeAttribute("crc");
 		
 		String settingsDoc = settingsElement.toXML("", false, true);
 		settingsDoc = "<c>\n<db>" + settingsDoc + "\n";
 		
 		try {
 			client.getConnection().send(settingsDoc);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void importServerSettings (InputStream stream, StormFrontClientSettings settings)
 	{
 		ServerSettings serverSettings = new ServerSettings(settings.getStormFrontClient());
 		serverSettings.load(settings.getStormFrontClient().getPlayerId().get(), stream);
 		
 		settings.addChildProvider(this);
 		settings.addClientSettingProvider(this);
 		
 		this.clientVersion = serverSettings.getClientVersion();
 		this.majorVersion = ""+serverSettings.getMajorVersion();
 		this.crc = serverSettings.getCrc();
 		
 		for (IHighlightString string : serverSettings.getHighlightStrings())
 		{
 			importHighlightString((HighlightPreset)string, settings);
 		}
 		
 		for (Preset preset : serverSettings.getPresets())
 		{
 			importPreset(preset, settings);
 		}
 		
 		importCommandLineSettings(serverSettings.getCommandLineSettings(), settings);
 		
 		for (cc.warlock.core.stormfront.settings.server.WindowSettings wSettings : serverSettings.getAllWindowSettings())
 		{
 			importWindowSettings(wSettings, settings);
 		}
 		
 		for (ServerScript script : serverSettings.getAllServerScripts())
 		{
 			importScript(script, settings);
 		}
 		
 		for (String varName : serverSettings.getVariableNames())
 		{
 			importVariable(varName, serverSettings.getVariable(varName), settings);
 		}
 		
 		for (IgnoreSetting ignore : serverSettings.getIgnores())
 		{
 			importIgnore(ignore, settings);
 		}
 		
 		for (MacroKey macro : serverSettings.getMacroSet(0))
 		{
 			importMacro(macro, settings);
 		}
 		
 		WarlockConfiguration.getWarlockConfiguration(ClientSettings.CLIENT_SETTINGS).save();
 		
 		for (IWarlockClientViewer viewer : settings.getStormFrontClient().getViewers())
 		{
 			if (viewer instanceof IStormFrontClientViewer)
 			{
 				IStormFrontClientViewer sfViewer = (IStormFrontClientViewer) viewer;
 				sfViewer.loadClientSettings(settings);
 			}
 		}
 	}
 	
 	protected WarlockColor stormfrontColorToWarlockColor (StormFrontColor color)
 	{
 		if (color.equals(StormFrontColor.DEFAULT_COLOR) || color.isSkinColor()) return new WarlockColor(WarlockColor.DEFAULT_COLOR);
 		else return new WarlockColor(color.toHexString());
 	}
 	
 	protected void importHighlightString (HighlightPreset string, StormFrontClientSettings settings)
 	{
 		Pattern pattern = string.getPattern();
 		IWarlockStyle style = new WarlockStyle();
 		style.setBackgroundColor(stormfrontColorToWarlockColor(string.getBackgroundColor()));
 		style.setForegroundColor(stormfrontColorToWarlockColor(string.getForegroundColor()));
 		style.setFullLine(string.isFillEntireLine());
		style.setSound(string.getSound());
 		
 		try {
 			HighlightString newString = new HighlightString(
 					settings.getHighlightConfigurationProvider(), pattern.pattern(), true, true, true, style);
 			settings.getHighlightConfigurationProvider().addHighlightString(newString);
 		} catch(PatternSyntaxException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void importPreset (Preset preset, StormFrontClientSettings settings)
 	{
 		WarlockStyle style = new WarlockStyle();
 		style.setName(preset.getName());
 		style.setBackgroundColor(stormfrontColorToWarlockColor(preset.getBackgroundColor()));
 		style.setForegroundColor(stormfrontColorToWarlockColor(preset.getForegroundColor()));
 		style.setFullLine(preset.isFillEntireLine());
 		
 		if (style != null) {
 			settings.getHighlightConfigurationProvider().addNamedStyle(preset.getName(), style);
 		}
 	}
 	
 	protected void importCommandLineSettings (cc.warlock.core.stormfront.settings.server.CommandLineSettings settings, StormFrontClientSettings clientSettings)
 	{
 		ICommandLineSettings cmdLineSettings =
 			clientSettings.getCommandLineConfigurationProvider().getCommandLineSettings();
 		
 		cmdLineSettings.setBackgroundColor(stormfrontColorToWarlockColor(settings.getBackgroundColor()));
 		cmdLineSettings.setForegroundColor(stormfrontColorToWarlockColor(settings.getForegroundColor()));
 		cmdLineSettings.setBarColor(stormfrontColorToWarlockColor(settings.getBarColor()));
 		
 		WarlockFont font = new WarlockFont();
 		font.setFamilyName(settings.getFontFace());
 		font.setSize(settings.getFontSizeInPixels());
 		cmdLineSettings.setFont(font);
 	}
 	
 	protected String mapWindowId (String original)
 	{
 		if (original.equals(ServerSettings.WINDOW_DEATHS)) {
 			return StormFrontClientSettings.WINDOW_DEATHS;
 		} else if (original.equals(ServerSettings.WINDOW_INVENTORY)) {
 			return StormFrontClientSettings.WINDOW_INVENTORY;
 		} else if (original.equals(ServerSettings.WINDOW_MAIN)) {
 			return ClientSettings.WINDOW_MAIN;
 		} else if (original.equals(ServerSettings.WINDOW_SPELLS)) {
 			return StormFrontClientSettings.WINDOW_SPELLS;
 		} else if (original.equals(ServerSettings.WINDOW_THOUGHTS)) {
 			return StormFrontClientSettings.WINDOW_THOUGHTS;
 		} else if (original.equals(ServerSettings.WINDOW_ROOM)) {
 			return StormFrontClientSettings.WINDOW_ROOM;
 		} else if (original.equals(ServerSettings.WINDOW_CHAR_SHEET)) {
 			return StormFrontClientSettings.WINDOW_CHAR_SHEET;
 		} else if (original.equals(ServerSettings.WINDOW_CONTAINER_STOW)) {
 			return StormFrontClientSettings.WINDOW_CONTAINER_STOW;
 		} else if (original.equals(ServerSettings.WINDOW_FAMILIAR)) {
 			return StormFrontClientSettings.WINDOW_FAMILIAR;
 		} else if (original.equals(ServerSettings.WINDOW_NEWS)) {
 			return StormFrontClientSettings.WINDOW_NEWS;
 		} else {
 			return original;
 		}
 	}
 	
 	protected void importWindowSettings (cc.warlock.core.stormfront.settings.server.WindowSettings settings, StormFrontClientSettings clientSettings)
 	{
 		WindowSettings wSettings = (WindowSettings) clientSettings.getWindowSettings(mapWindowId(settings.getId()));
 		
 		if (wSettings == null) {
 			wSettings = new WindowSettings(clientSettings.getWindowSettingsProvider());
 		}
 		
 		wSettings.setBackgroundColor(stormfrontColorToWarlockColor(settings.getBackgroundColor()));
 		wSettings.setForegroundColor(stormfrontColorToWarlockColor(settings.getForegroundColor()));
 		
 		WarlockFont font = new WarlockFont();
 		font.setFamilyName(settings.getColumnFontFace());
 		font.setSize(settings.getColumnFontSizeInPixels());
 		wSettings.setColumnFont(font);
 		wSettings.setId(mapWindowId(settings.getId()));
 		
 		clientSettings.getWindowSettingsProvider().addWindowSettings(wSettings);
 	}
 	
 	protected void importIgnore (IgnoreSetting ignore, StormFrontClientSettings settings)
 	{
 		Pattern pattern = ignore.getRegex();
 		
 		settings.getIgnoreConfigurationProvider().addIgnore(
 			new Ignore(settings.getIgnoreConfigurationProvider(), pattern.pattern(), true, true, true));
 		
 	}
 	
 	protected void importVariable (String var, String value, StormFrontClientSettings settings)
 	{
 		settings.getVariableConfigurationProvider().addVariable(
 			new Variable(settings.getVariableConfigurationProvider(), var, value));
 	}
 	
 	protected void importMacro (MacroKey macro, StormFrontClientSettings settings)
 	{
 		if (macroImporter != null) {
 			macroImporter.importMacro(settings, macro.getKey(), macro.getModifiers(), macro.getAction());
 		}
 	}
 	
 	protected void importScript (ServerScript script, StormFrontClientSettings settings)
 	{
 		File stormfrontScriptsDir = ConfigurationUtil.getUserDirectory("stormfront-scripts", true);
 		Set<File> scriptDirs = ScriptConfiguration.instance().getScriptDirectories();
 		
 		if (!scriptDirs.contains(stormfrontScriptsDir)) {
 			scriptDirs.add(stormfrontScriptsDir);
 		}
 		
 		String scriptName = script.getName() + ".cmd";
 		try {
 			FileWriter writer = new FileWriter(new File(stormfrontScriptsDir, scriptName));
 			writer.write(script.getScriptContents().toCharArray());
 			writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected void parseData() {
 		clientVersion = stringValue("client-version");
 		majorVersion = stringValue("major-version");
 		crc = stringValue("crc");
 	}
 	
 	@Override
 	protected void saveTo(List<Element> elements) {
 		Element element = DocumentHelper.createElement("stormfront-settings");
 		
 		element.addAttribute("client-version", clientVersion);
 		element.addAttribute("major-version", majorVersion);
 		element.addAttribute("crc", crc);
 
 		elements.add(element);
 	}
 
 	public String getCrc() {
 		return crc;
 	}
 
 	public void setCrc(String crc) {
 		this.crc = crc;
 	}
 
 	public String getClientVersion() {
 		return clientVersion;
 	}
 
 	public void setClientVersion(String clientVersion) {
 		this.clientVersion = clientVersion;
 	}
 
 	public String getMajorVersion() {
 		return majorVersion;
 	}
 
 	public void setMajorVersion(String majorVersion) {
 		this.majorVersion = majorVersion;
 	}
 
 	public IStormFrontMacroImporter getMacroImporter() {
 		return macroImporter;
 	}
 
 	public void setMacroImporter(IStormFrontMacroImporter macroImporter) {
 		this.macroImporter = macroImporter;
 	}
 }
