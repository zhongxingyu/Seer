 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 
 public class Game extends JFrame implements MouseListener {
 
 	/**
 	 * @param args
 	 */
 	
 	Monster ghost;
 	Map map;
 	List path;
 	boolean walkAllowed;
 
 	
 	public Game()
 	{
 		super("CIn Walking Ghost!");
 		setSize(640, 480);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		loadAssets();
 		ghost.x = 80;
 		ghost.y = 80;
 		
 		this.addMouseListener(this);
 		
 		
 		walkAllowed = false;
 		
 		
 	}
 	
 	public void loadAssets()
 	{
 		try {
 			ghost = new Monster("ghost.png");
 			map = new Map("tileSet.txt", "Mapa.txt");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 	}
 	
 	
 	//Nao usado
 	public Graphics2D getContext(Graphics g)
 	{
 		/***********CONFIGURING PAINTING CONTEXT************************/
 		//Criamos um contexto grfico com a rea de pintura restrita
 		//ao interior da janela.
 		Graphics2D clip = (Graphics2D) g.create(getInsets().left, 
 				getInsets().top, 
 				getWidth() - getInsets().right, 
 				getHeight() - getInsets().bottom);
 
 		//Pintamos o fundo do frame de preto
 		clip.setColor(Color.BLACK);
 		clip.fill(clip.getClipBounds());
 
 		//Pintamos a snake
 		clip.setColor(Color.WHITE);
 		/***********CONFIGURING PAINTING CONTEXT************************/
 		
 		return clip;
 	}
 	
 	
 	
 	
 	public void paint(Graphics g)
 	{
 		
 		Graphics clip = g;
 		
 		
 		map.paint(clip);
 		
 		
 		if( walkAllowed && path != null && !path.isEmpty())
 		{
			ghost.x = path.head.b*map.tileW;
			ghost.y = path.head.a*map.tileH;
 			
 			path.removeFront();
 		}
 		else if (path != null && path.isEmpty())
 		{
 			walkAllowed = false;
 			map.reload();
 		}
 		
 		ghost.paint(clip);
 		this.repaint();
 		try {
 			Thread.sleep(200);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 		
 		
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		// TODO Auto-generated method stub
 		
 		System.out.println("Mouse clicked Point: " + event.getPoint());
 		
 		
 		path = ghost.computePath(map, event.getPoint());
 		
 		/*
 		Node it = path.head;
 		while( it != null )
 		{
 			map.setTile(it.x, it.y, 2);
 			
 			it = it.next;
 		}
 		
 		System.out.println("Path length: " + path.size);
 		*/
 		walkAllowed = true;
 		
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 		//System.out.println("Mouse entered");
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		//System.out.println("mouse exited");
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 		//System.out.println("mouse pressed");
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 		//System.out.println("mouse released");
 	}
 
 }
