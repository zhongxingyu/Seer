 package ch.bfh.monopoly.gui;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.io.IOException;
 
import javax.naming.CommunicationException;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 
 import ch.bfh.monopoly.common.Monopoly;
 
 
 public class WelcomePanel extends JFrame{
 
 	private JTextArea info;
 	private static final long serialVersionUID = 1L;
 	private JFrame board;
 
 	/**
 	 * Construct a WelcomePanel
 	 */
 	public WelcomePanel(JFrame board){
 		this.board = board;
 		JPanel main = new JPanel();
 		main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));
 
 		main.add(imageLogo());
 		main.add(clientConfig());
 		main.add(serverConfig());
 		main.add(infoArea());
 
 		add(main);
 	}
 
 	/**
 	 * Draw the panel with the image
 	 * @return a JPanel with the logo of monopoly
 	 */
 	private JPanel imageLogo(){
 		JPanel img = new JPanel();
 		java.net.URL urlImg = Monopoly.class.getResource("/ch/bfh/monopoly/resources/welcomelogo.png");
 		ImageIcon logo = new ImageIcon(urlImg);
 		JLabel imgLab = new JLabel(logo);
 		img.setMaximumSize(new Dimension(300,114));
 		img.add(imgLab);
 		img.setAlignmentX(Component.LEFT_ALIGNMENT);
 		return img;
 	}
 
 	/**
 	 * Draw the panel with the configuration for join a game
 	 * @return a JPanel with the fields and button for join a game
 	 */
 	private JPanel clientConfig(){
 		JPanel client = new JPanel();
 		client.setLayout(new BoxLayout(client, BoxLayout.PAGE_AXIS));
 		client.setBorder(BorderFactory.createTitledBorder("Join a game"));
 		client.setMaximumSize(new Dimension(300,0));
 
 		JLabel labelIP = new JLabel("IP");
 		JTextField serverIP = new JTextField();
 		
 		serverIP.setAlignmentX(Component.LEFT_ALIGNMENT);
 		serverIP.setMaximumSize(new Dimension(125,20));
 
 		JLabel labelPort = new JLabel("Port");
 		JTextField serverPort = new JTextField();
 		
 		serverPort.setAlignmentX(Component.LEFT_ALIGNMENT);
 		serverPort.setMaximumSize(new Dimension(125,20));
 
 		JButton connect = new JButton("Connect");
 		
 		client.add(labelIP);
 		client.add(serverIP);
 		client.add(labelPort);
 		client.add(serverPort);
 		client.add(Box.createRigidArea(new Dimension(0,10)));
 		client.add(connect);
 
 		return client;
 	}
 
 	/**
 	 * Draw the panel with used to start a new server
 	 * @return a JPanel with the fields and button used to start monopoly a server 
 	 */
 	private JPanel serverConfig(){
 		JPanel client = new JPanel();
 		client.setLayout(new BoxLayout(client, BoxLayout.PAGE_AXIS));
 		client.setBorder(BorderFactory.createTitledBorder("Start a server"));
 		client.setMaximumSize(new Dimension(300,0));
 
 		JLabel labelIP = new JLabel("IP");
 		
 		final JTextField serverIP = new JTextField();
 		serverIP.setMaximumSize(new Dimension(125,20));
 		serverIP.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		JLabel labelPort = new JLabel("Port");
 		
 		final JTextField serverPort = new JTextField();
 		serverPort.setMaximumSize(new Dimension(125,20));
 		serverPort.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		JLabel player = new JLabel("Players");
 
 		final SpinnerNumberModel numPlayers = new SpinnerNumberModel(2, 2, 8, 1);
 		JSpinner spinPanel = new JSpinner(numPlayers);
 		spinPanel.setMaximumSize(new Dimension(50,50));
 		spinPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		JLabel labelLang = new JLabel("Locale");
 		String menuLang[] = {"French", "English"};
 		JComboBox langs = new JComboBox(menuLang);
 		langs.setMaximumSize(new Dimension(125,20));
 		langs.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		final JButton connect = new JButton("Start server");
 
 		connect.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				connect.setEnabled(false);
 				int port, maxPlayers;
 				String ip;
 
 				try{
 					ip = serverIP.getText();
 					port = Integer.parseInt(serverPort.getText());
 					maxPlayers = (Integer) numPlayers.getNumber();
 					info.append("Starting the server on IP " + ip + 
 							" and port " + port + " with " + maxPlayers + " players...\n");
 					
 					dispose();
 					board.setVisible(true);
 					board.setExtendedState(JFrame.MAXIMIZED_BOTH);
 
 					/*try {
 						Monopoly.communicate.startServer(ip, port, maxPlayers);
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						info.append(e1.getMessage()+"\n");
 						connect.setEnabled(true);
 					}*/
 				}catch(NumberFormatException e1){
 					info.append("Please fill in all the fields\n");
 					connect.setEnabled(true);
 				}
 			}
 		});
 
 		client.add(labelIP);
 		client.add(serverIP);
 		client.add(labelPort);
 		client.add(serverPort);
 		client.add(player);
 		client.add(spinPanel);
 		client.add(labelLang);
 		client.add(langs);
 		
 		client.add(Box.createRigidArea(new Dimension(0,10)));
 		client.add(connect);
 
 		client.setAlignmentX(Component.LEFT_ALIGNMENT);
 		return client;
 	}
 
 	/**
 	 * Draw the text area with the informational messages
 	 * @return a JScrollPane used to alert the player
 	 */
 	private JScrollPane infoArea(){
 		info = new JTextArea();
 		info.setEditable(false);
 		info.setWrapStyleWord(true);
 		info.setLineWrap(true);
 
 		JScrollPane pane = new JScrollPane(info);
 		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		pane.setAlignmentX(Component.LEFT_ALIGNMENT);
 		return pane;
 	}
 
 
 }
