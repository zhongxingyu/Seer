 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe.cscw2013;
 
 import etc.aloe.data.Message;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Segment;
 import etc.aloe.data.SegmentSet;
 import java.util.Date;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author michael
  */
 public class ThresholdSegmentationTest {
 
     public ThresholdSegmentationTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of segment method, of class ThresholdSegmentation.
      */
     @Test
     public void testSegment_byTimeAndParticipant() {
         System.out.println("segment_byTimeAndParticipant");
         MessageSet messages = new MessageSet();
 
         long now = new Date().getTime();
         long second = 1 * 1000;
         long minute = 60 * 1000;
 
         messages.add(new Message(0, new Date(now), "Alice", "it's"));
         messages.add(new Message(1, new Date(now + second), "Bob", "cow"));
         messages.add(new Message(2, new Date(now + 2 * second), "Bob", "time"));
         messages.add(new Message(3, new Date(now + minute), "Bob", "noooooooo"));
         messages.add(new Message(4, new Date(now + minute + second), "Bob", "once"));
         messages.add(new Message(5, new Date(now + 2 * minute), "Alice", "upon"));
         messages.add(new Message(6, new Date(now + 3 * minute), "Alice", "a"));
         messages.add(new Message(7, new Date(now + 3 * minute + second), "Alice", "time"));
 
         ThresholdSegmentation instance = new ThresholdSegmentation(30, true);
         SegmentSet segments = instance.segment(messages);
 
         //Expecting 5 segments
         assertEquals(5, segments.size());
 
         Segment seg0 = segments.get(0);
         Segment seg1 = segments.get(1);
         Segment seg2 = segments.get(2);
         Segment seg3 = segments.get(3);
         Segment seg4 = segments.get(4);
 
         assertEquals(1, seg0.getMessages().size()); //Alice
         assertEquals(1, seg1.getMessages().size()); //Alice
         assertEquals(2, seg2.getMessages().size()); //Alice, Alice
         assertEquals(2, seg3.getMessages().size()); //Bob, Bob
         assertEquals(2, seg4.getMessages().size()); //Bob, Bob
 
         assertEquals(messages.get(0), seg0.getMessages().get(0));
         assertEquals(messages.get(5), seg1.getMessages().get(0));
         assertEquals(messages.get(6), seg2.getMessages().get(0));
         assertEquals(messages.get(1), seg3.getMessages().get(0));
         assertEquals(messages.get(3), seg4.getMessages().get(0));
     }
 
     /**
      * Test of segment method, of class ThresholdSegmentation.
      */
     @Test
     public void testSegment_byTime() {
         System.out.println("segment_byTime");
         MessageSet messages = new MessageSet();
 
         long now = new Date().getTime();
         long second = 1 * 1000;
         long minute = 60 * 1000;
 
         messages.add(new Message(0, new Date(now), "Alice", "it's"));
         messages.add(new Message(1, new Date(now + second), "Bob", "cow"));
         messages.add(new Message(2, new Date(now + 2 * second), "Alice", "time"));
         messages.add(new Message(3, new Date(now + minute), "Bob", "noooooooo"));
         messages.add(new Message(4, new Date(now + minute + second), "Bob", "once"));
         messages.add(new Message(5, new Date(now + 2 * minute), "Alice", "upon"));
         messages.add(new Message(6, new Date(now + 3 * minute), "Bob", "a"));
         messages.add(new Message(7, new Date(now + 3 * minute + second), "Alice", "time"));
 
         ThresholdSegmentation instance = new ThresholdSegmentation(30, false);
         SegmentSet segments = instance.segment(messages);
 
         //Expecting 4 segments
         assertEquals(4, segments.size());
 
         Segment seg0 = segments.get(0);
         Segment seg1 = segments.get(1);
         Segment seg2 = segments.get(2);
         Segment seg3 = segments.get(3);
 
         assertEquals(3, seg0.getMessages().size());
         assertEquals(2, seg1.getMessages().size());
         assertEquals(1, seg2.getMessages().size());
         assertEquals(2, seg3.getMessages().size());
 
         assertEquals(messages.get(0), seg0.getMessages().get(0));
         assertEquals(messages.get(3), seg1.getMessages().get(0));
         assertEquals(messages.get(5), seg2.getMessages().get(0));
         assertEquals(messages.get(6), seg3.getMessages().get(0));
     }
 
     /**
      * Test of segment method, of class ThresholdSegmentation.
      */
     @Test
     public void testSegment_byParticipant() {
         System.out.println("segment_byParticipant");
         MessageSet messages = new MessageSet();
 
         messages.add(new Message(0, new Date(), "Alice", "it's"));
         messages.add(new Message(1, new Date(), "Bob", "cow"));
         messages.add(new Message(2, new Date(), "Alice", "time"));
         messages.add(new Message(3, new Date(), "Bob", "noooooooo"));
         messages.add(new Message(4, new Date(), "Bob", "once"));
         messages.add(new Message(5, new Date(), "Alice", "upon"));
         messages.add(new Message(6, new Date(), "Bob", "a"));
         messages.add(new Message(7, new Date(), "Alice", "time"));
 
         ThresholdSegmentation instance = new ThresholdSegmentation(30, true);
         SegmentSet segments = instance.segment(messages);
 
         //Expecting 2 segments, one for each participant
         assertEquals(2, segments.size());
 
         Segment seg0 = segments.get(0);
         Segment seg1 = segments.get(1);
 
         assertEquals(4, seg0.getMessages().size());
         assertEquals(4, seg1.getMessages().size());
 
         String participant0 = seg0.getMessages().get(0).getParticipant();
         String participant1 = seg1.getMessages().get(0).getParticipant();
         assertFalse(participant0.equals(participant1));
 
         for (Message message : seg0.getMessages()) {
             assertTrue(message.getParticipant().equals(participant0));
         }
 
         for (Message message : seg1.getMessages()) {
             assertTrue(message.getParticipant().equals(participant1));
         }
     }
 
     /**
      * Test of segment method, of class ThresholdSegmentation.
      */
     @Test
     public void testSegment_notByParticipant() {
         System.out.println("segment_notByParticipant");
         MessageSet messages = new MessageSet();
 
         messages.add(new Message(0, new Date(), "Alice", "it's"));
         messages.add(new Message(1, new Date(), "Bob", "cow"));
         messages.add(new Message(2, new Date(), "Alice", "time"));
         messages.add(new Message(3, new Date(), "Bob", "noooooooo"));
         messages.add(new Message(4, new Date(), "Bob", "once"));
         messages.add(new Message(5, new Date(), "Alice", "upon"));
         messages.add(new Message(6, new Date(), "Bob", "a"));
         messages.add(new Message(7, new Date(), "Alice", "time"));
 
         ThresholdSegmentation instance = new ThresholdSegmentation(30, false);
         SegmentSet segments = instance.segment(messages);
 
         //Expecting 1 segment
         assertEquals(1, segments.size());
 
         Segment seg0 = segments.get(0);
 
         assertEquals(8, seg0.getMessages().size());
 
         for (int i = 0; i < seg0.getMessages().size(); i++) {
             Message message = seg0.getMessages().get(i);
             Message expectedMessage = seg0.getMessages().get(i);
             assertEquals(expectedMessage, message);
         }
     }
 
 }
