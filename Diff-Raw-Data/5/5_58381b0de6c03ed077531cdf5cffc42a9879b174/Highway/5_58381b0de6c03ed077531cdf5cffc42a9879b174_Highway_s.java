 package simulation;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 
 public class Highway extends JLayeredPane{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1111;
 	
 	private BufferedImage background;
 	private int width, height;
 	private int HighwayLength;
 	private static String imgURL = "http://w.csie.org/~b99902022/images/highway.jpg";
 	
 	// use boolean to record highway state
 	// true: has car,  false: no car
 	private boolean[] HighwayState;
 	
 	// crash control
 	private boolean[] crashMark;
 	private boolean CrashFlag = false;
 	private int CrashFrom, CrashTo;
 	private Thread CrashThread;
 	
 	// interchange control
 	private boolean[] interchange;
 	private static int timeControl = 500;
 	
 	// Status control
 	private boolean stop;
 	private boolean CarAlive;
 	
 	public Highway(int HighwayLength)
 	{
 		try{
 			background = ImageIO.read(new URL(imgURL));
 			width = background.getWidth();
 			height = background.getHeight();
 			
 		}catch(Exception e){
 			System.out.println("read error");
 			background = null;
 		}
 		HighwayState = new boolean[width];
 		crashMark = new boolean[width];
 		interchange = new boolean[width];
 		this.HighwayLength = HighwayLength;
 		this.setLayout(null);
 		CarAlive = true;
 		stop = true;
 	}
 	public void addCar(int x, Car newCar, Thread t, int state)
 	{
 		newCar.setPosition(x);
 		newCar.setVisible(false);
 		this.add(newCar, JLayeredPane.POPUP_LAYER);
 		newCar.setState(state);
 		newCar.setHighway(this);
 		
 		CrashFlag = false;
 		CrashFrom = HighwayLength;
 		CrashTo = 0;
 		if(state == 1)
 		{
 			for(int i=0; i<newCar.getCarWidth(); i++)
 			{
 				try{
 					if(HighwayState[x+i] == false)
 						HighwayState[x+i] = true;
 					else
 					{
 						newCar.setState(-1);
 						markCrash(x+i);
 						if(x+i < CrashFrom)
 							CrashFrom = x+i;
 						if(x+i > CrashTo)
 							CrashTo = x+i;
 						CrashFlag = true;
 					}
 				}catch(Exception e){}
 			}
 		}
 		t.start();
 		if(CrashFlag == true)
 		{
 			CrashThread = new Crash(CrashFrom, CrashTo);
 			CrashThread.start();
 		}
 	}
 	public synchronized void CarRun(Car newCar, int PositionX, int curSpeed)
 	{
 		int newPosition = PositionX + curSpeed;
 		for(int i=0; i<newCar.getCarWidth(); i++)
 		{
 			try{
 				HighwayState[PositionX+i] = false;
 			}catch(Exception e){}
 		}
 		
 		CrashFlag = false;
 		CrashFrom = HighwayLength;
 		CrashTo = 0;
 		newCar.setPosition(PositionX+curSpeed);
 		for(int i=0; i<newCar.getCarWidth(); i++)
 		{
 			try{
 				if(HighwayState[newPosition+i] == false)
 					HighwayState[newPosition+i] = true;
 				else
 				{
 					newCar.setState(-1);
 					markCrash(newPosition+i);
 					if(newPosition+i < CrashFrom)
 						CrashFrom = newPosition+i;
 					if(newPosition+i > CrashTo)
 						CrashTo = newPosition+i;
 					CrashFlag = true;
 				}
 			}catch(Exception e){}
 		}
 		if(CrashFlag == true)
 		{
 			CrashThread = new Crash(CrashFrom, CrashTo);
 			CrashThread.start();
 		}
 	}
 	public void setInterchange(int PositionX)
 	{
 		InterchangeControl Interchange = new InterchangeControl(PositionX);
 		Thread control = new Thread(Interchange);
 		control.start();
 	}
 	public synchronized void CarGoOnInterchange(Car newCar, int PositionX)
 	{
 		if(interchange[PositionX] == true)
 		{
			if(frontCarDistance(PositionX, newCar) > 0 && backCarDistance(PositionX, newCar) > newCar.getWidth()/2)
 			{
 				newCar.setState(1);
 				interchange[PositionX] = false;
 			}
 		}
 	}
 	public int frontCarDistance(int x, Car newCar)
 	{
 		int i, Distance;
 		for(i=x+newCar.getCarWidth(); i<width; i++)
 			if(HighwayState[i] == true)
 				break;
 		Distance = i - x;
 		if(i >= width)
 			return width;
 		else
 			return Distance - newCar.getCarWidth();
 	}
 	public int backCarDistance(int x, Car newCar)
 	{
 		int i, Distance;
 		for(i=x; i>=0; i--)
 			if(HighwayState[i] == true)
 				break;
 		Distance = i;
 		if(i < 0)
 			return width;
 		else
 			return Distance;
 	}
 	public void clearState(int PositionX, Car newCar)
 	{
 		for(int i=0; i<newCar.getCarWidth(); i++)
 			HighwayState[i+PositionX] = false;
 	}
 	public void markCrash(int x)
 	{
 		crashMark[x] = true;
 	}
 	public void fixCrash(int x)
 	{
 		crashMark[x] = false;
 	}
 	public boolean checkCrash(int x, Car newCar)
 	{
 		for(int i=0; i<HighwayLength; i++)
 			if(crashMark[i] && i>=x && i<= x+newCar.getCarWidth())
 					return true;
 		return false;
 	}
 	public int getLength()
 	{
 		return HighwayLength; 
 	}
 	public void changeStop(boolean b)
 	{
 		stop = b;
 	}
 	public boolean getStop()
 	{
 		return stop;
 	}
 	public void setHighwayLength(int Length)
 	{
 		HighwayLength = Length;
 		this.setBounds(0, 0, Length, 150);
 	}
 	public void setAlive(boolean b)
 	{
 		CarAlive = b;
 		if(b)
 		{
 			for(int i=0; i<width; i++)
 			{
 				HighwayState[i] = false;
 				crashMark[i] = false;
 			}
 		}
 	}
 	public boolean getAlive()
 	{
 		return CarAlive;
 	}
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		try{
 			g.drawImage(background, 0, 0, width, height, null);
 		}catch(Exception e){}
 	}
 		
 	public class InterchangeControl implements Runnable{
 		
 		private int Position;
 		
 		public InterchangeControl(int Position)
 		{
 			this.Position = Position;
 		}
 		@Override
 		public void run() {
 			while(true)
 			{
 				try{
 					Thread.sleep(timeControl);
 				}catch(Exception e){}
 				if(HighwayState[Position] == false)
 				{
 					interchange[Position] = true;
 					try{
 						Thread.sleep(timeControl);
 					}catch(Exception e){}
 				}
 			}
 		}
 	}
 	public class Crash extends Thread{
 		
 		private int from;
 		private int to;
 		
 		public Crash(int from, int to)
 		{
 			this.from = from;
 			this.to = to;
 		}
 		
 		public void run()
 		{
 			for(int i=0; i<20; i++)
 			{
 				while(getStop())
 				{
 					try{
 						Thread.sleep(200);
 					}catch(Exception e){}
 					if(!CarAlive)
 						break;
 				}
 				try{
 					Thread.sleep(200);
 				}catch(Exception e){}
 				
 				if(!CarAlive)
 				{
 					break;
 				}
 			}
 			for(int i=from; i<=to; i++)
 			{
 				fixCrash(i);
 				HighwayState[i] = false;
 			}
 		}
 	}
 	public static void main(String[] args)
 	{
 		JFrame main = new JFrame("test");
 		main.setSize(1200, 600);
 		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		main.setLayout(null);
 		Highway highway = new Highway(1200);
 		main.add(highway);
 		highway.setBounds(0, 0, 1200, 400);
 		main.setVisible(true);
 		
 		highway.setInterchange(0);
 		/*
 		Car cartmp;
 		Thread threadtmp;
 		for(int i=0; i<10; i++)
 		{
 			cartmp = new Car();
 			cartmp.setHighway(highway);
 			threadtmp = new Thread(cartmp);
 			highway.addCar(0, cartmp, threadtmp, 0);
 		}
 		highway.setInterchange(500);
 		for(int i=0; i<10; i++)
 		{
 			cartmp = new Car();
 			cartmp.setHighway(highway);
 			threadtmp = new Thread(cartmp);
 			highway.addCar(500, cartmp, threadtmp, 0);
 		}*/
 	}
 }
 
