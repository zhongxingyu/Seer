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
 	
	public synchronized S3ObjectSummary get(String bucket, String key) {
 		if (!cache.containsKey(bucket)) {
			System.out.println("Filling cache!");
 			cache.put(bucket, toMap(scanner.listObjects(bucket)));
 		}
 		return cache.get(bucket).get(key);
 	}
 	
	public synchronized void clear(String bucket) {
		System.out.println("Clearing cache!");
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
