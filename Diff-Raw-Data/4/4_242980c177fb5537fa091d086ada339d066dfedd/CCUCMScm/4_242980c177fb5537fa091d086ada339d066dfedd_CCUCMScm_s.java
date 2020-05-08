 package net.praqma.hudson.scm;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.remoting.Future;
 import hudson.remoting.Pipe;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCM;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Project.Plevel;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.Util;
 import net.praqma.hudson.exception.CCUCMException;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.exception.TemplateException;
 import net.praqma.hudson.nametemplates.NameTemplate;
 import net.praqma.hudson.notifier.CCUCMNotifier;
 import net.praqma.hudson.remoting.*;
 import net.praqma.hudson.scm.Polling.PollingType;
 import net.praqma.hudson.scm.CCUCMState.State;
 import net.praqma.util.StopWatch;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.Logger.LogLevel;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.appenders.Appender;
 import net.praqma.util.debug.appenders.FileAppender;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.export.Exported;
 
 /**
  * is responsible for everything regarding Hudsons connection to ClearCase
  * pre-build. This class defines all the files required by the user. The
  * information can be entered on the config page.
  * 
  * @author Troels Selch
  * @author Margit Bennetzen
  * 
  */
 public class CCUCMScm extends SCM {
 
 	private Project.Plevel plevel;
 	private String levelToPoll;
 	private String loadModule;
 	private String component;
 	private String stream;
 	private String bl;
 	private StringBuffer pollMsgs = new StringBuffer();
 	private Stream integrationstream;
 	private boolean doPostBuild = true;
 	private String buildProject;
 	private String jobName = "";
 	private Integer jobNumber;
 	private String id = "";
 	transient private Logger logger = null;
 	public static CCUCMState ccucm = new CCUCMState();
 	private boolean forceDeliver;
 
 	private Appender app;
 
 	/* Old notifier fields */
 	private boolean recommend;
 	private boolean makeTag;
 	private boolean setDescription;
 	private Unstable treatUnstable;
 	private boolean createBaseline;
 	private String nameTemplate;
 
 	/* Threshold in milliseconds */
 	public static final long __CCUCM_STORED_BASELINES_THRESHOLD = 5 * 60 * 1000;
 	public static final String CCUCM_LOGGER_STRING = "include_classes";
 	private Polling polling;
 	private String viewtag = "";
 	private Set<String> subs;
 	private Baseline lastBaseline;
 
 	private RemoteUtil rutil;
 	private LoggerSetting loggerSetting;
 
 	private static DateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd" );
 
 	/**
 	 * Default constructor, mainly used for unit tests.
 	 */
 	public CCUCMScm() {
 	}
 
 	/**
 	 * 
 	 * @param component
 	 * @param levelToPoll
 	 * @param loadModule
 	 * @param newest
 	 * @param polling
 	 * @param stream
 	 * @param versionFrom
 	 * @param nameTemplate
 	 * @param buildProject
 	 * @param multiSite
 	 */
 	@DataBoundConstructor
 	public CCUCMScm( String component, String levelToPoll, String loadModule, boolean newest, String polling, String stream, String treatUnstable /*
 																																				 * Baseline
 																																				 * creation
 																																				 */, boolean createBaseline, String nameTemplate, boolean forceDeliver /*
 																																																						 * Notifier
 																																																						 * options
 																																																						 */, boolean recommend, boolean makeTag, boolean setDescription /*
 																																																																						 * Build
 																																																																						 * options
 																																																																						 */, String buildProject ) {
 
 		this.component = component;
 		this.levelToPoll = levelToPoll;
 		this.loadModule = loadModule;
 		this.stream = stream;
 		this.buildProject = buildProject;
 
 		this.polling = new Polling( polling );
 		this.treatUnstable = new Unstable( treatUnstable );
 
 		this.createBaseline = createBaseline;
 		this.nameTemplate = nameTemplate;
 
 		this.forceDeliver = forceDeliver;
 		this.recommend = recommend;
 		this.makeTag = makeTag;
 		this.setDescription = setDescription;
 		this.plevel = Util.getLevel( levelToPoll );
 	}
 
 	@Override
 	public boolean checkout( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile ) throws IOException, InterruptedException {
 		/* Prepare job variables */
 
 		jobName = build.getParent().getDisplayName().replace( ' ', '_' );
 		jobNumber = build.getNumber();
 		this.id = "[" + jobName + "::" + jobNumber + "]";
 
 		boolean result = true;
 
 		PrintStream consoleOutput = listener.getLogger();
 
 		consoleOutput.println( "[" + Config.nameShort + "] ClearCase UCM Plugin version " + net.praqma.hudson.Version.version );
 		consoleOutput.println( "[" + Config.nameShort + "] Forcing deliver: " + forceDeliver );
 
 		/* Preparing the logger */
 		logger = Logger.getLogger();
 		File logfile = new File( build.getRootDir(), "ccucmSCM.log" );
 		app = new FileAppender( logfile );
 		// app.setTag(id);
 		net.praqma.hudson.Util.initializeAppender( build, app );
 		Logger.addAppender( app );
 		this.loggerSetting = Logger.getLoggerSettings( app.getMinimumLevel() );
 		this.rutil = new RemoteUtil( loggerSetting, app );
 
 		logger.verbose( "Number of appenders: " + Logger.getNumberOfAppenders() );
 
 		logger.info( id + "CCUCMSCM checkout v. " + net.praqma.hudson.Version.version, id );
 
 		/* Recalculate the states */
 		int count = ccucm.recalculate( build.getProject() );
 		logger.debug( id + "Removed " + count + " from states.", id );
 
 		doPostBuild = true;
 
 		logger.debug( "STATES: " + ccucm.stringify() );
 		/* If we polled, we should get the same object created at that point */
 		State state = null;
 		try {
 			state = ccucm.getState( jobName, jobNumber );
 			logger.debug( "The existing state is: " + state.stringify() );
 		} catch( IllegalStateException e ) {
 			logger.debug( e.getMessage() );
 			state = ccucm.create( jobName, jobNumber );
 		}
 
 		state.setLoadModule( loadModule );
 		storeStateParameters( state );
 
 		logger.info( "Number of states: " + ccucm.size() );
 
 		logger.debug( id + "The initial state:\n" + state.stringify(), id );
 
 		/* Check template */
 		state.setCreatebaseline( createBaseline );
 		/* Trim template, strip out quotes */
 		if( nameTemplate.matches( "^\".+\"$" ) ) {
 			nameTemplate = nameTemplate.substring( 1, nameTemplate.length() - 1 );
 		}
 		state.setNameTemplate( nameTemplate );
 
 		/* Check input */
 		if( !checkInput( listener ) ) {
 			state.setPostBuild( false );
 			Logger.removeAppender( app );
 			return false;
 		}
 
 		// state.setLoggerSetting( Logger.getLoggerSettings(
 		// app.getMinimumLevel() ) );
 
 		/* Determining the Baseline modifier */
 		String baselineInput = getBaselineValue( build );
 
 		/* The special Baseline case */
 		if( build.getBuildVariables().get( baselineInput ) != null ) {
 			logger.debug( "BASELINE: " + baselineInput, id );
 			polling = new Polling( PollingType.none );
 			result = doBaseline( build, baselineInput, state, listener );
 		} else {
 			consoleOutput.println( "[" + Config.nameShort + "] Polling streams: " + polling.toString() );
 			result = pollStream( workspace, build.getProject(), state, listener );
 			if( !result ) {
 				consoleOutput.println( "[" + Config.nameShort + "] No valid baselines found" );
 			}
 		}
 
 		state.setPolling( polling );
 
 		/* If a baseline is found */
 		if( state.getBaseline() != null && result ) {
 			consoleOutput.println( "[" + Config.nameShort + "] Using " + state.getBaseline() );
 			/*
 			 * if( setDescription ) { build.setDescription("<small>" +
 			 * state.getBaseline().getShortname() + "</small>"); }
 			 */
 
 			if( polling.isPollingSelf() || !polling.isPolling() ) {
 				result = initializeWorkspace( build, workspace, changelogFile, listener, state );
 				if( plevel == null ) {
 					/* Save */
 					storeLastBaseline( state.getBaseline(), build.getProject() );
 				}
 			} else {
 				/* Only start deliver when NOT polling self */
 				result = beginDeliver( build, state, listener, changelogFile );
 			}
 
 		}
 
 		consoleOutput.println( "[" + Config.nameShort + "] Pre build steps done" );
 
 		/* If plevel is not null, make sure that the CCUCMNotofier is ON */
 		if( plevel != null ) {
 			boolean used = false;
 			for( Publisher p : build.getParent().getPublishersList() ) {
 				logger.debug( "NOTIFIER: " + p.toString(), id );
 				if( p instanceof CCUCMNotifier ) {
 					used = true;
 					break;
 				}
 			}
 
 			if( !used ) {
 				logger.info( "Adding notifier to project", id );
 				build.getParent().getPublishersList().add( new CCUCMNotifier() );
 			}
 			/* If plevel is null, make sure CCUCMNotofier is not enabled */
 		} else {
 			Iterator<Publisher> it = build.getParent().getPublishersList().iterator();
 			while( it.hasNext() ) {
 				Publisher p = it.next();
 				if( p instanceof CCUCMNotifier ) {
 					it.remove();
 				}
 			}
 
 			/* End the view */
 			try {
 				logger.debug( "Ending view " + viewtag );
 				rutil.endView( build.getWorkspace(), viewtag );
 			} catch( CCUCMException e ) {
 				consoleOutput.println( e.getMessage() );
 				logger.warning( e.getMessage() );
 			}
 
 			/* Removing the state from the list */
 			boolean done = state.remove();
 			logger.debug( id + "Removing job " + build.getNumber() + " from collection: " + done, id );
 		}
 
 		logger.debug( "FINAL STATES: " + ccucm.stringify() );
 
 		Logger.removeAppender( app );
 		return result;
 	}
 
 	private boolean storeLastBaseline( Baseline baseline, AbstractProject<?, ?> project ) {
 		FileWriter fw = null;
 		try {
 			fw = new FileWriter( new File( project.getRootDir(), ".lastbaseline" ), false );
 			fw.write( baseline.getFullyQualifiedName() );
 		} catch( IOException e ) {
 			logger.warning( "Could not write last baseline" );
 			return false;
 		} finally {
 			if( fw != null ) {
 				try {
 					fw.close();
 				} catch( IOException e ) {
 					logger.warning( "Unable to close file" );
 				}
 			}
 		}
 
 		return true;
 	}
 
 	private Baseline getLastBaseline( AbstractProject<?, ?> project, TaskListener listener ) throws ScmException {
 		FileReader fr = null;
 		PrintStream out = listener.getLogger();
 		try {
 			fr = new FileReader( new File( project.getRootDir(), ".lastbaseline" ) );
 			BufferedReader br = new BufferedReader( fr );
 			String bls = br.readLine();
 			logger.debug( "Read " + bls );
 			if( bls == null || bls.length() == 0 ) {
 				throw new ScmException( "No last baseline stored" );
 			}
 			Baseline bl = UCMEntity.getBaseline( bls, true );
 			Baseline loaded = (Baseline) rutil.loadEntity( project.getSomeWorkspace(), bl, getSlavePolling() );
 			return loaded;
 		} catch( FileNotFoundException e ) {
 		} catch( IOException e ) {
 			logger.warning( "Could not read last baseline" );
 			throw new ScmException( "Could not read last baseline" );
 		} catch( UCMException e ) {
 			logger.warning( "Unable to get last baseline!" );
 			throw new ScmException( "Unable to get last baseline" );
 			// } catch( CCUCMException e ) {
 			// logger.warning( "Unable to load last baseline" );
 			// throw new ScmException( "Unable to load last baseline" );
 		} catch( CCUCMException e ) {
 			out.println( "Unable to load entity: " + e.getMessage() );
 			e.printStackTrace();
 		} finally {
 			if( fr != null ) {
 				try {
 					fr.close();
 				} catch( IOException e ) {
 					logger.warning( "Unable to close file" );
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private boolean checkInput( TaskListener listener ) {
 		PrintStream out = listener.getLogger();
 
 		/* Check baseline template */
 		if( createBaseline ) {
 			/* Sanity check */
 			if( polling.isPollingOther() ) {
 				if( nameTemplate != null && nameTemplate.length() > 0 ) {
 					try {
 						NameTemplate.testTemplate( nameTemplate );
 					} catch( TemplateException e ) {
 						out.println( "[" + Config.nameShort + "] The template could not be parsed correctly: " + e.getMessage() );
 						return false;
 					}
 				} else {
 					out.println( "[" + Config.nameShort + "] A valid template must be provided to create a Baseline" );
 					return false;
 				}
 			} else {
 				out.println( "[" + Config.nameShort + "] You cannot create a baseline in this mode" );
 			}
 		}
 
 		/* Check polling vs plevel */
 		if( plevel == null ) {
 			if( polling.isPollingSelf() ) {
 				return true;
 			} else {
 				out.println( "[" + Config.nameShort + "] You cannot poll any on other than self" );
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private boolean initializeWorkspace( AbstractBuild<?, ?> build, FilePath workspace, File changelogFile, BuildListener listener, State state ) {
 
 		PrintStream consoleOutput = listener.getLogger();
 
 		EstablishResult er = null;
 		try {
 
 			Future<EstablishResult> i = null;
 			if( workspace.isRemote() ) {
 				final Pipe pipe = Pipe.createRemoteToLocal();
 				CheckoutTask ct = new CheckoutTask( listener, jobName, build.getNumber(), state.getStream(), loadModule, state.getBaseline(), buildProject, ( plevel == null ), pipe, Logger.getLoggerSettings( LogLevel.DEBUG ) );
 				i = workspace.actAsync( ct );
 				app.write( pipe.getIn() );
 			} else {
 				CheckoutTask ct = new CheckoutTask( listener, jobName, build.getNumber(), state.getStream(), loadModule, state.getBaseline(), buildProject, ( plevel == null ), null, Logger.getLoggerSettings( LogLevel.DEBUG ) );
 				i = workspace.actAsync( ct );
 			}
 			er = i.get();
 			String changelog = er.getMessage();
 
 			this.viewtag = er.getViewtag();
 			state.setChangeset( er.getChangeset() );
 
 			/* Write change log */
 			try {
 				FileOutputStream fos = new FileOutputStream( changelogFile );
 				fos.write( changelog.getBytes() );
 				fos.close();
 			} catch( IOException e ) {
 				logger.debug( id + "Could not write change log file", id );
 				consoleOutput.println( "[" + Config.nameShort + "] Could not write change log file" );
 			}
 
 		} catch( Exception e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] An error occured: " + e.getMessage() );
 			logger.warning( e, id );
 			e.printStackTrace( consoleOutput );
 			doPostBuild = false;
 			state.setPostBuild( false );
 			return false;
 		}
 
 		if( er == null || er.isFailed() ) {
 			consoleOutput.println( "[" + Config.nameShort + "] Could not establish workspace: " + er );
 			state.setPostBuild( false );
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public boolean doBaseline( AbstractBuild<?, ?> build, String baselineInput, State state, BuildListener listener ) {
 
 		PrintStream consoleOutput = listener.getLogger();
 		boolean result = true;
 
 		String baselinename = (String) build.getBuildVariables().get( baselineInput );
 		try {
 			state.setBaseline( UCMEntity.getBaseline( baselinename ) );
 			state.setStream( state.getBaseline().getStream() );
 			consoleOutput.println( "[" + Config.nameShort + "] Starting parameterized build with a CCUCM_baseline." );
 			/* The component could be used in the post build section */
 			state.setComponent( state.getBaseline().getComponent() );
 			state.setStream( state.getBaseline().getStream() );
 			logger.debug( id + "Saving the component for later use", id );
 		} catch( UCMException e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] Could not find baseline from parameter '" + baselinename + "'." );
 			state.setPostBuild( false );
 			result = false;
 			state.setBaseline( null );
 		}
 
 		return result;
 	}
 
 	public String getBaselineValue( AbstractBuild<?, ?> build ) {
 		Collection<?> c = build.getBuildVariables().keySet();
 		Iterator<?> i = c.iterator();
 
 		while( i.hasNext() ) {
 			String next = i.next().toString();
 			if( next.equalsIgnoreCase( "baseline" ) ) {
 				return next;
 			}
 		}
 
 		return null;
 	}
 
 	public class AscendingDateSort implements Comparator<Baseline> {
 
 		@Override
 		public int compare( Baseline bl1, Baseline bl2 ) {
 			if( bl2.getDate() == null ) {
 				return 1;
 			}
 			if( bl1.getDate() == null ) {
 				return -1;
 			}
 			return (int) ( ( bl1.getDate().getTime() / 1000 ) - ( bl2.getDate().getTime() / 1000 ) );
 		}
 	}
 
 	private boolean pollStream( FilePath workspace, AbstractProject<?, ?> project, State state, BuildListener listener ) {
 		boolean result = true;
 		PrintStream out = listener.getLogger();
 
 		try {
 
 			printParameters( out );
 			state.setStream( UCMEntity.getStream( stream ) );
 
 			if( !state.isAddedByPoller() ) {
 
 				List<Baseline> baselines = null;
 				/* Old skool self polling */
 				if( polling.isPollingSelf() ) {
 					baselines = getValidBaselinesFromStream( workspace, state, plevel, state.getStream(), state.getComponent() );
 				} else {
 					/* Find the Baselines and store them */
 					baselines = getBaselinesFromStreams( workspace, listener, out, state, state.getStream(), state.getComponent(), polling.isPollingChilds() );
 				}
 
 				int total = baselines.size();
 				int pruned = filterBaselines( baselines );
 				baselines = filterBuildingBaselines( project, baselines );
 
 				if( pruned > 0 ) {
 					out.println( "[" + Config.nameShort + "] Removed " + pruned + " of " + total + " Baselines." );
 				}
 
 				/* if we did not find any baselines we should return false */
 				if( baselines.size() < 1 ) {
 					return false;
 				}
 
 				/* Sort by date */
 				Collections.sort( baselines, new AscendingDateSort() );
 
 				state.setBaselines( baselines );
 				state.setBaseline( selectBaseline( state.getBaselines(), plevel ) );
 			}
 
 			if( state.getBaselines() == null || state.getBaselines().size() < 1 ) {
 				return false;
 			}
 
 			/* Print the baselines to jenkins out */
 			printBaselines( state.getBaselines(), out );
 			out.println( "" );
 
 		} catch( Exception e ) {
 			out.println( "[" + Config.nameShort + "] " + e.getMessage() );
 			logger.warning( e, id );
 			return false;
 		}
 
 		return result;
 	}
 
 	public boolean beginDeliver( AbstractBuild<?, ?> build, State state, BuildListener listener, File changelogFile ) {
 		FilePath workspace = build.getWorkspace();
 		PrintStream consoleOutput = listener.getLogger();
 		boolean result = true;
 
 		EstablishResult er = new EstablishResult();
 
 		try {
 			// logger.debug( "Remote delivering...." );
 			// consoleOutput.println("[" + Config.nameShort +
 			// "] Establishing deliver view");
 			// RemoteDeliver rmDeliver = new
 			// RemoteDeliver(UCMEntity.getStream(stream).getFullyQualifiedName(),
 			// listener, component, loadModule,
 			// state.getBaseline().getFullyQualifiedName(),
 			// build.getParent().getDisplayName());
 			RemoteDeliver rmDeliver = null;
 			logger.verbose( "Starting remote deliver" );
 			if( workspace.isRemote() ) {
 				final Pipe pipe = Pipe.createRemoteToLocal();
 				Future<EstablishResult> i = null;
 				rmDeliver = new RemoteDeliver( state.getStream().getFullyQualifiedName(), listener, pipe, loggerSetting, loadModule, state.getBaseline().getFullyQualifiedName(), build.getParent().getDisplayName(), state.getForceDeliver() );
 				i = workspace.actAsync( rmDeliver );
 				app.write( pipe.getIn() );
 				er = i.get();
 
 			} else {
 				rmDeliver = new RemoteDeliver( state.getStream().getFullyQualifiedName(), listener, null, loggerSetting, loadModule, state.getBaseline().getFullyQualifiedName(), build.getParent().getDisplayName(), state.getForceDeliver() );
 				er = workspace.act( rmDeliver );
 			}
 
 			state.setSnapView( er.getView() );
 			this.viewtag = er.getViewtag();
 
 			/* Write change log */
 			try {
 				// consoleOutput.println("[" + Config.nameShort +
 				// "] DIFF ON MASTER: " + er.getMessage());
 				FileOutputStream fos = new FileOutputStream( changelogFile );
 				if( er.isSuccessful() ) {
 					fos.write( er.getMessage().getBytes() );
 				} else {
 					fos.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><changelog></changelog>".getBytes() );
 				}
 				fos.close();
 			} catch( IOException e ) {
 				logger.debug( id + "Could not write change log file", id );
 				consoleOutput.println( "[" + Config.nameShort + "] Could not write change log file" );
 			}
 
 		} catch( IOException e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] " + e.getMessage() );
 			logger.warning( e, id );
 			result = false;
 		} catch( InterruptedException e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] " + e.getMessage() );
 			logger.warning( e, id );
 			result = false;
 		} catch( ExecutionException e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] " + e.getMessage() );
 			logger.warning( e, id );
 			result = false;
 		}
 
 		logger.debug( id + "RESULT: " + er.getResultType().toString(), id );
 		state.setChangeset( er.getChangeset() );
 
 		/* The result must be false if the returnStatus is not equal to 0 */
 		if( er.isFailed() ) {
 			logger.debug( id + "Result is false!", id );
 			result = false;
 		}
 
 		/*
 		 * If returnStatus == 1|2|3 the deliver was not started and should not
 		 * be tried cancelled later Perhaps isFailed could be used?
 		 */
 		if( er.isDeliverRequiresRebase() || er.isInterprojectDeliverDenied() || !er.isStarted() ) {
 			logger.debug( id + "No need for completing deliver", id );
 			state.setNeedsToBeCompleted( false );
 		}
 
 		consoleOutput.println( "[" + Config.nameShort + "] Deliver " + ( result ? "succeeded" : "failed" ) );
 		/*
 		 * If failed, cancel the deliver But only if the deliver actually
 		 * started and can be cancelled
 		 */
 		if( !result && er.isCancellable() ) {
 			try {
 				consoleOutput.print( "[" + Config.nameShort + "] Cancelling deliver. " );
 				// RemoteUtil.completeRemoteDeliver(workspace, listener, state,
 				// false);
 				rutil.completeRemoteDeliver( workspace, listener, state, false );
 				consoleOutput.println( "Success" );
 
 				/* Make sure, that the post step is not run */
 				state.setNeedsToBeCompleted( false );
 
 			} catch( Exception e ) {
 				consoleOutput.println( "Failed" );
 				logger.warning( e, id );
 				consoleOutput.println( "[" + Config.nameShort + "] " + e.getMessage() );
 			}
 		}
 
 		/* Write something useful to the output */
 		if( er.isMergeError() ) {
 			try {
 				consoleOutput.println( "[" + Config.nameShort + "] Changes need to be manually merged, The stream " + state.getBaseline().getStream().getShortname() + " must be rebased to the most recent baseline on " + state.getStream().getShortname() + " - During the rebase the merge conflict should be solved manually. Hereafter create a new baseline on " + state.getBaseline().getStream().getShortname() + "." );
 				state.setError( "merge error" );
 			} catch( Exception e ) {
 			}
 		}
 
 		try {
 			state.setStream( UCMEntity.getStream( stream ) );
 		} catch( UCMException e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] " + e.getMessage() );
 			logger.warning( e, id );
 			result = false;
 		}
 
 		return result;
 	}
 
 	@Override
 	public ChangeLogParser createChangeLogParser() {
 		return new ChangeLogParserImpl();
 	}
 
 	@Override
 	public void buildEnvVars( AbstractBuild<?, ?> build, Map<String, String> env ) {
 		super.buildEnvVars( build, env );
 
 		State state = ccucm.getState( jobName, jobNumber );
 
 		if( state.getBaseline() != null ) {
 			env.put( "CC_BASELINE", state.getBaseline().getFullyQualifiedName() );
 		} else {
 			env.put( "CC_BASELINE", "" );
 		}
 
 		env.put( "CC_VIEWTAG", viewtag );
 		String workspace = env.get( "WORKSPACE" );
 		if( workspace != null ) {
 			env.put( "CC_VIEWPATH", workspace + File.separator + "view" );
 		}
 	}
 
 	/**
 	 * This method polls the version control system to see if there are any
 	 * changes in the source code.
 	 * 
 	 */
 	@Override
 	public PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState rstate ) throws IOException, InterruptedException {
 
 		jobName = project.getDisplayName().replace( ' ', '_' );
 		jobNumber = project.getNextBuildNumber();
 		this.id = "[" + jobName + "::" + jobNumber + "]";
 
 		PollingResult p = PollingResult.NO_CHANGES;
 
 		FileAppender app = null;
 		/* Preparing the logger */
 		logger = Logger.getLogger();
 
 		/* Clean polling log */
 		File logdir = new File( project.getRootDir(), "ccucm-poll-logs" );
 		logdir.mkdir();
 		File[] logs = logdir.listFiles( new FilenameFilter() {
 			public boolean accept( File file, String name ) {
 				return name.endsWith( ".log" );
 			}
 		} );
 
 		if( logs.length > 1 ) {
 			/* If there exists some logs, delete those older than seven days */
 			Date seven = new Date();
 			Calendar cal = Calendar.getInstance();
			//cal.add( Calendar.DATE, -7 );
			cal.add( Calendar.MINUTE, -1 );
 
 			for( File log : logs ) {
 				int l = log.getName().length();
 				String date = log.getName().substring( l - 12, l - 4 );
 				//System.out.println( "DATE FOR " + jobName + " " + log + ": " + date );
 				try {
 					Date d = dateFormatter.parse( date );
 					if( d.before( cal.getTime() ) ) {
 						if( !log.delete() ) {
 							logger.warning( "Unable to delete " + log );
 						}
 					}
 				} catch( ParseException e ) {
 					logger.warning( "Unable to parse date: " + e.getMessage() );
 				}
 			}
 		}
 
 		String d = dateFormatter.format( new Date() );
 		File logfile = new File( logdir, jobName + "-" + jobNumber + "." + d + ".log" );
 		app = new FileAppender( logfile );
 		app.lockToCurrentThread();
 
 		/* If nothing is known, we log all */
 		if( project.isParameterized() ) {
 			net.praqma.hudson.Util.initializeAppender( project.getLastBuild(), app );
 		} else {
 			app.setEnabled( true );
 			app.setMinimumLevel( LogLevel.DEBUG );
 			app.setSubscribeAll( true );
 		}
 		Logger.addAppender( app );
 		this.rutil = new RemoteUtil( Logger.getLoggerSettings( app.getMinimumLevel() ), app );
 
 		logger.info( "Total number of states: " + ccucm.size() );
 		logger.debug( "THE STATES: " + ccucm.stringify() );
 		List<State> states = ccucm.getStates( jobName );
 		/* This is only interesting if there're any states */
 		logger.debug( "I got " + states.size() + " number of states for " + jobName );
 		if( states.size() > 0 ) {
 			for( State s : states ) {
 				try {
 					logger.debug( "Checking " + s.getJobNumber() );
 					Integer bnum = s.getJobNumber();
 					Object o = project.getBuildByNumber( bnum );
 					Build<?, ?> bld = (Build<?, ?>) o;
 					if( bld.hasntStartedYet() ) {
 						logger.debug( s.getJobNumber() + " is still waiting in queue, no need for polling" );
 						Logger.removeAppender( app );
 						return p;
 					}
 				} catch( Exception e ) {
 					logger.debug( "The state " + s.getJobNumber() + " threw " + e.getMessage() );
 				}
 			}
 		}
 		
 
 		boolean createdByThisPoll = false;
 		State state = null;
 		try {
 			state = ccucm.getState( jobName, jobNumber );
 			logger.debug( "The existing state is: " + state.stringify() );
 			logger.info( "Let's NOT poll" );
 			System.out.println( "Undo polling for " + jobName + " " + jobNumber );
 			Logger.removeAppender( app );
 			return p;
 		} catch( IllegalStateException e ) {
 			logger.debug( e.getMessage() );
 			state = ccucm.create( jobName, jobNumber );
 			createdByThisPoll = true;
 		}
 		
 		logger.debug( "Need for polling" );
 
 		storeStateParameters( state );
 
 		PrintStream out = listener.getLogger();
 		printParameters( out );
 
 		out.println( "[" + Config.nameShort + "] polling streams: " + polling );
 
 		state.setCreatebaseline( createBaseline );
 		/* Trim template, strip out quotes */
 		if( nameTemplate.matches( "^\".+\"$" ) ) {
 			nameTemplate = nameTemplate.substring( 1, nameTemplate.length() - 1 );
 		}
 		state.setNameTemplate( nameTemplate );
 
 		StopWatch sw = StopWatch.get( jobName + "-" + jobNumber );
 		sw.reset();
 		sw.start();
 
 		logger.debug( "Let's go!" );
 
 		/* Check input */
 		if( checkInput( listener ) ) {
 			try {
 				List<Baseline> baselines = null;
 
 				/* Old skool self polling */
 				if( polling.isPollingSelf() ) {
 					baselines = getValidBaselinesFromStream( workspace, state, plevel, state.getStream(), state.getComponent() );
 				} else {
 					/* Find the Baselines and store them */
 					baselines = getBaselinesFromStreams( workspace, listener, out, state, state.getStream(), state.getComponent(), polling.isPollingChilds() );
 				}
 
 				logger.debug( "I found " + baselines.size() + " baseline" + ( baselines.size() == 1 ? "" : "s" ) );
 
 				/* Discard baselines */
 				filterBaselines( baselines );
 				baselines = filterBuildingBaselines( project, baselines );
 
 				logger.debug( "When filtered, I have " + baselines.size() + " baseline" + ( baselines.size() == 1 ? "" : "s" ) );
 
 				if( baselines.size() > 0 ) {
 					p = PollingResult.BUILD_NOW;
 					/* Sort by date */
 					Collections.sort( baselines, new AscendingDateSort() );
 
 					state.setBaselines( baselines );
 					state.setBaseline( selectBaseline( state.getBaselines(), plevel ) );
 
 					/* If ANY */
 					if( plevel == null ) {
 						try {
 							lastBaseline = getLastBaseline( project, listener );
 						} catch( ScmException e ) {
 							out.println( e.getMessage() );
 						}
 
 						boolean newer = false;
 
 						/*
 						 * if the newest found baseline is newer than the last
 						 * baseline, build it If there's no last baseline, build
 						 * it
 						 */
 						if( lastBaseline != null ) {
 
 							try {
 								out.println( "The last baseline: " + lastBaseline.stringify() );
 							} catch( Exception e ) {
 								out.println( "Could not stringify last: " + e.getMessage() );
 								e.printStackTrace( out );
 							}
 
 							try {
 								out.println( "The found baseline: " + state.getBaseline().stringify() );
 							} catch( Exception e ) {
 								out.println( "Could not stringify state baseline" );
 							}
 
 							// if( lastBaseline.getDate().after(
 							// state.getBaseline().getDate() ) ) {
 							if( state.getBaseline().getDate().after( lastBaseline.getDate() ) ) {
 								// if
 								// (lastBaseline.getFullyQualifiedName().equals(state.getBaseline().getFullyQualifiedName()))
 								// {
 								newer = true;
 							}
 						} else {
 							newer = true;
 						}
 
 						if( !newer ) {
 							p = PollingResult.NO_CHANGES;
 						}
 					}
 
 				} else {
 					p = PollingResult.NO_CHANGES;
 				}
 
 				logger.debug( id + "The POLL state:\n" + state.stringify(), id ); // 5413_dev_from_BUILT
 
 				/* Remove state if not being built */
 				if( p == PollingResult.NO_CHANGES ) {
 					state.remove();
 				}
 			} catch( ScmException e ) {
 				out.println( "Error while retrieving baselines: " + e.getMessage() );
 				logger.warning( "Error while retrieving baselines: " + e.getMessage(), id );
 				p = PollingResult.NO_CHANGES;
 			}
 		}
 
 		/* Remove state if not being built */
 		if( p.equals( PollingResult.NO_CHANGES ) ) {
 			logger.debug( id + "No new baselines to build", id );
 			if( createdByThisPoll ) {
 				logger.debug( id + "Removing: " + state.stringify(), id );
 				state.remove();
 			}
 		} else {
 			state.setAddedByPoller( true );
 		}
 
 		sw.stop();
 		logger.debug( "Polling took " + sw.getSeconds() + " seconds" );
 		out.println( "Polling took " + sw.getSeconds() + " seconds" );
 		sw.delete();
 
 		Logger.removeAppender( app );
 		return p;
 	}
 
 	/**
 	 * Get the {@link Baseline}s from a {@link Stream}s related Streams.
 	 * 
 	 * @param build
 	 * @param consoleOutput
 	 * @param state
 	 * @return
 	 */
 	private List<Baseline> getBaselinesFromStreams( FilePath workspace, TaskListener listener, PrintStream consoleOutput, State state, Stream stream, Component component, boolean pollingChildStreams ) {
 
 		List<Stream> streams = null;
 		List<Baseline> baselines = new ArrayList<Baseline>();
 
 		try {
 			streams = rutil.getRelatedStreams( workspace, listener, stream, pollingChildStreams, this.getSlavePolling() );
 		} catch( CCUCMException e1 ) {
 			e1.printStackTrace( consoleOutput );
 			logger.warning( "Could not retrieve streams: " + e1.getMessage(), id );
 			consoleOutput.println( "[" + Config.nameShort + "] No streams found" );
 			return baselines;
 		}
 
 		consoleOutput.println( "[" + Config.nameShort + "] Scanning " + streams.size() + " stream" + ( streams.size() == 1 ? "" : "s" ) + " for baselines." );
 
 		int c = 1;
 		for( Stream s : streams ) {
 			try {
 				consoleOutput.printf( "[" + Config.nameShort + "] [%02d] %s ", c, s.getShortname() );
 				c++;
 				// List<Baseline> found = getValidBaselinesFromStream(project,
 				// state, Project.getPlevelFromString(levelToPoll), s,
 				// component);
 				List<Baseline> found = rutil.getRemoteBaselinesFromStream( workspace, component, s, plevel, this.getSlavePolling() );
 				for( Baseline b : found ) {
 					baselines.add( b );
 				}
 				consoleOutput.println( found.size() + " baseline" + ( found.size() == 1 ? "" : "s" ) + " found" );
 			} catch( CCUCMException e ) {
 				consoleOutput.println( "No baselines: " + e.getMessage() );
 			}
 		}
 
 		consoleOutput.println( "" );
 
 		return baselines;
 	}
 
 	/**
 	 * Given the {@link Stream}, {@link Component} and {@link Plevel} a list of
 	 * valid {@link Baseline}s is returned.
 	 * 
 	 * @param project
 	 *            The jenkins project
 	 * @param state
 	 *            The CCUCM {@link State}
 	 * @param plevel
 	 *            The {@link Plevel}
 	 * @param stream
 	 *            {@link Stream}
 	 * @param component
 	 *            {@link Component}
 	 * @return A list of {@link Baseline}s
 	 * @throws ScmException
 	 */
 	private List<Baseline> getValidBaselinesFromStream( FilePath workspace, State state, Project.Plevel plevel, Stream stream, Component component ) throws ScmException {
 		logger.debug( id + "Retrieving valid baselines.", id );
 
 		/* The baseline list */
 		List<Baseline> baselines = new ArrayList<Baseline>();
 
 		try {
 			baselines = rutil.getRemoteBaselinesFromStream( workspace, component, stream, plevel, this.getSlavePolling() );
 		} catch( CCUCMException e1 ) {
 			// throw new ScmException("Unable to get baselines from " +
 			// stream.getShortname() + ": " + e1.getMessage());
 			/* no op */
 			logger.debug( "No baselines: " + e1.getMessage() );
 		}
 
 		return baselines;
 	}
 
 	/**
 	 * Discards baselines being build
 	 * 
 	 * @param project
 	 * @param baselines
 	 * @return
 	 * @throws ScmException
 	 */
 	public List<Baseline> filterBuildingBaselines( AbstractProject<?, ?> project, List<Baseline> baselines ) throws ScmException {
 		logger.debug( id + "CCUCM=" + ccucm.stringify(), id );
 
 		List<Baseline> validBaselines = new ArrayList<Baseline>();
 
 		/* For each baseline in the list */
 		for( Baseline b : baselines ) {
 			/* Get the state for the current baseline */
 			State cstate = ccucm.getStateByBaseline( jobName, b.getFullyQualifiedName() );
 			logger.debug( "CSTATE: " + cstate );
 
 			/*
 			 * The baseline is in progress, determine if the job is still
 			 * running
 			 */
 			if( cstate != null ) {
 				Integer bnum = cstate.getJobNumber();
 				Object o = project.getBuildByNumber( bnum );
 				Build bld = (Build) o;
 
 				/* prevent null pointer exceptions */
 				if( bld != null ) {
 					try {
 						if( b.getPromotionLevel( true ).equals( cstate.getBaseline().getPromotionLevel( true ) ) ) {
 							logger.debug( id + b.getShortname() + " has the same promotion level" );
 							continue;
 						}
 					} catch( UCMException e ) {
 						logger.warning( id + "Unable to compare promotion levels on " + b.getShortname() );
 					}
 
 					/* The job is not running */
 					if( !bld.isLogUpdated() ) {
 						logger.debug( id + "Job " + bld.getNumber() + " is not building", id );
 						validBaselines.add( b );
 					} else {
 						logger.debug( id + "Job " + bld.getNumber() + " is building " + cstate.getBaseline().getFullyQualifiedName(), id );
 					}
 				} else {
 					/* What to do!? Let's skip it! */
 				}
 			} else {
 				validBaselines.add( b );
 			}
 		}
 
 		return validBaselines;
 	}
 
 	/**
 	 * Filter out baselines that is involved in a deliver or does not have a
 	 * label
 	 * 
 	 * @param baselines
 	 */
 	private int filterBaselines( List<Baseline> baselines ) {
 
 		int pruned = 0;
 
 		/* Remove deliver baselines */
 		Iterator<Baseline> it = baselines.iterator();
 		while( it.hasNext() ) {
 			Baseline baseline = it.next();
 			if( baseline.getShortname().startsWith( "deliverbl." ) || baseline.getLabelStatus().equals( LabelStatus.UNLABLED ) ) {
 				it.remove();
 				pruned++;
 			}
 		}
 		return pruned;
 	}
 
 	/**
 	 * Store the globally defined parameters in the state object
 	 * 
 	 * @param state
 	 */
 	private void storeStateParameters( State state ) {
 
 		try {
 			state.setStream( UCMEntity.getStream( stream, true ) );
 		} catch( UCMException e ) {
 			logger.warning( e, id );
 		}
 
 		try {
 			state.setComponent( UCMEntity.getComponent( component, true ) );
 		} catch( UCMException e ) {
 			logger.warning( e, id );
 		}
 
 		state.setUnstable( treatUnstable );
 
 		/* Notifier stuff */
 		state.setSetDescription( setDescription );
 		state.setMakeTag( makeTag );
 		state.setRecommend( recommend );
 		state.setForceDeliver( forceDeliver );
 
 		/*
 		 * try { state.setBaseline( UCMEntity.getBaseline( bl, false ) ); }
 		 * catch( UCMException e ) { logger.warning( e ); }
 		 */
 		state.setPlevel( plevel );
 	}
 
 	@Override
 	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener ) throws IOException, InterruptedException {
 		SCMRevisionState scmRS = null;
 
 		if( bl != null ) {
 			scmRS = new SCMRevisionStateImpl();
 		}
 		return scmRS;
 	}
 
 	private Baseline selectBaseline( List<Baseline> baselines, Project.Plevel plevel ) {
 		if( baselines.size() > 0 ) {
 			if( plevel != null ) {
 				return baselines.get( 0 );
 			} else {
 				return baselines.get( baselines.size() - 1 );
 			}
 		} else {
 			return null;
 		}
 	}
 
 	private void printParameters( PrintStream ps ) {
 		ps.println( "[" + Config.nameShort + "] Getting baselines for :" );
 		ps.println( "[" + Config.nameShort + "] * Stream:          " + stream );
 		ps.println( "[" + Config.nameShort + "] * Component:       " + component );
 		ps.println( "[" + Config.nameShort + "] * Promotion level: " + levelToPoll );
 		ps.println( "" );
 	}
 
 	public void printBaselines( List<Baseline> baselines, PrintStream ps ) {
 		if( baselines != null ) {
 			ps.println( "[" + Config.nameShort + "] Retrieved " + baselines.size() + " baseline" + ( baselines.size() == 1 ? "" : "s" ) + ":" );
 			if( !( baselines.size() > 8 ) ) {
 				for( Baseline b : baselines ) {
 					ps.println( "[" + Config.nameShort + "] + " + b.getShortname() + "(" + b.getDate() + ")" );
 				}
 			} else {
 				int i = baselines.size();
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( 0 ).getShortname() + "(" + baselines.get( 0 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( 1 ).getShortname() + "(" + baselines.get( 1 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( 2 ).getShortname() + "(" + baselines.get( 2 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "]   ..." );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( i - 3 ).getShortname() + "(" + baselines.get( i - 3 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( i - 2 ).getShortname() + "(" + baselines.get( i - 2 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( i - 1 ).getShortname() + "(" + baselines.get( i - 1 ).getDate() + ")" );
 			}
 		}
 	}
 
 	/*
 	 * The following getters and booleans (six in all) are used to display saved
 	 * userdata in Hudsons gui
 	 */
 	public String getLevelToPoll() {
 		return levelToPoll;
 	}
 
 	public String getComponent() {
 		return component;
 	}
 
 	public String getStream() {
 		return stream;
 	}
 
 	public String getLoadModule() {
 		return loadModule;
 	}
 
 	/*
 	 * getStreamObject() and getBaseline() are used by CCUCMNotifier to get the
 	 * Baseline and Stream in use, but does not work with concurrent builds!!!
 	 */
 	public Stream getStreamObject() {
 		return integrationstream;
 	}
 
 	@Exported
 	public String getBaseline() {
 		return bl;
 	}
 
 	public boolean getSlavePolling() {
 		CCUCMScmDescriptor desc = (CCUCMScmDescriptor) this.getDescriptor();
 		return desc.getSlavePolling();
 
 	}
 
 	@Exported
 	public String getPolling() {
 		return polling.toString();
 	}
 
 	@Exported
 	public String getTreatUnstable() {
 		return treatUnstable.toString();
 	}
 
 	@Exported
 	public boolean doPostbuild() {
 		return doPostBuild;
 	}
 
 	public String getBuildProject() {
 		return buildProject;
 	}
 
 	public boolean getForceDeliver() {
 		return forceDeliver;
 	}
 
 	public boolean isCreateBaseline() {
 		return this.createBaseline;
 	}
 
 	public String getNameTemplate() {
 		return this.nameTemplate;
 	}
 
 	public boolean isMakeTag() {
 		return this.makeTag;
 	}
 
 	public boolean isSetDescription() {
 		return this.setDescription;
 	}
 
 	public boolean isRecommend() {
 		return this.recommend;
 	}
 
 	public Appender getAppender() {
 		return app;
 	}
 
 	/**
 	 * This class is used to describe the plugin to Hudson
 	 * 
 	 * @author Troels Selch
 	 * @author Margit Bennetzen
 	 * 
 	 */
 	@Extension
 	public static class CCUCMScmDescriptor extends SCMDescriptor<CCUCMScm> implements hudson.model.ModelObject {
 
 		private boolean slavePolling;
 		private List<String> loadModules;
 
 		public CCUCMScmDescriptor() {
 			super( CCUCMScm.class, null );
 			loadModules = getLoadModules();
 			load();
 			Config.setContext();
 		}
 
 		/**
 		 * This method is called, when the user saves the global Hudson
 		 * configuration.
 		 */
 		@Override
 		public boolean configure( org.kohsuke.stapler.StaplerRequest req, JSONObject json ) throws FormException {
 			try {
 				String s = json.getString( "slavePolling" );
 				if( s != null ) {
 					slavePolling = Boolean.parseBoolean( s );
 				}
 			} catch( Exception e ) {
 				e.getMessage();
 			}
 
 			save();
 
 			return true;
 		}
 
 		public boolean getSlavePolling() {
 			return slavePolling;
 		}
 
 		/**
 		 * This is called by Hudson to discover the plugin name
 		 */
 		@Override
 		public String getDisplayName() {
 			return "ClearCase UCM";
 		}
 
 		/**
 		 * This method is called by the scm/CCUCM/global.jelly to validate the
 		 * input without reloading the global configuration page
 		 * 
 		 * @param value
 		 * @return
 		 */
 		public FormValidation doExecutableCheck( @QueryParameter String value ) {
 			return FormValidation.validateExecutable( value );
 		}
 
 		public FormValidation doTemplateCheck( @QueryParameter String value ) throws FormValidation {
 			try {
 				NameTemplate.testTemplate( NameTemplate.trim( value ) );
 				return FormValidation.ok( "The template seems ok" );
 			} catch( TemplateException e ) {
 				throw FormValidation.error( "Does not appear to be a valid template: " + e.getMessage() );
 			}
 		}
 
 		public void doLevelCheck( @QueryParameter String polling, @QueryParameter String level ) throws FormValidation {
 			System.out.println( "LEVEL CHECK: " + polling + " + " + level );
 			if( level.equalsIgnoreCase( "any" ) && !polling.equals( "self" ) ) {
 				throw FormValidation.error( "You can only combine self and any" );
 			}
 		}
 
 		@Override
 		public CCUCMScm newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
 			try {
 				String polling = formData.getString( "polling" );
 				String level = formData.getString( "levelToPoll" );
 
 				if( level.equalsIgnoreCase( "any" ) ) {
 					if( !polling.equalsIgnoreCase( "self" ) ) {
 						throw new FormException( "You can only use any with self polling", "polling" );
 					}
 				}
 			} catch( JSONException e ) {
 				throw new FormException( "You missed some fields: " + e.getMessage(), "CCUCM.polling" );
 			}
 			CCUCMScm instance = req.bindJSON( CCUCMScm.class, formData );
 			/* TODO This is actually where the Notifier check should be!!! */
 			return instance;
 		}
 
 		/**
 		 * Used by Hudson to display a list of valid promotion levels to build
 		 * from. The list of promotion levels is hard coded in
 		 * net.praqma.hudson.Config.java
 		 * 
 		 * @return
 		 */
 		public List<String> getLevels() {
 			return Config.getLevels();
 		}
 
 		/**
 		 * Used by Hudson to display a list of loadModules (whether to poll all
 		 * or only modifiable elements
 		 * 
 		 * @return
 		 */
 		public List<String> getLoadModules() {
 			loadModules = new ArrayList<String>();
 			loadModules.add( "All" );
 			loadModules.add( "Modifiable" );
 			return loadModules;
 		}
 	}
 }
