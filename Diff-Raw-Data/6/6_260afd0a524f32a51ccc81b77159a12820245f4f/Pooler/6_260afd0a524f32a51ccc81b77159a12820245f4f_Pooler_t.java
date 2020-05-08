 package model.MARK_II;
 
 /**
 * Abstract class extended by SpatialPooler.java and TemporalPooler.java classes.
  *
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version MARK II | June 26, 2013
  */
 public abstract class Pooler {
     protected Region region;
     private boolean learningState;
 
    // TODO: how do you make the learning algorithms code parallel for a tree
    // of Regions?
 
     public void changeRegion(Region newRegion)
     {
 	this.learningState = false;
 	if (newRegion == null) {
 	    throw new IllegalArgumentException(
 		    "newRegion in Pooler class changeRegion method cannot be null");
 	}
 	this.region = newRegion;
     }
 
     public boolean getLearningState() {
 	return this.learningState;
     }
 
     public void setLearningState(boolean learningState) {
 	this.learningState = learningState;
     }
 }
