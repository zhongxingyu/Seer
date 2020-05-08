 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.Rectangle;
 import java.io.File;
 import java.sql.*;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractListModel;
 import javax.swing.SwingUtilities;
 
 import edu.cmu.cs.diamond.opendiamond.*;
 import edu.cmu.cs.diamond.wholeslide.Wholeslide;
 
 final public class SearchModel extends AbstractListModel implements
         SearchEventListener {
     protected volatile boolean running;
 
     final protected Search search;
 
     final protected int limit;
 
     final protected Object lock = new Object();
 
     final protected List<WholeslideRegionResult> list = new LinkedList<WholeslideRegionResult>();
 
     public SearchModel(Search search, final Wholeslide ws, int limit) {
         this.search = search;
         this.limit = limit;
 
         search.addSearchEventListener(this);
 
         Thread t = new Thread(new Runnable() {
 
             private Connection conn;
 
             private PreparedStatement ps;
 
             public void run() {
                 initSQL();
 
                 // wait for start
                 synchronized (lock) {
                     while (!running) {
                         try {
                             System.out.println("waiting for start signal");
                             lock.wait();
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                     }
                 }
 
                 try {
                     int i = 0;
 
                     Pattern p = Pattern
                            .compile("/([^/-]+)-(\\d+)-(\\d+)\\.ppm$");
 
                     while (running && i < SearchModel.this.limit) {
                         final Result r = SearchModel.this.search
                                 .getNextResult();
                         if (r == null) {
                             break;
                         }
 
//                        System.out.println(r);
 
                         String name = r.getObjectName();
                         // TODO get metadata from the server in a different way
                         Matcher m = p.matcher(name);
                         if (!m.find()) {
                             continue;
                         }
 
                         String caseName = m.group(1);
                         int x = Integer.parseInt(m.group(2));
                         int y = Integer.parseInt(m.group(3));
 
                         Wholeslide resultWS = new Wholeslide(
                                 getFileForCaseName(caseName));
 
                         String sqlResults[] = getCaseInfo(caseName);
 
                         Rectangle bb = new Rectangle(x, y, 512, 512);
 
                         list
                                 .add(new WholeslideRegionResult(
                                         resultWS,
                                         bb,
                                         Util
                                                 .extractDouble(r
                                                         .getValue("_matlab_ans.double")) / 10000.0,
                                         150, sqlResults[0], sqlResults[1],
                                         sqlResults[2]));
 
                         final int index = list.size();
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 fireIntervalAdded(SearchModel.this, index,
                                         index);
                             }
                         });
                     }
                 } catch (InterruptedException e) {
                 } finally {
                     System.out.println("search done");
                     running = false;
 
                     try {
                         ps.close();
                     } catch (SQLException e) {
                         e.printStackTrace();
                     }
                 }
             }
 
             private File getFileForCaseName(String caseName) {
                 // XXX hardcoded
                 if (caseName.equals("file1")) {
                     caseName = "case3";
                 } else if (caseName.equals("cases9and10")) {
                     caseName = "cases9&10";
                 }
                
                System.out.println("loading wholeslide for " + caseName);
 
                 return new File("/home/agoode/dd/TRESTLE - 20x Images",
                         caseName.toUpperCase() + ".tif");
             }
 
             private void initSQL() {
                 try {
                     Class.forName("com.mysql.jdbc.Driver");
                 } catch (ClassNotFoundException e) {
                     e.printStackTrace();
                 }
 
                 try {
                     conn = DriverManager.getConnection(
                             "jdbc:mysql://kohinoor.diamond.cs.cmu.edu/diamond",
                             "diamond", "xxxxxxxx");
                     ps = conn
                             .prepareStatement("select clinical_history, diagnosis from pathology_case where name=?");
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
 
             private String[] getCaseInfo(String caseName) {
                 String r[] = new String[3];
 
                 if (caseName.equals("file1")) {
                     caseName = "case3";
                 }
 
                 try {
                     ps.setString(1, caseName);
 
                     System.out.println(ps);
 
                     ResultSet rs = ps.executeQuery();
                     while (rs.next()) {
                         String ch = rs.getString(1);
                         String d = rs.getString(2);
 
                         r[0] = ch.substring(0, 10) + "...<br>"
                                 + d.substring(0, 10) + "...";
                         r[1] = ch + "<br>" + d;
                         r[2] = "<h1>Clinical History</h1>" + ch
                                 + "<h1>Diagnosis</h1>" + d;
                         break;
                     }
                     rs.close();
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
                 return r;
             }
         });
         t.setDaemon(true);
         t.start();
     }
 
     public void searchStarted(SearchEvent e) {
         synchronized (lock) {
             System.out.println("sending start notify");
             running = true;
             lock.notify();
         }
     }
 
     public void searchStopped(SearchEvent e) {
         running = false;
     }
 
     public void removeSearchListener() {
         search.removeSearchEventListener(this);
     }
 
     public Object getElementAt(int index) {
         return list.get(index);
     }
 
     public int getSize() {
         return list.size();
     }
 }
