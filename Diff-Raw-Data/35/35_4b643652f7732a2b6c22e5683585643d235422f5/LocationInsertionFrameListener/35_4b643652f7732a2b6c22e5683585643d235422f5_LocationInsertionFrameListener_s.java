 package taberystwyth.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 
 import taberystwyth.db.SQLConnection;
 import taberystwyth.view.LocationInsertionFrame;
 import taberystwyth.view.OverviewFrame;
 
 public class LocationInsertionFrameListener implements ActionListener {
 
	private LocationInsertionFrame frame = LocationInsertionFrame.getInstance();
 	private SQLConnection conn = SQLConnection.getInstance();
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Save")) {
			String statement = "insert into location (name, location) values (" + 
 							   "\"" +
 							   frame.getLocationName().getText() + 
 							   "\", \"" + 
 							   frame.getRating().getText() +
 							   "\");";
 			conn.execute(statement);
 
 		}
 		if (e.getActionCommand().equals("Clear")){
 			frame.getLocationName().setText("");
 			frame.getRating().setText("");
 		}
 	}
 
 }
