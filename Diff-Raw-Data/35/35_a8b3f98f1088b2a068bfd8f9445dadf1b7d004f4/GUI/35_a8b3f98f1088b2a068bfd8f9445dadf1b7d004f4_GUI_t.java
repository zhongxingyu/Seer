 package backend;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JToolBar;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.JTabbedPane;
 import javax.swing.JComboBox;
 import javax.swing.JSplitPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.DefaultTableModel;
 
 
 public class GUI {
 	private League theLeague;
 	private JFrame frmFloodFantasyLeague;
 	private MyTableModel homeTable,addTable,tradeTable_1,tradeTable_2,dropTable;
 	private DefaultTableModel homeModel,addModel,tradeModel_1,tradeModel_2,dropModel;
 	private int currentTurn,pick,previousIndex;
 	private final String[] homeHeader={"Rank","Team","Points"},
 		playerInfoHeader={"Player","Position","Points All Season"};
 	
 	public GUI(League game){
 		this.theLeague=game;
 		currentTurn=0;
 		previousIndex=0;
 	}
 	
 	public void populateHome(){
 		User[] rankedTeams= theLeague.getRankedUsers();
 		while(homeModel.getRowCount()>0)
 			homeModel.removeRow(0);
 		String[] tempHome=new String[3];
 		for(int i=rankedTeams.length-1,j=0;i>=0;i--,j++){
 			tempHome[0]=Integer.toString(j);
 			tempHome[1]=rankedTeams[i].getName();
 			tempHome[2]=Double.toString(rankedTeams[i].getPoints());
 			homeModel.addRow(tempHome);
 		}
 	}
 	
 	public void populateAdd(){
 		Player[] rankedPlayers=theLeague.getRankedAvailablePlayers();
 		while(addModel.getRowCount()>0)
 			addModel.removeRow(0);
 		String[] tempAdd=new String[3];
 		for(int i=rankedPlayers.length-1;i>=0;i--){
 			tempAdd[0]=rankedPlayers[i].getName();
 			tempAdd[1]=rankedPlayers[i].getPosition();
 			tempAdd[2]=Double.toString(rankedPlayers[i].getPoints());
 			addModel.addRow(tempAdd);
 		}
 	}
 	
 	public void uploadStats(String fileName){
 		String[][] stats=StatParser.getStats(fileName);
 		if(stats==null){
 			displayError("Upload error!","Sorry, there was an error opening the stat file.");
 			return;
 		}
 		boolean valid=true;
 		for(int i=0;i<stats.length;i++){
 			valid=valid && League.athletes.containsKey(stats[i][0]);
 			if(!valid){
 				displayError("Athelete doesn't exist!",stats[i][0]+" is not a valid athlete.");
 				return;
 			}
 			valid=valid && League.ptsDist.containsKey(stats[i][1]);
 			if(!valid){
 				displayError("Action doesn't exist!",stats[i][1]+" Is not a valid action.");
 				return;
 			}
 			valid=valid && Integer.parseInt(stats[i][2])>0;
 			if(!valid){
 				displayError("Quantity must be positive!",stats[i][2]+" is not a positive number greater than zero.");
 				return;
 			}
 		}
 		for(int i=0;i<stats.length;i++){
 			double pts=League.ptsDist.get(stats[i][1]).getPoints() * Integer.parseInt(stats[i][2]);
 			Player temp=League.athletes.get(stats[i][0]);
 			temp.addPoints(pts);
 		}
 	}
 	
 	public void displayError(String title,String message){
 		JOptionPane.showMessageDialog(frmFloodFantasyLeague, message,title,JOptionPane.ERROR_MESSAGE);
 	}
 	
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	public void drawBoard() {
 		frmFloodFantasyLeague = new JFrame();
 		frmFloodFantasyLeague.setBackground(new Color(0, 0, 205));
 		frmFloodFantasyLeague.setTitle("FLOOD Fantasy League");
 		frmFloodFantasyLeague.setBounds(100, 100, 450, 300);
 		frmFloodFantasyLeague.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmFloodFantasyLeague.setSize(new Dimension(700, 400));
 		frmFloodFantasyLeague.setVisible(true);
 		
 		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		frmFloodFantasyLeague.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 		
 		
 		JScrollPane homePane = new JScrollPane();
 		tabbedPane.addTab("Home", null, homePane, null);
 		
 		homeModel = new DefaultTableModel(homeHeader,0);
 		homeTable = new MyTableModel(homeModel);
 		homeTable.setEnabled(false);
 		homePane.setViewportView(homeTable);
 		
 		populateHome();
 		
 		JSplitPane addSplitPane = new JSplitPane();
 		addSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		tabbedPane.addTab("Add", null, addSplitPane, null);
 		
 		JScrollPane addScrollPane = new JScrollPane();
 		addSplitPane.setRightComponent(addScrollPane);
 		
 		addModel = new DefaultTableModel(playerInfoHeader,0);
 		addTable = new MyTableModel(addModel);
 		addTable.setRowSelectionAllowed(true);
 		addTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		addScrollPane.setViewportView(addTable);
 		
 		JToolBar addToolBar = new JToolBar();
 		addToolBar.setFloatable(false);
 		addSplitPane.setLeftComponent(addToolBar);
 		
 		
 		
 		final JLabel addLabel = new JLabel();
 		addLabel.setMinimumSize(new Dimension(600, 15));
 		addLabel.setMaximumSize(new Dimension(32767, 15));
 		addToolBar.add(addLabel);
 		
 		JButton btnAdd = new JButton("Add");
 		btnAdd.setPreferredSize(new Dimension(100, 25));
 		btnAdd.setMaximumSize(new Dimension(100, 25));
 		btnAdd.setMinimumSize(new Dimension(100, 25));
 		addToolBar.add(btnAdd);
 		
 		JSplitPane tradeSplitPane = new JSplitPane();
 		tradeSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		tabbedPane.addTab("Trade", null, tradeSplitPane, null);
 		
 		JToolBar tradeToolBar = new JToolBar();
 		tradeToolBar.setFloatable(false);
 		tradeSplitPane.setLeftComponent(tradeToolBar);
 		
 		final JComboBox tradeComboBox_1 = new JComboBox();
 		tradeToolBar.add(tradeComboBox_1);
 		
 		final JComboBox tradeComboBox_2 = new JComboBox();
 		tradeToolBar.add(tradeComboBox_2);
 		
 		JButton btnTrade = new JButton("Trade");
 		tradeToolBar.add(btnTrade);
 		
 		JSplitPane tradeSplitPane_2 = new JSplitPane();
 		tradeSplitPane_2.setResizeWeight(0.5);
 		tradeSplitPane.setRightComponent(tradeSplitPane_2);
 		
 		JScrollPane tradeScrollPane_1 = new JScrollPane();
 		tradeSplitPane_2.setLeftComponent(tradeScrollPane_1);
 		
 		tradeModel_1 = new DefaultTableModel(playerInfoHeader,0);
 		tradeTable_1 = new MyTableModel(tradeModel_1);
 		tradeTable_1.setRowSelectionAllowed(true);
 		tradeTable_1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		tradeScrollPane_1.setViewportView(tradeTable_1);
 		
 		JScrollPane tradeScrollPane_2 = new JScrollPane();
 		tradeSplitPane_2.setRightComponent(tradeScrollPane_2);
 		
 		tradeModel_2 = new DefaultTableModel(playerInfoHeader,0);
 		tradeTable_2 = new MyTableModel(tradeModel_2);
 		tradeTable_2.setRowSelectionAllowed(true);
 		tradeTable_2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		tradeScrollPane_2.setViewportView(tradeTable_2);
 		
 		JSplitPane dropSplitPane = new JSplitPane();
 		dropSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		tabbedPane.addTab("Drop", null, dropSplitPane, null);
 		
 		JToolBar dropToolBar = new JToolBar();
 		dropToolBar.setFloatable(false);
 		dropSplitPane.setLeftComponent(dropToolBar);
 		
 		final JComboBox dropComboBox = new JComboBox();
 		dropToolBar.add(dropComboBox);
 		
 		JButton btnDrop = new JButton("Drop");
 		dropToolBar.add(btnDrop);
 		
 		JScrollPane dropScrollPane = new JScrollPane();
 		dropSplitPane.setRightComponent(dropScrollPane);
 		
 		dropModel = new DefaultTableModel(playerInfoHeader,0);
 		dropTable = new MyTableModel(dropModel);
		dropTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		dropScrollPane.setViewportView(dropTable);
 		
 		JPanel uploadPanel = new JPanel();
 		tabbedPane.addTab("Upload Stat File", null, uploadPanel, null);
 		
 		User[] rankedTeams= theLeague.getRankedUsers();
 		for(int i=0;i<rankedTeams.length;i++){
 			tradeComboBox_1.insertItemAt(rankedTeams[i].getName(),i);
 			tradeComboBox_2.insertItemAt(rankedTeams[i].getName(),i);
 			dropComboBox.insertItemAt(rankedTeams[i].getName(),i);
 		}
 		
 		//Fix populating tables
 		
 		tabbedPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				int selection = tabbedPane.getSelectedIndex();
 				switch(selection){
 				case 0:
 					populateHome();
 					previousIndex=selection;
 					break;
 				case 1:	//Populate add table
 					populateAdd();
 					previousIndex=selection;
 					break;
 				case 4:
 					JFileChooser chooser=new JFileChooser();
 					int result = chooser.showOpenDialog(null);
 					switch (result) {
 					case JFileChooser.APPROVE_OPTION:
 						File file=chooser.getSelectedFile();
 						uploadStats(file.getAbsolutePath());
 						break;
 					case JFileChooser.CANCEL_OPTION:
 						break;
 					case JFileChooser.ERROR_OPTION:
 						displayError("Upload Error!","Sorry, there was an error opening the stat file.");
 						break;
 					}
 					tabbedPane.setSelectedIndex(previousIndex);
 					break;
 				default:
 					previousIndex=selection;
 				}
 			}
 		});
 		
 		pick=Test.draftFunction(currentTurn);	//Gets the number representing the user's turn
 		addLabel.setText(theLeague.getUser(pick).getName()+"'s turn!");	//Puts the user's name in the label 
 		
 		btnAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				for(int i=0;i<addTable.getRowCount();i++){	//Iterate through table entries
 					if(addTable.isCellSelected(i,0)){	//If it's selected
 						if(!Test.draftPlayer(theLeague.getUser(pick),League.athletes.get(addModel.getValueAt(i,0)))){
							displayError("Invalid draft!","Sorry, your draft violates rules of the league.");
 							return;
 						}
 						currentTurn++;
 						addModel.removeRow(i);
 						pick=Test.draftFunction(currentTurn);
 						addLabel.setText(theLeague.getUser(pick).getName()+"'s turn!");
 						tradeComboBox_1.setSelectedIndex(-1);
 						tradeComboBox_2.setSelectedIndex(-1);
 						dropComboBox.setSelectedIndex(-1);
 						for(int j=0;j<tradeModel_1.getRowCount();j++){
 				    		tradeModel_1.removeRow(j);
 				    	}
 						for(int j=0;j<tradeModel_2.getRowCount();j++){
 				    		tradeModel_2.removeRow(j);
 				    	}
 						for(int j=0;j<dropModel.getRowCount();j++){
 				    		dropModel.removeRow(j);
 				    	}
						return;
 					}
 				}
 				displayError("Add error!","Sorry, no player was selected!");
 			}
 		});
 		
 		tradeComboBox_1.addActionListener (new ActionListener () {
 		    public void actionPerformed(ActionEvent e) {
 		    	if(tradeComboBox_1.getSelectedIndex()!=-1){
 			    	Player[] teamPlayers=League.teams.get(tradeComboBox_1.getSelectedItem()).getPlayers();
 			    	while(tradeModel_1.getRowCount()>0){
 			    		tradeModel_1.removeRow(0);
 			    	}
 			    	for(int i=0;i<teamPlayers.length;i++){
 			    		String[] temp={teamPlayers[i].getName(),teamPlayers[i].getPosition(),Double.toString(teamPlayers[i].getPoints())};
 			    		tradeModel_1.addRow(temp);
 			    	}
 		    	}
 		    }
 		});
 		
 		tradeComboBox_2.addActionListener (new ActionListener () {
 		    public void actionPerformed(ActionEvent e) {
 		    	if(tradeComboBox_2.getSelectedIndex()!=-1){
 			    	Player[] teamPlayers=League.teams.get(tradeComboBox_2.getSelectedItem()).getPlayers();
 			    	while(tradeModel_2.getRowCount()>0){
 			    		tradeModel_2.removeRow(0);
 			    	}
 			    	for(int i=0;i<teamPlayers.length;i++){
 			    		String[] temp={teamPlayers[i].getName(),teamPlayers[i].getPosition(),Double.toString(teamPlayers[i].getPoints())};
 			    		tradeModel_2.addRow(temp);
 			    	}
 		    	}
 		    }
 		});
 		
 		btnTrade.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (tradeComboBox_1.getSelectedIndex()==-1 || tradeComboBox_2.getSelectedIndex()==-1) {
 					displayError("Trade error!","Must select two teams to trade between.");
 					return;
 				}
 				else if(tradeComboBox_1.getSelectedIndex()==tradeComboBox_2.getSelectedIndex()){
 					displayError("Trade error!","Must select two different teams to trade between.");
 					return;
 				}
 				int rows1[] = tradeTable_1.getSelectedRows();
 				int rows2[] = tradeTable_2.getSelectedRows();
 				if(rows1.length==0 && rows2.length==0){
 					displayError("Trade error!","Must select at least one player to trade.");
 					return;
 				}
 				Player[] p1 = new Player[rows1.length];
 				Player[] p2 = new Player[rows2.length];
 				for (int i = 0; i < p1.length; i++) {
 					p1[i] = League.athletes.get(tradeModel_1.getValueAt(rows1[i],0));
 				}
 				for (int i = 0; i < p2.length; i++) {
 					p2[i] = League.athletes.get(tradeModel_2.getValueAt(rows2[i],0));
 				}
 				boolean success=Test.trade(League.teams.get(tradeComboBox_1
 						.getSelectedItem()), p1, League.teams
 						.get(tradeComboBox_2.getSelectedItem()), p2);
 				if(!success){
 					displayError("Invalid trade!","Sorry, your trade violates rules of the league.");
 					return;
 				}
 				Player[] teamPlayers = League.teams.get(tradeComboBox_1.getSelectedItem()).getPlayers();
 				while (tradeModel_1.getRowCount() > 0) {
 					tradeModel_1.removeRow(0);
 				}
 				for (int i = 0; i < teamPlayers.length; i++) {
 					String[] temp = { teamPlayers[i].getName(),
 							teamPlayers[i].getPosition(),
 							Double.toString(teamPlayers[i].getPoints()) };
 					tradeModel_1.addRow(temp);
 				}
 				teamPlayers = League.teams.get(tradeComboBox_2.getSelectedItem()).getPlayers();
 				while (tradeModel_2.getRowCount() > 0) {
 					tradeModel_2.removeRow(0);
 				}
 				for (int i = 0; i < teamPlayers.length; i++) {
 					String[] temp = { teamPlayers[i].getName(),
 							teamPlayers[i].getPosition(),
 							Double.toString(teamPlayers[i].getPoints()) };
 					tradeModel_2.addRow(temp);
 				}
 			}
 		});
 		
 		dropComboBox.addActionListener (new ActionListener () {
 		    public void actionPerformed(ActionEvent e) {
		    	if(dropComboBox.getSelectedIndex()!=-1){
 			    	Player[] teamPlayers=League.teams.get(dropComboBox.getSelectedItem()).getPlayers();
 			    	for(int i=0;i<dropModel.getRowCount();i++){
 			    		dropModel.removeRow(i);
 			    	}
 			    	for(int i=0;i<teamPlayers.length;i++){
 			    		String[] temp={teamPlayers[i].getName(),teamPlayers[i].getPosition(),Double.toString(teamPlayers[i].getPoints())};
 			    		dropModel.addRow(temp);
 			    	}
 		    	}
 		    }
 		});
 		
 		btnDrop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int index=dropTable.getSelectedRow();
 				if(index==-1){
 					displayError("Drop error!","Must select a team to drop a player from.");
 					return;
 				}
 				Player drop=League.athletes.get(dropModel.getValueAt(index,0));
 				boolean success=Test.dropPlayer(League.teams.get(dropComboBox.getSelectedItem()),drop);
 				if(!success){
 					displayError("Invalid drop!","Sorry, your drop violates rules of the league.");
 					return;
 				}
 				dropModel.removeRow(index);
 				tradeComboBox_1.setSelectedIndex(-1);
 				tradeComboBox_2.setSelectedIndex(-1);
 			}
 		});
 		
 		tabbedPane.setSelectedIndex(0);
 	}
 }
 class MyTableModel extends JTable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public MyTableModel(){
 		super();
 	}
 	public MyTableModel(DefaultTableModel model){
 		super(model);
 	}
 	public MyTableModel(Object[][] data,Object[] columnNames){
 		super(data,columnNames);
 	}
     public boolean isCellEditable(int row, int col) {
     	return false;
     }
 }
