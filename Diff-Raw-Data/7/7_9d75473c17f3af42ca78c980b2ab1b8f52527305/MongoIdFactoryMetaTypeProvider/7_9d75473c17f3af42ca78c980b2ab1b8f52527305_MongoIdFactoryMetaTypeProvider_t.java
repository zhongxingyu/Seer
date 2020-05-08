 /**
  * 
  */
 
 package org.eclipselabs.emongo.metatype;
 
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.eclipselabs.emongo.MongoDatabaseProvider;
 import org.eclipselabs.emongo.MongoIdFactory;
 import org.eclipselabs.emongo.config.ConfigurationProperties;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 /**
  * @author bhunt
  * 
  */
 public class MongoIdFactoryMetaTypeProvider implements MetaTypeProvider
 {
 	Set<String> databases = new CopyOnWriteArraySet<String>();
 
 	@Override
 	public String[] getLocales()
 	{
 		return null;
 	}
 
 	@Override
 	public ObjectClassDefinition getObjectClassDefinition(String arg0, String arg1)
 	{
 		AttributeDefinitionImpl database = new AttributeDefinitionImpl("MongoDatabaseProvider.target", "Database", AttributeDefinition.STRING);
 		database.setDescription("The MongoDB database");
 
 		String[] databaseAliases = new String[databases.size()];
 		String[] targetFilters = new String[databases.size()];
 
 		databases.toArray(databaseAliases);
 
 		for (int i = 0; i < databaseAliases.length; i++)
 			targetFilters[i] = "(" + MongoIdFactory.PROP_ALIAS + "=" + databaseAliases[i] + ")";
 
 		database.setOptionLabels(databaseAliases);
 		database.setOptionValues(targetFilters);
 
 		if (!databases.isEmpty())
 			database.setDefaultValue(new String[] { databases.iterator().next() });
 
 		AttributeDefinitionImpl collection = new AttributeDefinitionImpl(MongoIdFactory.PROP_COLLECTION, "Collection", AttributeDefinition.STRING);
 		collection.setDescription("The MongoDB collection within the database");
 
 		ObjectClassDefinitionImpl ocd = new ObjectClassDefinitionImpl(ConfigurationProperties.ID_FACTORY_PID, "MongoDB ID", "MongoDB ID Provider Configuration");
 		ocd.addAttribute(database);
 		ocd.addAttribute(collection);
 
 		return ocd;
 	}
 
	public void bindMongoDatabaseProvider(ServiceReference<MongoDatabaseProvider> serviceReference)
 	{
 		databases.add((String) serviceReference.getProperty(MongoDatabaseProvider.PROP_ALIAS));
 	}
 
	public void unbindMongoDatabaseProvider(ServiceReference<MongoDatabaseProvider> serviceReference)
 	{
 		databases.remove((String) serviceReference.getProperty(MongoDatabaseProvider.PROP_ALIAS));
 	}
 }
