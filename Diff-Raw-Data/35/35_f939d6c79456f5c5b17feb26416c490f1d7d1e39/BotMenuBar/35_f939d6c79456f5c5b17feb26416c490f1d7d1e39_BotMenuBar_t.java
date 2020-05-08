 package org.rsbot.gui;
 
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 
 import org.rsbot.bot.Bot;
 import org.rsbot.event.impl.DrawBoundaries;
 import org.rsbot.event.impl.DrawInventory;
 import org.rsbot.event.impl.DrawItems;
 import org.rsbot.event.impl.DrawModel;
 import org.rsbot.event.impl.DrawMouse;
 import org.rsbot.event.impl.DrawNPCs;
 import org.rsbot.event.impl.DrawObjects;
 import org.rsbot.event.impl.DrawPlayers;
 import org.rsbot.event.impl.DrawSettings;
 import org.rsbot.event.impl.DrawWeb;
 import org.rsbot.event.impl.MessageLogger;
 import org.rsbot.event.impl.TAnimation;
 import org.rsbot.event.impl.TCamera;
 import org.rsbot.event.impl.TFPS;
 import org.rsbot.event.impl.TFloorHeight;
 import org.rsbot.event.impl.TLoginIndex;
 import org.rsbot.event.impl.TMenu;
 import org.rsbot.event.impl.TMenuActions;
 import org.rsbot.event.impl.TMousePosition;
 import org.rsbot.event.impl.TPlayerPosition;
 import org.rsbot.event.impl.TTab;
 import org.rsbot.event.impl.TUserInputAllowed;
 import org.rsbot.event.impl.TWebStatus;
 import org.rsbot.event.listeners.PaintListener;
 import org.rsbot.event.listeners.TextPaintListener;
 import org.rsbot.util.GlobalConfiguration;
 
 public class BotMenuBar extends JMenuBar {
 	private static final long serialVersionUID = 971579975301998332L;
 	public static final Map<String, Class<?>> DEBUG_MAP = new LinkedHashMap<String, Class<?>>();
 	public static final String[] TITLES;
 	public static final String[][] ELEMENTS;
 
 	static {
 
 		// Text
 		DEBUG_MAP.put("Game State", TLoginIndex.class);
 		DEBUG_MAP.put("Current Tab", TTab.class);
 		DEBUG_MAP.put("Camera", TCamera.class);
 		DEBUG_MAP.put("Animation", TAnimation.class);
 		DEBUG_MAP.put("Floor Height", TFloorHeight.class);
 		DEBUG_MAP.put("Player Position", TPlayerPosition.class);
 		DEBUG_MAP.put("Mouse Position", TMousePosition.class);
 		DEBUG_MAP.put("User Input Allowed", TUserInputAllowed.class);
 		DEBUG_MAP.put("Menu Actions", TMenuActions.class);
 		DEBUG_MAP.put("Menu", TMenu.class);
 		DEBUG_MAP.put("FPS", TFPS.class);
 		DEBUG_MAP.put("Web Status", TWebStatus.class);
 
 		// Paint
 		DEBUG_MAP.put("Players", DrawPlayers.class);
 		DEBUG_MAP.put("NPCs", DrawNPCs.class);
 		DEBUG_MAP.put("Objects", DrawObjects.class);
 		DEBUG_MAP.put("Models", DrawModel.class);
 		DEBUG_MAP.put("Mouse", DrawMouse.class);
 		DEBUG_MAP.put("Inventory", DrawInventory.class);
 		DEBUG_MAP.put("Ground Items", DrawItems.class);
 		DEBUG_MAP.put("Calc Test", DrawBoundaries.class);
 		DEBUG_MAP.put("Settings", DrawSettings.class);
 		DEBUG_MAP.put("Web", DrawWeb.class);
 
 		// Other
 		DEBUG_MAP.put("Log Messages", MessageLogger.class);
 
		TITLES = new String[]{Messages.FILE, Messages.EDIT, Messages.VIEW, Messages.HELP};
 		ELEMENTS = new String[][]{
 				{"New Bot", "Close Bot", "-",
 					"Service Key", "Add Script", "Run Script", "Stop Script", "Pause Script", "-",
 					"Save Screenshot", "-",
 				"Exit"},
 				{"Accounts", "-",
 					"ToggleF Force Input", "ToggleF Disable Rendering", "ToggleF Disable Canvas", "-",
 					"ToggleF Disable Anti-Randoms", "ToggleF Disable Auto Login", "-",
 					"ToggleF Disable Advertisements", "ToggleF Disable Monitoring", "ToggleF Disable Confirmations"}, constructDebugs(),
 					{"Site", "Project", "About"}};
 	}
 
 	private static String[] constructDebugs() {
 		final List<String> debugItems = new ArrayList<String>();
 		debugItems.add("Hide Toolbar");
 		debugItems.add("Hide Log Window");
 		debugItems.add("All Debugging");
 		debugItems.add("-");
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (PaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		debugItems.add("-");
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (TextPaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		debugItems.add("-");
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (!TextPaintListener.class.isAssignableFrom(el) && !PaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		for (final ListIterator<String> it = debugItems.listIterator(); it.hasNext();) {
 			final String s = it.next();
 			if (!s.equals("-")) {
 				it.set("ToggleF " + s);
 			}
 		}
 		return debugItems.toArray(new String[debugItems.size()]);
 	}
 
 	private void constructItemIcons() {
 		final HashMap<String, String> map = new HashMap<String, String>(16);
 		map.put("New Bot", GlobalConfiguration.Paths.Resources.ICON_APPADD);
 		map.put("Close Bot", GlobalConfiguration.Paths.Resources.ICON_APPDELETE);
 		map.put("Service Key", GlobalConfiguration.Paths.Resources.ICON_KEY);
 		map.put("Add Script", GlobalConfiguration.Paths.Resources.ICON_SCRIPT_ADD);
 		map.put("Run Script", GlobalConfiguration.Paths.Resources.ICON_PLAY);
 		map.put("Stop Script", GlobalConfiguration.Paths.Resources.ICON_DELETE);
 		map.put("Pause Script", GlobalConfiguration.Paths.Resources.ICON_PAUSE);
 		map.put("Save Screenshot", GlobalConfiguration.Paths.Resources.ICON_PHOTO);
 		map.put("Exit", GlobalConfiguration.Paths.Resources.ICON_CLOSE);
 		map.put("Accounts", GlobalConfiguration.Paths.Resources.ICON_REPORTKEY);
 		map.put("Site", GlobalConfiguration.Paths.Resources.ICON_WEBLINK);
 		map.put("Project", GlobalConfiguration.Paths.Resources.ICON_USEREDIT);
 		map.put("About", GlobalConfiguration.Paths.Resources.ICON_INFO);
 		for (final Entry<String, String> item : map.entrySet()) {
 			final JMenuItem menu = commandMenuItem.get(item.getKey());
 			menu.setIcon(new ImageIcon(GlobalConfiguration.getImage(item.getValue())));
 		}
 	}
 
 	private final Map<String, JCheckBoxMenuItem> eventCheckMap = new HashMap<String, JCheckBoxMenuItem>();
 	private final Map<String, JCheckBoxMenuItem> commandCheckMap = new HashMap<String, JCheckBoxMenuItem>();
 	private final Map<String, JMenuItem> commandMenuItem = new HashMap<String, JMenuItem>();
 	private final ActionListener listener;
 
 	public BotMenuBar(final ActionListener listener) {
 		this.listener = listener;
 		for (int i = 0; i < TITLES.length; i++) {
 			final String title = TITLES[i];
 			final String[] elems = ELEMENTS[i];
 			add(constructMenu(title, elems));
 		}
 		constructItemIcons();
 		commandMenuItem.get("Service Key").setVisible(false);
 		commandCheckMap.get("Disable Monitoring").setVisible(false);
 	}
 
 	public void setOverrideInput(final boolean force) {
 		commandCheckMap.get("Force Input").setSelected(force);
 	}
 
 	public void setPauseScript(final boolean pause) {
 		commandMenuItem.get("Pause Script").setText(pause ? "Resume Script" : "Pause Script");
 	}
 
 	public void setBot(final Bot bot) {
 		if (bot == null) {
 			commandMenuItem.get("Close Bot").setEnabled(false);
 			commandMenuItem.get("Run Script").setEnabled(false);
 			commandMenuItem.get("Stop Script").setEnabled(false);
 			commandMenuItem.get("Pause Script").setEnabled(false);
 			commandMenuItem.get("Save Screenshot").setEnabled(false);
 			for (final JCheckBoxMenuItem item : eventCheckMap.values()) {
 				item.setSelected(false);
 				item.setEnabled(false);
 			}
 			disable("All Debugging", "Force Input", "Disable Rendering", "Disable Canvas", "Disable Anti-Randoms", "Disable Auto Login");
 		} else {
 			commandMenuItem.get("Close Bot").setEnabled(true);
 			commandMenuItem.get("Run Script").setEnabled(true);
 			commandMenuItem.get("Stop Script").setEnabled(true);
 			commandMenuItem.get("Pause Script").setEnabled(true);
 			commandMenuItem.get("Save Screenshot").setEnabled(true);
 			int selections = 0;
 			for (final Map.Entry<String, JCheckBoxMenuItem> entry : eventCheckMap.entrySet()) {
 				entry.getValue().setEnabled(true);
 				final boolean selected = bot.hasListener(DEBUG_MAP.get(entry.getKey()));
 				entry.getValue().setSelected(selected);
 				if (selected) {
 					++selections;
 				}
 			}
 			enable("All Debugging", selections == eventCheckMap.size());
 			enable("Force Input", bot.overrideInput);
 			enable("Disable Rendering", bot.disableRendering);
 			enable("Disable Canvas", bot.disableCanvas);
 			enable("Disable Anti-Randoms", bot.disableRandoms);
 			enable("Disable Auto Login", bot.disableAutoLogin);
 		}
 	}
 
 	public JCheckBoxMenuItem getCheckBox(final String key) {
 		return commandCheckMap.get(key);
 	}
 
 	private void disable(final String... items) {
 		for (final String item : items) {
 			commandCheckMap.get(item).setSelected(false);
 			commandCheckMap.get(item).setEnabled(false);
 		}
 	}
 
 	public void enable(final String item, final boolean selected) {
 		commandCheckMap.get(item).setSelected(selected);
 		commandCheckMap.get(item).setEnabled(true);
 	}
 
 	public void doClick(final String item) {
 		commandMenuItem.get(item).doClick();
 	}
 
 	public void loadPrefs() {
 		final String path = GlobalConfiguration.Paths.getMenuBarPrefs();
 		if (!new File(path).exists()) {
 			return;
 		}
 		FileReader freader = null;
 		BufferedReader in = null;
 		try {
 			freader = new FileReader(path);
 			in = new BufferedReader(freader);
 			String line;
 			while ((line = in.readLine()) != null) {
 				line = line.trim();
 				if (commandCheckMap.containsKey(line)) {
 					commandCheckMap.get(line).doClick();
 				}
 			}
 		} catch (final IOException ioe) {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (freader != null) {
 					freader.close();
 				}
 			} catch (final IOException ioe1) {
 			}
 		}
 	}
 
 	public void savePrefs() {
 		final String path = GlobalConfiguration.Paths.getMenuBarPrefs();
 		FileWriter fstream = null;
 		BufferedWriter out = null;
 		try {
 			final File f = new File(path);
 			if (f.exists()) {
 				f.delete();
 			}
 			fstream = new FileWriter(path);
 			out = new BufferedWriter(fstream);
 			for (final Entry<String, JCheckBoxMenuItem> item : commandCheckMap.entrySet()) {
 				final boolean checked = item.getValue().isSelected();
 				if (!checked) {
 					continue;
 				}
 				out.write(item.getKey());
 				out.newLine();
 			}
 		} catch (final IOException ioe) {
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (final IOException ioe1) {
 			}
 		}
 	}
 
 	private JMenu constructMenu(final String title, final String[] elems) {
 		final JMenu menu = new JMenu(title);
 		for (String e : elems) {
 			if (e.equals("-")) {
 				menu.add(new JSeparator());
 			} else {
 				JMenuItem jmi;
 				if (e.startsWith("Toggle")) {
 					e = e.substring("Toggle".length());
 					final char state = e.charAt(0);
 					e = e.substring(2);
 					jmi = new JCheckBoxMenuItem(e);
 					if (state == 't' || state == 'T') {
 						jmi.setSelected(true);
 					}
 					if (DEBUG_MAP.containsKey(e)) {
 						final JCheckBoxMenuItem ji = (JCheckBoxMenuItem) jmi;
 						eventCheckMap.put(e, ji);
 					}
 					final JCheckBoxMenuItem ji = (JCheckBoxMenuItem) jmi;
 					commandCheckMap.put(e, ji);
 				} else {
 					jmi = new JMenuItem(e);
 					commandMenuItem.put(e, jmi);
 				}
 				jmi.addActionListener(listener);
 				jmi.setActionCommand(title + "." + e);
 				menu.add(jmi);
 			}
 		}
 		return menu;
 	}
 }
