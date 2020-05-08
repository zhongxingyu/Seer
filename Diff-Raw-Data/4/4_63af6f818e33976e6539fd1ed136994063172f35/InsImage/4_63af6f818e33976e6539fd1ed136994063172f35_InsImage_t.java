 
 package com.inspedio.entity.basic;
 
 import java.io.IOException;
 
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 
 import com.inspedio.entity.primitive.InsSize;
 import com.inspedio.enums.TransformType;
 import com.inspedio.system.helper.InsUtil;
 import com.inspedio.system.helper.extension.InsAlignment;
 
 /**
  * This class represent graphic resources.<b>
  * It can be single image, or spritesheet (divided into some frame)
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsImage{
 
 	public String filepath;
 	public Image source;
 	public InsSize size;
 	public int frameWidth;
 	public int frameHeight;
 	public int frameCountX;
 	public int frameCountY;
 	public int frameTotal;
 	
 	/**
 	 * Create single frame image
 	 * 
 	 * @param	imagePath	File path of Image
 	 */
 	public InsImage(String imagePath)
 	{
 		this.filepath = imagePath;
 		try
 		{
 			this.source = Image.createImage(imagePath);
 			this.size = new InsSize(source.getWidth(), source.getHeight());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		this.frameWidth = this.size.width;
 		this.frameHeight = this.size.height;
 		this.filepath = imagePath;
 		this.frameCountX = 1;
 		this.frameCountY = 1;
 		this.frameTotal = 1;
 	}
 	
 	/**
 	 * Construct a new Image, and splits it into many frames
 	 * 
 	 * @param	imagePath	File path of Spritesheet Image
 	 * @param	frameWidth	The width of each frame
 	 * @param	frameHeight	The height of each frame
 	 */
 	public InsImage(String imagePath, int frameWidth, int frameHeight)
 	{
 		this.filepath = imagePath;
 		try
 		{
 			this.source = Image.createImage(imagePath);
 			this.size = new InsSize(source.getWidth(), source.getHeight());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		this.frameWidth = frameWidth;
 		this.frameHeight = frameHeight;
 		this.filepath = imagePath;
		this.frameCountX = (int) (this.size.width / this.frameWidth);
		this.frameCountY = (int) (this.size.height / this.frameHeight);
 		this.frameTotal = this.frameCountX * this.frameCountY;
 	}
 	
 	
 	public void drawImage(Graphics g, int X, int Y, InsAlignment Alignment)
 	{
 		g.drawImage(this.source, X, Y, InsAlignment.getAnchorValue(Alignment));
 	}
 	
 	public void drawRegion(Graphics g, int X_Source, int Y_Source, int Width, int Height, TransformType transform, int X_Dest, int Y_Dest, InsAlignment Alignment){
 		g.drawRegion(this.source, X_Source, Y_Source, Width, Height, TransformType.getTransformValue(transform), X_Dest, Y_Dest, InsAlignment.getAnchorValue(Alignment));
 	}
 	
 	public void drawFrame(Graphics g, int Frame, int X, int Y, InsAlignment alignment, TransformType transform){
 		if(Frame >= 0){
 			Frame = Frame % this.frameTotal;
 			int x_src = (Frame % this.frameCountX) * this.frameWidth;
 			int y_src = ((int) (Frame / this.frameCountX)) * this.frameHeight;
 			this.drawRegion(g, x_src, y_src, this.frameWidth, this.frameHeight, transform, X, Y, alignment);
 		} else {
 			Frame = InsUtil.Absolute(Frame) % this.frameTotal;
 			int x_src = (Frame % this.frameCountX) * this.frameWidth;
 			int y_src = ((int) (Frame / this.frameCountX)) * this.frameHeight;
 			this.drawRegion(g, x_src, y_src, this.frameWidth, this.frameHeight, TransformType.getMirror(transform), X, Y, alignment);
 		}
 	}
 	
 	public void drawFrameRegion(Graphics g, int Frame, int PercentageX, int PercentageY, int X, int Y, InsAlignment alignment, TransformType transform, InsAlignment Stretch){
 		if(Frame >= 0){
 			Frame = Frame % this.frameTotal;
 			int x_src = ((Frame % this.frameCountX) * this.frameWidth) + (int) ((Stretch.horizontal.getValue() * PercentageX * this.frameWidth) / 200);
 			int y_src = (((int) (Frame / this.frameCountX)) * this.frameHeight) + (int) ((Stretch.vertical.getValue() * PercentageY * this.frameHeight) / 200);
 			this.drawRegion(g, x_src, y_src, (this.frameWidth * PercentageX) / 100, (this.frameHeight * PercentageY) / 100, transform, X, Y, alignment);
 		} else {
 			Frame = InsUtil.Absolute(Frame) % this.frameTotal;
 			int x_src = ((Frame % this.frameCountX) * this.frameWidth) + (int) ((Stretch.horizontal.getValue() * PercentageX * this.frameWidth) / 200);
 			int y_src = (((int) (Frame / this.frameCountX)) * this.frameHeight) + (int) ((Stretch.vertical.getValue() * PercentageY * this.frameHeight) / 200);
 			this.drawRegion(g, x_src, y_src, (this.frameWidth * PercentageX) / 100, (this.frameHeight * PercentageY) / 100, TransformType.getMirror(transform), X, Y, alignment);
 		}
 	}
 	public void destroy()
 	{
 		this.source = null;
 	}
 	
 	
 }
