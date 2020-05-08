 /**
  * 
  */
 package org.vme.dao.impl.jpa;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 
 import org.fao.fi.vme.domain.annotations.ReferenceConceptName;
 import org.fao.fi.vme.domain.dto.ref.ReferenceYear;
 import org.fao.fi.vme.domain.model.Authority;
 import org.fao.fi.vme.domain.model.VmeCriteria;
 import org.fao.fi.vme.domain.model.VmeType;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.reference.AcronymAwareReferenceConcept;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.reference.ReferenceConcept;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vme.dao.ReferenceDAO;
 import org.vme.dao.ReferenceServiceException;
 import org.vme.dao.config.vme.VmeDB;
 
 /**
  * @author Fabrizio Sibeni
  * 
  */
 // @ConceptProvider
 public class ReferenceDaoImpl implements ReferenceDAO {
 	final static protected Logger LOG = LoggerFactory.getLogger(ReferenceDaoImpl.class);
 
 	@VmeDB
 	@Inject
 	private EntityManager em;
 
 	private Map<Integer, ReferenceYear> repYear;
 
 	public ReferenceDaoImpl() {
 		this.concepts = new ArrayList<Class<? extends ReferenceConcept>>();
 
 		this.concepts.add(Authority.class);
 		this.concepts.add(VmeCriteria.class);
 		this.concepts.add(VmeType.class);
 		this.concepts.add(ReferenceYear.class);
 		repYear = this.createYears();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vme.service.reference.ReferenceService#getReference(java.lang.Class,
 	 * java.lang.Long)
 	 */
 	@Override
 	public ReferenceConcept getReference(Class<? extends ReferenceConcept> concept, Long id)
 			throws ReferenceServiceException {
 		if (concept.equals(Authority.class)) {
 			return getAuthority(id);
 		} else if (concept.equals(VmeCriteria.class)) {
 			return getVmeCriteria(id);
 		} else if (concept.equals(VmeType.class)) {
 			return getVmeType(id);
 		} else if (concept.equals(ReferenceYear.class)) {
 			return getYear(id);
 		} else {
 			throw new ReferenceServiceException("Undefined reference concept");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vme.service.reference.ReferenceService#getAllReferences(java.lang
 	 * .Class)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public <R extends ReferenceConcept> List<R> getAllReferences(Class<R> concept) throws ReferenceServiceException {
 		if (concept.equals(Authority.class)) {
 			return (List<R>) getAllAuthorities();
 		} else if (concept.equals(VmeCriteria.class)) {
 			return (List<R>) getAllVmeCriterias();
 		} else if (concept.equals(VmeType.class)) {
 			return (List<R>) getAllVmeTypes();
 		} else if (concept.equals(ReferenceYear.class)) {
 			return (List<R>) getAllYears();
 		} else {
 			throw new ReferenceServiceException("Undefined reference concept");
 		}
 
 	}
 
 	public Authority getAuthority(Long key) {
 		List<?> res = em.createQuery("from Authority where id = " + key).getResultList();
 		return res.size() > 0 ? (Authority) res.get(0) : null;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Authority> getAllAuthorities() {
 		return em.createQuery("from Authority").getResultList();
 	}
 
 	public VmeCriteria getVmeCriteria(Long key) {
 		List<?> res = em.createQuery("from VmeCriteria where id = " + key).getResultList();
 		return res.size() > 0 ? (VmeCriteria) res.get(0) : null;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<VmeCriteria> getAllVmeCriterias() {
 		return em.createQuery("from VmeCriteria").getResultList();
 	}
 
 	public VmeType getVmeType(Long key) {
 		List<?> res = em.createQuery("from VmeType where id = " + key).getResultList();
 		return res.size() > 0 ? (VmeType) res.get(0) : null;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<VmeType> getAllVmeTypes() {
 		return em.createQuery("from VmeType").getResultList();
 	}
 
 	public ReferenceYear getYear(Long key) {
 		return repYear.get(key);
 	}
 
 	public List<ReferenceYear> getAllYears() {
 		return new LinkedList<ReferenceYear>(repYear.values());
 	}
 
 	private Map<Integer, ReferenceYear> createYears() {
 		Map<Integer, ReferenceYear> yearsMap = new LinkedHashMap<Integer, ReferenceYear>();
 
 		for (int year = Calendar.getInstance().get(Calendar.YEAR); year >= 2006; year--)
 			yearsMap.put(year, new ReferenceYear(year));
 
 		return yearsMap;
 	}
 
 	protected List<Class<? extends ReferenceConcept>> concepts;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vme.service.reference.ReferenceService#getConcepts()
 	 */
 	@Override
 	final public List<Class<? extends ReferenceConcept>> getConcepts() throws ReferenceServiceException {
 		return this.concepts;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vme.service.reference.ReferenceService#getConcept(java.lang.String)
 	 */
 	@Override
 	final public Class<? extends ReferenceConcept> getConcept(String acronym) throws ReferenceServiceException {
 		for (Class<? extends ReferenceConcept> concept : concepts) {
 			try {
 				String name = concept.getSimpleName();
 
 				if (concept.isAnnotationPresent(ReferenceConceptName.class))
 					name = concept.getAnnotation(ReferenceConceptName.class).value();
 
 				if (acronym.equalsIgnoreCase(name))
 					return concept;
 			} catch (Exception e) {
 				LOG.warn("Internal error", e);
 
 				throw new ReferenceServiceException("Internal error");
 			}
 		}
 
 		throw new ReferenceServiceException("Undefined reference ReferenceConcept");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vme.service.reference.ReferenceService#getReferencebyAcronym(java
 	 * .lang.Class, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	final public AcronymAwareReferenceConcept getReferenceByAcronym(
 			Class<? extends AcronymAwareReferenceConcept> concept, String acronym) throws ReferenceServiceException {
 		try {
 			Collection<AcronymAwareReferenceConcept> allReferenceObjects = (Collection<AcronymAwareReferenceConcept>) getAllReferences(concept);
 
 			for (ReferenceConcept reference : allReferenceObjects) {
 				if (reference instanceof AcronymAwareReferenceConcept) {
 					if (acronym.equals(((AcronymAwareReferenceConcept) reference).getAcronym()))
 						return (AcronymAwareReferenceConcept) reference;
 				}
 			}
 
 			return null;
 		} catch (Throwable t) {
 			throw new ReferenceServiceException(t);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.gcube.application.rsg.support.compiler.bridge.interfaces.reference
 	 * .ReferenceConceptProvider#getReferenceByID(java.lang.Class,
 	 * java.io.Serializable)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
	final public <R extends ReferenceConcept> R getReferenceByID(Class<R> concept, Long id) throws Throwable {
 		return (R) this.getReference(concept, id);
 	}
 }
