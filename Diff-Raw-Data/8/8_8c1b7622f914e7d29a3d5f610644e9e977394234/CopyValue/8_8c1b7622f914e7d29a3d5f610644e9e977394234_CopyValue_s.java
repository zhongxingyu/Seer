 package com.gentics.cr.lucene.indexer.transformer.other;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 /**
  * Sets a configured value
  * @author Andreas Perhab
  *
  */
 public class CopyValue extends ContentTransformer {
 
 	private static final String TRANSFORMER_SOUCREATTRIBUTE_KEY="sourceattribute";
 	private static final String TRANSFORMER_TARGETATTRIBUTE_KEY="targetattribute";
 	private String source_attribute;
 	private String target_attribute;
 	
 	/**
 	 * Creates instance of SetValueTransformer
 	 * @param config
 	 */
 	public CopyValue(GenericConfiguration config){
 		super(config);
 		source_attribute = (String)config.get(TRANSFORMER_SOUCREATTRIBUTE_KEY);
 		target_attribute = (String)config.get(TRANSFORMER_TARGETATTRIBUTE_KEY);
 	}
 	
 	@Override
 	public void processBean(CRResolvableBean bean) {
 		bean.set(target_attribute, bean.get(source_attribute));
 	}
 
 }
