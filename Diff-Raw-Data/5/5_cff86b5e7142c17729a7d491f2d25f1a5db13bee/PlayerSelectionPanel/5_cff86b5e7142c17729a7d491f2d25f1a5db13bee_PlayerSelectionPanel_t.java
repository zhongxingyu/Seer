 package warborn.view;
 
 import java.awt.Color;
 
 import javax.swing.*;
 
 import warborn.constants.PlayerData;
 
 public class PlayerSelectionPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	public JTextField tfPlayerName;
 	public JButton btColor;
 	public JComboBox<String> cbRace, cbGod;
 	
 	/**
 	 * Create the panel.
 	 */
 	public PlayerSelectionPanel(int number) {
 		
 		
 		setLayout(null);
 		
 		Color color = Color.BLACK;
 		String name = "Player 1";
 		switch(number){
 		case 1:
 			color = new Color(0, 0, 200);
 			name = "Erez";
 			break;
 		case 2:
 			color = Color.RED;
 			name = "Metho";
 			break;
 		case 3:
 			color = Color.GREEN;
 			name = "Rirouu";
 			break;
 		case 4:
 			color = Color.MAGENTA;
 			name = "Inaria";
 			break;
 		}
 		
 		btColor = new JButton();
 		btColor.setLocation(20, 20);
 		btColor.setSize(50, 50);
 		btColor.setBackground(color);
 		add(btColor);
 		
 		tfPlayerName = new JTextField(name);
 		tfPlayerName.setLocation(20, 80);
 		tfPlayerName.setSize(100, 30);
 		add(tfPlayerName);
 		
 		JLabel lbRace = new JLabel("Race:");
 		lbRace.setLocation(162, 20);
 		lbRace.setSize(50, 30);
 		add(lbRace);
 		
 		JLabel lbGod = new JLabel("God:");
 		lbGod.setLocation(130, 61);
 		lbGod.setSize(100, 30);
 		add(lbGod);
 
 		cbRace = new JComboBox<String>();
 		cbRace.setLocation(212, 20);
 		cbRace.setSize(100, 30);
 		cbRace.setBackground(Color.WHITE);
 		cbRace.setModel(getRaces());
 		add(cbRace);
 		
 		cbGod = new JComboBox<String>();
 		cbGod.setLocation(212, 61);
 		cbGod.setSize(100, 30);
 		cbGod.setBackground(Color.WHITE);
 		cbGod.setModel(getGods());
 		add(cbGod);
 		
 	}
 
 	private ComboBoxModel<String> getGods() {
 		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<String>();
 		for(int i = 0; i < PlayerData.getNumberOfGods(); i++){
 			boxModel.addElement(PlayerData.getGodName(i));
 		}
		boxModel.addElement("Any");
 		return boxModel;
 	}
 
 	private ComboBoxModel<String> getRaces() {
 		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<String>();
 		for(int i = 0; i < PlayerData.getNumberOfRaces(); i++){
 			boxModel.addElement(PlayerData.getRaceName(i));
 		}
		boxModel.addElement("Random");
 		return boxModel;
 	}
 	
 	public String getPlayerName(){
 		return tfPlayerName.getText();
 	}
 	
 	public Color getPlayerColor(){
 		return btColor.getBackground();
 	}
 	
 	public int getPlayerRace(){
 		return cbRace.getSelectedIndex();
 	}
 	
 	public int getPlayerGod(){
 		return cbGod.getSelectedIndex();
 	}
 	
 	public JButton getColorButton(){
 		return btColor;
 	}
 	
 	public JComboBox<String> getGodComboBox(){
 		return cbGod;
 	}
 	
 	public JComboBox<String> getRaceComboBox(){
 		return cbRace;
 	}
 
 }
