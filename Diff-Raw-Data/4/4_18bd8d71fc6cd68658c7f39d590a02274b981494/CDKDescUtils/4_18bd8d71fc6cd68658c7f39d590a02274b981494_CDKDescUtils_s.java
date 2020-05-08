 package net.guha.apps.cdkdesc;
 
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.exception.InvalidSmilesException;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.io.IChemObjectReader;
 import org.openscience.cdk.io.ReaderFactory;
 import org.openscience.cdk.io.formats.IResourceFormat;
 import org.openscience.cdk.io.iterator.IteratingMDLReader;
 import org.openscience.cdk.qsar.IDescriptor;
 import org.openscience.cdk.smiles.SmilesParser;
 
 import java.io.*;
 import java.util.Comparator;
 
 /**
  * @author Rajarshi Guha
  */
 
 public class CDKDescUtils {
 
     CDKDescUtils() {
     }
 
     /**
      * Checks whether the input file is in SMI format.
      * <p/>
      * The approach I take is to read the first line of the file.
      * This should splittable to give two parts. The first part should
      * be a valid SMILES string. If so this method returns true, otherwise
      * false
      *
      * @param filename The file to consider
      * @return true if the file is in SMI format, otherwise false
      */
     public static boolean isSMILESFormat(String filename) {
         String line1 = null;
         String line2 = null;
         try {
             BufferedReader in = new BufferedReader(new FileReader(filename));
             line1 = in.readLine();
             line2 = in.readLine();
             in.close();
         } catch (FileNotFoundException fnfe) {
             fnfe.printStackTrace();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
         assert line1 != null;
 
         String[] tokens = line1.split("\\s+");
         if (tokens.length == 0) return false;
 
         SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
         try {
             IMolecule m = sp.parseSmiles(tokens[0].trim());
         } catch (InvalidSmilesException ise) {
             return false;
         }
 
         // now check the second line
         // if there is no second line this probably a smiles
         // file
         if (line2 == null) return true;
 
         // o0k we have a second line, so lets see if it's a smiles
         tokens = line2.split("\\s+");
         if (tokens.length == 0) return false;
 
         sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
         try {
             IMolecule m = sp.parseSmiles(tokens[0].trim());
         } catch (InvalidSmilesException ise) {
             return false;
         }
 
         return true;
     }
 
     public static boolean isMDLFormat(String filename) {
         boolean flag;
         try {
             File file = new File(filename);
             ReaderFactory factory = new ReaderFactory();
             IChemObjectReader reader = factory.createReader(new FileReader(file));
             IResourceFormat format = reader.getFormat();
            flag = reader != null && format.getFormatName().startsWith("MDL Mol");
         } catch (Exception exception) {
             return false;
         }
         return flag;
     }
 
     public static int countMolecules(String filename) throws Exception {
         int numMol = 0;
         if (isSMILESFormat(filename)) {
             try {
                 BufferedReader in = new BufferedReader(new FileReader(filename));
                 while (true) {
                     String line = in.readLine();
                     if (line == null) break;
                     else
                         numMol++;
                 }
                 in.close();
             } catch (FileNotFoundException fnfe) {
                 fnfe.printStackTrace();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         } else if (isMDLFormat(filename)) {
             try {
                 FileInputStream inputStream = new FileInputStream(filename);
                 IteratingMDLReader mdlReader = new IteratingMDLReader(inputStream, DefaultChemObjectBuilder.getInstance());
                 while (mdlReader.hasNext()) {
                     mdlReader.next();
                     numMol++;
                 }
                 mdlReader.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         } else
             throw new Exception("Invalid file format supplied");
         return numMol;
     }
 
     public static boolean isMacOs() {
         String lcOSName = System.getProperty("os.name").toLowerCase();
         return lcOSName.startsWith("mac os x");
     }
 
     public static Comparator getDescriptorComparator() {
         return new Comparator() {
             public int compare(Object o1, Object o2) {
                 IDescriptor desc1 = (IDescriptor) o1;
                 IDescriptor desc2 = (IDescriptor) o2;
 
                 String[] comp1 = desc1.getSpecification().getSpecificationReference().split("#");
                 String[] comp2 = desc2.getSpecification().getSpecificationReference().split("#");
 
                 return comp1[1].compareTo(comp2[1]);
             }
         };
     }
 }
