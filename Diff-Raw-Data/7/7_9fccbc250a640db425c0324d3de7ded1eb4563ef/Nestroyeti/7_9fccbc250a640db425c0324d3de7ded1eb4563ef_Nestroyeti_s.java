 package modules;
 
 import main.NoiseModule;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * Created by whitelje on 2/7/14.
  */
 public class Nestroyeti extends NoiseModule {
 
     @Override
     public String getFriendlyName() {
         return "Nestroyeti";
     }
 
     @Override
     public String getDescription() {
         return "Returns the current temperature in the home of necroyeti";
     }
 
     @Override
     public String[] getExamples() {
         return new String[] {
                 ".nestroyeti"
         };
     }
 
     @Command("\\.nestroyeti")
    public void getTemp() {
         try{
        URL url = new URL("http://http://phire.org/nest/");
         URLConnection con = url.openConnection();
         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(con.getInputStream()));
         String output = in.readLine();
         this.bot.sendMessage(output);
         } catch (IOException e) {
             e.printStackTrace();
             this.bot.sendMessage("Error retrieving yeti weather...");
         }
 
     }
 
 
 }
