 package view.menu.subMenuPanels;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import inputOutput.GameIO;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import model.save.SavePath;
 
 import resources.MenuLookAndFeel;
 import resources.Translator;
 
 import view.menu.MenuButton;
 import view.menu.MenuToggleButton;
 
 /**
  * Panel to save and load the game.
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class SaveLoadGame extends SubMenuPanel {
 	
 	private static JButton loadButton;
 	private static JButton saveButton;
 	private static boolean canSave;
 	
 	/**
 	 * 
 	 * @param button the buttons to be added
 	 * @param bol <code>true<code> if the user is allowed to save.
 	 */
 	public SaveLoadGame(MenuButton[] button, boolean bol) {
 		super(getText(bol), getPanel(), button);
 		button[1].setEnabled(bol);
 		loadButton = button[0];
 		saveButton = button[1];
 		saveButton.setEnabled(false);
 		canSave = bol;
 		final JButton backB = button[2];
 		saveButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				backB.doClick();
 			}
 		});
 	}
 	private static String getText(boolean bol) {
 		if (bol){
 			return Translator.getMenuString("loadSave");
 		} else {
 			return Translator.getMenuString("load");
 		}
 	}
 	private static JPanel getPanel() {
 		JPanel p = new JPanel();
 		p.setLayout(new GridLayout(0, 1, MenuLookAndFeel.getGap(), MenuLookAndFeel.getGap()));
 				
 		String[] saveFiles = SavePath.getSaveFiles();
 		ButtonGroup group1 = new ButtonGroup();
 
 		final MenuToggleButton tb1 = new MenuToggleButton(Translator.getMenuString("newSave"));
 		group1.add(tb1);
 		tb1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadButton.setEnabled(true);
 				loadButton.setActionCommand(tb1.getText());
 				saveButton.setActionCommand(GameIO.NEW_NAME);
 				if(canSave) {
 					saveButton.setEnabled(true);
 				}
 			}
 		});
 		p.add(tb1);
 
		for(int i = saveFiles.length - 1; i >= 0; i--) {
 			final MenuToggleButton tb2 = new MenuToggleButton(saveFiles[i]);
 			group1.add(tb2);
 			tb2.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					loadButton.setEnabled(true);
 					loadButton.setActionCommand(tb2.getText());
 					saveButton.setActionCommand(tb2.getText());
 					if(canSave) {
 						saveButton.setEnabled(true);
 					}
 				}
 			});
 			p.add(tb2);
 		}
 		
 		return p;
 	}
 }
