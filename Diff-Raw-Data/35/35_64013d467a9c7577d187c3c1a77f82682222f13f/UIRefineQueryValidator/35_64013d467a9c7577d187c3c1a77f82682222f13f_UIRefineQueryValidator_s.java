 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.caintegrator.application.cache.PresentationTierCache;
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.Queriable;
 import gov.nih.nci.rembrandt.dto.query.Query;
 import gov.nih.nci.rembrandt.queryservice.validation.DataValidator;
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.SelectedQueryBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.struts.form.RefineQueryForm;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.util.LabelValueBean;
 /**
  * This class is intended to take the selected queries from the refine query
  * page...comments coming
  * @author BauerD
  * Mar 23, 2005
  *
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
 
 public class UIRefineQueryValidator {
 
 	private static Logger logger = Logger.getLogger(UIRefineQueryValidator.class);
 	private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
     private SampleBasedQueriesRetriever sampleBasedQueriesRetriever = new SampleBasedQueriesRetriever();
 	
 	public RefineQueryForm processCompoundQuery(ActionForm form,
                                                      HttpServletRequest request) 
                                                      throws Exception {
 		List selectedQueries = null;
 		RefineQueryForm refineQueryForm = (RefineQueryForm) form;
 		//this should turn off the run report buttons in the refine query page
 		refineQueryForm.setRunFlag("no");
 		//Clean out the old stored compound query from the bag, and form
 		CompoundQuery compoundQuery = null;
 		refineQueryForm.setQueryText("");
 		refineQueryForm.setCompoundViewColl(new ArrayList());
 		String sessionId = request.getSession().getId();
 		SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
 		//clears out the old compound query, in case something bombs
 		queryBag.setCompoundQuery(compoundQuery);
 		ActionErrors errors = new ActionErrors();
 		boolean isAllGenesQuery = refineQueryForm.isAllGenesQuery();
 		String selectedResultSet = refineQueryForm.getSelectedResultSet();
 		
 		/* CASE 1:
 		 * This is an "All Genes" query. Overwrite the selectedQueries list with
 		 * a list that only contains the selected all gene query
 		 */
 		if(isAllGenesQuery) {
 			try {
 				//Drop any of the selected queries from the ArrayList
 				refineQueryForm.setSelectedQueries(new ArrayList());
 				selectedQueries = getAllGenesQuery(refineQueryForm.getAllGeneQuery(), sessionId, selectedResultSet);
 			}catch(IllegalStateException ise) {
 				/*
 				 * This is thrown in the instance that the QueryBag gets hosed up.
 				 * Not really sure what to do in this case... so for now just log it
 				 */
 				logger.error(ise);
 				
 			}catch(OperationNotSupportedException onse) {
 				/*
 				 * This should get thrown in the instance that there is only an 
 				 * all genes query selected in the RefineQueryPage. There must
 				 * also be a result set specified... 
 				 */
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("gov.nih.nci.nautilus.ui.struts.action.refinequery.allgenequery"));
 			}
 		}else {
 			//Get the selected queries from the refineQueryForm
 			selectedQueries = refineQueryForm.getSelectedQueries();
 //			remove any incidental or incorrect selectedQueries from the List
 			selectedQueries = scrubTheLazyList(selectedQueries);
 		}
         
         /* CASE 1(step 2) & CASE 2:
          * This is called in the instance there is 1 query stored in the selectedQuery list.   
          * This can be either a selected "All Gene Query", or a regular query.
          */
         if(selectedQueries!=null&&selectedQueries.size()==1) {
            /*
             * Don't run if we have errors... this is important to do, because in the
             */
         	if(errors.isEmpty()) {
 	           SelectedQueryBean queryBean = (SelectedQueryBean)selectedQueries.get(0);
 	           if(!queryBean.getQueryName().equals("")&&!queryBean.getQueryName().equals(" ")) {
 		           	//They have chosen a query
 		           	String queryName = queryBean.getQueryName();
 		           	Queriable query = queryBag.getQuery(queryName);
 				  	compoundQuery = new CompoundQuery(query);
 	           }
 		   }else {
 		     //They have not chosen a query
 		     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("gov.nih.nci.nautilus.ui.struts.action.refinequery.no.query"));
 		    }
         }else if(selectedQueries!=null&&selectedQueries.size()>1) {
         	/* CASE 3:
              * There is more than 1 query selected
         	 */
            	Vector vectorOfTokens = new Vector();
             
             //Tokenize the selected queries
             for(int i = 0;i<selectedQueries.size(); i++)
             {
             	SelectedQueryBean query = (SelectedQueryBean)selectedQueries.get(i);
                 String leftParen = query.getLeftParen();
                 String queryName = query.getQueryName();
                 String rightParen = query.getRightParen();
                 String operatorType = query.getOperand();
                 
                 if (leftParen != null && leftParen.length() > 0) 
                        vectorOfTokens.add(leftParen);
                 if (query != null) 
                        vectorOfTokens.add(queryBag.getQuery(query.getQueryName()));
                 if (rightParen != null && rightParen.length() > 0) 
                        vectorOfTokens.add(rightParen);
                 if (operatorType != null && operatorType.length() > 0) 
                        vectorOfTokens.add(operatorType);
             }
             //Parse the selected queries
             try {
     			Parser queryParser = new Parser(vectorOfTokens);
     			queryParser.expression();
     			compoundQuery = queryParser.getCompoundQuery();
      			
     		}catch (Exception e) {
     			logger.debug("Error Parsing Query and/or creating Compound Query "+ e.getMessage());
                 errors.add(ActionErrors.GLOBAL_ERROR,new ActionError("gov.nih.nci.nautilus.ui.struts.action.refinequery.parse.error",e.getMessage()));
     			refineQueryForm.setErrors(errors);
     			logger.error(e);
     		}
         }
         /*
          * At this point we should have a CompoundQuery that contains 1 of 3
          * things. 1-A Single "AllGenesQuery" in the right child, 2-A Single
          * Query in the Right Child. 3-A CompoundQuery full of any number of
          * queries selected by the user in the RefineQuery page.
          */   
 		if(compoundQuery!=null) {
 			
 			
 			/*
 			 * This is where we will apply a result set of sample ids to the
 			 * CompoundQuery that was just created. In the instance that it is
 			 * an "AllGenes" query a result set is mandatory.  But we checked for
 			 * that earlier in the getAllGenesQuery() method, so no need to do
 			 * it here. But we do need to check to see if the user actually
 			 * has a result set they want us to apply.
 			 */
 		    
 			if(selectedResultSet!=null&&!"".equals(selectedResultSet)) {
 	        	//ClinicalDataQuery resultSetClinicalDataQuery = (ClinicalDataQuery)(sampleBasedQueriesRetriever.getQuery(sessionId, selectedResultSet));
 	        	/*
 	    		 * At this time there is only a single query in any result set.
 	    		 * So let me grab that single query out of the compound query 
 	    		 * and extract it's sampleCriteria to apply to the compoundQuery
 	    		 */
                // if(resultSetClinicalDataQuery!=null){
     	    		SampleCriteria sampleCrit = new SampleCriteria(); 
     	    			//resultSetClinicalDataQuery.getSampleIDCrit();
     	    		/*
     	    		 * But before we apply it, we need to make sure that we do not have
     	    		 * too many samples for an All Gene Query... currently that
     	    		 * number is a constant specified in the RembrandtConstants file
     	    		 * and is based on the largest disease sample group.  Later this
     	    		 * may be dynamically set with a count query against the
     	    		 * database.
     	    		 */
     	    		
     	    		//new custom list mgmt stuff
     	    		//get the name of the sample list we are using
     	    		UserListBeanHelper ul= new UserListBeanHelper(request.getSession());
     	    		UserList l = ul.getUserList(selectedResultSet);
     	    		List<SampleIDDE> sampleIDDEList = new ArrayList<SampleIDDE>();
     	    		Set<String> samples = null;
     	    		if(l!=null && l.getList()!=null)	{
     	    			//get the samples from that list
     	    			//create the sample crit
     	    			 samples = new HashSet<String>(l.getList());
     	    			//get the specimenNames associated with these samples
     	    			List<String> specimenNames = LookupManager.getSpecimenNames(samples);
     	   				//get the samples associated with these specimens
     	   				List<String> sampleIds = LookupManager.getSampleIDs(samples);
     	   				//Add back any samples that were just sampleIds to start with
     	   				if(sampleIds != null ){
     	   					samples.addAll(sampleIds);
     	   				}
     	   				if(specimenNames != null){
     	   					samples.addAll(specimenNames);
     	   				}
    	   				sampleIDDEList.addAll(ListConvertor.convertToSampleIDDEs(samples));
    	    			sampleCrit.setSampleIDs(sampleIDDEList);
     	    		}
     	    					     
     	    		if(isAllGenesQuery){
     	    			//Check for specimen_names for all genes Query
     	    			Collection<String> specimans =  DataValidator.validateSpecimanNames(samples);
     	    			if(specimans!= null && specimans.size()> RembrandtConstants.ALL_GENES_MAX_SAMPLE_COUNT){
     	    				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("gov.nih.nci.nautilus.ui.struts.action.refinequery.samplenumber.ofsamples",Integer.toString(RembrandtConstants.ALL_GENES_MAX_SAMPLE_COUNT)));
     	    			}else{
        	   				sampleIDDEList.addAll(ListConvertor.convertToSampleIDDEs(specimans));        	    			
     	    			}
     	    		
    		        	
    	    		}else {
     	    			sampleCrit.setSampleIDs(sampleIDDEList);
     	    			//drop the sample criteria into the compound query, clone it here
     		    		compoundQuery = (CompoundQuery)ReportGeneratorHelper.addSampleCriteriaToCompoundQuery((CompoundQuery)compoundQuery.clone(),sampleCrit, selectedResultSet);
    	    		}
                //}	
 	    	}
 			if(errors.isEmpty()) {
 				//store the sessionId that the compound query is associated with
 				compoundQuery.setSessionId(sessionId);
 				//Returned String representation of the final query
 				refineQueryForm.setQueryText(compoundQuery.toString());
 	            //We need to retrieve the list of available views
 	            List viewCollection = setRefineQueryView(compoundQuery, request);
 	            // Set collection of view types in Form
 	            refineQueryForm.setCompoundViewColl(viewCollection);
 	            //Stuff compoundquery in queryCollection
 	            queryBag.setCompoundQuery((CompoundQuery) compoundQuery);
 	            //This means show that Run Button as we have a query to run...
 	            refineQueryForm.setRunFlag("yes");
 			}
         }
 		
         refineQueryForm.setErrors(errors);
         return refineQueryForm;
  	}
 
 	public static List setRefineQueryView(CompoundQuery cQuery,
 			HttpServletRequest request) {
 		ArrayList queryViewColl = new ArrayList();
 		Properties props = new Properties();
 		props = ApplicationContext.getLabelProperties();
 
 		if (cQuery != null && props != null) {
 
 			ViewType[] availableViewTypes = cQuery.getValidViews();
 			//Set the View Types array in request to be used on return trip
 			//request.getSession().setAttribute(RembrandtConstants.VALID_QUERY_TYPES_KEY, availableViewTypes);
 
 			for (int viewIndex = 0; viewIndex < availableViewTypes.length; viewIndex++) {
 				ViewType thisViewType = (ViewType) availableViewTypes[viewIndex];
 				//per 1.5 reqt, we want to "comment out" the gene group view, possibly to re-add later
 				if(!thisViewType.equals(ViewType.GENE_GROUP_SAMPLE_VIEW))	{
 					String viewText = (String) props.get(thisViewType.getClass().getName());
 					queryViewColl.add(new LabelValueBean(viewText, Integer.toString(viewIndex)));
 				}
 			}
 		} else {
 			queryViewColl.add(new LabelValueBean(" ", " "));
 			logger.debug("Compound Query passed is null");
 		}
 		return queryViewColl;
 	}
 		
 	private List getAllGenesQuery(String allGeneQueryName, String sessionId, String selectedResultSet)throws IllegalStateException, OperationNotSupportedException {
 		SelectedQueryBean bean = null;
 		List queries = new ArrayList();
 		if(allGeneQueryName!=null&&!allGeneQueryName.equals("")) {
 			SessionQueryBag bag = presentationTierCache.getSessionQueryBag(sessionId);
 			Map allGenesQueries = bag.getAllGenesQueries();
 			if(selectedResultSet!=null&&!"".equals(selectedResultSet)) {
 				if(allGenesQueries!=null&&allGenesQueries.containsKey(allGeneQueryName)) {
 					bean = new SelectedQueryBean();
 					bean.setQueryName(allGeneQueryName);
 					bean.setAllGeneQuery(true);
 				}else {
 					throw new IllegalStateException("There isn't an All Genes Query by the name: "+allGeneQueryName);
 				}
 			}else {
 				throw new OperationNotSupportedException("No result set specified for the All Gene Query...this is required at this time");
 			}
 			/*
 			 * It is important to add the bean here, as it will not add the
 			 * query if the method needs to throw an exception.  This is
 			 * important as we do not want later logic to think that the query
 			 * is a good query..
 			 */
 			queries.add(bean);
 		}
 		return queries;
 	}
 	
 	private List scrubTheLazyList(List selectedQueries) {
 		if(selectedQueries!=null) {
 			Iterator iter = selectedQueries.iterator();
 			while(iter.hasNext()) {
 				SelectedQueryBean bean = (SelectedQueryBean)iter.next();
 				if(bean.getQueryName()==null||bean.getQueryName().equals("")) {
 					//drop the bean
 					selectedQueries.remove(bean);
 					//reset the iterator
 					iter = selectedQueries.iterator();
 				}
 			}
 		}
 		return selectedQueries;
 	}
     
   
 }
