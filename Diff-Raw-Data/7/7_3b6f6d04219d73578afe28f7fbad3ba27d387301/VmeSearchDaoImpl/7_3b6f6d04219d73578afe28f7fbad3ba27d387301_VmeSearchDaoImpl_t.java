 package org.vme.dao.impl.jpa;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.StringUtils;
 import org.fao.fi.figis.domain.VmeObservation;
 import org.fao.fi.vme.domain.dto.VmeDto;
 import org.fao.fi.vme.domain.model.Authority;
 import org.fao.fi.vme.domain.model.GeneralMeasure;
 import org.fao.fi.vme.domain.model.GeoRef;
 import org.fao.fi.vme.domain.model.InformationSource;
 import org.fao.fi.vme.domain.model.Profile;
 import org.fao.fi.vme.domain.model.SpecificMeasure;
 import org.fao.fi.vme.domain.model.ValidityPeriod;
 import org.fao.fi.vme.domain.model.Vme;
 import org.fao.fi.vme.domain.model.VmeCriteria;
 import org.fao.fi.vme.domain.model.VmeType;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.ConceptProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vme.dao.ReferenceDAO;
 import org.vme.dao.ReferenceServiceException;
 import org.vme.dao.VmeSearchDao;
 import org.vme.dao.config.vme.VmeDB;
 import org.vme.dao.sources.figis.FigisDao;
 
 /**
  * 
  * @author Fabrizio Sibeni
  * 
  */
 
 public class VmeSearchDaoImpl implements VmeSearchDao {
 	static final private Logger LOG = LoggerFactory.getLogger(VmeSearchDaoImpl.class);
 
 	@Inject
 	@VmeDB
 	private EntityManager entityManager;
 
 	@Inject
 	@ConceptProvider
 	private ReferenceDAO referenceDAO;
 
 	@Inject
 	private FigisDao figisDao;
 
 	private MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 	public VmeSearchDaoImpl() {
 		LOG.info("VME search engine 1.0");
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<VmeDto> searchVme(long authority_id, long type_id, long criteria_id, int year, String text)
 			throws Exception {
 		if (year == 0) {
 			year = Calendar.getInstance().get(Calendar.YEAR);
 		}
 		Query query = entityManager.createQuery(createHibernateSearchTextualQuery(authority_id, type_id, criteria_id,
 				year));
 		List<Vme> result = (List<Vme>) query.getResultList();
 		List<Vme> toRemove = postPurgeResult(year, text, result);
 		List<VmeDto> res = convertPersistenceResult(year, (List<Vme>) result, toRemove);
 		return res;
 	}
 
 	public List<VmeDto> getVmeById(long id, int year) {
 		String text_query;
 		if (year == 0) {
 			year = Calendar.getInstance().get(Calendar.YEAR);
 		}
 		text_query = "from Vme vme where vme.id = " + id;
 		Query query = entityManager.createQuery(text_query);
 		List<?> result = query.getResultList();
 		@SuppressWarnings("unchecked")
 		List<VmeDto> res = convertPersistenceResult(year, (List<Vme>) result, null);
 		return res;
 	}
 
 	public List<VmeDto> getVmeByInventoryIdentifier(String inv_id, int year) {
 		if (year == 0) {
 			year = Calendar.getInstance().get(Calendar.YEAR);
 		}
 		String text_query = "from Vme vme where vme.inventoryIdentifier = '" + inv_id + "'";
 		Query query = entityManager.createQuery(text_query);
 		List<?> result = query.getResultList();
 		@SuppressWarnings("unchecked")
 		List<VmeDto> res = convertPersistenceResult(year, (List<Vme>) result, null);
 		return res;
 	}
 
 	public List<VmeDto> getVmeByGeographicFeatureId(String geo_id, int year) {
 		if (year == 0) {
 			year = Calendar.getInstance().get(Calendar.YEAR);
 		}
 		// String text_query =
 		// "SELECT vme from Vme vme, GEO_REF gfl WHERE vme = gfl.vme and gfl IN (SELECT gfl from GEO_REF WHERE gfl.geographicFeatureID = '"
 		// + geo_id + "')";
 		String text_query = "select OBJECT(v) from Vme v join v.geoRefList g where g.geographicFeatureID =  '" + geo_id
 				+ "')";
 
 		@SuppressWarnings("unchecked")
 		List<Vme> result = entityManager.createQuery(text_query).getResultList();
 
 		List<VmeDto> res = convertPersistenceResult(year, result, null);
 		return res;
 	}
 
 	private String createHibernateSearchTextualQuery(long authority_id, long type_id, long criteria_id, int year)
 			throws Exception {
 		StringBuffer txtQuery = new StringBuffer(200);
 		String conjunction = " where";
 
 		txtQuery.append("Select vme from Vme vme");
 		// if (authority_id > 0 || type_id > 0 || criteria_id > 0) {
 		// txtQuery.append(" where");
 		// conjunction = "";
 		// }
 
 		if (authority_id > 0) {
 			Authority vmeAuthority = (Authority) entityManager.find(Authority.class, authority_id);
 			if (vmeAuthority != null) {
 				String authority = vmeAuthority.getAcronym();
 				txtQuery.append(conjunction);
 				txtQuery.append(" vme.rfmo.id = '");
 				txtQuery.append(authority);
 				txtQuery.append("'");
 				conjunction = " AND";
 			}
 		}
 
 		if (criteria_id > 0) {
 			VmeCriteria vmeCriteria = (VmeCriteria) entityManager.find(VmeCriteria.class,  criteria_id);
 			if (vmeCriteria != null) {
 				String criteria = vmeCriteria.getName();
 				txtQuery.append(conjunction);
 				txtQuery.append(" vme.criteria = '");
 				txtQuery.append(criteria);
 				txtQuery.append("'");
 				conjunction = " AND";
 			}
 		}
 
 		if (type_id > 0) {
 			VmeType vmeType = (VmeType) entityManager.find(VmeType.class,  type_id);
 			if (vmeType != null) {
 				String areaType = vmeType.getName();
 				txtQuery.append(conjunction);
 				txtQuery.append("  vme.areaType = '");
 				txtQuery.append(areaType);
 				txtQuery.append("'");
 				conjunction = " AND";
 			}
 		}
 
 		txtQuery.append(conjunction);
 		txtQuery.append(" vme.validityPeriod.beginYear <= ");
 		txtQuery.append(year);
 		// txtQuery.append(" AND vme.validityPeriod.endYear >= ");
 		// txtQuery.append(year);
 
 		String res = txtQuery.toString();
 		LOG.debug("FAB: {}", res);
 		return res;
 	}
 
 	private List<Vme> postPurgeResult(int year, String text, List<Vme> result) {
 		List<Vme> res = new LinkedList<Vme>();
 		// Patch placed to solve VME-10 JIRA issue.
 		for (Vme vme : result) {
 			// this adds all the SIODFA vmes to the list to be removed.
 			if (vme.getRfmo().getId().trim().equals("SIODFA")) {
 				res.add(vme);
 			}
 		}
 		if (year > 0) {
 			for (Vme vme : result) {
 
 				if (vme.getRfmo().getId().equals("SEAFO")) {
 					System.out.println(vme.getRfmo().getId());
 				}
 				if (vme.getId().equals(1109)) {
 					System.out.println(vme.getRfmo().getId());
 				}
 				System.out.println(vme.getRfmo().getId());
 
 				boolean toBeRemoved = false;
 				List<GeoRef> georef = vme.getGeoRefList();
 
 				// Intervention Erik van Ingen 30 Jan 2014. If the year
 				// of the profile matches the requested year, it will be
 				// removed? Probably it is the other way around:
 				boolean matchingGeoRefyear = false;
 				for (GeoRef profile : georef) {
 					if (year >= profile.getYear()) {
 						matchingGeoRefyear = true;
 					}
 				}
 				if (!matchingGeoRefyear) {
 					toBeRemoved = true;
 				}
 
 				if (!toBeRemoved) {
 					ValidityPeriod validityPeriod = vme.getValidityPeriod();
 					if (year < validityPeriod.getBeginYear()) {
 						toBeRemoved = true;
 					}
 				}
 
 				if (!toBeRemoved && text != null && !text.equals("")) {
 					toBeRemoved = !containRelevantText(vme, text);
 				}
 
 				// System.out.println(vme.getRfmo().getId());
 
 				if (toBeRemoved) {
 					res.add(vme);
 				}
 			}
 		}
 		return res;
 	}
 
 	private boolean containRelevantText(Vme vme, String text) {
 
 		if (StringUtils.containsIgnoreCase(vme.getAreaType(), text))
 			return true;
 		if (StringUtils.containsIgnoreCase(vme.getCriteria(), text))
 			return true;
 		for (String element : vme.getGeoArea().getStringMap().values()) {
 			if (StringUtils.containsIgnoreCase(element, text))
 				return true;
 		}
 //		if (StringUtils.containsIgnoreCase(vme.getGeoform(), text))
 //			return true;
 		for (GeoRef geoRef : vme.getGeoRefList()) {
 			if (StringUtils.containsIgnoreCase(geoRef.getGeographicFeatureID(), text))
 				return true;
 		}
 		if (StringUtils.containsIgnoreCase(vme.getInventoryIdentifier(), text))
 			return true;
 		for (String element : vme.getName().getStringMap().values()) {
 			if (StringUtils.containsIgnoreCase(element, text))
 				return true;
 		}
 
 		for (Profile profile : vme.getProfileList()) {
 			if (profile.getDescriptionBiological() != null) {
 				for (String element : profile.getDescriptionBiological().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 			if (profile.getDescriptionImpact() != null) {
 				for (String element : profile.getDescriptionImpact().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 			if (profile.getDescriptionPhisical() != null) {
 				for (String element : profile.getDescriptionPhisical().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 		}
 
 		for (GeneralMeasure generalMeasure : vme.getRfmo().getGeneralMeasureList()) {
 			if (generalMeasure.getFishingArea() != null) {
 				for (String element : generalMeasure.getFishingArea().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 
 			if (generalMeasure.getExplorataryFishingProtocol() != null) {
 				for (String element : generalMeasure.getExplorataryFishingProtocol().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 			if (generalMeasure.getVmeEncounterProtocol() != null) {
 				for (String element : generalMeasure.getVmeEncounterProtocol().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 			if (generalMeasure.getVmeIndicatorSpecies() != null) {
 				for (String element : generalMeasure.getVmeIndicatorSpecies().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 
 			if (generalMeasure.getVmeThreshold() != null) {
 				for (String element : generalMeasure.getVmeThreshold().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 
 			if (generalMeasure.getInformationSourceList() != null) {
 				for (InformationSource informationSource : generalMeasure.getInformationSourceList()) {
 
 					if (informationSource.getCitation() != null) {
 						for (String element : informationSource.getCitation().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text))
 								return true;
 						}
 					}
 					if (informationSource.getCommittee() != null) {
 						for (String element : informationSource.getCommittee().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text))
 								return true;
 						}
 					}
 
 					if (informationSource.getReportSummary() != null) {
 						for (String element : informationSource.getReportSummary().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text))
 								return true;
 						}
 					}
 					if (StringUtils.containsIgnoreCase(Integer.toString(informationSource.getPublicationYear()), text))
 						return true;
 					if (StringUtils.containsIgnoreCase(informationSource.getUrl() != null ? informationSource.getUrl()
 							.toExternalForm() : "", text))
 						return true;
 				}
 			}
 
 		}
 
 		for (SpecificMeasure specificMeasure : vme.getSpecificMeasureList()) {
 			if (specificMeasure.getVmeSpecificMeasure() != null) {
 				for (String element : specificMeasure.getVmeSpecificMeasure().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 			if (specificMeasure.getInformationSource() != null) {
 				if (specificMeasure.getInformationSource().getCitation() != null) {
 					for (String element : specificMeasure.getInformationSource().getCitation().getStringMap().values()) {
 						if (StringUtils.containsIgnoreCase(element, text))
 							return true;
 					}
 				}
 				if (specificMeasure.getInformationSource().getPublicationYear() != null
 						&& StringUtils.containsIgnoreCase(
 								Integer.toString(specificMeasure.getInformationSource().getPublicationYear()), text)) {
 					return true;
 				}
 				if (StringUtils.containsIgnoreCase(
 						specificMeasure.getInformationSource().getUrl() != null ? specificMeasure
 								.getInformationSource().getUrl().toExternalForm() : "", text))
 					return true;
 			}
 			if (specificMeasure.getInformationSource() != null
 					&& specificMeasure.getInformationSource().getCommittee() != null) {
 				for (String element : specificMeasure.getInformationSource().getCommittee().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 
 			if (specificMeasure.getInformationSource() != null
 					&& specificMeasure.getInformationSource().getReportSummary() != null) {
 				for (String element : specificMeasure.getInformationSource().getReportSummary().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text))
 						return true;
 				}
 			}
 
 		}
 		return false;
 	}
 
 	private List<VmeDto> convertPersistenceResult(int year, List<Vme> result, List<Vme> toRemove) {
 		List<VmeDto> res = new LinkedList<VmeDto>();
 		for (Vme vme : result) {
 			VmeDto dto = getVmeSearchDto(vme, year);
 			if (toRemove == null || toRemove != null && !toRemove.contains(vme)) {
 				if (!res.contains(dto)) {
 					res.add(dto);
 				}
 			}
 		}
 		return res;
 	}
 
 	private VmeDto getVmeSearchDto(Vme vme, int year) {
 		VmeDto res = new VmeDto();
 		res.setVmeId(vme.getId());
 		res.setInventoryIdentifier(vme.getInventoryIdentifier());
 		res.setLocalName(u.getEnglish(vme.getName()));
 		res.setEnvelope("");
 		String authority_acronym = vme.getRfmo().getId();
 		try {
 			Authority authority = (Authority) referenceDAO.getReferenceByAcronym(Authority.class, authority_acronym);
 
 			if (authority == null) {
				System.out.println("NULL auth");
			} else
				res.setOwner(authority.getName());
 		} catch (ReferenceServiceException e) {
 			res.setOwner(authority_acronym);
 			e.printStackTrace();
 		}
 
 		VmeObservation vo = figisDao.findFirstVmeObservation(vme.getId(), Integer.toString(year));
 		if (vo != null) {
 			res.setFactsheetUrl("fishery/vme/" + vo.getId().getVmeId() + "/" + vo.getId().getObservationId() + "/en");
 		} else {
 			res.setFactsheetUrl("");
 		}
 
 		res.setGeoArea(u.getEnglish(vme.getGeoArea()));
 		res.setValidityPeriodFrom(vme.getValidityPeriod().getBeginYear());
 		res.setValidityPeriodTo(vme.getValidityPeriod().getEndYear());
 		res.setVmeType(vme.getAreaType());
 		res.setYear(vme.getGeoRefList().get(0).getYear());
 		res.setGeographicFeatureId(vme.getGeoRefList().size() > 0 ? vme.getGeoRefList().get(0).getGeographicFeatureID()
 				: "");
 		return res;
 	}
 
 	/**
 	 * @param dao
 	 *            the dao to set
 	 */
 
 	/*
 	 * public void setDao(FigisDao dao) { this.dao = dao; }
 	 */
 
 }
