 package net.praqma.hudson.remoting;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.utils.Baselines;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.appenders.Appender;
 import net.praqma.util.debug.appenders.StreamAppender;
 import hudson.FilePath.FileCallable;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 public class GetRemoteBaselineFromStream implements FileCallable<List<Baseline>> {
 
 	private static final long serialVersionUID = -8984877325832486334L;
 
 	private Component component;
 	private Stream stream;
 	private Project.PromotionLevel plevel;
 	private Pipe pipe;
 	
 	private PrintStream pstream;
 	
 	private LoggerSetting loggerSetting;
 	private boolean multisitePolling;	
 
 	public GetRemoteBaselineFromStream( Component component, Stream stream, Project.PromotionLevel plevel, Pipe pipe, PrintStream pstream, LoggerSetting loggerSetting, boolean multisitePolling ) {
 		this.component = component;
 		this.stream = stream;
 		this.plevel = plevel;
 		this.pipe = pipe;
 		
 		this.pstream = pstream;
 		
 		this.loggerSetting = loggerSetting;
 		this.multisitePolling = multisitePolling;
 	}
     
     @Override
     public List<Baseline> invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
     	
     	Logger logger = Logger.getLogger();
 
     	Appender app = null;
     	if( pipe != null ) {
 	    	PrintStream toMaster = new PrintStream( pipe.getOut() );	    	
 	    	app = new StreamAppender( toMaster );
 	    	app.lockToCurrentThread();
 	    	Logger.addAppender( app );
 	    	app.setSettings( loggerSetting );
     	} else if( pstream != null ) {
 	    	app = new StreamAppender( pstream );
 	    	app.lockToCurrentThread();
 	    	Logger.addAppender( app );
 	    	app.setSettings( loggerSetting );    		
     	}
     	
     	logger.debug( "Retrieving remote baselines from " + stream.getShortname() );
     	
         /* The baseline list */
         List<Baseline> baselines = null;
         
         try {
        	baselines = Baselines.get( stream, component, plevel );
        } catch (ClearCaseException e) {
        		Logger.removeAppender( app );
             throw new IOException("Could not retrieve baselines from repository. " + e.getMessage(), e);
         }
         
         /* Load baselines remotely */
         for( Baseline baseline : baselines ) {
         	try {
         		logger.debug( "Loading the baseline " + baseline );
 				baseline.load();
 			} catch (ClearCaseException e) {
 				logger.warning( "Could not load the baseline " + baseline.getShortname() + ": " + e.getMessage() );
 				/* Maybe it should be removed from the list... In fact, this shouldn't happen */
 			}
         }
         
         Logger.removeAppender( app );
 
         return baselines;
     }
 
 }
