 package edu.mit.wi.haploview;
 
 import java.util.Vector;
 import java.util.Locale;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.FieldPosition;
 import java.text.NumberFormat;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import edu.mit.wi.haploview.TreeTable.*;
 import edu.mit.wi.pedfile.MathUtil;
 
 import javax.swing.*;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreeModel;
 
 
 public class HaploAssocPanel extends JPanel implements Constants,ActionListener{
     int initialHaplotypeDisplayThreshold;
     JTreeTable jtt;
 
 
     public HaploAssocPanel(Haplotype[][] haps){
         this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
 
         makeTable(haps);
     }
 
     public void makeTable(Haplotype[][] haps) {
         this.removeAll();
 
         initialHaplotypeDisplayThreshold = Options.getHaplotypeDisplayThreshold();
         Vector colNames = new Vector();
 
         colNames.add("Haplotype");
         colNames.add("Freq.");
         if (Options.getAssocTest() == ASSOC_TRIO){
             colNames.add("T:U");
         }else{
             colNames.add("Case, Control Ratios");
         }
         colNames.add("Chi Square");
         colNames.add("p value");
 
         HaplotypeAssociationNode root = new HaplotypeAssociationNode("Haplotype Associations");
 
         String[] alleleCodes = new String[5];
         alleleCodes[0] = "X";
         alleleCodes[1] = "A";
         alleleCodes[2] = "C";
         alleleCodes[3] = "G";
         alleleCodes[4] = "T";
 
         for(int i=0;i< haps.length;i++){
             Haplotype[] curBlock = haps[i];
             HaplotypeAssociationNode han = new HaplotypeAssociationNode("Block " + (i+1));
             double chisq;
 
             for(int j=0;j< curBlock.length; j++) {
                 if (curBlock[j].getPercentage()*100 >= Options.getHaplotypeDisplayThreshold()){
                     int[] genotypes = curBlock[j].getGeno();
                     StringBuffer curHap = new StringBuffer(genotypes.length);
                     for(int k=0;k<genotypes.length;k++) {
                         curHap.append(alleleCodes[genotypes[k]]);
                     }
 
                     double[][] counts;
                     if(Options.getAssocTest() == ASSOC_TRIO) {
                         counts = new double[1][2];
                         counts[0][0] = curBlock[j].getTransCount();
                         counts[0][1] = curBlock[j].getUntransCount();
                     }
                     else {
                         counts = new double[2][2];
                         counts[0][0] = curBlock[j].getCaseFreq();
                         counts[1][0] = curBlock[j].getControlFreq();
                         double caseSum=0;
                         double controlSum=0;
                         for (int k=0; k < curBlock.length; k++){
                             if (j!=k){
                                 caseSum += curBlock[k].getCaseFreq();
                                 controlSum += curBlock[k].getControlFreq();
                             }
                         }
                         counts[0][1] = caseSum;
                         counts[1][1] = controlSum;
                     }
                     chisq = getChiSq(counts);
                     han.add(new HaplotypeAssociationNode(curHap.toString(),curBlock[j].getPercentage(),counts,chisq,getPValue(chisq)));
                 }
             }
             root.add(han);
         }
         int countsOrRatios = SHOW_HAP_COUNTS;
         if(jtt != null) {
             //if were just updating the table, then we want to retain the current status of countsOrRatios
             HaplotypeAssociationModel ham = (HaplotypeAssociationModel) jtt.getTree().getModel();
             countsOrRatios = ham.getCountsOrRatios();
         }
 
         jtt = new JTreeTable(new HaplotypeAssociationModel(colNames, root));
 
         ((HaplotypeAssociationModel)(jtt.getTree().getModel())).setCountsOrRatios(countsOrRatios);
 
         jtt.getColumnModel().getColumn(0).setPreferredWidth(200);
         jtt.getColumnModel().getColumn(1).setPreferredWidth(50);
 
         //we need more space for the CC counts in the third column
         if(Options.getAssocTest() == ASSOC_CC) {
             jtt.getColumnModel().getColumn(2).setPreferredWidth(200);
             jtt.getColumnModel().getColumn(3).setPreferredWidth(75);
             jtt.getColumnModel().getColumn(4).setPreferredWidth(75);
         } else {
             jtt.getColumnModel().getColumn(2).setPreferredWidth(150);
             jtt.getColumnModel().getColumn(3).setPreferredWidth(100);
             jtt.getColumnModel().getColumn(4).setPreferredWidth(100);
         }
         jtt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
         Font monoFont = new Font("Monospaced",Font.PLAIN,12);
         jtt.setFont(monoFont);
         JTree theTree = jtt.getTree();
         theTree.setFont(monoFont);
 
         DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
         r.setLeafIcon(null);
         r.setOpenIcon(null);
         r.setClosedIcon(null);
         theTree.setCellRenderer(r);
 
         jtt.setPreferredScrollableViewportSize(new Dimension(600,jtt.getPreferredScrollableViewportSize().height));
 
         JScrollPane treeScroller = new JScrollPane(jtt);
         treeScroller.setMaximumSize(treeScroller.getPreferredSize());
         add(treeScroller);
 
         if(Options.getAssocTest() == ASSOC_CC) {
             JRadioButton countsButton = new JRadioButton("Show CC counts");
             JRadioButton ratiosButton = new JRadioButton("Show CC frequencies");
 
             ButtonGroup bg = new ButtonGroup();
 
             bg.add(countsButton);
             bg.add(ratiosButton);
             countsButton.addActionListener(this);
             ratiosButton.addActionListener(this);
             JPanel butPan = new JPanel();
             butPan.add(countsButton);
             butPan.add(ratiosButton);
             add(butPan);
             if(countsOrRatios == SHOW_HAP_RATIOS) {
                 ratiosButton.setSelected(true);
             }else{
                 countsButton.setSelected(true);
             }
         }
     }
 
     public static double getChiSq(double[][] counts) {
         double chisq=0;
 
         if (Options.getAssocTest() == ASSOC_TRIO){
             chisq = Math.pow( (counts[0][0] - counts[0][1]),2) / (counts[0][0] + counts[0][1]);
         }else{
             double N = counts[0][0] + counts[0][1] + counts[1][0] + counts[1][1];
             for (int i = 0; i < 2; i++){
                 for (int j = 0; j < 2; j++){
                     double nij = ((double)(counts[i][0] + counts[i][1])*(counts[0][j] + counts[1][j]))/N;
                     chisq += Math.pow( (counts[i][j] - nij), 2) / nij;
                 }
             }
         }
         chisq = Math.rint(chisq*1000.0)/1000.0;
         return chisq;
     }
     public static String getPValue(double chisq) {
         double pval = 0;
         pval= MathUtil.gammq(.5,.5*chisq);
         DecimalFormat df;
         //java truly sucks for simply restricting the number of sigfigs but still
         //using scientific notation when appropriate
         if (pval < 0.0001){
             df = new DecimalFormat("0.0000E0", new DecimalFormatSymbols(Locale.US));
         }else{
             df = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
         }
         String formattedNumber =  df.format(pval, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
         return formattedNumber;
     }
 
 
 
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if(command.equals("Show CC counts")) {
             HaplotypeAssociationModel ham = (HaplotypeAssociationModel)jtt.getTree().getModel();
             ham.setCountsOrRatios(SHOW_HAP_COUNTS);
             jtt.repaint();
         }
         else if (command.equals("Show CC frequencies")) {
             HaplotypeAssociationModel ham = (HaplotypeAssociationModel)jtt.getTree().getModel();
             ham.setCountsOrRatios(SHOW_HAP_RATIOS);
             jtt.repaint();
         }
 
 
     }
 }
