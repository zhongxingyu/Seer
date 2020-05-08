 package org.genedb.crawl.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.CrawlException;
 import org.genedb.crawl.annotations.ResourceDescription;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.LocationBoundaries;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.model.ResultsRegions;
 import org.genedb.crawl.model.Sequence;
 import org.gmod.cat.FeaturesMapper;
 import org.gmod.cat.OrganismsMapper;
 import org.gmod.cat.RegionsMapper;
 import org.gmod.cat.TermsMapper;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping("/regions")
 @ResourceDescription("Provides queries related to large genomic regions such as chromosomes or contigs")
 public class RegionsController extends BaseQueryController {
 	
 	private Logger logger = Logger.getLogger(RegionsController.class);
 	
 	@Autowired
 	RegionsMapper regionsMapper;
 	
 	@Autowired
 	TermsMapper termsMapper;
 	
 	@Autowired
 	FeaturesMapper featuresMapper;
 	
 	@Autowired
 	OrganismsMapper organismsMapper;
 	
 	private boolean cacheRegionsOnStartup = false;
 	private Map<String, List<Feature>> organismRegionMap = new HashMap<String, List<Feature>>();
 	
 	/**
 	 * Force the controller to cache all organism regions on startup.
 	 * @param cacheRegionsOnStartup
 	 */
 	public void setCacheRegionsOnStartup(boolean cacheRegionsOnStartup) {
 		this.cacheRegionsOnStartup = cacheRegionsOnStartup;
 	}
 	
 	@PostConstruct
 	void setup() throws CrawlException {
 		if (! cacheRegionsOnStartup) {
 			return;
 		}
 		for (Organism o : organismsMapper.list()) {
 			List<Feature> r = regionsMapper.inorganism( o.ID, null, null, null );
 			Collections.sort(r, new FeatureUniqueNameSorter());
 			organismRegionMap.put(String.valueOf(o.ID), r);
 			logger.info(String.format("Cached %s.", o.common_name));
 		}
 	}
 	
 	
 	
 	/**
 	 * The exclude parameter works in this form:
 	 * 	&exclude=repeat_region&exclude=gene
 	 * 
 	 * but not this form :
 	 * 
 	 * 	&exclude[]=repeat_region&exclude[]=gene
 	 * 
 	 * which JQuery would typically send. I think we can resolve this by setting 
 	 * 
 	 * 	jQuery.ajaxSettings.traditional = true;
 	 * 
 	 * or
 	 * 
 	 * 	$.ajaxSetup({ traditional: true }); 
 	 * 
 	 * in Web-Artemis.
 	 * 
 	 * 
 	 * @param region
 	 * @param start
 	 * @param end
 	 * @param exclude
 	 * @return
 	 * @throws CrawlException
 	 */
 	@RequestMapping(method=RequestMethod.GET, value={"/locations", "/locations.*"})
 	@ResourceDescription("Returns features and their locations on a region of interest")
 	public ResultsRegions locations(
 			ResultsRegions results,
 			@RequestParam("region") String region, 
 			@RequestParam(value="start",required=false) Integer start, 
 			@RequestParam(value="end", required=false) Integer end, 
 			@RequestParam(value="exclude", required=false) @ResourceDescription("A list of features to exclude.") List<String> exclude
 			) throws CrawlException {
 		
 		
 		if (start == null) {
 			start = 0;
 		}
 		
 		if (end == null) {
			end = regionsMapper.sequence(region).length;
 		}
 		
 		logger.info(String.format("Getting locations for %s.", region));
 				
 		// trying to speed up the boundary query by determining the types in advance
         List<Integer> geneTypes = termsMapper.getCvtermIDs("sequence", new String[] {"gene", "pseudogene"});
         
         logger.info("Gene Types " + geneTypes);
         
         int actualStart = start;
         int actualEnd = end;
         
         LocationBoundaries expandedBoundaries = regionsMapper.locationsMinAndMaxBoundaries(region, start, end, geneTypes);
         if (expandedBoundaries != null) {
 			if (expandedBoundaries.start != null && expandedBoundaries.start < start) {
 				actualStart = expandedBoundaries.start;
 			}
 			if (expandedBoundaries.end != null &&expandedBoundaries.end > end) {
 				actualEnd = expandedBoundaries.end;
 			}
         }
         
 		logger.info( String.format("Locating on %s : %s-%s (%s)", region, actualStart, actualEnd, exclude));
 		
 		results.locations = regionsMapper.locations(region, actualStart, actualEnd, exclude);
 		results.actual_end = actualEnd;
 		results.actual_start = actualStart;
 		
 		return results;
 
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value={"/locations_paged", "/locations_paged.*"})
 	@ResourceDescription("Returns features and their locations on a region of interest, paged by limit and offset.")
 	public ResultsRegions locationsPaged(
 			ResultsRegions results,
 			@RequestParam("region") String region, 
 			@RequestParam("limit") int limit, 
 			@RequestParam("offset") int offset, 
 			@RequestParam(value="exclude", required=false) @ResourceDescription("A list of features to exclude.") List<String> exclude
 			) throws CrawlException {
 		
 		
 		logger.info(String.format("Getting locations for %s.", region));
 				
 		// trying to speed up the boundary query by determining the types in advance
         List<Integer> geneTypes = termsMapper.getCvtermIDs("sequence", new String[] {"gene", "pseudogene"});
         
         logger.info("Gene Types " + geneTypes);
         
 		logger.info( String.format("Locating paged on %s : %s-%s (%s)", region, limit, offset, exclude));
 		
 		results.locations = regionsMapper.locationsPaged(region, limit, offset, exclude);
 		
 		return results;
 
 	}
 	
 	
 	@RequestMapping(method=RequestMethod.GET, value="/sequence")
 	@ResourceDescription("Returns the sequence on a region.")
 	public ResultsRegions sequence(
 			ResultsRegions results,
 			@RequestParam("region") String region, 
 			@RequestParam(value="start", required=false) Integer start, 
 			@RequestParam(value="end", required=false) Integer end,
 			@RequestParam(value="metadata_only", required=false, defaultValue="false") boolean metadataOnly) {
 		
 		List<Sequence> sequences = new ArrayList<Sequence>();
 		Sequence sequence = regionsMapper.sequence(region);
 		sequences.add(sequence);
 		results.sequences = sequences;
 		
 		String sequenceResidues = sequence.dna;
 		
 		int length = (sequence.length == null) ? sequenceResidues.length() : sequence.length;
 		if (length == 0) {
 			return results;
 		}
 		
 		// if it's a simple case of no start or end position, just return what we've got
 		if (start == null && end == null) {
 			
 			if (metadataOnly) {
 				sequence.dna = null;
 			}
 			sequence.start = 0;
 			sequence.end = length -1;
 			sequence.region = region;
 			
 			return results;
 		}
 		
 		
 		if (start == null) {
 			start = 0;
 		}
 		
 		if (end == null) {
 			end = length;
 		}
 		
 		int lastResiduePosition = length -1;
 		int actualStart = start -1;
 		int actualEnd = end -1;
 		
 		if (actualStart > lastResiduePosition || actualStart > actualEnd) {
 			return results;
 		}
 		
 		if (actualEnd > lastResiduePosition) {
 			actualEnd = lastResiduePosition;
 		}
 		
 		if (! metadataOnly) {
 			sequence.dna = sequenceResidues.substring(actualStart, actualEnd);
 		} else {
 			sequence.dna = null;
 		}
 		
 		sequence.start = start;
 		sequence.end = end;
		sequence.length = sequence.dna.length();
 		sequence.region = region;
 		
 		return results;
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="inorganism")
 	@ResourceDescription("Returns the regions in an organism.")
 	public ResultsRegions inorganism(ResultsRegions results, 
 			@RequestParam("organism") String organism,
 			@RequestParam(value="limit", required=false) Integer limit, 
 			@RequestParam(value="offset", required=false) Integer offset,
 			@RequestParam(value="type", required=false) String type) throws CrawlException {
 		
 		Organism o = getOrganism(organismsMapper, organism);
 		
 		List<Feature> r = null;
 		if (organismRegionMap.containsKey(o.ID)) {
 			r = organismRegionMap.get(o.ID);
 		} else {
 			r = regionsMapper.inorganism( o.ID, limit, offset, type);
 			Collections.sort(r, new FeatureUniqueNameSorter());
 			organismRegionMap.put(String.valueOf(o.ID), r);
 		}
 		
 		results.regions = r;
 		
 		return results;
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="typesinorganism")
 	@ResourceDescription("Returns the types of region present in an organism.")
 	public ResultsRegions typesInOrganism(ResultsRegions results, 
 			@RequestParam("organism") String organism
 			) throws CrawlException {
 		
 		Organism o = getOrganism(organismsMapper, organism);
 		
 		List<Cvterm> regionTypes = regionsMapper.typesInOrganism( o.ID );
 		List<Feature> regions = new ArrayList<Feature>();
 		
 		for (Cvterm regionType : regionTypes) {
 			Feature region = new Feature();
 			region.type = regionType;
 			regions.add(region);
 		}
 		
 		results.regions = regions;
 		
 		return results;
 	}
 	
 	class FeatureUniqueNameSorter implements Comparator<Feature> {
 		@Override
 		public int compare(Feature f1, Feature f2) {
 			return f1.uniqueName.compareTo(f2.uniqueName);
 		}
 	}
 	
 }
