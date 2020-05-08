 package signature.chemistry;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import signature.chemistry.Molecule;
 import signature.chemistry.MoleculeSignature;
 import signature.chemistry.Molecule.BondOrder;
 import signature.display.TreeDrawer;
 
 public class LargeMoleculeTest {
     
     public void addRing(int atomToAttachTo, int ringSize, Molecule molecule) {
         int numberOfAtoms = molecule.getAtomCount();
         int previous = atomToAttachTo;
         for (int i = 0; i < ringSize; i++) {
             molecule.addAtom("C");
             int current = numberOfAtoms + i;
             molecule.addSingleBond(previous, current);
             previous = current;
         }
         molecule.addSingleBond(numberOfAtoms, numberOfAtoms + (ringSize - 1));
     }
     
     public Molecule makeMinimalMultiRing(int ringCount, int ringSize) {
         Molecule mol = new Molecule();
         mol.addAtom("C");
         for (int i = 0; i < ringCount; i++) {
             addRing(0, ringSize, mol);
         }
         return mol;
     }
     
     public Molecule makeTetrakisTriphenylPhosphoranylRhodium() {
         Molecule ttpr = new Molecule();
         ttpr.addAtom("Rh");
         int phosphateCount = 3;
         for (int i = 1; i <= phosphateCount; i++) {
             ttpr.addAtom("P");
             ttpr.addSingleBond(0, i);
         }
         
         int phenylCount = 3;
         for (int j = 1; j <= phosphateCount; j++) {
             for (int k = 0; k < phenylCount; k++) {
                 addRing(j, 6, ttpr);
             }
         }
         return ttpr;
     }
     
     @Test
     public void dodecahedraneTest() {
         Molecule mol = new Molecule();
         for (int i = 0; i < 20; i++) { mol.addAtom("C"); }
         mol.addSingleBond(0, 1);
         mol.addSingleBond(0, 4);
         mol.addSingleBond(1, 2);
         mol.addSingleBond(2, 7);
         mol.addSingleBond(3, 4);
         mol.addSingleBond(3, 8);
         mol.addSingleBond(5, 10);
         mol.addSingleBond(5, 11);
         mol.addSingleBond(6, 11);
         mol.addSingleBond(6, 12);
         mol.addSingleBond(7, 13);
         mol.addSingleBond(8, 14);
         mol.addSingleBond(9, 10);
         mol.addSingleBond(9, 14);
         mol.addSingleBond(12, 17);
         mol.addSingleBond(13, 18);
         mol.addSingleBond(15, 16);
         mol.addSingleBond(15, 19);
         mol.addSingleBond(16, 17);
         mol.addSingleBond(18, 19);
         
         mol.addBond(0, 5, BondOrder.DOUBLE);
         mol.addBond(1, 6, BondOrder.DOUBLE);
         mol.addBond(2, 3, BondOrder.DOUBLE);
         mol.addBond(4, 9, BondOrder.DOUBLE);
         mol.addBond(7, 12, BondOrder.DOUBLE);
         mol.addBond(8, 13, BondOrder.DOUBLE);
         mol.addBond(10, 15, BondOrder.DOUBLE);
         mol.addBond(11, 16, BondOrder.DOUBLE);
         mol.addBond(17, 18, BondOrder.DOUBLE);
         mol.addBond(14, 19, BondOrder.DOUBLE);
         
         for (int i = 0; i < 20; i++) {
             Assert.assertEquals("Atom " + i + " has wrong order", 
                     4, mol.getTotalOrder(i));
         }
         
         MoleculeQuotientGraph mqg = new MoleculeQuotientGraph(mol);
         System.out.println(mqg);
         Assert.assertEquals(5, mqg.getVertexCount());
         Assert.assertEquals(9, mqg.getEdgeCount());
         Assert.assertEquals(3, mqg.numberOfLoopEdges());
 //        String directoryPath = "tmp5";
 //        drawTrees(mqg, directoryPath);
     }
     
     @Test
     public void ttprTest() {
         Molecule ttpr = makeTetrakisTriphenylPhosphoranylRhodium();
         MoleculeSignature molSig = new MoleculeSignature(ttpr);
 //        String sigString = molSig.toCanonicalString();
         String sigString = molSig.signatureStringForVertex(0);
         System.out.println(sigString);
     }
     
     @Test
     public void testMinimalMol() {
         Molecule mol = makeMinimalMultiRing(6, 3);
         MoleculeSignature molSig = new MoleculeSignature(mol);
 //        String sigString = molSig.toCanonicalString();
         String sigString = molSig.signatureStringForVertex(0);
         System.out.println(sigString);
 
         System.out.println(mol);
         System.out.println("result " + sigString);
     }
     
     public Molecule makeChain(int length) {
         Molecule chain = new Molecule();
         int previous = -1;
         for (int i = 0; i < length; i++) {
             chain.addAtom("C");
             if (previous != -1) {
                 chain.addSingleBond(previous, i);
             }
             previous = i;
         }
         return chain;
     }
     
     @Test
     public void testLongChains() {
         int length = 10;
         Molecule chain = makeChain(length);
         MoleculeSignature molSig = new MoleculeSignature(chain);
         String sigString = molSig.toCanonicalString();
         System.out.println(sigString);
     }
     
     public void drawTrees(MoleculeQuotientGraph mqg, String directoryPath) {
         List<String> signatureStrings = mqg.getVertexSignatureStrings();
         int w = 1200;
         int h = 400;
         TreeDrawer.makeTreeImages(signatureStrings, directoryPath, w, h);
     }
     
     @Test
     public void buckyballTest() {
         Molecule molecule = MoleculeReader.readMolfile("data/buckyball.mol");
         MoleculeQuotientGraph mqg = new MoleculeQuotientGraph(molecule);
         System.out.println(mqg);
         
 //        drawTrees(mqg, "tmp");
         
         Assert.assertEquals(32, mqg.getVertexCount());
         Assert.assertEquals(49, mqg.getEdgeCount());
         Assert.assertEquals(6, mqg.numberOfLoopEdges());
     }
     
     @Test
     public void buckyballWithoutMultipleBonds() {
         Molecule molecule = MoleculeReader.readMolfile("data/buckyball.mol");
         for (Molecule.Bond bond : molecule.bonds()) {
             bond.order = BondOrder.SINGLE;
         }
         MoleculeQuotientGraph mqg = new MoleculeQuotientGraph(molecule);
 //        drawTrees(mqg, "tmp3");
         System.out.println(mqg);
         Assert.assertEquals(1, mqg.getVertexCount());
         Assert.assertEquals(1, mqg.getEdgeCount());
         Assert.assertEquals(1, mqg.numberOfLoopEdges());
     }
     
     @Test
     public void faulonsBuckySignatures() {
         Molecule mol = MoleculeReader.readMolfile("data/buckyball.mol");
         try {
 //            String filename = "data/buckysigs.txt";
             String filename = "data/buckysigs3.txt";
             List<String> sigs = readSigs2(filename);
             MoleculeQuotientGraph mqg = new MoleculeQuotientGraph(mol, sigs);
             System.out.println(mqg);
             
 //            drawTrees(mqg, "tmp2");
             
             Assert.assertEquals(32, mqg.getVertexCount());
             Assert.assertEquals(49, mqg.getEdgeCount());
             Assert.assertEquals(6, mqg.numberOfLoopEdges());
         } catch (Exception e) {
             System.out.println(e);
             return;
         }
     }
     
     public List<String> readSigs(String filename) throws Exception {
         File file = new File(filename);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         List<String> sigs = new ArrayList<String>();
         while ((line = reader.readLine()) != null) {
             int index = line.indexOf(" ") + 1;
             int count = Integer.parseInt(line.substring(0, index - 1));
             String sig = line.substring(index);
             System.out.println(count);
             sigs.add(sig);
         }
        reader.close();
         return sigs;
     }
     
     public List<String> readSigs2(String filename) throws Exception {
         File file = new File(filename);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         List<String> sigs = new ArrayList<String>();
         while ((line = reader.readLine()) != null) { 
             String[] bits = line.split("\\s+");
             String sig = bits[3];
             sigs.add(sig);
         }
        reader.close();
         Collections.reverse(sigs);
         return sigs;
     }
     
     public static void main(String[] args) {
         new LargeMoleculeTest().testMinimalMol();
 //        new LargeMoleculeTest().ttprTest();
     }
 
 }
