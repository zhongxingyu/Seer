 package signature.simple;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import signature.AbstractVertexSignature;
 import signature.ColoredTree;
 import signature.SymmetryClass;
 
 public class ReconstructionTest {
 
     // XXX DOES NOT YET WORK - reconstructed graph may be isomorphic, 
     // but not automorphic
     public void reconstruct(SimpleGraph graph) {
         SimpleGraphSignature signature = new SimpleGraphSignature(graph);
         for (SymmetryClass symmetryClass : signature.getSymmetryClasses()) {
             String signatureString = symmetryClass.getSignatureString();
             ColoredTree tree = AbstractVertexSignature.parse(signatureString);
             SimpleGraphBuilder builder = new SimpleGraphBuilder();
             SimpleGraph reconstruction = builder.fromTree(tree);
             Assert.assertEquals(reconstruction.toString(), graph.toString());
         }
     }
 
     @Test
     public void petersensGraphTest() {
         SimpleGraph petersens = SimpleGraphFactory.makePetersensGraph();
//        reconstruct(petersens);
     }
     
     @Test
     public void bowtieaneTest() {
         SimpleGraph bowtie = SimpleGraphFactory.makeBowtieane();
         String tmp = new SimpleGraphSignature(bowtie).signatureStringForVertex(6);
         System.out.println(tmp);
         System.out.println("----------------------------------------");
         String tmp2 = new SimpleGraphSignature(bowtie).signatureStringForVertex(2);
         System.out.println(tmp2);
     }
     
 }
