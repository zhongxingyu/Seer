 package eu.ist.fears.client;
 
 
 import com.google.gwt.core.client.EntryPoint;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import eu.ist.fears.client.communication.Communication;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Fears implements EntryPoint, HistoryListener  {
 
 
 
 	static DockPanel main;
 	static VerticalPanel contentBox; 
 	static VerticalPanel menu;
 	static HorizontalPanel topBox; 
 
 
 
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 
 		RootPanel.get().setStyleName("body");
 
 		main= new DockPanel();
 		contentBox = new VerticalPanel();
 		VerticalPanel menuBox = new VerticalPanel();
 		topBox = new HorizontalPanel();
 		
 		menu = new VerticalPanel();
 
 		RootPanel.get().add(main);
 		main.add(topBox, DockPanel.NORTH);
 		main.add(contentBox, DockPanel.CENTER);
 		main.add(menuBox,DockPanel.WEST);
 
 		main.setStyleName("main");
 		topBox.setStyleName("top");
 		contentBox.setStyleName("content");
 		menuBox.setStyleName("menuBox");
 
 		topBox.add(new Label("Username: ..."));
 		topBox.add(new Label("N de Votos Restante: ..."));
 
 		menuBox.add(menu);
 		menu.setStyleName("menu");
 		updateMenu("");
 
 		History.addHistoryListener(this);
 
 		/*Mostrar o #  actual */
 		String initToken = History.getToken();
 		if (initToken.length() > 0) {
 			onHistoryChanged(initToken);
 		}else //Pagina Default:
 			viewListProjects();
 	}	
 	
 	public static void updateMenu(String project){
 		menu.clear();
 		if(project==""){
 			menu.add(new Hyperlink("Ver Lista de Projectos", "listProjects"));
 			menu.add(new HTML("<br>"));
 			menu.add(new HTML("<br>"));
 			menu.add(new Hyperlink("Sugestoes Default","defaultFeatures"));
 
 		}else{
 			menu.add(new Hyperlink("Ver Lista de Projectos", "listProjects"));
 			menu.add(new HTML("<br>"));
 			menu.add(new HTML("<b>" + project + "</b>"));
 			menu.add(new Hyperlink("     -  Ver Sugestoes","Project" + project + "?" + "listFeatures"));
 			menu.add(new Hyperlink("     -  Adicionar Sugestao","Project" + project + "?" + "addFeature"));
 			menu.add(new HTML("<br>"));
 			menu.add(new HTML("<br>"));
 			menu.add(new Hyperlink("Sugestoes Default","defaultFeatures"));
 		}
 		
 	}
 
 	public static void listFeatures(String projectName){
 		ListFeaturesWidget features = new ListFeaturesWidget(projectName);
 		contentBox.clear();
 		//RootPanel.get().setTitle(projectName);
 		updateMenu(projectName);
 		features.update();
 		contentBox.add(features);
 	}
 
 	public static void addFeature(String projectName){
 		contentBox.clear();
 		//RootPanel.get().setTitle("Adicionar Sugestao a "+  projectName);
 		updateMenu(projectName);
 		contentBox.add(new CreateFeatureWidget(projectName));
 
 	}
 
 	public static void viewFeature(String projectName, String featureName){
 		contentBox.clear();
 		//RootPanel.get().setTitle(featureName);
 		updateMenu(projectName);
 		contentBox.add(new DisplayFeatureDetailedWidget(projectName, featureName));
 	}
 
 	public static void viewListProjects(){
 		ListProjectsWidget projects = new ListProjectsWidget();
 		contentBox.clear();
 		//RootPanel.get().setTitle("Projectos");
 		updateMenu("");
 		projects.update();	
 		contentBox.add(projects);
 		
 	}
 
 
 
 	public void onHistoryChanged(String historyToken) {
 		// This method is called whenever the application's history changes. Set
 		// the label to reflect the current history token.
 		if("defaultFeatures".equals(historyToken)){
 			test();
 			History.newItem("Project" + "Fenix");
 		}else if(historyToken.startsWith("listProjects")){
 			viewListProjects();
 		}else if(historyToken.startsWith("Project")){
 			projectParse(historyToken.substring("Project".length()));	
 		}
 
 	}
 
 	private void projectParse(String string){
 		int parseAt =  string.indexOf('?');
 		int parseB =  string.indexOf("%3F");
 		String projectName;
 		String parse;
 
 		//Estamos no Caso: #ProjectXPTO  
 		if(parseAt==-1 && parseB==-1 ){
 			projectName=string;
 			listFeatures(projectName);
 			return;	
 		}
 
 		if(parseAt!=-1){
 			projectName = string.substring(0,parseAt);
 			parse = string.substring(parseAt+1);
 			
 		}else{
			projectName = string.substring(0,parseAt);
			parse = string.substring(parseB+2);
 		}
 		
 		if("listFeatures".equals(parse)){
 			listFeatures(projectName);	
 		}else if("addFeature".equals(parse)){
 			addFeature(projectName);
 		}else if(parse.startsWith("viewFeature")){
 			viewFeature(projectName, parse.substring("viewFeature".length()));
 		}
 
 	}
 
 	public void test(){
 
 		Communication _com= new Communication("service");
 		_com.addProject("Fenix", "sugestoes para o fenix", testCB);
 		String s;
 		
 		_com.addFeature("Fenix","Sugestao 1", "blalha vhajbv ahsha fgf dg  fg d gfg fghf fhd", testCB);
 		_com.addFeature("Fenix", "Login", "vh   ajbv ah  sha blal   fhdh fgh gh  gfh f  ha ", testCB);
 		_com.addFeature("Fenix", "Sug3", ".. .. .. ...   .... hgfghf  hhfgf fh ghf", testCB);
 		_com.addFeature("Fenix", "4", "dfs mmmmfds  fdsfds fsd fds fds fdsff", testCB);
 	}
 
 	AsyncCallback testCB = new AsyncCallback() {
 		public void onSuccess(Object result){ 
 		}
 
 		public void onFailure(Throwable caught) {
 		}
 	};
 
 }
