 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ReportBean;
 import gov.nih.nci.rembrandt.web.bean.SelectedQueryBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.ReportGeneratorHelper;
 import gov.nih.nci.rembrandt.web.helper.UIRefineQueryValidator;
 import gov.nih.nci.rembrandt.web.struts.form.RefineQueryForm;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.caintegrator.dto.de.InstitutionDE;
 import gov.nih.nci.caintegrator.dto.critieria.InstitutionCriteria;
 import gov.nih.nci.rembrandt.dto.query.Query;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.LookupDispatchAction;
 /**
  * This action is associated with the refine_tile.jsp tile and is mapped
  * for buttons on the page.  This is basicly the UI mechanism for creating
  * and running a compound query.
  * 
  * @author BauerD
  * Feb 15, 2005
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
 
 public class RefineQueryAction extends LookupDispatchAction {
     private static Logger logger = Logger.getLogger(RefineQueryAction.class);
 	private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
 	/**
     *  Responsible for looking at the user constructed compound query from the
     *  refine_tile.jsp.  If the Selected queries and operations are correct
     *  than it will create a compound query that should be executed
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
 	public ActionForward validateQuery(
 		ActionMapping mapping,
 		ActionForm form,
 		HttpServletRequest request,
 		HttpServletResponse response)
 		throws Exception {
 		String sessionId = request.getSession().getId();
         RefineQueryForm refineQueryForm = (RefineQueryForm) form;
 		ActionErrors errors = new ActionErrors();
         SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
         UIRefineQueryValidator myValidator = new UIRefineQueryValidator();
         refineQueryForm = myValidator.processCompoundQuery(form, request);
         if(refineQueryForm.getErrors()!=null&&!refineQueryForm.getErrors().isEmpty()) {
             //Processing returned some errors
         	saveErrors(request, refineQueryForm.getErrors());
             return (new ActionForward(mapping.getInput()));
          } 
          return mapping.findForward("displayQuery");
        
      }
 	/**
 	 * Makes the necessary calls to run a compound query, then forwards the 
 	 * request to the report rendering mechanism.
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward runReport(
 		ActionMapping mapping,
 		ActionForm form,
 		HttpServletRequest request,
 		HttpServletResponse response)
 		throws Exception {
 		//Get the sessionId
 		String sessionId = request.getSession().getId();
         ActionErrors errors = new ActionErrors();
         ActionForward thisForward = null;
 		RefineQueryForm refineQueryForm = (RefineQueryForm) form;
 		//Get the SessionQueryBag for this request and session
 		SessionQueryBag queryBag = presentationTierCache.getSessionQueryBag(sessionId);
 		// Get the viewType array from session 
 		ViewType [] availableViewTypes = null; //(ViewType []) request.getSession().getAttribute(RembrandtConstants.VALID_QUERY_TYPES_KEY);
 		//Make sure that we have a validated CompoundQuery to use
 		if (queryBag.hasCompoundQuery()) {
 			//Create compound query to execute
 			CompoundQuery cQuery = queryBag.getCompoundQuery();
 			availableViewTypes = cQuery.getValidViews();
 			ViewType selectView = availableViewTypes[Integer.parseInt(refineQueryForm.getCompoundView())];
 			cQuery.setAssociatedView(ViewFactory.newView(selectView));
            	//ReportGeneratorHelper will execute the query if necesary, or will
 			//retrieve from cache.  It will then generate the XML for the report
 			//and store in a reportBean in the cache for later retrieval
 			
 			//Processing institute criteria information.
 			String[] institutes = refineQueryForm.getInstituteView();
 			InstitutionCriteria ic = new InstitutionCriteria();
 			if (institutes != null && institutes.length > 0){
 					Collection<InstitutionDE> collection = InsitutionAccessHelper.getInsititutionCollectionWithDisplayNames(request.getSession());
 				
 						//set only the selected insitutions
 						for (int i = 0; i < institutes.length; i++){
 							for (Iterator it = collection.iterator(); it.hasNext();){
 								InstitutionDE de = (InstitutionDE)it.next();
 								if (institutes[i].equals(de.getDisplayName())){
 									ic.setInsitution(de);
 								}
 								else if(institutes[i].equals("ALL")){
 									//	if one of the selections is "ALL" then set all instututions the use has access to
 									ic.setInstitutions(InsitutionAccessHelper.getInsititutionCollection(request.getSession()));
 									break;
 								}
 						}
 					}
 					cQuery.setInstitutionCriteria(ic);	
 
 			}
 			else{
 				ic.setInstitutions(InsitutionAccessHelper.getInsititutionCollection(request.getSession()));
 				cQuery.setInstitutionCriteria(ic);	
 			}
 			//After setting everything, put this compound query into the SessionQueryBag
 			//serialize and later execution.
             queryBag.putCompoundQuery(cQuery);
             presentationTierCache.putSessionQueryBag(sessionId, queryBag);
             
 			ReportGeneratorHelper rgHelper = new ReportGeneratorHelper(cQuery, new HashMap());
 			ReportBean reportBean = rgHelper.getReportBean();
 			request.setAttribute("queryName", reportBean.getResultantCacheKey());
 			//Send to the appropriate view as per selection!!
 			thisForward = new ActionForward();
			//Encoding the query name to mask those URL sensitive characters
			String queryName = reportBean.getResultantCacheKey();
			queryName = URLEncoder.encode(queryName, "UTF-8");
			//thisForward.setPath("/runReport.do?method=runGeneViewReport&resultSetName="+reportBean.getResultantCacheKey());
			thisForward.setPath("/runReport.do?method=runGeneViewReport&resultSetName=" + queryName);
 		}else {
 			logger.error("SessionQueryBag has no Compound queries to execute");
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("gov.nih.nci.nautilus.ui.struts.action.executequery.querycoll.no.error"));
 			this.saveErrors(request, errors);
 			thisForward = mapping.findForward("failure");
 		}
 	    return thisForward;
 	 }
 	/**
 	 * A method used for debugging 
 	 * @param geneExprObjects
 	 */
 	private void print(ResultSet[] geneExprObjects) {
 		if(geneExprObjects != null){
 			logger.debug("Number of Records:"+ geneExprObjects.length);
 			for (int i =0; i < geneExprObjects.length; i++) {
 				GeneExpr.GeneExprSingle expObj = (GeneExpr.GeneExprSingle) geneExprObjects[i];
 				if(expObj != null){
 				logger.debug( "uID: " + expObj.getDesId() + "|geneSymbol: " + expObj.getGeneSymbol() +"|clone: " + expObj.getCloneName()+"|probeSet: "+expObj.getProbesetName()+"|biospecimenID: " + expObj.getBiospecimenId() );
 				}
 			}
 		}
 	}
     /**
      * Method called whenever there is change in the operands in the refine 
      * query page.  Currently it modifies the selected querries based on some
      * assumptions that can be made regarding the selected operand.  For instance
      * we can assume that if the user selects no operand for a query than any
      * querries below it are no longer needed as this must be the last query
      * in the compound query. Thus we remove them from the selectedQueries 
      * collection in the refineQueryForm.  The refineQuery tile then renders
      * the appropriate number of available rows to select from.  If the user
      * changes a operand from no operand to OR/AND than we know that they need
      * another row to select a query from.  
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward operandChange(
             ActionMapping mapping,
             ActionForm form,
             HttpServletRequest request,
             HttpServletResponse response)
             throws Exception {
    
         RefineQueryForm refineQueryForm = (RefineQueryForm)form;
         List selectedQuerries = refineQueryForm.getSelectedQueries();
         SelectedQueryBean lastQuery = (SelectedQueryBean)selectedQuerries.get(selectedQuerries.size()-1);
         //This logic is to prevent the adding of unnecesary rows...
         if(!lastQuery.getOperand().equals("")) {
         	refineQueryForm.addSelectedQuery();
         }else {
         	for(int i = 0; i < selectedQuerries.size();i++) {
         		if(((SelectedQueryBean)selectedQuerries.get(i)).getOperand().equals("")&& i!=selectedQuerries.size()-1) {
         			for(int j = selectedQuerries.size()-1; j>=i;j--) {
                        selectedQuerries.remove(j);
                     }
                     break;
                 }
            }
         
         }
         //Flag used by the refine_query.jsp to determine if we should show the 
         //run_report button
         refineQueryForm.setRunFlag("no");
         return mapping.findForward("displayQuery");
     }
     
   
 	/**
      * Creates and returns the key-value map for methods called based on which 
      * button is used to submit the form in the refine_tile.jsp.
      * @return key-method pairs for the RefineQueryAction
 	 */
 	protected Map getKeyMethodMap() {
 		 
         HashMap<String,String> map = new HashMap<String,String>();
         
         //Validate Query Button using validate method
         map.put("RefineQueryAction.validateButton", "validateQuery");
         
         //Run Report Button using displayResult method
         map.put("RefineQueryAction.runReportButton", "runReport");
        
         //Store the Query in the session as a ResultQuery
         map.put("RefineQueryAction.storeResultsButton","storeResults");
         
         //store current selected queries and add another row
         map.put("RefineQueryAction.queryOperandChange","operandChange");
      
         return map;
 	}
       
 }
