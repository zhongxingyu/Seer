 package hudson.plugins.sctmexecutor;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.plugins.sctmexecutor.exceptions.EncryptionException;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.rpc.ServiceException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.borland.scc.sccsystem.SystemService;
 import com.borland.scc.sccsystem.SystemServiceServiceLocator;
 import com.borland.tm.webservices.tmexecution.ExecutionHandle;
 import com.borland.tm.webservices.tmexecution.ExecutionWebService;
 import com.borland.tm.webservices.tmexecution.ExecutionWebServiceServiceLocator;
 
 /**
  * Executes a specified execution definition on Borland's SilkCentral Test Manager.
  * 
  * @author Thomas Fuerer
  * 
  */
 public class SCTMExecutor extends Builder {
   public static final SCTMExecutorDescriptor DESCRIPTOR = new SCTMExecutorDescriptor();
   private static final Logger LOGGER = Logger.getLogger("hudson.plumgins.sctmexecutor");  //$NON-NLS-1$
 
   private static int resultNoForLastBuild = 0;
 
   private final int projectId;
   private final String execDefIds;
 
   @DataBoundConstructor
   public SCTMExecutor(int projectId, String execDefIds) {
     this.projectId = projectId;
     this.execDefIds = execDefIds;
     
   }
 
   public Descriptor<Builder> getDescriptor() {
     return DESCRIPTOR;
   }
 
   public String getExecDefIds() {
     return execDefIds;
   }
 
   public int getProjectId() {
     return projectId;
   }
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
       throws InterruptedException, IOException {
     String serviceURL = DESCRIPTOR.getServiceURL();
     SystemService systemService;
     ExecutionWebService execService;
     try {
       systemService = new SystemServiceServiceLocator().getsccsystem(new URL(serviceURL + "/sccsystem?wsdl")); //$NON-NLS-1$
       execService = new ExecutionWebServiceServiceLocator().gettmexecution(new URL(serviceURL + "/tmexecution?wsdl")); //$NON-NLS-1$
 
       ISessionHandler sessionHandler = new SessionHandler(systemService, SCTMExecutor.DESCRIPTOR.getUser(), SCTMExecutor.DESCRIPTOR.getPassword());
       
       long sessionId = sessionHandler.getSessionId(-1);
       listener.getLogger().println(Messages.getString("SCTMExecutor.log.successfulLogin")); //$NON-NLS-1$
       execService.setCurrentProject(sessionId, projectId);
       List<ExecutionHandle> execHandles;
       try {
         execHandles = startExecutions(listener, execService, sessionId);
       } catch (IllegalArgumentException e) {
         return false;
       }
 
       collectResults(build, listener, execService, sessionHandler, execHandles);
       
       return true;
     } catch (ServiceException e) {
       LOGGER.log(Level.WARNING, e.getMessage(), e);
       listener.error(MessageFormat.format(Messages.getString("SCTMExecutor.err.urlOrServiceBroken"), serviceURL)); //$NON-NLS-1$
       return false;
     } catch (RemoteException e) {
       LOGGER.log(Level.WARNING, e.getMessage(), e);
      if (e.getMessage().contains("Not logged in."))
        listener.error(Messages.getString("SCTMExecutor.err.accessDenied")); //$NON-NLS-1$
      else
        listener.error(e.getMessage());
       return false;
     } catch (EncryptionException e){
       LOGGER.log(Level.WARNING, e.getMessage(), e);
       return false;
     } catch (Exception e) {
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       listener.error(MessageFormat.format("{0} {1}", Messages.getString("SCTMExecutor.log.unknownError"), e.getLocalizedMessage()));
       return false;
     }
   }
 
   private void collectResults(AbstractBuild<?, ?> build, BuildListener listener, ExecutionWebService execService,
       ISessionHandler sessionHandler, List<ExecutionHandle> execHandles) throws IOException, InterruptedException, ExecutionException {
     FilePath rootDir = build.getProject().getWorkspace();
     if (rootDir == null) {
       LOGGER.log(Level.SEVERE, "Cannot write the result file because slave is not connected.");
       listener.error(Messages.getString("SCTMExecutor.log.slaveNotConnected")); //$NON-NLS-1$
     }
     
     rootDir = createResultDir(rootDir, build.number);
     ExecutorService tp = DESCRIPTOR.getExecutorPool();
     List<Future<?>> results = new ArrayList<Future<?>>(execHandles.size());
     for (ExecutionHandle executionHandle : execHandles) {
       ResultCollectorThread resultCollector = new ResultCollectorThread(listener.getLogger(), execService, sessionHandler, executionHandle, new StdXMLResultWriter(rootDir, DESCRIPTOR.getServiceURL()));
       results.add(tp.submit(resultCollector));
     }
     
     for (Future<?> res : results) {
       res.get();
     }
   }
 
   private List<ExecutionHandle> startExecutions(BuildListener listener, ExecutionWebService execService, long sessionId)
       throws RemoteException {
     List<ExecutionHandle> execHandles = new ArrayList<ExecutionHandle>();
     for (Integer execDefId : csvToIntList(execDefIds)) {
       ExecutionHandle[] execHandleArr = execService.startExecution(sessionId, execDefId);
       if (execHandleArr.length <= 0 || execHandleArr[0] == null
           || (execHandleArr[0] != null && execHandleArr[0].getTimeStamp() <= 0)) {
         listener.error(Messages.getString(Messages.getString("SCTMExecutor.err.execDefNotFound"), execDefId)); //$NON-NLS-1$
         throw new IllegalArgumentException();
       } else {
         listener.getLogger().println(MessageFormat.format(Messages.getString("SCTMExecutor.log.successfulStartExecution"), execDefId));
         for (ExecutionHandle executionHandle : execHandleArr) {
           execHandles.add(executionHandle);
         }
       }
     }
     return execHandles;
   }
 
   private static FilePath createResultDir(FilePath rootDir, int currentBuildNo) throws IOException, InterruptedException {
     rootDir = new FilePath(rootDir, "SCTMResults"); //$NON-NLS-1$
     if (resultNoForLastBuild  != currentBuildNo) {
       if (rootDir.exists())
         rootDir.deleteRecursive();
       rootDir.mkdirs();
       resultNoForLastBuild = currentBuildNo;
     }
     return rootDir;
   }
 
   private List<Integer> csvToIntList(String execDefIds) {
     List<Integer> list = new LinkedList<Integer>();
     if (execDefIds.contains(",")) { //$NON-NLS-1$
       String[] ids = execDefIds.split(","); //$NON-NLS-1$
       for (String str : ids) {
         list.add(Integer.valueOf(str));
       }
     } else {
       list.add(Integer.valueOf(execDefIds));
     }
     return list;
   }
 }
