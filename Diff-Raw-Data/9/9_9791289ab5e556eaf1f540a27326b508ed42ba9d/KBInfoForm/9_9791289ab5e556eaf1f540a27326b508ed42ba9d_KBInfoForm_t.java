 package de.uni_leipzig.simba.saim.gui.widget;
 
 import com.vaadin.data.validator.IntegerValidator;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.saim.gui.validator.EndpointURLValidator;
 import de.uni_leipzig.simba.saim.gui.validator.PageSizeValidator;
 
 /** Allows the user to manually set the properties of a knowledge base, which are endpoint URL, graph URI, page size, restrictions */
 @SuppressWarnings("serial")
 public class KBInfoForm extends Form
 {	
 	//	protected final static String TEXTFIELD_WIDTH = "50em";
 		protected final static String WIDTH = "35em";
 	//	protected final static String URL_DEFAULT = "http://lgd.aksw.org:5678/sparql";
 	//	protected final static String GRAPH_DEFAULT = "http://www.instancematching.org/oaei/di/drugbank/";
 
 	//protected final VerticalLayout layout = new VerticalLayout();
 	protected final TextField url = new TextField("Endpoint URL", "http://example.com/sparql");
 	protected final TextField  graph = new TextField("Graph");
 	protected final TextField  pageSize = new TextField("Page size", "-1");
 	protected final TextField textFields[] = {url, graph, pageSize};
 	
 	protected final Button next = new Button("OK" );
 	protected final Component components[] = {url, graph, pageSize, next};
 
 	//	public void discard()
 	//	{
 	//		discard();
 	//	}
 	//	
 	public KBInfoForm(String title)
 	{
 		this.setCaption(title);
 //		this.setLayout(layout);
 //		layout.setSpacing(true);
 		//layout.addComponent(this);
 		this.setWidth(WIDTH);
 		addField("Endpoint URL",url);
 		url.addValidator(new EndpointURLValidator());
 		url.setRequired(true);
 		url.setRequiredError("The endpoint URL may not be empty.");
 		addField("Graph",graph);
 		addField("Page size",pageSize);
 		pageSize.addValidator(new PageSizeValidator("Page size needs to be an integer."));		
 		// Have a button bar in the footer.
 		HorizontalLayout buttonBar = new HorizontalLayout();
 		//buttonBar.setHeight("25px");
 		getFooter().addComponent(buttonBar);		 
 		// Add an Ok (commit), Reset (discard), and Cancel buttons
		setValidationVisible(true);
 //		Button okbutton = new Button("OK", this, "commit");
 //		buttonBar.addComponent(okbutton);
 		//buttonBar.setComponentAlignment(okbutton, Alignment.TOP_LEFT);
 		buttonBar.addComponent(new Button("Reset", this,"reset"));
 		//buttonBar.addComponent(new Button("Cancel",this,"cancel"));
 		getLayout().setMargin(true);
 		for(TextField field: textFields)
 		{
 			field.setWidth("100%");
 		}
 	}
 	
 	public void reset()
 	{
 		for(TextField field: textFields)
 		{
 			field.setValue("");
 		}
 	}
 
 	public KBInfo getKBInfo() {
 		KBInfo kbInfo = new KBInfo();
 		kbInfo.endpoint = url.getValue().toString();
 		kbInfo.graph = graph.getValue().toString();
 		int pageSizeInt = Integer.parseInt((String)pageSize.getValue());
 		kbInfo.pageSize = pageSizeInt;
 		return kbInfo;
 	}
 
 //	@SuppressWarnings("serial")
 //	private void addButtons() {
 //		next.setSizeFull();
 //		next.addListener(new ClickListener() {			
 //			@Override
 //			public void buttonClick(ClickEvent event) {
 //				if(checkValues()) {
 //					
 //					parentWindow.showNotification("Succesfully defined the endpoint...");
 //				}				
 //			}
 //		});				
 //	}
 
 //	/**
 //	 * Method to check values and trigger user notifications.
 //	 */
 //	private boolean checkValues() {
 //		String url_value = (String)url.getValue();
 //		String graphUri = (String)graph.getValue();
 //		String pageSizeString = pageSize.getValue().toString();
 //
 //		if(url_value.length()>0)  { //add check if URL is valid
 //			if(pageSizeString.length()>0) {
 //				int pageSize;
 //				try {
 //					pageSize = Integer.parseInt(pageSizeString);
 //					if(graphUri.length()>0) {
 //						return true;
 //					}
 //					else {
 //						// no graph entered
 //						//this.parentWindow.showNotification("No graph entered.");
 //						return true;
 //					}
 //				}catch(NumberFormatException e) {
 //					this.pageSize.setCaption("Please Enter a valid page size.");
 //					return false;
 //				}
 //			}
 //			else {//pageSize is empty
 //				return false;
 //			}
 //		} else {
 //			url.setCaption("Please enter a valid URL.");
 //			return false;
 //		}
 //	}
 } 
