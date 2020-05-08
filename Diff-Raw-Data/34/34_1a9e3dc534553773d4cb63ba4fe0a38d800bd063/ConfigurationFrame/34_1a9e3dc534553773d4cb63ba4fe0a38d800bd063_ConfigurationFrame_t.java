 package fr.ickik.formulamath.view;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.ickik.formulamath.FormulaMathException;
 import fr.ickik.formulamath.controler.FormulaMathController;
 import fr.ickik.formulamath.model.map.MapDimension;
 import fr.ickik.formulamath.model.player.PlayerManager;
 import fr.ickik.formulamath.model.player.PlayerType;
 
 /**
  * This frame helps the user to configure the game.
  * The user can choose between Human and Computer players;
  * give a name to every player.
  * @author Ickik.
 * @version 0.2.004, 5 June 2012
  */
 public class ConfigurationFrame extends AbstractFormulaMathFrame {
 
 	private final List<JToggleButton> togglePlayerTypeList = new ArrayList<JToggleButton>(PlayerManager.NUMBER_OF_PLAYER_MAX);
 	private final List<JTextField> nameTextFieldList = new ArrayList<JTextField>(PlayerManager.NUMBER_OF_PLAYER_MAX);
 	private final List<JLabel> labelList = new ArrayList<JLabel>(PlayerManager.NUMBER_OF_PLAYER_MAX);
 	private final Logger log = LoggerFactory.getLogger(ConfigurationFrame.class);
 	private int numberOfPlayerSelected = 1;
 	private int dimensionMapItem = 1;
 	//private int computerLevel = 1;
 	private final FormulaMathController controller;
 
 	/**
 	 * Default constructor. Creates the frame to configure the game.
 	 */
 	public ConfigurationFrame(FormulaMathController controller) {
 		this.controller = controller;
 	}
 	
 	public void display() {
 		createMainFrame();
 		displayFrame();
 		getFrame().toFront();
 	}
 
 	private void createMainFrame() {
 		JPanel panel = new JPanel(new BorderLayout());
 		panel.add(getConfigurationPanel(), BorderLayout.CENTER);
 		panel.add(getNumberPlayerPanel(), BorderLayout.NORTH);
 		File help = new File("./help.pdf");
 		if (help.exists()) {
 			log.debug("Help file exist");
 			panel.add(getButtonHelp(), BorderLayout.SOUTH);
 		} else {
 			log.debug("Help file not found");
 			panel.add(getButton(), BorderLayout.SOUTH);
 		}
 		getFrame().add(panel);
 	}
 
 	private JPanel getButton() {
 		JPanel panel = new JPanel(new GridLayout(1, 3));
 		JButton okButton = new JButton("OK");
 		okButton.setMnemonic(KeyEvent.VK_O);
 		getFrame().getRootPane().setDefaultButton(okButton);
 		okButton.addActionListener(getOKActionListener());
 		JButton cancelAndQuit = new JButton("Cancel & Quit");
 		cancelAndQuit.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
				try {
					controller.saveProperties();
				} catch (FormulaMathException e1) {
					log.error(e1.getMessage());
				}
 				close();
 				System.exit(0);
 			}
 		});
 		panel.add(okButton);
 		panel.add(cancelAndQuit);
 		return panel;
 	}
 	
 	private ActionListener getOKActionListener() {
 		return new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				if (!isHumanPlayer()) {
 					displayErrorMessage("No human player selected");
 					return;
 				}
 				controller.initManager(MapDimension.values()[dimensionMapItem].getValue());
 				log.trace("number of player selected : {}", numberOfPlayerSelected);
 				
 				for (int i = 0; i < numberOfPlayerSelected; i++) {//Si aucun joueur n'est selectionné, par defaut il est CPU
 					PlayerType type;
 					if (togglePlayerTypeList.get(i).isSelected()) {
 						type = PlayerType.HUMAN;
 					} else {
 						type = PlayerType.COMPUTER;
 					}
 					controller.addPlayer(type, nameTextFieldList.get(i).getText());
 				}
 				controller.closeConfigurationFrame();
 			}};
 	}
 	
 	private boolean isHumanPlayer() {
 		int nbHuman = 0;
 		for (int i = 0; i < numberOfPlayerSelected; i++) {
 			if (togglePlayerTypeList.get(i).isSelected()) {
 				nbHuman++;
 			}
 		}
 		if (nbHuman == 0) {
 			return false;
 		}
 		return true;
 	}
 
 	private JPanel getButtonHelp() {
 		JPanel panel = new JPanel(new GridBagLayout());
 		JButton okButton = new JButton("OK");
 		okButton.setMnemonic(KeyEvent.VK_O);
 		getFrame().getRootPane().setDefaultButton(okButton);
 		okButton.addActionListener(getOKActionListener());
 		JButton cancelAndQuit = new JButton("Cancel & Quit");
 		cancelAndQuit.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				close();
 				System.exit(0);
 			}
 		});
 		JButton helpButton = new JButton("help");
 		helpButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					controller.openHelpFile();
 				} catch (FormulaMathException exception) {
 					log.error("Help file not found or corrupted : {}", exception.getMessage());
 					displayErrorMessage(exception.getMessage());
 				}
 			}
 		});
 		GridBagConstraints grid = new GridBagConstraints();
 		grid.fill = GridBagConstraints.HORIZONTAL;
 		grid.gridx = 0;
 		grid.gridwidth = 2;
 		grid.weightx=1;
 		panel.add(okButton, grid);
 		grid.fill = GridBagConstraints.HORIZONTAL;
 		grid.gridx = 2;
 		grid.gridwidth = 2;
 		grid.weightx=1;
 		panel.add(cancelAndQuit, grid);
 		grid.fill = GridBagConstraints.HORIZONTAL;
 		grid.weightx=0.5;
 		grid.gridx = 4;
 		panel.add(helpButton, grid);
 		return panel;
 	}
 	
 	private JPanel getNumberPlayerPanel() {
 		GridLayout gridLayout = new GridLayout(2, 2);
 //		GridLayout gridLayout = new GridLayout(3, 2);
 		JPanel panel = new JPanel(gridLayout);
 
 		final JComboBox<String> sizeComboBox = new JComboBox<String>();
 		for (MapDimension d : MapDimension.values()) {
 			sizeComboBox.addItem(d.toString());
 		}
 		sizeComboBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dimensionMapItem = sizeComboBox.getSelectedIndex();
 			}
 		});
 		sizeComboBox.setSelectedIndex(dimensionMapItem);
 		
 		/*final JComboBox<String> levelComboBox = new JComboBox<String>(AILevelFactory.LEVEL);
 		levelComboBox.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				computerLevel = levelComboBox.getSelectedIndex();
 			}
 		});
 		sizeComboBox.setSelectedIndex(computerLevel);*/
 		
 		JLabel label = new JLabel("Number of player : ");
 		String[] nbPlayer = { "1", "2", "3", "4" };
 		final JComboBox<String> comboBox = new JComboBox<String>(nbPlayer);
 		comboBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				numberOfPlayerSelected = comboBox.getSelectedIndex() + 1;
 				for (int i = 0; i < numberOfPlayerSelected; i++) {
 					togglePlayerTypeList.get(i).setEnabled(true);
 					labelList.get(i).setEnabled(true);
 					nameTextFieldList.get(i).setEnabled(true);
 				}
 				for (int i = numberOfPlayerSelected; i < PlayerManager.NUMBER_OF_PLAYER_MAX; i++) {
 					togglePlayerTypeList.get(i).setEnabled(false);
 					labelList.get(i).setEnabled(false);
 					nameTextFieldList.get(i).setEnabled(false);
 				}
 			}
 		});
 		
 		//panel.add(new JLabel("Computer level : "));
 		//panel.add(levelComboBox);
 		panel.add(new JLabel("Dimension of the map : "));
 		panel.add(sizeComboBox);
 		panel.add(label);
 		panel.add(comboBox);
 		return panel;
 	}
 
 	private JPanel getConfigurationPanel() {
 		JPanel panel = new JPanel();
 		GridLayout gridLayout = new GridLayout(PlayerManager.NUMBER_OF_PLAYER_MAX, 3);
 		panel.setLayout(gridLayout);
 		for (int i = 0; i < PlayerManager.NUMBER_OF_PLAYER_MAX; i++) {
 			JToggleButton button = toggleButtonFactory();
 			togglePlayerTypeList.add(button);
 			panel.add(button);
 			JLabel lbl = new JLabel("Name : ");
 			panel.add(lbl);
 			labelList.add(lbl);
 			JTextField name = new JTextField();
 			nameTextFieldList.add(name);
 			panel.add(name);
 			if (i != 0) {
 				button.setEnabled(false);
 				lbl.setEnabled(false);
 				name.setEnabled(false);
 			} else {
 				button.setSelected(true);
 			}
 		}
 		return panel;
 	}
 	
 	private JToggleButton toggleButtonFactory() {
 		final JToggleButton button = new JToggleButton("Computer");
 		button.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				if (button.isSelected()) {
 					button.setText("Human");
 				} else {
 					button.setText("Computer");
 				}
 			}
 		});
 		return button;
 	}
 
 }
