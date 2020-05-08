 package common.color;
 
 /**
  * Colors in Parcour.
  * @author HM42
  *
  */
 public enum Color {
 	
 	/**
 	 * Undefiniert. Farbe konnte nicht bestimmt werden.
 	 */
 	UNDEFINED(-1,-1,-1),
 	/**
 	 * Abgrund.
 	 */
 	ABYSS(27,28,29),
 	/**
 	 * Plane.
 	 */
 	BLACK(20,24,32),
 	/**
 	 * Linie.
 	 */
 	SILVER(45,46,47),
 	/**
	 * Bruecke (dunkel).
 	 */
	BROWN_DARK(36,37,38),
	/**
	 * Bruecke (hell).
	 */
	BROWN_LIGHT(35,36,37), 
 	/**
 	 * Sumpf.
 	 */
 	BEIGE(19,20,21),
 	
 	//FARBFELD
 	/**
 	 * Gruen auf Farbfeld.
 	 */
 	GREEN(100,100,100), 
 	/**
 	 * Geld auf Farbfeld.
 	 */
 	YELLOW(100,100,100),
 	/**
 	 * Rot auf Farbfeld.
 	 */
 	RED(100,100,100);
 	
 	/**
 	 * Minimum gathered.
 	 */
 	private int min;
 	/**
 	 * Average calculated.
 	 */
 	private int avg;
 	/**
 	 * Maximum gathered.
 	 */
 	private int max;
 	
 	/**
 	 * 
 	 * @param min Minimum gathered.
 	 * @param avg Average calculated.
 	 * @param max Maximum gathered.
 	 */
 	Color(int min,int avg, int max) {
 	        this.min = min;
 	        this.avg = avg;
 	        this.max = max;
 	}
 	
 	/**
 	 * 
 	 * @return Minimum fix. 
 	 */
 	public final int min() { return this.min; }
 	
 	/**
 	 * 
 	 * @return Average fix.
 	 */
 	public final int avg() { return this.avg; }
 	/**
 	 * 
 	 * @return Maximum fix.
 	 */
 	public final int max() { return this.max; }
 	
 	/**
 	 * 
 	 * @param value to check
 	 * @return true if value between min and max.
 	 */
 	public final boolean isComprised(int value){
 		return ((this.min <= value) &&(this.max >= value));
 	}
 	
 }
