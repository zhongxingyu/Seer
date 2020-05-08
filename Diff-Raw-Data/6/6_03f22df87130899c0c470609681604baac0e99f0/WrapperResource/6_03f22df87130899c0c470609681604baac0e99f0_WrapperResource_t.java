 package org.osehra.das.wrapper.nwhin;
 
 import org.osehra.das.wrapper.nwhin.doc.AdapterDocQueryRetrieveFacade;
 import org.osehra.integration.core.component.ComponentImpl;
 import org.osehra.integration.core.receiver.MessageReceiver;
 import org.osehra.integration.core.receiver.MessageReceiverException;
 import org.osehra.integration.core.transformer.Transformer;
 import org.osehra.integration.core.transformer.TransformerException;
 import org.osehra.integration.core.transformer.xml.StringToXML;
 
 import java.util.Date;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.annotation.Scope;
 import org.w3c.dom.Document;
 
 /**
  *
  * @author root
  *
  */
 @org.springframework.stereotype.Component
 @Path("/")
 @Scope("request")
 public class WrapperResource extends ComponentImpl implements
 		MessageReceiver<UriInfo, Object> {
 
 	private static final Log LOG = LogFactory.getLog(WrapperResource.class);
 
 	private static final String ENCODING = "; charset=UTF-8";
 
 	AdapterDocQueryRetrieveFacade adapterDocQueryRetrieveFacade;
 	Transformer<Document, String> xmlToHtmlTransformer;
 
	@PersistenceContext(unitName="c32") EntityManager em;
 	/**
 	 * Reverse Proxy Cache for storing results.
 	 */
	//String REVERSE_PROXY_CACHE = "org.osehra.das.core.ReverseProxyCache";
 
 	/**
 	 * The Incoming URI Info Context from the Consumer request.
 	 */
 	@Context
 	protected UriInfo uriInfo;
 
 	// @Path("/{aa}/{pid:patientId}/{profile}/{domain}/{homeCommunityId}_{remoteRepositoryId}_{documentUniqueId}.{fileExtension:xml}")
 	@Path("/2.16.840.1.113883.4.349/{pid}/{profile}/{domain}/{speciality}/{homeCommunityId}_{remoteRepositoryId}_{documentUniqueId}.{fileExtension:xml}")
 	@GET
 	@Produces({ MediaType.APPLICATION_XML + ENCODING })
 	public Object getDomainXml(@PathParam("pid") String patientId,
 			@QueryParam("userName") String userName) {
 		// Go to adapter, get the C32 - return the C32 XML document for that
 		// patient
 		String c32Document = adapterDocQueryRetrieveFacade.getDocument(patientId, new Date(),
 				new Date(), userName);
 
 		C32DocumentEntity entity = new C32DocumentEntity();
 		entity.setDocument(c32Document);
 		//em.persist(entity);
 
 		//Query query = em.createQuery("hql query";)
 		//String filteredC32Doc = query.list();
 
 		return c32Document;
 	}
 
 	@Path("/2.16.840.1.113883.4.349/{pid}/{profile}/{domain}/{speciality}/{homeCommunityId}_{remoteRepositoryId}_{documentUniqueId}.{fileExtension:html}")
 	@GET
 	@Produces({ MediaType.APPLICATION_XML + ENCODING })
 	public Object getDomainHtml(@PathParam("pid") String patientId,
 			@QueryParam("userName") String userName) {
 		// Go to adapter, get the C32 - return the C32 XML document for that
 		// patient
 
 		try {
 			String cdaDocument = adapterDocQueryRetrieveFacade.getDocument(
 					patientId, new Date(), new Date(), userName);
 			final Document doc = new StringToXML().transform(cdaDocument);
 			return "<html></html>";
 
 		} catch (TransformerException ex) {
 			throw new RuntimeException(ex);
 		}
 
 	}
 
 	@Override
 	public Object receive(UriInfo uriInfo) throws MessageReceiverException {
 		return uriInfo;
 	}
 
 	/**
 	 * This method is used only for testing.
 	 *
 	 * @param uriInfo
 	 */
 	public void setUriInfo(UriInfo uriInfo) {
 		this.uriInfo = uriInfo;
 	}
 
 	public void setAdapterDocQueryRetrieveFacade(
 			AdapterDocQueryRetrieveFacade adapterDocQueryRetrieveFacade) {
 		this.adapterDocQueryRetrieveFacade = adapterDocQueryRetrieveFacade;
 	}
 }
