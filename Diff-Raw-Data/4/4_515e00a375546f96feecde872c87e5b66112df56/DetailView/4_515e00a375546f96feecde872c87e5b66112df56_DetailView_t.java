 package at.fakeroot.sepm.client;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import at.fakeroot.sepm.shared.client.serialize.ClientGeoObject;
 
 /**
  * @author Anca Cismasiu
  * Class containung the object details shown in the InfoWindow
  */
 
 public class DetailView extends InfoWindowContent implements ClickHandler{
 	
 	private IGeoManager gManager =null;
 	private ClientGeoObject gObject=null;
 	private static SimplePanel mySiPa = new SimplePanel();
 	private VerticalPanel myVePa=new VerticalPanel();
 	private Label title = null;
 	private HTML detail = null;
 	private FlowPanel tags= null;
 	
 	
 	public DetailView(ClientGeoObject object, IGeoManager geoManager){
 		super(mySiPa);
 		mySiPa.setWidget(myVePa);
 		gObject=object;
 		gManager=geoManager;
 		
 		title=new Label(gObject.getTitel());
 		detail = new HTML("Loading...");
 		tags = new FlowPanel();
 		setTagList();
 		
 		myVePa.add(title);
 		myVePa.add(detail);
 		myVePa.add(tags);		
 	}
 	
 /**
  * sets the object details in the InfoWindow
  * @param HTMLStr String text that will be interpreted as HTML
  * */	
 	public void setDetail(String HTMLStr){
 		detail.setHTML(HTMLStr);
 	}
 	
 /**
  * private method used to set the tag list in the InfoWindow
  * 
  * */	
	private void setTagList() {
 		String[] tagArray = gObject.getTags(); 
 		final Anchor[] anchor = new Anchor[tagArray.length];
 		for(int i=0; i<tagArray.length; i++){
 			anchor[i]=new Anchor(tagArray[i]);
			anchor[i].setHref("javascript:void()");
 			anchor[i].addClickHandler(this);
 			tags.add(anchor[i]);
 			tags.add(new InlineLabel(" "));
 		}
 	}
 
 /**
  * inherited method from ClickHandler-interface
  * */
 	public void onClick(ClickEvent ce) {
 		gManager.addSearchTag(((Anchor)ce.getSource()).getText());
 	}
 
 	
 	
 }
