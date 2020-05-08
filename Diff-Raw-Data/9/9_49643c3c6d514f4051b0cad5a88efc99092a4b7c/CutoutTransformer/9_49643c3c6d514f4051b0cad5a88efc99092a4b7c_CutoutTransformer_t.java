 package com.gentics.cr.lucene.indexer.transformer;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.util.CRUtil;
 
 /**
  * Regex Replacing within the content can be quite costful in terms of performance.
  * 
  * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
  * @version $Revision: 99 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CutoutTransformer extends ContentTransformer {
 	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
 	private static final String START_PATTERN_KEY = "startpattern";
 	private static final String END_PATTERN_KEY = "endpattern";
 
 	private String attribute = "";
 	private String startpattern = "";
 	private String endpattern = "";
 
 	/**
 	 * Create Instance of CommentSectionStripper
 	 *  @param config
 	 */
 	public CutoutTransformer(GenericConfiguration config) {
 		super(config);
 		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
 		String spt = (String) config.get(START_PATTERN_KEY);
 		if (spt != null)
 			startpattern = spt;
 
 		String ept = (String) config.get(END_PATTERN_KEY);
 		if (ept != null)
 			endpattern = ept;
 
 	}
 
 	@Override
 	public void processBean(CRResolvableBean bean) {
 		if (this.attribute != null) {
 			Object obj = bean.get(this.attribute);
 			if (obj != null) {
 				String newString = getStringContents(obj);
 				if (newString != null) {
 					bean.set(this.attribute, newString);
 				}
 			}
 		} else {
 			log.error("Configured attribute is null. Bean will not be processed");
 		}
 
 	}
 
 	private String getStringContents(Object obj) {
		String str = "";
 		if (obj instanceof String) {
 			str = (String) obj;
 		} else if (obj instanceof byte[]) {
 			try {
 				str = CRUtil.readerToString(new InputStreamReader(new ByteArrayInputStream((byte[]) obj)));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		int sindex = str.indexOf(this.startpattern);
 		int eindex = str.indexOf(this.endpattern);
 
 		String newString = str;
 
 		if (sindex != -1 && eindex != -1 && sindex < eindex) {
 			newString = str.substring(sindex, eindex);
 		}
 		return newString;
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
