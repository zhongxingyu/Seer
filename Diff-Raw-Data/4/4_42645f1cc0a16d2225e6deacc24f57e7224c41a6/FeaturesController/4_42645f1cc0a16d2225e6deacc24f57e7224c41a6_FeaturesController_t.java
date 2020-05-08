 package org.genedb.crawl.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.CrawlException;
 import org.genedb.crawl.annotations.ResourceDescription;
 import org.genedb.crawl.mappers.FeaturesMapper;
 import org.genedb.crawl.mappers.MapperUtil;
 import org.genedb.crawl.mappers.OrganismsMapper;
 import org.genedb.crawl.mappers.TermsMapper;
 import org.genedb.crawl.mappers.MapperUtil.HierarchicalSearchType;
 import org.genedb.crawl.model.BlastPair;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.HierarchyRelation;
 import org.genedb.crawl.model.HierarchicalFeature;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.model.Statistic;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 
 @Controller
 @RequestMapping("/features")
 @ResourceDescription("Feature related queries")
 public class FeaturesController extends BaseQueryController {
 	
 	private static Logger logger = Logger.getLogger(FeaturesController.class);
 	
 	@Autowired
 	FeaturesMapper featuresMapper;
 	
 	@Autowired
 	TermsMapper terms;
 	
 	@Autowired
 	OrganismsMapper organismsMapper;
 	
 	private String[] defaultRelationshipTypes = new String[] {"part_of", "derives_from"};
 	
 	@ResourceDescription("Get a feature's gene")
 	@RequestMapping(method=RequestMethod.GET, value="/genes")
 	public List<Feature> genes(@RequestParam(value="features") List<String> features) {
 		return MapperUtil.getGeneFeatures(featuresMapper, features);
 	}
 	
 	@ResourceDescription("Returns the hierarchy of a feature (i.e. the parent/child relationship graph), but routed on the feature itself (rather than Gene).")
 	@RequestMapping(method=RequestMethod.GET, value="/hierarchy")
 	public List<HierarchicalFeature> hierarchy( 
 			@RequestParam("features") List<String> features, 
 			@RequestParam(value="root_on_genes", defaultValue="false", required=false) boolean root_on_genes,
 			@RequestParam(value="relationships", required=false) String[] relationships) throws CrawlException {
 		
 		if (relationships == null) {
 			relationships = defaultRelationshipTypes;
 		}
 		
 		List<Cvterm> relationshipTypes = getRelationshipTypes(Arrays.asList(relationships), terms);
 		List<String> featuresToRecurse = features;
 		List<HierarchicalFeature> hfs = new ArrayList<HierarchicalFeature>();
 		
 		if (root_on_genes) {
 			featuresToRecurse = new ArrayList<String>();
 			
 			Collection<Feature> featureGenes = MapperUtil.getGeneFeatures(featuresMapper,features);
 			
 			for (Feature fg : featureGenes) {
 				featuresToRecurse.addAll(fg.genes);
 			}
 			
 		}
 		
 		for (String feature : featuresToRecurse) {
 			
 			HierarchicalFeature hf = new HierarchicalFeature();
 			hf.uniqueName = feature;
 			
 			MapperUtil.searchForRelations(featuresMapper, hf, relationshipTypes, HierarchicalSearchType.CHILDREN);
 			MapperUtil.searchForRelations(featuresMapper, hf, relationshipTypes, HierarchicalSearchType.PARENTS);
 			
 			hfs.add(hf);
 			
 			
 			
 			
 		}
 		
 		
 		return hfs;
 		
 		
 		
 	}
 	
 	@ResourceDescription("Returns coordinages of a feature if located on a region.")
 	@RequestMapping(method=RequestMethod.GET, value="/coordinates")
 	public List<Feature> coordinates(
 			@RequestParam("features") List<String> features, 
 			@RequestParam(value="region", required=false) String region ) {
 		return featuresMapper.coordinates(features, region);
 	}
 	
 	@ResourceDescription("Returns a feature's synonyms.")
 	@RequestMapping(method=RequestMethod.GET, value="/synonyms")
 	public List<Feature> synonyms(
 			@RequestParam("features") List<String> features,
 			@RequestParam(value="types", required=false) List<String> types) {
 		return featuresMapper.synonyms(features, types);
 	}
 	
 	@ResourceDescription("Return matching features")
 	@RequestMapping(method=RequestMethod.GET, value="/withnamelike")
 	public List<Feature> withnamelike( 
 			@RequestParam("term") String term,
 			@RequestParam(value="regex", defaultValue="false") boolean regex, 
 			@RequestParam(value="region", required=false) String region) {
 		List<Feature> synonyms = featuresMapper.synonymsLike(term, regex, region);
 		List<Feature> matchingFeatures = featuresMapper.featuresLike(term, regex, region);
 		matchingFeatures.addAll(synonyms);
 		return matchingFeatures;
 	}
 	
 	
 	@ResourceDescription("Return feature properties")
 	@RequestMapping(method=RequestMethod.GET, value="/properties")
 	public List<Feature> properties(
 			@RequestParam(value="features") List<String> features, 
 			@RequestParam(value="types", required=false) List<String> types) {
 		return featuresMapper.properties(features,types);
 	}
 	
 	@ResourceDescription("Return feature properties")
 	@RequestMapping(method=RequestMethod.GET, value="/withproperty")
 	public List<Feature> withproperty( 
 			@RequestParam("value") String value,
 			@RequestParam(value="regex", defaultValue="false") boolean regex, 
 			@RequestParam(value="region", required=false) String region,
 			@RequestParam(value="type", required=false) String type) {
 		return featuresMapper.withproperty(value, regex, region, type);
 	}
 	
 	@ResourceDescription("Return feature pubs")
 	@RequestMapping(method=RequestMethod.GET, value="/pubs")
 	public List<Feature> pubs(@RequestParam(value="features") List<String> features) {
 		return featuresMapper.pubs(features);
 	}
 	
 	@ResourceDescription("Return feature dbxrefs")
 	@RequestMapping(method=RequestMethod.GET, value="/dbxrefs")
 	public List<Feature> dbxrefs(@RequestParam(value="features") List<String> features) {
 		return featuresMapper.dbxrefs(features);
 	}
 	
 	
 	@ResourceDescription(value="Return feature cvterms")
 	@RequestMapping(method=RequestMethod.GET, value="/terms")
 	public List<Feature> terms(@RequestParam(value="features") List<String> features, @RequestParam(value="cvs", required=false) List<String> cvs) {
 		return featuresMapper.terms(features, cvs);
 	}
 	
 	@ResourceDescription("Return feature with specified cvterm")
 	@RequestMapping(method=RequestMethod.GET, value="/withterm")
 	public List<Feature> withterm(
 			@RequestParam(value="term") String term, 
 			@RequestParam(value="cv", required=false) String cv,
 			@RequestParam(value="regex", defaultValue="false") boolean regex, 
 			@RequestParam(value="region", required=false) String region) {
 		
 		logger.info(String.format("%s - %s - %s - %s", term, cv, regex, region));
 		
 		return featuresMapper.withterm(term, cv, regex, region);
 		
 	}
 	
 	@ResourceDescription("Return feature orthologues")
 	@RequestMapping(method=RequestMethod.GET, value="/orthologues")
 	public List<Feature> orthologues(@RequestParam(value="features") List<String> features) {
 		return featuresMapper.orthologues(features);
 	}
 	
 	@ResourceDescription(value="Return feature clusters")
 	@RequestMapping(method=RequestMethod.GET, value="/clusters")
 	public List<Feature> clusters(@RequestParam(value="features") List<String> features) {
 		return featuresMapper.clusters(features);
 	}
 	
 	@ResourceDescription(value="Return features that have had annotation changes")
 	@RequestMapping(method=RequestMethod.GET, value="/annotation_changes")
 	public List<Feature> annotationModified( 
 			@RequestParam(value="date") Date date, 
 			@RequestParam("organism") String organism, 
 			@RequestParam(value="region", required = false) String region) throws CrawlException {
 		Organism o = getOrganism(organismsMapper, organism);
 		return featuresMapper.annotationModified(date, o.ID, region);
 	}
 	
 	@ResourceDescription(value="Return features that have had annotation changes")
 	@RequestMapping(method=RequestMethod.GET, value="/annotation_changes_statistics")
 	public List<Statistic> annotationModifiedStatistics( 
 			@RequestParam(value="date") Date date, 
 			@RequestParam("organism") String organism, 
 			@RequestParam(value="region", required = false) String region) throws CrawlException {
 		Organism o = getOrganism(organismsMapper, organism);
 		return featuresMapper.annotationModifiedStatistics(date, o.ID, region);
 	}
 	
 	
 	@ResourceDescription("Return blast hits between two features")
 	@RequestMapping(method=RequestMethod.GET, value="/blastpair")
 	public List<BlastPair> blastpair( 
 			@RequestParam(value="f1") String f1, 
 			@RequestParam(value="start1") int start1, 
 			@RequestParam(value="end1") int end1,
 			@RequestParam(value="f2") String f2, 
 			@RequestParam(value="start2") int start2, 
 			@RequestParam(value="end2") int end2,
 			@RequestParam(value="length", required=false) Integer length,
			@RequestParam(value="normscore", required=false) Double score) {
		logger.info("Filtering on score :");
		logger.info(score);
 		return featuresMapper.blastPairs(f1, start1, end1, f2, start2, end2, length, score); 
 	}
 	
 		
 		
 	
 	
 }
