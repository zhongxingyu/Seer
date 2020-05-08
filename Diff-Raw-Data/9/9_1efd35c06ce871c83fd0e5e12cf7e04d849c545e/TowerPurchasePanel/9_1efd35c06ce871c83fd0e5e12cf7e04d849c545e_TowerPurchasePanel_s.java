 package src.ui.side;
 
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import java.util.ArrayList;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import src.FilePaths;
 import src.core.Damage;
 import src.core.Tower;
 import src.ui.controller.GameController;
 
 /**
  * Panel displaying buttons that allow the user to purchase a tower.
  */
 public class TowerPurchasePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	private GameController gc;
 	
 	private static final String purchaseTowersText = "Purchase Towers:";
 	
 	private JLabel purchaseTowersLabel;
 	private TowerStatsPanel towerStats;
 	private JButton[] towerButtons;
 	private Action[] towerButtonActions;
 	private Tower.Type[] buttonTypes = {Tower.Type.GUN, Tower.Type.ANTIAIR, Tower.Type.SLOWING, Tower.Type.MORTAR,
 			  							Tower.Type.FRIEND, Tower.Type.FLAME, Tower.Type.STASIS, Tower.Type.HTA};
 	
 	public TowerPurchasePanel(GameController controller) {
 		super(new GridBagLayout());
 		
 		gc = controller;
 		
 		purchaseTowersLabel = new JLabel(purchaseTowersText);
 		towerStats = new TowerStatsPanel(gc);
 		towerButtons = new JButton[8];
 		towerButtonActions = new Action[8];
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.weightx = 1;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.insets.set(0, 0, 10, 0);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		add(purchaseTowersLabel, c);
 		
 		// initialize a purchase button for each of the towers
 		for (int index = 0; index < 8; index++) {
 			String path = FilePaths.towersPath + "tower-icon"+(index+1)+".png";
 			
 			ImageIcon towerIcon = new ImageIcon(path);
 			final JButton towerButton = new JButton(towerIcon);
 			
 			towerButton.setBorder(BorderFactory.createEmptyBorder());
 			towerButton.setContentAreaFilled(false);
 
 			towerButtons[index] = towerButton;
 			
 			final Tower.Type type = buttonTypes[index];
 			towerButtonActions[index] = new AbstractAction() {
 				public void actionPerformed(ActionEvent e) {
 					gc.beginPurchasingTower(Tower.createTower(type));
 				}
 			};
 			
 			// set up buttons
 			towerButton.addActionListener(towerButtonActions[index]);
 			
 			// set up equivalent key bindings
 			Integer idx = index;
 			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(Character.forDigit(index + 1, 10)), idx);
 			this.getActionMap().put(idx, towerButtonActions[index]);
 			
 			// set up mouse hover on buttons
 			towerButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					towerStats.setTower(Tower.createTower(type));
 						gc.getSideBar().getPlayerStatsPanel().setShowingGoldChange(true);
 						gc.getSideBar().getPlayerStatsPanel().setGoldChange(" -" + Tower.createTower(type).getPrice());
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					towerStats.setTower(null);
 					gc.getSideBar().getPlayerStatsPanel().setShowingGoldChange(false);
 					gc.getSideBar().getPlayerStatsPanel().setGoldChange("");
 				}
 			});
 			
 			if(index == 7)
 				c.gridx = 2;
 			else
 				c.gridx = 0 + index % 3;
 
 			c.gridy = 1 + index / 3;
 			c.ipadx = 4;
 			c.insets.set(0, 5, 0, 5);
 			c.fill = GridBagConstraints.NONE;
 			add(towerButton, c);
 		}
 		
 		updateAllowedButtons();
 
 		c.insets = new Insets(20, 0, 0, 0);
 		c.gridx = 0;
 		c.gridy = 4;
 		c.gridwidth = 4;
 		c.anchor = GridBagConstraints.CENTER;
 		add(towerStats, c);
 	}
 	
 	private void updateAllowedButtons() {
 		for (int i = 0; i < 8; i++) {
 			JButton b = towerButtons[i];
 			Action a = towerButtonActions[i];
 			Tower.Type type = buttonTypes[i];
 			
 			Tower t = Tower.createTower(type);
 			
 			if (!gc.playerCanAfford(t)) {
 				b.setEnabled(false);
 				a.setEnabled(false);
 			} else if (!gc.getPaused()){
 				b.setEnabled(true);
 				a.setEnabled(true);
 			}
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		updateAllowedButtons();
 	}
 	
 	public void disableTowerPurchase(){
 		for (int x = 0; x< towerButtons.length; x++){
 			towerButtons[x].setEnabled(false);
 			towerButtonActions[x].setEnabled(false);
 		}
 	}
 	
 	public void enableTowerPurchase(){
 		for (int x = 0; x< towerButtons.length; x++){
 			towerButtons[x].setEnabled(true);
 			towerButtonActions[x].setEnabled(true);
 		}
 	}
 }
