 import DistGrep.Config;
 import DistGrep.Connection;
 import UnitTest.FileGenerator;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kyle
  * Date: 9/15/13
  * Time: 5:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DistributedTest {
 
    private static String path = "/home/kyle/Documents/CodeBase/UIUC/CS438/DistGrep/DistributedUnitTest/machine.log";
 
     public static void main(String[] args) throws IOException, InterruptedException {
         System.out.println("UNIT TEST\n---------------------");
 
         //3000000 lines are needed in our case to create a log-file greater than 100MB
         FileGenerator.generateFile(30, path);
 
         Config config = new Config("Config/distGrep.conf");
 
         Connection connection = new Connection(config);
 
        String result = connection.sendRawBroadcast("starttest");
 
        System.out.println("result");
     }
 }
