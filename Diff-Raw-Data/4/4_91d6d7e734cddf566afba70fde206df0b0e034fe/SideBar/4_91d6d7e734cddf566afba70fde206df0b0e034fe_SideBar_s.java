 /**
  * 
  */
 package de.unisiegen.tpml.ui;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.text.JTextComponent;
 
 import de.unisiegen.tpml.core.languages.LanguageScannerException;
 import de.unisiegen.tpml.graphics.StyledLanguageDocument;
 
 /**
  * @author marcell
  *
  */
 public class SideBar extends JComponent {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4668570581006435967L;
 	
 	private ImageIcon 									errorIcon	= null;
 	
 	private int[]												verticalPositions;
 	
 	private JScrollBar									vScrollBar;
 	
 	private JScrollBar									hScrollBar;
 	
 	private JScrollPane									scrollPane;
 	
 	private LanguageScannerException[]	exceptions;
 	
 	private StyledLanguageDocument			document;
 	
 	private JTextComponent							textComponent;
 	
 	private boolean											proppertyChanged;
 	
 	private int													currentLeft;
 	
 	private int													currentRight;
 
 	public SideBar (JScrollPane 						scrollPane,
 									StyledLanguageDocument	document,
 									JTextComponent					textComponent) {
 		super ();
 		
 		
 		this.currentLeft					= -1;
 		this.currentRight					= -1;
 		this.scrollPane						= scrollPane;
 		this.document							= document;
 		this.textComponent				= textComponent;
 		
 		this.errorIcon = new ImageIcon (getClass().getResource("/de/unisiegen/tpml/ui/icons/error.gif"));
 		
 		int imageWidth = this.errorIcon.getIconWidth();
 		
 		this.proppertyChanged 		= false;
 	
 		setMinimumSize(new Dimension (imageWidth, imageWidth));
 		
 		this.vScrollBar = this.scrollPane.getVerticalScrollBar();
 		this.hScrollBar = this.scrollPane.getHorizontalScrollBar();
 
 		this.vScrollBar.addAdjustmentListener(new AdjustmentListener () {
 			public void adjustmentValueChanged (AdjustmentEvent event) {
 				repaint ();
 			}
 		});
 		
 		this.document.addPropertyChangeListener("exceptions", new PropertyChangeListener() {
 			public void propertyChange (PropertyChangeEvent event) {
 				SideBar.this.proppertyChanged = true;
 				repaint ();
 			}
 		});
 		this.addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseMoved (MouseEvent event) {
 				SideBar.this.mouseMoved (event);
 			}
 		});
 		
 		this.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked (MouseEvent event) {
 				SideBar.this.mouseSelected(event);
 			}
 		});
 	}
 	
 	public void addSibeBarListener (SideBarListener listener) {
 		this.listenerList.add (SideBarListener.class, listener);
 	}
 	
 	public void removeSideBarListener (SideBarListener listener) {
 		this.listenerList.remove (SideBarListener.class, listener);
 	}
 	
 	private void fireSelectCurrentException () {
 		Object listeners[] = this.listenerList.getListenerList();
 		for (int i=0; i<listeners.length; i++) {
 			if (listeners [i] == SideBarListener.class) {
 				((SideBarListener)listeners [i+1]).markText(this.currentLeft, this.currentRight);
 			}
 		}
 	}
 	
 
 	@Override
 	public Dimension getPreferredSize () {
 		return new Dimension (this.errorIcon.getIconWidth(), getHeight ());
 	}
 	
 	@Override
 	protected void paintComponent (Graphics gc) {
 		
 		if (this.proppertyChanged) {
 			buildMarks ();
 		}
 		gc.setColor (getBackground ());
 		gc.fillRect (0, 0, getWidth (), getHeight ());
 		
 		if (this.verticalPositions == null) {
 			return;
 		}
 		
 		for (int i=0; i<this.verticalPositions.length; i++) {
 			if (this.verticalPositions[i] == -1) {
 				continue;
 			}
 			int y0 = this.verticalPositions[i] - this.errorIcon.getIconHeight()/2- this.vScrollBar.getValue();
 			int y1 = y0 + this.errorIcon.getIconHeight();
 			
 			if (y1 < 0 || y0 > getHeight ()) {
 				continue;
 			}
 			
 			gc.drawImage(this.errorIcon.getImage(), 0, y0, this);
 			
 		}
 		
 		
 		gc.fillRect(0, getHeight () - this.hScrollBar.getHeight(), getWidth (), this.hScrollBar.getHeight());
 	}
 	
 	
 	private void buildMarks () {
 		this.exceptions = this.document.getExceptions();
 		
 		this.verticalPositions = new int [this.exceptions.length];
 		for (int i=0; i<this.exceptions.length; i++) {
 			try {
 				this.verticalPositions[i] = -1;
 				
 				Rectangle rect = this.textComponent.modelToView(this.exceptions [i].getLeft());
 				if (rect == null) {
 					return;
 				}
 				this.verticalPositions[i] = rect.y + rect.height / 2;
 			} catch (Exception e) {
 				continue;
 			}
 			
 		}
 		this.proppertyChanged = false;
 	}
 	
 	private void mouseMoved (MouseEvent event) {
 		int y = event.getY() + this.vScrollBar.getValue ();
 		int hh = this.errorIcon.getIconHeight() / 2;
 		for (int i=0; i<this.verticalPositions.length; i++) {
 			if (y > this.verticalPositions[i] - hh &&
 					y <= this.verticalPositions[i] + hh) {
 				this.currentLeft	= this.exceptions[i].getLeft();
 				this.currentRight	= this.exceptions[i].getRight();
 				
 				setToolTipText(this.exceptions[i].getMessage());
 				setCursor(new Cursor (Cursor.HAND_CURSOR));
 				return;
 			}
 		}
 		setCursor(new Cursor (Cursor.DEFAULT_CURSOR));
 		setToolTipText (null);
 		this.currentLeft = -1;
 		this.currentRight = -1;
 		
 	}
 	
 	public void mouseSelected (MouseEvent event) {
 		if(this.currentLeft == -1 || this.currentRight == -1) {
 			return;
 		}
 		
 		fireSelectCurrentException();
 	}
 }
 
 
 
