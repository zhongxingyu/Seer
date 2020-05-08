 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package gui;
 
 import javax.swing.*;
 import javax.swing.text.DefaultEditorKit;
 
 import java.awt.Toolkit;
 import java.util.*;
 
 /**
  * Handle all of the code for building and maintaining menus.
  */
 public final class MenuManager {
     static MenuManager me;
 
     JFileChooser fileDialog;
 
     JMenuBar myMenu;
     
     public Map<String, JMenuItem> nameToItem;
     public Map<JMenuItem, String> itemToName;
 
     /**
      * Build the main menu.
      */
     private MenuManager() {
         nameToItem = new HashMap<String, JMenuItem>();
         itemToName = new HashMap<JMenuItem, String>();
 
         fileDialog = new JFileChooser();
         
         myMenu = buildBar(
             buildMenu("File", 'F',
                 buildItem("New", 'N', new actions.New()),
                 buildItem("Open", 'O', new actions.Open()),
                 buildItem("Save", 'S', new actions.Save()),
                 buildItem("Save as", null, new actions.SaveAs()),
                 buildItem("Close", 'W', new actions.Close()),
                 buildItem("Exit", "ALT F4", new actions.Exit())),
             buildMenu("Edit",
         		buildItem("Cut", 'X', new DefaultEditorKit.CutAction()),
         		buildItem("Copy", 'C', new DefaultEditorKit.CopyAction()),
         		buildItem("Paste", 'V', new DefaultEditorKit.PasteAction())),
             buildMenu("Scheme",
                 buildItem("Run", Options.CommandRun, new actions.Run()),
                 buildItem("Format", Options.CommandFormat, new actions.Format())),
             Options.buildOptionsMenu(),
             buildMenu("Help",
                buildItem("Show error console", null, new actions.ShowError()),
                 buildItem("About", "F1", new actions.ShowAbout())));
     }
 
     /**
      * Access the static menu manager.
      * @return
      */
     public static MenuManager me() {
     	if (me == null)
     		me = new MenuManager();
     	
     	return me;
     }
     
     /**
      * Build a JMenuBar.
      * @param menus All of the menus.
      * @return 
      */
     public JMenuBar buildBar(JMenu... menus) {
         JMenuBar menuBar = new JMenuBar();
         for (JMenu menu : menus)
             menuBar.add(menu);
         return menuBar;
     }
 
     /**
      * Build a menu with a mnemonic.
      * @param name The menu's name.
      * @param accel The mnemonic.
      * @param items A list of JMenuItems.
      * @return The menu.
      * @return
      */
     private JMenu buildMenu(String name, char accel, JMenuItem... items) {
         JMenu menu = buildMenu(name, items);
         if (!util.OS.IsOSX)
             menu.setMnemonic(accel);
         return menu;
     }
 
     /**
      * Build a menu.
      * @param name The menu's name.
      * @param items A list of JMenuItems.
      * @return The menu.
      */
     private JMenu buildMenu(String name, JMenuItem... items) {
         JMenu menu = new JMenu(name);
         for (JMenuItem item : items)
             menu.add(item);
         return menu;
     }
     
     /**
      * Build a JMenuItem (and store it in the dictionaries).
      * 
      * @param action An action.
      * @return The new item.
      */
 	private JMenuItem buildItem(String name, Object accel, Action action) {
     	JMenuItem item = new JMenuItem(action);
     	item.setText(name);
     	
         if (accel != null)
         {
             if (accel instanceof Character)
                 item.setAccelerator(KeyStroke.getKeyStroke(
                     ((Character) accel).charValue(),
                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                 ));
 
             else if (accel instanceof String)
                 item.setAccelerator(KeyStroke.getKeyStroke((String) accel));
 
             if (!util.OS.IsOSX && accel instanceof Character)
                 item.setMnemonic((Character) accel);
         }
     	
     	itemToName.put(item, name);
     	nameToItem.put(name, item);
     	
     	return item;
     }
     
     /**
      * Access the JMenuBar.
      * @return Said JMenuBar.
      */
     public static JMenuBar menu() {
        if (me == null)
            me = new MenuManager();
 
        return me.myMenu;
     }
 }
