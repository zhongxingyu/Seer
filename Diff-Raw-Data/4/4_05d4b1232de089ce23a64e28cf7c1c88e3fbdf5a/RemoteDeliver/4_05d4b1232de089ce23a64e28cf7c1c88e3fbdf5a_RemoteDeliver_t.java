 package net.praqma.hudson.remoting;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.praqma.clearcase.Deliver;
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.CleartoolException;
 import net.praqma.clearcase.exceptions.DeliverException;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.Util;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.remoting.EstablishResult.ResultType;
 import net.praqma.hudson.scm.CCUCMState;
 import net.praqma.hudson.scm.ClearCaseChangeset;
 import net.praqma.hudson.scm.CCUCMState.State;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.appenders.StreamAppender;
 
 /**
  * 
  * @author wolfgang
  * 
  */
 public class RemoteDeliver implements FileCallable<EstablishResult> {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Logger logger;
 	
 	private String jobName;
 	private String baseline;
 	private String destinationstream;
 	private BuildListener listener;
 	private String id = "";
 	private SnapshotView snapview;
 
 	/*
 	 * private boolean apply4level; private String alternateTarget; private
 	 * String baselineName;
 	 */
 	private String loadModule;
 	private PrintStream out = null;
 	private Pipe pipe;
 	private LoggerSetting loggerSetting;
 	private String viewtag = "";
 	private boolean forceDeliver;
 	private PrintStream pstream;
 	private File workspace;
 
 	public RemoteDeliver( String destinationstream, BuildListener listener, Pipe pipe, PrintStream pstream, LoggerSetting loggerSetting,
 	/* Common values */
 	String loadModule, String baseline, String jobName, boolean forceDeliver ) {
 		this.jobName = jobName;
 
 		this.baseline = baseline;
 		this.destinationstream = destinationstream;
 
 		this.listener = listener;
 		this.loadModule = loadModule;
 		
 		this.forceDeliver = forceDeliver;
 		
 		this.pstream = pstream;
 
 		this.pipe = pipe;
 		this.loggerSetting = loggerSetting;
 	}
 
 	public EstablishResult invoke( File workspace, VirtualChannel channel ) throws IOException {
 
 		out = listener.getLogger();
 		
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
 		
 		logger.debug( "Starting remote deliver" );
 
 		this.workspace = workspace;
 
 		/* Create the baseline object */
 		Baseline baseline = null;
 		try {
 			baseline = Baseline.get( this.baseline );
 			baseline.load();
 		} catch( ClearCaseException e ) {
 			Logger.removeAppender( app );
 			throw new IOException( "Could not create Baseline object: " + e.getMessage(), e );
 		}
 		
 		logger.debug( baseline + " created" );
 
 		/* Create the development stream object */
 		/* Append vob to dev stream */
 
 		Stream destinationStream = null;
 		try {
 			destinationStream = Stream.get( this.destinationstream );
 		} catch( ClearCaseException e ) {
 			Logger.removeAppender( app );
 			throw new IOException( "Could not create destination Stream object: " + e.getMessage(), e );
 		}
 		
 		logger.debug( destinationStream + " created" );
 
 		/* Make deliver view */
 		try {
 			snapview = makeDeliverView( destinationStream, workspace );
 		} catch( ScmException e ) {
 			Logger.removeAppender( app );
 			throw new IOException( "Could not create deliver view: " + e.getMessage() );
 		}
 		
 		logger.debug( "View: " + workspace );
 
 		String diff = "";
 		ClearCaseChangeset changeset = new ClearCaseChangeset();
 
 		try {
 			List<Activity> bldiff = Version.getBaselineDiff( destinationStream, baseline, true, snapview.getViewRoot() );
 			out.print( "[" + Config.nameShort + "] Found " + bldiff.size() + " activit" + ( bldiff.size() == 1 ? "y" : "ies" ) + ". " );
 
 			int c = 0;
 			for( Activity a : bldiff ) {
 				c += a.changeset.versions.size();
 				for( Version version : a.changeset.versions ) {
 					changeset.addChange( version.getFullyQualifiedName(), version.getUser() );
 				}
 			}
 			out.println( c + " version" + ( c == 1 ? "" : "s" ) + " involved" );
 			diff = Util.createChangelog( bldiff, baseline );
 		} catch( ClearCaseException e1 ) {
 			out.println( "[" + Config.nameShort + "] Unable to create change log: " + e1.getMessage() );
 		}
 		
 		logger.debug( "Changeset created" );
 
 		EstablishResult er = new EstablishResult( viewtag );
 		er.setView( snapview );
 		er.setMessage( diff );
 		er.setChangeset( changeset );
 
 		/* Make the deliver. Inline manipulation of er */
 		deliver( baseline, destinationStream, er, forceDeliver, 2 );
 
 		/* End of deliver */
 		Logger.removeAppender( app );
 		return er;
 	}
 
 	private void deliver( Baseline baseline, Stream dstream, EstablishResult er, boolean forceDeliver, int triesLeft ) throws IOException {
 		logger.verbose( "Delivering " + baseline.getShortname() + " to " + dstream.getShortname() + ". Tries left: " + triesLeft );
 		if( triesLeft < 1 ) {
 			out.println( "[" + Config.nameShort + "] Unable to deliver, giving up." );
 			er.setResultType( ResultType.DELIVER_IN_PROGRESS_NOT_CANCELLED );
 			return;
 		}
 
 		Deliver deliver = null;
 		try {
 			out.println( "[" + Config.nameShort + "] Starting deliver(tries left: " + triesLeft + ")" );
 			deliver = new Deliver( baseline, baseline.getStream(), dstream, snapview.getViewRoot(), snapview.getViewtag() );
 			deliver.deliver( true, false, true, false );
 			//baseline.deliver( baseline.getStream(), dstream, snapview.getViewRoot(), snapview.getViewtag(), true, false, true );
 			er.setResultType( ResultType.SUCCESS );
 		} catch( DeliverException e ) {
 			out.println( "[" + Config.nameShort + "] Failed to deliver: " + e.getMessage() );
 			logger.debug( "Failed to deliver: " + e.getMessage() );
 			logger.debug( e );
 			
 			/* Figure out what happened */
 			if( e.getType().equals( DeliverException.Type.REQUIRES_REBASE ) ) {
 				er.setResultType( ResultType.DELIVER_REQUIRES_REBASE );
 			}
 
 			if( e.getType().equals( DeliverException.Type.MERGE_ERROR ) ) {
 				er.setResultType( ResultType.MERGE_ERROR );
 			}
 
 			if( e.getType().equals( DeliverException.Type.INTERPROJECT_DELIVER_DENIED ) ) {
 				er.setResultType( ResultType.INTERPROJECT_DELIVER_DENIED );
 			}
 
 			if( e.getType().equals( DeliverException.Type.DELIVER_IN_PROGRESS ) ) {
 				out.println( "[" + Config.nameShort + "] Deliver already in progress" );
 
 				if( !forceDeliver ) {
 					er.setResultType( ResultType.DELIVER_IN_PROGRESS );
 				} else {
 	
 					/**
 					 * rollback deliver.. *******A DELIVER OPERATION IS ALREADY IN
 					 * PROGRESS. Details about the delivery:
 					 * 
 					 * <b Deliver operation in progress on stream
 					 * "stream:pds316_deliver_test@\PDS_PVOB"/> Started by "PDS316"
 					 * on "2011-09-28T11:23:53+02:00" Using integration activity
 					 * "deliver.pds316_deliver_test.20110928.112353". <bUsing view
 					 * "pds316_deliver_test_int" />. Baselines will be delivered to
 					 * the default target stream "stream:deliver_test_int@\PDS_PVOB"
 					 * in project "project:deliver_test@\PDS_PVOB".
 					 * 
 					 * Baselines to be delivered:
 					 * 
 					 * *******Please try again later.
 					 */
 	
 					String msg = e.getMessage();
 					String stream = "";
 					String oldViewtag = null;
 	
 					out.println( "[" + Config.nameShort + "] Forcing this deliver." );
 	
 					Pattern STREAM_PATTERN = Pattern.compile( "Deliver operation .* on stream \\\"(.*)\\\"", Pattern.MULTILINE );
 					Pattern TAG_PATTERN = Pattern.compile( "Using view \\\"(.*)\\\".", Pattern.MULTILINE );
 	
 					Matcher mSTREAM = STREAM_PATTERN.matcher( msg );
 					while( mSTREAM.find() ) {
 						stream = mSTREAM.group( 1 );
 					}
 	
 					Matcher mTAG = TAG_PATTERN.matcher( msg );
 					while( mTAG.find() ) {
 						oldViewtag = mTAG.group( 1 );
 					}
 	
 					File newView = null;
 					if( oldViewtag == null ) {
 						newView = snapview.getViewRoot();
 					} else {
 						newView = new File( workspace + "\\rm_delv_view" );
 					}
 	
 					try {
 						// rolling back the previous deliver operation
 						Stream ostream = Stream.get( stream, baseline.getPVob() ).load(); //.deliverRollBack( oldViewtag, newView );
 						Deliver.rollBack( oldViewtag, ostream, newView );
 					} catch( ClearCaseException ex ) {
 						out.println( ex.getMessage() );
 						throw new IOException( ex.getMessage(), ex.getCause() );
 					}
 	
 					// Recursive method call of INVOKE(...);
 					logger.verbose( "Trying to deliver again..." );
 					deliver( baseline, dstream, er, forceDeliver, triesLeft - 1 );
 				}
 			}
 		} catch( CleartoolException e ) {
 			logger.warning( "Unable to get status from stream: " + e.getMessage() );
 			throw new IOException( e );
 		}
 	}
 
 	private SnapshotView makeDeliverView( Stream stream, File workspace ) throws ScmException {
 		/* Replace evil characters with less evil characters */
 		String newJobName = jobName.replaceAll( "\\s", "_" );
 
 		viewtag = "CCUCM_" + newJobName + "_" + System.getenv( "COMPUTERNAME" ) + "_" + stream.getShortname();
 
 		File viewroot = new File( workspace, "view" );
 
 		return Util.makeView( stream, workspace, listener, loadModule, viewroot, viewtag );
 	}
 
 	public SnapshotView getSnapShotView() {
 		return this.snapview;
 	}
 }
