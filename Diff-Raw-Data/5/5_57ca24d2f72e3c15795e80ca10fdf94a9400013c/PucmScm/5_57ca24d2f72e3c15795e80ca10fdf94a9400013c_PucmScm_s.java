 package net.praqma.hudson.scm;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCM;
 import hudson.util.FormValidation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Baseline.BaselineDiff;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Component.BaselineList;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.UCMEntityException;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.persistence.UCMContext;
 import net.praqma.clearcase.ucm.persistence.UCMStrategyCleartool;
 import net.praqma.clearcase.ucm.utils.TagQuery;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
 import net.praqma.clearcase.ucm.view.UCMView;
 import net.praqma.hudson.Config;
 import net.praqma.utils.Debug;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 /**
  * CC4HClass is responsible for everything regarding Hudsons connection to
  * ClearCase pre-build. This class defines all the files required by the user.
  * The information can be entered on the config page.
  * 
  * @author Troels Selch Srensen
  * @author Margit Bennetzen
  * 
  */
 public class PucmScm extends SCM
 {
 
 	private String levelToPoll;
 	private String loadModule;
 	private String component;
 	private String stream;
 	private boolean newest;
 	private boolean newerThanRecommended;
 
 	private Baseline bl;
 	private List<String> levels = null;
 	private List<String> loadModules = null;
 	private BaselineList baselines;
 	private boolean compRevCalled;
 	private StringBuffer pollMsgs = new StringBuffer();
 	private Stream integrationstream;
 	private Component co;
 	private SnapshotView sv = null;
 	
 	protected static Debug logger = Debug.GetLogger();
 
 	/**
 	 * The constructor is used by Hudson to create the instance of the plugin
 	 * needed for a connection to ClearCase. It is annotated with
 	 * <code>@DataBoundConstructor</code> to tell Hudson where to put the
 	 * information retrieved from the configuration page in the WebUI.
 	 * 
 	 * @param component
 	 *            defines the component needed to find baselines.
 	 * @param levelToPoll
 	 *            defines the level to poll ClearCase for.
 	 * @param loadModule
 	 *            tells if we should load all modules or only the ones that are
 	 *            modifiable.
 	 * @param stream
 	 *            defines the stream needed to find baselines.
 	 * @param newest
 	 *            tells whether we should build only the newest baseline.
 	 * @param newerThanRecommended
 	 *            tells whether we should look at all baselines or only ones
 	 *            newer than the recommended baseline
 	 */
 	@DataBoundConstructor
 	public PucmScm( String component, String levelToPoll, String loadModule, String stream, boolean newest, boolean newerThanRecommended, boolean testing )
 	{
 		logger.trace_function();
 		this.component = component;
 		this.levelToPoll = levelToPoll;
 		this.loadModule = loadModule;
 		this.stream = stream;
 		this.newest = newest;
 		this.newerThanRecommended = newerThanRecommended;
 	}
 
 	@Override
 	public boolean checkout( AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile ) throws IOException, InterruptedException
 	{
 		logger.trace_function();
 		boolean result = true;
 
 		//consoleOutput Printstream is from Hudson, so it can only be accessed here
 		PrintStream consoleOutput = listener.getLogger();
 		consoleOutput.println("Workspace: "+workspace);
 
 		String jobname = build.getParent().getDisplayName();
 
 		// compRevCalled tells whether we have polled for baselines to build -
 		// so if we haven't polled, we do it now
 		if ( !compRevCalled )
 		{
 			result = baselinesToBuild( jobname );
 		}
 
 		compRevCalled = false;
 
 		// pollMsgs are set in either compareRemoteRevisionWith() or
 		// baselinesToBuild()
 		consoleOutput.println( pollMsgs );
 		pollMsgs = new StringBuffer();
 
 		if ( result )
 		{
 			result = makeWorkspace( consoleOutput , workspace );
 			if( !result )
 			{
 				consoleOutput.println( "Could not make workspace. Marking baseline with:" );
 			}
 		}
 
 		if ( result )
 		{
 			BaselineDiff changes = bl.GetDiffs(sv);
 			consoleOutput.println( changes.size() + " elements changed" );
 			result = writeChangelog( changelogFile, changes, consoleOutput );
 		}
 
 		return result;
 	}
 
 	private boolean makeWorkspace( PrintStream hudsonOut, FilePath workspace )
 	{
 		boolean result = true;
 		// We know we have a stream (st), because it is set in baselinesToBuild()
 
 		// TODO verify viewtag
 		String viewtag = "Hudson_Server_dev_view";//"pucm_" + System.getenv( "COMPUTERNAME" );//TODO +hudsonjobname i stinavn
 		
 		File viewroot = new File( workspace + "\\view\\"+viewtag );
 		try
 		{
 			if ( viewroot.mkdir() )
 			{
 				hudsonOut.println( "Created viewroot " + viewroot.toString() );
 			}
 			else
 			{
 				hudsonOut.println( "Reusing viewroot " + viewroot.toString() );
 			}
 		}
 		catch ( Exception e )
 		{
 			hudsonOut.println( "Could not make viewroot " + viewroot.toString() + ". Cause: " + e.getMessage() );
 			result = false;
 		}
 		
 		if( result )
 		{
 			
 			// Hvis der er et snaphotview med et givent viewtag i cleartool s:
 			if ( UCMView.ViewExists( viewtag ) )
 			{
 				sv = UCMView.GetSnapshotView( viewroot );
 				try
 				{
 					sv.ViewrootIsValid();//(returns uuid)
 				} catch (UCMException ucmE)
 				{
 					// then viewroot is regenerated
 					//TODO SnapshotView.RegenerateViewDotDat( viewroot, viewtag );//venter p at CHW laver den statisk :-) eller sletter viewroot/tag
 				}
 
 				//All below parameters according to LAK and CHW
 				//sv.Update( true, true, true, true/*chw fjerne force*/, false, COMP.valueOf( loadModule.toUpperCase() ), null );//components eller loadrules = null, det er loadmodules vi skal bruge
 			}
 			else
 			{
 				//TODO when CHW has made a function to make new snapshotview, it must be implemented
 				sv = UCMView.GetSnapshotView( viewroot );
 			}
 			if(sv==null)
 			{
 				result = false;
 			}
 			if( result )
 			{
 				Stream devstream = Config.devStream(); //view: Hudson_Server_dev_view
 				// Now we have to rebase - if a rebase is in progress, the old one must be stopped and the new started instead
 				if(devstream.IsRebaseInProgress())
 				{
 					devstream.CancelRebase();
 				}
 				//The last boolean, complete, must always be true from PUCM as we are always working on a read-only stream according to LAK
 				devstream.Rebase( sv, bl, true );
 			}
 		}
 		return result;
 	}
 
 	private boolean writeChangelog( File changelogFile, BaselineDiff changes, PrintStream hudsonOut ) throws IOException
 	{
 		boolean result;
 
 		logger.trace_function();
 
 		StringBuffer buffer = new StringBuffer();
 
 		// Here the .hudson/jobs/[project name]/builds/[buildnumber]/changelog.xml is written
 		hudsonOut.print( "Writing Hudson changelog..." );
 		try
 		{
 			buffer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
 			buffer.append( "<changelog>" );
 			buffer.append( "<changeset>" );
 			buffer.append( "<entry>" );
 			buffer.append( ( "<blName>" + bl.GetShortname() + "</blName>" ) );
 			for ( Activity act : changes )
 			{
 				buffer.append( "<activity>" );
 				buffer.append( ( "<actName>" + act.GetShortname() + "</actName>" ) );
 				buffer.append( ( "<author>Hudson" + act.GetUser() + "</author>" ) );
 				List<Version> versions = act.changeset.versions;
 				String temp;
 				for ( Version v : versions )
 				{
 					temp = "<file>" + v.toString() + " " + v.Blame() + "</file>";
 					buffer.append( temp );
 				}
 				buffer.append( "</activity>" );
 			}
 			buffer.append( "</entry>" );
 			buffer.append( "</changeset>" );
 
 			buffer.append( "</changelog>" );
 			FileOutputStream fos = new FileOutputStream( changelogFile );
 			fos.write( buffer.toString().getBytes() );
 			fos.close();
 			hudsonOut.println( " DONE" );
 			result = true;
 		}
 		catch ( Exception e )
 		{
 			hudsonOut.println( "FAILED" );
 			logger.log( "Changelog failed with " + e.getMessage() );
 			result = false;
 		}
 		return result;
 	}
 
 	@Override
 	public ChangeLogParser createChangeLogParser()
 	{
 		logger.trace_function();
 		return new ChangeLogParserImpl();
 	}
 
 	@Override
 	public PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline ) throws IOException, InterruptedException
 	{
 		logger.trace_function();
 		SCMRevisionStateImpl scmState = (SCMRevisionStateImpl) baseline;
 
 		PollingResult p;
 
 		if ( baselinesToBuild( scmState.getJobname() ) )
 		{
 			compRevCalled = true;
 			p = PollingResult.BUILD_NOW;
 		}
 		else
 		{
 			p = PollingResult.NO_CHANGES;
 		}
 		return p;
 	}
 
 	@Override
 	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener ) throws IOException, InterruptedException
 	{
 		logger.trace_function();
 		PrintStream hudsonOut = listener.getLogger();
 
 		SCMRevisionStateImpl scmRS = null;
 
 		if ( !( bl == null ) )
 		{
 			scmRS = new SCMRevisionStateImpl( build.getParent().getDisplayName(), String.valueOf( build.getNumber() ) );
 		}
 		return scmRS;
 	}
 
 	private boolean baselinesToBuild( String jobname )
 	{
 		logger.trace_function();
 
 		boolean result = true;
 
 		co = null;
 		integrationstream = null;
 		Stream devstream = null;
 
 		try
 		{
 			co = UCMEntity.GetComponent( "component:" + component, false );
 			
 			integrationstream = UCMEntity.GetStream( "stream:" + stream, false );
 			pollMsgs.append("Integrationstream exists: "+integrationstream.toString());
 						
 			//TODO: Afklaring med lak mht om vi overhovedet skal create stream, hvis ja, med hvilke parametre?
 			
 			/*if(Stream.StreamExists( stream ))
 			{
 				
 			} else
 			{
 				//Version2: kunne angive Hudson-projekt. Skal hardcodes i config.java CHRISTIAN - FIKS!!!! :-)
 				//TODO fiks: st = Stream.Create( pstream, nstream, readonly, null )CHW kommr med ny metode - mske
 				//nstream = gui-angivet stream, pstream fra config.java, readonly=true
 			}*/
 		}
 		catch ( UCMEntityException ucmEe )
 		{
 			pollMsgs.append( ucmEe.toString() + "\n" );
 			result = false;
 		}
 
 		if ( result )
 		{
 			try
 			{
 				pollMsgs.append( "Getting " );
 				pollMsgs.append( ( newerThanRecommended ? "baselines newer than the recomended baseline " : "all baselines " ) );
 				pollMsgs.append( "for stream " );
 				pollMsgs.append( stream );
 				pollMsgs.append( " and component " );
 				pollMsgs.append( component );
 				pollMsgs.append( " on promotionlevel " );
 				pollMsgs.append( levelToPoll );
 				pollMsgs.append( "\n" );
 
 				if ( newerThanRecommended )
 				{
 					baselines = co.GetBaselines( integrationstream, Project.Plevel.valueOf( levelToPoll ) ).NewerThanRecommended();
 				}
 				else
 				{
 					baselines = co.GetBaselines( integrationstream, Project.Plevel.valueOf( levelToPoll ) );
 				}
 			}
 			catch ( Exception e )
 			{
 				pollMsgs.append( "Could not retrieve baselines from repository\n" );
 				result = false;
 			}
 		}
 
 		if ( result )
 		{
 			if ( baselines.size() > 0 )
 			{
 				pollMsgs.append( "Retrieved baselines:\n" );
 				for ( Baseline b : baselines )
 				{
 					pollMsgs.append( b.GetShortname() );
 					pollMsgs.append( "\n" );
 				}
 
 				if ( newest )
 				{
					bl = baselines.get( 0 );
 					pollMsgs.append( "Building newest baseline: " );
 					pollMsgs.append( bl );
 					pollMsgs.append( "\n" );
 				}
 				else
 				{
					bl = baselines.get( baselines.size() - 1 );
 					pollMsgs.append( "Building next baseline: " );
 					pollMsgs.append( bl );
 					pollMsgs.append( "\n" );
 				}
 			}
 			else
 			{
 				pollMsgs.append( "No baselines on chosen parameters.\n" );
 				result = false;
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * The following getters and booleans (six in all) are used to display saved
 	 * userdata in Hudsons gui
 	 */
 
 	public String getLevelToPoll()
 	{
 		logger.trace_function();
 		return levelToPoll;
 	}
 
 	public String getComponent()
 	{
 		logger.trace_function();
 		return component;
 	}
 
 	public String getStream()
 	{
 		logger.trace_function();
 		return stream;
 	}
 
 	public String getLoadModule()
 	{
 		logger.trace_function();
 		return loadModule;
 	}
 
 	public boolean isNewest()
 	{
 		logger.trace_function();
 		return newest;
 	}
 
 	public boolean isNewerThanRecommended()
 	{
 		logger.trace_function();
 		return newerThanRecommended;
 	}
 
 	/*
 	 * getStreamObject() and getBaseline() are used by PucmNotifier to get the
 	 * Baseline and Stream in use
 	 */
 
 	public Stream getStreamObject()
 	{
 		logger.trace_function();
 		return integrationstream;
 	}
 
 	public Baseline getBaseline()
 	{
 		logger.trace_function();
 		return bl;
 	}
 
 	/**
 	 * This class is used to describe the plugin to Hudson
 	 * 
 	 * @author Troels Selch Srensen
 	 * @author Margit Bennetzen
 	 * 
 	 */
 	@Extension
 	public static class PucmScmDescriptor extends SCMDescriptor<PucmScm>
 	{
 
 		private String cleartool;
 		private List<String> levels;
 		private List<String> loadModules;
 
 		public PucmScmDescriptor()
 		{
 			super( PucmScm.class, null );
 			logger.trace_function();
 			levels = getLevels();
 			loadModules = getLoadModules();
 			load();
 			Config.setContext();
 		}
 
 		/**
 		 * This method is called, when the user saves the global Hudson
 		 * configuration.
 		 */
 		@Override
 		public boolean configure( org.kohsuke.stapler.StaplerRequest req, JSONObject json ) throws FormException
 		{
 			logger.trace_function();
 			cleartool = req.getParameter( "PUCM.cleartool" ).trim();
 			save();
 			return true;
 		}
 
 		/**
 		 * This is called by Hudson to discover the plugin name
 		 */
 		@Override
 		public String getDisplayName()
 		{
 			logger.trace_function();
 			return "Praqmatic UCM";
 		}
 
 		/**
 		 * This method is called by the scm/Pucm/global.jelly to validate the
 		 * input without reloading the global configuration page
 		 * 
 		 * @param value
 		 * @return
 		 */
 		public FormValidation doExecutableCheck( @QueryParameter String value )
 		{
 			logger.trace_function();
 			return FormValidation.validateExecutable( value );
 		}
 
 		/**
 		 * Called by Hudson. If the user does not input a command for Hudson to
 		 * use when polling, default value is returned
 		 * 
 		 * @return
 		 */
 		public String getCleartool()
 		{
 			logger.trace_function();
 			if ( cleartool == null || cleartool.equals( "" ) )
 			{
 				return "cleartool";
 			}
 			return cleartool;
 		}
 
 		/**
 		 * Used by Hudson to display a list of valid promotion levels to build
 		 * from. The list of promotion levels is hard coded in
 		 * net.praqma.hudson.Config.java
 		 * 
 		 * @return
 		 */
 		public List<String> getLevels()
 		{
 			logger.trace_function();
 			return Config.getLevels();
 		}
 
 		/**
 		 * Used by Hudson to display a list of loadModules (whether to poll all
 		 * or only modifiable elements
 		 * 
 		 * @return
 		 */
 		public List<String> getLoadModules()
 		{
 			logger.trace_function();
 			loadModules = new ArrayList<String>();
 			loadModules.add( "All" );
 			loadModules.add( "Modifiable" );
 			return loadModules;
 		}
 	}
 }
