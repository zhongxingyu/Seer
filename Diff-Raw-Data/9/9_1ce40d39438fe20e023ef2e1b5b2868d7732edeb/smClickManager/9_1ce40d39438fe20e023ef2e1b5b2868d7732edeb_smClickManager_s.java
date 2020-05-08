 package swarm.client.input;
 
 import swarm.shared.structs.smPoint;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
 import com.google.gwt.user.client.ui.Widget;
 
 public class smClickManager
 {
 	private final smPoint m_touchPoint = new smPoint();
 	
 	public smClickManager()
 	{
 	}
 	
 	public void addClickHandler(Widget widget, final smI_ClickHandler handler)
 	{
 		this.register(handler, widget.getElement());
 		/*
 		widget.addDomHandler(new MouseUpHandler()
 		{
 			@Override
 			public void onMouseUp(MouseUpEvent event)
 			{
 				handler.onClick();
 			}
 			
 		}, MouseUpEvent.getType());
 		
 		widget.addDomHandler(new TouchEndHandler()
 		{
 			@Override
 			public void onTouchEnd(TouchEndEvent event)
 			{
 				event.preventDefault();
 				
 				handler.onClick();
 			}
 			
 		}, TouchEndEvent.getType());
 		
 		/*if( m_isTouchOnlyInterface )
 		{
 			widget.addDomHandler(new TouchEndHandler()
 			{
 				@Override
 				public void onTouchEnd(TouchEndEvent event)
 				{
 					handler.onClick();
 				}
 				
 			}, TouchEndEvent.getType());
 		}
 		else
 		{
 			widget.addDomHandler(new ClickHandler()
 			{
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					handler.onClick();
 				}
 				
 			}, ClickEvent.getType());
 		}*/
 	}
 	
 	private native void register(smI_ClickHandler clickHandler, Element element)
 	/*-{
 
 		function handler(event)
 		{
			//return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() +
    //  target.getOwnerDocument().getScrollTop();
       
 			var clientX, clientY;
 			if( event.type == 'click' )
 			{
 				clientX = event.clientX;
 				clientY = event.clientY;
 			}
 			else
 			{
 				clientX = event.touches[0].clientX;
 				clientY = event.touches[0].clientY;
 			}
 			
 			var domImpl = @com.google.gwt.dom.client.DOMImpl::impl;
 			
 			var absoluteTop = domImpl.@com.google.gwt.dom.client.DOMImpl::getAbsoluteTop(Lcom/google/gwt/dom/client/Element;)(element);
 			var absoluteLeft = domImpl.@com.google.gwt.dom.client.DOMImpl::getAbsoluteLeft(Lcom/google/gwt/dom/client/Element;)(element);
 			var docScrollTop = domImpl.@com.google.gwt.dom.client.DOMImpl::getScrollTop(Lcom/google/gwt/dom/client/Document;)($doc);
 			var docScrollLeft = domImpl.@com.google.gwt.dom.client.DOMImpl::getScrollLeft(Lcom/google/gwt/dom/client/Document;)($doc);
 			
 			var relativeX = clientX - absoluteLeft + element.scrollLeft + docScrollLeft;
 			var relativeY = clientY - absoluteTop + element.scrollTop + docScrollTop;
			console.log(clientY, absoluteTop, element.scrollTop, docScrollTop);
 			
 			clickHandler.@swarm.client.input.smI_ClickHandler::onClick(II)(relativeX, relativeY);
 		}
 		
		var fastButton = new $wnd.google.ui.FastButton(element, handler);
 		
 	}-*/;
 }
