 package info.mikaelsvensson.ftpbackup.command.job;
 
 import info.mikaelsvensson.ftpbackup.command.AbstractCommand;
 import info.mikaelsvensson.ftpbackup.command.CommandResult;
 import info.mikaelsvensson.ftpbackup.command.CommandResultCode;
 import info.mikaelsvensson.ftpbackup.log.report.*;
 import info.mikaelsvensson.ftpbackup.model.Job;
 import info.mikaelsvensson.ftpbackup.model.ReportConfiguration;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFile;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFolder;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemRootFolder;
 import info.mikaelsvensson.ftpbackup.util.*;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 
 public abstract class AbstractJobCommand extends AbstractCommand {
 // ------------------------------ FIELDS ------------------------------
 
     static final Logger LOGGER = Logger.getLogger(AbstractJobCommand.class);
 
     final Job job;
 
     public void setFolderReader(final FolderReader folderReader) {
         this.folderReader = folderReader;
     }
 
     private FolderReader folderReader;
 
     // --------------------------- CONSTRUCTORS ---------------------------
 
     AbstractJobCommand(Job job) {
         super();
         this.job = job;
         folderReader = new DefaultFolderReader(job.getFileSet().getExclusionExpressions(), job.getFileSet().getInclusionExpressions());
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     public CommandResult perform() {
         Date commandStartDate = CalendarUtil.now().getTime();
         try {
             FTPSession session = FTPSession.connect(job.getConnectionSettings(), job.getTargetPath());
             FileProcessingStrategy strategy = createStrategy(session);
             JobActionsGeneratorStrategy jobActionsGeneratorStrategy = new DefaultJobActionsGeneratorStrategy(job.getFileSet().getFileArchivingExpressions());
             Collection<? extends ReportConfiguration> reportConfigurations = job.getReportConfigurations();
             Collection<Report> reports = new LinkedList<>();
 
             for (ReportConfiguration reportConfiguration : reportConfigurations) {
                 try {
                     Report report = createReport(reportConfiguration);
                     strategy.addFileProcessingStrategyListener(report);
                     jobActionsGeneratorStrategy.addJobActionsGeneratorStrategyListener(report);
                     folderReader.addFolderReaderListener(report);
                     reports.add(report);
                 } catch (ReportException e) {
                     LOGGER.warn(e);
                 }
             }
 
             String rootFolder = job.getFileSet().getRootFolder();
             FileSystemRootFolder localFiles = folderReader.getFileSystemTree(rootFolder);
             FileSystemFolder remoteFiles = getRemoteFiles(strategy);
 
             Collection<Runnable> actions = jobActionsGeneratorStrategy.createJobActions(localFiles, remoteFiles, session, strategy, commandStartDate);
 
             for (Runnable action : actions) {
                 action.run();
             }
 
             generateReports(reports);
 
             LOGGER.info("Local file set contains " + localFiles.getFileCount(true) + " files.");
             LOGGER.info("Remote file set contains " + remoteFiles.getFileCount(true) + " files.");
 
             return new CommandResult(CommandResultCode.SUCCESS, "Connection established. Remote directory is " + session.getWorkingDirectory());
         } catch (IOException e) {
             return new CommandResult(CommandResultCode.FAILURE, e.getMessage());
         } catch (FTPSessionException e) {
             return new CommandResult(CommandResultCode.FAILURE, e.getMessage());
         }
     }
 
     private Report createReport(ReportConfiguration configuration) throws ReportException {
         switch (configuration.getType()) {
             case CONSOLE:
                 return new ConsoleReport(
                         Boolean.parseBoolean(configuration.getParameters().get("stderr")),
                         configuration.getParameters().get("template"));
             case EMAIL:
                 return new EmailReport(
                         configuration.getParameters().get("to"),
                         configuration.getParameters().get("subjectTemplate"),
                         configuration.getParameters().get("bodyTemplate"));
             case REMOTE_FILE_LOG:
                 return new RemoteActionsLogFileReport(
                         configuration.getParameters().get("file"),
                         configuration.getParameters().get("defaultFormat"),
                         configuration.getParameters().get("movedFileFormat"));
             case LOCAL_FILE_SET:
                 return new LocalFileSetReport(
                         configuration.getParameters().get("file"),
                         configuration.getParameters().get("format"));
             default:
                 throw new ReportException("Does not understand report type " + configuration.getType());
         }
     }
 
 
     private FileSystemFolder getRemoteFiles(FileProcessingStrategy strategy) {
         String rootPath = job.getTargetPath();
         return getRemoteFiles(strategy, strategy.getWorkingDirectory(), new FileSystemRootFolder(rootPath));
     }
 
     private FileSystemFolder getRemoteFiles(FileProcessingStrategy strategy, String currentPath, FileSystemFolder folder) {
        LOGGER.debug("Looking for files in remote folder '" + currentPath + "'.");
         for (FTPFile dir : strategy.getDirectoriesInWorkingDirectory()) {
            if (!dir.getName().equals(".") && !dir.getName().equals("..") && dir.isDirectory()) {
                 if (strategy.changeWorkingDirectory(dir.getName())) {
                     FileSystemFolder subFolder = new FileSystemFolder(dir.getName(), dir.getTimestamp());
                     folder.addChild(subFolder);
                     getRemoteFiles(strategy, currentPath + "/" + dir.getName(), subFolder);
                     strategy.changeToParentDirectory();
                 }
             }
         }
         for (FTPFile file : strategy.getFilesInWorkingDirectory()) {
            if (!file.getName().equals(".") && !file.getName().equals("..") && file.isFile()) {
                 folder.addChild(new FileSystemFile(file.getName(), file.getTimestamp(), file.getSize()));
             }
         }
         return folder;
     }
 
     protected abstract FileProcessingStrategy createStrategy(FTPSession session) throws IOException;
 
     private void generateReports(Collection<? extends Report> reports) {
         for (Report report : reports) {
             report.generate();
         }
     }
 
 }
