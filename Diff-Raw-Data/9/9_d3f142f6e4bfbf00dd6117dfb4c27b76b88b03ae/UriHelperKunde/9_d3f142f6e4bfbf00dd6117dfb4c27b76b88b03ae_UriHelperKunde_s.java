 package de.shop.Kundenverwaltung.rest;
 
 import java.net.URI;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import de.shop.Auftragsverwaltung.rest.UriHelperAuftrag;
 import de.shop.Kundenverwaltung.domain.Kunde;
 import de.shop.Util.Log;
 
 
 @ApplicationScoped
 @Log
 public class UriHelperKunde {
 	
 	@Inject
	UriHelperAuftrag uriauftrag;
 	
 	public URI getUriKunde(Kunde kunde, UriInfo uriInfo) {
 		final UriBuilder ub = uriInfo.getBaseUriBuilder()
 		                             .path(KundeResource.class)
 		                             .path(KundeResource.class, "findKundenByKundennummer");
 		final URI kundeUri = ub.build(kunde.getKundenNr());
 		return kundeUri;
 	}
 	
 	
 	public void updateUriKunde(Kunde kunde, UriInfo uriInfo) {
 
 		kunde.setAuftraegeUri(uriauftrag.getUriAuftragByKunde(kunde, uriInfo));
 	}
 }
