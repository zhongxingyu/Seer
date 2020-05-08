 package com.hexcore.cas.ui;
 
 import java.util.TreeMap;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.ui.Theme.BorderShape;
 import com.hexcore.cas.ui.Theme.ButtonState;
 
 public class TabbedView extends View
 {
 	protected TreeMap<Integer, String>	captions;
 	protected int						hovered = -1;
 	protected boolean active;
 	
 	public TabbedView(Vector2i size)
 	{
 		super(size);
 		captions = new TreeMap<Integer, String>();
 	}
 
 	public TabbedView(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 		captions = new TreeMap<Integer, String>();
 	}
 	
 	@Override
 	public Vector2i getInnerSize()
 	{
 		return super.getInnerSize().subtract(0, window.getTheme().getTabHeight());
 	}
 	
 	@Override
 	public void add(Widget widget)
 	{
 		captions.put(widgets.size(), "Tab");
 		super.add(widget);
 	}	
 	
 	public void add(Widget widget, String name)
 	{
 		captions.put(widgets.size(), name);
 		super.add(widget);
 	}
 	
 	@Override
 	public void remove(Widget widget)
 	{
 		int index = widgets.indexOf(widget);
 		captions.remove(index);
 		widgets.remove(index);
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		if (!visible) return;
 		
 		Vector2i	pos = this.position.add(position);
 		int			tabHeight = window.getTheme().getTabHeight();
 		
 		// Render border and window
 		Vector2i innerSize = getInnerSize();
 		Vector2i innerPos = pos.add(0, window.getTheme().getTabHeight());
 				
 		if (background == null) 
 			window.getTheme().renderTabInside(gl, innerPos.subtract(0, tabHeight / 2), innerSize.add(0, tabHeight / 2));
 		else
 			window.renderRectangle(gl, innerPos.subtract(0, tabHeight / 2), innerSize.add(0, tabHeight / 2), 0, background);
 		
 		window.setClipping(gl, innerPos, innerSize);
 		Widget contents = getWidget();
 		if (contents != null) contents.render(gl, innerPos);
 		window.resetView(gl);
 		
 		// Render tabs
 		int	tabsWidth = 0;
 		for (int i = 0; i < widgets.size(); i++) tabsWidth += window.getTheme().getTabSize(captions.get(i)).x;
 		
 		int	x = (getWidth() - tabsWidth) / 2;
 		for (int i = 0; i < widgets.size(); i++)
 		{
 			BorderShape sides = new BorderShape();
 			if (i == 0) sides.add(BorderShape.LEFT);
 			if (i == widgets.size() - 1) sides.add(BorderShape.RIGHT);
 			
 			ButtonState state = ButtonState.NORMAL;
 			if (i == hovered) state = active ? ButtonState.ACTIVE : ButtonState.HOVER;
 			if (i == getIndex()) state = ButtonState.SELECTED;
 			
 			
 			String caption = captions.get(i);
 			Vector2i tabSize = window.getTheme().getTabSize(caption);
 			window.getTheme().renderTab(gl, pos.add(x, 0), caption, state, sides);
 			x += tabSize.x;
 		}
 	}
 
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = false;
 		
 		if ((event.type == Event.Type.MOUSE_MOTION) || (event.type == Event.Type.MOUSE_CLICK))
 		{
 			if ((event.position.x >= position.x) && (event.position.y >= position.y) && (event.position.y <= position.y + window.getTheme().getTabHeight()))
 			{
 				int	tabsWidth = 0;
 				for (int i = 0; i < widgets.size(); i++) tabsWidth += window.getTheme().getTabSize(captions.get(i)).x;
 				
 				int	x = position.x + (getWidth() - tabsWidth) / 2;
 				for (int i = 0; i < widgets.size(); i++)
 				{
 					Vector2i tabSize = window.getTheme().getTabSize(captions.get(i));
 					x += tabSize.x;
 					if (event.position.x <= x)
 					{
 						hovered = i;
 						break;
 					}
 				}
 				
 				handled = true;
 			}
 			
 			if (event.type == Event.Type.MOUSE_CLICK)
 			{
 				boolean wasActive = active;
 				active = event.pressed;
 		
				if (wasActive && !active && mouseover) setIndex(hovered);
 			}
 		}
 		
 		if (!handled && super.handleEvent(event, position)) 
 			handled = true;
 		
 		return handled;
 	}
 }
