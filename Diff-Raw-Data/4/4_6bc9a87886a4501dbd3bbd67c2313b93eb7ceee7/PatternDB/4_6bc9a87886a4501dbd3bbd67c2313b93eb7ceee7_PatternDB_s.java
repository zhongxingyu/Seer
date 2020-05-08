 package org.geworkbench.util.patterns;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
 
 /**
  * <p>Title: Sequence and Pattern Plugin</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @author not attributable
  * @version $Id$
  */
 
 public class PatternDB extends CSAncillaryDataSet<DSSequence> implements Serializable {
 	private static final long serialVersionUID = -902110075425415061L;
 	static Log log = LogFactory.getLog(PatternDB.class);
 
     private List<DSMatchedSeqPattern> patterns = new ArrayList<DSMatchedSeqPattern>();
     private File dataSetFile;
 
     public PatternDB(File _seqFile, DSDataSet<DSSequence> parent) {
         super(parent, "PatternDB");
         dataSetFile = _seqFile;
     }
 
     public boolean read(File _file) {
         try {
             file = new File(_file.getCanonicalPath());
             label = file.getName();
             BufferedReader reader = new BufferedReader(new FileReader(file));
             reader.readLine();
             String s = reader.readLine();
             if (s.startsWith("File:")) {
                 File newFile = new File(s.substring(5));
                 if (!dataSetFile.getName().equalsIgnoreCase(newFile.getName())) {
                     return false;
                 }
                 s = reader.readLine();
             }
             patterns.clear();
             while (s != null) {
                 CSMatchedSeqPattern pattern = new org.geworkbench.util.patterns.CSMatchedSeqPattern(s);
                 patterns.add(pattern);
                 s = reader.readLine();
             }
         } catch (IOException ex) {
             System.out.println("Exception: " + ex);
         }
         return true;
     }
 
     public void write(File file) {
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(file));
             int i = 0;
             Iterator<DSMatchedSeqPattern> it = patterns.iterator();
             String path = this.getDataSetFile().getCanonicalPath();
             writer.write(org.geworkbench.util.AlgorithmSelectionPanel.DISCOVER);
             writer.newLine();
             writer.write("File:" + path);
             writer.newLine();
             while (it.hasNext()) {
                 DSMatchedSeqPattern pattern = (DSMatchedSeqPattern) it.next();
                 writer.write("[" + i++ + "]\t");
                 pattern.write(writer);
             }
             writer.flush();
             writer.close();
         } catch (IOException ex) {
             System.out.println("Exception: " + ex);
         }
     }
 
     public void setFile(File _file) {
         file = _file;
         label = file.getName();
     }
 
     public List<DSMatchedSeqPattern> getPatterns() {
     	return patterns;
     }
 
     public int getPatternNo() {
         if (patterns == null) {
             patterns = new ArrayList<DSMatchedSeqPattern>();
             read(file);
         }
         return patterns.size();
     }
 
     public DSMatchedSeqPattern getPattern(int i) {
         if ((patterns.size() == 0) && (file != null)) {
             read(file);
         }
         if (i < patterns.size()) {
             return (DSMatchedSeqPattern) patterns.get(i);
         }
         return null;
     }
 
     public void add(DSMatchedSeqPattern pattern) {
         patterns.add(pattern);
     }
 
     @Override
     public boolean equals(Object ads) {
         if (ads instanceof PatternDB) {
             PatternDB pdb = (PatternDB) ads;
             if (pdb.getPatternNo() == getPatternNo()) {
                 return true;
             }
             return false;
         }
         return false;
     }
 
     /**
      *
      * @param out ObjectOutputStream
      * @throws IOException
      */
     private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
     }
 
     /**
      *
      * @param in ObjectInputStream
      * @throws IOException
      * @throws ClassNotFoundException
      */
     private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
     }
 
     @Override
     public int size(){
         if(patterns!=null){
             return patterns.size();
         }
         return 0;
     }
     /**
      * writeToFile
      *
      * @param fileName String
      */
     @Override
     public void writeToFile(String fileName) {
     	// not implemented
     	log.warn("writeToFile not implemented for PatternDB");
     }
 
     public File getDataSetFile() {
         return dataSetFile;
     }
 
     public void setDataSetFile(File _file) {
         dataSetFile = _file;
     }
 }
