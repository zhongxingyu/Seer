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
 // WIGGraphCanvasTrack.java
 // Since: Dec. 8, 2009
 //
 // $URL$ 
 // $Author$ yoshimura
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.GenomeBrowser;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.WigGraphData;
 import org.utgenome.gwt.utgb.client.canvas.GWTGenomeCanvas;
 import org.utgenome.gwt.utgb.client.db.datatype.BooleanType;
 import org.utgenome.gwt.utgb.client.db.datatype.FloatType;
 import org.utgenome.gwt.utgb.client.db.datatype.StringType;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackConfigChange;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupProperty;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.track.impl.TrackWindowImpl;
 import org.utgenome.gwt.utgb.client.ui.FormLabel;
 import org.utgenome.gwt.utgb.client.util.Properties;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.widgetideas.graphics.client.Color;
 
 public class WIGGraphCanvasTrack extends TrackBase {
 
 	protected TrackConfig config = new TrackConfig(this);
 	protected String fileName = "db/sample.wig.sqlite";
 	private final boolean isDebug = false;
 
 	private float maxValue = 20.0f;
 	private float minValue = 0.0f;
 	private boolean isAutoRange = false;
 	private boolean isLog = false;
 
 	private int height = 100;
 	private int leftMargin = 100;
 	private final int heightMargin = 10;
 
 	private List<WigGraphData> wigDataList;
 
 	//private ArrayList<Locus> genes = new ArrayList<Locus>();
 
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new WIGGraphCanvasTrack();
 			}
 		};
 	}
 
 	public WIGGraphCanvasTrack() {
 		super("WIG Graph Canvas");
 
 		layoutTable.setBorderWidth(0);
 		layoutTable.setCellPadding(0);
 		layoutTable.setCellSpacing(0);
 		layoutTable.getCellFormatter().setWidth(0, 0, leftMargin + "px");
 		layoutTable.setWidget(0, 0, labelPanel);
 		layoutTable.setWidget(0, 1, geneCanvas);
 
 		//		layoutTable.setHeight(100 + "px");
 
 		//CSS.border(geneCanvas, 2, "solid", "cyan");
 
 		//		geneCanvas.setLocusClickHandler(new LocusClickHandler() {
 		//			public void onClick(Locus locus) {
 		//				getTrackGroup().getPropertyWriter().setProperty("bss.query", locus.getName());
 		//			}
 		//		});
 
 	}
 
 	private final FlexTable layoutTable = new FlexTable();
 	private final GWTGenomeCanvas geneCanvas = new GWTGenomeCanvas();
 
 	private final AbsolutePanel labelPanel = new AbsolutePanel();
 
 	public Widget getWidget() {
 		return layoutTable;
 	}
 
 	@Override
 	public void draw() {
 
 	}
 
 	public static int calcXPositionOnWindow(long indexOnGenome, long startIndexOnGenome, long endIndexOnGenome, int windowWidth) {
 		double v = (indexOnGenome - startIndexOnGenome) * (double) windowWidth;
 		double v2 = v / (endIndexOnGenome - startIndexOnGenome);
 		return (int) v2;
 	}
 
 	@Override
 	public void onChangeTrackWindow(TrackWindow newWindow) {
 		update(newWindow);
 	}
 
 	@Override
 	public void onChangeTrackGroupProperty(TrackGroupPropertyChange change) {
 
 		if (change.containsOneOf(new String[] { UTGBProperty.TARGET })) {
 			update(change.getTrackWindow());
 		}
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 		config.addConfigParameter("File Name", new StringType("fileName"), fileName);
 		config.addConfigParameter("maxValue", new FloatType("maxValue"), String.valueOf(maxValue));
 		config.addConfigParameter("minValue", new FloatType("minValue"), String.valueOf(minValue));
 		config.addConfigParameter("Auto Range", new BooleanType("isAutoRange"), String.valueOf(isAutoRange));
 		config.addConfigParameter("Log Scale", new BooleanType("isLog"), String.valueOf(isLog));
 
 		update(group.getTrackWindow());
 	}
 
 	class UpdateCommand implements Command {
 		private final List<WigGraphData> dataList;
 
 		public UpdateCommand(List<WigGraphData> dataList) {
 			this.dataList = dataList;
 		}
 
 		public void execute() {
 			TrackWindow w = getTrackGroup().getTrackWindow();
 
 			// draw label
 			Label nameLabel = new FormLabel();
 			nameLabel.setStyleName("search-label");
 
			height = getDefaultWindowHeight();

 			labelPanel.clear();
 			labelPanel.setPixelSize(leftMargin, height);
 			labelPanel.add(nameLabel, 0, 0);
 			labelPanel.setWidgetPosition(nameLabel, 0, (height - nameLabel.getOffsetHeight()) / 2);
 
 			float tempMinValue = minValue;
 			float tempMaxValue = maxValue;
 
 			// get graph x-range
 			int s = w.getStartOnGenome();
 			int e = w.getEndOnGenome();
 			int width = w.getWindowWidth() - leftMargin;

 			geneCanvas.clear();
 			geneCanvas.setWindow(new TrackWindowImpl(width, s, e));
 			geneCanvas.setWindowHeight(height - heightMargin);
 			geneCanvas.setIndentHeight(heightMargin - 1);
 			geneCanvas.setPanelHeight(height);
 			geneCanvas.setIsLog(isLog);
 
 			// get graph y-range
 			if (isAutoRange) {
 				tempMinValue = 0.0f;
 				tempMaxValue = 0.0f;
 				for (WigGraphData data : dataList) {
 					tempMinValue = Math.min(tempMinValue, data.getMinValue());
 					tempMaxValue = Math.max(tempMaxValue, data.getMaxValue());
 				}
 				GWT.log("range:" + tempMinValue + "-" + tempMaxValue, null);
 			}
 
 			geneCanvas.setMinValue(tempMinValue);
 			geneCanvas.setMaxValue(tempMaxValue);
 
 			// draw frame
 			geneCanvas.drawFrame(labelPanel, leftMargin);
 
 			// draw data graph
 			for (WigGraphData data : dataList) {
 				if (isDebug) {
 					GWT.log(data.toString(), null);
 					for (long pos : data.getData().keySet()) {
 						GWT.log(pos + ":" + data.getData().get(pos), null);
 					}
 				}
 				if (data.getTrack().containsKey("name")) {
 					nameLabel.setText(data.getTrack().get("name"));
 				}
 				else {
 					nameLabel.setText(fileName);
 				}
 
 				Color color = new Color(Color.DARK_BLUE.toString());
 				if (data.getTrack().containsKey("color")) {
 					String colorStr = data.getTrack().get("color");
 					String c[] = colorStr.split(",");
 					if (c.length == 3)
 						color = new Color(Integer.valueOf(c[0]), Integer.valueOf(c[1]), Integer.valueOf(c[2]));
 				}
 				geneCanvas.drawWigGraph(data, color);
 
 				// adjust name label length
 				while (nameLabel.getOffsetWidth() > getLabelWidth(nameLabel, labelPanel)) {
 					nameLabel.setText(nameLabel.getText().substring(0, nameLabel.getText().length() - 1));
 					if (nameLabel.getText().equals(""))
 						break;
 				}
 			}
 
 			refresh();
 			getFrame().loadingDone();
 		}
 
 		private int getLabelWidth(Label nameLabel, AbsolutePanel labelPanel) {
 			int nameLabelTop = labelPanel.getWidgetTop(nameLabel);
 			int nameLabelBottom = nameLabelTop + nameLabel.getOffsetHeight();
 			int limit = Integer.MAX_VALUE;
 
 			for (int i = 0; i < labelPanel.getWidgetCount(); i++) {
 				Widget w = labelPanel.getWidget(i);
 				if (!labelPanel.getWidget(i).equals(nameLabel) && labelPanel.getWidgetTop(w) < nameLabelBottom
 						&& labelPanel.getWidgetTop(w) + w.getOffsetHeight() > nameLabelTop) {
 					limit = Math.min(limit, labelPanel.getWidgetLeft(w));
 				}
 			}
 
 			if (limit > leftMargin)
 				limit = leftMargin;
 
 			return limit;
 		}
 	}
 
 	public void update(TrackWindow newWindow) {
 		// retrieve gene data from the API
 		int s = newWindow.getStartOnGenome();
 		int e = newWindow.getEndOnGenome();
 		TrackGroupProperty prop = getTrackGroup().getPropertyReader();
 		String target = prop.getProperty(UTGBProperty.TARGET);
 		ChrLoc l = new ChrLoc();
 		l.start = s < e ? s : e;
 		l.end = s > e ? s : e;
 		l.target = target;
 
 		getFrame().setNowLoading();
 
 		GenomeBrowser.getService().getWigDataList(fileName, newWindow.getWindowWidth() - 100, l, new AsyncCallback<List<WigGraphData>>() {
 
 			public void onFailure(Throwable e) {
 				GWT.log("failed to retrieve wig data", e);
 				getFrame().loadingDone();
 			}
 
 			public void onSuccess(List<WigGraphData> dataList) {
 				wigDataList = dataList;
 				DeferredCommand.addCommand(new UpdateCommand(dataList));
 			}
 		});
 	}
 
 	@Override
 	public void onChangeTrackConfig(TrackConfigChange change) {
 		boolean isUpdate = false;
 
 		if (change.contains("fileName")) {
 			fileName = change.getValue("fileName");
 			isUpdate = true;
 		}
 
 		if (change.contains("maxValue")) {
 			maxValue = change.getFloatValue("maxValue");
 			GWT.log("max:" + maxValue, null);
 		}
 		if (change.contains("minValue")) {
 			minValue = change.getFloatValue("minValue");
 			GWT.log("min:" + minValue, null);
 		}
 		if (change.contains("isAutoRange")) {
 			isAutoRange = change.getBoolValue("isAutoRange");
 			GWT.log("auto range:" + isAutoRange, null);
 		}
 		if (change.contains("isLog")) {
 			isLog = change.getBoolValue("isLog");
 			GWT.log("log:" + isLog, null);
 		}
 
 		if (isUpdate) {
 			update(getTrackWindow());
 		}
 		else {
 			getFrame().setNowLoading();
 			DeferredCommand.addCommand(new UpdateCommand(wigDataList));
 		}
 	}
 
 	@Override
 	public void saveProperties(Properties saveData) {
 		saveData.add("fileName", fileName);
 		saveData.add("leftMargin", leftMargin);
 		saveData.add("maxValue", maxValue);
 		saveData.add("minValue", minValue);
 		saveData.add("isAutoRange", isAutoRange);
 		saveData.add("isLog", isLog);
 	}
 
 	@Override
 	public void restoreProperties(Properties properties) {
 		fileName = properties.get("fileName", fileName);
 		leftMargin = properties.getInt("leftMargin", leftMargin);
 		maxValue = properties.getFloat("maxValue", maxValue);
 		minValue = properties.getFloat("minValue", minValue);
 		isAutoRange = properties.getBoolean("isAutoRange", isAutoRange);
 		isLog = properties.getBoolean("isLog", isLog);
 
 		String p = properties.get("changeParamOnClick");
 		if (p != null) {
 			// set canvas action
 
 		}
 	}
 
 	@Override
 	public TrackConfig getConfig() {
 		return config;
 	}
 
 }
