 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 
 public class ExportDialog extends JDialog implements ActionListener, Constants{
 
     HaploView hv;
     JRadioButton dpButton, hapButton, checkButton, singleAssocButton, hapAssocButton, permAssocButton;
     JRadioButton txtButton, pngButton;
     JRadioButton allButton, someButton, adjButton;
     JCheckBox compressCheckBox;
     NumberTextField lowRange, upperRange;
 
     public ExportDialog(HaploView h){
         super(h, "Export Data");
         hv = h;
 
         JPanel contents = new JPanel();
         contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
 
         JPanel tabPanel = new JPanel();
         int currTab = hv.tabs.getSelectedIndex();
         tabPanel.setBorder(new TitledBorder("Tab to Export"));
         tabPanel.setLayout(new BoxLayout(tabPanel,BoxLayout.Y_AXIS));
         tabPanel.setAlignmentX(CENTER_ALIGNMENT);
         ButtonGroup g1 = new ButtonGroup();
         dpButton = new JRadioButton("LD");
         dpButton.setActionCommand("ldtab");
         dpButton.addActionListener(this);
         g1.add(dpButton);
         tabPanel.add(dpButton);
         if (currTab == VIEW_D_NUM){
             dpButton.setSelected(true);
         }
         hapButton = new JRadioButton("Haplotypes");
         hapButton.setActionCommand("haptab");
         hapButton.addActionListener(this);
         g1.add(hapButton);
         tabPanel.add(hapButton);
         if (currTab == VIEW_HAP_NUM){
             hapButton.setSelected(true);
         }
         if (hv.checkPanel != null){
             checkButton = new JRadioButton("Data Checks");
             checkButton.setActionCommand("checktab");
             checkButton.addActionListener(this);
             g1.add(checkButton);
             tabPanel.add(checkButton);
             if (currTab == VIEW_CHECK_NUM){
                 checkButton.setSelected(true);
             }
         }
         if (Options.getAssocTest() != ASSOC_NONE){
             singleAssocButton = new JRadioButton("Single Marker Association Tests");
             singleAssocButton.setActionCommand("singleassoctab");
             singleAssocButton.addActionListener(this);
             g1.add(singleAssocButton);
             tabPanel.add(singleAssocButton);
 
             hapAssocButton = new JRadioButton("Haplotype Association Tests");
             hapAssocButton.setActionCommand("hapassoctab");
             hapAssocButton.addActionListener(this);
             g1.add(hapAssocButton);
             tabPanel.add(hapAssocButton);
 
             permAssocButton = new JRadioButton("Permutation Results");
             permAssocButton.setActionCommand("permtab");
             permAssocButton.addActionListener(this);
             g1.add(permAssocButton);
             tabPanel.add(permAssocButton);
 
             if (currTab == VIEW_ASSOC_NUM){
                 Component c = ((JTabbedPane)hv.tabs.getComponent(currTab)).getSelectedComponent();
                 if(c == hv.tdtPanel){
                     singleAssocButton.setSelected(true);
                 }else if (c == hv.hapAssocPanel){
                     hapAssocButton.setSelected(true);
                 }else if (c == hv.permutationPanel){
                     permAssocButton.setSelected(true);
                 }
             }
         }
         contents.add(tabPanel);
 
         JPanel formatPanel = new JPanel();
         formatPanel.setBorder(new TitledBorder("Output Format"));
         ButtonGroup g2 = new ButtonGroup();
         txtButton = new JRadioButton("Text");
         txtButton.addActionListener(this);
         formatPanel.add(txtButton);
         g2.add(txtButton);
         txtButton.setSelected(true);
         pngButton = new JRadioButton("PNG Image");
         pngButton.addActionListener(this);
         formatPanel.add(pngButton);
         g2.add(pngButton);
         compressCheckBox = new JCheckBox("Compress image (smaller file)");
         formatPanel.add(compressCheckBox);
         compressCheckBox.setEnabled(false);
         if (currTab == VIEW_CHECK_NUM || currTab == VIEW_ASSOC_NUM){
             pngButton.setEnabled(false);
         }
         contents.add(formatPanel);
 
         JPanel rangePanel = new JPanel();
         rangePanel.setBorder(new TitledBorder("Range"));
         ButtonGroup g3 = new ButtonGroup();
         allButton = new JRadioButton("All");
         allButton.addActionListener(this);
         rangePanel.add(allButton);
         g3.add(allButton);
         allButton.setSelected(true);
         someButton = new JRadioButton("Marker ");
         someButton.addActionListener(this);
         rangePanel.add(someButton);
         g3.add(someButton);
         lowRange = new NumberTextField("",5,false);
         rangePanel.add(lowRange);
         rangePanel.add(new JLabel(" to "));
         upperRange = new NumberTextField("",5,false);
         rangePanel.add(upperRange);
         upperRange.setEnabled(false);
         lowRange.setEnabled(false);
         adjButton = new JRadioButton("Adjacent markers only");
         adjButton.addActionListener(this);
         rangePanel.add(adjButton);
         g3.add(adjButton);
         if (currTab != VIEW_D_NUM){
             someButton.setEnabled(false);
             adjButton.setEnabled(false);
         }
         contents.add(rangePanel);
 
         JPanel choicePanel = new JPanel();
         JButton okButton = new JButton("OK");
         okButton.addActionListener(this);
         choicePanel.add(okButton);
         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
         choicePanel.add(cancelButton);
         contents.add(choicePanel);
 
         this.setContentPane(contents);
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
     }
 
     public void actionPerformed (ActionEvent e){
         String command = e.getActionCommand();
         if (dpButton.isSelected()){
             if(txtButton.isSelected()){
                 adjButton.setEnabled(true);
                 compressCheckBox.setEnabled(false);
             }else{
                 compressCheckBox.setEnabled(true);
                 adjButton.setEnabled(false);
                 if (adjButton.isSelected()){
                     allButton.setSelected(true);
                 }
             }
             someButton.setEnabled(true);
         }else{
             compressCheckBox.setEnabled(false);
             someButton.setEnabled(false);
             adjButton.setEnabled(false);
             if (adjButton.isSelected() || someButton.isSelected()){
                 allButton.setSelected(true);
             }
         }
 
         if (someButton.isSelected()){
             upperRange.setEnabled(true);
             lowRange.setEnabled(true);
         }else{
             upperRange.setEnabled(false);
             lowRange.setEnabled(false);
         }
 
         if (command.equals("ldtab") || command.equals("haptab")){
             pngButton.setEnabled(true);
         }else if (command.equals("checktab") || command.equals("singleassoctab") || command.equals("hapassoctab")){
             pngButton.setEnabled(false);
             txtButton.setSelected(true);
         }else if (command.equals("OK")){
            int format;
            HaploviewTab tab;
             if (pngButton.isSelected()){
                 if (compressCheckBox.isSelected()){
                     format = COMPRESSED_PNG_MODE;
                 }else{
                     format = PNG_MODE;
                 }
             }else{
                 format = TXT_MODE;
             }
             if (dpButton.isSelected()){
                tab = hv.ldTab;
             } else if (hapButton.isSelected()){
                tab = hv.hapsTab;
             } else if (checkButton.isSelected()){
                tab = hv.checkTab;
             } else if (singleAssocButton.isSelected()){
                tab = hv.associationTab;
                ((JTabbedPane)tab.getComponent(0)).setSelectedComponent(hv.tdtPanel);
             }else if (hapAssocButton.isSelected()){
                tab = hv.associationTab;
                ((JTabbedPane)tab.getComponent(0)).setSelectedComponent(hv.hapAssocPanel);
             }else{
                tab = hv.associationTab;
                ((JTabbedPane)tab.getComponent(0)).setSelectedComponent(hv.permutationPanel);
             }
             this.dispose();
 
             if (allButton.isSelected()){
                 hv.export(tab,format,0,Chromosome.getUnfilteredSize());
             }else if (someButton.isSelected()){
                 try{
                     hv.export(tab,format,Integer.parseInt(lowRange.getText())-1, Integer.parseInt(upperRange.getText()));
                 }catch (NumberFormatException nfe){
                     JOptionPane.showMessageDialog(hv,
                             "Invalid marker range: " + lowRange.getText() + " - " + upperRange.getText(),
                             "Export Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
 
             }else{
                 hv.export(tab,format,-1,-1);
             }
 
         }else if (command.equals("Cancel")){
             this.dispose();
         }
     }
 
 
 }
