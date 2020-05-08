 package br.ufmg.dcc.vod.spiderpig.jobs.twitter.maxidbased;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
 import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
 import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
 import br.ufmg.dcc.vod.spiderpig.master.walker.ConfigurableWalker;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.common.collect.Lists;
 
 public class Walker implements ConfigurableWalker {
 
     private static final ExhaustCondition CONDITION = new ExhaustCondition();
 
     private static final Logger LOG = Logger.getLogger(Walker.class);
     
     @Override
     public Set<String> getRequiredParameters() {
         return new HashSet<>();
     }
 
     @Override
     public Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds) {
         return seeds;
     }
 
     @Override
     public Iterable<CrawlID> walk(CrawlID id, Iterable<CrawlID> links) {
     	if (links.iterator().hasNext()) {
     		long nextMaxId = Long.parseLong(links.iterator().next().getId());
     		
     		String[] split = id.getId().split("\t");
    		long currMaxId;
            String hashtag;
            if (split.length == 2) {
            	currMaxId = Long.parseLong(split[0]);
                hashtag = split[1];
            } else {
            	currMaxId = Long.MAX_VALUE;
            	hashtag = id.getId();
            }
             
            LOG.info("Received results for " + hashtag + " max " + currMaxId);
    		LOG.info("Will begin next query at " + nextMaxId);
     		
     		if (currMaxId == nextMaxId) {
    			LOG.info("Nothing more to do for (curr == next) " + hashtag);
                 return Collections.emptyList();
     		} else {
     			String crawlString = nextMaxId + "\t" + hashtag;
     			CrawlID nextToCrawl =
     					CrawlID.newBuilder().setId(crawlString).build();
     			return Lists.newArrayList(nextToCrawl);
     		}
     	} else {
    		LOG.info("Nothing more to do for (no result) " + id);
     		return Collections.emptyList();
     	}
     }
 
     @Override
     public void errorReceived(CrawlID crawled) {
     }
 
     @Override
     public StopCondition getStopCondition() {
         return CONDITION;
     }
 
     @Override
     public void configurate(Configuration configuration, 
             ConfigurableBuilder builder) throws BuildException {
     }
 }
