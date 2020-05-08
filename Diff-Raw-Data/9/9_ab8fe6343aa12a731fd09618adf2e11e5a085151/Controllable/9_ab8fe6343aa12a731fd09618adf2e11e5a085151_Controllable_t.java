 package javax.microedition.media;
 
 public interface Controllable {
     
 	/**
 	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
 	 */
 	Control getControl(String controlType);
 
     /**
      * @throws IllegalStateException
      */
     Control[] getControls();
 
 }
