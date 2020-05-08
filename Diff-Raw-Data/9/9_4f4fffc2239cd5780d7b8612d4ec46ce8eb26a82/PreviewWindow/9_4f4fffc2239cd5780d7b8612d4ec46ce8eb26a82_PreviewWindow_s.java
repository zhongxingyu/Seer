 package monbulk.MethodBuilder.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 
 import monbulk.MethodBuilder.client.event.MenuChangeEvent;
 import monbulk.client.desktop.Desktop;
 import monbulk.shared.Services.MethodService;
 import monbulk.shared.Services.ServiceRegistry;
 import monbulk.shared.Services.MethodService.MethodUpdateHandler;
 import monbulk.shared.util.MonbulkEnums.ServiceNames;
 import monbulk.shared.widgets.Window.IWindow;
 import monbulk.shared.widgets.Window.OkCancelWindow;
 
 /**
  * This is a popup window for performing a lot of the Method Service work and returning a confirmation to the user
  * 
  * @author Andrew Glenn
  *
  */
 public class PreviewWindow extends OkCancelWindow implements IWindow, MethodUpdateHandler {
 
 	private StringBuilder formattedText;
 	
 	public enum SupportedFormats{XML,TCL};
 	private SupportedFormats selectedFormat;	
 	
 	protected ScrollPanel _framingPanel;
 	protected HTMLPanel _PreviewPanel;
 	protected ClickHandler handleSave; 
 	protected HandlerRegistration registration;
 	private String oldMethodName;
 	private String newMethodName;
 	protected Label _Guide;
 	private final HandlerManager eventBus;
 	private String MethodID;
 	
 	public PreviewWindow(HandlerManager eventBus) {
 		super("MethodPreviewWindow", "Preview Method Data");
 		this.setWidth("400px");
 		this.setHeight("250px");
 		this.m_windowSettings.resizable=false;
 		this.m_windowSettings.width=400;
 		this.m_windowSettings.height=250;
 		this.m_windowSettings.setGlass=true;
 		this.m_windowSettings.minWidth=200;
 		this.m_windowSettings.minHeight=200;
 		this.m_cancel.setVisible(false);
 		
 		
 		_framingPanel = new ScrollPanel();
 		//this.setWidget(_framingPanel);
 		m_contentPanel.add(_framingPanel);
 		_PreviewPanel = new HTMLPanel("");
 		_framingPanel.add(_PreviewPanel);
 		_Guide= new Label();
 		this.eventBus= eventBus;
 		
 	}
 	public void loadPreview(StringBuilder tmpList,PreviewWindow.SupportedFormats inputFormat)
 	{
 		formattedText = tmpList;
 		selectedFormat = inputFormat;
 		_PreviewPanel.clear();
 		TextArea tmpArea = new TextArea();
 		tmpArea.setHeight("600px");
 		tmpArea.setWidth("600px");
 		//HTML output = new HTML();
 		//output.setHTML(tmpList.toString());
 		tmpArea.setText(formattedText.toString());
 		_PreviewPanel.add(tmpArea);
 		this.m_cancel.setVisible(false);
 		
 		
 	}
 	public void showInvalid(String Reasons,String Action)
 	{
 		
 		
 		_PreviewPanel.clear();
 		_Guide.setText("Unable to " + Action);
 		_Guide.setStyleName("InValidWindowText");
 		HTML lblReasons = new HTML();
 		lblReasons.setHTML(Reasons);
 		_PreviewPanel.add(_Guide);
 		_PreviewPanel.add(lblReasons);
 		this.m_cancel.setVisible(false);
 	}
 	/**
 	 * 
 	 * @param tmpList
 	 * @param inputFormat
 	 */
 	public void cloneMethod(String tmpList,PreviewWindow.SupportedFormats inputFormat,final String oldaName)
 	{
 		this.m_cancel.setVisible(true);
 		formattedText = new StringBuilder();
 		selectedFormat = inputFormat;
 		_PreviewPanel.clear();
 		oldMethodName = oldaName;
 		final TextArea tmpArea = new TextArea();
 		tmpArea.setHeight("40px");
 		tmpArea.setWidth("200px");
 		//HTML output = new HTML();
 		//output.setHTML(tmpList.toString());
 		tmpList = tmpList.replace("<method>", "");
 		tmpList = tmpList.replace("</method>", "");
 		//tmpList = tmpList.replace("(<id>)(.*)(</id>)","");
 		
 		this.formattedText.append(tmpList);
 		final String xml = tmpList;
 		tmpArea.setText("");
 		final MethodUpdateHandler tmpHandle = this;
 		handleSave = new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				try
 				{
 					MethodService tmpSvc = MethodService.get();
 					if(tmpSvc != null)
 					{
 						
 						newMethodName = tmpArea.getText();
 						//formattedText.append(tmpList);
 						tmpSvc.checkExists(tmpArea.getText(), tmpHandle);
 							
 					}
 					else
 					{
 						throw new ServiceRegistry.ServiceNotFoundException(ServiceNames.Methods);
 					}
 				}
 				catch (ServiceRegistry.ServiceNotFoundException e)
 				{
 					GWT.log("Couldn't find Method service");
 				}
 				
 			}
 			
 		};
 		registration = this.m_ok.addClickHandler(handleSave);
 		
 		_Guide.setText("Name of new method:");
 		_PreviewPanel.add(_Guide);
 		_PreviewPanel.add(tmpArea);
 	}
 	/**
 	 * 
 	 * @param tmpList
 	 * @param inputFormat
 	 */
 	public void loadPreview(String tmpList,PreviewWindow.SupportedFormats inputFormat, String ID)
 	{
 		this.MethodID = ID;
 		this.m_cancel.setVisible(false);
 		selectedFormat = inputFormat;
 		_PreviewPanel.clear();
 		final TextArea tmpArea = new TextArea();
 		tmpArea.setHeight("600px");
 		tmpArea.setWidth("600px");
 		//HTML output = new HTML();
 		//output.setHTML(tmpList.toString());
 		tmpList = tmpList.replace("<method>", "");
 		tmpList = tmpList.replace("</method>", "");
 		tmpArea.setText(tmpList);
 		try
 		{
 			MethodService tmpSvc = MethodService.get();
 			if(tmpSvc != null)
 			{
 				tmpSvc.createOrUpdate(tmpArea.getText(), this);
 				
 				
 					
 			}
 			else
 			{
 				throw new ServiceRegistry.ServiceNotFoundException(ServiceNames.Methods);
 			}
 		}
 		catch (ServiceRegistry.ServiceNotFoundException e)
 		{
 			GWT.log("Couldn't find Method service");
 		}
 		_Guide.setText("Saving Method... Please wait.");
 		_PreviewPanel.add(_Guide);
 		//_PreviewPanel.add(tmpArea);
 		
 		
 		
 	}
 	@Override
 	public void onUpdateMethod(String response) {
 		this.m_cancel.setVisible(false);
 		AbsolutePanel _w_response = new AbsolutePanel();
 		Label _r_text = new Label();
 		if(response!=null)
 		{
 			if(response=="")
 			{
 				_r_text.setText("Success: Your method has been updated");
 				this.eventBus.fireEvent(new MenuChangeEvent("Refresh",this.MethodID));
 			}
 			else if(response=="Delete")
 			{
 				_r_text.setText("Success: Your method has been deleted");
 				registration.removeHandler();
 				this.eventBus.fireEvent(new MenuChangeEvent("Refresh",""));
 			}
 			else
 			{
 				_r_text.setText("Success: Your method has been created with ID: " + response);
 				if(registration!=null)
 				{
 					registration.removeHandler();
 				}
 				this.eventBus.fireEvent(new MenuChangeEvent("Refresh",response));
 			}
 		}
 		else
 		{
 			_r_text.setText("Success: Your method has been updated");
 		}
 		this._PreviewPanel.clear();
 		_w_response.add(_r_text);
 		this._PreviewPanel.add(_w_response);
 		
 		this.m_windowSettings.windowTitle = "METHOD SAVED";
 		Desktop d = Desktop.get();		
 		
 		//this.m_ok.(handleSave);
 		d.show("MethodPreviewWindow",true);
 		/*if(response=="Delete")
 		{
 			this.eventBus.fireEvent(new MenuChangeEvent("Refresh-Restart"));
 		}	
 		else
 		{
 			this.eventBus.fireEvent(new MenuChangeEvent("Refresh"));
 		}*/
 		
 		
 		
 	}
 	@Override
 	public void checkExists(Boolean exists) {
 		if(!exists)
 		{
 			MethodService tmpSvc = MethodService.get();
 			if(tmpSvc != null)
 			{
 				
 				//Replace Old Name with new Name
 				
 				String fullXML = this.formattedText.toString().replace(this.oldMethodName, this.newMethodName);
 				tmpSvc.createOrUpdate(fullXML, this);
 					
 			}
 		}
 		else
 		{
 			this._Guide.setText("That method name is already in use, please try another");
 			Desktop d = Desktop.get();		
 			//this.m_ok.(handleSave);
 			d.show("MethodPreviewWindow",true);
 		}
 		
 	}
 	public void confirmDelete(final String MethodID)
 	{
 		this.m_cancel.setVisible(true);
 		_PreviewPanel.clear();
 		this._Guide.setText("Are you sure you want to delete the method with id: " + MethodID +"?");
 		final MethodUpdateHandler tmpHandle = this;
 		handleSave = new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				try
 				{
 					MethodService tmpSvc = MethodService.get();
 					if(tmpSvc != null)
 					{
 						tmpSvc.deleteMethod(MethodID, tmpHandle);
 							
 					}
 					else
 					{
 						throw new ServiceRegistry.ServiceNotFoundException(ServiceNames.Methods);
 					}
 				}
 				catch (ServiceRegistry.ServiceNotFoundException e)
 				{
 					GWT.log("Couldn't find Method service");
 				}
 				
 			}
 			
 		};
 		registration = this.m_ok.addClickHandler(handleSave);
 		_PreviewPanel.add(_Guide);
 	}
 	@Override
 	public void onDelete(String Response) {
 		AbsolutePanel _w_response = new AbsolutePanel();
 		Label _r_text = new Label();
 		
 		_r_text.setText("Success: Your method has been deleted");
 		registration.removeHandler();
 		
 		this._PreviewPanel.clear();
 		_w_response.add(_r_text);
 		this._PreviewPanel.add(_w_response);
 		
 		this.m_windowSettings.windowTitle = "METHOD SAVED";
 		Desktop d = Desktop.get();		
 		//d.setSize("200px", "200px");
 		
 		//this.m_ok.(handleSave);
 		d.show("MethodPreviewWindow",true);
 		/*if(response=="Delete")
 		{
 			this.eventBus.fireEvent(new MenuChangeEvent("Refresh-Restart"));
 		}	
 		else
 		{
 			this.eventBus.fireEvent(new MenuChangeEvent("Refresh"));
 		}*/
 		this.eventBus.fireEvent(new MenuChangeEvent("Refresh",""));
 		
 	}
 	
 
 }
