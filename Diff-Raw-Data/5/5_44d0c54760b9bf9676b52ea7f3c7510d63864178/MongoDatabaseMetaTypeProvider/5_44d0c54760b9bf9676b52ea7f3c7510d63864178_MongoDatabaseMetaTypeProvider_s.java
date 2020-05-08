 /**
  * 
  */
 
 package org.eclipselabs.emongo.metatype;
 
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.eclipselabs.emongo.MongoClientProvider;
 import org.eclipselabs.emongo.MongoDatabaseProvider;
 import org.eclipselabs.emongo.components.MongoDatabaseProviderComponent;
 import org.eclipselabs.emongo.config.ConfigurationProperties;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 /**
  * @author bhunt
  * 
  */
 public class MongoDatabaseMetaTypeProvider implements MetaTypeProvider
 {
 	private Set<String> mongoClientProviders = new CopyOnWriteArraySet<String>();
 
 	@Override
 	public String[] getLocales()
 	{
 		return null;
 	}
 
 	@Override
 	public ObjectClassDefinition getObjectClassDefinition(String id, String locale)
 	{
 		AttributeDefinitionImpl clientId = new AttributeDefinitionImpl(MongoDatabaseProvider.PROP_CLIENT_FILTER, "Client", AttributeDefinition.STRING);
 		clientId.setDescription("The MongoDB database client ID");
 
 		String[] clients = new String[mongoClientProviders.size()];
 		String[] targetFilters = new String[mongoClientProviders.size()];
 
 		mongoClientProviders.toArray(clients);
 
 		for (int i = 0; i < clients.length; i++)
 			targetFilters[i] = "(" + MongoClientProvider.PROP_CLIENT_ID + "=" + clients[i] + ")";
 
 		clientId.setOptionLabels(clients);
 		clientId.setOptionValues(targetFilters);
 
 		if (!mongoClientProviders.isEmpty())
 			clientId.setDefaultValue(new String[] { mongoClientProviders.iterator().next() });
 
 		AttributeDefinitionImpl alias = new AttributeDefinitionImpl(MongoDatabaseProvider.PROP_ALIAS, "Alias", AttributeDefinition.STRING)
 		{
 			@Override
 			public String validate(String value)
 			{
 				return MongoDatabaseProviderComponent.validateAlias(value);
 			}
 		};
 
 		alias.setDescription("The alias of the MongoDB database.");
 
 		AttributeDefinitionImpl database = new AttributeDefinitionImpl(MongoDatabaseProvider.PROP_DATABASE, "Database", AttributeDefinition.STRING)
 		{
 			@Override
 			public String validate(String value)
 			{
 				return MongoDatabaseProviderComponent.validateDatabaseName(value);
 			}
 		};
 
 		database.setDescription("The name MongoDB database.");
 
 		AttributeDefinitionImpl user = new AttributeDefinitionImpl(MongoDatabaseProvider.PROP_USER, "User", AttributeDefinition.STRING);
		database.setDescription("The user id to use for authenticating to the MongoDB server (optional).");
 
 		AttributeDefinitionImpl password = new AttributeDefinitionImpl(MongoDatabaseProvider.PROP_PASSWORD, "Password", AttributeDefinition.PASSWORD);
		database.setDescription("The user password to use for authenticating to the MongoDB server (optional).");
 
 		ObjectClassDefinitionImpl ocd = new ObjectClassDefinitionImpl(ConfigurationProperties.DATABASE_PID, "MongoDB Database", "MongoDB Database Configuration");
 		ocd.addAttribute(clientId);
 		ocd.addAttribute(alias);
 		ocd.addAttribute(database);
 		ocd.addAttribute(user);
 		ocd.addAttribute(password);
 
 		return ocd;
 	}
 
 	public void bindMongoClientProvider(ServiceReference<MongoClientProvider> serviceReference)
 	{
 		mongoClientProviders.add((String) serviceReference.getProperty(MongoClientProvider.PROP_CLIENT_ID));
 	}
 
 	public void unbindMongoClientProvider(ServiceReference<MongoClientProvider> serviceReference)
 	{
 		mongoClientProviders.remove((String) serviceReference.getProperty(MongoClientProvider.PROP_CLIENT_ID));
 	}
 }
