 package de.uni_leipzig.simba.saim.gui.widget.form;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import org.vaadin.jonatan.contexthelp.ContextHelp;
 import com.vaadin.event.FieldEvents.BlurEvent;
 import com.vaadin.event.FieldEvents.BlurListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TextField;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.core.DefaultEndpointLoader;
 import de.uni_leipzig.simba.saim.core.Endpoints;
 import de.uni_leipzig.simba.saim.gui.validator.EndpointURLValidator;
 import de.uni_leipzig.simba.saim.gui.validator.PageSizeValidator;
 /**
  * Allows the user to manually set the properties of a knowledge base, which are
  * endpoint URL, graph URI, page size, restrictions
  */
 @SuppressWarnings("serial")
 public class KBInfoForm extends Form
 {
 	private final Messages messages;
 
 	protected final static String		WIDTH			= "35em";									//$NON-NLS-1$
 	protected final ComboBox			presetComboBox;
 	protected final ComboBox			url;
 	protected final TextField			id;
 	protected final TextField			graph;
 	protected final TextField			pageSize;
 	protected final TextField			textFields[];
 	protected final Button				next;
 	protected final Component			components[];
 	KBInfo								kbInfo;
 
 	final EndpointURLValidator validator;
 
 	/** the knowledge base presets */
 	protected final Map<String, KBInfo>	presetToKB = new HashMap<String, KBInfo>();
 
 	public void close()
 	{
 		validator.close();
 	}
 	/**
 	 * Constructor to set default values of the fields.
 	 * @param title
 	 * @param defaultValues
 	 * @param messages
 	 */
 	public KBInfoForm(String title, KBInfo defaultValues,final Messages messages)
 	{
 		this.messages=messages;
 		presetComboBox	= new ComboBox(messages.getString("preset"));		//$NON-NLS-1$
 		url				= new ComboBox(messages.getString("endpointurl")); //$NON-NLS-1$
 		id				= new TextField(messages.getString("idnamespace")); //$NON-NLS-1$
 		graph			= new TextField(messages.getString("graph"));		//$NON-NLS-1$
 		pageSize		= new TextField("Page size", "-1");				//$NON-NLS-1$ //$NON-NLS-2$
 		textFields		= new TextField[] { graph, id, pageSize };
 		next			= new Button(messages.getString("ok"));			//$NON-NLS-1$
 		components		= new Component[] { url, graph,pageSize, next};
 		validator 		= new EndpointURLValidator(url,messages);
 		this.setImmediate(true);
 		this.setCaption(title);
 		this.setWidth(WIDTH);
 		this.setStyleName("kbInfoForm");
 		// Have a button bar in the footer.
 		HorizontalLayout buttonBar = new HorizontalLayout();
 		// buttonBar.setHeight("25px");
 		getFooter().addComponent(buttonBar);
 		// Add an Ok (commit), Reset (discard), and Cancel buttons
 		setValidationVisible(true);
 		buttonBar.addComponent(new Button(messages.getString("reset"), this, "reset")); //$NON-NLS-1$ //$NON-NLS-2$
 		getLayout().setMargin(true);
 
 		setupContextHelp();
 		if (defaultValues != null)
 		{
 			kbInfo = defaultValues;
 		}
 		else
 		{
 			kbInfo = new KBInfo();
 			kbInfo.endpoint = ""; //$NON-NLS-1$
 		}
 		presets();	setupContextHelp();
 		addFormFields();
 	}
 
 	protected void presets()
 	{
 		presetComboBox.setMultiSelect(false);
 		presetComboBox.setRequired(false);
 		presetComboBox.setWidth("100%"); //$NON-NLS-1$
 		presetComboBox.setNewItemsAllowed(false);
 
 		for (String preset : presetToKB.keySet()){
 			presetComboBox.addItem(preset);
 		}
 
 		presetComboBox.addListener(new ValueChangeListener()
 		{
 			@Override
 			public void valueChange(
 					com.vaadin.data.Property.ValueChangeEvent event)
 			{
 				if (presetToKB.containsKey(presetComboBox.getValue()))
 				{
 					KBInfo kb = presetToKB.get(presetComboBox.getValue());
 					if (kb.endpoint != null)
 					{
 						url.addItem(kb.endpoint);
 						url.select(kb.endpoint);
 						url.setEnabled(!(kb.endpoint.startsWith("C:\\")||(presetComboBox.getValue().toString().contains("local")&&!kb.endpoint.startsWith("http"))));
 								
 					}
 					if (kb.id != null)
 					{
 						id.setValue(kb.id);
 					}
 					if (kb.graph != null)
 					{
 						graph.setValue(kb.graph);
 					}
 					pageSize.setValue(Integer.toString(kb.pageSize));
 				}
 			}
 
 		});
 	}
 
 	private void addFormFields()
 	{
 		setDefaultEndpoints();
 		presets();
 		addField(messages.getString("presets"), presetComboBox); //$NON-NLS-1$
 		addField(messages.getString("endpointurl"), url); //$NON-NLS-1$
 
 		url.addValidator(validator);
 		url.setRequired(true);
 		url.setRequiredError(messages.getString("endpointurlmaynotbeempty")); //$NON-NLS-1$
 		url.setWidth("100%"); //$NON-NLS-1$
 		url.setReadOnly(false);
 		url.setImmediate(true);
 		url.setTextInputAllowed(true);
 		url.setInvalidAllowed(true);
 		url.setNewItemsAllowed(true);
 		for (String endpoint : Endpoints.endpointArray)
 		{
 			url.addItem(endpoint);
 		}
 		url.addListener(new BlurListener()
 		{
 			@Override
 			public void blur(BlurEvent event)
 			{
 				if (url.isValid())
 				{
 					if (!presetToKB.containsKey(url.getValue()))
 					{
 						try
 						{
 							String val = (String) url.getValue();
 							String idSuggestion = val.substring(val
 									.indexOf("http://") + 7); //$NON-NLS-1$
 							idSuggestion = idSuggestion.substring(0,
 									idSuggestion.indexOf("/")); //$NON-NLS-1$
 							if (idSuggestion.indexOf(".") > 0) //$NON-NLS-1$
 								idSuggestion = idSuggestion.substring(0,
 										idSuggestion.indexOf(".")); //$NON-NLS-1$
 							id.setValue(idSuggestion);
 							// if string is not long enough and thus substring
 							// fails
 						} catch (IndexOutOfBoundsException e)
 						{
 							id.setValue(url.getValue());
 						}
 					}
 				}
 			}
 		});
 		addField(messages.getString("idnamespace"), id); //$NON-NLS-1$
 		addField(messages.getString("graph"), graph); //$NON-NLS-1$
 		addField(messages.getString("pagesize"), pageSize); //$NON-NLS-1$
 		pageSize.addValidator(new PageSizeValidator(messages
 				.getString("pagesizeneedstobeapositiveintegerorminusone"))); //$NON-NLS-1$
 	}
 
 	protected void setupContextHelp()
 	{
 		ContextHelp contextHelp = new ContextHelp();
 		getLayout().addComponent(contextHelp);
 		contextHelp.addHelpForComponent(url,
 				messages.getString("contexthelp.endpointurl")); //$NON-NLS-1$
 		contextHelp.addHelpForComponent(id,
 				messages.getString("contexthelp.idnamespace")); //$NON-NLS-1$
 		contextHelp.addHelpForComponent(graph,
 				messages.getString("contexthelp.graph")); //$NON-NLS-1$
 		contextHelp.addHelpForComponent(pageSize,
 				messages.getString("contexthelp.pagesize")); //$NON-NLS-1$
 		// contextHelp.setFollowFocus(true);
 	}
 
 	public void reset()
 	{
 		for (TextField field : textFields)
 		{
 			field.setValue(""); //$NON-NLS-1$
 		}
 	}
 
 	public KBInfo getKBInfo()
 	{
 		KBInfo kbInfo = new KBInfo();
 		kbInfo.id = id.getValue().toString();
 		kbInfo.endpoint = url.getValue().toString();		
 		if (graph.getValue() != null) kbInfo.graph = graph.getValue()
 				.toString();
		int pageSizeInt = Integer.parseInt("" + pageSize.getValue());
 		kbInfo.pageSize = pageSizeInt;
 		return kbInfo;
 	}
 
 	private void setDefaultEndpoints()
 	{
 		presetToKB.clear();
 		presetComboBox.removeAllItems();
 
 		Set<String> sortedKeys =  new TreeSet<String>();
 		sortedKeys.addAll(DefaultEndpointLoader.getDefaultEndpoints().keySet());
 		for(String key : sortedKeys){
 			presetToKB.put(key, DefaultEndpointLoader.getDefaultEndpoints().get(key));
 			presetComboBox.addItem(key);
 		}
 	}
 
 	public void setValuesFromKBInfo(KBInfo info)
 	{
 		this.kbInfo = info;
 		if(info.endpoint!=null)
 		{
 			url.addItem(kbInfo.endpoint);
 			url.setValue(kbInfo.endpoint);
 		} else url.setValue("");
 		if(info.graph!=null)	{graph.setValue(kbInfo.graph);} else {graph.setValue("");}
 		if(info.id!=null)	{id.setValue(kbInfo.id);} else {id.setValue("");}
 		pageSize.setValue(kbInfo.pageSize);
 	}
 
 }
