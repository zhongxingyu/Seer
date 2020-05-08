 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package genecentricviewer;
 
 
 import edu.ucdenver.ccp.phenogen.applets.data.ExCorFullHeatMapData;
 import edu.ucdenver.ccp.phenogen.applets.data.FullHeatMapData;
 import edu.ucdenver.ccp.phenogen.applets.data.Gene;
 import edu.ucdenver.ccp.phenogen.applets.data.HeatMapData;
 import edu.ucdenver.ccp.phenogen.applets.data.PanelProbesetData;
 import edu.ucdenver.ccp.phenogen.applets.data.ProbesetData;
 import edu.ucdenver.ccp.phenogen.applets.data.Transcript;
 import edu.ucdenver.ccp.phenogen.applets.graphics.DEColumnLabelPanel;
 import edu.ucdenver.ccp.phenogen.applets.graphics.DERowLabelPanel;
 import edu.ucdenver.ccp.phenogen.applets.graphics.ExpressionChartPanel;
 import edu.ucdenver.ccp.phenogen.applets.graphics.FullTranscriptView;
 import edu.ucdenver.ccp.phenogen.applets.graphics.HeatMapLegend;
 import edu.ucdenver.ccp.phenogen.applets.exonviewer.MainExonCorPanel;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.image.ColorModel;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisSpace;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.NumberTickUnit;
 import org.jfree.chart.axis.TickUnit;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.labels.CategoryItemLabelGenerator;
 import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
 import org.jfree.chart.plot.*;
 import org.jfree.chart.util.DefaultShadowGenerator;
 import org.jfree.chart.util.ShadowGenerator;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.general.DefaultKeyedValueDataset;
 import org.jfree.data.general.DefaultKeyedValues2DDataset;
 import org.jfree.ui.Layer;
 
 /**
  *
  * @author smahaffey
  */
 public class GeneCentricViewer extends javax.swing.JApplet {
     //Applet parameters
     String geneXML="";
     String exonHeatMap="";
     String meanHeatMap="";
     String foldchangeHeatMap="";
     String panelHerit="";
     String panelExpr="";
     String generalURL="";
     String mainEnsemblID="";
     boolean initComplete=false;
     ArrayList<Gene> genes=new ArrayList<Gene>();
     ProbesetData psd=null;
     PanelProbesetData ppd=null;
     int curDimX=50,curDimY=20;
     ArrayList<Transcript> transcriptList=new ArrayList<Transcript>();
     
     ArrayList<String> filteredDEProbeList=new ArrayList<String>();
     ValueMarker heritValMarker=new ValueMarker(0.33);
     ArrayList<String> filteredHeritProbeList=new ArrayList<String>();
     ArrayList<String> filteredExpProbeList=new ArrayList<String>();
     HashMap completeProbeset=new HashMap();
     
     boolean[] loaded=new boolean[5];
     
     
     
     
     private void filterDE() {
         filteredDEProbeList=new ArrayList<String>();
         //filter based on XML file data
         if(transcriptFilterchbx.isSelected()){
             //System.out.println("Filter Single Transcript");
             Transcript tmp=transcriptList.get(transcriptFiltercb.getSelectedIndex());
             filteredDEProbeList=tmp.getProbeSetList(intronFilterchbx.isSelected(),oppStrandFilterchbx.isSelected(),annotFilterchbx.isSelected(),annotFiltercb.getSelectedItem().toString());
         }else {
             //System.out.println("Filter w/All Transcripts");
             for(int i=0;i<transcriptList.size();i++){
                 Transcript tmp=transcriptList.get(i);
                 ArrayList<String> tmpList=tmp.getProbeSetList(intronFilterchbx.isSelected(),oppStrandFilterchbx.isSelected(),annotFilterchbx.isSelected(),annotFiltercb.getSelectedItem().toString());
                 for(int j=0;j<tmpList.size();j++){
                     String tmpProbe=tmpList.get(j);
                     if(!existIn(tmpProbe,filteredDEProbeList)){
                         filteredDEProbeList.add(tmpProbe);
                     }
                 }
                 //System.out.println("filter probesize after transcript:"+tmp.getID()+":"+filteredDEProbeList.size());
             }
         }
         //finish filtering for filters that use other data not from XML file
         if(psFilterchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             String lookFor=psNametxt.getText();
             for(int i=0;i<filteredDEProbeList.size();i++){
                 String cur=filteredDEProbeList.get(i);
                 if(cur.contains(lookFor)){
                     tmpFiltered.add(cur);
                 }
             }
             filteredDEProbeList=tmpFiltered;
         }
         
         if(foldDiffchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             try{
                 double lookFor=Double.parseDouble(foldDifftxt.getText());
                 String sign=foldDiffSigncb.getSelectedItem().toString();
                 if(sign.equals("+")){
                     lookFor=Math.abs(lookFor);
                 }else if(sign.equals("-")&&lookFor>0){
                     lookFor=lookFor*-1;
                 }else if(sign.equals("+/-")&&lookFor<0){
                     lookFor=lookFor*-1;
                 }
                 String comparison=fdcb.getSelectedItem().toString();
                 for(int i=0;i<filteredDEProbeList.size();i++){
                     boolean found=false;
                     ArrayList<Double> row=psd.getFoldDiff(filteredDEProbeList.get(i));
                     if(row!=null){
                         for(int j=0;j<row.size()&&!found;j++){
                             double rowVal=row.get(j).doubleValue();
                             if(sign.equals("+/-")){
                                 rowVal=Math.abs(rowVal);
                             }
                             if(compare(rowVal,lookFor,comparison)){
                                 tmpFiltered.add(filteredDEProbeList.get(i));
                                 found=true;
                             }
                         }
                     }
                 }
                 filteredDEProbeList=tmpFiltered;
             }catch(NumberFormatException e){
                 
             }
         }
         
         if(pvalFilterchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             try{
                 double lookFor=Double.parseDouble(pvaltxt.getText());
                 String comparison=pvalcb.getSelectedItem().toString();
                 for(int i=0;i<filteredDEProbeList.size();i++){
                     boolean found=false;
                     ArrayList<Double> row=psd.getPval(filteredDEProbeList.get(i));
                     if(row!=null){
                         for(int j=0;j<row.size()&&!found;j++){
                             double rowVal=row.get(j).doubleValue();
                             if(compare(rowVal,lookFor,comparison)){
                                 tmpFiltered.add(filteredDEProbeList.get(i));
                                 found=true;
                             }
                         }
                     }
                 }
                 filteredDEProbeList=tmpFiltered;
             }catch(NumberFormatException e){
                 
             }
         }
         
         if(fdrFilterchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             try{
                 double lookFor=Double.parseDouble(fdrtxt.getText());
                 String comparison=fdrcb.getSelectedItem().toString();
                 for(int i=0;i<filteredDEProbeList.size();i++){
                     boolean found=false;
                     ArrayList<Double> row=psd.getFDR(filteredDEProbeList.get(i));
                     if(row!=null){
                         for(int j=0;j<row.size()&&!found;j++){
                             double rowVal=row.get(j).doubleValue();
                             if(compare(rowVal,lookFor,comparison)){
                                 tmpFiltered.add(filteredDEProbeList.get(i));
                                 found=true;
                             }
                         }
                     }
                 }
                 filteredDEProbeList=tmpFiltered;
             }catch(NumberFormatException e){
                 
             }
         }
         
     }
 
     private void updateDE() {
         HeatMapData hmd=null;
         
         if(filteredDEProbeList!=null&&psd!=null){
             //System.out.println("filtered Probe List size:"+filteredDEProbeList.size());
             int colWidth=0;
             if(jRadioButton1.isSelected()){
                 hmd=psd.generateMeanHeatMapData(filteredDEProbeList);
                 colWidth=psd.getTissues().size()*psd.getMeanStrains().size();
                 deColPanel.setLabels(psd.getTissues(), psd.getMeanStrains());
                 deHeatMapLegend.setHeatMapLabel("log Base 2 Means");
             }else{
                 hmd=psd.generateFoldDiffHeatMapData(filteredDEProbeList); 
                 colWidth=psd.getTissues().size()*psd.getFoldDiffStrains().size();
                 deColPanel.setLabels(psd.getTissues(), psd.getFoldDiffStrains());
                 deHeatMapLegend.setHeatMapLabel("Difference log Base 2 Means");
             }
             if(colWidth>0){
                 int hmWidth=jScrollPane3.getWidth();
                 int tmpWidth=hmWidth/colWidth;
                 if(tmpWidth>=10&&tmpWidth<=100){
                     curDimX=tmpWidth;
                 }else if(tmpWidth>100){
                     curDimX=100;
                 }else if(tmpWidth<10){
                     curDimX=10;
                 }
             }
             deHeatMapLegend.setHeatMapData(hmd);
             //deHeatMapLegend.setHeatMapLabel("Difference Log Base 2 Means");
             //DE_Tab.add(heatMapLegend1,java.awt.BorderLayout.SOUTH);
             hmLegendPanel.add(deHeatMapLegend,java.awt.BorderLayout.CENTER);
             deRowPanel.setDoubleBuffered(true);
             //System.out.println("Later filtered Probe List size:"+filteredDEProbeList.size());
             deRowPanel.setProbeset(filteredDEProbeList,completeProbeset);
             deRowPanel.setCurDim(curDimX, curDimY);
             deColPanel.setCurDim(curDimX, curDimY);
             this.heatMapGraphicsPanel1.setHeatMapData(hmd);
             this.heatMapGraphicsPanel1.setCurDim(curDimX, curDimY);
             jScrollPane3.repaint();
             jScrollPane3.revalidate();
             DE_Tab.repaint();
             DE_Tab.revalidate();
             jTabbedPane1.revalidate();
         }
     }
     
     void filterHerit(){
         filteredHeritProbeList=new ArrayList<String>();
         //filter based on XML file data
         if(transcriptFilterchbx.isSelected()){
             //System.out.println("Filter Single Transcript");
             Transcript tmp=transcriptList.get(transcriptFiltercb.getSelectedIndex());
             filteredHeritProbeList=tmp.getProbeSetList(intronFilterchbx.isSelected(),oppStrandFilterchbx.isSelected(),annotFilterchbx.isSelected(),annotFiltercb.getSelectedItem().toString());
         }else {
             //System.out.println("Filter w/All Transcripts");
             for(int i=0;i<transcriptList.size();i++){
                 Transcript tmp=transcriptList.get(i);
                 ArrayList<String> tmpList=tmp.getProbeSetList(intronFilterchbx.isSelected(),oppStrandFilterchbx.isSelected(),annotFilterchbx.isSelected(),annotFiltercb.getSelectedItem().toString());
                 for(int j=0;j<tmpList.size();j++){
                     String tmpProbe=tmpList.get(j);
                     if(!existIn(tmpProbe,filteredHeritProbeList)){
                         filteredHeritProbeList.add(tmpProbe);
                     }
                 }
                 //System.out.println("filter probesize after transcript:"+tmp.getID()+":"+filteredHeritProbeList.size());
             }
         }
         if(psFilterchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             String lookFor=psNametxt.getText();
             for(int i=0;i<filteredHeritProbeList.size();i++){
                 String cur=filteredHeritProbeList.get(i);
                 if(cur.contains(lookFor)){
                     tmpFiltered.add(cur);
                 }
             }
             filteredHeritProbeList=tmpFiltered;
         }
         
         if(heritFilterchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             try{
                 double lookFor=Double.parseDouble(heritFiltertxt.getText());
                 heritValMarker.setValue(lookFor);
                 for(int i=0;i<filteredHeritProbeList.size();i++){
                     boolean found=false;
                     ArrayList<Double> row=ppd.getHerit(filteredHeritProbeList.get(i));
                     if(row!=null){
                         for(int j=0;j<row.size()&&!found;j++){
                             double rowVal=row.get(j).doubleValue();
                             if(rowVal>=lookFor){
                                 tmpFiltered.add(filteredHeritProbeList.get(i));
                                 found=true;
                             }
                         }
                     }
                 }
                 filteredHeritProbeList=tmpFiltered;
             }catch(NumberFormatException e){
                 
             }
         }
         
         if(dabgchbx.isSelected()){
             ArrayList<String> tmpFiltered=new ArrayList<String>();
             try{
                 double lookFor=Double.parseDouble(dabgtxt.getText());
                 for(int i=0;i<filteredHeritProbeList.size();i++){
                     boolean found=false;
                     ArrayList<Double> row=ppd.getDABG(filteredHeritProbeList.get(i));
                     if(row!=null){
                         for(int j=0;j<row.size()&&!found;j++){
                             double rowVal=row.get(j).doubleValue();
                             if(rowVal>=lookFor){
                                 tmpFiltered.add(filteredHeritProbeList.get(i));
                                 found=true;
                             }
                         }
                     }
                 }
                 filteredHeritProbeList=tmpFiltered;
             }catch(NumberFormatException e){
                 
             }
         }
         //System.out.println("end of filtering herit size:"+filteredHeritProbeList.size());
     }
     
     void updateHerit() {
         if (filteredHeritProbeList != null && ppd != null) {
             ArrayList<DefaultKeyedValues2DDataset> dkvd = null;
             String[] tissue = null;
             tissue = ppd.getTissueList();
             /*if (exCorcb.getItemCount() <= tissue.length) {
                 exCorcb.removeAllItems();
                 exCorcb.addItem("None");
                 for (int i = 0; i < tissue.length; i++) {
                     exCorcb.addItem(tissue[i]);
                 }
             }*/
             if (this.heritIndivrb.isSelected()) {
                 dkvd = ppd.getChartData(filteredHeritProbeList);
             } else if (this.heritTissuerb.isSelected()) {
                 dkvd = ppd.getChartDataTissue(filteredHeritProbeList);
             } else {
                 dkvd = ppd.getChartDataSingle(filteredHeritProbeList);
             }
 
             //System.out.println("Herit returned chart data size:"+dkvd.size());
 
             heritGridPanel.removeAll();
 
             GridLayout gheritLayout = new GridLayout();
 
             int width = Integer.parseInt(jSpinner1.getValue().toString());
             
             if(this.heritWidenGraphscb.isSelected()){
                 width=1;
             }
 
             int height = dkvd.size() / width;
             if (height * width < dkvd.size()) {
                 height++;
             }
             if (height == 0) {
                 height = 1;
             }
             int chartHeight = 200;
             if (width == 1) {
                 chartHeight = 350;
             }
 
             heritGridPanel.setPreferredSize(new Dimension(800, chartHeight * height));
             heritGridPanel.setSize(800, chartHeight * height);
             //System.out.println("Grid Size:"+height+"x"+width);
             gheritLayout.setColumns(width);
             gheritLayout.setRows(height);
             heritGridPanel.setLayout(gheritLayout);
             for (int i = 0; i < dkvd.size(); i++) {
                 System.out.println("created chart"+i);
                 String title = "";
                 if(i<filteredHeritProbeList.size()){
                     title=filteredHeritProbeList.get(i);
                 }
                 String xaxisLbl = "";
                 if (this.heritSinglerb.isSelected()) {
                     title = "Heritability of Probesets";
                     xaxisLbl = "Probesets";
                 } else if (this.heritTissuerb.isSelected()) {
                     title = tissue[i];
                     xaxisLbl = "";
                 }
 
 
                 JFreeChart chart = ChartFactory.createBarChart(title, "Probesets", "Heritability", dkvd.get(i), PlotOrientation.VERTICAL, false, true, false);
                 chart.getCategoryPlot().setShadowGenerator(new DefaultShadowGenerator());
                 NumberAxis tmpY = (NumberAxis) ((CategoryPlot) chart.getPlot()).getRangeAxis();
                 tmpY.setRange(0.0, 1.0);
                 tmpY.setTickUnit(new NumberTickUnit(0.1));
                 if (dispHeritValueschbx.isSelected()) {
                     StandardCategoryItemLabelGenerator scilg = new StandardCategoryItemLabelGenerator(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, new DecimalFormat("###.##"));
                     chart.getCategoryPlot().getRenderer().setBaseItemLabelGenerator(scilg);
                     chart.getCategoryPlot().getRenderer().setBaseItemLabelsVisible(true);
                 }
 
                 chart.getCategoryPlot().getDomainAxis().setUpperMargin(0.01);
                 chart.getCategoryPlot().getDomainAxis().setLowerMargin(0.01);
 
                 for (int k = 0; k < tissue.length; k++) {
                     if (tissue[k].equals("Heart")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(k, new Color(251, 106, 74));
                     } else if (tissue[k].equals("Brown Adipose")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(k, new Color(158, 154, 200));
                     } else if (tissue[k].equals("Liver")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(k, new Color(116, 196, 118));
                     } else if (tissue[k].equals("Brain")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(k, new Color(107, 174, 214));
                     }
                 }
                 
                 
                
 
                 if (heritFilterchbx.isSelected()) {
                     chart.getCategoryPlot().addRangeMarker(heritValMarker, Layer.FOREGROUND);
                 }
                 ChartPanel pan = null;
                 if (this.heritIndivrb.isSelected()) {
                     pan = new ChartPanel(chart, false, false, true, true, true);
                 } else if (this.heritTissuerb.isSelected()) {
                     if (tissue[i].equals("Heart")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(251, 106, 74));
                     } else if (tissue[i].equals("Brown Adipose")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(158, 154, 200));
                     } else if (tissue[i].equals("Liver")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(116, 196, 118));
                     } else if (tissue[i].equals("Brain")) {
                         chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(107, 174, 214));
                     }
                     if(this.heritWidenGraphscb.isSelected()){
                         int pwidth = dkvd.get(0).getColumnCount() * 80;
                         int pmaxWidth = dkvd.get(0).getColumnCount() * 150;
                         int pheight = 300;
                         int pmaxHeight = 300;
                         pan = new ChartPanel(chart, pwidth, pheight, pwidth, pheight, pmaxWidth, pmaxHeight, true, false, true, false, true, true, true);
                         heritGridPanel.setPreferredSize(new Dimension(pwidth, 800));
                         heritGridPanel.setSize(pwidth, 800);
                     }else{
                         pan = new ChartPanel(chart, false, false, true, true, true);
                     }
                 } else {
                     gheritLayout.setColumns(1);
                     gheritLayout.setRows(1);
                     //System.out.println("Count" + dkvd.get(0).getColumnCount());
                     int pwidth = dkvd.get(0).getColumnCount() * 150;
                     int pmaxWidth = dkvd.get(0).getColumnCount() * 300;
                     int pheight = 800;
                     int pmaxHeight = 1200;
                     pan = new ChartPanel(chart, pwidth, pheight, pwidth, pheight, pmaxWidth, pmaxHeight, true, false, true, false, true, true, true);
                     heritGridPanel.setPreferredSize(new Dimension(pwidth, 800));
                     heritGridPanel.setSize(pwidth, 800);
                 }
                 heritGridPanel.add(pan);
             }
             heritGridPanel.revalidate();
         }
     }
 
     void filterExpr() {
         filteredExpProbeList = new ArrayList<String>();
         //filter based on XML file data
         if (transcriptFilterchbx.isSelected()) {
             //System.out.println("Filter Single Transcript");
             Transcript tmp = transcriptList.get(transcriptFiltercb.getSelectedIndex());
             filteredExpProbeList = tmp.getProbeSetList(intronFilterchbx.isSelected(), oppStrandFilterchbx.isSelected(), annotFilterchbx.isSelected(), annotFiltercb.getSelectedItem().toString());
         } else {
             //System.out.println("Filter w/All Transcripts");
             for (int i = 0; i < transcriptList.size(); i++) {
                 Transcript tmp = transcriptList.get(i);
                 ArrayList<String> tmpList = tmp.getProbeSetList(intronFilterchbx.isSelected(), oppStrandFilterchbx.isSelected(), annotFilterchbx.isSelected(), annotFiltercb.getSelectedItem().toString());
                 for (int j = 0; j < tmpList.size(); j++) {
                     String tmpProbe = tmpList.get(j);
                     if (!existIn(tmpProbe, filteredExpProbeList)) {
                         filteredExpProbeList.add(tmpProbe);
                     }
                 }
                 //System.out.println("filter probesize after transcript:"+tmp.getID()+":"+filteredHeritProbeList.size());
             }
         }
         if (psFilterchbx.isSelected()) {
             ArrayList<String> tmpFiltered = new ArrayList<String>();
             String lookFor = psNametxt.getText();
             for (int i = 0; i < filteredExpProbeList.size(); i++) {
                 String cur = filteredExpProbeList.get(i);
                 if (cur.contains(lookFor)) {
                     tmpFiltered.add(cur);
                 }
             }
             filteredExpProbeList = tmpFiltered;
         }
         if (heritFilterchbx.isSelected()) {
             ArrayList<String> tmpFiltered = new ArrayList<String>();
             try {
                 double lookFor = Double.parseDouble(heritFiltertxt.getText());
                 heritValMarker.setValue(lookFor);
                 for (int i = 0; i < filteredExpProbeList.size(); i++) {
                     boolean found = false;
                     ArrayList<Double> row = ppd.getHerit(filteredExpProbeList.get(i));
                     if (row != null) {
                         for (int j = 0; j < row.size() && !found; j++) {
                             double rowVal = row.get(j).doubleValue();
                             if (rowVal >= lookFor) {
                                 tmpFiltered.add(filteredExpProbeList.get(i));
                                 found = true;
                             }
                         }
                     }
                 }
                 filteredExpProbeList = tmpFiltered;
             } catch (NumberFormatException e) {
             }
         }
 
         if (dabgchbx.isSelected()) {
             ArrayList<String> tmpFiltered = new ArrayList<String>();
             try {
                 double lookFor = Double.parseDouble(dabgtxt.getText());
 
                 for (int i = 0; i < filteredExpProbeList.size(); i++) {
                     boolean found = false;
                     ArrayList<Double> row = ppd.getDABG(filteredExpProbeList.get(i));
                     if (row != null) {
                         for (int j = 0; j < row.size() && !found; j++) {
                             double rowVal = row.get(j).doubleValue();
                             //System.out.println(filteredExpProbeList.get(i)+":j="+j+":"+rowVal);
                             if (rowVal >= lookFor) {
                                 tmpFiltered.add(filteredExpProbeList.get(i));
                                 found = true;
                             }
                         }
                     }
                 }
                 filteredExpProbeList = tmpFiltered;
             } catch (NumberFormatException e) {
             }
         }
 
         //System.out.println("end of filtering herit size:"+filteredHeritProbeList.size());
     }
     
     void updateExpr(){
         String errorList="";
         if(filteredExpProbeList!=null&&ppd!=null){
         boolean showShapes=this.ExpMarkPntschbx.isSelected();
         boolean showError=this.ExpStdErrochbx.isSelected();
         ArrayList<DefaultKeyedValues2DDataset> dkvd=null;
         
         String[] tissue=null;
         tissue=ppd.getTissueList();
         String seriesType="probeset";
         if(Exp_strainrb.isSelected()){
             seriesType="strain";
         }
         
         expGridPanel.removeAll();
         
         GridLayout gexprLayout=new GridLayout();
         gexprLayout.setColumns(2);
         gexprLayout.setRows(2);
         expGridPanel.setLayout(gexprLayout);
         
         if(Exp_allrb.isSelected()){
             dkvd=ppd.getChartDataExpTissue(filteredExpProbeList,seriesType);
             //System.out.println("Exp returned chart data size:"+dkvd.size());
             for(int i=0;i<dkvd.size();i++){
                 if(dkvd.get(i).getRowCount()>0){
                     String title=tissue[i];
                     String xaxisLbl="";
                     JFreeChart chart=ChartFactory.createLineChart(title,"Strains","Log2 Expression",dkvd.get(i),PlotOrientation.VERTICAL,true,true,false);
                     chart.getCategoryPlot().getDomainAxis().setUpperMargin(0.01);
                     chart.getCategoryPlot().getDomainAxis().setLowerMargin(0.01);
                     ChartPanel pan=new ChartPanel(chart,false,false,true,true,true);
                     expGridPanel.add(pan);
                 }else{
                     if(errorList.equals("")){
                         errorList=tissue[i];
                     }else{
                         errorList=errorList+","+tissue[i];
                     }
                 }
             }    
                 
         }else if(Exp_indivrb.isSelected()){
             boolean wide=false;
             if(tissue.length==1){
                 wide=true;
                 gexprLayout.setColumns(1);
                 gexprLayout.setRows(1);
             }
             for(int i=0;i<tissue.length;i++){
                 if(ppd.getStrainHeader(tissue[i])!=null){
                     ExpressionChartPanel ecp=new ExpressionChartPanel();
                     ecp.setStrains(ppd.getStrainHeader(tissue[i]), tissue[i]);
                     ecp.setProbeset(completeProbeset);
                     if(!showError){
                         ecp.setData(ppd.getChartDataExpTissue(filteredExpProbeList, seriesType,tissue[i]),filteredExpProbeList,showShapes,wide);
                     }else{
                         ecp.setData(ppd.getChartDataExpTissueStats(filteredExpProbeList, seriesType,tissue[i]),filteredExpProbeList,showShapes,showError,wide);
                     }
                     expGridPanel.add(ecp);
                 }else{
                     if(errorList.equals("")){
                         errorList=tissue[i];
                     }else{
                         errorList=errorList+","+tissue[i];
                     }
                 }
             }
         }
         
         if(!errorList.equals("")){
             errorLbl.setText("Error generating data for tissue(s): "+errorList);
             errorLbl.setVisible(true);
         }
         
         expGridPanel.revalidate();
         }
     }
     
     private boolean compare(double doubleValue, double lookFor, String comparison) {
         boolean ret=false;
         if(comparison.equals(">=")){
             if(doubleValue>=lookFor){
                 ret=true;
             }
         }else if(comparison.equals("<=")){
             if(doubleValue<=lookFor){
                 ret=true;
             }
         }else if(comparison.equals("<")){
             if(doubleValue<lookFor){
                 ret=true;
             }
         }else if(comparison.equals(">")){
             if(doubleValue>lookFor){
                 ret=true;
             }
         }else if(comparison.equals("=")){
             if(doubleValue==lookFor){
                 ret=true;
             }
         }
         return ret;
     }
 
     private void filterUpdateExCor() {
         /*String annotString="full";
         if(this.annotFilterchbx.isSelected()){
             annotString=getAnnotationString();
         }
         String dabgTxt="0";
         boolean dabgNA=true;
         if(this.dabgchbx.isSelected()){
             dabgTxt=dabgtxt.getText();
             dabgNA=false;
         }
         String heritTxt="0";
         boolean heritNA=true;
         if(this.heritFilterchbx.isSelected()){
             heritTxt=this.heritFiltertxt.getText();
             heritNA=false;
         }
         boolean intron=this.intronFilterchbx.isSelected();
         boolean opStrand=this.oppStrandFilterchbx.isSelected();
         
         mainExonCorPanel1.filter(exCorLeftTranscb.getSelectedIndex(),exCorRightTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
         */ 
         int ind=exCorGenecb.getSelectedIndex();
         String geneID=genes.get(ind).getGeneID();
         if(fhmd!=null){
             //String url=this.generalURL+"exCor_"+geneID+".png";
             String geneSymbol=genes.get(ind).getGeneSymbol();
             //if(exCorcb.getSelectedIndex()>-1){
             //    mainExonCorPanel1.setURL(url, geneID+"("+genes.get(ind).getGeneSymbol()+")");
             //}
             mainExonCorPanel1.setHeatMap(fhmd,geneID,geneSymbol,genes.get(ind).getTranscripts());
             //mainExonCorPanel1.hideEIP();
             String annotString="full";
             if(this.annotFilterchbx.isSelected()){
                 annotString=getAnnotationString();
             }
             String dabgTxt="0";
             boolean dabgNA=true;
             if(this.dabgchbx.isSelected()){
                 dabgTxt=dabgtxt.getText();
                 dabgNA=false;
             }
             String heritTxt="0";
             boolean heritNA=true;
             if(this.heritFilterchbx.isSelected()){
                 heritTxt=this.heritFiltertxt.getText();
                 heritNA=false;
             }
             boolean intron=this.intronFilterchbx.isSelected();
             boolean opStrand=this.oppStrandFilterchbx.isSelected();
             mainExonCorPanel1.updateLeftMap(exCorLeftTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
             if(exCorRightTranscb.getSelectedIndex()>0){
                 mainExonCorPanel1.updateRightMap(exCorRightTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
             }
         }
     }
 
     void setQTLEnabled(boolean b) {
         jTabbedPane1.setEnabledAt(4, b);
     }
     
     
     
     
     
     private static class Corner extends JComponent {
         
         public Corner() {
         }
         @Override
         protected void paintComponent(Graphics g) {
             // Fill me with dirty brown/orange.
             g.setColor(Color.WHITE);
             g.fillRect(0, 0, getWidth(), getHeight());
         }
         
     }
     
     /**
      * Initializes the applet GeneCentricViewer
      */
     @Override
     public void init() {
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
             java.util.logging.Logger.getLogger(GeneCentricViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(GeneCentricViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(GeneCentricViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(GeneCentricViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the applet
          */
         try {
             java.awt.EventQueue.invokeAndWait(new Runnable() {
 
                 public void run() {
                     initComponents();                   
                 }
             });
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     
     @Override
     public void start() {
         //super.start();
         for(int i=0;i<loaded.length;i++){
             loaded[i]=false;
         }
         
         ArrayList<String[]> downloadFiles=new ArrayList<String[]>();
         
         generalURL=getParameter("genURL");
         mainEnsemblID=getParameter("main_ensembl_id");
         
         geneXML = generalURL+"Gene.xml";
         meanHeatMap = generalURL+"DE_means.csv";
         foldchangeHeatMap = generalURL+"DE_folddiff.csv";
         panelHerit = generalURL+ "Panel_Herit.csv";
         panelExpr=generalURL+"Panel_Expr_group.txt";
 
         //System.out.println("from page "+panelHerit);
         String[] tmp=new String[2];
         tmp[0]="geneXML";
         tmp[1]=geneXML;
         downloadFiles.add(tmp);
         tmp=new String[2];
         tmp[0]="deMean";
         tmp[1]=this.meanHeatMap;
         downloadFiles.add(tmp);
         tmp=new String[2];
         tmp[0]="deFoldDiff";
         tmp[1]=this.foldchangeHeatMap;
         downloadFiles.add(tmp);
         tmp=new String[2];
         tmp[0]="panelHerit";
         tmp[1]=this.panelHerit;
         downloadFiles.add(tmp);
         tmp=new String[2];
         tmp[0]="eqtlTranscript";
         tmp[1]="tmp_psList_transcript.txt";
         downloadFiles.add(tmp);
         tmp=new String[2];
         tmp[0]="panelExp";
         tmp[1]=this.panelExpr;
         downloadFiles.add(tmp);
         
         
         
         //tmp[0]=""
         
         jTabbedPane1.setEnabledAt(4, false);
         mainExonCorPanel1.setProgressBarVisible(false);
         errorLbl.setVisible(false);
         errorLbl1.setVisible(false);
 
         DownloadThread dt=new DownloadThread(this);
         dt.setFiles(downloadFiles);
         dt.start();
         jScrollPane3.setCorner(JScrollPane.UPPER_LEFT_CORNER, new Corner());
         jScrollPane3.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
         jScrollPane3.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new Corner());
         jScrollPane3.setColumnHeaderView(deColPanel);
         jScrollPane3.setRowHeaderView(deRowPanel);
         psNametxt.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 processPSTxt();
             }
             public void removeUpdate(DocumentEvent e) {
                 processPSTxt();
             }
             public void insertUpdate(DocumentEvent e) {
                 processPSTxt();
             }
             
         });
         
         foldDifftxt.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 processFoldDiffTxt();
             }
             public void removeUpdate(DocumentEvent e) {
                 processFoldDiffTxt();
             }
             public void insertUpdate(DocumentEvent e) {
                 processFoldDiffTxt();
             }
             
         });
         fdrtxt.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 processFDRTxt();
             }
             public void removeUpdate(DocumentEvent e) {
                 processFDRTxt();
             }
             public void insertUpdate(DocumentEvent e) {
                 processFDRTxt();
             }
             
         });
         
         pvaltxt.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 processPValTxt();
             }
             public void removeUpdate(DocumentEvent e) {
                 processPValTxt();
             }
             public void insertUpdate(DocumentEvent e) {
                 processPValTxt();
             }
             
         });
         heritFiltertxt.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 processHeritTxt();
             }
             public void removeUpdate(DocumentEvent e) {
                 processHeritTxt();
             }
             public void insertUpdate(DocumentEvent e) {
                 processHeritTxt();
             }
             
         });
         
         dabgtxt.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 processDabgTxt();
             }
             public void removeUpdate(DocumentEvent e) {
                 processDabgTxt();
             }
             public void insertUpdate(DocumentEvent e) {
                 processDabgTxt();
             }
             
         });
         
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("hideWorking", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
         
         initComplete=true;
         //mainExonCorPanel1.setLoadingLblText("Please select a Tissue to Load Data.");
         /*System.out.println("INIT STOP");
         while(dt.getFHMD()==null||dt.getGenes()==null){
             System.out.println("waiting");
         }
         this.setup(dt.getFHMD(), dt.getGenes());*/
    }
     
     void processPSTxt() {
         if (psFilterchbx.isSelected()) {
             processFilter();
             
         }
     }
 
     void processPValTxt() {
         if (pvalFilterchbx.isSelected()) {
             processFilter();
             
         }
     }
 
     void processFoldDiffTxt() {
         if (foldDiffchbx.isSelected()) {
             processFilter();
             
         }
     }
 
     void processFDRTxt() {
         if (this.fdrFilterchbx.isSelected()) {
             processFilter();
             
         }
     }
     
     void processHeritTxt() {
         if (this.heritFilterchbx.isSelected()) {
             processFilter();
         }
     }
     
     void processDabgTxt() {
         if (this.dabgchbx.isSelected()) {
             processFilter();
         }
     }
     
     void setupGene(ArrayList<Gene> genes) {
         transcriptFiltercb.removeAllItems();
         transcriptList=new ArrayList<Transcript>();
         int selected=-1;
         this.genes=genes;
         for(int i=0;i<genes.size();i++){
             //System.out.println("add"+genes.get(i).getGeneID());
             ArrayList<Transcript> trans=genes.get(i).getTranscripts();
             for(int j=0;j<trans.size();j++){
                 //System.out.println("add trans:"+trans.get(j).getID());
                 transcriptFiltercb.addItem(genes.get(i).getGeneID()+"."+trans.get(j).getID());
                 transcriptList.add(trans.get(j));
                 trans.get(j).fillProbesetMap(completeProbeset);
             }
             exCorGenecb.addItem(genes.get(i));
             if(genes.get(i).getGeneID().equals(mainEnsemblID)){
                 selected=i;
             }
         }
         if(selected>-1){
             exCorGenecb.setSelectedIndex(selected);
         }
         
     }
     
     void setupDE(ProbesetData psd) {
         this.psd=psd;
         /*if(psd.getProbeList().size()==0){
             jTabbedPane1.setEnabledAt(0, false);
             jTabbedPane1.setSelectedIndex(1);
             jTabbedPane1.setToolTipTextAt(0, "Disabled Only Available for Rat.");
         }*/
         this.processFilter();
         this.DE_loadinglbl.setVisible(false);
         this.DE_loadingpb.setVisible(false);
         loaded[0]=true;
         
         /*HeatMapData hmd=psd.generateMeanHeatMapData(psd.getProbeList());
         heatMapLegend1.setHeatMapData(hmd);
         DE_Tab.add(heatMapLegend1,java.awt.BorderLayout.SOUTH);   
         deRowPanel.setDoubleBuffered(true);
         deRowPanel.setProbeset(psd.getProbeList());
         deRowPanel.setCurDim(curDimX, curDimY);
         deColPanel.setLabels(psd.getTissues(), psd.getMeanStrains());
         deColPanel.setCurDim(curDimX, curDimY);
         jScrollPane3.setColumnHeaderView(deColPanel);
         jScrollPane3.setRowHeaderView(deRowPanel);
         this.heatMapGraphicsPanel1.setHeatMapData(hmd);
         this.heatMapGraphicsPanel1.setCurDim(curDimX, curDimY);
         
         jScrollPane3.repaint();
         jScrollPane3.revalidate();
         DE_Tab.repaint();
         DE_Tab.revalidate();
         jTabbedPane1.revalidate();*/
     }
     
     void setupHerit(PanelProbesetData ppd){
         //System.out.println("CALLED SETUP HERIT");
         this.ppd=ppd;
         this.heritValMarker.setLabel("Herit Cutoff");
         this.Herit_loadinglbl.setVisible(false);
         this.Herit_loadingpb.setVisible(false);
         filterHerit();
         updateHerit();
         loaded[1]=true;        
         
         /*DefaultKeyedValues2DDataset dkvd=new DefaultKeyedValues2DDataset();
         dkvd.addValue(0.55,"1","Brain");
         dkvd.addValue(0.3,"2","BAT");
         dkvd.addValue(0.4,"3","Heart");
         dkvd.addValue(0.2,"4","Liver");*/
     }
     
     void setupExpr(PanelProbesetData ppd){
         //System.out.println("CALLED SETUP Expr");
         this.ppd=ppd;
         this.Exp_loadinglbl.setVisible(false);
         this.Exp_loadingpb.setVisible(false);
         this.errorLbl.setVisible(false);
         filterExpr();
         updateExpr();
         loaded[2]=true;
         
         String[] tissue=ppd.getTissueList();
         //if (exCorcb.getItemCount() <= tissue.length) {
                 exCorcb.removeAllItems();
                 String errorList="";
                 for (int i = 0; i < tissue.length; i++) {
                     String tissueNoSpaces=tissue[i].replaceAll(" ", "_");
                     String url=this.generalURL+tissueNoSpaces+"_exonCorHeatMap.txt";
                     boolean exCorReady=false;
                     try {
                         exCorReady=ReadDataFiles.isExCorFileReady(url);
                     } catch (Exception ex) {
                     }
                     //System.out.println("BEFORE call to add:"+tissue[i]+":"+exCorReady);
                     if(exCorReady){
                         exCorcb.addItem(tissue[i]);
                     }else{
                         if(errorList.equals("")){
                             errorList=tissue[i];
                         }else{
                             errorList=errorList+","+tissue[i];
                         }
                     }
                 }
         //}
         this.ExCor_loadinglbl.setVisible(false);
         this.ExCor_loadingpb.setVisible(false);
         int ind=exCorGenecb.getSelectedIndex();
         String geneID=genes.get(ind).getGeneID();
         String url=this.generalURL+"exCor_"+geneID+".png";
         String geneSymbol=genes.get(ind).getGeneSymbol();
         if(exCorcb.getSelectedIndex()>-1){
             mainExonCorPanel1.setURL(url, geneID+"("+genes.get(ind).getGeneSymbol()+")");
         }
         mainExonCorPanel1.hideEIP();
         /*DefaultKeyedValues2DDataset dkvd=new DefaultKeyedValues2DDataset();
         dkvd.addValue(0.55,"1","Brain");
         dkvd.addValue(0.3,"2","BAT");
         dkvd.addValue(0.4,"3","Heart");
         dkvd.addValue(0.2,"4","Liver");*/
         if(!errorList.equals("")){
             errorLbl1.setText("Error generating data for tissue(s): "+errorList);
             errorLbl1.setVisible(true);
         }
     }
 
     /**
      * This method is called from within the init() method to initialize the
      * form. WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         DE_Disp1_rbgroup = new javax.swing.ButtonGroup();
         buttonGroup1 = new javax.swing.ButtonGroup();
         FillerPanel1 = new javax.swing.JPanel();
         Exp_Disp1 = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         Exp_probesetrb = new javax.swing.JRadioButton();
         Exp_strainrb = new javax.swing.JRadioButton();
         buttonGroup2 = new javax.swing.ButtonGroup();
         Exp_Disp2 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         Exp_indivrb = new javax.swing.JRadioButton();
         Exp_allrb = new javax.swing.JRadioButton();
         buttonGroup3 = new javax.swing.ButtonGroup();
         RNA_Tab = new javax.swing.JPanel();
         jTabbedPane2 = new javax.swing.JTabbedPane();
         jPanel6 = new javax.swing.JPanel();
         jPanel7 = new javax.swing.JPanel();
         uRLImagePanel2 = new edu.ucdenver.ccp.phenogen.applets.graphics.URLImagePanel();
         jTabbedPane3 = new javax.swing.JTabbedPane();
         jPanel8 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jPanel9 = new javax.swing.JPanel();
         jPanel10 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable2 = new javax.swing.JTable();
         jLabel2 = new javax.swing.JLabel();
         DABG_Filter = new javax.swing.JPanel();
         dabgchbx = new javax.swing.JCheckBox();
         dabgtxt = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         Herit_Filter = new javax.swing.JPanel();
         heritFiltertxt = new javax.swing.JTextField();
         heritFilterchbx = new javax.swing.JCheckBox();
         Exp_Disp3 = new javax.swing.JPanel();
         ExpMarkPntschbx = new javax.swing.JCheckBox();
         ExpStdErrochbx = new javax.swing.JCheckBox();
         ExCor_Disp1 = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         exCorcb = new javax.swing.JComboBox();
         ExCor_Disp2 = new javax.swing.JPanel();
         jLabel8 = new javax.swing.JLabel();
         exCorGenecb = new javax.swing.JComboBox();
         ExCor_Disp3 = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         exCorLeftTranscb = new javax.swing.JComboBox();
         ExCor_Disp4 = new javax.swing.JPanel();
         jLabel10 = new javax.swing.JLabel();
         exCorRightTranscb = new javax.swing.JComboBox();
         Herit_Disp3 = new javax.swing.JPanel();
         jLabel11 = new javax.swing.JLabel();
         jSpinner1 = new javax.swing.JSpinner();
         jLabel12 = new javax.swing.JLabel();
         Herit_Disp4 = new javax.swing.JPanel();
         heritWidenGraphscb = new javax.swing.JCheckBox();
         ExCor_Disp5 = new javax.swing.JPanel();
         ExCorLinkedSBchbx = new javax.swing.JCheckBox();
         DE_valueToDispcb = new javax.swing.JComboBox();
         jPanel12 = new javax.swing.JPanel();
         FilterOptPanel = new javax.swing.JPanel();
         fullLenFilterPanel = new javax.swing.JPanel();
         Transcript_Filter = new javax.swing.JPanel();
         transcriptFiltercb = new javax.swing.JComboBox();
         transcriptFilterchbx = new javax.swing.JCheckBox();
         filterGridPanel = new javax.swing.JPanel();
         DE_Filter1 = new javax.swing.JPanel();
         fdcb = new javax.swing.JComboBox();
         foldDifftxt = new javax.swing.JTextField();
         foldDiffSigncb = new javax.swing.JComboBox();
         foldDiffchbx = new javax.swing.JCheckBox();
         DE_Filter2 = new javax.swing.JPanel();
         pvalcb = new javax.swing.JComboBox();
         pvaltxt = new javax.swing.JTextField();
         pvalFilterchbx = new javax.swing.JCheckBox();
         DE_Filter3 = new javax.swing.JPanel();
         fdrcb = new javax.swing.JComboBox();
         fdrtxt = new javax.swing.JTextField();
         fdrFilterchbx = new javax.swing.JCheckBox();
         Probeset_Filter = new javax.swing.JPanel();
         psNametxt = new javax.swing.JTextField();
         psFilterchbx = new javax.swing.JCheckBox();
         Intron_Filter = new javax.swing.JPanel();
         intronFilterchbx = new javax.swing.JCheckBox();
         OpStrand_Filter = new javax.swing.JPanel();
         oppStrandFilterchbx = new javax.swing.JCheckBox();
         Annot_Filter = new javax.swing.JPanel();
         annotFilterchbx = new javax.swing.JCheckBox();
         annotFiltercb = new javax.swing.JComboBox();
         DisplayOptPanel = new javax.swing.JPanel();
         DE_Disp1 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jRadioButton1 = new javax.swing.JRadioButton();
         jRadioButton2 = new javax.swing.JRadioButton();
         DE_Disp2 = new javax.swing.JPanel();
         jCheckBox1 = new javax.swing.JCheckBox();
         Herit_Disp1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         heritIndivrb = new javax.swing.JRadioButton();
         heritSinglerb = new javax.swing.JRadioButton();
         heritTissuerb = new javax.swing.JRadioButton();
         Herit_Disp2 = new javax.swing.JPanel();
         dispHeritValueschbx = new javax.swing.JCheckBox();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         DE_Tab = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         heatMapGraphicsPanel1 = new edu.ucdenver.ccp.phenogen.applets.graphics.HeatMapGraphicsPanel();
         DE_loadingpb = new javax.swing.JProgressBar();
         DE_loadinglbl = new javax.swing.JLabel();
         jPanel4 = new javax.swing.JPanel();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         jButton7 = new javax.swing.JButton();
         hmLegendPanel = new javax.swing.JPanel();
         parentalExpressionHelpbtn = new javax.swing.JButton();
         Herit_Tab = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         heritGridPanel = new javax.swing.JPanel();
         Herit_loadinglbl = new javax.swing.JLabel();
         Herit_loadingpb = new javax.swing.JProgressBar();
         panelHeritHelpbtn = new javax.swing.JButton();
         Exp_Tab = new javax.swing.JPanel();
         Exp_loadinglbl = new javax.swing.JLabel();
         Exp_loadingpb = new javax.swing.JProgressBar();
         expGridPanel = new javax.swing.JPanel();
         panelExpressionHelpbtn = new javax.swing.JButton();
         errorLbl = new javax.swing.JLabel();
         ExCor_Tab = new javax.swing.JPanel();
         mainExonCorPanel1 = new edu.ucdenver.ccp.phenogen.applets.exonviewer.MainExonCorPanel();
         exonCorrHelpbtn = new javax.swing.JButton();
         ExCor_loadinglbl = new javax.swing.JLabel();
         ExCor_loadingpb = new javax.swing.JProgressBar();
         errorLbl1 = new javax.swing.JLabel();
         EQTL_Tab = new javax.swing.JPanel();
         jButton3 = new javax.swing.JButton();
         filterDisplayHelpbtn = new javax.swing.JButton();
 
         FillerPanel1.setBackground(new java.awt.Color(255, 255, 255));
 
         org.jdesktop.layout.GroupLayout FillerPanel1Layout = new org.jdesktop.layout.GroupLayout(FillerPanel1);
         FillerPanel1.setLayout(FillerPanel1Layout);
         FillerPanel1Layout.setHorizontalGroup(
             FillerPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 201, Short.MAX_VALUE)
         );
         FillerPanel1Layout.setVerticalGroup(
             FillerPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 40, Short.MAX_VALUE)
         );
 
         Exp_Disp1.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel3.setText("Display in Series:");
 
         buttonGroup2.add(Exp_probesetrb);
         Exp_probesetrb.setSelected(true);
         Exp_probesetrb.setText("Probesets");
         Exp_probesetrb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Exp_probesetrbActionPerformed(evt);
             }
         });
 
         buttonGroup2.add(Exp_strainrb);
         Exp_strainrb.setText("Strains");
         Exp_strainrb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Exp_strainrbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Exp_Disp1Layout = new org.jdesktop.layout.GroupLayout(Exp_Disp1);
         Exp_Disp1.setLayout(Exp_Disp1Layout);
         Exp_Disp1Layout.setHorizontalGroup(
             Exp_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_Disp1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel3)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(Exp_probesetrb)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(Exp_strainrb)
                 .addContainerGap(243, Short.MAX_VALUE))
         );
         Exp_Disp1Layout.setVerticalGroup(
             Exp_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(jLabel3)
                 .add(Exp_probesetrb)
                 .add(Exp_strainrb))
         );
 
         Exp_Disp2.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel4.setText("Display As:");
 
         buttonGroup3.add(Exp_indivrb);
         Exp_indivrb.setSelected(true);
         Exp_indivrb.setText("Individual");
         Exp_indivrb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Exp_indivrbActionPerformed(evt);
             }
         });
 
         buttonGroup3.add(Exp_allrb);
         Exp_allrb.setText("All Probesets by Tissue");
         Exp_allrb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Exp_allrbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Exp_Disp2Layout = new org.jdesktop.layout.GroupLayout(Exp_Disp2);
         Exp_Disp2.setLayout(Exp_Disp2Layout);
         Exp_Disp2Layout.setHorizontalGroup(
             Exp_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_Disp2Layout.createSequentialGroup()
                 .add(jLabel4)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(Exp_indivrb)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(Exp_allrb)
                 .addContainerGap(199, Short.MAX_VALUE))
         );
         Exp_Disp2Layout.setVerticalGroup(
             Exp_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(jLabel4)
                 .add(Exp_indivrb)
                 .add(Exp_allrb))
         );
 
         RNA_Tab.setBackground(new java.awt.Color(255, 255, 255));
 
         org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 952, Short.MAX_VALUE)
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 912, Short.MAX_VALUE)
         );
 
         jTabbedPane2.addTab("Isoform DE", jPanel6);
 
         org.jdesktop.layout.GroupLayout uRLImagePanel2Layout = new org.jdesktop.layout.GroupLayout(uRLImagePanel2);
         uRLImagePanel2.setLayout(uRLImagePanel2Layout);
         uRLImagePanel2Layout.setHorizontalGroup(
             uRLImagePanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 0, Short.MAX_VALUE)
         );
         uRLImagePanel2Layout.setVerticalGroup(
             uRLImagePanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 71, Short.MAX_VALUE)
         );
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {"Exon 1",  new Integer(10),  new Integer(10)},
                 {"Exon 2",  new Integer(12),  new Integer(12)},
                 {"Exon 4",  new Integer(4),  new Integer(11)},
                 {"Exon 5",  new Integer(13),  new Integer(13)},
                 {"Exon 6",  new Integer(12),  new Integer(12)},
                 {"Exon 7",  new Integer(11),  new Integer(11)},
                 {"Exon 8",  new Integer(10),  new Integer(11)},
                 {"Exon 9",  new Integer(13),  new Integer(14)},
                 {"Exon 10",  new Integer(12),  new Integer(14)},
                 {"Exon 11",  new Integer(15),  new Integer(13)},
                 {"Exon 12",  new Integer(11),  new Integer(11)}
             },
             new String [] {
                 "Exon", "BNLX", "SHRH"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         jScrollPane1.setViewportView(jTable1);
 
         org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 931, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
         );
 
         jTabbedPane3.addTab("Table", jPanel8);
 
         org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 931, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 789, Short.MAX_VALUE)
         );
 
         jTabbedPane3.addTab("Graph", jPanel9);
 
         jTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {"AGCUCAUGCU",  new Integer(1),  new Integer(34944606),  new Integer(5),  new Integer(6)},
                 {"GCAGCACG",  new Integer(30),  new Integer(34944636),  new Integer(8),  new Integer(7)},
                 {"CGAUCGAUGACU",  new Integer(75),  new Integer(34944681),  new Integer(10),  new Integer(12)},
                 {"AGCUAGUCUGA",  new Integer(104),  new Integer(34944710),  new Integer(15),  new Integer(14)}
             },
             new String [] {
                 "Sequence", "Exon Position", "Absolute Position", "BNLX Counts", "SHRH Counts"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         jScrollPane2.setViewportView(jTable2);
 
         jLabel2.setText("Exon 2");
 
         org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 931, Short.MAX_VALUE)
             .add(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel2)
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel10Layout.createSequentialGroup()
                 .add(jLabel2)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 548, Short.MAX_VALUE)
                 .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jTabbedPane3.addTab("Details", jPanel10);
 
         org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(uRLImagePanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jTabbedPane3)
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel7Layout.createSequentialGroup()
                 .add(uRLImagePanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jTabbedPane3))
         );
 
         jTabbedPane2.addTab("Exon Qauntitation", jPanel7);
 
         org.jdesktop.layout.GroupLayout RNA_TabLayout = new org.jdesktop.layout.GroupLayout(RNA_Tab);
         RNA_Tab.setLayout(RNA_TabLayout);
         RNA_TabLayout.setHorizontalGroup(
             RNA_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jTabbedPane2)
         );
         RNA_TabLayout.setVerticalGroup(
             RNA_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jTabbedPane2)
         );
 
         DABG_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         dabgchbx.setText("Detection Above Background:");
         dabgchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dabgchbxActionPerformed(evt);
             }
         });
 
         jLabel5.setText("% of Samples");
 
         org.jdesktop.layout.GroupLayout DABG_FilterLayout = new org.jdesktop.layout.GroupLayout(DABG_Filter);
         DABG_Filter.setLayout(DABG_FilterLayout);
         DABG_FilterLayout.setHorizontalGroup(
             DABG_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DABG_FilterLayout.createSequentialGroup()
                 .add(dabgchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(dabgtxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel5)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         DABG_FilterLayout.setVerticalGroup(
             DABG_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DABG_FilterLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel5)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .add(DABG_FilterLayout.createSequentialGroup()
                 .add(DABG_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(dabgchbx)
                     .add(dabgtxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(0, 0, Short.MAX_VALUE))
         );
 
         Herit_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         heritFiltertxt.setText("0.33");
 
         heritFilterchbx.setText("Heritability >=");
         heritFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 heritFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Herit_FilterLayout = new org.jdesktop.layout.GroupLayout(Herit_Filter);
         Herit_Filter.setLayout(Herit_FilterLayout);
         Herit_FilterLayout.setHorizontalGroup(
             Herit_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_FilterLayout.createSequentialGroup()
                 .add(heritFilterchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(heritFiltertxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
         );
         Herit_FilterLayout.setVerticalGroup(
             Herit_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_FilterLayout.createSequentialGroup()
                 .add(Herit_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(heritFilterchbx)
                     .add(heritFiltertxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(0, 2, Short.MAX_VALUE))
         );
 
         Exp_Disp3.setBackground(new java.awt.Color(255, 255, 255));
 
         ExpMarkPntschbx.setSelected(true);
         ExpMarkPntschbx.setText("Mark Points");
         ExpMarkPntschbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ExpMarkPntschbxActionPerformed(evt);
             }
         });
 
         ExpStdErrochbx.setText("Display Std Error Bars");
         ExpStdErrochbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ExpStdErrochbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Exp_Disp3Layout = new org.jdesktop.layout.GroupLayout(Exp_Disp3);
         Exp_Disp3.setLayout(Exp_Disp3Layout);
         Exp_Disp3Layout.setHorizontalGroup(
             Exp_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_Disp3Layout.createSequentialGroup()
                 .add(ExpMarkPntschbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(ExpStdErrochbx)
                 .add(0, 361, Short.MAX_VALUE))
         );
         Exp_Disp3Layout.setVerticalGroup(
             Exp_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(ExpMarkPntschbx)
                 .add(ExpStdErrochbx))
         );
 
         ExCor_Disp1.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel6.setText("Tissue:");
 
         exCorcb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Not Yet Available" }));
         exCorcb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exCorcbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout ExCor_Disp1Layout = new org.jdesktop.layout.GroupLayout(ExCor_Disp1);
         ExCor_Disp1.setLayout(ExCor_Disp1Layout);
         ExCor_Disp1Layout.setHorizontalGroup(
             ExCor_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp1Layout.createSequentialGroup()
                 .add(jLabel6)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(exCorcb, 0, 166, Short.MAX_VALUE))
         );
         ExCor_Disp1Layout.setVerticalGroup(
             ExCor_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(jLabel6)
                 .add(exCorcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         ExCor_Disp2.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel8.setText("Gene:");
 
         exCorGenecb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exCorGenecbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout ExCor_Disp2Layout = new org.jdesktop.layout.GroupLayout(ExCor_Disp2);
         ExCor_Disp2.setLayout(ExCor_Disp2Layout);
         ExCor_Disp2Layout.setHorizontalGroup(
             ExCor_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp2Layout.createSequentialGroup()
                 .add(jLabel8)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(exCorGenecb, 0, 126, Short.MAX_VALUE))
         );
         ExCor_Disp2Layout.setVerticalGroup(
             ExCor_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(jLabel8)
                 .add(exCorGenecb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         ExCor_Disp3.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel9.setText("Left Transcript:");
 
         exCorLeftTranscb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exCorLeftTranscbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout ExCor_Disp3Layout = new org.jdesktop.layout.GroupLayout(ExCor_Disp3);
         ExCor_Disp3.setLayout(ExCor_Disp3Layout);
         ExCor_Disp3Layout.setHorizontalGroup(
             ExCor_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp3Layout.createSequentialGroup()
                 .add(jLabel9)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(exCorLeftTranscb, 0, 263, Short.MAX_VALUE))
         );
         ExCor_Disp3Layout.setVerticalGroup(
             ExCor_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(jLabel9)
                 .add(exCorLeftTranscb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         ExCor_Disp4.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel10.setText("Right Transcript:");
 
         exCorRightTranscb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None" }));
         exCorRightTranscb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exCorRightTranscbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout ExCor_Disp4Layout = new org.jdesktop.layout.GroupLayout(ExCor_Disp4);
         ExCor_Disp4.setLayout(ExCor_Disp4Layout);
         ExCor_Disp4Layout.setHorizontalGroup(
             ExCor_Disp4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp4Layout.createSequentialGroup()
                 .add(jLabel10)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(exCorRightTranscb, 0, 233, Short.MAX_VALUE))
         );
         ExCor_Disp4Layout.setVerticalGroup(
             ExCor_Disp4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(jLabel10)
                 .add(exCorRightTranscb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         Herit_Disp3.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel11.setText("Display Width:");
 
         jSpinner1.setModel(new SpinnerNumberModel(2,1,5,1));
         jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinner1StateChanged(evt);
             }
         });
 
         jLabel12.setText("Charts Wide");
 
         org.jdesktop.layout.GroupLayout Herit_Disp3Layout = new org.jdesktop.layout.GroupLayout(Herit_Disp3);
         Herit_Disp3.setLayout(Herit_Disp3Layout);
         Herit_Disp3Layout.setHorizontalGroup(
             Herit_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_Disp3Layout.createSequentialGroup()
                 .add(jLabel11)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jSpinner1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel12)
                 .addContainerGap(194, Short.MAX_VALUE))
         );
         Herit_Disp3Layout.setVerticalGroup(
             Herit_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_Disp3Layout.createSequentialGroup()
                 .add(jSpinner1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 0, Short.MAX_VALUE))
             .add(Herit_Disp3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(Herit_Disp3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel11)
                     .add(jLabel12))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         Herit_Disp4.setBackground(new java.awt.Color(255, 255, 255));
 
         heritWidenGraphscb.setText("Widen Graphs(may require scrolling)");
         heritWidenGraphscb.setToolTipText("Makes the graphs wide enough that probeset labels will be displayed, but may require scrolling to see the entire graph.");
         heritWidenGraphscb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 heritWidenGraphscbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Herit_Disp4Layout = new org.jdesktop.layout.GroupLayout(Herit_Disp4);
         Herit_Disp4.setLayout(Herit_Disp4Layout);
         Herit_Disp4Layout.setHorizontalGroup(
             Herit_Disp4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_Disp4Layout.createSequentialGroup()
                 .add(heritWidenGraphscb)
                 .add(0, 113, Short.MAX_VALUE))
         );
         Herit_Disp4Layout.setVerticalGroup(
             Herit_Disp4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(heritWidenGraphscb)
         );
 
         ExCor_Disp5.setBackground(new java.awt.Color(255, 255, 255));
 
         ExCorLinkedSBchbx.setSelected(true);
         ExCorLinkedSBchbx.setText("Link Scroll Bars(Left and Right)");
         ExCorLinkedSBchbx.setToolTipText("Sometimes genes differ in size enough that you need to unlink the scroll bars.");
         ExCorLinkedSBchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ExCorLinkedSBchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout ExCor_Disp5Layout = new org.jdesktop.layout.GroupLayout(ExCor_Disp5);
         ExCor_Disp5.setLayout(ExCor_Disp5Layout);
         ExCor_Disp5Layout.setHorizontalGroup(
             ExCor_Disp5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCor_Disp5Layout.createSequentialGroup()
                 .add(ExCorLinkedSBchbx)
                 .add(0, 27, Short.MAX_VALUE))
         );
         ExCor_Disp5Layout.setVerticalGroup(
             ExCor_Disp5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(ExCorLinkedSBchbx)
         );
 
         DE_valueToDispcb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mean Exp Value", "Fold Change", "P-Value", "FDR" }));
         DE_valueToDispcb.setEnabled(false);
 
         setMaximumSize(new java.awt.Dimension(1000, 1200));
         setPreferredSize(new java.awt.Dimension(1000, 1200));
 
         jPanel12.setBackground(new java.awt.Color(255, 255, 255));
         jPanel12.setSize(new java.awt.Dimension(1000, 100));
 
         FilterOptPanel.setBackground(new java.awt.Color(255, 255, 255));
         FilterOptPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter Options", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
         FilterOptPanel.setLayout(new java.awt.BorderLayout());
 
         fullLenFilterPanel.setLayout(new java.awt.GridLayout(1, 1));
 
         Transcript_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         transcriptFiltercb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 transcriptFiltercbActionPerformed(evt);
             }
         });
 
         transcriptFilterchbx.setText("Transcript:");
         transcriptFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 transcriptFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Transcript_FilterLayout = new org.jdesktop.layout.GroupLayout(Transcript_Filter);
         Transcript_Filter.setLayout(Transcript_FilterLayout);
         Transcript_FilterLayout.setHorizontalGroup(
             Transcript_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Transcript_FilterLayout.createSequentialGroup()
                 .add(transcriptFilterchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(transcriptFiltercb, 0, 444, Short.MAX_VALUE))
         );
         Transcript_FilterLayout.setVerticalGroup(
             Transcript_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Transcript_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(transcriptFilterchbx)
                 .add(transcriptFiltercb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         fullLenFilterPanel.add(Transcript_Filter);
 
         FilterOptPanel.add(fullLenFilterPanel, java.awt.BorderLayout.NORTH);
 
         filterGridPanel.setBackground(new java.awt.Color(255, 255, 255));
         filterGridPanel.setLayout(new java.awt.GridLayout(4, 2));
 
         DE_Filter1.setBackground(new java.awt.Color(255, 255, 255));
 
         fdcb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ">=", "=", "<=" }));
         fdcb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fdcbActionPerformed(evt);
             }
         });
 
         foldDiffSigncb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+/-", "+", "-" }));
 
         foldDiffchbx.setText("Fold Diff.");
         foldDiffchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 foldDiffchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout DE_Filter1Layout = new org.jdesktop.layout.GroupLayout(DE_Filter1);
         DE_Filter1.setLayout(DE_Filter1Layout);
         DE_Filter1Layout.setHorizontalGroup(
             DE_Filter1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Filter1Layout.createSequentialGroup()
                 .add(foldDiffchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fdcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(foldDiffSigncb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(foldDifftxt))
         );
         DE_Filter1Layout.setVerticalGroup(
             DE_Filter1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Filter1Layout.createSequentialGroup()
                 .add(DE_Filter1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(foldDiffchbx)
                     .add(fdcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(foldDifftxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(foldDiffSigncb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(0, 4, Short.MAX_VALUE))
         );
 
         filterGridPanel.add(DE_Filter1);
 
         DE_Filter2.setBackground(new java.awt.Color(255, 255, 255));
 
         pvalcb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<=", "=", ">=", " " }));
         pvalcb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pvalcbActionPerformed(evt);
             }
         });
 
         pvalFilterchbx.setText("Pvalue:");
         pvalFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pvalFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout DE_Filter2Layout = new org.jdesktop.layout.GroupLayout(DE_Filter2);
         DE_Filter2.setLayout(DE_Filter2Layout);
         DE_Filter2Layout.setHorizontalGroup(
             DE_Filter2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Filter2Layout.createSequentialGroup()
                 .add(pvalFilterchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(pvalcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(pvaltxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
         );
         DE_Filter2Layout.setVerticalGroup(
             DE_Filter2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Filter2Layout.createSequentialGroup()
                 .add(DE_Filter2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(pvalFilterchbx)
                     .add(pvalcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(pvaltxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(0, 4, Short.MAX_VALUE))
         );
 
         filterGridPanel.add(DE_Filter2);
 
         DE_Filter3.setBackground(new java.awt.Color(255, 255, 255));
 
         fdrcb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<=", "=", ">=", " " }));
         fdrcb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fdrcbActionPerformed(evt);
             }
         });
 
         fdrFilterchbx.setText("False Discovery Rate");
         fdrFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fdrFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout DE_Filter3Layout = new org.jdesktop.layout.GroupLayout(DE_Filter3);
         DE_Filter3.setLayout(DE_Filter3Layout);
         DE_Filter3Layout.setHorizontalGroup(
             DE_Filter3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Filter3Layout.createSequentialGroup()
                 .add(fdrFilterchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fdrcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fdrtxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
         );
         DE_Filter3Layout.setVerticalGroup(
             DE_Filter3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Filter3Layout.createSequentialGroup()
                 .add(DE_Filter3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(fdrFilterchbx)
                     .add(fdrcb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(fdrtxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(0, 4, Short.MAX_VALUE))
         );
 
         filterGridPanel.add(DE_Filter3);
 
         Probeset_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         psFilterchbx.setText("Probeset Name:");
         psFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 psFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Probeset_FilterLayout = new org.jdesktop.layout.GroupLayout(Probeset_Filter);
         Probeset_Filter.setLayout(Probeset_FilterLayout);
         Probeset_FilterLayout.setHorizontalGroup(
             Probeset_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Probeset_FilterLayout.createSequentialGroup()
                 .add(psFilterchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(psNametxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
         );
         Probeset_FilterLayout.setVerticalGroup(
             Probeset_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Probeset_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(psFilterchbx)
                 .add(psNametxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         filterGridPanel.add(Probeset_Filter);
 
         Intron_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         intronFilterchbx.setText("Include Probesets in Introns");
         intronFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 intronFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Intron_FilterLayout = new org.jdesktop.layout.GroupLayout(Intron_Filter);
         Intron_Filter.setLayout(Intron_FilterLayout);
         Intron_FilterLayout.setHorizontalGroup(
             Intron_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, Intron_FilterLayout.createSequentialGroup()
                 .add(0, 0, Short.MAX_VALUE)
                 .add(intronFilterchbx, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 281, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
         Intron_FilterLayout.setVerticalGroup(
             Intron_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(intronFilterchbx)
         );
 
         filterGridPanel.add(Intron_Filter);
 
         OpStrand_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         oppStrandFilterchbx.setText("Include Probesets from opposite strand");
         oppStrandFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 oppStrandFilterchbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout OpStrand_FilterLayout = new org.jdesktop.layout.GroupLayout(OpStrand_Filter);
         OpStrand_Filter.setLayout(OpStrand_FilterLayout);
         OpStrand_FilterLayout.setHorizontalGroup(
             OpStrand_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(OpStrand_FilterLayout.createSequentialGroup()
                 .add(oppStrandFilterchbx)
                 .add(0, 0, Short.MAX_VALUE))
         );
         OpStrand_FilterLayout.setVerticalGroup(
             OpStrand_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(oppStrandFilterchbx)
         );
 
         filterGridPanel.add(OpStrand_Filter);
 
         Annot_Filter.setBackground(new java.awt.Color(255, 255, 255));
 
         annotFilterchbx.setText("Annotation Level:");
         annotFilterchbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 annotFilterchbxActionPerformed(evt);
             }
         });
 
         annotFiltercb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "core", "extended", "full" }));
         annotFiltercb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 annotFiltercbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Annot_FilterLayout = new org.jdesktop.layout.GroupLayout(Annot_Filter);
         Annot_Filter.setLayout(Annot_FilterLayout);
         Annot_FilterLayout.setHorizontalGroup(
             Annot_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Annot_FilterLayout.createSequentialGroup()
                 .add(annotFilterchbx)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(annotFiltercb, 0, 121, Short.MAX_VALUE)
                 .addContainerGap())
         );
         Annot_FilterLayout.setVerticalGroup(
             Annot_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Annot_FilterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(annotFilterchbx)
                 .add(annotFiltercb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         filterGridPanel.add(Annot_Filter);
 
         FilterOptPanel.add(filterGridPanel, java.awt.BorderLayout.CENTER);
 
         DisplayOptPanel.setBackground(new java.awt.Color(255, 255, 255));
         DisplayOptPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Display Options", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
         DisplayOptPanel.setLayout(new java.awt.GridLayout(4, 1));
 
         DE_Disp1.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel7.setText("Display:");
 
         DE_Disp1_rbgroup.add(jRadioButton1);
         jRadioButton1.setSelected(true);
         jRadioButton1.setText("Mean Values");
         jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jRadioButton1ActionPerformed(evt);
             }
         });
 
         DE_Disp1_rbgroup.add(jRadioButton2);
         jRadioButton2.setText("Fold Difference");
         jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jRadioButton2ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout DE_Disp1Layout = new org.jdesktop.layout.GroupLayout(DE_Disp1);
         DE_Disp1.setLayout(DE_Disp1Layout);
         DE_Disp1Layout.setHorizontalGroup(
             DE_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Disp1Layout.createSequentialGroup()
                 .add(jLabel7)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jRadioButton1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jRadioButton2)
                 .addContainerGap(81, Short.MAX_VALUE))
         );
         DE_Disp1Layout.setVerticalGroup(
             DE_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Disp1Layout.createSequentialGroup()
                 .add(DE_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel7)
                     .add(jRadioButton1)
                     .add(jRadioButton2))
                 .add(0, 17, Short.MAX_VALUE))
         );
 
         DisplayOptPanel.add(DE_Disp1);
 
         DE_Disp2.setBackground(new java.awt.Color(255, 255, 255));
 
         jCheckBox1.setText("Display values on Heat Map");
         jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox1ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout DE_Disp2Layout = new org.jdesktop.layout.GroupLayout(DE_Disp2);
         DE_Disp2.setLayout(DE_Disp2Layout);
         DE_Disp2Layout.setHorizontalGroup(
             DE_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Disp2Layout.createSequentialGroup()
                 .add(jCheckBox1)
                 .add(0, 176, Short.MAX_VALUE))
         );
         DE_Disp2Layout.setVerticalGroup(
             DE_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(DE_Disp2Layout.createSequentialGroup()
                 .add(jCheckBox1)
                 .add(0, 17, Short.MAX_VALUE))
         );
 
         DisplayOptPanel.add(DE_Disp2);
 
         Herit_Disp1.setBackground(new java.awt.Color(255, 255, 255));
 
         jLabel1.setText("Display Probesets in:");
 
         buttonGroup1.add(heritIndivrb);
         heritIndivrb.setText("Individual Graphs");
         heritIndivrb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 heritIndivrbActionPerformed(evt);
             }
         });
 
         buttonGroup1.add(heritSinglerb);
         heritSinglerb.setText("A Single Graph");
         heritSinglerb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 heritSinglerbActionPerformed(evt);
             }
         });
 
         buttonGroup1.add(heritTissuerb);
         heritTissuerb.setSelected(true);
         heritTissuerb.setText("By Tissue");
         heritTissuerb.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 heritTissuerbActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Herit_Disp1Layout = new org.jdesktop.layout.GroupLayout(Herit_Disp1);
         Herit_Disp1.setLayout(Herit_Disp1Layout);
         Herit_Disp1Layout.setHorizontalGroup(
             Herit_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_Disp1Layout.createSequentialGroup()
                 .add(jLabel1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(heritTissuerb)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(heritIndivrb)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(heritSinglerb))
         );
         Herit_Disp1Layout.setVerticalGroup(
             Herit_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, Herit_Disp1Layout.createSequentialGroup()
                 .add(0, 0, Short.MAX_VALUE)
                 .add(Herit_Disp1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(heritIndivrb)
                     .add(heritSinglerb)
                     .add(jLabel1)
                     .add(heritTissuerb))
                 .add(81, 81, 81))
         );
 
         DisplayOptPanel.add(Herit_Disp1);
 
         Herit_Disp2.setBackground(new java.awt.Color(255, 255, 255));
 
         dispHeritValueschbx.setText("Display Heritability Values");
         dispHeritValueschbx.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dispHeritValueschbxActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Herit_Disp2Layout = new org.jdesktop.layout.GroupLayout(Herit_Disp2);
         Herit_Disp2.setLayout(Herit_Disp2Layout);
         Herit_Disp2Layout.setHorizontalGroup(
             Herit_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_Disp2Layout.createSequentialGroup()
                 .add(dispHeritValueschbx)
                 .add(0, 184, Short.MAX_VALUE))
         );
         Herit_Disp2Layout.setVerticalGroup(
             Herit_Disp2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(dispHeritValueschbx)
         );
 
         DisplayOptPanel.add(Herit_Disp2);
 
         jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
         jTabbedPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPane1StateChanged(evt);
             }
         });
 
         DE_Tab.setBackground(new java.awt.Color(255, 255, 255));
         DE_Tab.setLayout(new java.awt.BorderLayout());
 
         DE_loadingpb.setToolTipText("Loading Data...");
         DE_loadingpb.setIndeterminate(true);
 
         DE_loadinglbl.setText("Loading Data...");
 
         org.jdesktop.layout.GroupLayout heatMapGraphicsPanel1Layout = new org.jdesktop.layout.GroupLayout(heatMapGraphicsPanel1);
         heatMapGraphicsPanel1.setLayout(heatMapGraphicsPanel1Layout);
         heatMapGraphicsPanel1Layout.setHorizontalGroup(
             heatMapGraphicsPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(heatMapGraphicsPanel1Layout.createSequentialGroup()
                 .add(heatMapGraphicsPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(heatMapGraphicsPanel1Layout.createSequentialGroup()
                         .add(310, 310, 310)
                         .add(DE_loadingpb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 379, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(heatMapGraphicsPanel1Layout.createSequentialGroup()
                         .add(456, 456, 456)
                         .add(DE_loadinglbl)))
                 .addContainerGap(612, Short.MAX_VALUE))
         );
         heatMapGraphicsPanel1Layout.setVerticalGroup(
             heatMapGraphicsPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(heatMapGraphicsPanel1Layout.createSequentialGroup()
                 .add(70, 70, 70)
                 .add(DE_loadinglbl)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(DE_loadingpb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(2454, Short.MAX_VALUE))
         );
 
         jScrollPane3.setViewportView(heatMapGraphicsPanel1);
 
         DE_Tab.add(jScrollPane3, java.awt.BorderLayout.CENTER);
 
         jPanel4.setBackground(new java.awt.Color(255, 255, 255));
         jPanel4.setBounds(new java.awt.Rectangle(0, 0, 100, 55));
         jPanel4.setPreferredSize(new java.awt.Dimension(973, 55));
 
         jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/Expand_Horiz.png"))); // NOI18N
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/Shrink_Horiz.png"))); // NOI18N
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/Expand_Vert.png"))); // NOI18N
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton6ActionPerformed(evt);
             }
         });
 
         jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/Shrink_Vert.png"))); // NOI18N
         jButton7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton7ActionPerformed(evt);
             }
         });
 
         hmLegendPanel.setBackground(new java.awt.Color(255, 255, 255));
         hmLegendPanel.setLayout(new java.awt.BorderLayout());
 
         parentalExpressionHelpbtn.setBackground(new java.awt.Color(255, 255, 255));
         parentalExpressionHelpbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/help.png"))); // NOI18N
         parentalExpressionHelpbtn.setBorder(null);
         parentalExpressionHelpbtn.setBorderPainted(false);
         parentalExpressionHelpbtn.setContentAreaFilled(false);
         parentalExpressionHelpbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         parentalExpressionHelpbtn.setFocusPainted(false);
         parentalExpressionHelpbtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
         parentalExpressionHelpbtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 parentalExpressionHelpbtnActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jButton7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(hmLegendPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(parentalExpressionHelpbtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(hmLegendPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jPanel4Layout.createSequentialGroup()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(19, Short.MAX_VALUE))
             .add(jPanel4Layout.createSequentialGroup()
                 .add(parentalExpressionHelpbtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 0, Short.MAX_VALUE))
         );
 
         DE_Tab.add(jPanel4, java.awt.BorderLayout.NORTH);
 
         jTabbedPane1.addTab("Parental Expression", DE_Tab);
 
         Herit_Tab.setBackground(new java.awt.Color(255, 255, 255));
 
         heritGridPanel.setBackground(new java.awt.Color(255, 255, 255));
         heritGridPanel.setLayout(new java.awt.GridLayout(1, 0));
         jScrollPane4.setViewportView(heritGridPanel);
 
         Herit_loadinglbl.setText("Loading Data...");
 
         Herit_loadingpb.setToolTipText("Loading Data...");
         Herit_loadingpb.setIndeterminate(true);
 
         panelHeritHelpbtn.setBackground(new java.awt.Color(255, 255, 255));
         panelHeritHelpbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/help.png"))); // NOI18N
         panelHeritHelpbtn.setBorder(null);
         panelHeritHelpbtn.setBorderPainted(false);
         panelHeritHelpbtn.setContentAreaFilled(false);
         panelHeritHelpbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         panelHeritHelpbtn.setFocusPainted(false);
         panelHeritHelpbtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
         panelHeritHelpbtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 panelHeritHelpbtnActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout Herit_TabLayout = new org.jdesktop.layout.GroupLayout(Herit_Tab);
         Herit_Tab.setLayout(Herit_TabLayout);
         Herit_TabLayout.setHorizontalGroup(
             Herit_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane4)
             .add(Herit_TabLayout.createSequentialGroup()
                 .add(239, 239, 239)
                 .add(Herit_loadingpb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 486, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(242, Short.MAX_VALUE))
             .add(Herit_TabLayout.createSequentialGroup()
                 .add(426, 426, 426)
                 .add(Herit_loadinglbl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(panelHeritHelpbtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
         Herit_TabLayout.setVerticalGroup(
             Herit_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Herit_TabLayout.createSequentialGroup()
                 .add(Herit_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(Herit_TabLayout.createSequentialGroup()
                         .add(12, 12, 12)
                         .add(Herit_loadinglbl))
                     .add(panelHeritHelpbtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(12, 12, 12)
                 .add(Herit_loadingpb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 886, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Panel Heritability", Herit_Tab);
 
         Exp_Tab.setBackground(new java.awt.Color(255, 255, 255));
 
         Exp_loadinglbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         Exp_loadinglbl.setText("Loading Data...This process may take a few extra minutes to complete.");
 
         Exp_loadingpb.setToolTipText("Loading Data...");
         Exp_loadingpb.setIndeterminate(true);
 
         expGridPanel.setBackground(new java.awt.Color(255, 255, 255));
         expGridPanel.setLayout(new java.awt.GridLayout(1, 0));
 
         panelExpressionHelpbtn.setBackground(new java.awt.Color(255, 255, 255));
         panelExpressionHelpbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/help.png"))); // NOI18N
         panelExpressionHelpbtn.setBorder(null);
         panelExpressionHelpbtn.setBorderPainted(false);
         panelExpressionHelpbtn.setContentAreaFilled(false);
         panelExpressionHelpbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         panelExpressionHelpbtn.setFocusPainted(false);
         panelExpressionHelpbtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
         panelExpressionHelpbtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 panelExpressionHelpbtnActionPerformed(evt);
             }
         });
 
         errorLbl.setForeground(new java.awt.Color(255, 0, 0));
         errorLbl.setText("Error: Generating data for tissue(s): ");
 
         org.jdesktop.layout.GroupLayout Exp_TabLayout = new org.jdesktop.layout.GroupLayout(Exp_Tab);
         Exp_Tab.setLayout(Exp_TabLayout);
         Exp_TabLayout.setHorizontalGroup(
             Exp_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_TabLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(errorLbl)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 27, Short.MAX_VALUE)
                 .add(Exp_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(Exp_loadingpb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                     .add(Exp_loadinglbl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .add(208, 208, 208)
                 .add(panelExpressionHelpbtn))
             .add(org.jdesktop.layout.GroupLayout.TRAILING, expGridPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         Exp_TabLayout.setVerticalGroup(
             Exp_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(Exp_TabLayout.createSequentialGroup()
                 .add(Exp_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(panelExpressionHelpbtn)
                     .add(Exp_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(Exp_loadinglbl)
                         .add(errorLbl)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(Exp_loadingpb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(expGridPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 904, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jTabbedPane1.addTab("Panel Expression", Exp_Tab);
 
         ExCor_Tab.setBackground(new java.awt.Color(255, 255, 255));
 
         exonCorrHelpbtn.setBackground(new java.awt.Color(255, 255, 255));
         exonCorrHelpbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/help.png"))); // NOI18N
         exonCorrHelpbtn.setBorder(null);
         exonCorrHelpbtn.setBorderPainted(false);
         exonCorrHelpbtn.setContentAreaFilled(false);
         exonCorrHelpbtn.setFocusPainted(false);
         exonCorrHelpbtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
         exonCorrHelpbtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exonCorrHelpbtnActionPerformed(evt);
             }
         });
 
         ExCor_loadinglbl.setText("Exon Correlation Values still being calculated Please Wait...");
 
         ExCor_loadingpb.setIndeterminate(true);
 
         errorLbl1.setForeground(new java.awt.Color(255, 0, 0));
         errorLbl1.setText("Error: Generating data for tissue(s): ");
 
         org.jdesktop.layout.GroupLayout ExCor_TabLayout = new org.jdesktop.layout.GroupLayout(ExCor_Tab);
         ExCor_Tab.setLayout(ExCor_TabLayout);
         ExCor_TabLayout.setHorizontalGroup(
             ExCor_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainExonCorPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, ExCor_TabLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(errorLbl1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 90, Short.MAX_VALUE)
                 .add(ExCor_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                     .add(ExCor_loadingpb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(ExCor_loadinglbl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .add(260, 260, 260)
                 .add(exonCorrHelpbtn))
         );
         ExCor_TabLayout.setVerticalGroup(
             ExCor_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, ExCor_TabLayout.createSequentialGroup()
                 .add(ExCor_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(exonCorrHelpbtn)
                     .add(ExCor_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(ExCor_loadinglbl)
                         .add(errorLbl1)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(ExCor_loadingpb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(mainExonCorPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Panel Exon Correlation", ExCor_Tab);
 
         EQTL_Tab.setBackground(new java.awt.Color(255, 255, 255));
 
         jButton3.setText("Open eQTL Page Again");
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout EQTL_TabLayout = new org.jdesktop.layout.GroupLayout(EQTL_Tab);
         EQTL_Tab.setLayout(EQTL_TabLayout);
         EQTL_TabLayout.setHorizontalGroup(
             EQTL_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(EQTL_TabLayout.createSequentialGroup()
                 .add(446, 446, 446)
                 .add(jButton3)
                 .addContainerGap(335, Short.MAX_VALUE))
         );
         EQTL_TabLayout.setVerticalGroup(
             EQTL_TabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(EQTL_TabLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jButton3)
                 .addContainerGap(917, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("eQTL", EQTL_Tab);
 
         filterDisplayHelpbtn.setBackground(new java.awt.Color(255, 255, 255));
         filterDisplayHelpbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/genecentricviewer/help.png"))); // NOI18N
         filterDisplayHelpbtn.setBorder(null);
         filterDisplayHelpbtn.setBorderPainted(false);
         filterDisplayHelpbtn.setContentAreaFilled(false);
         filterDisplayHelpbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         filterDisplayHelpbtn.setFocusPainted(false);
         filterDisplayHelpbtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
         filterDisplayHelpbtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filterDisplayHelpbtnActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel12Layout.createSequentialGroup()
                 .add(FilterOptPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(DisplayOptPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 394, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(filterDisplayHelpbtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
             .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 988, Short.MAX_VALUE)
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel12Layout.createSequentialGroup()
                 .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                         .add(org.jdesktop.layout.GroupLayout.LEADING, DisplayOptPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                         .add(FilterOptPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(filterDisplayHelpbtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 998, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.getAccessibleContext().setAccessibleName("Heritability");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
         processFilter();
         
     }//GEN-LAST:event_jRadioButton1ActionPerformed
 
     private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
         GridLayout gl=(GridLayout) DisplayOptPanel.getLayout();
         gl.setRows(4);
         if(jTabbedPane1.getSelectedIndex()==0){
             FilterOptPanel.add(fullLenFilterPanel,java.awt.BorderLayout.NORTH);
             filterGridPanel.removeAll();
             filterGridPanel.add(DE_Filter1);
             filterGridPanel.add(DE_Filter2);
             filterGridPanel.add(DE_Filter3);
             filterGridPanel.add(Probeset_Filter);
             filterGridPanel.add(Intron_Filter);
             filterGridPanel.add(OpStrand_Filter);
             filterGridPanel.add(Annot_Filter);
             filterGridPanel.repaint();
             filterGridPanel.revalidate();
             DisplayOptPanel.removeAll();
             DisplayOptPanel.add(DE_Disp1);
             DisplayOptPanel.add(DE_Disp2);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
             
         }else if(jTabbedPane1.getSelectedIndex()==1){
             FilterOptPanel.add(fullLenFilterPanel,java.awt.BorderLayout.NORTH);
             filterGridPanel.removeAll();
             filterGridPanel.add(Herit_Filter);
             filterGridPanel.add(DABG_Filter);
             filterGridPanel.add(Probeset_Filter);
             filterGridPanel.add(Intron_Filter);
             filterGridPanel.add(OpStrand_Filter);
             filterGridPanel.add(Annot_Filter);
             filterGridPanel.repaint();
             filterGridPanel.revalidate();
             DisplayOptPanel.removeAll();
             DisplayOptPanel.add(Herit_Disp1);
             DisplayOptPanel.add(Herit_Disp2);
             DisplayOptPanel.add(Herit_Disp3);
             DisplayOptPanel.add(Herit_Disp4);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
             
         }else if(jTabbedPane1.getSelectedIndex()==2){
             FilterOptPanel.add(fullLenFilterPanel,java.awt.BorderLayout.NORTH);
             filterGridPanel.removeAll();
             filterGridPanel.add(Herit_Filter);
             filterGridPanel.add(DABG_Filter);
             filterGridPanel.add(Probeset_Filter);
             filterGridPanel.add(Intron_Filter);
             filterGridPanel.add(OpStrand_Filter);
             filterGridPanel.add(Annot_Filter);
             filterGridPanel.repaint();
             filterGridPanel.revalidate();
             DisplayOptPanel.removeAll();
             DisplayOptPanel.add(Exp_Disp1);
             DisplayOptPanel.add(Exp_Disp2);
             DisplayOptPanel.add(Exp_Disp3);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
             
         }else if(jTabbedPane1.getSelectedIndex()==3){
             FilterOptPanel.remove(fullLenFilterPanel);
             filterGridPanel.removeAll();
             filterGridPanel.add(Herit_Filter);
             filterGridPanel.add(DABG_Filter);
             filterGridPanel.add(Intron_Filter);
             filterGridPanel.add(OpStrand_Filter);
             filterGridPanel.add(Annot_Filter);
             filterGridPanel.repaint();
             filterGridPanel.revalidate();
             DisplayOptPanel.removeAll();
             gl.setRows(5);
             DisplayOptPanel.add(ExCor_Disp1);
             DisplayOptPanel.add(ExCor_Disp2);
             DisplayOptPanel.add(ExCor_Disp3);
             DisplayOptPanel.add(ExCor_Disp4);
             DisplayOptPanel.add(ExCor_Disp5);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
         }else if(jTabbedPane1.getSelectedIndex()==4){
             filterGridPanel.removeAll();
             filterGridPanel.repaint();
             filterGridPanel.revalidate();
             DisplayOptPanel.removeAll();
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
             try{
                 JSObject window = (JSObject) JSObject.getWindow(this);
                 window.call("openeQTL", new Object[] {})   ;
             }catch(JSException e){
                 e.printStackTrace(System.err);
             }
         }
         if(loaded[jTabbedPane1.getSelectedIndex()]){
                 this.processFilter();
         }
         //this.processFilter();
     }//GEN-LAST:event_jTabbedPane1StateChanged
 
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("openeQTL", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
     }//GEN-LAST:event_jButton3ActionPerformed
 
     private void transcriptFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transcriptFilterchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_transcriptFilterchbxActionPerformed
 
     private void psFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_psFilterchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_psFilterchbxActionPerformed
 
     private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
         heatMapGraphicsPanel1.setDisplayNum(jCheckBox1.isSelected());
         jScrollPane3.repaint();
         jScrollPane3.revalidate();
     }//GEN-LAST:event_jCheckBox1ActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         curDimX=(int) (curDimX*1.2);
         updateDim();
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         curDimX=(int) (curDimX*0.8);
         if(curDimX<10){
             curDimX=10;
         }
         updateDim();
     }//GEN-LAST:event_jButton5ActionPerformed
 
     private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         curDimY=(int) (curDimY*1.2);
         updateDim();
     }//GEN-LAST:event_jButton6ActionPerformed
 
     private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
         curDimY=(int) (curDimY*0.8);
         if(curDimY<5){
             curDimY=5;
         }
         updateDim();
     }//GEN-LAST:event_jButton7ActionPerformed
 
     private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
         processFilter();
         
     }//GEN-LAST:event_jRadioButton2ActionPerformed
 
     private void intronFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intronFilterchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_intronFilterchbxActionPerformed
 
     private void oppStrandFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oppStrandFilterchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_oppStrandFilterchbxActionPerformed
 
     private void annotFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotFilterchbxActionPerformed
             processFilter();
             
     }//GEN-LAST:event_annotFilterchbxActionPerformed
 
     private void annotFiltercbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotFiltercbActionPerformed
         if(annotFilterchbx.isSelected()){
             processFilter();
             
         }
     }//GEN-LAST:event_annotFiltercbActionPerformed
 
     private void transcriptFiltercbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transcriptFiltercbActionPerformed
         if(this.transcriptFilterchbx.isSelected()){
             processFilter();
             
         }
     }//GEN-LAST:event_transcriptFiltercbActionPerformed
 
     private void foldDiffchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foldDiffchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_foldDiffchbxActionPerformed
 
     private void pvalFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pvalFilterchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_pvalFilterchbxActionPerformed
 
     private void fdcbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fdcbActionPerformed
         if(foldDiffchbx.isSelected()){
             processFilter();
             
         }
         
     }//GEN-LAST:event_fdcbActionPerformed
 
     private void pvalcbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pvalcbActionPerformed
         if(this.pvalFilterchbx.isSelected()){
             processFilter();
             
         }
     }//GEN-LAST:event_pvalcbActionPerformed
 
     private void fdrFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fdrFilterchbxActionPerformed
         processFilter();
         
     }//GEN-LAST:event_fdrFilterchbxActionPerformed
 
     private void fdrcbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fdrcbActionPerformed
         if(fdrFilterchbx.isSelected()){
             processFilter();
             
         }
     }//GEN-LAST:event_fdrcbActionPerformed
 
     private void heritFilterchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heritFilterchbxActionPerformed
         processFilter();
     }//GEN-LAST:event_heritFilterchbxActionPerformed
 
     private void heritIndivrbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heritIndivrbActionPerformed
         if(heritIndivrb.isSelected()){
             DisplayOptPanel.remove(Herit_Disp4);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
         }
         this.updateHerit();
     }//GEN-LAST:event_heritIndivrbActionPerformed
 
     private void heritSinglerbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heritSinglerbActionPerformed
         this.updateHerit();
     }//GEN-LAST:event_heritSinglerbActionPerformed
 
     private void dispHeritValueschbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dispHeritValueschbxActionPerformed
         this.updateHerit();
     }//GEN-LAST:event_dispHeritValueschbxActionPerformed
 
     private void heritTissuerbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heritTissuerbActionPerformed
         this.updateHerit();
     }//GEN-LAST:event_heritTissuerbActionPerformed
 
     private void Exp_probesetrbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Exp_probesetrbActionPerformed
         this.updateExpr();
     }//GEN-LAST:event_Exp_probesetrbActionPerformed
 
     private void Exp_strainrbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Exp_strainrbActionPerformed
         this.updateExpr();
     }//GEN-LAST:event_Exp_strainrbActionPerformed
 
     private void Exp_indivrbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Exp_indivrbActionPerformed
         if(Exp_indivrb.isSelected()){
             DisplayOptPanel.remove(Exp_Disp1);
             DisplayOptPanel.add(Exp_Disp3);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
         }
         this.updateExpr();
     }//GEN-LAST:event_Exp_indivrbActionPerformed
 
     private void Exp_allrbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Exp_allrbActionPerformed
         if(Exp_allrb.isSelected()){
             DisplayOptPanel.remove(Exp_Disp3);
             DisplayOptPanel.add(Exp_Disp1);
             DisplayOptPanel.repaint();
             DisplayOptPanel.revalidate();
         }
         this.updateExpr();
     }//GEN-LAST:event_Exp_allrbActionPerformed
 
     private void dabgchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dabgchbxActionPerformed
         this.processFilter();
     }//GEN-LAST:event_dabgchbxActionPerformed
 
     private void ExpMarkPntschbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExpMarkPntschbxActionPerformed
         this.updateExpr();
     }//GEN-LAST:event_ExpMarkPntschbxActionPerformed
 
     private void ExpStdErrochbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExpStdErrochbxActionPerformed
         this.updateExpr();
     }//GEN-LAST:event_ExpStdErrochbxActionPerformed
 
     private void exCorcbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exCorcbActionPerformed
         //load Exon Cor
         //mainExonCorPanel1.setLoadingLblText("Loading Data...");
         //mainExonCorPanel1.setProgressBarVisible(true);
         if(exCorcb.getSelectedIndex()>-1&&!exCorcb.getSelectedItem().toString().equals("Not Yet Available")){
             String tissue=exCorcb.getSelectedItem().toString();
             String tissueNoSpaces=tissue.replaceAll(" ", "_");
             String url=this.generalURL+tissueNoSpaces+"_exonCorHeatMap.txt";
             this.ExCor_loadinglbl.setText("Loading Data...");
             this.ExCor_loadinglbl.setVisible(true);
             this.ExCor_loadingpb.setVisible(true);
             fhmd=new ExCorFullHeatMapData();
             fhmd.readFromFile(url);
             String imageURL="";
             int geneIndex=exCorGenecb.getSelectedIndex();
             String geneID=genes.get(geneIndex).getGeneID();
             String geneSymbol=genes.get(geneIndex).getGeneSymbol();
             //mainExonCorPanel1.setImage();
             if(fhmd!=null){
                 mainExonCorPanel1.setHeatMap(fhmd,geneID,geneSymbol,genes.get(geneIndex).getTranscripts());
                 String annotString="full";
                 if(this.annotFilterchbx.isSelected()){
                     annotString=getAnnotationString();
                 }
                 String dabgTxt="0";
                 boolean dabgNA=true;
                 if(this.dabgchbx.isSelected()){
                     dabgTxt=dabgtxt.getText();
                     dabgNA=false;
                 }
                 String heritTxt="0";
                 boolean heritNA=true;
                 if(this.heritFilterchbx.isSelected()){
                     heritTxt=this.heritFiltertxt.getText();
                     heritNA=false;
                 }
                 boolean intron=this.intronFilterchbx.isSelected();
                 boolean opStrand=this.oppStrandFilterchbx.isSelected();
                 mainExonCorPanel1.updateLeftMap(exCorLeftTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
             }
             //mainExonCorPanel1.setup(fhmd,geneID,geneSymbol,imageURL,);
             this.ExCor_loadinglbl.setVisible(false);
             this.ExCor_loadingpb.setVisible(false);
         }else{
             //mainExonCorPanel1.setLoadingLblText("Please select a tissue(Above) to get started");
         }
         
     }//GEN-LAST:event_exCorcbActionPerformed
 
     private void exCorGenecbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exCorGenecbActionPerformed
         int ind=exCorGenecb.getSelectedIndex();
         exCorLeftTranscb.removeAllItems();
         exCorRightTranscb.removeAllItems();
         exCorRightTranscb.addItem("None");
         ArrayList<Transcript> trans=genes.get(ind).getTranscripts();
         mainExonCorPanel1.setSelectedGene(genes.get(ind));
         for(int i=0;i<trans.size();i++){
             exCorLeftTranscb.addItem(trans.get(i).getID());
             exCorRightTranscb.addItem(trans.get(i).getID());
         }
         //change Gene image 
         
         
         String geneID=genes.get(ind).getGeneID();
         
         if(fhmd!=null){
             String url=this.generalURL+"exCor_"+geneID+".png";
             String geneSymbol=genes.get(ind).getGeneSymbol();
             if(exCorcb.getSelectedIndex()>-1){
                 mainExonCorPanel1.setURL(url, geneID+"("+genes.get(ind).getGeneSymbol()+")");
             }
             mainExonCorPanel1.setHeatMap(fhmd,geneID,geneSymbol,genes.get(ind).getTranscripts());
             mainExonCorPanel1.hideEIP();
             String annotString="full";
             if(this.annotFilterchbx.isSelected()){
                 annotString=getAnnotationString();
             }
             String dabgTxt="0";
             boolean dabgNA=true;
             if(this.dabgchbx.isSelected()){
                 dabgTxt=dabgtxt.getText();
                 dabgNA=false;
             }
             String heritTxt="0";
             boolean heritNA=true;
             if(this.heritFilterchbx.isSelected()){
                 heritTxt=this.heritFiltertxt.getText();
                 heritNA=false;
             }
             boolean intron=this.intronFilterchbx.isSelected();
             boolean opStrand=this.oppStrandFilterchbx.isSelected();
             mainExonCorPanel1.updateLeftMap(exCorLeftTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
         }
     }//GEN-LAST:event_exCorGenecbActionPerformed
 
     private void exCorLeftTranscbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exCorLeftTranscbActionPerformed
         String annotString="full";
         if(this.annotFilterchbx.isSelected()){
             annotString=getAnnotationString();
         }
         String dabgTxt="0";
         boolean dabgNA=true;
         if(this.dabgchbx.isSelected()){
             dabgTxt=dabgtxt.getText();
             dabgNA=false;
         }
         String heritTxt="0";
         boolean heritNA=true;
         if(this.heritFilterchbx.isSelected()){
             heritTxt=this.heritFiltertxt.getText();
             heritNA=false;
         }
         boolean intron=this.intronFilterchbx.isSelected();
         boolean opStrand=this.oppStrandFilterchbx.isSelected();
         
         mainExonCorPanel1.updateLeftMap(exCorLeftTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
     }//GEN-LAST:event_exCorLeftTranscbActionPerformed
 
     private void exCorRightTranscbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exCorRightTranscbActionPerformed
         String annotString="full";
         if(this.annotFilterchbx.isSelected()){
             annotString=getAnnotationString();
         }
         String dabgTxt="0";
         boolean dabgNA=true;
         if(this.dabgchbx.isSelected()){
             dabgTxt=dabgtxt.getText();
             dabgNA=false;
         }
         String heritTxt="0";
         boolean heritNA=true;
         if(this.heritFilterchbx.isSelected()){
             heritTxt=this.heritFiltertxt.getText();
             heritNA=false;
         }
         boolean intron=this.intronFilterchbx.isSelected();
         boolean opStrand=this.oppStrandFilterchbx.isSelected();
         mainExonCorPanel1.updateRightMap(exCorRightTranscb.getSelectedIndex(), annotString, dabgTxt, heritTxt, opStrand, dabgNA, heritNA, intron);
         
     }//GEN-LAST:event_exCorRightTranscbActionPerformed
 
     private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
         // TODO add your handling code here:
         updateHerit();
     }//GEN-LAST:event_jSpinner1StateChanged
 
     private void ExCorLinkedSBchbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExCorLinkedSBchbxActionPerformed
         mainExonCorPanel1.setLinkSB(ExCorLinkedSBchbx.isSelected());
     }//GEN-LAST:event_ExCorLinkedSBchbxActionPerformed
 
     private void parentalExpressionHelpbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parentalExpressionHelpbtnActionPerformed
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("openParental", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
     }//GEN-LAST:event_parentalExpressionHelpbtnActionPerformed
 
     private void panelHeritHelpbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panelHeritHelpbtnActionPerformed
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("openHerit", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
     }//GEN-LAST:event_panelHeritHelpbtnActionPerformed
 
     private void filterDisplayHelpbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterDisplayHelpbtnActionPerformed
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("openFilterDisplay", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
     }//GEN-LAST:event_filterDisplayHelpbtnActionPerformed
 
     private void panelExpressionHelpbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panelExpressionHelpbtnActionPerformed
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("openPanelExpression", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
     }//GEN-LAST:event_panelExpressionHelpbtnActionPerformed
 
     private void exonCorrHelpbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exonCorrHelpbtnActionPerformed
         try{
             JSObject window = (JSObject) JSObject.getWindow(this);
             window.call("openExonCorr", new Object[] {})   ;
         }catch(JSException e){
             e.printStackTrace(System.err);
         }
     }//GEN-LAST:event_exonCorrHelpbtnActionPerformed
 
     private void heritWidenGraphscbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heritWidenGraphscbActionPerformed
         updateHerit();
     }//GEN-LAST:event_heritWidenGraphscbActionPerformed
     
     
     public void updateDim(){
         deRowPanel.setCurDim(curDimX, curDimY);
         deColPanel.setCurDim(curDimX, curDimY);
         heatMapGraphicsPanel1.setCurDim(curDimX, curDimY);
         jScrollPane3.repaint();
         jScrollPane3.revalidate();
     }
     
     private boolean existIn(String probeSetID, ArrayList<String> probeList) {
         boolean ret=false;
         for(int i=0;i<probeList.size()&&!ret;i++){
             if(probeList.get(i).equals(probeSetID)){
                 ret=true;
             }
         }
         return ret;
     }
     
     private void processFilter(){
         System.out.println("Sel index:"+jTabbedPane1.getSelectedIndex());
         if(jTabbedPane1.getSelectedIndex()==0){
             filterDE();
             updateDE();
         }else if(jTabbedPane1.getSelectedIndex()==1){
             filterHerit();
             updateHerit();
         }else if(jTabbedPane1.getSelectedIndex()==2){
             filterExpr();
             updateExpr();
         }else if(jTabbedPane1.getSelectedIndex()==3){
             filterUpdateExCor();
         }
     }
     
    private String getAnnotationString(){
        String ret="";
        if(this.annotFiltercb.getSelectedIndex()==0){
             ret="core";
         }else if(annotFiltercb.getSelectedIndex()==1){
             ret="extended";
         }else if(annotFiltercb.getSelectedIndex()==2){
             ret="full";
         }
        return ret;
    }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel Annot_Filter;
     private javax.swing.JPanel DABG_Filter;
     private javax.swing.JPanel DE_Disp1;
     private javax.swing.ButtonGroup DE_Disp1_rbgroup;
     private javax.swing.JPanel DE_Disp2;
     private javax.swing.JPanel DE_Filter1;
     private javax.swing.JPanel DE_Filter2;
     private javax.swing.JPanel DE_Filter3;
     private javax.swing.JPanel DE_Tab;
     private javax.swing.JLabel DE_loadinglbl;
     private javax.swing.JProgressBar DE_loadingpb;
     private javax.swing.JComboBox DE_valueToDispcb;
     private javax.swing.JPanel DisplayOptPanel;
     private javax.swing.JPanel EQTL_Tab;
     private javax.swing.JCheckBox ExCorLinkedSBchbx;
     private javax.swing.JPanel ExCor_Disp1;
     private javax.swing.JPanel ExCor_Disp2;
     private javax.swing.JPanel ExCor_Disp3;
     private javax.swing.JPanel ExCor_Disp4;
     private javax.swing.JPanel ExCor_Disp5;
     private javax.swing.JPanel ExCor_Tab;
     private javax.swing.JLabel ExCor_loadinglbl;
     private javax.swing.JProgressBar ExCor_loadingpb;
     private javax.swing.JCheckBox ExpMarkPntschbx;
     private javax.swing.JCheckBox ExpStdErrochbx;
     private javax.swing.JPanel Exp_Disp1;
     private javax.swing.JPanel Exp_Disp2;
     private javax.swing.JPanel Exp_Disp3;
     private javax.swing.JPanel Exp_Tab;
     private javax.swing.JRadioButton Exp_allrb;
     private javax.swing.JRadioButton Exp_indivrb;
     private javax.swing.JLabel Exp_loadinglbl;
     private javax.swing.JProgressBar Exp_loadingpb;
     private javax.swing.JRadioButton Exp_probesetrb;
     private javax.swing.JRadioButton Exp_strainrb;
     private javax.swing.JPanel FillerPanel1;
     private javax.swing.JPanel FilterOptPanel;
     private javax.swing.JPanel Herit_Disp1;
     private javax.swing.JPanel Herit_Disp2;
     private javax.swing.JPanel Herit_Disp3;
     private javax.swing.JPanel Herit_Disp4;
     private javax.swing.JPanel Herit_Filter;
     private javax.swing.JPanel Herit_Tab;
     private javax.swing.JLabel Herit_loadinglbl;
     private javax.swing.JProgressBar Herit_loadingpb;
     private javax.swing.JPanel Intron_Filter;
     private javax.swing.JPanel OpStrand_Filter;
     private javax.swing.JPanel Probeset_Filter;
     private javax.swing.JPanel RNA_Tab;
     private javax.swing.JPanel Transcript_Filter;
     private javax.swing.JComboBox annotFiltercb;
     private javax.swing.JCheckBox annotFilterchbx;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.ButtonGroup buttonGroup2;
     private javax.swing.ButtonGroup buttonGroup3;
     private javax.swing.JCheckBox dabgchbx;
     private javax.swing.JTextField dabgtxt;
     private javax.swing.JCheckBox dispHeritValueschbx;
     private javax.swing.JLabel errorLbl;
     private javax.swing.JLabel errorLbl1;
     private javax.swing.JComboBox exCorGenecb;
     private javax.swing.JComboBox exCorLeftTranscb;
     private javax.swing.JComboBox exCorRightTranscb;
     private javax.swing.JComboBox exCorcb;
     private javax.swing.JButton exonCorrHelpbtn;
     private javax.swing.JPanel expGridPanel;
     private javax.swing.JComboBox fdcb;
     private javax.swing.JCheckBox fdrFilterchbx;
     private javax.swing.JComboBox fdrcb;
     private javax.swing.JTextField fdrtxt;
     private javax.swing.JButton filterDisplayHelpbtn;
     private javax.swing.JPanel filterGridPanel;
     private javax.swing.JComboBox foldDiffSigncb;
     private javax.swing.JCheckBox foldDiffchbx;
     private javax.swing.JTextField foldDifftxt;
     private javax.swing.JPanel fullLenFilterPanel;
     private edu.ucdenver.ccp.phenogen.applets.graphics.HeatMapGraphicsPanel heatMapGraphicsPanel1;
     private javax.swing.JCheckBox heritFilterchbx;
     private javax.swing.JTextField heritFiltertxt;
     private javax.swing.JPanel heritGridPanel;
     private javax.swing.JRadioButton heritIndivrb;
     private javax.swing.JRadioButton heritSinglerb;
     private javax.swing.JRadioButton heritTissuerb;
     private javax.swing.JCheckBox heritWidenGraphscb;
     private javax.swing.JPanel hmLegendPanel;
     private javax.swing.JCheckBox intronFilterchbx;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton7;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JRadioButton jRadioButton2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSpinner jSpinner1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTabbedPane jTabbedPane2;
     private javax.swing.JTabbedPane jTabbedPane3;
     private javax.swing.JTable jTable1;
     private javax.swing.JTable jTable2;
     private edu.ucdenver.ccp.phenogen.applets.exonviewer.MainExonCorPanel mainExonCorPanel1;
     private javax.swing.JCheckBox oppStrandFilterchbx;
     private javax.swing.JButton panelExpressionHelpbtn;
     private javax.swing.JButton panelHeritHelpbtn;
     private javax.swing.JButton parentalExpressionHelpbtn;
     private javax.swing.JCheckBox psFilterchbx;
     private javax.swing.JTextField psNametxt;
     private javax.swing.JCheckBox pvalFilterchbx;
     private javax.swing.JComboBox pvalcb;
     private javax.swing.JTextField pvaltxt;
     private javax.swing.JComboBox transcriptFiltercb;
     private javax.swing.JCheckBox transcriptFilterchbx;
     private edu.ucdenver.ccp.phenogen.applets.graphics.URLImagePanel uRLImagePanel2;
     // End of variables declaration//GEN-END:variables
 
     DERowLabelPanel deRowPanel=new DERowLabelPanel();
     DEColumnLabelPanel deColPanel=new DEColumnLabelPanel();
     HeatMapLegend deHeatMapLegend=new HeatMapLegend();
     ExCorFullHeatMapData fhmd=null;
 }
