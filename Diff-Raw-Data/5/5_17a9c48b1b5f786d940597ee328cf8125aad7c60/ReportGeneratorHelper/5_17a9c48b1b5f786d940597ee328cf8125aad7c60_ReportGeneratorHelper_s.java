 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.dto.query.OperatorType;
 import gov.nih.nci.caintegrator.dto.view.View;
 import gov.nih.nci.caintegrator.dto.view.Viewable;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.rembrandt.cache.RembrandtContextListener;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.dto.query.Queriable;
 import gov.nih.nci.rembrandt.dto.query.Query;
 import gov.nih.nci.rembrandt.queryservice.ResultsetManager;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleResultset;
 import gov.nih.nci.rembrandt.queryservice.resultset.sample.SampleViewResultsContainer;
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.util.MoreStringUtils;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.FindingReportBean;
 import gov.nih.nci.rembrandt.web.bean.ReportBean;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.xml.ClassComparisonReport;
 import gov.nih.nci.rembrandt.web.xml.ReportGenerator;
 import gov.nih.nci.rembrandt.web.xml.ReportGeneratorFactory;
 import gov.nih.nci.rembrandt.web.xml.Transformer;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.io.HTMLWriter;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 
 /**
  * The ReportGeneratorHelper was written to act as a Report Generation manager
  * for the UI. It provides a single avenue where, if a UI element has a Query to
  * execute or the cache key to a previously stored Resultant, the necesary calls
  * are made to generate an XML document representing the report. The generated
  * XML will then be stored in a ReportBean that will also contain the cache key
  * where the resultant can be called again, if needed.
  * 
  * @author BauerD, LandyR 
  * Feb 8, 2005
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
 
 public class ReportGeneratorHelper {
 	//This is the general purpose Nautilus Logger
 	private static Logger logger = Logger
 			.getLogger(ReportGeneratorHelper.class);
 	//this is the XML logger for the reports
 	private static Logger xmlLogger;
 	//This is the element that is used to store all relevant report data
 	//it sores the resultant, the sessionId, the latest reportXML
 	private ReportBean _reportBean;
 	private CompoundQuery _cQuery;
 	private String _queryName = null;
 	private String _sessionId = null;
 	private static Properties applicationResources = applicationResources = ApplicationContext.getLabelProperties();
 	private RembrandtPresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
 	//Check the applications resource file and turn on report xml logging if 
 	//property nautilus.xml_logging = true, else then no xml report logging.
 	private static boolean xmlLogging;
 	static {
 		String property = (String)applicationResources.get("nautilus.xml_logging");
 		if("true".equals(property)){
 			xmlLogging = true;
 			//Get the XML_LOGGER specified in log4j.properties
 			xmlLogger = Logger.getLogger("XML_LOGGER");
 		}else {
 			xmlLogging = false;
 		}
 		
 	}
 	/**
 	 * This constructor is intended to be used by the UI when it needs to apply
 	 * a filter on a previously run report that requires the regeneration of the
 	 * report XML.  At this time it is only a "Show All Values" and "Copy Number
 	 * Filter" report.
 	 * 
 	 * This means that there must be a previous query that has been executed and
 	 * stored in the cache that can retrieveed.  The ReportBean given should be
 	 * the bean for the report that was previously generated and the user would
 	 * like to filter.
 	 * 
 	 * @param reportBean
 	 * @param filterParams -- this is all the XML filterParam values 
 	 */	
 	 public ReportGeneratorHelper(ReportBean reportBean, Map filterParams) {
 		logger.debug("Calling ReportGeneratorHelper to filter resultant");
 	 	Resultant oldResultant = reportBean.getResultant();
 		Resultant newResultant;
 		//Get the sessionId and name for this resultant, this works because,
 		//at the present time there is only one type of query that is ever
 		//run, and that is a CompoundQuery if that changes than it will be
 		//necesary to modify the code here and many other places
 		String sessionId = ((CompoundQuery)(reportBean.getAssociatedQuery())).getSessionId();
 		String oldQueryName = ((CompoundQuery)(reportBean.getAssociatedQuery())).getQueryName();
 		String newQueryName = "";
 		String filter_element = (String)filterParams.get("filter_element");
 		try {
 			//Apply the copy_number filter
 			if("copy_number".equals(filter_element)) {
 				logger.debug("Performing CopyNumber filter");
 				//execute copy number filter query
 				OperatorType operator = (OperatorType)filterParams.get("filter_value4");
 				Integer consecutiveCalls = (Integer)filterParams.get("filter_value5");
 				Integer percentCalls = (Integer)filterParams.get("filter_value6");
 				logger.debug("Old QueryName: "+oldQueryName);
 				newResultant = ResultsetManager.filterCopyNumber(oldResultant,consecutiveCalls,percentCalls,operator);
 				newQueryName = nameFilterQuery(oldQueryName, RembrandtConstants.FILTER_REPORT_SUFFIX);
 				logger.debug("New QueryName: "+newQueryName);
 			}else {
 				logger.debug("Performing a Show All Values Query");
 				/*
 				 * This is a hack...  we need to make sure that this isn't
 				 * something other than a show all values query
 				 */
 				logger.debug("Old QueryName: "+oldQueryName);
 				newResultant = ResultsetManager.executeShowAllQuery(oldResultant);
 				newQueryName = nameFilterQuery(oldQueryName, RembrandtConstants.SHOW_ALL_VALUES_SUFFIX);
 				logger.debug("New QueryName: "+newQueryName);
 			}
 			/*
 			 * We have to rename the resultant using the new query name with the
 			 * added suffix
 			 */
 			if(newResultant!=null) {
 				logger.debug("Setting newQueryName in resultant's associated query");
 				newResultant.getAssociatedQuery().setQueryName(newQueryName);
 			}
 			/*
 			 * At present the executeShowAllQuery(Resultant) method does not 
 			 * pass through the sessionId or the queryName, in the associated
 			 * Query in the showAllResults. So it is necesary to get the query
 			 * and set the sessionId and the queryName.  
 			 */
 			CompoundQuery query = (CompoundQuery)reportBean.getAssociatedQuery();
 			//check the associated query to make sure that it is a compound query
 			//also sets the _cQuery attribute
 		
 			checkCompoundQuery(query);
 			//check the sessionId that it isn't null or empty and set _sessionId in _cQuery
 			checkSessionId(sessionId);
 			//check that we have an old QueryName and set the class variable _queryName,
 			//adding that it is a all values report
 			checkQueryName( newQueryName);
 //			create a new ReportBean
 			_reportBean = new ReportBean();
 			//store the annotated query in the reportBean
 			_reportBean.setAssociatedQuery(_cQuery);
 			//store the results into the report bean
 			_reportBean.setResultant(newResultant);
 			//store the cache key that can be used to retrieve this bean later
 			_reportBean.setResultantCacheKey(_queryName);
 			//send the filterParamMap for processing and store in _reportBean
 			_reportBean.setFilterParams(processFilterParamMap(filterParams));
 			//generate the reportXML and store in the ReportBean
 			generateReportXML();
 			//drop this ReportBean in the session cache, use the _queryName as the 
 			//parameter
 			presentationTierCache.addNonPersistableToSessionCache(_sessionId,_queryName,_reportBean);
 		
 		}catch(Exception e) {
 			logger.error("Exception when trying to generate a Show All Values Report");
 			logger.error(e);
 		}
 		//All done...
 	}
 	
 	
 
 	/**
 	 * This is intended to be used to generate a ReportBean when you have a
 	 * query and want to reduce the result set by limiting the results to 
 	 * sample ids listed in the String[] sampleIds. As it is currently implemented
 	 * it does not check the cache for any result sets, because at present, we
 	 * execute the query all over again.  
 	 * 
 	 * In the future if performance is important we may consider just taking the
 	 * already existing result set and just extract the relevant sampleIds 
 	 * 
 	 * @param query --currently a CompoundQuery that you are selecting sample
 	 * ids from. No other Queriable object will actually work.  I know,I know
 	 * bad design. But this is to allow for the possibility of other types 
 	 * in the future.  We may later decide to constrain this to a CompoundQuery
 	 * but for now... well it is just going to have to be the way it is.
 	 * @param sampleIds --this is the array of sample ids that you would like to
 	 * contstrain
 	 * @throws Exception
 	 */
 	public ReportGeneratorHelper(Queriable query, String[] sampleIds) throws IllegalStateException {
 		//Clone the original query so that we do not modify it when we add samples to it
 		query = (CompoundQuery)query.clone();
 		//check the query to make sure that it is a compound query
 		checkCompoundQuery(query);
 		//check to make sure that we have a sessionId
 		checkSessionId( _cQuery.getSessionId());
 		//check that we have a queryName
 		checkQueryName( _cQuery.getQueryName());
 		//create a new ReportBean
 		_reportBean = new ReportBean();
 		Resultant sampleIdResults = null;
 		try {	
 			sampleIdResults = ResultsetManager.executeCompoundQuery(_cQuery, sampleIds);			
 		}catch(Exception e) {
 			logger.error("The ResultsetManager threw some exception");
 			logger.error(e);
 		}
 		/*
 		 * Make sure we got a result set back and then store all the information
 		 * in the bean.
 		 */
 		if(sampleIdResults!=null&&sampleIdResults.getResultsContainer()!=null) {
             sampleIdResults.getAssociatedQuery().setQueryName(_cQuery.getQueryName());
 			//store the results and query into the report bean
             _reportBean.setAssociatedQuery(sampleIdResults.getAssociatedQuery());
 			_reportBean.setResultant(sampleIdResults);
 		}else {
 			throw new IllegalStateException("ResultsetManager returned an empty resultant or threw an Exception");
 		}
 		//store the cache key that can be used to retrieve this bean later
 		_reportBean.setResultantCacheKey(_queryName);
 		//this is a result set
 		_reportBean.setSampleSetQuery(true);
 		//generate the reportXML and store in the ReportBean
 		generateReportXML();
 	}
 	
 	
 
 	/**
 	 * This constructor uses a TemplateMethod pattern to perform the following
 	 * tasks (They must happen in this order, though some may be omitted if the
 	 * use case does not require it): 
 	 * 		1-Check to see if the query is a CompoundQuery and cast if is
 	 * 		2-Check that there is a sessionId specified for the query
 	 * 		3-Check that we have a query name for the CompoundQuery
 	 * 		4-Check the SessionCache for any stored results for this query
 	 *      to avoid running to the database if not needed.
 	 *      5-Execute the query if there is no result set to use in the cache
 	 *      6-Generate the report xml based on the desired view
 	 *             
 	 * @param query --the query that you want some new view (Report) of
 	 */
 	public ReportGeneratorHelper(Queriable query, Map filterParams) {
 		try {
 			
 			//check the query to make sure that it is a compound query
 			checkCompoundQuery(query);
 			//check to make sure that we have a sessionId
 			checkSessionId( _cQuery.getSessionId());
 			//check that we have a queryName
 			checkQueryName( _cQuery.getQueryName());
 
 			/*
 			 * If the _reportBean is null then we know that we could not
 			 * find an appropriate result set in the session cache. So
 			 * lets run a query and get a result. Always run a query
 			 * if it is a preview report.
 			 * 
 			 */
 			if (_reportBean == null||RembrandtConstants.PREVIEW_RESULTS.equals(_queryName)) {
 				logger.debug("Executing Query");
 				if(RembrandtConstants.PREVIEW_RESULTS.equals(_queryName))	{
 					//put this in the map for preview
 					filterParams.put("showSampleSelect", new String("false"));
 				}
 				executeQuery();
 			}
 			if(_reportBean != null){//if its a valid query
 				_reportBean.setFilterParams(processFilterParamMap(filterParams));
 				this.generateReportXML();
 			}
 		}catch(Exception e) {
 			logger.error("Unable to create the ReportBean");
 			logger.error(e);
 		
 		  
 		}
 	}
 	
 	
 	public ReportGeneratorHelper(Finding finding)	{
 		
 	}
 	
 	/**
 	 * Checks the Queriable object and determines if it is a CompoundQuery.  If
 	 * so then set the class variable _cQuery = (CompoundQuery)query else
 	 * throw a new UnsupportedOperationException as we can not currently handle 
 	 * anything but CompoundQueries at this time.  I know that it doesn't make 
 	 * much sense to only allow a single implementation of the Queriable Interface
 	 * to really be passed when we specify in the signature that any Queriable
 	 * object will do.  It is just that in order to avoid tons of casting and
 	 * maintain flexibility for later implementations I did it this way.
 	 *  
 	 * @param query
 	 * @throws UnsupportedOperationException
 	 */
 	private void checkCompoundQuery(Queriable query) throws UnsupportedOperationException {
 		if (query != null && query instanceof CompoundQuery) {
 			//sets the class variable _cQuery 
 			_cQuery = (CompoundQuery)query;
 		}else {
 			/*
 			 * Some other object implementing the Queriable interface has been
 			 * passed. Currently we only support the compound query. But that is
 			 * not to say that we won't later support others. Hence, the if-else
 			 * statement here. I'll bet we want the run report button to use
 			 * this class so I better add some code here if we just get a query.
 			 */
 			logger.error("Non compound query submitted");
 			throw new UnsupportedOperationException(
 					"You must pass a CompoundQuery at this time");
 		}
 	}
 	
 	
 	/**
 	 * Check the sessionId in the Queriable object that we have gotten
 	 * @param sessionId
 	 * @throws IllegalStateException
 	 */
 	private void checkSessionId(String sessionId)throws IllegalStateException {
 		if (sessionId != null && !sessionId.equals("")) {
 			_sessionId = sessionId;
 			_cQuery.setSessionId(sessionId);
 		} else {
 			/*
 			 * We can not store the resultSet without a unique cache to
 			 * store it in. And since we are currently implementing a
 			 * session based cache system, that unique cache id should
 			 * be the session id. If there is no session id, than we can
 			 * assume that something is really wrong so throw a new
 			 * IllegalStateException
 			 */
 			logger.error("sessionId is empty for the compoundQuery");
 			throw new IllegalStateException(
 					"There does not appear to be a session associated with this query");
 		}
 	}
 	
 	/**
 	 * make sure that we have a name for our report
 	 * @param queryName
 	 */
 	private void checkQueryName(String queryName) {
 		if (!"".equals(queryName)&& queryName != null){
 			_queryName = queryName;
 		}else {
 			/*
 			 * If the query name is empty than we can assume that
 			 * the user has no interest in storing the query for
 			 * later use. However the user may still want to use the
 			 * current query report in other ways, like changing the
 			 * view or downloading it. SO we should probably keep it
 			 * around for a little bit. We do this by giving it a
 			 * fixed temp name to use as a session cache key.
 			 * 
 			 */
 			_queryName = presentationTierCache.getTempReportName(_sessionId);
 			
 		}
 		_cQuery.setQueryName(_queryName);
 	}
 	
 	/**
 	 * check the session cache for the results sets for the query
 	 * @param view
 	 */
 	private void checkCache(View view) {
 		/*
 		 * Use the sessionId to get the cache and see if 
 		 * a result set already exists for the queryName we have been 
 		 * given.
 		 */
 		_reportBean = presentationTierCache.getReportBean(_sessionId, _queryName, view);
 	}
 	
 	/**
 	 * This method will take the class variable ReportBean _reportBean and 
 	 * create the correct XML representation for the desired view of the results.  When
 	 * completed it adds the XML to the _reportBean and drops the _reportBean
 	 * into the sessionCache for later retrieval when needed
 	 * 
 	 * @throws IllegalStateException this is thrown when there is no resultant
 	 * found in _reportBean
 	 */
 	private void generateReportXML() throws IllegalStateException {
 		
 		/*
 		 * Get the correct report XML generator for the desired view
 		 * of the results.
 		 * 
 		 */
 		Document reportXML = null;
 		if (_reportBean != null) {
 			/*
 			 * We need to check the resultant to make sure that
 			 * the database has actually return something for the associated
 			 * query or filter.
 			 */
 			Resultant resultant = _reportBean.getResultant();
 			if(resultant!=null) {
 				try {
 					Viewable oldView = resultant.getAssociatedView();
                     //make sure old view is not null, if so, set it to the same as new view.
                     if(oldView == null){
                         oldView = _cQuery.getAssociatedView();
                     }
 					Viewable newView = _cQuery.getAssociatedView();
 					Map filterParams = _reportBean.getFilterParams();
 					/*
 					 * Make sure that we change the view on the resultSet
 					 * if the user changes the desired view from already
 					 * stored view of the results
 					 */
 					if(!oldView.equals(newView)||_reportBean.getReportXML()==null) {
 						//we must generate a new XML document for the
 						//view they want
 						logger.debug("Generating XML");
 						ReportGenerator reportGen = ReportGeneratorFactory
 								.getReportGenerator(newView);
 						resultant.setAssociatedView(newView);
 						reportXML = reportGen.getReportXML(resultant, filterParams);
 						logger.debug("Completed Generating XML");
 					}else {
 						//Old view is the current view
 						logger.debug("Fetching report XML from reportBean");
 						reportXML = _reportBean.getReportXML();
 					}
 				}catch(NullPointerException npe) {
 					logger.error("The resultant has a null value for something that was needed");
 					logger.error(npe);
 				}
 			}
 				
 			//XML Report Logging
 			if(xmlLogging) {
 				try {
 					StringWriter out = new StringWriter();
 					OutputFormat outformat = OutputFormat.createPrettyPrint();
 					XMLWriter writer = new XMLWriter(out, outformat);
 					writer.write(reportXML);
 					writer.flush();
 					xmlLogger.debug(out.getBuffer());
 				}catch(IOException ioe) {
 					logger.error("There was an error writing the XML to log");
 					logger.error(ioe);
 				}catch(NullPointerException npe){
 					logger.debug("There is no XML to log!");
 				}
 			}
 			_reportBean.setReportXML(reportXML);
 			presentationTierCache.addNonPersistableToSessionCache(_sessionId,_queryName,_reportBean);
 		   			   
 		    	
 		}else {
 			throw new IllegalStateException("There is no resultant to create report");
 		}
 	}
 	
 	public static void generateReportXML(Finding finding)	{
 		//TODO: shouldnt be called until findings are populated (!= running)
 		//TODO: is threadsafe?
 		//TODO: instance of
 		Document xmlDocument = ClassComparisonReport.getReportXML(finding, new HashMap());
 		FindingReportBean frb = new FindingReportBean();
 		frb.setFinding(finding);
 		frb.setXmlDoc(xmlDocument);
 		//TODO: check cache for collision - second param is key
 		ApplicationFactory.getPresentationTierCache().addNonPersistableToSessionCache(finding.getSessionId(),finding.getTaskId(), frb);
 	}
 	
 	/**
 	 * This executes the current Compound Query that is referenced in the _cQuery
 	 * class scope variable.  Stores this _cQuery in the ReportBean as the
 	 * associatedQuery
 	 * @throws Exception
 	 */
 	private void executeQuery() throws Exception{
 		//empty the cache before executing the query again
 		presentationTierCache.addNonPersistableToSessionCache(_sessionId,_queryName,null);
 		
 		if(_cQuery!=null) {
 			Resultant resultant = ResultsetManager.executeCompoundQuery(_cQuery);
 			/*
 			 * Create the _reportBean that will store everything we 
 			 * may need later when messing with the reports associated
 			 * with this result set.  This _reportBean will also 
 			 * be stored in the cache.
 			 */	
 			if(resultant != null){
 				_reportBean = new ReportBean();
 				_reportBean.setAssociatedQuery(_cQuery);
 				_reportBean.setResultant(resultant);
 				//The cache key will always be the compound query name
 				_reportBean.setResultantCacheKey(_cQuery.getQueryName());
 			}
 			else{
 				logger.debug("resultant is Null.  no results returned!");
 				//_reportBean = null;
 				_reportBean = new ReportBean();
 				_reportBean.setAssociatedQuery(_cQuery);
 				//_reportBean.setResultant(resultant);
 				//The cache key will always be the compound query name
 				_reportBean.setResultantCacheKey(_cQuery.getQueryName());
 			}		
 		}else{
 			logger.error("Compound Query is Null.  Can not execute!");
 			throw new NullPointerException("CompoundQuery is null.");
 		}
 	}
 	
 	/**
 	 * Returns the ReportBean that was generated by the constructor
 	 * @return --constructed ReportBean
 	 */
 	public ReportBean getReportBean() {
 		return _reportBean;
 	}
 	
 	/**
 	 * This static method will render a query report of the passed reportXML, in
 	 * HTML using the XSLT whose name has been passed, to the jsp whose 
 	 * jspWriter has been passed. The request is used to acquire any filter 
 	 * params that may have been added to the request and that may be applicable
 	 * to the XSLT.
 	 *  
 	 * @param request --the request that will contain any parameters you want applied
 	 * to the XSLT that you specify
 	 * @param reportXML -- this is the XML that you want transformed to HTML
 	 * @param xsltFilename --this the XSLT that you want to use
 	 * @param out --the JSPWriter you want the transformed document to go to...
 	 */
 	public static void renderReport(HttpServletRequest request, Document reportXML, String xsltFilename, JspWriter out) {
 		File styleSheet = new File(RembrandtContextListener.getContextPath()+"/XSL/"+xsltFilename);
 		// load the transformer using JAX
 		logger.debug("Applying XSLT "+xsltFilename);
         Transformer transformer;
 		try {
 			
 			// if the xsltFiname is pathway xlst, then do this
 			if((xsltFilename.equals(RembrandtConstants.DEFAULT_PATHWAY_XSLT_FILENAME))||
 			   (xsltFilename.equals(RembrandtConstants.DEFAULT_PATHWAY_DESC_XSLT_FILENAME))
 			   ||(xsltFilename.equals(RembrandtConstants.DEFAULT_GENE_XSLT_FILENAME))){
 			   transformer = new Transformer(styleSheet);
 			 }
 			// otherwise do this
 			
 			else {
 			   transformer = new Transformer(styleSheet, (HashMap)request.getAttribute(RembrandtConstants.FILTER_PARAM_MAP));
 			}
 			
 	     	Document transformedDoc = transformer.transform(reportXML);
 	        
 	     	/*
 	         * right now this assumes that we will only have one XSL for CSV
 	         * and it checks for that as we do not want to "pretty print" the CSV, we
 	         * only want to spit it out as a string, or formatting gets messed up
 	         * we will of course want to pretty print the XHTML for the graphical reports
 	         * later we can change this to handle mult XSL CSVs
 	         * RCL
 	         */
 	     	if(!xsltFilename.equals(RembrandtConstants.DEFAULT_XSLT_CSV_FILENAME)){
 	            OutputFormat format = OutputFormat.createPrettyPrint();
 	            XMLWriter writer;
 	            writer = new XMLWriter(out, format );	            
	            writer.write( transformedDoc );
 	            if(!xsltFilename.equals(RembrandtConstants.DEFAULT_PATHWAY_DESC_XSLT_FILENAME)) {
	               writer.close();
 	            }
 	        }
 	        else	{
 	            String csv = transformedDoc.getStringValue();
 	            csv.trim();
 	            out.println(csv);
 	        }
 		}catch (UnsupportedEncodingException uee) {
 			logger.error("UnsupportedEncodingException");
 			logger.error(uee);
 		}catch (IOException ioe) {
 			logger.error("IOException");
 			logger.error(ioe);
 		}
 	}
 	
 	
 	public static String renderReport(Map params, Document reportXML, String xsltFilename) {
 		//used only for finding based AJAX
 		
 		File styleSheet = new File(RembrandtContextListener.getContextPath()+"/XSL/"+xsltFilename);
 		// load the transformer using JAXP
 		logger.debug("Applying XSLT "+xsltFilename);
         Transformer transformer;
 		try {
 			transformer = new Transformer(styleSheet, params);			
 	     	Document transformedDoc = transformer.transform(reportXML);
 	        
 	     	/*
 	         * right now this assumes that we will only have one XSL for CSV
 	         * and it checks for that as we do not want to "pretty print" the CSV, we
 	         * only want to spit it out as a string, or formatting gets messed up
 	         * we will of course want to pretty print the XHTML for the graphical reports
 	         * later we can change this to handle mult XSL CSVs
 	         * RCL
 	         */
 	     	
 	     	if(!xsltFilename.equals(RembrandtConstants.DEFAULT_XSLT_CSV_FILENAME)){
 	            
 	     		StringBuffer sb = new StringBuffer();
 	     		OutputFormat format = OutputFormat.createCompactFormat();
 		        StringWriter sw = new StringWriter(1024);		        
 	            HTMLWriter writer = new HTMLWriter(sw, format);
 	            writer.write(transformedDoc);
 	            sb=sw.getBuffer();
 	            return sb.toString();
 
 	        }
 	        else	{
 	            String csv = transformedDoc.getStringValue();
 	            csv.trim();
 	            return csv;
 	        }
 		}catch (UnsupportedEncodingException uee) {
 			logger.error("UnsupportedEncodingException");
 			logger.error(uee);
 		}catch (IOException ioe) {
 			logger.error("IOException");
 			logger.error(ioe);
 		}
 		
 		return "Reporting Error";
 	}
 	/**
 	 * 
 	 * @param container --this is the container that you want the sample ids from
 	 * @return --A SampleCriteria of all the ResultsContainer sampleIds
 	 */
 	public static SampleCriteria extractSampleIds(ResultsContainer container)throws OperationNotSupportedException{
 		SampleCriteria theCriteria = new SampleCriteria();
 		Collection sampleIds = new ArrayList();
 		SampleViewResultsContainer svrContainer = null;
 		/*
 		 * These are currently the only two results containers that we have to
 		 * worry about at this time, I believe.
 		 */
 		if(container instanceof DimensionalViewContainer) {
 			//Get the SampleViewResultsContainer from the DimensionalViewContainer
 			DimensionalViewContainer dvContainer = (DimensionalViewContainer)container;
 			svrContainer = dvContainer.getSampleViewResultsContainer();
 		}else if(container instanceof SampleViewResultsContainer) {
 			svrContainer = (SampleViewResultsContainer)container;
 		}
 		//Handle the SampleViewResultsContainers if that is what we got
 		if(svrContainer!=null) {
 			Collection<SampleResultset> bioSpecimen = svrContainer.getSampleResultsets();
 			for(Iterator i = bioSpecimen.iterator();i.hasNext();) {
 				SampleResultset sampleResultset =  (SampleResultset)i.next();
 				sampleIds.add(new SampleIDDE(sampleResultset.getSampleIDDE().getValueObject()));
 			}
 		}else {
 			throw new OperationNotSupportedException("We are not able to able to extract SampleIds from: "+container.getClass());
 		}
 		//Drop the sampleIds into the SampleCriteria
 		theCriteria.setSampleIDs(sampleIds);
 		return theCriteria;
 	}
 	/**
 	 * will recurse through the compound query adding the SampleCriteria to each
 	 * of the associated queries.
 	 *  
 	 * @param query
 	 * @param sampleCriteria
 	 */
 	public static Queriable addSampleCriteriaToCompoundQuery(Queriable query, SampleCriteria sampleCriteria, String resultSetName) {
 		if(query!=null) {
 			if(query instanceof CompoundQuery) {
 				CompoundQuery newQuery = (CompoundQuery)query;
 				try {
 					newQuery.setLeftQuery(addSampleCriteriaToCompoundQuery(newQuery.getLeftQuery(), sampleCriteria, resultSetName));
 					newQuery.setRightQuery(addSampleCriteriaToCompoundQuery(newQuery.getRightQuery(), sampleCriteria, resultSetName));
 				}catch(Exception e) {
 					logger.error(e);
 				}
 			}else {
 				Query newQuery = (Query)query;
 				newQuery.setQueryName("("+newQuery.getQueryName()+" AND "+resultSetName+")");
 				newQuery.setSampleIDCrit(sampleCriteria);
 				return newQuery;
 			}
 			
 		}
 		return query;
 	}
 	/**
 	 * This is used to take the RefineQueryForm "filter_string" and create a 
 	 * list from the comma seperated tokens contained in the string. It also
 	 * removes any unacceptable characters and creates an acceptable string...
 	 * In truth it is utilizing the MoreStringUtils class to clean the strings.
 	 * @param filterParams
 	 */
 	private Map processFilterParamMap(Map filterParams) {
 		String test = "";
 		List tokens = null;
 		//String unallowableCharacters = " <>!:\"@#\\$%^&*()-+=/{}[]|?~`";
 		String unallowableCharacters = " <>!\"@#\\$%^&*()+=/{}[]|?~`";
 		if(filterParams!=null && filterParams.containsKey("filter_string")) {
 			//tokenize the string
 			StringTokenizer tokenizer = new StringTokenizer((String)filterParams.get("filter_string"), ",", false);
 			tokens = new ArrayList();
 			while(tokenizer.hasMoreTokens()) {
 				String cleanToken = MoreStringUtils.cleanString(unallowableCharacters, tokenizer.nextToken());				
 				cleanToken = cleanToken.toUpperCase();
 				tokens.add(cleanToken);
 			}
 			filterParams.put("filter_string", tokens);
 		}
 		return filterParams;
 		
 	}
 	/**
 	 * This will verify that the desired suffix is not already in the report
 	 * @param oldQueryName
 	 * @param suffix
 	 * @return
 	 */
 	private String nameFilterQuery(String oldQueryName, String suffix) {
 		String newQueryName;
 		if(oldQueryName.lastIndexOf(suffix)<0) {
 			newQueryName = oldQueryName + suffix;
 		}else {
 			newQueryName = oldQueryName;
 		}
 		return newQueryName;
 	}
 	
 
 }
