 package info.mikaelsvensson.ftpbackup.command.job;
 
 import info.mikaelsvensson.ftpbackup.command.CommandResult;
 import info.mikaelsvensson.ftpbackup.command.CommandResultCode;
 import info.mikaelsvensson.ftpbackup.model.Job;
 import info.mikaelsvensson.ftpbackup.util.FTPSession;
 
 import java.io.IOException;
 
 public class TestConnectionCommand extends AbstractJobCommand {
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public TestConnectionCommand(Job job) {
         super(job);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
    protected FileProcessingStrategy createStrategy(FTPSession session) throws IOException {
        return new RealFileProcessingStrategy(session);
     }
 
     @Override
     public CommandResult perform() {
         FTPSession session = FTPSession.connect(job.getConnectionSettings(), job.getTargetPath());
         try {
             return new CommandResult(CommandResultCode.SUCCESS, "Connection established. Remote directory is " + session.getWorkingDirectory());
         } catch (IOException e) {
             return new CommandResult(CommandResultCode.FAILURE, e.getMessage());
         }
     }
 }
