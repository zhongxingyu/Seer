 package org.iucn.sis.server.api.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.hibernate.TransientObjectException;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.locking.TaxonLockAquirer;
 import org.iucn.sis.server.api.persistance.SynonymDAO;
 import org.iucn.sis.server.api.persistance.TaxonDAO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.utils.DocumentUtils;
 import org.iucn.sis.server.api.utils.ServerPaths;
 import org.iucn.sis.server.api.utils.TaxaComparators;
 import org.iucn.sis.server.api.utils.TaxomaticException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Infratype;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.TaxonStatus;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.NodeCollection;
 import com.solertium.vfs.VFSPath;
 import com.solertium.vfs.provider.VersionedFileVFS;
 import com.solertium.vfs.utils.VFSUtils;
 import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;
 
 public class TaxomaticIO {
 
 	public final static String MERGE = "MERGE";
 	public final static String MERGE_UP_INFRARANK = "MERGE UP INFRARANK";
 	public final static String SPLIT = "SPLIT";
 	public final static String LATERAL_MOVE = "LATERAL MOVE";
 	public final static String PROMOTE = "PROMOTE";
 	public final static String DEMOTE = "DEMOTE";
 
 	protected String lastOperationUsername;
 	protected String lastOperationType;
 	protected Date lastOperationDate;
 	protected Set<Integer> taxaIDsChanged;
 	protected VersionedFileVFS vfs;
 
 	public TaxomaticIO(VersionedFileVFS vfs) {
 		this.vfs = vfs;
 		load();
 	}
 
 	public void demoteSpecies(Taxon taxon, Taxon newParent, User user) throws TaxomaticException {
 		if (taxon.getLevel() != TaxonLevel.SPECIES || newParent.getLevel() != TaxonLevel.SPECIES)
 			throw new TaxomaticException("You can only demote a species to an infrarank");
 
 		Set<Taxon> taxa = new HashSet<Taxon>();
 		Collection<Taxon> children = taxon.getChildren();
 		for (Taxon child : children) {
 			if (child.getLevel() != TaxonLevel.SUBPOPULATION)
 				throw new TaxomaticException("You can only demote a species to an infrarank");
 
 			Synonym synonym = Taxon.synonymizeTaxon(child);
 			synonym.setStatus(Synonym.NEW);
 			child.getSynonyms().add(synonym);
 			updateRLHistoryText(child, user);
 			child.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK_SUBPOPULATION));
 			taxa.add(child);
 		}
 
 		Synonym synonym = Taxon.synonymizeTaxon(taxon);
 		synonym.setStatus(Synonym.NEW);
 		taxon.getSynonyms().add(synonym);
 		updateRLHistoryText(taxon, user);
 		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK));
 		taxon.setInfratype(SIS.get().getInfratypeIO().getInfratype(Infratype.SUBSPECIES_NAME));
 		taxon.setParent(newParent);
 		newParent.getChildren().add(taxon);
 		taxon.getParent().getChildren().remove(taxon);
 		taxa.add(taxon);
 		taxa.add(newParent);
 
 		updateAndSave(taxa, user, true);
 			
 		recordLastUpdated(taxa, user, TaxomaticIO.DEMOTE);
 	}
 
 	private String generateRLHistoryText(Taxon taxon) {
 		String fullName = "<i>";
 		boolean isPlant = taxon.getFootprint().length >= 1 ? taxon.getFootprint()[0].equalsIgnoreCase("plantae")
 				: false;
 
 		for (int i = 5; i < (taxon.getLevel() >= TaxonLevel.SUBPOPULATION ? taxon.getLevel() - 1 : taxon.getLevel()); i++)
 			fullName += taxon.getFootprint()[i] + " ";
 
 		if (taxon.getInfratype() != null) {
 			if (taxon.getInfratype().getName().equals(Infratype.SUBSPECIES_NAME))
 				fullName += isPlant ? "subsp." : "ssp. ";
 			else if (taxon.getInfratype().getName().equals(Infratype.VARIETY_NAME))
 				fullName += "var. ";
 		}
 
 		if (taxon.getLevel() == TaxonLevel.SUBPOPULATION || taxon.getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION)
 			fullName += "</i>" + taxon.getName().replace("ssp.", "").replace("var.", "").trim();
 		else
 			fullName += taxon.getName().replace("ssp.", "").replace("var.", "").trim() + "</i>";
 
 		if (isPlant) {
 			fullName = fullName.replace("subsp.", "</i> subsp. <i>");
 			fullName = fullName.replace("var.", "</i> var. <i>");
 		}
 		return fullName;
 	}
 
 	public Date getLastOperationDate() {
 		return lastOperationDate;
 	}
 
 	public String getLastOperationType() {
 		return lastOperationType;
 	}
 
 	public String getLastOperationUsername() {
 		return lastOperationUsername;
 	}
 	
 	public Set<Integer> getTaxaIDsChanged() {
 		return taxaIDsChanged;
 	}
 
 	boolean isTaxomaticOperationNecessary(Taxon taxonToSave, Taxon oldTaxon) {
 		
 		return !(taxonToSave != null && oldTaxon != null && oldTaxon.getName().equals(taxonToSave.getName()) && 
 				oldTaxon.getParentID() == taxonToSave.getParentID() && 
 				oldTaxon.getTaxonLevel().getLevel() == taxonToSave.getTaxonLevel().getLevel() );
 	}
 
 	private void load() {
 		try {
 			if (vfs.exists(VFSUtils.parseVFSPath(ServerPaths.getLastTaxomaticOperationPath()))) {
 				Document operationDoc = vfs.getDocument(VFSUtils.parseVFSPath(ServerPaths
 						.getLastTaxomaticOperationPath()));
 				
 				taxaIDsChanged = new HashSet<Integer>();
 				
 				final NodeCollection nodes = new NodeCollection(operationDoc.getDocumentElement().getChildNodes());
 				for (Node node : nodes) {
 					if ("username".equals(node.getNodeName()))
 						lastOperationUsername = node.getTextContent();
 					else if ("timestamp".equals(node.getNodeName()))
 						lastOperationDate = new Date(Long.parseLong(node.getTextContent()));
 					else if ("type".equals(node.getNodeName()))
 						lastOperationType = node.getTextContent();
 					else if ("taxon".equals(node.getNodeName()))
 						taxaIDsChanged.add(Integer.valueOf(node.getTextContent()));
 					else if ("taxa".equals(node.getNodeName()))
 						taxaIDsChanged.add(Integer.valueOf(BaseDocumentUtils.impl.getAttribute(node, "id")));
 				}
 			}
 		} catch (VFSPathParseException e) {
 			Debug.println(e);
 		} catch (NumberFormatException e) {
 			Debug.println(e);
 		} catch (DOMException e) {
 			Debug.println(e);
 		} catch (IOException e) {
 			Debug.println(e);
 		}
 	}
 
 	public void mergeTaxa(List<Taxon> mergedTaxa, Taxon mainTaxa, User user) throws TaxomaticException {
 		List<Taxon> taxaToSave = new ArrayList<Taxon>();
 		for (Taxon mergedTaxon : mergedTaxa) {
 			mergedTaxon.setStatus(TaxonStatus.STATUS_SYNONYM);
 
 			Synonym synonym = Taxon.synonymizeTaxon(mergedTaxon);
 			synonym.setStatus(Synonym.MERGE);
 			mainTaxa.getSynonyms().add(synonym);
 			synonym.setTaxon(mainTaxa);
 
 			synonym = Taxon.synonymizeTaxon(mainTaxa);
 			synonym.setStatus("MERGE");
 			mergedTaxon.getSynonyms().add(synonym);
 			synonym.setTaxon(mergedTaxon);
 
 			for (Taxon child : mergedTaxon.getChildren()) {
 				child.setParent(mainTaxa);
 				mainTaxa.getChildren().add(child);
 				taxaToSave.add(child);
 			}
 			taxaToSave.add(mergedTaxon);
 		}
 		taxaToSave.add(mainTaxa);
 		
 		updateAndSave(taxaToSave, user, true);
 		recordLastUpdated(taxaToSave, user, TaxomaticIO.MERGE);
 	}
 
 	public void mergeUpInfraranks(List<Taxon> taxa, Taxon species, User user) throws TaxomaticException {
 		List<Taxon> taxaToSave = new ArrayList<Taxon>();
 		
 		for (Taxon mergedTaxon : taxa) {
 			mergedTaxon.setStatus(TaxonStatus.STATUS_SYNONYM);
 			
 			Synonym synonym = Taxon.synonymizeTaxon(mergedTaxon);
 			synonym.setStatus(Synonym.MERGE);
 			species.getSynonyms().add(synonym);
 			synonym.setTaxon(species);
 			
 			synonym = Taxon.synonymizeTaxon(species);
 			synonym.setStatus("MERGE");			
 			synonym.setTaxon(mergedTaxon);
 			mergedTaxon.getSynonyms().add(synonym);
 			
 			for (Taxon infrarankChild : mergedTaxon.getChildren()) {
 				// child.setTaxa(mainTaxa);
 				// mainTaxa.getParent().add(child);
 				String oldName = infrarankChild.generateFullName();
 				infrarankChild.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.SUBPOPULATION));
 				String newFullName = infrarankChild.generateFullName();
 				if (!oldName.equals(newFullName)) {
 					synonym = Taxon.synonymizeTaxon(infrarankChild);
 					if (infrarankChild.getTaxonomicAuthority() != null) {
 						synonym.setAuthority(infrarankChild.getTaxonomicAuthority(), infrarankChild.getLevel());
 					}
 					infrarankChild.getSynonyms().add(synonym);
 					infrarankChild.setFriendlyName(newFullName);
 				}
 				taxaToSave.add(infrarankChild);
 			}
 			taxaToSave.add(mergedTaxon);
 		}
 		taxaToSave.add(species);
 
 		updateAndSave(taxaToSave, user, true);
 		
 		recordLastUpdated(taxaToSave, user, TaxomaticIO.MERGE_UP_INFRARANK);
 	}
 
 	/**
 	 * Move the children taxa from current parent to new parent, important that
 	 * the new parent and the old parent are of the same taxon level
 	 * 
 	 * @param newParent
 	 * @param children
 	 * @param user
 	 * @return
 	 */
 	public void moveTaxa(Taxon newParent, Collection<Taxon> children, User user) throws TaxomaticException {
 		Set<Taxon> changed = new HashSet<Taxon>();
 		for (Taxon child : children)
 			changed.addAll(moveTaxon(newParent, child, user, Synonym.NEW, true));
 		
 		updateAndSave(changed, user, true);
 		
 		recordLastUpdated(changed, user, TaxomaticIO.LATERAL_MOVE);
 	}
 
 	/**
 	 * 
 	 * Moves the child from its current parent to the newParent. If the name has
 	 * changed, then a synonym is added with the status provided * Returns a set
 	 * of taxa that needs to be saved.
 	 * 
 	 * 
 	 * @param newParent
 	 * @param child
 	 * @param synStatus
 	 *            -- if null no synymn is created
 	 * @param updateAssessments
 	 *            -- if true, then updates teh assessment names
 	 * @return
 	 */
 	private Set<Taxon> moveTaxon(Taxon newParent, Taxon child, User user, String synStatus, boolean updateAssessments) {
 		Set<Taxon> changed = new HashSet<Taxon>();
 		Taxon oldParent = child.getParent();
 
 		if (synStatus != null) {
 			String oldChildName = child.generateFullName();
 			child.setParent(newParent);
 			String newChildName = child.generateFullName();
 
 			if (!oldChildName.equals(newChildName)) {
 
 				ArrayList<Taxon> footprints = new ArrayList<Taxon>();
 				footprints.add(child);
 				while (!footprints.isEmpty()) {
 					Taxon current = footprints.remove(0);
 					footprints.addAll(current.getChildren());
 
 					// DO NOT SWITCH ORDER OF SETTING PARENT, ORDER MATTERS
 					child.setParent(newParent);
 					String newName = current.generateFullName();
 					child.setParent(oldParent);
 					String oldName = current.generateFullName();
 
 					if (!oldName.equals(newName)) {
 						changed.add(current);
 						if (synStatus != null) {
 							Synonym syn = Taxon.synonymizeTaxon(current);
 							syn.setStatus(synStatus);
 							current.getSynonyms().add(syn);
 						}
 						if (updateAssessments) {
 							updateRLHistoryText(current, user);
 						}
 						child.setFriendlyName(newName);
 
 					}
 				}
 			}
 		}
 		changed.add(child);
 		changed.add(oldParent);
 		changed.add(newParent);
 
 		child.setParent(newParent);
 		oldParent.getChildren().remove(child);
 		newParent.getChildren().add(child);
 
 		return changed;
 	}
 
 	public void performTaxomaticUndo(String username) throws TaxomaticException {
 		// GATHER OPERTAION, FAST FAIL IF ERROR IN PARSING LAST OPERATION
 		if (getLastOperationType() == null)
 			throw new TaxomaticException("Could not find or load previous operation.");
 
 		// DO VALIDATION OF USER
 		if (!username.equalsIgnoreCase(this.lastOperationUsername) && !username.equalsIgnoreCase("admin"))
 			throw new TaxomaticException("Could not find or load previous operation for this user.  Another user may have edited this taxa.");
 
 		// GATHER LOCKS
 		String[] ids = new String[taxaIDsChanged.size()];
 		int i = 0;
 		for (Integer taxon : taxaIDsChanged) {
 			ids[i] = taxon + "";
 			i++;
 		}
 		TaxonLockAquirer lockAquirer = new TaxonLockAquirer(ids);
 		lockAquirer.aquireLocks();
 		
 		boolean validated = false;
 
 		if (!lockAquirer.isSuccess()) {
 			throw new TaxomaticException("Could not acquire lock to perform operation", false);
 		} else {
 			validated = SIS.get().getLocker().aquireWithRetry(ServerPaths.getLastTaxomaticOperationPath(), 5);
 			if (!validated)
 				throw new TaxomaticException("Could not acquire lock to perform operation", false);
 		}
 		
 		User user = SIS.get().getUserIO().getUserFromUsername(username);
 
 		try {
 			// DO VALIDATION ON TAXA
 			for (Integer taxaID : taxaIDsChanged) {
 				if (vfs.getLastModified(new VFSPath(ServerPaths.getTaxonURL(taxaID + ""))) > lastOperationDate.getTime()) {
 					validated = false;
 					break;
 				}
 			}
 			
 			if (!validated)
 				throw new TaxomaticException("Could not validate previous operations, stopping.", false);
 
 			for (Integer taxaID : taxaIDsChanged) {
 				final VFSPath uri = new VFSPath(ServerPaths.getTaxonURL(taxaID +""));
 				
 				List<String> revision = vfs.getRevisionIDsBefore(uri, null, 1);
 				List<Taxon> taxaToSave = new ArrayList<Taxon>();
 				List<Assessment> assessmentsToSave = new ArrayList<Assessment>();
 				
 				Taxon currentTaxon = SIS.get().getTaxonIO().getTaxonNonLazy(taxaID);
 				
 				//THERE ARE NO OTHER REVISIONS, THEREFORE IT WAS CREATED ON 
 				if (revision.size() == 0) {
 					taxaToSave.add(currentTaxon);
 					TaxonDAO.deleteAndDissociate(currentTaxon);
 				} else {
 					InputStream in = vfs.getInputStream(uri, revision.get(0));
 					StringBuilder out = new StringBuilder();
 					byte[] b = new byte[4096];
 					for (int n; (n = in.read(b))!= -1;)
 						out.append(new String(b, 0, n));
 					
 					NativeDocument ndoc = new JavaNativeDocument();
 					ndoc.parse(out.toString());
 					
 					Taxon oldTaxon = Taxon.fromXML(ndoc);
 
 					//IF SYNONYMS ADDED THEN NAME CHANGED, AND PUBLISHED ASSESSMENTS MIGHT BE UPDATED
 					if (currentTaxon.getSynonyms().size() > oldTaxon.getSynonyms().size()) {
 						for (Synonym synonym : currentTaxon.getSynonyms()) {
 							if (!oldTaxon.getSynonyms().contains(synonym)) {
 								SynonymDAO.delete(synonym);
 								TaxonDAO.refresh(oldTaxon);
 								taxaToSave.add(oldTaxon);
 								break;
 							}
 						}
 						
 						//NEED TO LOOK FOR CHANGED RLHISTORY
 						for (Assessment assessment : SIS.get().getAssessmentIO().readPublishedAssessmentsForTaxon(currentTaxon)) {
 							Field field = assessment.getField(CanonicalNames.RedListCriteria);
 							if (field == null)
 								field = new Field(CanonicalNames.RedListCriteria, assessment);
 							
 							RedListCriteriaField proxy = new RedListCriteriaField(field);
							String currentValue = "as " + proxy.getRLHistoryText();
 							if (currentValue.equalsIgnoreCase(generateRLHistoryText(currentTaxon))) {
 								proxy.setRLHistoryText("");
 								assessment.getField().add(field);
 								assessmentsToSave.add(assessment);
 							}
 						}	
 					}					
 				}
 				
 				for (Taxon taxon : taxaToSave)
 					writeTaxon(taxon, user);
 				
 				for (Assessment assessment : assessmentsToSave)
 					SIS.get().getAssessmentIO().writeAssessment(assessment, user, true);
 				
 			}
 		} catch (IOException e) {
 			Debug.println("Failed on undo: {0}");
 			throw new TaxomaticException(e);
 		} catch (PersistentException e) {
 			Debug.println("Failed on undo: {0}");
 			throw new TaxomaticException(e);
 		} finally {
 			lockAquirer.releaseLocks();
 			SIS.get().getLocker().releaseLock(ServerPaths.getLastTaxomaticOperationPath());
 		}
 
 	}
 
 	/**
 	 * Promotes the infrarank to a species. Once promoted, its former
 	 * grandparent (the genus) will be its parent, and all of its children will
 	 * have a taxon level of subpopulation.
 	 * 
 	 * @param taxon
 	 * @param user
 	 * @return
 	 */
 	public void promoteInfrarank(Taxon taxon, User user) throws TaxomaticException {
 		if (taxon.getTaxonLevel().getLevel() != (TaxonLevel.INFRARANK))
 			throw new TaxomaticException("You should only be promoting an infrarank.");
 		
 		Set<Taxon> changed = new HashSet<Taxon>();
 		changed.add(taxon);
 		changed.addAll(taxon.getChildren());
 
 		for (Taxon current : changed) {
 			Synonym synonym = Taxon.synonymizeTaxon(current);
 			synonym.setStatus(Synonym.NEW);
 			current.getSynonyms().add(synonym);
 			updateRLHistoryText(current, user);
 
 			if (current.getLevel() == TaxonLevel.INFRARANK)
 				current.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.SPECIES));
 			else if (current.getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION)
 				current.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.SUBPOPULATION));
 			else
 				throw new TaxomaticException("You should only be promoting an infrarank.");
 		}
 
 		taxon.setInfratype(null);
 		Taxon oldParent = taxon.getParent();
 		Taxon newParent = oldParent.getParent();
 		taxon.setParent(newParent);
 		oldParent.getChildren().remove(taxon);
 		newParent.getChildren().add(taxon);
 		changed.add(oldParent);
 		changed.add(newParent);
 
 		updateAndSave(changed, user, true);
 		recordLastUpdated(changed, user, TaxomaticIO.PROMOTE);
 	}
 
 	public synchronized void recordLastUpdated(Collection<Taxon> changedTaxon, User user, String operation) throws TaxomaticException {
 		lastOperationDate = new Date();
 		lastOperationType = operation;
 		lastOperationUsername = user.getUsername();
 
 		taxaIDsChanged = new HashSet<Integer>();
 		for (Taxon changed : changedTaxon)
 			taxaIDsChanged.add(changed.getId());
 
 		write();
 	}
 
 	//FIXME NEED FUTHER CHECKS FOR UNIQUE NAME IN KINGDOM
 	public void saveNewTaxon(Taxon taxon, User user) throws TaxomaticException {
 		if (taxon.getId() == 0 || SIS.get().getTaxonIO().getTaxonFromVFS(taxon.getId()) == null)
 			writeTaxon(taxon, null, user, false);
 		else
 			throw new TaxomaticException("This taxon already exists and can not be created.");
 	}
 
 	/**
 	 * Removes all children of the oldTaxon and places them into the the new
 	 * Parent which is stored in the parentTOChildren map
 	 * 
 	 * @param oldTaxon
 	 * @param user
 	 * @param parentToChildren
 	 * @return
 	 */
 	public void splitNodes(Taxon oldTaxon, User user, HashMap<Taxon, ArrayList<Taxon>> parentToChildren) throws TaxomaticException {
 		Set<Taxon> taxaToSave = new HashSet<Taxon>();
 		taxaToSave.add(oldTaxon);
 		
 		if (oldTaxon.getLevel() >= TaxonLevel.SPECIES)
 			oldTaxon.setStatus(TaxonStatus.STATUS_SYNONYM);
 
 		for (Entry<Taxon, ArrayList<Taxon>> entry : parentToChildren.entrySet()) {
 			Taxon parent = entry.getKey();
 			ArrayList<Taxon> children = entry.getValue();
 
 			Synonym synonym = Taxon.synonymizeTaxon(oldTaxon);
 			synonym.setStatus(Synonym.SPLIT);
 			if (synonym.getSpeciesAuthor() != null)
 				synonym.setSpeciesAuthor(synonym.getSpeciesAuthor() + " <i>pro parte</i>");
 			parent.getSynonyms().add(synonym);
 			taxaToSave.add(parent);
 
 			// MOVE EACH CHILD AND ADD SYNONYMS
 			for (Taxon child : children) {
 				taxaToSave.addAll(moveTaxon(parent, child, user, Synonym.SPLIT, false));
 			}
 		}
 
 		updateAndSave(taxaToSave, user, true);
 		recordLastUpdated(taxaToSave, user, TaxomaticIO.SPLIT);
 	}
 	
 	private void updateAndSave(Collection<Taxon> taxaToSave, User user, boolean requireLocking) throws TaxomaticException {
 		TaxonLockAquirer acquirer = new TaxonLockAquirer(taxaToSave);
 		if (requireLocking)
 			acquirer.aquireLocks();
 
 		if (requireLocking && !acquirer.isSuccess())
 			throw new TaxomaticException("Failed to acquire lock to save taxa.", false);
 		
 		ArrayList<Taxon> taxa = new ArrayList<Taxon>(taxaToSave);
 		Map<Integer, Taxon> idsToTaxon = new HashMap<Integer, Taxon>();
 		for (Taxon taxon : taxaToSave)
 			idsToTaxon.put(taxon.getId(), taxon);
 		
 		Collections.sort(taxa, TaxaComparators.getTaxonComparatorByLevel());
 			
 		try {
 			Date date = new Date();
 			for (Taxon taxon : taxa) {
 				//UPDATE FRIENDLY NAME
 				if (idsToTaxon.containsKey(taxon.getParent().getId())) {
 					taxon.setParent(idsToTaxon.get(taxon.getParent().getId()));
 					taxon.getParent().correctFullName();
 					Debug.println("parent was updated with fullname " + taxon.getParent().getName());
 				}
 				Debug.println("this is full name before " + taxon.getFullName());
 				taxon.correctFullName();					
 				Debug.println("this is full name after " + taxon.getFullName());
 				//ADD EDIT						
 				Edit edit = new Edit();
 				edit.setUser(user);
 				edit.setCreatedDate(date);
 				edit.getTaxon().add(taxon);
 				
 				taxon.getEdits().add(edit);
 				taxon.toXML();
 				
 				writeTaxon(taxon, user);
 			}
 		} finally {
 			acquirer.releaseLocks();
 		}
 	}
 	
 	/**
 	 * Updates the RL history on all published assessments associated with the
 	 * taxon
 	 * 
 	 * IMPORTANT -- Must be called before parent changed and before level
 	 * changed
 	 * 
 	 */
 	private void updateRLHistoryText(Taxon taxon, User user) {
 		for (Assessment current : SIS.get().getAssessmentIO().readPublishedAssessmentsForTaxon(taxon)) {
 			Field field = current.getField(CanonicalNames.RedListCriteria);
 			if (field == null)
 				field = new Field(CanonicalNames.RedListCriteria, current);
 			
 			RedListCriteriaField proxy = new RedListCriteriaField(field);
 			String text = proxy.getRLHistoryText();
 			if (text == null || text.equals("")) {
 				proxy.setRLHistoryText("as " + generateRLHistoryText(taxon));
 			}
 			
 			SIS.get().getAssessmentIO().writeAssessment(current, user, false);
 		}
 	}
 	
 	protected synchronized void write() throws TaxomaticException {
 		if (SIS.get().getLocker().aquireWithRetry(ServerPaths.getLastTaxomaticOperationPath(), 5)) {
 			try {
 				StringBuilder xml = new StringBuilder();
 				xml.append("<operation>");
 				xml.append("<username>" + lastOperationUsername + "</username>");
 				xml.append("<timestamp>" + lastOperationDate.getTime() + "</timestamp>");
 				xml.append("<type>" + lastOperationType + "</type>");
 				for (Integer taxaID : taxaIDsChanged)
 					xml.append("<taxon>" + taxaID + "</taxon>");
 				xml.append("</operation>");
 
 				if (!DocumentUtils.writeVFSFile(ServerPaths.getLastTaxomaticOperationPath(), vfs, xml.toString()))
 					throw new TaxomaticException("Failed to write operation history file.", false);
 			} finally {
 				SIS.get().getLocker().releaseLock(ServerPaths.getLastTaxomaticOperationPath());
 			}
 		}
 		else
 			throw new TaxomaticException("Failed to acquire lock to perform write.", false);
 	}
 	
 	void writeTaxon(Taxon taxonToSave, Taxon oldTaxon, User user, boolean requireLocking) throws TaxomaticException {
 		if (oldTaxon == null || !isTaxomaticOperationNecessary(taxonToSave, oldTaxon)) {
 			SIS.get().getTaxonIO().writeTaxon(taxonToSave, oldTaxon, user);
 		} else {
 			// TRY TO AQUIRE LOCKS
 			List<Taxon> taxaToSave;
 			try {
 				taxonToSave.toXML();
 				taxaToSave = SIS.get().getTaxonIO().getChildrenOfTaxonRecurisvely(taxonToSave.getId());
 			} catch (PersistentException e) {
 				throw new TaxomaticException("Failed to find child to write taxon.", e);
 			} catch (TransientObjectException e) {
 				throw new TaxomaticException("Failed to find child to write taxon.", e);
 			}
 			taxaToSave.add(0, taxonToSave);
 			
 			updateAndSave(taxaToSave, user, requireLocking);
 		}
 	}
 	
 	public void writeTaxon(Taxon taxonToSave, User user) throws TaxomaticException {
 		Taxon oldTaxon = SIS.get().getTaxonIO().getTaxonFromVFS(taxonToSave.getId());
 		if (oldTaxon == null)
 			throw new TaxomaticException("This taxa could not be found.", false);
 		
 		writeTaxon(taxonToSave, oldTaxon, user, true);
 	}
 
 }
