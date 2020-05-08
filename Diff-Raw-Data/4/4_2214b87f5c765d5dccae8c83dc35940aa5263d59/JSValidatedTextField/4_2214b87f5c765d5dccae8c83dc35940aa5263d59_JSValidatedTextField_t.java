 package js.incomplete;
 
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JTextField;
 
 public class JSValidatedTextField extends JTextField implements FocusListener {
 
 	private static final int PRESENCE_CHECK = 0;
 	private static final int LENGTH_CHECK = 1;
 	private static final int RANGE_CHECK = 2;
 	private static final int FORMAT_CHECK = 3;
 	
 	private int type;
 	private int minLength;
 	private int maxLength;
 	private double minValue;
 	private double maxValue;
 	private String pattern;
 	private boolean auto;
 	private String error;
 	
 	JSValidatedTextField() {
 		addFocusListener(this);
 	}
 	
 	public static JSValidatedTextField createPresenceCheckField() {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = PRESENCE_CHECK;
 		return field;
 	}
 	
 	public static JSValidatedTextField createLengthCheckField(int min, int max) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = LENGTH_CHECK;
 		field.minLength = min;
 		field.maxLength = max;
 		return field;
 	}
 	
 	public static JSValidatedTextField createRangeCheckField(double min, double max) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = RANGE_CHECK;
 		field.minValue = min;
 		field.maxValue = max;
 		return field;
 	}
 	
 	public static JSValidatedTextField createFormatCheckField(String regex) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = FORMAT_CHECK;
 		field.pattern = regex;
 		return field;
 	}
 	
	public String getErrorMessage() {
		return error;
	}
	
 	public void setAutoValidationEnabled(boolean state) {
 		auto = state;
 	}
 	
 	public boolean isAutoValidationEnabled() {
 		return auto;
 	}
 
 	public void focusGained(FocusEvent e) {
 
 	}
 
 	public void focusLost(FocusEvent e) {
 
 	}
 	
 }
