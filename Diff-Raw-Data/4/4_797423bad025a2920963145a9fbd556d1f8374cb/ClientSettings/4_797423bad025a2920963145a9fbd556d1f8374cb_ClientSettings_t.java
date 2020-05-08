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
 package cc.warlock.core.client.settings.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.PropertyListener;
 import cc.warlock.core.client.WarlockClientAdapter;
 import cc.warlock.core.client.WarlockClientRegistry;
 import cc.warlock.core.client.settings.IClientSettingProvider;
 import cc.warlock.core.client.settings.IClientSettings;
 import cc.warlock.core.client.settings.IHighlightProvider;
 import cc.warlock.core.client.settings.IHighlightString;
 import cc.warlock.core.client.settings.IIgnore;
 import cc.warlock.core.client.settings.IIgnoreProvider;
 import cc.warlock.core.client.settings.IVariable;
 import cc.warlock.core.client.settings.IVariableProvider;
 import cc.warlock.core.client.settings.IWindowSettings;
 import cc.warlock.core.client.settings.IWindowSettingsProvider;
 import cc.warlock.core.client.settings.macro.IMacro;
 import cc.warlock.core.client.settings.macro.IMacroCommand;
 import cc.warlock.core.client.settings.macro.IMacroProvider;
 import cc.warlock.core.client.settings.macro.IMacroVariable;
 import cc.warlock.core.client.settings.macro.internal.MacroConfigurationProvider;
 import cc.warlock.core.configuration.IConfigurationProvider;
 import cc.warlock.core.configuration.TreeConfigurationProvider;
 import cc.warlock.core.configuration.WarlockConfiguration;
 
 
 /**
  * This is the default Client Settings implementation, based on our XML Configuration backend.
  * This class includes a single default implementations for each {@link IClientSettingProvider}
  * @author marshall
  */
 @SuppressWarnings("unchecked")
 public class ClientSettings extends TreeConfigurationProvider implements IClientSettings {
 
 	public static final String CLIENT_SETTINGS = "clientSettings.xml";
 	
 	public static final String WINDOW_MAIN = "main";
 	
 	protected ArrayList<IClientSettingProvider> settingProviders = new ArrayList<IClientSettingProvider>();
 	
 	protected int version;
 	protected IWarlockClient client;
 
 	protected HighlightConfigurationProvider highlightConfigurationProvider;
 	protected IgnoreConfigurationProvider ignoreConfigurationProvider;
 	protected TriggerConfigurationProvider triggerConfigurationProvider;
 	protected VariableConfigurationProvider variableConfigurationProvider;
 	protected MacroConfigurationProvider macroConfigurationProvider;
 	protected WindowSettingsConfigurationProvider windowSettingsProvider;
 	
 	public ClientSettings (IWarlockClient client) {
 		super("client-settings");
 		this.client = client;
 		
 		highlightConfigurationProvider = new HighlightConfigurationProvider();
 		ignoreConfigurationProvider = new IgnoreConfigurationProvider();
 		triggerConfigurationProvider = new TriggerConfigurationProvider();
 		variableConfigurationProvider = new VariableConfigurationProvider();
 		macroConfigurationProvider = new MacroConfigurationProvider();
 		windowSettingsProvider = new WindowSettingsConfigurationProvider();
 		
 		addChildProvider(highlightConfigurationProvider);
 		addChildProvider(ignoreConfigurationProvider);
 		addChildProvider(triggerConfigurationProvider);
 		addChildProvider(variableConfigurationProvider);
 		addChildProvider(macroConfigurationProvider);
 		addChildProvider(windowSettingsProvider);
 		
 		addClientSettingProvider(highlightConfigurationProvider);
 		addClientSettingProvider(ignoreConfigurationProvider);
 		addClientSettingProvider(triggerConfigurationProvider);
 		addClientSettingProvider(variableConfigurationProvider);
 		addClientSettingProvider(macroConfigurationProvider);
 		addClientSettingProvider(windowSettingsProvider);
 		
 		setHandleChildren(true);
 		
 		WarlockClientRegistry.addWarlockClientListener(new WarlockClientAdapter() {
 			public void clientActivated(IWarlockClient client) {
 				if (client == ClientSettings.this.client) {
 					client.getClientId().addListener(new PropertyListener<String>() {
						public void propertyChanged(String value) {
 							parseClientSettings();
 						}
 					});
 				}
 			}
 		});
 	}
 	
 	protected void parseClientSettings()
 	{
 		WarlockConfiguration.getWarlockConfiguration(CLIENT_SETTINGS).addConfigurationProvider(this);
 	}
 	
 	@Override
 	public boolean supportsElement(Element element) {
 		if (element.getName().equals("client-settings")
 				&& client.getClientId() != null && client.getClientId().get() != null)
 		{
 			String clientId = element.attributeValue("client-id");
 			if (clientId.equals(client.getClientId().get())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	protected void parseData() {
 		version = intValue("version");
 	}
 	
 	@Override
 	protected void saveTo(List<Element> elements) {
 		Element element = DocumentHelper.createElement("client-settings");
 		element.addAttribute("version", version + "");
 		element.addAttribute("client-id", client.getClientId().get());
 		
 		elements.add(element);
 		
 		for (IConfigurationProvider provider : childProviders)
 		{
 			for (Element childElement : provider.getTopLevelElements()) {
 				element.add(childElement);
 			}
 		}
 	}
 
 	public List<? extends IClientSettingProvider> getAllProviders() {
 		return settingProviders;
 	}
 	
 	public <T extends IClientSettingProvider> List<T> getAllProviders(Class<T> providerClass) {
 		ArrayList<T> list = new ArrayList<T>();
 		for (IClientSettingProvider provider : settingProviders) {
 			if (providerClass.isAssignableFrom(provider.getClass())) {
 				list.add((T)provider);
 			}
 		}
 		return list;
 	}
 	
 	public void addClientSettingProvider(IClientSettingProvider provider) {
 		if (!settingProviders.contains(provider)) {
 			settingProviders.add(provider);
 		}
 	}
 
 	public void removeClientSettingProvider(IClientSettingProvider provider) {
 		settingProviders.remove(provider);
 	}
 	
 	public List<? extends IHighlightString> getAllHighlightStrings() {
 		ArrayList<IHighlightString> list = new ArrayList<IHighlightString>();
 		for (IHighlightProvider provider : getAllProviders(IHighlightProvider.class)) {
 			list.addAll(provider.getHighlightStrings());
 		}
 		return list;
 	}
 	
 	public List<? extends IIgnore> getAllIgnores() {
 		ArrayList<IIgnore> list = new ArrayList<IIgnore>();
 		for (IIgnoreProvider provider : getAllProviders(IIgnoreProvider.class)) {
 			list.addAll(provider.getIgnores());
 		}
 		return list;
 	}
 	
 	public List<? extends IMacro> getAllMacros() {
 		ArrayList<IMacro> list = new ArrayList<IMacro>();
 		for (IMacroProvider provider : getAllProviders(IMacroProvider.class)) {
 			list.addAll(provider.getMacros());
 		}
 		return list;
 	}
 	
 	public List<? extends IMacroVariable> getAllMacroVariables() {
 		ArrayList<IMacroVariable> list = new ArrayList<IMacroVariable>();
 		for (IMacroProvider provider : getAllProviders(IMacroProvider.class)) {
 			list.addAll(provider.getMacroVariables());
 		}
 		return list;
 	}
 	
 	public List<? extends IMacroCommand> getAllMacroCommands() {
 		ArrayList<IMacroCommand> list = new ArrayList<IMacroCommand>();
 		for (IMacroProvider provider : getAllProviders(IMacroProvider.class)) {
 			list.addAll(provider.getMacroCommands());
 		}
 		return list;
 	}
 	
 	public List<? extends IVariable> getAllVariables() {
 		ArrayList<IVariable> list = new ArrayList<IVariable>();
 		for (IVariableProvider provider : getAllProviders(IVariableProvider.class)) {
 			list.addAll(provider.getVariables());
 		}
 		return list;
 	}
 	
 	public IMacro getMacro(int keycode, int modifiers) {
 		for (IMacro macro : getAllMacros()) {
 			if (macro.getKeyCode() == keycode && macro.getModifiers() == modifiers) {
 				return macro;
 			}
 		}
 		return null;
 	}
 	
 	public IMacroVariable getMacroVariable(String id) {
 		for (IMacroVariable var : getAllMacroVariables()) {
 			if (var.getIdentifier().equals(id)) return var;
 		}
 		return null;
 	}
 	
 	public IMacroCommand getMacroCommand(String id) {
 		for (IMacroCommand command : getAllMacroCommands()) {
 			if (command.getIdentifier().equals(id)) return command;
 		}
 		return null;
 	}
 	
 	public IWarlockStyle getNamedStyle(String name) {
 		for (IHighlightProvider provider : getAllProviders(IHighlightProvider.class)) {
 			IWarlockStyle style = provider.getNamedStyle(name);
 			if (style != null) {
 				return style;
 			}
 		}
 		return null;
 	}
 	
 	public int getVersion() {
 		return version;
 	}
 	
 	public List<? extends IWindowSettings> getAllWindowSettings() {
 		ArrayList<IWindowSettings> list = new ArrayList<IWindowSettings>();
 		for (IWindowSettingsProvider provider : getAllProviders(IWindowSettingsProvider.class)) {
 			list.addAll(provider.getWindowSettings());
 		}
 		return list;
 	}
 	
 	public IWindowSettings getWindowSettings(String windowId) {
 		for (IWindowSettings settings : getAllWindowSettings()) {
 			if (settings.getId().equals(windowId)) return settings;
 		}
 		return null;
 	}
 	
 	public IVariable getVariable(String identifier) {
 		for (IVariableProvider provider : getAllProviders(IVariableProvider.class)) {
 			IVariable var = provider.getVariable(identifier);
 			if (var != null)
 				return var;
 		}
 		return null;
 	}
 	
 	public IWarlockClient getClient() {
 		return client;
 	}
 
 	public ArrayList<IClientSettingProvider> getSettingProviders() {
 		return settingProviders;
 	}
 
 	public HighlightConfigurationProvider getHighlightConfigurationProvider() {
 		return highlightConfigurationProvider;
 	}
 
 	public IgnoreConfigurationProvider getIgnoreConfigurationProvider() {
 		return ignoreConfigurationProvider;
 	}
 
 	public TriggerConfigurationProvider getTriggerConfigurationProvider() {
 		return triggerConfigurationProvider;
 	}
 
 	public VariableConfigurationProvider getVariableConfigurationProvider() {
 		return variableConfigurationProvider;
 	}
 
 	public MacroConfigurationProvider getMacroConfigurationProvider() {
 		return macroConfigurationProvider;
 	}
 
 	public WindowSettingsConfigurationProvider getWindowSettingsProvider() {
 		return windowSettingsProvider;
 	}
 	
 	public IWindowSettings getMainWindowSettings() {
 		IWindowSettings main = getWindowSettings(WINDOW_MAIN);
 		
 		if (main == null)
 		{
 			WindowSettings mainSettings = new WindowSettings(getWindowSettingsProvider());
 			mainSettings.setId(WINDOW_MAIN);
 			mainSettings.setBackgroundColor(client.getSkin().getDefaultWindowBackground());
 			mainSettings.setForegroundColor(client.getSkin().getDefaultWindowForeground());
 			
 			getWindowSettingsProvider().addWindowSettings(mainSettings);
 			main = mainSettings;
 		}
 		
 		return main;
 	}
 }
