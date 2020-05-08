 /*
  * This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  * 
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package taberystwyth.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.swing.JOptionPane;
 
 import taberystwyth.db.SQLConnection;
 import taberystwyth.view.LocationInsertionFrame;
 import taberystwyth.view.OverviewFrame;
 
 public class LocationInsertionFrameListener implements ActionListener {
     
    private LocationInsertionFrame frame = 
        LocationInsertionFrame.getInstance();
    
     private static LocationInsertionFrameListener instance = 
         new LocationInsertionFrameListener();
     
     public static LocationInsertionFrameListener getInstance(){
         return instance;
     }
     
     private LocationInsertionFrameListener(){
     }
     
     private SQLConnection sql = SQLConnection.getInstance();
     
     public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals("Save")) {
             synchronized (SQLConnection.getInstance()) {
                 Connection conn = sql.getConn();
                 try {
                     PreparedStatement p = conn
                             .prepareStatement("insert into locations (name, rating) values (?, ?);");
                     p.setString(1, frame.getLocationName().getText());
                     p.setInt(2,
                             Integer.parseInt((frame.getRating().getText())));
                     p.execute();
                     p.close();
                     System.out.println("entered team");
                     sql.cycleConn();
                     System.out.println("cycled conn");
                     sql.notifyObservers();
                     System.out.println("notified obs");
                 } catch (SQLException e1) {
                     JOptionPane
                             .showMessageDialog(
                                     OverviewFrame.getInstance(),
                                     "Was unable to insert that location into the database",
                                     "Unable to insert",
                                     JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
         if (e.getActionCommand().equals("Clear")) {
             frame.getLocationName().setText("");
             frame.getRating().setText("");
         }
     }
     
 }
