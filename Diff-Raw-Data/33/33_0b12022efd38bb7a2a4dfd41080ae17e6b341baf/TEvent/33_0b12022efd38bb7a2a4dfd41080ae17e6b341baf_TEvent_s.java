 package state.transducers;
 
 /**
  * Events that the transducer can fire
  * 
  * @author Jon Sun
  */
 public enum TEvent
 {
 	//gui
	START_ANIMATION1,
	START_ANIMATION2,
	ANIMATION1_FINISHED,
	ANIMATION2_FINISHED,
 	SendPart,DonePickingUpParts,
 	
 	//control panel
	START_ANIMATIONS, 
	ALL_ANIMATIONS_FINISHED,
 }
