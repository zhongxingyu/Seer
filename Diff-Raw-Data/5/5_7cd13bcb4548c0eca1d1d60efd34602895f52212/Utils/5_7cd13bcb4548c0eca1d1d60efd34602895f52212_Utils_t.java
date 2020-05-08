 /**
  * 
  */
 package pl.psnc.dl.wf4ever;
 
 import java.net.URI;
 import java.rmi.RemoteException;
 
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
 import pl.psnc.dlibra.metadata.EditionId;
 import pl.psnc.dlibra.service.DLibraException;
 
 /**
  * @author piotrhol
  *
  */
 public class Utils
 {
 
 	@SuppressWarnings("unused")
 	private final static Logger logger = Logger.getLogger(Utils.class);
 
 
 	public static EditionId getEditionId(DLibraDataSource dLibra, String RO,
 			String version, long editionId)
 		throws RemoteException, DLibraException
 	{
 		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
 			return dLibra.getEditionHelper().getLastEditionId(RO, version);
 		}
 		else {
 			return new EditionId(editionId);
 		}
 	}
 
 
 	public static URI createVersionURI(UriInfo uriInfo, String workspaceId,
 			String researchObjectId, String versionId)
 	{
		String path = String.format("workspaces/%s/ROs/%s/%s", workspaceId,
			researchObjectId, versionId);
 		return uriInfo.getBaseUri().resolve(path);
 	}
 }
