 package eu.europeana.uim.gui.cp.server;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 import eu.europeana.corelib.tools.lookuptable.Collection;
 import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
 import eu.europeana.uim.gui.cp.client.services.CollectionManagementProxy;
 import eu.europeana.uim.gui.cp.server.util.PropertyReader;
 import eu.europeana.uim.gui.cp.server.util.UimConfigurationProperty;
 import eu.europeana.uim.gui.cp.shared.CollectionMappingDTO;
 
 public class CollectionManagementProxyImpl extends
 		IntegrationServicesProviderServlet implements CollectionManagementProxy {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static CollectionMongoServer collectionMongoServer;
 	private static Collection collection;
 
 	private static final String MONGO_HOST= PropertyReader.getProperty(UimConfigurationProperty.MONGO_HOSTURL);
 	private static final int MONGO_PORT = Integer.parseInt(PropertyReader.getProperty(UimConfigurationProperty.MONGO_HOSTPORT));
 	private static final String MONGO_DB = PropertyReader.getProperty(UimConfigurationProperty.MONGO_DB_COLLECTIONS);
 	private static final String REPOSITORY = PropertyReader.getProperty(UimConfigurationProperty.UIM_REPOSITORY);
 	
 	static{
 		try {
 			collectionMongoServer = new CollectionMongoServer(new Mongo(
 					MONGO_HOST, MONGO_PORT), MONGO_DB);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	@Override
 	public List<CollectionMappingDTO> retrieveCollections() {
 		List<CollectionMappingDTO> collections = new ArrayList<CollectionMappingDTO>();
 
 			for (Collection collection : collectionMongoServer
 					.retrieveAllCollections()) {
 				CollectionMappingDTO collectionDTO = new CollectionMappingDTO();
 				collectionDTO.setOriginalCollection(collection
 						.getOldCollectionId());
 				collectionDTO.setNewCollection(collection.getNewCollectionId());
 				collections.add(collectionDTO);
 			}
			collectionMongoServer.close();
 
 		return collections;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.gui.cp.client.services.CollectionManagementProxy#saveOneCollection(eu.europeana.uim.gui.cp.shared.CollectionMappingDTO)
 	 */
 	@Override
 	public Boolean saveOneCollection(CollectionMappingDTO collectionDTO) {
 		try {
 			collectionMongoServer = new CollectionMongoServer(new Mongo(
 					MONGO_HOST, MONGO_PORT), MONGO_DB);
 			collection = new Collection();
 			collection.setNewCollectionId(collectionDTO.getNewCollection());
 			collection
 					.setOldCollectionId(collectionDTO.getOriginalCollection());
 			collectionMongoServer.saveCollection(collection);
			collectionMongoServer.close();
 		} catch (UnknownHostException e) {
 			return false;
 		} catch (MongoException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.gui.cp.client.services.CollectionManagementProxy#saveCollections(java.util.List)
 	 */
 	@Override
 	public Boolean saveCollections(List<CollectionMappingDTO> collections) {
 
 			for (CollectionMappingDTO collectionDTO : collections) {
 				
 				Collection collection = new Collection();
 				
 				collection.setNewCollectionId(collectionDTO.getNewCollection());
 				collection.setOldCollectionId(collectionDTO
 						.getOriginalCollection());
 				collectionMongoServer.saveCollection(collection);
 			}
			collectionMongoServer.close();
 
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.gui.cp.client.services.CollectionManagementProxy#retrieveCsvCollections(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public List<CollectionMappingDTO> retrieveCsvCollections(String location,
 			String delimiter) {
 		List<CollectionMappingDTO> collections = new ArrayList<CollectionMappingDTO>();
 
 		String[] csv;
 		try {
 			csv = readCsv(REPOSITORY +location);
 			for (String csvLine : csv) {
 				String[] fields = StringUtils.split(csvLine, delimiter);
 				CollectionMappingDTO collectionDTO = new CollectionMappingDTO();
 				collectionDTO.setOriginalCollection(fields[0]);
 				collectionDTO.setNewCollection(fields[1]);
 				collections.add(collectionDTO);
 			}
 		} catch (IOException e) {
 			return new ArrayList<CollectionMappingDTO>();
 		}
 		return collections;
 	}
 
 	/**
 	 * @param location
 	 * @return
 	 * @throws IOException
 	 */
 	private String[] readCsv(String location) throws IOException {
 		String strFileContents = "";
 		FileInputStream fin;
 		fin = new FileInputStream(location);
 		BufferedInputStream bin = new BufferedInputStream(fin);
 		byte[] contents = new byte[1024];
 		int bytesRead = 0;
 		while ((bytesRead = bin.read(contents)) != -1) {
 			strFileContents += new String(contents, 0, bytesRead);
 		}
 
 		bin.close();
 		fin.close();
 		return StringUtils.split(strFileContents, "\n");
 	}
 
 }
