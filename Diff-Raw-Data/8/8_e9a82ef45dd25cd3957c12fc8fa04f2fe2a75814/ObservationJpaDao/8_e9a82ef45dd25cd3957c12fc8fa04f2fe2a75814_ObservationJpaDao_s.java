 package org.vme.service.dao.impl.jpa;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.StringUtils;
 import org.fao.fi.figis.domain.VmeObservation;
 import org.fao.fi.vme.domain.dto.observations.ObservationDto;
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
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.runner.RunWith;
 import org.vme.service.dao.ObservationDAO;
 import org.vme.service.dao.ReferenceServiceException;
 import org.vme.service.dao.config.vme.VmeDB;
 import org.vme.service.dao.sources.figis.FigisDao;
 
 
 @RunWith(CdiRunner.class)
 public class ObservationJpaDao implements ObservationDAO {
 
 
 	@VmeDB
 	@Inject
 	private EntityManager entityManager;
 	
 	@Inject
 	private ReferenceJpaDao referenceDAO;
 	
 	@Inject
 	private FigisDao figisDao;
 
 	
 	private MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 
 
 	public ObservationJpaDao() {
 		System.out.println("VME search engine 1.0");
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public List<ObservationDto> searchObservations(long authority_id, long type_id, long criteria_id, int year, String text) throws Exception  {
 		if (year==0){
 			 year = Calendar.getInstance().get(Calendar.YEAR);
 		}
 		Query query = entityManager.createQuery(createHibernateSearchTextualQuery(authority_id, type_id, criteria_id, year));
 		List<Vme> result =  (List<Vme>)query.getResultList();
 		List<Vme> toRemove =  postPurgeResult(year, text, result);
 		List<ObservationDto> res = convertPersistenceResult(year, (List<Vme>) result, toRemove);
 		return res;
 	}
 
 
 
 	public List<ObservationDto> getObservationById(long id, int year)  {
 		String text_query;
 		if (year==0){
 			 year = Calendar.getInstance().get(Calendar.YEAR);
 		}
 		text_query = "from Vme vme where vme.id = " + id;
 		Query query = entityManager.createQuery(text_query);
 		List<?> result =   query.getResultList();
 		@SuppressWarnings("unchecked")
 		List<ObservationDto> res = convertPersistenceResult(year, (List<Vme>) result, null);
 		return res;
 	}	
 	
 	public List<ObservationDto> getObservationByInevntoryIdentifier(String inv_id, int year)  {
 		if (year==0){
 			 year = Calendar.getInstance().get(Calendar.YEAR);
 		}		
 		String	text_query = "from Vme vme where vme.inventoryIdentifier = '" + inv_id + "'";
 		Query query = entityManager.createQuery(text_query);
 		List<?> result =   query.getResultList();
 		@SuppressWarnings("unchecked")
 		List<ObservationDto> res = convertPersistenceResult(year, (List<Vme>) result, null);
 		return res;
 	}
 	
 	public List<ObservationDto> getObservationByGeographicFeatureId(String geo_id, int year)  {
 		if (year==0){
 			 year = Calendar.getInstance().get(Calendar.YEAR);
 		}		
 		String text_query = "SELECT vme from Vme vme, GEO_REF gfl WHERE vme = gfl.vme and gfl IN (SELECT gfl from GEO_REF WHERE gfl.geographicFeatureID = '" + geo_id + "')";
 		Query query = entityManager.createQuery(text_query);
 		List<?> result =   query.getResultList();
 		@SuppressWarnings("unchecked")
 		List<ObservationDto> res = convertPersistenceResult(year, (List<Vme>) result, null);
 		return res;
 	}
 
 
 
 	private String createHibernateSearchTextualQuery(long authority_id, long type_id, long criteria_id, int year) throws Exception {
 		StringBuffer txtQuery = new StringBuffer(200);
 		String conjunction;
 		txtQuery.append("Select vme from Vme vme");
 		if (authority_id>0 || type_id>0 || criteria_id>0){
 			txtQuery.append(" where");
 			conjunction = "";
 		} else {
 			return txtQuery.toString();
 		}
 
 		if (authority_id>0){
			Authority vmeAuthority = (Authority) entityManager.getReference(Authority.class, authority_id);
 			String authority = vmeAuthority.getAcronym();
 			txtQuery.append(conjunction);
 			txtQuery.append(" vme.rfmo.id = '");
 			txtQuery.append(authority);
 			txtQuery.append("'");
 			conjunction = " AND";
 		}
 
 		if (criteria_id>0){
			VmeCriteria vmeCriteria = (VmeCriteria) entityManager.getReference(VmeCriteria.class, criteria_id);
 			String criteria = vmeCriteria.getName();
 			txtQuery.append(conjunction);
 			txtQuery.append(" vme.criteria = '");
 			txtQuery.append(criteria);
 			txtQuery.append("'");
 			conjunction = " AND";
 		}
 
 		if (type_id>0){
			VmeType vmeType = (VmeType) entityManager.getReference(VmeType.class, type_id);
 			String areaType = vmeType.getName();
 			txtQuery.append(conjunction);
 			txtQuery.append("  vme.areaType = '");
 			txtQuery.append(areaType);
 			txtQuery.append("'");
 			conjunction = " AND";
 		}
 
 		txtQuery.append(" AND vme.validityPeriod.beginYear <= ");
 		txtQuery.append(year);
 		txtQuery.append(" AND vme.validityPeriod.endYear >= ");
 		txtQuery.append(year);
 		
 		String res = txtQuery.toString();
 		System.out.println("FAB:" + res);
 		return res;
 	}
 
 
 
 
 
 
 	private List<Vme> postPurgeResult (int year, String text,  List<Vme> result){
 		int requested_year = year;
 		List<Vme> res = new LinkedList<Vme>();
 		// Patch placed to solve VME-10 JIRA issue.
 		for (Vme vme : result) {
 			if (vme.getRfmo().getId().trim().equals("SIODFA")){
 				res.add(vme);
 			}
 		}
 		if (requested_year>0) {
 			for (Vme vme : result) {
 				boolean is_good = false;
 				List<GeoRef> georef = vme.getGeoRefList();
 				for (GeoRef profile : georef) {
 					if (profile.getYear()==requested_year) {
 						is_good = true;
 						break;
 					}
 				}
 				if (!is_good){
 					ValidityPeriod validityPeriod =  vme.getValidityPeriod();
 					if (validityPeriod.getBeginYear()<= requested_year && validityPeriod.getEndYear()>= requested_year){
 						is_good = true;
 					}
 				}
 				
 				if (is_good && (text!=null && !text.equals(""))){
 					is_good = containRelevantText(vme, text);
 				}
 				
 				if (!is_good){
 					res.add(vme);
 				}
 			}
 		}
 		return res;
 	}
 
 
 
 
 
 
 	private boolean containRelevantText(Vme vme, String text) {
 		if (StringUtils.containsIgnoreCase(vme.getAreaType(), text)) return true;
 		if (StringUtils.containsIgnoreCase(vme.getCriteria(), text)) return true;
 		for (String element : vme.getGeoArea().getStringMap().values()) {
 			if (StringUtils.containsIgnoreCase(element, text)) return true;
 		} 
 		if (StringUtils.containsIgnoreCase(vme.getGeoform(), text)) return true;
 		for (GeoRef geoRef : vme.getGeoRefList()) {
 			if (StringUtils.containsIgnoreCase(geoRef.getGeographicFeatureID(), text)) return true;
 		}
 		if (StringUtils.containsIgnoreCase(vme.getInventoryIdentifier(), text)) return true;
 		for (String element : vme.getName().getStringMap().values()) {
 			if (StringUtils.containsIgnoreCase(element, text)) return true;
 		} 
 		for (Profile profile : vme.getProfileList()) {
 			for (String element : profile.getDescriptionBiological().getStringMap().values()) {
 				if (StringUtils.containsIgnoreCase(element, text)) return true;
 			} 
 			for (String element : profile.getDescriptionImpact().getStringMap().values()) {
 				if (StringUtils.containsIgnoreCase(element, text)) return true;
 			} 
 			for (String element : profile.getDescriptionPhisical().getStringMap().values()) {
 				if (StringUtils.containsIgnoreCase(element, text)) return true;
 			} 
 		}
 		
 		for (GeneralMeasure generalMeasure : vme.getRfmo().getGeneralMeasureList()) {
 			if (StringUtils.containsIgnoreCase(generalMeasure.getFishingAreas(), text)) return true;
 			if (generalMeasure.getExplorataryFishingProtocols()!=null){
 				for (String element : generalMeasure.getExplorataryFishingProtocols().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			if (generalMeasure.getVmeEncounterProtocols()!=null){
 				for (String element : generalMeasure.getVmeEncounterProtocols().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			if (generalMeasure.getVmeIndicatorSpecies()!=null){
 				for (String element : generalMeasure.getVmeIndicatorSpecies().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 
 			if (generalMeasure.getVmeThreshold()!=null){
 				for (String element : generalMeasure.getVmeThreshold().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			
 			if (generalMeasure.getInformationSourceList()!=null){
 				for (InformationSource informationSource : generalMeasure.getInformationSourceList()) {
 					
 					if (informationSource.getCitation()!=null){
 						for (String element : informationSource.getCitation().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text)) return true;
 						} 
 					}
 					if (informationSource.getCommittee()!=null){
 						for (String element : informationSource.getCommittee().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text)) return true;
 						} 
 					}
 
 					if (informationSource.getReportSummary()!=null){
 						for (String element : informationSource.getReportSummary().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text)) return true;
 						} 
 					}
 					if (StringUtils.containsIgnoreCase(	Integer.toString(informationSource.getPublicationYear()), text)) return true;
 					if (StringUtils.containsIgnoreCase(informationSource.getUrl()!=null?informationSource.getUrl().toExternalForm():"", text)) return true;
 				}
 			}
 			
 
 		}
 		
 		
 		for (SpecificMeasure specificMeasure : vme.getSpecificMeasureList()) {
 			if (specificMeasure.getVmeSpecificMeasure()!=null){
 				for (String element : specificMeasure.getVmeSpecificMeasure().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			if (specificMeasure.getInformationSource()!=null){
 				if(specificMeasure.getInformationSource().getCitation()!=null){
 					for (String element : specificMeasure.getInformationSource().getCitation().getStringMap().values()) {
 						if (StringUtils.containsIgnoreCase(element, text)) return true;
 					} 
 				}
 				if (StringUtils.containsIgnoreCase(	Integer.toString(specificMeasure.getInformationSource().getPublicationYear()), text)) return true;
 				if (StringUtils.containsIgnoreCase(specificMeasure.getInformationSource().getUrl()!=null?specificMeasure.getInformationSource().getUrl().toExternalForm():"", text)) return true;
 			}
 			if (specificMeasure.getInformationSource()!=null && specificMeasure.getInformationSource().getCommittee()!=null){
 				for (String element : specificMeasure.getInformationSource().getCommittee().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 
 			if (specificMeasure.getInformationSource()!=null && specificMeasure.getInformationSource().getReportSummary()!=null){
 				for (String element : specificMeasure.getInformationSource().getReportSummary().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 
 		}
 		return false;
 	}
 
 
 	private List<ObservationDto> convertPersistenceResult(int year,  List<Vme> result, List<Vme> toRemove){
 		List<ObservationDto> res = new LinkedList<ObservationDto>();
 		for (Vme vme : result) {
 			if (toRemove==null || (toRemove!=null  && !toRemove.contains(vme))){
 				res.add(getVmeSearchDto(vme,year));
 			}
 		}
 		return res;
 	}
 
 
 
 
 	private ObservationDto getVmeSearchDto(Vme vme, int year) {
 		ObservationDto res = new ObservationDto();
 		res.setVmeId(vme.getId());
 		res.setInventoryIdentifier(vme.getInventoryIdentifier());
 		res.setLocalName(u.getEnglish(vme.getName()));
 		res.setEnvelope("");
 		String authority_acronym = vme.getRfmo().getId();
 		try {
 			Authority authority = (Authority)referenceDAO.getReferenceByAcronym(Authority.class, authority_acronym);
 			res.setOwner(authority.getName() + " (" + authority.getAcronym() + ")");
 		} catch (ReferenceServiceException e) {
 			res.setOwner(authority_acronym);
 			e.printStackTrace();
 		}
 		
 		VmeObservation vo = figisDao.findFirstVmeObservation(vme.getId(), Integer.toString(year));
 		if (vo!=null){
 			res.setFactsheetUrl("fishery/vme/"+ vo.getId().getVmeId() + "/" + vo.getId().getObservationId() +"/en");
 		} else {
 			res.setFactsheetUrl("");
 		}
 
 		res.setGeoArea(u.getEnglish(vme.getGeoArea()));
 		res.setValidityPeriodFrom(vme.getValidityPeriod().getBeginYear());
 		res.setValidityPeriodTo(vme.getValidityPeriod().getEndYear());
 		res.setVmeType(vme.getAreaType());
 		res.setYear(year);
 		res.setGeographicFeatureId(vme.getGeoRefList().size()>0?vme.getGeoRefList().get(0).getGeographicFeatureID():"");
 		return res;
 	}
 
 
 	/**
 	 * @param dao the dao to set
 	 */
 	
 /*
 	public void setDao(FigisDao dao) {
 		this.dao = dao;
 	}
 */
 
 
 
 
 }
