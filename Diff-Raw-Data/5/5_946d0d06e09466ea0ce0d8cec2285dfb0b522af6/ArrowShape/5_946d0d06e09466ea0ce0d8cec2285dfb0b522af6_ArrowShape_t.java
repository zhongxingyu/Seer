 package cytoscape.visual;
 
 import giny.view.EdgeView;
 
 /**
  * Defines arrow shapes.<br>
  * This replaces constants defined in Arrow.java.
  * 
  * @since Cytoscape 2.5
  * @author kono
  * 
  */
 public enum ArrowShape {
 	NONE("NONE", EdgeView.NO_END), 
 	DIAMOND("COLOR_DIAMOND",EdgeView.EDGE_COLOR_DIAMOND), 
 	DELTA("COLOR_DELTA", EdgeView.EDGE_COLOR_DELTA), 
 	ARROW("COLOR_ARROW", EdgeView.EDGE_COLOR_ARROW), 
	T("COLOR_T", EdgeView.EDGE_COLOR_T),
 	CIRCLE("COLOR_CIRCLE", EdgeView.EDGE_COLOR_CIRCLE), 
 	
 	// Not yet implemented
	REVERSE_ARROW("REVERSE_ARROW", -1);
 	
 	private String shapeName;
 	private int ginyType;
 
 	private ArrowShape(String shapeName, int ginyType) {
 		this.shapeName = shapeName;
 		this.ginyType = ginyType;
 	}
 
 	/**
 	 * Returns arrow type in GINY.
 	 * 
 	 * @return
 	 */
 	public int getGinyArrow() {
 		return ginyType;
 	}
 	
 	/**
 	 * Returns name of arrow shape.
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return shapeName;
 	}
 	
 	/**
 	 * 
 	 * @param text
 	 * @return
 	 */
 	public static ArrowShape parseArrowText(String text) {
 		return valueOf(text);
 	}
 }
