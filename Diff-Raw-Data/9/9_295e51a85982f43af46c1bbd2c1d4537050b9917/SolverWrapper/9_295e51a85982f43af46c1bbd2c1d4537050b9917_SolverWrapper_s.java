 import org.apache.commons.io.IOUtils;
 
 import java.io.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dextor
  * Date: 8/16/13
  * Time: 3:40 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SolverWrapper {
 
     public static String execute(String csp, int method) {
         try {
             File f = new File("solver.out");
            Process p = new ProcessBuilder(f.getAbsolutePath(), "\"" + csp + "\"", "-resolution-mode " + method).start();
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line = "";
             StringBuffer output = new StringBuffer();
 
             System.out.println("<OUTPUT>");
             while ((line = reader.readLine()) != null) {
                 System.out.println("Line: " + line);
                 output.append(line);
             }
             System.out.println("</OUTPUT>");
             reader.close();
 
             int exitVal = p.waitFor();
             System.out.println("Process exitValue: " + exitVal);
 
             reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
             line = null;
             while ((line = reader.readLine()) != null)
                 System.out.println("Error: " + line);
 
             reader.close();
 
             return output.toString();
 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         return "Solver error";
     }
 
 }
