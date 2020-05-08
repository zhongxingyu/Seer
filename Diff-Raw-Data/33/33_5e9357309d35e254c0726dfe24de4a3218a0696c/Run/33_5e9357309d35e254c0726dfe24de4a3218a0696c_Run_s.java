 package net.praqma.cli;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.praqma.util.execute.AbnormalProcessTerminationException;
 import net.praqma.util.execute.CmdResult;
 import net.praqma.util.execute.CommandLine;
 import net.praqma.util.execute.CommandLineInterface;
 import net.praqma.util.option.Option;
 import net.praqma.util.option.Options;
 
 public class Run {
 
     /**
      * @param args
      * @throws IOException 
      */
     public static void main(String[] args) throws IOException {
 	try {
 	    run(args);
 	} catch (IOException e) {
 	    throw e;
 	}
     }
     
     public static void run(String[] args) throws IOException {
 	Options o = new Options("1.0");
 
	Option ocommand = new Option("command", "c", true, 1, "The command to be executed");
 	Option opath    = new Option("path", "p", false, 1, "The path where the command is executed");
 	Option omerge   = new Option("merge", "m", false, 0, "If set error out is merged with standard out");
 
 	o.setOption(ocommand);
 	o.setOption(opath);
 	o.setOption(omerge);
 
 	o.setDefaultOptions();
 
 	o.setSyntax("Run -c dir");
 	o.setDescription("Execute a command through Java");
 
 	o.parse(args);
 
 	try {
 	    o.checkOptions();
 	} catch (Exception e) {
 	    System.err.println("Incorrect option: " + e.getMessage());
 	    o.display();
 	    System.exit(1);
 	}
 	
 	CommandLineInterface cli = CommandLine.getInstance();
 	
 	try {
	    CmdResult r = cli.run(ocommand.getString(), (opath.isUsed()?new File(opath.getString()):null), omerge.isUsed());
 	    System.out.println(r.stdoutBuffer);
 	} catch( AbnormalProcessTerminationException e ) {
 	    System.out.println(e.getMessage());
 	}
     }
 
 }
