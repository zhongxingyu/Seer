 package gui;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import domain.MapPart;
 import domain.Player;
 
 
 public class MapView extends JComponent{
 	MapPart mapPart=null;
 	float zoom=1;
 	int centerX;
 	int centerY;
 	private int leftCornerX;
 	private int leftCornerY;
 	Player player1;
 	private StatusView statusView;
 	public MapView(MapPart mappart,StatusView statusView, Player player1) {
 		this.mapPart=mappart;
 		repaint();
 		this.player1=player1;
 		this.statusView=statusView;
 		setVisible(true);
 		// TODO Auto-generated constructor stub
 	}
 	public void setMap(MapPart mapPart) {
 		this.mapPart=mapPart;
 		repaint();
 		setVisible(true);
 	}
 	public void paint(Graphics g) {
 		for(int x=0;x<mapPart.getHigth();x++){
 			for(int y=0;y<mapPart.getWith();y++){
 				if(mapPart.getPoint(x, y, 0)!=null)
 					mapPart.getPoint(x, y, 0).drawItSelf(g,this,x*(int)(64*zoom)-leftCornerX,y*(int)(64*zoom)-leftCornerY,zoom);
 				if(mapPart.getPoint(x, y, 1)!=null)
 					mapPart.getPoint(x, y, 1).drawItSelf(g,this,x*(int)(64*zoom)-leftCornerX,y*(int)(64*zoom)-leftCornerY,zoom);
 				if(mapPart.getPoint(x, y, 2)!=null)
 					mapPart.getPoint(x, y, 2).drawItSelf(g,this,x*(int)(64*zoom)-leftCornerX,y*(int)(64*zoom)-leftCornerY,zoom);
 			}
 		}
 		player1.drawItSelf(g,this,(int)getWidth()/2,(int)getHeight()/2,zoom);
 		statusView.drawItSelf(g,this,(int)getWidth()-400,(int)getHeight()-200,zoom);
 	}
 	public void moveTo(int x,int y,float zoom){//move the center of the camera to this point
 		
 		this.zoom=zoom;
 		this.centerX=x;
 		this.centerY=y;
		this.leftCornerX=(int)(x*zoom)-(int)((getHeight()/2));//denna stämmer inte riktigt med zoom
		this.leftCornerY=(int)(y*zoom)-(int)((getWidth()/2));//denna stämmer inte riktigt med zoom
 		repaint();
 	}
 }
