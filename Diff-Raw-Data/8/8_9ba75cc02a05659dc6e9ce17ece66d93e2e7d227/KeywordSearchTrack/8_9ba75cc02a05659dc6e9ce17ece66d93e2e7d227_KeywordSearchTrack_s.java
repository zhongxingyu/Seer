 /*--------------------------------------------------------------------------
  *  Copyright 2007 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // UTGBMedaka Project
 //
 // KeywordSearchTrack.java
 // Since: Aug 6, 2007
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.utgenome.gwt.utgb.client.GenomeBrowser;
 import org.utgenome.gwt.utgb.client.bio.KeywordSearchResult;
 import org.utgenome.gwt.utgb.client.bio.KeywordSearchResult.Entry;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyWriter;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.ui.FormLabel;
 import org.utgenome.gwt.utgb.client.util.JSONUtil;
 import org.utgenome.gwt.utgb.client.util.Properties;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Keyword Search using utgb-keyword service
  * 
  * @author leo
  * 
  */
 public class KeywordSearchTrack extends TrackBase {
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new KeywordSearchTrack();
 			}
 		};
 	}
 
 	private DockPanel basePanel = new DockPanel();
 	private HorizontalPanel layoutPanel = new HorizontalPanel();
 	private SearchInput keywordPanel;
 	private VerticalPanel searchResultPanel = new VerticalPanel();
 	private Pager pager = new Pager();
 	private PopupPanel keywordHelpPopup = new PopupPanel(true);
 	private ArrayList<String> keywordExampleList = new ArrayList<String>();
 	final HTML keywordHelp = new HTML("");
 	private String speciesScope = "any";
 
 	class SearchInput extends Composite {
 		private Label _label;
 		private Widget _form;
 		private HorizontalPanel _layoutPanel = new HorizontalPanel();
 
 		public SearchInput(String label, Widget form) {
 			_label = new Label(label);
 			_label.setStyleName("search-label");
 			_form = form;
 			_form.setStyleName("search-field");
 			_layoutPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 			_layoutPanel.add(_label);
 			_layoutPanel.add(_form);
 
 			initWidget(_layoutPanel);
 		}
 
 		public String getInput() {
 			return ((TextBox) _form).getText();
 		}
 
 	}
 
 	class Pager extends Composite {
 		int page = 0;
 		int maxPage = 0;
 		HorizontalPanel panel = new HorizontalPanel();
 		String keyword = "";
 
 		public Pager() {
 			panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 			panel.setStyleName("pager");
 			initWidget(panel);
 		}
 
 		public void update(String keyword, int page, int maxPage) {
 			this.keyword = keyword;
 			this.page = page;
 			this.maxPage = maxPage;
 			panel.clear();
 			panel.add(new FormLabel("page: "));
 			int minPagerPage = (page / 10) * 10 >= 0 ? ((page / 10) * 10) : page;
 			if (minPagerPage <= 0)
 				minPagerPage = 1;
 			int maxPagerPage = (minPagerPage + 10) > maxPage ? maxPage + 1 : minPagerPage + 10;
 			if (minPagerPage > 1) {
 				panel.add(getPagerLink(minPagerPage - 1, "<<"));
 				panel.add(getPagerLink(page - 1, "prev"));
 			}
 			for (int i = minPagerPage; i < maxPagerPage; i++) {
 				final int pageNum = i;
 				final String currentKeyword = keyword;
 				String pageNumStr = Integer.toString(pageNum);
 				if (i == page) {
 					Label label = new Label(pageNumStr);
 					Style.set(label, "color", "#FF9999");
 					Style.margin(label, Style.LEFT | Style.RIGHT, 2);
 					Style.bold(label);
 					//label.setStyleName("current");
 					panel.add(label);
 				}
 				else {
 					panel.add(getPagerLink(pageNum, pageNumStr));
 				}
 			}
 			if (maxPage > 1) {
 				if (page < maxPage) {
 					panel.add(getPagerLink(page + 1, "next"));
 				}
 				if (maxPagerPage < maxPage)
 					panel.add(getPagerLink(maxPagerPage, ">>"));
 			}
 		}
 
 		private Anchor getPagerLink(final int pageNum, String label) {
 			Anchor link = new Anchor(label);
 			Style.margin(link, Style.LEFT | Style.RIGHT, 2);
 			link.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent e) {
 					performSearch(keyword, pageNum, 10);
 				}
 			});
 			return link;
 		}
 	}
 
 	private class LocationMover implements ClickHandler {
 		private final Entry e;
 
 		public LocationMover(final Entry e) {
 			this.e = e;
 		}
 
 		public void onClick(ClickEvent event) {
 
 			TrackGroupPropertyWriter writer = getTrackGroup().getPropertyWriter();
 			HashMap<String, String> property = new HashMap<String, String>();
 			property.put(UTGBProperty.REVISION, e.ref);
 			property.put(UTGBProperty.TARGET, e.chr);
 
 			TrackWindow win = getTrackGroup().getTrackWindow();
 
 			int width = win.getEndOnGenome() - win.getStartOnGenome();
 			int left = e.start;
 			int right = e.end;
 			if (width < 0) {
 				width = -width;
 			}
 
 			// locate the new window so that the target region will be at 20% from the left side 
 			int newLeft = left - (int) (width * 0.3);
 			int newRight = right + (int) (width * 0.3);
 
 			try {
 				writer.setProperyChangeNotifaction(false);
 				if (!win.isReverseStrand())
 					writer.setTrackWindow(newLeft, newRight);
 				else
 					writer.setTrackWindow(newRight, newLeft);
 			}
 			finally {
 				writer.setProperyChangeNotifaction(true);
 			}
 			writer.setProperty(property);
 		}
 	}
 
 	private void performSearch(final String keyword, int numPage, int entriesPerPage) {
 		getFrame().setNowLoading();
 		String species = null;
 		if (!speciesScope.equals("any"))
 			species = getTrackGroup().getPropertyReader().getProperty(UTGBProperty.SPECIES, "");
 		String revision = getTrackGroup().getPropertyReader().getProperty(UTGBProperty.REVISION, "");
 		GenomeBrowser.getService().keywordSearch(species, revision, keyword, entriesPerPage, numPage, new AsyncCallback<KeywordSearchResult>() {
 			public void onFailure(Throwable caught) {
 				getFrame().loadingDone();
 				GWT.log("search failed:", caught);
 			}
 
 			public void onSuccess(KeywordSearchResult foundEntryList) {
 				if (foundEntryList == null) {
 					getFrame().loadingDone();
 					return;
 				}
 				searchResultPanel.clear();
 				if (foundEntryList.count <= 0) {
 					searchResultPanel.add(new FormLabel("no entry is found"));
 				}
 				else {
 					pager.update(keyword, foundEntryList.page, foundEntryList.maxPage);
 					searchResultPanel.add(pager);
 
 					//String species = getTrackGroupProperty(UTGBProperty.SPECIES);
 
 					for (Entry e : foundEntryList.result) {
 						HorizontalPanel hp = new HorizontalPanel();
 						hp.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 
 						Image icon = new Image("theme/image/item.gif");
 						Style.margin(icon, Style.LEFT, 10);
 						hp.add(icon);
						FormLabel tagLabel = new FormLabel(e.name);
						tagLabel.setWidth("120px");
 						Style.margin(tagLabel, Style.LEFT, 3);
						Style.trimOverflowedText(tagLabel);
 						hp.add(tagLabel);
 
 						String label = e.ref + "/" + e.chr + ":" + e.start + "-" + e.end + "";
 						Anchor link = new Anchor(label);
 						Style.fontSize(link, 12);
 						//link.setStyleName("searchresult");
 						link.addClickHandler(new LocationMover(e));
 
 						hp.add(link);
 						searchResultPanel.add(hp);
 					}
 				}
 				refresh();
 				getFrame().loadingDone();
 			}
 
 		});
 	}
 
 	class KeywordTextBox extends TextBox {
 		public KeywordTextBox() {
 			super();
 
 			addKeyUpHandler(new KeyUpHandler() {
 				public void onKeyUp(KeyUpEvent e) {
 					if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER)
 						performSearch(keywordPanel.getInput(), 1, 10);
 				}
 			});
 			addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent e) {
 					KeywordTextBox.this.setFocus(true);
 				}
 			});
 
 		}
 	}
 
 	public KeywordSearchTrack() {
 		super("Keyword Search");
 		basePanel.setStyleName("form");
 		//searchResultPanel.setStyleName("searchresult");
 
 		final KeywordTextBox keywordBox = new KeywordTextBox();
 
 		keywordPanel = new SearchInput("Keyword", keywordBox);
 		final HorizontalPanel hp = new HorizontalPanel();
 		hp.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 		hp.add(keywordPanel);
 		Button button = new Button("Search");
 		button.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 				performSearch(keywordPanel.getInput(), 1, 10);
 			}
 		});
 		hp.add(button);
 
 		// keywordHelp.setStyleName("");
 		keywordHelpPopup.add(keywordHelp);
 		keywordHelpPopup.setStyleName("helpPopup");
 		final Label helpLabel = new Label("keyword help");
 		helpLabel.setStyleName("help");
 		helpLabel.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 				keywordHelpPopup.setPopupPosition(helpLabel.getAbsoluteLeft() + 5, helpLabel.getAbsoluteTop() + 10);
 				keywordHelpPopup.show();
 			}
 		});
 		hp.add(helpLabel);
 		layoutPanel.add(hp);
 
 		basePanel.add(layoutPanel, DockPanel.CENTER);
 		basePanel.add(searchResultPanel, DockPanel.SOUTH);
 	}
 
 	public Widget getWidget() {
 		return basePanel;
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 		trackFrame.pack();
 		// trackFrame.disableClose();
 	}
 
 	@Override
 	public void draw() {
 		StringBuilder htmlBuf = new StringBuilder();
 		htmlBuf.append("<b>Keyword Examples:</b>");
 		htmlBuf.append("<ul>");
 		for (String e : keywordExampleList) {
 			htmlBuf.append("<li>");
 			htmlBuf.append(e);
 			htmlBuf.append("</li>");
 		}
 		htmlBuf.append("</ul>");
 
 		keywordHelp.setHTML(htmlBuf.toString());
 	}
 
 	@Override
 	public void restoreProperties(Properties properties) {
 		keywordExampleList.clear();
 		keywordExampleList.addAll(JSONUtil.parseJSONArray(properties.get("keyword.examples", "[]")));
 
 		speciesScope = properties.get("species.scope", "unknown");
 	}
 
 	@Override
 	public void saveProperties(Properties saveData) {
 		saveData.add("keyword.examples", JSONUtil.toJSONArray(keywordExampleList));
 		saveData.add("species.scope", speciesScope);
 	}
 }
