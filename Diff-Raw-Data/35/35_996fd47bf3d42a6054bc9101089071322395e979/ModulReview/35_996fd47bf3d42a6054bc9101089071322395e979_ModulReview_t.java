 ﻿package gui;
 import java.sql.PreparedStatement;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 
 import objects.ModulKu;
 
 import com.vaadin.data.util.*;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.BaseTheme;
 import com.vaadin.ui.themes.Runo;
 
 public class ModulReview extends Startseite implements Button.ClickListener{
 	String savestr;	
	Window modEdCr, errW;
 	public ListSelect module; //login
 	private AbsoluteLayout mainLayout;
 	private Button create, aendern;
 	private LinkedList<ModulKu> liste;
 	private Label label;
 	
 	public ModulReview(LinkedList<ModulKu> idiot){
 		Window test = starta.getWindow("Modulbearbeiten");
 		if(test != null){
 			starta.removeWindow(test);	
 		}
 		
 		liste = idiot;
 		modEdCr = new Window("");
 		modEdCr.setName("Modulbearbeiten");
 		starta.addWindow(modEdCr);
 	
 		module = openModulList(idiot);
 		buildMainLayout();
 		modEdCr.setContent(mainLayout);
 		
 		/*create = new Button("erstellen");
 		aendern = new Button("Ändern");*/				
 		 
 		
 		/*modEdCr.addComponent(module);
 		modEdCr.addComponent(create);
 		modEdCr.addComponent(aendern);*/
 		
 		Window old = starta.getWindow("Startseite");
 		old.open(new ExternalResource(modEdCr.getURL()));
 	
 		
 		
 	}
 	
 	//ButtonListener
 	public void buttonClick(Button.ClickEvent event){
 	
 		if(event.getButton() == create){
 			int modul = 0;
			contD.ändernModul(modul,this);
 			}
 			
 		if(event.getButton()== logout){
 		       starta.getMainWindow().getApplication().close();				
 		}
 		
 		if(event.getButton() == aendern){
 			String tmp = module.getValue().toString();
 			int modul =0;
 			for(int i=0; i < liste.size(); i++){
 				
 				if(tmp.equals(liste.get(i).gettitle())){
 					modul = liste.get(i).getid();
 					break;
 				}
 			}
			//System.out.println(modul);
			contD.ändernModul(modul,this);
 		}	
 	}
 	
 	//erstellt Listselect-Element mit Modulliste
 	public ListSelect openModulList(LinkedList<ModulKu> idiot) {
 	
 		ListSelect test = new ListSelect();
 	
 		for(int i=0; i < idiot.size(); i++){
 			test.addItem(idiot.get(i).gettitle());			
 		}
 		
 		
 	
 		test.setNullSelectionAllowed(false);
 		return test;
 	}
 	
 	private AbsoluteLayout buildMainLayout() {
 		// common part: create layout
 		mainLayout = new AbsoluteLayout();
 		mainLayout.setImmediate(false);
 		mainLayout.setWidth("100%");
 		mainLayout.setHeight("100%");
 		mainLayout.setMargin(false);
 		
 		// top-level component properties
 		mainLayout.setWidth("100.0%");
 		mainLayout.setHeight("100.0%");
 		
 		// label
 		label = new Label();
 		label.setImmediate(false);
 		label.setWidth("-1px");
 		label.setHeight("-1px");
 		label.setValue("Modul ändern oder bearbeiten");
 		label.setStyleName(Runo.LABEL_H1);
 		mainLayout.addComponent(label, "top:25.0%;left:35.0%;");
 		
 		// module
 		module.setImmediate(false);
 		module.setWidth("46.0%");
 		module.setHeight("70.0%");
 		mainLayout.addComponent(module, "top:35.0%;left:35.0%;");
 		
 		// create
 		create = new Button();
 		create.setCaption("Modul erstellen");
 		create.setImmediate(true);
 		create.setWidth("25.0%");
 		create.setHeight("-1px");
 		create.addListener(this);		
 		mainLayout.addComponent(create, "top:83.0%;left:35.0%;");
 		
 		// logout
 		logout = new Button();
 		logout.setCaption("logout");
 		logout.setImmediate(true);
 		logout.setWidth("-1px");
 		logout.setHeight("-1px");
 		logout.addListener(this);
 		logout.setStyleName(BaseTheme.BUTTON_LINK);
 		mainLayout.addComponent(logout, "top:93.0%;left:35.0%;");
 		
 		// aendern
 		aendern = new Button();
 		aendern.setCaption("Modul ändern");
 		aendern.setImmediate(false);
 		aendern.setWidth("25.0%");
 		aendern.setHeight("-1px");
 		aendern.addListener(this);
 		mainLayout.addComponent(aendern, "top:88.0%;left:35.0%;");
 		
 		return mainLayout;
 	}
	
	public void displayError(String text) {
		
		errW = new Window("Fehler");

		Label wrong = new Label(text);
		Layout error = new VerticalLayout();
		
		errW.setContent(error);
		errW.addComponent(wrong);
		modEdCr.addWindow(errW);
		errW.setHeight("200px");
		errW.setWidth("200px");
}
 }
 
