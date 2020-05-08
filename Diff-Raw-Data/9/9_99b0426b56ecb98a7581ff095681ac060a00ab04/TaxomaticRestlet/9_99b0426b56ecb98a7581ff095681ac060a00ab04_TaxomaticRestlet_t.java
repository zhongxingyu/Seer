 package org.iucn.sis.server.restlets.taxa;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.InfratypeIO;
 import org.iucn.sis.server.api.io.TaxomaticIO;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.api.utils.TaxomaticException;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.TaxomaticOperation;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeElementCollection;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.ElementCollection;
 import com.solertium.util.Mutex;
 import com.solertium.util.NodeCollection;
 
 public class TaxomaticRestlet extends BaseServiceRestlet {
 
 	private Mutex lock = new Mutex();
 
 	public TaxomaticRestlet(Context context) {
 		super(context);
 	}
 
 	public void definePaths() {
 		paths.add("/taxomatic/{operation}");
 		paths.add("/taxomatic/{operation}/{taxonid}");
 	}
 
 	private void trashTaxon(Integer id, Request request, final Response response, Session session) throws TaxomaticException, ResourceException {
 		TaxonIO taxonIO = new TaxonIO(session);
 		Taxon taxon = taxonIO.getTaxon(id);
 		if (taxon == null)
 			throw new TaxomaticException("Taxon not found.");
 		
 		taxonIO.trashTaxon(taxon, getUser(request, session));
 	}
 
 	/**
 	 * For demotions, a species is being demoted (Demoted1) to infrarank. The
 	 * user selects a species for which the species will be the new parent of
 	 * Demoted1. The children of Demoted1. The subpopulations of Demoted1 is
 	 * moved to the status of an infrarank subpopulation. If the species had
 	 * infrarank, the species can not be demoted until the infrarank has already
 	 * been moved out.
 	 * 
 	 * when demoting a -something to a sub-something, the sub-something gets a
 	 * synonym pointing to the demoted -something
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxomaticIO TODO
 	 * @param taxonIO TODO
 	 * @param user TODO
 	 * @param response
 	 */
 	private void demoteNode(Element documentElement, Request request, TaxomaticIO taxomaticIO, TaxonIO taxonIO, User user) throws TaxomaticException {
 		Element demoted = (Element) documentElement.getElementsByTagName("demoted").item(0);
 		String id = demoted.getAttribute("id");
 		String newParentID = demoted.getTextContent();
 		
 		Taxon taxon = taxonIO.getTaxon(Integer.valueOf(id));
 		Taxon parent = taxonIO.getTaxon(Integer.valueOf(newParentID));
 
 		taxomaticIO.demoteSpecies(taxon, parent, user);
 	}
 
 	private Taxon doAddNewTaxon(NativeElement newTaxon, TaxomaticIO taxomaticIO, User user) throws TaxomaticException {
 		Taxon taxon = Taxon.fromXML(newTaxon);
 		
 		taxomaticIO.saveNewTaxon(taxon, user);
 		
 		return taxon;
 	}
 
 	/**
 	 * Gets the last taxomatic operation if it is the correct user, or returns
 	 * bad request if the user is not able to do an undo.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param taxomaticIO TODO
 	 * @param user TODO
 	 */
 	public Representation getLastTaxomaticOperation(Request request, Response response, TaxomaticIO taxomaticIO, User user) throws TaxomaticException, ResourceException {
 		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
 		/*String lastUser = taxomaticIO.getLastOperationUsername();
 
 		if (username.equalsIgnoreCase(lastUser) || username.equalsIgnoreCase("admin")) {
 			Collection<Integer> taxaIDs = taxomaticIO.getTaxaIDsChanged();
 			StringBuilder files = new StringBuilder();
 			for (Integer file : taxaIDs) {
 				files.append(file + ", ");
 			}
 			if (taxaIDs.size() > 0) {
 				files.replace(files.length() - 2, files.length(), "");
 			} else
 				files.append("none (no taxa was affected)");
 			
 			String lastOperation = taxomaticIO.getLastOperationType();
 			return new StringRepresentation("A " + lastOperation
 					+ " was the last taxomatic operation that was performed, which affected taxa " + files.toString()
 					+ ".", MediaType.TEXT_PLAIN);
 		} else
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);*/
 	}
 
 	/**
 	 * For merge the taxomatic is designed so that you select one or more
 	 * species ( say Old1 and Old2) to merge into a species (New1, which has
 	 * already been previously created). Old1 and Old 2 will then receive a
 	 * synonym to New1 and Old1 and Old2 will both be marked as deprecated. None
 	 * of the information that was associated with Old1 or Old2 are moved into
 	 * New1. New1 will receive deprecated synonyms to both Old1 and Old2.
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxomaticIO TODO
 	 * @param taxonIO TODO
 	 * @param user TODO
 	 * @param response
 	 */
 	private void mergeTaxa(Element documentElement, Request request, TaxomaticIO taxomaticIO, TaxonIO taxonIO, User user) throws TaxomaticException {
 		String mergedIDs = "";
 		String mainID = "";
 		
 		try {
 			mergedIDs = documentElement.getElementsByTagName("merged").item(0).getTextContent();
 			mainID = documentElement.getElementsByTagName("main").item(0).getTextContent();
 		} catch (Exception e) {
 			throw new TaxomaticException("Invalid information supplid in request.  Need merged and main taxa.");
 		}
 		
 		List<Taxon> mergedTaxa = taxonIO.getTaxa(mergedIDs);
 		
 		Taxon taxon = taxonIO.getTaxon(Integer.valueOf(mainID));
 		
 		taxomaticIO.mergeTaxa(mergedTaxa, taxon, user);
 	}
 
 	/**
 	 * Given a document which holds the speciesID and the infranks ids, merges
 	 * the infraranks into the species and moves the suppopulations of the
 	 * infraranks to be suppopulations of the species. Places a synonym in the
 	 * infrarank, and in the suppopulatiosn.
 	 * 
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxomaticIO TODO
 	 * @param taxonIO TODO
 	 * @param user TODO
 	 * @param response
 	 */
 	private void mergeUpInfraranks(Element documentElement, Request request, TaxomaticIO taxomaticIO, TaxonIO taxonIO, User user) throws TaxomaticException {
 
 		String mergedIDs = "";
 		String mainID = "";
 		try {
 			mergedIDs = documentElement.getElementsByTagName("infrarank").item(0).getTextContent();
 			mainID = documentElement.getElementsByTagName("species").item(0).getTextContent();
 		} catch (Exception e) {
 			throw new TaxomaticException("Please supply valid infrarank and species");
 		}
 
 		List<Taxon> taxa = taxonIO.getTaxa(mergedIDs);
 		
 		Taxon main = taxonIO.getTaxon(Integer.valueOf(mainID));
 		
 		taxomaticIO.mergeUpInfraranks(taxa, main, user);
 	}
 
 	/**
 	 * Given a document which has the oldNode id and the assessment ids to move,
 	 * moves the assessmentID to the new parentID
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxonIO TODO
 	 * @param user TODO
 	 * @param session TODO
 	 * @param response
 	 */
 	private void moveAssessments(final Element documentElement, Request request, TaxonIO taxonIO, User user, Session session) throws TaxomaticException, ResourceException {
 		final NodeList oldNode = documentElement.getElementsByTagName("oldNode");
 		final NodeList nodeToMoveAssessmentsInto = documentElement.getElementsByTagName("nodeToMoveInto");
 		final NodeList assessmentNodeList = documentElement.getElementsByTagName("assessmentID");
 
 		if (oldNode.getLength() != 1) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		} else if (nodeToMoveAssessmentsInto.getLength() != 1) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		} else if (assessmentNodeList.getLength() == 0) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		} else {
 			String newNodeID = nodeToMoveAssessmentsInto.item(0).getTextContent();
 			
 			AssessmentIO io = new AssessmentIO(session);
 			
 			ArrayList<Assessment> assessments = new ArrayList<Assessment>();
 			for (Node id : new NodeCollection(assessmentNodeList)) {
 				assessments.add(io.getAttachedAssessment(
 						Integer.valueOf(id.getTextContent())));
 			}
 			
 			if (!io.moveAssessments(
 					taxonIO.getTaxon(Integer.valueOf(newNodeID)), assessments,
 					user)) {
 				throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
 			}
 		}
 	}
 
 	/**
 	 * For the lateral move, (for a species or any other rank) you choose the
 	 * new genus (or rank directly higher) where you would like to move the
 	 * species to. All of the children of the moved taxa are also moved. Besides
 	 * this, the information about the the moved species is unchanged.
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxonIO TODO
 	 * @param taxomaticIO TODO
 	 * @param user TODO
 	 * @param response
 	 */
 	private void moveNodes(Element documentElement, Request request, TaxonIO taxonIO, TaxomaticIO taxomaticIO, User user) throws TaxomaticException {
 		Element parentElement = (Element) documentElement.getElementsByTagName("parent").item(0);
 		String parentID = parentElement.getAttribute("id");
 		
 		final Taxon parentNode = taxonIO.getTaxonNonLazy(Integer.valueOf(parentID));
 		
 		final Collection<Taxon> childrenTaxa = new ArrayList<Taxon>();
 		NodeList newChildren = documentElement.getElementsByTagName("child");
 		for (int i = 0; i < newChildren.getLength(); i++) {
 			String id = ((Element) newChildren.item(i)).getAttribute("id");
 			childrenTaxa.add(taxonIO.getTaxonNonLazy(Integer.valueOf(id)));
 		}
 
 		taxomaticIO.moveTaxa(parentNode, childrenTaxa, user);
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		String operation = (String) request.getAttributes().get("operation");
 		
 		boolean acquired = lock.attempt();
 		if (!acquired)
 			throw new ResourceException(Status.CLIENT_ERROR_LOCKED);
 		
 		TaxomaticIO taxomaticIO = new TaxomaticIO(session);
 		User user = getUser(request, session);
 		
 		Representation representation = null;
 		try {
 			if (operation.equalsIgnoreCase("undo"))
 				representation = getLastTaxomaticOperation(request, response, taxomaticIO, user);
 			else if (operation.equalsIgnoreCase("history")) {
 				String taxonID = (String)request.getAttributes().get("taxonid");
 				if (taxonID == null)
 					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a taxon ID");
 				representation = getTaxomaticHistory(request, response, taxomaticIO, taxonID); 
 			}
 			else
 				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
 		} catch (TaxomaticException e){
 			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, e.getErrorAsDocument()));
 			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : 
 				Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		} finally {
 			if (acquired)
 				lock.release();
 		}
 		
 		return representation;
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		String operation = (String) request.getAttributes().get("operation");
 		
 		boolean acquired = lock.attempt();
 		if (!acquired)
 			throw new ResourceException(Status.CLIENT_ERROR_LOCKED);
 		
 		final TaxomaticIO taxomaticIO = new TaxomaticIO(session);
 		final TaxonIO taxonIO = new TaxonIO(session);
 		final User user = getUser(request, session);
 		
 		try {
 			if (operation.equalsIgnoreCase("update"))
 				updateTaxon(entity, request, taxonIO, taxomaticIO, session);
 			else if (operation.equalsIgnoreCase("undo"))
 				undoLastTaxomaticOperation(request, taxomaticIO, session);
 			else if (operation.equalsIgnoreCase("merge")) {
 				mergeTaxa(getEntityAsDocument(entity).getDocumentElement(), request, taxomaticIO, taxonIO, user);
 			} else if (operation.equalsIgnoreCase("moveAssessments")) {
 				moveAssessments(getEntityAsDocument(entity).getDocumentElement(), request, taxonIO, user, session);
 			} else if (operation.equalsIgnoreCase("mergeupinfrarank")) {
 				mergeUpInfraranks(getEntityAsDocument(entity).getDocumentElement(), request, taxomaticIO, taxonIO, user);
 			} else if (operation.equalsIgnoreCase("split")) {
 				splitNodes(getEntityAsDocument(entity).getDocumentElement(), request, taxomaticIO, taxonIO, user);
 			} else if (operation.equalsIgnoreCase("move")) {
 				moveNodes(getEntityAsDocument(entity).getDocumentElement(), request, taxonIO, taxomaticIO, user);
 			} else if (operation.equalsIgnoreCase("promote")) {
 				promoteInfrarank(getEntityAsDocument(entity).getDocumentElement(), request, taxonIO, taxomaticIO, user);
 			} else if (operation.equalsIgnoreCase("demote")) {
 				demoteNode(getEntityAsDocument(entity).getDocumentElement(), request, taxomaticIO, taxonIO, user);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
 		} catch (TaxomaticException e){
 			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, e.getErrorAsDocument()));
 			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : 
 				Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		} finally {
 			if (acquired)
 				lock.release();
 		}
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		String operation = (String) request.getAttributes().get("operation");
 		
 		boolean acquired = lock.attempt();
 		if (!acquired)
 			throw new ResourceException(Status.CLIENT_ERROR_LOCKED);
 		
 		TaxomaticIO taxomaticIO = new TaxomaticIO(session);
 		User user = getUser(request, session);
 		
 		try {
 			if (operation.equalsIgnoreCase("new"))
 				putNewTaxon(entity, request, response, taxomaticIO, user);
 			else if (operation.equalsIgnoreCase("batch")) {
 				putBatch(entity, request, response, taxomaticIO, session);
 			}
 			else
 				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
 		} catch (TaxomaticException e){
 			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, e.getErrorAsDocument()));
 			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : 
 				Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		} finally {
 			if (acquired)
 				lock.release();
 		}
 	}
 	
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
 		String operation = (String) request.getAttributes().get("operation");
 		
 		boolean acquired = lock.attempt();
 		if (!acquired)
 			throw new ResourceException(Status.CLIENT_ERROR_LOCKED);
 		
 		try {
 			trashTaxon(Integer.valueOf(operation), request, response, session);
 		} catch (NumberFormatException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		} catch (TaxomaticException e){
 			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, e.getErrorAsDocument()));
 			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : 
 				Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		} finally {
 			if (acquired)
 				lock.release();
 		}
 	}
 	
 	private Representation getTaxomaticHistory(Request request, Response response, TaxomaticIO taxomaticIO, String taxonID) throws TaxomaticException, ResourceException {
 		final Integer id;
 		try { 
 			id = Integer.valueOf(taxonID);
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid taxon ID " + taxonID + " supplied.");
 		}
 		
 		List<TaxomaticOperation> list = taxomaticIO.getTaxomaticHistory(id);
 		
 		StringBuilder xml = new StringBuilder();
 		xml.append("<root>");
 		for (TaxomaticOperation operation : list) {
 			xml.append(operation.toXML());
 		}
 		xml.append("</root>");
 		
 		return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 	}
 
 	/**
 	 * For promotions an infrank is being moved into a species. The infrarank
 	 * will just be promoted to be "brothers" with its previous parent, and all
 	 * of the subpopulations of the infrarank are moved to be subpopulations of
 	 * the newly promoted species. Besides this, the information about the the
 	 * promoted species is unchanged.
 	 * 
 	 * if you promote a sub-something to a -something, the -something isn't
 	 * getting a synonym pointing to the promoted sub-something. Check to make
 	 * sure it puts the authority in the synonym.
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxonIO TODO
 	 * @param taxomaticIO TODO
 	 * @param user TODO
 	 * @param response
 	 */
 	private void promoteInfrarank(Element documentElement, Request request, TaxonIO taxonIO, TaxomaticIO taxomaticIO, User user) throws TaxomaticException {
 		Element promoted = (Element) documentElement.getElementsByTagName("promoted").item(0);
 		String id = promoted.getAttribute("id");
 		
 		Taxon taxon = taxonIO.getTaxon(Integer.valueOf(id));
 		
 		taxomaticIO.promoteInfrarank(taxon, user);
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param request
 	 * @param response
 	 * @param taxomaticIO TODO
 	 * @param session TODO
 	 * @return
 	 * @throws Exception
 	 */
 	private void putBatch(Representation entity, Request request, Response response, TaxomaticIO taxomaticIO, Session session) throws TaxomaticException, ResourceException {
 		NativeDocument ndoc = new JavaNativeDocument();
 		try {
 			ndoc.parse(entity.getText());
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		NativeNodeList taxa = ndoc.getDocumentElement().getElementsByTagName(Taxon.ROOT_TAG);
 		NativeElementCollection collection = new NativeElementCollection(taxa);
 		
 		StringBuilder ids = new StringBuilder();
 		User user = getUser(request, session);
 		
 		for (NativeElement element : collection) {
 			Taxon taxon = doAddNewTaxon(element, taxomaticIO, user);
 			ids.append(taxon.getId() + ",");
 		}
 
 		if (ids.toString().endsWith(","))
 			response.setEntity(ids.substring(0, ids.length() - 1), MediaType.TEXT_PLAIN);
 		else
 			response.setEntity(ids.toString(), MediaType.TEXT_PLAIN);
 	}
 
 	private void putNewTaxon(Representation entity, Request request, Response response, TaxomaticIO taxomaticIO, User user) throws TaxomaticException, ResourceException {
 		NativeDocument ndoc = new JavaNativeDocument();
 		try {
 			ndoc.parse(entity.getText());
 		} catch (Exception e){
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		
 		Taxon taxon = doAddNewTaxon(ndoc.getDocumentElement(), taxomaticIO, user);
 		
 		response.setEntity(taxon.getId() + "", MediaType.TEXT_PLAIN);
 		response.setStatus(Status.SUCCESS_OK);		
 	}
 
 	/**
 	 * For split the taxomatic is designed so that you select the node (Old1)
 	 * that you would like to split. You are then prompted to create new taxa
 	 * which you would like to split the node into (at least 2, lets say New1,
 	 * New2). For each child of Old1, you can choose to place it under either
 	 * New1 or in New2. Old1 is deprecated, and has two new Synonyms linking to
 	 * New1 and New2. New1 and New2 receive the children that was selected for
 	 * them, and they both receive a deprecated synonym to Old1. None of the
 	 * assessments are transferred from Old1.
 	 * 
 	 * @param documentElement
 	 * @param request
 	 * @param taxomaticIO TODO
 	 * @param taxonIO TODO
 	 * @param user TODO
 	 * @param response
 	 */
 	private void splitNodes(Element documentElement, Request request, TaxomaticIO taxomaticIO, TaxonIO taxonIO, User user) throws TaxomaticException, ResourceException {
 		Element originalNode = (Element) documentElement.getElementsByTagName("current").item(0);
 		String oldId = originalNode.getTextContent();
 		
 		Taxon oldNode = taxonIO.getTaxon(Integer.valueOf(oldId));
 		
 		HashMap<Taxon, List<Taxon>> parentToChildren = new HashMap<Taxon, List<Taxon>>();
 		int childrenSize = 0;
 		
 		ElementCollection parents = new ElementCollection(documentElement.getElementsByTagName("parent"));
 		for (Element parent : parents) {
 			String parentID = parent.getAttribute("id");
 			Taxon parentTaxon = taxonIO.getTaxon(Integer.valueOf(parentID));
 			ElementCollection children = new ElementCollection(parent.getElementsByTagName("child"));
 			ArrayList<Taxon> childrenTaxa = new ArrayList<Taxon>();
 			for (Element child : children) {
 				String childID = child.getTextContent();
 				childrenTaxa.add(taxonIO.getTaxon(Integer.valueOf(childID)));
 			}
 			parentToChildren.put(parentTaxon, childrenTaxa);
 			childrenSize += childrenTaxa.size();
 
 		}
 		// DOING VALIDATION TO MAKE SURE THEY SPLIT ALL CHILDREN
 		if (oldNode.getChildren().size() != childrenSize)
 			throw new TaxomaticException("Request failed: You must split all the children.");
 
 		taxomaticIO.splitNodes(oldNode, user, parentToChildren);
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param taxomaticIO TODO
 	 * @param session TODO
 	 * @param docElement
 	 * @param response
 	 */
 	public void undoLastTaxomaticOperation(Request request, TaxomaticIO taxomaticIO, Session session) throws TaxomaticException, ResourceException {
 		User user = getUser(request, session);
 		
 		taxomaticIO.performTaxomaticUndo(user);
 	}
 	
 	public void updateTaxon(Representation entity, Request request, TaxonIO taxonIO, TaxomaticIO taxomaticIO, Session session) throws TaxomaticException, ResourceException {
 		final NativeDocument ndoc = new JavaNativeDocument();
 		try {
 			ndoc.parse(entity.getText());
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		
 		Taxon updatedTaxon = Taxon.fromXML(ndoc);
 		Taxon currentTaxon = taxonIO.getTaxon(updatedTaxon.getId());
 		
 		currentTaxon.setName(updatedTaxon.getName());
 		currentTaxon.setTaxonLevel(updatedTaxon.getTaxonLevel());
 		currentTaxon.setHybrid(updatedTaxon.getHybrid());
 		currentTaxon.setTaxonomicAuthority(updatedTaxon.getTaxonomicAuthority());
 		currentTaxon.setStatus(updatedTaxon.getStatusCode());
 		currentTaxon.setFeral(updatedTaxon.getFeral());
 		currentTaxon.setInvasive(updatedTaxon.getInvasive());
		currentTaxon.correctFullName();
 		
 		InfratypeIO io = new InfratypeIO(session);
 		
 		if (updatedTaxon.getInfratype() == null) 
 			currentTaxon.setInfratype(null);
 		else {
 			currentTaxon.setInfratype(io.getInfratype(updatedTaxon.getInfratype().getName()));
 		}
 		
 		taxomaticIO.writeTaxon(currentTaxon, getUser(request, session));
 	}
 
 }
