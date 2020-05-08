 package taberystwyth.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 
 import taberystwyth.db.SQLConnection;
 
 public class LocationInsertionFrameListener implements ActionListener {
 
 	private LocationInsertionFrame frame;
 	private SQLConnection conn = SQLConnection.getInstance();
 
 	public LocationInsertionFrameListener(LocationInsertionFrame frame) {
 		this.frame = frame;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Save")) {
 			String statement = "insert into location values ("
 					+ "\"" + frame.name.getText() + "\", \"" + frame.rating.getText()
 					+ "\");";
 			conn.execute(statement);
 		}
 		frame.name.setText("");
 		frame.rating.setText("");
 		try {
 			OverviewFrame.getInstance().refreshLocation();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 }
