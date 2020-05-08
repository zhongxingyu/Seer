 package org.iucn.sis.server.extensions.offline;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Properties;
 
 import org.gogoego.api.plugins.GoGoEgo;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.shared.api.models.OfflineMetadata;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 public class OfflineMetaDataRestlet extends BaseServiceRestlet {
 	
 	public OfflineMetaDataRestlet(Context context) {
 		super(context);
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/offline/metadata");
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 
 		Properties init = GoGoEgo.getInitProperties();
 		
 		String dbUri = init.getProperty("dbsession.sis.uri");
		String dbName = getDbNameFromUri(dbUri);
		String dbLocation =  removeJDBCPrefix(dbUri);
 		
 		File file = new File(dbLocation+".data.db");
 		if (!file.exists())
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "SIS Configuration error.");
 		
 		OfflineMetadata metadata = new OfflineMetadata();
 		metadata.setName(dbName);
 		metadata.setLocation(dbLocation);
 		metadata.setLastModified(new Date(file.lastModified()));
 		
 		return new StringRepresentation(metadata.toXML(), MediaType.TEXT_XML);		
 	}
 	
 	public String getDbNameFromUri(String uri){		
 		File file = new File(uri);
 		return file.getName();
 	}
 	
 	public String removeJDBCPrefix(String uri){		
 		String path = uri.substring(uri.indexOf("file:")+5,uri.length());
 		return path;
 	}
 }
