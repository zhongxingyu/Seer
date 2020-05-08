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
 // GenomeBrowser Project
 //
 // RulerTrack.java
 // Since: Jun 6, 2007
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import org.utgenome.gwt.utgb.client.track.Design;
 import org.utgenome.gwt.utgb.client.track.RangeSelectable;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackRangeSelector;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.ui.AbsoluteFocusPanel;
 import org.utgenome.gwt.utgb.client.util.Properties;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 class MouseListenerOnRulerWidget implements MouseDownHandler {
 	private final TrackRangeSelector _rangeSelector;
 
 	public MouseListenerOnRulerWidget(TrackRangeSelector rangeSelector) {
 		this._rangeSelector = rangeSelector;
 	}
 
 	public void onMouseDown(MouseDownEvent e) {
 		_rangeSelector.onMouseDownFromChild(e);
 	}
 }
 
 /**
  * Ruler computes a tick range within ruler trakcs
  * 
  * @author leo
  * 
  */
 class Ruler {
 	public static String[] _unitSuffix = { "", "K", "M", "G", "T", "P" };
 	public static int RULER_HEIGHT = 14;
 	private final static String TICK_IMAGE = Design.IMAGE_RULER_TICK;
 	static {
 		Image.prefetch(TICK_IMAGE);
 	}
 	private int tickUnit = 1;
 	private int tickUnitIndex = 0;
 	private long tickRangeOnGenome = 1;
 	private final TrackRangeSelector _rangeSelector;
 	private String rulerLabelStyle = "ruler-tick";
 	private final MouseListenerOnRulerWidget _commonMouseListener;
 
 	public Ruler(TrackRangeSelector rangeSelector) {
 		this._rangeSelector = rangeSelector;
 		_commonMouseListener = new MouseListenerOnRulerWidget(_rangeSelector);
 	}
 
 	public Ruler(TrackRangeSelector rangeSelector, String labelStyle) {
 		this._rangeSelector = rangeSelector;
 		_commonMouseListener = new MouseListenerOnRulerWidget(_rangeSelector);
 		rulerLabelStyle = labelStyle;
 	}
 
 	class RulerLabel extends Label {
 		public RulerLabel(String label) {
 			super(label);
 			setStyleName(rulerLabelStyle);
 			addMouseDownHandler(_commonMouseListener);
 		}
 	}
 
 	public void updateTickUnit(int windowWidth, long s, long e) {
 		// update tick unit
 		e++;
		long range = (e > s) ? e - s + 1 : s - e + 1;
 		if (range <= 0)
 			range = 1;
 		tickRangeOnGenome = suitableTickRangeOnGenome(range);
 		long div = tickRangeOnGenome;
 		int unitCount = 0;
 		tickUnit = 1;
 		while (((div = div / 1000) > 0) && unitCount < _unitSuffix.length) {
 			tickUnit *= 1000;
 			unitCount++;
 		}
 		tickUnitIndex = unitCount;
 	}
 
 	public void draw(AbsoluteFocusPanel panel, int windowWidth, long s, long e, boolean isReverseStrand) {
 		assert (s <= e);
 		panel.setSize(windowWidth + "px", RULER_HEIGHT + "px");
 		e++;
		long range = (e > s) ? e - s + 1 : s - e + 1;
 		if (range <= 0)
 			range = 1;
 
 		double pixelLengthPerRange = (double) windowWidth / (double) range;
 		//int tickRangeOnRuler = (int) (tickRangeOnGenome * pixelLengthPerRange);
 
 		//int numTicks = range / tickRangeOnGenome;
 
 		long initTickCursor = s;
 		if (((s - 1) % tickRangeOnGenome) != 0)
 			initTickCursor = ((s + tickRangeOnGenome) / tickRangeOnGenome) * tickRangeOnGenome;
 
 		for (long tickCursor = initTickCursor; tickCursor <= e; tickCursor += tickRangeOnGenome) {
 			long pos = tickCursor - s;
 			int tickX = (int) (pos * pixelLengthPerRange);
 			if (tickX >= 0) {
 				Image tick = new Image(TICK_IMAGE);
 				RulerLabel label = new RulerLabel(indexOnRuler(tickCursor));
 				if (!isReverseStrand) {
 					panel.add(tick, tickX, 0);
 					panel.add(label, tickX + 2, 0);
 				}
 				else {
 					panel.add(tick, windowWidth - tickX, 0);
 					panel.add(label, windowWidth - tickX + 2, 0);
 				}
 			}
 
 		}
 
 		/*
 		int initTickCursor = (s / tickRangeOnGenome) * tickRangeOnGenome;
 		for(int tickCursor = initTickCursor; tickCursor <= e; tickCursor += tickRangeOnGenome){
 			int tickX;
 			tickX = (int) ((tickCursor - s) * pixelLengthPerRange);
 			if (tickX >= 0) {
 				Image tick = new Image(TICK_IMAGE);
 				RulerLabel label = new RulerLabel(indexOnRuler(tickCursor));
 				if(!isReverseStrand)
 				{
 					panel.add(tick, tickX, 0);
 					panel.add(label, tickX + 2, 0);
 				}
 				else
 				{
 					panel.add(tick, windowWidth - tickX, 0);
 					panel.add(label, windowWidth - tickX + 2, 0);
 				}
 			}
 		}
 		*/
 	}
 
 	private String indexOnRuler(long genomePos) {
 		return (genomePos / tickUnit) + _unitSuffix[tickUnitIndex];
 	}
 
 	private long suitableTickRangeOnGenome(long range) {
 		if (range <= 1)
 			return 1;
 		int[] availableTickUnit = { 1, 2, 5, 10, 20, 25 };
 
 		/*
 		int factor = 1;
 		int currentTickLength = tickUnit;
 		int numTicks = range / currentTickLength;
 		
 		int loopCount = 0;
 		while (numTicks > numTicksMax || numTicks < 5) {
 			currentTickLength = availableTickUnit[loopCount % availableTickUnit.length] * factor;
 			numTicks = range / currentTickLength;
 			if (loopCount > 0 && loopCount % availableTickUnit.length == (availableTickUnit.length - 1))
 				factor *= 10;
 			loopCount++;
 		}
 		return currentTickLength;
 		*/
 
 		int numTicksMax = 13;
 		for (int i = numTicksMax; i > 0; i--) {
 			long tickRange = range / i;
 			if (tickRange <= 0)
 				continue;
 
 			long tickFraction = range % i;
 
 			long factor = (long) (Math.log(tickRange) / Math.log(10));
 			long factor10 = (long) Math.pow(10, factor);
 			long tickUnit = tickRange / factor10;
 			long tickFractionUnit = tickFraction / factor10;
 			if (tickUnit < tickFractionUnit)
 				continue;
 			for (int m = 0; m < availableTickUnit.length; m++) {
 				if (tickUnit == availableTickUnit[m])
 					return tickRange;
 			}
 		}
 		return range;
 	}
 }
 
 /**
  * RulerTrack
  * 
  * @author leo
  * 
  */
 public class RulerTrack extends TrackBase implements RangeSelectable {
 	private TrackRangeSelector _rangeSelector;
 	private Grid _layoutPanel = new Grid(1, 2);
 	private AbsoluteFocusPanel _basePanel = new AbsoluteFocusPanel();
 	private Ruler ruler;
 	private int _windowLeftMargin = 0;
 
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new RulerTrack();
 			}
 		};
 	}
 
 	public RulerTrack() {
 		super("Track Ruler");
 		init();
 	}
 
 	private void init() {
 		_basePanel.setStyleName("ruler");
 		DOM.setStyleAttribute(_basePanel.getElement(), "cursor", "pointer");
 		_basePanel.setTitle("click twice to zoom the specified range");
 		_rangeSelector = new TrackRangeSelector(this);
 		ruler = new Ruler(_rangeSelector);
 		_layoutPanel.setCellPadding(0);
 		_layoutPanel.setCellSpacing(0);
 		Style.fontSize(_layoutPanel, 0);
 	}
 
 	public RulerTrack(int windowLeftMargin) {
 		super("Track Ruler");
 		_windowLeftMargin = windowLeftMargin;
 		init();
 	}
 
 	public void clear() {
 		_basePanel.clear();
 	}
 
 	public Widget getWidget() {
 		return _layoutPanel;
 	}
 
 	@Override
 	public void draw() {
 		_basePanel.clear();
 		if (_windowLeftMargin > 0)
 			_layoutPanel.getCellFormatter().setWidth(0, 0, _windowLeftMargin + "px");
 		_layoutPanel.setWidget(0, 1, _basePanel);
 		TrackWindow w = getTrackGroup().getTrackWindow();
 		int windowWidth = w.getWindowWidth() - _windowLeftMargin;
 		long s = w.getStartOnGenome();
 		long e = w.getEndOnGenome();
 
 		if (s <= e) {
 			ruler.updateTickUnit(windowWidth, s, e);
 			ruler.draw(_basePanel, windowWidth, s, e, false);
 		}
 		else {
 			ruler.updateTickUnit(windowWidth, e, s);
 			ruler.draw(_basePanel, windowWidth, e, s, true);
 		}
 	}
 
 	@Override
 	public int getDefaultWindowHeight() {
 		return Ruler.RULER_HEIGHT;
 	}
 
 	@Override
 	public void onChangeTrackWindow(TrackWindow newWindow) {
 		_rangeSelector.setWindowWidth(newWindow.getWindowWidth());
 		refresh();
 	}
 
 	public AbsoluteFocusPanel getAbsoluteFocusPanel() {
 		return _basePanel;
 	}
 
 	public void onRangeSelect(int x1OnTrackWindow, int x2OnTrackWindow) {
 		TrackWindow w = getTrackGroup().getTrackWindow();
 		double factor = w.getWindowWidth() / (double) (w.getWindowWidth() - _windowLeftMargin);
 
 		if (x1OnTrackWindow > x2OnTrackWindow) {
 			int tmp = x1OnTrackWindow;
 			x1OnTrackWindow = x2OnTrackWindow;
 			x2OnTrackWindow = tmp;
 		}
 
 		int startOnGenome = w.calcGenomePosition((int) (x1OnTrackWindow * factor));
 		int endOnGenome = w.calcGenomePosition((int) (x2OnTrackWindow * factor));
 
 		getTrackGroup().getPropertyWriter().setTrackWindow(startOnGenome, endOnGenome);
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 		trackFrame.disablePack();
 		trackFrame.disableResize();
 	}
 
 	@Override
 	public void restoreProperties(Properties properties) {
 		_windowLeftMargin = properties.getInt("leftMargin", _windowLeftMargin);
 	}
 
 	@Override
 	public void saveProperties(Properties saveData) {
 		saveData.add("leftMargin", _windowLeftMargin);
 	}
 }
