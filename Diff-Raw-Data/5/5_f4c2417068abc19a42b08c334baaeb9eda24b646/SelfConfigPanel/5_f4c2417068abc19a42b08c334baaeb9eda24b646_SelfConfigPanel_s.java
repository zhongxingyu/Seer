 package de.uni_leipzig.simba.saim.gui.widget.panel;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.wolfie.refresher.Refresher;
 import com.github.wolfie.refresher.Refresher.RefreshListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.VerticalLayout;
 
 import de.uni_leipzig.simba.cache.HybridCache;
 import de.uni_leipzig.simba.genetics.util.PropertyMapping;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.gui.widget.form.SelfConfigMeshBasedBean;
 import de.uni_leipzig.simba.saim.gui.widget.form.SelfConfigMeshBasedForm;
 import de.uni_leipzig.simba.selfconfig.ComplexClassifier;
 import de.uni_leipzig.simba.selfconfig.MeshBasedSelfConfigurator;
 import de.uni_leipzig.simba.selfconfig.SimpleClassifier;
 /**
  * Displays self configuration panel. Loads data as in Configuration specified and runs the 
  * MeshBasedSelfConfigurator.
  * @author Lyko
  *
  */
 public class SelfConfigPanel extends PerformPanel{
 	private static final Logger logger = LoggerFactory.getLogger(SelfConfigPanel.class);
 	private final Messages messages;
 	private Layout mainLayout;
 	MeshBasedSelfConfigurator bsc;
 	List<SimpleClassifier> classifiers;
 	ComplexClassifier cc;
 	final ProgressIndicator indicator = new ProgressIndicator();
 	final Panel stepPanel = new Panel();
 	Panel resultPanel;
 	Select resultSelect = new Select();
 	String generatedMetricexpression = "";
 	Thread thread;
 	Configuration config;
 	
 	// to config self config
 	SelfConfigMeshBasedBean bean = new SelfConfigMeshBasedBean();
 	SelfConfigMeshBasedForm form;
 	Button start;
 	
 	/**
 	 * Constructor to may embed Panel in a parent component, e.g. an existing WizardStep Component.
 	 * @param parentComponent
 	 */
 	public SelfConfigPanel(final Messages messages) {this.messages=messages;}
 	
 	@Override
 	public void attach() {
 		this.config = ((SAIMApplication)getApplication()).getConfig();
 		init();
 	}
 	
 	/**
 	 * Initialize all Panel.
 	 */
 	private void init() {
 		this.setCaption(messages.getString("SelfConfigPanel.caption")); //$NON-NLS-1$
 		mainLayout = new VerticalLayout();
 		this.setContent(mainLayout);
 		Label descriptor = new Label(messages.getString("SelfConfigPanel.description")); //$NON-NLS-1$
 		mainLayout.addComponent(descriptor);
 		Refresher refresher = new Refresher();
 		SelfConfigRefreshListener listener = new SelfConfigRefreshListener();
 		refresher.addListener(listener);
 		addComponent(refresher);
 
 		
 		indicator.setCaption(messages.getString("SelfConfigPanel.progress")); //$NON-NLS-1$
 		mainLayout.addComponent(indicator);
 		indicator.setImmediate(true);
 		indicator.setVisible(false);
 		
 		stepPanel.setCaption(messages.getString("SelfConfigPanel.panelcaption")); //$NON-NLS-1$
 		mainLayout.addComponent(stepPanel);
 		stepPanel.setVisible(false);
 		
 		resultSelect.setCaption(messages.getString("SelfConfigPanel.classifierlistcaption")); //$NON-NLS-1$
 		resultSelect.setNullSelectionAllowed(false);
 		resultSelect.setVisible(false);
 		
 		mainLayout.addComponent(form = new SelfConfigMeshBasedForm(bean, messages));
 		start = new Button(messages.getString("SelfConfigPanel.startbutton"));
 		start.addListener(new ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				mainLayout.removeComponent(start);
 				indicator.setVisible(true);
 				stepPanel.setVisible(true);
 				performSelfConfiguration();
 				
 			}
 		});
 		mainLayout.addComponent(start);
 		
 		
 		resultPanel = new Panel();
 		mainLayout.addComponent(resultPanel);
 		// Buttons
 		VerticalLayout resultLayout = new VerticalLayout();
 		resultLayout.addComponent(resultSelect);
 		resultPanel.setContent(resultLayout);		
 
 		
 	
 	}
 	
 	/**
 	 * Performs SelfConfiguration
 	 */
 	protected void performSelfConfiguration() {
 		mainLayout.addComponent(indicator);
 		mainLayout.addComponent(stepPanel);
 		thread = new Thread() {
 			public void run() {
 
 				float steps = 5f;
 				indicator.setValue(new Float(1f/steps));
 				indicator.requestRepaint();
 				stepPanel.setCaption(messages.getString("SelfConfigPanel.sourcecache")); //$NON-NLS-1$
 				HybridCache sourceCache = HybridCache.getData(config.getSource());
 				indicator.setValue(new Float(2f/steps));
 				indicator.requestRepaint();
 				stepPanel.setCaption(messages.getString("SelfConfigPanel.targetcache")); //$NON-NLS-1$
 				HybridCache targetCache = HybridCache.getData(config.getTarget());
 				indicator.setValue(new Float(3f/steps));
 				stepPanel.setCaption(messages.getString("SelfConfigPanel.performselfconfig")); //$NON-NLS-1$
 				
 				bsc = new MeshBasedSelfConfigurator(sourceCache, targetCache, bean.getMinCoverage(), bean.getBeta());
 				classifiers = bsc.getBestInitialClassifiers();
 				showSimpleClassifiers();
 				indicator.setValue(new Float(4f/steps));
 				stepPanel.setCaption(messages.getString("SelfConfigPanel.gotinitialclassifiers")); //$NON-NLS-1$
 				if(classifiers.size()>0) {
 					classifiers = bsc.learnClassifer(classifiers);
 					//@TODO interface to change parameters
 					cc = bsc.getZoomedHillTop(bean.getGridPoints(), bean.getIterations(), classifiers);
 					System.out.println(cc);
 					for(SimpleClassifier co:cc.classifiers) {
 						System.out.println(co);
 					}
 //					classifiers = cc.classifiers;
 					indicator.setValue(new Float(5f/steps));
 					stepPanel.setCaption(messages.getString("SelfConfigPanel.complexclassifiercaption")); //$NON-NLS-1$
 					generatedMetricexpression = generateMetric(cc.classifiers, "");
 					showComplexClassifier();
 					
 					config.setMetricExpression(generatedMetricexpression);
 					config.setAcceptanceThreshold(getThreshold(cc.classifiers));
 				} else {
 					indicator.setValue(new Float(5f/steps));
 					stepPanel.setCaption(messages.getString("SelfConfigPanel.nosimpleclassifiers"));
 				}
 			}
 		};
 		thread.start();
 	}
 	
 	/**
 	 * Method to show results after initialization.
 	 */
 	private void showSimpleClassifiers() {
 //		Configuration config = Configuration.getInstance();
 		if(classifiers.size()>0) {
 			logger.info("Replacing property mapping.");
 //			config.propertyMapping = new PropertyMapping();
 		}
 		for(SimpleClassifier cl : classifiers) {
 			resultSelect.addItem(cl);
 			resultSelect.select(cl);
 //			if(cl.measure.equalsIgnoreCase("euclidean")) {
 //				logger.info("Adding number propertyMatch between: "+cl.sourceProperty +" - "+ cl.targetProperty);
 //				config.addPropertiesMatch(cl.sourceProperty, cl.targetProperty, false);
 //			}else {
 //				config.addPropertiesMatch(cl.sourceProperty, cl.targetProperty, true);
 //				logger.info("Adding string propertyMatch between: "+cl.sourceProperty +" - "+ cl.targetProperty);
 //			}
 		}
 		resultSelect.setVisible(true);		
 	}
 	
 	/**Method shows complex classifier*/
 	private void showComplexClassifier() {
 		if(this.cc == null)
 			return;
 		
 		Panel result = new Panel("Classifier: "+generatedMetricexpression
 				+ " with pseudo f-measure="+cc.fMeasure);
 		stepPanel.addComponent(result);
 		
 //		generateMetric.setEnabled(true);
 	}
 	
 	/**To enable refreshing while multithreading*/
 	public class SelfConfigRefreshListener implements RefreshListener  {
 		boolean running = true; 
 		private static final long serialVersionUID = -8765221895426102605L;		    
 		@Override 
 		public void refresh(final Refresher source)	{
 			if(!running) {
 				removeComponent(source);
 				source.setEnabled(false);
 			}
 		}
 	}
 	
 	/**Implements Listener for generateMetrik Button*/
 	class GenerateMetricButtonClickListener implements Button.ClickListener {
 		Layout l;
 		public GenerateMetricButtonClickListener(Layout content) {
 			l = content;
 		}
 		
 		@Override
 		public void buttonClick(ClickEvent event) {			
 			if(cc.classifiers.size()==1) {
 				config.setAcceptanceThreshold(cc.classifiers.get(0).threshold);
 			}
 			String metric = generatedMetricexpression;
 		
 			config.setMetricExpression(metric);
 //			Configuration.getInstance().setAcceptanceThreshold(cl.threshold);
 			l.removeAllComponents();
 			l.addComponent(new ExecutionPanel(messages));
 		}
 	}
 	
 	
 	/**
 	 * Generates Metric out of one SimpleClassifier
 	 * @param sl 
 	 * @return String like: <i>measure(sourceProp,targetProp)|threshold</i>
 	 */
 	private String generateMetric(SimpleClassifier sl) {
 		KBInfo source=config.getSource();
 		KBInfo target=config.getTarget();
 		String metric = ""; //$NON-NLS-1$
 		
 		metric += sl.measure+"("+source.var.replaceAll("\\?", "")+"."+sl.sourceProperty; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 		metric +=","+target.var.replaceAll("\\?", "")+"."+sl.targetProperty+")|"+sl.threshold; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		return metric;
 	}
 
 	/**
 	 * Recursive function to produce metric out of complex classifier.
 	 * @param sCList List of simple classifier which remain to be added.
 	 * @param expr The metric expression so far.
 	 * @return
 	 */
 	private String generateMetric(List<SimpleClassifier> originalCCList, String expr) {
 		// need to copy them
 		List<SimpleClassifier> sCList = new LinkedList<SimpleClassifier>();
 		for(SimpleClassifier sC: originalCCList)
 			sCList.add(sC);
 		
 		if(sCList.size()==0)
 			return expr;
 		if(expr.length() == 0) {// nothing generated before
 			if(sCList.size()==1) {
 				String metric = generateMetric(sCList.get(0));
 				return metric.substring(0, metric.lastIndexOf("|"));
 			}
 			else {// recursive
 				String nestedExpr = "AND("+generateMetric(sCList.remove(0))+","+generateMetric(sCList.remove(0))+")";
 				return generateMetric(sCList, nestedExpr);
 			}
 		} else { // have to combine, recursive
 			String nestedExpr = "AND("+expr+","+generateMetric(sCList.remove(0))+")";
 			return generateMetric(sCList, nestedExpr);			
 		}
 	}
 	
 	private double getThreshold(List<SimpleClassifier> classifiers) {
 		double min = Double.MAX_VALUE;
 		for(SimpleClassifier sC : classifiers) {
 			if(sC.threshold <= min)
 				min = sC.threshold;
 		}
 		return min>1?0.5d:min;
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onClose() {
 		//FIXME save stopping of thread
		if(thread.isAlive())
			thread.stop();
 		((SAIMApplication) SAIMApplication.getInstance()).refresh();
 		
 	}
 	@Override
 	public void start() {
 //		performSelfConfiguration();
 	}
 }
