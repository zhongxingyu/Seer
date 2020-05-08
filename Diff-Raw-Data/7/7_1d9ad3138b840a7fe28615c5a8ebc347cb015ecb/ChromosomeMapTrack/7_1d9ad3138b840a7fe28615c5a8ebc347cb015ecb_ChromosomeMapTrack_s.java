 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
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
 // utgb-core Project
 //
 // GenomeTrack.java
 // Since: Feb 17, 2009
 //
 // $URL: http://svn.utgenome.org/utgb/trunk/utgb/utgb-core/src/main/java/org/utgenome/gwt/utgb/client/track/lib/GenomeTrack.java $ 
 // $Author: leo $
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.ChrRange;
 import org.utgenome.gwt.utgb.client.bio.Coordinate;
 import org.utgenome.gwt.utgb.client.db.ValueDomain;
 import org.utgenome.gwt.utgb.client.db.datatype.StringType;
 import org.utgenome.gwt.utgb.client.track.Design;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackConfigChange;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupProperty;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyWriter;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.util.Properties;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ErrorEvent;
 import com.google.gwt.event.dom.client.ErrorHandler;
 import com.google.gwt.event.dom.client.LoadEvent;
 import com.google.gwt.event.dom.client.LoadHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * ChromosomeMapTrack is for visualizing Chromosome Map.
  * 
  * @author yoshimura
  * 
  */
 public class ChromosomeMapTrack extends TrackBase {
 
 	protected FlexTable layoutPanel = new FlexTable();
 
 	protected Image trackImage = new Image();
 	protected Frame frame = new Frame();
 	protected boolean isWidgetReady = false;
 
 	protected ChrRange chrRange = null;
 	private boolean isDebug = false;
 
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new ChromosomeMapTrack();
 			}
 		};
 	}
 
 	public ChromosomeMapTrack(String string) {
 		super(string);
 		init();
 	}
 
 	public ChromosomeMapTrack() {
 		this("Chromosome Map Track");
 	}
 
 	private void init() {
 		Style.fullWidth(layoutPanel);
 		layoutPanel.setCellPadding(0);
 		layoutPanel.setCellSpacing(0);
 		Style.fontSize(layoutPanel, 0);
 
 		Style.fullWidth(frame);
 		Style.margin(frame, 0);
 		Style.padding(frame, 0);
 		DOM.setElementProperty(frame.getElement(), "align", "left");
 		DOM.setElementPropertyInt(frame.getElement(), "marginHight", 0);
 		DOM.setElementPropertyInt(frame.getElement(), "marginWidth", 0);
 		DOM.setElementPropertyInt(frame.getElement(), "frameBorder", 0);
 
 		trackImage.addMouseDownHandler(new MouseDownHandler() {
 
 			public void onMouseDown(MouseDownEvent e) {
 				int x = e.getX();
 				int y = e.getY();
 				if (isDebug)
 					GWT.log("(x, y)=(" + x + ", " + y + ")", null);
 
 				ChrLoc chrLoc = getGenomeRange(x, y);
 
 				TrackGroupPropertyWriter propertyWriter = getTrackGroup().getPropertyWriter();
 				Properties p = new Properties();
 				p.put(UTGBProperty.TARGET, chrLoc.chr);
 				propertyWriter.setProperty(p, getTrackWindow().newWindow(chrLoc.start, chrLoc.end));
 			}
 
 			private ChrLoc getGenomeRange(int x, int y) {
 				ChrLoc chrLoc = new ChrLoc();
 				int viewWidth = getTrackGroup().getTrackWindow().getSequenceLength();
 
 				int windowWidth;
 				int index;
 				int chrNameWidth = chrRange.chrNameWidth;
 
 				String displayType = getConfig().getString(CONFIG_DISPLAY_TYPE, "normal");
 				if (!displayType.equals("rotate")) {
 					windowWidth = (trackImage.getWidth() - chrNameWidth);
 					index = (y * chrRange.ranges.size() / trackImage.getHeight());
 				}
 				else {
 					chrNameWidth = 20;
 					windowWidth = (trackImage.getHeight() - chrNameWidth);
 					GWT.log("" + trackImage.getWidth(), null);
 					index = (x * chrRange.ranges.size() / trackImage.getWidth());
 					x = trackImage.getHeight() - y;
 				}
 
 				if (index < chrRange.ranges.size()) {
 					chrLoc.chr = chrRange.ranges.get(index).chr;
 				}
 
 				chrLoc.start = (int) ((x - chrNameWidth) * chrRange.maxLength / (double) windowWidth) - (viewWidth / 2);
 				if (chrLoc.start <= 0) {
 					chrLoc.start = 1;
 				}
 
 				chrLoc.end = chrLoc.start + viewWidth;
 				if (chrLoc.end > chrRange.ranges.get(index).end) {
 					chrLoc.end = chrRange.ranges.get(index).end;
 					chrLoc.start = chrLoc.end - viewWidth;
 				}
 
 				return chrLoc;
 			}
 		});
 	}
 
 	public Widget getWidget() {
 		return layoutPanel;
 	}
 
 	@Override
 	public void draw() {
 
 		TrackGroupProperty propertyReader = getTrackGroup().getPropertyReader();
 		getBrowserService().getChrRegion(propertyReader.getProperty(UTGBProperty.SPECIES), propertyReader.getProperty(UTGBProperty.REVISION),
 				new AsyncCallback<ChrRange>() {
 
 					public void onFailure(Throwable arg0) {
 						GWT.log("get chrom region error!", arg0);
 					}
 
 					public void onSuccess(ChrRange r) {
 						chrRange = r;
 						setTrackGroupProperty(UTGBProperty.SEQUENCE_SIZE, Long.toString(r.maxLength));
 
 						if (isDebug) {
 							for (ChrLoc loc : chrRange.ranges) {
 								GWT.log(loc.chr + ":" + loc.start + "-" + loc.end, null);
 							}
 							GWT.log("max:" + chrRange.maxLength, null);
 						}
 					}
 				});
 
 		int leftMargin = getLeftMargin();
 		if (leftMargin > 0)
 			layoutPanel.getCellFormatter().setWidth(0, 0, leftMargin + "px");
 		if (isDebug)
 			GWT.log("left margin:" + leftMargin, null);
 
 		if (!isWidgetReady) {
 			layoutPanel.setWidget(0, 1, trackImage);
 			isWidgetReady = true;
 		}
 		trackImage.setUrl(getTrackURL());
 		getFrame().setNowLoading();
 	}
 
 	protected int getLeftMargin() {
 		return getConfig().getInt(CONFIG_LEFT_MARGIN, 0);
 	}
 
 	protected String getDisplayType() {
 		return getConfig().getString(CONFIG_DISPLAY_TYPE, "normal");
 	}
 
 	protected String getTrackBaseURL() {
		return "utgb-core/ChromosomeMap";
 	}
 
 	protected String getTrackURL() {
 		Coordinate c = getCoordinate();
 
 		Properties p = new Properties();
		TrackWindow w = getTrackGroup().getTrackWindow();
		p.add("start", w.getStartOnGenome());
		p.add("end", w.getEndOnGenome());
		p.add("width", w.getPixelWidth() - getLeftMargin());
 		p.add("displayType", getDisplayType());
 
 		for (String key : new String[] { "dbGroup", "dbName", "bss.query" }) {
 			String v = getTrackGroup().getProperty(key);
 			if (v != null)
 				p.add(key, v);
 		}
 
 		return c.getTrackURL(getTrackBaseURL(), p);
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 
 		trackImage.addLoadHandler(new LoadHandler() {
 			public void onLoad(LoadEvent arg0) {
 				getFrame().onUpdateTrackWidget();
 				getFrame().loadingDone();
 			}
 		});
 		trackImage.addErrorHandler(new ErrorHandler() {
 			public void onError(ErrorEvent arg0) {
 				trackImage.setUrl(Design.IMAGE_NOT_AVAILABLE);
 				getFrame().loadingDone();
 			}
 		});
 
 		int height = this.getDefaultWindowHeight();
 		if (height > 0) {
 			frame.setHeight(height + "px");
 			layoutPanel.setHeight(height + "px");
 		}
 
 		// set up the configuration panel
 		TrackConfig config = getConfig();
 		//config.addConfigParameter("Track Base URL", new StringType(CONFIG_TRACK_BASE_URL));
 		ValueDomain displayTypeDomain = ValueDomain.createNewValueDomain(new String[] { "normal", "compact", "rotate" });
 		config.addConfig("Display Type", new StringType(CONFIG_DISPLAY_TYPE, displayTypeDomain), "normal");
 		config.addHiddenConfig(CONFIG_LEFT_MARGIN, "0");
 	}
 
 	@Override
 	public void onChange(TrackGroupPropertyChange change, TrackWindow newWindow) {
 
 		if (newWindow != null
 				|| (change != null && change.containsOneOf(new String[] { UTGBProperty.SPECIES, UTGBProperty.REVISION, "dbGroup", "dbName", "bss.query" }))) {
 			draw();
 		}
 	}
 
 	@Override
 	public void onChangeTrackConfig(TrackConfigChange change) {
 		if (change.containsOneOf(new String[] { CONFIG_DISPLAY_TYPE })) {
 			refresh();
 		}
 	}
 
 	private final String CONFIG_LEFT_MARGIN = "leftMargin";
 	private final String CONFIG_DISPLAY_TYPE = "displayType";
 
 }
