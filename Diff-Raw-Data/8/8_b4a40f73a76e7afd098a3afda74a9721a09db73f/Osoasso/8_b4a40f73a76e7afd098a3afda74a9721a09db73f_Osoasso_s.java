 package org.josh.osoasso.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * @author Josh Peterson
  *
  * The class which implements a simple GWT front end for the Osoasso product.
  */
 public class Osoasso extends Composite implements EntryPoint {
 
 	/**
 	 * @author Josh Peterson
 	 *
 	 * This interface allows access to a style defined in the XML layout file from
 	 * from the Java code here. 
 	 */
 	interface OsoassoStyle extends CssResource
 	{
 		String individualOutputArea();
 	}
 	
 	interface OsoassoUiBinder extends UiBinder<DockLayoutPanel, Osoasso> {}
 	
 	private static OsoassoUiBinder uiBinder = GWT.create(OsoassoUiBinder.class);
 	private static Osoasso instance;
 	
 	private JavaScriptObject naclModule = null;
 	
 	@UiField TextBox inputField;
 	@UiField ScrollPanel scrollPanel;
 	@UiField VerticalPanel resultsPanel;
 	@UiField OsoassoStyle style;
 	@UiField Label moduleStatus;
 
 	public Osoasso()
 	{
 		initWidget(uiBinder.createAndBindUi(this));
 		instance = this;
 	}
 	
 	/**
 	 * We need a static method to allow the JavaScript event handler to call back
 	 * to the GWT Java code.
 	 * 
 	 * @param naclMessage The message from the Nacl call to native code.
 	 */
 	public static void onNaclMessageStatic(String naclMessage)
 	{
 		instance.onNaclMessage(naclMessage);
 	}
 
 	@Override
 	public void onModuleLoad()
 	{
 		DockLayoutPanel outer = uiBinder.createAndBindUi(this);
 		
 		Window.enableScrolling(false);
 	    Window.setMargin("0px");
 	    
 	    final NaclWidget naclWidget = new NaclWidget();
 	    naclWidget.setHTML("<embed name=\"nacl_module\" "
 	                       + "id=\"osoasso\" "
 	                       + "width=0 height=0 "
 	                       + "src=\"osoasso.nmf\" "
 	                       + "type=\"application/x-nacl\" />");
 	    
 	    RootLayoutPanel root = RootLayoutPanel.get();
 	    root.add(naclWidget);
 	    root.add(outer);
 	}
 	
 	@UiHandler("inputField")
 	void onKeyPress(KeyPressEvent e)
 	{
 		if (e.getCharCode() == KeyCodes.KEY_ENTER)
 		{
 			CallOsoassoNaclModuleInputMethod(naclModule, inputField.getText());
 			
 		    scrollPanel.scrollToBottom();
 		}
 	}
 	
 	public void onNaclMessage(String naclMessage)
 	{
 		CommitData commit = new CommitData(naclMessage);
 		CommitDataFormatter formatter = new CommitDataFormatter(commit);
 		
 		this.AddHTMLToResultsPanel(formatter.FormatAction());
 		this.AddHTMLToResultsPanel(formatter.FormatCommitMetaData());
 		this.AddHTMLToResultsPanel(formatter.FormatOutputName());
 		this.AddHTMLToResultsPanel(formatter.FormatOutputMatrix());
		//this.AddHTMLToResultsPanel("$$\\left[\\matrix{newx.x&newy.x&newz.x \\\\newx.y&newy.y&newz.y \\\\newx.z&newy.z&newz.z}\\right]$$");
		CallMathJax();
 	}
 	
 	/**
 	 * @author Josh Peterson
 	 *
 	 * Use the HTML onLoad callback to wait for the Nacl module to load.
 	 */
 	public class NaclWidget extends HTML
 	{
 		@Override
 	    protected void onLoad()
 	    {
 			naclModule = GetNaclModule();
 			if (naclModule != null)
 			{
 				moduleStatus.setText("Module loaded");
 				RegisterNaclListener(naclModule);
 			}
 			else 
 			{
 				moduleStatus.setText("Module failed to load");
 			}
 		}
 	}
 	
 	private void AddHTMLToResultsPanel(String html)
 	{
 		HTML htmlElement = new HTML(html);
 		htmlElement.getElement().addClassName(style.individualOutputArea());
 		
 		resultsPanel.add(htmlElement);
 	    resultsPanel.setSpacing(10);
 	}
 	
	private native void CallMathJax()
	/*-{
			$wnd.MathJax.Hub.Typeset();
	}-*/;
	
 	private native JavaScriptObject GetNaclModule()
 	 /*-{
 	 	return $doc.getElementById("osoasso");
 	 }-*/;
 	 
 	 public native void RegisterNaclListener(JavaScriptObject naclModule)
 	 /*-{
 	 	naclModule.addEventListener('message',
 								 	function(message_event)
 								 	{
 							      		@org.josh.osoasso.client.Osoasso::onNaclMessageStatic(Ljava/lang/String;)(message_event.data);
 							    	}, false);
 	 }-*/;
 	 
 	 protected native void CallOsoassoNaclModuleInputMethod(JavaScriptObject naclModule, String input)
 	 /*-{
 		    if (naclModule != null)
 		    {
 		    	naclModule.postMessage('input:' + input + ":me@it.com");
 		    }
 		    else
 		    {
 		    	alert("Danger: Nacl module not loaded!");
 		    }
 	 }-*/;
 }
