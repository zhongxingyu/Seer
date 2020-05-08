 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 /**
  *
  * @author Rasmus
  */
 public class UpdateUtil {
     
     public static final int FirmwareMain = 0;
     public static final int FirmwareSub = 2;
         
     public static boolean UpdateFirmware(String port)
     {
         boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
         int result;
        String prog = (isWindows) ? "avrdude-win/avrdude.exe" : "sudo avrdude-mac/avrdude";
         String conf = (isWindows) ? "avrdude-win/avrdude.conf" : "avrdude-mac/avrdude.conf";
         String filename = "firmware/StratoSnapper_v22.cpp.hex";
         StringBuilder sb = new StringBuilder();
         try {
             Process p = Runtime.getRuntime().exec(prog+" -C"+conf+" -v -v -v -v -patmega328p -carduino -P"+port+" -b57600 -D -Uflash:w:"+filename+":i");
             BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             String line;
             while ((line = r.readLine()) != null)
             {
                 sb.append(line).append("\r\n");
             }
             result = p.waitFor();
             
         } catch (IOException | InterruptedException ex) {
             sb.append(ex.getClass().getName());
             sb.append(ex.getMessage());
             result = -1;
         }
         if (result != 0)
         {
             try {
                 PrintWriter pw = new PrintWriter("logs/avrdude.log");
                 pw.write("Result from avrdude: " + result + "\r\n");
                 pw.write(sb.toString());
                 pw.flush();
                 pw.close();
             } catch (FileNotFoundException ex) {
                 
             }
         }
         return result == 0;
     }
 }
