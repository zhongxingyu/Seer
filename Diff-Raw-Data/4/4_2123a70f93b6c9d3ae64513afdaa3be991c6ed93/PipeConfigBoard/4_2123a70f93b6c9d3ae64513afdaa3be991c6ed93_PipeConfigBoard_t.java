 package pleocmd.itfc.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.MenuElement;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 
 import pleocmd.Log;
 import pleocmd.cfg.ConfigValue;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.PipePart;
 import pleocmd.pipe.PipePartDetection;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 public final class PipeConfigBoard extends JPanel {
 
 	private static final boolean PAINT_DEBUG = false;
 
 	private static final long serialVersionUID = -4525676341777864359L;
 
 	private static final int INNER_WIDTH = 8;
 
 	private static final int INNER_HEIGHT = 4;
 
 	private static final Color INNER_COLOR = new Color(220, 220, 220);
 
 	private static final Color RECT_COLOR = new Color(255, 255, 255);
 
 	private final JPopupMenu menuInput;
 
 	private final JPopupMenu menuConverter;
 
 	private final JPopupMenu menuOutput;
 
 	private final Set<PipePart> set;
 
 	private final Dimension bounds = new Dimension();
 
 	private int border1;
 
 	private int border2;
 
 	private PipePart currentPart;
 
 	private Rectangle currentConnection;
 
 	private PipePart currentConnectionsTarget;
 
 	private boolean currentConnectionValid;
 
 	private PipePart underCursor;
 
 	private int grayVal = 128;
 
 	public PipeConfigBoard() {
 		menuInput = createMenu("Input", Input.class);
 		menuConverter = createMenu("Converter", Converter.class);
 		menuOutput = createMenu("Output", Output.class);
 
 		set = new HashSet<PipePart>();
 		for (final PipePart pp : Pipe.the().getInputList())
 			set.add(pp);
 		for (final PipePart pp : Pipe.the().getConverterList())
 			set.add(pp);
 		for (final PipePart pp : Pipe.the().getOutputList())
 			set.add(pp);
 
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if (e.isPopupTrigger())
 					showPopup(e);
 				else {
 					updateCurrent(e.getPoint());
 					if (e.getModifiers() == InputEvent.BUTTON1_MASK
 							&& e.getClickCount() == 2) {
 						final PipePart pp = getCurrentPart();
 						if (pp != null) {
 							createConfigureDialog("Configure", pp, null);
 							repaint();
 						}
 					}
 				}
 			}
 
 			@Override
 			public void mouseReleased(final MouseEvent e) {
 				// if (e.isPopupTrigger()) showPopup(e);
 				releaseCurrent();
 			}
 
 			private void showPopup(final MouseEvent e) {
 				updateCurrent(e.getPoint());
 				showMenu(PipeConfigBoard.this, e.getX(), e.getY());
 			}
 		});
 
 		addMouseMotionListener(new MouseMotionListener() {
 			@Override
 			public void mouseDragged(final MouseEvent e) {
 				if (e.getModifiers() != InputEvent.BUTTON1_MASK) return;
 				PipeConfigBoard.this.mouseDragged(e.getPoint());
 			}
 
 			@Override
 			public void mouseMoved(final MouseEvent e) {
 				PipeConfigBoard.this.mouseMoved(e.getPoint());
 			}
 		});
 
 		addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(final ComponentEvent e) {
 				updateBounds(getWidth(), getHeight());
 			}
 		});
 		ToolTipManager.sharedInstance().registerComponent(this);
 	}
 
 	private JPopupMenu createMenu(final String name,
 			final Class<? extends PipePart> clazz) {
 		final JPopupMenu menu = new JPopupMenu();
 		final JMenu menuAdd = new JMenu("Add " + name);
 		menu.add(menuAdd);
 		for (final Class<? extends PipePart> pp : PipePartDetection.ALL_PIPEPART)
 			if (clazz.isAssignableFrom(pp)) {
 				final JMenuItem item = new JMenuItem(pp.getSimpleName());
 				menuAdd.add(item);
 				item.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(final ActionEvent e) {
 						addPipePart(pp);
 					}
 				});
 			}
 		menu.addSeparator();
 
 		final JMenuItem itemDelPart = new JMenuItem("Delete This PipePart");
 		itemDelPart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentPart();
 			}
 		});
 		menu.add(itemDelPart);
 
 		final JMenuItem itemDelPartConn = new JMenuItem(
 				"Delete Connections Of This PipePart");
 		itemDelPartConn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentPartsConnections();
 			}
 		});
 		menu.add(itemDelPartConn);
 
 		final JMenuItem itemDelConn = new JMenuItem("Delete This Connection");
 		itemDelConn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentConnection();
 			}
 		});
 		menu.add(itemDelConn);
 
 		return menu;
 	}
 
 	protected void removeCurrentConnection() {
 		if (currentPart != null && currentConnectionsTarget != null) {
 			currentPart.disconnectFromPipePart(currentConnectionsTarget);
 			currentConnection = null;
 			currentConnectionsTarget = null;
 			currentConnectionValid = false;
 			repaint();
 		}
 	}
 
 	protected void removeCurrentPartsConnections() {
 		if (currentPart != null) {
 			final Set<PipePart> copy = new HashSet<PipePart>(currentPart
 					.getConnectedPipeParts());
 			for (final PipePart pp : copy)
 				currentPart.disconnectFromPipePart(pp);
 			currentConnection = null;
 			currentConnectionsTarget = null;
 			currentConnectionValid = false;
 			repaint();
 		}
 	}
 
 	protected void removeCurrentPart() {
 		if (currentPart != null) {
 			for (final PipePart srcPP : set)
 				if (srcPP.getConnectedPipeParts().contains(currentPart))
 					srcPP.disconnectFromPipePart(currentPart);
 			set.remove(currentPart);
 			currentPart = null;
 			currentConnection = null;
 			currentConnectionsTarget = null;
 			currentConnectionValid = false;
 			repaint();
 		}
 	}
 
 	protected Set<PipePart> getSet() {
 		return set;
 	}
 
 	protected PipePart getCurrentPart() {
 		return currentPart;
 	}
 
 	/**
 	 * @return the {@link PipePart} which is under the cursor or <b>null</b> if
 	 *         none.
 	 */
 	protected PipePart getUnderCursor() {
 		return underCursor;
 	}
 
 	@Override
 	public void paintComponent(final Graphics g) {
 		final Graphics2D g2 = (Graphics2D) g;
 		if (PAINT_DEBUG) {
 			grayVal = (grayVal - 118) % 64 + 128;
 			g2.setColor(new Color(grayVal, grayVal, grayVal));
 		} else
 			g2.setColor(Color.LIGHT_GRAY);
 		final Rectangle clip = g2.getClipBounds();
 		g2.fillRect(clip.x, clip.y, clip.width, clip.height);
 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_BEVEL, 0, new float[] { 2, 2 }, 0));
 		g2.setColor(Color.BLACK);
 		g2.drawLine(border1, 0, border1, bounds.height);
 		g2.drawLine(border2, 0, border2, bounds.height);
 
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_BEVEL, 0, null, 0));
 		for (final PipePart pp : set) {
 			g2.setColor(pp == currentPart ? Color.BLUE : Color.BLACK);
 			drawPipePart(g2, pp, pp == underCursor);
 		}
 
 		g2.setColor(Color.BLACK);
 		for (final PipePart src : set)
 			for (final PipePart trg : src.getConnectedPipeParts())
 				drawConnection(g2, src.getGuiPosition(), trg.getGuiPosition(),
 						src, trg);
 		if (currentConnection != null && currentConnectionsTarget == null) {
 			g2.setColor(currentConnectionValid ? Color.BLUE : Color.RED);
 			drawConnection(g2, currentPart.getGuiPosition(), currentConnection,
 					currentPart, underCursor);
 		}
 	}
 
 	private static void drawPipePart(final Graphics2D g2, final PipePart part,
 			final boolean visibleInner) {
 		final Rectangle rect = part.getGuiPosition();
 		if (!rect.intersects(g2.getClipBounds())) return;
 		final Color clr = g2.getColor();
 		g2.setColor(RECT_COLOR);
 		g2.fillRect(rect.x, rect.y, rect.width, rect.height);
 		g2.setColor(clr);
 		g2.drawRect(rect.x, rect.y, rect.width, rect.height);
 
 		final String s = part.getClass().getSimpleName();
 		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);
 
 		if (visibleInner) {
 			final Rectangle inner = new Rectangle(rect);
 			inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
 			g2.setColor(INNER_COLOR);
 			g2.fillRect(inner.x, inner.y, inner.width, inner.height);
 			g2.setColor(clr);
 		}
 
 		final Shape clip = g2.getClip();
 		g2.clipRect(rect.x, rect.y, rect.width, rect.height);
 		g2.drawString(s, (float) (rect.x + (rect.width - sb.getWidth()) / 2),
 				(float) (rect.y + sb.getHeight() + (rect.height - sb
 						.getHeight()) / 2));
 		g2.setClip(clip);
 	}
 
 	private static void drawConnection(final Graphics2D g2,
 			final Rectangle srcRect, final Rectangle trgRect,
 			final PipePart src, final PipePart trg) {
 		if (!srcRect.union(trgRect).intersects(g2.getClipBounds())) return;
 		final Point srcPoint = new Point();
 		final Point trgPoint = new Point();
 		calcConnectorPositions(srcRect, trgRect, srcPoint, trgPoint);
 		g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
 		g2.fillPolygon(getArrowPolygon(srcPoint.x, srcPoint.y, trgPoint.x,
 				trgPoint.y));
 		drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y,
 				src.getOutputDescription());
 		drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x, srcPoint.y,
 				trg == null ? "" : trg.getInputDescription());
 	}
 
 	private static void drawConnectorLabel(final Graphics2D g2, final int sx,
 			final int sy, final int tx, final int ty, final String str) {
 		final double d = Math.atan2(sx - tx, sy - ty);
 		final AffineTransform at = g2.getTransform();
 		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(str, g2);
 		final int w = (int) (sb.getWidth() + 14);
 		int h = (int) (sb.getHeight() + 0);
 		double theta = Math.PI / 2 - d;
 		if (theta > Math.PI / 2) {
 			// text should never be bottom-up
 			theta += Math.PI;
 			h = -h;
 		}
 		g2.transform(AffineTransform.getTranslateInstance(sx - xCor(w / 2, d)
 				- xCor(h, d - Math.PI / 2), sy - yCor(w / 2, d)
 				- yCor(h, d - Math.PI / 2)));
 		g2.transform(AffineTransform.getRotateInstance(theta));
 		g2.drawString(str, -(int) sb.getWidth() / 2, (int) sb.getHeight() / 2);
 		g2.setTransform(at);
 	}
 
 	private static void calcConnectorPositions(final Rectangle srcRect,
 			final Rectangle trgRect, final Point srcPos, final Point trgPos) {
 		// calculate center of source and target
 		final int csx = srcRect.x + srcRect.width / 2;
 		final int csy = srcRect.y + srcRect.height / 2;
 		final int ctx = trgRect.x + trgRect.width / 2;
 		final int cty = trgRect.y + trgRect.height / 2;
 
 		// determine side of rectangle for the connection
 		final int dcx = ctx - csx;
 		final int dcy = cty - csy;
 		if (dcx > Math.abs(dcy)) {
 			// target is right of source
 			srcPos.setLocation(srcRect.x + srcRect.width, csy);
 			trgPos.setLocation(trgRect.x, cty);
 		} else if (dcy >= Math.abs(dcx)) {
 			// target is below source
 			srcPos.setLocation(csx, srcRect.y + srcRect.height);
 			trgPos.setLocation(ctx, trgRect.y);
 		} else if (dcx < 0 && Math.abs(dcx) > Math.abs(dcy)) {
 			// target is left of source
 			srcPos.setLocation(srcRect.x, csy);
 			trgPos.setLocation(trgRect.x + trgRect.width, cty);
 		} else {
 			// target is above source
 			srcPos.setLocation(csx, srcRect.y);
 			trgPos.setLocation(ctx, trgRect.y + trgRect.height);
 		}
 	}
 
 	// from http://www.java-forums.org/awt-swing/
 	// 5842-how-draw-arrow-mark-using-java-swing.html
 	private static Polygon getArrowPolygon(final int sx, final int sy,
 			final int tx, final int ty) {
 		final int i1 = 14; // how sharp the arrow's tip should look
 		final int i2 = 8; // how sharp the arrow's wings should look
 		final Polygon p = new Polygon();
 		final double d = Math.atan2(sx - tx, sy - ty);
 		p.addPoint(tx, ty); // tip
 		p.addPoint(tx + xCor(i1, d + .5), ty + yCor(i1, d + .5)); // wing 1
 		p.addPoint(tx + xCor(i2, d), ty + yCor(i2, d)); // on line
 		p.addPoint(tx + xCor(i1, d - .5), ty + yCor(i1, d - .5)); // wing 2
 		p.addPoint(tx, ty); // back to tip, close polygon
 		return p;
 	}
 
 	private static int xCor(final int len, final double dir) {
 		return (int) (len * Math.sin(dir));
 	}
 
 	private static int yCor(final int len, final double dir) {
 		return (int) (len * Math.cos(dir));
 	}
 
 	protected void addPipePart(final Class<? extends PipePart> part) {
 		try {
 			final PipePart pp = part.newInstance();
 			createConfigureDialog("Add", pp, new Runnable() {
 				@Override
 				public void run() {
					check(pp.getGuiPosition(), pp);
 					getSet().add(pp);
 					repaint();
 				}
 			});
 		} catch (final InstantiationException e) {
 			Log.error(e);
 		} catch (final IllegalAccessException e) {
 			Log.error(e);
 		}
 	}
 
 	/**
 	 * Should be invoked during a Drag&Drop operation if this section is the
 	 * source of the operation. Updates the position of the current connection,
 	 * if any, or otherwise tries to move the current remembered
 	 * {@link PipePart} to the given position.
 	 * 
 	 * @param p
 	 *            current cursor position
 	 */
 	protected void mouseDragged(final Point p) {
 		if (currentPart == null) return;
 		if (currentConnection != null) {
 			// move connector instead of pipe-part
 			if (currentConnectionsTarget != null)
 				currentPart.disconnectFromPipePart(currentConnectionsTarget);
 			currentConnectionsTarget = null;
 
 			Rectangle r = new Rectangle(currentPart.getGuiPosition());
 			r = r.union(currentConnection);
 
 			currentConnection.setLocation(p.x, p.y);
 			currentConnection.setSize(0, 0);
 			check(currentConnection, null);
 			currentConnectionValid = false;
 			for (final PipePart pp : set)
 				if (pp.getGuiPosition().contains(p)) {
 					currentConnectionValid = isConnectionAllowed(currentPart,
 							pp);
 					break;
 				}
 			mouseMoved(p);
 
 			r = r.union(currentConnection);
 			r.grow(8, 8);
 			repaint(r);
 		} else {
 			// move pipe-part
 			Rectangle r = new Rectangle(currentPart.getGuiPosition());
 			currentPart.getGuiPosition().setLocation(p.x, p.y);
 			check(currentPart.getGuiPosition(), currentPart);
 			r = r.union(currentPart.getGuiPosition());
 			for (final PipePart trgPP : currentPart.getConnectedPipeParts())
 				r = r.union(trgPP.getGuiPosition());
 			for (final PipePart srcPP : set)
 				if (srcPP.getConnectedPipeParts().contains(currentPart))
 					r = r.union(srcPP.getGuiPosition());
 			r.grow(4, 4);
 			repaint(r);
 		}
 	}
 
	protected void check(final Rectangle r, final PipePart pp) {
 		final int xMin;
 		final int xMax;
 		final int yMin = 1;
 		final int yMax = bounds.height - 1;
 		if (Input.class.isInstance(pp)) {
 			xMin = 1;
 			xMax = border1 - 1;
 		} else if (Converter.class.isInstance(pp)) {
 			xMin = border1 + 1;
 			xMax = border2 - 1;
 		} else if (Output.class.isInstance(pp)) {
 			xMin = border2 + 1;
 			xMax = bounds.width - 1;
 		} else {
 			xMin = 1;
 			xMax = bounds.width - 1;
 		}
 		if (r.x < xMin) r.x = xMin;
 		if (r.y < yMin) r.y = yMin;
 		if (r.x + r.width > xMax) r.x = xMax - r.width;
 		if (r.y + r.height > yMax) r.y = yMax - r.height;
 	}
 
 	/**
 	 * Should be called whenever the mouse is moved over this section.
 	 * 
 	 * @param p
 	 *            current cursor position
 	 */
 	protected void mouseMoved(final Point p) {
 		PipePart found = null;
 		for (final PipePart pp : set)
 			if (pp.getGuiPosition().contains(p)) {
 				found = pp;
 				break;
 			}
 		if (underCursor != found) {
 			if (underCursor != null) repaint(underCursor.getGuiPosition());
 			underCursor = found;
 			if (underCursor != null) repaint(underCursor.getGuiPosition());
 		}
 	}
 
 	/**
 	 * Remembers the {@link PipePart} and the connector which is under the given
 	 * cursor position for later use in {@link #mouseDragged(Point)} and during
 	 * {@link #paintComponent(Graphics)}.
 	 * 
 	 * @param p
 	 *            current cursor position
 	 */
 	protected void updateCurrent(final Point p) {
 		// invoked on click or when a drag&drop operation starts
 		updateCurrent0(p);
 		repaint();
 	}
 
 	private void updateCurrent0(final Point p) {
 		// check all pipe-parts
 		for (final PipePart pp : set)
 			if (pp.getGuiPosition().contains(p)) {
 				currentPart = pp;
 				final Rectangle inner = new Rectangle(pp.getGuiPosition());
 				inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
 				currentConnection = inner.contains(p) ? null : new Rectangle(
 						p.x, p.y, 0, 0);
 				currentConnectionsTarget = null;
 				currentConnectionValid = false;
 				return;
 			}
 
 		// check all connections
 		for (final PipePart srcPP : set)
 			for (final PipePart trgPP : srcPP.getConnectedPipeParts()) {
 				final Point ps = new Point();
 				final Point pt = new Point();
 				calcConnectorPositions(srcPP.getGuiPosition(), trgPP
 						.getGuiPosition(), ps, pt);
 				if (getArrowPolygon(ps.x, ps.y, pt.x, pt.y).contains(p)) {
 					currentPart = srcPP;
 					currentConnection = new Rectangle(p.x, p.y, 0, 0);
 					currentConnectionsTarget = trgPP;
 					currentConnectionValid = false;
 					return;
 				}
 			}
 
 		currentPart = null;
 		currentConnection = null;
 		currentConnectionsTarget = null;
 		currentConnectionValid = false;
 	}
 
 	/**
 	 * Forgets about the currently remembered {@link PipePart} and connection,
 	 * if any. If a connection has been remembered and is currently pointing to
 	 * a valid {@link PipePart}, the remembered {@link PipePart} is connected to
 	 * the one the connection is pointing to, even if it's outside of this
 	 * section.
 	 */
 	protected void releaseCurrent() {
 		// invoked on click or when a drag&drop operation is finished
 		if (currentConnection != null && currentConnectionsTarget == null) {
 			final Point p = new Point(currentConnection.getLocation());
 			for (final PipePart pp : set)
 				if (pp.getGuiPosition().contains(p)
 						&& isConnectionAllowed(currentPart, pp)) {
 					currentPart.connectToPipePart(pp);
 					break;
 				}
 		}
 		// currentPart = null;
 		currentConnection = null;
 		currentConnectionValid = false;
 		// currentConnectionsTarget = null;
 		repaint();
 	}
 
 	/**
 	 * @param s1
 	 *            first string
 	 * @param s2
 	 *            second string
 	 * @return true only if both strings contain something and that is not the
 	 *         same.
 	 */
 	private static boolean strDiffer(final String s1, final String s2) {
 		return s1 != null && s2 != null && !s1.isEmpty() && !s2.isEmpty()
 				&& !s1.equals(s2);
 	}
 
 	// TODO move to PipePart
 	private boolean isConnectionAllowed(final PipePart src, final PipePart trg) {
 		if (src == trg) return false;
 		if (src instanceof Output) return false;
 		if (trg instanceof Input) return false;
 		if (strDiffer(src.getOutputDescription(), trg.getInputDescription()))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Sorts all {@link PipePart}s according to their location on the board,
 	 * from top to down, from left to right.
 	 * 
 	 * @param <T>
 	 *            subclass of {@link PipePart} (compile-time)
 	 * @param clazz
 	 *            subclass of {@link PipePart} (run-time)
 	 * @return sorted list of {@link PipePart}s
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends PipePart> List<T> getSortedParts(final Class<T> clazz) {
 		final List<T> res = new ArrayList<T>();
 		for (final PipePart pp : set)
 			if (clazz.isInstance(pp)) res.add((T) pp);
 		Collections.sort(res, new Comparator<T>() {
 			@Override
 			public int compare(final T pp1, final T pp2) {
 				final Rectangle r1 = pp1.getGuiPosition();
 				final Rectangle r2 = pp2.getGuiPosition();
 				int cmp = r1.y - r2.y;
 				if (cmp == 0) cmp = r1.x - r2.x;
 				return cmp;
 			}
 		});
 		return res;
 	}
 
 	protected void updateBounds(final int width, final int height) {
 		bounds.setSize(width, height);
 		border1 = width / 5;
 		border2 = width * 4 / 5;
 		for (final PipePart pp : set)
 			check(pp.getGuiPosition(), pp);
 		repaint();
 	}
 
 	protected void showMenu(final Component invoker, final int x, final int y) {
 		if (x <= border1)
 			showMenu(menuInput, invoker, x, y);
 		else if (x >= border2)
 			showMenu(menuOutput, invoker, x, y);
 		else
 			showMenu(menuConverter, invoker, x, y);
 	}
 
 	protected void showMenu(final JPopupMenu menu, final Component invoker,
 			final int x, final int y) {
 		final MenuElement[] items = menu.getSubElements();
 		if (items.length >= 4) {
 			if (items[0] instanceof JMenuItem)
 				((JMenuItem) items[0]).setEnabled(currentPart == null);
 			if (items[1] instanceof JMenuItem)
 				((JMenuItem) items[1]).setEnabled(currentPart != null
 						&& currentConnection == null);
 			if (items[2] instanceof JMenuItem)
 				((JMenuItem) items[2]).setEnabled(currentPart != null
 						&& currentConnection == null
 						&& !currentPart.getConnectedPipeParts().isEmpty());
 			if (items[3] instanceof JMenuItem)
 				((JMenuItem) items[3]).setEnabled(currentConnection != null);
 		}
 		menu.show(invoker, x, y);
 	}
 
 	protected void createConfigureDialog(final String prefix,
 			final PipePart pp, final Runnable runIfOK) {
 		// no need to configure if no values assigned
 		if (pp.getGroup().isEmpty()) {
 			if (runIfOK != null) runIfOK.run();
 			return;
 		}
 
 		final JDialog dlg = new JDialog();
 		dlg.setTitle(String.format("%s %s", prefix, pp));
 		final Layouter lay = new Layouter(dlg);
 		for (final ConfigValue v : pp.getGuiConfigs()) {
 			// each config-value gets its own JPanel so they don't
 			// interfere with each other.
 			// LBL1 SUB1
 			// LBL2 SUB2
 			// LBL3 SUB3
 			// BUTTONS
 			final JPanel sub = new JPanel();
 			final Layouter laySub = new Layouter(sub);
 			final String compLabel = v.getLabel() + ":";
 			final JLabel lbl = new JLabel(compLabel, SwingConstants.RIGHT);
 			lbl.setVerticalAlignment(SwingConstants.TOP);
 			lay.add(lbl, false);
 			lay.addWholeLine(sub, false);
 			v.insertGUIComponents(laySub);
 		}
 
 		final JPanel buttons = new JPanel();
 		final Layouter lb = new Layouter(buttons);
 
 		lay.addVerticalSpacer();
 		lay.addWholeLine(buttons, false);
 
 		lb.addButton(Button.Help, Layouter.help(dlg, "PartConfigureDialog"));
 		lb.addSpacer();
 		dlg.getRootPane().setDefaultButton(
 				lb.addButton(Button.Ok, new Runnable() {
 					@Override
 					public void run() {
 						saveConfigChanges(pp);
 						dlg.dispose();
 						if (runIfOK != null) runIfOK.run();
 					}
 				}));
 		lb.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				saveConfigChanges(pp);
 			}
 		});
 		lb.addButton(Button.Cancel, new Runnable() {
 			@Override
 			public void run() {
 				dlg.dispose();
 			}
 		});
 
 		dlg.pack();
 		dlg.setLocationRelativeTo(null);
 		// dlg.setModal(true);
 		HelpDialog.closeHelpIfOpen();
 		dlg.setVisible(true);
 		HelpDialog.closeHelpIfOpen();
 	}
 
 	protected void saveConfigChanges(final PipePart pp) {
 		// TODO pp.getGroup().assertContextSensitiveCorrectness();
 		try {
 			for (final ConfigValue v : pp.getGuiConfigs())
 				v.setFromGUIComponents();
 			pp.configure();
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 	}
 
 	@Override
 	public String getToolTipText(final MouseEvent event) {
 		if (underCursor == null) return null;
 		final StringBuilder sb = new StringBuilder("<html><b>");
 		sb.append(underCursor.getClass().getSimpleName());
 		sb.append("</b><table>");
 		for (final ConfigValue v : underCursor.getGuiConfigs()) {
 			sb.append("<tr><td align=right>");
 			sb.append(v.getLabel().replace("<", "&lt;"));
 			sb.append("</td><td align=left>");
 			sb.append(v.asString().replace("<", "&lt;"));
 			sb.append("</td></tr>");
 		}
 		sb.append("</table></html>");
 		return sb.toString();
 	}
 
 }
