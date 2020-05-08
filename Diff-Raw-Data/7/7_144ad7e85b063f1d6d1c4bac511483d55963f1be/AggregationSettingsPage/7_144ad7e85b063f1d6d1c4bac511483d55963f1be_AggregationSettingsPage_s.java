 package cz.cuni.mff.odcleanstore.webfrontend.pages.outputws;
 
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.cr.*;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.Dao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.cr.*;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 public class AggregationSettingsPage extends FrontendPage
 {
 	private static final long serialVersionUID = 1L;
 
 	private static Logger logger = Logger.getLogger(AggregationSettingsPage.class);
 	
 	private Dao<PropertySettings> propertySettingsDao;
 	private GlobalAggregationSettingsDao globalAggregationSettingsDao;
 	
 	public AggregationSettingsPage() 
 	{
 		super(
 			"Home > OutputWS > CR > Aggregation Settings", 
 			"Adjust aggregation settings"
 		);
 		
 		// prepare DAO objects
 		//
 		propertySettingsDao = daoLookupFactory.getDao(PropertySettingsDao.class);
 		globalAggregationSettingsDao = daoLookupFactory.getGlobalAggregationSettingsDao();
 		
 		// register page components
 		//
 		addGlobalAggregationSettingsSection();
 		addPropertySettingsTable();
 	}
 
 	private void addGlobalAggregationSettingsSection()
 	{
 		GlobalAggregationSettings settings = globalAggregationSettingsDao.load();
 		
 		addDefaultAggregationTypeLabel(settings);
 		addDefaultMultivalueTypeLabel(settings);
		addDefaultErrorStrategy(settings);
 	}
 	
 	private void addDefaultAggregationTypeLabel(GlobalAggregationSettings settings)
 	{
 		AggregationType aggregationType = settings.getDefaultAggregationType();
 		
 		Label label = new Label("defaultAggregationType", aggregationType.getLabel());
 		label.add(new AttributeModifier("title", new Model<String>(aggregationType.getDescription())));
 		add(label);
 	}
 	
 	private void addDefaultMultivalueTypeLabel(GlobalAggregationSettings settings)
 	{
 		MultivalueType multivalueType = settings.getDefaultMultivalueType();
 		
 		Label label = new Label("defaultMultivalueType", multivalueType.getLabel());
 		label.add(new AttributeModifier("title", new Model<String>(multivalueType.getDescription())));
 		
 		add(label);
 	}
 	
	private void addDefaultErrorStrategy(GlobalAggregationSettings settings)
 	{
 		ErrorStrategy errorStrategy = settings.getDefaultErrorStrategy();
 		
 		Label label = new Label("defaultErrorStrategy", errorStrategy.getLabel());
 		label.add(new AttributeModifier("title", new Model<String>(errorStrategy.getDescription())));
 		
 		add(label);
 	}
 	
 	private void addPropertySettingsTable()
 	{
 		IModel<List<PropertySettings>> model = createModelForListView(propertySettingsDao);
 				
 		ListView<PropertySettings> listView = new ListView<PropertySettings>("propertySettingsTable", model)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(ListItem<PropertySettings> item) 
 			{
 				final PropertySettings property = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<PropertySettings>(property));
 				
 				addPropertyLabel(item);
 				addMultivalueTypeLabel(item, property);
 				addAggregationTypeLabel(item, property);			
 				
 				item.add(
 					createDeleteRawButton(
 						propertySettingsDao, 
 						property.getId(), 
 						"deleteProperty", 
 						"property", 
 						AggregationSettingsPage.class
 					)
 				);
 			}
 		};
 		
 		add(listView);
 	}
 	
 	private void addPropertyLabel(ListItem<PropertySettings> item)
 	{
 		item.add(new Label("property"));
 	}
 	
 	private void addMultivalueTypeLabel(ListItem<PropertySettings> item, 
 		PropertySettings property)
 	{
 		MultivalueType multivalueType = property.getMultivalueType();
 		
 		Label label = new Label("multivalueType", multivalueType.getLabel());
 		label.add(new AttributeModifier("title", new Model<String>(multivalueType.getDescription())));
 		
 		item.add(label);
 	}
 	
 	private void addAggregationTypeLabel(ListItem<PropertySettings> item,
 		PropertySettings property)
 	{
 		AggregationType aggregationType = property.getAggregationType();
 		
 		Label label = new Label("aggregationType", aggregationType.getLabel());
 		label.add(new AttributeModifier("title", new Model<String>(aggregationType.getDescription())));
 		
 		item.add(label);
 	}
 }
