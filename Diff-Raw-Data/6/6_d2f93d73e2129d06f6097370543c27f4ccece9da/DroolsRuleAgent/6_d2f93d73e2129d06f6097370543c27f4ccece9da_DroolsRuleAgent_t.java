 package com.orangeleap.tangerine.service.rule;
 
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.drools.agent.RuleAgent;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Service;
 
 @Service("droolsRuleAgent")
 public class DroolsRuleAgent implements ApplicationContextAware {
 	private static final Log logger = LogFactory.getLog(DroolsRuleAgent.class);
 
 
 	private RuleAgent ruleAgent = null;
 	private ApplicationContext applicationContext;
 	
 	public static Properties getDroolsProperties() {
 		String host = System.getProperty("drools.host");
 		String port = System.getProperty("drools.port");
 //		String url = "http://"+host+":"+port+"/drools/org.drools.brms.JBRMS/package/com.mpower/NEWEST";
 //		logger.debug("Setting Drools URL to "+url);
 		Properties props = new Properties();
 //		props.put("url", url);
 		props.put("newInstance", "false");
 		props.put("name","orangeleap");
		props.put("poll", System.getProperty("drools.pollinterval"));
		props.put("cache", System.getProperty("drools.cachedir"));
		props.put("dir",System.getProperty("drools.packagedir"));
 		return props;
 	}
 
 	public DroolsRuleAgent() {
 		
 	}
 	
 	RuleAgent getRuleAgent()
 	{
 		if (ruleAgent == null)
 			ruleAgent = RuleAgent.newRuleAgent(getDroolsProperties());
 		return ruleAgent;
 	}
 	
 	void setRuleAgent(RuleAgent agent)
 	{
 		ruleAgent = agent;
 	}
 	
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext)
 			throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 
 }
