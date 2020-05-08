 package arthur2d;
 
 
 import java.awt.AlphaComposite;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
public class Engine extends Canvas implements MouseListener, MouseMotionListener{
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private LinkedList<Drawable> drawables;
 	private LinkedList<Drawable> constants;
 	private LinkedList<ShadowCaster> shadows;
 	private LinkedList<Light> lights;
 	private BufferedImage[] layer;
 	private BufferedImage buffImg;
 	private int width, height;
 	private int resX, resY;
 	public int mousex, mousey;
 	private int camX, camY, camSX, camSY;
 	final EngineHost eng;
 	public Engine(EngineHost e, int w, int h, int rx, int ry, int layers) {
 		drawables = new LinkedList<Drawable>();
 		constants = new LinkedList<Drawable>();
 		shadows = new LinkedList<ShadowCaster>();
 		lights = new LinkedList<Light>();
 		width = w;
 
 		height = h;
 		resX = rx;
 		resY = ry;
 		camSX = rx;
 		camSY = ry;
 		layer = new BufferedImage[layers];
 		for (int i = 0; i < layers; i++) {
 			layer[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		}
 		buffImg = new BufferedImage(rx, ry, BufferedImage.TYPE_INT_ARGB);
 		eng = e;
 		new Thread() {
 			public void run() {
 				while(true) {
 					eng.doStuff();
 					repaint();
 					try {
 						sleep(10);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}.start();
 	}
 	public void update(Graphics g) {
 		paint(g);
 	}

 	public void paint(Graphics g) {
 		Graphics2D off = (Graphics2D) buffImg.getGraphics();
 		off.setColor(Color.white);
 		off.fillRect(0, 0, resX, resY);
 		for (int i = 0; i < layer.length; i++) {
 			Graphics2D t = (Graphics2D) layer[i].getGraphics();
 			t.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
 			t.setColor(new Color(0, 0, 0, 0));
 			t.fillRect(0, 0, width, height);
 		}
 		ListIterator<Drawable> drawIt = drawables.listIterator();
 		Drawable d;
 		while (drawIt.hasNext()) {
 			d = drawIt.next();
 			d.draw(layer);
 		}
 
 		for (int i = 0; i < layer.length; i++) {
 			off.drawImage(layer[i], 0, 0, resX, resY, camX, camY, camX + camSX, camY + camSY, this);
 		}
 		drawIt = constants.listIterator();
 		while (drawIt.hasNext()) {
 			d = drawIt.next();
 			d.draw(layer);
 		}
 

		renderLights(off);
 
 		g.drawImage(buffImg, 0, 0, resX, resY, this);
 	}
 	public void renderLights(Graphics2D g2) {
 		Area shadow = null;
 		BufferedImage shadowLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = (Graphics2D)shadowLayer.getGraphics();
 		ListIterator<Light> lightIt = lights.listIterator();
 		while (lightIt.hasNext()) {
 			Light l = lightIt.next();
 			Area shade = new Area();
 
 			ShadowCaster s;
 			ListIterator<ShadowCaster> shadowIt = shadows.listIterator();
 			while (shadowIt.hasNext()) {
 				s = shadowIt.next();
 				shade.add(renderShadow(l, s, g));
 
 			}
 			if (shadow == null) {
 				shadow = shade;
 			} else {
 				shadow.intersect(shade);
 			}
 				
 //			g.setPaint(l.getGradient());
 //			g.fill(new Rectangle(0, 0, 1000, 1000));
 			
 

 		}
 		g2.drawImage(shadowLayer, 0, 0, this);
		g2.setColor(Color.black);
		g2.fill(shadow);
 
 
 	}
 	
 	public Area renderShadow(Light l, ShadowCaster s, Graphics t) {
 		Area area = new Area();
 		Polygon shadow = new Polygon();
 		t.setColor(Color.black);
 		shadow.addPoint(s.xPos,  s.yPos);
 		shadow.addPoint(s.xPos + l.range * (s.xPos - l.xPos), s.yPos + l.range * (s.yPos - l.yPos));
 		shadow.addPoint(s.xPos + s.width + l.range * ((s.xPos + s.width) - l.xPos), s.yPos + l.range * (s.yPos - l.yPos));
 		shadow.addPoint(s.xPos + s.width, s.yPos);
 		area.add(new Area(shadow));
 		shadow = new Polygon();
 		shadow.addPoint(s.xPos,  s.yPos);
 		shadow.addPoint(s.xPos + l.range * (s.xPos - l.xPos), s.yPos + l.range * (s.yPos - l.yPos));
 		shadow.addPoint(s.xPos + l.range * ((s.xPos + s.width) - l.xPos), s.yPos + s.height + l.range * (s.yPos + s.height- l.yPos));
 		shadow.addPoint(s.xPos , s.yPos + s.height);
 		//		g.setColor(Color.red);
 		area.add(new Area(shadow));
 
 		shadow = new Polygon();
 		shadow.addPoint(s.xPos + s.width,  s.yPos + s.height);
 		shadow.addPoint(s.xPos + s.width + (l.range * (s.xPos + s.width - l.xPos)), s.yPos + s.height + l.range * (s.yPos + s.height - l.yPos));
 		shadow.addPoint(s.xPos + l.range * ((s.xPos) - l.xPos), s.yPos + s.height + l.range * (s.yPos + s.height- l.yPos));
 
 		shadow.addPoint(s.xPos , s.yPos + s.height);
 		//		g.setColor(Color.green);
 		area.add(new Area(shadow));
 
 
 		shadow = new Polygon();
 		shadow.addPoint(s.xPos + s.width, s.yPos + s.height);
 		shadow.addPoint(s.xPos + s.width + l.range * (s.xPos + s.width - l.xPos), s.yPos + s.height + l.range * (s.yPos + s.height- l.yPos));
 		shadow.addPoint(s.xPos + s.width + l.range * (s.xPos + s.width - l.xPos), s.yPos +  l.range * (s.yPos - l.yPos));
 
 		shadow.addPoint(s.xPos + s.width, s.yPos);
 		area.add(new Area(shadow));
 //		((Graphics2D) t).fill(area);
 		return area;
 	}
 
 	public void rendersShadow(Light l, ShadowCaster s, Graphics t) {
 		BufferedImage shadowImg = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = (Graphics2D)shadowImg.getGraphics();
 		g.setColor(Color.black);
 		g.drawOval(l.xPos, l.yPos, 10, 10);
 		g.fillRect(s.xPos, s.yPos, s.width, s.height);
 		//g.drawLine(50, 50, 50 + (s.xPos - 50) * l.range, 50 + (s.yPos + s.height - 50) * l.range);
 		Polygon shadow = new Polygon();
 		shadow.addPoint(s.xPos,  s.yPos);
 		shadow.addPoint(s.xPos + l.range * (s.xPos - l.xPos), s.yPos + l.range * (s.yPos - l.yPos));
 		shadow.addPoint(s.xPos + s.width + l.range * ((s.xPos + s.width) - l.xPos), s.yPos + l.range * (s.yPos - l.yPos));
 		shadow.addPoint(s.xPos + s.width, s.yPos);
 		g.fillPolygon(shadow);
 
 		shadow = new Polygon();
 		shadow.addPoint(s.xPos,  s.yPos);
 		shadow.addPoint(s.xPos + l.range * (s.xPos - l.xPos), s.yPos + l.range * (s.yPos - l.yPos));
 		shadow.addPoint(s.xPos + l.range * ((s.xPos + s.width) - l.xPos), s.yPos + s.height + l.range * (s.yPos + s.height- l.yPos));
 		shadow.addPoint(s.xPos , s.yPos + s.height);
 		//		g.setColor(Color.red);
 		g.fillPolygon(shadow);
 
 		shadow = new Polygon();
 		shadow.addPoint(s.xPos + s.width,  s.yPos + s.height);
 		shadow.addPoint(s.xPos + s.width + (l.range * (s.xPos + s.width - l.xPos)), s.yPos + s.height + l.range * (s.yPos + s.height - l.yPos));
 		shadow.addPoint(s.xPos + l.range * ((s.xPos) - l.xPos), s.yPos + s.height + l.range * (s.yPos + s.height- l.yPos));
 
 		shadow.addPoint(s.xPos , s.yPos + s.height);
 		//		g.setColor(Color.green);
 		g.fillPolygon(shadow);
 
 
 		shadow = new Polygon();
 		shadow.addPoint(s.xPos + s.width, s.yPos + s.height);
 		shadow.addPoint(s.xPos + s.width + l.range * (s.xPos + s.width - l.xPos), s.yPos + s.height + l.range * (s.yPos + s.height- l.yPos));
 		shadow.addPoint(s.xPos + s.width + l.range * (s.xPos + s.width - l.xPos), s.yPos +  l.range * (s.yPos - l.yPos));
 
 		shadow.addPoint(s.xPos + s.width, s.yPos);
 		//		g.setColor(Color.blue);
 		g.fillPolygon(shadow);
 		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC).derive(0.55f));
 		t.drawImage(shadowImg, 0, 0, this);
 
 	}
 	public void setCamera(int x, int y, int sx, int sy) {
 		camX = x;
 		camY = y;
 		camSX = sx;
 		camSY = sy;
 	}
 	public void flush() {
 		flushDrawables();
 		flushConstants();
 		flushLights();
 		flushShadows();
 	}
 	public void flushShadows() {
 		// TODO Auto-generated method stub
 		shadows.clear();
 	}
 	public void flushLights() {
 		// TODO Auto-generated method stub
 		lights.clear();
 	}
 	public void flushConstants() {
 		// TODO Auto-generated method stub
 		constants.clear();
 	}
 	public void flushDrawables() {
 		// TODO Auto-generated method stub
 		drawables.clear();
 	}
 	public void addDrawable(Drawable d) {
 		drawables.add(d);
 	}
 	public void addShadow(ShadowCaster s) {
 		shadows.add(s);
 	}
 	public void addLight(Light l) {
 		lights.add(l);
 	}
 	public void addConstant(Drawable d) {
 		constants.add(d);
 	}
 
 	public BufferedImage getScene() {
 		return buffImg;
 	}
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("bugger");
 	}
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
