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
 // utgb-core Project
 //
 // NavigatorTrack.java
 // Since: Oct 2, 2007
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.track.bean.SequenceInfo;
 import org.utgenome.gwt.utgb.client.ui.FormLabel;
 import org.utgenome.gwt.utgb.client.util.JSONUtil;
 import org.utgenome.gwt.utgb.client.util.Properties;
 import org.utgenome.gwt.utgb.client.util.StringUtil;
 import org.utgenome.gwt.utgb.client.util.xml.XMLWriter;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Navigator of the UTGB
  * 
  * @author leo
  * 
  */
 public class NavigatorTrack extends TrackBase {
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new NavigatorTrack();
 			}
 		};
 	}
 
 	public VerticalPanel panel = new VerticalPanel();
 	private ListBox speciesBox = new ListBox();
 	private ListBox revisionBox = new ListBox();
 	private TextBox targetBox = new TextBox();
 	private TextBox startBox = new TextBox();
 	private TextBox endBox = new TextBox();
 	private ArrayList<SequenceInfo> sequenceInfoList = new ArrayList<SequenceInfo>();
 
 	private class PropertyChangeHandler implements ChangeHandler {
 		private String proeprty;
 		private ListBox listBox;
 
 		public PropertyChangeHandler(String property, ListBox listBox) {
 			this.proeprty = property;
 			this.listBox = listBox;
 		}
 
 		public void onChange(ChangeEvent e) {
 			getTrackGroup().getPropertyWriter().setProperty(proeprty, listBox.getItemText(listBox.getSelectedIndex()));
 		}
 	}
 
 	private class SequenceRangeChangeListner implements KeyUpHandler {
 		public void onKeyUp(KeyUpEvent e) {
 			int keyCode = e.getNativeKeyCode();
 			if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
 				try {
 					getTrackGroup().setTrackWindowLocation(StringUtil.toInt(startBox.getText()), StringUtil.toInt(endBox.getText()));
 				}
 				catch (NumberFormatException ex) {
 					GWT.log("(" + startBox.getText() + ", " + endBox.getText() + ") is invalid range", ex);
 				}
 			}
 		}
 	}
 
 	public static void scroll(TrackGroup group, double movePercentageOnWindow) {
 		TrackWindow window = group.getTrackWindow();
 		int genomeRange = window.getEndOnGenome() - window.getStartOnGenome() + 1;
 		boolean isPlusStrand = true;
 		if (genomeRange < 0) {
 			genomeRange = -genomeRange;
 			isPlusStrand = false;
 		}
 		int offset = (int) (genomeRange * (movePercentageOnWindow / 100.0));
 		if (!isPlusStrand)
 			offset = -offset;
 
 		if (window.getStartOnGenome() + offset < 0) {
 			offset = -window.getStartOnGenome() + 1;
 		}
 		if (window.getEndOnGenome() + offset < 0) {
 			offset = -window.getEndOnGenome() + 1;
 		}
 
 		group.getPropertyWriter().setTrackWindow(window.getStartOnGenome() + offset, window.getEndOnGenome() + offset);
 	}
 
 	public static void zoom(TrackGroup group, int scaleDiff) {
 		if (scaleDiff == 0)
 			return; // do nothing
 
 		TrackWindow currentWindow = group.getTrackWindow();
 		int start = currentWindow.getStartOnGenome();
 		int end = currentWindow.getEndOnGenome();
 		int windowSize = (start < end) ? end - start + 1 : start - end + 1;
 
 		// compute a close window size that is a power of 10, e.g., 10, 100, 1000, 10000, ...
 		int windowUpperBound = (int) Math.pow(10L, Math.ceil(Math.log(windowSize) / Math.log(10)));
 		int windowLowerBound = (int) Math.pow(10L, Math.floor(Math.log(windowSize) / Math.log(10)));
 
 		if (windowUpperBound == windowLowerBound) {
 			if (scaleDiff > 0)
 				windowUpperBound *= 10;
 			else
 				windowLowerBound /= 10;
 		}
 
 		final double magnificationRatio = 0.25;
 
 		int tickWidth = (int) (windowUpperBound * magnificationRatio);
 		int currentPos = windowSize / tickWidth;
 		int nextPos = currentPos + scaleDiff;
 		int nextWindowSize;
 		if (nextPos <= 0) {
 			nextWindowSize = windowLowerBound;
 		}
 		else
 			nextWindowSize = tickWidth * nextPos;
 
 		zoomWindow(group, nextWindowSize);
 	}
 
 	public static void zoomWindow(TrackGroup group, int windowSize) {
 		TrackWindow currentWindow = group.getTrackWindow();
 		int start = currentWindow.getStartOnGenome();
 		int end = currentWindow.getEndOnGenome();
 
 		int middle = (start + end) / 2;
 
 		if (windowSize <= 100)
 			windowSize = 100;
 		if (windowSize >= 10000000) // 10M
 			windowSize = 10000000;
 
 		int half = windowSize / 2;
 		if (start <= end)
 			group.setTrackWindowLocation(middle - half + 1, middle + half);
 		else
 			group.setTrackWindowLocation(middle + half, middle - half + 1);
 
 	}
 
 	class ScrollButtonSet extends Composite {
 		HorizontalPanel _panel = new HorizontalPanel();
 
 		class WindowScrollButton extends Button implements ClickHandler {
 			int movePercentageOnWindow;
 
 			public WindowScrollButton(String label, int movePercentageOnWindow) {
 				super(label);
 				setStyleName("scrollbutton");
 				addClickHandler(this);
 				this.movePercentageOnWindow = movePercentageOnWindow;
 			}
 
 			public void onClick(ClickEvent e) {
 				scroll(getTrackGroup(), movePercentageOnWindow);
 			}
 		}
 
 		class ZoomButton extends Button implements ClickHandler {
 			int windowSize;
 
 			public ZoomButton(String label, int windowSize) {
 				super(label);
 				this.windowSize = windowSize;
 				setStyleName("scrollbutton");
 				addClickHandler(this);
 			}
 
 			public void onClick(ClickEvent e) {
 				zoomWindow(getTrackGroup(), windowSize);
 			}
 		}
 
 		public ScrollButtonSet() {
 			_panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 			_panel.add(new WindowScrollButton("<<< ", -95));
 			_panel.add(new WindowScrollButton("<< ", -50));
 			_panel.add(new WindowScrollButton("< ", -25));
 			_panel.add(new WindowScrollButton("> ", 25));
 			_panel.add(new WindowScrollButton(">> ", 50));
 			_panel.add(new WindowScrollButton(">>> ", 95));
 			_panel.add(new FormLabel("Window Size:"));
 			_panel.add(new ZoomButton("100B", 100));
 			_panel.add(new ZoomButton("1K", 1000));
 			_panel.add(new ZoomButton("10K", 10000));
 			_panel.add(new ZoomButton("100K", 100000));
 			_panel.add(new ZoomButton("1M", 1000000));
 			_panel.add(new ZoomButton("10M", 10000000));
 			initWidget(_panel);
 		}
 	}
 
 	private final HorizontalPanel hp = new HorizontalPanel();
 	private final Track _self = this;
 	private boolean isPlusStrand = true;
 
 	public NavigatorTrack() {
 		super("NavigatorTrack");
 		panel.setStyleName("toolbox");
 		panel.setWidth("100%");
 
 		speciesBox.addChangeHandler(new PropertyChangeHandler(UTGBProperty.SPECIES, speciesBox));
 		revisionBox.addChangeHandler(new PropertyChangeHandler(UTGBProperty.REVISION, revisionBox));
 		startBox.addKeyUpHandler(new SequenceRangeChangeListner());
 		endBox.addKeyUpHandler(new SequenceRangeChangeListner());
 		targetBox.addKeyUpHandler(new KeyUpHandler() {
 			public void onKeyUp(KeyUpEvent e) {
 				int keyCode = e.getNativeKeyCode();
 				if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
 					getTrackGroup().getPropertyWriter().setProperty(UTGBProperty.TARGET, targetBox.getText());
 				}
 			}
 		});
 		targetBox.setWidth("100px");
 		// value selectors
 		hp.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 		hp.add(new FormLabel("Species"));
 		hp.add(speciesBox);
 		hp.add(new FormLabel("Ref."));
 		hp.add(revisionBox);
 		hp.add(new FormLabel("Chr."));
 		hp.add(targetBox);
 		// window locator
 		startBox.setWidth("95px");
 		endBox.setWidth("95px");
 		HorizontalPanel hp2 = new HorizontalPanel();
 		hp2.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 		hp2.add(new FormLabel("Start"));
 		hp2.add(startBox);
 		hp2.add(new FormLabel("End"));
 		hp2.add(endBox);
 
 		Button strandSwitch = new Button("reverse");
 		Style.margin(strandSwitch, Style.LEFT, 2);
 		Style.border(strandSwitch, 2, Style.BORDER_OUTSET, "white");
 		strandSwitch.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 				isPlusStrand = !isPlusStrand;
 				TrackWindow window = getTrackGroup().getTrackWindow();
 				if (isPlusStrand) {
 					getTrackGroup().setTrackWindowLocation(window.getEndOnGenome(), window.getStartOnGenome());
 				}
 				else {
 					getTrackGroup().setTrackWindowLocation(window.getEndOnGenome(), window.getStartOnGenome());
 				}
 			}
 		});
 		hp2.add(strandSwitch);
 		hp2.add(new ScrollButtonSet());
 		// save view
 		final FormPanel saveViewForm = new FormPanel();
 		saveViewForm.setAction(GWT.getModuleBaseURL() + "utgb-core/EchoBackView");
 		saveViewForm.setEncoding(FormPanel.ENCODING_URLENCODED);
 		saveViewForm.setMethod(FormPanel.METHOD_POST);
 		final Hidden viewData = new Hidden("view");
 		final Hidden time = new Hidden("time");
 		final Button saveButton = new Button("save view");
 		HorizontalPanel formLayout = new HorizontalPanel();
 		formLayout.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 		viewData.setVisible(false);
 		formLayout.add(viewData);
 		formLayout.add(time);
 		formLayout.add(saveButton);
 		saveButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 				XMLWriter xmlWriter = new XMLWriter();
 				getTrackGroup().toXML(xmlWriter);
 				String view = xmlWriter.toString();
 				viewData.setValue(view);
 				// send the time stamp
 				Date today = new Date();
 				time.setValue(Long.toString(today.getTime()));
 				saveViewForm.submit();
 			}
 		});
 		saveViewForm.add(formLayout);
 		DOM.setStyleAttribute(saveViewForm.getElement(), "margin", "0");
 		hp.add(saveViewForm);
 		Button loadButton = new Button("load view");
 		loadButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 				getTrackGroup().insertTrack(new ViewLoaderTrack(), getTrackGroup().getTrackIndex(_self) + 1);
 			}
 		});
 		hp.add(loadButton);
 		// layout widgets
 		panel.add(hp);
 		panel.add(hp2);
 
 	}
 
 	public Widget getWidget() {
 		return panel;
 	}
 
 	private void retrieveSpeciesList() {
 		speciesBox.clear();
 		for (Iterator<SequenceInfo> it = sequenceInfoList.iterator(); it.hasNext();) {
 			SequenceInfo sequenceInfo = it.next();
 			speciesBox.addItem(sequenceInfo.getSpecies());
 		}
 		if (!sequenceInfoList.isEmpty())
 			updateListBox();
 	}
 
 	private void updateListBox() {
 		String species = getSelectedSpecies();
 
 		ArrayList<String> revisionList = new ArrayList<String>();
 		for (Iterator<SequenceInfo> it = sequenceInfoList.iterator(); it.hasNext();) {
 			SequenceInfo sequenceInfo = it.next();
 			if (sequenceInfo.getSpecies().equals(species)) {
 				revisionBox.clear();
 				for (Iterator<String> rit = sequenceInfo.getRevisionList().iterator(); rit.hasNext();) {
 					String revision = rit.next();
 					revisionBox.addItem(revision);
 				}
 			}
 		}
 
 		boolean canSelectRevision = selectItem(revisionBox, getTrackGroup().getPropertyReader().getProperty(UTGBProperty.REVISION));
 		if (!canSelectRevision) {
 			getTrackGroup().getPropertyWriter().setProperty(UTGBProperty.REVISION, revisionBox.getItemText(0));
 		}
 
 	}
 
 	private String getSelectedSpecies() {
 		return speciesBox.getItemText(speciesBox.getSelectedIndex());
 	}
 
 	private boolean selectItem(ListBox listBox, String value) {
 		for (int i = 0; i < listBox.getItemCount(); i++) {
 			String itemText = listBox.getItemText(i);
 			if (itemText.equals(value)) {
 				listBox.setSelectedIndex(i);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void updateRangeBox() {
 		startBox.setText(StringUtil.formatNumber(getTrackWindow().getStartOnGenome()));
 		endBox.setText(StringUtil.formatNumber(getTrackWindow().getEndOnGenome()));
 	}
 
 	@Override
 	public void onChangeTrackGroupProperty(TrackGroupPropertyChange change) {
 		final String[] relatedProperties = new String[] { UTGBProperty.SPECIES, UTGBProperty.REVISION, UTGBProperty.TARGET };
 		if (change.containsOneOf(relatedProperties)) {
 			String newSpecies = change.getProperty(UTGBProperty.SPECIES);
 			if (newSpecies != null && !newSpecies.equals(getSelectedSpecies())) {
 				selectItem(speciesBox, newSpecies);
 			}
 			updateListBox();
 
 			if (change.contains(UTGBProperty.TARGET))
 				targetBox.setText(change.getProperty(UTGBProperty.TARGET));
 
 			updateRangeBox();
 		}
 	}
 
 	@Override
 	public void onChangeTrackWindow(TrackWindow newWindow) {
 		updateRangeBox();
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 		trackFrame.disableClose();
 		TrackWindow w = group.getTrackWindow();
 		startBox.setText(StringUtil.formatNumber(w.getStartOnGenome()));
 		endBox.setText(StringUtil.formatNumber(w.getEndOnGenome()));
 		targetBox.setText(group.getPropertyReader().getProperty("target", "chr1"));
 
 		retrieveSpeciesList();
 	}
 
 	@Override
 	public void saveProperties(Properties saveData) {
 
 		StringBuffer buf = new StringBuffer();
 		buf.append("[");
 		int count = 0;
 		for (Iterator<SequenceInfo> it = sequenceInfoList.iterator(); it.hasNext(); count++) {
 			if (count > 0)
 				buf.append(",");
 			SequenceInfo sequenceInfo = it.next();
 			buf.append(sequenceInfo.toJSON());
 		}
 		buf.append("]");
 		saveData.add("sequenceList", buf.toString());
 	}
 
 	@Override
 	public void restoreProperties(Properties properties) {
 
 		JSONValue v = JSONParser.parse(properties.get("sequenceList", "[]"));
 		sequenceInfoList.clear();
 		JSONArray list = v.isArray();
 		if (list != null) {
 			for (int i = 0; i < list.size(); i++) {
 				JSONObject sequenceInfo = list.get(i).isObject();
 				if (sequenceInfo != null) {
 					JSONValue speciesValue = sequenceInfo.get("species");
 					if (speciesValue == null)
 						continue;
 
 					String species = JSONUtil.toStringValue(speciesValue);
 					SequenceInfo si = new SequenceInfo(species);
 					JSONValue arrayValue = sequenceInfo.get("revision");
 					JSONArray revisionArray = arrayValue.isArray();
 					for (int j = 0; j < revisionArray.size(); j++) {
 						String revision = JSONUtil.toStringValue(revisionArray.get(j));
 						si.addRevision(revision);
 					}
 					sequenceInfoList.add(si);
 				}
 			}
 		}
 	}
 }
