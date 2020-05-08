 import java.applet.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.util.*;
 import java.io.*;
 import java.util.ArrayList;
 class posCalc
 {
 	int absPosX, absPosY, camSizeX, camSizeY, onCamX, onCamY;
 	public posCalc(int camX, int camY, int csX, int csY)
 	{
 		onCamX=camX;
 		onCamY=camY;
 		camSizeX=csX;
 		camSizeY=csY;
 	}
 	int camY(int absY)
 	{
 		return onCamY-(absPosY-absY);
 	}
 	int camX(int absX)
 	{
 		return onCamX-(absPosX-absX);
 	}
 	void update(int pX, int pY)
 	{
 		absPosX=pX;
 		absPosY=pY;
 	}
 }
 class oBase
 {
 	Image img;
 	String imageName;
 
 	int posx,posy;
 	int vspeed=0, hspeed=0;
 	int width, height;
 
 	Boolean in_air = false;
 	Boolean gravity = false;
 
 	Boolean tiled = false;
 	int depth = 0;
 	public oBase(Image a, int x, int y,int d )//without mask
 	{
 		posx=x;
 		posy=y;
 		depth = d;
 		img = a;
 		width=0;
 		height=0;
 	}
 	public oBase(Image a, int x, int y,int d, int maskX, int maskY)//with mask
 	{
 		posx=x;
 		posy=y;
 		depth = d;
 		img = a;
 		width=maskX;
 		height=maskY;
 	}
 	public Boolean xinside(int x)
 	{
 		if(x>posx && x<posx+width)
 		{
 			return true;
 		}
 		return false;
 	}
 	public Boolean yinside(int y)
 	{
 		if(y>posy && y< posy+height)
 		{
 			return true;
 		}
 		return false;
 	}
 	/*
 	void move()
 	{
 		posx+=hspeed;
 		posy+=vspeed;
 		if(hspeed>0) //moving right
 		{
 			for(int i = 0)
 			if(this.posx+width) //check for collision
 
 		}
 		if(hspeed<0) //moving left
 		{
 			if(this.x)
 
 		}
 		if(vspeed>0)//moving up
 		{
 
 		}
 		if(vspeed<0)//moving down
 		{
 
 
 		}
 	}
 	*/
 }
 class oList
 {
 	posCalc pC;
 	Toolkit toolkit = Toolkit.getDefaultToolkit();
 	ArrayList<Image> images = new ArrayList<Image>();
 	ArrayList<oBase> objects = new ArrayList<oBase>();
 	int camera;
 	public oList(int cam, int screenSizeX, int screenSizeY, int cameraX, int cameraY)
 	{
 		pC=new posCalc(cameraX,cameraY,screenSizeX,screenSizeY);
 		camera = cam;
 	}
 	void loadObjects(String filename) throws IOException
 	{
 		Scanner oF = new Scanner(new File(filename));
 		while(oF.hasNextLine())
 		{
 			this.add(oF.nextInt(),oF.nextInt(),oF.nextInt(),oF.nextInt(),oF.nextInt(), oF.nextInt());
 		}
 	}
 	void loadImages(String filename) throws IOException
 	{
 		Scanner resFile = new Scanner(new File(filename));
 		while(resFile.hasNextLine())
 		{
 			this.addImage(resFile.nextLine());
 		}
 	}
 	void addImage(String img)
 	{
 		images.add(toolkit.getImage(img));
 	}
 	void add(oBase x)
 	{
 		objects.add(x);
 	}
 	void add(int ImageIndex, int x, int y, int depth)
 	{
 		objects.add(new oBase(images.get(ImageIndex),x,y,depth));
 	}
 	void add(int ImageIndex, int x, int y, int depth, int mX, int mY)
 	{
 		objects.add(new oBase(images.get(ImageIndex),x,y,depth,mX,mY));
 	}
 	private void swap(int x, int y)
 	{
 		oBase temp = objects.get(x);
 		objects.set(x,objects.get(y));
 		objects.set(y,temp);
 	}
 	public void sortDepth()//simple bubble sort
 	{
 		boolean sorted;
 		int p = 1;
 		do
 		{
 			sorted = true;
 			for (int q = 0; q < objects.size()-p; q++)
 				if (objects.get(q).depth < objects.get(q+1).depth) //sort objects with least depth to last so they are drawn last
 				{
 					swap(q,q+1);
 					sorted = false;
 				}
 			p++;
 		}
 		while (!sorted);
 	}
 	void setTiled(int i)
 	{
 		objects.get(i).tiled=true;
 	}
 	void unsetTiled(int i)
 	{
 		objects.get(i).tiled=false;
 	}
 	void setGravity(int i)
 	{
 		objects.get(i).gravity=true;
 	}
 	void unsetGravity(int i)
 	{
 		objects.get(i).gravity=false;
 	}
 	void sethSpeed(int i, int x)
 	{
 		objects.get(i).hspeed=x;
 	}
 	void setvSpeed(int i, int x)
 	{
 		objects.get(i).vspeed=x;
 	}
 	void move(Graphics g, game z)
 	{
 
 		for(int x = 0; x<objects.size();x++)
 		{
 
 			//objects.get(x).move();
 
 			//objects.get(x).posx+=objects.get(x).hspeed;
 			//objects.get(x).posy+=objects.get(x).vspeed;
 			if(objects.get(x).hspeed>0) //moving right
 			{
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).xinside(objects.get(x).posx+objects.get(x).width+objects.get(x).hspeed)) //if will collide right
								//if(objects.get(i).yinside(objects.get(x).posy)||objects.get(i).yinside(objects.get(x).posy+objects.get(x).height))
 								{
 									objects.get(x).posx=objects.get(i).posx-objects.get(x).width;
 									objects.get(x).hspeed=0;
 								}
 				}
 				objects.get(x).posx+=objects.get(x).hspeed;
 			}
 			if(objects.get(x).hspeed<0) //moving left
 			{
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).xinside(objects.get(x).posx+objects.get(x).hspeed)) //if will collide left
								//if(objects.get(i).yinside(objects.get(x).posy)||objects.get(i).yinside(objects.get(x).posy+objects.get(x).height))
 								{
 									objects.get(x).posx=objects.get(i).posx+objects.get(i).width;
 									objects.get(x).hspeed=0;
 								}
 				}
 				objects.get(x).posx+=objects.get(x).hspeed;
 
 			}
 			if(objects.get(x).vspeed<0)//moving up
 			{
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).yinside(objects.get(x).posy+objects.get(x).vspeed)) //if will collide top
 								if(objects.get(i).xinside(objects.get(x).posx)||objects.get(i).xinside(objects.get(x).posx+objects.get(x).width))
 								{
 									objects.get(x).posy=objects.get(i).posy+objects.get(i).height;
 									objects.get(x).vspeed=0;
 								}
 				}
 				objects.get(x).posy+=objects.get(x).vspeed;
 			}
 			if(objects.get(x).vspeed>0)//moving down
 			{
 
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).yinside(objects.get(x).posy+objects.get(x).height+objects.get(x).vspeed)) //if will collide bottom
 								if(objects.get(i).xinside(objects.get(x).posx)||objects.get(i).xinside(objects.get(x).posx+objects.get(x).width))
 								{
 									objects.get(x).posy=objects.get(i).posy-objects.get(x).height;
 									objects.get(x).vspeed=0;
 								}
 				}
 				objects.get(x).posy+=objects.get(x).vspeed;
 			}
 			if(camera < objects.size()) //if camera references an object that exists
 			pC.update(objects.get(camera).posx,objects.get(camera).posy);
 
 			if(objects.get(x).tiled == true) //if background
 			{
 				g.drawImage(objects.get(x).img, 700-objects.get(camera).posx%700,  350-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 0-objects.get(camera).posx%700,  350-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 700-objects.get(camera).posx%700,  0-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 0-objects.get(camera).posx%700,  0-objects.get(camera).posy%350, z);
 			}
 			else
 			{
 				g.drawImage(objects.get(x).img,	pC.camX(objects.get(x).posx),	pC.camY(objects.get(x).posy),z);
 			}
 
 		}
 	}
 	void draw(Graphics g, game z)
 	{
 
 		for(int x = 0; x<objects.size();x++)
 		{
 
 			//objects.get(x).move();
 
 			//objects.get(x).posx+=objects.get(x).hspeed;
 			//objects.get(x).posy+=objects.get(x).vspeed;
 			if(objects.get(x).hspeed>0) //moving right
 			{
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).xinside(objects.get(x).posx+objects.get(x).width+objects.get(x).hspeed)) //if will collide right
 								if(objects.get(i).yinside(objects.get(x).posy)||objects.get(i).yinside(objects.get(x).posy+objects.get(x).height))
 								{
 									objects.get(x).posx=objects.get(i).posx-objects.get(x).width;
 									objects.get(x).hspeed=0;
 								}
 				}
 				objects.get(x).posx+=objects.get(x).hspeed;
 			}
 			if(objects.get(x).hspeed<0) //moving left
 			{
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).xinside(objects.get(x).posx+objects.get(x).hspeed)) //if will collide left
 								if(objects.get(i).yinside(objects.get(x).posy)||objects.get(i).yinside(objects.get(x).posy+objects.get(x).height))
 								{
 									objects.get(x).posx=objects.get(i).posx+objects.get(i).width;
 									objects.get(x).hspeed=0;
 								}
 				}
 				objects.get(x).posx+=objects.get(x).hspeed;
 
 			}
 			if(objects.get(x).vspeed<0)//moving up
 			{
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).yinside(objects.get(x).posy+objects.get(x).vspeed)) //if will collide top
 								if(objects.get(i).xinside(objects.get(x).posx)||objects.get(i).xinside(objects.get(x).posx+objects.get(x).width))
 								{
 									objects.get(x).posy=objects.get(i).posy+objects.get(i).height;
 									objects.get(x).vspeed=0;
 								}
 				}
 				objects.get(x).posy+=objects.get(x).vspeed;
 			}
 			if(objects.get(x).vspeed>0)//moving down
 			{
 
 				for(int i = 0; i < objects.size(); i++)
 				{
 						if(i!=x&&!objects.get(i).tiled) //if not itself and not tiled
 							if(objects.get(i).yinside(objects.get(x).posy+objects.get(x).height+objects.get(x).vspeed)) //if will collide bottom
 								if(objects.get(i).xinside(objects.get(x).posx)||objects.get(i).xinside(objects.get(x).posx+objects.get(x).width))
 								{
 									objects.get(x).posy=objects.get(i).posy-objects.get(x).height;
 									objects.get(x).vspeed=0;
 								}
 				}
 				objects.get(x).posy+=objects.get(x).vspeed;
 			}
 			if(camera < objects.size()) //if camera references an object that exists
 			pC.update(objects.get(camera).posx,objects.get(camera).posy);
 
 			if(objects.get(x).tiled == true) //if background
 			{
 				g.drawImage(objects.get(x).img, 700-objects.get(camera).posx%700,  350-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 0-objects.get(camera).posx%700,  350-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 700-objects.get(camera).posx%700,  0-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 0-objects.get(camera).posx%700,  0-objects.get(camera).posy%350, z);
 			}
 			else
 			{
 				g.drawImage(objects.get(x).img,	pC.camX(objects.get(x).posx),	pC.camY(objects.get(x).posy),z);
 			}
 
 		}
 	}
 }
 class game extends Panel implements KeyListener
 {
 	oList objectlist = new oList(2,700,350,300,180);
 	public static void main(String[] args) throws IOException
 	{
 
 		Frame f = new Frame();
 		f.addWindowListener(new java.awt.event.WindowAdapter()
 		{
 			public void windowClosing(java.awt.event.WindowEvent e)
 			{
 				System.exit(0);
 			};
 		});
 		game x = new game();
 		x.setFocusable(true);
 		x.setSize(700,350); // same size as defined in the HTML APPLET
 		f.add(x);
 		f.pack();
 		x.init();
 		f.setSize(700,350+20); // add 20, seems enough for the Frame title,
 		while(true)
 		{
 			x.repaint();
 			f.show();
 			try{
 				Thread.sleep(20);
 			}
 			catch(InterruptedException ex){}
 		}
 	}
  	AudioClip soundFile1;
 	public void init() throws IOException
 	{
 
 		addKeyListener(this);
 		/*soundFile1.play();
 		addKeyListener(this);*/
 
 		//load images
 		objectlist.loadImages("game/res.dat");
 		objectlist.loadObjects("game/objects.dat");
 		//extra initializations
 		objectlist.sethSpeed(2,4);
 		//objectlist.add(1,500,500,0,this);
 		objectlist.setTiled(0);
 		objectlist.setGravity(2);
 	}
 	public void paint(Graphics g)
 	{
 		objectlist.sortDepth();
 		objectlist.draw(g,this);
 	}
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 	public void keyPressed(KeyEvent ke)
 	{
 		switch(ke.getKeyCode())
 		{
 			case KeyEvent.VK_DOWN:
 				objectlist.setvSpeed(objectlist.camera,10);
 				break;
 			case KeyEvent.VK_RIGHT:
 				objectlist.sethSpeed(objectlist.camera,10);
 				break;
 			case KeyEvent.VK_LEFT:
 				objectlist.sethSpeed(objectlist.camera,-10);
 				break;
 			case KeyEvent.VK_UP:
 				objectlist.setvSpeed(objectlist.camera,-10);
 				break;
 		}
 	}
 	public void keyTyped(KeyEvent ke) {}
 	public void keyReleased(KeyEvent ke)
 	{
 		switch(ke.getKeyCode())
 		{
 			case KeyEvent.VK_DOWN:
 				objectlist.setvSpeed(objectlist.camera,0);
 				break;
 			case KeyEvent.VK_RIGHT:
 				objectlist.sethSpeed(objectlist.camera,0);
 				//objects.get(camera).hspeed=0;
 				break;
 			case KeyEvent.VK_LEFT:
 				objectlist.sethSpeed(objectlist.camera,0);
 				//objects.get(camera).hspeed=0;
 				break;
 			case KeyEvent.VK_UP:
 				objectlist.setvSpeed(objectlist.camera,0);
 				break;
 		}
 	}
 	public boolean mouseDrag(Event e, int x, int y)
 	{
 
 		return true;
 	}
 	public boolean mouseDown(Event e, int x, int y)
 	{
 
 		return true;
 	}
 	public boolean mouseUp(Event e, int x, int y)
 	{
 
 		return true;
 	}
 }
