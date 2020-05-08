 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class SocialNetworkTest {
     private final static String PERSON1 = "Kernighan";
     private final static String PERSON2 = "Ritchie";
     private final static String PERSON3 = "Stallman";
 
     @Test
     public void testSizeGettersEmpty() {
         SocialNetwork network = new SocialNetwork();
         assertEquals(0, network.getNumNodes());
         assertEquals(0, network.getNumConnections());
     }
 
     @Test
     public void testSizeGetters() {
         SocialNetwork network = new SocialNetwork();
 
         Connection connection1 = new Connection(PERSON1, PERSON2);
         network.add(connection1);
 
         Connection connection2 = new Connection(PERSON3, PERSON2);
         network.add(connection2);
 
        assertEquals(2, network.getNumNodes());
        assertEquals(3, network.getNumConnections());
     }
 }
