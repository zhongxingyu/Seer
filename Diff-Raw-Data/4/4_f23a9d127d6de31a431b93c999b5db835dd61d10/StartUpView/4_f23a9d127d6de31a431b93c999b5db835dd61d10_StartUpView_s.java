 package at.fhv.audioracer.simulator.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import at.fhv.audioracer.client.player.IPlayerClientListener;
 import at.fhv.audioracer.client.player.PlayerClient;
 import at.fhv.audioracer.core.model.Player;
 
 @SuppressWarnings("serial")
 public class StartUpView extends JFrame {
 	
 	private JButton _btnReady;
 	private JButton _btnConnect;
 	private JButton _btnSetName;
 	
 	private JPanel contentPane;
 	
 	private JTextField _NameField;
 	
 	private PlayerClient _playerClient;
 	
 	private JList<Byte> _listFreeCars;
 	private JList<Player> _listConnectedPlayers;
 	
 	private JLabel _lblConnected;
 	private JLabel _lblReady;
 	
 	public static void main(String[] args) {
 		PlayerClient pc = new PlayerClient();
 		StartUpView suv = new StartUpView(pc);
 		suv.setVisible(true);
 		
 	}
 	
 	/**
 	 * Create the frame.
 	 */
 	public StartUpView(PlayerClient playerClient) {
 		
 		_playerClient = playerClient;
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblFreeCarsLabel = new JLabel("Free cars");
 		lblFreeCarsLabel.setBounds(10, 11, 80, 14);
 		contentPane.add(lblFreeCarsLabel);
 		
 		_listFreeCars = new JList<>();
 		_listFreeCars.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		_listFreeCars.setBounds(10, 24, 80, 227);
 		contentPane.add(_listFreeCars);
 		_listFreeCars.addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				
 				Byte selected = _listFreeCars.getSelectedValue();
 				if (selected != null) {
 					if (_playerClient.getPlayerServer().selectCar(selected)) {
 						ControlView cv = new ControlView(_playerClient);
 						cv.setVisible(true);
 					}
 				}
 			}
 		});
 		
 		_playerClient.getListenerList().add(new IPlayerClientListener() {
 			
 			@Override
 			public void onUpdateGameState(int playerId) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onUpdateFreeCars() {
 				Byte[] temp = new Byte[_playerClient.getFreeCarIds().length];
 				for (int i = 0; i < temp.length; i++) {
 					temp[i] = _playerClient.getFreeCarIds()[i];
 				}
 				_listFreeCars.setListData(temp);
 				
 			}
 			
 			@Override
			public void onUpdateCheckpointDirection() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onPlayerDisconnected(int playerId) {
 				onPlayerConnected(playerId);
 				
 			}
 			
 			@Override
 			public void onPlayerConnected(int playerId) {
 				Player[] players = new Player[_playerClient.getPlayers().size()];
 				_listConnectedPlayers.setListData(_playerClient.getPlayers().values()
 						.toArray(players));
 				
 			}
 			
 			@Override
 			public void onGameStarts() {
 				Thread t = new Thread(new Runnable() {
 					
 					@Override
 					public void run() {
 						
 						_playerClient.getPlayerServer().updateVelocity(_playerClient.getSpeed(),
 								_playerClient.getDirection());
 						try {
 							Thread.sleep(40);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				});
 				t.start();
 			}
 		});
 		
 		_btnReady = new JButton("Ready");
 		_btnReady.setBounds(100, 228, 89, 23);
 		contentPane.add(_btnReady);
 		
 		_btnConnect = new JButton("Connect");
 		_btnConnect.setBounds(199, 228, 89, 23);
 		contentPane.add(_btnConnect);
 		
 		_btnSetName = new JButton("setName");
 		_btnSetName.setBounds(298, 228, 89, 23);
 		contentPane.add(_btnSetName);
 		
 		_NameField = new JTextField();
 		_NameField.setText("GUEST");
 		_NameField.setBounds(301, 197, 86, 20);
 		contentPane.add(_NameField);
 		_NameField.setColumns(10);
 		
 		_listConnectedPlayers = new JList<>();
 		_listConnectedPlayers.setEnabled(false);
 		_listConnectedPlayers.setBounds(298, 24, 89, 168);
 		contentPane.add(_listConnectedPlayers);
 		
 		JLabel _lblConnectedPlayers = new JLabel("Connected Players");
 		_lblConnectedPlayers.setBounds(298, 11, 126, 14);
 		contentPane.add(_lblConnectedPlayers);
 		
 		_lblConnected = new JLabel("disconnected");
 		_lblConnected.setBounds(199, 200, 89, 14);
 		contentPane.add(_lblConnected);
 		
 		_lblReady = new JLabel("Unready");
 		_lblReady.setBounds(100, 200, 89, 14);
 		contentPane.add(_lblReady);
 		
 		_btnConnect.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					if (!_playerClient.hasConnection()) {
 						_playerClient.startClient(_playerClient.getPlayer().getName(), "localhost");
 						_btnConnect.setText("Disconnect");
 						_lblConnected.setText("Connected");
 					} else {
 						_playerClient.stopClient();
 						_btnConnect.setText("Connect");
 						_lblConnected.setText("Disconnected");
 					}
 				} catch (IOException e1) {
 					_btnConnect.setText("Connect");
 					_lblConnected.setText("Disconnected");
 					e1.printStackTrace();
 				}
 			}
 		});
 		
 		_btnSetName.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				
 				_playerClient.getPlayer().setName(_NameField.getText());
 				
 			}
 		});
 		
 		_btnReady.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				
 				_playerClient.getPlayerServer().setPlayerReady();
 				_lblReady.setText("Ready");
 				_btnReady.setText("Unready");
 				
 			}
 		});
 	}
 }
