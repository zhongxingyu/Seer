 package jcue.domain.eventcue;
 
 import jcue.domain.AbstractCue;
 import jcue.domain.CueList;
 import jcue.domain.ProjectFile;
 import jcue.domain.audiocue.AudioCue;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Stops, pauses or starts an audio cue
  *
  * @author Jaakko
  */
 public class TransportEvent extends AbstractEvent {
 
     public static final int STOP = 1;
     public static final int PAUSE = 2;
     public static final int START = 3;
     
     public static final int MODE_COUNT = 3;
     
     private int mode;
 
     public TransportEvent(int mode) {
         super(AbstractEvent.TYPE_TRANSPORT);
         
         this.mode = mode;
     }
 
     public TransportEvent() {
         this(STOP);
     }
 
     public int getMode() {
         return mode;
     }
 
     public void setMode(int mode) {
         this.mode = mode;
     }
     
     @Override
     public void perform() {
         if (this.mode == STOP) {
             super.targetCue.stop();
         } else if (this.mode == PAUSE) {
             super.targetCue.pause();
         } else if (this.mode == START) {
             super.targetCue.start(true);
         }
     }
 
     @Override
     public String toString() {
         return "Transport event";
     }
     
     public static String getModeString(int mode) {
         if (mode == STOP) {
             return "Stop";
         } else if (mode == PAUSE) {
             return "Pause";
         } else if (mode == START) {
             return "Start";
         } else {
             return "Unknown mode";
         }
     }
 
     @Override
     public Element toElement(Document doc) {
         Element result = super.toElement(doc);
 
         //Mode
         Element modeElem = doc.createElement("mode");
         modeElem.appendChild(doc.createTextNode(Integer.toString(mode)));
         result.appendChild(modeElem);
 
         return result;
     }
 
     public static TransportEvent fromElement(Element elem) {
         int mode = Integer.parseInt(ProjectFile.getTagValue("mode", elem));
         String targetName = ProjectFile.getTagValue("target", elem);
         AudioCue targetCue = (AudioCue) CueList.getInstance().getCue(targetName);
 
         TransportEvent result = new TransportEvent(mode);
 
         if (targetCue != null) {
             result.setTargetCue(targetCue);
         } else {
             ProjectFile.addToTargetQueue(result, targetName);
         }
 
         return result;
     }
 }
