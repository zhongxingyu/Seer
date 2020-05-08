 package org.iucn.sis.server.restlets.workingsets;
 
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.io.WorkingSetIO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.api.utils.DocumentUtils;
 import org.iucn.sis.shared.api.assessments.PublishedAssessmentsComparator;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 
 /**
  * The Working Set Restlet, handles calls to get and modify a user's
  * workingSet.xml file. Makes assumptions that an xml file exists in the users
  * working space.
  * 
  * @author liz.schwartz
  * 
  */
 public class WorkingSetRestlet extends BaseServiceRestlet {
 
 	public WorkingSetRestlet(Context context) {
 		super(context);
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/workingSet/{action}");
 		paths.add("/workingSet/{action}/{username}");
 		paths.add("/workingSet/{action}/{username}/{id}");
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		final String action = (String) request.getAttributes().get("action");
 		final String username = (String) request.getAttributes().get("username");
 		final String identifier = (String) request.getAttributes().get("id");
 		
 		if (username == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a username");
 		
 		final WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		
 		final Representation entity;
 		if (identifier == null) {
 			if (action.equalsIgnoreCase("subscribe"))
 				entity = getSubscribableWorkingSets(request, response, username, workingSetIO);
 			else
 				entity = getWorkingSets(username, workingSetIO);
 		}
 		else {
 			final Integer id;
 			try {
 				id = Integer.valueOf(identifier);
 			} catch (NumberFormatException e) {
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 			}
 			
 			if (action.equalsIgnoreCase("taxaList"))
 				entity = getTaxaFootprintForWorkingSet(request, response, username, id, workingSetIO);
 			else if (action.equalsIgnoreCase("taxaIDs"))
 				entity = getTaxaListForWorkingSet(username, id, workingSetIO);
 			else if (action.equalsIgnoreCase("assessments"))
 				entity = getAssessmentsForWorkingSet(request, response, username, id, workingSetIO, session);
 			else if (identifier.matches("\\d+"))
 				entity = getWorkingSet(username, id, workingSetIO);
 			else
 				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "No target for action " + action + " in working sets.");
 		}
 		
 		return entity;
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final String action = (String) request.getAttributes().get("action");
 		final String username = (String) request.getAttributes().get("username");
 		final String identifier = (String) request.getAttributes().get("id");
 		
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		
 		if (username == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a username");
 		
 		final Integer id;
 		try {
 			id = Integer.valueOf(identifier);
 		} catch (NumberFormatException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		} catch (NullPointerException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		}
 		
 		if (action.equalsIgnoreCase("taxaList"))
 			getTaxaFootprintWithAdditionalInfo(entity, response, username, id, workingSetIO, session);
 		else if (action.equalsIgnoreCase("editTaxa"))
 			editTaxaInWorkingSet(entity, request, response, username, id, session);
 		else
 			editWorkingSet(entity, username, id, session);	
 	}
 	
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final String action = (String) request.getAttributes().get("action");
 		final String username = (String) request.getAttributes().get("username");
 		final String identifier = (String) request.getAttributes().get("id");
 		
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		
 		if (username == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a username");
 		
 		if (action.equalsIgnoreCase("subscribe")) {
 			final Integer id;
 			try {
 				id = Integer.valueOf(identifier);
 			} catch (NumberFormatException e) {
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 			} catch (NullPointerException e) {
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 			}
 			
 			subscribeToPublicWorkingSet(request, response, username, id, workingSetIO);
 		}
 		else
 			createWorkingSet(entity, response, username, session);	
 	}
 	
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
 		final String action = (String) request.getAttributes().get("action");
 		final String username = (String) request.getAttributes().get("username");
 		final String identifier = (String) request.getAttributes().get("id");
 		
 		if (username == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a username");
 		
 		final Integer id;
 		try {
 			id = Integer.valueOf(identifier);
 		} catch (NumberFormatException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		} catch (NullPointerException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		}
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		WorkingSet ws = workingSetIO.readWorkingSet(id);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
 		
 		if (action.equalsIgnoreCase("unsubscribe")) {
 			if (!workingSetIO.unsubscribeFromWorkingset(username, id))
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not unsubscribe working set.");
 		}
 		else {
 			if (!workingSetIO.deleteWorkingset(ws))
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not delete working set.");
 		}
 	}
 	
 	/**
 	 * Gets all possible subscribable working sets, leaving out ones that users
 	 * are currently subscribed to.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param workingSetIO
 	 */
 	private Representation getSubscribableWorkingSets(Request request, Response response, String username, WorkingSetIO workingSetIO) throws ResourceException {
 		WorkingSet[] sets;
 		try {
 			sets = workingSetIO.getUnsubscribedWorkingSets(username);
 		} catch (PersistentException e) {
 			response.setEntity(e.getMessage(), MediaType.TEXT_PLAIN);
 			Debug.println("Failed to fetch subscribable working sets for {0}: \n{1}", username, e);
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 		
 		StringBuilder xml = new StringBuilder("<xml>\r\n");
 		for (WorkingSet set : sets)
 			xml.append(set.toXMLMinimal());
 		xml.append("</xml>");
 		
 		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 	}
 	
 	private Representation getWorkingSet(String username, Integer workingSetID, WorkingSetIO workingSetIO) throws ResourceException {
 		final WorkingSet ws = workingSetIO.readWorkingSet(workingSetID);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
 		
 		return new StringRepresentation(ws.toXML(), MediaType.TEXT_XML);
 	}
 
 	/**
 	 * Gets all working sets the user is subscribed to.
 	 * @param workingSetIO
 	 * @param request
 	 * @param response
 	 */
 	private Representation getWorkingSets(String username, WorkingSetIO workingSetIO) throws ResourceException {
 		final WorkingSet[] sets;
 		try {
 			sets = workingSetIO.getSubscribedWorkingSets(username);
 		} catch (PersistentException e) {
 			Debug.println("Failed to load working sets for {0}:\n{1}", username, e);
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 		
 		StringBuilder xml = new StringBuilder("<workingsets>");
 		for (WorkingSet set : sets)
 			xml.append(set.toXML());
 		xml.append("</workingsets>");
 		
		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 	}
 	
 	/**
 	 * For each taxa in the working set, returns the entire footprint.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param workingSetIO
 	 */
 	private Representation getTaxaFootprintForWorkingSet(Request request, Response response, String username, Integer workingSetID, WorkingSetIO workingSetIO) throws ResourceException {
 		final WorkingSet ws = workingSetIO.readWorkingSet(workingSetID);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Working set " + workingSetID + " not found for user " + username);
 		
 		StringBuilder csv = new StringBuilder();
 		for (Taxon taxon : ws.getTaxon()) {
 			csv.append(taxon.getFootprintCSV() + "\r\n");
 		}
 		writeReport(csv.toString(), username);
 		
 		response.setStatus(Status.SUCCESS_CREATED);
 		return new StringRepresentation("/raw" + getURLToSaveFootprint(username), MediaType.TEXT_ALL);
 	}
 	
 	private Representation getAssessmentsForWorkingSet(Request request, Response response, String username, Integer workingSetID, WorkingSetIO workingSetIO, Session session) throws ResourceException {
 		final WorkingSet ws = workingSetIO.readWorkingSet(workingSetID);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
 		
 		final StringBuilder xml = new StringBuilder();
 		xml.append("<root>");
 		for (Taxon taxon : ws.getTaxon()) {
 			AssessmentFilter filter = ws.getFilter();
 			AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);
 			
 			for (Assessment assessment : helper.getAssessments(taxon.getId()))
 				xml.append("<assessment id=\"" + assessment.getId() + "\" taxon=\"" + taxon.getId() + "\" />");
 		}
 		xml.append("</root>");
 		
 		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 	}
 	
 	/**
 	 * Returns all taxa associated with a working set
 	 * @param workingSetIO
 	 * @param request
 	 * @param response
 	 */
 	private Representation getTaxaListForWorkingSet(String username, Integer id, WorkingSetIO workingSetIO) throws ResourceException {
 		final WorkingSet ws = workingSetIO.readWorkingSet(id);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Working set " + id + " not found for user " + username);
 		
 		return new StringRepresentation(ws.getSpeciesIDsAsString(), MediaType.TEXT_PLAIN);
 	}
 
 	private void writeReport(String report, String username) {
 		DocumentUtils.writeVFSFile(getURLToSaveFootprint(username), SIS.get().getVFS(), report);
 	}
 	
 	private void getTaxaFootprintWithAdditionalInfo(Representation entity, Response response, String username, Integer id, WorkingSetIO workingSetIO, Session session) throws ResourceException {
 		WorkingSet ws = workingSetIO.readWorkingSet(id);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
 		
 		NativeDocument entityDoc = getEntityAsNativeDocument(entity);
 		
 		AssessmentFilter filter = AssessmentFilter.fromXML(entityDoc);
 		AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);
 
 		StringBuilder csv = new StringBuilder();
 		for (Taxon taxon : ws.getTaxon()) {
 			List<Assessment> assessments = helper.getAssessments(taxon.getId());
 			if (!assessments.isEmpty()) {
 				Collections.sort(assessments, new PublishedAssessmentsComparator());
 				Assessment first = assessments.get(0);
 				
 				String properCriteriaString;
 				String properCategoryAbbrev;
 				
 				RedListCriteriaField field = new RedListCriteriaField(first.getField(CanonicalNames.RedListCriteria));
 				if (field.isManual()) {
 					properCriteriaString = field.getManualCriteria();
 					properCategoryAbbrev = field.getManualCategory();
 				}
 				else {
 					properCriteriaString = field.getGeneratedCriteria();
 					properCategoryAbbrev = field.getGeneratedCategory();
 				}
 				
 				if ("".equals(properCategoryAbbrev))
 					properCategoryAbbrev = "N/A";
 				if ("".equals(properCriteriaString))
 					properCriteriaString = "N/A";
 				
 				csv.append(taxon.getFootprintCSV() + ",");
 				csv.append("\"" + properCriteriaString + "\",");
 				csv.append(properCategoryAbbrev + "\r\n");
 			}
 		}
 		
 		writeReport(csv.toString(), username);
 		response.setEntity("/raw" + getURLToSaveFootprint(username), MediaType.TEXT_ALL);
 		response.setStatus(Status.SUCCESS_CREATED);
 	}
 
 	protected void editTaxaInWorkingSet(Representation entity, Request request, Response response, String username, Integer id, Session session) throws ResourceException {
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		TaxonIO taxonIO = new TaxonIO(session);
 		
 		final WorkingSet ws = workingSetIO.readWorkingSet(id);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
 		
 		final User user = getUser(request, session);
 		if (user == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		
 		final Document doc = getEntityAsDocument(entity);
 		
 		Element docElement = doc.getDocumentElement();
 
 		final List<String> removeTaxonIDs = new ArrayList<String>();
 		final List<String> addedTaxonIDs = new ArrayList<String>();
 		
 		NodeList removed = docElement.getElementsByTagName("remove");
 		for (int i = 0; i < removed.getLength(); i++)
 			removeTaxonIDs.add(removed.item(i).getTextContent());
 		
 		NodeList added = docElement.getElementsByTagName("add");
 		for (int i = 0; i < added.getLength(); i++)
 			addedTaxonIDs.add(added.item(i).getTextContent());
 			
 		if (!removeTaxonIDs.isEmpty()) {
 			Collection<Taxon> taxaToRemove = new HashSet<Taxon>();
 			for (Taxon taxon : ws.getTaxon())
 				if (removeTaxonIDs.contains(Integer.toString(taxon.getId()))) {
 					taxaToRemove.add(taxon);
 				}
 			ws.getTaxon().removeAll(taxaToRemove);
 		}
 		
 		for (String taxonID : addedTaxonIDs) {
 			Taxon taxon = taxonIO.getTaxon(Integer.valueOf(taxonID));
 			if (taxon != null) {
 				ws.getTaxon().add(taxon);
 			}
 		}
 
 		if (workingSetIO.saveWorkingSet(ws, user)) {
 			response.setEntity("Successfully editted the taxa in your working set", MediaType.TEXT_PLAIN);
 			response.setStatus(Status.SUCCESS_OK);
 		} else {
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 		}
 	}
 
 	private void createWorkingSet(Representation entity, Response response, String username, Session session) throws ResourceException  {
 		NativeDocument ndoc = getEntityAsNativeDocument(entity);
 		
 		WorkingSet ws = WorkingSet.fromXML(ndoc.getDocumentElement());
 		
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		UserIO userIO = new UserIO(session);
 
 		// SETTING THE CREATOR AND ADDING THEM AS A SUBSCRIBED USER
 		User creator = userIO.getUserFromUsername(ws.getCreatorUsername());
 		ws.setCreator(creator);
 		ws.getUsers().add(creator);
 		
 		User user = userIO.getUserFromUsername(username);
 		if (ws.getId() == 0 && user != null) {
 			workingSetIO.saveWorkingSet(ws, user);
 			response.setStatus(Status.SUCCESS_OK);
 			response.setEntity(Integer.toString(ws.getId()), MediaType.TEXT_PLAIN);
 		} else {
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 		}
 	}
 
 	/**
 	 * Posts to a public working set and adds creates taxa
 	 * @param session
 	 * @param request
 	 * @param response
 	 */
 	private void editWorkingSet(Representation entity, String username, Integer id, Session session) throws ResourceException {
 		NativeDocument ndoc = getEntityAsNativeDocument(entity);
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		UserIO userIO = new UserIO(session);
 		
 		WorkingSet ws = workingSetIO.readWorkingSet(id);
 		if (ws == null)
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
 		
 		//Now, load in the changes...
 		WorkingSet.fromXML(ws, ndoc.getDocumentElement());
 		
 				
 		User user = userIO.getUserFromUsername(username);
 		if (user == null || !workingSetIO.saveWorkingSet(ws, user))
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	}
 	
 	private String getURLToSaveFootprint(String username) {
 		return "/users/" + username + "/reports/footprint.csv";
 	}
 
 	private void subscribeToPublicWorkingSet(Request request, Response response, String username, Integer id, WorkingSetIO workingSetIO) {
 		if (workingSetIO.subscribeToWorkingset(id, username)) {
 			response.setStatus(Status.SUCCESS_OK);
 			response.setEntity(workingSetIO.readWorkingSet(id).toXML(), MediaType.TEXT_XML);
 		} else {
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 		}
 	}
 
 }
