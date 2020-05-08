 package CalculatorD;
 
 import javax.swing.JTextField;
 
 /**
  * Represents a set of options for displaying the Calculator GUI.
  * @author Dustin Leavins
  */
 public class GUIOptions implements Cloneable{
 
     /**
      * Default font size of the display.
      */
     public static final int DEFAULT_DISPLAY_FONT_SIZE = 40;
 
 
     /**
      * Default font size of every button.
      */
     public static final int DEFAULT_BUTTON_FONT_SIZE = 24;
 
     /**
      * Minimum font size for display and buttons.
      */
     public static final int MIN_FONT_SIZE = 8;
 
     /**
      * Maximum font size for display and Buttons;
      */
     public static final int MAX_FONT_SIZE = 72;
 
 
     /**
      * Indicates that the decimal key on the keypad should be used for
      * the delete button on the calculator.
      */
     public static final boolean USE_DECIMAL_BUTTON_FOR_DELETE = true;
 
     /**
      * Indicates that the decimal key on the keypad should be used for
      * the decimal button on the calculator; this is the
      * behavior of the decimal key.
      */
     public static final boolean USE_DECIMAL_BUTTON_FOR_DECIMAL = false;
 
 
     /**
      * Default horizontal alignment of display;
      * Equals CENTER_HORIZONTAL_ALIGNMENT.
      */
     public static final int DEFAULT_HORIZONTAL_ALIGNMENT = JTextField.CENTER;
 
     /**
      * Equals its JTextField equivalent:  LEFT.
      */
     public static final int LEFT_HORIZONTAL_ALIGNMENT = JTextField.LEFT;
 
     /**
      * Equals its JTextField equivalent:  CENTER.
      */
     public static final int CENTER_HORIZONTAL_ALIGNMENT = JTextField.CENTER;
 
     /**
      * Equals its JTextField equivalent:  RIGHT.
      */
     public static final int RIGHT_HORIZONTAL_ALIGNMENT = JTextField.RIGHT;
 
     private int displayFontSize;
     private boolean useDecimalButtonForDelete;
     private int horizontalAlignment;
     private int buttonFontSize;
 
     /**
      * Constructor.
      * @param displayFontSize size of the font used to display
      * the result of the current calculation
      * @param buttonFontSize size of the font used by buttons
      * @param horizontalAlignment alignment of the calculation display;
      * use the constants provided in <code>GUIOptions</code>
      * @param useDecimalButtonForDelete should the decimal key on the
      * numpad be used as the delete key in the calculator?
      */
     public GUIOptions(int displayFontSize, 
             int buttonFontSize,
             int horizontalAlignment, 
             boolean useDecimalButtonForDelete) {
 
         // Checking & setting displayFontSize
         if (displayFontSize < MIN_FONT_SIZE) {
             this.displayFontSize = MIN_FONT_SIZE;
         }
         else if (displayFontSize > MAX_FONT_SIZE) {
             this.displayFontSize = MAX_FONT_SIZE;
         }
         else {
             this.displayFontSize = displayFontSize;
         }
 
         // Checking & setting buttonFontSize
         if (buttonFontSize < MIN_FONT_SIZE) {
             this.buttonFontSize = MIN_FONT_SIZE;
         }
         else if (buttonFontSize > MAX_FONT_SIZE) {
             this.buttonFontSize = MAX_FONT_SIZE;
         }
         else {
             this.buttonFontSize = buttonFontSize;
         }
 
         // Checking & setting horizontalAlignment
         if (horizontalAlignment == LEFT_HORIZONTAL_ALIGNMENT) {
            horizontalAlignment = LEFT_HORIZONTAL_ALIGNMENT;
         }
         else if (horizontalAlignment == RIGHT_HORIZONTAL_ALIGNMENT) {
            horizontalAlignment = RIGHT_HORIZONTAL_ALIGNMENT;
         }
         else {
            horizontalAlignment = CENTER_HORIZONTAL_ALIGNMENT;
         }
 
         // Setting useDecimalButtonForDelete
         this.useDecimalButtonForDelete = useDecimalButtonForDelete;
     }
 
     /**
      * Font size of display, as dictated by <code>this</code>.
      * @return font size of display
      */
     public int displayFontSize() {
         return displayFontSize; 
     }
 
     /**
      * Decimal key setting, as dictated by <code>this</code>.
      * @return <code>true</code> if the decimal key will be used for
      * delete, <code>false</code> if it will be used as decimal
      */
     public boolean useDecimalButtonForDelete() {
         return useDecimalButtonForDelete;
     }
 
     /**
      * Font size of buttons, as dictated by <code>this</code>.
      * @return font size of buttons
      */
     public int buttonFontSize() {
         return buttonFontSize;
     }
 
     /**
      * Returns the horizontal alignment of the display,  as dictated 
      * by <code>this</code>.
      * @return <code>LEFT_HORIZONTAL_ALIGNMENT</code>, 
      * <code>RIGHT_HORIZONTAL_ALIGNMENT</code>,
      * or <code>CENTER_HORIZONTAL_ALIGNMENT </code> depending on alignment
      */
     public int horizontalAlignment() {
         return horizontalAlignment;
     }
 
     /**
      * Returns a <code>GUIOptions</code> object loaded with default
      * options.
      * <p>Default options:</p>
      * <table>
      *     <tr>
      *         <td>Display Font Size</td>
      *         <td>40</td>
      *     </tr>
      *     <tr>
      *         <td>Button Font Size</td>
      *         <td>24</td>
      *     </tr>
      *     <tr>
      *         <td>Horizontal Alignment</td>
      *         <td>Center</td>
      *     </tr>
      *     <tr>
      *         <td>Use Decimal Button For</td>
      *         <td>Decimal</td>
      *     </tr>
 
 
      * </table>
      * @return default <code>GUIOptions</code>
      */
     public static GUIOptions defaultOptions() {
         return new GUIOptions(DEFAULT_DISPLAY_FONT_SIZE,
             DEFAULT_BUTTON_FONT_SIZE,
             DEFAULT_HORIZONTAL_ALIGNMENT,
             USE_DECIMAL_BUTTON_FOR_DECIMAL);
     }
 
     /**
      * Overrides <code>Object</code>'s implementation of clone.
      * @return clone object
      */
     public Object clone(){
         GUIOptions cloneOpt = new GUIOptions(this.displayFontSize,
             this.buttonFontSize,
             this.horizontalAlignment,
             this.useDecimalButtonForDelete);
 
         return (Object) cloneOpt;
     }
 
 }
