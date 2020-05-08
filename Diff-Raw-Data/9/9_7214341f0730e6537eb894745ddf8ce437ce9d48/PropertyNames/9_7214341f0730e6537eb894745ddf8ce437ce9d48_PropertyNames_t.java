 /* $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  *
  */
 package dk.statsbiblioteket.broadcasttranscoder.cli;
 
 public class PropertyNames {
 
     private PropertyNames(){}
 
     /*
     Properties related to DOMS
      */
     public static final String DOMS_ENDPOINT = "domsWSAPIEndpointUrl";
     public static final String DOMS_USER = "domsUsername";
     public static final String DOMS_PASSWORD = "domsPassword";
     public static final String DOMS_VIEWANGLE = "domsViewAngle";
 
     /*
     Properties related to file paths
      */
     public static final String FILE_DIR = "fileOutputDirectory";
     public static final String PREVIEW_DIR = "previewOutputDirectory";
     public static final String SNAPSHOT_DIR = "snapshotOutputDirectory";
     public static final String LOCK_DIR = "lockDirectory";
     public static final String FILE_DEPTH = "fileDepth";
 
     /*
     Properties related to transcoding
      */
     public static final String VIDEO_BITRATE = "videoBitrate";
     public static final String AUDIO_BITRATE = "audioBitrate";
     public static final String HEIGHT = "heightInPixels";
     public static final String X264_VLC_PARAMS = "x264VlcParams";
     public static final String X264_FFMPEG_PARAMS = "x264FfmpegProgramStreamParams";
     public static final String TRANSCODING_DIVISOR = "transcodingTimeoutDivisor";
     public static final String ANALYSIS_CLIP_LENGTH = "analysisCliplengthBytes";
 
     /*
     Properties related to Offsets
      */
     public static final String START_OFFSET_TS = "startOffsetTS";
     public static final String END_OFFSET_TS = "endOffsetTS";
     public static final String START_OFFSET_PS = "startOffsetPS";
     public static final String END_OFFSET_PS = "endOffsetPS";
     public static final String START_OFFSET_WAV = "startOffsetWAV";
     public static final String END_OFFSET_WAV = "endOffsetWAV";
 
     /*
     Properties relating to handling of missing data
      */
     public static final String MAX_MISSING_START = "maxMissingStart";
     public static final String MAX_MISSING_END = "maxMissingEnd";
     public static final String MAX_HOLE_SIZE = "maxHole";
     public static final String GAP_TOLERANCE = "gapToleranceSeconds";
 
 
     /*
     Properties related to nearline storage
      */
     public static final String FILE_FINDER = "nearlineFilefinderUrl";
     public static final String MAX_FILES_FETCHED = "maxFilesFetched";
 
     /*
     Properties relating to previews
      */
     public static final String PREVIEW_LENGTH = "previewLength";
     public static final String PREVIEW_TIMEOUT = "previewTimeout";
 
     /*
     Properties relating to snapshots
      */
     public static final String SNAPSHOT_SCALE = "snapshotScale";
     public static final String SNAPSHOT_TARGET_NUMERATOR = "snapshotTargetNumerator";
     public static final String SNAPSHOT_TARGET_DENOMINATIOR = "snapshotTargetDenominator";
     public static final String SNAPSHOT_FRAMES = "snapshotFrames";
     public static final String SNAPSHOT_PADDING = "snapshotPaddingSeconds";
     public static final String SNAPSHOT_TIMEOUT_DIVISOR = "snapshotTimeoutDivisor";
 
     public static final String SOX_TRANSCODE_PARAMS = "soxTranscodeParams";
 
     public static final String DEFAULT_TIMESTAMP= "defaultTranscodingTimestamp";
 
     /**
      * Properties relating to reklamefilm
      */
    public static final String REKLAMEFILM_ROOT_DIRECTORY_LIST = "reklamefilmRootDirectories";
 }
