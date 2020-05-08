 // Copyright © 2011 Martin Ueding <dev@martin-ueding.de>
 
 /*
  * This file is part of jscribble.
  *
  * jscribble is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  *
  * jscribble is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * jscribble.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jscribble.drawPanel;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 
 import javax.swing.JPanel;
 
 import jscribble.VersionName;
 import jscribble.helpers.Localizer;
 import jscribble.helpers.Logger;
 import jscribble.helpers.SettingsWrapper;
 import jscribble.notebook.BufferedImageWrapper;
 import jscribble.notebook.NoteBook;
 
 /**
  * Displays the current page of a NoteBook. It also listens to the mouse and
  * relays the movements to the NoteBook as line drawing commands. It also
  * features a command listener for more user interaction.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 @SuppressWarnings("serial")
 public class DrawPanel extends JPanel {
 	/**
 	 * Color of the help lines.
 	 */
 	private static final Color lineColor = SettingsWrapper.getColor("ruling_line_color");
 
 	/**
 	 * The spacing between the help lines.
 	 */
 	private static final int lineSpacing = SettingsWrapper.getInteger("ruling_line_spacing");
 
 	/**
 	 * The NoteBook that is currently displayed.
 	 */
 	private NoteBook notebook;
 
 	/**
 	 * Handles the image output.
 	 */
 	private ImageObserver io = this;
 
 	/**
 	 * Whether to display the help panel.
 	 */
 	private boolean showHelp = false;
 
 	/**
 	 * A list with all the HelpItem to display.
 	 */
 	private HelpItem[] helpItems = {
 		new HelpItem(Localizer.get("h, F1"), Localizer.get("show help")),
 		new HelpItem(Localizer.get("h, F1, <Esc>"),
 		Localizer.get("hide help")),
 		new HelpItem(
 		    Localizer.get("j, <Space>, <Enter>, <DownArrow>, <RightArrow>"),
 		    Localizer.get("go forward")),
 		new HelpItem(Localizer.get("k, <Backspace>, <UpArrow>, <LeftArrow>"),
 		Localizer.get("go backward")),
 		new HelpItem(Localizer.get("f, <Pos1>"), Localizer.get("goto first")),
 		new HelpItem(Localizer.get("l, <End>"), Localizer.get("goto last")),
 		new HelpItem(Localizer.get("<Alt-F4> / <CMD-Q>"),
 		Localizer.get("save & exit")),
 		new HelpItem(Localizer.get("+"),
 		Localizer.get("increase onion layers")),
 		new HelpItem(Localizer.get("-"),
 		Localizer.get("decrease onion layers")),
 		new HelpItem(Localizer.get("r"), Localizer.get("toggle ruling")),
 		new HelpItem(Localizer.get("g"), Localizer.get("toggle graph paper")),
 	};
 
 	/**
 	 * How many images should be composed in a see through way.
 	 */
 	private int onionMode;
 
 	/**
 	 * A cached image that is used instead of the original images in the onion
 	 * mode to conserve performance. Use it via the getter.
 	 */
 	private BufferedImage cachedImage;
 
 	/**
 	 * The wrapper for the current image. Use it via the getter.
 	 */
 	private BufferedImageWrapper imageWrapper;
 
 	/**
 	 * Whether the help splash screen is (still) displayed.
 	 */
 	private boolean showHelpSplash = true;
 
 	/**
 	 * Which type of ruling is used.
 	 */
 	private RulingType ruling = RulingType.NONE;
 
 	/**
 	 * Creates a new display panel that will listen to changes from a specific
 	 * NoteBook.
 	 *
 	 * @param notebook the NoteBook to display
 	 */
 	public DrawPanel(NoteBook notebook) {
 		this.notebook = notebook;
 
 		// Notify this instance when the notebook was is done drawing.
 		notebook.setDoneDrawing(new Redrawer(this));
 
 		// This handles the painting onto the NoteBook that this DrawPanel
 		// displays.
 		PaintListener pl = new PaintListener(this);
 		addMouseMotionListener(pl);
 		addMouseListener(pl);
 	}
 
 	/**
 	 * Draws the help screen if needed.
 	 *
 	 * @param g2 Graphics2D to draw in
 	 */
 	private void drawHelp(Graphics2D g2) {
 		if (!showHelp) {
 			return;
 		}
 
 		// Draw a dark rectangle to write the help text on.
 		g2.setColor(SettingsWrapper.getColor("help_screen_background_color"));
 		int helpMargin = SettingsWrapper.getInteger("help_screen_margin");
 		int helpBorderRadius = SettingsWrapper.getInteger("help_screen_border_radius");
 		g2.fillRoundRect(
 		    helpMargin,
 		    helpMargin,
 		    getWidth() - helpMargin * 2,
 		    getHeight() - helpMargin * 2,
 		    helpBorderRadius,
 		    helpBorderRadius);
 		g2.setColor(Color.WHITE);
 
 		// Iterate through the help items and display them.
 		int i = 0;
 		int vspacing = SettingsWrapper.getInteger("help_screen_vspacing");
 		int spacing = SettingsWrapper.getInteger("help_screen_spacing");
 		int padding = SettingsWrapper.getInteger("help_screen_padding");
 		for (HelpItem h : helpItems) {
 			g2.drawString(h.helptext, padding, i * vspacing + padding);
 			g2.drawString(h.key, spacing + padding, i * vspacing +
 			        padding);
 			i++;
 		}
 
 		// Print the version identifier.
 		g2.setColor(Color.GRAY);
 		g2.drawString(String.format(Localizer.get("Version %s"),
 		        VersionName.version), padding, getHeight() - padding);
 	}
 
 	/**
 	 * Draws a help splash screen at the beginning.
 	 *
 	 * @param g2 Graphics2D to draw in
 	 */
 	private void drawHelpSplash(Graphics2D g2) {
 		if (!showHelpSplash) {
 			return;
 		}
 
 		// Draw a dark rectangle to write the help text on.
 		g2.setColor(SettingsWrapper.getColor("help_splash_background_color"));
 		Dimension splashSize = new Dimension(
 		    getWidth() - SettingsWrapper.getInteger("help_splash_margin") * 2,
 		    SettingsWrapper.getInteger("help_splash_height"));
 		int helpSplashBorderRadius = SettingsWrapper.getInteger("help_splash_border_radius");
 		g2.fillRoundRect(
 		    (getWidth() - splashSize.width) / 2,
 		    (getHeight() - splashSize.height) / 2,
 		    splashSize.width, splashSize.height,
 		    helpSplashBorderRadius,
 		    helpSplashBorderRadius);
 		g2.setColor(Color.WHITE);
 
 		g2.drawString(Localizer.get("Press h or F1 to get help."),
 		        // TODO --> defaultConfig
 		        (getWidth() - splashSize.width) / 2 + 50, getHeight() / 2 + 5);
 	}
 
 	/**
 	 * Draws a line onto the current sheet. If onion mode is used, it will be
 	 * cached in another image. To the user, there will be no difference.
 	 *
 	 * @param x
 	 * @param y
 	 * @param x2
 	 * @param y2
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		if (hasCachedImage()) {
 			getImageWrapper().drawLine(x, y, x2, y2);
 		}
 		notebook.drawLine(x, y, x2, y2);
 
 		showHelpSplash = false;
 	}
 
 	/**
 	 * Draws the helping lines if needed.
 	 *
 	 * @param g2 Graphics2D to draw on
 	 */
 	private void drawLines(Graphics2D g2) {
 		if (ruling != RulingType.NONE) {
 			g2.setColor(lineColor);
 
 			// Vertical lines.
 			if (ruling == RulingType.GRAPH) {
 				for (int i = lineSpacing; i < getWidth(); i += lineSpacing) {
 					g2.drawLine(i, 0, i, getHeight());
 				}
 			}
 
 			// Horizontal lines.
 			for (int i = lineSpacing; i < getHeight(); i += lineSpacing) {
 				g2.drawLine(0, i, getWidth(), i);
 			}
 		}
 	}
 
 	/**
 	 * Draws the number of onion layers as a string.
 	 *
 	 * @param g2 Graphics2D to draw on
 	 */
 	private void drawOnionInfo(Graphics2D g2) {
 		if (!isOnionMode()) {
 			return;
 		}
 
 		g2.drawString(
 		    String.format(Localizer.get("Onion Layers: %d"), onionMode),
		    SettingsWrapper.getInteger("onion_info_position_top"),
		    SettingsWrapper.getInteger("onion_info_position_left")
 		);
 	}
 
 	/**
 	 * Draws the page number on top.
 	 *
 	 * @param g2 Graphics2D to draw on
 	 */
 	private void drawPageNumber(Graphics2D g2) {
 		g2.setColor(Color.BLUE);
 		g2.drawString(String.format(Localizer.get("Page %d/%d"),
 		        notebook.getCurrentSheet().getPagenumber(),
 		        // TODO --> defaultConfig
 		        notebook.getSheetCount()), getWidth() / 2, 15);
 	}
 
 	/**
 	 * Draws the scroll panels at the side of the screen if enabled in the
 	 * config.
 	 *
 	 * @param g Context of the current DrawPanel
 	 */
 	private void drawScrollPanels(Graphics2D g) {
 		// Do nothing if the option is not set.
 		if (!SettingsWrapper.getBoolean("show_scroll_panels", false)) {
 			return;
 		}
 
 		try {
 			// Read the dimension of the panel from the config file.
 			int scrollPanelRadius = SettingsWrapper.getInteger("scroll_panel_width");
 			int scrollPanelPadding = SettingsWrapper.getInteger("scroll_panel_padding");
 
 			// Draw the panels on the sides.
 			// TODO --> defaultConfig
 			g.setColor(new Color(0, 0, 0, 100));
 			g.fillRoundRect(-scrollPanelRadius, scrollPanelPadding,
 			        2 * scrollPanelRadius,
 			        getHeight() - 2 * scrollPanelPadding,
 			        scrollPanelRadius, scrollPanelRadius);
 			g.fillRoundRect(getWidth() - scrollPanelRadius,
 			        scrollPanelPadding, 2 * scrollPanelRadius,
 			        getHeight() - 2 * scrollPanelPadding,
 			        scrollPanelRadius, scrollPanelRadius);
 		}
 		catch (NumberFormatException e) {
 			Logger.handleError(Localizer.get(
 			            "Malformed entry in config file."));
 		}
 	}
 
 	/**
 	 * Erases a line on the NoteBook. If a cachedImage is used, the line is
 	 * erased on that image as well.
 	 *
 	 * @param x
 	 * @param y
 	 * @param x2
 	 * @param y2
 	 */
 	public void eraseLine(int x, int y, int x2, int y2) {
 		if (hasCachedImage()) {
 			getImageWrapper().eraseLine(x, y, x2, y2);
 
 			// FIXME Prevent erasing of the underlying onion layers, maybe by
 			// redoing the background image.
 		}
 
 		notebook.eraseLine(x, y, x2, y2);
 
 		showHelpSplash = false;
 	}
 
 	/**
 	 * Returns the cached image. This can be the original image if there is no
 	 * onion mode used, or the layered image if used. If there is no image yet,
 	 * it will be created and composed.
 	 *
 	 * @return Image which contains all the drawing information
 	 */
 	private BufferedImage getCachedImage() {
 		// If the onion mode is not enables, the original image can be used.
 		if (!isOnionMode() && ruling == RulingType.NONE) {
 			return notebook.getCurrentSheet().getImg();
 		}
 
 		if (cachedImage == null) {
 			// Create a new blank image.
 			cachedImage = new BufferedImage(getWidth(), getHeight(),
 			        BufferedImage.TYPE_BYTE_GRAY);
 			Graphics2D g2 = (Graphics2D) cachedImage.getGraphics();
 			// TODO --> defaultConfig
 			g2.setColor(Color.WHITE);
 			g2.fillRect(0, 0, getWidth(), getHeight());
 
 			// Go back as many pages as there should be onion layers.
 			int wentBack = 0;
 			for (; wentBack < onionMode; wentBack++) {
 				int prevPageNumber = notebook.getCurrentSheet()
 				        .getPagenumber();
 				notebook.goBackwards();
 				if (prevPageNumber == notebook.getCurrentSheet()
 				        .getPagenumber()) {
 					break;
 				}
 			}
 
 			// Set the layers to a given opacity.
 			g2.setComposite(AlphaComposite.getInstance(
 			            AlphaComposite.SRC_ATOP,
 			            // TODO --> defaultConfig
 			            (float)(0.8 / Math.max(onionMode, 1))));
 
 			// Iterate through from the bottom to the top layer and compose
 			// the images onto the cache image.
 			while (wentBack > 0) {
 				g2.drawImage(notebook.getCurrentSheet().getImg(), 0, 0, io);
 
 				// Move on to the next NoteSheet.
 				wentBack--;
 				notebook.goForward();
 			}
 
 			drawLines(g2);
 
 			g2.drawImage(notebook.getCurrentSheet().getImg(), 0, 0, io);
 		}
 
 		return cachedImage;
 	}
 
 	/**
 	 * Retrieves and initializes the BufferedImageWrapper for the current cached
 	 * image.
 	 *
 	 * @return BufferedImageWrapper of cachedImage
 	 */
 	private BufferedImageWrapper getImageWrapper() {
 		if (imageWrapper == null) {
 			imageWrapper = new BufferedImageWrapper(cachedImage);
 		}
 
 		return imageWrapper;
 	}
 
 	/**
 	 * Goes one page back.
 	 */
 	public void goBackwards() {
 		resetCachedImage();
 		notebook.goBackwards();
 	}
 
 	/**
 	 * Goes one page forward.
 	 */
 	public void goForward() {
 		resetCachedImage();
 		notebook.goForward();
 	}
 
 	/**
 	 * Goes to the first page.
 	 */
 	public void gotoFirst() {
 		resetCachedImage();
 		notebook.gotoFirst();
 	}
 
 	/**
 	 * Goes to the last page.
 	 */
 	public void gotoLast() {
 		resetCachedImage();
 		notebook.gotoLast();
 	}
 
 	/**
 	 * Determines whether a cached image is currently used.
 	 *
 	 * @return Whether a cached image is used.
 	 */
 	private boolean hasCachedImage() {
 		return cachedImage != null;
 	}
 
 	/**
 	 * Whether there are any onion layers displayed.
 	 *
 	 * @return Whether there are onion layers.
 	 */
 	private boolean isOnionMode() {
 		return onionMode > 0;
 	}
 
 	/**
 	 * Decreases the onion layers and does additional housekeeping.
 	 */
 	public void onionLayersDecrease() {
 		// Do nothing if there are no layers displayed currently.
 		if (!isOnionMode()) {
 			return;
 		}
 
 		resetCachedImage();
 		onionMode--;
 		repaint();
 	}
 
 	/**
 	 * Increases the onion layers and does additional housekeeping.
 	 */
 	public void onionLayersIncrease() {
 		resetCachedImage();
 
 		onionMode++;
 		repaint();
 	}
 
 	/**
 	 * Draws the NoteSheet and page number. If lines are on, they are drawn on
 	 * top of the image as well.
 	 *
 	 * @param g graphics context (usually given by Java itself).
 	 */
 	protected void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHints(new
 		        RenderingHints(RenderingHints.KEY_ANTIALIASING,
 		                RenderingHints.VALUE_ANTIALIAS_ON));
 
 
 		// Draw the current image.
 		g2.drawImage(getCachedImage(), 0, 0, io);
 
 		drawPageNumber(g2);
 		drawOnionInfo(g2);
 		drawScrollPanels(g2);
 		drawHelp(g2);
 		drawHelpSplash(g2);
 	}
 
 	/**
 	 * Resets the cached image after changing pages for instance.
 	 */
 	private void resetCachedImage() {
 		cachedImage = null;
 		imageWrapper = null;
 		showHelpSplash = false;
 	}
 
 	/**
 	 * Sets whether the help dialog is displayed.
 	 */
 	public void setShowHelp(boolean showHelp) {
 		this.showHelp = showHelp;
 	}
 
 	/**
 	 * Toggles the display of graph ruling.
 	 */
 	public void toggleGraphRuling() {
 		ruling = ruling == RulingType.GRAPH ? RulingType.NONE : RulingType.GRAPH;
 		resetCachedImage();
 	}
 
 	/**
 	 * Whether to display the help panel.
 	 */
 	public void toggleHelp() {
 		showHelp = !showHelp;
 		showHelpSplash = false;
 	}
 
 	/**
 	 * Toggles the display of (line) ruling.
 	 */
 	public void toggleRuling() {
 		ruling = ruling == RulingType.LINE ? RulingType.NONE : RulingType.LINE;
 		resetCachedImage();
 	}
 }
