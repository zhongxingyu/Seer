 package net.argius.stew.ui.window;
 
 import static net.argius.stew.ui.window.Menu.Item.*;
 import static net.argius.stew.ui.window.Utilities.getImageIcon;
 import static net.argius.stew.ui.window.Utilities.getMenuShortcutKeyMask;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 
 import net.argius.stew.*;
 
 /**
  * The menu bar.
  */
 final class Menu extends JMenuBar implements PropertyChangeListener {
 
     private static final ResourceManager res = ResourceManager.getInstance(Menu.class);
 
     /**
      * Menu Items.
      */
     enum Item {
         newWindow,
         closeWindow,
         quit,
         cut,
         copy,
         paste,
         selectAll,
         find,
         toggleFocus,
         clearMessage,
         showStatusBar,
         showColumnNumber,
         showInfoTree,
         showAlwaysOnTop,
         refresh,
         widenColumnWidth,
         narrowColumnWidth,
         adjustColumnWidth,
         autoAdjustMode,
         autoAdjustModeNone,
         autoAdjustModeHeader,
         autoAdjustModeValue,
         autoAdjustModeHeaderAndValue,
         executeCommand,
         breakCommand,
         lastHistory,
         nextHistory,
         sendRollback,
         sendCommit,
         connect,
         disconnect,
         postProcessMode,
         postProcessModeNone,
         postProcessModeFocus,
         postProcessModeShake,
         postProcessModeBlink,
         inputEcryptionKey,
         editConnectors,
         sortResult,
         importFile,
         exportFile,
         showHelp,
         showAbout;
     }
 
     private List<JMenuItem> lockingTargets;
     private List<JMenuItem> unlockingTargets;
     private EnumMap<Item, JMenuItem> itemToCompMap;
     private Map<JMenuItem, Item> compToItemMap;
 
     Menu(final AnyActionListener anyActionListener) {
         this.lockingTargets = new ArrayList<JMenuItem>();
         this.unlockingTargets = new ArrayList<JMenuItem>();
         this.itemToCompMap = new EnumMap<Item, JMenuItem>(Item.class);
         this.compToItemMap = new HashMap<JMenuItem, Item>();
         final boolean autoMnemonic = res.getInt("auto-mnemonic") == 1;
         AnyAction aa = new AnyAction(anyActionListener);
         for (final String groupId : res.get("groups").split(",", -1)) {
             final String groupKey = "group." + groupId;
             JMenu group = add(buildGroup(groupId, autoMnemonic));
             for (final String itemId : res.get(groupKey + ".items").split(",", -1)) {
                 if (itemId.length() == 0) {
                     group.add(new JSeparator());
                     continue;
                 }
                 JMenuItem m = group.add(buildItem(itemId, autoMnemonic));
                 Item item;
                 try {
                     item = Item.valueOf(itemId);
                 } catch (Exception ex) {
                     assert false : ex.toString();
                     continue;
                 }
                 m.addActionListener(aa);
                 itemToCompMap.put(item, m);
                 compToItemMap.put(m, item);
                 final String shortcutId = "item." + itemId + ".shortcut";
                 if (res.containsKey(shortcutId)) {
                     KeyStroke shortcutKey = KeyStroke.getKeyStroke(res.get(shortcutId));
                     if (shortcutKey != null) {
                         setAccelerator(item, shortcutKey);
                     }
                 }
                 switch (item) {
                     case closeWindow:
                     case quit:
                     case cut:
                     case copy:
                     case paste:
                     case selectAll:
                     case find:
                     case clearMessage:
                     case refresh:
                     case widenColumnWidth:
                     case narrowColumnWidth:
                     case adjustColumnWidth:
                     case autoAdjustMode:
                     case executeCommand:
                     case lastHistory:
                     case nextHistory:
                     case connect:
                     case disconnect:
                     case postProcessMode:
                     case sortResult:
                     case exportFile:
                         lockingTargets.add(m);
                         break;
                     case breakCommand:
                         unlockingTargets.add(m);
                         break;
                     default:
                 }
             }
         }
         for (final Item item : EnumSet.of(autoAdjustModeNone,
                                           autoAdjustModeHeader,
                                           autoAdjustModeValue,
                                           autoAdjustModeHeaderAndValue,
                                           postProcessMode,
                                           postProcessModeNone,
                                           postProcessModeFocus,
                                           postProcessModeShake,
                                           postProcessModeBlink)) {
             itemToCompMap.get(item).addActionListener(aa);
         }
         setEnabledStates(false);
     }
 
     private static JMenu buildGroup(String groupId, boolean autoMnemonic) {
         final String key = (res.containsKey("group." + groupId) ? "group" : "item") + '.' + groupId;
         final char mn = res.getChar(key + ".mnemonic");
         final String groupString = res.get(key) + (autoMnemonic ? "(" + mn + ")" : "");
         JMenu group = new JMenu(groupString);
         group.setMnemonic(mn);
         return group;
     }
 
     private JMenuItem buildItem(String itemId, boolean autoMnemonic) {
         final String itemKey = "item." + itemId;
         final char mn = res.getChar(itemKey + ".mnemonic");
         final JMenuItem m;
         if (res.isTrue(itemKey + ".checkbox")) {
             m = new JCheckBoxMenuItem();
         } else if (res.isTrue(itemKey + ".subgroup")) {
             m = buildGroup(itemId, autoMnemonic);
             ButtonGroup buttonGroup = new ButtonGroup();
             boolean selected = false;
             for (final String id : res.get(itemKey + ".items").split(",", -1)) {
                 final JMenuItem sub = buildItem(itemId + id, autoMnemonic);
                 m.add(sub);
                 buttonGroup.add(sub);
                 if (!selected) {
                     sub.setSelected(true);
                     selected = true;
                 }
                 Item subItem = Item.valueOf(itemId + id);
                 itemToCompMap.put(subItem, sub);
                 compToItemMap.put(sub, subItem);
             }
         } else {
             m = new JMenuItem();
         }
         m.setText(res.get(itemKey) + (autoMnemonic ? "(" + mn + ")" : ""));
         m.setMnemonic(mn);
         m.setActionCommand(itemId);
         m.setIcon(getImageIcon(String.format("menu-%s.png", itemId)));
         m.setDisabledIcon(getImageIcon(String.format("menu-disabled-%s.png", itemId)));
         return m;
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent e) {
         final String propertyName = e.getPropertyName();
         final Object source = e.getSource();
         if (source instanceof JLabel && propertyName.equals("ancestor")) {
             itemToCompMap.get(showStatusBar).setSelected(((JLabel)source).isVisible());
         } else if (source instanceof ResultSetTable && propertyName.equals("showNumber")) {
             itemToCompMap.get(showColumnNumber).setSelected((Boolean)e.getNewValue());
         } else if (source instanceof DatabaseInfoTree) {
             itemToCompMap.get(showInfoTree).setSelected(((Component)source).isEnabled());
         } else if (source instanceof JFrame && propertyName.equals("alwaysOnTop")) {
             itemToCompMap.get(showAlwaysOnTop).setSelected((Boolean)e.getNewValue());
        } else if (source instanceof ResultSetTable && propertyName.equals("autoAdjustMode")) {
            final String itemName = e.getNewValue().toString();
            if (!itemName.matches("[A-Z_]+")) { // ignore old version
                itemToCompMap.get(Item.valueOf(itemName)).setSelected(true);
            }
        } else if (source instanceof WindowOutputProcessor && propertyName.equals("postProcessMode")) {
             final String itemName = e.getNewValue().toString();
             if (!itemName.matches("[A-Z_]+")) { // ignore old version
                 itemToCompMap.get(Item.valueOf(itemName)).setSelected(true);
             }
         }
     }
 
     /**
      * Sets a Accelerator (shortcut key).
      * @param item
      * @param ks
      */
     void setAccelerator(Item item, KeyStroke ks) {
         int m = 0;
         final String s = ks.toString();
         if (s.contains("ctrl")) {
             m |= getMenuShortcutKeyMask();
         }
         if (s.contains("alt")) {
             m |= InputEvent.ALT_DOWN_MASK;
         }
         if (s.contains("shift")) {
             m |= InputEvent.SHIFT_DOWN_MASK;
         }
         itemToCompMap.get(item).setAccelerator(KeyStroke.getKeyStroke(ks.getKeyCode(), m));
     }
 
     /**
      * Sets the state that command was started or not.
      * @param commandStarted
      */
     void setEnabledStates(boolean commandStarted) {
         final boolean lockingTargetsState = !commandStarted;
         for (JMenuItem item : lockingTargets) {
             item.setEnabled(lockingTargetsState);
         }
         final boolean unlockingTargetsState = commandStarted;
         for (JMenuItem item : unlockingTargets) {
             item.setEnabled(unlockingTargetsState);
         }
     }
 
 }
