 package net.guha.apps.cdkdesc;
 
 import org.openscience.cdk.fingerprint.*;
 import java.util.BitSet;
 
 import net.guha.apps.cdkdesc.interfaces.ISwingWorker;
 import net.guha.apps.cdkdesc.interfaces.ITextOutput;
 import net.guha.apps.cdkdesc.output.PlainTextOutput;
 import net.guha.apps.cdkdesc.ui.ApplicationMenu;
 import net.guha.apps.cdkdesc.ui.ApplicationUI;
 import net.guha.apps.cdkdesc.ui.DescriptorTree;
 import net.guha.apps.cdkdesc.ui.DescriptorTreeLeaf;
 import net.guha.apps.cdkdesc.ui.ExceptionListDialog;
 import net.guha.apps.cdkdesc.workers.DescriptorSwingWorker;
 import net.guha.apps.cdkdesc.workers.FingerprintSwingWorker;
 import net.guha.ui.checkboxtree.CheckBoxTreeUtils;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.openscience.cdk.CDKConstants;
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
 import org.openscience.cdk.io.iterator.IteratingMDLReader;
 import org.openscience.cdk.io.iterator.IteratingSMILESReader;
 import org.openscience.cdk.qsar.DescriptorEngine;
 import org.openscience.cdk.qsar.DescriptorValue;
 import org.openscience.cdk.qsar.IDescriptor;
 import org.openscience.cdk.qsar.IMolecularDescriptor;
 import org.openscience.cdk.qsar.result.DoubleArrayResult;
 import org.openscience.cdk.qsar.result.DoubleResult;
 import org.openscience.cdk.qsar.result.IDescriptorResult;
 import org.openscience.cdk.qsar.result.IntegerArrayResult;
 import org.openscience.cdk.qsar.result.IntegerResult;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 import java.awt.*;
 import java.awt.dnd.DropTarget;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 
 /**
  * @author Rajarshi Guha
  */
 public class CDKdesc extends JFrame {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
     private ApplicationUI ui;
 
     private JProgressBar progressBar;
     private JLabel statusLabel;
     private JButton goButton;
 
     private ISwingWorker task;
 
 
     private Timer timer;
 
     private File tempFile;
 
     boolean wasCancelled = false;
 
     private DropTarget dropTarget;
 
 
     public CDKdesc() {
         super("CDK Descriptor Calculator");
 
         Calendar cal = new GregorianCalendar();
         long time = cal.getTimeInMillis();
 
         try {
             tempFile = File.createTempFile("cdkdesc", String.valueOf(time));
         } catch (IOException e) {
             e.printStackTrace();
         }
         tempFile.deleteOnExit();
         if (CDKDescUtils.isMacOs()) {
             System.setProperty("apple.laf.useScreenMenuBar", "true");
             System.setProperty("com.apple.mrj.application.apple.menu.about.name", "CDKDescUI");
         }
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (InstantiationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IllegalAccessException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (UnsupportedLookAndFeelException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         UIManager.put("ProgressBar.foreground", new java.awt.Color(156, 154, 206));
         UIManager.put("ProgressBar.background", java.awt.Color.lightGray);
         UIManager.put("Label.foreground", java.awt.Color.black);
 
 
         getContentPane().setLayout(new BorderLayout());
 
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 shutdown();
             }
         });
 
 
         goButton = new JButton("Go");
         goButton.setName("go");
         goButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (ui.descriptorPaneIsSelected()) goApp(e);
                 else goFingerprintApp(e);
             }
         });
 
 
         progressBar = new JProgressBar(0, 1);
         statusLabel = new JLabel("Ready               ");
 
         JPanel statusPanel = new JPanel(new BorderLayout());
         statusPanel.add(statusLabel, BorderLayout.WEST);
         statusPanel.add(progressBar, BorderLayout.EAST);
         Border emptyBorder = new EmptyBorder(4, 2, 4, 2);
         Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
         statusPanel.setBorder(BorderFactory.createCompoundBorder(lowerEtched, emptyBorder));
         progressBar.setVisible(false);
 
         JPanel bottomPanel = new JPanel(new BorderLayout());
         bottomPanel.setBorder(new EmptyBorder(2, 4, 2, 4));
         bottomPanel.add(goButton, BorderLayout.NORTH);
         bottomPanel.add(statusPanel, BorderLayout.SOUTH);
 
         getContentPane().add(bottomPanel, BorderLayout.SOUTH);
 
 
         DescriptorTree descriptorTree = null;
         try {
             descriptorTree = new DescriptorTree(true);
         } catch (CDKException e) {
             System.out.println("e = " + e);
         }
         ui = new ApplicationUI(descriptorTree);
 
 //        JScrollPane scrollPane = new JScrollPane(descriptorTree.getTree(), VERTICAL_SCROLLBAR_AS_NEEDED,
 //                HORIZONTAL_SCROLLBAR_AS_NEEDED);
 //
 //        JTabbedPane tabbedPane = new JTabbedPane();
 //        tabbedPane.add("Descriptors", scrollPane);
 //        tabbedPane.add("Fingerprints", new FingerprintPanel());
 
 //        ui.getSubpanel().add(tabbedPane, BorderLayout.CENTER);
         getContentPane().add(ui.getPanel(), BorderLayout.CENTER);
 
         ApplicationMenu appMenu = new ApplicationMenu(ui);
         setJMenuBar(appMenu.createMenu());
     }
 
 
     private void doSave() {
         if (AppOptions.getInstance().getOutputMethod().equals(CDKDescConstants.OUTPUT_CSV) ||
                 AppOptions.getInstance().getOutputMethod().equals(CDKDescConstants.OUTPUT_SPC) ||
                 AppOptions.getInstance().getOutputMethod().equals(CDKDescConstants.OUTPUT_TAB) ||
                 AppOptions.getInstance().getOutputMethod().equals(CDKDescConstants.OUTPUT_ARFF) ||
                 AppOptions.getInstance().getOutputMethod().equals(CDKDescConstants.OUTPUT_SDF)
                 ) {
             try {
                 FileChannel srcChannel = new FileInputStream(tempFile).getChannel();
                 FileChannel dstChannel = new FileOutputStream(ui.getOutFileTextField().getText()).getChannel();
                 dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                 srcChannel.close();
                 dstChannel.close();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
 
     private void goApp(ActionEvent e) {
 
         if (ui.getSdfFileTextField().getText().equals("") ||
                 ui.getOutFileTextField().getText().equals("")) {
             JOptionPane.showMessageDialog(null, "Must provide an input file and an output file",
                     "CDKDescUI Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
 
         progressBar.setVisible(true);
         progressBar.setStringPainted(true);
         progressBar.setIndeterminate(true);
 
         if (((JButton) e.getSource()).getName().equals("cancel")) {
             task.stop();
             return;
         }
 
         if (((JButton) e.getSource()).getName().equals("go")) {
             goButton.setName("cancel");
             goButton.setText("Cancel");
             wasCancelled = false;
         }
 
 
         DescriptorTree descriptorTree = ui.getDescriptorTree();
         List checkedDescriptors = CheckBoxTreeUtils.getCheckedLeaves(
                 descriptorTree.getCheckTreeManager(), descriptorTree.getTree());
         List<IDescriptor> selectedDescriptors = new ArrayList<IDescriptor>();
         for (Object obj : checkedDescriptors) {
             TreePath procPath = (TreePath) obj;
             DescriptorTreeLeaf aLeaf = (DescriptorTreeLeaf) ((DefaultMutableTreeNode) procPath.getLastPathComponent()).getUserObject();
             selectedDescriptors.add(aLeaf.getInstance());
         }
         Collections.sort(selectedDescriptors, CDKDescUtils.getDescriptorComparator());
 
         if (selectedDescriptors.size() == 0) {
             JOptionPane.showMessageDialog(null,
                     "You need to select one or more descriptors!",
                     "CDKDescUI Error",
                     JOptionPane.ERROR_MESSAGE);
             goButton.setName("go");
             goButton.setText("Go");
             progressBar.setValue(0);
             progressBar.setString("");
             return;
         }
 
 
         task = new DescriptorSwingWorker(selectedDescriptors, ui, progressBar, tempFile);
         if (task.getInputFormat().equals("invalid")) {
             goButton.setName("go");
             goButton.setText("Go");
             progressBar.setValue(0);
             progressBar.setString("");
             return;
         }
 
         final int totaLength = task.getLengthOfTask();
         //progressBar.setMaximum(totaLength);
         //progressBar.setValue(0);
         //progressBar.setString("");
 
 
         timer = new Timer(500, new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 int current = task.getCurrent();
                 //progressBar.setValue(current);
                 //progressBar.setString((int) round(100.0 * current / (double) totaLength, 0) + "%");
                 //goButton.setText("Molecule "+current);
 
                 statusLabel.setText("Mol. " + current);
 
                 if (task.isDone()) {
                     timer.stop();
                     goButton.setName("go");
                     goButton.setText("Go");
                     progressBar.setIndeterminate(false);
                     progressBar.setString("Completed");
                     progressBar.setVisible(false);
                     statusLabel.setText("Completed (" + current + ")");
 
                     doSave();
 
 
                     if (task.getExceptionList().size() > 0) {
                         ExceptionListDialog eld = new ExceptionListDialog(task.getExceptionList());
                         eld.setVisible(true);
                     }
 
                 } else if (task.isCancelled()) {
                     timer.stop();
                     goButton.setName("go");
                     goButton.setText("Go");
                     //progressBar.setValue(0);
                     progressBar.setIndeterminate(false);
                     progressBar.setString("");
                     progressBar.setVisible(false);
                     wasCancelled = true;
                     statusLabel.setText("Cancelled");
                 }
             }
         });
 
         if (wasCancelled) {
             wasCancelled = false;
             return;
         }
 
         task.go();
         timer.start();
     }
 
     private void goFingerprintApp(ActionEvent e) {
 
         if (ui.getSdfFileTextField().getText().equals("") ||
                 ui.getOutFileTextField().getText().equals("")) {
             JOptionPane.showMessageDialog(null, "Must provide an input file and an output file",
                     "CDKDescUI Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
 
         progressBar.setVisible(true);
         progressBar.setStringPainted(true);
         progressBar.setIndeterminate(true);
 
         if (((JButton) e.getSource()).getName().equals("cancel")) {
             task.stop();
             return;
         }
 
         if (((JButton) e.getSource()).getName().equals("go")) {
             goButton.setName("cancel");
             goButton.setText("Cancel");
             wasCancelled = false;
         }
 
         if (AppOptions.getSelectedFingerprintType() == null) {
             JOptionPane.showMessageDialog(null,
                     "You need to select a fingerprint type!",
                     "CDKDescUI Error",
                     JOptionPane.ERROR_MESSAGE);
             goButton.setName("go");
             goButton.setText("Go");
             progressBar.setValue(0);
             progressBar.setString("");
             return;
         }
 
 
         task = new FingerprintSwingWorker(ui, progressBar, tempFile);
         if (task.getInputFormat().equals("invalid")) {
             goButton.setName("go");
             goButton.setText("Go");
             progressBar.setValue(0);
             progressBar.setString("");
             return;
         }
 
         final int totaLength = task.getLengthOfTask();
         //progressBar.setMaximum(totaLength);
         //progressBar.setValue(0);
         //progressBar.setString("");
 
 
         timer = new Timer(500, new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 int current = task.getCurrent();
                 //progressBar.setValue(current);
                 //progressBar.setString((int) round(100.0 * current / (double) totaLength, 0) + "%");
                 //goButton.setText("Molecule "+current);
 
                 statusLabel.setText("Mol. " + current);
 
                 if (task.isDone()) {
                     timer.stop();
                     goButton.setName("go");
                     goButton.setText("Go");
                     progressBar.setIndeterminate(false);
                     progressBar.setString("Completed");
                     progressBar.setVisible(false);
                     statusLabel.setText("Completed (" + current + " in " + task.getElapsedTime() + "s)");
 
                     doSave();
 
 
                     if (task.getExceptionList().size() > 0) {
                         ExceptionListDialog eld = new ExceptionListDialog(task.getExceptionList());
                         eld.setVisible(true);
                     }
 
                 } else if (task.isCancelled()) {
                     timer.stop();
                     goButton.setName("go");
                     goButton.setText("Go");
                     //progressBar.setValue(0);
                     progressBar.setIndeterminate(false);
                     progressBar.setString("");
                     progressBar.setVisible(false);
                     wasCancelled = true;
                     statusLabel.setText("Cancelled");
                 }
             }
         });
 
         if (wasCancelled) {
             wasCancelled = false;
             return;
         }
 
         task.go();
         timer.start();
     }
 
     private void batchFingerprint(String inputFile, String outputFile, String fpType, boolean verbose) throws CDKException {
         if (verbose) {
             System.out.println("INFO: input:\t" + inputFile);
             System.out.println("INFO: output:\t" + outputFile);
             System.out.println("INFO: type:\t" + fpType);
         }
 
 	IFingerprinter fprinter = null;
 	if (fpType.equals("standard")) fprinter = new Fingerprinter();
 	else if (fpType.equals("extended")) fprinter = new ExtendedFingerprinter();
 	else if (fpType.equals("pubchem")) fprinter = new PubchemFingerprinter();
 	else if (fpType.equals("graph")) fprinter = new GraphOnlyFingerprinter();
 	else if (fpType.equals("maccs")) fprinter = new MACCSFingerprinter();
 	else if (fpType.equals("estate")) fprinter = new EStateFingerprinter();
 	else if (fpType.equals("substructure")) fprinter = new SubstructureFingerprinter();
 	else throw new CDKException("Invalid fingerprint type specified");
 
 	DefaultIteratingChemObjectReader iterReader = null;
 	try {
 	    BufferedWriter tmpWriter = new BufferedWriter(new FileWriter(outputFile));
 	    FileInputStream inputStream = new FileInputStream(inputFile);
 
             String lineSep = System.getProperty("line.separator");
             String itemSep = " ";
             String inputFormat = "invalid";
	    if (CDKDescUtils.isSMILESFormat(ui.getSdfFileTextField().getText())) {
 		inputFormat = "smi";
	    } else if (CDKDescUtils.isMDLFormat(ui.getSdfFileTextField().getText())) {
 		inputFormat = "mdl";
 	    }
 
 	    if (inputFormat.equals("smi")) iterReader = new IteratingSMILESReader(inputStream);
 	    else if (inputFormat.equals("mdl")) {
 		iterReader = new IteratingMDLReader(inputStream, DefaultChemObjectBuilder.getInstance());
 	    }
 	    
 	    
 	    int molCount = 0;
 	    
 	    // lets get the header line first
 	    tmpWriter.write("CDKDescUI " + fprinter.getClass().getName() + " " + fprinter.getSize() + " bits" + lineSep);
 	    assert iterReader != null;
 	    double elapsedTime = System.currentTimeMillis();
 
 	    while (iterReader.hasNext()) {  // loop over molecules
 		IMolecule molecule = (IMolecule) iterReader.next();
 		
 		try {
 		    molecule = (IMolecule) CDKDescUtils.checkAndCleanMolecule(molecule);
 		} catch (CDKException e) {
 //		    exceptionList.add(new ExceptionInfo(molCount + 1, molecule, e, ""));
 		    molCount++;
 		    continue;
 		}
 
 		try {
 		    BitSet fingerprint = fprinter.getFingerprint(molecule);
 		    String title = (String) molecule.getProperty(CDKConstants.TITLE);
 		    if (title == null) title = "Mol" + String.valueOf(molCount + 1);
 		    tmpWriter.write(title + itemSep + fingerprint.toString() + lineSep);
 		    tmpWriter.flush();
 		    molCount++;
 		} catch (Exception e) {
 		    //		    exceptionList.add(new ExceptionInfo(molCount + 1, molecule, e, ""));
 		    molCount++;
 		}
 	    }
 
 	    elapsedTime = ((System.currentTimeMillis() - elapsedTime) / 1000.0);
             
 	    iterReader.close();
 	    tmpWriter.close();
     } catch (IOException exception) {
 	exception.printStackTrace();
     }
 }
 
     private void batch(String inputFile, String outputFile, String descriptorType, boolean verbose) {
 
         if (verbose) {
             System.out.println("INFO: input:\t" + inputFile);
             System.out.println("INFO: output:\t" + outputFile);
             System.out.println("INFO: type:\t" + descriptorType);
         }
 
         DescriptorEngine engine = new DescriptorEngine(DescriptorEngine.MOLECULAR);
         List<String> classNames = engine.getDescriptorClassNames();
 
         List<String> validClassNames;
         if (descriptorType.equals("all")) {
             validClassNames = new ArrayList<String>(classNames);
         } else {
             validClassNames = new ArrayList<String>();
             for (String className : classNames) {
                 String[] dictClasses = engine.getDictionaryClass(className);
                 if (dictClasses == null) continue;
                 for (String dictClass : dictClasses) {
                     if (dictClass.indexOf(descriptorType) != -1) validClassNames.add(className);
                 }
             }
         }
 
         if (verbose) System.out.println("INFO: Will evaluate " + validClassNames.size() + " descriptors");
         List<IDescriptor> instances = engine.instantiateDescriptors(validClassNames);
         if (verbose) System.out.println("INFO: Got " + validClassNames.size() + " descriptor instances");
         engine.setDescriptorInstances(instances);
 
         // ok, we've got the desc engine set up, lets check inputs and start the fun
         String inputFormat = "invalid";
         if (CDKDescUtils.isSMILESFormat(inputFile)) inputFormat = "smi";
         else if (CDKDescUtils.isMDLFormat(inputFile)) inputFormat = "mdl";
         else {
             System.out.println("Currently only SMILES of SDF formats are supported");
             System.exit(-1);
         }
 
         DefaultIteratingChemObjectReader iterReader = null;
         BufferedWriter tmpWriter = null;
         try {
             tmpWriter = new BufferedWriter(new FileWriter(outputFile));
 
             FileInputStream inputStream = new FileInputStream(inputFile);
             if (inputFormat.equals("smi")) iterReader = new IteratingSMILESReader(inputStream);
             else if (inputFormat.equals("mdl")) {
                 iterReader = new IteratingMDLReader(inputStream, DefaultChemObjectBuilder.getInstance());
                 iterReader.addChemObjectIOListener(new SDFormatListener());
                 ((IteratingMDLReader) iterReader).customizeJob();
             }
         } catch (IOException exception) {
             System.out.println("ERROR: Error opening input file");
             System.out.println(exception.toString());
             System.exit(-1);
         }
 
         ITextOutput textOutput = new PlainTextOutput(tmpWriter);
         textOutput.setItemSeparator("\t");
 
 
         // lets get the header line first
         List<String> headerItems = new ArrayList<String>();
         headerItems.add("Title");
         for (IDescriptor descriptor : instances) {
             String[] names = descriptor.getDescriptorNames();
             headerItems.addAll(Arrays.asList(names));
         }
 
         try {
             assert textOutput != null;
             textOutput.writeHeader(headerItems.toArray(new String[]{}));
         } catch (IOException e) {
             System.out.println("ERROR: Error writing header line");
             System.out.println(e.toString());
             System.exit(-1);
         }
 
         double elapsedTime = System.currentTimeMillis();
 
         List<ExceptionInfo> exceptionList = new ArrayList<ExceptionInfo>();
         int nmol = 0;
 
         while (iterReader.hasNext()) {  // loop over molecules
             IMolecule molecule = (IMolecule) iterReader.next();
             String title = (String) molecule.getProperty(CDKConstants.TITLE);
             if (title == null) title = "Mol" + String.valueOf(nmol + 1);
 
             try {
                 molecule = (IMolecule) CDKDescUtils.checkAndCleanMolecule(molecule);
             } catch (CDKException e) {
                 exceptionList.add(new ExceptionInfo(nmol + 1, molecule, e, ""));
                 nmol++;
                 continue;
             }
 
             // OK, we can now eval the descriptors
             List<String> dataItems = new ArrayList<String>();
             dataItems.add(title);
 
             int ndesc = 0;
             for (Object object : instances) {
                 IMolecularDescriptor descriptor = (IMolecularDescriptor) object;
                 String[] comps = descriptor.getSpecification().getSpecificationReference().split("#");
 
                 DescriptorValue value = descriptor.calculate(molecule);
                 if (value.getException() != null) {
                     exceptionList.add(new ExceptionInfo(nmol + 1, molecule, value.getException(), comps[1]));
                     for (int i = 0; i < value.getNames().length; i++)
                         dataItems.add("NA");
                     continue;
                 }
 
                 IDescriptorResult result = value.getValue();
                 if (result instanceof DoubleResult) {
                     dataItems.add(String.valueOf(((DoubleResult) result).doubleValue()));
                 } else if (result instanceof IntegerResult) {
                     dataItems.add(String.valueOf(((IntegerResult) result).intValue()));
                 } else if (result instanceof DoubleArrayResult) {
                     for (int i = 0; i < ((DoubleArrayResult) result).length(); i++) {
                         dataItems.add(String.valueOf(((DoubleArrayResult) result).get(i)));
                     }
                 } else if (result instanceof IntegerArrayResult) {
                     for (int i = 0; i < ((IntegerArrayResult) result).length(); i++) {
                         dataItems.add(String.valueOf(((IntegerArrayResult) result).get(i)));
                     }
                 }
 
                 ndesc++;
 
                 if (verbose) {
                     System.out.print("\rINFO: Processed " + ndesc + " descriptors for " + nmol + " molecules");
                     System.out.flush();
                 }
             }
 
             for (int i = 0; i < dataItems.size(); i++) {
                 if (dataItems.get(i).equals("NaN")) dataItems.set(i, "NA");
             }
 
             try {
                 textOutput.writeLine(dataItems.toArray(new String[]{}));
             } catch (IOException e) {
                 System.out.println("\nERROR: Error writing data line");
                 System.out.println(e.toString());
                 System.exit(-1);
             }
 
             nmol++;
         }
 
         // calculation is done, lets eval the elapsed time
         elapsedTime = ((System.currentTimeMillis() - elapsedTime) / 1000.0);
 
         try {
             iterReader.close();
             tmpWriter.close();
         } catch (IOException e) {
             System.out.println("\nERROR: Error closing files");
             System.out.println(e.toString());
             System.exit(-1);
         }
 
         if (verbose) System.out.println("\nINFO: Completed in " + elapsedTime + " sec");
 
         if (exceptionList.size() > 0) {
             System.out.println("=============== Exceptions ===============");
             for (ExceptionInfo ei : exceptionList) {
                 System.out.println(ei.getMolecule().getProperty(CDKConstants.TITLE) + " " + ei.getDescriptorName() + " " + ei.getException());
             }
         }
     }
 
     private void shutdown() {
         System.exit(0);
     }
 
 
     private static void usage(Options options) {
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp("cdkdescui [OPTIONS] inputfile\n", options);
         System.out.println("\nCDKDescUI v" + CDKDescConstants.VERSION + " Rajarshi Guha <rajarshi.guha@gmail.com>\n");
         System.exit(-1);
     }
 
     public static void main(String[] args) throws CDKException {
         String outputFile = "output.txt";
         String inputFile = null;
         String descriptorType = null;
 	String fpType = null;
         boolean batchMode = false;
         boolean verbose = false;
 
         Options options = new Options();
         options.addOption("b", false, "Batch mode");
         options.addOption("h", false, "Help");
         options.addOption("v", false, "Verbose output");
         options.addOption("o", true, "Output file");
         options.addOption("t", true, "Descriptor type: all, topological, geometric, constitutional, electronic, hybrid");
 	options.addOption("f", true, "Fingerprint type: estate, extended, graph, standard, pubchem, substructure");
 
         CommandLineParser parser = new PosixParser();
         try {
             CommandLine cmd = parser.parse(options, args);
             if (cmd.hasOption("h")) usage(options);
             if (cmd.hasOption("b")) {
                 batchMode = true;
                 String[] tmp = cmd.getArgs();
                 if (tmp.length != 1) {
                     System.out.println("Must specify a single input file");
                     usage(options);
                 } else inputFile = tmp[0];
             }
             if (cmd.hasOption("v")) verbose = true;
             if (cmd.hasOption("o")) outputFile = cmd.getOptionValue("o");
 	    if (cmd.hasOption("f")) {
 		fpType = cmd.getOptionValue("f");
 	    }
             if (cmd.hasOption("t")) {
                 descriptorType = cmd.getOptionValue("t");
                 String[] validTypes = new String[]{"all", "topological", "geometric", "protein", "constitutional", "electronic", "hybrid"};
                 boolean typeOk = false;
                 for (String s : validTypes) {
                     if (s.equals(descriptorType)) {
                         typeOk = true;
                         break;
                     }
                 }
                 if (!typeOk) usage(options);
             }
         } catch (ParseException e) {
             System.out.println("Error parsing command line");
             System.exit(-1);
         }
 
 
         CDKdesc app = new CDKdesc();
         if (!batchMode) {
             app.pack();
             app.setVisible(true);
         } else {
	    System.out.println("ready to roll");
 	    if (fpType == null && descriptorType == null) throw new CDKException("One of -t or -f must be specified");
 	    else if (fpType != null && descriptorType != null) throw new CDKException("One of -t or -f must be specified");
             if (descriptorType != null) app.batch(inputFile, outputFile, descriptorType, verbose);
 	    else if (fpType != null) app.batchFingerprint(inputFile, outputFile, fpType, verbose);
         }
     }
 }
