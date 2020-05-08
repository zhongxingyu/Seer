 package net.canadensys.dataportal.vascan.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.canadensys.dataportal.vascan.APIService;
 import net.canadensys.dataportal.vascan.constant.Status;
 import net.canadensys.dataportal.vascan.dao.NameDAO;
 import net.canadensys.dataportal.vascan.dao.TaxonDAO;
 import net.canadensys.dataportal.vascan.model.DistributionModel;
 import net.canadensys.dataportal.vascan.model.NameConceptModelIF;
 import net.canadensys.dataportal.vascan.model.TaxonLookupModel;
 import net.canadensys.dataportal.vascan.model.TaxonModel;
 import net.canadensys.dataportal.vascan.model.VernacularNameModel;
 import net.canadensys.dataportal.vascan.model.api.DistributionAPIResult;
 import net.canadensys.dataportal.vascan.model.api.TaxonAPIResult;
 import net.canadensys.dataportal.vascan.model.api.VascanAPIResponse;
 import net.canadensys.dataportal.vascan.model.api.VernacularNameAPIResult;
 import net.canadensys.query.LimitedResult;
 
 import org.gbif.api.model.checklistbank.ParsedName;
 import org.gbif.api.vocabulary.NameType;
 import org.gbif.nameparser.NameParser;
 import org.gbif.nameparser.UnparsableException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class APIServiceImpl implements APIService{
 	private static NameParser GBIF_NAME_PARSER = new NameParser();
 	
 	public static String API_VERSION = "0.1";
 
 	@Autowired
 	private NameDAO nameDAO;
 	
 	@Autowired
 	private TaxonDAO taxonDAO;
 	
 	@Override
 	public String getAPIVersion() {
 		return API_VERSION;
 	}
 	
 	@Override
 	@Transactional(readOnly=true)
 	public List<VascanAPIResponse> search(List<String> idList, List<String> dataList) {
 		List<VascanAPIResponse> results = new ArrayList<VascanAPIResponse>();
 		int idx=0;
 		VascanAPIResponse currAPIResponse;
 		for(String currId : idList){
 			currAPIResponse = search(dataList.get(idx));
 			currAPIResponse.setLocalIdentifier(currId);
 			results.add(currAPIResponse);
 			idx++;
 		}
 		return results;
 	}
 	
 	@Override
 	@Transactional(readOnly=true)
 	public VascanAPIResponse search(String id, String searchTerm) {
 		VascanAPIResponse apiResponse = search(searchTerm);
 		apiResponse.setLocalIdentifier(id);
 		return apiResponse;
 	}
 	
 	@Transactional(readOnly=true)
 	@Override
 	public VascanAPIResponse search(Integer id) {
 		TaxonModel taxonModel = taxonDAO.loadTaxon(id);
 		
 		VascanAPIResponse apiResponse = new VascanAPIResponse();
 		apiResponse.setApiVersion(API_VERSION);
 		apiResponse.setSearchedId(id);
 		
 		if(taxonModel != null){
 			apiResponse.setNumResults(1);
 			fillVascanAPIResponse(apiResponse,Arrays.asList(id));
 		}
 		else{
 			apiResponse.setNumResults(0);
 		}
 		return apiResponse;
 	}
 	
 	/**
 	 * Main search function.
 	 * Search for a single term.
 	 * @param searchTerm
 	 * @return VascanAPIResponse object, never null
 	 */
 	private VascanAPIResponse search(String searchTerm) {
 		String pSearchTerm = parseScientificName(searchTerm);
 		LimitedResult<List<NameConceptModelIF>> searchResult = nameDAO.search(pSearchTerm,false);
 		
 		VascanAPIResponse apiResponse = new VascanAPIResponse();
 		apiResponse.setApiVersion(API_VERSION);
 		apiResponse.setNumResults(Long.valueOf(searchResult.getTotal_rows()).intValue());
 		apiResponse.setSearchTerm(searchTerm);
 		
 		//build a set of unique taxon id
 		List<Integer> taxonIdList = new ArrayList<Integer>();
 		for(NameConceptModelIF currName : searchResult.getRows()){
 			//avoid duplicated entry (where query matches in taxon name and vernacular name of the same taxon)
 			if(!taxonIdList.contains(currName.getTaxonId())){
 				taxonIdList.add(currName.getTaxonId());
 			}
 		}
 
 		if(!taxonIdList.isEmpty()){
 			fillVascanAPIResponse(apiResponse,taxonIdList);
 		}
 		return apiResponse;
 	}
 	
 	/**
 	 * Fill the VascanAPIResponse from a list of taxonID.
 	 * @param taxonIdList all taxonID related to a single VascanAPIResponse
 	 */
 	private void fillVascanAPIResponse(VascanAPIResponse apiResponse, List<Integer> taxonIdList){
 		List<TaxonModel> taxonModelList = taxonDAO.loadTaxonList(taxonIdList);
 		List<TaxonModel> parentsList;
 		TaxonAPIResult tar;
 		for(TaxonModel currTaxonModel : taxonModelList){
 			parentsList = currTaxonModel.getParents();
 			if(currTaxonModel.getStatus().getId() == Status.SYNONYM){
 				//synonyms can have more than one parent so we loop over parents
 				//end add a new entry for each of the parents
 				for(TaxonModel currParent : parentsList){
 					tar = createTaxonAPIResult(currTaxonModel);
 					tar.setAcceptedNameUsageID(currParent.getId());
 					tar.setAcceptedNameUsage(currParent.getLookup().getCalnameauthor());
 					apiResponse.addResult(tar);
 				}
 			}
 			else{ //accepted taxon
 				tar = createTaxonAPIResult(currTaxonModel);
 				//this will be false for the root only
 				if(parentsList.size() == 1){
 					tar.setParentNameUsageID(parentsList.get(0).getId());
 				}
 				tar.setHigherClassification(currTaxonModel.getLookup().getHigherclassification());
 				tar.setAcceptedNameUsageID(currTaxonModel.getId());
 				tar.setAcceptedNameUsage(currTaxonModel.getLookup().getCalnameauthor());
 				apiResponse.addResult(tar);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a TaxonAPIResult and fill data that is common to accepted and synonym taxon.
 	 * @param currTaxonModel
 	 * @return newly created TaxonAPIResult
 	 */
 	private TaxonAPIResult createTaxonAPIResult(TaxonModel taxonModel){
 		TaxonLookupModel tlm = taxonModel.getLookup();
 		
 		TaxonAPIResult tar = new TaxonAPIResult();
 		tar.setTaxonID(taxonModel.getId());
 		tar.setTaxonomicStatus(tlm.getStatus());
 		tar.setNameAccordingTo(taxonModel.getReference().getReference());
 		
 		tar.setCanonicalName(tlm.getCalname());
 		tar.setScientificName(tlm.getCalnameauthor());
 		tar.setScientificNameAuthorship(tlm.getAuthor());
 		tar.setTaxonRank(tlm.getRank());
 
 		for(VernacularNameModel currVernacular : taxonModel.getVernacularnames()){
 			VernacularNameAPIResult vnar = new VernacularNameAPIResult();
 			vnar.setVernacularName(currVernacular.getName());
 			vnar.setLanguage(currVernacular.getLanguage());
 			vnar.setSource(currVernacular.getReference().getReference());
 			tar.addVernacularName(vnar);
 		}
 		
 		for(DistributionModel currDistribution : taxonModel.getDistribution()){
 			DistributionAPIResult dar = new DistributionAPIResult();
 			dar.setLocality(currDistribution.getRegion().getRegion());
 			dar.setLocationID(currDistribution.getRegion().getIso3166_2());
 			dar.setOccurrenceStatus(currDistribution.getDistributionStatus().getDistributionstatus());
 			dar.setEstablishmentMeans(currDistribution.getDistributionStatus().getEstablishmentmeans());
 			tar.addDistribution(dar);
 		}
 		return tar;
 	}
 	
 	/**
 	 * Try to parse the provided name with GBIF name parser. We want to remove the authorship and date if provided.
 	 * @param rawScientificName
 	 * @return parsed scientific name or the name provided if we can't parse it.
 	 */
 	private static String parseScientificName(String rawScientificName){
 		ParsedName parsedName = null;
 		try{
 			parsedName = GBIF_NAME_PARSER.parse(rawScientificName);
 			if (NameType.WELLFORMED.equals(parsedName.getType())
 					|| NameType.SCINAME.equals(parsedName.getType())) {
 				return parsedName.canonicalNameWithMarker();
 			}
 		}
 		catch(UnparsableException uEx){} //ignore
 		return rawScientificName;
 	}
 }
