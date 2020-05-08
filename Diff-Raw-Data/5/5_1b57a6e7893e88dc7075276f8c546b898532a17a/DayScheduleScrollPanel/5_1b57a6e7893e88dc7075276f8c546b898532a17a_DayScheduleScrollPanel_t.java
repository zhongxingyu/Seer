 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * DayScheduleScrollPanel.java
  *
  * Created on May 15, 2009, 2:04:14 AM
  */
 
 package gui;
 
 // Self contained packages
 import model.*;
 
 // Java packages
 import javax.swing.tree.*;
 import javax.swing.tree.DefaultTreeModel;
 import java.util.Iterator;
 import java.util.Calendar;
 /**
  *
  * @author kurtisschmidt
  */
 public class DayScheduleScrollPanel extends javax.swing.JPanel {
 
     private DefaultMutableTreeNode rootNode;
     private DefaultMutableTreeNode timeNodes[];
     private DefaultTreeModel treeModel;
     private SongScheduler songScheduler;
     private Schedule schedules[];
 
     /** Creates new form DayScheduleScrollPanel */
     public DayScheduleScrollPanel( SongScheduler songScheduler, Time day ) {
         this.rootNode  = new DefaultMutableTreeNode("RootNode");
         this.timeNodes = new DefaultMutableTreeNode[24];
         this.schedules = new Schedule[24];
         this.songScheduler = songScheduler;
 
         // TODO: Get data for each hour or something.
         Time currentTime = day;
         for ( int i = 0; i < 24; i++ ) {
             schedules[i] = songScheduler.getSchedule( currentTime );
             timeNodes[i] = new DefaultMutableTreeNode(schedules[i]);
             rootNode.add( timeNodes[i] );
 
             Iterator<Song> iter = schedules[i].iterator();
             
             while( iter.hasNext() )
             {
                 timeNodes[i].add( new DefaultMutableTreeNode(iter.next()) );
             }
 
             // Move to the next hour
             currentTime = currentTime.getNextHour();
         }
 
         initComponents();
         jTree1.setCellRenderer( new DayTreeCellRenderer() );
         treeModel = (DefaultTreeModel)jTree1.getModel();
     }
 
     public void deleteSelectedSongs() {
         DefaultMutableTreeNode selected[] = getSelected();
 
         if (selected == null)
             return;
 
         for (int i = 0; i < selected.length; i++ ) {
             DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selected[i].getParent();
 
             // This is an hour node, delete it from the database.
             if ( parent == rootNode )
             {
                 // TODO: Delete schedule from database.
             }
             else
             {
                 Schedule schedule = (Schedule)parent.getUserObject();
                 Song song = (Song)selected[i].getUserObject();
 
                 schedule.remove( song );
                 treeModel.removeNodeFromParent( selected[i] );
             }
         }
     }
 
     public void addSong( Song song ) throws Exception {
         DefaultMutableTreeNode selected[] = getSelected();
 
         if ( selected.length > 1 )
         {
             throw new Exception("Error: Cannot add to more than 1 place at a time.");
         }
         else if ( selected.length < 1 )
         {
             throw new Exception("Error: Please select an hour or position to add the song.");
         }
 
         DefaultMutableTreeNode selectedNode = selected[0];
         DefaultMutableTreeNode scheduleNode = null;
         Object selectedItem = selectedNode.getUserObject();
 
         // Add the song to the selected Schedule
         if ( selectedItem instanceof Schedule )
         {
             Schedule schedule = (Schedule)selectedItem;
             schedule.add( song );
 
             scheduleNode = selectedNode;
         }
         else if ( selectedItem instanceof Song )
         {
             Schedule schedule = (Schedule)((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject();
 
             // TODO: Create a function to add to arbitrary places.
             schedule.add( song );
 
             scheduleNode = (DefaultMutableTreeNode)selectedNode.getParent();
         }
 
         DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(song);
         treeModel.insertNodeInto( childNode, scheduleNode, scheduleNode.getChildCount());
     }
 
     public void generateSchedules() throws Exception {
         DefaultMutableTreeNode selected[] = getSelected();
 
         /**********************************************************************
          * This section checks for error and ends the function if one is found.
          *********************************************************************/
         if ( selected.length < 1 )
         {
             throw new Exception("Error: Please select an hour or position to add the song.");
         }
 
         // Check each DefaultMutableTree, if one is a song, return error.
         for ( int i = 0; i < selected.length; i++ ) {
             if ( selected[i].getUserObject() instanceof Song )
                 throw new Exception("Error: You must select only hours, not songs before generating.");
         }
 
         /**********************************************************************
          * This section generates the schedules for each selected hour
          *********************************************************************/
         for ( int i = 0; i < selected.length; i++ ) {
             Schedule schedule = (Schedule)selected[i].getUserObject();
             songScheduler.generateOneHour( schedule.getTime() );
 
            selected[i].removeAllChildren();
            treeModel.reload( selected[i] );
             Iterator<Song> iter = schedule.iterator();
             while ( iter.hasNext() )
             {
                treeModel.insertNodeInto( new DefaultMutableTreeNode( (Song)iter.next() ), selected[i], selected[i].getChildCount() );
             }
         }
     }
     
     public DefaultMutableTreeNode[] getSelected()
     {
         javax.swing.tree.TreePath paths[] = jTree1.getSelectionPaths();
 
         if (paths == null)
             return null;
         
         DefaultMutableTreeNode selected[] = new DefaultMutableTreeNode[paths.length];
 
         for ( int i = 0; i < selected.length; i++ )
         {
             selected[i] = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
         }
 
         return selected;
     }
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jTree1 = new javax.swing.JTree();
 
         jTree1.setModel(new javax.swing.tree.DefaultTreeModel(rootNode));
         jTree1.setAlignmentX(0.0F);
         jTree1.setAlignmentY(0.0F);
         jTree1.setRootVisible(false);
         jTree1.setShowsRootHandles(true);
         jScrollPane1.setViewportView(jTree1);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTree jTree1;
     // End of variables declaration//GEN-END:variables
 
 }
