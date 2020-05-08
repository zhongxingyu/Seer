 package com.mathieubolla.io;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 
 public class S3KeyCache {
 	private final S3Scanner scanner;
 	private final Map<String, Map<String, S3ObjectSummary>> cache = new HashMap<String, Map<String,S3ObjectSummary>>();
 
 	@Inject
 	public S3KeyCache(S3Scanner scanner) {
 		this.scanner = scanner;
 	}
 	
	public S3ObjectSummary get(String bucket, String key) {
 		if (!cache.containsKey(bucket)) {
 			cache.put(bucket, toMap(scanner.listObjects(bucket)));
 		}
 		return cache.get(bucket).get(key);
 	}
 	
	public void clear(String bucket) {
 		cache.remove(bucket);
 	}
 	
 	private Map<String, S3ObjectSummary> toMap(List<S3ObjectSummary> summaries) {
 		Map<String, S3ObjectSummary> destination = new HashMap<String, S3ObjectSummary>();
 		for (S3ObjectSummary summary : summaries) {
 			destination.put(summary.getKey(), summary);
 		}
 		return destination;
 	}
 }
