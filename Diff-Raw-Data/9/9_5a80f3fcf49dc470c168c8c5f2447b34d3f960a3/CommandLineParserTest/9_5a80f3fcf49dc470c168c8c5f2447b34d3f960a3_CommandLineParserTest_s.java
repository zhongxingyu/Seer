 import com.arlandis.CommandLineParser;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class CommandLineParserTest {
 
     @Test
     public void testPortNumAgain(){
        String[] args = new String[] { "-p", "", "6500" };
         CommandLineParser parser = new CommandLineParser(args);
         assertEquals(Integer.valueOf(6500), parser.portNum());
     }
 
     @Test
     public void testDefaultPort(){
         String[] args = new String[0];
         CommandLineParser parser = new CommandLineParser(args);
         assertEquals(Integer.valueOf(8000), parser.portNum());
     }
 }
