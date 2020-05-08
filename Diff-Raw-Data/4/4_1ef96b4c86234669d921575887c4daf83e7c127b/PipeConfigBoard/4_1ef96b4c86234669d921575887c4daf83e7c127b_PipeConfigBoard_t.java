 // This file is part of PleoCommand:
 // Interactively control Pleo with psychobiological parameters
 //
 // Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
 //
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Boston, USA.
 
 package pleocmd.itfc.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.FilteredImageSource;
 import java.awt.image.ImageFilter;
 import java.awt.image.RGBImageFilter;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractButton;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.MenuElement;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import pleocmd.ImmutableRectangle;
 import pleocmd.Log;
 import pleocmd.StringManip;
 import pleocmd.cfg.ConfigValue;
 import pleocmd.exc.InternalException;
 import pleocmd.exc.PipeException;
 import pleocmd.exc.StateException;
 import pleocmd.itfc.gui.BoardPainter.PaintParameters;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.itfc.gui.help.HelpLoader;
 import pleocmd.itfc.gui.icons.IconLoader;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.PipePart;
 import pleocmd.pipe.PipePartDetection;
 import pleocmd.pipe.PipePart.HelpKind;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 final class PipeConfigBoard extends JPanel {
 
 	private static final long serialVersionUID = -4525676341777864359L;
 
 	/**
 	 * Square of distance in pixel to a line to consider it clicked.
 	 */
 	private static final double LINE_CLICK_DIST = 10;
 
 	/**
 	 * Grow repaint clips by this amount to consider labels of connections.
 	 */
 	private static final int GROW_LABEL_REDRAW = 14;
 
 	/**
 	 * Whether to snap components during drag&drop operations to an imaginary
 	 * raster.<br>
 	 * 0 for no raster, raster distance in pixel otherwise.
 	 */
 	private static final int SNAP_TO_GRID = 10;
 
 	private final Pipe pipe;
 
 	private final PaintParameters p = new PaintParameters();
 
 	private final BoardPainter painter;
 
 	private final JPopupMenu menuInput;
 
 	private final JPopupMenu menuConverter;
 
 	private final JPopupMenu menuOutput;
 
 	private int idxMenuAdd;
 
 	private int idxMenuRepl;
 
 	private int idxMenuConfPart;
 
 	private int idxMenuDelPart;
 
 	private int idxMenuDelPartConn;
 
 	private int idxMenuDelConn;
 
 	private int idxMenuToggleDgr;
 
 	private int idxMenuClearBoard;
 
 	private int idxMenuLayoutBoard;
 
 	private int idxMenuExportBoard;
 
 	private Point lastMenuLocation;
 
 	private Point handlePoint;
 
 	private Icon currentIcon;
 
 	private boolean delayedReordering;
 
 	private Thread layoutThread;
 
 	private boolean closed;
 
 	public PipeConfigBoard(final Pipe pipe) {
 		this.pipe = pipe;
 		painter = new BoardPainter();
 
 		setPreferredSize(new Dimension(400, 300));
 		ToolTipManager.sharedInstance().registerComponent(this);
 
 		menuInput = createMenu("Input", Input.class);
 		menuConverter = createMenu("Converter", Converter.class);
 		menuOutput = createMenu("Output", Output.class);
 
 		updateState();
 
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if (e.getButton() == MouseEvent.BUTTON3)
 					showPopup(e);
 				else
 					updateCurrent(e.getPoint());
 			}
 
 			@Override
 			public void mouseReleased(final MouseEvent e) {
 				if (e.getModifiers() == InputEvent.BUTTON1_MASK)
 					switch (e.getClickCount()) {
 					case 1:
 						checkIconClicked();
 						break;
 					case 2:
 						configureCurrentPart(true);
 						break;
 					}
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
 
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				assignFromPipe();
 			}
 
 		});
 	}
 
 	protected void assignFromPipe() {
 		painter.setBounds(getWidth(), getHeight(), false);
 		painter.setPipe(getPipe(), getGraphics(), true);
 		updatePrefBounds();
 		repaint();
 	}
 
 	public Pipe getPipe() {
 		return pipe;
 	}
 
 	@Override
 	protected void paintComponent(final Graphics g) {
 		p.g = g;
 		painter.paint(p);
 	}
 
 	private JPopupMenu createMenu(final String name,
 			final Class<? extends PipePart> clazz) {
 		final JPopupMenu menu = new JPopupMenu();
 
 		idxMenuAdd = menu.getSubElements().length;
 		final JMenu menuAdd = new JMenu("Add " + name);
 		menuAdd.setIcon(IconLoader.getIcon("list-add"));
 		menu.add(menuAdd);
 		for (final Class<? extends PipePart> pp : PipePartDetection.ALL_PIPEPART)
 			if (clazz.isAssignableFrom(pp)) {
 				final JMenuItem item = new JMenuItem(PipePart.getName(pp),
 						PipePart.getIcon(pp));
 				menuAdd.add(item);
 				item.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(final ActionEvent e) {
 						addPipePart(pp, getLastMenuLocation());
 					}
 				});
 				item.setToolTipText(PipePart.getDescription(pp));
 			}
 		idxMenuRepl = menu.getSubElements().length;
 		final JMenu menuRepl = new JMenu("Replace " + name + " With");
 		menuRepl.setIcon(IconLoader.getIcon("edit-rename"));
 		menu.add(menuRepl);
 		for (final Class<? extends PipePart> pp : PipePartDetection.ALL_PIPEPART)
 			if (clazz.isAssignableFrom(pp)) {
 				final JMenuItem item = new JMenuItem(PipePart.getName(pp),
 						PipePart.getIcon(pp));
 				menuRepl.add(item);
 				item.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(final ActionEvent e) {
 						replacePipePart(pp);
 					}
 				});
 				item.setToolTipText(PipePart.getDescription(pp));
 			}
 		menu.addSeparator();
 
 		idxMenuConfPart = menu.getSubElements().length;
 		final JMenuItem itemConfPart = new JMenuItem("Configure This PipePart",
 				BoardPainter.ICON_CONF);
 		itemConfPart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				configureCurrentPart(false);
 			}
 		});
 		menu.add(itemConfPart);
 
 		idxMenuDelPart = menu.getSubElements().length;
 		final JMenuItem itemDelPart = new JMenuItem("Delete This PipePart",
 				IconLoader.getIcon("del-pipepart"));
 		itemDelPart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentPart();
 			}
 		});
 		menu.add(itemDelPart);
 
 		idxMenuDelPartConn = menu.getSubElements().length;
 		final JMenuItem itemDelPartConn = new JMenuItem(
 				"Delete Connections Of This PipePart", IconLoader
 						.getIcon("del-connection-all"));
 		itemDelPartConn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentPartsConnections();
 			}
 		});
 		menu.add(itemDelPartConn);
 
 		idxMenuDelConn = menu.getSubElements().length;
 		final JMenuItem itemDelConn = new JMenuItem("Delete This Connection",
 				IconLoader.getIcon("del-connection"));
 		itemDelConn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentConnection();
 			}
 		});
 		menu.add(itemDelConn);
 
 		idxMenuToggleDgr = menu.getSubElements().length;
 		final JCheckBoxMenuItem itemToggleDgr = new JCheckBoxMenuItem(
 				"Visualize PipePart's Output", BoardPainter.ICON_DGR);
 		itemToggleDgr.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				toggleDiagram();
 			}
 		});
 		menu.add(itemToggleDgr);
 		menu.addSeparator();
 
 		idxMenuClearBoard = menu.getSubElements().length;
 		final JMenuItem itemClearBoard = new JMenuItem("Clear The Board",
 				IconLoader.getIcon("board-clear"));
 		itemClearBoard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				clearBoard();
 			}
 		});
 		menu.add(itemClearBoard);
 
 		idxMenuLayoutBoard = menu.getSubElements().length;
 		final JMenuItem itemLayoutBoard = new JMenuItem(
 				"Auto-Layout The Board", IconLoader.getIcon("board-auto"));
 		itemLayoutBoard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				layoutBoard();
 			}
 		});
 		menu.add(itemLayoutBoard);
 
 		idxMenuExportBoard = menu.getSubElements().length;
 		final JMenuItem itemExportBoard = new JMenuItem("Export The Board ...",
 				IconLoader.getIcon("board-export"));
 		itemExportBoard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				exportBoard();
 			}
 		});
 		menu.add(itemExportBoard);
 
 		return menu;
 	}
 
 	protected Point getLastMenuLocation() {
 		return lastMenuLocation;
 	}
 
 	protected void removeCurrentConnection() {
 		if (hasCurrentPart() && p.currentConnectionsTarget != null
 				&& ensureModifyable()) {
 			try {
 				p.currentPart
 						.disconnectFromPipePart(p.currentConnectionsTarget);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot delete connection");
 			}
 			resetCurrentConnection();
 			painter.updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void removeCurrentPartsConnections() {
 		if (hasCurrentPart() && ensureModifyable()) {
 			final Set<PipePart> copy = new HashSet<PipePart>(p.currentPart
 					.getConnectedPipeParts());
 			try {
 				for (final PipePart pp : copy)
 					p.currentPart.disconnectFromPipePart(pp);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot delete connections");
 			}
 			resetCurrentConnection();
 			painter.updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void removeCurrentPart() {
 		if (hasCurrentPart() && ensureModifyable()) {
 			try {
 				for (final PipePart srcPP : painter.getSet())
 					if (srcPP.getConnectedPipeParts().contains(p.currentPart))
 						srcPP.disconnectFromPipePart(p.currentPart);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot delete connections");
 			}
 			painter.getSet().remove(p.currentPart);
 			try {
 				if (p.currentPart instanceof Input)
 					getPipe().removeInput((Input) p.currentPart);
 				else if (p.currentPart instanceof Converter)
 					getPipe().removeConverter((Converter) p.currentPart);
 				else if (p.currentPart instanceof Output)
 					getPipe().removeOutput((Output) p.currentPart);
 				else
 					throw new InternalException(
 							"Invalid sub-class of PipePart '%s'", p.currentPart);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot remove PipePart '%s'", p.currentPart);
 			}
 			resetCurrentPart();
 			painter.updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void configureCurrentPart(final boolean onlyIfNoIcon) {
 		if (hasCurrentPart() && !p.currentPart.getGuiConfigs().isEmpty()
 				&& (!onlyIfNoIcon || currentIcon == null) && ensureModifyable()) {
 			createConfigureDialog("Configure", p.currentPart, null);
 			painter.recalculatePipePartWidth(p.currentPart, getGraphics());
 			repaint();
 		}
 	}
 
 	protected void checkIconClicked() {
 		if (currentIcon == BoardPainter.ICON_CONF) configureCurrentPart(false);
 		if (currentIcon == BoardPainter.ICON_DGR) toggleDiagram();
 	}
 
 	protected void toggleDiagram() {
 		if (hasCurrentPart())
 			p.currentPart.setVisualize(!p.currentPart.isVisualize());
 	}
 
 	protected void clearBoard() {
 		if (ensureModifyable()) {
 			try {
 				getPipe().reset();
 			} catch (final PipeException e) {
 				Log.error(e, "Cannot clear the board");
 			}
 			painter.getSet().clear();
 			resetCurrentPart();
 			delayedReordering = false;
 			painter.updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void layoutBoard() {
 		if (layoutThread != null) return;
 		p.layouter = new BoardAutoLayouter(this);
 		layoutThread = new Thread() {
 			@Override
 			public void run() {
 				layoutThreadRun();
 			}
 		};
 		updateState();
 		layoutThread.start();
 	}
 
 	protected void exportBoard() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Export Part 1: Image", "png"));
 		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
 
 		File filePNG = fc.getSelectedFile();
 		if (!filePNG.getName().contains("."))
 			filePNG = new File(filePNG.getPath() + ".png");
 
 		fc.resetChoosableFileFilters();
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Export Part 2: HTML", "html"));
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Export Part 2: Latex", "tex"));
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Export Part 2: Text", "txt"));
 		fc.setSelectedFile(new File(filePNG.getPath().replace(".png", "")));
 		fc.setFileFilter(fc.getChoosableFileFilters()[1]);
 		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
 		File fileTXT = fc.getSelectedFile();
 		if (!fileTXT.getName().contains(".")
 				&& fc.getFileFilter() instanceof FileNameExtensionFilter)
 			fileTXT = new File(fileTXT.getPath()
 					+ "."
 					+ ((FileNameExtensionFilter) fc.getFileFilter())
 							.getExtensions()[0]);
 
 		exportBoardToFile(filePNG, fileTXT);
 	}
 
 	private void exportBoardToFile(final File filePNG, final File fileTXT) {
 		try {
 			// export the image
 			final BoardPainter bp = new BoardPainter();
 			BufferedImage img = new BufferedImage(1, 1,
 					BufferedImage.TYPE_INT_RGB);
 			bp.setPipe(pipe, img.getGraphics(), false);
 			final Dimension pref = bp.getPreferredSize();
 			img = new BufferedImage(pref.width, pref.height,
 					BufferedImage.TYPE_INT_RGB);
 			bp.setBounds(pref.width, pref.height, false);
 			final Graphics g = img.getGraphics();
 			g.setClip(0, 0, pref.width, pref.height);
 			final PaintParameters pprm = new PaintParameters();
 			pprm.g = g;
 			pprm.modifyable = true;
 			final Color old1 = BoardConfiguration.CFG_BACKGROUND.getContent();
 			final Color old2 = BoardConfiguration.CFG_RECT_BACKGROUND
 					.getContent();
 			try {
 				BoardConfiguration.CFG_BACKGROUND.setContent(Color.WHITE);
 				BoardConfiguration.CFG_RECT_BACKGROUND.setContent(new Color(
 						245, 245, 255));
 				bp.paint(pprm);
 			} finally {
 				BoardConfiguration.CFG_BACKGROUND.setContent(old1);
 				BoardConfiguration.CFG_RECT_BACKGROUND.setContent(old2);
 			}
 			ImageIO.write(img, "png", filePNG);
 
 			// export the text
 			final BufferedWriter out = new BufferedWriter(new FileWriter(
 					fileTXT));
 			if (fileTXT.getPath().endsWith(".html"))
 				out.write(String.format("<html><h1>%s</h1><br><img src=\"%s\" "
 						+ "alt=\"Image of the Board\"><br>", pipe.getTitle(),
 						filePNG.getName()));
 			else if (fileTXT.getPath().endsWith(".tex"))
 				out.write(String.format("\\subsection{%s}\n\n\\imageOwn{%s}"
 						+ "{%s}{Overview for the Pipe: %s}"
 						+ "{width=\\textwidth}\n", pipe.getTitle(), filePNG
 						.getName().replace(".png", ""), pipe.getTitle(), pipe
 						.getTitle()));
 			else {
 				out.write(pipe.getTitle());
 				out.write('\n');
 				for (int i = pipe.getTitle().length(); i > 0; --i)
 					out.write('=');
 				out.write(String.format("\nFor an image of the "
 						+ "board see '%s'\n\n", filePNG.getName()));
 			}
 
 			for (final PipePart pp : pipe.getInputList())
 				exportPipePart(out, pp, bp, fileTXT);
 			for (final PipePart pp : pipe.getConverterList())
 				exportPipePart(out, pp, bp, fileTXT);
 			for (final PipePart pp : pipe.getOutputList())
 				exportPipePart(out, pp, bp, fileTXT);
 			out.close();
 		} catch (final IOException e) {
 			Log.error(e, "Cannot export board");
 		}
 	}
 
 	private static void exportPipePart(final BufferedWriter out,
 			final PipePart pp, final BoardPainter bp, final File file)
 			throws IOException {
 		if (file.getPath().endsWith(".html"))
 			out.write(getPipePartInfoHTML(pp, bp));
 		else if (file.getPath().endsWith(".tex"))
 			out.write(getPipePartInfoLatex(pp, bp));
 		else
 			out.write(getPipePartInfoASCII(pp, bp));
 	}
 
 	protected void layoutThreadRun() {
 		try {
 			p.layouter.start();
 		} finally {
 			p.layouter = null;
 			layoutThread = null;
 			if (!closed) {
 				updateState();
 				checkPipeOrdering(null);
 				painter.updateSaneConfigCache();
 				repaint();
 			}
 		}
 	}
 
 	protected void addPipePart(final Class<? extends PipePart> part,
 			final Point location) {
 		if (!ensureModifyable()) return;
 		try {
 			final PipePart pp = part.newInstance();
 			createConfigureDialog("Add", pp, new Runnable() {
 				@Override
 				public void run() {
 					final Rectangle r = pp.getGuiPosition().createCopy();
 					if (location != null) r.setLocation(location);
 					if (SNAP_TO_GRID > 0) {
 						r.x = r.x / SNAP_TO_GRID * SNAP_TO_GRID;
 						r.y = r.y / SNAP_TO_GRID * SNAP_TO_GRID;
 					}
 					getPainter().check(r, pp);
 					pp.setGuiPosition(r);
 					try {
 						if (pp instanceof Input)
 							getPipe().addInput((Input) pp);
 						else if (pp instanceof Converter)
 							getPipe().addConverter((Converter) pp);
 						else if (pp instanceof Output)
 							getPipe().addOutput((Output) pp);
 						else
 							throw new InternalException(
 									"Invalid sub-class of PipePart '%s'", pp);
 						getPainter().addToSet(pp, getGraphics(), true);
 						checkPipeOrdering(null);
 					} catch (final StateException e) {
 						Log.error(e, "Cannot add new PipePart");
 					}
 					getPainter().updateSaneConfigCache();
 					repaint();
 				}
 			});
 		} catch (final InstantiationException e) {
 			Log.error(e);
 		} catch (final IllegalAccessException e) {
 			Log.error(e);
 		}
 	}
 
 	protected void replacePipePart(final Class<? extends PipePart> part) {
 		if (!ensureModifyable() || p.currentPart == null) return;
 		try {
 			final PipePart pp = part.newInstance();
 			createConfigureDialog("Replace With", pp, new Runnable() {
 				@Override
 				public void run() {
 					try {
 						if (pp instanceof Input)
 							getPipe().addInput((Input) pp);
 						else if (pp instanceof Converter)
 							getPipe().addConverter((Converter) pp);
 						else if (pp instanceof Output)
 							getPipe().addOutput((Output) pp);
 						else
 							throw new InternalException(
 									"Invalid sub-class of PipePart '%s'", pp);
 						for (final PipePart srcPP : getPainter().getSet())
 							if (srcPP.getConnectedPipeParts().contains(
 									getCurrentPart()))
 								srcPP.connectToPipePart(pp);
 						for (final PipePart trgPP : getCurrentPart()
 								.getConnectedPipeParts())
 							pp.connectToPipePart(trgPP);
 					} catch (final StateException e) {
 						Log.error(e, "Cannot replace PipePart");
 					}
 					getPainter().addToSet(pp, getGraphics(), true);
 					final Rectangle r = pp.getGuiPosition().createCopy();
 					r.setLocation(getCurrentPart().getGuiPosition().getX(),
 							getCurrentPart().getGuiPosition().getY());
 					removeCurrentPart();
 					getPainter().check(r, pp);
 					pp.setGuiPosition(r);
 					getPainter().updateSaneConfigCache();
 					checkPipeOrdering(null);
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
 	 * @param ps
 	 *            current cursor position (scaled to screen)
 	 */
 	protected void mouseDragged(final Point ps) {
 		if (!hasCurrentPart() || handlePoint == null) return;
 		final Point pOrg = getOriginal(ps);
 		if (p.currentConnection == null) {
 			// move pipe-part
 			final Rectangle orgPos = p.currentPart.getGuiPosition()
 					.createCopy();
 			final Rectangle newPos = new Rectangle(orgPos);
 			Rectangle clip = new Rectangle(orgPos);
 			unionConnectionTargets(clip);
 			unionConnectionSources(clip);
 			newPos.setLocation(pOrg.x - handlePoint.x, pOrg.y - handlePoint.y);
 			if (SNAP_TO_GRID > 0) {
 				newPos.x = newPos.x / SNAP_TO_GRID * SNAP_TO_GRID;
 				newPos.y = newPos.y / SNAP_TO_GRID * SNAP_TO_GRID;
 			}
 			painter.check(newPos, p.currentPart);
 			p.currentPart.setGuiPosition(newPos);
 			if (!checkPipeOrdering(null)) p.currentPart.setGuiPosition(orgPos);
 			clip = clip.union(newPos);
 			unionConnectionTargets(clip);
 			unionConnectionSources(clip);
 			// need to take care of labels
 			clip.grow(GROW_LABEL_REDRAW, GROW_LABEL_REDRAW);
 			scaleRect(clip);
 			repaint(clip);
 			updatePrefBounds();
 		} else {
 			// move connector instead of pipe-part
 			if (!ensureModifyable()) return;
 			if (p.currentConnectionsTarget != null) {
 				try {
 					p.currentPart
 							.disconnectFromPipePart(p.currentConnectionsTarget);
 				} catch (final StateException e) {
 					Log.error(e, "Cannot delete connection");
 				}
 				if (painter.updateSaneConfigCache()) repaint();
 				p.currentConnectionsTarget = null;
 			}
 
 			final Rectangle r = p.currentPart.getGuiPosition().createCopy()
 					.union(p.currentConnection);
 
 			p.currentConnection.setLocation(pOrg.x - handlePoint.x, pOrg.y
 					- handlePoint.y);
 			p.currentConnection.setSize(0, 0);
 			painter.check(p.currentConnection, null);
 			p.currentConnectionValid = false;
 			for (final PipePart pp : painter.getSet())
 				if (pp.getGuiPosition().contains(
 						p.currentConnection.getLocation())) {
 					p.currentConnectionValid = p.currentPart
 							.isConnectionAllowed(pp);
 					break;
 				}
 			mouseMoved(ps);
 
 			Rectangle2D.union(r, p.currentConnection, r);
 			// need to take care of labels
 			r.grow(GROW_LABEL_REDRAW, GROW_LABEL_REDRAW);
 			scaleRect(r);
 			repaint(r);
 		}
 	}
 
 	private void unionConnectionSources(final Rectangle r) {
 		for (final PipePart srcPP : painter.getSet())
 			if (srcPP.getConnectedPipeParts().contains(p.currentPart))
 				unionConnection(r, srcPP);
 	}
 
 	private void unionConnectionTargets(final Rectangle r) {
 		for (final PipePart trgPP : p.currentPart.getConnectedPipeParts())
 			unionConnection(r, trgPP);
 	}
 
 	private void unionConnection(final Rectangle r, final PipePart pp) {
 		final Point pt = new Point();
 		BoardPainter.calcConnectorPositions(p.currentPart.getGuiPosition(), pp
 				.getGuiPosition(), null, pt);
 		Rectangle2D.union(r, new Rectangle(pt.x, pt.y, 0, 0), r);
 	}
 
 	/**
 	 * Should be called whenever the mouse is moved over this section.
 	 * 
 	 * @param ps
 	 *            current cursor position (scaled to screen)
 	 */
 	protected void mouseMoved(final Point ps) {
 		final Point porg = getOriginal(ps);
 		PipePart found = null;
 		for (final PipePart pp : painter.getSet())
 			if (pp.getGuiPosition().contains(porg)) {
 				found = pp;
 				break;
 			}
 		if (p.underCursor != found) {
 			// TODO FIX remove if-clause above once hover is per icon
 			if (p.underCursor != null)
 				repaint(scaleRect(p.underCursor.getGuiPosition().createCopy()));
 			p.underCursor = found;
 			if (p.underCursor != null)
 				repaint(scaleRect(p.underCursor.getGuiPosition().createCopy()));
 		}
 	}
 
 	/**
 	 * Remembers the {@link PipePart} and the connector which is under the given
 	 * cursor position for later use in {@link #mouseDragged(Point)} and during
 	 * {@link #paintComponent(Graphics)}.
 	 * 
 	 * @param ps
 	 *            current cursor position (scaled to screen)
 	 */
 	protected void updateCurrent(final Point ps) {
 		// invoked on click or when a drag&drop operation starts
 		updateCurrent0(ps);
 		repaint();
 	}
 
 	private void updateCurrent0(final Point pscr) {
 		resetCurrentPart();
 
 		// check all pipe-parts
 		final Point porg = getOriginal(pscr);
 		for (final PipePart pp : painter.getSet())
 			if (pp.getGuiPosition().contains(porg)) {
 				p.currentPart = pp;
 				final Object res = BoardPainter.getPipePartElement(pp, porg);
 				if (res instanceof Icon)
 					currentIcon = (Icon) res;
 				else if (res instanceof Point)
 					handlePoint = (Point) res;
 				else {
 					p.currentConnection = new Rectangle(porg.x, porg.y, 0, 0);
 					handlePoint = new Point(0, 0);
 				}
 				return;
 			}
 
 		// check all connections
 		for (final PipePart srcPP : painter.getSet())
 			for (final PipePart trgPP : srcPP.getConnectedPipeParts()) {
 				final Point ps = new Point();
 				final Point pt = new Point();
 				BoardPainter.calcConnectorPositions(srcPP.getGuiPosition(),
 						trgPP.getGuiPosition(), ps, pt);
				if (Line2D.ptSegDistSq(ps.x, ps.y, pt.x, pt.y, porg.x, porg.y) < LINE_CLICK_DIST) {
 					p.currentPart = srcPP;
 					p.currentConnection = new Rectangle(porg.x, porg.y, 0, 0);
 					p.currentConnectionsTarget = trgPP;
 					handlePoint = new Point(porg.x - pt.x, porg.y - pt.y);
 					return;
 				}
 			}
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
 		if (p.currentConnection != null && p.currentConnectionsTarget == null
 				&& ensureModifyable()) {
 			final Point pOrg = new Point(p.currentConnection.getLocation());
 			for (final PipePart pp : painter.getSet())
 				if (pp.getGuiPosition().contains(pOrg)
 						&& p.currentPart.isConnectionAllowed(pp)) {
 					try {
 						p.currentPart.connectToPipePart(pp);
 					} catch (final StateException e) {
 						Log.error(e, "Cannot create connection");
 					}
 					painter.updateSaneConfigCache();
 					break;
 				}
 		}
 		resetCurrentTransients();
 		repaint();
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
 	private <T extends PipePart> List<T> getSortedParts(final Class<T> clazz) {
 		final List<T> res = new ArrayList<T>();
 		for (final PipePart pp : painter.getSet())
 			if (clazz.isInstance(pp)) res.add((T) pp);
 		Collections.sort(res, new Comparator<T>() {
 			@Override
 			public int compare(final T pp1, final T pp2) {
 				final ImmutableRectangle r1 = pp1.getGuiPosition();
 				final ImmutableRectangle r2 = pp2.getGuiPosition();
 				int cmp = r1.getY() - r2.getY();
 				if (cmp == 0) cmp = r1.getX() - r2.getX();
 				return cmp;
 			}
 		});
 		return res;
 	}
 
 	/**
 	 * Checks whether {@link PipePart}s in the {@link Pipe} need to be reordered
 	 * to reflect their GUI positions. If reordering is needed but the board is
 	 * in read-only state (as the Pipe is running), this method will return
 	 * false and delay the reordering until the Pipe has finished running.
 	 * 
 	 * @param warnIfNeeded
 	 *            if not empty, a message will be printed if reordering is
 	 *            needed
 	 * @return true if reordering was not needed or succeeded, false if
 	 *         reordering was needed and failed.
 	 * @see #updateState()
 	 */
 	protected boolean checkPipeOrdering(final String warnIfNeeded) {
 		// is reordering needed?
 		final List<Input> orderInput = getSortedParts(Input.class);
 		final List<Converter> orderConverter = getSortedParts(Converter.class);
 		final List<Output> orderOutput = getSortedParts(Output.class);
 		if (getPipe().getInputList().equals(orderInput)
 				&& getPipe().getConverterList().equals(orderConverter)
 				&& getPipe().getOutputList().equals(orderOutput)) {
 			Log.detail("Pipe ordering has not changed");
 			return true;
 		}
 
 		if (warnIfNeeded != null && !warnIfNeeded.isEmpty())
 			Log.warn(warnIfNeeded);
 
 		if (!ensureModifyable()) {
 			Log.detail("Pipe ordering has changed, reordering will be delayed");
 			// delay until pipe has finished ...
 			delayedReordering = true;
 			// ... and stop drag&drop operation (if any)
 			return false;
 		}
 		Log.detail("Pipe ordering has changed and will be reordered");
 
 		// reorder
 		try {
 			getPipe().reorderInputs(orderInput);
 			getPipe().reorderConverter(orderConverter);
 			getPipe().reorderOutputs(orderOutput);
 		} catch (final StateException e) {
 			Log.error(e, "Cannot reorder PipeParts");
 		} catch (final IllegalArgumentException e) {
 			Log.error(e, "Cannot reorder PipeParts");
 		}
 		delayedReordering = false;
 		repaint();
 		return true;
 	}
 
 	protected void updatePrefBounds() {
 		setPreferredSize(painter.getPreferredSize());
 		revalidate();
 	}
 
 	protected void updateBounds(final int width, final int height) {
 		painter.setBounds(width, height, true);
 		checkPipeOrdering("Resizing the board changed the ordering of the Pipe !!! "
 				+ "Please check ordering of all PipeParts.");
 		repaint();
 	}
 
 	protected void showMenu(final Component invoker, final int x, final int y) {
 		lastMenuLocation = getOriginal(new Point(x, y));
 		if (x <= painter.getBorder1(true))
 			showMenu(menuInput, invoker, x, y);
 		else if (x >= painter.getBorder2(true))
 			showMenu(menuOutput, invoker, x, y);
 		else
 			showMenu(menuConverter, invoker, x, y);
 	}
 
 	protected void showMenu(final JPopupMenu menu, final Component invoker,
 			final int x, final int y) {
 		final MenuElement[] items = menu.getSubElements();
 		((AbstractButton) items[idxMenuAdd]).setEnabled(p.modifyable
 				&& !hasCurrentPart());
 		((AbstractButton) items[idxMenuRepl]).setEnabled(p.modifyable
 				&& hasCurrentPart());
 		((AbstractButton) items[idxMenuConfPart]).setEnabled(p.modifyable
 				&& hasCurrentPart() && p.currentConnection == null
 				&& !p.currentPart.getGuiConfigs().isEmpty());
 		((AbstractButton) items[idxMenuDelPart]).setEnabled(p.modifyable
 				&& hasCurrentPart() && p.currentConnection == null);
 		((AbstractButton) items[idxMenuDelPartConn]).setEnabled(p.modifyable
 				&& hasCurrentPart() && p.currentConnection == null
 				&& !p.currentPart.getConnectedPipeParts().isEmpty());
 		((AbstractButton) items[idxMenuDelConn]).setEnabled(p.modifyable
 				&& p.currentConnection != null);
 		((AbstractButton) items[idxMenuToggleDgr]).setEnabled(hasCurrentPart());
 		((AbstractButton) items[idxMenuToggleDgr]).setSelected(hasCurrentPart()
 				&& p.currentPart.isVisualize());
 		((AbstractButton) items[idxMenuClearBoard]).setEnabled(p.modifyable
 				&& !hasCurrentPart());
 		((AbstractButton) items[idxMenuLayoutBoard])
 				.setEnabled(!hasCurrentPart() && layoutThread == null);
 		((AbstractButton) items[idxMenuExportBoard])
 				.setEnabled(!hasCurrentPart());
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
 		dlg.setTitle(String.format("%s %s", prefix, pp.getName()));
 		final JPanel cfgItemPanel = new JPanel();
 		final Layouter lay = new Layouter(cfgItemPanel);
 		boolean hasGreedy = false;
 		int idx = 0;
 		for (final ConfigValue v : pp.getGuiConfigs()) {
 			// each config-value gets its own JPanel so they don't
 			// interfere with each other.
 			// LBL1 SUB1
 			// LBL2 SUB2
 			// LBL3 SUB3
 			// BUTTONS
 			final JPanel sub = new JPanel();
 			final int cmpCnt = sub.getComponentCount();
 			final boolean greedy = v.insertGUIComponents(new Layouter(sub));
 			final String compLabel = v.getLabel() + ":";
 			final JLabel lbl = new JLabel(compLabel, SwingConstants.RIGHT);
 			lbl.setVerticalAlignment(SwingConstants.TOP);
 			lay.add(lbl, false);
 			lay.addWholeLine(sub, greedy);
 			hasGreedy |= greedy;
 			for (int i = cmpCnt; i < sub.getComponentCount(); ++i)
 				if (sub.getComponent(i) instanceof JComponent)
 					((JComponent) sub.getComponent(i)).setToolTipText(pp
 							.getConfigHelp(idx));
 			++idx;
 		}
 		if (!hasGreedy) lay.addVerticalSpacer();
 
 		final JPanel buttons = new JPanel();
 		final Layouter lb = new Layouter(buttons);
 
 		dlg.setLayout(new BorderLayout());
 		dlg.add(cfgItemPanel, BorderLayout.CENTER);
 		dlg.add(buttons, BorderLayout.SOUTH);
 		final Icon cfgImage = pp.getConfigImage();
 		if (cfgImage instanceof ImageIcon) {
 			final JLabel lbl = new JLabel(
 					createTransparentImage((ImageIcon) cfgImage),
 					SwingConstants.RIGHT);
 			lbl.setVerticalAlignment(SwingConstants.TOP);
 			dlg.add(lbl, BorderLayout.WEST);
 		}
 
 		final String helpFile = pp.getHelpFile();
 		final JButton btnHelp = lb.addButton(Button.Help, Layouter.help(dlg,
 				helpFile));
 		btnHelp.setEnabled(HelpLoader.isHelpAvailable(helpFile));
 		lb.addSpacer();
 		dlg.getRootPane().setDefaultButton(
 				lb.addButton(Button.Ok, new Runnable() {
 					@Override
 					public void run() {
 						if (saveConfigChanges(dlg, pp, true)) {
 							dlg.dispose();
 							if (runIfOK != null) runIfOK.run();
 						}
 					}
 				}));
 		lb.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				saveConfigChanges(dlg, pp, false);
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
 		dlg.setModal(true);
 		HelpDialog.closeHelpIfOpen();
 		dlg.setVisible(true);
 		HelpDialog.closeHelpIfOpen();
 	}
 
 	private Icon createTransparentImage(final ImageIcon image) {
 		final ImageFilter filter = new RGBImageFilter() {
 			@Override
 			public int filterRGB(final int x, final int y, final int rgb) {
 				// replace opaque white with transparent
 				return rgb == 0xFFFFFFFF ? 0 : rgb;
 			}
 		};
 		return new ImageIcon(Toolkit.getDefaultToolkit().createImage(
 				new FilteredImageSource(image.getImage().getSource(), filter)));
 	}
 
 	protected boolean saveConfigChanges(final Component dlg, final PipePart pp,
 			final boolean continueable) {
 		if (!ensureModifyable()) return false;
 		for (final ConfigValue v : pp.getGuiConfigs())
 			v.setFromGUIComponents();
 		pp.configValuesChanged();
 		if (painter.updateSaneConfigCache()) repaint();
 		final String cfgRes = pp.isCachedConfigSane();
 		if (cfgRes != null && !cfgRes.isEmpty()) {
 			Log.warn("Configuration is invalid: %s", cfgRes);
 			if (JOptionPane.showOptionDialog(dlg, String.format(
 					"Configuration is invalid: %s%s", cfgRes,
 					continueable ? "\nIgnore and continue?" : ""), null,
 					continueable ? JOptionPane.YES_NO_OPTION
 							: JOptionPane.DEFAULT_OPTION,
 					JOptionPane.ERROR_MESSAGE, null, null, null) != JOptionPane.YES_OPTION)
 				return false;
 		}
 		try {
 			pp.configure();
 		} catch (final PipeException e) {
 			Log.error(e);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String getToolTipText(final MouseEvent event) {
 		if (p.underCursor == null) return null;
 		return getPipePartInfoHTML(p.underCursor, painter);
 	}
 
 	public void updateState() {
 		final boolean modifNow = !MainFrame.the().isPipeRunning()
 				&& layoutThread == null && !closed;
 		if (p.modifyable ^ modifNow) {
 			p.modifyable = modifNow;
 			if (delayedReordering) checkPipeOrdering(null);
 			repaint();
 		}
 	}
 
 	private boolean ensureModifyable() {
 		if (!p.modifyable)
 			Log.error("Configuration board is read-only as "
 					+ "the Pipe or the Auto-Layouter is currently "
 					+ "running.");
 		return p.modifyable;
 	}
 
 	public void setZoom(final double zoom) {
 		if (zoom > 0)
 			painter.setScale(1 + zoom * 9);
 		else
 			painter.setScale(zoom < 0 ? 1 / (1 + -zoom * 9) : 1);
 		updatePrefBounds();
 		repaint();
 		MainFrame.the().getMainPipePanel().getPipeFlowVisualization()
 				.modified();
 	}
 
 	private Point getOriginal(final Point scaled) {
 		final double scale = painter.getScale();
 		return new Point((int) (scaled.x / scale), (int) (scaled.y / scale));
 	}
 
 	private Rectangle scaleRect(final Rectangle r) {
 		final double scale = painter.getScale();
 		r.x *= scale;
 		r.y *= scale;
 		r.width *= scale;
 		r.height *= scale;
 		return r;
 	}
 
 	public void closed() {
 		closed = true;
 		updateState();
 		if (layoutThread != null && p.layouter != null) {
 			p.layouter.interrupt();
 			layoutThread.interrupt();
 		}
 	}
 
 	public boolean hasCurrentPart() {
 		return p.currentPart != null;
 	}
 
 	public void resetCurrentTransients() {
 		currentIcon = null;
 		handlePoint = null;
 		p.currentConnection = null;
 		p.currentConnectionValid = false;
 	}
 
 	public void resetCurrentPart() {
 		p.currentPart = null;
 		currentIcon = null;
 		handlePoint = null;
 		resetCurrentConnection();
 	}
 
 	public void resetCurrentConnection() {
 		p.currentConnection = null;
 		p.currentConnectionsTarget = null;
 		p.currentConnectionValid = false;
 	}
 
 	public BoardPainter getPainter() {
 		return painter;
 	}
 
 	public PipePart getCurrentPart() {
 		return p.currentPart;
 	}
 
 	void setPipeflow(final Collection<PipeFlow> pipeflow) {
 		p.pipeflow = pipeflow;
 	}
 
 	private static String getPipePartInfoASCII(final PipePart pp,
 			final BoardPainter bp) {
 		final StringBuilder sb = new StringBuilder("");
 		sb.append(pp.getName());
 		sb.append("\n\t");
 		sb.append(pp.getDescription());
 		sb.append("\n");
 		for (final ConfigValue v : pp.getGuiConfigs()) {
 			sb.append("\t");
 			sb.append(v.getLabel());
 			sb.append("\t");
 			sb.append(v.asString().replace("\n", "\n\t\t"));
 			sb.append("\n");
 		}
 		sb.append("\n");
 		final String sc = bp.getSaneConfigCache().get(pp);
 		if (sc != null) {
 			sb.append("\tBad configuration:\n\t");
 			sb.append(sc.replace("\n", "\n\t"));
 		}
 		return sb.toString();
 	}
 
 	private static String getPipePartInfoHTML(final PipePart pp,
 			final BoardPainter bp) {
 		final StringBuilder sb = new StringBuilder("<html><b>");
 		sb.append(StringManip.safeHTML(pp.getName()));
 		sb.append("</b><p>");
 		sb.append(StringManip.safeHTML(pp.getDescription()));
 		sb.append("<table border=1>");
 		for (final ConfigValue v : pp.getGuiConfigs()) {
 			sb.append("<tr><td align=right>");
 			sb.append(StringManip.safeHTML(v.getLabel()));
 			sb.append("</td><td align=left>");
 			sb.append(StringManip.safeHTML(v.asString()));
 			sb.append("</td></tr>");
 		}
 		sb.append("</table>");
 		final String sc = bp.getSaneConfigCache().get(pp);
 		if (sc != null) {
 			sb.append("<p style=\"color:red\"><b>Bad configuration:</b><br>");
 			sb.append(StringManip.safeHTML(sc));
 			sb.append("</p>");
 		}
 		sb.append("<p style=\"color:blue\"><b>Statistics:</b><br>");
 		sb.append(pp.getFeedback().getHTMLTable());
 		sb.append("</p>");
 		sb.append("</html>");
 		return sb.toString();
 	}
 
 	private static String getPipePartInfoLatex(final PipePart pp,
 			final BoardPainter bp) {
 		final StringBuilder sb = new StringBuilder("");
 		String icoName = PipePartDetection.callHelp(pp.getClass(),
 				HelpKind.Icon);
 		if (icoName == null)
 			icoName = pp.getClass().getSimpleName() + "-icon.png";
 		sb.append(String.format("\n\\subsubsection*{\\protect\\mbox{\\protect"
 				+ "\\includegraphics[width=5mm]{%s}} %s}\n\n", icoName.replace(
 				".png", ""), StringManip.safeTex(pp.getName())));
 		sb.append(StringManip.safeTex(pp.getDescription()));
 		sb.append("\n\n");
 		sb.append("\\begin{longtable}{p{0.25\\textwidth} | "
 				+ "p{0.75\\textwidth}}\n");
 		// sb.append("\\hline\n");
 		for (final ConfigValue v : pp.getGuiConfigs()) {
 			sb.append(StringManip.safeTex(v.getLabel()));
 			sb.append(" & ");
 			sb.append(StringManip.safeTex(v.asString()).replace("\n", "\n & "));
 			sb.append(" \\\\\n");
 			// sb.append(" \\\\ \\hline\n");
 		}
 		sb.append("\\end{longtable}\n");
 		final String sc = bp.getSaneConfigCache().get(pp);
 		if (sc != null) {
 			sb.append("\\textcolor{red}{Bad configuration:\\\\\n");
 			sb.append(StringManip.safeTex(sc));
 			sb.append("\n}\n");
 		}
 		return sb.toString();
 	}
 
 }
