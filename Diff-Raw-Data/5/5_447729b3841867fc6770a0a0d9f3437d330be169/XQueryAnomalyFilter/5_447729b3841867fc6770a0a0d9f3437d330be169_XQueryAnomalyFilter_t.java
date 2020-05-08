 /*
  *  StrangeFind, an anomaly detection system for the OpenDiamond Platform
  *
  *  Copyright (c) 2007-2008 Carnegie Mellon University
  *  All rights reserved.
  *
  *  StrangeFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  StrangeFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with StrangeFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking StrangeFind statically or dynamically with other modules is
  *  making a combined work based on StrangeFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  * 
  *  In addition, as a special exception, the copyright holders of
  *  StrangeFind give you permission to combine StrangeFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for StrangeFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of StrangeFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.snapfind2.search;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.io.*;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.util.Map.Entry;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import edu.cmu.cs.diamond.opendiamond.*;
 import edu.cmu.cs.diamond.snapfind2.Annotator;
 import edu.cmu.cs.diamond.snapfind2.Decorator;
 import edu.cmu.cs.diamond.snapfind2.LogicEngine;
 import edu.cmu.cs.diamond.snapfind2.SnapFindSearch;
 
 public class XQueryAnomalyFilter implements SnapFindSearch {
 
     private boolean negateEasyOp;
 
     public XQueryAnomalyFilter(Component parent) {
         attrMap = new HashMap<String, String>();
 
         // load file
         JFileChooser chooser = new JFileChooser();
         FileNameExtensionFilter filter = new FileNameExtensionFilter(
                 "Attribute Map Files", "attrmap");
         chooser.setFileFilter(filter);
         int returnVal = chooser.showOpenDialog(parent);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File f = chooser.getSelectedFile();
             parseAttrFile(f, attrMap);
         } else {
             // XXX
             throw new RuntimeException("You must choose a file");
         }
 
         // init GUI elements
         checkboxes = new JCheckBox[attrMap.size()];
         stddevs = new JSpinner[attrMap.size()];
         niceLabels = new String[attrMap.size()];
         labels = new String[attrMap.size()];
         queries = new String[attrMap.size()];
 
         {
             int i = 0;
             for (Entry<String, String> e : attrMap.entrySet()) {
                 // convert spaces to underscores, in an attempt to not crash the
                 // lexer?
                 labels[i] = e.getKey().replaceAll("\\t", " ").replaceAll("_",
                         "__").replaceAll(" ", "_");
 
                 niceLabels[i] = e.getKey();
                 queries[i] = e.getValue();
                 i++;
             }
         }
 
         for (int i = 0; i < niceLabels.length; i++) {
             checkboxes[i] = new JCheckBox(niceLabels[i] + " ($" + (i + 1) + ")");
             checkboxes[i].setToolTipText(queries[i]);
             checkboxes[i].setSelected(true);
             checkboxes[i].addChangeListener(new ChangeListener() {
 
                 @Override
                 public void stateChanged(ChangeEvent e) {
                     updateEasyLogicExpression();
                 }
             });
 
             JSpinner s = new JSpinner(
                     new SpinnerNumberModel(3.0, 1.0, 7.0, 0.5));
             stddevs[i] = s;
         }
     }
 
     protected void updateEasyLogicExpression() {
         if (easyOp == "") {
             return; // not easy
         }
 
         JTextArea t = logicalExpressionTextArea;
 
         StringBuilder sb = new StringBuilder();
         if (negateEasyOp) {
             sb.append("NOT(");
         }
 
         sb.append(easyOp + "(");
 
         boolean someSelected = false;
         boolean first = true;
 
         for (int i = 0; i < checkboxes.length; i++) {
             JCheckBox c = checkboxes[i];
             if (c.isSelected()) {
                 someSelected = true;
 
                 if (!first) {
                     sb.append(", ");
                 } else {
                     first = false;
                 }
                 sb.append("$" + (i + 1));
             }
         }
         sb.append(")");
         if (negateEasyOp) {
             sb.append(")");
         }
 
         String text;
         if (someSelected) {
             text = sb.toString();
         } else {
             text = "";
         }
 
         t.setText(text);
     }
 
     static private void parseAttrFile(File f, Map<String, String> attrMap) {
         FileReader fr = null;
         BufferedReader in = null;
         try {
             fr = new FileReader(f);
             in = new BufferedReader(fr);
 
             String line;
             while ((line = in.readLine()) != null) {
                 String tokens[] = line.split(":", 2);
 
                 String key = tokens[0].replaceAll("\\t", " ");
                 attrMap.put(key, tokens[1]);
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (fr != null) {
                 try {
                     fr.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public Annotator getAnnotator() {
         final List<String> selectedLabels = new ArrayList<String>();
         final List<String> niceSelectedLabels = new ArrayList<String>();
         for (int i = 0; i < checkboxes.length; i++) {
             JCheckBox c = checkboxes[i];
             if (c.isSelected()) {
                 selectedLabels.add(labels[i]);
                 niceSelectedLabels.add(niceLabels[i]);
             }
         }
 
         return new Annotator() {
             public String annotate(Result r) {
                 return annotateTooltip(r);
             }
 
             public String annotateNonHTML(Result r) {
                 return annotateTooltip(r, false);
             }
 
             public String annotateTooltip(Result r) {
                 return annotateTooltip(r, true);
             }
 
             private String annotateTooltip(Result r, boolean useHTML) {
 
                 DecimalFormat df = new DecimalFormat("0.###");
                 StringBuilder sb = new StringBuilder();
 
                 if (useHTML) {
                     sb.append("<html>");
                 }
 
                 String server = r.getServerName();
                 String name = getName(r);
                 int samples = getSamples(r, 0);
 
                 for (int i = 0; i < labels.length; i++) {
                     boolean isA = getIsAnomalous(r, i);
 
                     if (useHTML) {
                         sb.append("<p>");
                    } else {
                        sb.append("\n\n");
                     }
                     if (isA) {
                         if (useHTML) {
                             sb.append("<b>");
                         }
                         sb.append("*");
                     }
 
                     String descriptor = niceLabels[i];
                     double stddev = getStddev(r, i);
                     double value = getValue(r, i);
                     double mean = getMean(r, i);
                     double stddevDiff = getStddevDiff(stddev, value, mean);
                     String aboveOrBelow = getAboveOrBelow(stddevDiff, "+", "−");
 
                     sb.append(descriptor);
                     if (isA) {
                         if (useHTML) {
                             sb.append("</b>");
                         }
                     }
                     sb.append(" = " + format(mean, df) + " " + aboveOrBelow
                             + " " + format(Math.abs(stddevDiff), df) + "σ ("
                             + format(value, df) + ")");
                 }
 
                 if (useHTML) {
                     sb.append("<hr><p>" + name + "<p>" + server + " ["
                             + samples + "]</html>");
                 } else {
                    sb.append("\n---\n\n" + name + "\n\n" + server + " ["
                             + samples + "]");
                 }
 
                 return sb.toString();
 
                 //
                 // double stddev = getStddev(r);
                 // String keyValue = selectedLabels.get(key);
                 // String strValue = Util.extractString(r.getValue(keyValue));
                 //
                 // return "<html><p>" + descriptor + "<br>" + "= "
 
                 // DecimalFormat df = new DecimalFormat("0.###");
                 //
                 // int key = getKey(r);
                 // String descriptor = niceSelectedLabels.get(key);
                 //
                 // double stddev = getStddev(r);
                 // String keyValue = selectedLabels.get(key);
                 // String strValue = Util.extractString(r.getValue(keyValue));
                 // System.out.println("key: " + key + ", value: " + keyValue
                 // + ", strValue: " + strValue);
                 // double value = getValue(strValue);
                 // double mean = getMean(r);
                 // double stddevDiff = getStddevDiff(stddev, value, mean);
                 //
                 // String aboveOrBelow = getAboveOrBelow(stddevDiff);
                 //
                 //
                 // return "<html><p><b>" + descriptor + "</b> = "
                 // + df.format(value) + "<p><b>"
                 // + df.format(Math.abs(stddevDiff)) + "</b> stddev <b>"
                 // + aboveOrBelow + "</b> mean of <b>" + df.format(mean)
                 // + "</b><hr><p>" + name + "<p>" + server + " ["
                 // + samples + "]</html>";
             }
 
             private String getAboveOrBelow(double stddevDiff) {
                 return getAboveOrBelow(stddevDiff, "above", "below");
             }
 
             private String getName(Result r) {
                 String name = r.getObjectName();
                 name = name.substring(name.lastIndexOf('/') + 1);
                 return name;
             }
 
             private String getAboveOrBelow(double stddevDiff, String above,
                     String below) {
                 String aboveOrBelow = Math.signum(stddevDiff) >= 0.0 ? above
                         : below;
                 return aboveOrBelow;
             }
 
             private int getSamples(Result r, int descriptor) {
                 int samples = Util.extractInt(r
                         .getValue("anomaly-descriptor-count-" + descriptor
                                 + ".int"));
                 return samples;
             }
 
             private double getValue(Result r, int descriptor) {
                 double value = Util.extractDouble(r
                         .getValue("anomaly-descriptor-value-" + descriptor
                                 + ".double"));
                 return value;
             }
 
             private double getStddevDiff(double stddev, double value,
                     double mean) {
                 double stddevDiff = (value - mean) / stddev;
                 return stddevDiff;
             }
 
             private double getMean(Result r, int descriptor) {
                 double mean = Util.extractDouble(r
                         .getValue("anomaly-descriptor-mean-" + descriptor
                                 + ".double"));
                 return mean;
             }
 
             private double getValue(String strValue) {
                 double value = Double.parseDouble(strValue);
                 return value;
             }
 
             private double getStddev(Result r, int descriptor) {
                 double stddev = Util.extractDouble(r
                         .getValue("anomaly-descriptor-stddev-" + descriptor
                                 + ".double"));
                 return stddev;
             }
 
             private boolean getIsAnomalous(Result r, int descriptor) {
                 int isAnomalous = Util.extractInt(r
                         .getValue("anomaly-descriptor-is_anomalous-"
                                 + descriptor + ".int"));
                 return isAnomalous == 1 ? true : false;
             }
 
             @Override
             public String annotateOneLine(Result r) {
                 StringBuilder sb = new StringBuilder();
 
                 for (int i = 0; i < labels.length; i++) {
                     if (getIsAnomalous(r, i)) {
                         sb.append("$" + (i + 1) + " ");
                     }
                 }
 
                 if (sb.toString().equals("")) {
                     sb.append("none");
                 }
 
                 return "<html><p>Anomalous: " + sb.toString() + "</html>";
             }
 
             @Override
             public String annotateVerbose(Result r) {
                 // TODO do this with XML parser
                 // XXX
                 ByteArrayInputStream in = new ByteArrayInputStream(r.getData());
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(in));
 
                 String line;
                 StringBuilder output = new StringBuilder();
 
                 boolean skip = false;
                 try {
                     while ((line = reader.readLine()) != null) {
                         if (line.equals("<ResultType name=\"Cell\">")) {
                             skip = true;
                         }
 
                         if (skip == true && line.equals("</ResultType>")) {
                             skip = false;
                         }
 
                         if (!skip) {
                             output.append(line);
                             output.append('\n');
                         }
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return output.toString();
             }
         };
     }
 
     protected String format(double d, DecimalFormat df) {
         if (Double.isNaN(d)) {
             return "NaN";
         } else {
             return df.format(d);
         }
     }
 
     public Decorator getDecorator() {
         return null;
     }
 
     private static final DoubleComposer composer = new DoubleComposer() {
         public double compose(String key, double a, double b) {
             return a + b;
         }
     };
 
     public DoubleComposer getDoubleComposer() {
         return composer;
     }
 
     public String[] getApplicationDependencies() {
         return new String[] { "xquery" };
     }
 
     public Filter[] getFilters() {
         Filter xquery = null;
         Filter anom = null;
         try {
             FilterCode c;
 
             c = new FilterCode(new FileInputStream(
                     "/opt/snapfind/lib/fil_xquery.so"));
 
             byte queryBlob[] = generateQueryBlob();
 
             System.out.println("queryBlob: " + new String(queryBlob));
 
             xquery = new Filter("xquery", c, "f_eval_xquery", "f_init_xquery",
                     "f_fini_xquery", 0, new String[0], new String[] {}, 400,
                     queryBlob);
             System.out.println(xquery);
 
             List<String> paramsList = new ArrayList<String>();
             for (int i = 0; i < checkboxes.length; i++) {
                 paramsList.add(labels[i]);
                 paramsList.add(stddevs[i].getValue().toString());
             }
 
             String anomArgs[] = new String[paramsList.size() + 3];
             anomArgs[0] = ignoreSpinner.getValue().toString(); // skip
             anomArgs[1] = UUID.randomUUID().toString(); // random value
             anomArgs[2] = LogicEngine
                     .getMachineCodeForExpression(logicalExpressionTextArea
                             .getText()); // machine code
             System.arraycopy(paramsList.toArray(), 0, anomArgs, 3, paramsList
                     .size());
             c = new FilterCode(new FileInputStream(
                     "/home/agoode/diamond-git/snapfind2/native/fil_anomaly.so"));
             anom = new Filter("anomaly", c, "f_eval_afilter", "f_init_afilter",
                     "f_fini_afilter", 1, new String[] { "xquery" }, anomArgs,
                     400);
             System.out.println(anom);
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return new Filter[] { xquery, anom };
     }
 
     private byte[] generateQueryBlob() {
         StringBuilder sb = new StringBuilder();
 
         sb.append("document { <attributes>");
 
         for (int i = 0; i < labels.length; i++) {
             // TODO(agoode) validate or escape xpath/xquery input
             sb.append("<attribute name='" + labels[i] + "' value='"
                     + queries[i] + "'/>");
         }
 
         sb
                 .append("<attribute name='image-1' value='{//Images/ImageFile[1]/text()}'/>"
                         + "<attribute name='image-2' value='{//Images/ImageFile[2]/text()}'/>"
                         + "<attribute name='image-3' value='{//Images/ImageFile[3]/text()}'/>"
                         + "</attributes>}");
 
         return sb.toString().getBytes();
     }
 
     final private JCheckBox[] checkboxes;
 
     final private JSpinner[] stddevs;
 
     final private JSpinner ignoreSpinner = new JSpinner(new SpinnerNumberModel(
             5, 0, 100, 1));
 
     final private Map<String, String> attrMap;
 
     final private String labels[];
 
     final private String niceLabels[];
 
     final private String queries[];
 
     final private JTextArea logicalExpressionTextArea = new JTextArea();
 
     protected String easyOp;
 
     public JPanel getInterface() {
         // XXX do this another way
         JPanel result = new JPanel();
         result.setBorder(BorderFactory
                 .createTitledBorder("XQuery Anomaly Detector"));
         result.setLayout(new BorderLayout());
 
         Box b = Box.createVerticalBox();
         result.add(b);
 
         JPanel innerResult = new JPanel();
         innerResult.setLayout(new SpringLayout());
 
         innerResult.add(new JLabel("Priming count"));
         innerResult.add(ignoreSpinner);
 
         innerResult.add(new JLabel(" "));
         innerResult.add(new JLabel(" "));
 
         innerResult.add(new JLabel("Descriptor"));
         innerResult.add(new JLabel("Std. dev."));
         for (int i = 0; i < labels.length; i++) {
             innerResult.add(checkboxes[i]);
             innerResult.add(stddevs[i]);
         }
 
         Util.makeCompactGrid(innerResult, labels.length + 3, 2, 5, 5, 2, 2);
 
         b.add(innerResult);
 
         // logic
         ButtonGroup bg = new ButtonGroup();
 
         Box h = Box.createHorizontalBox();
         b.add(h);
 
         JRadioButton cb = new JRadioButton("OR");
         bg.add(cb);
         h.add(cb);
         cb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (((JRadioButton) e.getSource()).isSelected()) {
                     easyOp = "OR";
                     negateEasyOp = false;
                     updateEasyLogicExpression();
                 }
             }
         });
         cb.setSelected(true);
 
         cb = new JRadioButton("AND");
         cb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (((JRadioButton) e.getSource()).isSelected()) {
                     easyOp = "AND";
                     negateEasyOp = false;
                     updateEasyLogicExpression();
                 }
             }
         });
         bg.add(cb);
         h.add(cb);
 
         cb = new JRadioButton("NOT");
         bg.add(cb);
         h.add(cb);
         cb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (((JRadioButton) e.getSource()).isSelected()) {
                     easyOp = "OR";
                     negateEasyOp = true;
                     updateEasyLogicExpression();
                 }
             }
         });
 
         cb = new JRadioButton("Custom:");
         cb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (((JRadioButton) e.getSource()).isSelected()) {
                     easyOp = "";
                     logicalExpressionTextArea.setEditable(true);
                 } else {
                     logicalExpressionTextArea.setEditable(false);
                 }
             }
         });
         bg.add(cb);
         h.add(cb);
 
         logicalExpressionTextArea.setRows(4);
         logicalExpressionTextArea.setLineWrap(true);
         logicalExpressionTextArea.setEditable(false);
         b.add(new JScrollPane(logicalExpressionTextArea));
 
         return result;
     }
 
 }
