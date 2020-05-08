 /*
  * MetaPanelWidget.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.instance.client.article;
 
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 public class MetaPanelWidget extends Composite implements
 		MetaPanelPresenter.Display {
 	public static interface MetaPanelUiBinder extends
 			UiBinder<Widget, MetaPanelWidget> {
 	}
 
 	@UiField
	HasText hashLabel;
 
 	@UiField
 	HasText dateLabel;
 
 	@UiField
 	HasText searchDateLabel;
 	
 	@UiField
 	HasText parseDateLabel;
 
 	@UiField
 	HasWidgets tagsPanel;
 
 	@UiField
 	HasWidgets mappingInputPanel;
 
 	@Inject
 	public MetaPanelWidget(MetaPanelUiBinder uiBinder) {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
	public HasText getHashLabel() {
 		return hashLabel;
 	}
 
 	@Override
 	public HasText getDateLabel() {
 		return dateLabel;
 	}
 
 	@Override
 	public HasText getSearchDateLabel() {
 		return searchDateLabel;
 	}
 	
 	@Override
 	public HasText getParseDateLabel() {
 		return parseDateLabel;
 	}
 
 	@Override
 	public HasWidgets getTagsPanel() {
 		return tagsPanel;
 	}
 
 	@Override
 	public HasWidgets getMappingInputPanel() {
 		return mappingInputPanel;
 	}
 }
