 package com.hexcore.cas.ui;
 
 import java.util.ArrayList;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Recti;
 import com.hexcore.cas.math.Vector2i;
 
 public class DropDownBox extends Widget
 {
 	private int					selected = -1;
 	private int					mouseoverItem = -1;
 	private ArrayList<String>	items;
 	
 	public DropDownBox(Vector2i size)
 	{
 		super(size);
 		items = new ArrayList<String>();
 	}
 
 	public DropDownBox(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 		items = new ArrayList<String>();
 	}
 	
 	public boolean	canGetFocus() {return true;}
 	
 	public void		addItem(String text) {items.add(text);}
 	
 	public void 	setSelected(int index) {selected = index;}
 	public int 		getSelected() {return selected;}
 	
 	public String	getSelectedText() 
 	{
 		if ((selected < 0) || (selected >= items.size())) return "";
 		return items.get(selected);
 	}
 
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);		
 		window.getTheme().renderDropDownBox(gl, pos, size, getSelectedText(), focused);
 	}
 	
 	@Override
 	public void renderExtras(GL gl, Vector2i position)
 	{
 		window.getTheme().renderDropDownBoxList(gl, position, size, items, selected, mouseoverItem);
 	}
 
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = super.handleEvent(event, position);
 		
 		if ((event.type == Event.Type.MOUSE_CLICK) && event.pressed)
 		{
 			window.toggleFocus(this);
 			handled = true;
 		}
 		
 		return handled;
 	}
 	
 	@Override
 	public boolean handleEventExtras(Event event, Vector2i position)
 	{
 		boolean handled = super.handleEvent(event, position);
 		
 		if ((event.type == Event.Type.MOUSE_CLICK) && event.pressed)
 		{
 			if (focused && (mouseoverItem >= 0))
 			{
 				selected = mouseoverItem;
 				
 				Event changeEvent = new Event(Event.Type.CHANGE);
 				changeEvent.target = this;
 				window.sendWindowEvent(changeEvent);
 			}
			
			window.toggleFocus(this);
			handled = true;
 		}
 		else if (event.type == Event.Type.MOUSE_MOTION)
 		{
 			mouseoverItem = -1;
 			for (int i = 0; i < items.size(); i++)
 			{
 				Recti rect = window.getTheme().getDropDownBoxItemRect(position, size, i);
 				
 				if ((rect.position.x <= event.position.x) && (rect.position.y <= event.position.y) &&
 						(rect.position.x + rect.size.x >= event.position.x) && (rect.position.y + rect.size.y >= event.position.y))
 					mouseoverItem = i;
 			}
 		}
 		
 		return handled;
 	}
 }
