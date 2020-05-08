 package com.mamykin.psytest.client.model.elements;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.mamykin.psytest.client.model.SlideElementWidget;
 
 public class SlideSingleChoiceWidget extends SlideElementWidget {
 
 	private final SlideSingleChoiceElement element;
 	private final VerticalPanel panel = new VerticalPanel();
 	private final List<RadioButton> radioButtons;
 
 	public SlideSingleChoiceWidget(SlideSingleChoiceElement element) {
 		super();
 		this.element = element;
 		this.radioButtons = new ArrayList<RadioButton>();
 		panel.add(new Label(element.getQuestion()));
 		for (SlideChoiceAnswer answer : element.getAnswers()) {
 			HorizontalPanel answerPanel = new HorizontalPanel();
 			answerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 			RadioButton radioButton = new RadioButton(element.getId(), answer.getText());
 			
 			radioButtons.add(radioButton);
 			if(answer.hasImage()){
 				Image image = new Image();
 				image.setUrl(answer.getImageUrl());
 				answerPanel.add(image);
 			}
 			answerPanel.add(radioButton);
 			panel.add(answerPanel);
 		}
 		initWidget(panel);
 	}
 
 	@Override
 	public boolean isValid() {
		return getCurrentSelectionIndex() != -1;
 	}
 
 	@Override
 	public void recordValues() {
 		element.setUserSelectedAnswerIndex(getCurrentSelectionIndex());
 	}
 
 	private int getCurrentSelectionIndex() {
 		for (int i = 0; i < radioButtons.size(); i++) {
 			RadioButton rb = radioButtons.get(i);
 			if (rb.getValue() == true) {
 				// something is selected
 				return i;
 			}
 		}
 		// nothing is selected
 		return -1;
 	}
 
 }
