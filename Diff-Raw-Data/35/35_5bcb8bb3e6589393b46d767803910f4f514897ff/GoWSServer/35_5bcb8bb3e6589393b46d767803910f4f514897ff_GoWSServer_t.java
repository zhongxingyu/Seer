 package org.bioinfo.infrared.ws.server.rest.functgen.annotation;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.infrared.core.common.FeatureList;
 import org.bioinfo.infrared.core.funcannot.GO;
 import org.bioinfo.infrared.funcannot.GODBManager;
 import org.bioinfo.infrared.ws.server.rest.exception.VersionException;
 
 import com.google.gson.reflect.TypeToken;
 
 
 @Path("/{version}/{species}/annotation/go")
 @Produces("text/plain")
 public class GoWSServer extends AnnotationWSServer {
 
 	public GoWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
		
 		super(version, species, uriInfo);
 	}
 
 	private GODBManager getGODBManager(){
 		return  new GODBManager(infraredDBConnector);
 	}
 	
 	@GET
 	@Path("/{goId}")
 	public Response getByIds(@PathParam("goId") String goIds) {
 		return getGOListByIds(goIds);
 	}
 	
 	@GET
 	@Path("/{goId}/info")
 	public Response getGOListByIds(@PathParam("goId") String goIds) {
 		try {
 			List<String> ids = StringUtils.toList(goIds, ",");
 			GODBManager goDbManager = getGODBManager();
 			FeatureList<GO> goList = goDbManager.getByAccesions(ids);
 			return generateResponseFromFeatureList(goIds, goList, new TypeToken<FeatureList<GO>>() {}.getType());
 		} catch (Exception e) {
 			return generateErrorResponse(e.toString());
 		}
 	}
 	
	@Override
	public boolean isValidSpecies() {
		if("hsa".equalsIgnoreCase(species) || "mmu".equalsIgnoreCase(species) || "rno".equalsIgnoreCase(species)) {
			return true;
		}
		return false;
	}
	
 	
 }
