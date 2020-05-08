 package compositor;
 
 /*TODO :
 *  - verifier maj de l'affichage (possible problème)
 *  
 *  - redimentionnement coince un peu
  *  - faire un mode modif (genre surbrillance)
  *  -ajout texte aux fenetres
  *  
  *  gérer des racourcis clavier
  *  
  *  exporter methodes fermetures, maxim, etc
  *  
  *  faire que la gestion des collisions internes a la fenetres soient gérées directemetn dans window.
  */
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 import application.BouncingPoint;
 import application.MovingShapes;
 import application.SmoothChange;
 import window.Window;
 
 
 public class Compositor extends JFrame implements MouseListener, MouseMotionListener, KeyListener {
 
 	private static final long serialVersionUID = 1493982699619655700L;
 	private static Compositor instance = null;
 	
 	
 	//variables pour les event
 	private Integer mouseClickX=0, mouseClickY=0;
 	private Character mode=' ', keyPressed=' ';
 	
 	//applications
 	private ArrayList<Window> windows = new ArrayList<Window>();
 	private ArrayList<Window> icons = new ArrayList<Window>();
 	
 	//constantes icones
 	private Integer origIconX = 50, origIconY=40, padding=10;
 	private Integer iMax;
 	
 	// Fin variables ---------------------------------------------------------------------
 	
 	public static Compositor getInstance() {
 		if (instance == null)
 			instance = new Compositor("Compositor");
 		
 		return instance;
 	}
 	
 	private Compositor(String name) {
 		super(name);
 		Compositor.instance = this;
 		setSize((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
 				(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()-30);
 		//pour prendre a peu pres la taille de l'ecran
 		setLocation(0,0);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setUndecorated(true); // ne pas afficher le bandeau et les bords
 		setVisible(true);
 		createBufferStrategy(2);
 		
 		iMax = (getHeight()-origIconY)/(padding+Window.iconSize); 
 		// le nombre max d'icones sur une hauteur de fenetre
 		
 		for(int i=0;i<10;i++) {
 			double rand = Math.random();
 			if(rand<0.33)
 				windows.add(new Window(new MovingShapes(), 110*(1+(i%5)), 110*(1+(i/5))));
 			else if(rand>0.66)
 				windows.add(new Window(new BouncingPoint(), 110*(1+(i%5)), 110*(1+(i/5))));
 			else
 				windows.add(new Window(new SmoothChange(), 110*(1+(i%5)), 110*(1+(i/5))));
 		} // on ajoute 10 applications
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addKeyListener(this);
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		Graphics2D context = (Graphics2D)g;
 		Area drawable = new Area(new Rectangle(0,0,getWidth(),getHeight()));
 
 		for(int i=windows.size()-1;i>=0;i--) { // puis dessin des fenetres
 			context.setClip(drawable);
 			drawable.subtract(new Area(windows.get(i).draw(context)));
 		}
 		for(int i=0;i<icons.size();i++) {
 			if(icons.get(i)!=null)
 			{
 				context.setClip(drawable);
 				drawable.subtract(new Area(icons.get(i).drawIcon(context, 
 						origIconX+(i/iMax)*(padding+Window.iconSize), origIconY+(i%iMax)*
 						(padding+Window.iconSize))));
 			}
 		}
 		context.setClip(drawable);
 		context.setColor(Color.white);
 		context.fillRect(0, 0, getWidth(), getHeight());
 	}
 	
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		
 		for(int i=windows.size()-1;i>=0;i--) { // parcours de la fin au debut (pour prendre les fenetres par ordre d'apparition
 			Window w = windows.get(i);
 			
 			if(collision(e.getX(), e.getY(), w.getPosiX(), w.getPosiY(), w.getWidth(), w.getHeight())) {
 			//premier test pour voir si collision avec la fenetre (optimisation + evite des erreurs si les boutons sortent de la fenetre)
 				
 				//fermeture
 				if(collision(e.getX(), e.getY(), w.getPosiXClose(), w.getPosiY()+Window.margin, 
 						Window.sizeButton, Window.sizeButton)) {
 					windows.remove(w);
 					repaint(w.getPosiX(),w.getPosiY(), w.getWidth(), w.getHeight());
 					w = null;
 					return;
 				}
 				
 				//Maximize
 				if(collision(e.getX(), e.getY(), w.getPosiXMaximize(), w.getPosiY()+Window.margin, 
 						Window.sizeButton, Window.sizeButton)) {
 					if(w.isMaximised()) {
 						w.restore();
 						w.setMaximised(false);
 						w.maj();
 					}
 					else {
 						w.save();
 						w.setMaximised(true);
 						w.setPosiX(0); w.setPosiY(0);
 						w.setWidth(getWidth()); w.setHeight(getHeight());
 						
 					}
 					windows.remove(w);
 					windows.add(w);
 					repaint();
 					return;
 				}
 				
 				//iconify
 				if(collision(e.getX(), e.getY(), w.getPosiXIconify(), w.getPosiY()+Window.margin, 
 						Window.sizeButton, Window.sizeButton)) {
 					windows.remove(w);
 					if(icons.indexOf(null)!=-1)
 						icons.set(icons.indexOf(null), w); // si on trouve une place libre
 					else
 						icons.add(w);
 					
 					repaint();
 					return;
 				}
 				
 				if(collision(e.getX(), e.getY(), w.getPosiX(), w.getPosiY(), w.getWidth(), 
 						w.getHeight())) {
 					e.translatePoint(-w.getPosiX(), -w.getPosiY());
 					w.mouseClicked(e);
 				}
 			}
 		}
 		
 		//desiconification
 		for(int i=0;i<icons.size();i++) {
 			if(collision(e.getX(), e.getY(), origIconX+(i/iMax)*(padding+Window.iconSize), 
 					origIconY+(i%iMax)*(padding+Window.iconSize), Window.iconSize, Window.iconSize)) {
 				windows.add(icons.get(i));
 				icons.set(i, null);
 				repaint();
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		mouseClickX = e.getX(); mouseClickY = e.getY();
 		
 		if(keyPressed=='b') { // nouvelle fenetre
 			mode='n';
 			windows.add(new Window(new BouncingPoint(), mouseClickX, mouseClickY));
 			return;
 		}
 		if(keyPressed=='m') { // nouvelle fenetre
 			mode='n';
 			windows.add(new Window(new MovingShapes(), mouseClickX, mouseClickY));
 			return;
 		}
 		if(keyPressed=='s') { // nouvelle fenetre
 			mode='n';
 			windows.add(new Window(new SmoothChange(), mouseClickX, mouseClickY));
 			return;
 		}
 			
 		for(int i=windows.size()-1;i>=0;i--) {
 			Window w = windows.get(i);
 			
 			if(collision(e.getX(), e.getY(), w.getPosiX(), w.getPosiY(), w.getWidth(), w.getHeight())) {
 				
 				//collision avec le bandeau
 				if(collision(e.getX(), e.getY(), w.getPosiX()+Window.margin, w.getPosiY()+Window.margin, 
 						w.getWidth()-3*Window.marginTop+Window.margin, Window.marginTop-Window.margin)) {
 					mode = 'd';
 					setCursor(new Cursor(Cursor.MOVE_CURSOR));
 				}
 				else {
 					//collision avec le margin d'en bas
 					if(collision(e.getX(), e.getY(), w.getPosiX(), w.getPosiY()+w.getHeight()-Window.margin, w.getWidth(), Window.margin)) {
 						mode = 'h';
 						setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
 					}
 					
 					//collision avec le margin à droite
 					if(collision(e.getX(), e.getY(), w.getPosiX()+w.getWidth()-Window.margin, w.getPosiY(), Window.margin, w.getHeight())) {
 						if(mode=='h') {
 							mode='a';
 							setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
 						}
 						else {
 							mode='w';
 							setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
 						}
 					}
 				}
 				
 				windows.remove(w);
 				windows.add(w);
 				
 				// peut etre remplacé par if mode == ' '
 				if(collision(e.getX(), e.getY(), w.getPosiX()+Window.margin, w.getPosiY()+Window.marginTop, 
 						w.getWidth()-2*Window.margin, w.getHeight()-Window.marginTop-Window.margin)) {
 					e.translatePoint(-w.getPosiX(), -w.getPosiY());
 					w.mousePressed(e);
 				}
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		mode = ' ';
 		mouseClickX = mouseClickY = 0;
 		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 		
 		if(windows.size()>0) {
 			if(collision(e.getX(), e.getY(), windows.get(windows.size()-1).getPosiX(), 
 					windows.get(windows.size()-1).getPosiY(), windows.get(windows.size()-1).getWidth(), 
 					windows.get(windows.size()-1).getHeight())) {
 				e.translatePoint(-windows.get(windows.size()-1).getPosiX(), -windows.get(windows.size()-1).getPosiY());
 				windows.get(windows.size()-1).mouseReleased(e);
 			}
 		}
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if(windows.size()>0) {
 			Window w = windows.get(windows.size()-1);
 	
 			switch(mode) {
 				case 'd':
 					w.translate(e.getX() - mouseClickX, e.getY() - mouseClickY);
 					break;
 				case 'h':
 					w.setHeight(e.getY() - w.getPosiY());
 					break;
 				case 'w':
 					w.setWidth(e.getX() - w.getPosiX());
 					break;
 				case 'a': case 'n':
 					w.setHeight(e.getY() - w.getPosiY());
 					w.setWidth(e.getX() - w.getPosiX());
 					break;
 			}
 			
 			if(mode != ' ') {
 				if(mode!='d')
 					repaint(w.getPosiX(), w.getPosiY(), mouseClickX+Window.margin, mouseClickY+Window.margin);
 				else
 					repaint();
 				
 				w.setMaximised(false);
 				mouseClickX=e.getX();
 				mouseClickY=e.getY();
 			}
 			
 			if(collision(e.getX(), e.getY(), w.getPosiX(), w.getPosiY(), w.getWidth(), w.getHeight())) {
 				e.translatePoint(-w.getPosiX(), -w.getPosiY());
 				w.mouseDragged(e);
 			}
 		}
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		for(Window w:windows) {
 			if(collision(e.getX(), e.getY(), w.getPosiX(), w.getPosiY(), w.getWidth(), w.getHeight())) {
 				/*if(collision(e.getX(), e.getY(), w.getPosiX()+w.getWidth()-Window.margin, w.getPosiY(), Window.margin, w.getHeight())) {
 					setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
 				}
 				else
 					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 				*/
 				e.translatePoint(-w.getPosiX(), -w.getPosiY());
 				w.mouseMoved(e);
 			}
 			/*else
 				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));*/
 		}
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(keyPressed == ' ')
 			keyPressed=e.getKeyChar();
 		
 		if(windows.size()>0)
 			windows.get(windows.size()-1).keyPressed(e);
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if(e.getKeyChar()==keyPressed)
 			keyPressed=' ';
 		
 		if(windows.size()>0)
 			windows.get(windows.size()-1).keyReleased(e);
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e) {
 		if(windows.size()>0)
 			windows.get(windows.size()-1).keyTyped(e);
 	}
 	
 	//override but useless
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 	
 	public static final boolean collision(int mouseX, int mouseY, int x, int y, int w, int h) {
     	
     	if(mouseX < x)
     		return false;
     	if(mouseY < y)
     		return false;
 		if(mouseX > x + w)
         	return false;
     	if(mouseY > y + h)
         	return false;
     		
     	return true;
     }
 }
