 package etc.aloe.cscw2013;
 
 import etc.aloe.data.Message;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Segment;
 import etc.aloe.data.SegmentSet;
 import etc.aloe.processes.SegmentResolution;
 import etc.aloe.processes.Segmentation;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  *
  */
 public class ThresholdSegmentation implements Segmentation {
 
     private final int thresholdSeconds;
     private final boolean byParticipant;
     private SegmentResolution resolution;
 
     public ThresholdSegmentation(int thresholdSeconds, boolean byParticipant) {
         this.thresholdSeconds = thresholdSeconds;
         this.byParticipant = byParticipant;
     }
 
     private List<Message> sortByParticipant(List<Message> original) {
         List<Message> messages = new ArrayList<Message>(original);
 
         Collections.sort(messages, new Comparator<Message>() {
             @Override
             public int compare(Message o1, Message o2) {
                 return o1.getParticipant().compareTo(o2.getParticipant());
             }
         });
 
         return messages;
     }
 
     private List<Message> sortByTime(List<Message> original) {
         List<Message> messages = new ArrayList<Message>(original);
 
         Collections.sort(messages, new Comparator<Message>() {
             @Override
             public int compare(Message o1, Message o2) {
                 return o1.getTimestamp().compareTo(o2.getTimestamp());
             }
         });
 
         return messages;
     }
 
     @Override
     public SegmentSet segment(MessageSet messageSet) {
System.out.println("Segmenting with " + thresholdSeconds + " second threshold," + (byParticipant ? "" : " not") + " separating by participant.");
         List<Message> messages = sortByTime(messageSet.getMessages());
         if (byParticipant) {
             messages = sortByParticipant(messages);
         }
 
         SegmentSet segments = new SegmentSet();
 
         Segment current = new Segment();
         long lastTime = 0;
         String lastParticipant = null;
 
         int numLabeled = 0;
         for (Message message : messages) {
             long msgSeconds = message.getTimestamp().getTime() / 1000;
             long diffSeconds = (msgSeconds - lastTime);
 
             boolean newSegment = false;
             if (lastTime > 0 && diffSeconds > thresholdSeconds) {
                 newSegment = true;
             }
             if (byParticipant && lastParticipant != null && !lastParticipant.equals(message.getParticipant())) {
                 newSegment = true;
             }
 
             if (newSegment) {
                 if (this.resolution != null) {
                     current.setTrueLabel(this.resolution.resolveLabel(current));
                     if (current.hasTrueLabel()) {
                         numLabeled++;
                     }
                 }
                 segments.add(current);
                 current = new Segment();
             }
 
             lastTime = msgSeconds;
             lastParticipant = message.getParticipant();
             current.add(message);
         }
 
         if (current.getMessages().size() > 0) {
             if (this.resolution != null) {
                 current.setTrueLabel(this.resolution.resolveLabel(current));
                 if (current.hasTrueLabel()) {
                     numLabeled++;
                 }
             }
             segments.add(current);
         }
 
         System.out.println("Grouped messages into " + segments.size() + " segments (" + numLabeled + " labeled).");
 
         return segments;
     }
 
     @Override
     public void setSegmentResolution(SegmentResolution resolution) {
         this.resolution = resolution;
     }
 }
