 /*
  * Copyright 2012 Sebastien Zurfluh
  * 
  * This file is part of "Parcours".
  * 
  * "Parcours" is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * "Parcours" is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with "Parcours".  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.sebastienzurfluh.client.view;
 
 import java.util.Stack;
 
 import ch.sebastienzurfluh.client.control.eventbus.Event;
 import ch.sebastienzurfluh.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.client.control.eventbus.Event.EventType;
 import ch.sebastienzurfluh.client.control.eventbus.events.DataType;
 import ch.sebastienzurfluh.client.control.eventbus.events.PageChangeEvent;
 import ch.sebastienzurfluh.client.control.eventbus.EventBusListener;
 import ch.sebastienzurfluh.client.model.Model;
 import ch.sebastienzurfluh.client.model.structure.Data;
 
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 /**
  * Hierarchy widget gives a way to go up in the booklet's hierarchy. For instance, when looking at a page, the user
  * can see "Booklet > Chapter > Page", and choose to go back to the chapter or the booklet.
  *
  * @author Sebastien Zurfluh
  *
  */
 public class HierarchyWidget extends SimplePanel implements EventBusListener {
 	private Data currentPage;
 	private EventBus eventBus;
 	
	private Label separator = new Label(" > ");
 	private Model model;
 	
 	public HierarchyWidget(EventBus eventBus, Model model) {
 		this.model = model;
 		this.eventBus = eventBus;
 		
 		this.setStyleName("hierarchyWidget");
 		
 		eventBus.addListener(this);
 	}
 
 	@Override
 	public EventType getEventType() {
 		return EventType.PAGE_CHANGE_EVENT;
 	}
 
 	@Override
 	public void notify(Event e) {
 		if(e instanceof PageChangeEvent) {
 			PageChangeEvent pageChangeEvent = (PageChangeEvent) e;
 			
 			if(pageChangeEvent.getPageType() == DataType.SUPER) {
 				this.setVisible(false);
 			} else {
 				this.setVisible(true);
 			}
 			
 			currentPage = pageChangeEvent.getData();
 			int depth = 0;
 			FlowPanel historyPanel = new FlowPanel();
 			
 			
 			switch (pageChangeEvent.getPageType()) {
 			case PAGE:
 				depth++;
 			case CHAPTER:
 				depth++;
 			case BOOKLET:
 				depth++;
 			case SUPER:
 			default:
 			}
 			
 			Stack<TextLink> stack = new Stack<TextLink>();
 			
 			while(depth > 0) {
 				stack.push(new TextLink(eventBus, currentPage.getReference(), currentPage.getPageTitle()));
 				currentPage = model.getParentOf(currentPage.getReference());
 				depth--;
 			}
 			
 			while(!stack.isEmpty()) {
				historyPanel.add(separator);
 				historyPanel.add(stack.pop());
 			}
 			if (historyPanel.getWidgetCount() == 0) {
 				this.setVisible(false);
 			} else {
 				this.setVisible(true);
 				historyPanel.remove(0);
 			}
 			
 			setWidget(historyPanel);
 		}
 	}
 
 }
