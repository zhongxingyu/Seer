 package com.evervoid.client.ui;
 
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.GraphicManager;
 import com.evervoid.client.graphics.Sizeable;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.client.views.Bounds;
 import com.jme3.font.BitmapText;
 import com.jme3.font.LineWrapMode;
 import com.jme3.font.Rectangle;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class BaseText extends EverNode implements Sizeable
 {
 	private final Transform aBottomLeftOffset;
 	private final ColorRGBA aColor;
 	private final BitmapText aText;
 
 	public BaseText(final String text, final ColorRGBA color)
 	{
 		aColor = color;
 		aText = new BitmapText(GraphicManager.getFont("redensek"));
 		aText.setColor(aColor);
 		attachChild(aText);
 		aBottomLeftOffset = getNewTransform();
 		setText(text);
 	}
 
 	@Override
 	public Vector2f getDimensions()
 	{
 		return new Vector2f(getWidth(), getHeight());
 	}
 
 	@Override
 	public float getHeight()
 	{
 		return aText.getHeight();
 	}
 
 	public float getLineHeight()
 	{
 		return aText.getLineHeight();
 	}
 
 	public int getLines()
 	{
 		return aText.getLineCount();
 	}
 
 	@Override
 	public float getWidth()
 	{
		// Yay we got this bug fixed:
 		// http://jmonkeyengine.org/groups/gui/forum/topic/bitmaptext-getlinewidth-always-returns-0/
 		return aText.getLineWidth();
 	}
 
 	@Override
 	public void setAlpha(final float alpha)
 	{
		// aText.setColor(new ColorRGBA(aColor.r, aColor.g, aColor.b, aColor.a * alpha));
 	}
 
 	public void setColor(final ColorRGBA color)
 	{
 		aText.setColor(color);
 	}
 
 	public void setColor(final int start, final int end, final ColorRGBA color)
 	{
 		aText.setColor(start, end, color);
 	}
 
 	public void setRenderBounds(final Bounds bounds)
 	{
 		aText.setBox(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
 		aText.setLineWrapMode(LineWrapMode.NoWrap);
 		aText.setEllipsisChar('_');
 	}
 
 	public void setText(final String text)
 	{
 		aText.setText(text);
 		if (!text.isEmpty()) {
 			// BitmapTexts are drawn towards the bottom, so we gotta compensate for that
 			aBottomLeftOffset.translate(0, getHeight());
 		}
 	}
 }
