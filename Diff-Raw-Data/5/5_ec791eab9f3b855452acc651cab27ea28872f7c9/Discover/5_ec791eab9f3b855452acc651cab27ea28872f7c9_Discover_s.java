 package my.triviagame.gui;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Queues;
 import java.awt.Cursor;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.table.DefaultTableModel;
 import my.triviagame.bll.Database;
 import my.triviagame.dal.DALException;
 import my.triviagame.dal.IAlbumDescriptor;
 import my.triviagame.dal.IDAL;
 import my.triviagame.dal.ITrackDescriptor;
 
 public class Discover extends javax.swing.JDialog {
     
     public static Pattern SEARCH_TOKENIZER = Pattern.compile("([^\\s\"]+)|(\"[^\"]+\")");
     
     public static final Logger logger = Logger.getLogger(Discover.class.getName());
     
     /**
      * Manages the Instant Search functionality.
      */
     private class InstantSearch {
         
         private final String[] STOP_REQUEST = new String[]{};
         
         private final Thread searchThread;
         private final BlockingQueue<SearchQuery> searchRequests;
         List<SearchQuery> outstandingRequests = Lists.newArrayList();
         
         public InstantSearch() {
             searchRequests = Queues.newLinkedBlockingQueue();
             searchThread = new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     serveSearchRequests();
                 }
             });
         }
         
         /**
          * Start handling search requests in the background.
          */
         public void start() {
             searchThread.start();
         }
         
         /**
          * Stop handling search requests in the background.
          * TODO: this is never called, add as a listener for dialog close event
          */
         public void stop() {
             // Insert end-of-stream value
             searchRequests.add(SearchQuery.INVALID_QUERY);
         }
         
         /**
          * Submit a set of keywords for a search.
          */
         public void submit(boolean isEntered, String... keywords) {
             searchRequests.add(new SearchQuery(keywords, isEntered));
         }
         
         /**
          * Thread procedure for search worker thread.
          */
         private void serveSearchRequests() {
             // Serve forever
             while (true) {
                 // Wait for the next keywords to search
                 SearchQuery query = getNextQuery();
                 if (query == null) {
                     // Received request to shut down
                     break;
                 }
                 try {
                     IDAL dal = Database.getDataAccessLayer();
                     // Perform search
                     List<ITrackDescriptor> searchResults =
                             my.triviagame.bll.Discover.getTrackDescriptors(dal, query.keywords);
                     // Show empty results only if the user explicitly requested
                     if (!searchResults.isEmpty() || query.isEntered) {
                         results = searchResults;
                         SetResultSafely();
                     }
                 } catch (DALException ex) {
                     logger.log(Level.SEVERE, "Failed to get search results", ex);
                 }
             }
         }
         
         /**
          * Get the next set of keywords to search.
          * 
          * @return keywords to search with or null to shut down.
          */
         private SearchQuery getNextQuery() {
             try {
                 SearchQuery query = searchRequests.take();
                 if (!query.isValid()) {
                     // Received request to shut down
                     return null;
                 }
                 outstandingRequests.clear();
                 searchRequests.drainTo(outstandingRequests);
                 if (!outstandingRequests.isEmpty()) {
                     for (SearchQuery q : outstandingRequests) {
                         if (!q.isValid()) {
                             // Received request to shut down
                             return null;
                         }
                     }
                     // Serve only the most recent (last in queue)
                     query = outstandingRequests.get(outstandingRequests.size() - 1);
                 }
                 return query;
             } catch (InterruptedException ex) {
                 // Signal a shutdown request
                 return null;
             }
         }
     }
     
     private static class SearchQuery {
         
         public static final SearchQuery INVALID_QUERY = new SearchQuery(null, true);
         
         /** Keywords to search for. */
         public final String[] keywords;
         /** Whether the user entered the query explicitly (hit enter or pressed button). */
         public final boolean isEntered;
         
         public SearchQuery(String[] keywords, boolean isEntered) {
             this.keywords = keywords;
             this.isEntered = isEntered;
         }
         
         /**
          * Returns whether this query contains actual keywords to search.
          */
         public boolean isValid() {
             return keywords != null;
         }
     }
     
     private InstantSearch searcher = new InstantSearch();
     private List<ITrackDescriptor> results = Collections.emptyList();
 
     /**
      * Creates new form Discover
      */
     public Discover(java.awt.Frame parent, boolean modal) {
         super(parent, modal);
         initComponents();
         setLocationRelativeTo(null);
         this.getRootPane().setDefaultButton(jButton1);
         this.jTextFieldSearchBox.selectAll();
         this.jLabelNoResults.setVisible(false);
         jTextFieldSearchResults.setAutoCreateRowSorter(true);
         searcher.start();
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         jTextFieldSearchBox = new javax.swing.JTextField();
         jButton1 = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextFieldSearchResults = new javax.swing.JTable();
         jLabelNoResults = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setMaximumSize(new java.awt.Dimension(2147483647, 563));
         setResizable(false);
 
         jPanel1.setBackground(new java.awt.Color(255, 255, 255));
         jPanel1.setMaximumSize(new java.awt.Dimension(32767, 563));
         jPanel1.setPreferredSize(new java.awt.Dimension(1024, 563));
 
         jTextFieldSearchBox.setText("Type to search! Enter several words to match all, use * for wildcards and surround with double quotes (\") for continuous text.");
         jTextFieldSearchBox.setToolTipText("");
         jTextFieldSearchBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 onSearchEnter(evt);
             }
         });
         jTextFieldSearchBox.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 onSearchType(evt);
             }
         });
 
         jButton1.setText("Search!");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 onSearchPress(evt);
             }
         });
 
         jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/triviagame/gui/resources/Discover.jpg"))); // NOI18N
         jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 
         jTextFieldSearchResults.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Song", "Artist", "Album", "Album Artist", "Track #", "Genre", "Length"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTextFieldSearchResults.setSurrendersFocusOnKeystroke(true);
         jTextFieldSearchResults.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 myHandler(evt);
             }
         });
         jScrollPane1.setViewportView(jTextFieldSearchResults);
 
         jLabelNoResults.setForeground(new java.awt.Color(255, 0, 0));
         jLabelNoResults.setText("Sorry, we couldn't find what you were looking for. Please refine your search and try again.");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabelNoResults, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                         .addGap(0, 355, Short.MAX_VALUE)
                         .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(351, 351, 351))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                         .addComponent(jTextFieldSearchBox, javax.swing.GroupLayout.DEFAULT_SIZE, 903, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton1)
                         .addGap(25, 25, 25))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jScrollPane1)
                         .addContainerGap())))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextFieldSearchBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabelNoResults)
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(272, 272, 272))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 639, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void onSearchPress(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSearchPress
         refreshSearch();
     }//GEN-LAST:event_onSearchPress
 
     public void refreshSearch()
     {
         jPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         search(jTextFieldSearchBox.getText(), true);
     }
 
     private void myHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myHandler
         if (evt.getClickCount() != 2)
         {
             return;
         }
         
         if (evt.getButton() != java.awt.event.MouseEvent.BUTTON1)
         {
             return;
         }
 
         JTable table;
         try
         {
             table = (JTable)evt.getSource();
         }
         catch (Exception ex)
         {
             return;
         }
         
         int selectedRowIndex = table.getSelectedRow();
        ITrackDescriptor trackDesc = getTrackDescByRow(selectedRowIndex, table);
         IDAL dataAccessLayer = Database.getDataAccessLayer();
         int albumId = trackDesc.getAlbumId();
         List<Integer> albumIds = new ArrayList<Integer>();
         albumIds.add(albumId);
         
         List<IAlbumDescriptor> albumDescriptors = null;
         try {
             albumDescriptors = dataAccessLayer.getAlbumDescriptors(albumIds);
         } catch (DALException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         IAlbumDescriptor albumDesc = albumDescriptors.get(0);
         List<ITrackDescriptor> trackDescriptors = null;
         try {
             trackDescriptors = dataAccessLayer.getAlbumTrackDescriptors(albumDesc);
         } catch (DALException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         
         AddUpdate addUpdateForm = new AddUpdate(new JFrame(), true, this, albumDesc, trackDescriptors);
         addUpdateForm.show();
     }//GEN-LAST:event_myHandler
 
     private void onSearchType(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_onSearchType
         if (evt.getID() == KeyEvent.KEY_TYPED) {
             search(jTextFieldSearchBox.getText() + evt.getKeyChar(), false);
         } else {
             search(jTextFieldSearchBox.getText(), false);
         }
     }//GEN-LAST:event_onSearchType
 
     private void onSearchEnter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSearchEnter
         jPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         search(jTextFieldSearchBox.getText(), true);
     }//GEN-LAST:event_onSearchEnter
 
     private ITrackDescriptor getTrackDescByRow(int selectedRowIndex, JTable table)
     {
         String song = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,0).toString();
         String artist = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,1).toString();
         String album = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,2).toString();
         String albumArtist = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,3).toString();
         String trackNum = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,4).toString();
         String genre = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,5).toString();
         String length = ((DefaultTableModel)table.getModel()).getValueAt(selectedRowIndex,6).toString();
         
         for (ITrackDescriptor currTrack : results)
         {
             int s = currTrack.getLengthInSeconds();
             if ((currTrack.getTitle().equals(song)) &&
                 (currTrack.getAlbumArtist().equals(albumArtist)) &&
                 (currTrack.getArtistName().equals(artist)) &&
                 (currTrack.getAlbumName().equals(album)) &&
                 (String.valueOf(currTrack.getTrackNum()).equals(trackNum)) &&
                 (currTrack.getGenre().equals(genre)) &&
                 (String.format("%02d:%02d:%02d", s/3600, (s%3600)/60, (s%60)).equals(length)))
             {
                 return currTrack;
             }
         }
         
         return null;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(Discover.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(Discover.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(Discover.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(Discover.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabelNoResults;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTextField jTextFieldSearchBox;
     private javax.swing.JTable jTextFieldSearchResults;
     // End of variables declaration//GEN-END:variables
     
     private DefaultTableModel createEmptyTable() {
         return new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Song", "Artist", "Album", "Album Artist", "Track #", "Genre", "Length"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false
             };
 
             @Override
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             @Override
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         };
     }
     
     private void SetResult()
     {
         DefaultTableModel table = createEmptyTable();
         
         for (ITrackDescriptor track : results) {
             int sec = track.getLengthInSeconds();
             table.addRow(new String[] {
                 track.getTitle(),
                 track.getArtistName(),
                 track.getAlbumName(),
                 track.getAlbumArtist(),
                 String.valueOf(track.getTrackNum()),
                 track.getGenre(),
                 String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, (sec % 60))});
         }
         
         jTextFieldSearchResults.setModel(table);
         // Show "no results" message iff no results were found
         jLabelNoResults.setVisible(results.isEmpty());
         
         jPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
 
     private void SetResultSafely()
     {
         Runnable r = new Runnable()
         {
             @Override
             public void run()
             {
                 try
                 {
                     SetResult();
                 } catch (Exception ex)
                 {
                     logger.log(Level.SEVERE, "Failed to set results safely", ex);
                 } 
             }
         };
 
         SwingUtilities.invokeLater(r);
     }
     
     private void search(String query, boolean isEntered) {
         if (query.isEmpty()) {
             JOptionPane.showMessageDialog(this,"Please enter text to search.");
             jTextFieldSearchBox.selectAll();
             return;
         }
         
         List<String> keywordsList = Lists.newArrayList();
         Matcher m = SEARCH_TOKENIZER.matcher(query);
         while (m.find()) {
             if (m.group(1) != null) {
                 keywordsList.add(m.group(1));
             } else {
                 keywordsList.add(m.group(2));
             }
         }
         String[] keywords = keywordsList.toArray(new String[0]);
 
         searcher.submit(isEntered, keywords);
     }
 }
