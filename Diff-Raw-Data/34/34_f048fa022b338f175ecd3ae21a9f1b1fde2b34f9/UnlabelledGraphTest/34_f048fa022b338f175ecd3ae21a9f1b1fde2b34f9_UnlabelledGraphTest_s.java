 package signature;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import signature.simple.SimpleGraph;
 import signature.simple.SimpleGraphBuilder;
 import signature.simple.SimpleGraphSignature;
 import signature.simple.SimpleVertexSignature;
 
 public class UnlabelledGraphTest {
     
     public SimpleGraphSignature signatureFromString(String string) {
         SimpleGraph graph = new SimpleGraph(string);
         return new SimpleGraphSignature(graph, 0); 
     }
 	
     @Test 
     public void testChain() {
         String chain = "0:1,1:2,2:3,3:4";
         SimpleGraphSignature signature = signatureFromString(chain);
         System.out.println(signature.getDAG());
        System.out.println(signature.toString());
        StringBuffer maxSignature = new StringBuffer();
        signature.canonize(0, maxSignature);
         System.out.println("max" + maxSignature);
     }
     
     @Test
     public void testColoredTreeRoundtrip() {
         String signatureString = "[.]([.]([.,1])[.]([.,1]))";
         SimpleVertexSignature sig = new SimpleVertexSignature();
         ColoredTree tree = sig.parse(signatureString);
         Assert.assertEquals(signatureString, tree.toString());
         
         SimpleGraphBuilder builder = new SimpleGraphBuilder();
         SimpleGraph graph = builder.fromTree(tree);
         System.out.println(graph + "\n" + tree);
     }
 	
 }
