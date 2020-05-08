 package ru.sokol.client.pages;
 
import ru.sokol.client.login.UserAngine;

import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HtmlContainer;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 
 public class CV extends LayoutContainer {
 	public CV() {
 		super(new FitLayout());
 //		setLayout(new FitLayout());
 		setBorders(false);
 //		setHeaderVisible(false);
 //		setAutoHeight(true);
 		HtmlContainer title = new HtmlContainer();
		title.setHtml("<h1>Работает, " + UserAngine.getNickName() + "</h1>");
 		this.add(title);
 		syncSize();
 	}
 }
