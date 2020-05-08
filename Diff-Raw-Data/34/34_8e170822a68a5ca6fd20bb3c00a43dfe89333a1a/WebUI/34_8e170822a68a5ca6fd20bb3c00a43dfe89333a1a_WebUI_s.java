 package com.bleulace.web;
 
 import java.util.Map.Entry;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.context.ApplicationContext;
 
 import com.porotype.iconfont.FontAwesome;
 import com.vaadin.annotations.PreserveOnRefresh;
 import com.vaadin.annotations.Push;
 import com.vaadin.annotations.Widgetset;
 import com.vaadin.navigator.Navigator;
 import com.vaadin.navigator.View;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.UI;
 
 //@formatter:off
 /**
  * @founder Hamid Bundu
  * @founder Arleigh Dickerson
  * 
  * @author Arleigh Dickerson
  */
 //@formatter:on
 @Push
@Configurable
 @PreserveOnRefresh
 @Widgetset("com.vaadin.DefaultWidgetSet")
 public class WebUI extends UI
 {
 	@Autowired
 	private ApplicationContext ctx;
 
 	@Override
 	protected void init(VaadinRequest request)
 	{
 		Navigator nav = new Navigator(this, this);
 		for (Entry<String, View> entry : ctx.getBeansOfType(View.class)
 				.entrySet())
 		{
 			nav.addView(entry.getKey(), entry.getValue());
 		}
 		setNavigator(nav);
 		FontAwesome.load();
 	}
 }
