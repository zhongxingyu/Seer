 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.domscontent.BroadcastMetadata;
 import dk.statsbiblioteket.broadcasttranscoder.domscontent.Channel;
 import dk.statsbiblioteket.broadcasttranscoder.util.CalendarUtils;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileFormatEnum;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *  Calculates the clips needed from each source file, based on the programBroadcast.
  */
 public class ClipFinderProcessor extends ProcessorChainElement {
 
     private static Logger logger = LoggerFactory.getLogger(ClipFinderProcessor.class);
 
 
     @Override
     protected void processThis(TranscodeRequest request, SingleTranscodingContext context) throws ProcessorException {
         Integer programNumber = null;
         final boolean isMultiMux = request.getFileFormat().equals(FileFormatEnum.MULTI_PROGRAM_MUX);
         if (isMultiMux) {
             programNumber = findProgramId(request);
             if (programNumber == null) {
                 throw new ProcessorException("Could not find channel in mux");
             }
         }
         long programStart = CalendarUtils.getTimestamp(request.getProgramBroadcast().getTimeStart());
         long programEnd = CalendarUtils.getTimestamp(request.getProgramBroadcast().getTimeStop());
         int startOffset;
         int endOffset;
         switch (request.getFileFormat()) {
             case MPEG_PS:
                 if (request.isTvmeter()) {
                     startOffset = context.getStartOffsetPSWithTVMeter();
                     endOffset = context.getEndOffsetPSWithTVMeter();
                 } else {
                     startOffset = context.getStartOffsetPS();
                     endOffset = context.getEndOffsetPS();
                 }
                 break;
             case AUDIO_WAV:
                 startOffset = context.getStartOffsetWAV();
                 endOffset = context.getEndOffsetWAV();
                 break;
             default:
                 if (request.isTvmeter()) {
                     startOffset = context.getStartOffsetTSWithTVMeter();
                     endOffset = context.getEndOffsetTSWithTVMeter();
                 } else {
                     startOffset = context.getStartOffsetTS();
                     endOffset = context.getEndOffsetTS();
                 }
                 break;
         }
        startOffset += context.getDigitvStartOffset();
        endOffset += context.getDigitvEndOffset();
         programStart += startOffset*1000L;
         programEnd += endOffset*1000L;
         request.setStartOffsetUsed(startOffset);
         request.setEndOffsetUsed(endOffset);
         long bitrate = request.getBitrate();
         List<TranscodeRequest.FileClip> clips = new ArrayList<TranscodeRequest.FileClip>();
         for (BroadcastMetadata metadata: request.getBroadcastMetadata()) {
             File file = request.getFileMap().get(metadata);
             TranscodeRequest.FileClip clip = new TranscodeRequest.FileClip(file.getAbsolutePath());
             clip.setProgramId(programNumber);   //null if not multi-mux
             long fileStart = CalendarUtils.getTimestamp(metadata.getStartTime());
             long fileEnd = CalendarUtils.getTimestamp(metadata.getStopTime());
             clip.setFileStartTime(fileStart);
             clip.setFileEndTime(fileEnd);
             //Four possibilites
             //File contains start and stop times
             //File contains only start
             //File contains only stop
             //File contains neither
             if (programStart >= fileStart && programEnd <= fileEnd) {
                 clip.setStartOffsetBytes(bitrate * (programStart - fileStart) / 1000L);
                 clip.setClipLength(bitrate*(programEnd-programStart)/1000L);
                 logger.debug("Added clip " + clip);
                 clips.add(clip);
             } else if (programStart >= fileStart && programStart <= fileEnd && programEnd > fileEnd) {
                 clip.setStartOffsetBytes(bitrate * (programStart - fileStart) / 1000L);
                 logger.debug("Added clip " + clip);
                 clips.add(clip);
             } else if (programEnd >= fileStart && programEnd <= fileEnd && programStart < fileStart) {
                 clip.setClipLength(bitrate*(programEnd - fileStart )/1000L);
                 logger.debug("Added clip " + clip);
                 clips.add(clip);
             } else if (programEnd < fileStart || programStart > fileEnd) {
                 //This is not an error because the file could be necessary depending on offsets.
                 logger.warn("File " + file.getAbsolutePath() + " is included in program " + request.getObjectPid()+ " " +
                         "but does not appear to be part of the broadcast which ran from " + request.getProgramBroadcast().getTimeStart() + " to " +
                         request.getProgramBroadcast().getTimeStop());
             }
             request.setClips(clips);
             logger.debug("Clips found:\n" + clips.toString());
         }
 
 
     }
 
     private Integer findProgramId(TranscodeRequest request) {
         String channelName = request.getProgramBroadcast().getChannelId();
         logger.debug("Finding program number for " + channelName);
         BroadcastMetadata metadata = request.getBroadcastMetadata().get(0);
         for (Channel channel: metadata.getChannels().getChannel()) {
              if (channel.getChannelID().equals(channelName)) {
                  logger.debug("Program number for " + channelName + " is " + channel.getMuxProgramNr());
                  return channel.getMuxProgramNr();
              }
         }
         return null;
     }
 
 }
