 package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.graph;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
 
 public class PCGraph extends JPanel implements Observer {
 
 	public static final int X_MARGIN = 50;
 	public static final int Y_MARGIN = 50;
 	public static final int TITLE_MARGIN = 20;
 	public static final int LABEL_MARGIN = 5;
 
 	public static final String[] AXIS_TITLE = { "SUPPORT", "CONFIDENCE", "NOD",
 			"NOR", "NOF", "LBM", "LAM", "START", "END" };
 
 	public static final Cursor DEFAULT_CURSOR = new Cursor(
 			Cursor.DEFAULT_CURSOR);
 	public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
 	public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
 
 	public static final Color METRIC_GRAPH_BACKGROUND_COLOR = Color.white;
 	public static final Color METRIC_GRAPH_STATUS_COLOR = Color.black;
 	public static final Color METRIC_GRAPH_AXIS_COLOR = Color.black;
 	public static final Color METRICS_AXIS_TITLE_COLOR = Color.black;
 	public static final Color METRICS_AXIS_LABEL_COLOR = Color.black;
 	public static final Color METRICS_FILTER_AREA_ON_COLOR = new Color(90, 90,
 			180, 63);
 	public static final Color METRICS_FILTER_AREA_OFF_COLOR = new Color(90, 90,
 			180, 20);
 	public static final Color METRICS_UN_SELECTED_DATA_COLOR = Color.lightGray;
 	public static final Color METRICS_SELECTED_DATA_COLOR = Color.red;
 
 	class Filter implements MouseMotionListener, MouseListener {
 
 		final private int[] x;
 		final private int[] y;
 		final private double[] yRate;
 
 		private Point pressedPoint;
 
 		private int draggingPolygonalIndex;
 
 		Filter() {
 			this.x = new int[2 * AXIS_TITLE.length];
 			this.y = new int[2 * AXIS_TITLE.length];
 			this.yRate = new double[2 * AXIS_TITLE.length];
 
 			this.reset();
 		}
 
 		@Override
 		public void mouseClicked(final MouseEvent evt) {
 		}
 
 		@Override
 		public void mouseEntered(final MouseEvent evt) {
 		}
 
 		@Override
 		public void mouseExited(final MouseEvent evt) {
 		}
 
 		@Override
 		public void mousePressed(final MouseEvent evt) {
 
 			if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
 				this.pressedPoint = evt.getPoint();
 				this.draggingPolygonalIndex = this
 						.getPolygonalIndex(this.pressedPoint);
 				if (this.draggingPolygonalIndex != -1)
 					PCGraph.this.setCursor(HAND_CURSOR);
 			}
 		}
 
 		@Override
 		public void mouseReleased(final MouseEvent evt) {
 
 			PCGraph.this.setCursor(WAIT_CURSOR);
 
 			if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
 
 				if (this.draggingPolygonalIndex != -1) {
 
 					this.update();
 					this.draggingPolygonalIndex = -1;
 
 					ObservedModificationPatterns.getInstance(MPLABEL.SELECTED)
 							.clear(PCGraph.this);
 				}
 			}
 
 			PCGraph.this.setCursor(DEFAULT_CURSOR);
 		}
 
 		@Override
 		public void mouseDragged(final MouseEvent evt) {
 
 			if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
 
 				if (this.draggingPolygonalIndex != -1) {
 
 					int deltaY = this.pressedPoint.y - evt.getY();
 					double deltaRate = ((double) deltaY)
 							/ ((double) PCGraph.this.getYSpace());
 
 					this.yRate[this.draggingPolygonalIndex] += deltaRate;
 
 					if (this.yRate[this.draggingPolygonalIndex] < 0.0)
 						this.yRate[this.draggingPolygonalIndex] = 0.0;
 					if (this.yRate[this.draggingPolygonalIndex] > 1.0)
 						this.yRate[this.draggingPolygonalIndex] = 1.0;
 
 					int otherSideIndex = 2 * AXIS_TITLE.length - 1
 							- this.draggingPolygonalIndex;
 
 					if (this.draggingPolygonalIndex < otherSideIndex) {
 
 						if (this.yRate[this.draggingPolygonalIndex] < this.yRate[otherSideIndex])
 							this.yRate[this.draggingPolygonalIndex] = this.yRate[otherSideIndex];
 
 					} else {
 
 						if (this.yRate[this.draggingPolygonalIndex] > this.yRate[otherSideIndex])
 							this.yRate[this.draggingPolygonalIndex] = this.yRate[otherSideIndex];
 					}
 
 					this.pressedPoint = evt.getPoint();
 
 					PCGraph.this.repaint();
 				}
 			}
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent evt) {
 		}
 
 		private void reset() {
 
 			// reset filter range
 			for (int i = 0; i < AXIS_TITLE.length; i++) {
 
 				this.x[i] = X_MARGIN + i * PCGraph.this.getXInterval();
 				this.x[2 * AXIS_TITLE.length - 1 - i] = X_MARGIN + i
 						* PCGraph.this.getXInterval();
 
 				this.y[i] = PCGraph.this.getReversedY(Y_MARGIN);
 				this.y[2 * AXIS_TITLE.length - 1 - i] = PCGraph.this
 						.getReversedY(PCGraph.this.getHeight() - Y_MARGIN);
 
 				this.yRate[i] = 1.0;
 				this.yRate[2 * AXIS_TITLE.length - 1 - i] = 0.0;
 			}
 
 			this.update();
 		}
 
 		void update() {
 
 			ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).clear(
 					PCGraph.this);
 
 			final Set<ModificationPattern> inPatterns = new HashSet<ModificationPattern>();
 			final Set<ModificationPattern> outPatterns = new HashSet<ModificationPattern>();
 
 			for (final ModificationPattern pattern : ObservedModificationPatterns
 					.getInstance(MPLABEL.ALL).get()) {
 
 				if (pattern.support > PCGraph.this.maxSUPPORT * this.yRate[0]) {
 					outPatterns.add(pattern);
 				} else if (pattern.confidence > PCGraph.this.maxCONFIDENCE
 						* this.yRate[1]) {
 					outPatterns.add(pattern);
 				} else if (pattern.getNOD() > PCGraph.this.maxNOD
 						* this.yRate[2]) {
 					outPatterns.add(pattern);
 				} else if (pattern.getNOR() > PCGraph.this.maxNOR
 						* this.yRate[3]) {
 					outPatterns.add(pattern);
 				} else if (pattern.getNOF() > PCGraph.this.maxNOF
 						* this.yRate[4]) {
 					outPatterns.add(pattern);
 				} else if (pattern.getLBM() > PCGraph.this.maxLBM
 						* this.yRate[5]) {
 					outPatterns.add(pattern);
 				} else if (pattern.getLAM() > PCGraph.this.maxLAM
 						* this.yRate[6]) {
 					outPatterns.add(pattern);
				} else if (pattern.getRevisions().first().number > PCGraph.this.maxRevision.number
 						* this.yRate[7]) {
 					outPatterns.add(pattern);
				} else if (pattern.getRevisions().last().number > PCGraph.this.maxRevision.number
 						* this.yRate[8]) {
 					outPatterns.add(pattern);
 				}
 
 				else if (PCGraph.this.maxSUPPORT * this.yRate[17] > pattern.support) {
 					outPatterns.add(pattern);
 				} else if (PCGraph.this.maxCONFIDENCE * this.yRate[16] > pattern.confidence) {
 					outPatterns.add(pattern);
 				} else if (PCGraph.this.maxNOD * this.yRate[15] > pattern
 						.getNOD()) {
 					outPatterns.add(pattern);
 				} else if (PCGraph.this.maxNOR * this.yRate[14] > pattern
 						.getNOR()) {
 					outPatterns.add(pattern);
 				} else if (PCGraph.this.maxNOF * this.yRate[13] > pattern
 						.getNOF()) {
 					outPatterns.add(pattern);
 				} else if (PCGraph.this.maxLBM * this.yRate[12] > pattern
 						.getLBM()) {
 					outPatterns.add(pattern);
 				} else if (PCGraph.this.maxLAM * this.yRate[11] > pattern
 						.getLAM()) {
 					outPatterns.add(pattern);
				} else if (PCGraph.this.maxRevision.number * this.yRate[10] > pattern
						.getRevisions().first().number) {
 					outPatterns.add(pattern);
				} else if (PCGraph.this.maxRevision.number * this.yRate[9] > pattern
						.getRevisions().last().number) {
 					outPatterns.add(pattern);
 				} else {
 					inPatterns.add(pattern);
 				}
 			}
 
 			ObservedModificationPatterns.getInstance(MPLABEL.FILTERED).setAll(
 					inPatterns, PCGraph.this);
 			ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).clear(
 					PCGraph.this);
 			PCGraph.this.repaint();
 		}
 
 		void draw(final Graphics g) {
 
 			for (int i = 0; i < AXIS_TITLE.length; i++) {
 
 				this.x[i] = X_MARGIN + i * PCGraph.this.getXInterval();
 				this.x[2 * AXIS_TITLE.length - 1 - i] = X_MARGIN + i
 						* PCGraph.this.getXInterval();
 
 				this.y[i] = PCGraph.this
 						.getReversedY((int) (Y_MARGIN + PCGraph.this
 								.getYSpace() * this.yRate[i]));
 				this.y[2 * AXIS_TITLE.length - 1 - i] = PCGraph.this
 						.getReversedY((int) (Y_MARGIN + PCGraph.this
 								.getYSpace()
 								* this.yRate[2 * AXIS_TITLE.length - 1 - i]));
 			}
 
 			g.setColor(METRICS_FILTER_AREA_ON_COLOR);
 			g.fillPolygon(this.x, this.y, 2 * AXIS_TITLE.length);
 		}
 
 		private int getPolygonalIndex(final Point p) {
 
 			final int validAreaWidth = PCGraph.this.getWidth()
 					/ (2 * AXIS_TITLE.length);
 			final int validAreaHeight = (int) (PCGraph.this.getYSpace() * 0.1);
 
 			final int axisIndex = getNearestAxisIndex(p.x);
 
 			final int bottomY = Y_MARGIN
 					+ (int) (PCGraph.this.getYSpace() * this.yRate[axisIndex]);
 			final int topY = Y_MARGIN
 					+ (int) (PCGraph.this.getYSpace() * this.yRate[2
 							* AXIS_TITLE.length - 1 - axisIndex]);
 
 			Rectangle topValidArea = new Rectangle(this.x[axisIndex]
 					- validAreaWidth / 2, PCGraph.this.getReversedY(topY)
 					- validAreaHeight / 2, validAreaWidth, validAreaHeight);
 			Rectangle bottomValidArea = new Rectangle(this.x[axisIndex]
 					- validAreaWidth / 2, PCGraph.this.getReversedY(bottomY)
 					- validAreaHeight / 2, validAreaWidth, validAreaHeight);
 
 			boolean topContains = topValidArea.contains(p);
 			boolean bottomContains = bottomValidArea.contains(p);
 
 			if (topContains && bottomContains) {
 
 				int halfY = (topY + bottomY) / 2;
 				if (halfY > p.y)
 					bottomContains = false;
 				else
 					topContains = false;
 			}
 
 			if (bottomContains)
 				return axisIndex;
 			else if (topContains)
 				return 2 * AXIS_TITLE.length - 1 - axisIndex;
 			else
 				return -1;
 		}
 
 		private int getNearestAxisIndex(int x) {
 
 			double x_interval = ((double) PCGraph.this.getWidth())
 					/ ((double) AXIS_TITLE.length);
 			int index = (int) (x / x_interval);
 
 			return index;
 		}
 	}
 
 	final private Filter filter;
 
 	private int maxSUPPORT;
 	private float maxCONFIDENCE;
 	private int maxNOD;
 	private int maxNOR;
 	private int maxNOF;
 	private int maxLBM;
 	private int maxLAM;
 	private Revision minRevision;
 	private Revision maxRevision;
 
 	public PCGraph() {
 
 		this.filter = new Filter();
 		this.addMouseListener(this.filter);
 		this.addMouseMotionListener(this.filter);
 
 		this.resetMaxValues();
 	}
 
 	@Override
 	public void paint(final Graphics g) {
 
 		this.drawBackGround(g);
 		this.drawStatus(g);
 		this.drawAxis(g);
 		this.drawModificationPatterns(g);
 		this.drawLabel(g);
 		this.filter.draw(g);
 
 		this.setBorder(new TitledBorder(new LineBorder(Color.black),
 				"Parallel Coordinate Graph of Modification Patterns"));
 	}
 
 	@Override
 	public void update(final Observable o, final Object arg) {
 
 		if (o instanceof ObservedModificationPatterns) {
 			final ObservedModificationPatterns patterns = (ObservedModificationPatterns) o;
 			if (patterns.label.equals(MPLABEL.ALL)) {
 				this.resetMaxValues();
 				this.filter.reset();
 				this.filter.update();
 			}
 		}
 	}
 
 	public void init() {
 		this.reset();
 	}
 
 	public void reset() {
 		this.filter.reset();
 	}
 
 	private int getYSpace() {
 		return this.getHeight() - 2 * Y_MARGIN;
 	}
 
 	private int getXInterval() {
 		return (this.getWidth() - 2 * X_MARGIN) / (AXIS_TITLE.length - 1);
 	}
 
 	private void resetMaxValues() {
 		this.maxSUPPORT = 1;
 		this.maxCONFIDENCE = 1.0f;
 		this.maxNOD = 1;
 		this.maxNOR = 1;
 		this.maxNOF = 1;
 		this.maxLBM = 1;
 		this.maxLAM = 1;
 		this.minRevision = new Revision(Long.MAX_VALUE, "", "");
 		this.maxRevision = new Revision(Long.MIN_VALUE, "", "");
 		for (final ModificationPattern pattern : ObservedModificationPatterns
 				.getInstance(MPLABEL.ALL).get()) {
 			if (this.maxSUPPORT < pattern.support) {
 				this.maxSUPPORT = pattern.support;
 			}
 			if (this.maxNOD < pattern.getNOD()) {
 				this.maxNOD = pattern.getNOD();
 			}
 			if (this.maxNOR < pattern.getNOR()) {
 				this.maxNOR = pattern.getNOR();
 			}
 			if (this.maxNOF < pattern.getNOF()) {
 				this.maxNOF = pattern.getNOF();
 			}
 			if (this.maxLBM < pattern.getLBM()) {
 				this.maxLBM = pattern.getLBM();
 			}
 			if (this.maxLAM < pattern.getLAM()) {
 				this.maxLAM = pattern.getLAM();
 			}
 			if (this.minRevision.number > pattern.getRevisions().first().number) {
 				this.minRevision = pattern.getRevisions().first();
 			}
 			if (this.maxRevision.number < pattern.getRevisions().first().number) {
 				this.maxRevision = pattern.getRevisions().first();
 			}
 			if (this.minRevision.number > pattern.getRevisions().last().number) {
 				this.minRevision = pattern.getRevisions().last();
 			}
 			if (this.maxRevision.number < pattern.getRevisions().last().number) {
 				this.maxRevision = pattern.getRevisions().last();
 			}
 		}
 	}
 
 	private void drawBackGround(final Graphics g) {
 
 		final int width = this.getWidth();
 		final int height = this.getHeight();
 
 		g.setColor(METRIC_GRAPH_BACKGROUND_COLOR);
 		g.fillRect(0, 0, width, height);
 	}
 
 	private void drawStatus(final Graphics g) {
 
 		final int allNumber = ObservedModificationPatterns
 				.getInstance(MPLABEL.ALL).get().size();
 		final int filteredNumber = ObservedModificationPatterns
 				.getInstance(MPLABEL.FILTERED).get().size();
 
 		g.setColor(METRIC_GRAPH_STATUS_COLOR);
 		g.drawString(filteredNumber + "/" + allNumber + " are selected", 20, 25);
 	}
 
 	private void drawAxis(final Graphics g) {
 
 		int width = this.getWidth();
 		int height = this.getHeight();
 		int y_title = TITLE_MARGIN;
 		int y_line_start = X_MARGIN;
 		int y_line_end = height - X_MARGIN;
 		double data_space = (width - (2 * X_MARGIN)) / (AXIS_TITLE.length - 1);
 
 		for (int i = 0; i < AXIS_TITLE.length; i++) {
 			int x_location = Y_MARGIN + (int) (data_space * i);
 			g.setColor(METRIC_GRAPH_AXIS_COLOR);
 			g.drawLine(x_location, this.getReversedY(y_line_start), x_location,
 					this.getReversedY(y_line_end));
 			g.setColor(METRICS_AXIS_TITLE_COLOR);
 			g.drawString(AXIS_TITLE[i], x_location - 10,
 					this.getReversedY(y_title));
 		}
 	}
 
 	private void drawModificationPatterns(final Graphics g) {
 
 		g.setColor(METRICS_UN_SELECTED_DATA_COLOR);
 		for (final ModificationPattern pattern : ObservedModificationPatterns
 				.getInstance(MPLABEL.ALL).get()) {
 			this.drawModificationPattern(g, pattern);
 		}
 
 		g.setColor(METRICS_SELECTED_DATA_COLOR);
 		for (final ModificationPattern pattern : ObservedModificationPatterns
 				.getInstance(MPLABEL.FILTERED).get()) {
 			this.drawModificationPattern(g, pattern);
 		}
 	}
 
 	private void drawModificationPattern(final Graphics g,
 			final ModificationPattern pattern) {
 
 		final int[] y = new int[AXIS_TITLE.length];
 		y[0] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* (((double) pattern.support) / ((double) this.maxSUPPORT))));
 		y[1] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* pattern.confidence));
 		y[2] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* (((double) pattern.getNOD()) / ((double) this.maxNOD))));
 		y[3] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* (((double) pattern.getNOR()) / ((double) this.maxNOR))));
 		y[4] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* (((double) pattern.getNOF()) / ((double) this.maxNOF))));
 		y[5] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* (((double) pattern.getLBM()) / ((double) this.maxLBM))));
 		y[6] = this.getReversedY((int) (Y_MARGIN + this.getYSpace()
 				* (((double) pattern.getLAM()) / ((double) this.maxLAM))));
		y[7] = this
				.getReversedY((int) (Y_MARGIN + this.getYSpace()
						* (this.maxRevision.number - pattern.getRevisions()
								.first().number)
						/ (this.maxRevision.number - this.minRevision.number + 1)));
		y[8] = this
				.getReversedY((int) (Y_MARGIN + this.getYSpace()
						* (this.maxRevision.number - pattern.getRevisions()
								.first().number)
						/ (this.maxRevision.number - this.minRevision.number + 1)));
 		// get x axis plot location
 		final int[] x = new int[AXIS_TITLE.length];
 		int data_space = (this.getWidth() - 2 * X_MARGIN)
 				/ (AXIS_TITLE.length - 1);
 		for (int i = 0; i < AXIS_TITLE.length; i++) {
 			x[i] = X_MARGIN + i * data_space;
 		}
 
 		g.drawPolyline(x, y, AXIS_TITLE.length);
 	}
 
 	private void drawLabel(final Graphics g) {
 
 		// this value is space between metrics values
 		// int data_space = this.getYInterval();
 		final int data_space = this.getXInterval();
 
 		this.drawSUPPORTLabel(g);
 		this.drawCONFIDENCELabel(g, data_space);
 		this.drawNODLabel(g, data_space);
 		this.drawNORLabel(g, data_space);
 		this.drawNOFLabel(g, data_space);
 		this.drawLBMLabel(g, data_space);
 		this.drawLAMLabel(g, data_space);
 		this.drawSTARTLabel(g, data_space);
 		this.drawENDLabel(g, data_space);
 	}
 
 	private void drawSUPPORTLabel(final Graphics g) {
 
 		final int x_RAD = X_MARGIN + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString(Integer.toString(0), x_RAD, this.getReversedY(Y_MARGIN));
 		if (1 < this.maxSUPPORT) {
 			g.drawString(Integer.toString(this.maxSUPPORT / 2), x_RAD,
 					this.getReversedY(Y_MARGIN + HIGHT / 2));
 		}
 		g.drawString(Integer.toString(this.maxSUPPORT), x_RAD,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawCONFIDENCELabel(final Graphics g, final int data_space) {
 
 		final int x_LEN = X_MARGIN + data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString("0.0", x_LEN, this.getReversedY(Y_MARGIN));
 		g.drawString("0.25", x_LEN, this.getReversedY(Y_MARGIN + HIGHT / 4));
 		g.drawString("0.5", x_LEN, this.getReversedY(Y_MARGIN + HIGHT / 2));
 		g.drawString("0.75", x_LEN, this.getReversedY(Y_MARGIN + HIGHT * 3 / 4));
 		g.drawString("1.0", x_LEN, this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawNODLabel(final Graphics g, final int data_space) {
 
 		final int x_RNR = X_MARGIN + 2 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString("0", x_RNR, this.getReversedY(Y_MARGIN));
 		if (1 < this.maxNOD) {
 			g.drawString(Integer.toString(this.maxNOD / 2), x_RNR,
 					this.getReversedY(Y_MARGIN + HIGHT / 2));
 		}
 		g.drawString(Integer.toString(this.maxNOD), x_RNR,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawNORLabel(final Graphics g, final int data_space) {
 
 		final int x_RNR = X_MARGIN + 3 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString("0", x_RNR, this.getReversedY(Y_MARGIN));
 		if (1 < this.maxNOR) {
 			g.drawString(Integer.toString(this.maxNOR / 2), x_RNR,
 					this.getReversedY(Y_MARGIN + HIGHT / 2));
 		}
 		g.drawString(Integer.toString(this.maxNOR), x_RNR,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawNOFLabel(final Graphics g, final int data_space) {
 
 		final int x_NIF = X_MARGIN + 4 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString("0", x_NIF, this.getReversedY(Y_MARGIN));
 		if (1 < this.maxNOF) {
 			g.drawString(Integer.toString(this.maxNOF / 2), x_NIF,
 					this.getReversedY(Y_MARGIN + HIGHT / 2));
 		}
 		g.drawString(Integer.toString(this.maxNOF), x_NIF,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawLBMLabel(final Graphics g, final int data_space) {
 
 		final int x_NIF = X_MARGIN + 5 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString("0", x_NIF, this.getReversedY(Y_MARGIN));
 		if (1 < this.maxLBM) {
 			g.drawString(Integer.toString(this.maxLBM / 2), x_NIF,
 					this.getReversedY(Y_MARGIN + HIGHT / 2));
 		}
 		g.drawString(Integer.toString(this.maxLBM), x_NIF,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawLAMLabel(final Graphics g, final int data_space) {
 
 		final int x_NIF = X_MARGIN + 6 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString("0", x_NIF, this.getReversedY(Y_MARGIN));
 		if (1 < this.maxLAM) {
 			g.drawString(Integer.toString(this.maxLAM / 2), x_NIF,
 					this.getReversedY(Y_MARGIN + HIGHT / 2));
 		}
 		g.drawString(Integer.toString(this.maxLAM), x_NIF,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawSTARTLabel(final Graphics g, final int data_space) {
 
 		final int x_START = X_MARGIN + 7 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString(StringUtility.removeTime(this.minRevision.date), x_START,
 				this.getReversedY(Y_MARGIN));
 		g.drawString(StringUtility.removeTime(this.maxRevision.date), x_START,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private void drawENDLabel(final Graphics g, final int data_space) {
 
 		final int x_END = X_MARGIN + 8 * data_space + LABEL_MARGIN;
 		final int HIGHT = this.getYSpace();
 
 		g.setColor(METRICS_AXIS_LABEL_COLOR);
 		g.drawString(StringUtility.removeTime(this.minRevision.date), x_END,
 				this.getReversedY(Y_MARGIN));
 		g.drawString(StringUtility.removeTime(this.maxRevision.date), x_END,
 				this.getReversedY(Y_MARGIN + HIGHT));
 	}
 
 	private int getReversedY(int y) {
 		return this.getHeight() - y;
 	}
 }
