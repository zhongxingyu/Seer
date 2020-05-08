 package net.guha.apps.cdkdesc;
 
 
 import net.guha.apps.cdkdesc.ui.ApplicationUI;
 import org.openscience.cdk.CDKConstants;
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.graph.ConnectivityChecker;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.interfaces.IMoleculeSet;
 import org.openscience.cdk.io.MDLWriter;
 import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;
 import org.openscience.cdk.io.iterator.IteratingMDLReader;
 import org.openscience.cdk.io.iterator.IteratingSMILESReader;
 import org.openscience.cdk.qsar.DescriptorValue;
 import org.openscience.cdk.qsar.IDescriptor;
 import org.openscience.cdk.qsar.IMolecularDescriptor;
 import org.openscience.cdk.qsar.result.*;
 
 import javax.swing.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author Rajarshi Guha
  */
 public class DescriptorSwingWorker {
 
     private ApplicationUI ui;
     private List<IDescriptor> descriptors;
     private List<ExceptionInfo> exceptionList;
 
     private String inputFormat = "mdl";
     private File tempFile;
 
     private int lengthOfTask = 1;
     private int current = 0;
     private int molCount = 0;
     private boolean done = false;
     private boolean canceled = false;
 
 
     public DescriptorSwingWorker(List<IDescriptor> descriptors,
                                  ApplicationUI ui, JProgressBar progressBar, File tempFile) {
         this.descriptors = descriptors;
         this.ui = ui;
         this.tempFile = tempFile;
 
         exceptionList = new ArrayList<ExceptionInfo>();
 
         // see what type of file we have
         inputFormat = "invalid";
         if (CDKDescUtils.isSMILESFormat(ui.getSdfFileTextField().getText())) {
             inputFormat = "smi";
         } else if (CDKDescUtils.isMDLFormat(ui.getSdfFileTextField().getText())) {
             inputFormat = "mdl";
         }
 
         if (inputFormat.equals("invalid")) {
             done = true;
             canceled = true;
            progressBar.setIndeterminate(false);
             JOptionPane.showMessageDialog(null,
                    "Input file format was not recognized. It should be SDF or SMI" +
                            "\nYou should avoid supplying Markush structures",
                     "CDKDescUI Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
         // find out how many molecules we have
 //        try {
 //            numIMolecule = CDKDescUtils.countMolecules(ui.getSdfFileTextField().getText());
 //        } catch (Exception e) {
 //            inputFormat = "invalid";
 //            done = true;
 //            canceled = true;
 //            JOptionPane.showMessageDialog(null,
 //                    "Input file format was not recognized. It should be SDF or SMI",
 //                    "CDKDescUI Error",
 //                    JOptionPane.ERROR_MESSAGE);
 //        }
 
 //        lengthOfTask = descriptors.size() * numIMolecule;
     }
 
     public void go() {
         final SwingWorker worker = new SwingWorker() {
             public Object construct() {
                 current = 0;
                 done = false;
                 canceled = false;
                 return new ActualTask();
             }
         };
         worker.start();
     }
 
     public List<ExceptionInfo> getExceptionList() {
         return exceptionList;
     }
 
     public String getInputFormat() {
         return inputFormat;
     }
 
     public int getLengthOfTask() {
         return lengthOfTask;
     }
 
     public int getCurrent() {
         return molCount;
     }
 
     public void stop() {
         canceled = true;
     }
 
     public boolean isDone() {
         return done;
     }
 
     public boolean isCancelled() {
         return canceled;
     }
 
 
     class ActualTask {
 
         private boolean evalToTextFile(String sdfFileName, String outputFormat) {
             String lineSep = System.getProperty("line.separator");
             String itemSep = " ";
 
             if (outputFormat.equals(CDKDescConstants.OUTPUT_TAB)) {
                 itemSep = "\t";
             } else if (outputFormat.equals(CDKDescConstants.OUTPUT_CSV)) {
                 itemSep = ",";
             } else if (outputFormat.equals(CDKDescConstants.OUTPUT_SPC)) {
                 itemSep = " ";
             }
 
             DefaultIteratingChemObjectReader iterReader = null;
             try {
                 BufferedWriter tmpWriter = new BufferedWriter(new FileWriter(tempFile));
 
                 FileInputStream inputStream = new FileInputStream(sdfFileName);
                 if (inputFormat.equals("smi")) iterReader = new IteratingSMILESReader(inputStream);
                 else if (inputFormat.equals("mdl"))
                     iterReader = new IteratingMDLReader(inputStream, DefaultChemObjectBuilder.getInstance());
 
                 molCount = 0;
 
                 boolean firstTime = true;
                 String headerLine = "";
 
                 while (iterReader.hasNext()) {
                     if (canceled) return false;
                     IMolecule molecule = (IMolecule) iterReader.next();
 
                     if (!ConnectivityChecker.isConnected(molecule)) {
                         // lets see if we have just two parts if so, we assume its a salt and just work
                         // on the larger part. Ideally we should have a check to ensure that the smaller
                         //  part is a metal/halogen etc.
                         IMoleculeSet fragments = ConnectivityChecker.partitionIntoMolecules(molecule);
                         if (fragments.getMoleculeCount() > 2) {
                             exceptionList.add(new ExceptionInfo(molCount, molecule, new CDKException("More than 2 components. Skipped")));
                         } else {
                             IMolecule frag1 = fragments.getMolecule(0);
                             IMolecule frag2 = fragments.getMolecule(1);
                             if (frag1.getAtomCount() > frag2.getAtomCount()) molecule = frag1;
                             else molecule = frag2;
                             exceptionList.add(new ExceptionInfo(molCount, molecule, new CDKException("2 disconnected components. Using the larger one")));
                         }
                         molCount++;
                         continue;
                     }
 
                     StringWriter stringWriter = new StringWriter();
                     for (Object object : descriptors) {
                         if (canceled) return false;
                         IMolecularDescriptor descriptor = (IMolecularDescriptor) object;
                         String[] comps = descriptor.getSpecification().getSpecificationReference().split("#");
 
                         try {
                             DescriptorValue value = descriptor.calculate(molecule);
                             String[] descName = value.getNames();
 
                             IDescriptorResult result = value.getValue();
                             if (result instanceof DoubleResult) {
                                 stringWriter.write(((DoubleResult) result).doubleValue() + itemSep);
                                 if (firstTime) headerLine = headerLine + descName[0] + itemSep;
                             } else if (result instanceof IntegerResult) {
                                 stringWriter.write(((IntegerResult) result).intValue() + itemSep);
                                 if (firstTime) headerLine = headerLine + descName[0] + itemSep;
                             } else if (result instanceof DoubleArrayResult) {
                                 for (int i = 0; i < ((DoubleArrayResult) result).length(); i++) {
                                     stringWriter.write(((DoubleArrayResult) result).get(i) + itemSep);
                                     if (firstTime) headerLine = headerLine + descName[i] + itemSep;
                                 }
                             } else if (result instanceof IntegerArrayResult) {
                                 for (int i = 0; i < ((IntegerArrayResult) result).length(); i++) {
                                     stringWriter.write(((IntegerArrayResult) result).get(i) + itemSep);
                                     if (firstTime) headerLine = headerLine + descName[i] + itemSep;
                                 }
                             }
                             current++;
                         } catch (CDKException e) {
                             exceptionList.add(new ExceptionInfo(molCount, molecule, e));
 
                         }
                     }
 
                     headerLine = headerLine + lineSep;
                     String dataLine = stringWriter.toString() + lineSep;
                     String pattern = itemSep + lineSep;
                     headerLine = headerLine.replace(pattern, lineSep);
                     dataLine = dataLine.replace(pattern, lineSep);
 
                     if (firstTime) {
                         tmpWriter.write("Title" + itemSep + headerLine);
                         firstTime = false;
                     }
 
                     String title = (String) molecule.getProperty(CDKConstants.TITLE);
                     if (title == null) title = String.valueOf(molCount);
                     tmpWriter.write(title + itemSep + dataLine);
                     tmpWriter.flush();
                     molCount++;
                 }
                 iterReader.close();
                 tmpWriter.close();
 
                 done = true;
             } catch (IOException exception) {
                 exception.printStackTrace();
             }
             return true;
         }
 
         private boolean evalToSDF(String sdfFileName) {
             DefaultIteratingChemObjectReader iterReader = null;
             try {
                 MDLWriter tmpWriter = new MDLWriter(new FileWriter(tempFile));
 
                 FileInputStream inputStream = new FileInputStream(sdfFileName);
                 if (inputFormat.equals("smi")) iterReader = new IteratingSMILESReader(inputStream);
                 else if (inputFormat.equals("mdl"))
                     iterReader = new IteratingMDLReader(inputStream, DefaultChemObjectBuilder.getInstance());
 
                 int counter = 1;
 
                 while (iterReader.hasNext()) {
                     if (canceled) return false;
                     IMolecule molecule = (IMolecule) iterReader.next();
                     HashMap<String, Object> map = new HashMap<String, Object>();
                     for (Object object : descriptors) {
                         if (canceled) return false;
                         IMolecularDescriptor descriptor = (IMolecularDescriptor) object;
                         String[] comps = descriptor.getSpecification().getSpecificationReference().split("#");
                         String descName = comps[1];
                         try {
                             DescriptorValue value = descriptor.calculate(molecule);
 
                             IDescriptorResult result = value.getValue();
                             if (result instanceof DoubleResult) {
                                 map.put(descName, ((DoubleResult) result).doubleValue());
                             } else if (result instanceof IntegerResult) {
                                 map.put(descName, ((IntegerResult) result).intValue());
                             } else if (result instanceof DoubleArrayResult) {
                                 for (int i = 0; i < ((DoubleArrayResult) result).length(); i++) {
                                     map.put(descName + "." + i, ((DoubleArrayResult) result).get(i));
                                 }
                             } else if (result instanceof IntegerArrayResult) {
                                 for (int i = 0; i < ((IntegerArrayResult) result).length(); i++)
                                     map.put(descName + "." + i, ((IntegerArrayResult) result).get(i));
                             }
                             current++;
                         } catch (CDKException e) {
                             exceptionList.add(new ExceptionInfo(counter, molecule, e));
                             System.err.println("Molecule " + counter + " failed on " + descriptor.getSpecification().getImplementationIdentifier());
                         }
                     }
                     tmpWriter.setSdFields(map);
                     tmpWriter.write(molecule);
                     counter++;
                 }
                 iterReader.close();
                 tmpWriter.close();
                 done = true;
             } catch (IOException exception) {
                 exception.printStackTrace();
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return true;
         }
 
 
         ActualTask() {
             String outputMethod = AppOptions.getInstance().getOutputMethod();
             String sdfFileName = ui.getSdfFileTextField().getText();
 
             if (outputMethod.equals(CDKDescConstants.OUTPUT_TAB) ||
                     outputMethod.equals(CDKDescConstants.OUTPUT_CSV) ||
                     outputMethod.equals(CDKDescConstants.OUTPUT_SPC)) {
                 boolean status = evalToTextFile(sdfFileName, outputMethod);
                 if (!status) return;
             } else if (outputMethod.equals(CDKDescConstants.OUTPUT_SDF)) {
                 boolean status = evalToSDF(sdfFileName);
                 if (!status) return;
             }
 
         }
     }
 }
 
 
 
