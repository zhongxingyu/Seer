 package signature.chemistry;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import signature.chemistry.Molecule.BondOrder;
 
 public class MoleculeReader {
 
     public static Molecule readMolfile(String filename) {
         Molecule molecule = null;
         try {
             File file = new File(filename);
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String line;
             List<String> block = new ArrayList<String>();
             while ((line = reader.readLine()) != null) {
                 block.add(line);
             }
             molecule = makeMolecule(block);
         } catch (IOException ioe) {
             System.err.println(ioe.toString());
         } catch (Exception e) {
             System.err.println(e.toString());
         }
         return molecule;
     }
     
     
 	/**
 	 * Read a list of Molecule from SDFile
 	 * @param filename path to SDFile
 	 * @return List of Molecules
 	 */
     public static List<Molecule> readSDFFile(String filename) {
 
         File file = new File(filename);
         try {
             return readSDFfromStream(new FileInputStream(file));
         } catch (FileNotFoundException e) {
             System.err.println(e.toString());
         }
         return null;
     }
     
 	/**
 	 * Read a list of Molecule from an InputStream, providing SDFile contents
 	 * @param stream InputStream to read from
 	 * @return List of Molecules
 	 */
     public static List<Molecule> readSDFfromStream(InputStream stream) {
         List<Molecule> molecules = new ArrayList<Molecule>();
         try {
             InputStreamReader ir = new InputStreamReader(stream);
             
             BufferedReader reader = new BufferedReader(ir);
             String line;
             int i = 0;
             List<String> block = new ArrayList<String>();
             while ((line = reader.readLine()) != null) {
                 if (line.startsWith("$$$$")) {
                     Molecule molecule;
                     try {
                         molecule = MoleculeReader.makeMolecule(block); 
                         molecules.add(molecule);
                     } catch (Exception e) {
                         System.err.println("failed for block " + i + " " + e);
                         e.printStackTrace();
                     }
                     block.clear();
                     i++;
                 } else {
                     block.add(line);
                 }
             }
         } catch (IOException ioe) {
             System.err.println(ioe.toString());
         } 
         return molecules;
     }
     
     private static Molecule makeMolecule(List<String> block) throws Exception {
         Molecule molecule = new Molecule();
         // counts are on the fourth line
         String countLine = block.get(3);
         int atomCount = Integer.parseInt(countLine.substring(0, 3).trim());
         int bondCount = Integer.parseInt(countLine.substring(3, 6).trim());
         
         // atom block starts on the fifth line (4th index)
         int atomLineStart = 4;
         int atomLineEnd   = atomCount + atomLineStart; 
         for (int i = atomLineStart; i < atomLineEnd; i++) {
             String symbol = block.get(i).substring(30, 33).trim();
             int atomIndex = i - atomLineStart;
             molecule.addAtom(atomIndex, symbol);
         }
         
         if (atomCount > 1) {
             // bond block starts right after the atom block
             int bondLineStart = atomLineEnd;
             int bondLineEnd = bondLineStart + bondCount;
             for (int i = bondLineStart; i < bondLineEnd; i++) {
                 String bondLine = block.get(i);
                 int atomNumberA = 
                     Integer.parseInt(bondLine.substring(0, 3).trim());
                 int atomNumberB = 
                     Integer.parseInt(bondLine.substring(3, 6).trim());
                 int order = Integer.parseInt(bondLine.substring(7, 10).trim());
                 BondOrder o = convertIntToBondOrder(order);
                 molecule.addBond(atomNumberA - 1, atomNumberB - 1, o);
         	}
         }
         return molecule;
     }
     
     private static BondOrder convertIntToBondOrder(int o) {
         switch (o) {
             case 1: return BondOrder.SINGLE;
             case 2: return BondOrder.DOUBLE;
             case 3: return BondOrder.TRIPLE;
             case 4: return BondOrder.AROMATIC;
             default : return BondOrder.SINGLE; 
         }
     }
 
 }
