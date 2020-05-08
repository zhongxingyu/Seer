 package com.gentics.cr;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class RequestProcessorMerger {
 	
 	/**
 	 * Set this property of the request to true in order to use the beans of the secondary request processor
 	 */
 	public static final String USE_SECONDARY_KEY="secondary";
 	
 	/**
 	 * Merge the results of two request processor. The primary RequestProcessor overrules the secondary RP. The request is made using the CRRequest
 	 * @param uniquemergeattribute
 	 * @param primaryRP
 	 * @param secondaryRP
 	 * @param request
 	 * @return
 	 * @throws CRException
 	 */
 	public static Collection<CRResolvableBean> merge(String uniquemergeattribute,RequestProcessor primaryRP, RequestProcessor secondaryRP, CRRequest request) throws CRException
 	{
 		
 		Collection<CRResolvableBean> rp1res = primaryRP.getObjects(request);
 		
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		
 		LinkedHashMap<Object,CRResolvableBean> resultMap = new LinkedHashMap<Object,CRResolvableBean>();
 		LinkedHashMap<Object,CRResolvableBean> resultMap2 = new LinkedHashMap<Object,CRResolvableBean>();
 		
 		String mergefilter="";
 		
 		boolean first=true;
 		
 		for(CRResolvableBean crBean:rp1res)
 		{
 			Object id = crBean.get(uniquemergeattribute);
 			if(first)
 			{
 				first=false;
 				mergefilter+="\""+id+"\"";
 			}
 			else
 			{
 				mergefilter+=","+"\""+id+"\"";
 			}
 			resultMap.put(id, crBean);
 		}
 		rp1res=null;
		CRRequest request2 = new CRRequest();
		request2.setAttributeArray(request.getAttributeArray());
		request2.setDoReplacePlinks(request.getDoReplacePlinks());
		request2.setDoVelocity(request.getDoVelocity());
		request2.setRequestFilter("object."+uniquemergeattribute+" CONTAINSONEOF ["+mergefilter+"]");
		Collection<CRResolvableBean> rp2res = secondaryRP.getObjects(request2);
 		String[] attributes = request.getAttributeArray();
 		
 		for(CRResolvableBean crBean:rp2res)
 		{
 			Object id = crBean.get(uniquemergeattribute);
 			resultMap2.put(id, crBean);
 		}
 		
 		String SecMerge = (String)request.get("secondary");
 		if(Boolean.parseBoolean(SecMerge))
 		{
 			//Resolvable from second RP will be used
 			useSecondaryMerge(result, resultMap, resultMap2, attributes);
 		}
 		else
 		{
 			//Resolvable from first RP will be used
 			useFirstMerge(result, resultMap, resultMap2, attributes);
 		}
 		
 		return(result);
 	}
 
 	/**
 	 * Use beans of first RP and put attributes of secondary RP int beans of first RP
 	 * @param result
 	 * @param resultMap
 	 * @param resultMap2
 	 * @param attributes
 	 */
 	private static void useFirstMerge(ArrayList<CRResolvableBean> result,
 			LinkedHashMap<Object, CRResolvableBean> resultMap,
 			LinkedHashMap<Object, CRResolvableBean> resultMap2,
 			String[] attributes) {
 		
 		for(Entry<Object,CRResolvableBean> e:resultMap.entrySet())
 		{
 			
 			Object id = e.getKey();
 			CRResolvableBean resultBean2 = resultMap2.remove(id);
 			CRResolvableBean resultBean = e.getValue();
 			//MERGE ATTRIBUTES
 			if(resultBean!=null && resultBean2!=null)
 			{
 				for(String attribute:attributes)
 				{
 					Object value = resultBean2.get(attribute);
 					if(value!=null)
 					{
 						resultBean.set(attribute, value);
 					}
 				}
 				result.add(resultBean);
 			}
 			else if(resultBean2==null && resultBean!=null)
 			{
 				result.add(resultBean);
 			}
 		}
 	}
 	
 	/**
 	 * Use beans of secondary RP and put attributes from first RP into beans of secondary RP
 	 * @param result
 	 * @param resultMap
 	 * @param resultMap2
 	 * @param attributes
 	 */
 	private static void useSecondaryMerge(ArrayList<CRResolvableBean> result,
 			LinkedHashMap<Object, CRResolvableBean> resultMap,
 			LinkedHashMap<Object, CRResolvableBean> resultMap2,
 			String[] attributes) {
 		
 		for(Entry<Object,CRResolvableBean> e:resultMap.entrySet())
 		{
 			
 			Object id = e.getKey();
 			CRResolvableBean resultBean2 = resultMap2.remove(id);
 			CRResolvableBean resultBean = e.getValue();
 			//MERGE ATTRIBUTES
 			if(resultBean!=null && resultBean2!=null)
 			{
 				for(String attribute:attributes)
 				{
 					Object value = resultBean.get(attribute);
 					if(value!=null)
 					{
 						resultBean2.set(attribute, value);
 					}
 				}
 				result.add(resultBean2);
 			}
 			else if(resultBean2==null && resultBean!=null)
 			{
 				result.add(resultBean);
 			}
 		}
 	}
 
 }
