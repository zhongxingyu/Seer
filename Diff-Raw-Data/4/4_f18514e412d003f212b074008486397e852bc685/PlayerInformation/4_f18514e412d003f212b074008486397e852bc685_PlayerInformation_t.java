 package App.view;
 
 import App.listener.ConfirmPlayerCreationListener;
 import App.listener.IncrementListener;
 
 import java.awt.FlowLayout;
 import java.awt.Dimension;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import java.awt.Component;
 import javax.swing.Box;
 
 public class PlayerInformation extends JPanel {
 
 	/**
 	 * Prevents "serializable" warning
 	 */
 	private static final long serialVersionUID = -3313579360751444648L;
 	private Display gameFrame;
     private JTextField enteredPlayerName;
     private JFormattedTextField enteredPilotSkill;
     private JFormattedTextField enteredFighterSkill;
     private JFormattedTextField enteredTraderSkill;
     private JFormattedTextField enteredEngineerSkill;
     private JLabel ErrorLabel;
 
     /**
 	 * Create the panel.
 	 */
 	public PlayerInformation(Display gameFrame) {
 		this.gameFrame = gameFrame;
 
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         
         Component verticalStrut = Box.createVerticalStrut(20);
         add(verticalStrut);
 
         JPanel playerName = new JPanel();
         add(playerName);
         playerName.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 
         JLabel lblPlayerName = new JLabel("Player Name");
         playerName.add(lblPlayerName);
 
 
         enteredPlayerName = new JTextField();
         playerName.add(enteredPlayerName);
         enteredPlayerName.setColumns(10);
 
         JPanel PilotSkill = new JPanel();
         add(PilotSkill);
 
         JLabel lblPilotSkill = new JLabel("Pilot Skill Points");
         PilotSkill.add(lblPilotSkill);
 
         enteredPilotSkill = new JFormattedTextField();
         enteredPilotSkill.setText("0");
         enteredPilotSkill.setColumns(2);
         PilotSkill.add(enteredPilotSkill);
         
         JButton PInc = new JButton("+");
         PInc.addActionListener(new IncrementListener(enteredPilotSkill, IncrementListener.INC));
         PilotSkill.add(PInc);
         
         JButton PDec = new JButton("-");
         PDec.addActionListener(new IncrementListener(enteredPilotSkill, IncrementListener.DEC));
         PilotSkill.add(PDec);
 
         JPanel FighterSkill = new JPanel();
         add(FighterSkill);
 
         JLabel lblFighterSkill = new JLabel("Fighter Skill Points");
         FighterSkill.add(lblFighterSkill);
 
         enteredFighterSkill = new JFormattedTextField();
         enteredFighterSkill.setText("0");
         enteredFighterSkill.setColumns(2);
         FighterSkill.add(enteredFighterSkill);
         
         JButton FInc = new JButton("+");
         FInc.addActionListener(new IncrementListener(enteredFighterSkill, IncrementListener.INC));
         FighterSkill.add(FInc);
         
         JButton FDec = new JButton("-");
         FDec.addActionListener(new IncrementListener(enteredFighterSkill, IncrementListener.DEC));
         FighterSkill.add(FDec);
 
         JPanel TraderSkill = new JPanel();
         add(TraderSkill);
 
         JLabel lblTraderSkill = new JLabel("Trader Skill Points");
         TraderSkill.add(lblTraderSkill);
 
         enteredTraderSkill = new JFormattedTextField();
         enteredTraderSkill.setText("0");
         enteredTraderSkill.setColumns(2);
         TraderSkill.add(enteredTraderSkill);
         
         JButton TInc = new JButton("+");
         TInc.addActionListener(new IncrementListener(enteredTraderSkill, IncrementListener.INC));
         TraderSkill.add(TInc);
         
         JButton TDec = new JButton("-");
         TDec.addActionListener(new IncrementListener(enteredTraderSkill, IncrementListener.DEC));
         TraderSkill.add(TDec);
 
         JPanel EngineerSkill = new JPanel();
         add(EngineerSkill);
 
         JLabel lblEngineerSkill = new JLabel("Engineer Skill Points");
         EngineerSkill.add(lblEngineerSkill);
 
         enteredEngineerSkill = new JFormattedTextField();
         enteredEngineerSkill.setText("0");
         enteredEngineerSkill.setColumns(2);
         EngineerSkill.add(enteredEngineerSkill);
         
         JButton EInc = new JButton("+");
         EInc.addActionListener(new IncrementListener(enteredEngineerSkill, IncrementListener.INC));
         EngineerSkill.add(EInc);
         
         JButton EDec = new JButton("-");
         EDec.addActionListener(new IncrementListener(enteredEngineerSkill, IncrementListener.DEC));
         EngineerSkill.add(EDec);
 
         JPanel Difficulty = new JPanel();
         add(Difficulty);
 
         JLabel lblDifficulty = new JLabel("Difficulty");
         Difficulty.add(lblDifficulty);
 
         JComboBox<String> comboBox = new JComboBox<String>();
         comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Easy", "Medium", "Hard", "Impossible"}));
         Difficulty.add(comboBox);
 
        JPanel Confirm = new JPanel();
         add(Confirm);
 
         JButton btnConfirm = new JButton("Confirm");
         btnConfirm.setPreferredSize(new Dimension(120, 30));
         btnConfirm.addActionListener(new ConfirmPlayerCreationListener(gameFrame,this));
         Confirm.add(btnConfirm);
         
         JPanel ErrorPanel = new JPanel();
         add(ErrorPanel);
         
         ErrorLabel = new JLabel("");
         ErrorPanel.add(ErrorLabel);
 
 	}
 
     public void setErrorMessage(String message){
     	ErrorLabel.setText(message);
     }
 
     public String getTxtEnterPlayerNameData() {
         return enteredPlayerName.getText();
     }
 
     public int getEnteredPilotSkill() {
         return Integer.parseInt(enteredPilotSkill.getText());
     }
 
     public int getEnteredFighterSkill() {
         return Integer.parseInt(enteredFighterSkill.getText());
     }
 
     public int getEnteredTraderSkill() {
         return Integer.parseInt(enteredTraderSkill.getText());
     }
 
     public int getEnteredEngineerSkill() {
         return Integer.parseInt(enteredEngineerSkill.getText());
     }
 }
