 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ru.natty.web.client.iwidget;
 
 import com.google.gwt.user.client.ui.Label;
 
 /**
  *
  * @author necto
  */
 public class IPlayer extends IWidget
 {
 	private Label url;
 
 	public IPlayer (Integer id)
 	{
		super(id, new Label());
		url = (Label)getWidget();
 	}
 
 	public IPlayer (Integer id, String nurl)
 	{
		super(id, new Label());
		this.url = (Label)getWidget();
 		this.url.setText(nurl);
 	}
 
 	public void setNewUrl (String nUrl)
 	{
		url.setText(nUrl);
 	}
 
 }
