 package com.orange.groupbuy.web.client.component;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.AnchorElement;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Widget;
 import com.orange.groupbuy.web.client.model.SearchResult;
 
 public class GroupBuyWidget extends Widget {
 
 	// private static final String TARGET_BLANK = "_blank";
 
 	private static GroupBuyWidgetUiBinder uiBinder = GWT
 			.create(GroupBuyWidgetUiBinder.class);
 
 	interface GroupBuyWidgetUiBinder extends
 			UiBinder<DivElement, GroupBuyWidget> {
 	}
 
 	@UiField
 	AnchorElement siteNameLabel;
 
 	@UiField
 	AnchorElement link;
 
 	@UiField
 	AnchorElement imageAnchor;
 
 	@UiField
 	ImageElement image;
 
 	@UiField
 	Element originalPriceLabel;
 
 	@UiField
 	SpanElement rebateLabel;
 
 	@UiField
 	Element boughtNumLabel;
 
 	@UiField
 	DivElement valueLabel;
 
 	@UiField
 	AnchorElement detailsButton;
 
 	public GroupBuyWidget() {
 		setElement(uiBinder.createAndBindUi(this));
 	}
 
 	public void updateModel(SearchResult result) {
 		if (result != null) {
 			siteNameLabel.setHref(result.getSiteUrl());
 			siteNameLabel.setInnerText(result.getSiteName());
 			//
 			link.setTitle(result.getDesctiption());
 			link.setHref(result.getProductUrl());
 			link.setInnerHTML(result.getDesctiption());
 			//
 			imageAnchor.setHref(result.getProductUrl());
 			imageAnchor.setTitle(result.getDesctiption());
 			image.setSrc(result.getImageUrl());
 			// image.setTitle(result.getDesctiption());
 
 			originalPriceLabel.setInnerText(formatMoney(result.getPrice()));
 			rebateLabel.setInnerText(formatRebate(result.getRebate()));
 			//
 			boughtNumLabel.setInnerText(formatNumber(result.getBought()));
 			valueLabel.setInnerText(formatNumber(result.getValue()));
 
 			detailsButton.setHref(result.getProductUrl());
 		}
 	}
 
 	private String formatNumber(Integer result) {
 		return String.valueOf(result);
 	}
 
 	private String formatNumber(Double result) {
 		return String.valueOf(result);
 	}
 
 	private String formatRebate(Double result) {
		// TODO:
		return (result < 1 ? String.valueOf(result * 10) : String
				.valueOf(result)) + "折";
 	}
 
 	private String formatMoney(Double result) {
 		return "￥" + String.valueOf(result);
 	}
 }
