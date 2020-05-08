 package org.geworkbench.util;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.MenuElement;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.analysis.AbstractAnalysis;
 import org.geworkbench.builtin.projects.PendingTreeNode;
 import org.geworkbench.builtin.projects.ProjectPanel;
 import org.geworkbench.engine.config.MenuListener;
 import org.geworkbench.engine.config.PluginDescriptor;
 import org.geworkbench.engine.config.VisualPlugin;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.config.rules.MalformedMenuItemException;
 import org.geworkbench.engine.config.rules.NotMenuListenerException;
 import org.geworkbench.engine.config.rules.NotVisualPluginException;
 import org.geworkbench.engine.config.rules.PluginObject;
 import org.geworkbench.engine.management.ComponentRegistry;
 
 /**
  * Base class for command panels: AnalysisPanel, FilteringPanel, NormalizationPanel
  * @author mw2518
  * $Id$
  */
 public abstract class CommandBase implements MenuListener, VisualPlugin {
 	private static Log log = LogFactory.getLog(CommandBase.class);
 			
 	private static final String topMenuItem = "Commands";
	final private String popMenuItem;
 	final protected HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
 	
 	private JDialog dialog = null;
 
	protected CommandBase(String string) {
		popMenuItem = string;
	}

 	final protected void hideDialog() {
 		if (dialog == null) { // this should not happen because this method should not be called before showDialog
 			return;
 		}
 		dialog.setVisible(false);
 	}
 	
 	final private void showDialog(String title) {
 		if (dialog == null) { // first time
 			dialog = new JDialog();
 			dialog.add(getComponent());
 			dialog.pack();
 			dialog.setLocationRelativeTo(null);
 		}
 		dialog.pack();
 		dialog.setTitle(title);
 		dialog.setVisible(true);
 	}
 
 	protected AbstractAnalysis[] availableCommands;
 	final protected AbstractAnalysis getCommandByName(String commandName) {
 		if(availableCommands==null) { // this should not be called before initialized
 			log.error("availableCommands==null");
 			return null;
 		}
 		for (AbstractAnalysis command: availableCommands) {
 			if( commandName.equals(command.getLabel()) ) return command;
 		}
 		log.error("no match command");
 		return null;
 	}
 
 	// string name version should be replaced eventually
 	protected abstract void setSelectedCommandByName(String commandName);
 
 	final protected void updateMenuItems() {
 		for (AbstractAnalysis command: availableCommands) {
 			String name = ComponentRegistry.getRegistry()
 			.getDescriptorForPlugin(command).getLabel();
 			try {
 				setMenuItem(name);
 			} catch (NotMenuListenerException e) {
 				e.printStackTrace();
 			} catch (NotVisualPluginException e) {
 				e.printStackTrace();
 			} catch (MalformedMenuItemException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void setMenuItem(String name) throws NotMenuListenerException, NotVisualPluginException, MalformedMenuItemException{
 		String path = topMenuItem + GeawConfigObject.menuItemDelimiter + popMenuItem + GeawConfigObject.menuItemDelimiter;
 		String menuName = path + name;
 		String menuPath = path + name.replace(GeawConfigObject.menuItemDelimiter, PluginObject.escapedMenuItemDelimiter);
 		if (listeners.get(menuName)==null)
 			listeners.put(menuName, new ActionListener(){
 				public void actionPerformed(ActionEvent e){
 					setSelectedCommandByName(e.getActionCommand());
 					showDialog(e.getActionCommand());
 				}
 			});
 		PluginDescriptor pluginDesc = ComponentRegistry.getRegistry().getDescriptorForPlugin(this);
 		if (pluginDesc!=null)
 			PluginObject.registerMenuItem(pluginDesc, menuPath, "always", menuName, null, null);
 	}
 
 	protected void clearMenuItems(){
     	MenuElement[] elements = GeawConfigObject.getMenuBar().getSubElements();
     	for (MenuElement element: elements){
     		JMenu menu = (JMenu)element.getComponent();
     		if (menu.getText().equals(topMenuItem)){
     			JPopupMenu popMenu = menu.getPopupMenu();
     			MenuElement[] subelements = popMenu.getSubElements();
     			for (MenuElement subelement: subelements){
     				JMenuItem submenu = (JMenuItem)subelement.getComponent();
     				if (submenu.getText().equals(popMenuItem)){
     					submenu.removeAll();
     				}
     			}
     			break;
     		}
     	}
     }
 
 	protected boolean pendingNodeSelected(){
 		return ProjectPanel.getInstance().getSelection().getSelectedNode() instanceof PendingTreeNode;
 	}
 
 	public ActionListener getActionListener(String var) {
 		return listeners.get(var);
 	}
 }
