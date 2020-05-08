 package org.geworkbench.util.sequences;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputMethodEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.Rectangle2D;
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 
 import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
 import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
 import org.geworkbench.events.GeneSelectorEvent;
 import org.geworkbench.util.Util;
 import org.geworkbench.util.patterns.CSMatchedSeqPattern;
 import org.geworkbench.util.patterns.PatternLocations;
 import org.geworkbench.util.patterns.PatternOperations;
 import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;
 
 /**
  * Widget provides all GUI services for sequence panel displays.
  *
  * Widget is controlled by its associated component, SequenceViewAppComponent
  * Copyright: Copyright (c) 2003
  *
  * Company: Califano Lab </p>
  *
  * @author
  * @version $Id$
  */
 public class SequenceViewWidget extends JPanel {
 	private static final long serialVersionUID = -6141589995966150788L;
 
 	protected HashMap<CSSequence, PatternSequenceDisplayUtil> patternLocationsMatches;
 	protected List<DSMatchedPattern<DSSequence, CSSeqRegistration>> selectedPatterns = new ArrayList<DSMatchedPattern<DSSequence, CSSeqRegistration>>();
 
 	public JToolBar jToolBar1 = new JToolBar();
 	// TODO create the public/protected method to add button instead of exposing jToolBar1 as public
 
 	protected SequenceViewWidgetPanel seqViewWPanel = new SequenceViewWidgetPanel();
 	protected CSSequenceSet<DSSequence> activeSequenceDB = null;
 	protected boolean subsetMarkerOn = true;
 	protected DSPanel<? extends DSGeneMarker> activatedMarkers = null;
 
 	private int prevSeqId = -1;
 	private int prevSeqDx = 0;
 	protected DSSequenceSet<DSSequence> sequenceDB = new CSSequenceSet<DSSequence>();
 	protected DSSequenceSet<?> orgSequenceDB = new CSSequenceSet<CSSequence>();
 	protected DSSequenceSet<DSSequence> displaySequenceDB = new CSSequenceSet<DSSequence>();
 
 	// Panels and Panes
 	protected JDetailPanel sequencedetailPanel = new JDetailPanel();
 	private JPanel bottomPanel = new JPanel();
 	private JButton leftShiftButton = new JButton();
 	private JButton rightShiftButton = new JButton();
 	protected JScrollPane seqScrollPane = new JScrollPane();
 
 	// these two can be hidden in derived class
 	protected JCheckBox showAllBtn = new JCheckBox();
 	protected  JCheckBox jAllSequenceCheckBox = new JCheckBox();
 
 	private JLabel jViewLabel = new JLabel();
 	protected JComboBox jViewComboBox = new JComboBox();
 	protected static final String LINEVIEW = "Line";
 	private static final String FULLVIEW = "Full Sequence";
 
 	protected boolean isLineView = true; // true is for LineView.
 	private boolean onlyShowPattern = false;
 	private static final String LEFT = "left";
 	private static final String RIGHT = "right";
 	private boolean goLeft = false;
 	protected int xStartPoint = -1;
 	private static final int GAP = 40;
 
 	/* The only constructor. */
 	public SequenceViewWidget() {
 		try {
 			jbInit();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void jbInit() throws Exception {
 
 		this.setLayout(new BorderLayout());
 		sequencedetailPanel.setBorder(BorderFactory.createEtchedBorder());
 		sequencedetailPanel.setMinimumSize(new Dimension(50, 40));
 		sequencedetailPanel.setPreferredSize(new Dimension(60, 50));
 		seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
 		seqViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				jDisplayPanel_mouseClicked(e);
 			}
 		});
 		this.addInputMethodListener(new java.awt.event.InputMethodListener() {
 			public void inputMethodTextChanged(InputMethodEvent e) {
 			}
 
 			public void caretPositionChanged(InputMethodEvent e) {
 				showPatterns();
 			}
 		});
 		this.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent e) {
 				showPatterns();
 			}
 		});
 		showAllBtn
 				.setToolTipText("Click to show sequences with selected patterns.");
 		showAllBtn.setText("All / Matching Pattern");
 		showAllBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showAllBtn_actionPerformed(e);
 			}
 		});
 		jAllSequenceCheckBox.setToolTipText("Click to display all sequences.");
 		jAllSequenceCheckBox.setSelected(false);
 		jAllSequenceCheckBox.setText("All Sequences");
 		jAllSequenceCheckBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				refreshMaSetView();
 			}
 		});
 		jViewLabel.setText("View: ");
 
 		seqViewWPanel.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				seqViewWPanel.this_mouseMoved(e);
 			}
 		});
 		jViewComboBox.addItem(LINEVIEW);
 		jViewComboBox.addItem(FULLVIEW);
 		jViewComboBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				initPanelView();
 			}
 		});
 		jViewComboBox.setToolTipText("Select a view to display results.");
 
 		bottomPanel = new JPanel();
 		ImageIcon leftButtonIcon = Util.createImageIcon("/images/back.gif");
 		leftShiftButton.setIcon(leftButtonIcon);
 		ImageIcon rightButtonIcon = Util.createImageIcon("/images/forward.gif");
 		rightShiftButton.setIcon(rightButtonIcon);
 		bottomPanel.setLayout(new BorderLayout());
 		bottomPanel.add(leftShiftButton, BorderLayout.WEST);
 		bottomPanel.add(sequencedetailPanel, BorderLayout.CENTER);
 		bottomPanel.add(rightShiftButton, BorderLayout.EAST);
 		leftShiftButton.setEnabled(false);
 		rightShiftButton.setEnabled(false);
 		leftShiftButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setMoveDirection(LEFT);
 				updateBottomPanel();
 			}
 		});
 		rightShiftButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setMoveDirection(RIGHT);
 				updateBottomPanel();
 			}
 		});
 
 		this.add(bottomPanel, BorderLayout.SOUTH);
 		this.add(seqScrollPane, BorderLayout.CENTER);
 		this.add(jToolBar1, BorderLayout.NORTH);
 		jToolBar1.add(jViewLabel);
 		jToolBar1.add(jViewComboBox);
 		jToolBar1.add(showAllBtn);
 		jToolBar1.add(jAllSequenceCheckBox);
 		jToolBar1.addSeparator();
 
 		if (sequenceDB != null) {
 			seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 		}
 
 		seqScrollPane.getViewport().add(seqViewWPanel, null);
 	}
 
 	private void setMoveDirection(String directionStr) {
 		if (directionStr.equals(LEFT)) {
 			goLeft = true;
 		} else {
 			goLeft = false;
 		}
 	}
 
 	/**
 	 * geneSelectorAction
 	 *
 	 * @param e
 	 *            GeneSelectorEvent
 	 */
 	public void sequenceDBUpdate(GeneSelectorEvent e) {
 		if (e.getPanel() == null) {
 			return;
 		}
 		if (e.getPanel() != null && e.getPanel().size() > 0) {
 			activatedMarkers = e.getPanel().activeSubset();
 		} else {
 			activatedMarkers = null;
 		}
 		refreshMaSetView();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void refreshMaSetView() {
 		subsetMarkerOn = !jAllSequenceCheckBox.isSelected();
 		if (subsetMarkerOn) {
 			if (activatedMarkers != null && activatedMarkers.size() > 0) {
 
 				if (subsetMarkerOn && (orgSequenceDB != null)) {
 					activeSequenceDB = (CSSequenceSet<DSSequence>) ((CSSequenceSet<DSSequence>) orgSequenceDB)
 							.getActiveSequenceSet(activatedMarkers);
 				}
 
 			} else if (orgSequenceDB != null) {
 				activeSequenceDB = (CSSequenceSet<DSSequence>) orgSequenceDB;
 			}
 
 		} else if (orgSequenceDB != null) {
 			activeSequenceDB = (CSSequenceSet<DSSequence>) orgSequenceDB;
 		}
 		if (activeSequenceDB != null) {
 			sequenceDB = activeSequenceDB;
 			initPanelView();
 		}
 	}
 
 	/* show the patterns over sequence viewer */
 	public void patternSelectionHasChanged(List<DSMatchedPattern<DSSequence, CSSeqRegistration>> patterns) {
 		setPatterns(patterns);
 		refreshMaSetView();
 	}
 
 	protected void updateBottomPanel() {
 
 		DSSequence selectedSequence = getSelectedSequence();
 		if (selectedSequence == null) {
 			return;
 		}
 		if (goLeft) {
 			if (xStartPoint - GAP > 0) {
 				xStartPoint = xStartPoint >= GAP ? xStartPoint - GAP : 1;
 			} else {
 				xStartPoint = 1;
 				leftShiftButton.setEnabled(false);
 			}
 		} else {
 			if (xStartPoint < selectedSequence.length() - GAP) {
 				xStartPoint += GAP;
 			} else {
 				rightShiftButton.setEnabled(false);
 			}
 		}
 		sequencedetailPanel.repaint();
 
 		prevSeqDx = xStartPoint;
 		sequencedetailPanel.setOpaque(false);
 
 	}
 
 	/**
 	 * Update the detail of sequence.
 	 *
 	 * @param e
 	 *            MouseEvent
 	 */
 	private void jDisplayPanel_mouseClicked(MouseEvent e) {
 		seqViewWPanel.this_mouseClicked(e);
 		xStartPoint = seqViewWPanel.getSeqXclickPoint();
 		sequencedetailPanel.repaint();
 	}
 
 	private void showPatterns() {
 		for (DSMatchedPattern<DSSequence, CSSeqRegistration> pattern : selectedPatterns) {
 			if (((DSMatchedSeqPattern) pattern).getASCII() == null) {
 				PatternOperations.fill((CSMatchedSeqPattern) pattern,
 						displaySequenceDB);
 			}
 		}
 		repaint();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setSequenceDB(DSSequenceSet<?> db) {
 		orgSequenceDB = db;
 		sequenceDB = (DSSequenceSet<DSSequence>) db;
 		displaySequenceDB = (DSSequenceSet<DSSequence>) db;
 		refreshMaSetView();
 		// selectedPatterns = new ArrayList();
 		if (sequenceDB != null) {
 			seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 			seqViewWPanel.setSelectedSequence(null);
 			// seqViewWPanel.initialize(null, db);
 			selectedPatterns.clear();
 			repaint();
 		}
 	}
 
 	public SequenceViewWidgetPanel getSeqViewWPanel() {
 		return seqViewWPanel;
 	}
 
 	public void setPatterns(
 			List<DSMatchedPattern<DSSequence, CSSeqRegistration>> matches) {
 		selectedPatterns.clear();
 		for (int i = 0; i < matches.size(); i++) {
 			selectedPatterns.add(matches.get(i));
 		}
 	}
 
 	private void showAllBtn_actionPerformed(ActionEvent e) {
 		if (selectedPatterns == null && showAllBtn.isSelected()) {
 			if (sequenceDB == null) {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"No sequence is stored right now, please load sequences first.",
 								"No Pattern", JOptionPane.ERROR_MESSAGE);
 			} else {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"No pattern is stored right now, please generate patterns with Pattern Discory module first.",
 								"No Pattern", JOptionPane.ERROR_MESSAGE);
 				seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 				displaySequenceDB = sequenceDB;
 			}
 
 			showAllBtn.setSelected(false);
 
 		}
 		initPanelView();
 
 	}
 
 	/**
 	 * Transform the patterns to patternsUtil class. Child class should override
 	 * this method.
 	 */
 	protected void updatePatternSeqMatches() {
 		patternLocationsMatches = PatternOperations.processPatterns(
 				selectedPatterns, sequenceDB);
 
 	}
 
 	/**
 	 * Initiate the Panel, which should be used as the entry point.
 	 *
 	 * @return boolean
 	 */
 	public void initPanelView() {
 		updatePatternSeqMatches();
 		isLineView = jViewComboBox.getSelectedItem().equals(LINEVIEW);
 		onlyShowPattern = showAllBtn.isSelected();
 
 		if (onlyShowPattern) {
 			displaySequenceDB = new CSSequenceSet<DSSequence>();
 		}
 
 		// if (patternLocationsMatches != null && sequenceDB != null) {
 		if (patternLocationsMatches != null && sequenceDB != null) {
 			seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 			for (int i = 0; i < sequenceDB.size(); i++) {
 				DSSequence sequence = sequenceDB.getSequence(i);
 				PatternSequenceDisplayUtil pdu = patternLocationsMatches
 						.get(sequence);
 				if (onlyShowPattern) {
 					if (pdu.hasPattern()) {
 						displaySequenceDB.addASequence(sequence);
 					}
 				}
 			}
 			if (onlyShowPattern) {
 				seqViewWPanel.initialize(patternLocationsMatches,
 						displaySequenceDB, isLineView);
 			} else {
 				seqViewWPanel.initialize(patternLocationsMatches, sequenceDB,
 						isLineView);
 			}
 
 		} else {
 			if (sequenceDB != null) {
 				seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
 				displaySequenceDB = sequenceDB;
 
 				showAllBtn.setSelected(false);
 				seqViewWPanel.initialize(patternLocationsMatches,
 						displaySequenceDB, isLineView);
 
 			} else {
 				seqViewWPanel.removeAll();
 			}
 		}
 	}
 
 	protected DSSequence getSelectedSequence() {
 		return seqViewWPanel.getSelectedSequence();
 	}
 
 	/*
 	 * This class is to implement the paint behavior. All the fields are from
 	 * the enclosing class instance.
 	 */
 	protected class JDetailPanel extends JPanel {
 		private static final long serialVersionUID = -1720620118289765818L;
 
 		@Override
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			DSSequence selectedSequence = getSelectedSequence();
 			if (selectedSequence == null) {
 				g.clearRect(0, 0, getWidth(), getHeight());
 				rightShiftButton.setEnabled(false);
 				leftShiftButton.setEnabled(false);
 				return;
 
 			}
 			if (xStartPoint < 0 || xStartPoint >= selectedSequence.length()) {
 				rightShiftButton.setEnabled(false);
 				leftShiftButton.setEnabled(false);
 				return;
 			}
 			if (xStartPoint >= 0
 					&& xStartPoint < selectedSequence.length() - 2 * GAP) {
 				rightShiftButton.setEnabled(true);
 			} else {
 				rightShiftButton.setEnabled(false);
 			}
 			if (xStartPoint > GAP) {
 				leftShiftButton.setEnabled(true);
 			} else {
 				leftShiftButton.setEnabled(false);
 			}
 			final Font font = new Font("Courier", Font.BOLD, 10);
 			int seqId = -1;
 			int seqDx = -1;
 			if (sequenceDB != null) {
 				for (int i = 0; i < sequenceDB.size(); i++) {
 					DSSequence seq = sequenceDB.getSequence(i);
 					if (seq == selectedSequence) {
 						seqId = i;
 					}
 				}
 			}
 			seqDx = xStartPoint;
 
 			DSSequence sequence = selectedSequence;
 			// Check if we are clicking on a new sequence
 			if ((seqId < 0 || seqId == prevSeqId) && seqDx == prevSeqDx)
 				return;
 
 			g.clearRect(0, 0, getWidth(), getHeight());
 			g.setFont(font);
 
 			if (sequence == null || seqDx < 0 || seqDx >= sequence.length())
 				return;
 
 			// turn anti alising on
 			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			// shift the selected pattern/sequence into middle of the
 			// panel.
 			int startPoint = 0;
 			if (seqDx > GAP) {
 				startPoint = seqDx / 10 * 10 - GAP;
 			}
 			FontMetrics fm = g.getFontMetrics(font);
 
 			String seqAscii = sequence.getSequence().substring(startPoint);
 			Rectangle2D r2d = fm.getStringBounds(seqAscii, g);
 			int seqLength = seqAscii.length();
 			double xscale = (r2d.getWidth() + 3) / (double) (seqLength);
 			double yscale = 0.6 * r2d.getHeight();
 			g.drawString(seqAscii, 10, 20);
 			int paintPoint = 0;
 			while (paintPoint < seqLength) {
 				g.drawString("|", 10 + (int) (paintPoint * xscale),
 						(int) (GAP / 2 + yscale));
 				g.drawString(
 						new Integer(paintPoint + 1 + startPoint).toString(),
 						10 + (int) (paintPoint * xscale),
 						(int) (GAP / 2 + 2 * yscale));
 				paintPoint += GAP;
 			}
 
 			if (patternLocationsMatches == null)
 				return;
 
 			PatternSequenceDisplayUtil psd = patternLocationsMatches
 					.get(sequence);
 			if (psd == null)
 				return;
 
 			TreeSet<PatternLocations> patternsPerSequence = psd.getTreeSet();
 			if (patternsPerSequence == null || patternsPerSequence.size() <= 0)
 				return;
 
 			for (PatternLocations pl : patternsPerSequence) {
 				CSSeqRegistration registration = pl.getRegistration();
 				if (registration == null)
 					continue;
 
 				Rectangle2D r = fm.getStringBounds(seqAscii, g);
 				double scale = (r.getWidth() + 3)
 						/ (double) (seqAscii.length());
 				CSSeqRegistration seqReg = (CSSeqRegistration) registration;
				int patLength = pl.getAsciiLength();
 				int dx = seqReg.x1;
 				double x1 = (dx - startPoint) * scale + 10;
 				double x2 = ((double) patLength) * scale;
 				if (pl.getPatternType().equals(PatternLocations.TFTYPE)) {
 					x2 = Math.abs(registration.x2 - registration.x1) * scale;
 				}
 				g.setColor(PatternOperations.getPatternColor(new Integer(pl
 						.getIdForDisplay())));
 				g.drawRect((int) x1, 2, (int) x2, 23);
 				g.drawString("|", (int) x1, (int) (GAP / 2 + yscale));
 				g.drawString("|", (int) (x1 + x2 - scale),
 						(int) (GAP / 2 + yscale));
 				g.drawString(new Integer(dx + 1).toString(), (int) x1,
 						(int) (GAP / 2 + 2 * yscale));
				g.drawString(new Integer(dx + patLength).toString(),
 						(int) (x1 + x2 - scale), (int) (GAP / 2 + 2 * yscale));
 				if (pl.getPatternType().equals(PatternLocations.TFTYPE)) {
 
 					g.setColor(Color.RED);
 
 					int shape = 3;
 					int[] xi = new int[shape];
 					int[] yi = new int[shape];
 					int triangleSize = 8;
 					if (registration.strand == 0) {
 						xi[0] = xi[1] = (int) x1;
 						yi[0] = 2;
 						yi[1] = 2 + triangleSize;
 						xi[2] = xi[0] + triangleSize / 2;
 						yi[2] = 2 + triangleSize / 2;
 					} else {
 						xi[0] = xi[1] = (int) (x1 + x2);
 						yi[0] = 2;
 						yi[1] = 2 + triangleSize;
 						xi[2] = xi[0] - triangleSize / 2;
 						yi[2] = 2 + triangleSize / 2;
 					}
 
 					g.drawPolygon(xi, yi, shape);
 					g.fillPolygon(xi, yi, shape);
 				}
 
 			} // end of loop through patternsPerSequence
 
 		} // end of paintComponent
 	} // end of class JDetailPanel
 
 }
