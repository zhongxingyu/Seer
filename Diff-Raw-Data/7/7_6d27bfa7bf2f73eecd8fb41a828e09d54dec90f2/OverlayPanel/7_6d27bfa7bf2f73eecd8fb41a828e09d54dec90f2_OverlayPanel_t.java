 package manager.util;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 public class OverlayPanel extends JPanel{
 	
 	private static final Color BG_COLOR = new Color(0,0,0,80);
 	private static final Border PADDING = BorderFactory.createEmptyBorder(10, 10, 10, 10);
 	
 	public OverlayPanel() {
 		super();
 		setOpaque(false);
 		setBackground(BG_COLOR);
 		setBorder(PADDING);
 	}
 	
 	@Override
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		
 		g.setColor(getBackground());
         Rectangle r = g.getClipBounds();
         g.fillRect(r.x, r.y, r.width, r.height);
         super.paintComponent(g);
 	}
 	
	public void setPanelSize(Dimension d) {
 		setPreferredSize(d);
 		setMinimumSize(d);
 		setMaximumSize(d);
 	}
 	
	public void setPanelSize(int width, int height) {
		setPanelSize(new Dimension(width, height));
 	}
 }
