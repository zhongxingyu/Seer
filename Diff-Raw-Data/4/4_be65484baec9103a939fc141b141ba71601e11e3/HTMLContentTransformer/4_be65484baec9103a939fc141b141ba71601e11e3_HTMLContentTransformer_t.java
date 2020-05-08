 package com.gentics.cr.lucene.indexer.transformer.html;
 import java.io.StringReader;
 
 import com.gentics.api.portalnode.connector.PortalConnectorHelper;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 import com.gentics.cr.plink.PLinkStripper;
 import com.gentics.cr.util.CRUtil;
 
 
 /**
  * 
  * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
  * @version $Revision: 99 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class HTMLContentTransformer extends ContentTransformer{
 
	private static final PLinkStripper stripper = new PLinkStripper();;
 	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
 	private String attribute="";
 	
 	/**
 	 * Get new instance of HTMLContentTransformer
 	 * @param config
 	 */
 	public HTMLContentTransformer(GenericConfiguration config)
 	{
 		super(config);
 		attribute = (String)config.get(TRANSFORMER_ATTRIBUTE_KEY);
 	}
 	
 	/**
 	 * Converts a string containing html to a String that does not contain html tags can be indexed by lucene
 	 * @param obj
 	 * @return
 	 */
 	private String getStringContents(Object obj)throws CRException
 	{
 		String ret = null;
 		HTMLStripReader sr = getContents(obj);
 		try
 		{
 			if(sr!=null)
 			{
 				ret = CRUtil.readerToPrintableCharString(sr);
 			}
 			sr.close();
 		}catch(Exception ex)
 		{
 			throw new CRException(ex);
 		}
 		return(ret);
 	}
 	/**
 	 * Converts a object containing html to a String that does not contain html tags can be indexed by lucene
 	 * @param obj
 	 * @return HTMLStripReader of contents
 	 */
 	private HTMLStripReader getContents(Object obj)
 	{
 		String contents = null;
 		if(obj instanceof String)
 		{
 			contents = PortalConnectorHelper.replacePLinks((String)obj,stripper);
 		}
 		else
 		{
 			throw new IllegalArgumentException();
 		}
 		return new HTMLStripReader(new StringReader(contents));
     }
 
 	@Override
 	public void processBean(CRResolvableBean bean)throws CRException {
 		if(this.attribute!=null)
 		{
 			Object obj = bean.get(this.attribute);
 			if(obj!=null)
 			{
 				String newString = getStringContents(obj);
 				if(newString!=null)
 				{
 					bean.set(this.attribute, newString);
 				}
 			}
 		}
 		else
 		{
 			log.error("Configured attribute is null. Bean will not be processed");
 		}
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 		
 	}
 }
