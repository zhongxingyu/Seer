 package de.unihamburg.zbh.fishoracle.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.gwtext.client.widgets.BoxComponent;
 import com.gwtext.client.widgets.HTMLPanel;
 import com.gwtext.client.widgets.MessageBox;
 import com.gwtext.client.widgets.Panel;
 import com.gwtext.client.widgets.TabPanel;
 import com.gwtext.client.widgets.event.PanelListenerAdapter;
 
 import de.unihamburg.zbh.fishoracle.client.data.GWTImageInfo;
 import de.unihamburg.zbh.fishoracle.client.ImgPanel;
 import de.unihamburg.zbh.fishoracle.client.rpc.Search;
 import de.unihamburg.zbh.fishoracle.client.rpc.SearchAsync;
 
 public class CenterPanel extends TabPanel{
 
 	private TabPanel centerPanel = null;
 	private MainPanel mp = null;;
 	private CenterPanel cp = null;
 	
 	public CenterPanel(MainPanel mainPanel) {
 		
 		mp = mainPanel;
 		cp = this;
 		
         this.setDeferredRender(false);  
         this.setEnableTabScroll(true);
         this.setActiveTab(0);
         
         Panel startingPanel = new HTMLPanel();
         
         startingPanel.setHtml("" +
         		"<br><center><h1>FISH Oracle</h1> <i>alpha</i></center><br>" +
         		"You can serach for Amplicons by giving an Amplicon Stable ID" +
         		" e.g. '60.01' or for a gene specified by a gene name e.g. 'kras'" +
         		" or a Karyoband giving the exact Karyoband identifier e.g. '8q21.3'." +
         		" By clicking on an element a window opens that shows additional information." +
         		" As the amplicon data is incompatible to the Ensembl version 55 the currently" +
         		" used Ensembl version is 54. If you want to search for a gene in the Ensembl " +
         		" browser you better also use version 54 " +
         		"<a href=\"http://may2009.archive.ensembl.org\" target=_blank>http://may09.archive.ensembl.org</a>" +
         		"<br><br>" +
         		"FISH Oracle uses:<br><br> " +
         		"<li> the Google Web Toolkit <a href=\"http://code.google.com/webtoolkit/\" target=_blank>http://code.google.com/webtoolkit/</a></li>" +
         		"<li> the Ensembl human core database <a href=\"http://www.ensembl.org\" target=_blank>http://www.ensembl.org</a></li>" +
         		"<li> AnnotationSketch of the GenomeTools <a href=\"http://www.genometools.org\" target=_blank>http://www.genometools.org</a></li>" +
         		"</ul>");
         
         startingPanel.setTitle("Welcome");
         startingPanel.setAutoScroll(true);
         startingPanel.setClosable(false);
         
         this.add(startingPanel);
         
         this.addListener(new MyPanelListenerAdapter(this, mp));
         
 	}
 
 	public TabPanel getCenterPanel() {
 		return centerPanel;
 	}
 
 	public void setCenterPanel(TabPanel centerPanel) {
 		this.centerPanel = centerPanel;
 	}	
 	
 	/*=============================================================================
 	 *||                              RPC Calls                                  ||
 	 *=============================================================================
 	 * */	
 	
 		
 	public void imageRedraw(GWTImageInfo imgInfo){
 			
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<GWTImageInfo> callback = new AsyncCallback<GWTImageInfo>(){
 			public void onSuccess(GWTImageInfo result){
 				
				if(cp.getActiveTab() instanceof ImgPanel){	
				
 				ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
 				
 				imagePanel.remove(imagePanel.getImageLayer().getElement().getId(), true);
 				
 				imagePanel.setImageInfo(result);
 				
 				imagePanel.getChrBox().setValue(imagePanel.getImageInfo().getChromosome());
 				imagePanel.getStartBox().setValue(Integer.toString(imagePanel.getImageInfo().getStart()));
 				imagePanel.getEndBox().setValue(Integer.toString(imagePanel.getImageInfo().getEnd()));
 				
 				AbsolutePanel absp =  mp.createImageLayer(result);
 				
 				imagePanel.add(absp);
 				
 				imagePanel.setImageLayer(absp);
 				
 				imagePanel.doLayout();
 				
				}
				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				MessageBox.alert("Nothing found!");
 			}
 		};
 		req.redrawImage(imgInfo, callback);
 	}
 
 	
 }
 
 class MyPanelListenerAdapter extends PanelListenerAdapter {
 	
 	CenterPanel cp=null;
 	MainPanel mp=null;
 	
 	public MyPanelListenerAdapter(CenterPanel centerPanel, MainPanel mainPanel){
 		cp = centerPanel;
 		mp = mainPanel;
 	}
 	
 	public void onResize(BoxComponent component, int adjWidth, int adjHeight, int rawWidth, int rawHeight){
 		if(component.getWidth() >= 150){
			if(cp.getActiveTab() instanceof ImgPanel){	
 			ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
 			imagePanel.getImageInfo().setWidth(component.getWidth() - 20);
 			cp.imageRedraw(imagePanel.getImageInfo());
			}
 		}
 	}
 
 
 
 }
