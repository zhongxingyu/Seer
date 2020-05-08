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
 package com.pronoiahealth.olhie.client.pages.tv;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.Dependent;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.jboss.errai.ui.nav.client.local.Page;
 import org.jboss.errai.ui.nav.client.local.PageShowing;
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 
 import com.github.gwtbootstrap.client.ui.DropdownSubmenu;
 import com.github.gwtbootstrap.client.ui.NavLink;
 import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
 import com.github.gwtbootstrap.client.ui.base.IconAnchor;
 import com.github.gwtbootstrap.client.ui.constants.IconType;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Composite;
 import com.pronoiahealth.olhie.client.navigation.AnonymousRole;
 import com.pronoiahealth.olhie.client.pages.AbstractPage;
 import com.pronoiahealth.olhie.client.shared.events.tv.TVChannelProgrammingRequestEvent;
 import com.pronoiahealth.olhie.client.shared.events.tv.TVChannelProgrammingResponseEvent;
 import com.pronoiahealth.olhie.client.shared.vo.ChannelProgram;
 import com.pronoiahealth.olhie.client.shared.vo.ChannelProgramList;
 import com.pronoiahealth.olhie.client.utils.Utils;
 
 /**
  * TVPage.java<br/>
  * Responsibilities:<br/>
  * 1.
  * 
  * @author John DeStefano
  * @version 1.0
  * @since Dec 23, 2013
  * 
  */
 @Dependent
 @Templated("#root")
 @Page(role = { AnonymousRole.class })
 public class TVPage extends AbstractPage {
 
 	@Inject
 	private TVPromotion tvPromotion;
 
 	@Inject
 	private TVVideo tvVideo;
 
 	@Inject
 	@DataField
 	private SplitDropdownButton splitButton;
 
 	@DataField
 	private Element tvVideoContainer = DOM.createDiv();
 
 	@Inject
 	private Event<TVChannelProgrammingRequestEvent> tVChannelProgrammingRequestEvent;
 
 	private ClickHandler itemSelected;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public TVPage() {
 	}
 
 	@PostConstruct
 	protected void postConstruct() {
 		// Configure the splitbutton
 		splitButton.setIcon(IconType.MAGIC);
 		splitButton.setText("Pick a Channel");
		splitButton.setDropup(false);
 
 		// Construct call back
 		itemSelected = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				// Get what was clicked and request the media be streamed back
 				IconAnchor a = (IconAnchor) event.getSource();
 				String programRef = a.getElement().getAttribute("programRef");
 				
 				// Set video
 				tvVideo.setVideoRef(Utils
 						.buildRestServiceForTVDownload(programRef));
 				
 				// Make sure video is visible
 				setContainerContents(tvVideo);
 				tvVideo.loadMedia();
 			}
 		};
 
 		// Fire event to load channel guide
 		tVChannelProgrammingRequestEvent
 				.fire(new TVChannelProgrammingRequestEvent());
 	}
 
 	@PageShowing
 	protected void onPageShowing() {
 		setContainerContents(tvPromotion);
 	}
 
 	/**
 	 * Set-up<br/>
 	 * 1. Screen back ground<br/>
 	 * 2. Scroll to top <br/>
 	 * 
 	 * @see com.google.gwt.user.client.ui.Widget#onLoad()
 	 */
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		
 		// Set the background
 		setPageBackgroundClass("ph-TVPage-TV-Bkgd");
 
 		// Bind to the root div of this page
 		addFullPageScrolling();
 	}
 
 	private void setContainerContents(Composite containerWidget) {
 		// Clear the container
 		if (tvVideoContainer.getChildCount() > 0) {
 			tvVideoContainer.removeChild(tvVideoContainer.getChild(0));
 		}
 
 		// Place the promotion in the container
 		tvVideoContainer.appendChild(containerWidget.getElement());
 	}
 
 	/**
 	 * Populate the button list
 	 * 
 	 * @param tVChannelProgrammingResponseEvent
 	 */
 	protected void observesTVChannelProgrammingResponseEvent(
 			@Observes TVChannelProgrammingResponseEvent tVChannelProgrammingResponseEvent) {
 		List<ChannelProgramList> lst = tVChannelProgrammingResponseEvent
 				.getChannelGuide();
 		if (lst != null && lst.size() > 0) {
 			for (ChannelProgramList cpLst : lst) {
 				// Create the channel menu
 				String channelName = cpLst.getChannelName();
 				DropdownSubmenu sm = new DropdownSubmenu(channelName);
 
 				// Add the programming
 				List<ChannelProgram> cpl = cpLst.getProgramList();
 				if (cpl != null && cpl.size() > 0) {
 					for (ChannelProgram cp : cpl) {
 						NavLink link = new NavLink(cp.getProgramName());
 						link.getAnchor().getElement()
 								.setAttribute("programRef", cp.getProgramKey());
 						link.addClickHandler(itemSelected);
 						sm.getMenuWiget().add(link);
 					}
 				}
 				splitButton.getMenuWiget().add(sm);
 			}
 		}
 	}
 
 }
