 package com.canoo.codecamp.dolphinpi.swingbaseddisplay.departureboardswingbased.splitflap;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.LinearGradientPaint;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 
 public class SplitFlap extends JComponent implements ActionListener {
 	// <editor-fold defaultstate="collapsed" desc="Variable declarations">
 	public static final String[] TIME_0_TO_5 = {"1", "2", "3", "4", "5", "0"};
 	public static final String[] TIME_0_TO_9 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
 	public static final String[] NUMERIC = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
 	public static final String[] ALPHANUMERIC = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
 			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
 			"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
 			"W", "X", "Y", "Z", "Ä", "Ö", "Ü", "(", ")"};
 	public static final String[] EXTENDED = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
 			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
 			"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z", "Ä", "Ö", "Ü", "-", "/", ":", ",",
			".", ";", "@", "#", "+", "?", "!", "%", "$", "=", "<",
			">"};
 	private static final String PROPERTY_TEXT = "text";
 
 	private final Rectangle INNER_BOUNDS; //new Rectangle(0, 0, 45, 62);
 	private final BasicStroke THIN_STROKE;
 	private volatile int currentFlipSeqImage;
 	private BufferedImage backgroundImage;
 	private BufferedImage foregroundImage;
 	private BufferedImage flipSequenceImage;
 	private String[] selection;
 	private ArrayList<String> selectedSet;
 	private int currentSelectionIndex;
 	private int nextSelectionIndex;
 	private int previousSelectionIndex;
 	private String text;
 	private boolean flipping;
 	private boolean flipComplete;
 	private Font font;
 	private Color fontColor;
 	private final Rectangle2D CLIP;
 	private final Rectangle2D TOP_CLIP;
 	private final Rectangle2D BOTTOM_CLIP;
 
 	private transient final ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
 		@Override
 		public void componentResized(final ComponentEvent EVENT) {
 			final boolean SQUARE = getWidth() == getHeight() ? true : false;
 			final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
 			Container parent = getParent();
 			if ((parent != null) && (parent.getLayout() == null)) {
 				if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
 					setSize(getMinimumSize().width, getMinimumSize().height);
 				} else {
 					if (SQUARE) {
 						setSize(SIZE, SIZE);
 					} else {
 						setSize(getWidth(), getHeight());
 					}
 				}
 			} else {
 				if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
 					setPreferredSize(getMinimumSize());
 				} else {
 					if (SQUARE) {
 						setPreferredSize(new Dimension(SIZE, SIZE));
 					} else {
 						setPreferredSize(new Dimension(getWidth(), getHeight()));
 					}
 				}
 			}
 			calcInnerBounds();
 			if (SQUARE) {
 				init(INNER_BOUNDS.width, INNER_BOUNDS.width);
 			} else {
 				init(INNER_BOUNDS.width, INNER_BOUNDS.height);
 			}
 		}
 	};
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Constructor">
 	public SplitFlap() {
 		addComponentListener(COMPONENT_LISTENER);
 		GlobalTimer.INSTANCE.addComponent(this);
 		INNER_BOUNDS = new Rectangle(0, 0, 22, 31);
 		THIN_STROKE = new BasicStroke(0.5f);
 		currentFlipSeqImage = 0;
 		selection = EXTENDED;
 		selectedSet = new ArrayList<String>(64);
 		currentSelectionIndex = 0;
 		nextSelectionIndex = 1;
 		previousSelectionIndex = selection.length - 1;
 		text = " ";
 		flipping = false;
 		flipComplete = false;
 		fontColor = new Color(0xFFE401);
 		CLIP = new Rectangle2D.Double();
 		TOP_CLIP = new Rectangle2D.Double();
 		BOTTOM_CLIP = new Rectangle2D.Double();
 
 		selectedSet.addAll(Arrays.asList(EXTENDED));
 
 		init(getWidth(), getHeight());
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Initialization">
 	private void init(final int WIDTH, final int HEIGHT) {
 		if (WIDTH <= 1 || HEIGHT <= 1) {
 			return;
 		}
 
 		if (backgroundImage != null) {
 			backgroundImage.flush();
 		}
 		backgroundImage = createBackgroundImage(WIDTH, HEIGHT);
 
 		if (foregroundImage != null) {
 			foregroundImage.flush();
 		}
 		foregroundImage = createForegroundImage(WIDTH, HEIGHT);
 
 		font = FlipImages.INSTANCE.getFont().deriveFont((0.6451612903f * HEIGHT)).deriveFont(Font.BOLD);
 
 		FlipImages.INSTANCE.recreateImages(WIDTH, HEIGHT);
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Visualization">
 	@Override
 	protected void paintComponent(Graphics g) {
 		final Graphics2D G2 = (Graphics2D) g.create();
 		G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		// Draw instrument background
 		G2.drawImage(backgroundImage, 0, 0, this);
 
 		// Get current clip
 		CLIP.setRect(G2.getClipBounds());
 
 		// Draw top character
 		G2.setClip(TOP_CLIP);
 		G2.setPaint(fontColor);
 		G2.setFont(font);
 		FontMetrics metrics = G2.getFontMetrics();
 		Rectangle2D charBounds = metrics.getStringBounds(selectedSet.get(currentSelectionIndex), G2);
 		G2.translate(getWidth() * 0.1111111111111111, getHeight() * 0.078);
 		G2.drawString(selectedSet.get(currentSelectionIndex), (float) ((TOP_CLIP.getWidth() - charBounds.getWidth()) / 2f), (float) (TOP_CLIP.getHeight() + (metrics.getHeight() / 2f) - metrics.getDescent()));
 		G2.translate(-getWidth() * 0.1111111111111111, -getHeight() * 0.078);
 
 		// Draw bottom character
 		G2.setClip(BOTTOM_CLIP);
 		G2.setPaint(fontColor);
 		G2.setFont(font);
 		metrics = G2.getFontMetrics();
 		if (!flipComplete) {
 			charBounds = metrics.getStringBounds(selectedSet.get(previousSelectionIndex), G2);
 			G2.translate((getWidth() * 0.1111111111111111), getHeight() * 0.49);
 			G2.drawString(selectedSet.get(previousSelectionIndex), (float) ((BOTTOM_CLIP.getWidth() - charBounds.getWidth()) / 2f), ((metrics.getHeight() / 2f) - metrics.getDescent()));
 			G2.translate((-getWidth() * 0.1111111111111111), -getHeight() * 0.49);
 		} else {
 			charBounds = metrics.getStringBounds(selectedSet.get(currentSelectionIndex), G2);
 			G2.translate((getWidth() * 0.1111111111111111), getHeight() * 0.49);
 			G2.drawString(selectedSet.get(currentSelectionIndex), (float) ((BOTTOM_CLIP.getWidth() - charBounds.getWidth()) / 2f), ((metrics.getHeight() / 2f) - metrics.getDescent()));
 			G2.translate((-getWidth() * 0.1111111111111111), -getHeight() * 0.49);
 		}
 
 		// Set clip back to original clip
 		G2.setClip(CLIP);
 
 		G2.drawImage(foregroundImage, 0, 0, this);
 
 		// Draw flip images
 		if (flipSequenceImage != null) {
 			G2.drawImage(flipSequenceImage, 0, 0, this);
 		}
 
 		G2.dispose();
 	}
 	//</editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Getters and Setters">
 	public Color getFontColor() {
 		return fontColor;
 	}
 
 	public void setFontColor(final Color FONT_COLOR) {
 		fontColor = FONT_COLOR;
 		repaint(INNER_BOUNDS);
 	}
 
 	public String[] getSelection() {
 		return selection;
 	}
 
 	public void setSelection(final String[] SELECTION) {
 		selectedSet.clear();
 		if (SELECTION.length == 0) {
 			selectedSet.addAll(Arrays.asList(EXTENDED));
 		} else {
 			selectedSet.addAll(Arrays.asList(SELECTION));
 		}
 		currentSelectionIndex = 0;
 		nextSelectionIndex = 1;
 		previousSelectionIndex = selectedSet.size() - 1;
 	}
 
 	public ArrayList<String> getSelectedSet() {
 		return selectedSet;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(final String TEXT) {
 		if (TEXT.isEmpty()) {
 			return;
 		}
 		String oldText = text;
 		if (!TEXT.isEmpty() || selectedSet.contains(TEXT.substring(0, 1))) {
 			text = TEXT.substring(0, 1);
 		} else {
 			text = selectedSet.get(0);
 		}
 		firePropertyChange(PROPERTY_TEXT, oldText, text);
 
 		if (!selectedSet.get(currentSelectionIndex).equals(text)) {
 			flipping = true;
 			flipComplete = true;
 		}
 	}
 
 	public final String getNextText() {
 		return selectedSet.get(nextSelectionIndex);
 	}
 
 	public final String getPreviousText() {
 		return selectedSet.get(previousSelectionIndex);
 	}
 
 	public final void flipForward() {
 		previousSelectionIndex = currentSelectionIndex;
 		currentSelectionIndex++;
 		if (currentSelectionIndex >= selectedSet.size()) {
 			currentSelectionIndex = 0;
 		}
 		nextSelectionIndex = currentSelectionIndex + 1;
 		if (nextSelectionIndex >= selectedSet.size()) {
 			nextSelectionIndex = 0;
 		}
 		setText(selectedSet.get(currentSelectionIndex));
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Image related">
 	private BufferedImage createBackgroundImage(final int WIDTH, final int HEIGHT) {
 		final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
 		final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.OPAQUE);
 		final Graphics2D G2 = IMAGE.createGraphics();
 
 		G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
 
 		// Background
 		final Point2D START_BACKGROUND = new Point2D.Float(0, 0);
 		final Point2D STOP_BACKGROUND = new Point2D.Float(0, HEIGHT);
 
 		final float[] FRACTIONS_BACKGROUND = {
 				0.0f,
 				1.0f
 		};
 
 		final Color[] COLORS_BACKGROUND = {
 				new Color(0x53524D),
 				new Color(0x3B4137)
 		};
 
 		final LinearGradientPaint BACKGROUND_FILL = new LinearGradientPaint(START_BACKGROUND, STOP_BACKGROUND,
 				FRACTIONS_BACKGROUND, COLORS_BACKGROUND);
 
 		G2.setPaint(BACKGROUND_FILL);
 		G2.fill(new Rectangle2D.Float(0, 0, WIDTH, HEIGHT));
 
 		// Highlight
 		final Point2D START_HIGHLIGHT = new Point2D.Double(0, 0.0322580645 * HEIGHT);
 		final Point2D STOP_HIGHLIGHT = new Point2D.Double(0, 0.9677419355 * HEIGHT);
 
 		final float[] FRACTIONS_HIGHLIGHT = {
 				0.0f,
 				0.03f,
 				0.97f,
 				1.0f
 		};
 
 		final Color[] COLORS_HIGHLIGHT = {
 				new Color(0x1C1910),
 				new Color(0x3E3B32),
 				new Color(0x3E3B32),
 				new Color(0x938B80),};
 
 		final LinearGradientPaint HIGHLIGHT_FILL = new java.awt.LinearGradientPaint(START_HIGHLIGHT, STOP_HIGHLIGHT,
 				FRACTIONS_HIGHLIGHT, COLORS_HIGHLIGHT);
 
 		G2.setPaint(HIGHLIGHT_FILL);
 		G2.fill(new Rectangle2D.Double(0.0444444444 * WIDTH, 0.0322580645 * HEIGHT,
 				0.9111111111 * WIDTH, 0.935483871 * HEIGHT));
 
 		// Inner Background
 		final Point2D START_INNER_BACKGROUND = new Point2D.Double(0, 0.0483870968 * HEIGHT);
 		final Point2D STOP_INNER_BACKGROUND = new Point2D.Double(0, 0.9516129032 * HEIGHT);
 
 		final float[] FRACTIONS_INNER_BACKGROUND = {
 				0.0f,
 				0.02f,
 				0.96f,
 				0.98f,
 				1.0f
 		};
 
 		final Color[] COLORS_INNER_BACKGROUND = {
 				new Color(39, 39, 39, 255),
 				new Color(0, 0, 0, 255),
 				new Color(0, 0, 0, 255),
 				new Color(101, 101, 101, 255),
 				new Color(0, 0, 0, 255)
 		};
 
 		final LinearGradientPaint INNER_BACKGROUND_FILL = new LinearGradientPaint(START_INNER_BACKGROUND, STOP_INNER_BACKGROUND,
 				FRACTIONS_INNER_BACKGROUND, COLORS_INNER_BACKGROUND);
 
 		G2.setPaint(INNER_BACKGROUND_FILL);
 		G2.fill(new Rectangle2D.Double(0.0666666667 * WIDTH, 0.0483870968 * HEIGHT,
 				0.8666666667 * WIDTH, 0.9032258065 * HEIGHT));
 
 		// Top
 		G2.translate(getWidth() * 0.1111111111111111, getHeight() * 0.08064516129032258);
 		final GeneralPath TOP = new GeneralPath();
 		TOP.moveTo(0, HEIGHT * 0.4032258065 * 0.12);
 		TOP.quadTo(0, 0, HEIGHT * 0.4032258065 * 0.12, 0);
 		TOP.lineTo(WIDTH * 0.77777777777 * 0.9142857142857143, 0);
 		TOP.quadTo(WIDTH * 0.77777777777, 0, WIDTH * 0.77777777777, HEIGHT * 0.4032258065 * 0.12);
 		TOP.lineTo(WIDTH * 0.77777777777, HEIGHT * 0.4032258065 * 0.76);
 		TOP.lineTo(WIDTH * 0.77777777777 * 0.9714285714285714, HEIGHT * 0.4032258065 * 0.76);
 		TOP.lineTo(WIDTH * 0.77777777777 * 0.9714285714285714, HEIGHT * 0.4032258065);
 		TOP.lineTo(WIDTH * 0.77777777777 * 0.02857142857142857, HEIGHT * 0.4032258065);
 		TOP.lineTo(WIDTH * 0.77777777777 * 0.02857142857142857, HEIGHT * 0.4032258065 * 0.76);
 		TOP.lineTo(0, HEIGHT * 0.4032258065 * 0.76);
 		TOP.closePath();
 
 		final Point2D TOP_START = new Point2D.Double(0, TOP.getBounds2D().getMinY());
 		final Point2D TOP_STOP = new Point2D.Double(0, TOP.getBounds2D().getMaxY());
 
 		final float[] TOP_FRACTIONS = {
 				0.0f,
 				0.03f,
 				0.98f,
 				1.0f
 		};
 
 		final Color[] TOP_COLORS = {
 				new Color(98, 98, 98, 255),
 				new Color(8, 8, 8, 255),
 				new Color(38, 38, 38, 255),
 				new Color(64, 64, 64, 255)
 		};
 
 		final LinearGradientPaint TOP_FILL = new LinearGradientPaint(TOP_START, TOP_STOP,
 				TOP_FRACTIONS, TOP_COLORS);
 
 		G2.setPaint(TOP_FILL);
 		G2.fill(TOP);
 		G2.translate(-getWidth() * 0.1111111111111111, -getHeight() * 0.08064516129032258);
 
 		// Bottom
 		G2.translate((getWidth() * 0.1111111111111111), getHeight() * 0.5161290322580645);
 		final GeneralPath BOTTOM = new GeneralPath();
 		BOTTOM.moveTo(WIDTH * 0.77777777777 * 0.02857142857142857, 0);
 		BOTTOM.lineTo(WIDTH * 0.77777777777 * 0.9714285714285714, 0);
 		BOTTOM.lineTo(WIDTH * 0.77777777777 * 0.9714285714285714, HEIGHT * 0.4032258065 * 0.24);
 		BOTTOM.lineTo(WIDTH * 0.77777777777, HEIGHT * 0.4032258065 * 0.24);
 		BOTTOM.lineTo(WIDTH * 0.77777777777, HEIGHT * 0.4032258065 * 0.88);
 		BOTTOM.quadTo(WIDTH * 0.77777777777, HEIGHT * 0.4032258065, WIDTH * 0.77777777777 * 0.9142857142857143, HEIGHT * 0.4032258065);
 		BOTTOM.lineTo(WIDTH * 0.77777777777 * 0.08571428571428572, HEIGHT * 0.4032258065);
 		BOTTOM.quadTo(0, HEIGHT * 0.4032258065, 0, HEIGHT * 0.4032258065 * 0.88);
 		BOTTOM.lineTo(0, HEIGHT * 0.4032258065 * 0.24);
 		BOTTOM.lineTo(WIDTH * 0.77777777777 * 0.02857142857142857, HEIGHT * 0.4032258065 * 0.24);
 		BOTTOM.closePath();
 
 		final Point2D BOTTOM_START = new Point2D.Double(0, BOTTOM.getBounds2D().getMinY());
 		final Point2D BOTTOM_STOP = new Point2D.Double(0, BOTTOM.getBounds2D().getMaxY());
 
 		final float[] BOTTOM_FRACTIONS = {
 				0.0f,
 				0.03f,
 				0.06f,
 				0.92f,
 				0.96f,
 				1.0f
 		};
 
 		final Color[] BOTTOM_COLORS = {
 				new Color(81, 81, 81, 255),
 				new Color(62, 62, 62, 255),
 				new Color(62, 62, 62, 255),
 				new Color(89, 89, 89, 255),
 				new Color(85, 85, 85, 255),
 				new Color(112, 112, 112, 255)
 		};
 
 		final LinearGradientPaint BOTTOM_FILL = new LinearGradientPaint(BOTTOM_START, BOTTOM_STOP,
 				BOTTOM_FRACTIONS, BOTTOM_COLORS);
 
 		G2.setPaint(BOTTOM_FILL);
 		G2.fill(BOTTOM);
 		G2.translate((-getWidth() * 0.1111111111111111), -getHeight() * 0.5161290322580645);
 
 		TOP_CLIP.setRect(WIDTH * 0.1111111111111111, HEIGHT * 0.08064516129032258,
 				TOP.getBounds2D().getWidth(), TOP.getBounds2D().getHeight());
 		BOTTOM_CLIP.setRect(WIDTH * 0.1111111111111111, HEIGHT * 0.5161290322580645,
 				BOTTOM.getBounds2D().getWidth(), BOTTOM.getBounds2D().getHeight());
 
 		G2.dispose();
 
 		return IMAGE;
 	}
 
 	private BufferedImage createForegroundImage(final int WIDTH, final int HEIGHT) {
 		final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
 		final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
 		final Graphics2D G2 = IMAGE.createGraphics();
 
 		final int IMAGE_WIDTH = IMAGE.getWidth();
 		final int IMAGE_HEIGHT = IMAGE.getHeight();
 
 		G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
 
 		// Draw flip axis
 		G2.setStroke(THIN_STROKE);
 		G2.setColor(new Color(0x999999));
 		G2.draw(new Line2D.Double(0.1333333333 * WIDTH, 0.4838709677 * HEIGHT,
 				0.84444444 * WIDTH, 0.4838709677 * HEIGHT));
 		G2.setColor(Color.BLACK);
 		G2.draw(new Line2D.Double(0.1333333333 * WIDTH, 0.5 * HEIGHT,
 				0.84444444 * WIDTH, 0.5 * HEIGHT));
 
 		// Draw side bars
 		final Rectangle2D SIDEBARBACK_RIGHT = new Rectangle2D.Double(IMAGE_WIDTH * 0.9278350515463918, IMAGE_HEIGHT * 0.34459459459459457,
 				IMAGE_WIDTH * 0.061855670103092786, IMAGE_HEIGHT * 0.32432432432432434);
 		final Point2D SIDEBARBACK_RIGHT_START = new Point2D.Double(0, SIDEBARBACK_RIGHT.getBounds2D().getMinY());
 		final Point2D SIDEBARBACK_RIGHT_STOP = new Point2D.Double(0, SIDEBARBACK_RIGHT.getBounds2D().getMaxY());
 		final float[] SIDEBARBACK_FRACTIONS = {
 				0.0f,
 				0.29f,
 				1.0f
 		};
 		final Color[] SIDEBARBACK_COLORS = {
 				new Color(0, 0, 0, 255),
 				new Color(73, 74, 77, 255),
 				new Color(0, 0, 0, 255)
 		};
 		final LinearGradientPaint SIDEBARBACK_RIGHT_FILL = new LinearGradientPaint(SIDEBARBACK_RIGHT_START, SIDEBARBACK_RIGHT_STOP,
 				SIDEBARBACK_FRACTIONS, SIDEBARBACK_COLORS);
 		G2.setPaint(SIDEBARBACK_RIGHT_FILL);
 		G2.fill(SIDEBARBACK_RIGHT);
 
 		final Rectangle2D SIDEBARFRONT_RIGHT = new Rectangle2D.Double(IMAGE_WIDTH * 0.9381443298969072, IMAGE_HEIGHT * 0.34797297297297297,
 				IMAGE_WIDTH * 0.041237113402061855, IMAGE_HEIGHT * 0.3108108108108108);
 		final Point2D SIDEBARFRONT_RIGHT_START = new Point2D.Double(0, SIDEBARFRONT_RIGHT.getBounds2D().getMinY());
 		final Point2D SIDEBARFRONT_RIGHT_STOP = new Point2D.Double(0, SIDEBARFRONT_RIGHT.getBounds2D().getMaxY());
 		final float[] SIDEBARFRONT_FRACTIONS = {
 				0.0f,
 				0.28f,
 				0.57f,
 				0.93f,
 				0.96f,
 				1.0f
 		};
 		final Color[] SIDEBARFRONT_COLORS = {
 				new Color(32, 30, 31, 255),
 				new Color(116, 117, 121, 255),
 				new Color(30, 31, 31, 255),
 				new Color(30, 31, 31, 255),
 				new Color(51, 45, 48, 255),
 				new Color(15, 13, 14, 255)
 		};
 		final LinearGradientPaint SIDEBARFRONT_RIGHT_FILL = new LinearGradientPaint(SIDEBARFRONT_RIGHT_START, SIDEBARFRONT_RIGHT_STOP,
 				SIDEBARFRONT_FRACTIONS, SIDEBARFRONT_COLORS);
 		G2.setPaint(SIDEBARFRONT_RIGHT_FILL);
 		G2.fill(SIDEBARFRONT_RIGHT);
 
 		final Rectangle2D SIDEBARBACK_LEFT = new Rectangle2D.Double(IMAGE_WIDTH * 0.010309278350515464, IMAGE_HEIGHT * 0.34459459459459457,
 				IMAGE_WIDTH * 0.061855670103092786, IMAGE_HEIGHT * 0.32432432432432434);
 		final Point2D SIDEBARBACK_LEFT_START = new Point2D.Double(0, SIDEBARBACK_LEFT.getBounds2D().getMinY());
 		final Point2D SIDEBARBACK_LEFT_STOP = new Point2D.Double(0, SIDEBARBACK_LEFT.getBounds2D().getMaxY());
 
 		final LinearGradientPaint SIDEBARBACK_LEFT_FILL = new LinearGradientPaint(SIDEBARBACK_LEFT_START, SIDEBARBACK_LEFT_STOP,
 				SIDEBARBACK_FRACTIONS, SIDEBARBACK_COLORS);
 		G2.setPaint(SIDEBARBACK_LEFT_FILL);
 		G2.fill(SIDEBARBACK_LEFT);
 
 		final Rectangle2D SIDEBARFRONT_LEFT = new Rectangle2D.Double(IMAGE_WIDTH * 0.020618556701030927, IMAGE_HEIGHT * 0.34797297297297297,
 				IMAGE_WIDTH * 0.041237113402061855, IMAGE_HEIGHT * 0.3108108108108108);
 		final Point2D SIDEBARFRONT_LEFT_START = new Point2D.Double(0, SIDEBARFRONT_LEFT.getBounds2D().getMinY());
 		final Point2D SIDEBARFRONT_LEFT_STOP = new Point2D.Double(0, SIDEBARFRONT_LEFT.getBounds2D().getMaxY());
 
 		final LinearGradientPaint SIDEBARFRONT_LEFT_FILL = new LinearGradientPaint(SIDEBARFRONT_LEFT_START, SIDEBARFRONT_LEFT_STOP,
 				SIDEBARFRONT_FRACTIONS, SIDEBARFRONT_COLORS);
 		G2.setPaint(SIDEBARFRONT_LEFT_FILL);
 		G2.fill(SIDEBARFRONT_LEFT);
 
 		G2.dispose();
 
 		return IMAGE;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Size related">
 
 	/**
 	 * Calculates the area that is available for painting the display
 	 */
 	private void calcInnerBounds() {
 		final java.awt.Insets INSETS = getInsets();
 		INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
 	}
 
 	/**
 	 * Returns a rectangle2d representing the available space for drawing the component taking the insets into account
 	 * (e.g. given through borders etc.)
 	 *
 	 * @return rectangle2d that represents the area available for rendering the component
 	 */
 	private Rectangle getInnerBounds() {
 		return INNER_BOUNDS;
 	}
 
 	@Override
 	public Dimension getMinimumSize() {
 		return new java.awt.Dimension(22, 31);
 	}
 
 	@Override
 	public void setPreferredSize(final Dimension DIM) {
 		super.setPreferredSize(DIM);
 		calcInnerBounds();
 		init(DIM.width, DIM.height);
 	}
 
 	@Override
 	public void setSize(final int WIDTH, final int HEIGHT) {
 		super.setSize(WIDTH, HEIGHT);
 		calcInnerBounds();
 		init(WIDTH, HEIGHT);   // Rectangular component
 	}
 
 	@Override
 	public void setSize(final Dimension DIM) {
 		super.setSize(DIM);
 		calcInnerBounds();
 		init(DIM.width, DIM.height);
 	}
 
 	@Override
 	public void setBounds(final Rectangle BOUNDS) {
 		super.setBounds(BOUNDS);
 		calcInnerBounds();
 		init(BOUNDS.width, BOUNDS.height);
 	}
 
 	@Override
 	public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
 		super.setBounds(X, Y, WIDTH, HEIGHT);
 		calcInnerBounds();
 		init(WIDTH, HEIGHT);
 	}
 
 	public void actionPerformed(final ActionEvent EVENT) {
 		if (EVENT.getActionCommand().equals("flip") && flipping) {
 			previousSelectionIndex = currentSelectionIndex;
 			currentSelectionIndex++;
 			if (currentSelectionIndex >= selectedSet.size()) {
 				currentSelectionIndex = 0;
 			}
 			nextSelectionIndex = currentSelectionIndex + 1;
 			if (nextSelectionIndex >= selectedSet.size()) {
 				nextSelectionIndex = 0;
 			}
 
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					repaint(INNER_BOUNDS);
 				}
 			});
 			flipComplete = false;
 
 			if (selectedSet.get(currentSelectionIndex).equals(text)) {
 				flipping = false;
 			}
 		}
 
 		if (EVENT.getActionCommand().equals("flipSequence") && !flipComplete) {
 			if (currentFlipSeqImage == 9) {
 				currentFlipSeqImage = 0;
 				flipSequenceImage = null;
 				flipComplete = true;
 				repaint(INNER_BOUNDS);
 			} else {
 				flipSequenceImage = FlipImages.INSTANCE.getFlipImageArray()[currentFlipSeqImage];
 				currentFlipSeqImage++;
 				repaint(INNER_BOUNDS);
 			}
 		}
 	}
 	// </editor-fold>
 }
