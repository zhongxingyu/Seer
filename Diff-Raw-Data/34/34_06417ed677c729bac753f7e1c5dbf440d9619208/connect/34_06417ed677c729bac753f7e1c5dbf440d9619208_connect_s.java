 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.TimerTask;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceInfo;
 import javax.management.timer.Timer;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTabbedPane;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 
 public class connect {
 
 	
 	private static JLabel status = null;
 	private static JLabel ipLabel = null;
 	private static HandShake obj;
 	private static JmDNS server;
 	private static ServiceInfo info;
 	private static InetAddress ipadd;
 	private static JButton run;
 	private static JButton switchOff;
 	private static JFrame frame;
 	private static boolean canClose = false;
 	private static JPasswordField passwd;
 	private static JLabel passError;
 	private static JPanel setPanel;
 	private static JPanel panel;
 
 	public static void main(String[] args) {
 
 		frame = new JFrame("Receiver");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		obj = new HandShake();
 		ipadd = null;
 		String n = null;
 
 		run = new JButton("Run server");
 		switchOff = new JButton("Switch Off");
 		run.setEnabled(false);
 		switchOff.setEnabled(false);
 		status = new JLabel("Status : Initializing..");
 
 		try {
 			ipadd = getFirstNonLoopbackAddress();

 			setJmdns();
 
 			if (ipadd == null)
 				n = "Not Connected";
 			else
 				n = ipadd.getHostAddress().toString();
 		} catch (Exception e) {
 
 		}
 		ipLabel = new JLabel("IP address :" + n);
 
 		 panel = new JPanel();
 		panel.setLayout(new GridLayout(2, 3, 5, 2));
 		panel.setBorder(new EmptyBorder(10, 10, 10, 20));
 
 		panel.add(run);
 		panel.add(switchOff);
 		panel.add(ipLabel);
 		panel.add(status);
 
 		JTabbedPane tab = new JTabbedPane();
 		tab.setSize(200, 20);
 		frame.add(tab, BorderLayout.CENTER);
 		tab.add("control", panel);
 
 		setPanel = new JPanel();
 		setPanel.setLayout(new GridBagLayout());
 		
 		GridBagConstraints passText = new GridBagConstraints();
 		passText.gridx = 0;
 		passText.gridy = 0;
 		//passText.insets = new Insets(10, 10, 10, 10);
 
 		JLabel passT = new JLabel("Set Password : ");
 
 		setPanel.add(passT, passText);
 
 		Password passobj=new Password();
 		Integer res=new Integer(-2);
 		res=passobj.getLength();
 		System.out.println(res);
 		String dummyPassword=null;
 		
 		if(res!=null)
 		{
 			dummyPassword="";
 			for(int i=0;i<res;i++)
 			{
 				dummyPassword+="e";
 			}
 		}
 		passobj.destroy();
 		GridBagConstraints passBox = new GridBagConstraints();
 		passBox.gridx = 1;
 		passBox.gridy = 0;
 		
 		
 		if(dummyPassword==null)
 		passwd = new JPasswordField(10);
 		else
 		passwd = new JPasswordField(dummyPassword,10);
 			
 		setPanel.add(passwd, passBox);
 
 		GridBagConstraints passHint = new GridBagConstraints();
 		passHint.gridx = 2;
 		passHint.gridy = 0;
 		//passHint.insets = new Insets(10, 10, 10, 10);
 		JLabel hint = new JLabel("(Min 6 char, Max 12)");
 		setPanel.add(hint, passHint);
 
 		GridBagConstraints saveButton = new GridBagConstraints();
 		saveButton.gridx = 1;
 		saveButton.gridy = 1;
 		saveButton.insets = new Insets(10, 10, 10, 10);
 		JButton save = new JButton("Save");
 		setPanel.add(save, saveButton);
 
 		GridBagConstraints passLabel = new GridBagConstraints();
 		passLabel.gridx = 2;
 		passLabel.gridy = 1;
 
 		passError = new JLabel();
 		passError.setVisible(false);
 		setPanel.add(passError, passLabel);
 
 		tab.add("Settings", setPanel);
 
 		int frameWidth = 425;
 		int frameHeight = 150;
 		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment()
 				.getCenterPoint();
 		frame.setBounds(center.x - frameWidth / 2, center.y - frameHeight / 2,
 				frameWidth, frameHeight);
 
 		
 		
 		run.disable();
 		switchOff.disable();
 		
 		if(res==null)
 		tab.setSelectedIndex(1);
 		
 		frame.setVisible(true);
 	
 
 		run.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 
 				obj.start(status);
 
 			}
 		});
 
 		switchOff.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				
 				obj.stop(status);
 
 			}
 		});
 
 		frame.addWindowListener(new WindowAdapter() {
 
 			public void windowClosing(WindowEvent e) {
 
 				if (canClose) {
 					server.unregisterService(info);
 					
 					try
 					{
 						obj.cleanup();
 						obj.serverSocket.close();
 						obj.socket.close();
 					}
 					catch(Exception ex)
 					{
 						System.out.println(ex.getMessage());
 					}
 				}
 			}
 
 		});
 
 		save.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 
 				char passd[] = passwd.getPassword();
 				String passdStr = new String(passd);
 
 				if (passdStr.length() < 6) {
 
 					
 					passError.setText("Password too short");
 					passError.setVisible(true);
 					
 
 				} else if (passdStr.length() > 12) {
 					passError.setText("Password too Long");
 					passError.setVisible(true);
 
 				} else {
 					Password serverPassword = new Password();
 					try {
 
 						serverPassword.savePassword(passdStr);
 						passError.setText("Saved");
 						passError.setVisible(true);
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 					}
 
 					
 					serverPassword.destroy();
 
 				}
 
 			}
 		});
 
 	}
 
 	
 
 	
 	private static void setJmdns() {
 
 		Thread jmdnsthread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 
 				try {
 					String name = ipadd.getLocalHost().getHostName();
 					server = JmDNS.create(ipadd, "hello");
 
 					name = name.replace(".local", "");
 					name = name.replace("-", " ");
 					System.out.println(name);
 
 					info = ServiceInfo.create("_test._tcp.local.", name, 1234,
 							"test");
 					server.registerService(info);
 
 					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 					run.setEnabled(true);
 					switchOff.setEnabled(true);
 					canClose = true;
 					obj.start(status);
 					
 					
 					
 
 				} catch (Exception e) {
 
 				}
 
 			}
 		});
 
 		jmdnsthread.start();
 	}
 
 	private static InetAddress getFirstNonLoopbackAddress()
 			throws SocketException {
 		Enumeration en = NetworkInterface.getNetworkInterfaces();
 		while (en.hasMoreElements()) {
 			NetworkInterface i = (NetworkInterface) en.nextElement();
 			for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
 				InetAddress addr = (InetAddress) en2.nextElement();
 				if (!addr.isLoopbackAddress()) {
 					if (addr instanceof Inet4Address) {
 						return addr;
 					}
 
 				}
 			}
 		}
 		return null;
 	}
 
 }
