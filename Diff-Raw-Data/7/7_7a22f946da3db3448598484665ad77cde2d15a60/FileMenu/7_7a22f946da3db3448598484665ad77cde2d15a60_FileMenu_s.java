 package gsingh.learnkirtan.ui.menu;
 
 import gsingh.learnkirtan.ui.action.ActionFactory;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 
 @SuppressWarnings("serial")
 public class FileMenu extends JMenu {
 
 	public FileMenu(ActionFactory actionFactory) {
 		super("File");
 
 		JMenuItem createItem = new JMenuItem("Create new shabad", KeyEvent.VK_C);
 		JMenuItem openItem = new JMenuItem("Open existing shabad",
 				KeyEvent.VK_O);
 		JMenuItem saveItem = new JMenuItem("Save current shabad", KeyEvent.VK_S);
 		JMenuItem propertiesItem = new JMenuItem("File properties",
 				KeyEvent.VK_P);
 
 		// TODO Sync these keystrokes with actionFactory in ShabadEditor
 		createItem.setAction(actionFactory.newCreateAction());
 		createItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				ActionEvent.CTRL_MASK));
 		openItem.setAction(actionFactory.newOpenAction());
 		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
 				ActionEvent.CTRL_MASK));
 		saveItem.setAction(actionFactory.newSaveAction());
 		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				ActionEvent.CTRL_MASK));
 
 		propertiesItem.setAction(actionFactory.newPropertiesAction());
 		propertiesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
 				ActionEvent.ALT_MASK));
 
 		setMnemonic(KeyEvent.VK_F);
 		add(createItem);
 		add(openItem);
 		add(saveItem);
 		addSeparator();
 		add(propertiesItem);
 	}
 }
