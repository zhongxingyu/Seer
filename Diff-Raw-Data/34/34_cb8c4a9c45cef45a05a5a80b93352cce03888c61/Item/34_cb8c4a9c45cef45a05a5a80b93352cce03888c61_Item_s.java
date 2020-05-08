 package jogo;
 
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
public class Item extends Renderizavel{
	
 	private Game game;
 	private BufferedImage item;
		
	Item(int x, int y, int altura, int largura, Game game){
		
		super(x, y, altura, largura,false,false,false);
		this.game=game;
		
 		SpriteSheet ss = new SpriteSheet(game.getImageitem());
		
 		item = ss.grabImage(1, 1, 38, 42);
 	}
	
	public Rectangle getBounds(){
		return new Rectangle((int)x,(int)y,largura,altura);
 	}
	
	public void render(Graphics g){
		g.drawImage(item, (int)x, (int)y, null);
 	}
 }
