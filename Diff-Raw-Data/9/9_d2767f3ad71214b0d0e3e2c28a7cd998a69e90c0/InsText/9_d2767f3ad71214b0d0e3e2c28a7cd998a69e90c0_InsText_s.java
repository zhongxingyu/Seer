 package com.inspedio.entity;
 
 import java.util.Vector;
 
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Graphics;
 
 import com.inspedio.enums.FontSize;
 import com.inspedio.enums.FontStyle;
 import com.inspedio.enums.VAlignment;
 import com.inspedio.system.core.InsCanvas;
 import com.inspedio.system.core.InsGlobal;
 import com.inspedio.system.helper.InsUtil;
 
 /**
  * InsText represent simple Text for Drawing
  */
 public class InsText extends InsBasic{
 
 	protected boolean singleLine;
 	protected String[] texts;
 	protected String text;
 	protected Font font;
 	protected int color;
 	protected int fontHeight;
 	
 	public InsText()
 	{
 		this("");
 	}
 	
 	public InsText(String Text)
 	{
 		this(Text, InsGlobal.middleX, InsGlobal.middleY);
 		
 	}
 	
 	public InsText(String Text, int X, int Y)
 	{
 		super();
 		this.font = InsCanvas.defaultFont;
 		this.color = InsCanvas.COLOR_BLACK;
 		this.visible = true;
 		this.fontHeight = this.font.getHeight();
 		this.setPosition(X, Y);
 		this.setText(Text);
 		this.singleLine = true;
 		this.texts = null;
 		
 	}
 	
 	/**
 	 * Set Single Line Text
 	 */
 	public void setText(String Text)
 	{
 		this.text = Text;
 		this.singleLine = true;
 		this.size.height = this.fontHeight;
 	}
 	
 	/**
 	 * Set MultiLine Texts
 	 */
 	public void setText(String[] Texts)
 	{
 		this.texts = Texts;
 		this.singleLine = false;
 		this.size.height = this.fontHeight * this.texts.length;
 	}
 	
 	/**
 	 * Wrap Text with given Maximum Width (in pixel)
 	 */
 	public void wrapText(String Text, int maxWidth){
 		if(Text.length() > 0){
 			String[] split = InsUtil.splitString(InsUtil.formatString(Text), " ");
 			
 			Vector v = new Vector();
 			String tmp = new String();
 			int count = 0;
 			for(int i = 0; i < split.length; i++){
				if(count < maxWidth){
 					tmp += split[i] + " "; 
 					count += this.font.stringWidth(split[i] + " ");
 				} else {
 					v.addElement(tmp);
 					tmp = split[i] + " "; 
 					count = this.font.stringWidth(split[i] + " ");
 				}
 			}
 			
 			v.addElement(tmp);
 
 			this.setText(InsUtil.vectorToStringArray(v));
 			
 		} else {
 			this.setText(Text);
 		}
 	}
 	
 	public void setFont(int Color, FontSize Size, FontStyle Style){
 		this.font = Font.getFont(Font.FACE_SYSTEM, Style.getValue(), Size.getValue());
 		this.color = Color;
 		this.fontHeight = this.font.getHeight();
 		if(this.singleLine){
 			this.size.height = this.fontHeight;
 		} else {
 			this.size.height = this.fontHeight * this.texts.length;
 		}
 	}
 	
 	public void setColor(int Color){
 		this.color = Color;
 	}
 		
 	public void draw(Graphics g)
 	{
 		if(this.visible)
 		{
 			g.setFont(this.font);
 			g.setColor(this.color);
 			if(this.singleLine){
 				g.drawString(this.text, this.position.x, this.getTop(), VAlignment.TOP.anchor | this.align.horizontal.anchor);
 			} else {
 				for(int i = 0; i < this.texts.length; i++){
 					g.drawString(this.texts[i], this.position.x, this.getTop() + (this.fontHeight * i), VAlignment.TOP.anchor | this.align.horizontal.anchor);
 				}
 			}
 		}
 	}
 }
