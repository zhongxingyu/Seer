 package client.login;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import models.FailedResult;
 import models.FalseResult;
 import server.ClientCommunicator;
 import communicator.ValidateUserParams;
 import communicator.ValidateUserResult;
 import client.ClientGUI;
 import client.SearchGUI;
 import client.TextPrompt;
 import client.TextPrompt.Show;
 
 public class LoginGUI extends JFrame{
 
 	private static final long serialVersionUID = -3795015357219531829L;
 	private JTextField username;
 	private JTextField password;
 	private String host;
 	private String port;
 	private ClientGUI cg;
 	
 	public LoginGUI(final String host, final String port){
 		super("Login GUI");
 		
 		this.host = host;
 		this.port = port;
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Dimension panelSize = new Dimension(400, 100);
 		this.setSize(panelSize);
 		this.setMinimumSize(panelSize);
 		this.setMaximumSize(panelSize);
 		Container pane = this.getContentPane();
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		pane.setLayout(new GridBagLayout());
 		
 		JLabel u = new JLabel("Username:");
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 		pane.add(u, c);
 		
 		JLabel p = new JLabel("Password:");
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 1;
 		pane.add(p, c);
 		
 		username = new JTextField();
 		TextPrompt usernameTextPrompt = new TextPrompt("", username);
 //		usernameTextPrompt.changeAlpha(.75f);
 //		usernameTextPrompt.setShow(Show.FOCUS_LOST);
 		c.weightx = 7;
 		c.gridx = 1;
 		c.gridy = 0;
 		pane.add(username, c);
 
 		password = new JPasswordField();
 		TextPrompt passwordTextPrompt = new TextPrompt("", password);
 //		passwordTextPrompt.changeAlpha(.75f);
 //		passwordTextPrompt.setShow(Show.FOCUS_LOST);
 		c.weightx = 7;
 		c.gridx = 1;
 		c.gridy = 1;
 		pane.add(password, c);
 		
		username.setText("test1");
		password.setText("test1");
 		
 		
 		JPanel jp = new JPanel();
 		jp.setLayout(new GridBagLayout());
 		
 		
 		JButton loginButton = new JButton("Login");
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 		loginButton.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(username.getText().length() > 0 && password.getText().length() > 0) 
 					validateUser(username.getText(), password.getText(), host, port);
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				
 			}
 			
 		});
 		jp.add(loginButton, c);
 		
 		JButton exitButton = new JButton("Exit");
 		c.weightx = 1;
 		c.gridx = 2;
 		c.gridy = 0;
 		exitButton.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
				if(username.getText().length() > 0 && password.getText().length() > 0) 
					validateUser(username.getText(), password.getText(), host, port);
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				
 			}
 			
 		});
 		jp.add(exitButton, c);
 		
 		c.gridx = 1;
 		c.gridy = 3;
 		pane.add(jp, c);
 		this.pack();
 	}
 	
 	public void loadClient(){
 		System.out.println("logingui loading client");
 		this.setVisible(false);
 		this.cg = new ClientGUI(username.getText(), password.getText(), host, port, this);
 	}
 	
 	public class LoginSuccess extends JFrame{
 		private static final long serialVersionUID = -3174658209839081083L;
 		
 		LoginGUI lg = null;
 		
 		LoginSuccess(ValidateUserResult vur, LoginGUI lg){
 			super("Welcome to Indexer");
 			System.out.println("Login Success");
 			this.lg = lg;
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			Dimension panelSize = new Dimension(400, 100);
 			this.setSize(panelSize);
 			this.setMinimumSize(panelSize);
 			this.setMaximumSize(panelSize);
 			Container pane = this.getContentPane();
 			GridBagConstraints c = new GridBagConstraints();
 			c.fill = GridBagConstraints.HORIZONTAL;
 			pane.setLayout(new GridBagLayout());
 			
 			JLabel label = new JLabel("Welcome, " + vur.getFirstName() + " " + vur.getLastName() + ".\nYou have indexed " + vur.getCount() + " records.");
 			label.setVisible(true);
 			
 			JButton button = new JButton("OK");
 			c.weightx = 1;
 			c.gridx = 2;
 			c.gridy = 0;
 			button.setVisible(true);
 			button.addMouseListener(new MouseListener(){
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					loadClient();// success pane
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 					
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					
 				}
 				
 			});
 			
 			pane.add(button);
 			pane.add(label);
 			this.setVisible(true);
 		}
 		
 		public void loadClient(){
 			this.setVisible(false);
 			lg.loadClient();
 		}
 	}
 	
 	public class LoginError extends JFrame{
 		private static final long serialVersionUID = -4243880986399306282L;
 		LoginGUI lg = null;
 		
 		LoginError(ValidateUserResult vur, LoginGUI lg){
 			super("Login Failed");
 			System.out.println("Login Error");
 			this.lg = lg;
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			Dimension panelSize = new Dimension(400, 100);
 			this.setSize(panelSize);
 			this.setMinimumSize(panelSize);
 			this.setMaximumSize(panelSize);
 			Container pane = this.getContentPane();
 			GridBagConstraints c = new GridBagConstraints();
 			c.fill = GridBagConstraints.HORIZONTAL;
 			pane.setLayout(new GridBagLayout());
 			
 			JLabel label = new JLabel("Invalid Username and/or Password.");
 			label.setVisible(true);
 			
 			JButton button = new JButton("OK");
 			c.weightx = 1;
 			c.gridx = 2;
 			c.gridy = 0;
 			button.setVisible(true);
 			button.addMouseListener(new MouseListener(){
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					System.out.println("mouse clicked");
 					closeSelf();
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 					
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					
 				}
 				
 			});
 			
 			pane.add(button);
 			pane.add(label);
 			this.setVisible(true);
 		}
 		
 		public void closeSelf(){
 			System.out.println("close self");
 			this.setVisible(false);
 		}
 	}
 	
 	private void validateUser(String username, String password, String host, String port){
 		System.out.println("validating user");
 		ClientCommunicator cc = new ClientCommunicator();
 		ValidateUserParams var = new ValidateUserParams();
 		var.setUsername(username);
 		var.setPassword(password);
 		try {
 			Object response = cc.validateUser(var, host, port);
 			if(response instanceof ValidateUserResult){
 				this.setVisible(false);
 				LoginSuccess ls = new LoginSuccess((ValidateUserResult)response, this);
 				
 			}else if(response instanceof FailedResult){
 				System.out.println("failed");
 				LoginError le = new LoginError((ValidateUserResult)response, this);
 			}else if(response instanceof FalseResult){
 				System.out.println("false");
 				LoginError le = new LoginError((ValidateUserResult)response, this);
 				le.setVisible(true);
 			}
 		} catch (Exception e) {
 //			result = "FAILED";
 		}finally{
 //			getView().setResponse(result);
 		}
 	}
 	
 	public static void main(final String[] args) {
 		EventQueue.invokeLater(new Runnable() {		
 			public void run() {
 				LoginGUI loginGui = new LoginGUI(args[0], args[1]);
 //				LoginGUI loginGui = new LoginGUI("localhost", "39640");
 				loginGui.setVisible(true);
 //				// Create the frame window object
 //				SimpleFrame frame = new SimpleFrame();
 //
 //				// Make the frame window visible
 //				frame.setVisible(true);
 			}
 		});
 	}
 
 	public void resetView() {
 		this.cg.setVisible(false);
 		this.cg = null;
 		this.loadClient();
 	}
 
 	public void logout() {
 		this.username.setText("");
 		this.password.setText("");
 		this.setVisible(true);
 		this.cg.setVisible(false);
 		this.cg = null;
 	}
 }
