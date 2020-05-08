 package com.gentics.cr.lucene.indexer.transformer;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.util.CRUtil;
 
 /**
  * Substrings the indexed content (everything before a given start index pattern
  *  will be removed).
  * 
  * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
  * @version $Revision: 99 $
  * @author $Author: andrea.schauerhuber@gmail.com $
  *
  * Default configuration for indexer:
  * #SUBSTRINGER
  * # for recognizing any code in implementation before a specific substring 
  * (defaults to <!DOCTYPE declaration) 
  * index.AWO.CR.PAGES.transformer.0.attribute=content
  * index.AWO.CR.PAGES.transformer.0.rule=object.obj_type==10007
  * index.AWO.CR.PAGES.transformer.0.transformerclass=
  * com.gentics.cr.lucene.indexer.transformer.SubstringTransformer
  * # startindexpattern 
  * index.AWO.CR.PAGES.transformer.0.startindexpattern=<!DOCTYPE
  *
  */
 public class SubstringTransformer extends ContentTransformer {
 	/**
 	 * default preview length.
 	 */
 	private static final int DEFAULT_PREVIEW_LENGTH = 10;
 	/**
 	 * attribute key.
 	 */
 	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
 	/**
 	 * start pattern.
 	 */
 	private static final String SUBSTRING_START_INDEX_PATTERN_KEY = "startindexpattern";
 	/**
 	 * attribute.
 	 */
 	private String attribute = "";
 	/**
 	 * default pattern.
 	 */
 	private String startindexpattern = "<!DOCTYPE";
 
 	/**
 	 * Create Instance of SubstringTransformer.
 	 * if the startindexpattern is not configured in the config: 
 	 * the default pattern "<!DOCTYPE" will be used
 	 * @param config configuration
 	 */
 	public SubstringTransformer(final GenericConfiguration config) {
 		super(config);
 		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
 		String pt = (String) config.get(SUBSTRING_START_INDEX_PATTERN_KEY);
 		if (pt != null) {
 			startindexpattern = pt;
 		}
 	}
 
 	/**
 	 * @param bean bean
 	 */
 	@Override
 	public final void processBean(final CRResolvableBean bean) {
 		if (this.attribute != null) {
 			Object obj = bean.get(this.attribute);
 			System.out.println("Indexing Contentid:" + bean.get("contentid"));
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
 
 	/**
 	 * get contents.
 	 * @param obj obj
 	 * @return contents of obj
 	 */
 	private String getStringContents(final Object obj) {
		String str = null;
 		if (obj instanceof String) {
 			str = (String) obj;
 		} else if (obj instanceof byte[]) {
 			try {
 				str = CRUtil.readerToString(new InputStreamReader(new ByteArrayInputStream((byte[]) obj)));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// Replace all occurrences of pattern in input
 		int startindex = str.indexOf(this.startindexpattern);
 		if (startindex < 0) {
 			log.debug("Pattern not found");
 			return null;
 		}
 		log.debug("StartIndex of pattern '" + this.startindexpattern + "':" + startindex);
 		String resultstring = str.substring(startindex);
 		log.debug("Original String:" + str.substring(0, DEFAULT_PREVIEW_LENGTH) + "...");
 		log.debug("New String:" + resultstring.substring(0, DEFAULT_PREVIEW_LENGTH) + "...");
 
 		return resultstring;
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
