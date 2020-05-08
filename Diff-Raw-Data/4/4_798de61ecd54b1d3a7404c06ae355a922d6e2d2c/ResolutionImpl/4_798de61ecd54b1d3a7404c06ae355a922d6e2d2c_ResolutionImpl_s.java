 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe.cscw2013;
 
 import etc.aloe.data.Message;
 import etc.aloe.data.Segment;
 import etc.aloe.processes.SegmentResolution;
 
 /**
  *
  * @author michael
  */
 public class ResolutionImpl implements SegmentResolution {
 
     @Override
     public Boolean resolveLabel(Segment segment) {
         boolean labelSetBySomeone = false;
         for (Message message : segment.getMessages()) {
             if (message.hasTrueLabel()) {
                 labelSetBySomeone = true;
                 if (message.getTrueLabel() == true) {
                     return true;
                 }
             }
         }
        
        if (!labelSetBySomeone) {
             return false;
         } else {
             return null;
         }
     }
 }
