 package com.mamykin.psytest.client.model.elements;
 
import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.mamykin.psytest.client.model.SlideElementWidget;
 
 public class SlideTextAreaWidget extends SlideElementWidget {
 
 	private final VerticalPanel panel = new VerticalPanel();
	private final Label errorMessage = new Label("Please enter your response in the text area.");
 	private final TextArea textArea = new TextArea();
 	private final SlideTextAreaElement element;
 
 	public SlideTextAreaWidget(SlideTextAreaElement element) {
 		super();
 		this.element = element;
		errorMessage.setVisible(false);
		errorMessage.setStyleName("error");
		panel.add(errorMessage);
 		textArea.setCharacterWidth(80);
 		textArea.setVisibleLines(10);
 		panel.add(textArea);
 		initWidget(panel);
 	}
 
 	@Override
 	public boolean isValid() {
		if (textArea.getText().trim().length() > 0){
			errorMessage.setVisible(false);
			return true;
		}else{
			errorMessage.setVisible(true);
			return false;
		}
 	}
 	
 	@Override
 	public void recordValues() {
 		element.setText(textArea.getText());
 	}
 }
