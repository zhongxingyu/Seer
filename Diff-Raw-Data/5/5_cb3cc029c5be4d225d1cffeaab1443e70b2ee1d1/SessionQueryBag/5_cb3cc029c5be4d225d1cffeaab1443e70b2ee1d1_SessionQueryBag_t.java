 package gov.nih.nci.rembrandt.web.bean;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.rembrandt.dto.query.ComparativeGenomicQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.GeneExpressionQuery;
 import gov.nih.nci.rembrandt.dto.query.Queriable;
 import gov.nih.nci.rembrandt.dto.query.Query;
 import gov.nih.nci.rembrandt.web.struts.form.ClinicalDataForm;
 import gov.nih.nci.rembrandt.web.struts.form.ComparativeGenomicForm;
 import gov.nih.nci.rembrandt.web.struts.form.GeneExpressionForm;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 
 /**
  * @author SahniH, BauerD, LandyR, RossoK 
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class SessionQueryBag implements Serializable,Cloneable {
 	private static transient Logger logger = Logger.getLogger(SessionQueryBag.class);
 	/*
 	 * queryMap is the current map of all queries the user has created, these
 	 * are not the compoundQueries and resultants that are stored in the cache.
 	 * These are the queries that the user creates and later "Refines" with 
 	 * other queries to generate a result set.  I know that this is confusing. 
 	 * Each query is stored where key=queryName and value=some Queriable object
 	 * that was created by the user in the build query pages.
 	 */
 	private Map<String, Query> queryMap = new TreeMap<String, Query>();
     //this map is strictly for queryDTOs
     private transient Map<String, QueryDTO> queryDTOMap = new TreeMap<String, QueryDTO>();    
 	//hold form beans
 	private Map<String,ActionForm> formBeanMap = new HashMap<String,ActionForm>();
 	
 	/* This is the current compound query that has been validated and is ready
 	 * to run...
 	 */
 	private transient CompoundQuery compoundQuery = null;
 	//private CompoundQuery compoundQuery = null;
 	
 	private Map<String, Queriable> compoundQueryMap = new TreeMap<String, Queriable>();
 	
 	public void putQuery(Query query, ActionForm form) {
 		if(queryMap == null){
     		queryMap = new TreeMap<String, Query>();   
     		logger.debug("queryMap was null");
     	}
    	if(formBeanMap == null){
     		formBeanMap = new HashMap<String,ActionForm>();
     		logger.debug("formBeanMap was null");
     	}
 		if (query != null && query.getQueryName() != null) {
 			queryMap.put(query.getQueryName(), query);
 			formBeanMap.put(query.getQueryName(), form);
 		}else{
 			logger.debug("Null pointer encountered in putQuery");
 		}
 	}
     public void putQueryDTO(QueryDTO queryDTO, ActionForm form) {
     	if(queryDTOMap == null){
     		queryDTOMap = new TreeMap<String, QueryDTO>();   
     		logger.debug("queryDTOMap was null");
     	}
    	if(formBeanMap == null){
     		formBeanMap = new HashMap<String,ActionForm>();
     		logger.debug("formBeanMap was null");
     	}
         if (queryDTO != null && queryDTO.getQueryName() != null) {
             queryDTOMap.put(queryDTO.getQueryName(), queryDTO);
             formBeanMap.put(queryDTO.getQueryName(), form);
         }
         else{
         	logger.debug("Null pointer encountered in putQueryDTO");
         }
     }
 
 	public Collection getQueries() {
 		return queryMap.values();
 	}
 
 	public Collection getQueryNames() {
 		return queryMap.keySet();
 	}
     public Collection getQueryDTOs() {
     	if(queryDTOMap != null){
     		return queryDTOMap.values();
     	}
     	return Collections.EMPTY_LIST;
     }
 
     public Collection getQueryDTONames() {
     	if(queryDTOMap != null){
     		return queryDTOMap.keySet();
     	}
     	return Collections.EMPTY_LIST;
     }
 	
 	public void putQuery(Query query) {
 	  queryMap.put(query.getQueryName(), query);
 	}
 
 	public void removeQuery(String queryName) {
 		if (queryName != null  && queryMap!= null  && formBeanMap != null) {
 			queryMap.remove(queryName);
 			formBeanMap.remove(queryName);
 		}else{
         	logger.debug("Null pointer encountered in removeQuery");
         }
 	}
 
 	public Query getQuery(String queryName) {
 		if (queryName != null) {
 			return (Query) queryMap.get(queryName);
 		}
 		return null;
 	}
 	
 	public Queriable getCompoundQuery(String queryName) {
 		if (queryName != null) {
 			return (Queriable) compoundQueryMap.get(queryName);
 		}
 		return null;
 	}
 	
 	public void putCompoundQuery(Queriable queriable) {
 		if(compoundQueryMap == null){
 			compoundQueryMap = new TreeMap<String, Queriable>();
     		logger.debug("compoundQueryMap was null");
     	}		
 		if(queriable != null && queriable.getQueryName() != null){
 			compoundQueryMap.put(queriable.getQueryName(), queriable);
 		}else{
         	logger.debug("Null pointer encountered in putCompountQuery");
         }
 	}
 
 	public void removeCompoundQuery(String queryName) {
 		if (queryName != null && compoundQueryMap != null) {
 			compoundQueryMap.remove(queryName);
 		}else{
         	logger.debug("Null pointer encountered in removeCompoundQuery");
         }
 	}
 
 	public Collection getCompoundQueryNames() {
 		return compoundQueryMap.keySet();
 	}
     public Collection getCompoundQueries() {
         return compoundQueryMap.values();
     }
     public QueryDTO getQueryDTO(String queryName) {
         if (queryName != null && queryDTOMap != null) {
             return (QueryDTO) queryDTOMap.get(queryName);
         }else{
         	logger.debug("Null pointer encountered in getQueryDTO");
         }
         return null;
     }
 
 	public void removeAllQueries() {
 		queryMap.clear();
 	}
 
 	/**
 	 * @return Returns the compoundQuery.
 	 */
 	public CompoundQuery getCompoundQuery() {
 		return this.compoundQuery;
 	}
 
 	/**
 	 * @param compoundQuery
 	 *            The compoundQuery to set.
 	 */
 	public void setCompoundQuery(CompoundQuery compoundQuery) {
 		this.compoundQuery = compoundQuery;
 	}
 
 	public boolean hasCompoundQuery() {
 		if (this.getCompoundQuery() != null)
 			return true;
 		return false;
 	}
 
 	public boolean hasQuery() {
 		return (!this.getQueryNames().isEmpty());
 	}
 	
 	/**
 	 * This method will return the latest group of all genes querries available.
 	 * It iterates through the current list of queries and checks for
 	 * isAllGenesQuery() and stores them in the Map if they are. There is no
 	 * setter for this property as it is only a subset of the current queries
 	 * stored in the session.
 	 * 
 	 * @return -- a current Map of all the All Genes Queries
 	 */
 	public Map getAllGenesQueries() {
 		//this map is generated from the queryMap, storing only the allGenesQueries
 		Map<String, Query> allGenesQueries = new HashMap<String, Query>();
 		Set keys = queryMap.keySet();
 		for(Iterator i = keys.iterator();i.hasNext();) {
 			Query query = (Query)queryMap.get(i.next());
 			boolean possibleAllGeneQuery = false;
 			if(query instanceof ComparativeGenomicQuery) {
 				ComparativeGenomicQuery cgQuery = (ComparativeGenomicQuery)query;
 				if(cgQuery.isAllGenesQuery()) {
 					allGenesQueries.put(cgQuery.getQueryName(),cgQuery);
 				}
 			}else if(query instanceof GeneExpressionQuery) {
 				GeneExpressionQuery geQuery = (GeneExpressionQuery)query;
 				if(geQuery.isAllGenesQuery()) {
 					allGenesQueries.put(geQuery.getQueryName(),geQuery);
 				}
 			}
 		}
 		return allGenesQueries;
 	}
 	/**
 	 * Creates a new Map that will contain all the current queries that are not
 	 * all gene queries.  This list is created dynamicly as the list of current
 	 * queries can change at any time and we do not want a reference to a non
 	 * existing query to show up when this method is called.
 	 * 
 	 * @return  -- a current Map of all non all genes queries.
 	 */
 	public Map getNonAllGeneQueries() {
 		//this map is generated from the queryMap, storing only the non-allGenesQueries
 		Map<String, Query> nonAllGeneQueries = new HashMap<String, Query>();
 		Set keys = queryMap.keySet();
 		for(Iterator i = keys.iterator();i.hasNext();) {
 			Query query = (Query)queryMap.get(i.next());
 			boolean possibleAllGeneQuery = false;
 			if(query instanceof ComparativeGenomicQuery) {
 				ComparativeGenomicQuery cgQuery = (ComparativeGenomicQuery)query;
 				if(!cgQuery.isAllGenesQuery()) {
 					nonAllGeneQueries.put(cgQuery.getQueryName(),cgQuery);
 				}
 			}else if(query instanceof GeneExpressionQuery) {
 				GeneExpressionQuery geQuery = (GeneExpressionQuery)query;
 				if(!geQuery.isAllGenesQuery()) {
 					nonAllGeneQueries.put(geQuery.getQueryName(),geQuery);
 				}
 			}else {
 				nonAllGeneQueries.put(query.getQueryName(), query);
 			}
 		}
 		return nonAllGeneQueries;
 		
 	}
 
     /**
      * @return Returns the formBeanMap.
      */
     public Map getFormBeanMap() {
         return formBeanMap;
     }
     /**
      * @param formBeanMap The formBeanMap to set.
      */
     public void setFormBeanMap(Map<String,ActionForm> formBeanMap) {
         this.formBeanMap = formBeanMap;
     }
     /**
 	 * Overrides the protected Object.clone() method exposing it as public.
 	 * It performs a 2 tier copy, that is, it does a memcopy of the instance
 	 * and then sets all the non-primitive data fields to clones of themselves.
 	 * 
 	 * @return -A minimum 2 deep copy of this object.
 	 */
 	public Object clone() {
 		SessionQueryBag myClone = null;
 		
 		myClone = new SessionQueryBag();
 		Map<String, Query> clonedQueryMap = null;
 	    if(queryMap != null){
 	    	clonedQueryMap = new TreeMap<String, Query>();
         	Set keys = queryMap.keySet(); 
     		for(Object elementKey: keys) {
     			Query it = queryMap.get(elementKey);
     			Query q = (Query)it;
     			Query itClone = (Query)q.clone();
     			clonedQueryMap.put((String)elementKey,itClone);
     		}
         }
 	    
 		Map<String, Queriable> clonedCompoundQueryMap = null;
 	    if(compoundQueryMap != null){
 	    	clonedCompoundQueryMap = new TreeMap<String, Queriable>();
         	Set keys = compoundQueryMap.keySet(); 
     		for(Object elementKey: keys) {
     			Queriable it = compoundQueryMap.get(elementKey);
     			Queriable q = (Queriable)it;
     			Queriable itClone = (Queriable)q.clone();
     			clonedCompoundQueryMap.put((String)elementKey,itClone);
     		}
         }
 	    
 	    myClone.queryMap = clonedQueryMap;
 	    Map<String,ActionForm> clonedformBeanMap = null;
 	    if(formBeanMap != null){
 	    	clonedformBeanMap = new HashMap<String,ActionForm>();
         	Set keys = formBeanMap.keySet(); 
     		for(Object elementKey: keys) {
     			ActionForm it = formBeanMap.get(elementKey);
     			ActionForm itClone = null;
     			if(it instanceof GeneExpressionForm) {
     				GeneExpressionForm gef = (GeneExpressionForm)it;
     				itClone = gef.cloneMe();
     			}else if(it instanceof ClinicalDataForm) {
     				ClinicalDataForm cdf = (ClinicalDataForm)it;
     				itClone = cdf.cloneMe();
     			}else if(it instanceof ComparativeGenomicForm) {
     				ComparativeGenomicForm cgf = (ComparativeGenomicForm)it;
     				itClone = cgf.cloneMe();
     			}else {
     				logger.error("Unsupported FormType to clone");
     			}
     			clonedformBeanMap.put((String)elementKey,itClone);
     		}
     		 myClone.formBeanMap = clonedformBeanMap;
         }
 		return myClone;
 	}
 
 	private Map cloneMap(Map thisQueryMap) {
 		HashMap myClone = new HashMap();
 		
 		return myClone;
 	}
 
 	class Handler {
 	}
 
 	public Map<String, Queriable> getCompoundQueryMap() {
 		return compoundQueryMap;
 	}
 	public void setCompoundQueryMap(Map<String, Queriable> compoundQueryMap) {
 		this.compoundQueryMap = compoundQueryMap;
 	}
  }
