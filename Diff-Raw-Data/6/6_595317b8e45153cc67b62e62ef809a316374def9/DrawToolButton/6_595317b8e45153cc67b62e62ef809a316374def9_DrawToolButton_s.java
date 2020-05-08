 package cpsc310.client;
 
import com.google.gwt.user.client.ui.CustomButton;
 import com.google.gwt.user.client.ui.Image;
 
public class DrawToolButton extends CustomButton {
 		
 	private BtnType buttonType;
 	private Boolean isDrawing;
 	
 	/**
 	 * Constructor for drawing buttons for Google Map
 	 */
 	public DrawToolButton() {
 		super();
 		isDrawing = false;
 	}
 	
 	/**
 	 * Enum values to restrict input for the setButtonType method.
 	 */
 	public enum BtnType {
 		DRAW, ERASE
 	}
 	
 	/**
 	 * Method to set the type of drawing button to display.
 	 * @pre type != null;
 	 * @post buttonType = type;
 	 * @param type - the type of drawing button to display; valid types are: "draw" and "erase"
 	 */
 	public void setButtonType(BtnType type) {
 		buttonType = type;
 	}
 	
 	/**
 	 * Method to set if the button is in drawing mode.
 	 * @pre (drawEnabled != null) && (buttonType.equals(BtnType.DRAW);
 	 * @post isDrawing = drawEnabled;
 	 * @param drawEnabled
 	 */
 	public void setDrawEnabled(Boolean drawEnabled) {
 		isDrawing = drawEnabled;
 	}
 	
 	/**
 	 * Method to return if the button has been pressed; i.e. user wants to draw on the Google Map
 	 * @pre true;
 	 * @post true;
 	 * @return if the button has been pressed for drawing mode.
 	 */
 	public boolean drawEnabled() {
 		return isDrawing;
 	}
 	
 	/**
 	 * Method to set the button as a draw button.
 	 */
 	public void setDrawImage() {
 		this.getUpFace().setImage(new Image("https://fbcdn-photos-a.akamaihd.net/photos-ak-snc1/v85006/101/257432264338889/app_1_257432264338889_9807.gif"));
 		this.getDownFace().setImage(new Image("http://www.ugrad.cs.ubc.ca/~y0c7/Water_lilies.jpg"));
 	}
 	
 	/**
 	 * Method to set the button as an erase button.
 	 */
 	public void setEraseImage() {
 		//this.getUpFace().setImage(image);
 	}
 	
 }
