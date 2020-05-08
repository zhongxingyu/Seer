 package org.cruxframework.crux.showcase.client.controller;
 
 import org.cruxframework.crux.core.client.controller.Controller;
 import org.cruxframework.crux.core.client.controller.Create;
 import org.cruxframework.crux.core.client.controller.Expose;
 import org.cruxframework.crux.core.client.formatter.Formatter;
 import org.cruxframework.crux.core.client.formatter.InvalidFormatException;
 import org.cruxframework.crux.core.client.formatter.annotation.FormatterName;
 import org.cruxframework.crux.core.client.screen.Screen;
 import org.cruxframework.crux.core.client.screen.ScreenWrapper;
 import org.cruxframework.crux.widgets.client.maskedtextbox.MaskedTextBox;
 import org.cruxframework.crux.widgets.client.maskedtextbox.MaskedTextBoxBaseFormatter;
 
 import com.google.gwt.user.client.ui.Label;
 
 @Controller("maskedTextBoxController")
 public class MaskedTextBoxController {
 	
 	private String currentFormatterName = "phone";
 	
 	@Create
 	protected MaskedScreen screen;
 	
 	@Expose
 	public void changeFormat(){
 		
 		String newFormatterName = "phone".equals(currentFormatterName) ? "date" : "phone";
 		Formatter newFormatter = Screen.getFormatter(newFormatterName);
 		currentFormatterName = newFormatterName;
 		
 		MaskedTextBox maskedTextBox = screen.getMaskedTextBox();
 		maskedTextBox.setUnformattedValue(null);		
 		maskedTextBox.setFormatter(newFormatter);		
 		
 		swapLabel();
 	}
 
 	@FormatterName("phone")
 	public static class PhoneFormatter extends MaskedTextBoxBaseFormatter{
 		
 		public String getMask(){
 			return "(999)999-9999";
 		}
 
 		public String format(Object input){
 			if (input == null || !(input instanceof String) || ((String)input).length() != 10){
 				return "";
 			}
 			
 			String strInput = (String) input;
 			return "("+strInput.substring(0,3)+")"+strInput.substring(3,6)+"-"+strInput.substring(6);
 		}
 
 		public Object unformat(String input) throws InvalidFormatException{
 			if (input == null || !(input instanceof String) || ((String)input).length() != 13){
 				return "";
 			}
 			String inputStr = (String)input;
 			inputStr = inputStr.substring(1,4)+inputStr.substring(5,8)+inputStr.substring(9,13);
 			return inputStr;
 		}
 	}
 
 	private void swapLabel()
 	{
 		String label = currentFormatterName.substring(0, 1).toUpperCase() + currentFormatterName.substring(1);
 		screen.getMaskedLabel().setText(label + ":");
 	}
 	
 	public static interface MaskedScreen extends ScreenWrapper{
 		MaskedTextBox getMaskedTextBox();
 		Label getMaskedLabel();
 	}
 }
