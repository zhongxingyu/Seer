 /*******************************************************************************
  * Copyright (c) 2013 Pronoia Health LLC.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Pronoia Health LLC - initial API and implementation
  *******************************************************************************/
 package com.pronoiahealth.olhie.client.pages.bulletinboard.widgets;
 
 //import static com.watopi.chosen.client.Chosen.Chosen;
 //import static com.google.gwt.query.client.GQuery.$;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import com.github.gwtbootstrap.client.ui.Hero;
 import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.pronoiahealth.olhie.resources.MessageFormatResources;
 
 /**
  * HelpVideoWidget.java<br/>
  * Responsibilities:<br/>
  * 1.
  * 
  * @author John DeStefano
  * @version 1.0
  * @since Oct 31, 2013
  * 
  */
 public class HelpVideoWidget extends Composite {
 
 	@Inject
 	UiBinder<Widget, HelpVideoWidget> binder;
 
 	@UiField
 	public Hero hero;
 
 	@UiField
 	public SplitDropdownButton helpTopic;
 
 	@UiField
 	public HTML topicTitle;
 
 	@UiField
 	public Frame videoFrame;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public HelpVideoWidget() {
 	}
 
 	@PostConstruct
 	protected void postConstruct() {
 		initWidget(binder.createAndBindUi(this));
 
 		// Build Introduction
 		HelpVideoDropdownSubmenu introSM = new HelpVideoDropdownSubmenu(
 				"Introduction to Olhie",
 				HelpVideoConstants.INSTANCE.introductionUrlMap(),
 				HelpVideoConstants.INSTANCE.introductionDisplayList(),
 				topicTitle, videoFrame);
 		helpTopic.getMenuWiget().add(introSM);
 
 		// Build overview
 		HelpVideoDropdownSubmenu overviewSM = new HelpVideoDropdownSubmenu(
 				"Technical", HelpVideoConstants.INSTANCE.overviewUrlMap(),
 				HelpVideoConstants.INSTANCE.overviewDisplayList(), topicTitle,
 				videoFrame);
 		helpTopic.getMenuWiget().add(overviewSM);
 
 		// Build Help topic
 		HelpVideoDropdownSubmenu helpSM = new HelpVideoDropdownSubmenu(
 				"Help Topics", HelpVideoConstants.INSTANCE.helpUrlMap(),
 				HelpVideoConstants.INSTANCE.helpDisplayList(), topicTitle,
 				videoFrame);
 		helpTopic.getMenuWiget().add(helpSM);
 
 		// Set up display
 		hero.getElement().setAttribute("style",
 				"padding: 20px; height: 665px; width: 607px;");
		hero.setStyleName("draggableWidget ph-BulletinBoard-Widget-HelpVideo-Bkg", true);
 
 		// Topic title
 		String firstTitle = "";
 
 		// Always lead with help display
 		if (HelpVideoConstants.INSTANCE.helpDisplayList() != null
 				&& HelpVideoConstants.INSTANCE.helpDisplayList().length > 0) {
 			firstTitle = HelpVideoConstants.INSTANCE.helpDisplayList()[0];
 		}
 
 		topicTitle.setHTML(MessageFormatResources.INSTANCE
 				.setHelpTopicTitle(HelpVideoConstants.INSTANCE
 						.helpDisplayList()[0]));
 
 		// Video Frame
 		videoFrame.getElement().setAttribute("type", "text/html");
 		if (firstTitle != null && firstTitle.length() > 0) {
 			videoFrame.setUrl(HelpVideoConstants.INSTANCE.helpUrlMap().get(
 					firstTitle));
 		}
 	}
 }
