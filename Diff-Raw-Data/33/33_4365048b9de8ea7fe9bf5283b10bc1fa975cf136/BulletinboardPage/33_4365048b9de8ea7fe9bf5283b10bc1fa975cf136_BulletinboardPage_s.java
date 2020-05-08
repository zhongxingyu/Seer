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
 package com.pronoiahealth.olhie.client.pages.bulletinboard;
 
 import static com.google.gwt.query.client.GQuery.$;
 import gwtquery.plugins.draggable.client.DraggableOptions;
 import gwtquery.plugins.draggable.client.DraggableOptions.HelperType;
 import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
 import gwtquery.plugins.draggable.client.events.BeforeDragStartEvent;
 import gwtquery.plugins.draggable.client.events.BeforeDragStartEvent.BeforeDragStartEventHandler;
 import gwtquery.plugins.draggable.client.events.DragStartEvent;
 import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
 import gwtquery.plugins.draggable.client.events.DragStopEvent;
 import gwtquery.plugins.draggable.client.events.DragStopEvent.DragStopEventHandler;
 import gwtquery.plugins.draggable.client.gwt.DraggableWidget;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.jboss.errai.ui.nav.client.local.DefaultPage;
 import org.jboss.errai.ui.nav.client.local.Page;
 import org.jboss.errai.ui.nav.client.local.PageShowing;
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 
 import com.google.gwt.dom.client.Style.Cursor;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.user.client.ui.Widget;
 import com.pronoiahealth.olhie.client.pages.AbstractPage;
 import com.pronoiahealth.olhie.client.pages.AppSelectors;
 import com.pronoiahealth.olhie.client.pages.bulletinboard.widgets.CarouselSliderWidget;
 import com.pronoiahealth.olhie.client.pages.bulletinboard.widgets.CurrentStatusWidget;
 import com.pronoiahealth.olhie.client.pages.bulletinboard.widgets.HelpVideoWidget;
 import com.pronoiahealth.olhie.client.widgets.dnd.DroppablePanel;
 
 /**
  * BulletinboardPage.java<br/>
  * Responsibilities:<br/>
  * 1. Serves as default page in Errai Navigation system<br/>
  * 
  * @author John DeStefano
  * @version 1.0
  * @since May 26, 2013
  * 
  */
 @Templated("#root")
 @Page(role = { DefaultPage.class })
 public class BulletinboardPage extends AbstractPage {
 
 	@Inject
 	@DataField("col1")
 	private DroppablePanel col1;
 
 	@Inject
 	@DataField("col2")
 	private DroppablePanel col2;
 
 	@Inject
 	private CurrentStatusWidget statusWidget;
 
 	@Inject
 	private CarouselSliderWidget carouselWidget;
 
 	@Inject
 	private HelpVideoWidget helpVideoWidget;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public BulletinboardPage() {
 	}
 
 	/**
 	 * Inject the custom resources
 	 */
 	@PostConstruct
 	private void postConstruct() {
 		createAddDraggableWidgetToDroppablePanel(col1, statusWidget,
 				".draggableWidget");
		createAddDraggableWidgetToDroppablePanel(col2, helpVideoWidget,
 				".draggableWidget");
		createAddDraggableWidgetToDroppablePanel(col2, carouselWidget,
 				".draggableWidget");
 	}
 
 	private void createAddDraggableWidgetToDroppablePanel(DroppablePanel panel,
 			Widget w, String acceptClass) {
 
 		// Set a filter on what types of widgets this panel will accept
 		panel.setAccept(acceptClass);
 
 		// Create the draggable widget
 		DraggableWidget<Widget> dw = new DraggableWidget<Widget>(w);
 
 		// Before drag start
 		dw.addBeforeDragHandler(new BeforeDragStartEventHandler() {
 			@Override
 			public void onBeforeDragStart(BeforeDragStartEvent event) {
 				$(event.getDraggable()).css("position", "absolute");
 			}
 		});
 
 		// Drag start handler to dynamically adjust the size of the
 		// droppable panel widgets on the page. Without this you can't
 		// drag from column to column
 		dw.addDragStartHandler(new DragStartEventHandler() {
 			@Override
 			public void onDragStart(DragStartEvent event) {
 				int content = AppSelectors.INSTANCE.getCenterBackground()
 						.height();
 				GQuery droppables = AppSelectors.INSTANCE
 						.getAllDroppableWidgets();
 				droppables.height(content);
 			}
 		});
 
 		// On drag stop
 		dw.addDragStopHandler(new DragStopEventHandler() {
 			@Override
 			public void onDragStop(DragStopEvent event) {
 				$(event.getDraggable()).css("position", "relative")
 						.css("top", null).css("left", null);
 			}
 		});
 
 		// Can't create and re-use
 		// Must have unique set for application to each widget
 		DraggableOptions dragOptions = new DraggableOptions();
 		dragOptions.setOpacity(new Float(0.8));
 		dragOptions.setZIndex(2000);
 		dragOptions.setCursor(Cursor.MOVE);
 		dragOptions.setRevert(RevertOption.NEVER);
 		dragOptions.setRevertDuration(500);
 		dragOptions.setHelper(HelperType.CLONE);
 
 		// Set drag options
 		dw.setDraggableOptions(dragOptions);
 
 		// Add to column
 		panel.addWidget(dw);
 	}
 
 	@PageShowing
 	protected void onPageShowing() {
 		// Set the background which will make the containing div overflow
 		setPageBackgroundClass("ph-BulletinBoard-Background");
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
 
 		// Bind to the root div of this page
 		addFullPageScrolling();
 	}
 }
