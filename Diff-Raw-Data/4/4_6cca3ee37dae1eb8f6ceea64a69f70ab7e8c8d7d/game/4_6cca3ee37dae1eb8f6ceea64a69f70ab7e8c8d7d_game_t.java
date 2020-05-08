 import java.applet.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.util.ArrayList;
 public class game extends Applet implements KeyListener
 {
 	int i = 0;
 	int j = 0;
 	int objects_index=0;
  	AudioClip soundFile1;
 	Image snoopy;
 	Image background;
 	ArrayList<oBase> objects = new ArrayList<oBase>();
 	public void init()
 	{
 		soundFile1 = getAudioClip(getDocumentBase(),"music/01.wav");
 		background = getImage(getDocumentBase(),"backgrounds/01.jpg");
 		addKeyListener(this);
 		soundFile1.play();
 
 		objects.add(new oBase("img/snoopy.gif",380,100,this));
 		objects.get(0).hspeed = 5;
 		objects_index++;
 		//objects.add(new oBack("backgrounds/01.jpg",0,0,this));
 		//objects_index++;
 	}
 
 	public void paint(Graphics g)
 	{
 		//g.drawImage(background,(0-objects.get(0).posx)%700,j,this);
 		//g.drawImage(background,(700-objects.get(0).posx)%700,j,this);
 		g.drawImage(background,0-i,j,this);
 		g.drawImage(background,700-i,j,this);
		//g.drawImage(snoopy,300,180,this);

		for(int x = 0; x<objects_index;x++)
 		{
 			g.drawImage(objects.get(x).img,objects.get(x).posx,objects.get(x).posy,this);
 			objects.get(x).posx+=objects.get(x).hspeed;
 			objects.get(x).posy+=objects.get(x).vspeed;
 		}
 		i+=2;
 
 		//Restarts the background
 		if(i>700)
 			i-=700;
 		if(i<0)
 			i+=700;
 	}
 
 	public void keyPressed(KeyEvent ke) {
 		switch(ke.getKeyCode())
 		{
 			case KeyEvent.VK_DOWN:
 					j-=5;
 				break;
 			case KeyEvent.VK_RIGHT:
 				i+=3;
 				break;
 			case KeyEvent.VK_LEFT:
 				i-=3;
 				break;
 			case KeyEvent.VK_UP:
 					j+=5;
 				break;
 		}
 	}
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 	public void keyTyped(KeyEvent ke) {}
 	public void keyReleased(KeyEvent ke) {
 		switch(ke.getKeyCode())
 		{
 			case KeyEvent.VK_DOWN:
 					j-=5;
 				break;
 			case KeyEvent.VK_RIGHT:
 				i+=3;
 				break;
 			case KeyEvent.VK_LEFT:
 				i-=3;
 				break;
 			case KeyEvent.VK_UP:
 					j+=5;
 				break;
 		}
 	}
 }
 
