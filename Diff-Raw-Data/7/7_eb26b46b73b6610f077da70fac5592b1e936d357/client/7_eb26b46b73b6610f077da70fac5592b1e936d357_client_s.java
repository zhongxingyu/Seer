import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
 
 public class client
 {
 
     public static void main(String[] args)
     {
         byte[] boardbytes = new byte[1024];
         String result;
         String board;
 
         if (args.length < 1) {
             System.err.println("You need to supply board number as argument");
             return;
         }
 
         try {
             Socket socket = new Socket("cvap103.nada.kth.se", 5555);
             InputStream inRaw = socket.getInputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(inRaw));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 
             out.println(args[0]);
 
             inRaw.read(boardbytes);
             board = new String(boardbytes);
             System.out.println(board);
 
             // String solution = new
             // String("0 3 3 1 0 0 2 1 2 2 0 2 2 1 3 3 3 3 2 1 1 3 0 3 0 1 2 2 0 3");
             String solution = new String(
                     "U R R D U U L D L L U L L D R R R R L D D R U R U D L L U R");
 
             out.println(solution);
 
             result = in.readLine();
             System.out.println(result);
 
             out.close();
             in.close();
             socket.close();
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
