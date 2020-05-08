 package ard.piraso.ui.base.action;
 
 import ard.piraso.ui.api.manager.ModelEvent;
 import ard.piraso.ui.api.manager.ModelOnChangeListener;
 import ard.piraso.ui.base.manager.ModelManagers;
 import org.openide.awt.*;
 import org.openide.util.ImageUtilities;
 import org.openide.util.actions.Presenter;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * New Context monitor action
  */
 @ActionID(category="File", id="ard.piraso.ui.base.action.NewContextMonitorAction")
 @ActionRegistration(displayName="New Context Monitor")
 @ActionReferences({
         @ActionReference(path="Menu/File", position=270),
         @ActionReference(path="Toolbars/File", position=270)
 })
 public class NewContextMonitorAction extends AbstractAction implements ActionListener, Presenter.Menu, Presenter.Toolbar {
     private static final String SMALL_ICON_PATH = "ard/piraso/ui/base/icons/new.png";
     private static final String LARGE_ICON_PATH = "ard/piraso/ui/base/icons/new24.png";
 
    private static final int MAX_MENU_ITEM_COUNT = 20;
 
     public NewContextMonitorAction() {
         putValue("iconBase", SMALL_ICON_PATH);
        putValue(Action.SHORT_DESCRIPTION, "New Context Monitor");
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         System.out.println("here!");
     }
 
     @Override
     public JMenuItem getMenuPresenter() {
         JMenu menu = new JMenu("New Context Monitor");
         menu.setIcon(ImageUtilities.loadImageIcon(SMALL_ICON_PATH, true));
         addMenuItems(menu);
 
         ModelManagers.MONITORS.addModelOnChangeListener(new SyncMenuItemsHandler(menu));
         ModelManagers.PROFILES.addModelOnChangeListener(new SyncMenuItemsHandler(menu));
 
         return menu;
     }
 
     public Component getToolbarPresenter() {
         JPopupMenu popup = new JPopupMenu();
         addMenuItems(popup);
 
         ModelManagers.MONITORS.addModelOnChangeListener(new SyncMenuItemsHandler(popup));
         ModelManagers.PROFILES.addModelOnChangeListener(new SyncMenuItemsHandler(popup));
 
         JButton button = DropDownButtonFactory.createDropDownButton(ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false), popup);
         Actions.connect(button, this);
 
         return button;
     }
 
     private void addMenuItems(final JComponent menu) {
         List<String> profiles = ModelManagers.PROFILES.getNames();
         int added = 0;
 
         for(Iterator<String> itr = profiles.iterator(); itr.hasNext() && added <= MAX_MENU_ITEM_COUNT; added++) {
             String profileName = itr.next();
             JMenuItem item = new JMenuItem(String.format("Profile: %s", profileName));
 
             menu.add(item);
         }
 
         if(added > 0) {
             addSeparator(menu);
         }
 
         List<String> monitors = ModelManagers.MONITORS.getNames();
         for(Iterator<String> itr = monitors.iterator(); itr.hasNext() && added <= MAX_MENU_ITEM_COUNT; added++) {
             String monitorName = itr.next();
             JMenuItem item = new JMenuItem(String.format("Monitor: %s", monitorName));
             menu.add(item);
         }
 
         addSeparator(menu);
         JMenuItem item = new JMenuItem(String.format("Others..."));
         item.addActionListener(this);
         menu.add(item);
     }
 
     private void addSeparator(final JComponent menu) {
         if(menu instanceof JMenu) {
             ((JMenu) menu).addSeparator();
         } else if(menu instanceof JPopupMenu) {
             ((JPopupMenu) menu).addSeparator();
         }
     }
 
     private class SyncMenuItemsHandler implements ModelOnChangeListener {
         private JComponent component;
 
         private SyncMenuItemsHandler(JComponent component) {
             this.component = component;
         }
 
         @Override
         public void onChange(ModelEvent evt) {
             component.removeAll();
             addMenuItems(component);
         }
     }
 }
