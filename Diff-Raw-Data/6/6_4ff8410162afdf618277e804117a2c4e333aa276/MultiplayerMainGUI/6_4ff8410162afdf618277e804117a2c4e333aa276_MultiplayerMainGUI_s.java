 package ReactorEE.swing;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 import ReactorEE.Networking.Message;
 import ReactorEE.Networking.SocketUtil;
 import ReactorEE.simulator.GUIRefresher;
 import ReactorEE.simulator.PlantController;
 import ReactorEE.simulator.ReactorUtils;
 import ReactorEE.sound.Sound;
 
 public class MultiplayerMainGUI extends MainGUI{
 
 	private String saboteurIP;
 	JLabel lblAvailableSabos = new JLabel("Available Sabotages  0");
 	GUIRefresher guir = null;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					@SuppressWarnings("unused")
 					MultiplayerMainGUI window = new MultiplayerMainGUI(new PlantController(new ReactorUtils()), "localhost");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 
 	/**
 	 * Create the application.
 	 * @wbp.parser.entryPoint
 	 */
 	public MultiplayerMainGUI(final PlantController plantController, String IP) {
 		super(plantController);
 		this.saboteurIP = IP;
 		
		layeredPane.remove(1);
 		java.net.URL imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/plantBackgroundSabo.png");
         ImageIcon backgroundImageIcon = new ImageIcon(imageURL);
         JLabel backgroundImageLabel = new JLabel(backgroundImageIcon);
         backgroundImageLabel.setBackground(new Color(0, 153, 0));
         backgroundImageLabel.setBounds(0, 0, 1040, 709);
         layeredPane.add(backgroundImageLabel, 1);
 		
 		
 		btnRepairPump1.removeActionListener(btnRepairPump1.getActionListeners()[0]);
 		btnRepairPump1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	Sound.play(Sound.DEFAULT_BUTTON_CLICK);
                 try {
                 	if(guir.useSabo())
                 		new Message().run("pump1", saboteurIP, SocketUtil.SABOTAGE_LISTENER_PORT_NO);
 				} catch (Exception e1) {e1.printStackTrace();
 					JOptionPane.showMessageDialog(getFrame(), "Unable to connect to Operator", "Connection Error", JOptionPane.ERROR_MESSAGE);
 				}
             }
         });
 		
 		btnRepairPump2.removeActionListener(btnRepairPump2.getActionListeners()[0]);
 		btnRepairPump2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	Sound.play(Sound.DEFAULT_BUTTON_CLICK);
             	try {
                 	if(guir.useSabo())
                 		new Message().run("pump2", saboteurIP, SocketUtil.SABOTAGE_LISTENER_PORT_NO);
 				} catch (Exception e1) {e1.printStackTrace();
 					JOptionPane.showMessageDialog(getFrame(), "Unable to connect to Operator", "Connection Error", JOptionPane.ERROR_MESSAGE);
 				}
             }
         });
 		
 		btnRepairPump3.removeActionListener(btnRepairPump3.getActionListeners()[0]);
 		btnRepairPump3.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	Sound.play(Sound.DEFAULT_BUTTON_CLICK);
             	try {
                 	if(guir.useSabo())
                 		new Message().run("pump3", saboteurIP, SocketUtil.SABOTAGE_LISTENER_PORT_NO);
 				} catch (Exception e1) {e1.printStackTrace();
 					JOptionPane.showMessageDialog(getFrame(), "Unable to connect to Operator", "Connection Error", JOptionPane.ERROR_MESSAGE);
 				}
             }
         });
 		
 		btnRepairTurbine.removeActionListener(btnRepairTurbine.getActionListeners()[0]);
 		btnRepairTurbine.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	Sound.play(Sound.DEFAULT_BUTTON_CLICK);
             	try {
                 	if(guir.useSabo())
                 		new Message().run("turbine", saboteurIP, SocketUtil.SABOTAGE_LISTENER_PORT_NO);
 				} catch (Exception e1) {e1.printStackTrace();
 					JOptionPane.showMessageDialog(getFrame(), "Unable to connect to Operator", "Connection Error", JOptionPane.ERROR_MESSAGE);
 				}
             }
         });
 		
 		btnRepairOperatingSoftware.removeActionListener(btnRepairOperatingSoftware.getActionListeners()[0]);
 		btnRepairOperatingSoftware.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	Sound.play(Sound.DEFAULT_BUTTON_CLICK);
             	try {
                 	if(guir.useSabo())
                 		new Message().run("operator software", saboteurIP, SocketUtil.SABOTAGE_LISTENER_PORT_NO);
 				} catch (Exception e1) {e1.printStackTrace();
 					JOptionPane.showMessageDialog(getFrame(), "Unable to connect to Operator", "Connection Error", JOptionPane.ERROR_MESSAGE);
 				}
             }
         });
 		
 		//disable step button as sabateur cannot controll playing of the game. keeping it allows saboteur to see whether the game is playing or not.
 		btnStep.removeActionListener(btnStep.getActionListeners()[0]);
 		
 		//initialise label displaying the number of sabtages that that sabateur has.
         lblAvailableSabos.setFont(new Font("Tahoma", Font.PLAIN, 30));
        layeredPane.setLayer(lblAvailableSabos, 2);
         lblAvailableSabos.setBounds(90, 493, 315, 59);
         layeredPane.add(lblAvailableSabos);
         
         //disable gui componnets that are not needed in multiplayer
       	btnLoad.setEnabled(false);
       	btnLoad.setVisible(false);
       	btnSave.setEnabled(false);
       	btnSave.setVisible(false);
       	//Move remaining buttons
       	btnShowManual.setBounds(btnLoad.getX(), btnShowManual.getY(), btnShowManual.getWidth(), btnShowManual.getHeight());
       	btnShowScores.setBounds(btnSave.getX(), btnShowScores.getY(), btnShowScores.getWidth(), btnShowScores.getHeight());
       	
       	btnNewGame.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					new Message().run("ANCHOVY KILL", Inet4Address.getLocalHost().getHostAddress(), SocketUtil.GAMESTATE_LISTENER_PORT_NO);
 				} catch (UnknownHostException e1) {
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				//Reset game values to default - makes it obvious that the multiplayer game is no longer active.
 				plantController.newMultiplayerGame(initialNameValue);
 			}
 		});
 	}
 	
 	@Override
 	public void endGameHandler(){
 		@SuppressWarnings("unused")
 		EndGameGUI endGameGui = new EndGameGUI(this, plantController.getUIData().getScore());
 	}
 	
 	@Override
 	public void updateGUI(){
 		super.updateGUI();
 		if(lblAvailableSabos != null && guir!= null){
 			lblAvailableSabos.setText("Available Sabotages  " + guir.getNumberOfAvailableSabotages());
 		}
 		
 	}
 	
 	public void setGUIRefresher(GUIRefresher guir){
 		this.guir = guir;
 	}
 
 	
 }
 
