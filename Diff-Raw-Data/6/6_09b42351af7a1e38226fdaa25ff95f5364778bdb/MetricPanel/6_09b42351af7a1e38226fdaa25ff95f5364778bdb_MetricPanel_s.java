 package de.uni_leipzig.simba.saim.gui.widget.panel;
 
import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import lombok.Getter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vaadin.cytographer.Cytographer;
 import org.vaadin.cytographer.GraphProperties;
 
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 
 import csplugins.layout.algorithms.force.ForceDirectedLayout;
import cytoscape.Cytoscape;
import cytoscape.visual.VisualPropertyType;
 import de.konrad.commons.sparql.PrefixHelper;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.core.metric.Measure;
 import de.uni_leipzig.simba.saim.core.metric.MetricParser;
 import de.uni_leipzig.simba.saim.core.metric.Node;
 import de.uni_leipzig.simba.saim.core.metric.Operator;
 import de.uni_leipzig.simba.saim.core.metric.Output;
 import de.uni_leipzig.simba.saim.core.metric.Property;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.LearnClickListener;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.SelfConfigClickListener;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.StartMappingListener;
 
 /** Contains instances of ClassMatchingForm and lays them out vertically.*/
 public class MetricPanel extends Panel{    
 
 	final static Logger logger = LoggerFactory.getLogger(MetricPanel.class);
 	private static final long serialVersionUID = 6766679517868840795L;
 	
 	@Getter private final Messages messages;
 	@Getter private Configuration config;
 	
 	private VerticalLayout mainLayout, sourceLayout, targetLayout, metricsLayout, operatorsLayout;
 	private HorizontalLayout layout, buttonLayout;
 	private Set<String> sourceProps,targetProps;
 	private Button selfConfigButton, learnButton, startMapping, setMetric;
 	
 	@Getter private Cytographer cytographer;
 	
 	public MetricPanel(final Messages messages) {	
 		this.messages = messages;
 	}
 	
 	@Override
 	public void attach() {
 		if((SAIMApplication)getApplication()!= null)
 			config = ((SAIMApplication)getApplication()).getConfig();
 		
 		mainLayout = new VerticalLayout();
 		mainLayout.setSpacing(false);
 		mainLayout.setMargin(false);
 		final VerticalLayout accordionLayout = new VerticalLayout();
 		layout = new HorizontalLayout();
 		layout.addComponent(accordionLayout);
 		layout.setSpacing(false);
 		layout.setMargin(false);
 		setContent(mainLayout);
 		mainLayout.addComponent(layout);
 		final ProgressIndicator progress = new ProgressIndicator();
 		progress.setIndeterminate(false);
 		accordionLayout.addComponent(progress);
 		//buttons		
 		mainLayout.addComponent(getButtonLayout());
 		// accordion panel
 		Panel accordionPanel = new Panel();
 		accordionPanel.setHeight("100%"); //$NON-NLS-1$
 		accordionLayout.addComponent(accordionPanel);
 		accordionPanel.setWidth("25em"); //$NON-NLS-1$
 		final Accordion accordion = new Accordion();		
 		accordion.setHeight("100%"); //$NON-NLS-1$
 
 		accordionPanel.addComponent(accordion);
 		
 		sourceLayout =  new VerticalLayout();
 		targetLayout =  new VerticalLayout();
 		metricsLayout =  new VerticalLayout();
 		operatorsLayout =  new VerticalLayout();
 		
 		accordion.addTab(sourceLayout,messages.getString("MetricPanel.sourceproperties")); //$NON-NLS-1$
 		accordion.addTab(targetLayout,messages.getString("MetricPanel.targetproperties")); //$NON-NLS-1$
 		accordion.addTab(metricsLayout,messages.getString("MetricPanel.metrics"));  //$NON-NLS-1$
 		accordion.addTab(operatorsLayout,messages.getString("MetricPanel.operators"));	 //$NON-NLS-1$
 		// add Cytographer
 		cytographer = makeCytographer();
 		layout.addComponent(cytographer);
 		
 		new Thread(){			
 			@Override
 			public void run(){
 				//	performPropertyMapping();
 				getAllProps();
 
 				for(String s : sourceProps) {
 					final Label check = new Label(s);
 					sourceLayout.addComponent(check); 
 				}
 						
 				for(String t : targetProps) {
 					final Label check = new Label(t);
 					targetLayout.addComponent(check);
 				}
 				accordionLayout.removeComponent(progress);
 				progress.setEnabled(false);
 			}
 		}.start();
 //		metricsLayout.addComponent( new Label(messages.getString("MetricPanel.0"))); 
 //		operatorsLayout.addComponent( new Label(messages.getString("MetricPanel.8"))); 		
 		for(String label : Measure.identifiers)
 			metricsLayout.addComponent( new Label(label)); 
 		for(String label : Operator.identifiers)
 			operatorsLayout.addComponent( new Label(label)); 	
 		
 		sourceLayout.addListener(   new AccordionLayoutClickListener(cytographer,GraphProperties.Shape.SOURCE,   config));
 		targetLayout.addListener(   new AccordionLayoutClickListener(cytographer,GraphProperties.Shape.TARGET,   config));
 		metricsLayout.addListener(  new AccordionLayoutClickListener(cytographer,GraphProperties.Shape.METRIC,   config));
 		operatorsLayout.addListener(new AccordionLayoutClickListener(cytographer,GraphProperties.Shape.OPERATOR, config));
 		
 		this.checkButtons();
 	}
 	
 	private Cytographer makeCytographer(){
 
 		final int HEIGHT = 450;
 		final int WIDTH = 800;		
 		cytographer = new Cytographer(WIDTH, HEIGHT,getApplication());
 				
 		String metricExpression = config.getMetricExpression();
 		if( metricExpression != null){
 			Output o = MetricParser.parse(metricExpression, config.getSource().var.replaceAll("\\?", ""));
 			o.param1 = config.getAcceptanceThreshold();
 			o.param2 = config.getVerificationThreshold();
 			makeMetricRecursive(o, -1); 
 			cytographer.applyLayoutAlgorithm(new ForceDirectedLayout());		
 			cytographer.repaintGraph();
 		}else{
 			cytographer.addNode(new Output().id, WIDTH/2, HEIGHT/2, GraphProperties.Shape.OUTPUT);
 			cytographer.repaintGraph();
 		}
 		return cytographer;		
 	}
 
 	/**
 	 * Recursive  function to create a graphical representation out of a output node.
 	 * @param n Call with the Output (root) node.
 	 * @param parentId On call just use an arbitrary value: 
 	 */
 	private void makeMetricRecursive(Node n, int parentId) {
 		if(n.getClass()==Output.class) {
 			parentId = addNode(n);
 		}
 		HashMap<Integer, Node> cList = new HashMap<Integer, Node>();
 		for(Node c : n.getChilds()) {
 				cList.put(addNode(c), c);
 		}
 		for(Entry<Integer, Node> c : cList.entrySet()) {
 			addEdge(parentId, c.getKey());
 		}
 		for(Entry<Integer, Node> c : cList.entrySet()) {
 			makeMetricRecursive(c.getValue(), c.getKey());
 		}
 	}
 	
 	private int addNode(Node n){
 		Integer id = null;
 		// make node
 		if(n instanceof Output){
 			id= cytographer.addNode(new Output().id, 0, 0, GraphProperties.Shape.OUTPUT);
 			List<Object> l = new ArrayList<Object>();
 			l.add(((Output)n).param1);
 			l.add(((Output)n).param2);
 			cytographer.setNodeMetadata(id+"", l);
 		}else if(n instanceof Operator){
 			id= cytographer.addNode(((Operator)n).id, 0, 0, GraphProperties.Shape.OPERATOR);
 			List<Object> l = new ArrayList<Object>();
 			l.add(((Operator)n).param1);
 			l.add(((Operator)n).param2);		
 			cytographer.setNodeMetadata(id+"", l);
 			
 		}else if(n instanceof Property){
 			if(((Property)n).getOrigin().equals(Property.Origin.TARGET)){
 				id=cytographer.addNode(((Property)n).id, 0, 0, GraphProperties.Shape.TARGET);
 			}else{
 				id=cytographer.addNode(((Property)n).id, 0, 0, GraphProperties.Shape.SOURCE);
 			}
 			
 		}else if(n instanceof Measure){
 			id=cytographer.addNode(((Measure)n).id, 0, 0, GraphProperties.Shape.METRIC);
 		}
 		return id;
 	}
 	
 	private void addEdge(int nID,int cID){
 		cytographer.createAnEdge(nID, cID, new String(nID+"_to_"+cID));		
 	}
 	
 	private void getAllProps() {
 		sourceProps = new HashSet<String>();
 		targetProps = new HashSet<String>();
 //		Configuration config = Configuration.getInstance();
 //		if(config.isLocal) {
 //			logger.info("Local data - using specified properties");
 			if(config != null)
 			if( config.getSource() != null && config.getSource().properties != null && config.getSource().properties.size()>0 &&
 					config.getTarget() != null && config.getTarget().properties != null && config.getTarget().properties.size()>0) {
 				for(String prop : config.getSource().properties) {
 					if(prop.trim().length()>0) {
 						String s_abr=PrefixHelper.abbreviate(prop);
 						if(!sourceProps.contains(s_abr))
 							sourceProps.add(s_abr);
 					}
 				}
 				
 				for(String prop : config.getTarget().properties) {
 					if(prop.trim().length()>0) {
 						String s_abr=PrefixHelper.abbreviate(prop);
 						if(!targetProps.contains(s_abr))
 							targetProps.add(s_abr);
 					}
 				}
 				selfConfigButton.setEnabled(true);
 			}			
 			return;
 	}
 	
 	/**
 	 * Creates the button is the lower window.
 	 * @return
 	 */
 	public Layout getButtonLayout() {
 		this.setMetric = new Button(messages.getString("MetricPanel.setmetricbutton"));
 		setMetric.setEnabled(true);
 		setMetric.addListener(new ClickListener() {			
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 2349781868228639555L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				setMetricFromGraph();
 			}
 		});
 		
 		selfConfigButton = new Button(messages.getString("MetricPanel.startselfconfigbutton")); //$NON-NLS-1$
 		selfConfigButton.setEnabled(false);
 		selfConfigButton.addListener(new SelfConfigClickListener((SAIMApplication) getApplication(), messages));
 		
 		this.learnButton = new Button(messages.getString("MetricPanel.learnmetricbutton")); //$NON-NLS-1$
 		learnButton.setEnabled(false);
 		learnButton.addListener(new LearnClickListener((SAIMApplication) getApplication(), messages));
 		
 		this.startMapping = new Button(messages.getString("MetricPanel.startmappingbutton")); //$NON-NLS-1$
 		startMapping.setEnabled(false);
 		startMapping.addListener(new StartMappingListener((SAIMApplication) getApplication(), messages));
 		
 		buttonLayout = new HorizontalLayout();
 		buttonLayout.addComponent(setMetric);
 		buttonLayout.addComponent(selfConfigButton);
 		buttonLayout.addComponent(learnButton);
 		buttonLayout.addComponent(startMapping);
 		return buttonLayout;		
 	}
 	
 	/**
 	 * Method to set Metric from the graph.
 	 */
 	protected void setMetricFromGraph() {
 		if(!cytographer.getMetric().isComplete()) {
 			getApplication().getMainWindow().showNotification(messages.getString("MetricPanel.settingnotablenotcomplete")); //$NON-NLS-1$
 		} else {
 			Node node =  cytographer.getMetric();
 			String expr = cytographer.getMetric().toString();
 			config.setMetricExpression(expr);
 			System.out.println(node.param1+" - "+node.param2); //$NON-NLS-1$
 			if(node.param1 != null && node.param2 != null) {
 				config.setAcceptanceThreshold(node.param1);
 				config.setVerificationThreshold(node.param2);
 				getApplication().getMainWindow().showNotification("Setting: "+expr+ "with thresholds "+node.param1+" / "+node.param2); //$NON-NLS-1$
 				checkButtons();
 			}
 			else {
 				getApplication().getMainWindow().showNotification(messages.getString("MetricPanel.settingnotablenothreholds")); //$NON-NLS-1$
 			}
 		}		
 	}
 
 	/**
 	 * Checks whether the Buttons (selfconfig, learning and startMapping) could be activated.
 	 */
 	public void checkButtons() {
 		if((SAIMApplication)getApplication()!=null) {
 			Configuration config = ((SAIMApplication)getApplication()).getConfig();
 			if( config.getSource() != null && config.getSource().properties != null && config.getSource().properties.size()>0 &&
 					config.getTarget() != null && config.getTarget().properties != null && config.getTarget().properties.size()>0) {
 				selfConfigButton.setEnabled(true);
 				if(config.getMetricExpression() != null && config.getMetricExpression().length()>0) {
 					learnButton.setEnabled(true);
 					startMapping.setEnabled(true);
 				}
 			}		
 		}
 	}
 
 	/**Listener to react on clicks in the accordion panel.*/
 	class AccordionLayoutClickListener implements LayoutClickListener
 	{
 		private static final long serialVersionUID = -3498649095113131161L;
 		private Cytographer cytographer;
 	
 		private GraphProperties.Shape shape;
 		private Configuration config;
 		
 		public AccordionLayoutClickListener(Cytographer cytographer,GraphProperties.Shape shape, Configuration config)
 		{
 			this.cytographer = cytographer;
 			this.shape = shape;
 			this.config = config;
 		}
 		
 		@Override
 		public void layoutClick(LayoutClickEvent event) {
 			// its left button
 			if(event.getButtonName().equalsIgnoreCase("left") && event.getClickedComponent() instanceof Label ){ //$NON-NLS-1$
 				String label = ((Label)event.getClickedComponent()).getValue().toString();
 				int x = (int)cytographer.getWidth()/2;
 				int y = (int)cytographer.getHeight()/2;
 				
 				switch(shape){
 				case SOURCE :{
 					String pref = config.getSource().var.replaceAll("\\?", ""); //$NON-NLS-1$
 					cytographer.addNode(pref+"."+label, x,y, shape); //$NON-NLS-1$
 					cytographer.addDefaultProperty(label, config.getSource());
 					break;
 				}
 				case TARGET : {
 					String pref = config.getTarget().var.replaceAll("\\?", ""); //$NON-NLS-1$
 					cytographer.addNode(pref+"."+label, x,y, shape); //$NON-NLS-1$
 					cytographer.addDefaultProperty(label, config.getTarget());
 					break;
 				}
 				default :
 						cytographer.addNode(label, x,y, shape);
 				}
 				// repaint
 				cytographer.repaintGraph();
 			}
 		}
 	} // end of class AccordionClickListener
 }// end of MetricPanel
