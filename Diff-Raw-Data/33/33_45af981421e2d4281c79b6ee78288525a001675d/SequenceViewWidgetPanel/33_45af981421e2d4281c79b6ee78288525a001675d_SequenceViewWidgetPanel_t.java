 package org.geworkbench.util.sequences;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.TreeSet;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JViewport;
 import javax.swing.SwingUtilities;
 
 import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
 import org.geworkbench.util.patterns.PatternLocations;
 import org.geworkbench.util.patterns.PatternOperations;
 import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;
 
 /**
  *
  * This panel is the central part of SequenceViewWidegt and display the
  * sequences as either lines or full sequences of letters. It is only used in
  * SequenceViewWidget.
  *
  * @version $Id$
  */
 public class SequenceViewWidgetPanel extends JPanel {
 
 	private static final long serialVersionUID = 7202257250696337753L;
 
 	private int xOff = 80;
 	private final int yOff = 20;
 
 	private final int yStep = 14;
 	private double scale = 1.0;
 	private int selected = 0;
 	private int maxSeqLen = 1;
 
 	protected DSSequenceSet<? extends DSSequence> sequenceDB = null;
 	private HashMap<CSSequence, PatternSequenceDisplayUtil> sequencePatternmatches;
 
 	protected boolean lineView;
 	protected boolean singleSequenceView;
 	private final static Color SEQUENCEBACKGROUDCOLOR = Color.BLACK;
 	private final static Color DRECTIONCOLOR = Color.RED;
 	private double yBasescale;
 	private int xBaseCols;
 	private int[] eachSeqStartRowNum;
 	private double xBasescale;
 	protected int seqXclickPoint = 0;
 	private DSSequence selectedSequence;
 	private JPopupMenu itemListPopup = new JPopupMenu();
 
 	/* The only constructor. */
 	public SequenceViewWidgetPanel() {
 		try {
 			itemListPopup = new JPopupMenu();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void addMenuItem(JMenuItem item) {
 		itemListPopup.add(item);
 		repaint();
 	}
 
 	/**
 	 * New Initialization method. It should be used as a main entry point.
 	 * Others initialization method should be disabled or replaced.
 	 *
 	 * @param patternSeqMatches
 	 *            HashMap
 	 * @param seqDB
 	 *            DSSequenceSet
 	 * @param isLineView
 	 *            boolean
 	 */
 	public void initialize(
 			HashMap<CSSequence, PatternSequenceDisplayUtil> patternSeqMatches,
 			DSSequenceSet<? extends DSSequence> seqDB, boolean isLineView) {
 		sequencePatternmatches = patternSeqMatches;
 		sequenceDB = seqDB;
 		lineView = isLineView;
 		// keep the original xOff/labelLength ratio: xOff=80 -> maxDisplayChars=12
 		xOff = getMaxLabLen() * 20 / 3;
 		repaint();
 
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		if (sequenceDB == null) {
 			g.clearRect(0, 0, getWidth(), getHeight());
 			return;
 		}
 
 		if (lineView) {
 			if (!singleSequenceView || selected >= sequenceDB.size()) {
 				paintText(g);
 			} else {
 				paintSingleSequence(g);
 			}
 		} else {
 			paintFullView(g);
 
 		}
 	}
 
 	private void paintFullView(Graphics g) {
 		singleSequenceView = false; // make sure when the view shifts, the
 									// singlesequenceview is not selected.
 		int rowId = 0;
 		double y = yOff + 3;
 		double xscale = 0.1;
 		double yscale = 0.1;
 		int cols = 1;
 		boolean setupScale = true;
 		eachSeqStartRowNum = new int[sequenceDB.size()];
 		int seqId = 0;
 		Font f = new Font("Courier New", Font.PLAIN, 11);
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		FontMetrics fm = g.getFontMetrics(f);
 		g.setFont(f);
 		for (DSSequence theone : sequenceDB) {
 			if (theone == null)
 				continue;
 
 			String asc = theone.getSequence();
 			Rectangle2D r2d = fm.getStringBounds(asc, g);
 			int width = this.getWidth();
 
 			// Set up scales.
 			if (setupScale) {
 				xscale = (r2d.getWidth() + 3) / (double) (asc.length());
 				yscale = 1.3 * r2d.getHeight();
 				yBasescale = yscale;
 				cols = (int) (width / xscale) - 8;
 				xBaseCols = cols;
 				xBasescale = xscale;
 				setupScale = false;
 			}
 
 			String lab = theone.getLabel();
 			g.setColor(SEQUENCEBACKGROUDCOLOR);
 			// if (lab.length() > 10) {
 			// g.drawString(lab.substring(0, 10), 2, y + 3);
 			// }
 			// else {
 			g.drawString(lab, 2, (int) (y));
 			// }
 			y += 1 * yscale;
 			int begin = 0 - cols;
 			int end = 0;
 			eachSeqStartRowNum[seqId] = rowId;
 
 			while (end < asc.length()) {
 				rowId++;
 
 				begin = end;
 				end += cols;
 				String onepiece = "";
 				if (end > asc.length()) {
 					onepiece = asc.substring(begin, asc.length());
 				} else {
 					onepiece = asc.substring(begin, end);
 
 				}
 				g.setColor(SEQUENCEBACKGROUDCOLOR);
 				g.drawString(onepiece, (int) (6 * xscale), (int) y);
 				y += 1 * yscale;
 			}
 			rowId++;
 
 			// don't skip counting seqId if null shows up
 			if (sequencePatternmatches == null) {
 				seqId++;
 				continue;
 			}
 			PatternSequenceDisplayUtil psd = sequencePatternmatches.get(theone);
 			if (psd == null) {
 				seqId++;
 				continue;
 			}
 
 			TreeSet<PatternLocations> patternsPerSequence = psd.getTreeSet();
 			if (patternsPerSequence == null) {
 				seqId++;
 				continue;
 			}
 
 			for (PatternLocations pl : patternsPerSequence) {
 				CSSeqRegistration reg = pl.getRegistration();
 				if (reg == null)
 					continue;
 
 				if (pl.getPatternType().equals(PatternLocations.DEFAULTTYPE)) {
 					drawPattern(g, theone, reg.x1, xscale, yscale,
 							eachSeqStartRowNum[seqId], cols,
 							PatternOperations.getPatternColor(pl
 									.getIdForDisplay()), pl);
 
 				} else if (pl.getPatternType().equals(PatternLocations.TFTYPE)) {
 					drawPattern(g, theone, reg.x1, Math.abs(reg.x1 - reg.x2),
 							xscale, yscale, eachSeqStartRowNum[seqId], cols,
 							PatternOperations.getPatternColor(pl
 									.getIdForDisplay()), reg.strand);
 
 				}
 			}
 			seqId++;
 		} // end processing sequences.
 
 		int maxY = (int) y + yOff;
 		setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
 		revalidate();
 	}
 
 	private void paintSingleSequence(Graphics g) {
 		selected = Math.min(selected, sequenceDB.size() - 1);
 		DSSequence theone = sequenceDB.getSequence(selected);
 		int rowId = 0;
 		int y = yOff;
 		if (theone == null)
 			return;
 
 		Font f = new Font("Courier New", Font.PLAIN, 11);
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		FontMetrics fm = g.getFontMetrics(f);
 		String asc = theone.getSequence();
 		Rectangle2D r2d = fm.getStringBounds(asc, g);
 		double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
 		double yscale = 1.3 * r2d.getHeight();
 		int width = this.getWidth();
 		int cols = (int) (width / xscale) - 8;
 		yBasescale = yscale;
 		xBasescale = xscale;
 		xBaseCols = cols;
 		g.setFont(f);
 
 		String lab = theone.getLabel();
 		y += (int) (rowId * yscale);
 		g.setColor(SEQUENCEBACKGROUDCOLOR);
 
 		g.drawString(lab, 2, y + 3);
 
 		int x0 = (int) (lab.length() * xscale);
 		int x = x0 + (int) (theone.length() * scale);
 
 		g.drawLine(x0, y, x, y);
 
 		int begin = 0 - cols;
 		int end = 0;
 
 		while (end < asc.length()) {
 			rowId++;
 			y = yOff + (int) (rowId * yscale);
 
 			begin = end;
 			end += cols;
 			String onepiece = "";
 			if (end > asc.length()) {
 				onepiece = asc.substring(begin, asc.length());
 			} else {
 				onepiece = asc.substring(begin, end);
 
 			}
 			g.drawString(onepiece, (int) (6 * xscale), y + 3);
 		}
 
 		if (sequencePatternmatches == null) {
 			setPreferredSize(new Dimension(this.getWidth() - yOff, y + yOff));
 			revalidate();
 			return;
 		}
 
 		PatternSequenceDisplayUtil psd = sequencePatternmatches.get(theone);
 		if (psd == null) {
 			setPreferredSize(new Dimension(this.getWidth() - yOff, y + yOff));
 			revalidate();
 			return;
 
 		}
 
 		TreeSet<PatternLocations> patternsPerSequence = psd.getTreeSet();
 
 		if (patternsPerSequence == null) {
 			setPreferredSize(new Dimension(this.getWidth() - yOff, y + yOff));
 			revalidate();
 			return;
 		}
 
 		for (PatternLocations pl : patternsPerSequence) {
 			CSSeqRegistration reg = pl.getRegistration();
 			if (reg == null)
 				continue;
 
 			if (pl.getPatternType().equals(PatternLocations.DEFAULTTYPE)) {
 				drawPattern(
 						g,
 						theone,
 						reg.x1,
 						xscale,
 						yscale,
 						0,
 						cols,
 						PatternOperations.getPatternColor(pl.getIdForDisplay()),
 						pl);
 
 			} else if (pl.getPatternType().equals(PatternLocations.TFTYPE)) {
 				drawPattern(
 						g,
 						theone,
 						reg.x1,
 						Math.abs(reg.x1 - reg.x2),
 						xscale,
 						yscale,
 						0,
 						cols,
 						PatternOperations.getPatternColor(pl.getIdForDisplay()),
 						reg.strand);
 
 			}
 		}
 
 		setPreferredSize(new Dimension(this.getWidth() - yOff, y + yOff));
 		revalidate();
 	}
 
 	private void paintText(Graphics g) {
 		Font f = new Font("Courier New", Font.PLAIN, 10);
 		int rowId = -1;
 
 		int seqNo = sequenceDB.getSequenceNo();
 
 		scale = Math.min(5.0, (double) (this.getWidth() - 20 - xOff)
 				/ (double) maxSeqLen);
 
 		g.clearRect(0, 0, getWidth(), getHeight());
 		// draw the patterns
 		g.setFont(f);
 		JViewport scroller = (JViewport) this.getParent();
 		Rectangle r = new Rectangle();
 		r = scroller.getViewRect();
 
 		for (int seqId = 0; seqId < seqNo; seqId++) {
 			rowId++;
 			drawSequence(g, seqId, seqId, maxSeqLen);
 			CSSequence sequence = (CSSequence) sequenceDB.get(seqId);
 			if (sequencePatternmatches == null)
 				continue;
 
 			PatternSequenceDisplayUtil psd = sequencePatternmatches
 					.get(sequence);
 			if (psd == null)
 				continue;
 
 			TreeSet<PatternLocations> patternsPerSequence = psd.getTreeSet();
 			if (patternsPerSequence == null)
 				continue;
 
 			for (PatternLocations pl : patternsPerSequence) {
 				CSSeqRegistration reg = pl.getRegistration();
 				if (reg != null) {
 					if (pl.getPatternType()
 							.equals(PatternLocations.DEFAULTTYPE)) {
 						drawPattern(g, seqId, reg.x1, reg.length(), r,
 								PatternOperations.getPatternColor(pl
 										.getIdForDisplay()));
 					} else if (pl.getPatternType().equals(
 							PatternLocations.TFTYPE)) {
 						drawPattern(g, seqId, reg.x1, reg.length(), r,
 								PatternOperations.getPatternColor(pl
 										.getIdForDisplay()),
 								reg.strand);
 
 					}
 				}
 			}
 		} // end of loop of seqNo
 
 		int maxY = (rowId + 1) * yStep + yOff;
 		setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
 		revalidate();
 	}
 
 	/**
 	 * drawPattern for type TFBS in painttext
 	 *
 	 */
 	private void drawPattern(Graphics g, int rowId, int xStart, int length,
 			Rectangle r, Color color, int strandDirection) {
 
 		int y = yOff + rowId * yStep;
 		if (y <= r.y)
 			return;
 
 		if (y > r.y + r.height)
 			return;
 
 		double x0 = xStart;
 		double dx = length;
 		int xa = xOff + (int) (x0 * scale);
 		int xb = xa + (int) (dx * scale) - 1;
 		g.setColor(color);
 		int heightForRect = (int) (0.66 * yStep);
 		g.draw3DRect(xa, y - heightForRect / 2, xb - xa, heightForRect, false);
 		g.fill3DRect(xa, y - heightForRect / 2, xb - xa, heightForRect, false);
 		// create a triangle
 		int[] xi = new int[3];
 		int[] yi = new int[3];
 		if (strandDirection == 0) {
 			xi[0] = xi[1] = xb;
 			yi[0] = y - heightForRect / 2;
 			yi[1] = y + heightForRect / 2;
 			xi[2] = xb + heightForRect / 2;
 			yi[2] = y;
 		} else {
 			xi[0] = xi[1] = xa;
 			yi[0] = y - heightForRect / 2;
 			yi[1] = y + heightForRect / 2;
 			xi[2] = xa - heightForRect / 2;
 			yi[2] = y;
 		}
 		g.setColor(SEQUENCEBACKGROUDCOLOR);
 		g.drawPolygon(xi, yi, 3);
 		g.fillPolygon(xi, yi, 3);
 	}
 
 	public void setMaxSeqLen(int maxSeqLen) {
 		this.maxSeqLen = maxSeqLen;
 	}
 
 	public void setSelectedSequence(DSSequence selectedSequence) {
 		this.selectedSequence = selectedSequence;
 		// update selected index.
 		if (sequenceDB != null && selectedSequence != null) {
 			int location = 0;
 			for (Object seq : sequenceDB) {
 				if (seq.equals(selectedSequence)) {
 					selected = location;
 					return;
 				}
 				location++;
 			}
 		}
 	}
 
 	private void drawSequence(Graphics g, int rowId, int seqId, double len) {
 		String lab = ">seq " + seqId;
 		if (sequenceDB.getSequenceNo() > 0) {
 			DSSequence theSequence = sequenceDB.getSequence(seqId);
 			len = (double) theSequence.length();
 			lab = theSequence.getLabel();
 
 		}
 		int y = yOff + rowId * yStep;
 		int x = xOff + (int) (len * scale);
 		g.setColor(SEQUENCEBACKGROUDCOLOR);
 
 		g.drawString(lab, 4, y + 3);
 		g.drawLine(xOff, y, x, y);
 
 	}
 
 	/* for DEFAULTTYPE in paintText */
 	private void drawPattern(Graphics g, int rowId, int xStart, int length,
 			Rectangle r, Color color) {
 
 		int y = yOff + rowId * yStep;
 		if (y <= r.y)
 			return;
 		if (y > r.y + r.height)
 			return;
 
 		double x0 = xStart;
 		double dx = length;
 		int xa = xOff + (int) (x0 * scale) + 1;
 		int xb = xa + (int) (dx * scale) - 1;
 		g.setColor(color);
 
 		int heightForRect = (int) (0.66 * yStep);
 		g.draw3DRect(xa, y - heightForRect / 2, xb - xa, heightForRect, false);
 	}
 
 	// changed for the simplified use,should replace the above method.
 	/**
 	 * Draw pattern DEFAULTTYPE for (1) full view and (2) Single Sequence Mode.
 	 *
 	 * @param g
 	 *            Graphics
 	 * @param hitSeq
 	 *            DSSequence
 	 * @param offset
 	 *            int
 	 * @param xscale
 	 *            double
 	 * @param yscale
 	 *            double
 	 * @param yBase
 	 *            int
 	 * @param cols
 	 *            int
 	 * @param color
 	 *            Color
 	 * @param highlight
 	 *            String
 	 */
 	private void drawPattern(Graphics g, DSSequence hitSeq, int offset,
 			double xscale, double yscale, int yBase, int cols, Color color,
 			PatternLocations pl) {
 
 		int length = pl.getAsciiLength();
 		String hitSeqStr = hitSeq.getSequence().substring(offset,
 				offset + length);
 		
 		int x = (int) ((6 + offset % cols) * xscale);
 		double y = ((yBase + 2 + (offset / cols)) * yscale);
 		int xb = (int) (length * xscale);
 
 		int height = (int) (1.15 * yscale);
 
 		if (offset % cols + length <= cols) {
 			g.clearRect(x, (int) y - height / 2, xb, height);
 			g.setColor(color);
			g.drawString(hitSeqStr.toUpperCase(), x, (int) (y - 1 * yscale
 					+ yOff + 3));
 			g.draw3DRect(x, (int) y - height / 2, xb, height, false);
 
 		} else {
 
 			int startx = (int) (6 * xscale);
 			int endx = (int) ((cols + 6) * xscale);
 			int k = (offset + length) / cols - offset / cols;
 			g.clearRect(x, (int) y - height / 2, endx - x, height);
 			g.setColor(color);
			g.drawString(hitSeqStr.substring(0, cols - offset % cols)
 					.toUpperCase(), x, (int) (y - 1 * yscale + yOff + 3));
 			g.draw3DRect(x, (int) y - height / 2, endx - x, height, true);
 			int endP = 0;
 			for (int i = 1; i < k; i++) {
 				g.clearRect(startx, (int) (y - height / 2 + (i * yscale)), endx
 						- startx, height);
 				int startPoint = cols - offset % cols + (i - 1) * cols;
 				endP = cols - offset % cols + i * cols;
 				if (endP >= length) {
 					endP = length;
 				}
 				g.setColor(color);
				g.drawString(hitSeqStr.substring(startPoint, endP)
 						.toUpperCase(), startx, (int) (y + (i - 1) * yscale
 						+ yOff + 3));
 				g.draw3DRect(startx, (int) (y - height / 2 + (i * yscale)),
 						endx - startx, height, true);
 			}
 			g.clearRect(startx, (int) (y - height / 2 + (k * yscale)),
 					(int) (((offset + length) % cols) * xscale), height);
 			g.setColor(color);
			int startPoint = cols - offset % cols + (k - 1) * cols;
			g.drawString(hitSeqStr.substring(startPoint).toUpperCase(), startx,
 					(int) (y + (k - 1) * yscale + yOff + 3));
 			g.draw3DRect(startx, (int) (y - height / 2 + (k * yscale)),
 					(int) (((offset + length) % cols) * xscale), height, true);
 
 		}
 	}
 
 	/**
 	 * Draw TFTYPE pattern in full view and Single Sequence Mode.
 	 *
 	 * @param g
 	 *            Graphics
 	 * @param hitSeq
 	 *            DSSequence
 	 * @param offset
 	 *            int
 	 * @param length
 	 *            int
 	 * @param xscale
 	 *            double
 	 * @param yscale
 	 *            double
 	 * @param yBase
 	 *            int
 	 * @param cols
 	 *            int
 	 * @param color
 	 *            Color
 	 * @param strand
 	 *            int
 	 */
 	private void drawPattern(Graphics g, DSSequence hitSeq, int offset,
 			int length, double xscale, double yscale, int yBase, int cols,
 			Color color, int strand) {
 
 		int x = (int) ((6 + offset % cols) * xscale);
 
 		int xb = (int) (length * xscale);
 
 		int height = (int) (1.15 * yscale);
 		double y = ((yBase + 2 + (offset / cols)) * yscale);
 		String hitSeqStr = hitSeq.getSequence().substring(offset,
 				offset + length);
 		if (offset % cols + length <= cols) {
 			g.clearRect(x, (int) y - height / 2, xb, height);
 			g.setColor(SEQUENCEBACKGROUDCOLOR);
 			g.drawString(hitSeqStr.toUpperCase(), x, (int) (y - 1 * yscale
 					+ yOff + 3));
 			g.setColor(color);
 
 			g.draw3DRect(x, (int) y - height / 2 + 2, xb, height, false);
 			g.setColor(DRECTIONCOLOR);
 
 			int shape = 3;
 			int[] xi = new int[shape];
 			int[] yi = new int[shape];
 			if (strand == 0) {
 				xi[0] = xi[1] = x;
 				yi[0] = (int) y - height / 2 - 2;
 				yi[1] = (int) y - height / 2 + 6;
 				xi[2] = xi[0] + 4;
 				yi[2] = (int) y - height / 2 + 2;
 				// g.drawPolyline(xi, yi, addtionalPoint);
 			} else {
 				xi[0] = xi[1] = x + xb;
 				yi[0] = (int) y - height / 2 - 2;
 				yi[1] = (int) y - height / 2 + 6;
 				xi[2] = xi[0] - 4;
 				yi[2] = (int) y - height / 2 + 2;
 
 			}
 
 			g.drawPolygon(xi, yi, shape);
 			g.fillPolygon(xi, yi, shape);
 
 		} else {
 
 			int startx = (int) (6 * xscale);
 			int endx = (int) ((cols + 6) * xscale);
 			int k = (offset + length) / cols - offset / cols;
 			g.clearRect(x, (int) y - height / 2, endx - x, height);
 			g.setColor(SEQUENCEBACKGROUDCOLOR);
 			g.drawString(hitSeqStr.substring(0, cols - offset % cols)
 					.toUpperCase(), x, (int) (y - 1 * yscale + yOff + 3));
 			g.setColor(color);
 
 			g.draw3DRect(x, (int) y - height / 2 + 2, endx - x, height, true);
 			g.setColor(SEQUENCEBACKGROUDCOLOR);
 
 			g.setColor(DRECTIONCOLOR);
 
 			int shape = 3;
 			int[] xi = new int[shape];
 			int[] yi = new int[shape];
 			if (strand == 0) {
 				xi[0] = xi[1] = x;
 				yi[0] = (int) y - height / 2 - 2;
 				yi[1] = (int) y - height / 2 + 6;
 				xi[2] = xi[0] + 4;
 				yi[2] = (int) y - height / 2 + 2;
 				// g.drawPolyline(xi, yi, addtionalPoint);
 			} else {
 				xi[0] = xi[1] = x + xb;
 				yi[0] = (int) y - height / 2 - 2;
 				yi[1] = (int) y - height / 2 + 6;
 				xi[2] = xi[0] - 4;
 				yi[2] = (int) y - height / 2 + 2;
 
 			}
 
 			g.drawPolygon(xi, yi, shape);
 			g.fillPolygon(xi, yi, shape);
 
 			for (int i = 1; i < k; i++) {
 				g.clearRect(startx, (int) (y - height / 2 + (i * yscale)), endx
 						- startx, height);
 				g.setColor(SEQUENCEBACKGROUDCOLOR);
 				g.drawString(
 						hitSeqStr.substring(
 								cols - offset % cols + (k - 1) * cols,
 								cols - offset % cols + k * cols).toUpperCase(),
 						startx, (int) (y + (k - 1) * yscale + yOff + 3));
 				g.setColor(color);
 
 				g.draw3DRect(startx, (int) (y - height / 2 + (i * yscale)),
 						endx - startx, height, true);
 			}
 			g.clearRect(startx, (int) (y - height / 2 + (k * yscale)),
 					(int) (((offset + length) % cols) * xscale), height);
 			g.setColor(SEQUENCEBACKGROUDCOLOR);
 			g.drawString(
 					hitSeqStr.substring(cols - offset % cols + (k - 1) * cols)
 							.toUpperCase(), startx, (int) (y + (k - 1) * yscale
 							+ yOff + 3));
 			g.setColor(color);
 
 			g.draw3DRect(startx, (int) (y - height / 2 + (k * yscale)),
 					(int) (((offset + length) % cols) * xscale), height, true);
 		}
 	}
 
 	public int getSeqXclickPoint() {
 		return seqXclickPoint;
 	}
 
 	public DSSequence getSelectedSequence() {
 		return selectedSequence;
 	}
 
 	private int getSeqDx(int x) {
 		int seqDx = (int) ((double) (x - xOff) / scale);
 		return seqDx;
 	}
 
 	/**
 	 * Handle Mouse clicks.
 	 *
 	 * @param e
 	 *            MouseEvent
 	 */
 	public void this_mouseClicked(final MouseEvent e) {
 		if (e.isMetaDown()) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					itemListPopup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			});
 
 			return;
 		}
 		setTranslatedParameters(e);
 
 		if (e.getClickCount() == 2) {
 			singleSequenceView = !singleSequenceView;
 			this.repaint();
 		}
 
 	}
 
 	/**
 	 * Set up the corresponding parameters when mouse moves.
 	 *
 	 * @param e
 	 *            MouseEvent
 	 */
 	private void setMouseMoveParameters(MouseEvent e) {
 		int y = e.getY();
 		int x = e.getX();
 		int mouseSelected = -1;
 		int mouseMovePoint = -1;
 		DSSequence mouseSelectedSequence;
 		if (!lineView) {
 			mouseSelected = getSeqIdInFullView(y);
 			if (eachSeqStartRowNum != null
 					&& mouseSelected < eachSeqStartRowNum.length) {
 				mouseMovePoint = (int) ((int) ((y - yOff - 1 - ((double) eachSeqStartRowNum[mouseSelected])
 						* yBasescale) / yBasescale)
 						* xBaseCols + x / xBasescale - 5);
 			}
 		} else {
 			if (!singleSequenceView) {
 				mouseSelected = getSeqId(y);
 				mouseMovePoint = getSeqDx(x);
 
 			} else {
 
 				mouseMovePoint = (int) ((int) ((y - yOff - 1) / yBasescale)
 						* xBaseCols + x / xBasescale - 5);
 			}
 		}
 		if (sequenceDB != null && selected < sequenceDB.size()) {
 			mouseSelectedSequence = sequenceDB.getSequence(mouseSelected);
 		} else {
 			mouseSelectedSequence = null;
 		}
 		if (mouseSelectedSequence != null &&
 			(mouseMovePoint <= mouseSelectedSequence.length())
 					&& (mouseMovePoint > 0)) {
 				this.setToolTipText("" + mouseMovePoint);
 		}
 		{
 			this.setToolTipText(null);
 		}
 
 	}
 
 	/**
 	 * Set up corresponding parameters when a mouse click happens.
 	 *
 	 * @param e
 	 *            MouseEvent
 	 */
 	private void setTranslatedParameters(MouseEvent e) {
 		int y = e.getY();
 		int x = e.getX();
 
 		if (!lineView) {
 			selected = getSeqIdInFullView(y);
 			if (eachSeqStartRowNum != null
 					&& selected < eachSeqStartRowNum.length) {
 				seqXclickPoint = (int) ((int) ((y - yOff - 1 - ((double) eachSeqStartRowNum[selected])
 						* yBasescale) / yBasescale)
 						* xBaseCols + x / xBasescale - 5);
 			}
 		} else {
 			if (!singleSequenceView) {
 				selected = getSeqId(y);
 				seqXclickPoint = getSeqDx(x);
 
 			} else {
 
 				seqXclickPoint = (int) ((int) ((y - yOff - 1) / yBasescale)
 						* xBaseCols + x / xBasescale - 5);
 			}
 		}
 		if (sequenceDB != null && selected < sequenceDB.size()) {
 			selectedSequence = sequenceDB.getSequence(selected);
 		} else {
 			selectedSequence = null;
 		}
 		if (selectedSequence != null 
 			&&(seqXclickPoint <= selectedSequence.length())
 					&& (seqXclickPoint > 0)) {
 				this.setToolTipText("" + seqXclickPoint);
 		}
 
 		this.setToolTipText(null);
 	}
 
 	/**
 	 * getSeqIdInFullView
 	 *
 	 * @param y
 	 *            int
 	 * @return int
 	 */
 	private int getSeqIdInFullView(int y) {
 		double yBase = (y - yOff - 3) / yBasescale + 1;
 		if (eachSeqStartRowNum != null) {
 			for (int i = 0; i < eachSeqStartRowNum.length; i++) {
 				if (eachSeqStartRowNum[i] > yBase) {
 					return Math.max(0, i - 1);
 				}
 
 			}
 			return Math.max(0, eachSeqStartRowNum.length - 1);
 		}
 		return 0;
 	}
 
 	private int getSeqId(int y) {
 		int seqId = (y - yOff + 5) / yStep;
 		return seqId;
 	}
 
 	public void this_mouseMoved(MouseEvent e) {
 		setMouseMoveParameters(e);
 		if (!lineView) {
 			mouseOverFullView(e);
 		} else {
 			mouseOverLineView(e);
 		}
 	}
 
 	private void mouseOverFullView(MouseEvent e)
 			throws ArrayIndexOutOfBoundsException {
 		if (sequenceDB == null) {
 			return;
 		}
 		int x1 = e.getX();
 		int y1 = e.getY();
 
 		Font f = new Font("Courier New", Font.PLAIN, 11);
 		Graphics g = this.getGraphics();
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		FontMetrics fm = g.getFontMetrics(f);
 		if (sequenceDB.getSequence(selected) == null
 				|| sequenceDB.getSequence(selected).getSequence() == null) {
 			return;
 		}
 		String asc = sequenceDB.getSequence(selected).getSequence();
 		Rectangle2D r2d = fm.getStringBounds(asc, g);
 		double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
 		double yscale = 1.3 * r2d.getHeight();
 		int width = this.getWidth();
 		int cols = (int) (width / xscale) - 8;
 		int dis = (int) ((int) ((y1 - yOff - 1) / yscale) * cols + x1 / xscale - 5);
 		if (sequenceDB.getSequence(selected) != null) {
 			if (((y1 - yOff - 1) / yscale > 0) && (dis > 0)
 					&& (dis <= sequenceDB.getSequence(selected).length())) {
 				this.setToolTipText("" + dis);
 			}
 		}
 	}
 
 	private void mouseOverLineView(MouseEvent e)
 			throws ArrayIndexOutOfBoundsException {
 		int y = e.getY();
 		int x = e.getX();
 		// displayInfo = "";
 		if (!singleSequenceView) {
 			int seqid = getSeqId(y);
 
 			if (sequenceDB == null) {
 				return;
 			}
 			int off = this.getSeqDx(x);
 			DSSequence sequence = sequenceDB.getSequence(seqid);
 			if (sequence != null) {
 				if ((off <= sequenceDB.getSequence(seqid).length())
 						&& (off > 0)) {
 
 					String texttip = getTipInfo(sequence, off);
 					this.setToolTipText(texttip);
 				}
 			}
 		} else {
 
 			Font f = new Font("Courier New", Font.PLAIN, 11);
 			Graphics g = this.getGraphics();
 			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			FontMetrics fm = g.getFontMetrics(f);
 			if (sequenceDB.getSequence(selected) == null
 					|| sequenceDB.getSequence(selected).getSequence() == null) {
 				return;
 			}
 			DSSequence sequence = sequenceDB.getSequence(selected);
 			String asc = sequence.getSequence();
 
 			Rectangle2D r2d = fm.getStringBounds(asc, g);
 			double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
 			double yscale = 1.3 * r2d.getHeight();
 			int width = this.getWidth();
 			int cols = (int) (width / xscale) - 8;
 			int dis = (int) ((int) ((y - yOff - 1) / yscale) * cols + x
 					/ xscale - 5);
 			if (sequenceDB.getSequence(selected) != null) {
 				if (x >= 6 * xscale && x <= (cols + 6) * xscale
 						&& ((y - yOff - 1) / yscale > 0) && (dis > 0)
 						&& (dis <= sequenceDB.getSequence(selected).length())) {
 
 					String texttip = getTipInfo(sequence, dis);
 					this.setToolTipText(texttip);
 
 					// this.setToolTipText("" + dis);
 				}
 			}
 		}
 	}
 
 	/**
 	 * getTipInfo
 	 *
 	 * @param sequence
 	 *            DSSequence
 	 * @param off
 	 *            int
 	 * @return String
 	 */
 	private String getTipInfo(DSSequence sequence, int off) {
 		String tip = "" + off;
 		if (sequencePatternmatches != null) {
 			PatternSequenceDisplayUtil psd = sequencePatternmatches
 					.get(sequence);
 			if (psd != null) {
 				TreeSet<PatternLocations> patternsPerSequence = psd
 						.getTreeSet();
 				if (patternsPerSequence != null
 						&& patternsPerSequence.size() > 0) {
 					for (PatternLocations pl : patternsPerSequence) {
 						CSSeqRegistration reg = pl.getRegistration();
 						if (reg != null && reg.x1 + 1 <= off && reg.x2 >= off) {
 							int x1 = reg.x1 + 1;
 							int x2 = reg.x2;
 							if (pl.getPatternType().equals(
 									PatternLocations.DEFAULTTYPE)) {
 								tip = tip + " " + pl.getAscii() + "<" + x1
 										+ "," + x2 + "> ";
 							} else if (pl.getPatternType().equals(
 									PatternLocations.TFTYPE)) {
 								tip = tip + " " + pl.getAscii() + "<" + x1
 										+ "," + x2 + "> ";
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return tip;
 	}
 
 	public void setSingleSequenceView(boolean singleSequenceView) {
 		this.singleSequenceView = singleSequenceView;
 	}
 
 	public void setSequenceDB(DSSequenceSet<DSSequence> sequenceDB) {
 		this.sequenceDB = sequenceDB;
 	}
 
 	public void setLineView(boolean lineView) {
 		this.lineView = lineView;
 		revalidate();
 	}
 
 	public void setSeqXclickPoint(int seqXclickPoint) {
 		this.seqXclickPoint = seqXclickPoint;
 	}
 	
 	private int getMaxLabLen(){
 		int maxLabLen = 0;
 		int seqNo = sequenceDB.getSequenceNo();
 		for (int seqId = 0; seqId < seqNo; seqId++) {
 			String lab = ">seq " + seqId;
 			DSSequence theSequence = sequenceDB.getSequence(seqId);
 			if (theSequence != null && theSequence.getLabel().length() > 0)
 				lab = theSequence.getLabel();
 			if (maxLabLen < lab.length())  maxLabLen = lab.length();
 		}
 		return maxLabLen;
 	}
 
 }
