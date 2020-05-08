 package taberystwyth.view;
 
 import java.awt.GridLayout;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import taberystwyth.db.SQLConnection;
 
 public class ViewRoundFrame extends JFrame {
 	
 	JLabel roundLabel = new JLabel("Round:");
 	JLabel motionLabel = new JLabel("Motion:");
 	
 	JComboBox rounds = new JComboBox();
 	
 	JTextField motion = new JTextField();
 	
 	JButton clear = new JButton("Clear");
 	JButton view = new JButton("View");
 	
 	Vector<JLabel> roundOptions = new Vector<JLabel>();
 	
 
 	public ViewRoundFrame(){
 		setVisible(true);
 		setTitle("View Rounds");
 		
 		setLayout(new GridLayout(3,2));
 		
 		
 		add(roundLabel);
 		add(rounds);
 		
 		addRounds();
 		
 		add(motionLabel);
 		add(motion);
 		
 		add(clear);
 		add(view);
 		
 		pack();
 	}
 
 
 	private void addRounds() {
 		SQLConnection db = SQLConnection.getInstance();
		ResultSet rs = db.executeQuery("select distinct round from room;");
 		
 		try {
 			while(rs.next()){
				rounds.addItem(rs.getString("round"));
 			}
 		} catch (SQLException e) {
 			db.panic(e,"Unable to select roundnumbers");
 		}
 		
 
 	}
 }
