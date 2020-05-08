 package de.uni_leipzig.simba.saim.gui.widget.Listener;
 
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.gui.widget.panel.selfconfiguration.GenericSelfConfigurationPanel;
 
 /**Listener for SelfConfig button.*/
 public class SelfConfigClickListener extends MetricPanelListeners implements Button.ClickListener
 {		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelfConfigClickListener(SAIMApplication application, Messages messages) {
 		super(application, messages);
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event)
 	{
 		getWindow(new GenericSelfConfigurationPanel(application, messages));
 	}			
 }
