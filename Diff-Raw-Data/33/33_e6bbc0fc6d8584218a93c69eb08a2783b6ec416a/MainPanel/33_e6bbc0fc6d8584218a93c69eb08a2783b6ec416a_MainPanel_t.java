 package de.unihamburg.zbh.fishoracle.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Image;
 import com.gwtext.client.core.EventObject;
 import com.gwtext.client.core.Margins;
 import com.gwtext.client.core.NameValuePair;
 import com.gwtext.client.core.RegionPosition;
 import com.gwtext.client.widgets.Button;
 import com.gwtext.client.widgets.MessageBox;
 import com.gwtext.client.widgets.Panel;
 import com.gwtext.client.widgets.TabPanel;
 import com.gwtext.client.widgets.Toolbar;
 import com.gwtext.client.widgets.ToolbarButton;
 import com.gwtext.client.widgets.Window;
 import com.gwtext.client.widgets.event.ButtonListenerAdapter;
 import com.gwtext.client.widgets.event.KeyListener;
 import com.gwtext.client.widgets.form.TextField;
 import com.gwtext.client.widgets.grid.GridView;
 import com.gwtext.client.widgets.grid.PropertyGridPanel;
 import com.gwtext.client.widgets.layout.BorderLayout;
 import com.gwtext.client.widgets.layout.BorderLayoutData;
 import com.gwtext.client.widgets.layout.FitLayout;
 
 import de.unihamburg.zbh.fishoracle.client.data.Amplicon;
 import de.unihamburg.zbh.fishoracle.client.data.GWTImageInfo;
 import de.unihamburg.zbh.fishoracle.client.data.Gen;
 import de.unihamburg.zbh.fishoracle.client.data.RecMapInfo;
 import de.unihamburg.zbh.fishoracle.client.rpc.Search;
 import de.unihamburg.zbh.fishoracle.client.rpc.SearchAsync;
 
 
 public class MainPanel{
 
 	private Panel mainPanel = null;
 	private Panel borderPanel = null;
 	private TabPanel cp = null;
 	private CenterPanel cpContainer = null;
 	
 	public MainPanel() {
 	
 	mainPanel = new Panel();
 	
     mainPanel.setBorder(false);
     mainPanel.setPaddings(15);
     mainPanel.setLayout(new FitLayout());
 
     borderPanel = new Panel();
     borderPanel.setLayout(new BorderLayout());
 	
     NorthPanel northPanel = new NorthPanel();
     CenterPanel centerPanel = new CenterPanel(this);
     cpContainer = centerPanel;
     cp = centerPanel.getCenterPanel();
     WestPanel westPanel = new WestPanel(this, cp);
     
     
     BorderLayoutData westData = new BorderLayoutData(RegionPosition.WEST);
     westData.setSplit(true);
     westData.setMinSize(175);
     westData.setMaxSize(400);
     westData.setMargins(new Margins(0, 5, 0, 0));
     
     borderPanel.add(northPanel.getNorthPanel(), new BorderLayoutData(RegionPosition.NORTH));
     borderPanel.add(westPanel.getWestPanel(), westData);
     borderPanel.add(centerPanel.getCenterPanel(), new BorderLayoutData(RegionPosition.CENTER));
     
     mainPanel.add(borderPanel);
     
 	}
 
 	public Panel getMainPanel() {
 		return mainPanel;
 	}
 
 	public void setMainPanel(Panel mainPanel) {
 		this.mainPanel = mainPanel;
 	}
 	
 	
 	
 	public AbsolutePanel createImageLayer(GWTImageInfo imgInfo){
 		
 		AbsolutePanel absPanel = new AbsolutePanel();
         absPanel.setPixelSize(imgInfo.getWidth(), imgInfo.getHeight());
 		
 		Image image = new Image(imgInfo.getImgUrl());
         
 		absPanel.add(image);
 		
 		int rmc;
 		
 		for(rmc=0; rmc < imgInfo.getRecmapinfo().size(); rmc++){
 			
 			final Image spaceImg = new Image();
 			
 			spaceImg.addClickHandler(new MyClickHandler(imgInfo.getRecmapinfo().get(rmc), this));
 			
 			int southeast_x = (int) imgInfo.getRecmapinfo().get(rmc).getSoutheastX();
 			
 			int northwest_x = (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestX();
 			
 			spaceImg.setUrl("images/1pximg.gif");
 			
 			spaceImg.setWidth(Integer.toString((southeast_x - northwest_x)));
 			
 			spaceImg.setHeight(Integer.toString((int) (imgInfo.getRecmapinfo().get(rmc).getSoutheastY() - imgInfo.getRecmapinfo().get(rmc).getNorthwestY())));
 
 			spaceImg.addStyleName("image_hand");
 			
 			absPanel.add(spaceImg, (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestX(), (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestY());
 			
 		}
 		
 		return absPanel;
 	}
 	
 	public void jumpto(){
 		
 		ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
 		GWTImageInfo imgInfo = imagePanel.getImageInfo();
 		
 		String newChr;
 	    int newStart;
 	    int newEnd;
 		
 		newChr = imagePanel.getChrBox().getValueAsString();
 	    
 	    newStart = Integer.parseInt(imagePanel.getStartBox().getValueAsString());
 	    
 	    newEnd = Integer.parseInt(imagePanel.getEndBox().getValueAsString());
 	    
 	    if(newStart >= newEnd || newEnd - newStart <= 10){
 	    	
 	    	MessageBox.alert("The end value must at least be 10 base pairs greater than the start value!");
 	    	
 	    } else {
 	    
 	    	imgInfo.setChromosome(newChr);
 	    	
 	    	imgInfo.setStart(newStart);
 	    
 	    	imgInfo.setEnd(newEnd);
 		
 	    	cpContainer.imageRedraw(imgInfo);
 	    }
 
 	}
 	
 	public void newImageTab(final GWTImageInfo imgInfo){
 		
 	KeyListener rangeListener = new KeyListener(){
 			@Override
 			public void onKey(int key, EventObject e) {	
 				jumpto();
 			}
 		};	
 		
 		
 	Toolbar toolbar = new Toolbar();
         
         ToolbarButton left = new ToolbarButton(null, new ButtonListenerAdapter(){
 			public void onClick(Button button, EventObject e) {
 				
 				ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
 				GWTImageInfo imgInfo = imagePanel.getImageInfo();
 				int range;
 				int percRange;
 				int perc = 10;
 				int newStart;
 				int newEnd;
 		
 				range = imgInfo.getEnd() - imgInfo.getStart(); 
 		
 				percRange = range * perc / 100;
 	    
 				newStart = imgInfo.getStart() - percRange;
 	    
 				newEnd = imgInfo.getEnd() - percRange;
 	    
 				if(newStart > 0){
 	    
 					imgInfo.setStart(newStart);
 	    
 					imgInfo.setEnd(newEnd);
 		
 					cpContainer.imageRedraw(imgInfo);
 	    	
 				} else {
 					MessageBox.alert("You have reached the chromsomes end ...");
 				}
         	}
        });
         
         left.setIcon("icons/arrow_left.png");
         
         ToolbarButton right = new ToolbarButton(null, new ButtonListenerAdapter(){
 			public void onClick(Button button, EventObject e) {
 				
 				ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
     			GWTImageInfo imgInfo = imagePanel.getImageInfo();
 				
     			int range;
     			int percRange;
     			int perc = 10;
     			int newStart;
     			int newEnd;
     			
     			range = imgInfo.getEnd() - imgInfo.getStart(); 
     			
     		    percRange = range * perc / 100;
     			
     		    
     		    newStart = imgInfo.getStart() + percRange;
     		    
     		    newEnd = imgInfo.getEnd() + percRange;
     		    
     		    imgInfo.setStart(newStart);
     		    
     		    imgInfo.setEnd(newEnd);
     		    
 				cpContainer.imageRedraw(imgInfo);	
         	}
        });
         
         right.setIcon("icons/arrow_right.png");
         
         ToolbarButton zoomin = new ToolbarButton(null, new ButtonListenerAdapter(){
 			public void onClick(Button button, EventObject e) {
 				
 				ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
     			GWTImageInfo imgInfo = imagePanel.getImageInfo();
 				
     			int range;
     			int percRange;
     			int perc = 10;
     			int newStart;
     			int newEnd;
     			
     			range = imgInfo.getEnd() - imgInfo.getStart(); 
     			
     		    percRange = range * perc / 100;
     			
     		    
     		    newStart = imgInfo.getStart() + percRange;
     		    
     		    newEnd = imgInfo.getEnd() - percRange;
     		    
     		    if(newEnd - newStart > 10){
     		    
     		    	imgInfo.setStart(newStart);
     		    
     		    	imgInfo.setEnd(newEnd);
     			
     		    	cpContainer.imageRedraw(imgInfo);
     		    	
     		    } else {
     		    	MessageBox.alert("You have reached the highest zoom level ...");
     		    }
         	}
        });
         
         zoomin.setIcon("icons/arrow_in.png");
         
         ToolbarButton zoomout = new ToolbarButton(null, new ButtonListenerAdapter(){
 			public void onClick(Button button, EventObject e) {
 				
 				ImgPanel imagePanel = (ImgPanel) cp.getActiveTab();
     			GWTImageInfo imgInfo = imagePanel.getImageInfo();
 				
     			int range;
     			int percRange;
     			int perc = 10;
     			int newStart;
     			int newEnd;
     			
     			range = imgInfo.getEnd() - imgInfo.getStart(); 
     			
     		    percRange = range * perc / 100;
     			
     		    
     		    newStart = imgInfo.getStart() - percRange;
     		    
     		    newEnd = imgInfo.getEnd() + percRange;
     		    
    		    
    		    if(newStart < 0){
    		    	
    		    	newEnd = newEnd - newStart;
    		    	newStart = 0;
    		    }
    		    
     		    imgInfo.setStart(newStart);
     		    
     		    imgInfo.setEnd(newEnd);
     			
 				cpContainer.imageRedraw(imgInfo);	
         	}
        });
         
         zoomout.setIcon("icons/arrow_out.png");
         
         ToolbarButton jumpTo = new ToolbarButton(null, new ButtonListenerAdapter(){
 			public void onClick(Button button, EventObject e) {
 				
 				jumpto();
         	}
        });
         
         jumpTo.setIcon("icons/arrow_refresh.png");
         
         TextField chr = new TextField(); 
         chr.setWidth("25px");
         chr.addKeyListener(KeyCodes.KEY_ENTER, rangeListener);
         chr.setValue(imgInfo.getChromosome());
         
         TextField start = new TextField();
         start.setWidth("75px");
         start.addKeyListener(KeyCodes.KEY_ENTER, rangeListener);
         start.setValue(Integer.toString(imgInfo.getStart()));
         
         TextField end = new TextField();
         end.setWidth("75px");
         end.addKeyListener(KeyCodes.KEY_ENTER, rangeListener);
         end.setValue(Integer.toString(imgInfo.getEnd()));
         
         toolbar.addButton(left);
         toolbar.addText("scroll");
         toolbar.addButton(right);
         
         toolbar.addSeparator();
         
         toolbar.addButton(zoomin);
         toolbar.addText("zoom");
         toolbar.addButton(zoomout);
         
         toolbar.addSeparator();
         
         toolbar.addText("Chromosome: ");
         toolbar.addField(chr);
         toolbar.addText("Start: ");
         toolbar.addField(start);
         toolbar.addText("End: ");
         toolbar.addField(end);
 		
         toolbar.addButton(jumpTo);
 		
 		ImgPanel tab = addImgTab(imgInfo.getQuery());
 		tab.add(toolbar);
 		
 		tab.setChrBox(chr);
 		tab.setStartBox(start);
 		tab.setEndBox(end);
 		
 		AbsolutePanel absPanel = createImageLayer(imgInfo);
 		
 		tab.add(absPanel);
 		
 		tab.setImageLayer(absPanel);
 		
 		//tab.setImage(image);
 		tab.setImageInfo(imgInfo);
 		cp.add(tab);
 		cp.activate(tab.getId());
 		cp.scrollToTab(tab, true);
 	}
 	
 
 	
 	private ImgPanel addImgTab(String name) {
         ImgPanel tab = new ImgPanel();
         tab.setAutoScroll(true);
         tab.setTitle(name);
         tab.setClosable(true);
         return tab;
     }
 	
 	public void loadWindow(Amplicon amp){
 		
 		Window window = new Window();
 
 		window.setTitle("Amplicon " + amp.getAmpliconStableId());
 		 
 		window.setClosable(true);  
 		window.setPlain(true);    
 		
 		Panel panel = new Panel();  
 		panel.setBorder(false);
 		panel.setPaddings(15);  
 
 		PropertyGridPanel grid = new PropertyGridPanel();   
 		grid.setWidth(300);  
 		grid.setAutoHeight(true);  
 		grid.setSorted(false);   
 		
 		GridView view = new GridView();  
 		view.setForceFit(true);  
 		view.setScrollOffset(2); // the grid will never have scrollbars  
 		grid.setView(view);  
 		   
 		NameValuePair[] source = new NameValuePair[8];  
 		source[0] = new NameValuePair("Amplicon Stable ID", Double.toString(amp.getAmpliconStableId()));  
 		source[1] = new NameValuePair("Chromosome", amp.getChromosome());  
 		source[2] = new NameValuePair("Start", amp.getStart());  
 		source[3] = new NameValuePair("End", amp.getEnd());  
 		source[4] = new NameValuePair("Case Name", amp.getCaseName());  
 		source[5] = new NameValuePair("Tumor Type", amp.getTumorType());  
 		source[6] = new NameValuePair("Continuous", amp.getContinuous());  
 		source[7] = new NameValuePair("Amplevel", amp.getAmplevel());   
 		
 		grid.setSource(source);
 		
 		panel.add(grid);
 		
 		window.add(panel);
 		
 		window.setCloseAction(Window.HIDE);
 		//window.show(spaceImg.getElement().getId());
 		
 		window.show();
 	}
 	
 	public void loadWindow(Gen gene){
 		
 		Window window = new Window();
 
 		window.setTitle("Gene " + gene.getGenName());
 		 
 		window.setClosable(true);  
 		window.setPlain(true);  
 		
 		Panel panel = new Panel();  
 		panel.setBorder(false);
 		panel.setPaddings(15);  
 
 		PropertyGridPanel grid = new PropertyGridPanel();    
		grid.setWidth(700);
 		grid.setAutoHeight(true);  
 		grid.setSorted(false);   
 		
 		GridView view = new GridView();  
 		view.setForceFit(true);  
 		view.setScrollOffset(2); // the grid will never have scrollbars  
 		grid.setView(view);  
 		   
 		NameValuePair[] source = new NameValuePair[9];  
 		source[0] = new NameValuePair("Ensembl Stable ID", gene.getAccessionID()); 
 		source[1] = new NameValuePair("Name", gene.getGenName());
 		source[2] = new NameValuePair("Chromosome", gene.getChr());  
 		source[3] = new NameValuePair("Start", gene.getStart());  
 		source[4] = new NameValuePair("End", gene.getEnd());  
 		source[5] = new NameValuePair("Length", gene.getLength());
 		source[6] = new NameValuePair("Strand", gene.getStrand());  
 		source[7] = new NameValuePair("Bio Type" ,gene.getBioType()); 
 		source[8] = new NameValuePair("Description", gene.getDescription());
 		
 		grid.setSource(source);
 		
 		panel.add(grid);
 		
 		window.add(panel);
 		
 		window.setCloseAction(Window.HIDE);
 		//window.show(spaceImg.getElement().getId());
 		
 		window.show();
 	}
 }
 
 class MyClickHandler implements ClickHandler{
 	
 	RecMapInfo recInfo;
 	MainPanel main;
 	
 	public MyClickHandler(RecMapInfo recmapinfo, MainPanel m){
 		this.recInfo = recmapinfo;
 		this.main = m;
 	}
 	
 	@Override
 	public void onClick(ClickEvent event) {
 		
 		if(recInfo.getType().equals("amplicon")){
 			
 			ampliconDetails(recInfo.getElementName());
 			
 		}
 		if(recInfo.getType().equals("gene")){
 			System.out.println("blubb");
 			geneDetails(recInfo.getElementName());
 			
 		}
 	}
 	
 	/*=============================================================================
 	 *||                              RPC Calls                                  ||
 	 *=============================================================================
 	 * */	
 		 
 		
 	public void ampliconDetails(String query){
 			
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<Amplicon> callback = new AsyncCallback<Amplicon>(){
 			public void onSuccess(Amplicon amp){
 				
 				main.loadWindow(amp);
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				MessageBox.alert("Nothing found!");
 			}
 		};
 		req.getAmpliconInfo(query, callback);
 	}
 	
 	
 	public void geneDetails(String query){
 		
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<Gen> callback = new AsyncCallback<Gen>(){
 			public void onSuccess(Gen gene){
 				
 				main.loadWindow(gene);
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				MessageBox.alert("Nothing found!");
 			}
 		};
 		req.getGeneInfo(query, callback);
 	}
 }
