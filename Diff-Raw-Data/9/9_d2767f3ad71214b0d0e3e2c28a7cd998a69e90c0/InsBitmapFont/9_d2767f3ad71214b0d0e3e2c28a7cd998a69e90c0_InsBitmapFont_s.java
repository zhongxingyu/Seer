 package com.inspedio.entity;
 
 import java.util.Vector;
 
 import javax.microedition.lcdui.Graphics;
 
 import com.inspedio.entity.primitive.InsImage;
 import com.inspedio.enums.HAlignment;
 import com.inspedio.enums.TransformType;
 import com.inspedio.enums.VAlignment;
 import com.inspedio.system.core.InsGlobal;
 import com.inspedio.system.helper.InsUtil;
 import com.inspedio.system.helper.extension.InsAlignment;
 
 /**
  * <code>InsBitmapFont</code> is a spritesheet which consists of alphanumeric or symbols.<br>
  * Used to display custom font with image. <br>
  * You can set Text to be displayed, but have to make sure you define charMapping in <code>getCharFrame(c)</code> method.
  * 
  * @author Hyude
  * @version 1.0
  */
 public abstract class InsBitmapFont extends InsBasic{
 	
 	/**
 	 * Sprite that contains Bitmap FontSheet
 	 */
 	protected InsImage fontsheet;
 	/**
 	 * current Text displayed by bitmapFont (Single Line)
 	 */
 	protected String currentText;
 	/**
 	 * Whether current Text is single Line or multiple
 	 */
 	protected boolean singleLine;
 	/**
 	 * current Text displayed by bitmapFont (Multi Line)
 	 */
 	protected String[] currentTexts;
 	/**
 	 * Frame Array of current text (Single Line)
 	 */
 	protected int[] frameChar;
 	/**
 	 * Frame Array of current text (Multi Line)
 	 */
 	protected int[][] frameChars;
 	/**
 	 * Length of current Text
 	 */
 	protected int length;
 	/**
 	 * How much line text has (for multiline)
 	 */
 	protected int lineCount;
 	/**
 	 * Hw much space between text line
 	 */
 	protected int lineSpace;
 	
 	/**
 	 * Maximum Allowed Size of text. If you set text longer than maximum size, it will be cropped.
 	 */
 	public int maxSize;
 	
 	protected InsAlignment topleft;
 	
 	public InsBitmapFont(String imagePath, int fontWidth, int fontHeight, int maxSize, String Text)
 	{
 		super(InsGlobal.middleX, InsGlobal.middleY, fontWidth, fontHeight);
 		this.fontsheet = InsGlobal.cache.getImage(imagePath, fontWidth, fontHeight);
 		this.currentText = "";
 		this.length = 0;
 		this.maxSize = maxSize;
 		this.lineSpace = 5;
 		this.setText(Text);
 		this.topleft = new InsAlignment(HAlignment.LEFT, VAlignment.TOP);
 	}
 	
 	
 	public void draw(Graphics g)
 	{
 		if(this.singleLine){
 			if(this.frameChar != null)
 			{
 				int refX = this.position.x - (this.align.horizontal.getValue() * (this.getWidth()/ 2));
 				int refY = this.position.y - (this.align.vertical.getValue() * (this.getHeight()/ 2));
 				
 				for(int i = 0; i < this.length; i++)
 				{
 					this.fontsheet.drawFrame(g, this.frameChar[i], refX + (i * this.fontsheet.frameWidth), refY, this.topleft, TransformType.NONE);
 				}
 			}
 		} else {
 			if(this.frameChars != null)
 			{
 				int refX = 0;
 				int refY = 0;
 				int top = this.position.y - (this.align.vertical.getValue() * (this.getHeight()/ 2));
 				int count = 0;
 				
 				for(int i = 0; i < this.lineCount; i++)
 				{
 					count = this.frameChars[i].length;
 					refX = this.position.x - (this.align.horizontal.getValue() * ((count * this.fontsheet.frameWidth)/ 2));
 					refY = top + (i * (this.fontsheet.frameHeight + this.lineSpace));
 					
 					for(int j = 0; j < count; j++){
 						this.fontsheet.drawFrame(g, this.frameChars[i][j], refX + (j * this.fontsheet.frameWidth), refY, this.topleft, TransformType.NONE);
 					}
 				}
 			}
 		}
 		 
 	}
 	
 	/**
 	 * Get the corresponding frame for given char<br>
 	 * Override this to implement your own BitmapFont
 	 */
 	public abstract int getCharFrame(char c);
 	
 	/**
 	 * This is how you set Text. Do not try to modify text without this method.
 	 */
 	public void setText(String Text)
 	{
 		this.singleLine = true;
 		this.length = Math.min(this.maxSize, Text.length());
 		this.lineCount = 1;
 		this.frameChar = new int[this.length];
 		for(int i = 0; i < this.length; i++)
 		{
 			this.frameChar[i] = this.getCharFrame(Text.charAt(i)) % this.fontsheet.frameTotal;
 		}
 		
 		this.currentText = Text;
 	}
 	
 	/**
 	 * This is how you set Text. Do not try to modify text without this method.
 	 */
 	public void setText(String[] Texts)
 	{
 		this.singleLine = false;
 		this.lineCount = Texts.length;
 		this.frameChars = new int[lineCount][];
 		this.length = 0;
 		this.currentTexts = Texts;
 		int count = 0;
 		for(int i = 0; i < lineCount; i++){
 			count = Texts[i].length();
 			this.length = Math.max(length, count);
 			this.frameChars[i] = new int[count];
 			for(int j = 0; j < count; j++)
 			{
 				this.frameChars[i][j] = this.getCharFrame(Texts[i].charAt(j)) % this.fontsheet.frameTotal;
 			}
 		}
 	}
 	
 	/**
 	 * This is how you set Text. Do not try to modify text without this method.
 	 */
 	public void setText(String[] Texts, int LineSpace)
 	{
 		this.setText(Texts);
 		this.lineSpace = LineSpace;
 	}
 	
 	/**
 	 * Wrap Text with given Maximum Width (in pixel)
 	 */
 	public void wrapText(String Text, int maxWidth){
 		if(Text.length() > 0){
 			String[] split = InsUtil.splitString(InsUtil.formatString(Text), " ");
 			int maxChar = maxWidth / this.fontsheet.frameWidth;
 			
 			Vector v = new Vector();
 			String tmp = new String();
 			int count = 0;
 			for(int i = 0; i < split.length; i++){
				if(count < maxChar){
 					tmp += split[i] + " "; 
 					count += split[i].length() + 1;
 				} else {
 					v.addElement(tmp);
 					tmp = split[i] + " ";
 					count = split[i].length() + 1;
 				}
 			}
 			v.addElement(tmp);
 
 			this.setText(InsUtil.vectorToStringArray(v));
 			
 		} else {
 			this.setText(Text);
 		}
 	}
 	
 	public void wrapText(String Text, int maxWidth, int LineSpace){
 		this.wrapText(Text, maxWidth);
 		this.lineSpace = LineSpace;
 	}
 	
 	public int getWidth()
 	{
 		return (this.length * this.fontsheet.frameWidth);
 	}
 	
 	public int getHeight()
 	{
 		if(this.singleLine){
 			return this.fontsheet.frameHeight;
 		} else {
 			return (this.fontsheet.frameHeight * this.lineCount) + (this.lineSpace * (this.lineCount -1));
 		}
 		
 	}
 }
