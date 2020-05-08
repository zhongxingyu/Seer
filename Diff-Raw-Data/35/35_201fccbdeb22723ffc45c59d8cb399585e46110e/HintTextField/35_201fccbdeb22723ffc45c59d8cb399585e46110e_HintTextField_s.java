 package view;
 
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JTextField;
 
 /**
  * A class representing a textfield with a hint.
  */
 public class HintTextField extends JTextField implements FocusListener {
 
 	private static final long serialVersionUID = 1L;
 	private String hint;
 	private int col;
	
 	/**
 	 * creates a new hinted text field
	 * @param hint the hint to show
	 * @param col number of columns of the textfield
 	 */
	public HintTextField(String hint, int col){
		
 		super(hint, col);
		this.hint=hint;
		this.col=col;
 		super.addFocusListener(this);
 	}
 
 	/**
 	 * deletes the hint when focus is gained.
 	 */
    @Override
    public void focusGained(FocusEvent e) {
    	super.setEnabled(true);
        if(this.getText().isEmpty()) {
            super.setText("");
        }
    }
     /**
      * Sets the hint if textfield is empty
      */
     @Override
     public void focusLost(FocusEvent e) {
         if(this.getText().isEmpty()) {
             super.setText(hint);
         }
     }
 
     /**
      * Controlls that the string is not the hint. 
      */
     @Override
     public String getText() {
         String typed = super.getText();
         return typed.equals(hint) ? "" : typed;
     }
     
 }
 
