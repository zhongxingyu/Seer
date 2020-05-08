 package org.iucn.sis.server.restlets.taxa;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.SynonymDAO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.api.utils.TaxomaticException;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 
 public class SynonymRestlet extends BaseServiceRestlet {
 
 	public SynonymRestlet(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void definePaths() {
 		paths.add("/taxon/{taxon_id}/synonym/{id}");
 		paths.add("/taxon/{taxon_id}/synonym");
 	}
 	
 	private Taxon getTaxon(Request request, Session session) throws ResourceException {
 		final TaxonIO io = new TaxonIO(session);
 		final Taxon taxon;
 		try {
 			taxon = io.getTaxon(Integer.valueOf((String)request.getAttributes().get("taxon_id")));
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Node not found, or could not be loaded.", e);
 		}
 		if (taxon == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Taxon not found");
 		return taxon;
 	}
 	
 	private Integer getSynonymID(Request request) throws ResourceException {
 		try {
 			return Integer.valueOf((String)request.getAttributes().get("id"));
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		}
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final Taxon taxon = getTaxon(request, session);
 		final TaxonIO io = new TaxonIO(session);
 		
 		final NativeDocument newDoc = new JavaNativeDocument();
 		try {
 			newDoc.parse(entity.getText());
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		
 		Synonym synonym = Synonym.fromXML(newDoc.getDocumentElement(), taxon);
 		
 		try {
 			io.writeTaxon(taxon, getUser(request, session));
 		} catch (TaxomaticException e) { 
 			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		}
 		
		session.flush();
		
 		response.setStatus(Status.SUCCESS_OK);
 		response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final Taxon taxon = getTaxon(request, session);
 		
 		final Synonym synonym;
 		try {
 			synonym = SynonymDAO.getSynonymByORMID(session, getSynonymID(request));
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Synonym not found, or could not be loaded.", e);			
 		}
 		
 		if (synonym == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Synonym not found.");
 		
 		NativeDocument newDoc = new JavaNativeDocument();
 		try {
 			newDoc.parse(entity.getText());
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		
 		Synonym.fromXML(synonym, newDoc.getDocumentElement(), null);
 		
 		synonym.setTaxon(taxon);
 		
 		try {
 			SISPersistentManager.instance().saveObject(session, synonym);
 		} catch (PersistentException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 					
 		taxon.toXML();
 			
 		response.setStatus(Status.SUCCESS_OK);
 		response.setEntity(synonym.getId() + "", MediaType.TEXT_PLAIN);
 		
 	}
 	
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
 		Integer id = getSynonymID(request);
 		Taxon taxon = getTaxon(request, session);
 		Synonym toDelete = null;
 		for (Synonym syn : taxon.getSynonyms()) {
 			if (syn.getId() == id) {
 				toDelete = syn;
 				break;
 			}
 		}
 		if (toDelete == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		
 		//FIXME: shouldn't this added synonym get removed from the database for this taxon?
 		taxon.getSynonyms().remove(toDelete);
 		
 		taxon.toXML();
 		
 		final TaxonIO taxonIO = new TaxonIO(session);
 		
 		try {
 			taxonIO.writeTaxon(taxon, getUser(request, session));
 		} catch (TaxomaticException e) {
 			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		}
 		
 		try {
 			SIS.get().getManager().deleteObject(session, toDelete);
 		} catch (PersistentException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		}
 	}
 
 }
