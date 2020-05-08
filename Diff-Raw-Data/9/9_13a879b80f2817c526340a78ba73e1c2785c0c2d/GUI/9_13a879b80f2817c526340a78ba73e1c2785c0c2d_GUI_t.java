 package gui;
 
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 
 import liarsDiceModel.Facade;
 import liarsDiceModel.LiarsDiceGameFactory;
 import liarsDiceModel.Player;
 import liarsDiceModel.Statistics;
 
 public class GUI extends JFrame {
    
     private Facade facade;
     
     private JButton runButton;
     private StatsTableModel statsTableModel;
     private JTable statsTable;
     private int numPlayersPerGame;
     private int numGameRepeatsPerTournament;
    
     public GUI()
     {
     	facade = new Facade();
     	
     	//defaults for tournament setup
     	facade.chooseGame(new LiarsDiceGameFactory());
    	numPlayersPerGame = 3;
     	numGameRepeatsPerTournament = 1;
     	
     	//setup the general layout
         Container pane = getContentPane();
         JPanel tournamentPane = new JPanel();
         tournamentPane.setLayout(new BoxLayout(tournamentPane, BoxLayout.Y_AXIS));
         JPanel playPane = new JPanel();
         playPane.setLayout(new BoxLayout(playPane, BoxLayout.Y_AXIS));
         
         //tabs
         JTabbedPane tabPane = new JTabbedPane();
         tabPane.addTab("Tournament", tournamentPane);
         tabPane.addTab("Play", playPane);
         pane.add(tabPane);
        
         //tournament view
         runButton = new JButton("Run Tournament");
         runButton.setPreferredSize(new Dimension(160,20));
         runButton.addActionListener(new ButtonListener());
         tournamentPane.add(runButton);
         
         statsTableModel = new StatsTableModel();
         statsTable = new JTable(statsTableModel);
         statsTable.setPreferredScrollableViewportSize(new Dimension(1000, 300));
         statsTable.setFillsViewportHeight(true);
         JScrollPane statsTableScrollPane = new JScrollPane(statsTable);
         tournamentPane.add(statsTableScrollPane);
         
         //play view: unimplemented
         
 
         //wrapup
         setTitle("The Programmer's Tournament");
         setLocation(500, 600);
         setVisible(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         pack();
     }
    
     
     private class StatsTableModel extends AbstractTableModel {
         private String[] columnNames = {
         		"Player ID", 
         		"Bot Name", 
         		"Wins", 
         		"Losses", 
         		"Exceptions", 
         		"Invalid Decisions",
         		"Timeouts"};
         private Object[][] data = new Object[][] {};
         
         public void loadTable(Facade f) {
         	java.util.List<Player> players = f.getPlayers();
         	data = new Object[players.size()][];
         	for (int p=0; p<players.size(); p++)
         	{
             	data[p] = new Object[7];
         		Statistics stats = players.get(p).getStatistics(); 
         		
         		setValueAt(players.get(p).getID(), p, 0);
         		setValueAt(players.get(p).getName(), p, 1);
         		setValueAt(stats.getWins(), p, 2);
         		setValueAt(stats.getLosses(), p, 3);
         		setValueAt(stats.getExceptions(), p, 4);
         		setValueAt(stats.getInvalidDecisions(), p, 5);
         		setValueAt(stats.getTimeouts(), p, 6);
         	}
         }
 
         public int getColumnCount() {
             return columnNames.length;
         }
 
         public int getRowCount() {
             return data.length;
         }
 
         public String getColumnName(int col) {
             return columnNames[col];
         }
 
         public Object getValueAt(int row, int col) {
             return data[row][col];
         }
 
         public Class getColumnClass(int c) {
             return getValueAt(0, c).getClass();
         }
 
         /*
          * Don't need to implement this method unless your table's
          * editable.
         public boolean isCellEditable(int row, int col) {
             //Note that the data/cell address is constant,
             //no matter where the cell appears onscreen.
             if (col < 2) {
                 return false;
             } else {
                 return true;
             }
         }
          */
 
         public void setValueAt(Object value, int row, int col) {
             data[row][col] = value;
             fireTableCellUpdated(row, col);
         }
     }
 
 
 	private class ButtonListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e) {
 			facade.runTournament(numPlayersPerGame, numGameRepeatsPerTournament);
 			statsTableModel.loadTable(facade);
         }
     }
 	
     public static void main(String[] args)
     {
     	GUI gui = new GUI();
     }
 }
