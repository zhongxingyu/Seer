 package jdigest;
 
 import java.awt.FontMetrics;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 
 import javax.swing.JLabel;
 
 public class JLeftClippingLabel extends JLabel
 {
 	private static final long serialVersionUID = 1L;
 
 	private String fullText;
 
 	public JLeftClippingLabel(String text)
 	{
 		super(text);
 		fullText = text;
 
 		this.addComponentListener(new ComponentAdapter()
 		{
 			public void componentResized(ComponentEvent e)
 			{
 				adjustText();
 			}
 		});
 	}
 
 	public void setText(String text)
 	{
 		fullText = text;
 		adjustText();
 	}
 
 	private void adjustText()
 	{
 		int availTextWidth = getSize().width
 			- getInsets().left - getInsets().right;
 		String text = fullText;
 
 		if(availTextWidth <= 0)
 			super.setText(text);
 		else
 		{
 			FontMetrics fm = getFontMetrics(getFont());
 			if(fm.stringWidth(text) <= availTextWidth)
 				super.setText(text);
 			else
 			{
 				String clipString = "...";
 				int width = fm.stringWidth(clipString);
 				int i = text.length();
				while(width <= availTextWidth && --i >= 0)
 					width += fm.charWidth(text.charAt(i));
 	
 				String newText = text.substring(i + 1);
 				super.setText("..." + newText);
 			}
 		}
 	}
 
 //	public static void main(String[] args)
 //	{
 //		try
 //		{
 //			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 //		}
 //		catch(Exception e)
 //		{
 //		}
 //
 //		JFrame w = new JFrame();
 //		w.setTitle("test");
 //		w.add(new JLeftClippingLabel("a very long text which will be clipped"));
 //		w.pack();
 //		w.setVisible(true);
 //		w.setLocationRelativeTo(null);
 //	}
 }
