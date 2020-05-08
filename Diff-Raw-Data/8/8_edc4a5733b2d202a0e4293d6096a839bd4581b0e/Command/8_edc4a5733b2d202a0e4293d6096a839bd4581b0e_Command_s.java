 package net.praqma.util.execute;
 
 
 import java.io.File;
 import java.io.IOException;
 
 import net.praqma.util.debug.Logger;
 
 /**
  * CLI class
  * @author wolfgang
  *
  */
 public abstract class Command
 {
 	protected static Logger logger = Logger.getLogger();
 	protected static final String linesep = System.getProperty( "line.separator" );
 	
 	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
 	{
 		return run( cmd, null, false, false );
 	}
 	
 	public static CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException
 	{
 		return run( cmd, dir, false, false );
 	}
 	
 	public static CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException
 	{
 		return run( cmd, dir, merge, false );
 	}
 	
 	/**
 	 * Execute a command line operation.
 	 * @param cmd The command itself
 	 * @param dir The working directory
 	 * @param merge Merge stderror with stdout
 	 * @param ignore Ignore any abnormal process terminations. This will allow the output to be returned without exceptions to be thrown.
 	 * @return
 	 * @throws CommandLineException
 	 * @throws AbnormalProcessTerminationException
 	 */
 	public static CmdResult run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException
 	{
 		logger.trace_function();		
 		logger.debug( "$ " + cmd );
 		
 		try
 		{
			ProcessBuilder pb = new ProcessBuilder( cmd );
 			pb.redirectErrorStream( merge );
 			
 			if( dir != null )
 			{
 				logger.debug( "Changing current working directory to " + dir );
 				pb.directory( dir );
 			}
 			
 			CmdResult result = new CmdResult();
 			
 			logger.debug( "Starting process" );
 			Process p = pb.start();
 
 			/* Starting Gobbler threads */
 			StreamGobbler output = new StreamGobbler( p.getInputStream() );
 			StreamGobbler errors = new StreamGobbler( p.getErrorStream() );
 			p.getOutputStream().close();
 
 			output.start();
 			errors.start();
 			
 			int exitValue = 0;
 			try
 			{
 				exitValue = p.waitFor();
 			}
 			catch( InterruptedException e )
 			{
 				p.destroy();
 			}
             finally
             {
                 Thread.interrupted();
             }
             
             logger.debug( "Ending process" );
 			
 			/* Abnormal process termination, with error out as message */
 			if ( exitValue != 0 )
 			{
 				logger.debug( "Abnormal process termination(" + exitValue + "): " + errors.sres.toString() );
 				
 				/* Only throw the exception if it is not ignored, this is default */
 				if( !ignore )
 				{
 					throw new AbnormalProcessTerminationException( errors.sres.toString() );
 				}
 			}
 			
 			try
 			{
 				output.join();
 			}
 			catch ( InterruptedException e )
 			{
 				logger.error( "Could not join output thread" );
 			}
 			
 			try
 			{
 				errors.join();
 			}
 			catch ( InterruptedException e )
 			{
 				logger.error( "Could not join errors thread" );
 			}
 			
 			/* Closing streams */
 			p.getErrorStream().close();
 			p.getInputStream().close();
 			
 			/* Setting command result */
 			result.stdoutBuffer = output.sres;
 			result.stdoutList   = output.lres;
 			
 			result.errorBuffer  = errors.sres;
 			result.errorList    = errors.lres;
 			
 			return result;
 		}
 		catch ( IOException e )
 		{
 			logger.warning( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
 			throw new CommandLineException( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
 		}
 	}
 }
