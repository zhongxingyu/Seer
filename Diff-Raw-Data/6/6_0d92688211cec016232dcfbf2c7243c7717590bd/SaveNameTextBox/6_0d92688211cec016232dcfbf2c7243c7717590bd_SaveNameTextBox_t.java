 package nl.tue.fingerpaint.client.gui.textboxes;
 
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.gui.panels.SaveItemPopupPanel;
 
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.user.client.ui.TextBox;
 
 /**
  * TextBox that can be used to let the user input a name for an item to save.
  * 
  * @author Group Fingerpaint
  * @see SaveItemPopupPanel
  */
 public class SaveNameTextBox extends TextBox implements KeyPressHandler {
 
 	/**
 	 * Construct a new {@link TextBox} that can be used to input a name for an
 	 * item to save.
 	 */
 	public SaveNameTextBox() {
 		super();
 		setMaxLength(30);
 		addKeyPressHandler(this);
 		ensureDebugId("saveNameTextBox");
 	}
 
 	/**
 	 * Adds the key that was pressed to the textbox, but only if the key is one
 	 * of A-Za-z0-9 and it is not a space at the beginning of the text. If
 	 * backspace is pressed, the last character removed, and if enter was
 	 * pressed, the panel behaves as if the Save button was clicked.	 * 
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onKeyPress(KeyPressEvent event) {
 		String text = GuiState.saveNameTextBox.getText();
 		String inputCharacter = Character.toString(event.getCharCode());
 		int textlength = text.length();
 		if (inputCharacter.matches("[~`!@#$%^&*()+={}\\[\\]:;\"|\'\\\\<>?,./]")) {
 			cancelKey();
 		} else if (inputCharacter.matches("\\s") && textlength == 0) {
 			// Do not allow names to start with a space
 			cancelKey();
 		} else if (inputCharacter.matches("[A-Za-z0-9]")) {
 			textlength++;
			GuiState.saveItemPanelButton.setEnabled(textlength > 0);
 		} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_BACKSPACE) {
 			textlength--;
			GuiState.saveItemPanelButton.setEnabled(textlength > 0);
 		} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
 			GuiState.saveItemPanelButton.click();
 		}
		
 	}
 }
