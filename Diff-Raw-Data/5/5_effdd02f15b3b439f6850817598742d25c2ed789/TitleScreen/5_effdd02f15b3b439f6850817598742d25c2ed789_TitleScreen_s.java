 package twosnakes;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.io.*;
 import java.lang.Math;
 
 import javax.imageio.*;
 import javax.swing.*;
 
 import javax.swing.JFrame;
 
 import java.awt.geom.AffineTransform;
 
 public class TitleScreen {
 	
 	public final int SPACING = 65;
 	public final int XSTEP = 15;
 	public final String filenames[] = {"s", "n", "a", "K", "e", "s", "space", "o", "n", "space", "a", "space", "s", "c", "r", "e", "e", "n"};
 	private int x[];
 	private int actualX[];
 	private int y[];
 	BufferedImage[] images;
 	BufferedImage backgroundImage;
 	
 	public TitleScreen()
 	{
 		images = new BufferedImage[filenames.length];
 		for (int i = 0; i < filenames.length; i++)
         {
         	try {
				images[i] = ImageIO.read(new File("Images\\" + filenames[i] + ".png"));
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
         }
 		
 		try {
			backgroundImage = ImageIO.read(new File("Images\\T_background.png"));
 		} catch (IOException e2) {
 			e2.printStackTrace();
 			backgroundImage = null;
 		}
 		
 		x = new int[filenames.length];
 		actualX = new int[filenames.length];
 		y = new int[filenames.length];
 		
 		for (int i = 0; i < filenames.length; i++)
         {
         	x[i] = (i * SPACING) - (filenames.length * SPACING);
         	actualX[i] = (i * SPACING) - (filenames.length * SPACING);
         }
         int[] y = new int[filenames.length];
 	}
 	
 	public void step()
 	{
 		for (int i = 0; i < filenames.length; i++)
     	{
     		x[i] += XSTEP;
     		if (actualX[filenames.length / 2] < 1280 / 2)
     		{
     			actualX[i] += XSTEP;
     		}
     		y[i] = (int)(Math.sin((double)x[i] / (double)60) * 33) + 150;
     	}
 	}
 	
 	
 	
 	public void draw(Graphics g)
 	{
 		Graphics2D g2d = (Graphics2D)g;
 		g2d.drawImage(backgroundImage, 0, 0, null);
 		
 		for (int i = 0; i < filenames.length; i++)
     	{
     		AffineTransform trans = new AffineTransform();
     		trans.translate(actualX[i], y[i]);
     		trans.rotate(Math.cos((double)x[i] / (double)60) / 1.75, images[i].getWidth() / 2, images[i].getHeight() / 2);
 	        g2d.drawImage(images[i], trans, null);
     	}
 	}
 	
 }
