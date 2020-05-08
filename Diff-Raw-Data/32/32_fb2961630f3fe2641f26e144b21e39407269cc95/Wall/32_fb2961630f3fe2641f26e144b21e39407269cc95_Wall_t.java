 package gui;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JPanel;
 
 import dataContainers.MediaFile;
 import dataContainers.MediaLibrary;
 
 
 public class Wall extends JPanel{
 
 	private static final long serialVersionUID = -5495354692927105826L;
 	MediaLibrary currentLib = new MediaLibrary();
 	
 	private boolean m_moving = false;
 	private int m_x = 0;
 	private int m_y = 0;
 	private int m_lx = 0;
 	private int m_ly = 0;
 
 	Wall(){
 		setBackground(Color.BLUE);
 		
 		for(int i = 0; i < 10; i++){
 			MediaFile next = new MediaFile("library/doc2.txt", "graphics/thumb1.bmp");
 			currentLib.add(next);
 		}
 		
 		addMouseListener(new MouseListener(){
 			
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				m_moving = true;
				m_lx = arg0.getX();
				m_ly = arg0.getY();
 			}
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				m_moving = false;
 			}
 		});
 		
 		addMouseMotionListener(new MouseMotionListener(){
 
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 				if(m_moving){
 					m_x += arg0.getX() - m_lx;
 					m_y += arg0.getY() - m_ly;
					m_lx = arg0.getX();
					m_ly = arg0.getY();
		//			m_lx = m_x;
		//			m_ly = m_y;
 					repaint();
 				}
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent arg0) {
 			}
 		});
 	}
 	
 	public void paint(Graphics g){
 		super.paint(g);
 		
 		for(int i = 0; i < currentLib.size(); i++){
 			int x = i * 150;
 			int y = 0;
 			
 			while(x >= 450){
 				x -= 450;
 				y += 150;
 			}
 			
 //			System.out.print("@draw: mx = " + m_x + "\n");
 			g.drawImage(currentLib.get(i).thumbnail, x + m_x, y + m_y, null);
 		}
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
