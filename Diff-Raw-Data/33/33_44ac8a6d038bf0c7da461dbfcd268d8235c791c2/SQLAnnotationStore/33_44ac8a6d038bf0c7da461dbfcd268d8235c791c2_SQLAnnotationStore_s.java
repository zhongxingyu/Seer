 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008-2010 Carnegie Mellon University
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
 
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.cmu.cs.openslide.gui.Annotation;
 import edu.cmu.cs.openslide.gui.DefaultSelectionListModel;
 import edu.cmu.cs.openslide.gui.SelectionListModel;
 
 public class SQLAnnotationStore implements AnnotationStore {
 
     final private Connection conn;
 
     final private PreparedStatement insertRoiStatement;
 
     final private PreparedStatement insertRoiSlideStatement;
 
     final private PreparedStatement insertAnnotationStatement;
 
     final private PreparedStatement deleteAllStatement;
 
     final private PreparedStatement selectStatement;
 
     public SQLAnnotationStore(String sqlHost, String sqlUsername,
             String sqlPassword, String sqlDatabase)
             throws ClassNotFoundException, SQLException {
         Class.forName("com.mysql.jdbc.Driver");
 
         conn = DriverManager.getConnection("jdbc:mysql://" + sqlHost + "/"
                 + sqlDatabase, sqlUsername, sqlPassword);
         conn.setAutoCommit(false);
 
         deleteAllStatement = conn
                 .prepareStatement("update roi_slide set deleted=1 where quickhash1=?;");
 
         insertRoiStatement = conn.prepareStatement(
                 "insert into roi (path, author_id) values (?,?);",
                 new String[] { "id" });
 
         insertRoiSlideStatement = conn
                 .prepareStatement("insert into roi_slide (quickhash1, roi_id) values (?,?);");
 
         insertAnnotationStatement = conn
                 .prepareStatement("insert into annotation (roi_id, author_id, text) values (?,?,?)");
 
         selectStatement = conn
                 .prepareStatement("select text, path, author.name from roi left join (annotation) on (roi.id=annotation.roi_id) join (roi_slide) on (roi_slide.roi_id=roi.id) join (author) on (author.id=roi.author_id) where deleted=0 and quickhash1=?;");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * edu.cmu.cs.diamond.pathfind.AnnotationStore#saveAnnotations(java.lang
      * .String, edu.cmu.cs.openslide.gui.SelectionListModel)
      */
 
     // TODO preserve existing ids
     @Override
     public void saveAnnotations(String qh1, SelectionListModel ssModel)
             throws IOException {
         // clear all
         try {
             deleteAllStatement.setString(1, qh1);
             deleteAllStatement.execute();
 
             // add all
             for (Annotation a : ssModel) {
                SlideAnnotation sa = (SlideAnnotation) a;
                String path = SlideAnnotation.shapeToString(sa.getShape());
 
                 // insert path
                 insertRoiStatement.setString(1, path);
                 insertRoiStatement.setInt(2, 1);
                 insertRoiStatement.execute();
                 ResultSet keys = insertRoiStatement.getGeneratedKeys();
                 keys.next();
                 int roiID = keys.getInt(1);
 
                 // insert text, if not null
                for (SlideAnnotationNote n : sa.getNotes()) {
                    String text = n.getText();
                    if (text != null) {
                        insertAnnotationStatement.setInt(1, roiID);
                        insertAnnotationStatement.setInt(2, 1);
                        insertAnnotationStatement.setString(3, text);
                        insertAnnotationStatement.execute();
                     }

                    // tie it to the slide
                    insertRoiSlideStatement.setString(1, qh1);
                    insertRoiSlideStatement.setInt(2, roiID);
                    insertRoiSlideStatement.execute();
                 }
             }
 
             conn.commit();
         } catch (SQLException e) {
             throw new IOException(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * edu.cmu.cs.diamond.pathfind.AnnotationStore#getAnnotations(java.lang.
      * String)
      */
 
     // TODO read more than one note
     @Override
     public SelectionListModel getAnnotations(String quickhash1)
             throws IOException {
         DefaultSelectionListModel slm = new DefaultSelectionListModel();
 
         try {
             selectStatement.setString(1, quickhash1);
             ResultSet r = selectStatement.executeQuery();
 
             while (r.next()) {
                 String text = r.getString(1);
                 String path = r.getString(2);
                 String author = r.getString(3);
 
                 SlideAnnotationNote n = new SlideAnnotationNote(text);
                 List<SlideAnnotationNote> notes = new ArrayList<SlideAnnotationNote>();
                 notes.add(n);
                 SlideAnnotation a = new SlideAnnotation(SlideAnnotation
                         .stringToShape(path), null, author, notes);
 
                 slm.add(a);
             }
 
             return slm;
         } catch (SQLException e) {
             throw new IOException(e);
         }
     }
 }
