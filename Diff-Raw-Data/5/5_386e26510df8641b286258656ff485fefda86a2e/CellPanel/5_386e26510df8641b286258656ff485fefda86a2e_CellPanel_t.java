 package com.example.folio.client.cover.grid;
 
 import com.example.folio.client.Folio;
 import com.example.folio.client.place.ProjectPlace;
 import com.example.folio.client.util.Position;
 import com.example.folio.client.util.Rectangle;
 import com.example.folio.client.util.Utils;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.HasMouseOutHandlers;
 import com.google.gwt.event.dom.client.HasMouseOverHandlers;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 
 public class CellPanel extends AbsolutePanel 
 	implements HasMouseOverHandlers, HasMouseOutHandlers, HasClickHandlers {
 
	public static final int MOUSE_OVER_COMMENT_AREA_HEIGHT = 50;
	
 	private CoverBlock block;
 	private AbsolutePanel infoPanel;
 	private Image image;
 	private Label blockName;
 	private Label blockDesc;
 
 	public CellPanel(CoverBlock block) {
 		this.block = block;
 		setStyleName("block");
 		
 		image = new Image(block.getImageUrl());
 		
 		infoPanel = new AbsolutePanel();
 		infoPanel.setStyleName("block_info");
 		blockName = new Label();
 		blockName.setStyleName("block_info_title");
 		infoPanel.add(blockName);
 		infoPanel.setWidgetPosition(blockName, 10, 8);
 		blockDesc = new Label();
 		blockDesc.setStyleName("block_info_desc");
 		infoPanel.add(blockDesc);
 		infoPanel.setWidgetPosition(blockDesc, 10, 24);
 		
 		this.addMouseOverHandler(new MouseOverHandler() {			
 			@Override
 			public void onMouseOver(MouseOverEvent arg0) {
 				CellPanel.this.add(infoPanel);
 				CellPanel.this.setWidgetPosition(
 					infoPanel, 
 					0, 
					CellPanel.this.block.calcRectangle().height - MOUSE_OVER_COMMENT_AREA_HEIGHT
 				);
 			}
 		});
 		this.addMouseOutHandler(new MouseOutHandler() {			
 			@Override
 			public void onMouseOut(MouseOutEvent arg0) {
 				CellPanel.this.remove(infoPanel);			
 			}
 		});
 		this.addClickHandler(new ClickHandler() {			
 			@Override public void onClick(ClickEvent arg0) {
 //				String url = CellPanel.this.block.getImageUrl();
 				CellPanel.this.remove(infoPanel);
 				Folio.instance.injector.getPlaceController().goTo(new ProjectPlace());
 			}
 		});
 	}
 	
 	public void redraw() {
 //		if (image != null && this.getChildren().contains(image)) {
 //			remove(image);
 //		}
 		if (block.getImageInfo().scale) {
 			if (!this.getChildren().contains(image)) {
 				add(image);
 			}			
 			Rectangle blockRect = block.calcRectangle();			
 			Utils.scaleImage(this, image, blockRect.getDimension());
 			
 //			float blockRatio = (float)blockRect.width/(float)blockRect.height;
 //			int imgHeight = image.getHeight();
 //			int imgWidth = image.getWidth();
 //			float imgRatio = (float)imgWidth/(float)imgHeight;
 //			
 //			int resultWidth = 0;
 //			int resultHeight = 0;
 //			if (blockRatio > imgRatio) {
 //				resultWidth = blockRect.width;
 //				resultHeight = (int) ((float)imgHeight * ((float)resultWidth/(float)imgWidth));
 //			} else {
 //				resultHeight = blockRect.height;
 //				resultWidth = (int) ((float)imgWidth * ((float)resultHeight/(float)imgHeight));
 //			}
 //			image.setPixelSize(resultWidth, resultHeight);
 //			
 //			int xOffset = 0;
 //			int yOffset = 0;
 //			if (resultWidth > blockRect.width) {
 //				xOffset = (resultWidth - blockRect.width) / 2;
 //			}
 //			if (resultHeight > blockRect.height) {
 //				yOffset = (resultHeight - blockRect.height) / 2;
 //			}
 //			
 //			if (!this.getChildren().contains(image)) {
 //				add(image);
 //			}
 //			
 //			setWidgetPosition(image, -xOffset, -yOffset);
 		} else {
 			Position focusPoint = block.getImageInfo().focusPoint;
 			Rectangle blockRect = block.calcRectangle();
 			int imgHeight = image.getHeight();
 			int imgWidth = image.getWidth();
 			
 			int xOffset = 0;
 			int yOffset = 0;
 			xOffset = Math.max(focusPoint.getX() - blockRect.width/2, 0);
 			yOffset = Math.max(focusPoint.getY() - blockRect.height/2, 0);
 			xOffset = Math.min(xOffset, imgWidth - blockRect.width);
 			yOffset = Math.min(yOffset, imgHeight - blockRect.height);
 
 			if (!this.getChildren().contains(image)) {
 				add(image);
 			}
 			setWidgetPosition(image, -xOffset, -yOffset);
 		}
 
 		blockName.setText(block.getName());
 		blockDesc.setText(block.toString());
 	}
 
 	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
 		return addDomHandler(handler, MouseOverEvent.getType());
 	}
 
 	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
 		return addDomHandler(handler, MouseOutEvent.getType());
 	}
 
 	public HandlerRegistration addClickHandler(ClickHandler handler) {
 		return addDomHandler(handler, ClickEvent.getType());
 	}
 	
 	public void reconnectToBlock(CoverBlock block) {
 		this.block = block;
 	}
 
 }
