 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pszalwinski
  * Date: 9/19/13
  * Time: 3:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
     public static void main(String[] args) throws IOException {
         String text = "this is some text\r\n";
         BufferedReader br = new BufferedReader(new StringReader(text));
         br.readLine();

 
     }
 }
