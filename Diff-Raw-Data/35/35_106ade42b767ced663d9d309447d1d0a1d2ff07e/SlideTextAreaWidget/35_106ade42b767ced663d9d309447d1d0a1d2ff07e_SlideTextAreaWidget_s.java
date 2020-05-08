 package com.mamykin.psytest.client.model.elements;
 
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.mamykin.psytest.client.model.SlideElementWidget;
 
 public class SlideTextAreaWidget extends SlideElementWidget {
 
 	private final VerticalPanel panel = new VerticalPanel();
 	private final TextArea textArea = new TextArea();
 	private final SlideTextAreaElement element;
 
 	public SlideTextAreaWidget(SlideTextAreaElement element) {
 		super();
 		this.element = element;
 		textArea.setCharacterWidth(80);
 		textArea.setVisibleLines(10);
 		panel.add(textArea);
 		initWidget(panel);
 	}
 
 	@Override
 	public boolean isValid() {
		return (textArea.getText().trim().length() > 0);
 	}
 	
 	@Override
 	public void recordValues() {
 		element.setText(textArea.getText());
 	}
 }
