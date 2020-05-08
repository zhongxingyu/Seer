 package org.iucn.sis.server.api.io;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.locking.LockType;
 import org.iucn.sis.server.api.persistance.AssessmentCriteria;
 import org.iucn.sis.server.api.persistance.AssessmentDAO;
 import org.iucn.sis.server.api.persistance.AssessmentTypeCriteria;
 import org.iucn.sis.server.api.persistance.TaxonCriteria;
 import org.iucn.sis.server.api.persistance.UserCriteria;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.utils.RegionConflictException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.io.AssessmentIOMessage;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.fields.ProxyField;
 import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.restlet.data.Status;
 
 /**
  * Performs file system IO operations for Assessments.
  * 
  * @author adam.schwartz
  * 
  */
 public class AssessmentIO {
 
 	private final Session session;
 	
 	public AssessmentIO(Session session) {
 		this.session = session; 
 	}
 
 	/**
 	 * Contains information about a write that occurs via this helper class. If
 	 * a single assessment is written back, the status and last modified fields
 	 * will be set to reflect the lock status of the assessment, and if the
 	 * writeback was successful, the new last modified date to be sent back to
 	 * the client. The result String may be populated with the assessment's ID. <br>
 	 * <br>
 	 * If a bulk save was performed, the result String will be populated with an
 	 * XML document containing information on successful and failed writebacks.
 	 * 
 	 * @author adam
 	 */
 	public static class AssessmentIOWriteResult {
 		public Status status;
 		public Integer id;
 		public Edit edit;
 
 		public AssessmentIOWriteResult(Status status, Integer assessmentID, Edit edit) {
 			this.status = status;
 			this.id = assessmentID;
 			this.edit = edit;
 		}
 	}
 
 	/**
 	 * The default call to getAssessment. Should get from cached then get from
 	 * VFS, then get from DB. This version will not be merged into database so
 	 * if you need to update and then save you should call
 	 * getAttachedAssessment. If you don't know if it exists, then call exists
 	 * first.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public Assessment getAssessment(Integer id) {
 		Assessment assessment = null;
 
 		// TODO: GET FROM CACHE FIRST
 		// assessment = getFromVFS(id);
 		// if (assessment == null)
 			assessment = getNonCachedAssessment(id);
 
 		return assessment;
 
 	}
 
 	public String getAssessmentXML(Integer id) {
 		Assessment assessment = getAssessment(id);
 		if (assessment != null)
 			return assessment.toXML();
 		else
 			return null;
 		/*try {
 			return getXMLFromVFS(id);
 		} catch (NotFoundException e) {
 			Assessment assessment = getNonCachedAssessment(id);
 			Edit edit = assessment.getLastEdit();
 			String xml = assessment.toXML();
 			String serverPaths = ServerPaths.getAssessmentURL(assessment);
 			DocumentUtils.writeVFSFile(serverPaths, vfs, xml);
 			
 			if (edit != null)
 				vfs.setLastModified(new VFSPath(serverPaths), edit.getCreatedDate());
 
 			return xml;
 		} catch (BoundsException e) {
 			Debug.println(e);
 		} catch (IOException e) {
 			Debug.println(e);
 		}
 		return null;*/
 	}
 
 	/**
 	 * The way to get an "attached" assessment if you need to do operations on
 	 * the assessment and then save.
 	 * 
 	 * @param id
 	 * @return
 	 * @throws PersistentException
 	 * @throws HibernateException
 	 */
 	public Assessment getAttachedAssessment(Integer id) {
 		return getNonCachedAssessment(id);
 		/*Assessment assessment = getFromVFS(id);
 		try {
 			assessment = SIS.get().getManager().mergeObject(assessment);
 		} catch (PersistentException e) {
 			//Guess we're getting it uncached...
 			assessment = null;
 		}
 		
 		if (assessment == null)
 			assessment = getNonCachedAssessment(id);
 		
 		return assessment;*/
 	}
 
 	/**
 	 * The last case call for the Assessment. This is called when building an
 	 * assessment object from the DB only. Will take minutes. By default call
 	 * getAssessment
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public Assessment getNonCachedAssessment(Integer id) {
 		try {
 			return AssessmentDAO.getAssessment(session, id);
 		} catch (PersistentException e) {
 			// TODO Auto-generated catch block
 			Debug.println(e);
 			return null;
 		}
 	}
 
 	/**
 	 * Reads in regional draft assessments based on the taxonID. When we shuffle
 	 * storage to store globals in with the regionals, this will read in all
 	 * assessments.
 	 * 
 	 * @param vfs
 	 * @param taxonID
 	 * @return a list of Assessment objects. Will never be null, might be empty.
 	 * @throws PersistentException
 	 */
 	public List<Assessment> readDraftAssessmentsForTaxon(Integer taxonID) {
 
 		AssessmentCriteria criteria = new AssessmentCriteria(session);
 		
 		AssessmentTypeCriteria assessmentTypeCriteria = criteria.createAssessment_typeCriteria();
 		assessmentTypeCriteria.id.eq(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID);
 		TaxonCriteria taxonCriteria = criteria.createTaxonCriteria();
 		taxonCriteria.id.eq(taxonID);
 		return Arrays.asList(AssessmentDAO.getAssessmentsByCriteria(criteria));
 	}
 
 	public List<Assessment> readPublishedAssessmentsForTaxon(Integer taxonID) {
 		AssessmentCriteria criteria = new AssessmentCriteria(session);
 		
 		AssessmentTypeCriteria assessmentTypeCriteria = criteria.createAssessment_typeCriteria();
 		assessmentTypeCriteria.id.eq(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID);
 		TaxonCriteria taxonCriteria = criteria.createTaxonCriteria();
 		taxonCriteria.id.eq(taxonID);
 		return Arrays.asList(AssessmentDAO.getAssessmentsByCriteria(criteria));
 	}
 
 	/**
 	 * This method returns a List of all draft assessments - global and regional
 	 * - for one taxon. If no assessments exist, the list will be empty.
 	 * 
 	 * @param taxonID
 	 */
 	public List<Assessment> readAssessmentsForTaxon(Integer taxonID) {
 
 		AssessmentCriteria criteria = new AssessmentCriteria(session);
 		
 		TaxonCriteria taxonCriteria = criteria.createTaxonCriteria();
 		taxonCriteria.id.eq(taxonID);
 		return new ArrayList<Assessment>(Arrays.asList(AssessmentDAO.getAssessmentsByCriteria(criteria)));
 	}
 
 	public List<Assessment> readPublishedAssessmentsForTaxon(Taxon taxon) {
 		return readPublishedAssessmentsForTaxon(taxon.getId());
 	}
 
 	public List<Assessment> getAssessments(List<Integer> ids, boolean hardFail) {
 		ArrayList<Assessment> list = new ArrayList<Assessment>();
 		for (Integer cur : ids) {
 			Assessment assessment = getAssessment(cur);
 			if (assessment == null) {
 				if (hardFail)
 					return null;
 			} else {
 				list.add(assessment);
 			}
 		}
 
 		return list;
 	}
 
 	/**
 	 * Reads in a list of regional draft assessments for a list of taxon ids.
 	 * 
 	 * @param vfs
 	 * @param ids
 	 *            - a list of taxon ids, as a csv
 	 * @param region
 	 *            - restrict to this region
 	 * @return
 	 */
 	public List<Assessment> readRegionalDraftAssessmentsForTaxonList(List<Integer> taxonIDs, List<Integer> regionIDs) {
 		List<Assessment> list = new ArrayList<Assessment>();
 		for (Integer taxonID : taxonIDs) {
 			for (Assessment assessment : readDraftAssessmentsForTaxon(taxonID)) {
 				for (Integer regionID : regionIDs)
 					if (assessment.getRegionIDs().contains(regionID)) {
 						list.add(assessment);
 						break;
 					}
 			}
 		}
 
 		return list;
 	}
 
 	public Assessment[] getTrashedAssessments() throws PersistentException {
 		return AssessmentDAO.getTrashedAssessments(session);
 	}
 
 	/**
 	 * Returns either the restored Deleted assessment or null if failure
 	 * 
 	 * @param assessmentID
 	 * @param user
 	 * @return
 	 * @throws PersistentException
 	 */
 	public AssessmentIOWriteResult restoreTrashedAssessments(Integer assessmentID, User user) throws PersistentException {
 		Assessment assessment = AssessmentDAO.getTrashedAssessment(session, assessmentID);
 		if (assessment != null) {
 			assessment.setState(Assessment.ACTIVE);
 			return writeAssessment(assessment, user, true);
 		}
 		return null;
 
 	}
 
 	public AssessmentIOWriteResult trashAssessment(Assessment assessment, User user) {
 		assessment.setState(Assessment.DELETED);
 		return writeAssessment(assessment, user, true);
 	}
 
 	public boolean permenantlyDeleteAssessment(Integer assessmentID, User user) {
 		Assessment assessment = getDeletedAssessment(assessmentID);
 		if (assessment != null) {
 			try {
 				return AssessmentDAO.deleteAndDissociate(assessment, session);
 			} catch (PersistentException e) {
 				Debug.println(e);
 			}
 		}
 		return false;
 	}
 
 	public boolean permenantlyDeleteAllTrashedAssessments() {
 		
 		try {
 			for (Assessment assessmentToSave : getTrashedAssessments())
 				if (!AssessmentDAO.deleteAndDissociate(assessmentToSave, session)) {
 					throw new PersistentException("Unable to delete assessment " + assessmentToSave.getId());
 				}
 			return true;
 		} catch (PersistentException e) {
 			Debug.println(e);
 		}
 		return false;
 		
 	}
 
 	public Assessment getDeletedAssessment(Integer assessmentID) {
 		try {
 			return AssessmentDAO.getTrashedAssessment(session, assessmentID);
 		} catch (PersistentException e) {
 			// TODO Auto-generated catch block
 			Debug.println(e);
 			return null;
 		}
 	}
 
 	public AssessmentIOMessage restoreDeletedAssessmentsAssociatedWithTaxon(Integer taxonID, User user)
 			throws PersistentException {
 		AssessmentCriteria criteria = new AssessmentCriteria(session);
 		TaxonCriteria taxCriteria = criteria.createTaxonCriteria();
 		taxCriteria.id.eq(taxonID);
 		Assessment[] assessments = AssessmentDAO.getTrashedAssessmentsByCriteria(criteria);
 		for (Assessment ass : assessments) {
 			ass.setState(Assessment.ACTIVE);
 		}
 		return writeAssessments(Arrays.asList(assessments), user, true);
 	}
 
 	public AssessmentIOWriteResult writeAssessment(Assessment assessmentToSave, User user, boolean requireLocking) {
 
 		Status lockStatus = Status.SUCCESS_OK;
 		if (SIS.amIOnline() && requireLocking)
			lockStatus = SIS.get().getLocker().
			persistentLockAssessment(assessmentToSave.getId(), LockType.SAVE_LOCK,
 					user);
 
 		if (lockStatus.isSuccess()) {
 			Edit edit = new Edit();
 			edit.setUser(user);
 			edit.getAssessment().add((assessmentToSave));
 			assessmentToSave.getEdit().add(edit);
 			assessmentToSave.toXML();
 
 			
 				try {
 					SIS.get().getManager().saveObject(session, assessmentToSave);
 				} catch (PersistentException e) {
 					Debug.println(e);
 					return new AssessmentIOWriteResult(lockStatus, 0, null);
 				}
 
 			return new AssessmentIOWriteResult(lockStatus, assessmentToSave.getId(), edit);
 
 		}
 		return new AssessmentIOWriteResult(lockStatus, 0, null);
 
 	}
 
 	/**
 	 * Best effort to write assessments. If some were locked then it will be
 	 * noted in in the assessmentIOmessage
 	 * 
 	 * @param assessments
 	 * @param user
 	 * @param requireLocking
 	 * @return
 	 */
 	public AssessmentIOMessage writeAssessments(List<Assessment> assessments, User user, boolean requireLocking) {
 		AssessmentIOMessage ret = new AssessmentIOMessage();
 		for (Assessment current : assessments) {
 			AssessmentIOWriteResult result = writeAssessment(current, user, requireLocking);
 			if (result.status.isClientError()) {
 				ret.addLocked(current);
 			} else if (result.status.isServerError()) {
 				ret.addFailed(current);
 			} else if (result.status.isSuccess()) {
 				ret.addSuccessfullySaved(current);
 			}/* else if (result.status.isClientError()) {
 				ret.addInsufficientPermissions(current);
 			}*/
 		}
 		return ret;
 	}
 	
 	public void saveAssessments(Collection<Assessment> assessments, User user) throws PersistentException {
 		Status lockStatus = Status.SUCCESS_OK;
 		if (SIS.amIOnline())
 			lockStatus = SIS.get().getLocker().persistentLockAssessments(assessments, LockType.SAVE_LOCK, user);
 
 		if (lockStatus.isSuccess()) {
 			for (Assessment assessmentToSave : assessments) {
 				Edit edit = new Edit();
 				edit.setUser(user);
 				edit.getAssessment().add((assessmentToSave));
 				assessmentToSave.getEdit().add(edit);
 				assessmentToSave.toXML();
 				SIS.get().getManager().saveObject(session, assessmentToSave);
 				SIS.get().getManager().saveObject(session, edit);
 			}
 		}
 	}
 
 	/**
 	 * FIXME: this is bad for obvious reasons.
 	 * @param assessments
 	 * @param user
 	 * @return
 	 */
 	public boolean saveAssessmentsWithNoFail(Collection<Assessment> assessments, User user) {
 		Status lockStatus = Status.SUCCESS_OK;
 		boolean success = false;
 		if (SIS.amIOnline()) {
 			lockStatus = SIS.get().getLocker().persistentLockAssessments(assessments, LockType.SAVE_LOCK, user);
 		}
 
 		if (lockStatus.isSuccess()) {
 			try {
 				for (Assessment assessmentToSave : assessments) {
 					Edit edit = new Edit();
 					edit.setUser(user);
 					edit.getAssessment().add((assessmentToSave));
 					assessmentToSave.getEdit().add(edit);
 					assessmentToSave.toXML();
 					SIS.get().getManager().saveObject(session, assessmentToSave);
 					SIS.get().getManager().saveObject(session, edit);
 				}
 				success = true;
 			} catch (PersistentException e) {	
 				Debug.println(e);
 			}
 		}
 
 		return success;
 	}
 
 	public boolean moveAssessments(Taxon newParent, Collection<Assessment> assessments, User user) {
 		for (Assessment assessment : assessments) {
 			assessment.setTaxon(newParent);
 		}
 		return saveAssessmentsWithNoFail(assessments, user);
 	}
 
 	public boolean allowedToCreateNewAssessment(Assessment assessment) {
 		return assessment.isPublished() || 
 			!assessment.hasRegions() || 
 			!(assessment.isDraft() && conflicts(assessment, 
 					readDraftAssessmentsForTaxon(assessment.getTaxon().getId())));
 	}
 	
 	public boolean conflicts(Assessment assessment, List<Assessment> existing) {
 		List<Integer> regionIDs = assessment.getRegionIDs();
 		String defaultSchema = SIS.get().getDefaultSchema();
 		for (Assessment cur : existing) {
 			if (!cur.getSchema(defaultSchema).equals(assessment.getSchema(defaultSchema)))
 				continue;
 			
 			if ((cur.isGlobal() && assessment.isGlobal())
 					|| cur.getRegionIDs().containsAll(regionIDs)) {
 				if (cur.getId() != assessment.getId())
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public AssessmentIOWriteResult saveNewAssessment(Assessment assessent, User user) throws RegionConflictException {
 		if (!allowedToCreateNewAssessment(assessent))
 			throw new RegionConflictException();
		return writeAssessment(assessent, user, false);
 	}
 	
 	public void publish(Assessment assessment) {
 		assessment.setType(AssessmentType.PUBLISHED_ASSESSMENT_TYPE);
 		//TODO: attach reference from target release to publicationreference
 		Field rlAsmAuthorsField = assessment.getField(CanonicalNames.RedListAssessmentAuthors);
 		ProxyField rlAsmAuthors = new ProxyField(rlAsmAuthorsField);
 		String value = rlAsmAuthors.getStringPrimitiveField("value");
 		if ("".equals(value)) {
 			if (rlAsmAuthorsField == null) {
 				rlAsmAuthorsField = new Field(CanonicalNames.RedListAssessmentAuthors, assessment);
 				rlAsmAuthors = new ProxyField(rlAsmAuthorsField);
 				assessment.getField().add(rlAsmAuthorsField);
 				session.save(rlAsmAuthorsField);
 			}
 			RedListCreditedUserField assessors = new RedListCreditedUserField(assessment.getField(CanonicalNames.RedListAssessors));
 			if (!assessors.getUsers().isEmpty()) {
 				UserCriteria criteria = new UserCriteria(session);
 				criteria.id.in(assessors.getUsers().toArray(new Integer[assessors.getUsers().size()]));
 				List<User> users = Arrays.asList(criteria.listUser());
 				rlAsmAuthors.setStringPrimitiveField("value", RedListCreditedUserField.generateText(users, assessors.getOrder()));
 				session.saveOrUpdate(rlAsmAuthorsField);
 			}
 		}
 		
 		for (String fieldName : CanonicalNames.credits) {
 			Field field = assessment.getField(fieldName);
 			if (field == null)
 				continue;
 			
 			RedListCreditedUserField proxy = new RedListCreditedUserField(field);
 			if (!proxy.getText().equals("")) {
 				proxy.setTextPrimitiveField("publication", proxy.getText());
 			}
 			else if (!proxy.getUsers().isEmpty()) {
 				UserCriteria criteria = new UserCriteria(session);
 				criteria.id.in(proxy.getUsers().toArray(new Integer[proxy.getUsers().size()]));
 				List<User> users = Arrays.asList(criteria.listUser());
 				
 				proxy.setTextPrimitiveField("publication",
 					RedListCreditedUserField.generateText(users, proxy.getOrder())
 				);
 			}
 			session.update(field);
 		}
 		
 		/*
 		 * TODO: elide highlight tag from narratives
 		 */
 		
 		Field taxonomicNotes = assessment.getTaxon().getTaxonomicNotes();
 		if (taxonomicNotes != null) {
 			Set<Reference> refs = new HashSet<Reference>();
 			for (Reference reference : taxonomicNotes.getReference())
 				refs.add((Reference)session.get(Reference.class, reference.getId()));
 			
 			taxonomicNotes = taxonomicNotes.deepCopy(false, false);
 			taxonomicNotes.setReference(refs);
 			
 			Field existing = assessment.getField(CanonicalNames.TaxonomicNotes);
 			if (existing != null)
 				assessment.getField().remove(existing);
 			assessment.getField().add(taxonomicNotes);
 		}
 	}
 
 	/**
 	 * ONLY CALLED BY THE SISHIBERNATE LISTENER, OR WHEN SOMETHING 
 	 * BAD HAPPENS AND YOU NEED TO WRITE IT OUT TO THE VFS
 	 * 
 	 * @param assessment
 	 */
 	public void afterSaveAssessment(Assessment assessment) {
 		/*Edit edit = assessment.getLastEdit();
 		String xml = assessment.toXML();
 		String serverPaths = ServerPaths.getAssessmentURL(assessment);
 		DocumentUtils.writeVFSFile(serverPaths, vfs, xml);
 		
 		vfs.setLastModified(new VFSPath(serverPaths), edit.getCreatedDate());*/
 	}
 
 	/*protected Assessment getFromVFS(Integer id) {
 		try {
 			String assessmentXML = getXMLFromVFS(id);
 			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
 			ndoc.parse(assessmentXML);
 			return Assessment.fromXML(ndoc);
 		} catch (NotFoundException e) {
 			Debug.println("--- Assessment " + id + " not found on File System. Serving DB copy.");
 		} catch (BoundsException e) {
 			Debug.println(e);
 		} catch (IOException e) {
 			Debug.println(e);
 		}
 		return null;
 	}
 
 	protected String getXMLFromVFS(Integer id) throws NotFoundException, BoundsException, IOException {
 		return vfs.getString(new VFSPath(ServerPaths.getAssessmentUrl(id)));
 	}*/
 
 }
