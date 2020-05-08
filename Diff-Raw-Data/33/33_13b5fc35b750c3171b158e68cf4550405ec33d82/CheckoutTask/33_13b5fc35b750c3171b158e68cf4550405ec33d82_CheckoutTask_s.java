 package net.praqma.hudson.scm;
 
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.utils.BaselineDiff;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.utils.BaselineList;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.UCMView;
 import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.util.debug.Logger;
 
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.VirtualChannel;
 
 
 
 public class CheckoutTask implements FileCallable<String> {
 	
 	private PrintStream hudsonOut;
 	private Stream integrationstream;
 	private String jobname;
 	private SnapshotView sv;
 	private String loadModule;
 	private Baseline bl;
 	private String buildProject;
 	private Logger logger;
 	private String intStream;
 	private String baselinefqname;
 	private BuildListener listener;
 	private Integer jobNumber;
 	private String id = "";
 	
 	/*public CheckoutTask(PrintStream hudsonOut, String jobname, Stream integrationstream, String loadModule, Dto dto, String buildProject){
 		this.hudsonOut=hudsonOut;
 		this.integrationstream=integrationstream;
 		this.jobname=jobname;
 		this.loadModule=loadModule;
 		this.bl=dto.getBl();
 		this.buildProject=buildProject;
 	}*/
 	
 	public CheckoutTask( BuildListener listener, String jobname, Integer jobNumber, String intStream, String loadModule, String baselinefqname, String buildProject )
 	{
 		this.jobname = jobname;
 		this.intStream = intStream;
 		this.loadModule = loadModule;
 		this.baselinefqname = baselinefqname;
 		this.buildProject = buildProject;
 		this.listener = listener;
 		
 		this.id = "[" + jobname + "::" + jobNumber + "]";
 	}
 	
 	
 	public String invoke( File workspace, VirtualChannel channel ) throws IOException
 	{
 		logger = Logger.getLogger();
 		hudsonOut = listener.getLogger();
 		
 		boolean doPostBuild = true;
 		String diff = "";
 		
 		try
 		{
 			UCM.SetContext( UCM.ContextType.CLEARTOOL );
 			// hudsonOut.println("output from checkouttask");
 			makeWorkspace( workspace );
 			BaselineDiff bldiff = bl.GetDiffs( sv );
 			diff = createChangelog( bldiff, hudsonOut );
 			doPostBuild = true;
 		}
 		catch ( ScmException e )
 		{
 			logger.debug( id + "SCM exception: " + e.getMessage() );
 			hudsonOut.println( "SCM exception: " + e.getMessage() );
 		}
 		catch ( UCMException e )
 		{
 			logger.debug( id + "Could not get changes. " + e.getMessage() );
 			hudsonOut.println( "Could not get changes. " + e.getMessage() );
 		}
 
 		return diff;
 	}
 	
 	
     
     private void makeWorkspace(File workspace) throws ScmException
     {
     	// We know we have a stream (st), because it is set in
     	// baselinesToBuild()
 		try
 		{
 			integrationstream = UCMEntity.GetStream( intStream, false );
			logger.debug( id + "hul igennem:3 " );
 			bl = Baseline.GetBaseline( baselinefqname );
 		}
 		catch ( UCMException e )
 		{
 			throw new ScmException( "Could not get stream. " + e.getMessage() );
 		}
 		if ( workspace != null )
 		{
 			logger.debug( id + "workspace: " + workspace.getAbsolutePath() );
 		}
 		else
 		{
 			logger.debug( id + "workspace must be null???" );
 		}
 		
 		StringBuffer notHudsonOut = new StringBuffer();
 		
 
     	String viewtag = "pucm_" + System.getenv( "COMPUTERNAME" ) + "_" + jobname;
 
     	File viewroot = new File( workspace.getPath() + "\\view" );
 
     	hudsonOut.println( "viewtag " + viewtag );
 
     	try
     	{
     		if ( viewroot.exists() )
     		{
     			hudsonOut.println( "Reusing viewroot: " + viewroot.toString() );
     		}
     		else
     			if ( viewroot.mkdir() )
     			{
     				hudsonOut.println( "Created folder for viewroot:  " + viewroot.toString() );
     			}
     			else
     			{
     				throw new ScmException( "Could not create folder for viewroot:  " + viewroot.toString() );
     			}
     	}
     	catch ( Exception e )
     	{
     		throw new ScmException( "Could not make workspace (for viewroot " + viewroot.toString() + "). Cause: " + e.getMessage() );
 
     	}
     	
     	Stream devstream = null;
 
     	devstream = getDeveloperStream( "stream:" + viewtag, Config.getPvob( integrationstream ), hudsonOut );
 
     	if ( UCMView.ViewExists( viewtag ) )
     	{
     		hudsonOut.println( "Reusing viewtag: " + viewtag + "\n" );
     		try
     		{
     			SnapshotView.ViewrootIsValid( viewroot );
     			hudsonOut.println( "Viewroot is valid in ClearCase" );
     		}
     		catch ( UCMException ucmE )
     		{
     			try
     			{
     				hudsonOut.println( "Viewroot not valid - now regenerating.... " );
     				SnapshotView.RegenerateViewDotDat( viewroot, viewtag );
     			}
     			catch ( UCMException ucmEe )
     			{
     				logger.warning( id + "Could regenerate workspace." );
     				throw new ScmException( "Could not make workspace - could not regenerate view: " + ucmEe.getMessage() + " Type: " + "" );
     			}
     		}
     		
     		hudsonOut.print( "Getting snapshotview..." );
     		
     		try
     		{
     			sv = UCMView.GetSnapshotView( viewroot );
     			hudsonOut.println( " DONE" );
     		}
     		catch ( UCMException e )
     		{
     			logger.warning( id + "Could not get view for workspace. " + e.getMessage() );
     			throw new ScmException( "Could not get view for workspace. " + e.getMessage() );
     		}
     	}
     	else
     	{
     		try
     		{
     			hudsonOut.println( "View doesn't exist" );
     			sv = SnapshotView.Create( devstream, viewroot, viewtag );
     			hudsonOut.println( " - created new view in local workspace" );
     			logger.log( "The view did not exist and created a new" );
     		}
     		catch ( UCMException e )
     		{
     			logger.warning( id + "The view could not be created" );
     			throw new ScmException( " - could not create a new view for workspace. " + e.getMessage() );
     		}
     	}
 
     	// All below parameters according to LAK and CHW -components
     	// corresponds to pucms loadmodules, loadrules must always be
     	// null from pucm
     	try
     	{
    		hudsonOut.println( "Updating view using " + loadModule.toLowerCase() + " modules..." );
 
     		sv.Update( true, true, true, false, COMP.valueOf( loadModule.toUpperCase() ), null );
     		hudsonOut.println( " DONE" );
     	}
     	catch ( UCMException e )
     	{
     		throw new ScmException( "Could not update snapshot view. " + e.getMessage() );
     	}
 
     	// Now we have to rebase - if a rebase is in progress, the
     	// old one must be stopped and the new started instead
     	if ( devstream.IsRebaseInProgress() )
     	{
    		hudsonOut.println( "Cancelling previous rebase..." );
     		devstream.CancelRebase();
     		hudsonOut.println( " DONE" );
     	}
     	// The last boolean, complete, must always be true from PUCM
     	// as we are always working on a read-only stream according
     	// to LAK
    	hudsonOut.println( "Rebasing development stream (" + devstream.GetShortname() + ") against parent stream (" + integrationstream.GetShortname() + ")" );
     	devstream.Rebase( sv, bl, true );
     	hudsonOut.println( " DONE" );
     	hudsonOut.println( "Log written to " + logger.getPath() );
     }
 
     private Stream getDeveloperStream( String streamname, String pvob, PrintStream hudsonOut ) throws ScmException
     {
     	Stream devstream = null;
 
     	try
     	{
     		if ( Stream.StreamExists( streamname + pvob ) )
     		{
     			devstream = Stream.GetStream( streamname + pvob, false );
     		}
     		else
     		{
     			if ( buildProject.equals( "" ) )
     				buildProject = "hudson";
     			devstream = Stream.Create( Config.getIntegrationStream( bl, hudsonOut, buildProject ), streamname + pvob, true, bl );
     		}
     	}
     	/*
     	 * This tries to handle the issue where the project hudson is not
     	 * available
     	 */
     	catch ( ScmException se )
     	{
     		throw se;
 
     	}
     	catch ( Exception e )
     	{
     		throw new ScmException( "Could not get stream: " + e.getMessage() );
     	}
 
     	return devstream;
     }
     
 	private String createChangelog( BaselineDiff changes, PrintStream hudsonOut )
 	{
 		StringBuffer buffer = new StringBuffer();
 
 		hudsonOut.println( "Writing Hudson changelog..." );
 
 		buffer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
 		buffer.append( "<changelog>" );
 		buffer.append( "<changeset>" );
 		buffer.append( "<entry>" );
 		buffer.append( ( "<blName>" + bl.GetShortname() + "</blName>" ) );
 		for ( Activity act : changes )
 		{
 			buffer.append( "<activity>" );
 			buffer.append( ( "<actName>" + act.GetShortname() + "</actName>" ) );
 			try
 			{
 				buffer.append( ( "<author>" + act.GetUser() + "</author>" ) );
 			}
 			catch ( UCMException e )
 			{
 				buffer.append( ( "<author>Unknown</author>" ) );
 			}
 			List<Version> versions = act.changeset.versions;
 			String temp;
 			for ( Version v : versions )
 			{
 				temp = "<file>" + v.GetSFile() + "[" + v.GetRevision() + "] user: " + v.Blame() + "</file>";
 				buffer.append( temp );
 			}
 			buffer.append( "</activity>" );
 		}
 		buffer.append( "</entry>" );
 		buffer.append( "</changeset>" );
 
 		buffer.append( "</changelog>" );
 
 		
 		return buffer.toString();
 	}
     
     public SnapshotView getSnapshotView(){
     	return sv;
     }
     
     public BaselineDiff getBaselineDiffs() throws UCMException{
     	return bl.GetDiffs( sv );
     }
     
 
 }
 
 
