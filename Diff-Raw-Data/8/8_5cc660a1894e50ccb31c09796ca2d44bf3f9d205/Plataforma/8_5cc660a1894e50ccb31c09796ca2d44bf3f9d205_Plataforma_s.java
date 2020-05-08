 package jogo;
 


 public class Plataforma {
 
 	private int x;
 	private int y;
 	private int largura;
	
 	public Plataforma(int x, int y, int largura) {
 		this.x = x;
 		this.y = y;
 		this.largura = largura;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public int getLargura() {
 		return largura;
 	}
 
 	public void setLargura(int largura) {
 		this.largura = largura;
 	}
	
 }
