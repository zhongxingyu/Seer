 package net.bubbaland.trivia.client;
 
 
 import java.awt.LayoutManager;

 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 
 /**
  * Super-class for most of the dialog boxes in the trivia GUI.
  * 
  * Implements an AncestorListener that will focus on the element when created. 
  * 
  */
 public class TriviaDialog extends JPanel implements AncestorListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long	serialVersionUID	= -4127179718225373888L;
 
 	public TriviaDialog() {
 		super();
 	}
 
 	public TriviaDialog(LayoutManager layout) {
 		super(layout);
 	}
 
 	public TriviaDialog(boolean isDoubleBuffered) {
 		super(isDoubleBuffered);
 	}
 
 	public TriviaDialog(LayoutManager layout, boolean isDoubleBuffered) {
 		super(layout, isDoubleBuffered);
 	}
 
 	@Override
 	public void ancestorAdded(final AncestorEvent event) {
 		// Change the focus to the text area when created
 		final AncestorListener al= this;
         SwingUtilities.invokeLater(new Runnable(){
 
             @Override
             public void run()
             {
                 JComponent component = (JComponent)event.getComponent();
                 component.requestFocusInWindow();
                 component.removeAncestorListener( al );
             }
         });
 	}
 
 	@Override
 	public void ancestorMoved(AncestorEvent event) {
 	}
 
 	@Override
 	public void ancestorRemoved(AncestorEvent event) {
 	}
 
 }
