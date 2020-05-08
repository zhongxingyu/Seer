 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ru.natty.web.client.iwidget;
 
import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  *
  * @author necto
  */
 public class IPlayer extends IWidget
 {
 	private Label url;
	private DecoratorPanel panel;
 
 	public IPlayer (Integer id)
 	{
		super(id, new DecoratorPanel());
		panel = (DecoratorPanel)getWidget();
		url = new Label();
		panel.setWidget(url);
 	}
 
 	public IPlayer (Integer id, String nurl)
 	{
		super(id, new DecoratorPanel());
		panel = (DecoratorPanel)getWidget();
		url = new Label();
		panel.setWidget(url);
 		this.url.setText(nurl);
 	}
 
 	public void setNewUrl (String nUrl)
 	{
		url.setText("now playing: " + nUrl);
 	}
 
 }
