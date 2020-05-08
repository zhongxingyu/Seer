 package ddddbb.gui;
 
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.HierarchyBoundsListener;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 import ddddbb.game.Level;
 import ddddbb.game.Scene4d;
 import ddddbb.game.Settings;
 import ddddbb.gen.AChangeListener;
 import ddddbb.gen.MyChangeListener;
 import ddddbb.math.D2Graphics;
 import ddddbb.math.D3Graphics;
 import ddddbb.math.D4Graphics;
 import ddddbb.math.Point4d;
 
 @SuppressWarnings("serial")
 public class ViewScreen extends JPanel implements MyChangeListener, ItemListener {
 
 	protected int x0,y0,width=0,height=0;
 	protected BufferedImage buffImg;
 	protected Color bColor = Color.BLACK;
 	protected Composite composite;
 	Graphics2D g;
 	D2Graphics g2;
 	D3Graphics g3;
 	D4Graphics g4;
 	
 	private final Level scene;
 	private final Settings ss;
 	private final Scene4d goalScene;
 	
 	public ViewScreen(
 			final Settings _ss,
 			final Level _scene,
 			final Scene4d _goalScene
 	) {
 		scene = _scene;
 		ss = _ss;
 		goalScene = _goalScene;
 		
 		scene.addChangeListener(this);
 		//ss.xdpcm.addChangeListener(this);
 		//ss.ydpcm.addChangeListener(this);
 		ss.drawTetrahedral.addChangeListener(this);
 		//Opt.drawTrihedral.addChangeListener(this);
 		
 		new AChangeListener() {
 			public void stateChanged() {
 				updateAlias();
 				repaint();
 			}}.addTo(ss.antiAliased);
 		new AChangeListener() {
 			public void stateChanged() {
 				if (g3==null) { return; }
 				g3.setBrightness(ss.brightness.getDouble());
 				repaint();
 			}}.addTo(ss.brightness);
 		new AChangeListener() {
 			public void stateChanged() {
 				if (g3==null) { return; }
 				g3 = ss.viewType.sel().getD3Graphics(g2,scene.camera3d);
 				g4.setGraphics(g3);
 				repaint();
 			}}.addTo(ss.viewType);
 
 //		setPreferredSize(new Dimension(
 //				(int)(10.0*Opt.xcm.getDouble()),
 //				(int)(10.0*Opt.ycm.getDouble()))); // 10x10cm
 
 		MouseControl mouseControl = new MouseControl(ss,scene);
 		addMouseMotionListener(mouseControl);
 		addMouseListener(mouseControl);
 		addHierarchyBoundsListener(new HierarchyBoundsListener() {
 			public void ancestorMoved(HierarchyEvent e) {
 			}
 
 			public void ancestorResized(HierarchyEvent e) {
 				updateGraphics();
 			}
 			
 		});
 		
 //		addFocusListener(new FocusListener() {
 //			public void focusGained(FocusEvent e) {
 //				System.out.println("ViewScreen gained focus");
 //			}
 //
 //			public void focusLost(FocusEvent e) {
 //				System.out.println("ViewScreen lost focus");
 //			}});
 	}
 	
 	public synchronized void updateGraphics() {
 		Dimension size = getSize();  
 		width = size.width;
 		height = size.height;
 		if ( width > 0 & height > 0 ) {
 			x0 = width/2;
 			y0 = height/2;
 //			buffImg = (BufferedImage)createImage(width,height);
 			buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 			g = buffImg.createGraphics();
 			g.setBackground(bColor);
 			g.setTransform(new AffineTransform());
 			g.translate(x0,y0);
 			if ( g2 == null ) {
 				g2 = new D2Graphics(g,ss.xdpcm.getDouble(),ss.ydpcm.getDouble());
 				g3 = ss.viewType.sel().getD3Graphics(g2,scene.camera3d);
 				g4 = new D4Graphics(g3,ss.perspective);
 			}
 			else {
 				g2.setGraphics(g);
 				g3.setGraphics(g2); 
 				g4.setGraphics(g3);
 			}
 			updateAlias();
 			g3.setBrightness(ss.brightness.getDouble());
 		}		
 	}
 
 	protected void updateAlias() {
 		if (g==null) { return; }
 		if (ss.antiAliased.isSelected()) {
 			g.setRenderingHint(
 					RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON
 			);
 		}
 		else {
 			g.setRenderingHint(
 					RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_OFF
 			);
 		}
 	}
 	
 	
 	/** double buffered paint */
 	public void paint(Graphics g0) {
 		if ( g == null ) { updateGraphics(); }
 		g.clearRect(-width/2,-height/2,width,height);
 		
 		if (ss.drawTetrahedral.isSelected()) { 
 			g4.drawTetrahedral();
 		}
 //		if (Opt.drawTrihedral.isSelected()) { 
 //			g3.drawTrihedral();
 //		}
 		
 		if (ss.showGoal.isSelected()) {
 			goalScene.paint(g3);
 		}
 		else {
 			scene.paint(g3);
		}
		for (int i=0;i<scene.compounds.size();i++) {
			Point4d p = (Point4d)scene.compounds.get(i).center();
			if (i==scene.compounds.selInt()) {
				g4.drawMark(p,0.1);				
			}
			else {
				g4.drawString(i+1+"", p);				
 			}
 		}
 
 		((Graphics2D)g0).drawImage(buffImg, null, 0, 0);
 		super.paintChildren(g0);
 //		super.p
 	}
 	
 //	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
 //		if (pageIndex > 0) { return Printable.NO_SUCH_PAGE; }
 //		Color backGround = g.getBackground();
 //		Color lColor = g3.lColor;
 //		Color rColor = g3.rColor;
 //		g.setBackground(Color.WHITE);
 //		if ( g3 instanceof BarEyeGraphics ) {
 //			g3.lColor = Color.BLACK;
 //			g3.rColor = Color.BLACK;
 //		}
 //		paint(graphics);
 //		g3.lColor = lColor;
 //		g3.rColor = rColor;
 //		g.setBackground(backGround);
 //		return Printable.PAGE_EXISTS;
 //	}
 
 	public void stateChanged() {
 		repaint();
 	}
 	public void itemStateChanged(ItemEvent e) {
 		repaint();
 	}
 	
 }
