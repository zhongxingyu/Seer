 package dk.statsbiblioteket.broadcasttranscoder.cli;
 
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.TranscodingRecord;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmFileResolver;
 
 import java.io.File;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 1/23/13
  * Time: 3:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SingleTranscodingContext<T extends TranscodingRecord> extends InfrastructureContext<T> {
 
     private String programpid;
     private File behaviourConfigFile;
     private boolean overwrite;
     private boolean onlyTranscodeChanges;
 
 
 
     private int videoBitrate;
     private int audioBitrate;
     private int videoHeight;
     private String vlcTranscodingString;
     private String ffmpegTranscodingString;
     private String vlcRemuxingString;
     private float transcodingTimeoutDivisor;
     private long analysisClipLength;
     private String videoOutputSuffix;
 
     private int maxMissingStart;
     private int maxMissingEnd;
     private int maxHole;
     private int gapToleranceSeconds;
 
     private int startOffsetTS;
     private int endOffsetTS;
     private int startOffsetPS;
     private int endOffsetPS;
     private int startOffsetWAV;
     private int endOffsetWAV;
     private int startOffsetTSWithTVMeter;
     private int endOffsetTSWithTVMeter;
     private int startOffsetPSWithTVMeter;
     private int endOffsetPSWithTVMeter;
 
 
     private int previewLength;
     private int previewTimeout;
 
     private int snapshotScale;
     private int snapshotTargetNumerator;
     private int snapshotTargetDenominator;
     private int snapshotFrames;
     private int snapshotPaddingSeconds;
     private float snapshotTimeoutDivisor;
 
     private String soxTranscodeParams;
 
     private long timestampOfExistingTranscoding;
     private long timestampOfNewTranscoding;
 
     private long transcodingTimestamp;
 
     private long defaultTranscodingTimestamp;
 
     private ReklamefilmFileResolver reklamefilmFileResolver;
 
     private String domsViewAngle;
 
     public String getVlcRemuxingString() {
         return vlcRemuxingString;
     }
 
     public void setVlcRemuxingString(String vlcRemuxingString) {
         this.vlcRemuxingString = vlcRemuxingString;
     }
 
     public SingleTranscodingContext() {
     }
 
     public String getProgrampid() {
         return programpid;
     }
 
     public void setProgrampid(String programpid) {
         this.programpid = programpid;
     }
 
     public File getBehaviourConfigFile() {
         return behaviourConfigFile;
     }
 
     public void setBehaviourConfigFile(File behaviourConfigFile) {
         this.behaviourConfigFile = behaviourConfigFile;
     }
 
     public boolean isOverwrite() {
         return overwrite;
     }
 
     public void setOverwrite(boolean overwrite) {
         this.overwrite = overwrite;
     }
 
 
 
     public int getVideoBitrate() {
         return videoBitrate;
     }
 
     public void setVideoBitrate(int videoBitrate) {
         this.videoBitrate = videoBitrate;
     }
 
     public int getAudioBitrate() {
         return audioBitrate;
     }
 
     public void setAudioBitrate(int audioBitrate) {
         this.audioBitrate = audioBitrate;
     }
 
     public int getVideoHeight() {
         return videoHeight;
     }
 
     public void setVideoHeight(int videoHeight) {
         this.videoHeight = videoHeight;
     }
 
     public String getVlcTranscodingString() {
         return vlcTranscodingString;
     }
 
     public void setVlcTranscodingString(String vlcTranscodingString) {
         this.vlcTranscodingString = vlcTranscodingString;
     }
 
     public String getFfmpegTranscodingString() {
         return ffmpegTranscodingString;
     }
 
     public void setFfmpegTranscodingString(String ffmpegTranscodingString) {
         this.ffmpegTranscodingString = ffmpegTranscodingString;
     }
 
     public float getTranscodingTimeoutDivisor() {
         return transcodingTimeoutDivisor;
     }
 
     public void setTranscodingTimeoutDivisor(float transcodingTimeoutDivisor) {
         this.transcodingTimeoutDivisor = transcodingTimeoutDivisor;
     }
 
     public long getAnalysisClipLength() {
         return analysisClipLength;
     }
 
     public void setAnalysisClipLength(long analysisClipLength) {
         this.analysisClipLength = analysisClipLength;
     }
 
     public int getMaxMissingStart() {
         return maxMissingStart;
     }
 
     public void setMaxMissingStart(int maxMissingStart) {
         this.maxMissingStart = maxMissingStart;
     }
 
     public int getMaxMissingEnd() {
         return maxMissingEnd;
     }
 
     public void setMaxMissingEnd(int maxMissingEnd) {
         this.maxMissingEnd = maxMissingEnd;
     }
 
     public int getMaxHole() {
         return maxHole;
     }
 
     public void setMaxHole(int maxHole) {
         this.maxHole = maxHole;
     }
 
     public int getGapToleranceSeconds() {
         return gapToleranceSeconds;
     }
 
     public void setGapToleranceSeconds(int gapToleranceSeconds) {
         this.gapToleranceSeconds = gapToleranceSeconds;
     }
 
     public int getStartOffsetTS() {
         return startOffsetTS;
     }
 
     public void setStartOffsetTS(int startOffsetTS) {
         this.startOffsetTS = startOffsetTS;
     }
 
     public int getEndOffsetTS() {
         return endOffsetTS;
     }
 
     public void setEndOffsetTS(int endOffsetTS) {
         this.endOffsetTS = endOffsetTS;
     }
 
     public int getStartOffsetPS() {
         return startOffsetPS;
     }
 
     public void setStartOffsetPS(int startOffsetPS) {
         this.startOffsetPS = startOffsetPS;
     }
 
     public int getEndOffsetPS() {
         return endOffsetPS;
     }
 
     public void setEndOffsetPS(int endOffsetPS) {
         this.endOffsetPS = endOffsetPS;
     }
 
     public int getStartOffsetWAV() {
         return startOffsetWAV;
     }
 
     public void setStartOffsetWAV(int startOffsetWAV) {
         this.startOffsetWAV = startOffsetWAV;
     }
 
     public int getEndOffsetWAV() {
         return endOffsetWAV;
     }
 
     public void setEndOffsetWAV(int endOffsetWAV) {
         this.endOffsetWAV = endOffsetWAV;
     }
 
     public int getStartOffsetTSWithTVMeter() {
         return startOffsetTSWithTVMeter;
     }
 
     public void setStartOffsetTSWithTVMeter(int startOffsetTSWithTVMeter) {
         this.startOffsetTSWithTVMeter = startOffsetTSWithTVMeter;
     }
 
     public int getEndOffsetTSWithTVMeter() {
         return endOffsetTSWithTVMeter;
     }
 
     public void setEndOffsetTSWithTVMeter(int endOffsetTSWithTVMeter) {
         this.endOffsetTSWithTVMeter = endOffsetTSWithTVMeter;
     }
 
     public int getStartOffsetPSWithTVMeter() {
         return startOffsetPSWithTVMeter;
     }
 
     public void setStartOffsetPSWithTVMeter(int startOffsetPSWithTVMeter) {
         this.startOffsetPSWithTVMeter = startOffsetPSWithTVMeter;
     }
 
     public int getEndOffsetPSWithTVMeter() {
         return endOffsetPSWithTVMeter;
     }
 
     public void setEndOffsetPSWithTVMeter(int endOffsetPSWithTVMeter) {
         this.endOffsetPSWithTVMeter = endOffsetPSWithTVMeter;
     }
 
 
     public int getPreviewLength() {
         return previewLength;
     }
 
     public void setPreviewLength(int previewLength) {
         this.previewLength = previewLength;
     }
 
     public int getPreviewTimeout() {
         return previewTimeout;
     }
 
     public void setPreviewTimeout(int previewTimeout) {
         this.previewTimeout = previewTimeout;
     }
 
     public int getSnapshotScale() {
         return snapshotScale;
     }
 
     public void setSnapshotScale(int snapshotScale) {
         this.snapshotScale = snapshotScale;
     }
 
     public int getSnapshotTargetNumerator() {
         return snapshotTargetNumerator;
     }
 
     public void setSnapshotTargetNumerator(int snapshotTargetNumerator) {
         this.snapshotTargetNumerator = snapshotTargetNumerator;
     }
 
     public int getSnapshotTargetDenominator() {
         return snapshotTargetDenominator;
     }
 
     public void setSnapshotTargetDenominator(int snapshotTargetDenominator) {
         this.snapshotTargetDenominator = snapshotTargetDenominator;
     }
 
     public int getSnapshotFrames() {
         return snapshotFrames;
     }
 
     public void setSnapshotFrames(int snapshotFrames) {
         this.snapshotFrames = snapshotFrames;
     }
 
     public int getSnapshotPaddingSeconds() {
         return snapshotPaddingSeconds;
     }
 
     public void setSnapshotPaddingSeconds(int snapshotPaddingSeconds) {
         this.snapshotPaddingSeconds = snapshotPaddingSeconds;
     }
 
     public float getSnapshotTimeoutDivisor() {
         return snapshotTimeoutDivisor;
     }
 
     public void setSnapshotTimeoutDivisor(float snapshotTimeoutDivisor) {
         this.snapshotTimeoutDivisor = snapshotTimeoutDivisor;
     }
 
     public String getSoxTranscodeParams() {
         return soxTranscodeParams;
     }
 
     public void setSoxTranscodeParams(String soxTranscodeParams) {
         this.soxTranscodeParams = soxTranscodeParams;
     }
 
     public long getTimestampOfExistingTranscoding() {
         return timestampOfExistingTranscoding;
     }
 
     public void setTimestampOfExistingTranscoding(long timestampOfExistingTranscoding) {
         this.timestampOfExistingTranscoding = timestampOfExistingTranscoding;
     }
 
     public long getTimestampOfNewTranscoding() {
         return timestampOfNewTranscoding;
     }
 
     public void setTimestampOfNewTranscoding(long timestampOfNewTranscoding) {
         this.timestampOfNewTranscoding = timestampOfNewTranscoding;
     }
 
     public long getTranscodingTimestamp() {
         return transcodingTimestamp;
     }
 
     public void setTranscodingTimestamp(long transcodingTimestamp) {
         this.transcodingTimestamp = transcodingTimestamp;
     }
 
     public long getDefaultTranscodingTimestamp() {
         return defaultTranscodingTimestamp;
     }
 
     public void setDefaultTranscodingTimestamp(long defaultTranscodingTimestamp) {
         this.defaultTranscodingTimestamp = defaultTranscodingTimestamp;
     }
 
     public ReklamefilmFileResolver getReklamefilmFileResolver() {
         return reklamefilmFileResolver;
     }
 
     public void setReklamefilmFileResolver(ReklamefilmFileResolver reklamefilmFileResolver) {
         this.reklamefilmFileResolver = reklamefilmFileResolver;
     }
 
     public String getDomsViewAngle() {
         return domsViewAngle;
     }
 
     public void setDomsViewAngle(String domsViewAngle) {
         this.domsViewAngle = domsViewAngle;
     }
 
     public boolean isOnlyTranscodeChanges() {
         return onlyTranscodeChanges;
     }
 
     public void setOnlyTranscodeChanges(boolean onlyTranscodeChanges) {
         this.onlyTranscodeChanges = onlyTranscodeChanges;
     }
 
     public String getVideoOutputSuffix() {
         return videoOutputSuffix;
     }
 
     public void setVideoOutputSuffix(String videoOutputSuffix) {
         this.videoOutputSuffix = videoOutputSuffix;
     }
 }
