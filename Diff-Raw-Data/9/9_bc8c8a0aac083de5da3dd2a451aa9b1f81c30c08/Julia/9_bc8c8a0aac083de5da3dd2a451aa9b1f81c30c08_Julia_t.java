 package com.barantschik.trinkets.fractal;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class Julia extends JPanel implements MouseListener, KeyListener
 {
 	private static final long serialVersionUID = 1L;
 
 	private final int BASE_NUM_ITERATIONS = 20;
 	private final int ADD_ITER_PER_ZOOM = 20;
 	private final int SIZE_X = 1000, SIZE_Y = 1000;
 	private final Color PRISONER_COLOR = Color.BLACK;
 	private final Color ORIGINAL_BASE_COLOR = Color.BLUE;
 	private final boolean IS_MANDELBROT = false;
 	
 	private double cReal = 0, cImaginary = 0;
 	private boolean started = false;
 	private double centerX = 0, centerY = 0;
 	private double rangeX = 4, rangeY = 4;
 	private int numIterAdded = 0;
 	private int hue = 360; //HSV color system
 	private Color baseColor = ORIGINAL_BASE_COLOR;
 	
 	public Julia()
 	{
		cReal = -0.8;
		cImaginary = 0.156;
 
 		setSize(SIZE_X, SIZE_Y);
 		setPreferredSize(new Dimension(SIZE_X, SIZE_Y));
		setFocusable(true);
		
 		addMouseListener(this);
 		addKeyListener(this);
 
 		try
 		{
 			Thread.sleep(10);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		started = true;
 		repaint();
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		if(started)
 		{
 			baseColor = ORIGINAL_BASE_COLOR;
 			hue = 360;
 
 			g.setColor(PRISONER_COLOR);
 			g.fillRect(0, 0, SIZE_X, SIZE_Y);
 			g.setColor(baseColor);
 
 			Complex c = new Complex(cReal, cImaginary);
 			Complex[][] field = new Complex[SIZE_X][SIZE_Y];
 			Complex[][] baseField = null;
 			if(IS_MANDELBROT)
 			{
 				for(int i = 0; i < SIZE_X; i++)
 				{
 					for(int j = 0; j < SIZE_Y; j++)
 					{
 						field[i][j] = new Complex(((2 * rangeX * (i / (double) SIZE_X) - rangeX) + centerX), (-(2 * rangeY * (j / (double) SIZE_Y) - rangeY) + centerY));
 					}
 
 				}
 				baseField = new Complex[SIZE_X][SIZE_Y];
 				for(int i = 0; i < SIZE_X; i++)
 				{
 					for(int j = 0; j < SIZE_Y; j++)
 					{
 						baseField[i][j] = new Complex(((2 * rangeX * (i / (double) SIZE_X) - rangeX) + centerX), (-(2 * rangeY * (j / (double) SIZE_Y) - rangeY) + centerY));
 					}
 				}
 			}
 			else
 			{				
 				for(int i = 0; i < SIZE_X; i++)
 				{
 					for(int j = 0; j < SIZE_Y; j++)
 					{
 						field[i][j] = new Complex(((2 * rangeX * (i / (double) SIZE_X) - rangeX) + centerX), (-(2 * rangeY * (j / (double) SIZE_Y) - rangeY) + centerY));
 					}
 				}
 			}
 
 			for(int i = 0; i < BASE_NUM_ITERATIONS + numIterAdded; i++)
 			{
 				if(hue == 0) hue = 360;
 				//Random r = new Random();
 				//hue = r.nextInt(360) + 1;
 				hue -= 4;
 				double hNew = hue / 60.0;
 				double x = 1 - Math.abs((hNew % 2) - 1);
 				float brightness =1;
 				if(hNew >= 0 && hNew < 1) baseColor = new Color(brightness, (float) x * brightness, 0.0f);
 				else if(hNew >= 0 && hNew < 1) baseColor = new Color(brightness, (float) x * brightness, 0.0f);
 				else if(hNew >= 1 && hNew < 2) baseColor = new Color((float) x * brightness, brightness, 0.0f);
 				else if(hNew >= 2 && hNew < 3) baseColor = new Color(0.0f, brightness, (float) x * brightness);
 				else if(hNew >= 3 && hNew < 4) baseColor = new Color(0.0f, (float) x * brightness, brightness);
 				else if(hNew >= 4 && hNew < 5) baseColor = new Color((float) x * brightness, 0.0f, brightness);
 				else if(hNew >= 5 && hNew < 6) baseColor = new Color(brightness, 0.0f, (float) x * brightness);
 				g.setColor(baseColor);
 
 				for(int col = 0; col < SIZE_X; col++)
 				{
 					for(int row = 0; row < SIZE_Y; row++)
 					{
 						if(!field[col][row].isOutsideRange())
 						{
 							field[col][row].square();
 							if(IS_MANDELBROT) field[col][row].add(baseField[col][row]);
 							else field[col][row].add(c);
 
 							if(field[col][row].magnitude() > 2)
 							{
 								g.drawLine(col, row, col, row);
 								field[col][row].outsideRange();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void makeImage()
 	{
 		System.out.println("started");
 		final int SIZE_X = 4000, SIZE_Y = 4000;
 		BufferedImage hugeImage = new BufferedImage(SIZE_X, SIZE_Y, BufferedImage.TYPE_INT_RGB);
 		Graphics g = hugeImage.getGraphics();
 		baseColor = ORIGINAL_BASE_COLOR;
 		hue = 360;
 
 		g.setColor(PRISONER_COLOR);
 		g.fillRect(0, 0, SIZE_X, SIZE_Y);
 		g.setColor(baseColor);
 
 		Complex c = new Complex(cReal, cImaginary);
 		Complex[][] field = new Complex[SIZE_X][SIZE_Y];
 		Complex[][] baseField = null;
 		if(IS_MANDELBROT)
 		{
 			for(int i = 0; i < SIZE_X; i++)
 			{
 				for(int j = 0; j < SIZE_Y; j++)
 				{
 					field[i][j] = new Complex(((2 * rangeX * (i / (double) SIZE_X) - rangeX) + centerX), (-(2 * rangeY * (j / (double) SIZE_Y) - rangeY) + centerY));
 				}
 
 			}
 			baseField = new Complex[SIZE_X][SIZE_Y];
 			for(int i = 0; i < SIZE_X; i++)
 			{
 				for(int j = 0; j < SIZE_Y; j++)
 				{
 					baseField[i][j] = new Complex(((2 * rangeX * (i / (double) SIZE_X) - rangeX) + centerX), (-(2 * rangeY * (j / (double) SIZE_Y) - rangeY) + centerY));
 				}
 			}
 		}
 		else
 		{				
 			for(int i = 0; i < SIZE_X; i++)
 			{
 				for(int j = 0; j < SIZE_Y; j++)
 				{
 					field[i][j] = new Complex(((2 * rangeX * (i / (double) SIZE_X) - rangeX) + centerX), (-(2 * rangeY * (j / (double) SIZE_Y) - rangeY) + centerY));
 				}
 			}
 		}
 
 		for(int i = 0; i < BASE_NUM_ITERATIONS + numIterAdded; i++)
 		{
 			if(hue == 0) hue = 360;
 			//Random r = new Random();
 			//hue = r.nextInt(360) + 1;
 			hue -= 4;
 			double hNew = hue / 60.0;
 			double x = 1 - Math.abs((hNew % 2) - 1);
 			float brightness = 1;
 			if(hNew >= 0 && hNew < 1) baseColor = new Color(brightness, (float) x * brightness, 0.0f);
 			else if(hNew >= 0 && hNew < 1) baseColor = new Color(brightness, (float) x * brightness, 0.0f);
 			else if(hNew >= 1 && hNew < 2) baseColor = new Color((float) x * brightness, brightness, 0.0f);
 			else if(hNew >= 2 && hNew < 3) baseColor = new Color(0.0f, brightness, (float) x * brightness);
 			else if(hNew >= 3 && hNew < 4) baseColor = new Color(0.0f, (float) x * brightness, brightness);
 			else if(hNew >= 4 && hNew < 5) baseColor = new Color((float) x * brightness, 0.0f, brightness);
 			else if(hNew >= 5 && hNew < 6) baseColor = new Color(brightness, 0.0f, (float) x * brightness);
 			g.setColor(baseColor);
 
 			for(int col = 0; col < SIZE_X; col++)
 			{
 				for(int row = 0; row < SIZE_Y; row++)
 				{
 					if(!field[col][row].isOutsideRange())
 					{
 						field[col][row].square();
 						if(IS_MANDELBROT) field[col][row].add(baseField[col][row]);
 						else field[col][row].add(c);
 
 						if(field[col][row].magnitude() > 20000)
 						{
 							g.drawLine(col, row, col, row);
 							field[col][row].outsideRange();
 						}
 					}
 				}
 			}
 		}
 		
 		try
 		{
 			ImageIO.write(hugeImage, "PNG", new File("image.png"));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		System.out.println("made");
 	}
 	
 	public void mousePressed(MouseEvent e)
 	{
 		double x = e.getX();
 		double y = e.getY();
 
 		centerX = (2 * rangeX * (x / (double) SIZE_X) - rangeX) + centerX;
 		centerY = -(2 * rangeY * (y / (double) SIZE_Y) - rangeY) + centerY;
 		rangeX /= 4;
 		rangeY /= 4;
 
 		numIterAdded += ADD_ITER_PER_ZOOM;
 		repaint();
 	}
 	
 	public void keyPressed(KeyEvent e)
 	{
 		if(e.getKeyChar() == 'q')
 		{			
 			numIterAdded += ADD_ITER_PER_ZOOM * 10;
 			repaint();
 		}
 		else if(e.getKeyChar() == 'p')
 		{
 			makeImage();
 		}
 	}
 
 	//Unimplemented methods
 	public void mouseClicked(MouseEvent e){}
 	public void mouseEntered(MouseEvent e){}
 	public void mouseExited(MouseEvent e){}
 	public void mouseReleased(MouseEvent e){}
 	public void keyReleased(KeyEvent e){}
 	public void keyTyped(KeyEvent e){}
 }
