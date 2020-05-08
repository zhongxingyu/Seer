 package uk.ac.ebi.age.tools.mmode;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import uk.ac.ebi.age.admin.client.AgeAdminService;
 import uk.ac.ebi.age.admin.remote.AgeAdminRemote;
import uk.ac.ebi.age.ext.user.exception.NotAuthorizedException;
import uk.ac.ebi.age.ext.user.exception.UserAuthException;
 
 public class MModeTool
 {
  private static Options options;
  private static CmdLineParser parser = new CmdLineParser( options = new Options() );
 
  /**
   * @param args
   */
  public static void main(String[] args)
  {
   
   try
   {
    parser.parseArgument(args);
   }
   catch(CmdLineException e)
   {
    System.err.println("Can't parse command line");
    usage();
    System.exit(1);
   }
   
   if( options.getArgs() == null || options.getArgs().size() != 1 )
   {
    System.err.println("Invalid number of arguments");
    usage();
    System.exit(1);
   }
   
   if( options.getDatabaseURL() == null )
   {
    System.err.println("Database URL should be specified");
    usage();
    System.exit(1);
   }
   
   if( options.getUser() == null )
   {
    System.err.println("User name should be specified");
    usage();
    System.exit(1);
   }
 
   
   int timeout = -1;
   
   if( options.getTimeout() != null )
   {
    try
    {
     timeout = Integer.parseInt(options.getTimeout());
    }
    catch(Exception e)
    {
     System.err.println("Invalid timeout. Should be integer");
     usage();
     System.exit(1);
    }
   }
   
   boolean set=true;
   
   if( "set".equalsIgnoreCase(options.getArgs().get(0)) )
    set = true;
   else if( "reset".equalsIgnoreCase(options.getArgs().get(0)) )
    set = false;
   else
   {
    System.err.println("Invalid argument: '"+options.getArgs().get(0)+"'");
    usage();
    System.exit(1);
   }
   
   AgeAdminService adm = AgeAdminRemote.getInstance(options.getDatabaseURL());
   
   boolean res = false;
   
   try
   {
    adm.login(options.getUser(), options.getPassword());
   }
   catch(UserAuthException e)
   {
    System.err.println("Login failed");
    System.exit(1);
   }
   
   try
   {
    res = adm.setMaintenanceMode(set, timeout);
   }
   catch(NotAuthorizedException e)
   {
    System.err.println("User '"+options.getUser()+"' was not authorized to set/reset maintenance mode");
    System.exit(1);
   }
   
   if( ! res )
    System.out.println("System already in requested mode");
   else
    System.out.println("Maintenance mode was "+(set?"set":"reset"));
  }
 
  static void usage()
  {
   System.err.println("Usage:\njava -jar MModeTool -u uname -p password -h host -t timeout set|reset ");
  }
 }
