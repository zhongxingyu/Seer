 /**
  * as this class uses the scale-method which all classes that implement Pict
  * have definied, the generic type of the class must extend Pict
  * 
  * Scaled extends Repeated as it has the addtional requirement that it's type
  * must have a .scale()-method and the global scale is always 1.0
  * 
  * 
  * ASSERTIONS (Zusicherungen): 
  * - same as repeated 
  * - type-parameter must implement pict
  * 
  * @author OOP Gruppe 187
  * 
  */
 
 public class Scaled<P extends Pict> extends Repeated<P> {
 
 	/**
 	 * constructor which takes an array of pictograms; only used to make
 	 * debugging easier
 	 * 
 	 * @param data
 	 *            array with all the values
 	 */
 	public Scaled(P data[][]) {
 		super(data);
 	}
 
 	/**
 	 * scales the size of each box by calling their scale-method
 	 * 
 	 * @param factor
 	 */
 	public void scale(double factor) {
 		assert (0.1 <= factor && factor <= 10.0) : "invalid factor";
 
 		if (factor < 0.1 || factor > 10.0) {
 			throw new IllegalArgumentException("illegal factor");
 		}
 
 		Object[][] data = getData();
 
 		// scale objects
 		for (int i = 0; i < data.length; i++) {
 			for (int j = 0; j < data[0].length; j++) {
 				((Pict) data[i][j]).scale(factor);
 			}
 		}
 	}
 
 	/**
 	 * returns a string-list of all boxes
 	 * 
 	 * @return string-representation
 	 */
 	public String toString() {
 		StringBuilder ret = new StringBuilder();
 
 		Object[][] data = getData();
 
 		int maxWidth = PictHelper.getMaxWidth(data);
 		int maxHeight = PictHelper.getMaxHeight(data);
 
 		String currentLine;
 		int currentWidth;
 
 		for (int i = 0; i < data.length; i++) {
 			for (int k = 0; k < maxHeight; k++) {
 				for (int j = 0; j < data[0].length; j++) {
 					currentLine = PictHelper.getLine(data[i][j].toString(), k);
 					currentWidth = PictHelper.getWidth(currentLine);
 					ret.append(currentLine);
 					for (int l = currentWidth; l < maxWidth - 1; l++) {
 						ret.append(" ");
 					}
 				}
				if(k != maxHeight - 1) {
 					ret.append("\n");
 				}
 			}
 
 		}
 
 		return ret.toString();
 	}
 
 }
