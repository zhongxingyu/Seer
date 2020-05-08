 package org.gwaspi.dao.jpa;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import org.gwaspi.dao.SampleInfoService;
 import org.gwaspi.model.SampleInfo;
 import org.gwaspi.model.SampleKey;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class JPASampleInfoService implements SampleInfoService {
 
 	private static final Logger log = LoggerFactory.getLogger(JPASampleInfoService.class);
 	private static final Logger LOG
 			= LoggerFactory.getLogger(JPASampleInfoService.class); // FIXME we not need two ;-)
 
 	private EntityManagerFactory emf = null;
 
 	public JPASampleInfoService() {
 	}
 
 	private EntityManager open() {
 
 		EntityManager em = emf.createEntityManager();
 		return em;
 	}
 	private void begin(EntityManager em) {
 		em.getTransaction().begin();
 	}
 	private void commit(EntityManager em) {
 		em.getTransaction().commit();
 	}
 	private void rollback(EntityManager em) {
 
 		if (em == null) {
 			LOG.error("Failed to create an entity manager");
 		} else {
 			try {
 				if (em.isOpen() && em.getTransaction().isActive()) {
 					em.getTransaction().rollback();
 				} else {
 					LOG.error("Failed to rollback a transaction: no active"
 							+ " connection or transaction");
 				}
 			} catch (PersistenceException ex) {
 				LOG.error("Failed to rollback a transaction", ex);
 			}
 		}
 	}
 	private void close(EntityManager em) {
 
 		if (em == null) {
 			LOG.error("Failed to create an entity manager");
 		} else {
 			try {
 				if (em.isOpen()) {
 					em.close();
 				}
 			} catch (IllegalStateException ex) {
 				LOG.error("Failed to close an entity manager", ex);
 			}
 		}
 	}
 
 	@Override
 	public String createSamplesInfoTable() {
 		return "1";
 	}
 
 	@Override
 	public List<SampleInfo> getAllSampleInfoFromDB() throws IOException {
 
 		List<SampleInfo> sampleInfos = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			sampleInfos = em.createNamedQuery("sampleInfo_list").getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching all sample-infos", ex);
 		} finally {
 			close(em);
 		}
 
 		return sampleInfos;
 	}
 
 	@Override
 	public List<SampleInfo> getAllSampleInfoFromDBByPoolID(Integer studyId) throws IOException {
 
 		List<SampleInfo> sampleInfos = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("sampleInfo_listByStudyId");
 			query.setParameter("studyId", studyId);
 			sampleInfos = (List<SampleInfo>) query.getResultList();
 		} catch (NoResultException ex) {
 			LOG.trace("Failed fetching a sample-info by study-id: " + studyId
 					+ " (not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching sample-info", ex);
 		} finally {
 			close(em);
 		}
 
 		return sampleInfos;
 	}
 
 	@Override
 	public List<SampleInfo> getCurrentSampleInfoFromDB(SampleKey key, Integer studyId) throws IOException {
 
 		List<SampleInfo> sampleInfos = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"sampleInfo_listBySampleIdFamilyIdStudyId");
 			query.setParameter("sampleId", key.getSampleId());
 			query.setParameter("familyId", key.getFamilyId());
 			query.setParameter("studyId", studyId);
 			sampleInfos = (List<SampleInfo>) query.getResultList();
 		} catch (NoResultException ex) {
 			LOG.trace("Failed fetching a sample-info by"
 					+ ": sample-id: " + key.getSampleId()
 					+ ", family-id: " + key.getFamilyId()
 					+ ", study-id: " + studyId
 					+ "; (not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching sample-info", ex);
 		} finally {
 			close(em);
 		}
 
 		return sampleInfos;
 	}
 
 	@Override
 	public void deleteSamplesByPoolId(Integer studyId) throws IOException {
 
 		int deleted = 0;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("sampleInfo_deleteByStudyId");
 			query.setParameter("studyId", studyId);
 			deleted = query.executeUpdate();
 		} catch (NoResultException ex) {
 			LOG.trace("Failed deleting sample-infos by"
 					+ ": study-id: " + studyId
 					+ "; (not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed deleting sample-infos", ex);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public void insertSampleInfos(Integer studyId, Collection<SampleInfo> sampleInfos) throws IOException {
 
 		for (SampleInfo sampleInfo : sampleInfos) {
 			sampleInfo.setPoolId(studyId);
 			EntityManager em = null;
 			try {
 				em = open();
 				begin(em);
 				em.persist(sampleInfo);
 				commit(em);
 			} catch (Exception ex) {
 				LOG.error("Failed adding a sample-info", ex);
 				rollback(em);
 			} finally {
 				close(em);
 			}
 		}
 	}
 }
