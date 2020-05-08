 package monbulk.MetadataEditor;
 
 import java.util.ArrayList;
 
 import monbulk.shared.Services.Metadata;
 import monbulk.client.desktop.Desktop;
 import monbulk.client.event.*;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 public class AttributesPanel extends ElementPanel implements WindowEventHandler
 {
 	private static AttributesPanelUiBinder uiBinder = GWT.create(AttributesPanelUiBinder.class);
 	interface AttributesPanelUiBinder extends UiBinder<Widget, AttributesPanel> { }
 
 	@UiField ListBox m_attributes;
 	@UiField Button m_add;
 	@UiField Button m_remove;
 	@UiField Button m_edit;
 	
 	private Metadata.Element m_newElement;
 
 	public AttributesPanel()
 	{
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	public void set(Metadata.Element element)
 	{
 		super.set(element);
 		m_attributes.clear();
 		
 		for (Metadata.Element e : element.getAttributes())
 		{
 			String name = e.getSetting("name", "");
 			if (name.length() > 0)
 			{
 				m_attributes.addItem(name);
 			}
 		}
 		
 		setButtonState(false);
 	}
 
 	public void update(Metadata.Element element)
 	{
 	}
 
 	public Metadata.ElementTypes getType()
 	{
 		return Metadata.ElementTypes.Attribute;
 	}
 	
 	@UiHandler("m_attributes")
 	void onAttributeSelected(ChangeEvent event)
 	{
 		setButtonState(true);
 	}
 	
 	private void showEditor(Metadata.Element element)
 	{
		AttributesEditor editor = (AttributesEditor)Desktop.get().show("AttributesEditor", true);
 		editor.setElement(element);
 	}
 
 	@UiHandler("m_edit")
 	void onEditClicked(ClickEvent event)
 	{
 		int index = m_attributes.getSelectedIndex();
 		ArrayList<Metadata.Element> attributes = m_element.getAttributes();
 		if (index >= 0 && index < attributes.size()) 
 		{
 			// Show the editor and set the element to edit.
 			Metadata.Element element = attributes.get(index);
 			showEditor(element);
 		}
 	}
 	
 	@UiHandler("m_remove")
 	void onRemoveClicked(ClickEvent event)
 	{
 		int index = m_attributes.getSelectedIndex();
 		ArrayList<Metadata.Element> attributes = m_element.getAttributes();
 		if (index >= 0 && index < attributes.size())
 		{
 			attributes.remove(index);
 			m_attributes.removeItem(index);
 			
 			index = index < attributes.size() ? index : (attributes.size() - 1);
 			if (index >= 0)
 			{
 				m_attributes.setSelectedIndex(index);
 			}
 			else
 			{
 				setButtonState(false);
 			}
 		}
 	}
 	
 	@UiHandler("m_add")
 	void onAddClicked(ClickEvent event)
 	{
 		try
 		{
 			m_newElement = Metadata.createElement("String", "attribute", "An attribute", true);
 			Desktop.get().getEventBus().addHandler(WindowEvent.TYPE, this);
 			showEditor(m_newElement);
 		}
 		catch (Exception e)
 		{
 		}
 	}
 	
 	private void setButtonState(boolean enabled)
 	{
 		m_edit.setEnabled(enabled);
 		m_remove.setEnabled(enabled);
 	}
 	
 	public void onWindowEvent(WindowEvent event)
 	{
 		if (event.getWindowId().equals("AttributesEditor"))
 		{
 			Desktop.get().getEventBus().removeHandler(WindowEvent.TYPE, this);
 
 			switch (event.getEventType())
 			{
 				case Ok:
 				{
 					// Add the new attribute to the list.
 					String name = m_newElement.getSetting("name", "");
 					if (name.length() > 0)
 					{
 						m_attributes.addItem(name);
 						m_element.getAttributes().add(m_newElement);
 					}
 					break;
 				}
 				
 				case Cancel:
 				{
 					break;
 				}
 			}
 			
 			m_newElement = null;
 		}
 	}
 }
