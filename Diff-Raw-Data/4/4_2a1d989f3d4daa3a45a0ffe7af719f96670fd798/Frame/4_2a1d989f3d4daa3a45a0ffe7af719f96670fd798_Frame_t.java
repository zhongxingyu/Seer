 package prototype;
 
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 //import javax.swing.JLabel;
 
 public class Frame extends JFrame{
 	private static final long serialVersionUID = 1L;	//ntig, damit kein Warning angezeigt wird
 	//DEKLARATION
 	private BufferStrategy buff;
 	final Player player;
 	private Map map;
 	
 	
 	public Frame(String name,Player player, Map map){
 		super(name);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 		setResizable(false);
 //		screen = new Screen();
 //		screen.setBounds(0, 0, worldsizex, worldsizey);
 //		add(screen);
 		
 		
 		
 		
 		addKeyListener(new Keyboard());
 		this.player = player;
 		this.map=map;
 	}
 	
 	
 	//METHODEN
 	public void makeBuff(){		//kann das auch in den konstruktor...test spter
 		createBufferStrategy(2);
 		buff= getBufferStrategy();
 	}
 	
 	public void setSizeRight(int x,int y){		//kann nicht im Konstruktor gemacht werden, wegen falscher Insets
 		setSize(x+getInsets().left+getInsets().right, y+getInsets().top+getInsets().bottom);	//Gre + Randeinrckungen, damit der Sichtbare bereich genau die eingegebene Gre hat
 	}
 	
 	
 	
 	public void nextFrame(){
 		Graphics g=buff.getDrawGraphics();//bergibt ein malobjekt aus der bufferstrat
 			
 		for(int x = 0; x < 32 ; x++){
 			for(int y = 0 ; y< 18 ; y++){
				map.getTile(x, y);
				g.drawImage(Tile.getLook(map.getTile(x, y).getTex()), map.getTile(x, y).getBounding().x+getInsets().left, map.getTile(x, y).getBounding().y+getInsets().top, null);
 			}
 		}
 		
 		g.drawImage(player.getBimg(), player.getBounding().x+getInsets().left, player.getBounding().y+getInsets().top, null);
 		
 		g.dispose();	//gibt den zeichner wieder frei
 		buff.show();	//zeigt dann den aktuellen buffer
 	}
 }
