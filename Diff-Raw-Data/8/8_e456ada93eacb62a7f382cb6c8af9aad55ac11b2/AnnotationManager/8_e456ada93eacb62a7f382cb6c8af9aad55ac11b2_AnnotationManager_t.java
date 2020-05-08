 /**
  * AnnotationManager.java created on 08.08.2012
  */
 
 package gst.data;
 
 import org.unisens.Event;
 
 import gst.test.Debug;
 import gst.ui.StatusBar;
 
 /**
  * Class that holds information of the selected {@code AnnotationController} and predefined annotations.
  * @author Enrico Grunitz
 * @version 0.1.1.3 (22.10.2012)
  */
 public class AnnotationManager {
 	/**
 	 * Enumeration for the predefined annotations. 
 	 */
 	public enum Preset {
 		N,
 		V,
 		B,
 		E;
 	}
 	
 	/** the currently selected data channel to read from or write to */		private AnnotationController selectedAnnotationChannel;
 	/** stored information of the last written annotation type */			private String type;
 	/** stored information of the last written annotation comment */		private String comment;
 	
 	/**
 	 * Constructor for objects with no selected channel and the first preset selected.
 	 */
 	public AnnotationManager() {
 		this.selectedAnnotationChannel = null;
 		this.selectPreset(Preset.N);
 	}
 	
 	/**
 	 * Sets the selected channel.
 	 * @param actrl the controller to use or null to clear selection
 	 */
 	public void selectController(AnnotationController actrl) {
 		this.selectedAnnotationChannel = actrl;
 		if(actrl != null) {
 			StatusBar.getInstance().updateText(actrl.getFullName());
 		} else {
 			StatusBar.getInstance().updateText("-keine Auswahl-");
 		}
 	}
 	
 	/** @return {@link #selectedAnnotationChannel} */
 	public AnnotationController selectedController() {
 		return this.selectedAnnotationChannel;
 	}
 	
 	/**
 	 * Selects the preset to use.
 	 * @param preset preset to use
 	 * @return false if the preset is not defined, else true
 	 */
 	public boolean selectPreset(Preset preset) {
 		switch(preset) {
 		case N:
 			this.type = "N";
 			this.comment =  "";
 			break;
 		case V:
 			this.type = "V";
 			this.comment =  "";
 			break;
 		case B:
 			this.type = "B";
 			this.comment =  "signal loss";
 			break;
 		case E:
 			this.type = "E";
 			this.comment =  "signal recovered";
 			break;
 		default:
 			Debug.println(Debug.annotationManager, "unknown preset selected");
 			return false;
 		}
 		StatusBar.getInstance().updateText(this.type, this.comment);
 		return true;
 	}
 	
 	/**
 	 * Writes an annotation to the selected channel.
 	 * @param time point in time where the annotation belongs to
 	 * @param type the type
 	 * @param comment the comment
 	 * @return true if successful, else false (no channel selected)
 	 */
 	public boolean addAnnotation(double time, String type, String comment) {
 		if(this.selectedAnnotationChannel != null) {
 			this.type = type;
 			this.comment = comment;
 			this.selectedAnnotationChannel.addAnnotation(time, type, comment);
 			return true;
 		}
 		Debug.println(Debug.annotationManager, "annotation not added - no channel selected");
 		return false;
 	}
 	
 	/**
 	 * Writes an annotation the the selected channel. Type and comment are the same last used to write a annotation or of the previously
 	 * selected preset
 	 * @param time point in time where the annotation belongs to
 	 * @return true if successful, else false (no channel selected)
 	 */
 	public boolean addAnnotation(double time) {
 		return this.addAnnotation(time, this.type, this.comment);
 	}
 	
 	/**
 	 * Removes an annotation from the selected channel at the given point in time. Convenience method for
 	 * {@link #removeAnnotation(double, double)} with {@code range == 0}.
 	 * @param time the point in time
 	 * @return the number of annotations removed
 	 */
 	public int removeAnnotation(double time) {
 		return this.removeAnnotation(time, 0);
 	}
 	
 	/**
 	 * Removes all annotations from the selected channel between {@code time - range} and {@code time + range}.
 	 * @param time the central point in time
 	 * @param range the range
 	 * @return the number of annotations removed
 	 */
 	public int removeAnnotation(double time, double range) {
 		if(this.selectedAnnotationChannel != null) {
 			AnnotationList al = this.selectedAnnotationChannel.getAnnotation(time, range);
 			this.selectedAnnotationChannel.removeAnnotation(al);
 			return al.size();
 		}
 		Debug.println(Debug.annotationManager, "annotation(s) not removed - no channel selected");
 		return 0;
 	}
 	
 	/**
 	 * Returns the number of annotations at the specified point in time. Convenience method for
 	 * {@link #getAnnotationCount(double, double)} with {@code range == 0}.
 	 * @param time the time
 	 * @return number of annotations
 	 */
 	public int getAnnotationCount(double time) {
 		return this.getAnnotationCount(time, 0);
 	}
 	
 	/**
 	 * Returns the number of annotations in the range {@code time - range} to {@code time + range}). 
 	 * @param time central point in time
 	 * @param range the range
 	 * @return number of annotations in the specified time range
 	 */
 	public int getAnnotationCount(double time, double range) {
		if(this.selectedAnnotationChannel != null) {
			return this.selectedAnnotationChannel.getAnnotation(time, range).size();
		}
		return 0;
 	}
 	
 	/**
 	 * Moves all annotations in range ({@code time - range} to {@code time + range}) to the new range ({@code newTime - range}
 	 * to {@code newTime + range}).
 	 * @param time central point in time of starting time-range
 	 * @param range the range
 	 * @param newTime central point in time of the destination time-range
 	 * @return number of annotations moved
 	 */
 	public int moveAnnotations(double time, double range, double newTime) {
 		AnnotationList list = this.selectedAnnotationChannel.getAnnotation(time, range);
 		this.selectedAnnotationChannel.removeAnnotation(list);
 		double timeDiff = newTime - time;
 		for(int i = 0; i < list.size(); i++) {
 			// adding annotations one by one may be slow due to multiple DataChange-notifications
 			this.selectedAnnotationChannel.addAnnotation(list.getTime(i) + timeDiff, list.getType(i), list.getComment(i));
 		}
 		return list.size();
 	}
 	
 	/**
 	 * Moves all annotations from {@code time} to {@code newTime}. Convenience method for
 	 * {@link #moveAnnotations(double, double, double)} with {@code range == 0}.
 	 * @param time time of annotations to move
 	 * @param newTime time of annotations to move to
 	 * @return number of annotations moved
 	 */
 	public int moveAnnotations(double time, double newTime) {
 		return this.moveAnnotations(time, 0, newTime);
 	}
 	
 	/**
 	 * Updates the message of the {@link gst.ui.StatusBar} to represent the annotations in the given time range.
 	 * @param time central point of time
 	 * @param range the range
 	 */
 	public void updateStatusAnnotationNear(double time, double range) {
 		if(this.selectedAnnotationChannel == null) {
 			return;
 		}
 		AnnotationList list = this.selectedAnnotationChannel.getAnnotation(time, range);
 		if(list.size() == 0) {
 			StatusBar.getInstance().updateInfoText(null, null);
 		} else  if(list.size() == 1) {
 			Event event = list.getEvent(0);
 			StatusBar.getInstance().updateInfoText(event.getType(), event.getComment());
 		} else {
 			// multiple annotations in range
 			String t = list.getType(0);
 			for(int i = 1; i < list.size(); i++) {
 				t += "," + list.getType(i);
 			}
 			StatusBar.getInstance().updateInfoText(t, "mehrere Annotationen");
 		}
 	}
 
 }
