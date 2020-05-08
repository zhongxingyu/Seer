 package org.rsbot.gui;
 
 import org.rsbot.Configuration;
 import org.rsbot.bot.Bot;
 import org.rsbot.event.impl.*;
 import org.rsbot.event.listeners.PaintListener;
 import org.rsbot.event.listeners.TextPaintListener;
 import org.rsbot.locale.Messages;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.util.*;
 import java.util.List;
 import java.util.Map.Entry;
 
 /**
  * @author Paris
  * @author Timer
  */
 public class BotMenuBar extends JMenuBar {
 	private static final long serialVersionUID = 971579975301998332L;
 	private static final Messages msg = Messages.getInstance();
 	public static final Map<String, Class<?>> DEBUG_MAP = new LinkedHashMap<String, Class<?>>();
 	private static final String[] TITLES;
 	private static final String[][] ELEMENTS;
 
 	private static final boolean EXTENDED_VIEW_INITIAL = !Configuration.RUNNING_FROM_JAR;
 	private static final String[] EXTENDED_VIEW_ITEMS = {"Game State", "Current Tab", "Login Info", "Camera", "Floor Height",
 			"Mouse Position", "User Input Allowed", "Menu", "Menu Actions", "Cache", "Models", "Calc Test", "Settings",
 			"Character Moved"};
 
 	static {
 		// Text
 		DEBUG_MAP.put("Game State", TLoginIndex.class);
 		DEBUG_MAP.put("Current Tab", TTab.class);
 		DEBUG_MAP.put("Login Info", TLogin.class);
 		DEBUG_MAP.put("Camera", TCamera.class);
 		DEBUG_MAP.put("Animation", TAnimation.class);
 		DEBUG_MAP.put("Floor Height", TFloorHeight.class);
 		DEBUG_MAP.put("Player Position", TPlayerPosition.class);
 		DEBUG_MAP.put("Mouse Position", TMousePosition.class);
 		DEBUG_MAP.put("User Input Allowed", TUserInputAllowed.class);
 		DEBUG_MAP.put("Menu Actions", TMenuActions.class);
 		DEBUG_MAP.put("Menu", TMenu.class);
 		DEBUG_MAP.put("FPS", TFPS.class);
 		DEBUG_MAP.put("Cache", TWebStatus.class);
 
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
 		DEBUG_MAP.put("Character Moved", CharacterMovedLogger.class);
 
 		TITLES = new String[]{msg.FILE, msg.EDIT, msg.VIEW, msg.TOOLS, msg.HELP};
 		ELEMENTS = new String[][]{
 				{msg.NEWBOT, msg.CLOSEBOT, msg.MENUSEPERATOR,
 						msg.ADDSCRIPT,
 						msg.RUNSCRIPT, msg.STOPSCRIPT,
 						msg.PAUSESCRIPT, msg.MENUSEPERATOR,
 						msg.SAVESCREENSHOT, msg.MENUSEPERATOR,
 						msg.HIDEBOT, msg.EXIT},
 				{msg.ACCOUNTS, msg.MENUSEPERATOR,
 						msg.TOGGLEFALSE + msg.FORCEINPUT,
 						msg.TOGGLEFALSE + msg.LESSCPU,
 						msg.TOGGLEFALSE + msg.DISABLECANVAS,
 						(EXTENDED_VIEW_INITIAL ? msg.TOGGLETRUE : msg.TOGGLEFALSE) + msg.EXTDVIEWS,
 						msg.MENUSEPERATOR,
 						msg.TOGGLEFALSE + msg.DISABLEANTIRANDOMS,
 						msg.TOGGLEFALSE + msg.DISABLEAUTOLOGIN},
 				constructDebugs(), {msg.CLEARCACHE, msg.OPTIONS}, {msg.SITE, msg.PROJECT, msg.ABOUT}};
 	}
 
 	private static String[] constructDebugs() {
 		final List<String> debugItems = new ArrayList<String>();
 		debugItems.add(msg.HIDETOOLBAR);
 		debugItems.add(msg.HIDELOGPANE);
 		debugItems.add(msg.ALLDEBUGGING);
 		debugItems.add(msg.MENUSEPERATOR);
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (PaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		debugItems.add(msg.MENUSEPERATOR);
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (TextPaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		debugItems.add(msg.MENUSEPERATOR);
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (!TextPaintListener.class.isAssignableFrom(el) && !PaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		for (final ListIterator<String> it = debugItems.listIterator(); it.hasNext();) {
 			final String s = it.next();
 			if (!s.equals(msg.MENUSEPERATOR)) {
 				it.set(msg.TOGGLEFALSE + s);
 			}
 		}
 		return debugItems.toArray(new String[debugItems.size()]);
 	}
 
 	private void constructItemIcons() {
 		final HashMap<String, String> map = new HashMap<String, String>(16);
 		map.put(msg.NEWBOT, Configuration.Paths.Resources.ICON_APPADD);
 		map.put(msg.CLOSEBOT, Configuration.Paths.Resources.ICON_APPDELETE);
 		map.put(msg.ADDSCRIPT, Configuration.Paths.Resources.ICON_SCRIPT_ADD);
 		map.put(msg.RUNSCRIPT, Configuration.Paths.Resources.ICON_PLAY);
 		map.put(msg.STOPSCRIPT, Configuration.Paths.Resources.ICON_DELETE);
 		map.put(msg.PAUSESCRIPT, Configuration.Paths.Resources.ICON_PAUSE);
 		map.put(msg.SAVESCREENSHOT, Configuration.Paths.Resources.ICON_PHOTO);
 		map.put(msg.HIDEBOT, Configuration.Paths.Resources.ICON_ARROWIN);
 		map.put(msg.EXIT, Configuration.Paths.Resources.ICON_CLOSE);
 		map.put(msg.ACCOUNTS, Configuration.Paths.Resources.ICON_REPORTKEY);
 		map.put(msg.CLEARCACHE, Configuration.Paths.Resources.DATABASE_ERROR);
 		map.put(msg.OPTIONS, Configuration.Paths.Resources.ICON_WRENCH);
 		map.put(msg.SITE, Configuration.Paths.Resources.ICON_WEBLINK);
 		map.put(msg.PROJECT, Configuration.Paths.Resources.ICON_GITHUB);
 		map.put(msg.ABOUT, Configuration.Paths.Resources.ICON_INFO);
 		for (final Entry<String, String> item : map.entrySet()) {
 			final JMenuItem menu = commandMenuItem.get(item.getKey());
 			menu.setIcon(new ImageIcon(Configuration.getImage(item.getValue())));
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
 			final String[] elements = ELEMENTS[i];
 			add(constructMenu(title, elements));
 		}
 		constructItemIcons();
 		commandMenuItem.get(msg.HIDEBOT).setVisible(SystemTray.isSupported());
 		setExtendedView(EXTENDED_VIEW_INITIAL);
 	}
 
 	public void setExtendedView(final boolean show) {
 		for (String disableFeature : EXTENDED_VIEW_ITEMS) {
 			if (commandCheckMap.containsKey(disableFeature)) {
 				commandCheckMap.get(disableFeature).setVisible(show);
 			}
 		}
 	}
 
 	public void setOverrideInput(final boolean force) {
 		commandCheckMap.get(msg.FORCEINPUT).setSelected(force);
 	}
 
 	public void setPauseScript(final boolean pause) {
 		final JMenuItem item = commandMenuItem.get(msg.PAUSESCRIPT);
 		item.setText(pause ? msg.RESUMESCRIPT : msg.PAUSESCRIPT);
 		final Image image = Configuration.getImage(pause ? Configuration.Paths.Resources.ICON_START : Configuration.Paths.Resources.ICON_PAUSE);
 		if (image != null) {
 			item.setIcon(new ImageIcon(image));
 		}
 	}
 
 	public JMenuItem getMenuItem(final String name) {
 		return commandMenuItem.get(name);
 	}
 
 	public void setBot(final Bot bot) {
 		if (bot == null) {
 			commandMenuItem.get(msg.CLOSEBOT).setEnabled(false);
 			commandMenuItem.get(msg.RUNSCRIPT).setEnabled(false);
 			commandMenuItem.get(msg.STOPSCRIPT).setEnabled(false);
 			commandMenuItem.get(msg.PAUSESCRIPT).setEnabled(false);
 			commandMenuItem.get(msg.SAVESCREENSHOT).setEnabled(false);
 			for (final JCheckBoxMenuItem item : eventCheckMap.values()) {
 				item.setSelected(false);
 				item.setEnabled(false);
 			}
			disable(msg.ALLDEBUGGING, msg.FORCEINPUT, msg.LESSCPU, msg.DISABLEANTIRANDOMS, msg.DISABLEAUTOLOGIN);
 		} else {
 			commandMenuItem.get(msg.CLOSEBOT).setEnabled(true);
 			commandMenuItem.get(msg.RUNSCRIPT).setEnabled(true);
 			commandMenuItem.get(msg.STOPSCRIPT).setEnabled(true);
 			commandMenuItem.get(msg.PAUSESCRIPT).setEnabled(true);
 			commandMenuItem.get(msg.SAVESCREENSHOT).setEnabled(true);
 			int selections = 0;
 			for (final Map.Entry<String, JCheckBoxMenuItem> entry : eventCheckMap.entrySet()) {
 				entry.getValue().setEnabled(true);
 				final boolean selected = bot.hasListener(DEBUG_MAP.get(entry.getKey()));
 				entry.getValue().setSelected(selected);
 				if (selected) {
 					++selections;
 				}
 			}
 			enable(msg.ALLDEBUGGING, selections == eventCheckMap.size());
 			enable(msg.FORCEINPUT, bot.overrideInput);
 			enable(msg.LESSCPU, bot.disableRendering);
 			enable(msg.DISABLEANTIRANDOMS, bot.disableRandoms);
 			enable(msg.DISABLEAUTOLOGIN, bot.disableAutoLogin);
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
 
 	void enable(final String item, final boolean selected) {
 		commandCheckMap.get(item).setSelected(selected);
 		commandCheckMap.get(item).setEnabled(true);
 	}
 
 	public void setEnabled(final String item, final boolean mode) {
 		commandCheckMap.get(item).setEnabled(mode);
 	}
 
 	public void doClick(final String item) {
 		commandMenuItem.get(item).doClick();
 	}
 
 	public void doTick(final String item) {
 		commandCheckMap.get(item).doClick();
 	}
 
 	public boolean isTicked(final String item) {
 		return commandCheckMap.get(item).isSelected();
 	}
 
 	private JMenu constructMenu(final String title, final String[] elements) {
 		final JMenu menu = new JMenu(title);
 		for (String e : elements) {
 			if (e.equals(msg.MENUSEPERATOR)) {
 				menu.add(new JSeparator());
 			} else {
 				JMenuItem jmi;
 				if (e.startsWith(msg.TOGGLE)) {
 					e = e.substring(msg.TOGGLE.length());
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
