 package com.gofetch.beans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import org.primefaces.event.NodeSelectEvent;
 
 import com.charts.GoogleChartsWrapper;
 import com.gofetch.models.URLNodeImpl;
 import com.gofetch.entities.Link;
 import com.gofetch.entities.LinkDBService;
 import com.gofetch.entities.MiscSocialData;
 import com.gofetch.entities.MiscSocialDataDBService;
 import com.gofetch.entities.URL;
 import com.gofetch.entities.URLAndLinkData;
 import com.gofetch.entities.URLDBService;
 import com.gofetch.entities.User;
 import com.gofetch.entities.UserDBService;
 
 /* bean that backs dashboard.xhtml
  * 
  */
 @ManagedBean
 @ViewScoped
 public class FullScreenDashboardBean implements Serializable{
 
 
 	//////////////
 	//
 	private static final long serialVersionUID = 1L;
 	
 	private static Logger log = Logger.getLogger(FullScreenDashboardBean.class
 			.getName());
 	//
 	///////////////
 	
 	
 	///////////////
 	// Entities:
 	
 	private List <User> clientsFromDB = null; //new ArrayList<User>(); // pulls all clients from the DB. may want to break this functionality out...
 	private List <User> clientsSelectedByUser = new ArrayList<User>();
 	private List <URL> sourceURLs = new ArrayList<URL>();
 	private List <Link> links = new ArrayList<Link>();
 	private List<URLAndLinkData> urlAndLinks = new ArrayList<URLAndLinkData>();
 	private List <MiscSocialData> socialData = new ArrayList<MiscSocialData>();
 	//private List<URL> targetURLs;// = new ArrayList<URL>(); // - initialize after we make call to DB.
 	  
 	//
 	///////////////
 	
 	//////////////////
 	// Helper objects
 	
 	private GoogleChartsWrapper googleSocialChart = new GoogleChartsWrapper();
 	private GoogleChartsWrapper googleBackLinksChart = new GoogleChartsWrapper();
 	
 //	private String chartData = "[" 
 //            + "['Year', 'Facebook', 'Twitter'],"
 //            + "['2004',  1000,      400],"
 //            + "['2005',  1170,      460],"
 //            + "['2006',  660,       1120],"
 //            + "['2007',  1030,      540]"
 //           + "]";
 	
 	
 	//////////////////////////////////////////////////////////
 	// Data structures for drop downs, menus, etc.
 	
 	//1: Clients:
 	private LinkedHashMap<String, Integer> clientsMenu = null; // contents of the client's menu
 	private List<String> selectedClients; // clients that the user has selected
 	private String selectedClientsKey;	  // clients that the user has selected
 	private List <Integer> clientsIDs;	  // id's back from the DB id???
 	
 	
 	///////
 	// Flags:
 	
 	//TODO: delete when tested - move all these flags to local JS file.
 	//private boolean showTargetURLs; // if true - client's target URLs are visible as accordian stack /tree.
 	// of the two fields below: only one can be true at a time: acts as a switch showing relevant content of dashboard
 	private boolean showClientsDashboard; // if true - client-focused version of dashboard is visible
 	private boolean showContactsDashboard; // if true - contacts-focused version of dashboard is visible
 	
 	private boolean showTree; // may need to keep this one - as not too sure how to deal with empty tree- it shows up 
 								// as an ugly bar on screen
 	
 	// flags end
 	///////////////////////////////////////////////////////
 	
 	
 	
 			 
 //	public String getChartData() {
 //		return chartData;
 //		//return googleChart.getChartDataString();
 //	}
 //
 //	public void setChartData(String chartData) {
 //		this.chartData = chartData;
 //	}
 	
 	
 	/*
 	 * We want to get all backlink data for the table and chart and all historical social data for table...
 	 */
 	public void onNodeSelected(NodeSelectEvent event){
 		
 		String urlAddress = event.getTreeNode().getData().toString();
 		
 		Integer urlID; 
 			
 		URLDBService urlDB = new URLDBService();
 		LinkDBService linkDB = new LinkDBService();
 		MiscSocialDataDBService socialDB = new MiscSocialDataDBService();
 		
 		urlID= urlDB.getURLIDFromAddress(urlAddress);
 		
 		//1: get back link data:
 		//TODO: struggling to turn this into  a join...
 		sourceURLs = urlDB.getBackLinkURLs(urlID);
 		links = linkDB.getAllLinks(urlID);
 		
 		socialData = socialDB.getAllSocialData(urlID);
 		
		googleSocialChart.parseSocialData(socialData);
 		googleBackLinksChart.parseBackLinkData(urlAddress, links, sourceURLs);
 		
 		
 		
 		//urlAndLinks = urlDB.getURLAndLinkData(urlID);
 		
 
 	}
 
 	 
 	//
 	/////////
 
 	private Integer urlEntry;
 	
 	
 	//////
 	// client's target and associated competitors' URLs:
 	
 	private URLTreeBean urlTreeBean = new URLTreeBean();
 		
 	public FullScreenDashboardBean(){
 		
 		showTree = false;
 		
 	/////
 		// Testing:
 		googleSocialChart.setSocialDataString("[" 
 	            + "['Year', 'Facebook', 'Twitter'],"
 	            + "['2004',  1000,      400],"
 	            + "['2005',  1170,      460],"
 	            + "['2006',  660,       1120],"
 	            + "['2007',  1030,      540]"
 	           + "]");
 //		googleChart.setChartDataString("[" 
 //		                          + "['Year', 'Facebook', 'Twitter'],"
 //		                          + "['2004',  1000,      400],"
 //		                          + "['2005',  1170,      460],"
 //		                          + "['2006',  660,       1120],"
 //		                          + "['2007',  1030,      540]"
 //		                         + "]");
 		
 		//TODO: delete when sure...
 		URL backLinkURL = new URL();
 		
 		//Need to get this somehow...
 		//link.setAnchor_text("AnchorText123");
 		
 		backLinkURL.setDomain_authority(23);
 		backLinkURL.setUrl_address("www.hotmail.com/yeah");
 
 		
 		sourceURLs.add(backLinkURL);
 		
 	}
 	
 
 	
 	////////
 	// action controllers:
 	
 	
 	public void clientSelectionChange(){
 		
 		List <Integer> clientsIDs = new ArrayList<Integer>();
 		
 		URLDBService urlDBService = new URLDBService();
 		
 		List<URL> targetURLs;
 		
 		//////
 		//
 		
 		// nothing is selected in the drop down menu...
 		if (selectedClients.isEmpty()) {
 			
 			urlTreeBean.clearTree();
 			showTree = false;
 			return;
 		}
 		
 		// run through all selected clients, getting their id's as integers
 		
 		for(String clientID : selectedClients){
 		
 			clientsIDs.add(Integer.valueOf(clientID));
 			
 		}
 		
 		//TODO: might be a more optimised way than just clearing
 		clientsSelectedByUser.clear();
 		urlTreeBean.clearTree();
 		
 		Integer clientFromDBID, clientIDFromUserSel;
 		
 		// loop through the clients getting adding them to list if selected by user
 		for(int a = 0; a < clientsFromDB.size(); a++){
 			
 			for(int b = 0; b < clientsIDs.size(); b++){
 				
 				clientFromDBID = clientsFromDB.get(a).getId();
 				clientIDFromUserSel = clientsIDs.get(b);
 				
 				if(clientFromDBID.equals(clientIDFromUserSel)){
 					
 					clientsSelectedByUser.add(clientsFromDB.get(a));
 					
					// and add to the tree:
 				
 					URLNodeImpl newNode = new URLNodeImpl(clientsFromDB.get(a).getDisplayed_name(), urlTreeBean.getModel());
 					
 					// then need to get that client's target URLs /  competitor URLs and present as children under that client:
 					targetURLs = urlDBService.getClientsTargetURLs(clientsFromDB.get(a).getId(), true);
 					
 					for(int c = 0; c < targetURLs.size(); c++){
 						
 						new URLNodeImpl(targetURLs.get(c).getUrl_address(), newNode);
 						
 					}
 				}
 			}
 
 		}
 		
 		showTree = true;
 		
 		//testing email here...
 		//EmailWrapper.SendEmail("Alan@propellernet.co.uk", "Alan Donohoe", "robot@gofetchdata.appspotmail.com", "GoFetch Robot", "If you get this message, then our robot in the cloud has learned how to email.", "Hello From The GoFetch Robot");
 	}
 	
 	
 	public void switchToClientsDashboard(){
 		// for now this is all handled by JS
 		showClientsDashboard = true; 
 		showContactsDashboard = false;
 		
 	}	
 	
 	public void switchToContactsDashboard(){
 		// for now this is all handled by JS
 		showClientsDashboard = false; 
 		showContactsDashboard = true;
 		
 	}
 	
 	// action controllers - end
 	////////
 	
 	
 	
 	////////
 	// helper methods:
 	
 //	private Integer getSelectedClientInteger() {
 //
 //		String clientSelectedString = String.valueOf(selectedClients.get(0));
 //		Integer clientSelected = Integer.valueOf(clientSelectedString);
 //
 //		return clientSelected;
 //
 //	}
 	
 	//
 	///////////
 
 	@PostConstruct
 	public void init(){
 		
 		//showTargetURLs = true;
 		
 		showClientsDashboard = true; // start with client-facing dashboard first... - can later set this in session object...
 		showContactsDashboard = false;
 		
 		clientsMenu = new LinkedHashMap<String, Integer>();
 		
 		UserDBService usersDB = new UserDBService();
 		
 		clientsFromDB = usersDB.getAllClients();
 		
 		for (User user : clientsFromDB) {
 			clientsMenu.put(user.getDisplayed_name(), user.getId());
 		}
 		
 		
 
 
 //		DefaultTreeNode documents = new DefaultTreeNode("Documents", root); 
 //		DefaultTreeNode pictures = new DefaultTreeNode("Pictures", root); 
 //		DefaultTreeNode music = new DefaultTreeNode("Music", root);
 //		DefaultTreeNode work = new DefaultTreeNode("Work", documents); 
 //		DefaultTreeNode primefaces = new DefaultTreeNode("PrimeFaces", documents);
 //		
 //		//Documents 
 //		DefaultTreeNode expenses = new DefaultTreeNode("document", "Expenses.doc", work); 
 //		DefaultTreeNode resume = new DefaultTreeNode("document", "Resume.doc", work); 
 //		DefaultTreeNode refdoc = new DefaultTreeNode("document", "RefDoc.pages", primefaces);
 //		
 //		//Pictures 
 //		DefaultTreeNode barca = new DefaultTreeNode("picture", "barcelona.jpg", pictures); 
 //		DefaultTreeNode primelogo = new DefaultTreeNode("picture", "logo.jpg", pictures); 
 //		DefaultTreeNode optimus = new DefaultTreeNode("picture", "optimus.png", pictures);
 //		
 //		//Music 
 //		DefaultTreeNode turkish = new DefaultTreeNode("Turkish", music);
 //		DefaultTreeNode cemKaraca = new DefaultTreeNode("Cem Karaca", turkish);
 //		DefaultTreeNode erkinKoray = new DefaultTreeNode("Erkin Koray", turkish); 
 //		DefaultTreeNode mogollar = new DefaultTreeNode("Mogollar", turkish);
 //		DefaultTreeNode nemalacak = new DefaultTreeNode("mp3", "Nem Alacak Felek Benim", cemKaraca); 
 //		DefaultTreeNode resimdeki = new DefaultTreeNode("mp3", "Resimdeki Goz Yaslari", cemKaraca);
 //		DefaultTreeNode copculer = new DefaultTreeNode("mp3", "Copculer", erkinKoray); 
 //		DefaultTreeNode oylebirgecer = new DefaultTreeNode("mp3", "Oyle Bir Gecer", erkinKoray);
 //		DefaultTreeNode toprakana = new DefaultTreeNode("mp3", "Toprak Ana", mogollar); 
 //		DefaultTreeNode bisiyapmali = new DefaultTreeNode("mp3", "Bisi Yapmali", mogollar);	
 		//
 			////////
 	}
 	
 		
 
 	
 	
 
 	/////
 	// getters and setters:
 	
 	public Integer getUrlEntry() {
 		return urlEntry;
 	}
 
 	public void setUrlEntry(Integer urlEntry) {
 		this.urlEntry = urlEntry;
 	}
 	
 //	public List<URL> getTargetURLs() {
 //		return targetURLs;
 //	}
 //
 //	public void setTargetURLs(List<URL> targetURLs) {
 //		this.targetURLs = targetURLs;
 //	}
 	
 //	public boolean isShowTargetURLs() {
 //		return showTargetURLs;
 //	}
 //	public void setShowTargetURLs(boolean showTargetURLs) {
 //		this.showTargetURLs = showTargetURLs;
 //	}
 
 	public List<User> getClientsFromDB() {
 		return clientsFromDB;
 	}
 	public void setClientsFromDB(List<User> clientsFromDB) {
 		this.clientsFromDB = clientsFromDB;
 	}
 	public LinkedHashMap<String, Integer> getClientsMenu() {
 		return clientsMenu;
 	}
 	public void setClientsMenu(LinkedHashMap<String, Integer> clientsMenu) {
 		this.clientsMenu = clientsMenu;
 	}
 	
 	public List<String> getSelectedClients() {
 		return selectedClients;
 	}
 	public void setSelectedClients(List<String> selectedClients) {
 		this.selectedClients = selectedClients;
 	}
 	public String getSelectedClientsKey() {
 		return selectedClientsKey;
 	}
 	public void setSelectedClientsKey(String selectedClientsKey) {
 		this.selectedClientsKey = selectedClientsKey;
 	}
 
 
 	public boolean isShowClientsDashboard() {
 		return showClientsDashboard;
 	}
 
 
 	/**
 	 * 
 	 * @param showClientDashboard sets the panel of the dashboard to show clients panel
 	 * if true, then showContactsDashboard is automatically set to false and the clients panel is made visible
 	 * if false, then showContactsDashboard is automatically set to true and the clients panel is hidden
 	 */
 	//TODO: should these be private? - and can only change views by calling: showContactsDashboard or: showClientsDashboard
 	public void setShowClientsDashboard(boolean showClientDashboard) {
 		this.showClientsDashboard = showClientDashboard;
 		
 //		// set other flag
 //		if(showClientDashboard)
 //			showContactsDashboard = false;
 //		else
 //			showContactsDashboard = true;
 	}
 
 
 	public boolean isShowContactsDashboard() {
 		return showContactsDashboard;
 	}
 
 
 	/**
 	 * 
 	 * @param showContactsDashboard sets the panel of the dashboard to show contacts panel
 	 * if true, then showClientDashboard is automatically set to false and the contacts panel is made visible
 	 * if false, then showClientDashboard is automatically set to true and the contacts panel is hidden
 	 */
 	public void setShowContactsDashboard(boolean showContactsDashboard) {
 		this.showContactsDashboard = showContactsDashboard;
 		
 		// set other flag
 //		if(showContactsDashboard)
 //			showClientsDashboard = false;
 //		else
 //			showClientsDashboard = true;
 		
 	}
 	
 	
 	
 	// getters & setters end....
 	///////
 
 
 	//////////
 	// possible junk - delete after testing:
 
 //	public URLTreeBean getUrlTreeBean() {
 //		return urlTreeBean;
 //	}
 //
 //	public void setUrlTreeBean(URLTreeBean urlTreeBean) {
 //		this.urlTreeBean = urlTreeBean;
 //	}	
 //	
 	
 //	public DefaultTreeNode getUrlTree() {
 //		return urlTree;
 //	}
 //
 //
 //	public void setUrlTree(DefaultTreeNode urlTree) {
 //		this.urlTree = urlTree;
 //	}
 	
 	
 	public URLTreeBean geturlTreeBean() {
 		return urlTreeBean;
 	}
 
 
 
 	public boolean isShowTree() {
 		return showTree;
 	}
 
 
 
 	public void setShowTree(boolean showTree) {
 		this.showTree = showTree;
 	}
 
 	public GoogleChartsWrapper getGoogleSocialChart() {
 		return googleSocialChart;
 	}
 
 	public void setGoogleSocialChart(GoogleChartsWrapper googleChart) {
 		this.googleSocialChart = googleChart;
 	}
 	
 	
 }
