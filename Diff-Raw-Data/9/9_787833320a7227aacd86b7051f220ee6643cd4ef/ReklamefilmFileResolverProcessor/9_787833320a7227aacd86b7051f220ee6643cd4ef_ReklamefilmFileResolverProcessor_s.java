 package dk.statsbiblioteket.broadcasttranscoder.reklamefilm;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
 import dk.statsbiblioteket.broadcasttranscoder.processors.ProcessorChainElement;
 import dk.statsbiblioteket.broadcasttranscoder.processors.ProcessorException;
 import dk.statsbiblioteket.broadcasttranscoder.processors.TranscodeRequest;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileFormatEnum;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 
 /**
  * This processor resolves the input pid to a locally mounted mediafile and store it in the
  * clipperCommand field of the request (since we always just clip the whole file).
  */
 public class ReklamefilmFileResolverProcessor extends ProcessorChainElement {
 
     private static Logger logger = LoggerFactory.getLogger(ReklamefilmFileResolverProcessor.class);
 
 
     @Override
     protected void processThis(TranscodeRequest request, Context context) throws ProcessorException {
         ReklamefilmFileResolver resolver = context.getReklamefilmFileResolver();
         String pid = context.getProgrampid();
         File mediafile = resolver.resolverPidToLocalFile(pid);
         request.setClipperCommand(mediafile.getAbsolutePath());
         logger.info("Resolved " + pid + " to " + request.getClipperCommand());
         final long nominalDurationSeconds = 300L;
         request.setBitrate(mediafile.length()/nominalDurationSeconds);
         request.setFileFormat(FileFormatEnum.MPEG_PS);
       }
 }
