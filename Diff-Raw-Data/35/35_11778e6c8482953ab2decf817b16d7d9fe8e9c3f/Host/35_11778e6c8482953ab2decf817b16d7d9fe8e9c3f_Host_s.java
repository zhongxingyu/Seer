 package com.sirolf2009.monopolie;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.URISyntaxException;
 import java.text.NumberFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 
 import com.sirolf2009.monopolie.communication.Client;
 import com.sirolf2009.monopolie.communication.Connector;
 import com.sirolf2009.monopolie.communication.packet.PacketStreet;
 import com.sirolf2009.monopolie.communication.packet.PacketTeam;
 import com.sirolf2009.monopolie.street.Street;
 import com.sirolf2009.monopolie.team.Team;
 
 public class Host {
 
 	private ServerSocket socket;
 	public Connector connector;
 	public Client[] clients;
 
 	protected JTextField txtMoney;
 	protected JTextField txtDraws;
 	protected JLabel lblTeamColor;
 	protected JCheckBox cbxInJail;
 	protected JList<Street> lstStreets;
 	protected JPopupMenu popStreets;
 	protected JList<Object> lstPopStreets;
 	public JList<Team> lstClients;
 
 	protected JFrame frame;
 
 	private Map<String, Street> streets = new HashMap<String, Street>();
 
 	public static int port = 1941;
 
 	public static Host instance;
 
 	public Host(String[] args) {
 		instance = this;
 		try {
 			System.out.println("init");
 			init(args);
 			System.out.println("init-ed");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (BiffException e) {
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
		} finally {
			save();
 		}
 	}
 
 	private void init(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, BiffException, URISyntaxException, UnsupportedLookAndFeelException {
 		port = Integer.parseInt(args[0]);
 
 		Workbook workbook = Workbook.getWorkbook(getClass().getResourceAsStream("/Zalmplaat.xls"));
 		Sheet sheet = workbook.getSheet(0);
 		for(int i = 0; i < 69; i++) {
 			Street street = new Street();
 			street.name = sheet.getCell(0, i).getContents();
 			street.x = Integer.parseInt(sheet.getCell(1, i).getContents());
 			street.y = Integer.parseInt(sheet.getCell(2, i).getContents());
 			street.size = 0; // Integer.parseInt(sheet.getCell(3, i).getContents());
 			streets.put(street.name, street);
 		}
 
 		for(LookAndFeelInfo look : UIManager.getInstalledLookAndFeels()) {
 			if(look.getName() == "Nimbus") {
 				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 				break;
 			}
 		}
 
 		frame = new JFrame("Monopolie Host");
 		frame.setSize(800, 600);
 		frame.setLayout(null);
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new WindowListener() {
 			@Override
 			public void windowOpened(WindowEvent arg0) {
 			}
 			@Override
 			public void windowIconified(WindowEvent arg0) {
 			}
 			@Override
 			public void windowDeiconified(WindowEvent arg0) {
 			}
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {
 			}
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				save();
 				System.exit(0);
 			}
 			@Override
 			public void windowClosed(WindowEvent arg0) {
 			}
 			@Override
 			public void windowActivated(WindowEvent arg0) {
 			}
 		});
 
 		lstClients = new JList<Team>();
 		lstClients.setBounds(15, 15, 200, 600-70);
 		lstClients.addListSelectionListener(new DisplayClient(this));
 		lstClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		lstClients.setCellRenderer(new ClientCellRenderer());
 		frame.add(lstClients);
 
 		JLabel lblColor = new JLabel("Team:");
 		alignRight(lstClients, lblColor, 100, 30);
 		frame.add(lblColor);
 		lblTeamColor = new JLabel("Klik op een team");
 		alignRight(lblColor, lblTeamColor, 100, 30);
 		frame.add(lblTeamColor);
 
 		JLabel lblMoney = new JLabel("Geld:");
 		alignBelow(lblColor, lblMoney, 100, 30);
 		frame.add(lblMoney);
 		txtMoney = new JFormattedTextField(NumberFormat.getInstance());
 		alignBelow(lblTeamColor, txtMoney, 100, 30);
 		frame.add(txtMoney);
 
 		JLabel lblDraws = new JLabel("Kanskaarten:");
 		alignBelow(lblMoney, lblDraws, 100, 30);
 		frame.add(lblDraws);
 		txtDraws = new JFormattedTextField(NumberFormat.getInstance());
 		alignBelow(txtMoney, txtDraws, 100, 30);
 		frame.add(txtDraws);
 
 		JLabel lblInJail = new JLabel("Gevangenis:");
 		alignBelow(lblDraws, lblInJail, 100, 30);
 		frame.add(lblInJail);
 		cbxInJail = new JCheckBox();
 		alignBelow(txtDraws, cbxInJail, 100, 30);
 		frame.add(cbxInJail);
 
 		JLabel lblStreets = new JLabel("Straten:");
 		alignBelow(lblInJail, lblStreets, 100, 30);
 		frame.add(lblStreets);
 		lstStreets = new JList<Street>();
 		alignBelow(cbxInJail, lstStreets, 300, 300);
 		lstStreets.setLayout(new BorderLayout());
 		frame.add(lstStreets);
 		JScrollPane scrLstStreets = new JScrollPane();
 		lstStreets.add(scrLstStreets, BorderLayout.EAST);
 		JButton btnAddStreet = new JButton("Toevoegen");
 		alignBelow(lstStreets, btnAddStreet, 150-(15/2), 30);
 		btnAddStreet.addActionListener(new ShowPopupStreet(this));
 		frame.add(btnAddStreet);
 		JButton btnRemoveStreet = new JButton("Verwijderen");
 		alignRight(btnAddStreet, btnRemoveStreet, 150-(15/2), 30);
 		btnRemoveStreet.addActionListener(new RemoveStreet(this));
 		frame.add(btnRemoveStreet);
 
 		popStreets = new JPopupMenu("Kies een straat");
 		popStreets.setLayout(new BorderLayout());
 		lstPopStreets = new JList<Object>(streets.keySet().toArray());
 		JScrollPane scrPopStreets = new JScrollPane(lstPopStreets);
 		popStreets.add(scrPopStreets, BorderLayout.CENTER);
 		JButton btnPopAddStreet = new JButton("OK");
 		btnPopAddStreet.addActionListener(new AddStreet(this));
 		popStreets.add(btnPopAddStreet, BorderLayout.SOUTH);
 		frame.add(popStreets);
 
 		JButton btnUpdate = new JButton("update");
 		alignRight(txtMoney, btnUpdate, 200, 50);
 		btnUpdate.addActionListener(new UpdateClient(this));
 		frame.add(btnUpdate);
 
 		frame.setVisible(true);
 
 		socket = new ServerSocket(port);
 		clients = new Client[Integer.parseInt(args[1])];
 		for(int i = 0; i < clients.length; i++) {
 			try {
 				clients[i] = new Client(socket.accept());
 				while(clients[i].team == null) { Thread.sleep(1); }
 				clients[i].isConnected = true;
 				DefaultListModel<Team> model = new DefaultListModel<Team>();
 				for(int j = 0; j <= i; j ++) {
 					model.addElement(clients[j].team);
 				}
 				lstClients.setModel(model);
 				frame.repaint();
 				System.out.println(clients[i]+" connected");
 				Thread.sleep(1);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("clients connected");
 		connector = new Connector(socket);
 		new Thread(connector).start();
 		if(Boolean.parseBoolean(args[2])) {
 			if(Integer.parseInt(args[3]) != -1) {
 				load(Integer.parseInt(args[3]));
 			} else {
 				load();
 			}
 		}
 	}
 
 	public void updateTeamInfoIfDisplayed(Team team) {
 		if(lstClients.getSelectedValue() != null) {
 			if(lstClients.getSelectedValue().teamColor.equals(team.teamColor)) {
 				lblTeamColor.setText(team+"");
 				cbxInJail.setSelected(team.inJail);
 				txtDraws.setText(team.draws+"");
 				txtMoney.setText(team.money+"");
 				DefaultListModel<Street> streetModel = new DefaultListModel<Street>();
 				for(Street street : team.getOwnedStreets(streets.values())) {
 					streetModel.addElement(street);
 				}
 				lstStreets.setModel(streetModel);
 				frame.repaint();
 			}
 		}
 	}
 
 	public Client getClientFromTeam(Team team) {
 		for(Client client : clients) {
 			if(client.team.isSameTeamAs(team)) {
 				return client;
 			}
 		}
 		return null;
 	}
 	
 	public Client getClientFromTeamColor(Color team) {
 		for(Client client : clients) {
 			if(client.team.teamColor.equals(team)) {
 				return client;
 			}
 		}
 		return null;
 	}
 
 	public void save() {
 		try {
 			File saveFolder = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()+"save");
 			int ID = Integer.parseInt(Calendar.getInstance().get(Calendar.MONTH)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+""+Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+""+Calendar.getInstance().get(Calendar.SECOND));
 			File saveFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()+"save/saveFile"+ID+".txt");
 			System.out.println("save file: "+saveFile.getAbsolutePath());
 			if(!saveFolder.exists()) {
 				saveFolder.mkdirs();
 			}
 			if(!saveFile.exists()) {
 				saveFile.createNewFile();
 			}
 			PrintWriter writer = new PrintWriter(saveFile.getAbsolutePath(), "UTF-8");
 			for(Client client : clients) {
 				if(client == null)
 					continue;
 				Team team = client.team;
 				writer.println("#Team "+team.teamColor.getRGB());
 				writer.println(team.money);
 				writer.println(team.draws);
 				writer.println(team.inJail);
 			}
 			for(Street street : streets.values()) {
 				writer.println("#Street "+street.name);
 				writer.println(street.x);
 				writer.println(street.y);
 				writer.println(street.houses);
 				writer.println(street.size);
 				writer.println(street.owningTeam != null ? street.owningTeam.teamColor.getRGB() : "null");
 			}
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void load() throws IOException {
 		File saveFolder = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()+"save");
 		int latestID = 0;
 		for(int i = 0; i < saveFolder.list().length; i++) {
 			String saveFileName = saveFolder.list()[i];
 			saveFileName = saveFileName.replaceAll("saveFile", "");
 			saveFileName = saveFileName.replaceAll(".txt", "");
 			int currentID = Integer.parseInt(saveFileName);
 			latestID = currentID > latestID ? currentID : latestID;
 		}
 		if(latestID == 0) {
 			return;
 		}
 		load(latestID);
 	}
 
 	public void load(int saveID) throws IOException {
 		File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()+"save/saveFile"+saveID+".txt");
 		System.out.println("loading saveFile "+file.getAbsolutePath());
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		String line;
 		while((line = reader.readLine()) != null) {
 			if(line.startsWith("#Team ")) {
 				Color teamColor = new Color(Integer.parseInt(line.replace("#Team ", "")));
 				System.out.println(teamColor);
 				int money = Integer.parseInt(reader.readLine());
 				System.out.println(money);
 				int draws = Integer.parseInt(reader.readLine());
 				boolean inJail = Boolean.parseBoolean(reader.readLine());
 				for(Client client : clients) {
 					if(client.team.teamColor.equals(teamColor)) {
 						client.team.money = money;
 						client.team.draws = draws;
 						client.team.inJail = inJail;
 						client.sender.send(new PacketTeam(client.team));
 						break;
 					}
 				}
 			} else if(line.startsWith("#Street ")) {
 				String streetName = line.replace("#Street ", "");
 				int x = Integer.parseInt(reader.readLine());
 				int y = Integer.parseInt(reader.readLine());
 				int houses = Integer.parseInt(reader.readLine());
 				int size = Integer.parseInt(reader.readLine());
 				String owningTeam = reader.readLine();
 				Street oldStreet = streets.get(streetName);
 				streets.get(streetName).x = x;
 				streets.get(streetName).y = y;
 				streets.get(streetName).houses = houses;
 				streets.get(streetName).size = size;
 				if(!owningTeam.equals("null")) {
 					Color teamColor = new Color(Integer.parseInt(reader.readLine()));
 					for(Client client : clients) {
 						if(client.team.teamColor.equals(teamColor)) {
 							streets.get(streetName).owningTeam = client.team;
 							break;
 						}
 					}
 				} else {
 					streets.get(streetName).owningTeam = null;
 				}
 				if(!streets.get(streetName).equals(oldStreet)) {
 					for(Client client : clients) {
 						client.sender.send(new PacketStreet(streets.get(streetName)));
 					}
 				}
 			}
 		}
 		reader.close();
 	}
 
 	private void alignRight(Component one, Component two, int width, int height) {
 		two.setBounds(one.getX()+one.getWidth()+15, one.getY(), width , height);
 	}
 
 	private void alignBelow(Component one, Component two, int width, int height) {
 		two.setBounds(one.getX(), one.getY()+one.getHeight()+15, width , height);
 	}
 	
 	public void setStreet(String name, Street street) {
 		streets.put(name, street);
 	}
 	
 	public Street getStreet(String name) {
 		return streets.get(name);
 	}
 
 	public static void main(String[] args) {
 		if(args.length != 4) {
 			System.err.println("Usage <port> <teams> <shouldLoad> <loadVersion>");
 			args = new String[] {"1941", "8", "false", "-1"};
 		}
 		System.out.println("launching with args: "+args[0]+", "+args[1]+", "+args[2]+", "+args[3]);
 		new Host(args);
 	}
 
 	public Map<String, Street> getStreets() {
 		return streets;
 	}
 
 }
 
 class ShowPopupStreet implements ActionListener {
 	Host host;
 	public ShowPopupStreet(Host host) {
 		this.host = host;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getID() == 1001) {
 			if(host.lstClients.getSelectedIndex() != -1) {
 				host.popStreets.show((Component) e.getSource(), 0, 0);
 			}
 		}
 	}
 }
 
 class AddStreet implements ActionListener {
 	Host host;
 	public AddStreet(Host host) {
 		this.host = host;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getID() == 1001) {
 			DefaultListModel<Street> model = new DefaultListModel<Street>();
 			for(Object string : host.lstPopStreets.getSelectedValuesList()) {
 				model.addElement(host.getStreet(string.toString()));
 			}
 			for(int i = 0; i < host.lstStreets.getModel().getSize(); i++) {
 				model.addElement(host.lstStreets.getModel().getElementAt(i));
 			}
 			host.lstStreets.setModel(model);
 			host.frame.repaint();
 			host.popStreets.setVisible(false);
 		}
 	}
 }
 
 class RemoveStreet implements ActionListener {
 	Host host;
 	public RemoveStreet(Host host) {
 		this.host = host;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getID() == 1001) {
 			List<Street> removeList = host.lstStreets.getSelectedValuesList();
 			for(Street street : removeList) {
 				Team team = street.owningTeam;
 				street.owningTeam = null;
 				street.houses = 0;
 				host.getClientFromTeam(team).sender.send(new PacketStreet(street));
 			}
 			DefaultListModel<Street> model = new DefaultListModel<Street>();
 			ListModel<Street> oldModel = host.lstStreets.getModel();
 			for(int i = 0; i < oldModel.getSize(); i++) {
 				if(!removeList.contains(oldModel.getElementAt(i))) {
 					model.addElement(oldModel.getElementAt(i));
 				}
 			}
 			host.lstStreets.setModel(model);
 			host.frame.repaint();
 		}
 	}
 }
 
 class DisplayClient implements ListSelectionListener {
 	Host host;
 	public DisplayClient(Host host) {
 		this.host = host;
 	}
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		Client client = host.clients[host.lstClients.getSelectedIndex()];
 		host.lblTeamColor.setText(client.team+"");
 		host.cbxInJail.setSelected(client.team.inJail);
 		host.txtDraws.setText(client.team.draws+"");
 		host.txtMoney.setText(client.team.money+"");
 		DefaultListModel<Street> streetModel = new DefaultListModel<Street>();
 		for(Street street : client.team.getOwnedStreets(host.getStreets().values())) {
 			streetModel.addElement(street);
 		}
 		host.lstStreets.setModel(streetModel);
 		host.frame.repaint();
 	}
 }
 
 class UpdateClient implements ActionListener {
 	Host host;
 	public UpdateClient(Host host) {
 		this.host = host;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(host.lstClients.getSelectedIndex() == -1)
 			return;
 		Client client = host.clients[host.lstClients.getSelectedIndex()];
 		Team team = client.team;
 		team.draws = Integer.parseInt(host.txtDraws.getText());
 		team.inJail = host.cbxInJail.isSelected();
 		team.money = Integer.parseInt(host.txtMoney.getText());
 		PacketTeam teamPacket = new PacketTeam(client.team);
 		client.getSender().send(teamPacket);
 		for(int i = 0; i < host.lstStreets.getModel().getSize(); i++) {
 			Street street = host.lstStreets.getModel().getElementAt(i);
 			PacketStreet packetstreet = new PacketStreet(street);
 			client.sender.send(packetstreet);
 		}
 	}
 }
 
 class ClientCellRenderer extends DefaultListCellRenderer {
 	private static final long serialVersionUID = 4692305377583657368L;
 
 	@Override
 	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		if(!Host.instance.getClientFromTeam((Team) value).isConnected) {
 			component.setBackground(Color.red);
 		}
 		return component;
 	}
 	
 }
