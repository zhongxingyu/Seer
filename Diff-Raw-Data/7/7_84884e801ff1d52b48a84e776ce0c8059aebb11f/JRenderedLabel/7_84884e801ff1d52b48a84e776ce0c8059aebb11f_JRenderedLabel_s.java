 /*
  * Created on Jun 14, 2004
  */
 package zz.utils.ui;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 
 /**
  * A label that renders a value with a {@link javax.swing.ListCellRenderer}.
  * The value is set with {@link #setValue(Object)}. The renderer is set
  * with {@link #setRenderer(ListCellRenderer)}
  * @author gpothier
  */
 public class JRenderedLabel extends JPanel
 {
 	private static final Dimension EMPTY = new Dimension();
 	
 	/**
 	 * We need this list for the renderer.
 	 */
 	public static final JList LIST = new JList();
 	
 	private ListCellRenderer itsRenderer;
 	private Object itsValue;
 	
 	public JRenderedLabel ()
 	{
 		this (null);
 	}
 	
 	public JRenderedLabel (Object aValue)
 	{
 		this (null, aValue);
 	}
 	
 	public JRenderedLabel (ListCellRenderer aRenderer)
 	{
 		this (aRenderer, null);
 	}
 	
 	public JRenderedLabel (ListCellRenderer aRenderer, Object aValue)
 	{
 		setValue(aValue);
 		setRenderer(aRenderer);
 		setOpaque(false);
 	}
 
 	public Object getValue()
 	{
 		return itsValue;
 	}
 	
 	/**
 	 * Sets the value this label should render.
 	 */
 	public void setValue(Object aValue)
 	{
 		itsValue = aValue;
 		revalidate();
 		repaint();
 	}
 	
 	public void setRenderer (ListCellRenderer aRenderer)
 	{
 		itsRenderer = aRenderer;
 		revalidate();
 		repaint();
 	}
 	
 	public ListCellRenderer getRenderer()
 	{
 		return itsRenderer;
 	}
 	
 	protected void paintComponent(Graphics aG)
 	{
 		if (itsRenderer != null)
 		{
 			JComponent theComponent = (JComponent) itsRenderer.getListCellRendererComponent(
 					LIST, 
 					getValue(), 
 					0, 
 					false, 
 					false);
 			
 			theComponent.setOpaque(isOpaque());
 			theComponent.setBounds(0, 0, getWidth(), getHeight());
 			theComponent.paint(aG);
 		}
 	}
 	
 	public Dimension getPreferredSize()
 	{
 		if (isPreferredSizeSet()) return super.getPreferredSize();
 		if (itsRenderer != null)
 		{
 			Component theComponent = itsRenderer.getListCellRendererComponent(LIST, getValue(), 0, false, false);
 			return theComponent.getPreferredSize();
 		}
 		else return EMPTY;
 	}
 }
