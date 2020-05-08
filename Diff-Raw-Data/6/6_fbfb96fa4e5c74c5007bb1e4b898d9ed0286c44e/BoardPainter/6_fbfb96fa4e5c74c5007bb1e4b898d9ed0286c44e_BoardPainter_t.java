 package pleocmd.itfc.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.UIManager;
 
 import pleocmd.ImmutableRectangle;
 import pleocmd.itfc.gui.icons.IconLoader;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.PipePart;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 public final class BoardPainter {
 
 	private static final boolean PAINT_DEBUG = false;
 
 	// Main colors
 
 	/**
 	 * Background color.
 	 */
 	private static final Color BACKGROUND = Color.LIGHT_GRAY;
 
 	/**
 	 * Color for target-area which doesn't involve a reordering of the Pipe.
 	 */
 	private static final Color MOVEMENT_HINT = new Color(192, 208, 192);
 
 	// Drawing of section borders
 
 	/**
 	 * Color of section delimiter.
 	 */
 	private static final Color SECT_BORDER = Color.BLACK;
 
 	/**
 	 * Amount of fragments when calculating section borders, i.e. parts of the
 	 * whole width to use for the first and last section.
 	 */
 	private static final int SECTION_FRAG = 5;
 
 	/**
 	 * Amount of pixels to try to keep free when calculating section borders.
 	 */
 	private static final int SECTION_SPACE = 20;
 
 	// Drawing of shadows
 
 	/**
 	 * Color of shadows.
 	 */
 	private static final Color SHADOW_COLOR = Color.GRAY;
 
 	/**
 	 * Visual depth of shadows in pixels.
 	 */
 	private static final int SHADOW_DEPTH = 4;
 
 	/**
 	 * Whether to draw a shadow for top-to-down hint arrow.
 	 */
 	private static final boolean SHADOW_ORDERHINT = true;
 
 	/**
 	 * Whether to draw a shadow for PipeParts.
 	 */
 	private static final boolean SHADOW_RECTS = true;
 
 	/**
 	 * Whether to draw a shadow for connections.
 	 */
 	private static final boolean SHADOW_CONNECTIONS = true;
 
 	/**
 	 * Whether to draw a shadow for connections' labels.
 	 */
 	private static final boolean SHADOW_CONNECTIONS_LABEL = false;
 
 	// Order Hint Arrow
 
 	/**
 	 * Background color of top-to-down hint arrow.
 	 */
 	private static final Color ORDER_HINT_BACK = new Color(255, 255, 128);
 
 	/**
 	 * Width of top-to-down hint arrow's trunk relative to first section width.
 	 */
 	private static final double ORDER_HINT_TRUNK_WIDTH = 0.3;
 
 	/**
 	 * Height of top-to-down hint arrow's trunk relative to the boards height.
 	 */
 	private static final double ORDER_HINT_TRUNK_HEIGHT = 0.65;
 
 	/**
 	 * Width of top-to-down hint arrow's head relative to first section width.
 	 */
 	private static final double ORDER_HINT_ARROW_WIDTH = 0.3;
 
 	/**
 	 * Height of top-to-down hint arrow's head relative to the boards height.
 	 */
 	private static final double ORDER_HINT_ARROW_HEIGHT = 0.3;
 
 	// Drawing of icons inside a PipePart
 
 	/**
 	 * Color of the rectangle around a icon which is not selected nor hovered.
 	 */
 	private static final Color ICON_OUTLINE = new Color(128, 128, 0);
 
 	/**
 	 * Color of the rectangle around a icon which is not selected but hovered.
 	 */
 	private static final Color ICON_OUTLINE_HOVER = new Color(255, 255, 128);
 
 	/**
 	 * Color of the rectangle around a icon which is selected but not hovered.
 	 */
 	private static final Color ICON_OUTLINE_SEL = new Color(128, 128, 0);
 
 	/**
 	 * Color of the rectangle around a icon which is selected and hovered.
 	 */
 	private static final Color ICON_OUTLINE_SEL_HOVER = new Color(255, 255, 128);
 
 	/**
 	 * General width of an icon.
 	 */
 	private static final int ICON_WIDTH = 18;
 
 	/**
 	 * Maximal amount of icons possible inside a PipePart.
 	 */
 	private static final int ICON_MAX = 4;
 
 	/**
 	 * Configuration icon.
 	 */
 	static final Icon ICON_CONF = IconLoader.getIcon("configure");
 	// CS_IGNORE_PREV sorted by type, not visibility
 
 	/**
 	 * Position of the Configuration icon.
 	 */
 	private static final int ICON_CONF_POS = -1;
 
 	/**
 	 * Visualization icon.
 	 */
 	static final Icon ICON_DGR = IconLoader.getIcon("games-difficult");
 	// CS_IGNORE_PREV sorted by type, not visibility
 
 	/**
 	 * Position of the Visualization icon.
 	 */
 	private static final int ICON_DGR_POS = 0;
 
 	// Drawing of a PipePart
 
 	/**
 	 * Background color of a PipePart.
 	 */
 	private static final Color RECT_BACKGROUND = new Color(255, 255, 255);
 
 	/**
 	 * Background color of inner section and icons of a modifiable PipePart.
 	 */
 	private static final Color INNER_MODIFIABLE = new Color(200, 200, 255);
 
 	/**
 	 * Background color of inner section and icons of a read-only PipePart.
 	 */
 	private static final Color INNER_READONLY = Color.LIGHT_GRAY;
 
 	/**
 	 * Color of PipePart's border if the PipePart is sane but not selected.
 	 */
 	private static final Color OUTER_OK = Color.BLACK;
 
 	/**
 	 * Color of PipePart's border if the PipePart is not sane nor selected.
 	 */
 	private static final Color OUTER_BAD = Color.RED;
 
 	/**
 	 * Color of PipePart's border if the PipePart is sane and selected.
 	 */
 	private static final Color OUTER_SEL_OK = Color.BLUE;
 
 	/**
 	 * Color of PipePart's border if the PipePart is not sane but selected.
 	 */
 	private static final Color OUTER_SEL_BAD = Color.MAGENTA;
 
 	/**
 	 * Maximal width of a PipePart's rectangle in pixel.
 	 */
 	private static final int MAX_RECT_WIDTH = 200;
 
 	/**
 	 * Amount of pixels between inner and outer part left and right of a
 	 * PipePart's rectangle.
 	 */
 	private static final int INNER_WIDTH = ICON_WIDTH + 2;
 
 	/**
 	 * Amount of pixels between inner and outer part top and bottom of a
 	 * PipePart's rectangle.
 	 */
 	private static final int INNER_HEIGHT = 6;
 
 	// Drawing of connections
 
 	/**
 	 * Color of a connections whose PipePart is sane but not selected.
 	 */
 	private static final Color CONNECTION_OK = Color.BLACK;
 
 	/**
 	 * Color of a connections whose PipePart is not sane nor selected.
 	 */
 	private static final Color CONNECTION_BAD = Color.RED;
 
 	/**
 	 * Color of a connections whose PipePart is sane and selected.
 	 */
 	private static final Color CONNECTION_SEL_OK = Color.BLUE;
 
 	/**
 	 * Color of a connections whose PipePart is not sane but selected.
 	 */
 	private static final Color CONNECTION_SEL_BAD = Color.MAGENTA;
 
 	/**
 	 * Thickness of the arrows' head of a connector.
 	 */
 	private static final int CONN_ARROW_HEAD = 14;
 
 	/**
 	 * Thickness of the arrows' wings of a connector.
 	 */
 	private static final int CONN_ARROW_WING = 8;
 
 	// Miscellaneous
 
 	private final Set<PipePart> set;
 
 	private final Set<PipePart> saneConfigCache;
 
 	private final Dimension bounds = new Dimension();
 
 	private int border1;
 
 	private int border2;
 
 	private double scale = 1.0;
 
 	private int grayVal = 128;
 
 	private Pipe pipe;
 
 	public BoardPainter() {
 		set = new HashSet<PipePart>();
 		saneConfigCache = new HashSet<PipePart>();
 	}
 
 	public void paint(final Graphics g, final PipePart currentPart,
 			final PipePart underCursor,
 			final ImmutableRectangle currentConnection,
 			final PipePart currentConnectionsTarget,
 			final boolean currentConnectionValid,
 			final BoardAutoLayouter layouter, final boolean modifyable) {
 		if (pipe == null || scale <= Double.MIN_NORMAL) return;
 		Rectangle clip = g.getClipBounds();
 		if (clip == null)
 			clip = new Rectangle(0, 0, bounds.width, bounds.height);
 		final BufferedImage img = new BufferedImage(clip.width, clip.height,
 				BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g2 = img.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 		g2.setClip(0, 0, clip.width, clip.height);
 
 		final Rectangle clipOrg = new Rectangle((int) (clip.x / scale),
 				(int) (clip.y / scale), (int) (clip.width / scale),
 				(int) (clip.height / scale));
 
 		final long start = System.currentTimeMillis();
 		if (PAINT_DEBUG) {
 			grayVal = (grayVal - 118) % 64 + 128;
 			g2.setColor(new Color(grayVal, grayVal, grayVal));
 		} else
 			g2.setColor(BACKGROUND);
 		g2.fillRect(0, 0, clip.width, clip.height);
 		g2.translate(-clip.x, -clip.y);
 		g2.scale(scale, scale);
 		drawMovementHint(g2, currentPart);
 		final long time1 = System.currentTimeMillis();
 
 		if (clip.x < border1) drawOrderingHint(g2);
 		final long time2 = System.currentTimeMillis();
 		drawSectionBorders(g2);
 		final long time3 = System.currentTimeMillis();
 		final int cnt4 = drawPipeParts(g2, clipOrg, currentPart, underCursor,
 				modifyable);
 		final long time4 = System.currentTimeMillis();
 		final int cnt5 = drawConnections(g2, clipOrg, currentPart,
 				currentConnection, underCursor, currentConnectionsTarget,
 				currentConnectionValid);
 		final long time5 = System.currentTimeMillis();
 		drawAutoLayoutInfo(g2, layouter);
 
 		if (PAINT_DEBUG) {
 			g2.translate(clip.x / scale, clip.y / scale);
 			drawDebugTime(g2, time1 - start, -1, "Background", 1);
 			drawDebugTime(g2, time2 - time1, -1, "Hint", 2);
 			drawDebugTime(g2, time3 - time2, -1, "Border", 3);
 			drawDebugTime(g2, time4 - time3, cnt4, "Parts", 4);
 			drawDebugTime(g2, time5 - time4, cnt5, "Conn's", 5);
 		}
 
 		g.drawImage(img, clip.x, clip.y, null);
 	}
 
 	private void drawDebugTime(final Graphics2D g2, final long elapsed,
 			final int count, final String name, final int pos) {
 		final Font f = g2.getFont();
 		g2.setFont(f.deriveFont(10f));
 		g2.setColor(Color.GREEN);
 		final String str = count == -1 ? name : String.format("%d %s", count,
 				name);
 		g2.drawString(String.format("%d ms for %s", elapsed, str), 0, 10 * pos);
 		g2.setFont(f);
 	}
 
 	private void drawMovementHint(final Graphics2D g2,
 			final PipePart currentPart) {
 		final List<? extends PipePart> list;
 		final int x0;
 		final int x1;
 		if (currentPart instanceof Input) {
 			list = pipe.getInputList();
 			x0 = 0;
 			x1 = border1;
 		} else if (currentPart instanceof Converter) {
 			list = pipe.getConverterList();
 			x0 = border1;
 			x1 = border2;
 		} else if (currentPart instanceof Output) {
 			list = pipe.getOutputList();
 			x0 = border2;
 			x1 = (int) (bounds.width / scale);
 		} else
 			return;
 		final int idx = list.indexOf(currentPart);
 		final PipePart before = idx > 0 ? list.get(idx - 1) : null;
 		final PipePart after = idx < list.size() - 1 ? list.get(idx + 1) : null;
 
 		final int y0 = before == null ? 0 : before.getGuiPosition().getY()
 				+ before.getGuiPosition().getHeight() + 1;
 		final int y1 = after == null ? (int) (bounds.height / scale) : after
 				.getGuiPosition().getY() - 1;
 		final Rectangle r = new Rectangle(x0, y0, x1 - x0, y1 - y0);
 		if (PAINT_DEBUG)
 			g2.setColor(new Color(grayVal, grayVal + 16, grayVal));
 		else
 			g2.setColor(MOVEMENT_HINT);
 		g2.fill(r);
 	}
 
 	private void drawOrderingHint(final Graphics2D g2) {
 		final double w = border1;
 		final double h = bounds.height / scale;
 		final int tw = (int) (w * ORDER_HINT_TRUNK_WIDTH);
 		final int th = (int) (h * ORDER_HINT_TRUNK_HEIGHT);
 		final int aw = (int) (w * ORDER_HINT_ARROW_WIDTH);
 		final int ah = (int) (h * ORDER_HINT_ARROW_HEIGHT);
 		final int cw = tw + 2 * aw;
 		final int ch = th + ah;
 		final int ow = (int) ((w - cw) / 2);
 		final int oh = (int) ((h - ch) / 2);
 		final Polygon p = new Polygon();
 		p.addPoint(ow + aw, oh);
 		p.addPoint(ow + aw + tw, oh);
 		p.addPoint(ow + aw + tw, oh + th);
 		p.addPoint(ow + aw + tw + aw, oh + th);
 		p.addPoint(ow + aw + tw / 2, oh + th + ah);
 		p.addPoint(ow, oh + th);
 		p.addPoint(ow + aw, oh + th);
 		p.addPoint(ow + aw, oh);
 
 		if (SHADOW_ORDERHINT && SHADOW_DEPTH > 0) {
 			final AffineTransform at = g2.getTransform();
 			g2.translate(SHADOW_DEPTH, SHADOW_DEPTH);
 			g2.setColor(SHADOW_COLOR);
 			g2.fillPolygon(p);
 			g2.setTransform(at);
 		}
 
 		g2.setColor(ORDER_HINT_BACK);
 		g2.fillPolygon(p);
 	}
 
 	private void drawSectionBorders(final Graphics2D g2) {
 		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
 		g2.setColor(SECT_BORDER);
 		final int h = (int) (bounds.height / scale + 0.5);
 		g2.drawLine(border1, 0, border1, h);
 		g2.drawLine(border2, 0, border2, h);
 	}
 
 	private int drawPipeParts(final Graphics2D g2, final Rectangle clip,
 			final PipePart currentPart, final PipePart underCursor,
 			final boolean modifyable) {
 		int cnt = 0;
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_BEVEL, 0, null, 0));
 		for (final PipePart pp : set)
 			cnt += drawPipePart(g2, pp, pp == underCursor, clip, currentPart,
 					modifyable);
 		return cnt;
 	}
 
 	private int drawConnections(final Graphics2D g2, final Rectangle clip,
 			final PipePart currentPart,
 			final ImmutableRectangle currentConnection,
 			final PipePart underCursor,
 			final PipePart currentConnectionsTarget,
 			final boolean currentConnectionValid) {
 		int cnt = 0;
 		for (final PipePart src : set)
 			for (final PipePart trg : src.getConnectedPipeParts()) {
 				final boolean sel = currentPart == src
 						&& currentConnectionsTarget == trg;
 				if (saneConfigCache.contains(src))
 					g2.setColor(sel ? CONNECTION_SEL_OK : CONNECTION_OK);
 				else
 					g2.setColor(sel ? CONNECTION_SEL_BAD : CONNECTION_BAD);
 				cnt += drawConnection(g2, src.getGuiPosition(), trg
 						.getGuiPosition(), src, trg, clip);
 			}
 		if (currentConnection != null && currentConnectionsTarget == null) {
 			g2.setColor(currentConnectionValid ? CONNECTION_SEL_OK
 					: CONNECTION_SEL_BAD);
 			cnt += drawConnection(g2, currentPart.getGuiPosition(),
 					currentConnection, currentPart, underCursor, clip);
 		}
 		return cnt;
 	}
 
 	private int drawPipePart(final Graphics2D g2, final PipePart part,
 			final boolean hover, final Rectangle clip,
 			final PipePart currentPart, final boolean modifyable) {
 		final ImmutableRectangle rect = part.getGuiPosition();
 		if (!rect.intersects(clip)) return 0;
 
 		final Color outerClr;
 		if (saneConfigCache.contains(part))
 			outerClr = part == currentPart ? OUTER_SEL_OK : OUTER_OK;
 		else
 			outerClr = part == currentPart ? OUTER_SEL_BAD : OUTER_BAD;
 
 		if (SHADOW_RECTS && SHADOW_DEPTH > 0) {
 			final AffineTransform at = g2.getTransform();
 			g2.translate(SHADOW_DEPTH, SHADOW_DEPTH);
 			g2.setColor(SHADOW_COLOR);
 			rect.fill(g2);
 			g2.setTransform(at);
 		}
 
 		g2.setColor(RECT_BACKGROUND);
 		rect.fill(g2);
 
 		g2.setColor(outerClr);
 		rect.draw(g2);
 
 		final String s = part.getName();
 		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);
 
 		if (hover) {
 			final Rectangle inner = rect.createCopy();
 			inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
 			g2.setColor(modifyable ? INNER_MODIFIABLE : INNER_READONLY);
 			g2.fill(inner);
 		}
 
 		// restrict drawing to inside the rectangle
 		final Shape shape = g2.getClip();
 		rect.asClip(g2);
 
 		// draw main icon
 		Icon mainIcon = part.getIcon();
 		if (mainIcon == null) {
 			final BufferedImage img = new BufferedImage(rect.getHeight(), rect
 					.getHeight(), BufferedImage.TYPE_INT_ARGB);
 			final Icon cfgImage = part.getConfigImage();
 			if (cfgImage instanceof ImageIcon) {
 				final Graphics2D g = img.createGraphics();
 				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 				g.drawImage(((ImageIcon) cfgImage).getImage(), 0, 0, rect
 						.getHeight(), rect.getHeight(), null);
 				mainIcon = new ImageIcon(img);
 			} else
 				mainIcon = IconLoader.getMissingIcon();
 		}
 		mainIcon.paintIcon(null, g2, rect.getX(), rect.getY()
 				+ (rect.getHeight() - mainIcon.getIconHeight()) / 2);
 
 		// draw name of PipePart
 		g2.setColor(outerClr);
 		g2.drawString(s, (float) (rect.getX() + (rect.getWidth() - sb
 				.getWidth()) / 2), (float) (rect.getY() + sb.getHeight()));
 
 		// draw clickable icons
 		// TODO hover should be per icon here
 		drawIcon(g2, hover, rect, ICON_CONF, ICON_CONF_POS, !modifyable
 				|| part.getGuiConfigs().isEmpty(), false, modifyable);
 		drawIcon(g2, hover, rect, ICON_DGR, ICON_DGR_POS, false, part
 				.isVisualize(), modifyable);
 
 		// restore old clip
 		g2.setClip(shape);
 		return 1;
 	}
 
 	/**
 	 * Draws an icon to the image
 	 * 
 	 * @param g2
 	 *            {@link Graphics2D} of the image
 	 * @param hover
 	 *            if true, an outline will be drawn
 	 * @param rect
 	 *            position of the image in which to align the icon
 	 * @param icon
 	 *            {@link Icon} to draw
 	 * @param pos
 	 *            the position inside the rectangle as follow<br>
 	 *            [ 1 2 3 ..... -2 -1 0 ]
 	 * @param disabled
 	 *            if true, icon is drawn in disabled state
 	 * @param selected
 	 *            if true, icon is drawn in selected state
 	 * @param modifyable
 	 *            determines the color of the icon's background
 	 */
 	private void drawIcon(final Graphics2D g2, final boolean hover,
 			final ImmutableRectangle rect, final Icon icon, final int pos,
 			final boolean disabled, final boolean selected,
 			final boolean modifyable) {
 		final Rectangle b = getIconBounds(rect, icon, pos);
 		if (hover) {
 			g2.setColor(modifyable ? INNER_MODIFIABLE : INNER_READONLY);
 			g2.fill(b);
 		}
 		Icon ico = disabled ? (selected ? UIManager.getLookAndFeel()
 				.getDisabledSelectedIcon(null, icon) : UIManager
 				.getLookAndFeel().getDisabledIcon(null, icon)) : icon;
 		if (ico == null) ico = icon;
 		ico.paintIcon(null, g2, b.x, b.y);
 		if (hover)
 			g2.setColor(selected ? ICON_OUTLINE_SEL_HOVER : ICON_OUTLINE_HOVER);
 		else
 			g2.setColor(selected ? ICON_OUTLINE_SEL : ICON_OUTLINE);
 		g2.draw3DRect(b.x, b.y, b.width, b.height, !selected);
 	}
 
 	private int drawConnection(final Graphics2D g2,
 			final ImmutableRectangle srcRect, final ImmutableRectangle trgRect,
 			final PipePart src, final PipePart trg, final Rectangle clip) {
 		if (!srcRect.union(trgRect.createCopy()).intersects(clip)) return 0;
 
 		final Point srcPoint = new Point();
 		final Point trgPoint = new Point();
 		calcConnectorPositions(srcRect, trgRect, srcPoint, trgPoint);
 		final Polygon arrow = getArrowPolygon(srcPoint.x, srcPoint.y,
 				trgPoint.x, trgPoint.y);
 
 		if (SHADOW_CONNECTIONS && SHADOW_DEPTH > 0) {
 			final Color clr = g2.getColor();
 			final AffineTransform at = g2.getTransform();
 			g2.translate(SHADOW_DEPTH / 2, SHADOW_DEPTH / 2);
 			g2.setColor(SHADOW_COLOR);
 			g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
 			g2.fillPolygon(arrow);
 			drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x,
 					trgPoint.y, SHADOW_CONNECTIONS_LABEL ? src
 							.getOutputDescription() : "");
 			drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x,
 					srcPoint.y, trg == null || !SHADOW_CONNECTIONS_LABEL ? ""
 							: trg.getInputDescription());
 			g2.setTransform(at);
 			g2.setColor(clr);
 		}
 
 		g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
 		g2.fillPolygon(arrow);
 
 		drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y,
 				src.getOutputDescription());
 		drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x, srcPoint.y,
 				trg == null ? "" : trg.getInputDescription());
 		return 1;
 	}
 
 	private void drawConnectorLabel(final Graphics2D g2, final int sx,
 			final int sy, final int tx, final int ty, final String str) {
 		if (str.isEmpty()) return;
 
 		// draw in image
 		final Rectangle sb = g2.getFontMetrics().getStringBounds(str, g2)
 				.getBounds();
 		final int sw = (int) (sb.width * scale);
 		final int sh = (int) (sb.height * scale);
 		if (sw <= 0 || sh <= 0) return;
 		final BufferedImage img = new BufferedImage(sw, sh,
 				BufferedImage.TYPE_INT_ARGB);
 		final Graphics2D imgG2D = img.createGraphics();
 		imgG2D.scale(scale, scale);
 		imgG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		if (PAINT_DEBUG) {
 			imgG2D.setColor(Color.YELLOW);
 			imgG2D.fillRect(0, 0, sb.width, sb.height);
 		}
 		imgG2D.setColor(g2.getColor());
 		imgG2D.setFont(g2.getFont());
		imgG2D.drawString(str, 0, -sb.y);
 
 		// make sure text is never bottom-up and correctly positioned
 		double d = Math.atan2(sy - ty, sx - tx);
 		int xoffs = 16;
 		if (d > Math.PI / 2)
 			d -= Math.PI;
 		else if (d < -Math.PI / 2.0)
 			d += Math.PI;
 		else
 			xoffs = (int) (-sb.width * scale - xoffs);
 
 		// draw rotated image
 		final Shape shape = g2.getClip();
 		final AffineTransform at = g2.getTransform();
 
 		// only use translation from original transformation
 		// scaling is already done in imgG2D
 		g2.setTransform(new AffineTransform());
 		g2.translate(at.getTranslateX() + sx * scale, at.getTranslateY() + sy
 				* scale);
 		g2.rotate(d);
 		final int len = (int) (Math.sqrt((sx - tx) * (sx - tx) + (sy - ty)
 				* (sy - ty)) * scale);
 		if (xoffs < 0)
 			g2.clipRect(-len / 2, 0, len / 2, sh);
 		else
 			g2.clipRect(0, 0, len / 2, sh);
 		g2.drawImage(img, new AffineTransformOp(new AffineTransform(),
 				AffineTransformOp.TYPE_BILINEAR), xoffs, 0);
 		g2.setTransform(at);
 		g2.setClip(shape);
 	}
 
 	private void drawAutoLayoutInfo(final Graphics2D g2,
 			final BoardAutoLayouter layouter) {
 		if (layouter == null) return;
 		final int w = bounds.width * 2 / 3;
 		final int h = 20;
 		final int x = (bounds.width - w) / 2;
 		final int y = (bounds.height - h) / 2;
 		g2.setColor(Color.LIGHT_GRAY);
 		g2.fill3DRect(x, y, w, h, false);
 		g2.setColor(new Color(128, 128, 255));
 		g2.fill3DRect(x + 1, y + 1, (int) (w * layouter.getProgress()) - 1,
 				h - 2, true);
 		g2.setColor(Color.BLACK);
 		final String s = layouter.getProgressText();
 		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);
 		g2.drawString(s, (int) ((bounds.width - sb.getWidth()) / 2), (int) (y
 				+ h - (h - sb.getHeight()) / 2));
 	}
 
 	public static void calcConnectorPositions(final ImmutableRectangle srcRect,
 			final ImmutableRectangle trgRect, final Point srcPos,
 			final Point trgPos) {
 		// calculate center of source and target
 		final int csx = srcRect.getX() + srcRect.getWidth() / 2;
 		final int csy = srcRect.getY() + srcRect.getHeight() / 2;
 		final int ctx = trgRect.getX() + trgRect.getWidth() / 2;
 		final int cty = trgRect.getY() + trgRect.getHeight() / 2;
 
 		// determine side of rectangle for the connection
 		final int dcx = ctx - csx;
 		final int dcy = cty - csy;
 		if (dcx > Math.abs(dcy)) {
 			// target is right of source
 			if (srcPos != null)
 				srcPos.setLocation(srcRect.getX() + srcRect.getWidth(), csy);
 			if (trgPos != null) trgPos.setLocation(trgRect.getX(), cty);
 		} else if (dcy >= Math.abs(dcx)) {
 			// target is below source
 			if (srcPos != null)
 				srcPos.setLocation(csx, srcRect.getY() + srcRect.getHeight());
 			if (trgPos != null) trgPos.setLocation(ctx, trgRect.getY());
 		} else if (dcx < 0 && Math.abs(dcx) > Math.abs(dcy)) {
 			// target is left of source
 			if (srcPos != null) srcPos.setLocation(srcRect.getX(), csy);
 			if (trgPos != null)
 				trgPos.setLocation(trgRect.getX() + trgRect.getWidth(), cty);
 		} else {
 			// target is above source
 			if (srcPos != null) srcPos.setLocation(csx, srcRect.getY());
 			if (trgPos != null)
 				trgPos.setLocation(ctx, trgRect.getY() + trgRect.getHeight());
 		}
 	}
 
 	// from http://www.java-forums.org/awt-swing/
 	// 5842-how-draw-arrow-mark-using-java-swing.html
 	public static Polygon getArrowPolygon(final int sx, final int sy,
 			final int tx, final int ty) {
 		final Polygon polygon = new Polygon();
 		final double d = Math.atan2(sx - tx, sy - ty);
 		// tip
 		polygon.addPoint(tx, ty);
 		// wing 1
 		polygon.addPoint(tx + xCor(CONN_ARROW_HEAD, d + .5), ty
 				+ yCor(CONN_ARROW_HEAD, d + .5));
 		// on line
 		polygon.addPoint(tx + xCor(CONN_ARROW_WING, d), ty
 				+ yCor(CONN_ARROW_WING, d));
 		// wing 2
 		polygon.addPoint(tx + xCor(CONN_ARROW_HEAD, d - .5), ty
 				+ yCor(CONN_ARROW_HEAD, d - .5));
 		// back to tip, close polygon
 		polygon.addPoint(tx, ty);
 		return polygon;
 	}
 
 	private static int xCor(final int len, final double dir) {
 		return (int) (len * Math.sin(dir));
 	}
 
 	private static int yCor(final int len, final double dir) {
 		return (int) (len * Math.cos(dir));
 	}
 
 	/**
 	 * Gets the bounding rectangle around an icon.
 	 * 
 	 * @param rect
 	 *            position of the image in which to align the icon
 	 * @param icon
 	 *            {@link Icon} which would be drawn
 	 * @param pos
 	 *            the position inside the rectangle as follow<br>
 	 *            [ 1 2 3 ..... -2 -1 0 ]
 	 * @return the bounding rectangle
 	 */
 	public static Rectangle getIconBounds(final ImmutableRectangle rect,
 			final Icon icon, final int pos) {
 		final boolean alignRight = pos <= 0;
 		final Rectangle b = new Rectangle();
 		b.width = icon.getIconWidth();
 		b.height = icon.getIconHeight();
 		if (alignRight)
 			b.x = rect.getX() + rect.getWidth() - (1 - pos) * ICON_WIDTH;
 		else
 			b.x = rect.getX() + pos * ICON_WIDTH - b.width;
 		b.y = rect.getY() + rect.getHeight() - ICON_WIDTH;
 		return b;
 	}
 
 	public static Object getPipePartElement(final PipePart pp,
 			final Point position) {
 		final Rectangle inner = pp.getGuiPosition().createCopy();
 		inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
 		final Rectangle ibC = getIconBounds(pp.getGuiPosition(), ICON_CONF,
 				ICON_CONF_POS);
 		final Rectangle ibD = getIconBounds(pp.getGuiPosition(), ICON_DGR,
 				ICON_DGR_POS);
 		if (ibC.contains(position))
 			return ICON_CONF;
 		else if (ibD.contains(position))
 			return ICON_DGR;
 		else if (inner.contains(position))
 			return new Point(position.x - pp.getGuiPosition().getX(),
 					position.y - pp.getGuiPosition().getY());
 		else
 			return null;
 	}
 
 	public void setPipe(final Pipe pipe, final Graphics g,
 			final boolean allowMoving) {
 		this.pipe = pipe;
 		set.clear();
 		for (final PipePart pp : pipe.getInputList())
 			addToSet(pp, g, allowMoving);
 		for (final PipePart pp : pipe.getConverterList())
 			addToSet(pp, g, allowMoving);
 		for (final PipePart pp : pipe.getOutputList())
 			addToSet(pp, g, allowMoving);
 		updateSaneConfigCache();
 	}
 
 	public Pipe getPipe() {
 		return pipe;
 	}
 
 	void addToSet(final PipePart pp, final Graphics g, final boolean allowMoving) {
 		set.add(pp);
 		if (allowMoving) {
 			final Rectangle r = pp.getGuiPosition().createCopy();
 			r.height = g.getFontMetrics().getHeight() + ICON_WIDTH + 2;
 			r.width = Math.min(MAX_RECT_WIDTH, Math.max(ICON_WIDTH * ICON_MAX,
 					(int) g.getFontMetrics().getStringBounds(pp.getName(), g)
 							.getWidth()
 							+ r.height * 2));
 			check(r, pp);
 			pp.setGuiPosition(r);
 		}
 	}
 
 	public Set<PipePart> getSet() {
 		return set;
 	}
 
 	public Set<PipePart> getSaneConfigCache() {
 		return saneConfigCache;
 	}
 
 	public boolean updateSaneConfigCache() {
 		final Set<PipePart> sane = pipe.getSanePipeParts();
 		if (saneConfigCache.equals(sane)) return false;
 		saneConfigCache.clear();
 		saneConfigCache.addAll(sane);
 		pipe.modified();
 		return true;
 	}
 
 	public Dimension getPreferredSize() {
 		// get lower right corner
 		int x = 0;
 		int y = 0;
 		for (final PipePart pp : set) {
 			final ImmutableRectangle r = pp.getGuiPosition();
 			x = Math.max(x, r.getX() + r.getWidth());
 			y = Math.max(y, r.getY() + r.getHeight());
 		}
 
 		// add some free space and consider scaling
 		return new Dimension((int) (x * scale + 4), (int) (y * scale + 4));
 	}
 
 	public Dimension getBounds() {
 		return bounds;
 	}
 
 	public void setBounds(final int width, final int height,
 			final boolean allowMoving) {
 		bounds.setSize(width, height);
 		border1 = Math
 				.min(width / SECTION_FRAG, MAX_RECT_WIDTH + SECTION_SPACE);
 		border2 = width - border1;
 		if (allowMoving) for (final PipePart pp : set) {
 			final Rectangle r = pp.getGuiPosition().createCopy();
 			check(r, pp);
 			pp.setGuiPosition(r);
 		}
 	}
 
 	public int getBorder1(final boolean considerScaling) {
 		return considerScaling ? (int) (border1 * scale) : border1;
 	}
 
 	public int getBorder2(final boolean considerScaling) {
 		return considerScaling ? (int) (border2 * scale) : border2;
 	}
 
 	public double getScale() {
 		return scale;
 	}
 
 	public void setScale(final double scale) {
 		this.scale = scale;
 	}
 
 	void check(final Rectangle r, final PipePart pp) {
 		final int xMin;
 		final int xMax;
 		final int yMin = 1;
 		final int yMax = (int) (bounds.height / scale) - 1;
 		if (Input.class.isInstance(pp)) {
 			xMin = 1;
 			xMax = border1 - 1;
 		} else if (Converter.class.isInstance(pp)) {
 			xMin = border1 + 1;
 			xMax = border2 - 1;
 		} else if (Output.class.isInstance(pp)) {
 			xMin = border2 + 1;
 			xMax = (int) (bounds.getWidth() / scale) - 1;
 		} else {
 			xMin = 1;
 			xMax = (int) (bounds.getWidth() / scale) - 1;
 		}
 		if (r.x < xMin) r.x = xMin;
 		if (r.y < yMin) r.y = yMin;
 		if (r.x + r.getWidth() > xMax) r.x = xMax - r.width;
 		if (r.y + r.height > yMax) r.y = yMax - r.height;
 		for (final PipePart other : set)
 			if (other != pp && other.getGuiPosition().intersects(r)) {
 				// move r, so it doesn't intersect anymore
 				final ImmutableRectangle rO = other.getGuiPosition();
 				final Rectangle i = rO.intersection(r);
 				final int x0 = r.x + r.width / 2;
 				final int y0 = r.y + r.height / 2;
 				final int x1 = rO.getX() + rO.getWidth() / 2;
 				final int y1 = rO.getY() + rO.getHeight() / 2;
 				if (i.getWidth() < i.height && r.x - i.getWidth() >= xMin
 						&& r.x + r.getWidth() + i.getWidth() <= xMax) {
 					if (x0 > x1) // move right
 						r.translate(i.width, 0);
 					else
 						// move left
 						r.translate(-i.width, 0);
 				} else if (y0 > y1) {
 					if (r.getY() + r.height + i.height > yMax)
 						// move up instead of down
 						r.translate(0, i.height - r.height - rO.getHeight());
 					else
 						r.translate(0, i.height); // move down
 				} else if (r.getY() - i.height < yMin)
 					// move down instead of up
 					r.translate(0, r.height + rO.getHeight() - i.height);
 				else
 					r.translate(0, -i.height); // move up
 				// check bounds again
 				// (overlapping is better than being out of bounds)
 				if (r.x < xMin) r.x = xMin;
 				if (r.getY() < yMin) r.y = yMin;
 				if (r.x + r.getWidth() > xMax) r.x = xMax - r.width;
 				if (r.getY() + r.height > yMax) r.y = yMax - r.height;
 			}
 	}
 
 }
