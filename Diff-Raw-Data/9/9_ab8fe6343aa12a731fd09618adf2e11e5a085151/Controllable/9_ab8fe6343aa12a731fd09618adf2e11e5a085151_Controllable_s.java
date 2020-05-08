 package javax.microedition.media;
 
 public interface Controllable {
     
 	/**
 	 * @throws IllegalArgumentException
	 * @throw IllegalStateException
 	 */
 	Control getControl(String controlType);
 
     /**
      * @throws IllegalStateException
      */
     Control[] getControls();
 
 }
