 package gui;
 
 import gui.ServiceUserStatus.Pair;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableColumn;
 
 import mediator.IMediatorGui;
 import mediator.Mediator;
 
 public class Gui extends JPanel implements IGui{
 	private static final long serialVersionUID = -8012095796913881980L;
 
 	private IMediatorGui mediator;
 
 	private JPanel loginScreen;
 	private JTable auctionTable;
 	private MyDefaultTableModel model;
 	
 	private String userName;
 	private String userType;
 	
 	private ArrayList<String> userServices;
 	private ArrayList<ServiceUserStatus> services;
 	private ArrayList<TableCellEditor> editors;
 	private ArrayList<JComboBox> comboBoxes;
 	
 	private JButton logoutButton;
 	public Gui() {
 		mediator = new Mediator();
 		mediator.setGui(this);
 		this.services = new ArrayList<ServiceUserStatus>();
 		this.loginGui();
 	}
 	
 	public IMediatorGui getMediator () {
 		return this.mediator;
 	}
 	
 	public JTable getAuctionTable() {
 		return this.auctionTable;
 	}
 	
 	public String getUserName () {
 		return this.userName;
 	}
 	
 	public String getUserType () {
 		return this.userType;
 	}
 	
 	public ArrayList<ServiceUserStatus> getServices () {
 		return this.services;
 	}
 	
 	public void loginGui() {
 		final JTextField user = new JTextField();
 		final JPasswordField pass = new JPasswordField();
 		
 		JButton loginButton = new JButton("Login");
 		loginScreen = new JPanel(new GridLayout(3, 2));
 		
 		this.setLayout(new BorderLayout());
 		this.add(loginScreen, BorderLayout.CENTER);
 		
 		loginScreen.add(new JLabel("Username: ", JLabel.RIGHT));
 		loginScreen.add(user);
 		
 		loginScreen.add(new JLabel("Password: ", JLabel.RIGHT));
 		loginScreen.add(pass);
 		
 		loginScreen.add(new JLabel(""));
 		loginScreen.add(loginButton);
 		
 		loginButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String usernameText = user.getText();
 				String passwordText = new String(pass.getPassword());
 				if (usernameText.isEmpty()) {
 					JOptionPane.showMessageDialog(
 							null, "Name is empty!", "Error", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				if (passwordText.isEmpty()) {
 					JOptionPane.showMessageDialog(
 							null, "Password is empty!", "Error", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				userType = mediator.getUserType(usernameText, passwordText);
 				if (userType != null) {
 					userName = usernameText;
 					mediator.startNetwork();
 					auctionGui();
 				}
 				else {
 					JOptionPane.showMessageDialog(
 							null, "Login error!", "Error", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 			}
 		});
 	}
 	
 	
 	public void auctionGui() {
 		JPanel topPanel = new JPanel(new GridLayout(1, 2));
 		JLabel userNameAndType = new JLabel(userName + ", " + userType, JLabel.CENTER);
 		model = new MyDefaultTableModel();
 		logoutButton = new JButton("Logout");
 		
 		this.remove(loginScreen);
 		this.setLayout(new BorderLayout());
 		
 		topPanel.add(userNameAndType);
 		topPanel.add(logoutButton);
 		
 		logoutButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				if (userType.equals("seller")) {
 					for (int i = 0; i < services.size(); i++) {
 						ArrayList<Pair<String, String>> serviceUsers = services.get(i).getList();
 						
 						for (int j = 0; j < serviceUsers.size(); j++) {
 							if (!serviceUsers.get(j).getStatus().equals("No offer") &&
 									!serviceUsers.get(j).getStatus().equals("Transfer completed") &&
 									!serviceUsers.get(j).getStatus().equals("Offer exceeded")) {
 								{
 									JOptionPane.showMessageDialog(
 											null, "Cannot logout!", "Error", JOptionPane.ERROR_MESSAGE);
 									return;
 								}
 							}
 						}
 					}
 				}
 				
 				mediator.logOut(userName);
 				System.exit(0);
 			}
 		});
 		
 		String[] columnNames = new String[]{"Service", "Status", "Users", "Progress"};
 		for(int i = 0; i < columnNames.length; i++) {
 			model.addColumn(columnNames[i]);
 		}
 		
 		userServices = mediator.getUserServices(userName);
 		comboBoxes = new ArrayList<JComboBox>();
 		editors = new ArrayList<TableCellEditor>();
 		
 		for(int i = 0; i < userServices.size(); i++) {
 			Object[] rowParams = {userServices.get(i), "inactive", new JComboBox(), new JProgressBar(0, 10)};
 			model.addRow(rowParams);
 			
 			JComboBox combo = new JComboBox();
 			DefaultCellEditor dce = new DefaultCellEditor(combo);
 			editors.add(dce);
 			comboBoxes.add(combo);
 			
 		}
 		
 		auctionTable = new JTable(model)
 		{
             //  Determine editor to be used by row
             public TableCellEditor getCellEditor(int row, int column)
             {
                 int modelColumn = convertColumnIndexToModel( column );
 
                 if (modelColumn == 2)
                     return editors.get(row);
                 else
                     return super.getCellEditor(row, column);
             }
         };
         
 		auctionTable.setCellSelectionEnabled(true);
 
 		// modifica cell renderer pentru ultimele 2 coloane
 		TableColumn col = auctionTable.getColumnModel().getColumn(2);
 		MyTableCellRenderer customRenderer = new MyTableCellRenderer(auctionTable);
 		col.setCellRenderer(customRenderer);
 				
 		col = auctionTable.getColumnModel().getColumn(3);
 		col.setCellRenderer(customRenderer);
 				
 		final Gui currentGui = this;
 		auctionTable.addMouseListener(new MouseAdapter () {
 			public void mouseClicked(MouseEvent e) {
 				if (SwingUtilities.isRightMouseButton(e)) {
 					// select the cell before expanding popup menu
 					Point p = e.getPoint();
 					final int rowNumber = auctionTable.rowAtPoint(p);
 					final int columnNumber = auctionTable.columnAtPoint(p);
 					ListSelectionModel lmodel = auctionTable.getSelectionModel();
 
 					//services column for buyers
 					if (columnNumber == 0 && userType.equals("buyer")) {
 			    		JPopupMenu jpop = new JPopupMenu();
 			    		JMenuItem it1 = new JMenuItem("Launch offer request");
 			    		JMenuItem it2 = new JMenuItem("Drop offer request");
 			    		it1.addActionListener(new CustomActionListener(currentGui));
 			    		it2.addActionListener(new CustomActionListener(currentGui));
 			    		jpop.add(it1);
 			    		jpop.add(it2);
 			    		add(jpop);
 			    		jpop.show(e.getComponent(), e.getX(), e.getY());
 						lmodel.setSelectionInterval(rowNumber, rowNumber);
 					}
 					
 					//users column for buyers
 					if (columnNumber == 2 && userType.equals("buyer")) {
 			    		JPopupMenu jpop = new JPopupMenu();
 			    		JMenuItem it1 = new JMenuItem("Accept offer");
 			    		JMenuItem it2 = new JMenuItem("Refuse offer");
 			    		it1.addActionListener(new CustomActionListener(currentGui));
 			    		it2.addActionListener(new CustomActionListener(currentGui));
 			    		jpop.add(it1);
 			    		jpop.add(it2);
 			    		add(jpop);
 			    		jpop.show(e.getComponent(), e.getX(), e.getY());
 						lmodel.setSelectionInterval(rowNumber, rowNumber);						
 					}
 					
 					//users column for sellers
 					if (columnNumber == 2 && userType.equals("seller")) {
 			    		JPopupMenu jpop = new JPopupMenu();
 			    		JMenuItem it1 = new JMenuItem("Make offer");
 			    		JMenuItem it2 = new JMenuItem("Drop auction");
 			    		it1.addActionListener(new CustomActionListener(currentGui));
 			    		it2.addActionListener(new CustomActionListener(currentGui));
 			    		jpop.add(it1);
 			    		jpop.add(it2);
 			    		add(jpop);
 			    		jpop.show(e.getComponent(), e.getX(), e.getY());
 						lmodel.setSelectionInterval(rowNumber, rowNumber);
 						
 					}
 				}
 			}
 		});
 		
 		JScrollPane auctionScreen = new JScrollPane(auctionTable);
 		this.add(auctionScreen, BorderLayout.CENTER);
 		this.add(topPanel, BorderLayout.NORTH);
 		this.revalidate();
 		
 		if (this.userName.equals("andreea"))
 			this.userActivatedService("valeriu", "imprimare");
 		else {
 			ArrayList<String> users = new ArrayList<String>();
 			users.add("andreea");
 			populateServiceUserList("imprimare", users);
 		}
 	}
 	
 	
 	
 	// metode pe care le apeleaza mediatorul::
 	
 	// cand un buyer activeaza un serviciu, primeste de la server o lista
 	// cu sellerii ce furnizeaza serviciul respectiv
 	public void populateServiceUserList (String service, ArrayList<String> users) {
 		int row = -1;
 		for(int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (auctionTable.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (row == -1)
 			return;
 
 		model.setValueAt("active", row, 1);
 		ServiceUserStatus newService = new ServiceUserStatus(service);
 		for(int i = 0; i < users.size(); i++) {
 			newService.addUser(users.get(i), new String("No offer"));
 		}
 		
 		this.services.add(newService);
 		
 		for (int i = 0; i < users.size(); i++) {
 			comboBoxes.get(row).addItem(users.get(i));
 		}
 		
 		
 		comboBoxes.get(row).setSelectedItem(null);
 		
 		//this.auctionTable.getColumn("Users").setCellEditor(new DefaultCellEditor(comboBox));
 
 		final String currentService = service;
 		comboBoxes.get(row).addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int row = -1;
 				for (int i = 0; i < model.getRowCount(); i++) {
 					if (model.getValueAt(i, 0).equals(currentService)) {
 						row = i;
 						break;
 					}
 				}
 				
 				if (row != -1) {
 					for (int i = 0; i < services.size(); i++) {
 						if (services.get(i).getService().equals(currentService)) {
 							model.setValueAt(services.get(i).getStatus((String)comboBoxes.get(row).getSelectedItem()), row, 1);
 							auctionTable.setModel(model);
 							break;
 						}
 					}
 				}
 			}
 			
 		});
 		
 		row = -1;
 
 		for (int i = 0; i < model.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 
 		if (row != -1) {
 			this.model.setValueAt(comboBoxes.get(row), row, 2);
 		}
 		
 		auctionTable.setModel(this.model);
 	}
 	
 	public void removeServiceUserList (String service) {
 		int index = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < model.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				if (index != -1) {
 					services.remove(index);
 				}
 				JComboBox comboBox = new JComboBox();
 				DefaultCellEditor dce = new DefaultCellEditor(comboBox);
 				editors.set(i, dce);
 				comboBoxes.set(i, comboBox);
 
 				model.setValueAt(comboBox, i, 2);
 				model.setValueAt("inactive", i, 1);
 				auctionTable.setModel(model);
 				
 
 				
 				auctionTable.getColumn("Users").setCellEditor(new DefaultCellEditor(comboBox));
 			}
 		}
 	}
 	
 	public void makeOffer (String remoteUser, String service) {
 		// modificam statusul lui remoteuser pt service in services
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (row != -1 && index != -1){
 			services.get(index).changeStatus(remoteUser, new String("Offer made"));
 			model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 			this.auctionTable.setModel(model);
 		}
 		
 	}
 	
 	public void sellerMadeOffer (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (row != -1 && index != -1){
 			if (userType.equals("buyer")) 
 				services.get(index).changeStatus(remoteUser, new String("Offer made"));
 			else if (userType.equals("seller"))
 				services.get(index).changeStatus(remoteUser, new String("Offer exceeded"));
 						
 			if (model.getValueAt(row, 2) instanceof String) {
 				String value = (String)model.getValueAt(row, 2);
 				if (value != null && value.equals(remoteUser)) {
 					model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 				}
 			}
 			else {
 				JComboBox comboBox = (JComboBox) model.getValueAt(row, 2);
 				if (comboBox.getSelectedItem() != null && comboBox.getSelectedItem().equals(remoteUser))
 					model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 			}
 			
 			this.auctionTable.setModel(model);
 		}
 	}
 	
 	public void dropAuction (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (index != -1 && row != -1) {
 			String currentStatus = model.getValueAt(row,  1).toString();
 			
 			if (currentStatus.equals("Offer exceeded")) {
 				services.get(index).changeStatus(remoteUser, new String("No offer"));
 				model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 				this.auctionTable.setModel(model);
 			}
 			else {
 				JOptionPane.showMessageDialog(
 						null, "Cannot drop auction!", "Error", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		}
 	}
 	
 	public void sellerDroppedAuction (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (row != -1 && index != -1){
 			services.get(index).changeStatus(remoteUser, new String("No offer"));
 						
 			if (model.getValueAt(row, 2) instanceof String) {
 				String value = (String)model.getValueAt(row, 2);
 				if (value != null && value.equals(remoteUser)) {
 					model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 				}
 			}
 			else {
 				JComboBox comboBox = (JComboBox) model.getValueAt(row, 2);
 				if (comboBox.getSelectedItem() != null && comboBox.getSelectedItem().equals(remoteUser))
 					model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 			}
 			
 			this.auctionTable.setModel(model);
 		}
 	}
 	
 	public void acceptOffer (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (index != -1 && row != -1){
 			String currentStatus = model.getValueAt(row,  1).toString();
 			
 			if (currentStatus.equals("Offer made")) {
 				services.get(index).changeStatus(remoteUser, new String("Offer accepted"));
 				model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 				
 				
 				this.auctionTable.setModel(model);
 				this.startTransfer(remoteUser, service);
 			}
 			else {
 				JOptionPane.showMessageDialog(
 						null, "No offer made!", "Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 	
 	public void buyerAcceptedOffer (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (index != -1 && row != -1){
 			String currentStatus = services.get(index).getStatus(remoteUser);
 			
 			if (currentStatus.equals("Offer made") || currentStatus.equals("Offer exceeded")) {
 				services.get(index).changeStatus(remoteUser, new String("Offer accepted"));
 				model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 				
 				model.setValueAt(remoteUser, row,2);
 				
 				this.auctionTable.setModel(model);
 				this.startTransfer(remoteUser, service);
 			}
 		}
 	}
 	
 	public void refuseOffer (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (index != -1 && row != -1){
 			String currentStatus = model.getValueAt(row,  1).toString();
 			
 			if (currentStatus.equals("Offer made")) {
 				services.get(index).changeStatus(remoteUser, new String("Offer refused"));
 				model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 				this.auctionTable.setModel(model);
 			}
 			else {
 				JOptionPane.showMessageDialog(
 						null, "No offer made!", "Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 	
 	public void buyerRefusedOffer (String remoteUser, String service) {
 		int index = -1;
 		int row = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
 		for (int i = 0; i < auctionTable.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				row = i;
 				break;
 			}
 		}
 		
 		if (index != -1 && row != -1){
 			String currentStatus = model.getValueAt(row,  1).toString();
 			
 			if (currentStatus.equals("Offer made") || currentStatus.equals("Offer exceeded")) {
 				services.get(index).changeStatus(remoteUser, new String("Offer refused"));
 				
				String user = (String) model.getValueAt(row, 2);
				if (user.equals(remoteUser))
 					model.setValueAt(services.get(index).getStatus(remoteUser), row, 1);
 
 				this.auctionTable.setModel(model);
 			}
 		}
 	}
 	
 	public void startTransfer(String remoteUser, String service) {
 		int index = -1;
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i).getService().equals(service)) {
 				index = i;
 			}
 		}
 		
		int row = -1;
		for (int i = 0; i < auctionTable.getRowCount(); i++) {
			if (model.getValueAt(i, 0).equals(service)) {
				row = i;
				break;
			}
		}
		
 		final int indx = index;
		final int r = row;
 		final String remoteUserAux = remoteUser;
 
 		if (indx != -1) {
 			Transfer tr = new Transfer();
 			PropertyChangeListener listener = new PropertyChangeListener() {
 
 				@Override
 				public void propertyChange(PropertyChangeEvent evt) {
 					JProgressBar progressBar = (JProgressBar)model.getValueAt(indx, 3);
 			        if ("progress".equals(evt.getPropertyName())) {
 			        	Integer newValue = (Integer)evt.getNewValue();
 			        	Integer minValue = (Integer)progressBar.getMinimum();
 			        	Integer maxValue = (Integer)progressBar.getMaximum();
 			        	
 			        	if (newValue.equals(minValue + 1)) {
 			    			services.get(indx).changeStatus(remoteUserAux, new String("Transfer started"));
			    			if (model.getValueAt(r, 2).toString().equals(remoteUserAux))
				        		model.setValueAt(services.get(indx).getStatus(remoteUserAux), r, 1);
 			    			auctionTable.setModel(model);
 			        	}
 			        	if (newValue.equals(minValue + 2)) {
 			        		services.get(indx).changeStatus(remoteUserAux, new String("Transfer in progress"));
			        		if (model.getValueAt(r, 2).toString().equals(remoteUserAux))
			        			model.setValueAt(services.get(indx).getStatus(remoteUserAux), r, 1);
 			    			auctionTable.setModel(model);
 			        	}
 			        	
 			        	if (newValue.equals(maxValue)) {
 			        		if (!services.get(indx).getStatus(remoteUserAux).equals("Transfer failed")) 
 			        			services.get(indx).changeStatus(remoteUserAux, new String("Transfer completed"));
			        		model.setValueAt(services.get(indx).getStatus(remoteUserAux), r, 1);
			        		model.setValueAt(remoteUserAux, r, 2);
 			    			auctionTable.setModel(model);
 			        	}
 			        	
 			        	//model.setValueAt(remoteUserAux, indx, 2);
 			        	progressBar.setValue((Integer)evt.getNewValue());
			        	model.setValueAt(progressBar, r, 3);
 			        	auctionTable.setModel(model);
 			        }
 				}
 			};
 			
 			tr.addPropertyChangeListener(listener);
 	    	tr.execute();
 		}
 	}
 	
 	
 	public void userActivatedService(String remoteUser, String service) {
 		int index = -1;
 		for (int i = 0; i < model.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				//caut in arraylist intrarea corespunzatoare serviciului, daca
 				//aceasta exista
 				int j;
 				for(j = 0; j < services.size(); j++) {
 					if(services.get(j).getService().equals(service)){
 						services.get(j).addUser(remoteUser, "No offer");
 						
 						JComboBox comboBox = (JComboBox) model.getValueAt(i, 2);
 						comboBox.addItem(remoteUser);
 						model.setValueAt(comboBox, i, 2);
 						auctionTable.setModel(model);
 						
 						break;
 					}
 				}
 				if(j == services.size()) {
 					ArrayList<String> users = new ArrayList<String>();
 					users.add(remoteUser);
 					//nu exista serviciul; il adaug in lista
 					this.populateServiceUserList(service, users);
 				}
 			}
 		}
 	}
 	
 	
 	public void userDeactivatedService(String remoteUser, String service) {
 		int index = -1;
 		for (int i = 0; i < model.getRowCount(); i++) {
 			if (model.getValueAt(i, 0).equals(service)) {
 				//caut in arraylist intrarea corespunzatoare serviciului, daca
 				//aceasta exista
 				int j;
 				for(j = 0; j < services.size(); j++) {
 					if(services.get(j).getService().equals(service)){
 						if(services.get(j).getStatus(remoteUser) != null) {
 							services.get(j).removeUser(remoteUser);
 							
 							if (services.get(j).getSize() != 0) {
 								JComboBox comboBox = (JComboBox) model.getValueAt(i, 2);
 								comboBox.removeItem(remoteUser);
 								if (comboBox.getSelectedItem() == null) {
 									model.setValueAt("active", i, 1);
 								}
 								
 								model.setValueAt(comboBox, i, 2);
 								auctionTable.setModel(model);
 							}
 							else {
 								this.removeServiceUserList(service);
 							}
 							
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void userLoggedOut (String username) {
 		for (int i = 0; i < model.getRowCount(); i++) {
 			for (int j = 0; j < services.size(); j++) {
 				
 				if (!model.getValueAt(i, 0).equals(services.get(j).getService()))
 					continue;
 				
 				ArrayList<Pair<String, String>> userServices = services.get(j).getList();
 				
 				for (int k = 0; k < userServices.size(); k++) {
 					if (userServices.get(k).getUser().equals(username)) {
 						if (userServices.get(k).getStatus().contains("Transfer") &&
 							!userServices.get(k).getStatus().equals("Transfer completed")) {
 							services.get(j).changeStatus(userServices.get(k).getUser(), "Transfer failed");
 							if (model.getValueAt(i, 2).equals(username)) {
 								model.setValueAt("Transfer failed", i, 1);
 								auctionTable.setModel(model);
 							}
 						}
 						
 						else {
 							services.get(j).removeUser(username);
 							if (model.getValueAt(i, 2).equals(username)) {
 								comboBoxes.get(i).setSelectedItem(null);
 								comboBoxes.get(i).removeItem(username);
 								model.setValueAt(null, i, 2);
 								model.setValueAt("active", i, 1);
 								auctionTable.setModel(model);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 		
 	public static void buildGUI() {
 		JFrame frame = new JFrame("Teapots"); // title
 		frame.setContentPane(new Gui()); // content: the JPanel above
 		frame.setSize(600, 300); // width / height
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit application when window is closed
 		frame.setVisible(true); // show it!
 	}
 	
 	
 	public static void main(String[] args) {
 		// run on EDT (event-dispatching thread), not on main thread!
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				buildGUI();
 			}
 		});
 	}
 	
 }
