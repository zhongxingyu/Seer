 package beamscheduling;
 
 import java.util.*;
 
 public class EdgeChannelSet {
     public Edge edge;
    public Vector channels;
     
    public EdgeChannelSet(Edge e, Vector c) {
         this.edge = e;
         this.channels = c;
     }
     
     public String toString() {
         String out = "["+edge.id+"] => {"+channels+"}";
         return(out);
     }
 }
 
