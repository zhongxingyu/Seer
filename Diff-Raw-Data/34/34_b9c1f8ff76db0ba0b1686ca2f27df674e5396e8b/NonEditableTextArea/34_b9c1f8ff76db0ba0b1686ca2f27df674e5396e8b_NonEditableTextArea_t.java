 package gui;
 
 import java.awt.Dimension;
 
 /**
  * Store command history.
  */
 public class NonEditableTextArea extends SchemeTextArea {
 	private static final long serialVersionUID = -1184733261901876758L;
 
 	/**
 	 * Create the history text area. 
 	 */
 	public NonEditableTextArea(MainFrame main) {
 		setPreferredSize(new Dimension(100, getHeight() / 2 - 100));
         code.setEditable(false);
 	}
	
    /**
     * Append text to the end of the code area.
     *
     * @param text Text to append.
     */
    public void append(String text) {
    	super.append(text);
    	goToEnd();
    }
 }
