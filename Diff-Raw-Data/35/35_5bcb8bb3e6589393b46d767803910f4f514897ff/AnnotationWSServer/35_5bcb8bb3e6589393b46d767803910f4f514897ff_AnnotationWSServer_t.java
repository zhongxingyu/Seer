 package org.bioinfo.infrared.ws.server.rest.functgen.annotation;
 
 import java.io.IOException;
 
 import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
 import org.bioinfo.infrared.ws.server.rest.exception.VersionException;
 
 
 @Path("/{version}/{species}/annotation")
 @Produces("text/plain")

 public class AnnotationWSServer extends GenericRestWSServer {
 
	public AnnotationWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
 		super(version, species, uriInfo);
 	}
	@Override
	public boolean isValidSpecies() {
		return true;
	}
 	
 }
