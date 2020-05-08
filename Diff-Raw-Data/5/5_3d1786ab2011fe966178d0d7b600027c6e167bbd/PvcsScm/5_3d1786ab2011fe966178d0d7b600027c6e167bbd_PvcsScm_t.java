 package hudson.plugins.pvcs_scm;
 
 import hudson.plugins.pvcs_scm.changelog.ChangeLogDocument;
 import hudson.plugins.pvcs_scm.changelog.PvcsChangeLogSet;
 import hudson.plugins.pvcs_scm.changelog.PvcsChangeLogEntry;
 
 import hudson.Launcher;
 import hudson.Proc;
 import hudson.Util;
 import hudson.FilePath;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.model.Run;
 
 import hudson.scm.ChangeLogParser;
 import hudson.scm.SCM;
 import hudson.scm.SCMDescriptor;
 
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormFieldValidator;
 
 import java.io.File;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.PipedOutputStream;
 import java.io.PipedInputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import javax.servlet.ServletException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Provides integration with PCVS.
  *
  * @author Brian Lalor &lt;blalor@bravo5.org&gt;
  */
 public class PvcsScm extends SCM
 {
     private final Log logger = LogFactory.getLog(getClass());
 
     /**
      * Date format required by commands passed to PVCS
      */
     private static final String IN_DATE_FORMAT = "MM/dd/yyyy hh:mm:ss aa";
 
     /**
      * Date format returned in the output of PVCS commands.
      */
     private static final String OUT_DATE_FORMAT = "MMM dd yyyy HH:mm:ss";
 
     private String projectRoot;
     private String archiveRoot;
 
     /**
      * Additional prefix to changeset filenames to match with what {@link hudson.maven.FilteredChangeLogSet}
      * expects.
      */
     private String changeLogPrefixFudge;
 
     private String moduleDir;
     private boolean cleanCopy;
     
     // {{{ constructor
     @DataBoundConstructor
     public PvcsScm(final String projectRoot,
                    final String archiveRoot,
                    final String changeLogPrefixFudge,
                    final String moduleDir,
                    final boolean cleanCopy) 
     {
         this.projectRoot = projectRoot;
         this.archiveRoot = archiveRoot;
         this.changeLogPrefixFudge = changeLogPrefixFudge;
         this.moduleDir = moduleDir;
         this.cleanCopy = cleanCopy;
 
         logger.debug("created new instance");
     }
     // }}}
 
     // {{{ getProjectRoot
     public String getProjectRoot() {
         return projectRoot;
     }
     // }}}
     
     // {{{ setProjectRoot
     public void setProjectRoot(final String projectRoot) {
         this.projectRoot = projectRoot;
     }
     // }}}
 
     // {{{ getArchiveRoot
     public String getArchiveRoot() {
         return archiveRoot;
     }
     // }}}
     
     // {{{ setArchiveRoot
     public void setArchiveRoot(final String archiveRoot) {
         this.archiveRoot = archiveRoot;
     }
     // }}}
     
     // {{{ isCleanCopy
     public boolean isCleanCopy() {
         return cleanCopy;
     }
     // }}}
     
     // {{{ setCleanCopy
     public void setCleanCopy(final boolean cleanCopy) {
         this.cleanCopy = cleanCopy;
     }
     // }}}
     
     // {{{ getModuleDir
     public String getModuleDir() {
         return moduleDir;
     }
     // }}}
     
     // {{{ setModuleDir
     public void setModuleDir(final String moduleDir) {
         this.moduleDir = moduleDir;
     }
     // }}}
 
     // {{{ getChangeLogPrefixFudge
     public String getChangeLogPrefixFudge() {
         return changeLogPrefixFudge;
     }
     // }}}
     
     // {{{ setChangeLogPrefixFudge
     public void setChangeLogPrefixFudge(final String changeLogPrefixFudge) {
         this.changeLogPrefixFudge = changeLogPrefixFudge;
     }
     // }}}
 
     // {{{ requiresWorkspaceForPolling
     /**
      * @todo
      */
     @Override
     public boolean requiresWorkspaceForPolling() {
         return false;
     }
     // }}}
     
     // {{{ checkout
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean checkout(final AbstractBuild build,
                             final Launcher launcher,
                             final FilePath workspace,
                             final BuildListener listener,
                             final File changelogFile)
         throws IOException, InterruptedException
     {
         logger.trace("in checkout()");
         
         boolean checkoutSucceeded = true;
 
         ChangeLogDocument doc = ChangeLogDocument.Factory.newInstance();
 
         Run previousBuild = build.getPreviousBuild();
         if (previousBuild != null) {
             doc.setChangeLog(getModifications(launcher,
                                               listener,
                                               previousBuild.getTimestamp()));
 
             doc.getChangeLog().setLastBuildId(previousBuild.getId());
             doc.getChangeLog().setLastBuildTime(previousBuild.getTimestamp());
         } else {
             doc.addNewChangeLog();
         }
             
         doc.getChangeLog().setBuildId(build.getId());
         doc.save(changelogFile);
 
         if (cleanCopy) {
             listener.getLogger().println("clean copy configured; deleting contents of " + workspace);
 
             logger.info("deleting contents of workspace " + workspace);
             
             workspace.deleteContents();
         }
 
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add(getDescriptor().getExecutable());
         cmd.add("-nb", "run", "-ns", "-y");
         cmd.add("get");
         cmd.add("-pr" + projectRoot);
         cmd.add("-bp/");
         cmd.add("-o");
         cmd.add("-a.");
         cmd.add("-z");
         cmd.add(moduleDir);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
        logger.debug("launching command " + cmd.toList());
         
         Proc proc = launcher.launch(cmd.toCommandArray(), new String[0], baos, workspace);
         int rc = proc.join();
 
         if (rc != 0) {
             // checkoutSucceeded = false;
             
             logger.error("command exited with " + rc);
             listener.error("command exited with " + rc);
             // listener.error(baos.toString());
             listener.error("continuing anyway.  @todo: filter results from PVCS");
             
         } /* else */ { 
             if (logger.isTraceEnabled()) {
                 logger.trace("pcli output:\n" + new String(baos.toByteArray()));
             }
             
             listener.getLogger().println("pcli output:");
             listener.getLogger().write(baos.toByteArray(), 0, baos.size());
 
         }
 
         return checkoutSucceeded;
     }
     // }}}
 
     // {{{ pollChanges
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean pollChanges(final AbstractProject project,
                                final Launcher launcher,
                                final FilePath workspace,
                                final TaskListener listener)
         throws IOException, InterruptedException
     {
         logger.debug("polling for changes in " + workspace);
         
         // default to change being detected
         boolean changeDetected = true;
         
         if (project.getLastBuild() == null) {
             logger.info("no existing build; starting a new one");
             listener.getLogger().println("no existing build; starting a new one");
         } else {
             PvcsChangeLogSet changeSet =
                 getModifications(launcher,
                                  listener,
                                  project.getLastBuild().getTimestamp());
 
 
             changeDetected = (changeSet.sizeOfEntryArray() > 0);
 
             if (! changeDetected) {
                 listener.getLogger().println("no changes detected");
             } else {
                 for (PvcsChangeLogEntry entry : changeSet.getEntryArray()) {
                     listener.getLogger().print("==> " + entry.getFileName() + " ");
                     listener.getLogger().print(entry.getRevision() + " ");
                     listener.getLogger().print(entry.getModifiedTime().getTime() + " ");
                     listener.getLogger().println(entry.getModifiedTime().getTime());
                     listener.getLogger().println(entry.getComment());
                 }
             }
         }
         
         return changeDetected;
     }
     // }}}
     
     // {{{ getModifications
     /**
      * Returns a PvcsChangeLogSet containing all change entries since
      * lastBuild.
      *
      * @param launcher the launcher to use to invoke the PVCS client.
      * @param listener task listener for outputting status
      * @param lastBuild the last time the job was built.
      */
     public PvcsChangeLogSet getModifications(final Launcher launcher,
                                              final TaskListener listener,
                                              final Calendar lastBuild)
         throws IOException, InterruptedException
     {
         Calendar now = Calendar.getInstance();
         
         logger.info("looking for changes between " + lastBuild.getTime() + " and " + now.getTime());
         listener.getLogger().println("looking for changes between " + lastBuild.getTime() + " and " + now.getTime());
 
         SimpleDateFormat df = new SimpleDateFormat(IN_DATE_FORMAT);
         
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add(getDescriptor().getExecutable());
         cmd.add("-nb", "run", "-ns", "-q");
         cmd.add("vlog");
         cmd.add("-pr" + projectRoot);
         cmd.add("-i");
         cmd.add("-ds" + df.format(lastBuild.getTime()));
         cmd.add("-de" + df.format(now.getTime()));
         cmd.add("-z");
         cmd.add(moduleDir);
 
         PipedOutputStream os = new PipedOutputStream();
 
         PvcsLogReader logReader =
             new PvcsLogReader(new PipedInputStream(os),
                               archiveRoot,
                               changeLogPrefixFudge,
                               lastBuild.getTime());
 
        logger.debug("launching command " + cmd.toList());
         
         Proc proc = launcher.launch(cmd.toCommandArray(), new String[0], null, os);
 
         Thread t = new Thread(logReader);
         t.start();
 
         int rc = proc.join();
         os.close();
         
         t.join();
 
         if (rc != 0) {
             logger.error("command failed, returned " + rc);
             listener.error("command failed, returned " + rc);
         }
 
         return logReader.getChangeLogSet();
     }
     // }}}
     
     // {{{ createChangeLogParser
     /**
      * {@inheritDoc}
      */
     @Override
     public PvcsChangeLogParser createChangeLogParser() {
         return new PvcsChangeLogParser();
     }
     // }}}
     
     // {{{ getDescriptor
     /**
      * 
      */
     @Override
     public DescriptorImpl getDescriptor() {
         return DescriptorImpl.DESCRIPTOR;
     }
     // }}}
 
     public static final class DescriptorImpl extends SCMDescriptor<PvcsScm>
     {
         private static final Log LOGGER = LogFactory.getLog(DescriptorImpl.class);
         
 		public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
         private String executable = "pcli";
     
         // {{{ constructor
         private DescriptorImpl() {
             super(PvcsScm.class, null);
             load();
         }
         // }}}
 
         // {{{ getDisplayName
         /**
          * {@inheritDoc}
          */
         @Override
         public String getDisplayName() {
             return "PVCS";
         }
         // }}}
     
         // {{{ configure
         /**
          * 
          */
         @Override
         public boolean configure(final StaplerRequest req) throws FormException {
             LOGGER.debug("configuring from " + req);
             
             executable = Util.fixEmpty(req.getParameter("pvcs.executable").trim());
             save();
             return true;
         }
         // }}}
     
         // {{{ getExecutable
         public String getExecutable() {
             return executable;
         }
         // }}}
 
         // {{{ doExecutableCheck
         /**
          * 
          */
         public void doExecutableCheck(final StaplerRequest req,
                                       final StaplerResponse resp)
             throws IOException, ServletException
             {
                 new FormFieldValidator.Executable(req, resp).process();
             }
         // }}}
     }
 }
