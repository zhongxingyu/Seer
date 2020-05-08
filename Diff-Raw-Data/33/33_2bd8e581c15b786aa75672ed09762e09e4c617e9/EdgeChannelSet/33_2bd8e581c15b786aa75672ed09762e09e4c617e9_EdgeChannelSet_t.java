 package beamscheduling;
 
 import java.util.*;
 
 public class EdgeChannelSet {
     public Edge edge;
    public Vector<Integer> channels;
     
    public EdgeChannelSet(Edge e, Vector<Integer> c) {
         this.edge = e;
         this.channels = c;
     }
     
     public String toString() {
         String out = "["+edge.id+"] => {"+channels+"}";
         return(out);
     }
 }
 
