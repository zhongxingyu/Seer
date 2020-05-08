 package jogo;
 
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
public class Maconha extends Pessoa{
	
 	private Game game;
 	private BufferedImage maconha;
		
	Maconha(int x, int y, int altura, int largura, Game game){
		
		super(x, y, altura, largura,false,false,false);
		this.game=game;
		
 		SpriteSheet ss = new SpriteSheet(game.getImageMaconha());
		
 		maconha = ss.grabImage(1, 1, 28, 28);
 	}
	
	public Rectangle getBounds(){
		return new Rectangle((int)x,(int)y,largura,altura);
 	}
	
	public void render(Graphics g){
		g.drawImage(maconha, (int)x, (int)y, null);
 	}
 }
