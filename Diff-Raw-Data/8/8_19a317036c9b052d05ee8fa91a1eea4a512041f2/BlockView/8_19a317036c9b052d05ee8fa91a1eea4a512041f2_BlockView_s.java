 /*****************************************************************************
  * Schaltwerk - A free and extensible digital simulator
  * Copyright (c) 2013 Christian Wichmann
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  *****************************************************************************/
 package de.ichmann.java.schaltwerk.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.geom.Ellipse2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.SwingUtilities;
 import javax.swing.event.EventListenerList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.ichmann.java.schaltwerk.blocks.Block;
 import de.ichmann.java.schaltwerk.blocks.Input;
 import de.ichmann.java.schaltwerk.blocks.Output;
 import de.ichmann.java.schaltwerk.blocks.Signal;
 
 /**
  * Represents controller and view for a block in a logical circuit based on the
  * MVC-paradigm. As model all classes that extends <code>Block</code> can be
  * used.
  * 
  * Using listeners other components can be kept informed about this view and its
  * properties.
  * 
  * @author Christian Wichmann
  */
 public class BlockView {
 
 	private static final Logger LOG = LoggerFactory.getLogger(BlockView.class);
 
 	/**
 	 * Stores the listeners on this model.
 	 */
 	private EventListenerList listenerList = new EventListenerList();
 
 	private Dimension blockViewSize = null;
 	private static final int PADDING = 10;
 	private static final int INTERNAL_PADDING = 5;
 	private static final int MIN_BLOCK_WIDTH = 60;
 	private static final int MIN_BLOCK_HEIGHT = 100;
 	private static final int HEADER_GAP = 50;
 	private static final int SIGNAL_GAP = 25;
 	private static final int WIDTH_GAP = 40 + INTERNAL_PADDING + PADDING;
 	private static final int RADIUS_SIGNAL = 15;
 	private static final int RADIUS_INVERTED = 5;
 
 	private Rectangle blockBounds = new Rectangle();
 	private final List<SignalShape> signalShapes = new ArrayList<SignalShape>();
 
 	private static final Color highlightColor = new Color(222, 222, 222);
 
 	private boolean highlighted = false;
 	private boolean selected = false;
 
 	/**
 	 * Stores a shape describing a signal (input or output of a block). It is
 	 * used to determine where a block can be connected to another.
 	 * 
 	 * This component is the view/controller for a underlying signal.
 	 * 
 	 * @author Christian Wichmann
 	 */
 	public class SignalShape extends Ellipse2D.Float {
 
 		private static final long serialVersionUID = -1254640466415111925L;
 
 		private final Signal attachedSignal;
 
 		/**
 		 * Initializes a shape that is attached to a specific signal, either an
 		 * input or an output of a block.
 		 * 
 		 * @param x
 		 *            the X coordinate of the upper-left corner of the framing
 		 *            rectangle
 		 * @param y
 		 *            the Y coordinate of the upper-left corner of the framing
 		 *            rectangle
 		 * @param w
 		 *            the width of the framing rectangle
 		 * @param h
 		 *            the height of the framing rectangle
 		 * @param attachedSignal
 		 *            sihnal that is attached to this shape
 		 */
 		public SignalShape(float x, float y, float w, float h,
 				Signal attachedSignal) {
 
 			super(x, y, w, h);
 
 			this.attachedSignal = attachedSignal;
 		}
 
 		/**
 		 * Returns the attached signal for this shape.
 		 * 
 		 * @return attached signal for this shape
 		 */
 		public Signal getAttachedSignal() {
 
 			return attachedSignal;
 		}
 
 		/**
 		 * Returns a point for this signal. The point is the center of the
 		 * signal.
 		 * 
 		 * @return center point for given signal
 		 */
 		public Point pointForSignal() {
 
 			Point p = null;
 
 			p = new Point((int) x, (int) y - RADIUS_SIGNAL);
 
 			assert p != null;
 			return p;
 		}
 	}
 
 	/**
 	 * Model for this block defining inputs, outputs and name of it.
 	 */
 	private Block model = null;
 
 	/**
 	 * Initializes a block view component containing a given block model.
 	 * 
 	 * @param model
 	 *            block model to be set
 	 */
 	public BlockView(Block model) {
 
 		super();
 
 		setModel(model);
 
 		initialize();
 
 		calculateSizeAndSetBounds();
 
 		calculateSignalShapes();
 	}
 
 	/**
 	 * Initialize properties for this block view component.
 	 */
 	private void initialize() {
 
 	}
 
 	/*
 	 * ===== Methods for dealing with the model and its properties =====
 	 */
 
 	public Block getModel() {
 
 		return model;
 	}
 
 	public void setModel(final Block newModel) {
 
 		model = newModel;
 		// TODO handle repaint etc
 	}
 
 	public String getBlockID() {
 
 		return getModel().getBlockID();
 	}
 
 	public Output getOutput(final String outputID) {
 
 		return getModel().output(outputID);
 	}
 
 	public Input getInput(final String inputID) {
 
 		return getModel().input(inputID);
 	}
 
 	/*
 	 * ===== Methods concerning painting this component =====
 	 */
 
 	protected void drawBlock(final Graphics g) {
 
 		Graphics2D g2d = (Graphics2D) g;
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
 				RenderingHints.VALUE_RENDER_SPEED);
 
 		// paint background
 		g2d.setColor(ColorFactory.getInstance().getBackgroundColor());
 		g2d.fillRect(blockBounds.x, blockBounds.y, blockViewSize.width,
 				blockViewSize.height);
 		g2d.setColor(ColorFactory.getInstance().getForegroundColor());
 
 		// highlight block
 		if (highlighted) {
 			g2d.setColor(highlightColor);
 			g2d.fillRect(blockBounds.x, blockBounds.y, blockViewSize.width,
 					blockViewSize.height);
 			g2d.setColor(ColorFactory.getInstance().getForegroundColor());
 		}
 
 		// paint block border
 		g2d.setFont(FontFactory.getInstance().createTextFont());
 		g2d.drawRect(blockBounds.x + PADDING, blockBounds.y + PADDING,
 				blockViewSize.width - PADDING - PADDING, blockViewSize.height
 						- PADDING - PADDING);
 
 		// paint block name
		String blockName = getModel().getBlockID();
 		g2d.drawString(blockName, blockBounds.x + blockViewSize.width / 2
 				- calculateFontSize(g2d, blockName).width / 2, blockBounds.y
 				+ HEADER_GAP / 2);
 
 		// paint inputs
 		int y = HEADER_GAP;
 		String[] inputList = getModel().inputList();
 		for (String input : inputList) {
 
 			g2d.drawString(input, blockBounds.x + PADDING + INTERNAL_PADDING,
 					blockBounds.y + y);
 			g2d.drawLine(blockBounds.x, blockBounds.y + y, blockBounds.x
 					+ PADDING, blockBounds.y + y);
 			y += SIGNAL_GAP;
 		}
 
 		// paint outputs
 		y = HEADER_GAP;
 		for (String output : getModel().outputList()) {
 
 			g2d.drawString(output,
 					blockBounds.x + blockViewSize.width - PADDING
 							- INTERNAL_PADDING
 							- calculateFontSize(g2d, output).width,
 					blockBounds.y + y);
 			g2d.drawLine(blockBounds.x + blockViewSize.width,
 					blockBounds.y + y, blockBounds.x + blockViewSize.width
 							- PADDING, blockBounds.y + y);
 
 			// paint inverter circle for outputs starting with tilde
 			if (output.startsWith("~")) {
 
 				g2d.setColor(ColorFactory.getInstance().getBackgroundColor());
 				g2d.fillOval(blockBounds.x + blockViewSize.width - PADDING,
 						blockBounds.y + y - RADIUS_INVERTED / 2,
 						RADIUS_INVERTED, RADIUS_INVERTED);
 				g2d.setColor(ColorFactory.getInstance().getForegroundColor());
 				g2d.drawOval(blockBounds.x + blockViewSize.width - PADDING,
 						blockBounds.y + y - RADIUS_INVERTED / 2,
 						RADIUS_INVERTED, RADIUS_INVERTED);
 			}
 
 			y += SIGNAL_GAP;
 		}
 
 		// paint border different when selected
 		if (selected) {
 			g2d.setColor(ColorFactory.getInstance().getHighlightColor());
 			g2d.drawRect(blockBounds.x + PADDING, blockBounds.y + PADDING,
 					blockViewSize.width - PADDING - PADDING,
 					blockViewSize.height - PADDING - PADDING);
 		}
 	}
 
 	/**
 	 * Calculates size of a given string using a predefined font in the current
 	 * graphics context. (see
 	 * http://docs.oracle.com/javase/tutorial/2d/text/measuringtext.html)
 	 * 
 	 * @param graphics
 	 *            current graphics context
 	 * @param stringToMeasure
 	 *            string for which to get size
 	 * @return dimension object containing size
 	 */
 	private Dimension calculateFontSize(final Graphics graphics,
 			final String stringToMeasure) {
 
 		// get metrics from the graphics
 		FontMetrics metrics = graphics.getFontMetrics(FontFactory.getInstance()
 				.createTextFont());
 
 		// get the height of a line of text in this
 		// font and render context
 		int hgt = metrics.getHeight();
 
 		// get the advance of my text in this font
 		// and render context
 		int adv = metrics.stringWidth(stringToMeasure);
 
 		// calculate the size of a box to hold the
 		// text with some padding
 		Dimension size = new Dimension(adv + 1, hgt + 1);
 
 		return size;
 	}
 
 	/**
 	 * Calculates size of this block.
 	 * 
 	 * @return size of this block
 	 */
 	private Dimension calculateSizeAndSetBounds() {
 
 		if (blockViewSize == null) {
 
 			LOG.debug("Calculating size for block view component...");
 
 			// calculate width by measuring text widths for inputs and outputs
 			int width = calcaluteWidth();
 
 			// calculate height by counting inputs and outputs
 			final int in = getModel().countInputs();
 			final int out = getModel().countOutputs();
 			int height = HEADER_GAP + SIGNAL_GAP * (in > out ? in : out);
 
 			blockViewSize = new Dimension(width > MIN_BLOCK_WIDTH ? width
 					: MIN_BLOCK_WIDTH, height > MIN_BLOCK_HEIGHT ? height
 					: MIN_BLOCK_HEIGHT);
 		}
 
 		blockBounds.setBounds(0, 0, blockViewSize.width, blockViewSize.height);
 
 		return blockViewSize;
 	}
 
 	/**
 	 * Calculates width for this block view component by measuring text widths
 	 * for all inputs and outputs and choosing the minimal necessary width.
 	 * 
 	 * @return width for this block view component
 	 */
 	private int calcaluteWidth() {
 
 		@SuppressWarnings("deprecation")
 		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(
 				FontFactory.getInstance().createTextFont());
 		// TODO use non deprecated methods Font.getLineMetrics! But that
 		// needs a FontRenderContext.
 		// FontRenderContext frc = new FontRenderContext(FontFactory
 		// .getInstance().createTextFont().getTransform(), true, true);
 
 		int inputWidth = 0;
 		int outputWidth = 0;
 		String[] inputList = getModel().inputList();
 		for (String input : inputList) {
 			int size = SwingUtilities.computeStringWidth(fm, input);
 			inputWidth = inputWidth < size ? size : inputWidth;
 		}
 		String[] outputList = getModel().outputList();
 		for (String output : outputList) {
 			int size = SwingUtilities.computeStringWidth(fm, output);
 			outputWidth = outputWidth < size ? size : outputWidth;
 		}
 
 		// find minimal necessary width for all inputs, outputs and blocks
 		// header
 		int width = inputWidth + WIDTH_GAP + outputWidth;
 		int headerWidth = SwingUtilities.computeStringWidth(fm, getModel()
 				.getBlockID());
 		width = width < headerWidth ? headerWidth : width;
 
 		assert width >= headerWidth;
 		assert width >= inputWidth;
 		assert width >= outputWidth;
 		assert width > WIDTH_GAP;
 		return width;
 	}
 
 	/**
 	 * Calculates a specific shape for all input and output signals as 'contact'
 	 * for connecting this block with others. Use method
 	 * <code>checkIfPointIsSignal(Point p)</code> to get signal for a given
 	 * point in the circuit.
 	 */
 	private void calculateSignalShapes() {
 
 		int x = 0;
 		int y = 0;
 
 		signalShapes.clear();
 
 		// add signal shapes for inputs
 		String[] inputList = getModel().inputList();
 		x = blockBounds.x;
 		y = blockBounds.y + HEADER_GAP + RADIUS_SIGNAL;
 		for (String input : inputList) {
 			signalShapes.add(new SignalShape(x, y, RADIUS_SIGNAL,
 					RADIUS_SIGNAL, getModel().input(input)));
 			y += SIGNAL_GAP;
 		}
 
 		// add signal shapes for outputs
 		String[] outputList = getModel().outputList();
 		x = blockBounds.x + blockBounds.width;
 		y = blockBounds.y + HEADER_GAP + RADIUS_SIGNAL;
 		for (String output : outputList) {
 			signalShapes.add(new SignalShape(x, y, RADIUS_SIGNAL,
 					RADIUS_SIGNAL, getModel().output(output)));
 			y += SIGNAL_GAP;
 		}
 
 		assert signalShapes.size() == inputList.length + outputList.length;
 	}
 
 	/**
 	 * Checks whether a given point is inside this blocks boundary.
 	 * 
 	 * @param p
 	 *            point to check whether it is inside this blocks boundary
 	 * @return true, if point is inside this blocks boundary
 	 */
 	public boolean contains(final Point p) {
 
 		return blockBounds.contains(p);
 	}
 
 	/**
 	 * Returns center point of this block view component.
 	 * 
 	 * @return center point for this block
 	 */
 	public Point getCenterPoint() {
 
 		return new Point((int) blockBounds.getCenterX(),
 				(int) blockBounds.getCenterY());
 	}
 
 	/**
 	 * Moves this blocks coordinates by a given delta. No repaint will
 	 * automatically be done!
 	 * 
 	 * @param delta
 	 *            delta to move this block by
 	 */
 	public void moveBlockView(final Point delta) {
 
 		// TODO check bounds?!
 		blockBounds.x += delta.x;
 		blockBounds.y += delta.y;
 
 		for (SignalShape s : signalShapes) {
 			s.x += delta.x;
 			s.y += delta.y;
 		}
 	}
 
 	/**
 	 * Checks whether a given point lies on one of this blocks signals (inputs,
 	 * outputs).
 	 * 
 	 * @param p
 	 *            point to check for
 	 * @return signals shape for given point or null when point is not a signal
 	 */
 	public SignalShape checkIfPointIsSignal(final Point p) {
 
 		for (SignalShape s : signalShapes) {
 			if (s.contains(p)) {
 				return s;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * ===== Getter and setter for block view attributes =====
 	 */
 
 	/**
 	 * Sets whether this block is highlighted because mouse hovers over it.
 	 * 
 	 * @param highlighted
 	 *            whether to highlight this block
 	 */
 	public void setHighlighted(final boolean highlighted) {
 
 		this.highlighted = highlighted;
 	}
 
 	/**
 	 * Returns whether this block is highlighted because mouse hovers over it.
 	 * 
 	 * @return whether this block is highlighted
 	 */
 	public boolean isHighlighted() {
 
 		return highlighted;
 	}
 
 	/**
 	 * Sets whether this block is selected.
 	 * 
 	 * @param selected
 	 *            whether to selected this block
 	 */
 	public void setSelected(final boolean selected) {
 
 		this.selected = selected;
 	}
 
 	/**
 	 * Returns whether this block is selected.
 	 * 
 	 * @return whether this block is selected
 	 */
 	public boolean isSelected() {
 
 		return selected;
 	}
 
 	/*
 	 * ===== Methods to handle block view listener =====
 	 */
 
 	/**
 	 * Adds listener to this view to be informed of changes in view.
 	 * 
 	 * @param l
 	 *            listener that will be informed when view changes
 	 */
 	public void addActionListener(ActionListener l) {
 
 		/*
 		 * TODO create own listener type BlockListener and BlockEvent?
 		 */
 		listenerList.add(ActionListener.class, l);
 	}
 
 	/**
 	 * /** Removes listener from this view.
 	 * 
 	 * @param l
 	 *            listener to be removed from this view
 	 */
 	public void removeActionListener(ActionListener l) {
 
 		listenerList.remove(ActionListener.class, l);
 	}
 
 	/**
 	 * Notifies all listeners that have registered interest for notification on
 	 * this view.
 	 * 
 	 * @see EventListenerList
 	 */
 	@SuppressWarnings("unused")
 	private void fireBlockViewChanged() {
 
 		Object[] listeners = listenerList.getListenerList();
 
 		// Process the listeners last to first, notifying
 		// those that are interested in this event
 		for (Object object : listeners) {
 			if (object instanceof ActionListener) {
 				((ActionListener) object).actionPerformed(null);
 			}
 		}
 	}
 }
