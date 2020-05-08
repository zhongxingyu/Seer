 package net.canadensys.dataportal.vascan.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.canadensys.dataportal.vascan.ChecklistService;
 import net.canadensys.dataportal.vascan.constant.Rank;
 import net.canadensys.dataportal.vascan.dao.TaxonDAO;
 import net.canadensys.dataportal.vascan.model.TaxonLookupModel;
 
 import org.apache.commons.lang3.BooleanUtils;
 import org.apache.commons.lang3.ObjectUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service("checklistService")
 public class ChecklistServiceImpl implements ChecklistService{
 	
 	private static final String CHECKED = "checked=\"checked\"";
 	private static final String SELECTED = "selected=\"selected\"";
 	
	private static final List<String> CHECKLIST_RELATED_QUERY_TERMS = new ArrayList<String>(8);
 	static{
 		CHECKLIST_RELATED_QUERY_TERMS.add("province");
 		CHECKLIST_RELATED_QUERY_TERMS.add("combination");
 		CHECKLIST_RELATED_QUERY_TERMS.add("habit");
 		CHECKLIST_RELATED_QUERY_TERMS.add("taxon");
 		CHECKLIST_RELATED_QUERY_TERMS.add("status");
 		CHECKLIST_RELATED_QUERY_TERMS.add("rank");
 		CHECKLIST_RELATED_QUERY_TERMS.add("sort");
 		CHECKLIST_RELATED_QUERY_TERMS.add("hybrids");
 		CHECKLIST_RELATED_QUERY_TERMS.add("limitResults");
 	}
 	
 	@Autowired
 	private TaxonDAO taxonDAO;
 	
 	@Transactional(readOnly=true)
 	@Override
 	public Map<String,Object> retrieveChecklistData(Map<String,String[]> parameters){
 		//this will be used to know if you should provide default values
 		boolean noChecklistQuery = !containsChecklistQueryParameter(parameters);
 		
 		Map<String,Object> data = new HashMap<String,Object>();
 		
 		/* request params */
 	    /* provinces */
 	    String[] province = null;
 	    if(parameters.get("province") != null)
 	    	province = parameters.get("province");
 	    
 	    /* combination */
 	    String combination = null;
 	    if(parameters.get("combination") != null){
 	        combination = parameters.get("combination")[0];
 	    }
 	    
 	    /* habitus */
 	    String habit = null;
 	    if(parameters.get("habit") != null)
 	        habit = parameters.get("habit")[0];
 	    
 	    /* taxonid */
 	    int taxon = -1;
 	    if(parameters.get("taxon") != null)
 	        taxon = Integer.valueOf(parameters.get("taxon")[0]);
 	    
 	    /* distribution */
 	    String[] status = null;
 	    if(parameters.get("status") != null)
 	        status = parameters.get("status");
 	    
 	    /* rank */
 	    String[] rank = null;
 	    if(parameters.get("rank") != null)
 	    	rank = parameters.get("rank");
 	    
 	    /* include hybrids */
 	    boolean hybrids;
 	    String shybrids = null;
 	    if(parameters.get("hybrids") != null)
 	    	shybrids = parameters.get("hybrids")[0];
 	    
 	    /* sort */
 	    String sort = null;
 	    if(parameters.get("sort") != null)
 	        sort = parameters.get("sort")[0];
 	    
 	    /* limit number of results */
 	    String nolimit = null;
 	    if(parameters.get("nolimit") != null)
 	        nolimit = parameters.get("nolimit")[0];
 	    String limitResults = null;
 	    if(parameters.get("limitResults") != null)
 	    	limitResults = parameters.get("limitResults")[0];
 	    
 	    /* postback values checks & selects */
 	    // for taxon dropdown list, property selected is added to taxon hashmap
 	    Map<String,String> habitusSelected = new HashMap<String,String>();
 	    Map<String,String> combinationSelected = new HashMap<String,String>();
 	    Map<String,String> sortSelected = new HashMap<String,String>();
 	    Map<String,String> statusChecked = new HashMap<String,String>();
 	    Map<String,String> rankChecked = new HashMap<String,String>();
 	    Map<String,String> limitResultsChecked = new HashMap<String,String>();
 	    Map<String,String> hybridsChecked = new HashMap<String,String>();
 	    Map<String,String> territoryChecked = new HashMap<String,String>();
 	    
 	    if(habit != "" && habit != null){
 	    	habitusSelected.put(habit.toLowerCase(),SELECTED);
 	    }
 	    else{
 	    	habitusSelected.put("all",SELECTED);
 	    	habit = "all";
 	    }
 	    
 	    if(combination != "" && combination != null){
 	    	combinationSelected.put(combination.toLowerCase(),SELECTED);
 	    }
 	    else{
 	    	combinationSelected.put("anyof",SELECTED);
 	        combination = "anyof";
 	    }
 	    
 	    // get statuses from the querystring. if statuses are empty, force 
 	    // native, introduced and ephemeral...
 	    if(status != null){
 	    	for(String s : status){
 	    	    statusChecked.put(s.toLowerCase(),CHECKED);
 	    	}
 	    }
 	    else{
 	    	statusChecked.put("introduced",CHECKED);
 	    	statusChecked.put("native",CHECKED);
 	    	statusChecked.put("ephemeral",CHECKED);
 	        statusChecked.put("excluded",CHECKED);
 	        statusChecked.put("extirpated",CHECKED);
 	        statusChecked.put("doubtful",CHECKED);
 	    	String statuses[] = {"introduced","native","ephemeral","excluded","extirpated","doubtful"};
 	    	status = statuses;
 	    }
 	    
 	    // checked provinces and territories
 	    if(province != null){
 	    	for(String s : province){
 	    	    territoryChecked.put(s.toUpperCase(),CHECKED);	
 	    	}
 	    }    
 	    
 	    // hybrids checkbox
	    // the default value is true but if not check, the form will not send it.
 	    if(BooleanUtils.toBoolean(shybrids) || noChecklistQuery){
 	    	hybrids = true;
 	    	hybridsChecked.put("display",CHECKED);
 	    }
 	    else{
 	    	hybrids = false;
 	    	hybridsChecked.put("display","");
 	    }
 
 	    // sort options
 	    if(sort != "" && sort != null){
 	        sortSelected.put(sort.toLowerCase(),SELECTED);
 	    }
 	    else{
 	    	sort = "taxonomically";
 	    	sortSelected.put(sort,SELECTED);
 	    }
 
 	    String[] ranks = {
 	    	    Rank.CLASS_LABEL,
 	    	    Rank.SUBCLASS_LABEL,
 	    	    Rank.SUPERORDER_LABEL,
 	    	    Rank.ORDER_LABEL,
 	    	    Rank.FAMILY_LABEL,
 	    	    Rank.SUBFAMILY_LABEL,
 	    	    Rank.TRIBE_LABEL,
 	    	    Rank.SUBTRIBE_LABEL,
 	    	    Rank.GENUS_LABEL,
 	    	    Rank.SUBGENUS_LABEL,
 	    	    Rank.SECTION_LABEL,
 	    	    Rank.SUBSECTION_LABEL,
 	    	    Rank.SERIES_LABEL,
 	    	    Rank.SPECIES_LABEL,
 	    	    Rank.SUBSPECIES_LABEL,
 	    	    Rank.VARIETY_LABEL
 	    };
 	    
 	    // init all ranks as checked
 	    for(String r : ranks){
 	    	rankChecked.put(r,CHECKED);	
 	    }
 	    // check main_rank & sub_rank "All" checkbox since all ranks are checked
 	    rankChecked.put("main_rank",CHECKED);
 	    rankChecked.put("sub_rank",CHECKED);
 	    
 	    // if rank is received from querystring, reinit all ranks to unchecked and only check ranks present in querystring
 	    int main_rank = 0;
 	    int sub_rank = 0;
 	    if(rank != null){
 	    	for(String r : ranks){
 	            rankChecked.put(r,""); 
 	        }
 	        rankChecked.put("main_rank","");
 	        rankChecked.put("sub_rank","");
 	    	for(String r : rank){
 	    		rankChecked.put(r.toLowerCase(),CHECKED);
 	    		if(r.toLowerCase().equals(Rank.CLASS_LABEL) ||
 	    		r.toLowerCase().equals(Rank.ORDER_LABEL) ||
 	    		r.toLowerCase().equals(Rank.FAMILY_LABEL) ||
 	    		r.toLowerCase().equals(Rank.GENUS_LABEL) ||
 	    		r.toLowerCase().equals(Rank.SPECIES_LABEL))
 	    			main_rank++;
 	    		else
 	    			sub_rank++;
 	    	}
 	    }
 	    // there must be a better way to do this... maybe only with jquery stuff... 
 	    if(main_rank ==  5)
 	        rankChecked.put("main_rank",CHECKED);
 	    if(sub_rank ==  11)
 	        rankChecked.put("sub_rank",CHECKED);  
 	    
 	    // limit checkbox
 	    if(nolimit == null && limitResults == null){
 	        limitResults = "true";
 	        limitResultsChecked.put("display",CHECKED);
 	    }
 	    else if(nolimit != null && limitResults != null){
 	        limitResults = "true";  
 	        limitResultsChecked.put("display",CHECKED);       
 	    }
 	    else{
 	    	limitResults = "";
 	    	limitResultsChecked.put("display","");
 	    }
 
 	    /* */  
 	    boolean searchOccured = false;
 	    Integer totalResults = 0;
 	    List<Map<String,Object>> taxonDistributions = new ArrayList<Map<String,Object>>();
 	    if(taxon != -1){
 	    	searchOccured = true;
 	        int limitResultsTo = 0;
 	        totalResults = taxonDAO.countTaxonLookup(habit, taxon,combination, province, status, rank, hybrids);
 			
 	        if(limitResults.equals("true")){
 	        	limitResultsTo = 200;
 	        }
 	        
 	        Iterator<TaxonLookupModel> it = taxonDAO.loadTaxonLookup(limitResultsTo, habit, taxon, combination, province, status, rank, hybrids, sort);
 	        if(it !=null){
 	            while(it.hasNext()){
 	                   HashMap<String,Object> distributionData = new HashMap<String,Object>();
 	                   TaxonLookupModel currTlm = it.next();
 	                   distributionData.put("fullScientificName",currTlm.getCalnamehtml());
 	                   distributionData.put("taxonId",currTlm.getTaxonId());
 	                   distributionData.put("rank",currTlm.getRank());
 	                   List<Map<String,Object>> taxonHabitus = new ArrayList<Map<String,Object>>();
 	                   String habituses[] = currTlm.getCalhabit().split(",");
 	                   if(habituses != null){
 	                	   for(String h : habituses){
 	                		   HashMap<String,Object> habitusData = new HashMap<String,Object>();
 	                		   habitusData.put("habit",h);
 	                		   taxonHabitus.add(habitusData);
 	                	   }
 	                   }
 	                   distributionData.put("habit",taxonHabitus);
 	                   distributionData.put("AB",currTlm.getAB());
 	                   distributionData.put("BC",currTlm.getBC());
 	                   distributionData.put("GL",currTlm.getGL());
 	                   distributionData.put("NL_L",currTlm.getNL_L());
 	                   distributionData.put("MB",currTlm.getMB());
 	                   distributionData.put("NB",currTlm.getNB());
 	                   distributionData.put("NL_N",currTlm.getNL_N());
 	                   distributionData.put("NT",currTlm.getNT());
 	                   distributionData.put("NS",currTlm.getNS());
 	                   distributionData.put("NU",currTlm.getNU());
 	                   distributionData.put("ON",currTlm.getON());
 	                   distributionData.put("PE",currTlm.getPE());
 	                   distributionData.put("QC",currTlm.getQC());
 	                   distributionData.put("PM",currTlm.getPM());
 	                   distributionData.put("SK",currTlm.getSK());
 	                   distributionData.put("YT",currTlm.getYT());
 	                   taxonDistributions.add(distributionData);
 	            }
 	        }
 	    }
 	    data.put("distributions",taxonDistributions);
 	    data.put("habit",habitusSelected);
 	    data.put("sort",sortSelected);
 	    data.put("hybrids",hybridsChecked);
 	    data.put("status",statusChecked);
 	    data.put("limitResults",limitResultsChecked);
 	    data.put("rank",rankChecked);
 	    data.put("combination",combinationSelected);
 	    data.put("territory",territoryChecked);
 	    data.put("taxons",getChecklistTaxons(taxon));
 	    data.put("isSearch",searchOccured);
 	    data.put("numResults",ObjectUtils.defaultIfNull(totalResults,0).intValue());
 	    
 	    return data;
 	}
 	
 	public List<Map<String,Object>> getChecklistTaxons(int selectedTaxonId){
 		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
 		List<Object[]> taxons = taxonDAO.getAcceptedTaxon(Rank.GENUS);
 		if(taxons != null){
 			HashMap<String,Object> t;
 			for(Object[] taxon : taxons){
 				int id = (Integer)taxon[0];
 				String calname = (String)taxon[1];
 				String rank = (String)taxon[2];
 				t = new HashMap<String,Object>();
 				
 				if(id == selectedTaxonId){
 					t.put("selected", "selected=\"selected\"");
 				}
 				t.put("id",id);
 				t.put("calname", calname);
 				t.put("rank", rank);
 				results.add(t);
 			}
 			taxons.clear();
 		}
 		return results;
 	}
 	
 	/**
 	 * Check if the received parameters contains at least one parameter related to the checklist builder.
 	 * @param parameters
 	 * @return
 	 */
 	private boolean containsChecklistQueryParameter(Map<String,String[]> parameters){
 		for(String currKey : parameters.keySet()){
 			if(CHECKLIST_RELATED_QUERY_TERMS.contains(currKey)){
 				return true;
 			}
 		}
 		return false;
 	}
 }
