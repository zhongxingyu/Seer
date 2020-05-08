 package de.uni_leipzig.simba.saim.gui.widget.panel;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.terminal.ClassResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.ListSelect;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 import de.konrad.commons.sparql.PrefixHelper;
 import de.konrad.commons.sparql.SPARQLHelper;
 import de.uni_leipzig.simba.data.Mapping;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.learning.query.DefaultPropertyMapper;
 import de.uni_leipzig.simba.learning.query.LabelBasedPropertyMapper;
 import de.uni_leipzig.simba.learning.query.PropertyMapper;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.core.Pair;
 import de.uni_leipzig.simba.saim.gui.widget.PropertyComboBox;
 import de.uni_leipzig.simba.saim.gui.widget.panel.MetricPanel.AccordionLayoutClickListener;
 import de.uni_leipzig.simba.saim.util.SortedMapping;
 /** Contains instances of ClassMatchingForm and lays them out vertically.*/
 @SuppressWarnings("serial")
 public class PropertyMatchingPanel extends Panel
 {
 	private final Messages messages;
 	private static final boolean CACHING = true;
 	private final Layout mainLayout;
 	private static transient final Logger logger = LoggerFactory.getLogger(PropertyMatchingPanel.class);
 	private List<Object[]> rows = new Vector<Object[]>();
 	private ClassResource closeImageResource;
 	private Table table = new Table();
	private List<String> sourceProperties;
	private List<String> targetProperties;
 	private final ProgressIndicator progress = new ProgressIndicator();
 	private Label progressLabel;
 	private boolean listenerActive = true;
 	Button simpleAlgorithm;
 	// to perform automatic mappings
 	Thread propMapper;
 	Button computeStringBasedMapping;
 	Button computeDefaultPropertyMapping;
 	// to display computed ones
 	Layout selectionLayout = new VerticalLayout();
 	Layout resultLayout = new VerticalLayout();
 	ListSelect select;
 	Button useAll;
 
 
 	public PropertyMatchingPanel(final Messages messages) {
 		this.messages=messages;
 		progressLabel = new Label(messages.getString("generatingpropertymatching"));	 //$NON-NLS-1$
 		mainLayout = new VerticalLayout();
 		mainLayout.setStyleName("mainLayout");
 		this.setStyleName("propertyMatchingPanel");
 		select = new ListSelect(messages.getString("PropertyMatchingPanel.computed")); //$NON-NLS-1$
 		useAll  = new Button(messages.getString("PropertyMatchingPanel.useButton")); //$NON-NLS-1$
 		selectionLayout.addComponent(select);
 		useAll.setEnabled(false);
 		mainLayout.addComponent(selectionLayout);
 		mainLayout.addComponent(resultLayout);
 		// Buttons to control property mapping
 		computeStringBasedMapping = new Button("Compute String Based PropertyMapping");
 		computeDefaultPropertyMapping = new Button("Compute Default PropertyMapping");
 		computeStringBasedMapping.addListener(new ControlPropertyMappingListener(true));
 		computeDefaultPropertyMapping.addListener(new ControlPropertyMappingListener(false));
 		HorizontalLayout buttonLayout = new HorizontalLayout();
 		buttonLayout.addComponent(computeStringBasedMapping);
 		buttonLayout.addComponent(computeDefaultPropertyMapping);
 		buttonLayout.addComponent(useAll);
 		selectionLayout.addComponent(buttonLayout);
 
 		setContent(mainLayout);
 		getContent().setWidth("100%"); //$NON-NLS-1$
 		/* Create the table with a caption. */
 
 		//	setupContextHelp();
 		Layout progressLayout = new HorizontalLayout();
 		resultLayout.addComponent(progressLayout);
 
 		progress.setIndeterminate(true);
 		progressLayout.addComponent(progressLabel);
 		progressLayout.addComponent(progress);
 
 		propMapper = new PropertyMappingThread(true);
 		propMapper.start();
 	}
 
 	Cache cache = null;
 
 	//	Thread propMapper = new Thread()
 	//	{
 	//		@Override
 	//		public void run()
 	//		{
 	//			progress.setVisible(true);
 	//			progressLabel.setVisible(true);
 	//			Map<String,HashMap<String,Double>> map = performAutomaticPropertyMapping().map;
 	//			//			try{Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
 	//			//			Map<String,HashMap<String,Double>> map = mockPropertyMap();
 	//			displayPropertyMapping(map);
 	//			progress.setVisible(false);
 	//			progressLabel.setVisible(false);
 	//		}
 	//		/**
 	//		 * Method tries to getpropertyMapping
 	//		 */
 	//		private Mapping performAutomaticPropertyMapping() {
 	//			Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
 	////			PropertyMapper propMap = new PropertyMapper();
 	//			LabelBasedPropertyMapper propMap = new LabelBasedPropertyMapper();
 	//			return propMap.getPropertyMapping(config.getSource().endpoint, config.getTarget().endpoint, config.getSource().getClassOfendpoint(), config.getTarget().getClassOfendpoint());
 	////			return new Mapping();
 	//		}
 	//		private Map<String, HashMap<String, Double>> mockPropertyMap()
 	//		{
 	//			Map<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();
 	//			HashMap<String,Double> value = new HashMap<String,Double>();
 	//			value.put("rdfs:label",0.337); //$NON-NLS-1$
 	//			map.put("rdfs:label",value); //$NON-NLS-1$
 	//			return map;
 	//		}
 	//	};
 
 	private Object columnValue(Object o)
 	{
 		return ((PropertyComboBox)o).getValue();
 	}
 
 	public boolean isValid() // empty and full pairs ok, half full ones are not
 	{
 		for(Object[] row: rows)
 		{if(columnValue(row[0])==null^columnValue(row[1])==null) {return false;}}
 		return true;
 	}
 
 	//	protected void setupContextHelp()
 	//	{
 	//		ContextHelp contextHelp = new ContextHelp();
 	//		getContent().addComponent(contextHelp);
 	//	}
 
 	private String classRestrictionToClass(String classRestriction)
 	{
 		return classRestriction.substring((classRestriction.lastIndexOf(' ')+1)).replace("<","").replace(">",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 	}
 
 	/**	returns all properties (not just the ones from the property matching) that instances of the knowledge base of the
 	 * class restriction specified in the KBInfo have. <b>May break if the class restriction is not set.</b>*/
 	private Set<String> allPropertiesFromKBInfo(KBInfo kb, Model model)
 	{
 		return SPARQLHelper.properties(				
 				kb.endpoint,
 				kb.graph,
 				classRestrictionToClass(kb.getClassRestriction()),
 				model);
 	}
 
 	private List<String> mockAllPropertiesFromKBInfo(KBInfo kb)
 	{
 		return Arrays.asList(new String[] {"rdfs:label","rdfs:schmabel"}); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private class RowChangeListener implements ValueChangeListener
 	{
 		final Object[] row;
 
 		public RowChangeListener(Object[] source)
 		{
 			if(source.length!=3) throw new IllegalArgumentException();
 			this.row=source;
 		}
 
 		@Override
 		public void valueChange(ValueChangeEvent event)
 		{
 			// TODO: find a better solution that is not succeptible to rare possible timing problems
 			// (user may insert at the same time as insertion from displayPropertyMapping() takes place)
 			if(!listenerActive) {return;}
 			if(row==rows.get(rows.size()-1)) // complete last row -> create new
 			{
 				if(!(((PropertyComboBox)row[0]).getValue()==null||((PropertyComboBox)row[1]).getValue()==null))
 				{
 					Object[] row = createTableRow();
 					table.addItem(row,row);
 				}
 			}
 			else if(columnValue(row[0])==null&&columnValue(row[1])==null) // remove empty rows at non-last position
 			{
 				removeRow(row);
 			}
 		}
 	}
 
 	private Object[] createTableRow()
 	{
 		PropertyComboBox sourceBox = new PropertyComboBox(sourceProperties,messages);
 		PropertyComboBox targetBox = new PropertyComboBox(targetProperties,messages);
 		//Embedded closeImage = new Embedded("",closeImageResource);
 		//		CSSInject css = new CSSInject();
 		//		css.setValue(".center {margin-left:auto;margin-right:auto;}");
 		//		this.getContent().addComponent(css);
 		// TODO:  make the button smaller but keep the cross in the middle
 		Button closeRowButton = new Button();
 		//		closeRowButton.setWidth("24px");
 		closeRowButton.setIcon(closeImageResource);
 		//		closeRowButton.setStyleName("center");
 
 		final Object[] row = {sourceBox,targetBox,closeRowButton};
 		sourceBox.addListener(new RowChangeListener(row));
 		targetBox.addListener(new RowChangeListener(row));
 		closeRowButton.addListener(new ClickListener()
 		{
 			@Override
 			public void buttonClick(ClickEvent event)
 			{
 				if(rows.size()>1) {removeRow(row);}
 			}
 		});
 		rows.add(row);
 		return row;
 	}
 
 	private void removeRow(Object[] row)
 	{
 		rows.remove(row);
 		if(!table.removeItem(row));
 	}
 
 	@Override
 	public void attach()
 	{
 		super.attach();
 
 		new Thread() {
 			@Override
 			public void run() {
 				getAllProperties();
 				//		List<String> sourcePropertiesFull = new LinkedList<String>();
 				//		List<String> targetPropertiesFull = new LinkedList<String>();
 				//		sourceProperties = new LinkedList<String>();
 				//		targetProperties = new LinkedList<String>();
 				//		if(Configuration.getInstance().isLocal) {
 				//			for(String prop : Configuration.getInstance().getSource().properties) {
 				//				sourcePropertiesFull.add(prop);
 				//			}
 				//
 				//			for(String prop : Configuration.getInstance().getTarget().properties) {
 				//				targetPropertiesFull.add(prop);
 				//			}
 				//		} else {
 				//			sourcePropertiesFull = allPropertiesFromKBInfo(source);
 				//			targetPropertiesFull = allPropertiesFromKBInfo(target);
 				//		}
 				//		// abbreviate
 				//		for(String prop : sourcePropertiesFull) {
 				//			String s_abr=PrefixHelper.abbreviate(prop);
 				//			sourceProperties.add(s_abr);
 				//		}
 				//
 				//		for(String prop : targetPropertiesFull) {
 				//			String s_abr=PrefixHelper.abbreviate(prop);
 				//			targetProperties.add(s_abr);
 				//		}
 				table.setWidth("97%"); //$NON-NLS-1$
 				mainLayout.addComponent(table);
 				closeImageResource = new ClassResource("img/no_crystal_clear_16.png",getApplication());		 //$NON-NLS-1$
 				/* Define the names and data types of columns.
 				 * The "default value" parameter is meaningless here. */
 				table.addContainerProperty(messages.getString("sourceproperty"), PropertyComboBox.class,  null); //$NON-NLS-1$
 				table.addContainerProperty(messages.getString("targetproperty"), PropertyComboBox.class,  null); //$NON-NLS-1$
 				table.addContainerProperty("", Button.class,  null); //$NON-NLS-1$
 				//table.setColumnWidth("",48);
 
 				/* Add a few items in the table. */
 
 				//		Button closeButton = new Button();
 				//		closeButton.setWidth("16px");
 				//		closeButton.setHeight("16px");
 				//		closeButton.setIcon(resource);
 				Object[] row = createTableRow();
 				table.addItem(row,row);
 				//addComponent(new PropertyComboBox(mockAllPropertiesFromKBInfo(null)));
 
 			}
 		}.start();
 
 	}
 
 	//	private class PropertyPairPanel extends Panel
 	//	{
 	//		public PropertyPairPanel()
 	//		{
 	//			setContent(new HorizontalLayout());
 	//			ComboBox sourcePropertyComboBox = new ComboBox();
 	//			sourcePropertyComboBox.setWidth("50%");
 	//			ComboBox targetPropertyComboBox = new ComboBox();
 	//			targetPropertyComboBox.setWidth("50%");
 	//			addComponent(sourcePropertyComboBox);
 	//			addComponent(targetPropertyComboBox);
 	//		}
 	//	}
 
 	/**
 	 * Method to add Properties to according KBInfo.
 	 * @param s URI of the property. May or may not be abbreviated.
 	 * @param info KBInfo of endpoint property belongs to.
 	 */
 	private void addProperty(String s, KBInfo info) {
 		String prop;
 //		System.out.println("Add property "+s+" to "+info.id); //$NON-NLS-1$ //$NON-NLS-2$
 		if(s.startsWith("http:")) {//do not have a prefix, so we generate one //$NON-NLS-1$
 			PrefixHelper.generatePrefix(s);
 			prop = PrefixHelper.abbreviate(s);
 		} else {// have the prefix already
 			prop = s;
 			s = PrefixHelper.expand(s);
 		}
 		if(!info.properties.contains(prop))
 			info.properties.add(prop);
 		info.functions.put(prop, new HashMap<String,String>()); //$NON-NLS-1$
 		info.functions.get(prop).put(prop, "nolang->lowercase");
 		String base = PrefixHelper.getBase(s);
 		info.prefixes.put(PrefixHelper.getPrefix(base), PrefixHelper.getURI(PrefixHelper.getPrefix(base)));
 
 		LoggerFactory.getLogger(AccordionLayoutClickListener.class).info(info.var+": adding property: "+prop+" with prefix "+PrefixHelper.getPrefix(base)+" - "+PrefixHelper.getURI(PrefixHelper.getPrefix(base))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 	private void getAllProperties() {
 		logger.info("Started getAllProperties()");
		sourceProperties = new LinkedList<String>();
		targetProperties = new LinkedList<String>();
 		Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
 		if(config.isLocal) {
 			logger.info("Local data - using specified properties"); //$NON-NLS-1$
 			for(String prop : config.getSource().properties) {
 				String s_abr=PrefixHelper.abbreviate(prop);
 				sourceProperties.add(s_abr);
 			}
 
 			for(String prop : config.getTarget().properties) {
 				String s_abr=PrefixHelper.abbreviate(prop);
 				targetProperties.add(s_abr);
 			}
 			return;
 		}
 		Set<String> propListSource = null;
 		Set<String> propListTarget = null;
 		KBInfo info = config.getSource();
 		String className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);//$NON-NLS-1$
 	
 		
 		
 		String classNameExp = expandClassName(info, className);
 		propListSource = SPARQLHelper.properties(info.endpoint, info.graph, classNameExp, config.sourceModel);
 		logger.info("Got "+propListSource.size()+ " source props"); //$NON-NLS-1$ //$NON-NLS-2$
 		info = config.getTarget();
 		className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8); //$NON-NLS-1$
 	
 		classNameExp = expandClassName(info, className);
 		propListTarget = SPARQLHelper.properties(info.endpoint, info.graph, classNameExp, config.targetModel);
 		logger.info("Got "+propListTarget.size()+ " target props"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		for(String prop : propListSource) {
 			String s_abr=PrefixHelper.abbreviate(prop);
 			try {
 				sourceProperties.add(URLDecoder.decode(s_abr, "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				sourceProperties.add(s_abr);
 			}
 		}
 		for(String prop : propListTarget) {
 			String s_abr=PrefixHelper.abbreviate(prop);
 			try {
 				targetProperties.add(URLDecoder.decode(s_abr, "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				targetProperties.add(s_abr);
 			}
 		}
 	}
 
 	//TODO quick fix to support multiple computations
 	UseComputedClickListener useComputedListener;
 	Property.ValueChangeListener selectListener = new Property.ValueChangeListener() {
 		@Override
 		public void valueChange(ValueChangeEvent event) {
 			ClassMatchItem item = (ClassMatchItem) select.getValue();
 			addSingleMatchToTable(item.getSourceClass(), item.getTargetClass());
 		}
 	};
 	/**
 	 * TODO causing java.util.ConcurrentModificationExceptions !!!
 	 * WoRKAROUND: copy list to display, makes changes, replace it
 	 * Show computed Property mapping in select, activate Button to use them all.
 	 * @param map
 	 */
 	private synchronized void displayPropertyMapping(Map<String, HashMap<String, Double>> map)
 	{
 
 		TreeMap<Double, List<Pair<String>>> sort = new SortedMapping(map).sort();
 		logger.info("Displaying property Mapping"); //$NON-NLS-1$
 		synchronized(select) {
 			select.removeListener(selectListener);
 			useAll.removeListener(useComputedListener);
 			select.removeAllItems();
 			useComputedListener = new UseComputedClickListener(map);
 			useAll.addListener(useComputedListener);
 			if(map.size()>0)
 				useAll.setEnabled(true);
 			for(Entry<Double, List<Pair<String>>> e: sort.descendingMap().entrySet()) {
 				for(Pair<String> pair : e.getValue()) {
 					if(e.getKey()>0)
 						select.addItem(new ClassMatchItem(pair.getA(), pair.getB(), e.getKey()));
 				}
 			}
 			select.setImmediate(true);
 			select.setNullSelectionAllowed(false);
 			select.addListener(selectListener);
 		}		
 	}
 
 	/**
 	 * Public method to submit computed property mapping from the ButtonListener to the table.
 	 * @param propertyMap
 	 */
 	public void addMapToTable(Map<String, HashMap<String, Double>> propertyMap) {
 		for(String key : propertyMap.keySet()) {
 			for(Entry<String, Double> e : propertyMap.get(key).entrySet())
 			{
 				addSingleMatchToTable(key, e.getKey());
 			}
 		}
 		useAll.setEnabled(false);
 	}
 	/**
 	 * Method to add a single match to table.
 	 * @param sourceClass
 	 * @param targetClass
 	 */
 	public void addSingleMatchToTable(String sourceClass, String targetClass) {
 		if(sourceClass == null || sourceClass.length()==0 || targetClass == null || targetClass.length()==0)
 			return;
 		listenerActive=false;
 		Object[] row = createTableRow();
 		PropertyComboBox sourceBox = (PropertyComboBox) row[0];
 		PropertyComboBox targetBox = (PropertyComboBox) row[1];
 		sourceBox.addItem(sourceClass);
 		sourceBox.select(sourceClass);
 		targetBox.addItem(targetClass);
 		targetBox.select(targetClass);
 		table.addItem(row,row);
 		listenerActive=true;
 	}
 
 	/**
 	 * Called on next button click.
 	 */
 	public void submit() {
 		propMapper.stop();
 		Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
 		for(Object[] row : rows) {
 			if(((PropertyComboBox)row[0]).getValue() != null && ((PropertyComboBox)row[0]).getValue()!=null &&
 					((PropertyComboBox)row[0]).getValue().toString().length()>0 &&
 					((PropertyComboBox)row[1]).getValue().toString().length()>0) {
 				addProperty(((PropertyComboBox)row[0]).getValue().toString(),config.getSource());
 				addProperty(((PropertyComboBox)row[1]).getValue().toString(),config.getTarget());
 				config.addPropertiesMatch(((PropertyComboBox)row[0]).getValue().toString(), ((PropertyComboBox)row[1]).getValue().toString(), true);
 			}
 		}
 	}
 
 	/**
 	 * Listener to add all computed property matches to the table.
 	 * @author Lyko
 	 */
 	class UseComputedClickListener implements Button.ClickListener {
 		Map<String, HashMap<String, Double>> propertyMap;
 		public UseComputedClickListener(Map<String, HashMap<String, Double>> propertyMap) {
 			this.propertyMap = propertyMap;
 		}
 
 		@Override
 		public void buttonClick(ClickEvent event) {
 			addMapToTable(propertyMap);
 		}
 	}
 
 	/** React on Button clicks to compute automatic property mapping, either string based or with the default mechanism.
 	 *  @author Lyko */
 	class ControlPropertyMappingListener implements Button.ClickListener {
 		boolean stringBased = true;
 		public ControlPropertyMappingListener(boolean stringBased) {
 			this.stringBased = stringBased;
 		}
 		@Override
 		public void buttonClick(ClickEvent event) {
 			if(propMapper != null) {
 				propMapper.stop();
 			}
 			propMapper = new PropertyMappingThread(stringBased);
 			propMapper.start();
 		}
 
 	}
 
 	/**
 	 * Thread that performs automatic property mappings.
 	 * @author Lyko
 	 */
 	class PropertyMappingThread extends Thread {
 
 		boolean stringBased = true;
 
 		public PropertyMappingThread(boolean stringBased) {
 			this.stringBased = stringBased;
 		}
 
 		@Override
 		public void run()
 		{
 			//disable button
 			if(stringBased) {
 				computeStringBasedMapping.setEnabled(false);
 				computeDefaultPropertyMapping.setEnabled(true);
 			} else {
 				computeDefaultPropertyMapping.setEnabled(false);
 				computeStringBasedMapping.setEnabled(true);
 			}
 			progress.setEnabled(true);
 			progress.setVisible(true);
 			progressLabel.setVisible(true);
 
 			Map<String,HashMap<String,Double>> map = performAutomaticPropertyMapping().map;
 			if(map != null)
 				displayPropertyMapping(map);
 			{
 				progress.setEnabled(false);
 				progress.setVisible(false);
 				progressLabel.setVisible(false);
 			}
 			//enable button
 			if(stringBased) {
 				computeStringBasedMapping.setEnabled(true);
 			} else {
 				computeDefaultPropertyMapping.setEnabled(true);
 			}
 		}
 		/**
 		 * Method tries to getpropertyMapping
 		 */
 		private Mapping performAutomaticPropertyMapping() {
 			Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
 			List<Object> parameters = Arrays.asList(new Object[] {config.getSource().endpoint, config.getTarget().endpoint,
 					config.getSource().getClassOfendpoint(), config.getTarget().getClassOfendpoint(), stringBased});
 			Cache mappingCache = null;
 			if(CACHING) {
 				mappingCache = CacheManager.getInstance().getCache("automaticpropertymapping"); //$NON-NLS-1$
 				//			if(mappingCache.getStatus()==net.sf.ehcache.Status.STATUS_UNINITIALISED) {mappingCache.initialise();}
 				if(mappingCache.isKeyInCache(parameters))
 				{
 					logger.info("Property Mapping Cache hit"); //$NON-NLS-1$
 					return (Mapping) mappingCache.get(parameters).getValue();
 				}
 			}
 
 			PropertyMapper propMap;
 			if(stringBased) {
 				logger.info("Starting string based PropertyMapper");
 				propMap = new LabelBasedPropertyMapper();
 				propMap.setSourceModel(config.sourceModel);
 				propMap.setTargetModel(config.targetModel);
 			} else {
 				logger.info("Starting default PropertyMapper");
 				propMap = new DefaultPropertyMapper();
 				propMap.setSourceModel(config.sourceModel);
 				propMap.setTargetModel(config.targetModel);
 			}
 			try {
 				Mapping m = propMap.getPropertyMapping(config.getSource().endpoint, config.getTarget().endpoint, expandClassName(config.getSource(),config.getSource().getClassOfendpoint()), expandClassName(config.getTarget(),config.getTarget().getClassOfendpoint()));
 
 				if(CACHING) {
 					mappingCache = CacheManager.getInstance().getCache("automaticpropertymapping"); //$NON-NLS-1$
 					if(mappingCache.getStatus()==net.sf.ehcache.Status.STATUS_UNINITIALISED) {mappingCache.initialise();}
 					logger.info("Saving automatic computed Property mapping to cache...");
 					mappingCache.put(new Element(parameters, m));
 					mappingCache.flush();
 				}
 				return m;
 			}catch(Exception e) {
 				getWindow().showNotification("Error performing property mapping: "+e.getMessage());
 				e.printStackTrace();
 				logger.info("Error performing property mapping: "+e.getMessage());
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Bean for a single class match.
 	 * @author Lyko
 	 */
 	class ClassMatchItem {
 		private String sourceClass;
 		private String targetClass;
 		private double similarity;
 		/**
 		 * Class to wrap arorund ClassMateches.
 		 * @param sourceClass
 		 * @param targetClass
 		 * @param similarity
 		 */
 		public ClassMatchItem(String sourceClass, String targetClass, double similarity) {
 			this.sourceClass=sourceClass;
 			this.targetClass=targetClass;
 			this.similarity=similarity;
 		}
 		public ClassMatchItem(String sourceClass, String targetClass) {
 			this.sourceClass=sourceClass;
 			this.targetClass=targetClass;
 		}
 		public String getSourceClass() {
 			try {
 				return URLDecoder.decode(sourceClass, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				return sourceClass;
 			}
 		}
 		public void setSourceClass(String sourceClass) {
 			this.sourceClass = sourceClass;
 		}
 		public String getTargetClass() {
 			try {
 				return URLDecoder.decode(targetClass, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				return targetClass;
 			}
 		}
 		public void setTargetClass(String targetClass) {
 			this.targetClass = targetClass;
 		}
 		public double getSimilarity() {
 			return similarity;
 		}
 		public void setSimilarity(double similarity) {
 			this.similarity = similarity;
 		}
 		@Override
 		public String toString() {
 			String ret = getSourceClass() + " - " +getTargetClass(); //$NON-NLS-1$
 			if(!Double.isNaN(similarity))
 				ret+= " (" + similarity + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 			return ret;
 		}
 	}
 	
 	/**
 	 * Workaround to expanding abbreviated classname to full URI as SPARQWLModule requires them and
 	 * generated prefixes are not persistent in the PrefixHelper.
 	 * @param info
 	 * @param className
 	 * @return
 	 */
 	public String expandClassName(KBInfo info, String className) {
 		if(className.startsWith("http://"))
 			return className;
 		else {
 			String base = className.substring(0, className.indexOf(":")).trim();
 			if(info.prefixes.containsKey(base)) {
 //				logger.info("Restructering classname "+className+" to "+info.prefixes.get(base)+className.substring(className.indexOf(":")+1));
 				return info.prefixes.get(base)+className.substring(className.indexOf(":")+1);
 			}
 		}
 		return className;
 	}
 }
