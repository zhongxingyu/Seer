 package fr.meijin.run4win.webapp.server;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.UIManager;
 
 public class ServerRunner extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 8246957846370294364L;
 	
 	private JettyServer jettyServer;
 
 	public ServerRunner(JettyServer jettyServer) {
 		this.jettyServer = jettyServer;
 		try {
 			showSplash();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	public void showSplash() throws Exception {
 
 		JPanel content = (JPanel) getContentPane();
 		content.setBackground(Color.black);
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
 		Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/credit.png"));
 		setIconImage(icon);
 		
 		this.setTitle("Run 4 Win");
 		
		int width = 710;
		int height = 375;
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (screen.width - width) / 2;
 		int y = (screen.height - height) / 2;
 		setBounds(x, y, width, height);
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				if(jettyServer.isStarted()) {
 					try {
 						System.out.println("Stopping jetty");
 						jettyServer.stop();
 					} catch (Exception exception) {
 						exception.printStackTrace();
 					}
 				}
 			}
 		},"Stop Jetty Hook"));
 		
 		URL imgURL = getClass().getResource("/splash.jpg");
 		
 	    
 		JLabel image = new JLabel(new ImageIcon(imgURL));
 		Color green = new Color(88, 254, 110);
 
 		UIManager.put("ProgressBar.selectionForeground", green);
 		UIManager.put("ProgressBar.selectionBackground", Color.white);
 		UIManager.put("ProgressBar.foreground", green);
 		UIManager.put("ProgressBar.background", Color.black);
 
 		JProgressBar progressBar = new JProgressBar();
 		progressBar.setValue(0);
 		progressBar.setBorderPainted(false);
 
		content.add(image, BorderLayout.CENTER);
 		content.add(progressBar, BorderLayout.SOUTH);
		content.setBorder(BorderFactory.createLineBorder(Color.black, 10));
 
 		setVisible(true);
 
 		while (!jettyServer.isStarted()) {
 			Thread.sleep(30);
 			if(progressBar.getValue()==100)
 				progressBar.setValue(0);
 			progressBar.setValue(progressBar.getValue()+1);
 		}
 
 		progressBar.setVisible(false);
 		content.remove(progressBar);
 
 		JButton button = new JButton("Quit");
 		button.setSize(50, 20);
 		button.addActionListener(this);
 		JPanel south = new JPanel();
 		south.setBackground(Color.black);
 		south.add(button, BorderLayout.CENTER);
 		south.setSize(new Dimension(60, 30));
 		content.add(south, BorderLayout.SOUTH);
 		setVisible(true);
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(jettyServer.isStarted())
 			try {
 				System.out.println("Stopping jetty");
 				jettyServer.stop();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(0);
 			}
 		
 		System.exit(0);
 	}
 
 	public JettyServer getJettyServer() {
 		return jettyServer;
 	}
 
 	public void setJettyServer(JettyServer jettyServer) {
 		this.jettyServer = jettyServer;
 	}
 }
