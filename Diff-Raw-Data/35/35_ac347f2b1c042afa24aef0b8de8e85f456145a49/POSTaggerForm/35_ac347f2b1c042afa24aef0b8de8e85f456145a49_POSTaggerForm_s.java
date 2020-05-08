 package ir.ac.iust.nlp.postagger;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import org.apache.commons.io.FileUtils;
 
 /**
  *
  * @author Mojtaba Khallash
  */
 public class POSTaggerForm extends javax.swing.JFrame {
 
     DefaultListModel wordModel;
     DefaultListModel predModel;
     DefaultListModel goldModel;
     
     int lock = 0;
         
     public POSTaggerForm() {
         initComponents();
         
         SpinnerNumberModel num_model = new SpinnerNumberModel(100, 1, 10000000, 1); 
         spMaxIters.setModel(num_model);
         
         wordModel = (DefaultListModel)lstWordsList.getModel();
         predModel = (DefaultListModel)lstPredictedTags.getModel();
         goldModel = (DefaultListModel)lstGoldTags.getModel();
         
         setDrop();
         
         String curDir = System.getProperty("user.dir");
         txtInputCoNLLFile.setText(curDir + File.separator + "data" + File.separator + "conll" + File.separator + "test.conll");
         txtOutputTagPath.setText(curDir + File.separator);
         txtOutputDepPath.setText(curDir + File.separator);
         txtOutputPath.setText(curDir + File.separator);
         txtInputTagFile.setText(curDir + File.separator + "data" + File.separator + "tag" + File.separator + "gold_test.lbl");
         txtInputCoNLLFileT2D.setText(curDir + File.separator + "data" + File.separator + "conll" + File.separator + "test.conll");
         txtTrainFile.setText(curDir + File.separator + "data" + File.separator + "tag" + File.separator + "train.lbl");
         txtTrainModelPath.setText(curDir + File.separator + "model" + File.separator);
         txtModelPath.setText(curDir + File.separator + "data" + File.separator + "model" + File.separator);
         txtInputFile.setText(curDir + File.separator + "data" + File.separator + "tag" + File.separator + "test.lbl");
         txtGoldFile.setText(curDir + File.separator + "data" + File.separator + "tag" + File.separator + "gold_test.lbl");
     }
     
     private void setDrop() {
         FileDrop fd;
                 
         fd = new FileDrop(null, txtGoldFile, new FileDrop.Listener() {
             @Override
             public void filesDropped(java.io.File[] files) {
                 if (files.length > 0) {
                     try {
                         boolean dropped = false;
                         for (int i = 0; i < files.length; i++) {
                             if (files[i].isFile()) {
                                 txtGoldFile.setText(files[i].getCanonicalPath());
                                 dropped = true;
                                 chkGoldFile.setSelected(true);
                                 break;
                             }
                         }
                         if (dropped == false) {
                             JOptionPane.showMessageDialog(null, "File needed.");
                         }
                     } // end try
                     catch (java.io.IOException e) {
                     }
                 }   // end for: through each dropped file
             }   // end filesDropped
         }); // end FileDrop.Listener
         
         setupDrop(txtInputFile, true);
         setupDrop(txtTrainFile, true);
         setupDrop(txtTrainFile, true);
         setupDrop(txtInputCoNLLFile, true);
         setupDrop(txtInputTagFile, true);
         setupDrop(txtInputCoNLLFileT2D, true);
         setupDrop(txtModelPath, false);
         setupDrop(txtTrainModelPath, false);
         setupDrop(txtOutputTagPath, false);
         setupDrop(txtOutputDepPath, false);
         setupDrop(txtOutputPath, false);
     }
     
     private void setupDrop(final JTextField text, final boolean isInput) {
         FileDrop fd = new FileDrop(null, text, new FileDrop.Listener() {
             @Override
             public void filesDropped(java.io.File[] files) {
                 if (files.length > 0) {
                     try {
                         if (isInput == true) {
                             boolean dropped = false;
                             for (int i = 0; i < files.length; i++) {
                                 if (files[i].isFile()) {
                                     text.setText(files[i].getCanonicalPath());
                                     dropped = true;
                                     break;
                                 }
                             }
                             if (dropped == false) {
                                 JOptionPane.showMessageDialog(null, "File needed.");
                             }                        
                         }
                         else {
                             if (files[0].isFile()) {
                                 files[0] = files[0].getParentFile();
                             }
 
                             text.setText(files[0].getCanonicalPath() + File.separator);
                         }
                     } // end try
                     catch (java.io.IOException e) {
                     }
                 }   // end for: through each dropped file
             }   // end filesDropped
         }); // end FileDrop.Listener
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         rbgPOSTypeD2T = new javax.swing.ButtonGroup();
         rbgPOSTypeT2D = new javax.swing.ButtonGroup();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         pnlConverter = new javax.swing.JPanel();
         tabConverter = new javax.swing.JTabbedPane();
         pnlDep2Tag = new javax.swing.JPanel();
         lblInputCoNLLFile = new javax.swing.JLabel();
         txtInputCoNLLFile = new javax.swing.JTextField();
         btnBrowseInputCoNLLFile = new javax.swing.JButton();
         lblOutputTagPath = new javax.swing.JLabel();
         txtOutputTagPath = new javax.swing.JTextField();
         btnBrowseOutputTagPath = new javax.swing.JButton();
         btnConertDep2Tag = new javax.swing.JButton();
         chkAddGoldTag = new javax.swing.JCheckBox();
         pnlPOSTypeD2T = new javax.swing.JPanel();
         rbFPOSD2T = new javax.swing.JRadioButton();
         rbCPOSD2T = new javax.swing.JRadioButton();
         pnlTag2Dep = new javax.swing.JPanel();
         lblInputTagFile = new javax.swing.JLabel();
         txtInputTagFile = new javax.swing.JTextField();
         btnBrowseInputTagFile = new javax.swing.JButton();
         lblOutputDepPath = new javax.swing.JLabel();
         txtOutputDepPath = new javax.swing.JTextField();
         btnBrowseOutputDepPath = new javax.swing.JButton();
         btnConvertTag2Dep = new javax.swing.JButton();
         pnlPOSTypeT2D = new javax.swing.JPanel();
         rbFPOST2D = new javax.swing.JRadioButton();
         rbCPOST2D = new javax.swing.JRadioButton();
         lblInputCoNLLFileT2D = new javax.swing.JLabel();
         txtInputCoNLLFileT2D = new javax.swing.JTextField();
         btnBrowseInputCoNLLFileT2D = new javax.swing.JButton();
         pnlTrain = new javax.swing.JPanel();
         lblTrainFile = new javax.swing.JLabel();
         txtTrainFile = new javax.swing.JTextField();
         btnBrowseTrainFile = new javax.swing.JButton();
         lblTrainModelPath = new javax.swing.JLabel();
         txtTrainModelPath = new javax.swing.JTextField();
         btnBrowseTrainModelPath = new javax.swing.JButton();
         lblMaxIters = new javax.swing.JLabel();
         spMaxIters = new javax.swing.JSpinner();
         btnStartTraining = new javax.swing.JButton();
         jScrollbar2 = new javax.swing.JScrollPane();
         txtTrainLog = new javax.swing.JTextArea();
         pnlTag = new javax.swing.JPanel();
         lblInputFile = new javax.swing.JLabel();
         txtInputFile = new javax.swing.JTextField();
         btnBrowseInputFile = new javax.swing.JButton();
         lblOutputPath = new javax.swing.JLabel();
         txtOutputPath = new javax.swing.JTextField();
         btnBrowseOutputPath = new javax.swing.JButton();
         chkGoldFile = new javax.swing.JCheckBox();
         txtGoldFile = new javax.swing.JTextField();
         btnBrowseGoldFile = new javax.swing.JButton();
         tabTag = new javax.swing.JTabbedPane();
         pnlTagLog = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         txtTagLog = new javax.swing.JTextArea();
         pnlTagResults = new javax.swing.JPanel();
         lblTagEvaluation = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         lstWordsList = new javax.swing.JList(new DefaultListModel());
         lblWordsList = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         lstPredictedTags = new javax.swing.JList(new DefaultListModel());
         jScrollPane4 = new javax.swing.JScrollPane();
         lstGoldTags = new javax.swing.JList(new DefaultListModel());
         lblPredictedTags = new javax.swing.JLabel();
         lblGoldTags = new javax.swing.JLabel();
         btnStartTagging = new javax.swing.JButton();
         lblModelPath = new javax.swing.JLabel();
         txtModelPath = new javax.swing.JTextField();
         btnBrowseModelPath = new javax.swing.JButton();
         pnlAbout = new javax.swing.JPanel();
         lblMXPostTitle = new javax.swing.JLabel();
         lblMXPostSiteTitle = new javax.swing.JLabel();
         lblMXPostSiteValue = new javax.swing.JTextField();
         lblMXPostAuthorsTitle = new javax.swing.JLabel();
         lblMXPostAuthorsValue = new javax.swing.JTextField();
         lblMXPostCitationTitle = new javax.swing.JLabel();
         jScrollPane9 = new javax.swing.JScrollPane();
         txtMXPostCitationValue = new javax.swing.JTextArea();
         lblMXpostLicenseTitle = new javax.swing.JLabel();
         jScrollPane5 = new javax.swing.JScrollPane();
         txtMXPostLicenseValue = new javax.swing.JTextArea();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("POS Tagger");
 
         lblInputCoNLLFile.setText("Input CoNLL File:");
 
         txtInputCoNLLFile.setEditable(false);
 
         btnBrowseInputCoNLLFile.setText("...");
         btnBrowseInputCoNLLFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseInputCoNLLFileActionPerformed(evt);
             }
         });
 
         lblOutputTagPath.setText("Output Path:");
 
         txtOutputTagPath.setEditable(false);
 
         btnBrowseOutputTagPath.setText("...");
         btnBrowseOutputTagPath.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseOutputTagPathActionPerformed(evt);
             }
         });
 
         btnConertDep2Tag.setText("Start Convert");
         btnConertDep2Tag.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnConertDep2TagActionPerformed(evt);
             }
         });
 
         chkAddGoldTag.setSelected(true);
         chkAddGoldTag.setText("Add Gold Tag");
 
         pnlPOSTypeD2T.setBorder(javax.swing.BorderFactory.createTitledBorder("Type"));
 
         rbgPOSTypeD2T.add(rbFPOSD2T);
         rbFPOSD2T.setSelected(true);
         rbFPOSD2T.setText("Fine-grained POS");
 
         rbgPOSTypeD2T.add(rbCPOSD2T);
         rbCPOSD2T.setText("Coarse-grained POS");
 
         javax.swing.GroupLayout pnlPOSTypeD2TLayout = new javax.swing.GroupLayout(pnlPOSTypeD2T);
         pnlPOSTypeD2T.setLayout(pnlPOSTypeD2TLayout);
         pnlPOSTypeD2TLayout.setHorizontalGroup(
             pnlPOSTypeD2TLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPOSTypeD2TLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlPOSTypeD2TLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rbFPOSD2T)
                     .addComponent(rbCPOSD2T))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnlPOSTypeD2TLayout.setVerticalGroup(
             pnlPOSTypeD2TLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPOSTypeD2TLayout.createSequentialGroup()
                 .addComponent(rbFPOSD2T)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(rbCPOSD2T))
         );
 
         javax.swing.GroupLayout pnlDep2TagLayout = new javax.swing.GroupLayout(pnlDep2Tag);
         pnlDep2Tag.setLayout(pnlDep2TagLayout);
         pnlDep2TagLayout.setHorizontalGroup(
             pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlDep2TagLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlDep2TagLayout.createSequentialGroup()
                         .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblInputCoNLLFile)
                             .addComponent(lblOutputTagPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(txtInputCoNLLFile)
                             .addComponent(txtOutputTagPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(btnBrowseOutputTagPath, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                             .addComponent(btnBrowseInputCoNLLFile, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(pnlDep2TagLayout.createSequentialGroup()
                         .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(chkAddGoldTag)
                             .addComponent(pnlPOSTypeD2T, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 398, Short.MAX_VALUE)
                         .addComponent(btnConertDep2Tag)))
                 .addContainerGap())
         );
         pnlDep2TagLayout.setVerticalGroup(
             pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlDep2TagLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblInputCoNLLFile)
                     .addComponent(txtInputCoNLLFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseInputCoNLLFile))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblOutputTagPath)
                     .addComponent(txtOutputTagPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseOutputTagPath))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlDep2TagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlDep2TagLayout.createSequentialGroup()
                         .addComponent(chkAddGoldTag)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pnlPOSTypeD2T, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(btnConertDep2Tag))
                 .addContainerGap(146, Short.MAX_VALUE))
         );
 
         tabConverter.addTab("Dependency to Tagger", pnlDep2Tag);
 
         lblInputTagFile.setText("Input Tag File:");
 
         txtInputTagFile.setEditable(false);
 
         btnBrowseInputTagFile.setText("...");
         btnBrowseInputTagFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseInputTagFileActionPerformed(evt);
             }
         });
 
         lblOutputDepPath.setText("Output Path:");
 
         txtOutputDepPath.setEditable(false);
 
         btnBrowseOutputDepPath.setText("...");
         btnBrowseOutputDepPath.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseOutputDepPathActionPerformed(evt);
             }
         });
 
         btnConvertTag2Dep.setText("Start Convert");
         btnConvertTag2Dep.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnConvertTag2DepActionPerformed(evt);
             }
         });
 
         pnlPOSTypeT2D.setBorder(javax.swing.BorderFactory.createTitledBorder("Type"));
 
         rbgPOSTypeT2D.add(rbFPOST2D);
         rbFPOST2D.setSelected(true);
         rbFPOST2D.setText("Fine-grained POS");
 
         rbgPOSTypeT2D.add(rbCPOST2D);
         rbCPOST2D.setText("Coarse-grained POS");
 
         javax.swing.GroupLayout pnlPOSTypeT2DLayout = new javax.swing.GroupLayout(pnlPOSTypeT2D);
         pnlPOSTypeT2D.setLayout(pnlPOSTypeT2DLayout);
         pnlPOSTypeT2DLayout.setHorizontalGroup(
             pnlPOSTypeT2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPOSTypeT2DLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlPOSTypeT2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rbFPOST2D)
                     .addComponent(rbCPOST2D))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnlPOSTypeT2DLayout.setVerticalGroup(
             pnlPOSTypeT2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPOSTypeT2DLayout.createSequentialGroup()
                 .addComponent(rbFPOST2D)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(rbCPOST2D))
         );
 
         lblInputCoNLLFileT2D.setText("Input CoNLL File:");
 
         txtInputCoNLLFileT2D.setEditable(false);
 
         btnBrowseInputCoNLLFileT2D.setText("...");
         btnBrowseInputCoNLLFileT2D.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseInputCoNLLFileT2DActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlTag2DepLayout = new javax.swing.GroupLayout(pnlTag2Dep);
         pnlTag2Dep.setLayout(pnlTag2DepLayout);
         pnlTag2DepLayout.setHorizontalGroup(
             pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTag2DepLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlTag2DepLayout.createSequentialGroup()
                         .addComponent(pnlPOSTypeT2D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 398, Short.MAX_VALUE)
                         .addComponent(btnConvertTag2Dep))
                     .addGroup(pnlTag2DepLayout.createSequentialGroup()
                         .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblInputTagFile)
                             .addComponent(lblOutputDepPath)
                             .addComponent(lblInputCoNLLFileT2D))
                         .addGap(14, 14, 14)
                         .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(txtInputTagFile)
                             .addComponent(txtInputCoNLLFileT2D)
                             .addComponent(txtOutputDepPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(btnBrowseInputCoNLLFileT2D, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                             .addComponent(btnBrowseOutputDepPath, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                             .addComponent(btnBrowseInputTagFile, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap())
         );
         pnlTag2DepLayout.setVerticalGroup(
             pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTag2DepLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtInputTagFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseInputTagFile)
                     .addComponent(lblInputTagFile))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblInputCoNLLFileT2D)
                     .addComponent(txtInputCoNLLFileT2D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseInputCoNLLFileT2D))
                 .addGap(8, 8, 8)
                 .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(lblOutputDepPath)
                         .addComponent(txtOutputDepPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(btnBrowseOutputDepPath))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlTag2DepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlPOSTypeT2D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnConvertTag2Dep))
                 .addContainerGap(140, Short.MAX_VALUE))
         );
 
         tabConverter.addTab("Tagger to Dependency", pnlTag2Dep);
 
         javax.swing.GroupLayout pnlConverterLayout = new javax.swing.GroupLayout(pnlConverter);
         pnlConverter.setLayout(pnlConverterLayout);
         pnlConverterLayout.setHorizontalGroup(
             pnlConverterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlConverterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(tabConverter)
                 .addContainerGap())
         );
         pnlConverterLayout.setVerticalGroup(
             pnlConverterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlConverterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(tabConverter)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("Converter", pnlConverter);
 
         lblTrainFile.setText("Train File:");
 
         txtTrainFile.setEditable(false);
 
         btnBrowseTrainFile.setText("...");
         btnBrowseTrainFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseTrainFileActionPerformed(evt);
             }
         });
 
         lblTrainModelPath.setText("Model Path:");
 
         txtTrainModelPath.setEditable(false);
 
         btnBrowseTrainModelPath.setText("...");
         btnBrowseTrainModelPath.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseTrainModelPathActionPerformed(evt);
             }
         });
 
         lblMaxIters.setText("Maximum Iterations:");
 
         btnStartTraining.setText("Start Training");
         btnStartTraining.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStartTrainingActionPerformed(evt);
             }
         });
 
         txtTrainLog.setColumns(20);
         txtTrainLog.setRows(5);
         jScrollbar2.setViewportView(txtTrainLog);
 
         javax.swing.GroupLayout pnlTrainLayout = new javax.swing.GroupLayout(pnlTrain);
         pnlTrain.setLayout(pnlTrainLayout);
         pnlTrainLayout.setHorizontalGroup(
             pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTrainLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollbar2)
                     .addGroup(pnlTrainLayout.createSequentialGroup()
                         .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblTrainFile)
                             .addComponent(lblTrainModelPath))
                         .addGap(18, 18, 18)
                         .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(txtTrainFile)
                             .addComponent(txtTrainModelPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(btnBrowseTrainModelPath, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                             .addComponent(btnBrowseTrainFile, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(pnlTrainLayout.createSequentialGroup()
                         .addComponent(lblMaxIters)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(spMaxIters, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 373, Short.MAX_VALUE)
                         .addComponent(btnStartTraining)))
                 .addContainerGap())
         );
         pnlTrainLayout.setVerticalGroup(
             pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTrainLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTrainFile)
                     .addComponent(txtTrainFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseTrainFile))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTrainModelPath)
                     .addComponent(txtTrainModelPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseTrainModelPath))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlTrainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblMaxIters)
                     .addComponent(spMaxIters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnStartTraining))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollbar2, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("Train", pnlTrain);
 
         lblInputFile.setText("Input File:");
 
         txtInputFile.setEditable(false);
 
         btnBrowseInputFile.setText("...");
         btnBrowseInputFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseInputFileActionPerformed(evt);
             }
         });
 
         lblOutputPath.setText("Output Path:");
 
         txtOutputPath.setEditable(false);
 
         btnBrowseOutputPath.setText("...");
         btnBrowseOutputPath.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseOutputPathActionPerformed(evt);
             }
         });
 
         chkGoldFile.setSelected(true);
         chkGoldFile.setText("Gold File:");
 
         txtGoldFile.setEditable(false);
 
         btnBrowseGoldFile.setText("...");
         btnBrowseGoldFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseGoldFileActionPerformed(evt);
             }
         });
 
         txtTagLog.setColumns(20);
         txtTagLog.setEditable(false);
         txtTagLog.setRows(5);
         jScrollPane1.setViewportView(txtTagLog);
 
         javax.swing.GroupLayout pnlTagLogLayout = new javax.swing.GroupLayout(pnlTagLog);
         pnlTagLog.setLayout(pnlTagLogLayout);
         pnlTagLogLayout.setHorizontalGroup(
             pnlTagLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
         );
         pnlTagLogLayout.setVerticalGroup(
             pnlTagLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
         );
 
         tabTag.addTab("Log", pnlTagLog);
 
         lblTagEvaluation.setText("Total: 0 - Correct: 0 - Accuracy: 0%");
 
         lstWordsList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lst_mouseClick(evt);
             }
         });
         lstWordsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstWoldsList_valueChanged(evt);
             }
         });
         jScrollPane2.setViewportView(lstWordsList);
 
         lblWordsList.setText("Words List");
 
         lstPredictedTags.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lst_mouseClick(evt);
             }
         });
         lstPredictedTags.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstPredictedTags_valueChanged(evt);
             }
         });
         jScrollPane3.setViewportView(lstPredictedTags);
 
         lstGoldTags.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lst_mouseClick(evt);
             }
         });
         lstGoldTags.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstGoldTags_valueChanged(evt);
             }
         });
         jScrollPane4.setViewportView(lstGoldTags);
 
         lblPredictedTags.setText("Predicted Tags");
 
         lblGoldTags.setText("Gold Tags");
 
         javax.swing.GroupLayout pnlTagResultsLayout = new javax.swing.GroupLayout(pnlTagResults);
         pnlTagResults.setLayout(pnlTagResultsLayout);
         pnlTagResultsLayout.setHorizontalGroup(
             pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTagResultsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTagResultsLayout.createSequentialGroup()
                         .addGroup(pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblWordsList)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblPredictedTags)
                             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
                         .addGap(7, 7, 7)
                         .addGroup(pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblGoldTags)))
                     .addComponent(lblTagEvaluation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlTagResultsLayout.setVerticalGroup(
             pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTagResultsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblWordsList)
                     .addComponent(lblPredictedTags)
                     .addComponent(lblGoldTags))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlTagResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lblTagEvaluation)
                 .addContainerGap())
         );
 
         tabTag.addTab("Results", pnlTagResults);
 
         btnStartTagging.setText("Start Tagging");
         btnStartTagging.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStartTaggingActionPerformed(evt);
             }
         });
 
         lblModelPath.setText("Model Path:");
 
         txtModelPath.setEditable(false);
 
         btnBrowseModelPath.setText("...");
         btnBrowseModelPath.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseModelPathActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlTagLayout = new javax.swing.GroupLayout(pnlTag);
         pnlTag.setLayout(pnlTagLayout);
         pnlTagLayout.setHorizontalGroup(
             pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTagLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTagLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(btnStartTagging))
                     .addComponent(tabTag)
                     .addGroup(pnlTagLayout.createSequentialGroup()
                         .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblInputFile)
                             .addComponent(lblOutputPath)
                             .addComponent(chkGoldFile)
                             .addComponent(lblModelPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(txtGoldFile)
                             .addComponent(txtOutputPath)
                             .addComponent(txtInputFile)
                             .addComponent(txtModelPath))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(btnBrowseModelPath, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                             .addComponent(btnBrowseGoldFile, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                             .addComponent(btnBrowseOutputPath, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                             .addComponent(btnBrowseInputFile, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap())
         );
         pnlTagLayout.setVerticalGroup(
             pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTagLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlTagLayout.createSequentialGroup()
                         .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblModelPath)
                             .addComponent(txtModelPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(4, 4, 4))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTagLayout.createSequentialGroup()
                         .addComponent(btnBrowseModelPath)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                 .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblInputFile)
                     .addComponent(txtInputFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseInputFile))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblOutputPath)
                     .addComponent(txtOutputPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseOutputPath))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(chkGoldFile)
                     .addComponent(txtGoldFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowseGoldFile))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnStartTagging)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(tabTag)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("Tag", pnlTag);
 
         pnlAbout.addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 pnlAbout_Shown(evt);
             }
         });
 
         lblMXPostTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         lblMXPostTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblMXPostTitle.setText("MXPost (Version 1.0)");
 
         lblMXPostSiteTitle.setText("Site:");
 
         lblMXPostSiteValue.setEditable(false);
         lblMXPostSiteValue.setForeground(new java.awt.Color(0, 102, 204));
         lblMXPostSiteValue.setText("http://www.inf.ed.ac.uk/resources/nlp/local_doc/MXPOST.html");
         lblMXPostSiteValue.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         lblMXPostSiteValue.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
 
         lblMXPostAuthorsTitle.setText("Authors:");
 
         lblMXPostAuthorsValue.setEditable(false);
         lblMXPostAuthorsValue.setText("Adwait Ratnaparkhi");
         lblMXPostAuthorsValue.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         lblMXPostCitationTitle.setText("Citation:");
 
         txtMXPostCitationValue.setColumns(20);
         txtMXPostCitationValue.setEditable(false);
         txtMXPostCitationValue.setLineWrap(true);
         txtMXPostCitationValue.setRows(5);
         txtMXPostCitationValue.setText("A. Ratnaparkhi, \"A maximum entropy model for part-of-speech tagging\", in Proceedings of the Empirical Methods in Natural Language Processing Conference, University of Pennsylvania, pp. 133-142, 1996.");
         jScrollPane9.setViewportView(txtMXPostCitationValue);
 
         lblMXpostLicenseTitle.setText("License");
 
         txtMXPostLicenseValue.setColumns(20);
         txtMXPostLicenseValue.setEditable(false);
         txtMXPostLicenseValue.setRows(5);
         txtMXPostLicenseValue.setText("                              SOFTWARE LICENSE\n\n                          MXPOST and MXTERMINATOR\n\n                            All rights reserved.\n\n                        (c) 1997 Adwait Ratnaparkhi\n\n                      Developed by Adwait Ratnaparkhi\n                         University of Pennsylvania\n                 Dept. of Computer and Information Science\n                           200 South 33rd Street\n                          Philadelphia, PA. 19104\n\n   LICENSE INFORMATION\n\n   Adwait Ratnaparkhi (\"Owner\") grants to the individual researcher who\n   downloads this software (\"Licensee\") a non-exclusive, non-transferable\n   run-time license to use the MXPOST and MXTERMINATOR software\n   (\"Software\"), subject to the restrictions listed below under \"Scope of\n   Grant.\"\n\n   SCOPE OF GRANT\n\n   The Licensee may:\n     * use the Software for educational or research purposes;\n     * permit others under the Licensee's supervision at the same site to\n       use the Software for educational or research purposes;\n     * copy the Software for archival purposes, provided that any such\n       copy contains all of the original proprietary notices.\n\n   The Licensee may not:\n     * use the Software for commercial purposes;\n     * allow any individual who is not under the direct supervision of\n       the Licensee to use the Software;\n     * redistribute the Software;\n     * copy the Software other than as specified above;\n     * rent, lease, grant a security interest in, or otherwise transfer\n       rights to the Software;\n     * remove any proprietary notices or labels accompanying the\n       Software;\n\n   DISCLAIMER\n\n   The Owner makes no representations or warranties about the suitability\n   of the Software and Linguistic Resources, either express or implied,\n   including but not limited to the implied warranties of\n   merchantability, fitness for a particular purpose, or\n   non-infringement. The Owner shall not be liable for any damages\n   suffered by Licensee as a result of using, modifying or distributing\n   the Software or its derivatives.\n\n   CONSENT\n\n   By downloading, using or copying the Software, Licensee agrees to\n   abide by the intellectual property laws, and all other applicable laws\n   of the U.S., and the terms of this License. Ownership of the Software\n   shall remain solely with the Owner.\n\n   TERMINATION\n\n   The Owner shall have the right to terminate this license at any time\n   by written notice. Licensee shall be liable for any infringement or\n   damages resulting from Licensee's failure to abide by the terms of\n   this License.");
         jScrollPane5.setViewportView(txtMXPostLicenseValue);
 
         javax.swing.GroupLayout pnlAboutLayout = new javax.swing.GroupLayout(pnlAbout);
         pnlAbout.setLayout(pnlAboutLayout);
         pnlAboutLayout.setHorizontalGroup(
             pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAboutLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(lblMXPostTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAboutLayout.createSequentialGroup()
                         .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblMXpostLicenseTitle)
                             .addComponent(lblMXPostCitationTitle))
                         .addGap(11, 11, 11)
                         .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane9)
                             .addComponent(jScrollPane5)))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAboutLayout.createSequentialGroup()
                         .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblMXPostAuthorsTitle)
                             .addComponent(lblMXPostSiteTitle))
                         .addGap(10, 10, 10)
                         .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblMXPostSiteValue, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                             .addComponent(lblMXPostAuthorsValue))))
                 .addContainerGap())
         );
         pnlAboutLayout.setVerticalGroup(
             pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlAboutLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblMXPostTitle)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblMXPostSiteTitle)
                     .addComponent(lblMXPostSiteValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblMXPostAuthorsTitle)
                     .addComponent(lblMXPostAuthorsValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlAboutLayout.createSequentialGroup()
                         .addComponent(lblMXPostCitationTitle)
                         .addGap(0, 91, Short.MAX_VALUE))
                     .addGroup(pnlAboutLayout.createSequentialGroup()
                         .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)))
                 .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(pnlAboutLayout.createSequentialGroup()
                         .addComponent(lblMXpostLicenseTitle)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("About", pnlAbout);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnBrowseInputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseInputFileActionPerformed
         txtInputFile.setText(showFileDialog(txtInputFile.getText(), false, null));
     }//GEN-LAST:event_btnBrowseInputFileActionPerformed
 
     private void btnBrowseOutputPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseOutputPathActionPerformed
         txtOutputPath.setText(showFileDialog(txtOutputPath.getText(), true, null));
     }//GEN-LAST:event_btnBrowseOutputPathActionPerformed
 
     private void btnBrowseGoldFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseGoldFileActionPerformed
         String oldVal = txtGoldFile.getText();
         txtGoldFile.setText(showFileDialog(txtGoldFile.getText(), false, null));
         String newVal = txtGoldFile.getText();
         if (!oldVal.equals(newVal)) {
             chkGoldFile.setSelected(true);
         }
     }//GEN-LAST:event_btnBrowseGoldFileActionPerformed
 
     private void btnStartTaggingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartTaggingActionPerformed
 
         wordModel.clear();
         predModel.clear();
         goldModel.clear();
         txtTagLog.setText("");
         btnStartTagging.setEnabled(false);
         tabTag.setSelectedIndex(0);
         tabTag.setEnabledAt(1, false);
         
         File modelFrom = new File(txtModelPath.getText());
         File modelTo = new File(System.getProperty("user.dir") + File.separator + modelFrom.getName());
         if (!modelFrom.equals(modelTo)) {
             modelTo.mkdirs();
             copyDirectory(modelFrom, modelTo);
         }
         
         PrintStream out = new PrintStream(new OutputStream() {
 
             private StringBuffer buffer = new StringBuffer();
 
             @Override
             public void write(int b)
                     throws IOException {
                 this.buffer.append((char) b);
                 txtTagLog.setText(buffer.toString());
                 txtTagLog.setCaretPosition(txtTagLog.getDocument().getLength() - 1);
             }
         });
         RunnableTagging.out = out;
         
         String gold = null;
         if (chkGoldFile.isSelected() == true) {
             gold = txtGoldFile.getText();
         }
         
         // Run in a new thread
         Runnable job = new RunnableTagging(this, 
                 modelFrom.getName(),
                 txtInputFile.getText(), 
                 txtOutputPath.getText() + "tagged_output.lbl",
                 gold);
         ExecutorService threadPool = Executors.newFixedThreadPool(1);
         threadPool.execute(job);
         threadPool.shutdown();        
     }//GEN-LAST:event_btnStartTaggingActionPerformed
 
     private void btnBrowseModelPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseModelPathActionPerformed
         txtModelPath.setText(showFileDialog(txtModelPath.getText(), true, null));
     }//GEN-LAST:event_btnBrowseModelPathActionPerformed
 
     private void btnBrowseTrainFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseTrainFileActionPerformed
         txtTrainFile.setText(showFileDialog(txtTrainFile.getText(), false, null));
     }//GEN-LAST:event_btnBrowseTrainFileActionPerformed
 
     private void btnBrowseTrainModelPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseTrainModelPathActionPerformed
         txtTrainModelPath.setText(showFileDialog(txtTrainModelPath.getText(), true, null));
     }//GEN-LAST:event_btnBrowseTrainModelPathActionPerformed
 
     private void btnBrowseInputCoNLLFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseInputCoNLLFileActionPerformed
         txtInputCoNLLFile.setText(showFileDialog(txtInputCoNLLFile.getText(), false, null));
     }//GEN-LAST:event_btnBrowseInputCoNLLFileActionPerformed
 
     private void btnBrowseOutputTagPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseOutputTagPathActionPerformed
         txtOutputTagPath.setText(showFileDialog(txtOutputTagPath.getText(), true, null));
     }//GEN-LAST:event_btnBrowseOutputTagPathActionPerformed
 
     private void btnBrowseInputTagFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseInputTagFileActionPerformed
         txtInputTagFile.setText(showFileDialog(txtInputTagFile.getText(), false, null));
     }//GEN-LAST:event_btnBrowseInputTagFileActionPerformed
 
     private void btnBrowseOutputDepPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseOutputDepPathActionPerformed
         txtOutputDepPath.setText(showFileDialog(txtOutputDepPath.getText(), true, null));
     }//GEN-LAST:event_btnBrowseOutputDepPathActionPerformed
 
     private void btnConertDep2TagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConertDep2TagActionPerformed
         try {
             Converter.POSType type = Converter.POSType.CPOS;
             if (rbFPOSD2T.isSelected() == true) {
                 type = Converter.POSType.FPOS;
             }
             
             Converter.convertDependencyToTag(txtInputCoNLLFile.getText(), 
                     txtOutputTagPath.getText() + "pos.lbl", 
                     chkAddGoldTag.isSelected(), 
                     type);
         }
         catch(Exception ex) {}
     }//GEN-LAST:event_btnConertDep2TagActionPerformed
 
     private void btnConvertTag2DepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertTag2DepActionPerformed
         try {
             Converter.POSType type = Converter.POSType.CPOS;
             if (rbFPOST2D.isSelected() == true) {
                 type = Converter.POSType.FPOS;
             }
             
             Converter.convertTagToDependency(txtInputTagFile.getText(), 
                     txtInputCoNLLFileT2D.getText(), 
                     txtOutputDepPath.getText() + "predict_pos.conll", type);
         }
         catch(Exception ex) {
             ex.printStackTrace();
         }
     }//GEN-LAST:event_btnConvertTag2DepActionPerformed
 
     private void btnBrowseInputCoNLLFileT2DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseInputCoNLLFileT2DActionPerformed
         txtInputCoNLLFileT2D.setText(showFileDialog(txtInputCoNLLFileT2D.getText(), false, null));
     }//GEN-LAST:event_btnBrowseInputCoNLLFileT2DActionPerformed
 
     private void btnStartTrainingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartTrainingActionPerformed
 
         try {
             txtTrainLog.setText("");
             btnStartTraining.setEnabled(false);
 
             PrintStream out = new PrintStream(new OutputStream() {
 
                 private StringBuffer buffer = new StringBuffer();
 
                 @Override
                 public void write(int b)
                         throws IOException {
                     this.buffer.append((char) b);
                     txtTrainLog.setText(buffer.toString());
                     txtTrainLog.setCaretPosition(txtTrainLog.getDocument().getLength() - 1);
                 }
             });
             RunnableTrain.out = out;
 
             File trainFrom = new File(txtTrainFile.getText());
             File trainTo = new File(System.getProperty("user.dir") + File.separator + trainFrom.getName());
             if (!trainFrom.equals(trainTo))
                 FileUtils.copyFile(trainFrom, trainTo);
             
             File modelTo = new File(txtTrainModelPath.getText());
             File modelFrom = new File(System.getProperty("user.dir") + File.separator + modelTo.getName());
             modelFrom.mkdirs();
 
             // Run in a new thread
             Runnable job = new RunnableTrain(this, 
                     modelTo.getName(),
                     trainFrom.getName(), 
                     Integer.parseInt(spMaxIters.getValue().toString()));
             ExecutorService threadPool = Executors.newFixedThreadPool(1);
             threadPool.execute(job);
             threadPool.shutdown();
         }
         catch (IOException | NumberFormatException ex) {}
     }//GEN-LAST:event_btnStartTrainingActionPerformed
 
     private void lst_mouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lst_mouseClick
         lock = 0;
     }//GEN-LAST:event_lst_mouseClick
 
     private void lstWoldsList_valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstWoldsList_valueChanged
         lock++;
         if (lock == 1) {
             
             lstPredictedTags.setSelectedIndex(lstWordsList.getSelectedIndex());
             lstPredictedTags.setBounds(lstWordsList.getBounds());
             
             if (lstGoldTags.getModel().getSize() == lstPredictedTags.getModel().getSize()) {
                 lstGoldTags.setSelectedIndex(lstWordsList.getSelectedIndex());
                 lstGoldTags.setBounds(lstWordsList.getBounds());
             }
         } else {
             if (lstGoldTags.getModel().getSize() == lstPredictedTags.getModel().getSize()) {
                 if (lock == 3)
                     lock = 0;
             }
             else if (lock == 2)
                 lock = 0;
         }
     }//GEN-LAST:event_lstWoldsList_valueChanged
 
     private void lstPredictedTags_valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPredictedTags_valueChanged
         lock++;
         if (lock == 1) {
             
             lstWordsList.setSelectedIndex(lstPredictedTags.getSelectedIndex());
             lstWordsList.setBounds(lstPredictedTags.getBounds());
             
             if (lstGoldTags.getModel().getSize() == lstPredictedTags.getModel().getSize()) {
                 lstGoldTags.setSelectedIndex(lstPredictedTags.getSelectedIndex());
                 lstGoldTags.setBounds(lstPredictedTags.getBounds());
             }
         } else {
             if (lstGoldTags.getModel().getSize() == lstPredictedTags.getModel().getSize()) {
                 if (lock == 3)
                     lock = 0;
             }
             else if (lock == 2)
                 lock = 0;
         }
     }//GEN-LAST:event_lstPredictedTags_valueChanged
 
     private void lstGoldTags_valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstGoldTags_valueChanged
         lock++;
         if (lock == 1) {
             
             if (lstGoldTags.getModel().getSize() == lstPredictedTags.getModel().getSize()) {
                 lstWordsList.setSelectedIndex(lstGoldTags.getSelectedIndex());
                 lstWordsList.setBounds(lstGoldTags.getBounds());
             
                 lstPredictedTags.setSelectedIndex(lstGoldTags.getSelectedIndex());
                 lstPredictedTags.setBounds(lstGoldTags.getBounds());
             }
         } else {
             if (lstGoldTags.getModel().getSize() == lstPredictedTags.getModel().getSize()) {
                 if (lock == 3)
                     lock = 0;
             }
             else if (lock == 2)
                 lock = 0;
         }
     }//GEN-LAST:event_lstGoldTags_valueChanged
 
     private void pnlAbout_Shown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlAbout_Shown
         txtMXPostLicenseValue.setBounds(0, 0, 0, 0);
     }//GEN-LAST:event_pnlAbout_Shown
 
     public void UpdateList(List<String> words, List<String> predictTags,
             List<String> goldTags) {
         int count = words.size();
        for (int i = 0; i < count; i++) {
            wordModel.addElement(words.get(i));
            predModel.addElement(predictTags.get(i));
             if (goldTags != null && goldTags.size() == count) {
                 goldModel.addElement(goldTags.get(i));
             }
         }        
     }
     
     public void trainThreadFinished() {
         btnStartTraining.setEnabled(true);
      
         File trainFrom = new File(txtTrainFile.getText());
         File trainTo = new File(System.getProperty("user.dir") + File.separator + trainFrom.getName());
         if (!trainFrom.equals(trainTo))
             trainTo.delete();
             
         File modelTo = new File(txtTrainModelPath.getText());
         File modelFrom = new File(System.getProperty("user.dir") + File.separator + modelTo.getName());
         if (!modelFrom.equals(modelTo)) {
             copyDirectory(modelFrom, modelTo);
             deleteDirectory(modelFrom);
         }
     }
 
     public static void copyDirectory(File path, File to) {
         if (path.exists()) {
             File[] files = path.listFiles();
             for (int i = 0; i < files.length; i++) {
                 if (!files[i].isDirectory()) {
                     try {
                         File tmp = new File(to.getAbsolutePath() + File.separator + files[i].getName());
                         FileUtils.copyFile(files[i], tmp);
                     }
                     catch(Exception ex){
                         ex.printStackTrace();
                     }
                 }
             }
         }
     }
 
     public static boolean deleteDirectory(File path) {
         if (path.exists()) {
             File[] files = path.listFiles();
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     deleteDirectory(files[i]);
                 } else {
                     files[i].delete();
                 }
             }
         }
         return (path.delete());
     }
     
     public void tagThreadFinished(String res) {
         btnStartTagging.setEnabled(true);
         tabTag.setEnabledAt(1, true);
 
         lblTagEvaluation.setText(res);
         
         File modelFrom = new File(txtModelPath.getText());
         File modelTo = new File(System.getProperty("user.dir") + File.separator + modelFrom.getName());
         if (!modelFrom.equals(modelTo)) {
             deleteDirectory(modelTo);
         }
     }
     
     private String showFileDialog(String currentDir, boolean isFolder,
             FileNameExtensionFilter filter) {
         JFileChooser fc = new JFileChooser();
         if (currentDir.length() == 0) {
             fc.setCurrentDirectory(new java.io.File("."));
         } else {
             fc.setCurrentDirectory(new java.io.File(currentDir));
         }
         fc.setMultiSelectionEnabled(false);
         if (filter != null) {
             fc.setFileFilter(filter);
         }
         String title = "Select File";
         if (isFolder == true) {
             fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             title = "Select Folder";
         }
 
         if (fc.showDialog(this, title) == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
 
             String path = file.getPath();
             if (isFolder == true && path.lastIndexOf(File.separator) != path.length() - 1) {
                 path = path + File.separator;
             }
 
             return path;
         } else {
             return currentDir;
         }
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
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(POSTaggerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 new POSTaggerForm().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBrowseGoldFile;
     private javax.swing.JButton btnBrowseInputCoNLLFile;
     private javax.swing.JButton btnBrowseInputCoNLLFileT2D;
     private javax.swing.JButton btnBrowseInputFile;
     private javax.swing.JButton btnBrowseInputTagFile;
     private javax.swing.JButton btnBrowseModelPath;
     private javax.swing.JButton btnBrowseOutputDepPath;
     private javax.swing.JButton btnBrowseOutputPath;
     private javax.swing.JButton btnBrowseOutputTagPath;
     private javax.swing.JButton btnBrowseTrainFile;
     private javax.swing.JButton btnBrowseTrainModelPath;
     private javax.swing.JButton btnConertDep2Tag;
     private javax.swing.JButton btnConvertTag2Dep;
     private javax.swing.JButton btnStartTagging;
     private javax.swing.JButton btnStartTraining;
     private javax.swing.JCheckBox chkAddGoldTag;
     private javax.swing.JCheckBox chkGoldFile;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JScrollPane jScrollbar2;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JLabel lblGoldTags;
     private javax.swing.JLabel lblInputCoNLLFile;
     private javax.swing.JLabel lblInputCoNLLFileT2D;
     private javax.swing.JLabel lblInputFile;
     private javax.swing.JLabel lblInputTagFile;
     private javax.swing.JLabel lblMXPostAuthorsTitle;
     private javax.swing.JTextField lblMXPostAuthorsValue;
     private javax.swing.JLabel lblMXPostCitationTitle;
     private javax.swing.JLabel lblMXPostSiteTitle;
     private javax.swing.JTextField lblMXPostSiteValue;
     private javax.swing.JLabel lblMXPostTitle;
     private javax.swing.JLabel lblMXpostLicenseTitle;
     private javax.swing.JLabel lblMaxIters;
     private javax.swing.JLabel lblModelPath;
     private javax.swing.JLabel lblOutputDepPath;
     private javax.swing.JLabel lblOutputPath;
     private javax.swing.JLabel lblOutputTagPath;
     private javax.swing.JLabel lblPredictedTags;
     private javax.swing.JLabel lblTagEvaluation;
     private javax.swing.JLabel lblTrainFile;
     private javax.swing.JLabel lblTrainModelPath;
     private javax.swing.JLabel lblWordsList;
     private javax.swing.JList lstGoldTags;
     private javax.swing.JList lstPredictedTags;
     private javax.swing.JList lstWordsList;
     private javax.swing.JPanel pnlAbout;
     private javax.swing.JPanel pnlConverter;
     private javax.swing.JPanel pnlDep2Tag;
     private javax.swing.JPanel pnlPOSTypeD2T;
     private javax.swing.JPanel pnlPOSTypeT2D;
     private javax.swing.JPanel pnlTag;
     private javax.swing.JPanel pnlTag2Dep;
     private javax.swing.JPanel pnlTagLog;
     private javax.swing.JPanel pnlTagResults;
     private javax.swing.JPanel pnlTrain;
     private javax.swing.JRadioButton rbCPOSD2T;
     private javax.swing.JRadioButton rbCPOST2D;
     private javax.swing.JRadioButton rbFPOSD2T;
     private javax.swing.JRadioButton rbFPOST2D;
     private javax.swing.ButtonGroup rbgPOSTypeD2T;
     private javax.swing.ButtonGroup rbgPOSTypeT2D;
     private javax.swing.JSpinner spMaxIters;
     private javax.swing.JTabbedPane tabConverter;
     private javax.swing.JTabbedPane tabTag;
     private javax.swing.JTextField txtGoldFile;
     private javax.swing.JTextField txtInputCoNLLFile;
     private javax.swing.JTextField txtInputCoNLLFileT2D;
     private javax.swing.JTextField txtInputFile;
     private javax.swing.JTextField txtInputTagFile;
     private javax.swing.JTextArea txtMXPostCitationValue;
     private javax.swing.JTextArea txtMXPostLicenseValue;
     private javax.swing.JTextField txtModelPath;
     private javax.swing.JTextField txtOutputDepPath;
     private javax.swing.JTextField txtOutputPath;
     private javax.swing.JTextField txtOutputTagPath;
     private javax.swing.JTextArea txtTagLog;
     private javax.swing.JTextField txtTrainFile;
     private javax.swing.JTextArea txtTrainLog;
     private javax.swing.JTextField txtTrainModelPath;
     // End of variables declaration//GEN-END:variables
 }
