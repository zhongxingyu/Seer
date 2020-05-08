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
 
 import org.apache.log4j.Logger;
 
 import taberystwyth.db.TabServer;
 import taberystwyth.view.JudgeInsertionFrame;
 
 /**
  * This class is the listener for the judge insertion frame
  * 
  * @author Roberto Sarrionandia
  * @author Cal Paterson
  *
  */
 public class JudgeInsertionFrameListener implements ActionListener {
     
     private static final Logger LOG = Logger
             .getLogger(JudgeInsertionFrameListener.class);
     
     private JudgeInsertionFrame frame;
     
     @Override
 	public void actionPerformed(ActionEvent e) {
         frame = JudgeInsertionFrame.getInstance();
         if (e.getActionCommand().equals("Save")) {
             try {
                 Connection sql = TabServer.getConnectionPool().getConnection();
                 synchronized (sql) {
                    String s = "insert into judges " +
                    		"(\"name\", " +
                    		"\"institution\", " +
                    		"\"rating\") " +
                    		"values (?,?,?);";
                     PreparedStatement p = sql.prepareStatement(s);
                     p.setString(1, frame.getJudgeName().getText());
                     p.setString(2, frame.getInstitution().getText());
                     p.setString(3, frame.getRating().getText());
                     p.execute();
                     p.close();
                     sql.commit();
                 }
             } catch (SQLException e1) {
                 LOG.error("Unable to insert a judge", e1);
                 JOptionPane.showMessageDialog(frame,
                         "Unable to insert that judge into the database",
                         "Database Error", JOptionPane.ERROR_MESSAGE);
             }
             frame.getJudgeName().setText("");
             frame.getInstitution().setText("");
             frame.getRating().setText("");
         } else if (e.getActionCommand().equals("Clear")) {
             frame.getJudgeName().setText("");
             frame.getInstitution().setText("");
             frame.getRating().setText("");
         }
     }
     
 }
