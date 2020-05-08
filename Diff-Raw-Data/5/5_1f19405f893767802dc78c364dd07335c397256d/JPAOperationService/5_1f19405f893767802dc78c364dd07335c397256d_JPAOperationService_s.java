 package org.gwaspi.dao.jpa;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.dao.OperationService;
 import org.gwaspi.dao.sql.OperationServiceImpl;
 import org.gwaspi.global.Config;
 import org.gwaspi.model.MatrixOperationSpec;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.OperationMetadata;
 import org.gwaspi.model.ReportsList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * JPA implementation of a operation service.
  * Uses abstracted DB access to store data,
  * see persistence.xml for DB settings.
  */
 public class JPAOperationService implements OperationService {
 
 	private static final Logger LOG
 			= LoggerFactory.getLogger(JPAOperationService.class);
 
 	private final EntityManagerFactory emf;
 
 
 	public JPAOperationService(EntityManagerFactory emf) {
 		this.emf = emf;
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
 	public Operation getById(int operationId) throws IOException {
 
 		Operation operation = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("operation_fetchById");
 			query.setParameter("id", operationId);
 			operation = (Operation) query.getSingleResult();
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a operation by id: " + operationId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a operation by id: " + operationId, ex);
 		} finally {
 			close(em);
 		}
 
 		return operation;
 	}
 
 	@Override
 	public List<Operation> getOperationsList(int parentMatrixId) throws IOException {
 
 		List<Operation> operations = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("operation_listByParentMatrixId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			operations = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operations", ex);
 		} finally {
 			close(em);
 		}
 
 		return operations;
 	}
 
 	@Override
 	public List<Operation> getOperationsList(int parentMatrixId, int parentOpId) throws IOException {
 
 		List<Operation> operations = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operation_listByParentMatrixIdParentOperationId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("parentOperationId", parentOpId);
 			operations = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operations", ex);
 		} finally {
 			close(em);
 		}
 
 		return operations;
 	}
 
 	@Override
 	public List<Operation> getOperationsList(int parentMatrixId, int parentOpId, OPType opType) throws IOException {
 
 		List<Operation> operations = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operation_listByParentMatrixIdParentOperationIdOperationType");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("parentOperationId", parentOpId);
 			query.setParameter("operationType", opType);
 			operations = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operations", ex);
 		} finally {
 			close(em);
 		}
 
 		return operations;
 	}
 
 	@Override
 	public List<OperationMetadata> getOperationsTable(int parentMatrixId) throws IOException {
 
 		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			operationsMetadata = query.getResultList();
 
 			for (int i = 0; i < operationsMetadata.size(); i++) {
 				operationsMetadata.set(i, OperationServiceImpl.completeOperationMetadata(operationsMetadata.get(i)));
 			}
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operation-metadata", ex);
 		} finally {
 			close(em);
 		}
 
 		return operationsMetadata;
 	}
 
 	@Override
 	public List<OperationMetadata> getOperationsTable(int parentMatrixId, int operationId) throws IOException {
 
 		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixIdOperationId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("operationId", operationId);
 			operationsMetadata = query.getResultList();
 
 			query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixIdParentOperationId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("parentOperationId", operationId);
 			operationsMetadata.addAll(query.getResultList());
 
 			for (int i = 0; i < operationsMetadata.size(); i++) {
 				operationsMetadata.set(i, OperationServiceImpl.completeOperationMetadata(operationsMetadata.get(i)));
 			}
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operation-metadata", ex);
 		} finally {
 			close(em);
 		}
 
 		return operationsMetadata;
 	}
 
 	@Override
 	public String createOperationsMetadataTable() {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 		return "1";
 	}
 
 	@Override
 	public void insertOPMetadata(OperationMetadata operationMetadata) throws IOException {
 
 		EntityManager em = null;
 		try {
 			em = open();
 			begin(em);
 			em.persist(operationMetadata);
 			Operation operation = new Operation(
 					operationMetadata.getOPId(),
 					operationMetadata.getOPName(),
 					operationMetadata.getMatrixCDFName(),
 					operationMetadata.getGenotypeCode(),
 					operationMetadata.getParentMatrixId(),
 					operationMetadata.getParentOperationId(),
 					"", // command
 					operationMetadata.getDescription(),
 					operationMetadata.getStudyId()
 					);
 			em.persist(operation);
 			commit(em);
 		} catch (Exception ex) {
 			LOG.error("Failed persisting operation-metadata", ex);
 			rollback(em);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public List<MatrixOperationSpec> getMatrixOperations(int matrixId) throws IOException {
 
 		List<MatrixOperationSpec> matrixOperationSpecs = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixId");
 			query.setParameter("parentMatrixId", matrixId);
 			matrixOperationSpecs = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching matrix-operation-specs", ex);
 		} finally {
 			close(em);
 		}
 
 		return matrixOperationSpecs;
 	}
 
 	@Override
 	public void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException {
 
 		try {
 			Operation op = getById(opId);
 			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
 
 			List<Operation> operations = getOperationsList(op.getParentMatrixId(), opId);
 			if (!operations.isEmpty()) {
 				operations.add(op);
 				for (int i = 0; i < operations.size(); i++) {
 					File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + operations.get(i).getNetCDFName() + ".nc");
 					org.gwaspi.global.Utils.tryToDeleteFile(matrixOPFile);
 					if (deleteReports) {
 						ReportsList.deleteReportByOperationId(operations.get(i).getId());
 					}
 
 					EntityManager em = null;
 					try {
 						em = open();
 						begin(em);
 						em.remove(getById(operations.get(i).getId()));
 						commit(em);
 					} catch (Exception ex) {
 						LOG.error("Failed removing a matrix", ex);
 						rollback(em);
 					} finally {
 						close(em);
 					}
 				}
 			} else {
 				File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + op.getNetCDFName() + ".nc");
 				org.gwaspi.global.Utils.tryToDeleteFile(matrixOPFile);
 				if (deleteReports) {
 					ReportsList.deleteReportByOperationId(opId);
 				}
 
 				EntityManager em = null;
 				try {
 					em = open();
 					begin(em);
 					em.remove(getById(opId));
 					commit(em);
 				} catch (Exception ex) {
 					LOG.error("Failed removing a matrix", ex);
 					rollback(em);
 				} finally {
 					close(em);
 				}
 			}
 		} catch (IOException ex) {
 			LOG.warn(null, ex);
 			// PURGE INEXISTING OPERATIONS FROM DB
 			EntityManager em = null;
 			try {
 				em = open();
 				begin(em);
 				em.remove(getById(opId));
 				commit(em);
 			} catch (Exception exi) {
 				LOG.error("Failed removing a matrix", ex);
 				rollback(em);
 			} finally {
 				close(em);
 			}
 		} catch (IllegalArgumentException ex) {
 			LOG.warn(null, ex);
 			// PURGE INEXISTING OPERATIONS FROM DB
 			EntityManager em = null;
 			try {
 				em = open();
 				begin(em);
 				em.remove(getById(opId));
 				commit(em);
 			} catch (Exception exi) {
 				LOG.error("Failed removing a matrix", ex);
 				rollback(em);
 			} finally {
 				close(em);
 			}
 		}
 	}
 
 	@Override
 	public OperationMetadata getOperationMetadata(int operationId) throws IOException {
 
 		OperationMetadata operationMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("operationMetadata_fetchById");
 			query.setParameter("id", operationId);
 			operationMetadata = (OperationMetadata) query.getSingleResult();
 			operationMetadata = OperationServiceImpl.completeOperationMetadata(operationMetadata);
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a operation-metadata by id: " + operationId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a operation-metadata by id: " + operationId, ex);
 		} finally {
 			close(em);
 		}
 
 		return operationMetadata;
 	}
 
 	@Override
 	public OperationMetadata getOperationMetadata(String netCDFName) throws IOException {
 
 		OperationMetadata operationMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_fetchByNetCDFName");
 			query.setParameter("netCDFName", netCDFName);
 			operationMetadata = (OperationMetadata) query.getSingleResult();
 			operationMetadata = OperationServiceImpl.completeOperationMetadata(operationMetadata);
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a operation-metadata by netCDF-name: " + netCDFName
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a operation-metadata by netCDF-name: " + netCDFName, ex);
 		} finally {
 			close(em);
 		}
 
 		return operationMetadata;
 	}
 }
