 package net.canadensys.dataportal.vascan.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.canadensys.dataportal.vascan.NameService;
 import net.canadensys.dataportal.vascan.constant.Rank;
 import net.canadensys.dataportal.vascan.constant.Status;
 import net.canadensys.dataportal.vascan.dao.TaxonDAO;
 import net.canadensys.dataportal.vascan.dao.TaxonomyDAO;
 import net.canadensys.dataportal.vascan.dao.VernacularNameDAO;
 import net.canadensys.dataportal.vascan.manager.TaxonManager;
 import net.canadensys.dataportal.vascan.model.HabitModel;
 import net.canadensys.dataportal.vascan.model.TaxonLookupModel;
 import net.canadensys.dataportal.vascan.model.TaxonModel;
 import net.canadensys.dataportal.vascan.model.VernacularNameModel;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service("nameService")
 public class NameServiceImpl implements NameService{
 	
 	@Autowired
 	private VernacularNameDAO vernacularNameDAO;
 	
 	@Autowired
 	private TaxonDAO taxonDAO;
 	
 	@Autowired
 	private TaxonomyDAO taxonomyDAO;
 	
 	private TaxonManager taxonManager= new TaxonManager();
 	
 	@Autowired
 	private PropertyMapHelper propertyMapHelper;
 	
 	@Transactional(readOnly=true)
 	@Override
 	public Map<String,Object> retrieveNameData(String name, String redirect, Map<String,Object> extra){
 	    /* 
 	     * Name hub ;
 	     * This page will display all available information for a taxon OR for a
 	     * vernacular name;
 	     * in the case where :
 	    	 we have many match for a vernacular name,
 	    	 we have multiple taxon for a given name (accepted, synonym, source)
 	     * we need to display the disambiguation page
 	     */
 	    	    
 	    String pageTitle = name;
 	    String nameH1 = name;
 
 	    // is the name param for a vernacular name
 	    boolean isVernacularName = false;
 	    // should we display a warning saying this is a vernacular name of something
 	    boolean isVernacularNameWarning = false;
 	    String vernacularNameWarning = "";
 	    int vernacularNameWarningId = 0;
 	    String vernacularNameWarningStatus = "";
 	    String vernacularNameWarningLanguage = "";
 	    String vernacularNameWarningReference = "";
 	    String vernacularNameWarningReferenceShort = "";
 	    String vernacularNameWarningLink = "";
 	    
 	    // should we stop the redirect
 	    boolean isRedirect = true;
 	    if(ObjectUtils.equals(redirect,"no")){
 	        isRedirect = false;
 	    }
 	    
 	    // is the name param for a taxon
 	    boolean isTaxon = false;
 	    // should we display a warning saying this is a synonym of something
 	    boolean isSynonym = false;
 	    boolean isSynonymWarning = false;
 	    String synonymWarning = "";
 	    String synonymWarningUrl = "";
 	    String synonymWarningH1 = "";
 	    String synonymWarningReference = "";
 	    String synonymWarningReferenceShort = "";
 	    String synonymWarningLink = "";
 	    int synonymWarningId = 0;
 
 	    
 	    // does this page requires disambiguation
 	    boolean requiresDisambiguation = false;
 	 
 	    
 	    /*
 	     * the following vars are containers that will receive the related taxon
 	     * for all name
 	     *
 	     */
 	    List<TaxonModel> nameTaxons = null;
 	    
 	    /*
 	     * the following vars are containers that will receive the related taxon
 	     * for all vernacular name 
 	     */
 	    List<VernacularNameModel> vernacularNames = null;
 	    List<TaxonModel> vernacularTaxons = new ArrayList<TaxonModel>();
 
 
 	    // disambiguation vernacular concept container
 	    List<Map<String,Object>> disambiguationVernaculars = new ArrayList<Map<String,Object>>();
 	    // disambiguation taxons container
 	    List<Map<String,Object>> disambiguationTaxons = new ArrayList<Map<String,Object>>();
 
 	    // single taxon concept 
 	    TaxonModel taxon = null;
 	    
 	    // id
 	    int id = 0;
 	    
 	    // status 
 	    String status = "";
 	 
 	    // rank
 	    String rank = "";
 	    
 	    // rankid
 	    int rankid = 0;
 	    
 	    // reference code
 	    String reference = "";
 
 	    // reference short
 	    String referenceShort = "";
 	    
 	    // reference link
 	    String link = "";
 	    
 	    // data hashmap that will be passed on to root document for display in .ftl
 	    Map<String,Object> data = new HashMap<String,Object>();
 	    
 	    // content vectors (propety store for object passed to the ftl)
 	    List<Map<String,Object>> taxonParents = new ArrayList<Map<String,Object>>();
 	    List<Map<String,Object>> taxonHabituses = new ArrayList<Map<String,Object>>();
 	    
 	    vernacularNames = vernacularNameDAO.loadVernacularNameByName(name);
 	    // for a given vernacular name, try to find all the related taxons
 	   
 	    if(vernacularNames != null){
 	        //get all concepts attached to a vernacular name and add all taxons
 	        
 	        for(VernacularNameModel vernacular : vernacularNames){
 	            vernacularTaxons.add(vernacular.getTaxon());
 	        }
 	   		if(vernacularTaxons != null){
 		        if(vernacularTaxons.size() > 1){
 		        	isVernacularName = true;
 		            requiresDisambiguation = true;
 		        }
 		        else if(vernacularTaxons.size() == 1){
 		        	isVernacularName = true;
 		            isVernacularNameWarning = true;
 		            vernacularNameWarning = vernacularNames.get(0).getName().trim();
 		            vernacularNameWarningId = vernacularNames.get(0).getId();
 		            vernacularNameWarningStatus = vernacularNames.get(0).getStatus().getStatus();
 		            vernacularNameWarningLanguage = vernacularNames.get(0).getLanguage();
 		            vernacularNameWarningReference = vernacularNames.get(0).getReference().getReference();
 		            vernacularNameWarningReferenceShort = vernacularNames.get(0).getReference().getReferenceshort();
 		            vernacularNameWarningLink = vernacularNames.get(0).getReference().getUrl();
 		        	taxon = vernacularTaxons.get(0);
 		        	
 	                if(!isRedirect){
 	                    pageTitle = vernacularNames.get(0).getName();
 	                }
 		        }
 		    }
 	    }
 	    
 	    // for a given name, try to find all the related taxons
 	    nameTaxons = taxonDAO.loadTaxonByName(name);
 	   	if(nameTaxons != null){
 	        if(nameTaxons.size() > 1){
 	            isTaxon = true;
 	            requiresDisambiguation = true;
 	        }
 	        else if(nameTaxons.size() == 1){
 	        	taxon = nameTaxons.get(0);
 	            // if this is a synonym concept, we need to find the accepted parent of this taxon
 	            // to display on the page. In the extreamly rare case where a synonym taxon has more
 	            // than one parent, we need to go to disambiguation.
 	            if(taxon.getStatus().getId() == Status.SYNONYM){
 	            	isSynonymWarning = true;
 	            	isSynonym = true;
 	            	synonymWarning = taxon.getLookup().getCalnamehtmlauthor();
 	            	synonymWarningUrl = taxon.getLookup().getCalname();
 	            	synonymWarningH1 = taxon.getLookup().getCalnamehtml();
 	            	synonymWarningId = taxon.getId();
 	                synonymWarningReference = taxon.getReference().getReference();
 	                synonymWarningReferenceShort = taxon.getReference().getReferenceshort();
 	                synonymWarningLink = taxon.getReference().getUrl();
 	            	
 	            	if(taxon.getParents() != null && taxon.getParents().size() > 1){
 	           			isTaxon = true;
 	          			requiresDisambiguation = true;
 	            	}
 	           		else if(taxon.getParents() != null && taxon.getParents().size()  == 1){
 	      			    isTaxon = true;
 	      			    nameTaxons = taxon.getParents();
 	      			    taxon = nameTaxons.get(0);
 	            	}
 	            	if(!isRedirect){
 	                    pageTitle = taxon.getLookup().getCalname();
 	                }
 	            }
 	            else{
 	                isTaxon = true;
 	            }
 	        }
 	    }
 	   	
 	   	// is a vernacular name shares a name with a taxon ; automatic disambiguation
 	   	if(isTaxon && isVernacularName){
 	   		requiresDisambiguation = true;
 	   	}
 
 	    // raise error if name param is not a taxon and not a vernacular name 
 	    if(!isTaxon && !isVernacularName){
 	    	return null;
 	    }
 	    else if(requiresDisambiguation){
 	        if(isVernacularName){
 	   		    for(VernacularNameModel vernacular : vernacularNames){
 	   		    	Map<String,Object> vernacularDisambiguation = new HashMap<String,Object>();
 	   		    	// base discriminating vernacular info : name, source, status, language
 	   		    	vernacularDisambiguation.put("vernacularid",vernacular.getId());
 	   		    	vernacularDisambiguation.put("name",vernacular.getName());
 	   		    	vernacularDisambiguation.put("reference",vernacular.getReference().getReference());
 	   		    	vernacularDisambiguation.put("referenceShort",vernacular.getReference().getReferenceshort());
 	   		    	vernacularDisambiguation.put("link",vernacular.getReference().getUrl());
 	   		    	vernacularDisambiguation.put("language",vernacular.getLanguage());
 	   		    	vernacularDisambiguation.put("status",vernacular.getStatus().getStatus());
 	   		    	// taxon concept
 	   		    	HashMap<String,Object> t = new HashMap<String,Object>();
 	   		    	t.put("taxonId",vernacular.getTaxon().getId());
 	   		    	t.put("fullScientificName",vernacular.getTaxon().getLookup().getCalnamehtmlauthor());
 	   		    	t.put("reference",vernacular.getTaxon().getReference().getReference());
 	   		    	t.put("referenceShort",vernacular.getTaxon().getReference().getReferenceshort());
 	   		    	t.put("link",vernacular.getTaxon().getReference().getUrl());
 	   		    	t.put("status",vernacular.getTaxon().getStatus().getStatus());
 	   		    	t.put("rank",vernacular.getTaxon().getRank().getRank());
 	   		    	vernacularDisambiguation.put("taxon",t);
 	   		    	disambiguationVernaculars.add(vernacularDisambiguation);
 	   		    }
 	    	}
 	    	
 	    	if(isTaxon){
 	    		if(isRedirect){
 	    			  //pageTitle = TaxonManager.findByName(name).get(0).getLookup().getCalname();
 	    			pageTitle = nameTaxons.get(0).getLookup().getCalname();
 	    		}
 	            //nameH1 = TaxonManager.findByName(name).get(0).getLookup().getCalnamehtml();
 	    		nameH1 = nameTaxons.get(0).getLookup().getCalnamehtml();
 	            for(TaxonModel taxonConcept : nameTaxons){
 	                Map<String,Object> taxonDisambiguation = new HashMap<String,Object>();
 	                List<Map<String,Object>> taxonDisambiguationParents = new ArrayList<Map<String,Object>>();
 	                taxonDisambiguation.put("taxonId",taxonConcept.getId());
 	                
 	                taxonDisambiguation.put("fullScientificName",taxonConcept.getLookup().getCalnamehtmlauthor());
 	                taxonDisambiguation.put("reference",taxonConcept.getReference().getReference());
 	                taxonDisambiguation.put("referenceShort",taxonConcept.getReference().getReferenceshort());
 	                taxonDisambiguation.put("link",taxonConcept.getReference().getUrl());
 	                taxonDisambiguation.put("status",taxonConcept.getStatus().getStatus());
 	                taxonDisambiguation.put("rank",taxonConcept.getRank().getRank());
 	                
 	                List<TaxonModel> parents = taxonConcept.getParents();
 	                for(TaxonModel parent : parents){
 	                	HashMap<String,Object> taxonParent = new HashMap<String,Object>();
 	                	taxonParent.put("taxonId",parent.getId());
 	                    taxonParent.put("fullScientificName",parent.getLookup().getCalnamehtmlauthor());
 	                    taxonParent.put("reference",parent.getReference().getReference());
 	                    taxonParent.put("referenceShort",parent.getReference().getReferenceshort());
 	                    taxonParent.put("link",parent.getReference().getUrl());
 	                    taxonParent.put("status",parent.getStatus().getStatus());
 	                    taxonParent.put("rank",parent.getRank().getRank());
 	                    taxonDisambiguationParents.add(taxonParent);
 	                }
 	                
 	                taxonDisambiguation.put("parents",taxonDisambiguationParents);
 	                
 	                disambiguationTaxons.add(taxonDisambiguation);
 	            }
 	    	}
 	    }
 	    else{
 	    	// id
 	    	id =  taxon.getId();
 	    	
 	    	// fullscientific name
 	        name = taxon.getLookup().getCalnamehtmlauthor();
 
 	    	if(isRedirect){
 		       pageTitle = taxon.getLookup().getCalname();
 	    	}
 	        
 	        nameH1 = taxon.getLookup().getCalnamehtml();
 	        
 	        // status
 	        status = taxon.getStatus().getStatus();
 	        
 	        
 	        // rank
 	        rank = taxon.getRank().getRank();
 
 	        // rankid
 	        rankid = taxon.getRank().getId();
 	        
 	        // refrence 
 	        reference = taxon.getReference().getReference();
 
 	        // refrenceShort
 	        referenceShort = taxon.getReference().getReferenceshort();
 	        
 	        // link 
 	        link = taxon.getReference().getUrl();
 	        
 	        //same as 
 	        
 	        propertyMapHelper.fillHybridParents(taxon.getHybridparents(),data);
 	        
 	        // parents ; it is accepted that a taxon has only one parent, but this
 	        // is not a rule and some exceptions may occur with synonyms. To prevent
 	        // errors, the getParent() method of a txon returns a list of most of 
 	        // the time one element...
 	        List<TaxonModel> parents = taxon.getParents();
 	        if(parents != null){
 	            for(TaxonModel parent : parents){
 	                HashMap<String,Object> parentInfo = new HashMap<String,Object>();
 	                try{
 	                    parentInfo.put("fullScientificName",parent.getLookup().getCalnamehtmlauthor());
 	                    taxonParents.add(parentInfo);
 	                }
 	                catch(NullPointerException e){
 	                    //drop on error
 	                }
 	            }
 	        }
 	        
 	        //Generate classification
 	        List<TaxonLookupModel> classificationList = new ArrayList<TaxonLookupModel>();
 	        taxonManager.getParentClassification(taxon,classificationList);
 	        classificationList.add(taxon.getLookup());
	        classificationList.addAll(taxonomyDAO.getAcceptedChildrenListFromNestedSets(taxon.getId(), PropertyMapHelper.getRankLabelRange(taxon.getRank().getId())));
 	        propertyMapHelper.fillTaxonClassification(classificationList, data);
 	        
 	        
 	        propertyMapHelper.fillVernacularNames(taxon.getVernacularnames(), data);
 	        
 	        propertyMapHelper.fillSynonyms(taxon.getChildren(), data);
 
 	        propertyMapHelper.fillTaxonDistribution(taxon.getDistribution(),taxon,data);
 	        
 	        // habitus
 	        List<HabitModel> habituses;
 	        habituses = taxon.getHabit();
 	        if(habituses != null){
 	            for(HabitModel habitus : habituses){
 	                try{
 	                    HashMap<String,Object> habitusData = new HashMap<String,Object>();  
 	                    habitusData.put("habitus",habitus.getHabit());
 	                    taxonHabituses.add(habitusData);
 	                }
 	                catch(NullPointerException e){
 	                    //drop on error
 	                }
 	            }
 	        }         
 	    }
 	    
 	    data.put("taxonId",id);
 	    
 	    data.put("requiresDisambiguation",requiresDisambiguation);
 	    data.put("isRedirect",isRedirect);
 	    data.put("isTaxon",isTaxon);
 	    data.put("id",id);
 	    data.put("name",name);
 	    data.put("pageTitle",pageTitle);
 	    data.put("nameH1",nameH1);
 	    
 	    data.put("isSynonym",isSynonym);
 	    data.put("isSynonymWarning",isSynonymWarning);
 	    data.put("synonymWarning",synonymWarning);
 	    data.put("synonymWarningUrl",synonymWarningUrl);
 	    data.put("synonymWarningH1",synonymWarningH1);
 	    data.put("synonymWarningId",synonymWarningId);
 	    data.put("synonymWarningReference",synonymWarningReference);
 	    data.put("synonymWarningReferenceShort",synonymWarningReferenceShort);
 	    data.put("synonymWarningLink",synonymWarningLink);
 	    data.put("isVernacularName",isVernacularName);
 	    data.put("isVernacularNameWarning",isVernacularNameWarning);
 	    data.put("vernacularNameWarning",vernacularNameWarning);
 	    data.put("vernacularNameWarningId",vernacularNameWarningId);
 	    data.put("vernacularNameWarningStatus",vernacularNameWarningStatus);
 	    data.put("vernacularNameWarningLanguage",vernacularNameWarningLanguage);
 	    data.put("vernacularNameWarningReference",vernacularNameWarningReference);
 	    data.put("vernacularNameWarningReferenceShort",vernacularNameWarningReferenceShort);
 	    data.put("vernacularNameWarningLink",vernacularNameWarningLink);
 	    data.put("status",status);
 	    data.put("reference",reference);
 	    data.put("referenceShort",referenceShort);
 	    data.put("link",link);
 	    data.put("rank",rank);
 	    data.put("rankId",rankid);
 	    
 	    data.put("disambiguationVernaculars",disambiguationVernaculars);
 	    data.put("disambiguationTaxons",disambiguationTaxons);
 	    //data.put("hybridParents",taxonHybridParents);
 	    data.put("parents",taxonParents);
 	    //data.put("tree",taxonClassification);
 	    //data.put("vernacularNames",taxonVernacularNames);
 	    //data.put("synonyms",taxonSynonyms);
 	    data.put("habituses",taxonHabituses);
 	    //data.put("computedDistribution",computedDistribution);
 	    //data.put("distributions",taxonDistributions);
 
 	    // add extra data to page global hashmap
 	    extra.put("rank",rank.toLowerCase());
 	    extra.put("isDisambiguation",requiresDisambiguation);
 	    extra.put("isVernacular",isVernacularName);
 	    extra.put("isSynonym",isSynonym);
 	    
 	    return data;
 	}
 }
