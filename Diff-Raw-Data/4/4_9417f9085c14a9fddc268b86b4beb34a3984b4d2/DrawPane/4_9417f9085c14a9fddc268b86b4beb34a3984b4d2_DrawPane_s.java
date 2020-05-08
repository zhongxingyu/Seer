 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class DrawPane extends JPanel implements ActionListener{
 	private boolean runMenu = true;
 	BufferedImage menu;
 	Timer timer = null;
 	
 	DrawPane(){
 		start();
 		try {
 			menu = ImageIO.read(new File("./menu.jpg"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void actionPerformed(ActionEvent paramActionEvent)
   {
 	Monster_Controller.update();
     timer.restart();
   }
 	
 	public void start() {
    timer = new Timer(1000, this);
    timer.setInitialDelay(1000);
     timer.start();
   }
 	
 	
 	public void runMenu(){
 		if(runMenu){
 			runMenu = false;
 		}else
 			runMenu = true;
 	}
 	
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		
 		if(!runMenu){
 			for(int j = 0; j < getHeight()/32; j++){
 				for(int i = 0; i < getWidth()/32; i++){
 					Sprite s = Data.getMapSprite(i,j);
 					if(s != null)
 						g.drawImage(s.getImage(), Math.round(s.getX()), Math.round(s.getY()), null);
 				}
 			} 
 		Tower_Controler.update(g);
 //		Monster_Controller.update(g);
 		Monster_Controller.draw(g);
 		}else{
 			g.drawImage(menu,0,0,getWidth(), getHeight(),null);
 		}
 	}
 }
