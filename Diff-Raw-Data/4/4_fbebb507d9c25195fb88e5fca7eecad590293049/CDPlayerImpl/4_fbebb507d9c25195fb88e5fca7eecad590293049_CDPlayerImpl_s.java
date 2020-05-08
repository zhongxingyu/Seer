 // Automatically generated code, do not edit
 package statemachine.generated;
 import java.util.Map;
 import statemachine.year4.codegen.GeneratedMachine;
 public class CDPlayerImpl extends GeneratedMachine {
   @Override protected void internalProcessEvent(int event) {
     switch(state) {
     case 0: // STOPPED
       switch(event) {
       case 0: // BACK
         if(track>1) {
           state = 0; // STOPPED
         }
       break;
       case 1: // FORWARD
         {
           track+=1;
           state = 0; // STOPPED
         }
       break;
       case 2: // PLAY
         if(track==0) {
           track=1;
           state = 1; // PLAYING
         }
       else
         {
           state = 1; // PLAYING
         }
       break;
       default: ; // ignore
       }
     break;
     case 1: // PLAYING
       switch(event) {
       case 3: // STOP
         {
           track=0;
           state = 0; // STOPPED
         }
       break;
       case 4: // TRACK_END
         {
           track+=1;
           state = 1; // PLAYING
         }
       break;
       case 5: // PAUSE
         {
           state = 2; // PAUSED
         }
       break;
       default: ; // ignore
       }
     break;
     case 2: // PAUSED
       switch(event) {
       case 0: // BACK
         if(track>1) {
           track+=-1;
          state = 3; // PAUED
         }
       break;
       case 3: // STOP
         {
           state = 0; // STOPPED
         }
       break;
       case 1: // FORWARD
         {
           track+=1;
           state = 2; // PAUSED
         }
       break;
       case 2: // PLAY
         {
           state = 1; // PLAYING
         }
       break;
       default: ; // ignore
       }
     break;
     default: throw new Error("Internal error: unsupported state code");
     }
   }
   private int track;
   /** Get the value of the extended state track
     * @return value of track
   */
   public int get_track() { return track; }
   @Override protected void internalInitialize(Map<String, Integer> event_code2int, Map<Integer, String> state_int2code) {
    state_int2code.put(3,"PAUED");
     state_int2code.put(0,"STOPPED");
     state_int2code.put(1,"PLAYING");
     state_int2code.put(2,"PAUSED");
     event_code2int.put("STOP",3);
     event_code2int.put("BACK",0);
     event_code2int.put("FORWARD",1);
     event_code2int.put("TRACK_END",4);
     event_code2int.put("PAUSE",5);
     event_code2int.put("PLAY",2);
   }
 }
