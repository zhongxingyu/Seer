 package test.graph.group;
 
 import graph.group.GraphDiscretePartitionRefiner;
 import graph.model.Graph;
 import group.PermutationGroup;
 
 import org.junit.Test;
 
 public class IntVLongTest {
     
     @Test
     public void tertTButTest() {
         Graph g = new Graph("0:1,0:2,0:3,0:4," +
         		            "1:5,1:6,1:7," +
        		            "2:8,2:9,2:10" +
        		            "3:11,3:12,3:13" +
         		            "4:14,4:15,4:16");
         GraphDiscretePartitionRefiner refiner = new GraphDiscretePartitionRefiner();
         PermutationGroup group = refiner.getAutomorphismGroup(g);
         int oN = group.order();
         long oL = group.orderAsLong();
         System.out.println(oN);
         System.out.println(oL);
     }
     
     @Test
     public void tButTest() {
         Graph g = new Graph("0:1,0:2,0:3,0:4");
         GraphDiscretePartitionRefiner refiner = new GraphDiscretePartitionRefiner();
         PermutationGroup group = refiner.getAutomorphismGroup(g);
         int oN = group.order();
         long oL = group.orderAsLong();
         System.out.println(oN);
         System.out.println(oL);
     }
     
     @Test
     public void adornedTButTest() {
         Graph g = new Graph("0:1,0:2,0:3,0:4," +
                 "1:5,1:6,1:7,"
                 + "2:8,2:9,2:10"
                 + "3:11,3:12,3:13"
 //                + "4:14,4:15,4:16"
                 );
         GraphDiscretePartitionRefiner refiner = new GraphDiscretePartitionRefiner();
         PermutationGroup group = refiner.getAutomorphismGroup(g);
         int oN = group.order();
         long oL = group.orderAsLong();
         System.out.println(oN);
         System.out.println(oL);
     }
     
}
