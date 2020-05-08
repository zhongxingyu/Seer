 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view;
 
 import controller.PrimerDesign;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.*;
 import model.Sequence;
 /**
  *
  * @author ross
  */
 public class PrimerSelectionPanel extends javax.swing.JPanel implements DocumentListener {
 
     /**
      * Creates new form PrimerSelectionPanel
      */
     
     private static ConcurrentSkipListSet<Integer> matchSet;
     private String lineNums;
     private String doubleLineNums;
     private ArrayList<Character> validChars = new ArrayList<Character>();
     private static int attempts;
     private static boolean pass;
     final Highlighter highO, highC;
     final Highlighter.HighlightPainter painterO, painterC;
     private String parsedO, parsedC;
     public static model.TestResult test;
     public static model.TestResult fTest;
     public static model.TestResult rTest;
     private boolean reversed;
         
     /*
     private class PrimerFinder implements Runnable {
 
         private String primer;
         private String strand;
 
         public PrimerFinder(String p, String s) {
             primer = p;
             strand = s;
         }
 
         @Override
         public void run() {
             for (int i = 0; i < strand.length(); i++) {
                 
                 if (primer.length() > 0 && strand.substring(i, (i + primer.length() - 1)).equalsIgnoreCase(primer)) {
                     matchSet.add(i);
                 } 
                 //else if (matchSet.contains(i)) {
                 //    matchSet.remove(i);
                 //}
             }
         }
     }
     */
     public static int realIndex(int x, int block) {
         //Potential issue: assumes line % block= 0.
         int xRounded = x - (x % block);
         return (x + (xRounded /block));
     }
     
     public static ArrayList<ArrayList<Integer>> doubleIndices(int s, int e) {
         ArrayList<Integer> start = new ArrayList<Integer>();
         ArrayList<Integer> end = new ArrayList<Integer>();
         ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();
         
         // gets the line 's' is on >>>> (((s - (s % 70))/70) + 1)
         
         int sLine = (((s - (s % 70))/70) + 1);
         int eLine = (((e - (e % 70))/70) + 1);
         
         int firstStart = (s + (s - (s %140)) + 70) - (70 * (sLine % 2));
         int firstEnd = (firstStart + 70) - ((firstStart + 70) % 70);
         int secondStart = firstEnd + 70;
         int thirdEnd = (e + (e - (e % 140)) + 70) - (70 * (eLine % 2));
         int thirdStart = thirdEnd - (thirdEnd % 70);
         int secondEnd = thirdStart - 140;
         
         start.add(realIndex(firstStart, 10)); 
         start.add(realIndex(secondStart, 10)); 
         start.add(realIndex(thirdStart, 10));
         end.add(realIndex(firstEnd, 10)); 
         end.add(realIndex(secondEnd, 10)); 
         end.add(realIndex(thirdEnd, 10));
         out.add(start);
         out.add(end);
         return out;
     }
     
     public PrimerSelectionPanel() {
         
         /*
         Issue: 
         * The number panel increments are hardcoded.
         * The block and line variables used in toString should be modifiable and
         * uniform across all screens.
         */
         
         initComponents();
         
         attempts = 0;
         reversed = false;
         
         // Create the StyleContext and the document
         validChars.add('a'); 
         validChars.add('t'); 
         validChars.add('c'); 
         validChars.add('g'); 
         validChars.add(' ');
         validChars.add('\t');
         validChars.add('\n');
 
         StyleContext sc = new StyleContext();
         final DefaultStyledDocument oDoc = new DefaultStyledDocument(sc);
         final DefaultStyledDocument cDoc = new DefaultStyledDocument(sc);
         final DefaultStyledDocument bDoc = new DefaultStyledDocument(sc);
 
         // Create and add the main document style
         Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
         final Style mainStyle = sc.addStyle("MainStyle", defaultStyle);
         StyleConstants.setFontFamily(mainStyle, "monospaced");
         StyleConstants.setForeground(mainStyle, Color.GRAY);
     
         // Create and add the target style
         final Style targetStyle = sc.addStyle("TargetStyle", null);
         StyleConstants.setFontFamily(targetStyle, "monospaced");
         StyleConstants.setForeground(targetStyle, Color.BLACK);
         StyleConstants.setBold(targetStyle, true);
         
         Style originalStyle = sc.addStyle("OriginalStyle", defaultStyle);
         StyleConstants.setFontFamily(originalStyle, "monospaced");
         StyleConstants.setForeground(originalStyle, Color.ORANGE);
         
         Style originalTargetStyle = sc.addStyle("OriginalTargetStyle", defaultStyle);
         StyleConstants.setFontFamily(originalTargetStyle, "monospaced");
         StyleConstants.setForeground(originalTargetStyle, Color.ORANGE);
         StyleConstants.setBold(originalTargetStyle, true);
         
         Style complementaryTargetStyle = sc.addStyle("ComplemenaryTargetStyle", defaultStyle);
         StyleConstants.setFontFamily(complementaryTargetStyle, "monospaced");
         StyleConstants.setForeground(complementaryTargetStyle, Color.BLUE);
         StyleConstants.setBold(complementaryTargetStyle, true);
         
         Style complementaryStyle = sc.addStyle("ComplementaryStyle", defaultStyle);
         StyleConstants.setFontFamily(complementaryStyle, "monospaced");
         StyleConstants.setForeground(complementaryStyle, Color.BLUE);
         
         int badStart = PrimerDesign.area.getStartTarget() -1;
         int badEnd = PrimerDesign.area.getEndTarget() -1;
         
         int realStart = realIndex(badStart, 10);
         int realEnd = realIndex(badEnd, 10) + 1;
         
         oDoc.setLogicalStyle(0, mainStyle);
         cDoc.setLogicalStyle(0, mainStyle);
         bDoc.setLogicalStyle(0, originalStyle);
         try {
             // Add the text to the document
             oDoc.insertString(0, PrimerDesign.start.getInSequence().toString('o', 10, 70), null);
             cDoc.insertString(0, PrimerDesign.start.getInSequence().toString('c', 10, 70), null);
             bDoc.insertString(0, PrimerDesign.start.getInSequence().toString('b', 10, 70), null);
         } catch (BadLocationException ex) {
             Logger.getLogger(PrimerSelectionPanel.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         oStrandTextPane.setDocument(oDoc);
         cStrandTextPane.setDocument(cDoc);
         bStrandTextPane.setDocument(bDoc);
         
         // Section for colouring the complementary strand
         int colourStart = 0;
         while(colourStart <= bDoc.getLength()){
             
             if((colourStart + 154) > bDoc.getLength()){
                 bDoc.setCharacterAttributes(colourStart, 77 - (154 - (bDoc.getLength() - colourStart))/2, complementaryStyle, false);
             }
             else{
                 bDoc.setCharacterAttributes(colourStart, 77, complementaryStyle, false);
             }
             
             colourStart += 154;
         }
         
         // Apply the character attributes to target section
         oDoc.setCharacterAttributes(realStart, (realEnd - realStart), targetStyle, false);
         cDoc.setCharacterAttributes(realStart, (realEnd - realStart), targetStyle, false);
         
         ArrayList<ArrayList<Integer>> indices = doubleIndices(badStart, badEnd);
         ArrayList<Integer> starts = indices.get(0);
         ArrayList<Integer> ends = indices.get(1);
         
         // Bold target section
         for (int i = 0; i < starts.size(); i++) {
             if (i == 0){
                 bDoc.setCharacterAttributes(starts.get(i), (ends.get(i) - starts.get(i)), complementaryTargetStyle, false);
                 bDoc.setCharacterAttributes(starts.get(i) + 77, (ends.get(i) - starts.get(i)), originalTargetStyle, false);
             }
             else if (i == 1){
                 colourStart = starts.get(i);
                 while(colourStart <= ends.get(i)){
                         bDoc.setCharacterAttributes(colourStart, 77 , complementaryTargetStyle, false);
                         bDoc.setCharacterAttributes(colourStart + 77, 77 , originalTargetStyle, false);
             
                         colourStart += 154;
                 }
         
             }
             else if ( i == 2){
                 bDoc.setCharacterAttributes(starts.get(i), (ends.get(i) - starts.get(i) + 1), complementaryTargetStyle, false);
                 bDoc.setCharacterAttributes(starts.get(i) + 77, (ends.get(i) - starts.get(i) + 1), originalTargetStyle, false);
             }
         }
         
         lineNums = "";
         doubleLineNums = "";
         int x = 1;
         for(int i = 0; x < PrimerDesign.start.getInSequence().length(); i++){
             lineNums += x + "\n";
             doubleLineNums += x + "\n\n";
             x += 70;
         }
         lineNumberTextArea.setText(lineNums);
         lineNumberTextArea.setCaretPosition(0);
         
         lineAreaScroll.getVerticalScrollBar().setModel(
                 oStrandScroll.getVerticalScrollBar().getModel());
         
         // ORIGINAL STRAND HIGHLIGHTING PREP
         highO = new DefaultHighlighter();
         painterO = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
         oStrandTextPane.setHighlighter(highO);
         parsedO = Sequence.parser(new Scanner(oStrandTextPane.getText()));
         forwardPrimerTextField.getDocument().addDocumentListener(this);
         // COMPLEMENTARY STRAND HIGHLIGHTING PREP
         highC = new DefaultHighlighter();
         painterC = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
         cStrandTextPane.setHighlighter(highC);
         parsedC = Sequence.parser(new Scanner(cStrandTextPane.getText()));
         
         displayTabbedPane.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 updateLineNums(displayTabbedPane.getSelectedIndex());
             }
         });
      
         
     }
     
     public void updateLineNums(int tab){
         
         if (tab == 0){
             lineNumberTextArea.setText(lineNums);
             lineNumberTextArea.setCaretPosition(0);
             lineAreaScroll.getVerticalScrollBar().setModel(
                     oStrandScroll.getVerticalScrollBar().getModel());
             reversePrimerTextField.getDocument().removeDocumentListener(this);
             forwardPrimerTextField.getDocument().addDocumentListener(this);
             SwingUtilities.invokeLater(searchO);
         }else if (tab == 1){
             lineNumberTextArea.setText(lineNums);
             lineNumberTextArea.setCaretPosition(0);
             lineAreaScroll.getVerticalScrollBar().setModel(
                     cStrandScroll.getVerticalScrollBar().getModel());
             forwardPrimerTextField.getDocument().removeDocumentListener(this);
             reversePrimerTextField.getDocument().addDocumentListener(this);
             SwingUtilities.invokeLater(searchC);
         } else {
             lineNumberTextArea.setText(doubleLineNums);
             lineNumberTextArea.setCaretPosition(0);
             lineAreaScroll.getVerticalScrollBar().setModel(
                     bStrandScroll.getVerticalScrollBar().getModel());
         }
     }
     
     Runnable searchO = new Runnable() {
         @Override
         public void run() {
 
             // ORIGINAL STRAND - FORWARD PRIMER
             String sO = forwardPrimerTextField.getText().replaceAll("\\s", "");
             //forwardPrimerTextField.setText(forwardPrimerTextField.getText().replaceAll("\\s",""));
             highO.removeAllHighlights();
             int indexO = parsedO.indexOf(sO, 0);
             int endO = 0;
             int checked = 0;
             
             while (indexO >= 0 && sO.length() > 0) {   // match found
                 try {
                     endO = indexO + sO.length();
                     highO.addHighlight(realIndex(indexO + checked, 10), realIndex(endO + checked, 10), painterO);
                     oStrandTextPane.setCaretPosition(realIndex(endO, 10));
 
                 } catch (BadLocationException e) {
                     e.printStackTrace();    // PROPER HANDLING NEEDED
                 }
                 parsedO = parsedO.substring(endO);
                 checked += endO;
                 indexO = parsedO.indexOf(sO, 0);
                 
             }
             parsedO = Sequence.parser(new Scanner(oStrandTextPane.getText()));
         }
     };
     
     Runnable searchC = new Runnable() {
         @Override
         public void run() {
             //this.reversePrimerTextField.setText(model.Primer.reverse(this.reversePrimerTextField.getText()));
             // COMPLEMENTARY STRAND - REVERSE PRIMER
             String sC = reversePrimerTextField.getText().replaceAll("\\s", "");
             //reversePrimerTextField.setText(reversePrimerTextField.getText().replaceAll("\\s",""));
             highC.removeAllHighlights();
             int indexC = parsedC.indexOf(sC, 0);
             int endC = 0;
             int checked = 0;
             
             if (reverseButton.isSelected()){
                 sC = model.Primer.reverse(sC);
                 System.out.println(sC);
             }
                 
             
             while (indexC >= 0 && sC.length() > 0) {   // match found
                 try {
                     endC = indexC + sC.length();
                     highC.addHighlight(realIndex(indexC + checked, 10), realIndex(endC + checked, 10), painterC);
                     cStrandTextPane.setCaretPosition(realIndex(endC, 10));
                 } catch (BadLocationException e) {
                     e.printStackTrace();    // PROPER HANDLING NEEDED
                 }
                 parsedC = parsedC.substring(endC);
                 checked += endC;
                 indexC = parsedC.indexOf(sC, 0);
             }
             parsedC = Sequence.parser(new Scanner(cStrandTextPane.getText()));
         }
         
         
     };
     
     
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         titleLabel = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         instructionTextPane = new javax.swing.JTextPane();
         forwardPrimerTextField = new javax.swing.JTextField();
         reversePrimerTextField = new javax.swing.JTextField();
         forwardPrimerLabel = new javax.swing.JLabel();
         reversePrimerLabel = new javax.swing.JLabel();
         backButton = new javax.swing.JButton();
         nextButton = new javax.swing.JButton();
         showRulesButton = new javax.swing.JButton();
         lineAreaScroll = new javax.swing.JScrollPane();
         lineNumberTextArea = new javax.swing.JTextArea();
         displayTabbedPane = new javax.swing.JTabbedPane();
         oStrandScroll = new javax.swing.JScrollPane();
         oStrandTextPane = new javax.swing.JTextPane();
         cStrandScroll = new javax.swing.JScrollPane();
         cStrandTextPane = new javax.swing.JTextPane();
         bStrandScroll = new javax.swing.JScrollPane();
         bStrandTextPane = new javax.swing.JTextPane();
        reverseButton = new javax.swing.JButton();
         fPrimerCheckButton = new javax.swing.JButton();
         rPrimerCheckButton = new javax.swing.JButton();
 
 
         setToolTipText("");
         setPreferredSize(new java.awt.Dimension(800, 600));
 
         titleLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 24)); // NOI18N
         titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         titleLabel.setText("Primer Selection");
 
         instructionTextPane.setEditable(false);
         instructionTextPane.setFont(new java.awt.Font("DejaVu Sans", 0, 14)); // NOI18N
         instructionTextPane.setText("You now have to choose the forward and reverse primers to amplify the region. Manually type or copy and paste the desired primer sequence into the boxes below. Click the \"Show Primer Design Rules\" button below to see general primer design rules.");
         jScrollPane2.setViewportView(instructionTextPane);
 
         forwardPrimerTextField.setMinimumSize(new java.awt.Dimension(8, 25));
         forwardPrimerTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 forwardPrimerTextFieldActionPerformed(evt);
             }
         });
 
         reversePrimerTextField.setMinimumSize(new java.awt.Dimension(8, 25));
 
         forwardPrimerLabel.setText("Forward Primer:");
 
         reversePrimerLabel.setText("Reverse Primer:");
 
         backButton.setText("Back");
         backButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 backButtonActionPerformed(evt);
             }
         });
 
         nextButton.setText("Next");
         nextButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextButtonActionPerformed(evt);
             }
         });
 
         showRulesButton.setText("Show Primer Design Rules");
         showRulesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showRulesButtonActionPerformed(evt);
             }
         });
 
         lineNumberTextArea.setEditable(false);
         lineNumberTextArea.setColumns(1);
         lineNumberTextArea.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 13)); // NOI18N
         lineNumberTextArea.setRows(5);
         lineAreaScroll.setViewportView(lineNumberTextArea);
 
         oStrandTextPane.setEditable(false);
         oStrandTextPane.setBackground(new java.awt.Color(254, 254, 254));
         oStrandTextPane.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 13)); // NOI18N
         oStrandTextPane.setMaximumSize(new java.awt.Dimension(700, 2147483647));
         oStrandScroll.setViewportView(oStrandTextPane);
 
         displayTabbedPane.addTab("DNA Sequence (5' - 3')", oStrandScroll);
 
         cStrandTextPane.setEditable(false);
         cStrandTextPane.setBackground(new java.awt.Color(254, 254, 254));
         cStrandTextPane.setFont(new java.awt.Font("DejaVu Sans Mono", 0, 13)); // NOI18N
         cStrandScroll.setViewportView(cStrandTextPane);
 
         displayTabbedPane.addTab("Complementary (3' - 5')", cStrandScroll);
 
         bStrandTextPane.setEditable(false);
         bStrandTextPane.setBackground(new java.awt.Color(254, 254, 254));
         bStrandScroll.setViewportView(bStrandTextPane);
 
         displayTabbedPane.addTab("Double Stranded", bStrandScroll);
 
         reverseButton.setText("Reverse");
         reverseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 reverseButtonActionPerformed(evt);
             }
         });
 
         fPrimerCheckButton.setText("Forward Primer Check");
         fPrimerCheckButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fPrimerCheckButtonActionPerformed(evt);
             }
         });
 
         rPrimerCheckButton.setText("Reverse Primer Check");
         rPrimerCheckButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rPrimerCheckButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(fPrimerCheckButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(rPrimerCheckButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(showRulesButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(10, 10, 10)
                         .addComponent(forwardPrimerLabel)
                         .addGap(14, 14, 14)
                         .addComponent(forwardPrimerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                         .addComponent(reversePrimerLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(reversePrimerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(reverseButton))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(lineAreaScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(displayTabbedPane))
                     .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {forwardPrimerTextField, reversePrimerTextField});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addComponent(titleLabel)
                 .addGap(6, 6, 6)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(6, 6, 6)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(forwardPrimerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(reversePrimerLabel)
                         .addComponent(reversePrimerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(reverseButton))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(7, 7, 7)
                         .addComponent(forwardPrimerLabel)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(displayTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(37, 37, 37)
                         .addComponent(lineAreaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                         .addGap(6, 6, 6)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(backButton)
                         .addComponent(fPrimerCheckButton)
                         .addComponent(rPrimerCheckButton))
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(nextButton)
                         .addComponent(showRulesButton)))
                 .addGap(12, 12, 12))
         );
 
         layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {forwardPrimerTextField, reversePrimerTextField});
 
     }// </editor-fold>//GEN-END:initComponents
 
     
     private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
         PrimerDesign.window.remove(PrimerDesign.primerSelect);
         PrimerDesign.window.setVisible(false);
 
         PrimerDesign.window.getContentPane().add(PrimerDesign.area);
         PrimerDesign.window.pack();
         PrimerDesign.window.setVisible(true);
     }//GEN-LAST:event_backButtonActionPerformed
 
     private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
         String fP = forwardPrimerTextField.getText();
         String rP = reversePrimerTextField.getText();
         attempts++;
         try {
             for (int i = 0; i < fP.length(); i++) {
                 if (!validChars.contains(fP.charAt(i)))
                     throw new HighCountException();
             }
             for (int i = 0; i < rP.length(); i++) {
                 if (!validChars.contains(rP.charAt(i)))
                     throw new HighCountException();
             }
             fP = Sequence.parser(new Scanner(fP));
             rP = Sequence.parser(new Scanner(rP));
             if (fP.contains("n") || rP.contains("n"))
                 throw new NException();
             PrimerDesign.start.getInSequence().setFPrimer(new model.Primer(fP));
             PrimerDesign.start.getInSequence().setRPrimer(new model.Primer(rP));
             test = PrimerDesign.start.getInSequence().primerTest();
             System.out.println(test.toString());
             pass = test.acceptable();
             PrimerEvaluationDialog ped = new PrimerEvaluationDialog(PrimerDesign.window, true);
             ped.setLocation(96, 100);
             ped.setVisible(true);
             if (pass) {
 
                  PrimerDesign.window.remove(PrimerDesign.primerSelect);
                  PrimerDesign.window.setVisible(false);
 
                  PrimerDesign.temperature = new FinalTemperaturePanel();
                  PrimerDesign.window.getContentPane().add(PrimerDesign.temperature);
                  PrimerDesign.window.pack();
                  PrimerDesign.window.setVisible(true);
              }           
            
        } catch(NException e1) {
            NPrimerBox npb = new NPrimerBox(PrimerDesign.window, true);
            npb.setLocation(187,450);
            npb.setVisible(true);
        }catch(HighCountException e) {
            InvalidInputBox iib = new InvalidInputBox(PrimerDesign.window, true);
            iib.setLocation(187, 450);
            iib.setVisible(true);
        }
 
     }//GEN-LAST:event_nextButtonActionPerformed
 
     private void showRulesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showRulesButtonActionPerformed
        //PrimerRulesDialog dialog = new PrimerRulesDialog(PrimerDesign.window, true);
        //dialog.setVisible(true);
        String []args = null; 
        PrimerRulesDialog.main(args);
     }//GEN-LAST:event_showRulesButtonActionPerformed
    
     private void forwardPrimerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardPrimerTextFieldActionPerformed
         /*
         Thread t = new Thread(new PrimerFinder(
                 forwardPrimerTextField.getText(),
                 PrimerDesign.start.getInSequence().getOStrand()));
         
         t.start();
         
         System.out.println(matchSet.toString());
         */
     }//GEN-LAST:event_forwardPrimerTextFieldActionPerformed
 
     private void reverseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseButtonActionPerformed
         this.reversePrimerTextField.setText(model.Primer.reverse(this.reversePrimerTextField.getText()));
     }//GEN-LAST:event_reverseButtonActionPerformed
 
     private void fPrimerCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fPrimerCheckButtonActionPerformed
         String fP = forwardPrimerTextField.getText();
         model.Primer fPrimer = new model.Primer(fP);
         model.TestResult test = fPrimer.test();
         IndividualEvaluationDialog ied = new IndividualEvaluationDialog(PrimerDesign.window, false);
         ied.setText(test.toString());
         ied.setVisible(true);
     }//GEN-LAST:event_fPrimerCheckButtonActionPerformed
 
     private void rPrimerCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rPrimerCheckButtonActionPerformed
         String rP = reversePrimerTextField.getText();
         model.Primer rPrimer = new model.Primer(rP);
         model.TestResult test = rPrimer.test();
         IndividualEvaluationDialog ied = new IndividualEvaluationDialog(PrimerDesign.window, false);
         ied.setText(test.toString());
         ied.setVisible(true);
     }//GEN-LAST:event_rPrimerCheckButtonActionPerformed
     
     
     
     public int getAttempts() {
         return attempts;
     }
     
     public boolean getPass() {
         return pass;
     }
     public void setPass(boolean p) {
         pass = p;
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane bStrandScroll;
     private javax.swing.JTextPane bStrandTextPane;
     private javax.swing.JButton backButton;
     private javax.swing.JScrollPane cStrandScroll;
     private javax.swing.JTextPane cStrandTextPane;
     private javax.swing.JTabbedPane displayTabbedPane;
     private javax.swing.JButton fPrimerCheckButton;
     private javax.swing.JLabel forwardPrimerLabel;
     private javax.swing.JTextField forwardPrimerTextField;
     private javax.swing.JTextPane instructionTextPane;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane lineAreaScroll;
     private javax.swing.JTextArea lineNumberTextArea;
     private javax.swing.JButton nextButton;
     private javax.swing.JScrollPane oStrandScroll;
     private javax.swing.JTextPane oStrandTextPane;
<<<<<<< HEAD
     private javax.swing.JButton rPrimerCheckButton;
    private javax.swing.JButton reverseButton;
=======
     private javax.swing.JToggleButton reverseButton;
>>>>>>> 4dc9806fbf3a2305cd794767693cbee053455712
     private javax.swing.JLabel reversePrimerLabel;
     private javax.swing.JTextField reversePrimerTextField;
     private javax.swing.JButton showRulesButton;
     private javax.swing.JLabel titleLabel;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void insertUpdate(DocumentEvent e) {
         if (e.getDocument() == forwardPrimerTextField.getDocument()){
             searchO.run();
         } else if (e.getDocument() == reversePrimerTextField.getDocument()){
             searchC.run();
         }
     }
 
     @Override
     public void removeUpdate(DocumentEvent e) {
         if (e.getDocument() == forwardPrimerTextField.getDocument()){
             searchO.run();
         } else if (e.getDocument() == reversePrimerTextField.getDocument()){
             searchC.run();
         }
     }
 
     @Override
     public void changedUpdate(DocumentEvent e) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
