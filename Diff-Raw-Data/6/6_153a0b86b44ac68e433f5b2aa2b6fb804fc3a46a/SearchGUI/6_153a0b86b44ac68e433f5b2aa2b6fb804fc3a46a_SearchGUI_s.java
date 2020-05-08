 package mediastore;
 
 /**
  * Name: Milton John
  * Section: 1
  * Program: Lab
  * Date: Apr 18, 2013
  */
 /**
  * A class that displays the results of a search
  *
  * @author Milton John, Cole Arnold, Ryan Smith
  * @version 1.0 Apr 18, 2013
  *
  */
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 
 public class SearchGUI extends JFrame implements MouseListener {
 
     private JTable searchList;
     private JPanel tablePanel;                 //panel for search bar
     private boolean managerMode;
     private Statement stmt;
     private ResultSet results;
     private static final String[] COLS = { "ID", "Author", "Title", "Duration", "Genre", "Rating", "Total Reviews", "Price", "Number Sold", "Release Year" };
     private int rows = 23;
     private Object[][] data = new Object[ rows ][ 10 ];
     private int row = 0;
     private boolean searchMode;
 
     public SearchGUI() throws SQLException, IOException {
         this( false, "" );
     }
 
     public SearchGUI( boolean managerMode, String query ) throws SQLException, IOException {
 
         super( "MediaStore" );
 
         this.managerMode = managerMode;
         searchMode = true;
         addWindowListener( new SearchGUIExitHandler( managerMode ) );
 
         setLayout( new BorderLayout() );                //set up layout
 
         searchList = new JTable( data, COLS );
 
         results = MediaStoreGUI.db.searchItem( query );
 
         while( results.next() ) {
             if( row > rows - 1 ) {
                 break;
             }
             searchList.setValueAt( results.getInt( "media_id" ), row, 0 );
             searchList.setValueAt( results.getString( "author" ), row, 1 );
             searchList.setValueAt( results.getString( "title" ), row, 2 );
             searchList.setValueAt( results.getInt( "duration" ), row, 3 );
             searchList.setValueAt( results.getString( "genre" ), row, 4 );
             searchList.setValueAt( results.getDouble( "rating" ), row, 5 );
             searchList.setValueAt( results.getInt( "total_reviews" ), row, 6 );
             searchList.setValueAt( results.getDouble( "price" ), row, 7 );
             searchList.setValueAt( results.getInt( "numsold" ), row, 8 );
             searchList.setValueAt( results.getInt( "releaseyear" ), row, 9 );
 
             row++;
         }
 
 
         searchList.setPreferredScrollableViewportSize( new Dimension( 350, 100 ) );
         searchList.setFillsViewportHeight( true );
         searchList.setAutoCreateRowSorter( true );
         searchList.getTableHeader().setReorderingAllowed( false );
         searchList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         searchList.addMouseListener( this );
         searchList.setShowGrid( false );
         searchList.setIntercellSpacing( new Dimension( 0, 0 ) );
 
 
         tablePanel = new JPanel();
 
         tablePanel.setLayout( new BorderLayout() );
 
         tablePanel.add( searchList.getTableHeader(), BorderLayout.NORTH );
         tablePanel.add( searchList, BorderLayout.CENTER );
         add( tablePanel, BorderLayout.CENTER );
         validate();
 
         tablePanel.addMouseListener( this );
         add( tablePanel );
 
     }
 
     @Override
     public void mouseClicked( MouseEvent e ) {
     }
 
     @Override
     public void mousePressed( MouseEvent e ) {
         if( e.getClickCount() > 1 ) {
             Point p = e.getPoint();
             JTable target = (JTable) e.getSource();
             int row = target.rowAtPoint( new Point( e.getX(), e.getY() ) );
             if( row == -1 ) {
                 return;
             }
             int col = 0;
             Object valueAt = target.getValueAt( row, col );
             Media media = MediaStoreGUI.db.getMediaFromID( Integer.parseInt( target.getValueAt( row, col ).toString() ) );
             searchMode = true;
             try {
                 MediaStoreGUI.mediaViewerScreen( media, MediaStoreGUI.loggedInCustomer, managerMode, searchMode );
             } catch( IOException ex ) {
                 Logger.getLogger( MediaTabbedPaneGUI.class.getName() ).log( Level.SEVERE, null, ex );
             } catch( SQLException ex ) {
                 Logger.getLogger( MediaTabbedPaneGUI.class.getName() ).log( Level.SEVERE, null, ex );
             }
         }
     }
 
     @Override
     public void mouseReleased( MouseEvent me ) {
     }
 
     @Override
     public void mouseEntered( MouseEvent me ) {
     }
 
     @Override
     public void mouseExited( MouseEvent me ) {
     }
 
     private class SearchGUIExitHandler extends WindowAdapter {
 
         private boolean managerMode;
 
         public SearchGUIExitHandler( boolean managerMode ) {
             this.managerMode = managerMode;
         }
 
         @Override
         public void windowClosing( WindowEvent e ) {
            MediaStoreGUI.customerScreen( rootPaneCheckingEnabled );
 
         }
     }
 }
