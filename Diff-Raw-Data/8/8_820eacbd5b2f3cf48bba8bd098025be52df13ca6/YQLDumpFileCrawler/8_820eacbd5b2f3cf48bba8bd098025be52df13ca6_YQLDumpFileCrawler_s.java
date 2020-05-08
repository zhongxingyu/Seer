 package de.hpi.fgis;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import de.hpi.fgis.concurrency.APIAccessRateLimitGuard.RateLimitedTask;
 import de.hpi.fgis.concurrency.AsyncResultHandler;
 import de.hpi.fgis.database.mongodb.CachedMongoDBObjectManager;
 import de.hpi.fgis.database.mongodb.MongoDBObjectManager;
 import de.hpi.fgis.twitter.TwitterDumpFileReader;
 import de.hpi.fgis.util.FileUtil;
 import de.hpi.fgis.util.ProgressReport;
 import de.hpi.fgis.yql.DeserializationException;
 import de.hpi.fgis.yql.YQLAccessRateLimitGuard;
 import de.hpi.fgis.yql.YQLCrawler;
 import de.hpi.fgis.yql.YQLCrawler.CrawlingResults;
 
 /**
  * Basic executor to parse a tweet file and crawl all contained urls via YQL
  * @author tongr
  */
 public class YQLDumpFileCrawler {
 	public static void main(String[] args) {
 		String folder;
 		if(args.length<=0) {
 			//folder = "./";
 			throw new IllegalArgumentException("Please specify an input folder!");
 		} else {
 			folder = args[0];
 		}
 		
 		new YQLDumpFileCrawler().parse(new FileUtil().scan(folder, "tweets_w_links_n_htags_.*\\.stream", false).toArray(new String[0]));
 	}
 	protected static final Logger LOG = Logger.getLogger(YQLDumpFileCrawler.class.getName());
 	private boolean finished = false;
 	// on average we will (re-)try to retrieve the data of an url 3-times before rejecting the resource
 	private final double retryProbability = 2D/3D;
 	private final int chunkSize = 100;
 	private final CachedMongoDBObjectManager redirectMan = new CachedMongoDBObjectManager(new MongoDBObjectManager("redirects", false), "from", 1000000, true);
 	private final MongoDBObjectManager webpageSink = new MongoDBObjectManager("webpages", false);
 	private final MongoDBObjectManager alignmentSink = new MongoDBObjectManager("alignments", false);
 	private final MongoDBObjectManager tweetSink = new MongoDBObjectManager("tweets", false);
 	
 	private void parse(String... files) {
 		System.out.println(new Date().toString());
 		
 		final Queue<AlignmentCandidate> alignmentCandidates = new LinkedList<>();
 		final Queue<AlignmentCandidate> retryAlignmentCandidates = new LinkedList<>();
 		
 		Closeable taskCloser = initJobs(alignmentCandidates, retryAlignmentCandidates);
 		addAlignmentTasks(alignmentCandidates, Collections.unmodifiableCollection(retryAlignmentCandidates), files);
 		
 		
 		
 		
 		try {
 			int pending;
 			do {
 				synchronized (this) {
 					// wait for 1s (for the pending requests to be executed)
 					this.wait(1000);
 				}
 				
 				synchronized (alignmentCandidates) {
 					pending = alignmentCandidates.size();
 				}
 			
 			} while( pending > 0 );
 			do {
 				synchronized (this) {
 					// wait for 1s (there might be some pending requests)
 					this.wait(1000);
 				}
 				
 				synchronized (retryAlignmentCandidates) {
 					pending = retryAlignmentCandidates.size();
 				}
 			} while( pending > 0 );
 			
 			synchronized (this) {
 				// wait for 10min (there might be some pending requests that will be re-tried forever, we'll cancel this after 10min)
 				this.wait(600000);
 			}
 			System.out.println(new Date().toString());
 		} catch (InterruptedException e) {
 			// ignore
 		} finally {
 			try {
 				taskCloser.close();
 			} catch (Exception e) {
 				LOG.log(Level.WARNING, "Unable to close pending modules", e);
 			}
 		}
 	}
 	
 	private Closeable initJobs(final Queue<AlignmentCandidate> alignmentCandidates, final Queue<AlignmentCandidate> retryAlignmentCandidates) {
 		final YQLAccessRateLimitGuard guard = YQLAccessRateLimitGuard.getInstance();
 		final YQLCrawler crawler = new YQLCrawler();
 		
 		final ProgressReport rpt = new ProgressReport("Crawling urls from tweets ...").setUnit("tweets").setReport(2500);
 		RateLimitedTask task = new RateLimitedTask() {
 			private final Random r = new Random();
 			@Override
 			public void run() {
 				try {
 					final HashSet<String> toBeCrawled = new HashSet<>(chunkSize*2);
 					final ArrayList<AlignmentCandidate> currentAlignments = new ArrayList<>(chunkSize);
 					final HashMap<String, String> cachedRedirects = new HashMap<>();
 
 					int approxCandidateCount = 0;
 					synchronized (alignmentCandidates) {
 						approxCandidateCount = alignmentCandidates.size();
 					}
 					boolean retry = false;
 					synchronized (retryAlignmentCandidates) {
 						// execute retries, if the number of retries is higher than the actual candidate list
 						if(retryAlignmentCandidates.size()>approxCandidateCount) {
 							while(toBeCrawled.size()<chunkSize && retryAlignmentCandidates.size()>0) {
 								AlignmentCandidate candidate = retryAlignmentCandidates.poll();
 								currentAlignments.add(candidate);
 								
 								for(String url : candidate.originalUrls()) {
 									DBObject redirect = redirectMan.findOne(url);
 									
 									if(redirect==null) {
 										toBeCrawled.add(url);
 									} else {
 										cachedRedirects.put(url, (String) redirect.get("to")); 
 									}
 								}
 							}
 							
 							if(toBeCrawled.size()>0) {
 								retry = true;
 							}
 						}
 					}
 					if(!retry) {
 						synchronized (alignmentCandidates) {
 							while(toBeCrawled.size()<chunkSize && alignmentCandidates.size()>0) {
 								AlignmentCandidate candidate = alignmentCandidates.poll();
 								currentAlignments.add(candidate);
 								
 								for(String url : candidate.originalUrls()) {
 									DBObject redirect = redirectMan.findOne(url);
 									
 									if(redirect==null) {
 										toBeCrawled.add(url);
 									} else {
 										cachedRedirects.put(url, (String) redirect.get("to")); 
 									}
 								}
 							}
 						}
 					}
 					
 					if(toBeCrawled.size()>0) {
 						final boolean isRetry = retry;
 						crawler.crawlAsync(toBeCrawled, new AsyncResultHandler<CrawlingResults>() {
 							
 							@Override
 							public void onThrowable(Throwable t) {
 								if(!(t instanceof IOException || t instanceof DeserializationException)) {
 									LOG.log(Level.WARNING, "Unexpected error occured!", t);
 								} else if (isRetry) {
 									LOG.log(Level.WARNING, "Some data extraction problems occured repeatedly!", t);
 								} else {
 									LOG.log(Level.INFO, "Some data extraction problems occured, retrying in several seconds ... ", t);
 									synchronized (retryAlignmentCandidates) {
 										retryAlignmentCandidates.addAll(currentAlignments);
 									}
 								}
 							}
 							
 							@Override
 							public void onCompleted(CrawlingResults data) {
 								if(data==null) {
 									return;
 								}
 								try {
 									ArrayList<DBObject> redirectItems = new ArrayList<>(data.urls().size());
 									
 									// store redirects
 									for(Entry<String, String> e : data.redirects().entrySet()) {
 										DBObject newItem = new BasicDBObject(2);
 										newItem.put("from", e.getKey());
 										newItem.put("to", e.getValue());
 										redirectItems.add(newItem);
 									}
 									redirectMan.store(redirectItems);
 									
 									ArrayList<DBObject> webpageItems = new ArrayList<>(data.urls().size());
 
 									for(String url : data.urls()) {
 										// store web content & header
 										BasicDBObject newWebPageItem = new BasicDBObject(3);
 										if(data.content(url)!=null) {
 											newWebPageItem.put("url", url);
 											newWebPageItem.put("content", data.content(url));
 										}
 										if(data.header(url)!=null) {
 											newWebPageItem.put("url", url);
											newWebPageItem.put("headers", new BasicDBObject(data.header(url)));
 										}
 										if(newWebPageItem.containsField("url")) {
 											webpageItems.add(newWebPageItem);
 										}
 									}
 									webpageSink.store(webpageItems);
 									
 									ArrayList<AlignmentCandidate> toBeReCrawled = new ArrayList<>(currentAlignments.size());
 									final int maxBulkSize = chunkSize*10;
 									// store alignments
 									ArrayList<DBObject> alignmentItems = new ArrayList<>(maxBulkSize);
 									for(AlignmentCandidate alignment : currentAlignments) {
 										HashSet<String> actualUrls = new HashSet<>(alignment.originalUrls().size());
 										ArrayList<String> toBeRetried = new ArrayList<>(alignment.originalUrls().size());
 										for(String origUrl : alignment.originalUrls()) {
 											if(cachedRedirects.containsKey(origUrl)) {
 												actualUrls.add(cachedRedirects.get(origUrl));
 											} else if(data.redirects().containsKey(origUrl)) {
 												actualUrls.add(data.redirects().get(origUrl));
 											} else if( r.nextDouble() < retryProbability ) {
 												// no redirects found! --> retry later (w/ specific retry probability)
 												toBeRetried.add(origUrl);
 											}
 										}
 										
 										if(toBeRetried.size()>0) {
 											toBeReCrawled.add(new AlignmentCandidate(alignment.tweetId(), toBeRetried, alignment.hashtags()));
 										}
 										
 										
 										for(String url : actualUrls) {
 											if(url!=null) {
 												for(String ht : alignment.hashtags()) {
 													DBObject newItem = new BasicDBObject(3);
 													newItem.put("hashtag", ht);
 													newItem.put("url", url);
 													newItem.put("tweet_id", alignment.tweetId());
 													alignmentItems.add(newItem);
 												}
 											}
 										}
 										if(alignmentItems.size()>maxBulkSize*.9) {
 											alignmentSink.store(alignmentItems);
 											alignmentItems = new ArrayList<>(maxBulkSize);
 										}
 									}
 									if(alignmentItems.size()>0) {
 										alignmentSink.store(alignmentItems);
 									}
 									
 									synchronized (retryAlignmentCandidates) {
 										retryAlignmentCandidates.addAll(toBeReCrawled);
 									}
 								} catch (Throwable t) {
 									t.printStackTrace();
 								}
 								
 								rpt.inc(currentAlignments.size());
 							}
 						});
 					}
 				} catch (IOException e1) {
 					e1.printStackTrace();
 					return;
 				}
 			}
 			
 			@Override
 			public boolean repeat() {
 				if(finished) {
 					rpt.finish();
 					crawler.close();
 					guard.close();
 					return false;
 				}
 				return true;
 			}
 		};
 		guard.add(task);
 		
 		return new Closeable() {
 			
 			@Override
 			public void close() throws IOException {
 				guard.close();
 				crawler.close();
 			}
 		};
 	}
 
 	private void addAlignmentTasks(final Queue<AlignmentCandidate> alignmentCandidates, Collection<AlignmentCandidate> pendingRetryCandidates, String... files) {
 		for(String file : files) {
 			System.out.print("parsing tweets of: ");
 			System.out.println(file);
 			TwitterDumpFileReader reader = new TwitterDumpFileReader(file).showProgress(false);
 			ArrayList<DBObject> tweetStack = new ArrayList<>(chunkSize*30);
 			
 			
 			for(DBObject tweet : reader) {
 				if(tweet.containsField("urls") && tweet.get("urls") instanceof List && tweet.containsField("hashtags") && tweet.get("hashtags") instanceof List) {
 					@SuppressWarnings("unchecked")
 					final List<String> listOfUrls = (List<String>)tweet.get("urls");
 					@SuppressWarnings("unchecked")
 					final List<String> listOfHashtags = (List<String>)tweet.get("hashtags");
 					
 					/* ignore spam tweets with particular hashtags
 					boolean spam = false;
 					for(String ht : listOfHashtags) {
 						if("gameinsight".equalsIgnoreCase(ht) || "nowplaying".equalsIgnoreCase(ht) || "listenlive".equalsIgnoreCase(ht)) {
 							spam = true;
 							break;
 						}
 					}
 					if(spam) {
 						continue;
 					}
 					//*/
 					
 					tweetStack.add(tweet);
 					if(tweetStack.size()>chunkSize*25) {
 						// persist tweets immediately
 						synchronized (tweetSink) {
 							tweetSink.store(tweetStack);
 							tweetStack.clear();
 						}
 					}
 					
 					if(listOfUrls.size()>0 && listOfHashtags.size()>0) {
 						synchronized (alignmentCandidates) {
 							alignmentCandidates.add(new AlignmentCandidate(tweet.get("tweet_id"), listOfUrls, listOfHashtags));
 						}
 
 						int pendingAlignmentCandidateCount;
 						do {
 							try {
 								synchronized (reader) {
 									reader.wait(200);
 								}
 							} catch (InterruptedException e) {
 								// ignore
 							}
 							synchronized (alignmentCandidates) {
 								pendingAlignmentCandidateCount = alignmentCandidates.size();
 							}
 							synchronized (pendingRetryCandidates) {
 								pendingAlignmentCandidateCount = pendingRetryCandidates.size();
 							}
 							// wait for the candidate count to be low enough (loop until this is the case)
 						} while(pendingAlignmentCandidateCount>chunkSize*10);
 					}
 				}
 			}
 
 			if(tweetStack.size()>0) {
 				// persist missing tweets
 				synchronized (tweetSink) {
 					tweetSink.store(tweetStack);
 					tweetStack.clear();
 				}
 			}
 		}
 		finished = true;
 	}
 	
 	static class AlignmentCandidate {
 		private final Object tweetId;
 		private final List<String> originalUrls;
 		private final List<String> hashtags;
 		public AlignmentCandidate(Object tweetId, List<String> originalUrls, List<String> hashtags) {
 			this.tweetId = tweetId;
 			this.originalUrls = originalUrls;
 			this.hashtags = hashtags;
 		}
 		public Object tweetId() {
 			return tweetId;
 		}
 		public List<String> originalUrls() {
 			return originalUrls;
 		}
 		public List<String> hashtags() {
 			return hashtags;
 		}
 	}
 }
