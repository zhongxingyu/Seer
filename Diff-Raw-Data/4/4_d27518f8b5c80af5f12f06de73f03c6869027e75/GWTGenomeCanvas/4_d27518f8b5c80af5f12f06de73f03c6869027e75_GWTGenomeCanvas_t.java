 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
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
 // GWTGenomeCanvas.java
 // Since: Jul 8, 2008
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.canvas;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.bio.CDS;
 import org.utgenome.gwt.utgb.client.bio.Exon;
 import org.utgenome.gwt.utgb.client.bio.Gene;
 import org.utgenome.gwt.utgb.client.bio.GraphData;
 import org.utgenome.gwt.utgb.client.bio.InfoSilkGenerator;
 import org.utgenome.gwt.utgb.client.bio.Interval;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitor;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitorBase;
 import org.utgenome.gwt.utgb.client.bio.Read;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.ReferenceSequence;
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout.LocusLayout;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.ui.FixedWidthLabel;
 import org.utgenome.gwt.utgb.client.ui.RoundCornerFrame;
 import org.utgenome.gwt.utgb.client.util.Optional;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.widgetideas.graphics.client.Color;
 import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 
 /**
  * Browser-side graphic canvas for drawing gene objects
  * 
  * @author leo
  * 
  */
 public class GWTGenomeCanvas extends Composite {
 
 	//private int windowWidth = 800;
 
 	private final int DEFAULT_GENE_HEIGHT = 9;
 	private int geneHeight = DEFAULT_GENE_HEIGHT;
 	private int geneMargin = 2;
 	private boolean reverse = false;
 
 	// widget
 	private GWTCanvas canvas = new GWTCanvas();
 	private AbsolutePanel basePanel = new AbsolutePanel();
 	private AbsolutePanel panel = new AbsolutePanel();
 	private static PopupInfo popupLabel = new PopupInfo();
 	private LocusClickHandler clickHandler = null;
 
 	private IntervalLayout intervalLayout = new IntervalLayout();
 	private TrackWindow trackWindow;
 
 	public static enum CoverageStyle {
 		DEFAULT, SMOOTH
 	};
 
 	private CoverageStyle coverageStyle = CoverageStyle.DEFAULT;
 
 	private List<Widget> readLabels = new ArrayList<Widget>();
 
 	private TrackGroup trackGroup;
 
 	public GWTGenomeCanvas() {
 		initWidget();
 	}
 
 	public void setCoverageStyle(String style) {
 		if (style == null)
 			return;
 
 		CoverageStyle s = CoverageStyle.valueOf(CoverageStyle.class, style.toUpperCase());
 		if (s != null) {
 			coverageStyle = s;
 		}
 
 	}
 
 	static class PopupInfo extends PopupPanel {
 
 		OnGenome locus;
 		private VerticalPanel info = new VerticalPanel();
 
 		public PopupInfo() {
 			super(true);
 
 			Style.padding(info, Style.LEFT | Style.RIGHT, 5);
 			Style.fontColor(info, "white");
 			Style.fontSize(info, 14);
 			Style.margin(info, 0);
 			Style.preserveWhiteSpace(info);
 
 			RoundCornerFrame infoFrame = new RoundCornerFrame("336699", 0.7f, 4);
 			infoFrame.setWidget(info);
 			this.setWidget(infoFrame);
 
 		}
 
 		public void setLocus(OnGenome g) {
 			if (this.locus == g)
 				return;
 
 			this.locus = g;
 
 		}
 
 		public void update() {
 			if (locus == null)
 				return;
 
 			InfoSilkGenerator silk = new InfoSilkGenerator();
 			locus.accept(silk);
 			info.clear();
 			for (String line : silk.getLines()) {
 				info.add(new HTML(line));
 			}
 		}
 
 	}
 
 	public void setLocusClickHandler(LocusClickHandler handler) {
 		this.clickHandler = handler;
 	}
 
 	@Override
 	public void onBrowserEvent(Event event) {
 
 		super.onBrowserEvent(event);
 
 		int type = DOM.eventGetType(event);
 		switch (type) {
 		case Event.ONMOUSEOVER:
 
 			break;
 		case Event.ONMOUSEMOVE: {
 			// show readLabels 
 			OnGenome g = overlappedInterval(event, 2);
 			if (g != null) {
 				if (popupLabel.locus != g) {
 
 					Style.cursor(canvas, Style.CURSOR_POINTER);
 
 					int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 					int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 					displayInfo(clientX, clientY, g);
 				}
 			}
 			else {
 				if (dragStartPoint.isDefined()) {
 					// scroll the canvas
 					int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 					int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 					DragPoint p = dragStartPoint.get();
 					int xDiff = clientX - p.x;
 					//int yDiff = clientY - p.y;
 					basePanel.setWidgetPosition(panel, xDiff, 0);
 				}
 				else {
 					Style.cursor(canvas, Style.CURSOR_AUTO);
 					popupLabel.setLocus(null);
 				}
 			}
 
 			break;
 		}
 		case Event.ONMOUSEOUT: {
 			resetDrag(event);
 			break;
 		}
 		case Event.ONMOUSEDOWN: {
 			// invoke a click event 
 			int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 			int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 			OnGenome g = overlappedInterval(event, 2);
 			if (g != null) {
 				if (clickHandler != null)
 					clickHandler.onClick(clientX, clientY, g);
 				event.preventDefault();
 			}
 			else if (dragStartPoint.isUndefined()) {
 				dragStartPoint.set(new DragPoint(clientX, clientY));
 				Style.cursor(canvas, Style.CURSOR_RESIZE_E);
 				event.preventDefault();
 			}
 
 			break;
 		}
 		case Event.ONMOUSEUP: {
 
 			resetDrag(event);
 			break;
 		}
 		}
 
 	}
 
 	private void resetDrag(Event event) {
 
 		int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 		int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 		if (dragStartPoint.isDefined() && trackWindow != null) {
 			DragPoint p = dragStartPoint.get();
 			int startDiff = trackWindow.calcGenomePosition(clientX) - trackWindow.calcGenomePosition(p.x);
 			if (startDiff != 0) {
 				int newStart = trackWindow.getStartOnGenome() - startDiff;
 				if (newStart < 1)
 					newStart = 1;
 				int newEnd = newStart + trackWindow.getWidth();
 				TrackWindow newWindow = trackWindow.newWindow(newStart, newEnd);
 				if (trackGroup != null)
 					trackGroup.setTrackWindow(newWindow);
 			}
 		}
 
 		dragStartPoint.reset();
 		event.preventDefault();
 
 		Style.cursor(canvas, Style.CURSOR_AUTO);
 	}
 
 	public static class DragPoint {
 		public final int x;
 		public final int y;
 
 		public DragPoint(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 
 	private Optional<DragPoint> dragStartPoint = new Optional<DragPoint>();
 
 	public void displayInfo(final int clientX, final int clientY, final OnGenome g) {
 		if (popupLabel == null)
 			popupLabel = new PopupInfo();
 
 		popupLabel.setLocus(g);
 
 		Timer timer = new Timer() {
 			@Override
 			public void run() {
 				popupLabel.removeFromParent();
 				if (popupLabel.locus == g) {
 					popupLabel.setPopupPosition(clientX + 10, clientY + 3);
 					popupLabel.update();
 					popupLabel.show();
 				}
 			}
 		};
 
 		timer.schedule(100);
 	}
 
 	public void setShowLabels(boolean show) {
 		intervalLayout.setKeepSpaceForLabels(show);
 	}
 
 	/**
 	 * compute the overlapped intervals for the mouse over event
 	 * 
 	 * @param event
 	 * @param xBorder
 	 * @return
 	 */
 	private OnGenome overlappedInterval(Event event, int xBorder) {
 
 		int x = drawPosition(getXOnCanvas(event));
 		int y = getYOnCanvas(event);
 
 		return intervalLayout.overlappedInterval(x, y, xBorder, geneHeight);
 	}
 
 	public int getXOnCanvas(Event event) {
 		int clientX = DOM.eventGetClientX(event);
 		return clientX - canvas.getAbsoluteLeft() + Window.getScrollLeft();
 	}
 
 	public int getYOnCanvas(Event event) {
 		int clientY = DOM.eventGetClientY(event);
 		return clientY - canvas.getAbsoluteTop() + Window.getScrollTop();
 	}
 
 	private void initWidget() {
 		panel.add(canvas, 0, 0);
 		basePanel.add(panel, 0, 0);
 		initWidget(basePanel);
 
 		sinkEvents(Event.ONMOUSEMOVE | Event.ONMOUSEOVER | Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEOUT);
 	}
 
 	public void setWindow(TrackWindow w) {
 		this.trackWindow = w;
 
 		reverse = w.isReverseStrand();
 		intervalLayout.setTrackWindow(w);
 	}
 
 	public void setTrackGroup(TrackGroup trackGroup) {
 		this.trackGroup = trackGroup;
 	}
 
 	public int pixelPositionOnWindow(int indexOnGenome) {
 		return trackWindow.calcXPositionOnWindow(indexOnGenome);
 	}
 
 	public void clear() {
 		clearExceptLayout();
 		intervalLayout.clear();
 	}
 
 	private void clearExceptLayout() {
 		canvas.clear();
 
 		if (popupLabel != null)
 			popupLabel.removeFromParent();
 
 		for (Widget w : readLabels) {
 			w.removeFromParent();
 		}
 		readLabels.clear();
 		basePanel.setWidgetPosition(panel, 0, 0);
 	}
 
 	@Override
 	public void setPixelSize(int width, int height) {
 		canvas.setCoordSize(width, height);
 		canvas.setPixelWidth(width);
 		canvas.setPixelHeight(height);
 		panel.setPixelSize(width, height);
 		basePanel.setPixelSize(width, height);
 	}
 
 	public static int width(int x1, int x2) {
 		return (x1 < x2) ? x2 - x1 : x1 - x2;
 	}
 
 	class ReadPainter implements OnGenomeDataVisitor {
 
 		private LocusLayout gl;
 		private int h = GWTGenomeCanvas.this.geneHeight + GWTGenomeCanvas.this.geneMargin;
 
 		public void setLayoutInfo(LocusLayout layout) {
 			this.gl = layout;
 			gl.scaleHeight(h);
 		}
 
 		public void visitGene(Gene g) {
 			int gx1 = pixelPositionOnWindow(g.getStart());
 			int gx2 = pixelPositionOnWindow(g.getEnd());
 
 			int geneWidth = gx2 - gx1;
 			if (geneWidth <= 10) {
 				draw(g, gl.getYOffset());
 			}
 			else {
 				CDS cds = g.getCDS().size() > 0 ? g.getCDS().get(0) : null;
 				draw(g, g.getExon(), cds, gl.getYOffset());
 			}
 
 			drawLabel(g);
 		}
 
 		private void drawLabel(OnGenome r) {
 			int gx1 = pixelPositionOnWindow(r.getStart());
 			int gx2 = pixelPositionOnWindow(r.getStart() + r.length());
 
 			if (intervalLayout.hasEnoughSpaceForLables()) {
 				String n = r.getName();
 				if (n != null) {
 					int textWidth = IntervalLayout.estimiateLabelWidth(r, geneHeight);
 
 					FixedWidthLabel label = new FixedWidthLabel(n, textWidth);
 					Style.fontSize(label, geneHeight);
 					Style.fontColor(label, getExonColorText(r));
 
 					Style.verticalAlign(label, "middle");
 
 					int yPos = gl.getYOffset() - 1;
 
 					if (gx1 - textWidth < 0) {
 						if (reverse) {
 							Style.textAlign(label, "right");
 							panel.add(label, drawPosition(gx2) - textWidth - 1, yPos);
 						}
 						else {
 							Style.textAlign(label, "left");
 							panel.add(label, drawPosition(gx2) + 1, yPos);
 						}
 					}
 					else {
 						if (reverse) {
 							Style.textAlign(label, "left");
 							panel.add(label, drawPosition(gx1) + 1, yPos);
 						}
 						else {
 							Style.textAlign(label, "right");
 							panel.add(label, drawPosition(gx1) - textWidth - 1, yPos);
 						}
 					}
 
 					readLabels.add(label);
 				}
 			}
 
 		}
 
 		public void visitInterval(Interval interval) {
 			draw(interval, gl.getYOffset());
 		}
 
 		public void visitRead(Read r) {
 			draw(r, gl.getYOffset());
 			drawLabel(r);
 		}
 
 		public void visitSAMRead(SAMRead r) {
 			draw(r, gl.getYOffset());
 			drawLabel(r);
 		}
 
 		public void visitSequence(ReferenceSequence referenceSequence) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 
 		}
 
 		public void visitGraph(GraphData graph) {
 			// TODO Auto-generated method stub
 
 		}
 
 	};
 
 	private class FindMaximumHeight extends OnGenomeDataVisitorBase {
 		int maxHeight = 1;
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			if (readCoverage.coverage == null)
 				return;
 
 			// set canvas size
 			for (int height : readCoverage.coverage) {
 				if (height > maxHeight)
 					maxHeight = height;
 			}
 		}
 	}
 
 	private class CoveragePainter extends OnGenomeDataVisitorBase {
 
 		private int heigtOfRead = 1;
 		private float scalingFactor = 1.0f;
 
 		public CoveragePainter(int heightOfRead, float scalingFactor) {
 			this.heigtOfRead = heightOfRead;
 			this.scalingFactor = scalingFactor;
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			canvas.saveContext();
 			canvas.setStrokeStyle(getColor("#FFFFFF", 0.0f));
 			canvas.setFillStyle(getColor("#6699CC", 0.6f));
 			canvas.setLineWidth(0.1f);
 			canvas.setLineCap("round");
 
 			canvas.saveContext();
 			canvas.beginPath();
 			canvas.moveTo(-0.5f, -1.0f);
 			for (int x = 0; x < readCoverage.pixelWidth; ++x) {
 				int h = readCoverage.coverage[x];
 				if (h <= 0) {
 					canvas.lineTo(x + 0.5f, -1.0f);
 					continue;
 				}
 
 				int y = (int) ((h * heigtOfRead) * scalingFactor);
 				canvas.lineTo(x + 0.5f, y + 0.5f);
 				canvas.stroke();
 			}
 			canvas.moveTo(readCoverage.pixelWidth + 0.5f, -1.0f);
 			canvas.fill();
 			canvas.restoreContext();
 
 			canvas.restoreContext();
 		}
 	}
 
 	private class RoughCoveragePainter extends OnGenomeDataVisitorBase {
 
 		private int heigtOfRead = 1;
 		private float scalingFactor = 1.0f;
 
 		public RoughCoveragePainter(int heightOfRead, float scalingFactor) {
 			this.heigtOfRead = heightOfRead;
 			this.scalingFactor = scalingFactor;
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			canvas.saveContext();
 			canvas.setStrokeStyle(getColor("#6699CC", 0.6f));
 			canvas.setLineWidth(1.0f);
 			canvas.setLineCap("round");
 
 			for (int x = 0; x < readCoverage.pixelWidth; ++x) {
 				int h = readCoverage.coverage[x];
 				if (h <= 0) {
 					continue;
 				}
 				int y = (int) ((h * heigtOfRead) * scalingFactor);
 				canvas.saveContext();
 				canvas.translate(x + 0.5f, 0);
 				canvas.beginPath();
 				canvas.moveTo(0, 0);
 				canvas.lineTo(0, y + 0.5f);
 				canvas.stroke();
 				canvas.restoreContext();
 			}
 
 			canvas.restoreContext();
 		}
 	}
 
 	public <T extends OnGenome> void drawBlock(List<T> block) {
 
 		// compute max height
 		FindMaximumHeight hFinder = new FindMaximumHeight();
 		for (OnGenome each : block) {
 			each.accept(hFinder);
 		}
 		int heightOfRead = hFinder.maxHeight > 30 ? 2 : DEFAULT_GENE_HEIGHT;
 
 		int canvasHeight = hFinder.maxHeight * heightOfRead;
 		float scalingFactor = 1.0f;
 
 		final int MAX_CANVAS_HEIGHT = 150;
 		if (canvasHeight > MAX_CANVAS_HEIGHT) {
 			scalingFactor = (float) (MAX_CANVAS_HEIGHT) / canvasHeight;
 			canvasHeight = MAX_CANVAS_HEIGHT;
 		}
 
 		setPixelSize(trackWindow.getPixelWidth(), canvasHeight);
 
 		// draw coverage
 		OnGenomeDataVisitor cPainter;
 		switch (coverageStyle) {
 		case SMOOTH:
 			cPainter = new CoveragePainter(heightOfRead, scalingFactor);
 			break;
 		case DEFAULT:
 		default:
 			cPainter = new RoughCoveragePainter(heightOfRead, scalingFactor);
 			break;
 		}
 		for (OnGenome each : block) {
 			each.accept(cPainter);
 		}
 	}
 
 	public void draw(List<OnGenome> locusList) {
 		layoutRead(locusList);
 		drawLayout();
 	}
 
 	private void drawLayout() {
 		final ReadPainter painter = new ReadPainter();
 
 		intervalLayout.depthFirstSearch(new PrioritySearchTree.Visitor<LocusLayout>() {
 			public void visit(LocusLayout gl) {
 				painter.setLayoutInfo(gl);
 				gl.getLocus().accept(painter);
 			}
 		});
 	}
 
 	private void layoutRead(List<OnGenome> readList) {
 
 		int maxOffset = intervalLayout.createLayout(readList, geneHeight);
 		if (maxOffset > 30)
 			geneHeight = 2;
 		else
 			geneHeight = DEFAULT_GENE_HEIGHT;
 
 		int h = geneHeight + geneMargin;
 		int height = (maxOffset + 1) * h;
 
 		setPixelSize(trackWindow.getPixelWidth(), height);
 	}
 
 	public static Color getGeneColor(Interval l) {
 		return getGeneColor(l, 1f);
 	}
 
 	public static Color getGeneColor(Interval l, float alpha) {
 		return getColor(getExonColorText(l), alpha);
 	}
 
 	public static Color getColor(String hex, float alpha) {
 		int r = Integer.parseInt(hex.substring(1, 3), 16);
 		int g = Integer.parseInt(hex.substring(3, 5), 16);
 		int b = Integer.parseInt(hex.substring(5, 7), 16);
 		return new Color(r, g, b, alpha);
 	}
 
 	private static String getExonColorText(OnGenome g) {
 		final String senseColor = "#d80067";
 		final String antiSenseColor = "#0067d8";
 		if (g instanceof Read) {
 			Read r = (Read) g;
 			if (r.getColor() == null) {
 				return r.isSense() ? senseColor : antiSenseColor;
 			}
 			else {
 				return r.getColor();
 			}
 		}
 		else
 			return senseColor;
 	}
 
 	public void draw(Gene gene, List<Exon> exonList, CDS cds, int yPosition) {
 		// assume that exonList are sorted
 
 		if (exonList.isEmpty()) {
 			Color c = getGeneColor(gene);
 			drawGeneRect(pixelPositionOnWindow(gene.getStart()), pixelPositionOnWindow(gene.getEnd()), yPosition, c, true);
 		}
 
 		for (Exon e : exonList) {
 			drawExon(gene, e, cds, yPosition);
 		}
 
 		canvas.saveContext();
 		canvas.setFillStyle(getGeneColor(gene, 0.7f));
 		canvas.setStrokeStyle(getGeneColor(gene, 0.7f));
 		canvas.setLineWidth(0.5f);
 
 		// draw the arrow between exons
 		boolean isSense = gene.isSense() ? !reverse : reverse;
 		double arrowHeight = geneHeight / 2.0 + 0.5;
 
 		for (int i = 0; i < exonList.size() - 1; i++) {
 			Exon prev = exonList.get(i);
 			Exon next = exonList.get(i + 1);
 
 			int x1 = pixelPositionOnWindow(prev.getEnd());
 			int x2 = pixelPositionOnWindow(next.getStart());
 			float yAxis = yPosition + (geneHeight / 2) + 0.5f;
 
 			canvas.saveContext();
 			canvas.beginPath();
 			canvas.moveTo(drawPosition(x1) + 0.5f, yAxis);
 			canvas.lineTo(drawPosition(x2) - 0.5f, yAxis);
 			canvas.stroke();
 			canvas.restoreContext();
 
 			for (int x = x1; x + 4 <= x2; x += 5) {
 				canvas.saveContext();
 				canvas.translate(drawPosition(x) + 2.0f, yPosition + arrowHeight);
 				if (!isSense)
 					canvas.rotate(Math.PI);
 				canvas.beginPath();
 				canvas.moveTo(-2.0f, -arrowHeight + 1.5f);
 				canvas.lineTo(1.5f, 0);
 				canvas.lineTo(-2.0f, arrowHeight - 1.5f);
 				canvas.stroke();
 				canvas.restoreContext();
 			}
 
 		}
 		canvas.restoreContext();
 
 	}
 
 	private int drawPosition(int x) {
 		if (reverse)
 			return (trackWindow.getPixelWidth() - x);
 		else
 			return x;
 	}
 
 	public static Color getExonColor(Gene g) {
 		return getGeneColor(g, 0.5f);
 	}
 
 	public static Color getCDSColor(Interval g) {
 		return getGeneColor(g);
 	}
 
 	public void draw(Interval gene, int yPosition) {
 		int gx = pixelPositionOnWindow(gene.getStart());
 		int gx2 = pixelPositionOnWindow(gene.getEnd());
 
 		drawGeneRect(gx, gx2, yPosition, getCDSColor(gene), true);
 	}
 
 	public void draw(Gene gene, int yPosition) {
 		int gx = pixelPositionOnWindow(gene.getStart());
 		int gx2 = pixelPositionOnWindow(gene.getEnd());
 
 		drawGeneRect(gx, gx2, yPosition, getGeneColor(gene), true);
 	}
 
 	public void drawExon(Gene gene, Exon exon, CDS cds, int yPosition) {
 		int ex = pixelPositionOnWindow(exon.getStart());
 		int ex2 = pixelPositionOnWindow(exon.getEnd());
 
 		drawGeneRect(ex, ex2, yPosition, getExonColor(gene), true);
 
 		// draw CDS
 		if (cds != null) {
 			int cx = pixelPositionOnWindow(cds.getStart());
 			int cx2 = pixelPositionOnWindow(cds.getEnd());
 
 			if (cx <= cx2) {
 				if (ex <= cx2 && ex2 >= cx) {
 					int cdsStart = (ex <= cx) ? cx : ex;
 					int cdsEnd = (ex2 <= cx2) ? ex2 : cx2;
 
 					drawGeneRect(cdsStart, cdsEnd, yPosition, getCDSColor(gene), false);
 				}
 			}
 
 		}
 
 	}
 
 	public void drawGeneRect(int x1, int x2, int y, Color c, boolean drawShadow) {
 
 		float boxWidth = x2 - x1 - 0.5f;
 		if (boxWidth <= 0)
 			boxWidth = 1f;
 
 		canvas.saveContext();
 		double drawX = drawPosition(reverse ? x2 : x1);
 		canvas.setFillStyle(Color.WHITE);
 		canvas.fillRect(drawX, y, boxWidth, geneHeight);
 		canvas.setFillStyle(c);
 		canvas.fillRect(drawX, y, boxWidth, geneHeight);
 		canvas.restoreContext();
 
 		if (drawShadow) {
 			canvas.saveContext();
 			canvas.setStrokeStyle(new Color(30, 30, 30, 0.6f));
 			double shadowStart = drawPosition(reverse ? x2 : x1);
 			canvas.translate(shadowStart, y);
 			canvas.beginPath();
 			canvas.moveTo(1.5f, geneHeight + 0.5f);
 			canvas.lineTo(boxWidth + 0.5f, geneHeight + 0.5f);
 			canvas.lineTo(boxWidth + 0.5f, 0.5f);
 			canvas.stroke();
 			canvas.restoreContext();
 		}
 
 	}
 
 }
