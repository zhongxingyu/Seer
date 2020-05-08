 package view.menu.subMenuPanels;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import controller.GameController;
 import controller.IO.GameIO;
 
 import view.menu.MenuButton;
 
 /**
  * not done mvc
  * Panel to save and load the game.
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class SaveLoadGame extends SubMenuPanel{
 	
 	/**
 	 * 
 	 * @param button the escape button to previously menu.
 	 * @param bol <code>true<code> if saving is allowed.
 	 * @param controller the controller to save the gamestate from.
 	 */
 	public SaveLoadGame(MenuButton button, boolean bol, GameController controller) {
		super(getText(bol), getPanel(), new MenuButton[]{getLoadButton(controller), getSaveButton(bol, controller), button});
 	}
	private static MenuButton getLoadButton(final GameController controller) {
 		MenuButton b = new MenuButton("Load");
 		b.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
				controller.init(GameIO.loadGame());
 			}
 		});
 		return b;
 	}
 
 	private static MenuButton getSaveButton(boolean bol, final GameController controller) {
 		MenuButton b = new MenuButton("Save");
 		b.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				GameIO.saveGame(controller);
 			}
 		});
 		b.setEnabled(bol);
 		return b;
 	}
 	private static String getText(boolean bol) {
 		if (bol){
 			return "Load / Save Game";
 		} else {
 			return "Load Game";
 		}
 	}
 	private static JPanel getPanel() {
 		// TODO Auto-generated method stub
 		JPanel panel = new JPanel();
 		panel.add(new JLabel("Load/SavePanel  A list of saved games should be visible"));
 		return panel;
 	}
 
 }
