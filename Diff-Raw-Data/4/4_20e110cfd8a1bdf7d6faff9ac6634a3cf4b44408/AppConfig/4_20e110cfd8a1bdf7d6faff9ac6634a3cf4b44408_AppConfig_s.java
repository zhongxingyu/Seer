 package com.aksimata.pilot;
 
 import java.util.List;
 
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.soluvas.commons.tenant.TenantRef;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Lazy;
 import org.springframework.context.annotation.Scope;
 
 @Configuration
 @ComponentScan("com.aksimata.pilot")
 @Lazy
 public class AppConfig {
 	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
 	
 	@Bean @Scope("request")
 	public TenantRef tenant() {
 		final Url url = RequestCycle.get().getRequest().getUrl();
 		final List<String> segments = url.getSegments();
 		log.info("segments {}", segments);
 		if (segments.size() >= 3 && segments.get(0).equals("t")) {
			return new TenantRef(segments.get(1), segments.get(1), segments.get(2));
 		} else {
 			log.warn("{} is not multitenant URI", url);
 			return null;
 //			return new TenantRef("aaa", "bbb", "cc");
 		}
 	}
 
 }
