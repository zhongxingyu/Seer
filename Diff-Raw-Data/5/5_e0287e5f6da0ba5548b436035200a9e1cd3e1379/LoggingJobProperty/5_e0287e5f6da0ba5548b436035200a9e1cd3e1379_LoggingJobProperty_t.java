 package net.praqma.logging;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 import hudson.model.*;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import hudson.Extension;
 
 public class LoggingJobProperty extends JobProperty<Job<?, ?>> {
 
 	public static final String[] levels = { "all", "finest", "finer", "fine", "config", "info", "warning", "severe" };
 
 	private List<LoggingTarget> targets;
 
 	private boolean pollLogging = false;
     private int pruneDays = 0;
 	
	private transient Map<Long, LoggingHandler> pollhandler;
 
 	@DataBoundConstructor
 	public LoggingJobProperty( boolean pollLogging, int pruneDays ) {
 		this.pollLogging = pollLogging;
         this.pruneDays = pruneDays;

        pollhandler = new HashMap<Long, LoggingHandler>();
 	}
 	
 	public LoggingHandler getPollhandler( long id, String name ) throws IOException {
 		LoggingHandler pollhandler = this.pollhandler.get( id );
 		
 		if( pollhandler == null && name != null ) {
 			File path = new File( owner.getRootDir(), Logging.POLLLOGPATH );
 			if( !path.exists() ) {
 				if( !path.mkdirs() ) {
                     System.out.println( "Could not mkdirs: " + path );
                 }
 			}
 
             /* Pruning */
             File[] logs = Logging.getLogs( path );
             Logging.prune( logs, pruneDays );
 
             File file = Logging.getLogFile( path, name );
 			FileOutputStream fos = new FileOutputStream( file, true );
 			pollhandler = LoggingUtils.createHandler( fos );
 			
 			pollhandler.addTargets( getTargets() );
 			
 			this.pollhandler.put( id, pollhandler );
 		}
 		
 		return pollhandler;
 	}
 	
 	public void resetPollhandler( long id ) {
 		pollhandler.put( id, null );
 	}
 	
 	public LoggingAction getLoggingAction( long id, String name ) throws IOException {
 		LoggingHandler handler = getPollhandler( id, name );
         if( handler != null ) {
 		    return new LoggingAction( null, handler, getTargets() );
         } else {
             return null;
         }
 	}
 
     @Override
     public Collection<? extends Action> getJobActions( Job<?, ?> job ) {
         List<LoggingProjectAction> list = new ArrayList<LoggingProjectAction>();
         list.add(new LoggingProjectAction( job ));
         return (Collection<LoggingProjectAction>) list;
     }
 
     private void setTargets( List<LoggingTarget> targets ) {
 		this.targets = targets;
 	}
 
 	public List<LoggingTarget> getTargets() {
 		return targets;
 	}
 
 	public boolean isPollLogging() {
 		return pollLogging;
 	}
 
     public int getPruneDays() {
         return pruneDays;
     }
 
 	@Extension
 	public static class DescriptorImpl extends JobPropertyDescriptor {
 
 		public JobProperty<?> newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
 			Object debugObject = formData.get( "debugLog" );
 
 			System.out.println( formData.toString( 2 ) );
 
 			if( debugObject != null ) {
 				JSONObject debugJSON = (JSONObject) debugObject;
 
 				boolean pollLogging = debugJSON.getBoolean( "pollLogging" );
                 int pruneDays = debugJSON.getInt( "pruneDays" );
 
 				LoggingJobProperty instance = new LoggingJobProperty( pollLogging, pruneDays );
 
 				List<LoggingTarget> targets = req.bindParametersToList( LoggingTarget.class, "logging.logger." );
 				instance.setTargets( targets );
 
 				return instance;
 			}
 
 			return null;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Logging";
 		}
 
 		@Override
 		public boolean isApplicable( Class<? extends Job> jobType ) {
 			return true;
 		}
 
 		public String[] getLogLevels() {
 			return levels;
 		}
 
         public int[] getDays() {
             return new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};
         }
 
 		public List<LoggingTarget> getAcceptableLoggerNames( LoggingJobProperty instance ) {
 			if( instance == null ) {
 				return new ArrayList<LoggingTarget>();
 			} else {
 				return instance.getTargets();
 			}
 		}
 
 	}
 
 	public String toString() {
 		return "Logging job property, " + targets;
 	}
 
 }
