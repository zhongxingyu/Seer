 package ch.informatica08.yanel.gwt.client;
 
 
 import org.wyona.yanel.gwt.client.ConfigurableComponentsAware;
 import org.wyona.yanel.gwt.client.ui.gallery.Gallery;
 import org.wyona.yanel.gwt.client.ui.gallery.GalleryViewer;
 import org.wyona.yanel.gwt.client.ui.gallery.ImageItem;
 import org.wyona.yanel.gwt.client.ui.gallery.Item;
 import org.wyona.yanel.gwt.client.ui.gallery.TextItem;
 
 import ch.informatica08.yanel.gwt.client.ui.gallery.ImageGalleryViewer;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.HTTPRequest;
 import com.google.gwt.user.client.ResponseTextHandler;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.NodeList;
 import com.google.gwt.xml.client.XMLParser;
 
 /**
  * Yanel.ImageBrowser.configurations = {id:&lt;root panel id>, gallery_provider_url : &lt;URL of the data for the component(e.g. atom data)>}
  * */
 public class ImageBrowser extends ConfigurableComponentsAware implements EntryPoint{
 	
     public static interface ParameterNames{
         public static final String IMAGE_LISTER_URL = "gallery_provider_url";
     }
     
     public void onModuleLoad() {
     	initializeInstances();
     }
     
     protected void initializeInstances() {
     	RootPanel [] p = getRootPanels();
     	for (int i = 0; i < p.length; i++) {
     		if(null == p[i]){
     			continue;
     		}
     		
     		final int componentNumber = i;
     		Gallery g = new Gallery(){
     			protected void init() {
     				
     				HTTPRequest.asyncGet(getConfiguration(ParameterNames.IMAGE_LISTER_URL, componentNumber), new ResponseTextHandler(){
     					public void onCompletion(String responseText) {
     						
     						final Element gallery = XMLParser.parse(responseText).getDocumentElement();
    						title = gallery.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();//.toString();
     						
     						if(title == null){
     							title = "NO TITLE";
     						}
     						
 							NodeList nl = gallery.getElementsByTagName("entry");
     						for (int j = 0; j < nl.getLength(); j++) {
     							Element item = (Element)nl.item(j);
    							String caption = item.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();//.toString();
     							String summary = null;
     							String src = ((Element)item.getElementsByTagName("content").item(0)).getAttribute("src");
     							
     							
     							NodeList summaryNl = item.getElementsByTagName("summary");
     							if(summaryNl.getLength() > 0){
    								summary = ((Element)summaryNl.item(0)).getFirstChild().getNodeValue();//.toString();
     							}
     							
     							Item i = new ImageItem(caption, src);
     							i.setSummary(summary);
     							
     							itemCache.put(new Integer(j), i);
 								size++;
 							}
     						
     						if(size <= 0){
     	    					itemCache.put(new Integer(0), Item.getNoItemsItem());
     	    					size++;
     	    				}
     						
     						selectItem(0);
     					}
     				});
     			}
     			
     			protected void retrieveItem(int index) {
     				// Everything is cached during init()
     			}
     		};
     		
     		GalleryViewer gv = new ImageGalleryViewer(g){
     			protected Widget getWidgetForItem(Item item) {
     				if (item instanceof ImageItem) {
 						ImageItem imageItem = (ImageItem) item;
 						return new Image(imageItem.getSrc());
 					}else if (item instanceof TextItem) {
 						TextItem textItem = (TextItem)item;
 						return new Label(textItem.getText());
 					}else{
 						return super.getWidgetForItem(item);	
 					}
     				
     			}
     		};
     		
             p[i].add(gv);
 		}
     }
 }
