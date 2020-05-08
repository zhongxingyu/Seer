 
 package org.paxle.desktop.impl.dialogues;
 
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 
 import javax.swing.JPanel;
 import javax.swing.JViewport;
 import javax.swing.Scrollable;
 import javax.swing.SwingConstants;
 
 public class ScrollablePanel extends JPanel implements Scrollable {
 	
 	public static final int NO_TRACK = 0;
 	public static final int MAXIMIZE = 1;
 	public static final int ALWAYS_TRACK = 2;
 	
 	private static final long serialVersionUID = 1L;
 	
 	private int tracksHeight = 0;
 	private int tracksWidth = 0;
 	
 	public ScrollablePanel() {
 	}
 	
 	public ScrollablePanel(final LayoutManager layout, final int tracksWidth, final int tracksHeight) {
 		super(layout);
 		this.tracksWidth = tracksWidth;
 		this.tracksHeight = tracksHeight;
 	}
 	
 	public ScrollablePanel(final boolean isDoubleBuffered) {
 		super(isDoubleBuffered);
 	}
 	
 	public ScrollablePanel(final LayoutManager layout, final boolean isDoubleBuffered) {
 		super(layout, isDoubleBuffered);
 	}
 	
 	public Dimension getPreferredScrollableViewportSize() {
 		return super.getPreferredSize();
 	}
 	
 	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
 		switch (orientation) {
 			case SwingConstants.VERTICAL:
 				return visibleRect.height;
 			case SwingConstants.HORIZONTAL:
 				return visibleRect.width;
 			default:
 				throw new IllegalArgumentException("Invalid orientation: " + orientation);
 		}
 	}
 	
 	public void setTracksWidth(int tracksWidth) {
 		this.tracksWidth = tracksWidth;
 	}
 	
 	public void setTracksHeight(int tracksHeight) {
 		this.tracksHeight = tracksHeight;
 	}
 	
 	public boolean getScrollableTracksViewportHeight() {
 		switch (tracksHeight) {
 			case NO_TRACK: return false;
 			case MAXIMIZE: return (super.getParent() instanceof JViewport) && (
 				((JViewport)super.getParent()).getVisibleRect().height > super.getPreferredSize().height);
 			case ALWAYS_TRACK: return true;
 			default:
 				throw new RuntimeException("illegal value for tracks-height: " + tracksHeight);
 		}
 	}
 	
 	public boolean getScrollableTracksViewportWidth() {
 		switch (tracksWidth) {
 			case NO_TRACK: return false;
 			case MAXIMIZE: return (super.getParent() instanceof JViewport) && (
 				((JViewport)super.getParent()).getVisibleRect().width > super.getPreferredSize().width);
 			case ALWAYS_TRACK: return true;
 			default:
 				throw new RuntimeException("illegal value for tracks-width: " + tracksWidth);
 		}
 	}
 	
 	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
 		switch (orientation) {
 			case SwingConstants.VERTICAL:
 				return visibleRect.height / 10;
 			case SwingConstants.HORIZONTAL:
 				return visibleRect.width / 10;
 			default:
 				throw new IllegalArgumentException("Invalid orientation: " + orientation);
 		}
 	}
}
