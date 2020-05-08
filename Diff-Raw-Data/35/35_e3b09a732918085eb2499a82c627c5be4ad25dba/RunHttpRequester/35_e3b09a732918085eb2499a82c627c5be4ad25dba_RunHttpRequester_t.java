 package httprequester;
 
 import org.apache.commons.cli.*;
 
 /**
  *
  * @author Benjamin
  */
 public class RunHttpRequester
 {
     public static void main(String[] args)
     {
         try
         {
             HttpRequester hr = new HttpRequester();
             HttpRequesterController controller;
             Options options = new Options();
             
            Option o = new Option("cli", "cli", false, "Start from command line.");
             o.setRequired(false);
             options.addOption(o);
             CommandLineParser parser = new BasicParser();
             CommandLine cmd = parser.parse(options, args, true);
             
            if(cmd.hasOption("cli") | cmd.hasOption("help"))
                 controller = new Controller(hr, Controller.CLI, args);
             else
                 controller = new Controller(hr, Controller.GUI, args);
             
         } catch (ParseException ex)
         {
             System.out.println(ex.getMessage());
         }
     }
 }
