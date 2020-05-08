 package org.genedb.crawl.mappers;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.ibatis.annotations.Param;
 
 import org.genedb.crawl.model.BlastPair;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.HierarchyGeneFetchResult;
 import org.genedb.crawl.model.HierarchyRelation;
 import org.genedb.crawl.model.Statistic;
 
 public interface FeaturesMapper {
 	
 	//int getFeatureID(String uniquename);
 	
 	List<HierarchyGeneFetchResult> getGeneForFeature(@Param("features") List<String> features );
 	List<HierarchyRelation> getRelationshipsParents(@Param("feature") String feature, @Param("relationships") List<Cvterm> relationships );
 	List<HierarchyRelation> getRelationshipsChildren(@Param("feature") String feature, @Param("relationships") List<Cvterm> relationships );
 	
 	List<Feature> coordinates(@Param("features") List<String> features, @Param("region") String region );
 	List<Feature> synonyms(@Param("features") List<String> features, @Param("types") List<String> types );
 	
 	List<Feature> properties(@Param("features") List<String> features, @Param("types") List<String> types );
 	
 	
 	
 	List<Feature> pubs (@Param("features") List<String> features);
 	List<Feature> dbxrefs (@Param("features") List<String> features);
 	List<Feature> terms (@Param("features") List<String> features, @Param("cvs") List<String> cvs);
 	List<Feature> orthologues(@Param("features") List<String> features);
 	List<Feature> clusters(@Param("features") List<String> features);
 	
 	List<Feature> synonymsLike(
 			@Param("term") String term, 
 			@Param("regex") Boolean regex, 
 			@Param("region") String region);
 	
 	List<Feature> featuresLike(
 			@Param("term") String term, 
 			@Param("regex") Boolean regex, 
 			@Param("region") String region);
 	
 	List<Feature> withproperty(
 			@Param("value") String value, 
 			@Param("regex") Boolean regex, 
 			@Param("region") String region,
 			@Param("type") String type);
 	
 	List<Feature> withterm(
 			@Param("cvterm") String cvterm,
 			@Param("cv") String cv,
 			@Param("regex") Boolean regex, 
 			@Param("region") String region);
 	
 	List<Feature> timelastmodified(
 			@Param("date") Date date, 
 			@Param("organism_id") Integer organism_id, 
 			@Param("types") List<String> types,
 			@Param("exclude") boolean exclude);
 	
 	
 	List<Feature> annotationModified(
 			@Param("date") Date date, 
 			@Param("organism_id") Integer organism_id, 
 			@Param("region") String region);
 	
 	List<Statistic> annotationModifiedStatistics(@Param("date") Date date, @Param("organism_id") Integer organism_id, @Param("region") String region);
 	
 	List<BlastPair> blastPairs(
		@Param("regex") String f1, @Param("regex") int start1, @Param("regex") int end1, 
		@Param("regex") String f2, @Param("regex") int start2, @Param("regex") int end2, 
		@Param("regex") Integer length, 
		@Param("regex") Integer normscore);
 	
 }
