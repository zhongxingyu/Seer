 package net.praqma.hudson.remoting;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.RebaseException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.Cool;
 import net.praqma.clearcase.PVob;
 import net.praqma.clearcase.Rebase;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.Components;
 import net.praqma.clearcase.ucm.view.SnapshotView.LoadRules;
 import net.praqma.hudson.*;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.remoting.EstablishResult.ResultType;
 import net.praqma.hudson.scm.ClearCaseChangeset;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.Logger.LogLevel;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.appenders.StreamAppender;
 import net.praqma.util.structure.Tuple;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 public class CheckoutTask implements FileCallable<EstablishResult> {
 
 	private static final long serialVersionUID = -7029877626574728221L;
 	private PrintStream hudsonOut;
 	private String jobname;
 	private SnapshotView sv;
 	private String loadModule;
 	private Baseline bl;
 	private String buildProject;
 	private Pipe pipe;
 	private Stream targetStream;
 	private BuildListener listener;
 	private Integer jobNumber;
 	private String id = "";
 
 	private String log = "";
 	
 	private boolean any = false;
 	
 	private PrintStream pstream;
 	
 	
 	private Logger logger;
 	private LoggerSetting loggerSetting;
 
 	public CheckoutTask( BuildListener listener, String jobname, Integer jobNumber, Stream targetStream, String loadModule, Baseline baseline, String buildProject, boolean any, Pipe pipe, PrintStream pstream, LoggerSetting settings ) {
 		this.jobname = jobname;
 		this.jobNumber = jobNumber;
 		this.targetStream = targetStream;
 		this.loadModule = loadModule;
 		this.bl = baseline;
 		this.buildProject = buildProject;
 		this.listener = listener;
 		this.pipe = pipe;
 		this.pstream = pstream;
 		
 		this.loggerSetting = settings;
 		
 		this.any = any;
 
 		this.id = "[" + jobname + "::" + jobNumber + "-cotask]";
 	}
 
 	@Override
 	public EstablishResult invoke( File workspace, VirtualChannel channel ) throws IOException {
 
 		hudsonOut = listener.getLogger();
 		
     	logger = Logger.getLogger();
         
     	StreamAppender app = null;
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
 
 		logger.info( id + "Starting CheckoutTask" );
 
 		String diff = "";
 		String viewtag = makeViewtag();
 		
 		EstablishResult er = new EstablishResult();
 		er.setResultType( ResultType.SUCCESS );
 		ClearCaseChangeset changeset = new ClearCaseChangeset();
 
 		try {
 			logger.debug( id + "Getting dev stream" );
 			Stream devstream = getDeveloperStream( "stream:" + viewtag, targetStream.getPVob() );
 			logger.debug( id + "Getting foundation baseline" );
 			Baseline foundation = devstream.getFoundationBaseline();
 			
 			if( !foundation.getStream().equals( targetStream ) ) {
 				hudsonOut.println( "[" + Config.nameShort + "] The foundation baseline " + foundation.getShortname() + " does not match the stream " + targetStream.getShortname() + ". Changelog will probably be bogus." );
 			}
 			
 			logger.debug( id + "Making workspace" );
 			
 			makeWorkspace( workspace, viewtag );
 			List<Activity> bldiff = null;
 			if( any ) {
 				if( devstream.isCreated() ) {
 					logger.debug( "Diffing newly created stream" );
 					bldiff = Version.getBaselineDiff( targetStream.getFoundationBaseline(), bl, true, sv.getViewRoot() );
 				} else {
 					logger.debug( "Diffing OOOOld stream" );
 					bldiff = Version.getBaselineDiff( foundation, bl, true, sv.getViewRoot() );
 				}
 			} else {
 				//bldiff = bl.getDifferences( sv );
 				bldiff = Version.getBaselineDiff( bl, null, true, sv.getViewRoot() );
 			}
 			
 			logger.debug( id + "Creating change log" );
 			//List<Activity> bldiff = Version.getBaselineDiff( bl, null, true, sv.getViewRoot() );
 			diff = Util.createChangelog( bldiff, bl );
 			hudsonOut.print( "[" + Config.nameShort + "] Found " + bldiff.size() + " activit" + ( bldiff.size() == 1 ? "y" : "ies" ) + ". " );
 			int c = 0;
 			for( Activity a : bldiff ) {
 				c += a.changeset.versions.size();
 				for( Version version : a.changeset.versions ) {
 					changeset.addChange( version.getFullyQualifiedName(), version.getUser() );
 				}
 			}
 			
 			logger.debug( id + "DONE" );
 			hudsonOut.println( c + " version" + ( c == 1 ? "" : "s" ) + " involved" );
 			
 			logger.info( "CheckoutTask finished normally" );
 			
 		} catch (net.praqma.hudson.exception.ScmException e) {
 			logger.warning( id + "SCM exception: " + e.getMessage() );
 			hudsonOut.println( "[" + Config.nameShort + "] SCM exception: " + e.getMessage() );
 			er.setResultType( ResultType.INITIALIZE_WORKSPACE_ERROR );
 		} catch (ClearCaseException e) {
 			logger.debug( id + "Could not get changes. " + e.getMessage() );
 			logger.info( e );
 			e.print( hudsonOut );
 			er.setResultType( ResultType.INITIALIZE_WORKSPACE_ERROR );
 		}
 
 		er.setLog( log );
 		er.setMessage( diff );
 		er.setViewtag( viewtag );
 		er.setChangeset( changeset );
 		Logger.removeAppender( app );
 		return er;
 	}
 	
 	private String makeViewtag() {
 		String newJobName = jobname.replaceAll("\\s", "_");
 		String viewtag = "CCUCM_" + newJobName + "_" + System.getenv("COMPUTERNAME");
 		
 		return viewtag;
 	}
 
 	private void makeWorkspace( File workspace, String viewtag ) throws ScmException, ClearCaseException {
 		// We know we have a stream (st), because it is set in
 		// baselinesToBuild()
 		if( workspace != null ) {
 			logger.debug( id + "workspace: " + workspace.getAbsolutePath() );
 		} else {
 			logger.debug( id + "workspace must be null???" );
 		}
 
 		File viewroot = new File( workspace, "view" );
 		
 		logger.debug( id + "Creating dev strem" );
 		Stream devstream = getDeveloperStream( "stream:" + viewtag, targetStream.getPVob() );
 
 		logger.debug( id + "Making view" );
 		sv = Util.makeView( devstream, workspace, listener, loadModule, viewroot, viewtag, false );
 		
 
 		// Now we have to rebase - if a rebase is in progress, the
 		// old one must be stopped and the new started instead
 		logger.debug( id + "Checking rebasing" );
 		if( Rebase.isInProgress( devstream ) ) {
 			hudsonOut.print( "[" + Config.nameShort + "] Cancelling previous rebase." );
 			Rebase.cancelRebase( devstream );
 			hudsonOut.println( " Done" );
 		}
 		// The last boolean, complete, must always be true from CCUCM
 		// as we are always working on a read-only stream according
 		// to LAK
 		hudsonOut.print( "[" + Config.nameShort + "] Rebasing development stream (" + devstream.getShortname() + ") against parent stream (" + targetStream.getShortname() + ")" );
 		try {
 			logger.debug( id + "Rebasing" );
 			Rebase rebase = new Rebase( devstream, sv, bl );
 			rebase.rebase( true );
 			logger.debug( id + "Rebasing done" );
 		} catch( RebaseException e1 ) {
 			logger.debug( id + "Rebasing failed: " + e1.getMessage() );
 			hudsonOut.println( " Failed" );
 			throw e1;
 		}
 		
 		hudsonOut.println( " Done" );
 		
 		try {
             hudsonOut.println("[" + Config.nameShort + "] Updating view using " + loadModule.toLowerCase() + " modules");
             logger.debug( id + "Updating stream" );
             //sv.Update(true, true, true, false, Components.valueOf(loadModule.toUpperCase()), null);
             sv.Update(true, true, true, false, new LoadRules( sv, Components.valueOf(loadModule.toUpperCase()) ));
             logger.debug( id + "Updating done" );
         } catch (ClearCaseException e) {
             e.print( hudsonOut );
             throw new ScmException("Could not update snapshot view. " + e.getMessage());
         }
 	}
 
 	private Stream getDeveloperStream( String streamname, PVob pvob ) throws ScmException {
 		Stream devstream = null;
 
 		try {
			if( Stream.streamExists( streamname + "@" + pvob ) ) {
 				devstream = Stream.get( streamname, pvob );
 			} else {
 				if( buildProject.equals( "" ) ) {
 					buildProject = null;
 				}
				devstream = Stream.create( Config.getIntegrationStream( bl, buildProject ), streamname + "@" + pvob, true, bl );
 			}
 		}
 		/*
 		 * This tries to handle the issue where the project hudson is not
 		 * available
 		 */
 		catch (ScmException se) {
 			throw se;
 
 		} catch (Exception e) {
 			throw new ScmException( "Could not get stream: " + e.getMessage() );
 		}
 
 		return devstream;
 	}
 
 	public SnapshotView getSnapshotView() {
 		return sv;
 	}
 
 }
