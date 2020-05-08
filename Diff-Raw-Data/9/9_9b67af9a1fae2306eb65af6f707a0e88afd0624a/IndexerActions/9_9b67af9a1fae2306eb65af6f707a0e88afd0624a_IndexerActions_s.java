 package webspider.actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import webspider.Settings;
 import webspider.core.indexer.Indexer;
 
 /**
  * Actions/methods defined to be used with the Indexer
  * @author Shaabi Mohammed
  */
 public class IndexerActions implements ActionListener{
     private JButton controlButton;
     private JButton stopButton;
     private JLabel urllistLabel;
     private JButton urllistButton;
 
     private JLabel stats_status;
     private JLabel stats_totalurls;
     private JLabel stats_currenturl;
     private JLabel stats_keywordsindexed;
 
     private JFileChooser chooser = new JFileChooser();
     private File inputFile;
 
     private SpiderActions actions;
 
     /**
      * Instance of Indexer
      */
     protected Indexer indexer;
 
     IndexerActions(SpiderActions actions) {
         this.actions = actions;
         indexer = new Indexer(actions);
     }
 
     /**
      * Actionlistner handler for actions invoked
      * @param e
      */
     public void actionPerformed(ActionEvent e) {
         if(e.getActionCommand().equals("start")){
                 if(inputFile == null){
                     JOptionPane.showMessageDialog(null, "Please select an input File.");
                 }else if(inputFile.getName().indexOf("_") == -1){
                     JOptionPane.showMessageDialog(null, "Invalid File name (Should be 'hostname_type.bdmc').");
                 }else{
                     controlButton.setActionCommand("pause");
                     controlButton.setText("Pause");
                     stopButton.setEnabled(true);
                     if(Settings.BACK_BUTTON)actions.getBacker().setEnabled(false);
                     urllistButton.setEnabled(false);
                     startIndexer(inputFile.getAbsolutePath());
                 }
         }else if(e.getActionCommand().equals("pause")){
             controlButton.setActionCommand("resume");
             controlButton.setText("Resume");
             indexer.stopIndexer();
         }else if(e.getActionCommand().equals("resume")){
             controlButton.setActionCommand("pause");
             controlButton.setText("Pause");
             indexer.resume();
         }else if(e.getActionCommand().equals("stop")){
             controlButton.setActionCommand("start");
             controlButton.setText("Start");
             stopButton.setEnabled(false);
             if(Settings.BACK_BUTTON)actions.getBacker().setEnabled(true);
             urllistButton.setEnabled(true);
             indexer.killIndexer();
         }else if(e.getActionCommand().equals("browseurllist")){
             int returnVal = chooser.showOpenDialog(null);
             if(returnVal == JFileChooser.APPROVE_OPTION) {
                urllistLabel.setText("URL List : " + chooser.getSelectedFile().getAbsolutePath());
                actions.log("Selected URL List File: " + chooser.getSelectedFile().getAbsolutePath());
                inputFile = chooser.getSelectedFile();
             }
         }
     }
 
     /**
      * takes path of url list and starts analizing keywords
      * @param inputpath
      */
     public void startIndexer(String inputpath){
         File inputFile = new File(inputpath);
         String outputFile = Settings.DEFAULT_PATH + "/" + (inputFile.getName().split("_"))[0] + "_index" + Settings.FILE_INDEX_EXTENSION;
         indexer.IndexCrawledPages(inputFile.getAbsolutePath(), outputFile);
     }
 
     // Statistics elements
     /**
      * Update GUI statistic information
      */
     public void updateStats(){
         if(indexer.isRunning() && Settings.GUI){
             stats_status.setText("Status : " + indexer.getStatus());
             stats_totalurls.setText("Total URLs : " + indexer.totalURLCount());
             stats_currenturl.setText("Current URL : " + indexer.currentURL());
             stats_keywordsindexed.setText("Keywords Indexed : " + indexer.KeywordsIndexedCount());
         }
     }
 
     /**
      * Resets GUI buttons to initial state
      */
     public void resetButtons(){
         if(Settings.GUI){
             controlButton.setActionCommand("start");
             controlButton.setText("Start");
             stopButton.setEnabled(false);
             urllistButton.setEnabled(true);
             if(Settings.BACK_BUTTON)actions.getBacker().setEnabled(true);
         }
     }
 
     /**
      * status label setter
      * @param stats_status
      */
     public void initStatus(JLabel stats_status){
         this.stats_status = stats_status;
     }
 
     /**
      * totalurls label setter
      * @param stats_totalurls
      */
     public void initTotalurls(JLabel stats_totalurls){
         this.stats_totalurls = stats_totalurls;
     }
 
     /**
      * current url setter
      * @param stats_currenturl
      */
     public void initCurrenturl(JLabel stats_currenturl){
         this.stats_currenturl = stats_currenturl;
     }
 
     /**
      * keywords indexed label
      * @param stats_keywordsindexed
      */
     public void initKeywordsindexed(JLabel stats_keywordsindexed){
         this.stats_keywordsindexed = stats_keywordsindexed;
     }
 
     //ELEMENTS
     /**
      * control button setter
      * @param controlButton
      */
     public void initContoller(JButton controlButton){
         this.controlButton = controlButton;
     }
 
     /**
      * stop button setter
      * @param stopButton
      */
     public void initStopper(JButton stopButton){
         this.stopButton = stopButton;
     }
 
     /**
      * urllist label setter
      * @param urllistLabel
      */
     public void initurllistLabel(JLabel urllistLabel){
         this.urllistLabel = urllistLabel;
     }
 
     /**
      * url list browser button initalizer and setter
      * @param urllistButton
      */
     public void initurllistButton(JButton urllistButton){
         this.urllistButton = urllistButton;
         actions.disableTF(chooser);
         chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(new File("./output/spider"));
         chooser.setFileFilter(new BDMFilter(Settings.CRAWLER_EXTENSION));
     }
 }
