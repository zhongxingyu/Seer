 package com.gentics.cr.lucene.search;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.search.Query;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRException;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.search.highlight.ContentHighlighter;
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class LuceneRequestProcessor extends RequestProcessor {
 
 	protected static Logger log = Logger.getLogger(LuceneRequestProcessor.class);
 	protected static Logger ext_log = Logger.getLogger(LuceneRequestProcessor.class);
 	private CRSearcher searcher = null;
 	protected String name=null;
 	
 	private boolean getStoredAttributes = false;
 	
 	private static final String SCORE_ATTRIBUTE_KEY = "SCOREATTRIBUTE";
 	private static final String GET_STORED_ATTRIBUTE_KEY = "GETSTOREDATTRIBUTES";
 	
 	private Hashtable<String,ContentHighlighter> highlighters;
 	
 	
 	/**
 	 * Create new instance of LuceneRequestProcessor
 	 * @param config
 	 * @throws CRException
 	 */
 	public LuceneRequestProcessor(CRConfig config) throws CRException {
 		super(config);
 		this.name=config.getName();
 		this.searcher = new CRSearcher(config);
 		getStoredAttributes = Boolean.parseBoolean((String)config.get(GET_STORED_ATTRIBUTE_KEY));
 		highlighters = ContentHighlighter.getTransformerTable((GenericConfiguration)config);
 	}
 	
 	private static final String SEARCH_COUNT_KEY = "SEARCHCOUNT";
 	
 	private static final String ID_ATTRIBUTE_KEY = "idAttribute";
 	
 	@SuppressWarnings("unchecked")
 	private static List<Field> toFieldList(List l)
 	{
 		return((List<Field>) l);
 	}
 	
 	/**
 	 * This returns a collection of CRResolvableBeans containing the IDATTRIBUTE and all STORED ATTRIBUTES of the Lucene Documents
 	 * 
 	 * @param request - CRRequest containing the query in RequestFilter
 	 * @param doNavigation - if set to true there will be generated explanation output to the explanation logger of CRSearcher
 	 * @return search result as Collection of CRResolvableBean
 	 * @throws CRException 
 	 */
 	public Collection<CRResolvableBean> getObjects(CRRequest request,
 			boolean doNavigation) throws CRException {
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		int count = request.getCount();
 		//IF COUNT IS NOT SET IN THE REQUEST, USE DEFAULT VALUE LOADED FROM CONFIG
 		if(count<=0)
		{	
			String cstring = (String)this.config.get(SEARCH_COUNT_KEY);
			if(cstring!=null)count=new Integer(cstring);
		}
 		if(count<=0)
			log.error("DEFAULT COUNT IS LOWER OR EQUAL TO 0! THIS WILL RESULT IN AN ERROR. OVERTHINK YOUR CONFIG (insert rp.<rpnumber>.searchcount=<value> int your properties file)!");
 		
 		
 		String scoreAttribute = (String)config.get(SCORE_ATTRIBUTE_KEY);
 		//GET RESULT
 		HashMap<String,Object> searchResult = this.searcher.search(request.getRequestFilter(),getSearchedAttributes(),count,doNavigation);
 		LinkedHashMap<Document,Float> docs = objectToLinkedHashMapDocuments(searchResult.get("result"));
 		Query parsedQuery = (Query)searchResult.get("query");
 		if(docs!=null)
 		{
 			String idAttribute = (String)this.config.get(ID_ATTRIBUTE_KEY);
 			for(Entry<Document,Float> e:docs.entrySet())
 			{
 				Document doc = e.getKey();
 				Float score = e.getValue();
 				CRResolvableBean crBean = new CRResolvableBean(doc.get(idAttribute));
 				if(getStoredAttributes)
 				{
 					for(Field f:toFieldList(doc.getFields()))
 					{
 						if(f.isStored())
 						{
 							if(f.isBinary())
 							{
 								crBean.set(f.name(), f.getBinaryValue());
 							}
 							else
 							{
 								crBean.set(f.name(), f.stringValue());
 							}
 						}
 					}
 				}
 				
 				if(scoreAttribute!=null && !"".equals(scoreAttribute))
 				{
 					crBean.set(scoreAttribute, score);
 				}
 				//IF HIGHLIGHTERS ARE CONFIGURED => DO HIGHLIGHTNING
 				if(highlighters!=null)
 				{
 					for(Entry<String,ContentHighlighter> ch:highlighters.entrySet())
 					{
 						ContentHighlighter h = ch.getValue();
 						String att = ch.getKey();
 						//IF crBean matches the highlighters rule => highlight
 						if(h.match(crBean))
 						{ 
 							String ret = h.highlight((String)crBean.get(att), parsedQuery);
 							crBean.set(att,ret);
 						}
 					}
 				}
 				
 				ext_log.debug("Found "+crBean.getContentid()+" with score "+score.toString());
 				result.add(crBean);
 			}
 		}
 		/*if(doNavigation)
 		{
 			//NOT IMPLEMENTED YET, BUT WE DO GENERATE MORE EXPLANATION OUTPUT YET
 			//log.error("LUCENEREQUESTPROCESSER CAN NOT YET RETURN A TREE STRUCTURE");
 		}*/
 		return result;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	private LinkedHashMap<Document,Float> objectToLinkedHashMapDocuments(Object obj)
 	{
 		return((LinkedHashMap<Document,Float>) obj);
 	}
 	
 	private static final String SEARCHED_ATTRIBUTES_KEY = "searchedAttributes";
 	
 	private String[] getSearchedAttributes()
 	{
 		String sa = (String)this.config.get(SEARCHED_ATTRIBUTES_KEY);
 		String[] ret=null;
 		if(sa!=null)
 		{
 			ret = sa.split(",");
 		}
 		return ret;
 	}
 
 }
