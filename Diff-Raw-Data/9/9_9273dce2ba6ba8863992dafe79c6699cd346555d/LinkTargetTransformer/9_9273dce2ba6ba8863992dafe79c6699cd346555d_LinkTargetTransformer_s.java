 package com.gentics.cr.lucene.indexer.transformer;
 
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 /**
  * 
  * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
  * @version $Revision: 99 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class LinkTargetTransformer extends ContentTransformer {
 	private static Logger log = Logger.getLogger(LinkTargetTransformer.class);
 	private static final String ATTRIBUTE_KEY="attribute";
 		
 	private static final String EXTERNALTARGET_KEY="externaltarget";
 	private static final String EXTERNAL_TITLE_KEY="externaltitle";
 	
 	private String attribute="content";
 	Pattern targetlinkResolverPattern = Pattern.compile("\\<a [^>]*(href)\\=\"([^\"]+)\"[^>]*>");
 	
 	GenericConfiguration config;
 	String externaltitle = null;
 	String externaltarget = null;
 	/**
 	 * Create new Instance
 	 * @param config
 	 */
 	public LinkTargetTransformer(GenericConfiguration config) {
 		super(config);
 		this.config=config;
 		String att_string = config.getString(ATTRIBUTE_KEY);
 		if(att_string != null)
 		{
 			this.attribute = att_string;
 		}
 		externaltarget = this.config.getString(EXTERNALTARGET_KEY);
 		externaltitle = this.config.getString(EXTERNAL_TITLE_KEY);
		
		
 	}
 
 	@Override
 	public void processBean(CRResolvableBean bean) throws CRException {
 		String content = (String)bean.get(this.attribute);
 		if(content!=null)
 		{
 			// starttime
 			long s = new Date().getTime();
 			String replacement = processTarget(content);
 			bean.set(this.attribute, replacement);
 			long e = new Date().getTime();
 			log.debug("Resolving static URLs took " + (e - s)+"ms");
 		}
 	}
 	
 	
 	
 	private String processTarget(String content) {
 		  Matcher matcher = targetlinkResolverPattern.matcher(content);
 		  StringBuffer buf = new StringBuffer();
 		  while (matcher.find()) {
 			  
 			  String whole = matcher.group(0);
 			  String newlink = whole;
 			  if(whole.indexOf("http")>=0)
 			  {
 				  //IS EXTERNAL
 				  if(whole.indexOf("target=\"")==-1)
 				  {
 					  //DOES NOT HAVE TARGET
 					  String replacement = " target=\""+this.externaltarget+"\"";
 					  if(whole.indexOf("title=\"")==-1)
 					  {
 						  //DOES NOT HAVE TITLE
 						  replacement+=" title=\""+this.externaltitle+"\"";
 					  }
 					  String rep = newlink.substring(0, newlink.length()-1);
 					  newlink = rep+replacement+">";
 				  }
 			  }
 			  //REAPPEND FINISHED LINK
 			  //we need to escape $ as this kills the expression
 			  matcher.appendReplacement(buf, newlink.replace("$", "\\$"));
 		  }
 		  matcher.appendTail(buf);
 		  return buf.toString();
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
