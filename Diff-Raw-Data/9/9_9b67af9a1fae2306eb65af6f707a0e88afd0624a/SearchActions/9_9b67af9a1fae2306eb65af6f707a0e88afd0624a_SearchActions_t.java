 package webspider.actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import webspider.Settings;
 import webspider.core.indexer.Indexer;
 
 
 /**
  * Actions/methods defined to be used with the Indexer search
  * @author Shaabi Mohammed
  */
 public class SearchActions implements ActionListener{
     private JButton controlButton;
 
     private JLabel indexlistLabel;
     private JTextField keywordField;
     private JButton indexlistButton;
 
     private JLabel stats_status;
     private JLabel stats_keyword;
     private JLabel stats_totalkeywords;
 
     private JFileChooser chooser = new JFileChooser();
     private File inputFile;
     
     private SpiderActions actions;
 
     SearchActions(SpiderActions actions) {
         this.actions = actions;
         indexer = new Indexer(actions);
     }
 
      /**
      * Instance of Indexer
      */
     protected Indexer indexer;
 
     /**
      * Actionlistner handler for actions invoked, including 
      * find, browse index
      * @param e
      */
     public void actionPerformed(ActionEvent e) {
         if(e.getActionCommand().equals("find")){
             if(inputFile == null){
                 JOptionPane.showMessageDialog(null, "Please select an input File.");
             }else if(keywordField.getText().isEmpty()){
                 JOptionPane.showMessageDialog(null, "Please enter a search keyword");
             }else{
                 startSearch(keywordField.getText());
                 updateStats();
             }
             //startSearch(i, null);
         }else if(e.getActionCommand().equals("browseindexlist")){
             int returnVal = chooser.showOpenDialog(null);
             if(returnVal == JFileChooser.APPROVE_OPTION) {
                inputFile = chooser.getSelectedFile();
                indexlistLabel.setText("URL List : " + chooser.getSelectedFile().getAbsolutePath());
                actions.log("Selected Keyword Index File: " + chooser.getSelectedFile().getAbsolutePath());
                indexer.loadIndexTable(inputFile.getAbsolutePath());
                updateStats();
             }
         }
     }
 
     /**
      * Starts a search for a keyword in a given index file
      * @param dbfilePath the index database to load
      * @param keyword the keyword to serch for
      */
     public void startSearch(String dbfilePath, String keyword){
         actions.log(dbfilePath);
         indexer.loadIndexTable(dbfilePath);
         indexer.search(keyword);
     }
 
     /**
      * starts a search when the index has been already loaded
      * @param keyword the keyword to search for
      */
     public void startSearch(String keyword){
         indexer.search(keyword);
     }
 
     /**
      * resets all interface buttons to default states
      */
     public void resetButtons(){
         indexlistButton.setEnabled(true);
         if(Settings.BACK_BUTTON)actions.getBacker().setEnabled(true);
     }
 
     // Statistics elements
     /**
      * Updates the statistics for the search
      */
     public void updateStats(){
         if(Settings.GUI && !indexer.isNull()){
             stats_keyword.setText("Searching for : " + keywordField.getText());
             stats_totalkeywords.setText("Total Keywords : " + indexer.getKeywordCount());
         }
     }
 
     /**
      *
      * @param stats_status
      */
     public void initStatus(JLabel stats_status){
         this.stats_status = stats_status;
     }
 
     /**
      *
      * @param stats_keyword
      */
     public void initKeyword(JLabel stats_keyword){
         this.stats_keyword = stats_keyword;
     }
 
     /**
      *
      * @param stats_totalkeywords
      */
     public void initTotalKeywords(JLabel stats_totalkeywords){
         this.stats_totalkeywords = stats_totalkeywords;
     }
 
     //ELEMENTS
     /**
      * 
      * @param controlButton
      */
     public void initContoller(JButton controlButton){
         this.controlButton = controlButton;
     }
 
     /**
      *
      * @param indexlistLabel
      */
     public void initindexlistLabel(JLabel indexlistLabel){
         this.indexlistLabel = indexlistLabel;
     }
 
     /**
      *
      * @param kewordField
      */
     public void initKeywordField(JTextField kewordField){
         this.keywordField = kewordField;
     }
 
     /**
      *
      * @param indexlistButton
      */
     public void initindexlistButton(JButton indexlistButton){
         this.indexlistButton = indexlistButton;
         actions.disableTF(chooser);
         chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(new File(Settings.DEFAULT_PATH));
         chooser.setFileFilter(new BDMFilter(Settings.FILE_INDEX_EXTENSION));
     }
 }
