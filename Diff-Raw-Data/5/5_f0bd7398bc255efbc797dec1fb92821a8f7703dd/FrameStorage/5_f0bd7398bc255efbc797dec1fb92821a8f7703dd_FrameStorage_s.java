 package caja.jcastulo.stream;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import org.slf4j.LoggerFactory;
 
 /**
  * Represents a number of <code>TimedFrame</code>s in a period of time
  * 
  * Thread safe to avoid <code>TimedFrame</code>s been accessed and modify at the same time
  *
  * @author bysse
  */
 public class FrameStorage {
 
     /**
      * The logger
      */
     final org.slf4j.Logger logger = LoggerFactory.getLogger(FrameStorage.class);
     
     /**
      * The timed frames
      */
     private LinkedList<TimedFrame> timedFrames = new LinkedList<TimedFrame>();
     
     /**
      * This is MP3 frame length in milliseconds
      */
     public static final long LENGTH = 26; 
 
     /**
      * Looks for the frame that overlaps the given time. If the FrameStorage is
      * empty {@link EmptyFrameStorageException} is thrown. If no frame
      * could be found for the specified time
      * {@link NoLoadedFrameException} or {@link OldFrameException}
      * is thrown.
      *
      * @param time - the time with which the <code>TimedFrame</code> is looked for
      * @return a TimedFrame that overlapped the given time.
      */
     public synchronized TimedFrame find(final long time) {
         if (timedFrames.isEmpty()) {
             throw new EmptyFrameStorageException();
         }
         long firstFrameTime = timedFrames.getFirst().getStartTime();
         long lastFrameTime = timedFrames.getLast().getStopTime();
         // make sure the frame is within the represented interval
         if (lastFrameTime <= time) {
             //log.debug("Request: "+time+", LastFrame: "+lastFrameTime+", Diff: "+(time-lastFrameTime));			
             throw new NoLoadedFrameException();
         }
         if(time < firstFrameTime) {
             throw new OldFrameException();
         }
         int index = (int) ((time - firstFrameTime) / LENGTH);
        return timedFrames.get(index);
     }
 
     /**
      * Adds a timed frame to the FrameStorage. This method only adds the frame to the
      * end of the storage. So adding out-of-order frames will cause error in
      * playback.
      *
      * @param timedFrame - timedFrame to be added
      */
     public synchronized void add(TimedFrame timedFrame) {
         timedFrames.add(timedFrame);
     }
 
     /**
      * Removes all timed frames that are older than the given time.
      *
      * @param time - would be the older timed frame
      */
     public synchronized void purgeUntil(long time) {
         Iterator<TimedFrame> iterator = timedFrames.iterator();
         while (iterator.hasNext()) {
             if (iterator.next().getStopTime() <= time) {
                 iterator.remove();
             } else {
                 break;
             }
         }
     }
 
     /**
     * @return End time of the last frame in storage or null if the storage is empty.
      */
     public synchronized Long getLastFrameTime() {
         if (timedFrames.isEmpty()) {
             return null;
         }
         return timedFrames.getLast().getStopTime();
     }
 
     /**
      * Clears the frame storage.
      */
     public synchronized void clear() {
         logger.debug("clearing frame storage");
         timedFrames.clear();
     }
 
     /**
      * @return start time of first frame or null if the storage is empty.
      */
     public synchronized Long getFirstFrameTime() {
         if (timedFrames.isEmpty()) {
             return null;
         }
         return timedFrames.getFirst().getStartTime();
     }
 
     
     @Override
     public String toString() {
         return "FixedFrameSizeFrameStorage{" + "number of frames=" + timedFrames.size() + ", frameLength=" + LENGTH + '}';
     }
     
 }
