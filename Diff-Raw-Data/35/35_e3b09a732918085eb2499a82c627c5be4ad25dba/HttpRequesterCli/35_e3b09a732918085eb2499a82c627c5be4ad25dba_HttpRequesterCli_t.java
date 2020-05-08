 package httprequesterviews;
 
 import httprequester.*;
import java.util.Iterator;
 import org.apache.commons.cli.*;
 
 /**
  *
  * @author Benjamin
  */
 public class HttpRequesterCli implements HttpRequesterView, HttpRequesterObserver
 {
     private Options options;
     private CommandLine cmd;
     private HttpRequesterObservable hr;
     private HttpRequesterController controller;
     
     public HttpRequesterCli(HttpRequesterObservable hr, HttpRequesterController controller, String[] args)
     {
         this.hr = hr;
         this.controller = controller;
         setOptions();
         
         try
         {
             CommandLineParser parser = new BasicParser();
             cmd = parser.parse(options, args);
         }
         catch(Exception ex)
         {
             System.out.println(ex.getMessage());
            showOptions();
            System.exit(1);
         }
     }
     
     @Override
     public void showView()
     {
         System.out.println("\nHttpRequester version " + HttpRequester.VERSION);
         hr.addObserver(this);
         
         controller.startHttpRequester();
     }
     
     @Override
     public void errorUpdate() {}
 
     @Override
     public void startingUpdate() {}
 
     @Override
     public void finishingUpdate() {}
 
     @Override
     public int getThreads()
     {
         if(cmd.hasOption("t"))
             return Integer.parseInt(cmd.getOptionValue("t"));
         else
             return 1;
     }
 
     @Override
     public int getRequests()
     {
         if(cmd.hasOption("r"))
             return Integer.parseInt(cmd.getOptionValue("r"));
         else
             return 10;
     }
 
     @Override
     public int getSleep()
     {
         if(cmd.hasOption("s"))
             return Integer.parseInt(cmd.getOptionValue("s"));
         else
             return 500;
     }
 
     @Override
     public String getUrl()
     {
         return cmd.getOptionValue("u");
     }
 
     @Override
     public boolean getCheckUrl()
     {
         return cmd.hasOption("c");
     }
 
     @Override
     public boolean getReturnData()
     {
         return cmd.hasOption("d");
     }
 
     @Override
     public boolean getReturnHeaders()
     {
         return cmd.hasOption("h");
     }
     
     @Override
     public void update(NotifyObject arg)
     {
         int status = arg.getStatus();
         
         if(!cmd.hasOption("q") || status != NotifyObject.RESPONSE)
         {
             if(status == NotifyObject.FINISHED) System.out.println("\nFinished.");
             if(status == NotifyObject.RESPONSE) System.out.println();
             System.out.println(arg.getMessage());
             
             if(status == NotifyObject.RESPONSE)
             {
                 if(getReturnHeaders()) System.out.printf("%s\n", arg.getHeaderString());
                 if(getReturnData()) System.out.printf("\n%s\n", arg.getData());
             }
         }
     }
    
    private void showOptions() 
    {
        Iterator i = options.getOptions().iterator();
        while(i.hasNext())
        {
            Option o = (Option) i.next();
            System.out.printf("-%-3s | --%-9s  %s\n", o.getOpt(), o.getLongOpt(), o.getDescription());
        }
    }
 
     private void setOptions()
     {
         Option o;
         options = new Options();
         
        o = new Option("cli", "cli", false, "Run on the command line.");
         o.setRequired(true);
         options.addOption(o);
         
         o = new Option("q", "quiet", false, "Show no output.");
         o.setRequired(false);
         options.addOption(o);
         
         o = new Option("d", "data", false, "Show response data.");
         o.setRequired(false);
         options.addOption(o);
         
         o = new Option("h", "headers", false, "Show response headers.");
         o.setRequired(false);
         options.addOption(o);
         
         o = new Option("t", "threads", true, "Set the number of threads.");
         o.setRequired(false);
         o.setArgs(1);
         options.addOption(o);
         
         o = new Option("r", "requests", true, "Set the number of requests.");
         o.setRequired(false);
         o.setArgs(1);
         options.addOption(o);
         
         o = new Option("c", "check-url", false, "Check if the url is valid before starting.");
         o.setRequired(false);
         options.addOption(o);
         
         o = new Option("s", "sleep", true, "Set the number of milliseconds between requests.");
         o.setRequired(false);
         o.setArgs(1);
         options.addOption(o);
         
         o = new Option("u", "url", true, "The url to send the requests to.");
         o.setRequired(true);
         o.setArgs(1);
         options.addOption(o);
     }
 }
