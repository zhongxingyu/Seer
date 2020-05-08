 package com.mpower.controller;
 
 import java.io.Console;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 import javax.xml.parsers.*;
 
 import net.sf.jasperreports.crosstabs.JRCrosstab;
 import net.sf.jasperreports.engine.JRChild;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.xml.JRXmlWriter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.util.Assert;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractWizardFormController;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 import ar.com.fdvs.dj.core.DynamicJasperHelper;
 import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
 import ar.com.fdvs.dj.domain.DynamicReport;
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.mpower.domain.ReportCrossTabFields;
 import com.mpower.domain.ReportCustomFilterDefinition;
 import com.mpower.domain.ReportDataSource;
 import com.mpower.domain.ReportDataSubSource;
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportFieldGroup;
 import com.mpower.domain.ReportFilter;
 import com.mpower.domain.ReportGroupByField;
 import com.mpower.domain.ReportSelectedField;
 import com.mpower.domain.ReportWizard;
 import com.mpower.service.ReportCustomFilterDefinitionService;
 import com.mpower.service.ReportFieldGroupService;
 import com.mpower.service.ReportFieldService;
 import com.mpower.service.ReportSourceService;
 import com.mpower.service.ReportSubSourceService;
 import com.mpower.service.ReportWizardService;
 //import com.mpower.service.ReportWizardService;
 import com.mpower.service.SessionService;
 import com.mpower.service.JasperServerService;
 import com.mpower.util.ReportGenerator;
 import com.mpower.util.ReportQueryGenerator;
 import com.mpower.util.ModifyReportJRXML;
 import com.mpower.view.DynamicReportView;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 import org.springframework.security.providers.dao.DaoAuthenticationProvider;
 import org.springframework.security.providers.dao.UserCache;
 import org.springframework.security.userdetails.UserDetails;
 import org.springframework.security.userdetails.UserDetailsService;
 
 public class ReportWizardFormController extends AbstractWizardFormController {
 	private ReportSubSourceService  reportSubSourceService;
 
 	/** Logger for this class and subclasses */
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private DynamicReportView       dynamicView;
 	private ReportSourceService     reportSourceService;
 	private ReportWizard            wiz;
 	private ReportWizardService     reportWizardService;
 	private JasperServerService     jasperServerService;
 	private ReportFieldGroupService reportFieldGroupService;
 
 	private ReportFieldService      reportFieldService;
 	private ReportCustomFilterDefinitionService      reportCustomFilterDefinitionService;
 	private SessionService          sessionService;
 	private DataSource jdbcDataSource;
 	private String reportUnitDataSourceURI;
 	private ReportGenerator	reportGenerator;
 	private long previousDataSubSourceId = -1;
 	private UserDetailsService userDetailsService;
 	
 	public ReportWizardFormController() {
 	}
 
 	@Override
 	protected Object formBackingObject(HttpServletRequest request)
 	throws ServletException {
 		logger.info("**** in formBackingObject");
 		wiz = new ReportWizard();
 		wiz.setDataSources(reportSourceService.readSources());
 
 		logger.info("Count " + wiz.getDataSources().size());
 		return wiz;
 	}
 
 	public DynamicReportView getDynamicView() {
 		return dynamicView;
 	}
 
 	public DataSource getJdbcDataSource() {
 		return jdbcDataSource;
 	}
 
 	public ReportFieldGroupService getReportFieldGroupService() {
 		return reportFieldGroupService;
 	}
 
 	public ReportFieldService getReportFieldService() {
 		return reportFieldService;
 	}
 
 	public ReportGenerator getReportGenerator() {
 		return reportGenerator;
 	}
 
 	public ReportSourceService getReportSourceService() {
 		return reportSourceService;
 	}
 
 	public ReportSubSourceService getReportSubSourceService() {
 		return reportSubSourceService;
 	}
 	public String getReportUnitDataSourceURI() {
 		return reportUnitDataSourceURI;
 	}
 	public ReportWizardService getReportWizardService() {
 		return reportWizardService;
 	}
 
 	public SessionService getSessionService() {
 		return sessionService;
 	}
 
 	// returns the last page as the success view
 	private String getSuccessView() {
 		return getPages()[getPages().length - 1];
 	}
 
 	@Override
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		Assert.notNull(request, "Request must not be null");
 
 
 		return super.handleRequest(request, response);
 	}
 
 	@Override
 	protected void postProcessPage(HttpServletRequest request, Object command,
 			Errors errors, int page) throws Exception {
 		logger.info("**** in onSubmit()");
 		Map<String, Object> params = new HashMap<String, Object>();
 		ReportWizard wiz = (ReportWizard) command;
 
 		Assert.notNull(request, "Request must not be null");
 		
 		if (page == 0)
 		{
 			if (request.getParameter("_target0") != null
 				|| request.getParameter("_target1") != null
 				|| request.getParameter("_target2") != null
 				|| request.getParameter("_target3") != null
 				|| request.getParameter("_target4") != null) {
 				if (wiz.getSubSourceId() != previousDataSubSourceId || wiz.getDataSource() == null)
 				{
 					previousDataSubSourceId = wiz.getSubSourceId();
 					wiz.setDataSource(reportSourceService.find(wiz.getSrcId()));
 					ReportDataSource rds = reportSourceService.find(wiz.getSrcId());
 					List<ReportDataSubSource> lrdss = reportSubSourceService.readSubSourcesByReportSourceId(rds.getId());
 					wiz.setDataSource(rds);
 					wiz.setDataSubSources(lrdss);
 					
 					ReportDataSubSource       rdss = reportSubSourceService.find( wiz.getSubSourceId());
 		
 					List<ReportFieldGroup>    lrfg = reportFieldGroupService.readFieldGroupBySubSourceId(rdss.getId());
 					wiz.setFieldGroups(lrfg);
 					
 					wiz.setDataSubSource(rdss);
 		
 					wiz.getDataSubSource().setReportCustomFilterDefinitions(reportCustomFilterDefinitionService.readReportCustomFilterDefinitionBySubSourceId(rdss.getId()));
 					
 					List<ReportField> fields = new LinkedList<ReportField>();
 					// Iterate across the field groups in the
 					Iterator itGroup = lrfg.iterator();
 					while (itGroup.hasNext()) {
 						ReportFieldGroup rfg = (ReportFieldGroup) itGroup.next();
 						fields.addAll(reportFieldService.readFieldByGroupId(rfg.getId()));
 					}
 					wiz.setFields(fields);
 					
 					// once the data source and sub-source have been selected, select the default fields
 					wiz.populateDefaultReportFields();
 					
 					// clear out any selected filters, chart settings, etc.
 					wiz.getReportFilters().clear();
 					wiz.getReportChartSettings().clear();
 					wiz.getReportCrossTabFields().getReportCrossTabColumns().clear();
 					wiz.getReportCrossTabFields().getReportCrossTabRows().clear();
 					wiz.getReportCrossTabFields().setReportCrossTabMeasure(-1);
 				}
 			} else {
 				previousDataSubSourceId = -1;
 			}				
 		} 
 		
 		if (request.getParameter("_target5") != null) {
 			//
 			// We are saving this report to jasperserver
 			try {
 				saveReport(wiz);
 			} catch (Exception e) {
 				logger.error(e.getLocalizedMessage());
 				errors.reject(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	@Override
 	protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int currentPage) {
 		//
 		// if we are saving a report then redirect the user back to where they hit saveas from
 		if (request.getParameter("_target5") != null) {
 			try {
 				saveReport(wiz);
 			} catch (Exception e) {
 				logger.error(e.getLocalizedMessage());
 				errors.reject(e.getLocalizedMessage());
 			}
 			
 			ReportWizard wiz = (ReportWizard) command;
 			return wiz.getPreviousPage();
 		}
 		
 		return super.getTargetPage(request, command, errors, currentPage);
 	}
 	
 	@Override
 	protected ModelAndView processFinish(HttpServletRequest request,
 			HttpServletResponse arg1, Object arg2, BindException arg3)
 	throws Exception {
 
 
 		return new ModelAndView(getSuccessView(),"reportsouce",wiz);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected Map referenceData(HttpServletRequest request, Object command,
 			Errors errors, int page) throws Exception {
 		ReportWizard wiz = (ReportWizard) command;
 		Map refData = new HashMap();
 
 		refData.put("page",page);
 		refData.put("maxpages", getPages().length-6); // 5 pages that are not actual steps
 
 		// see if we went backwards
 		if (wiz.getPreviousPage() > page)
 			wiz.setPreviousPage(page -1);
 
 		refData.put("previouspage", wiz.getPreviousPage());
 		//
 		// Report Source
 		if (page == 0) {
 			reportGenerator.resetInputControls();
 
 			// if no user or password is in the request, see if the user has already been
 			// populated on the wiz.  Going to the second page and then back to the first could cause that.
 			String userName = request.getParameter("username");
 			String password = request.getParameter("password");
 			if (userName == null || password == null) {
 				userName = wiz.getUsername();
 				password = wiz.getPassword();
 			} else {
 				
 				
 				UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
 				wiz.setUsername(userName);
 				password = userDetails.getPassword();
 				wiz.setPassword(password);
 			}
 
 			// If there is no user from the request or the wiz, send back to jasper server	    
 			if (userName == null || password == null) {	    	
 				refData.put("userFound", false);
 			} else {
 				refData.put("userFound", true);
 				refData.put("subSourceId", wiz.getSubSourceId());
 				reportGenerator.setReportUserName(userName);
 				reportGenerator.setReportPassword(password);
 
 				jasperServerService.setUserName(userName);
 				jasperServerService.setPassword(password);
 				wiz.setReportTemplateList(jasperServerService.list("/Reports/" + wiz.getCompany() + "/templates"));
 				wiz.setReportTemplatePath(((ResourceDescriptor)wiz.getReportTemplateList().get(0)).getUriString());
 			}
 		}
 
 		//
 		// Report Format
 		if (page == 1) {
 			String reportType = wiz.getReportType();
 			if (reportType.compareToIgnoreCase("summary") == 0)
 				reportType = "tabular";
 			refData.put("reportType", reportType);
 		}
 
 		//
 		// Report content selection
 		if (page == 2) {
 			String reportType = wiz.getReportType();
 			refData.put("reportType", reportType);
 
 			ReportDataSubSource rdss = reportSubSourceService.find(wiz.getSubSourceId());
 			List<ReportFieldGroup>    lrfg = reportFieldGroupService.readFieldGroupBySubSourceId(rdss.getId());
 			wiz.setFieldGroups(lrfg);
 			
 			refData.put("fieldGroups", wiz.getFieldGroups());
 
 			if (reportType.compareTo("tabular") == 0 || reportType.compareTo("summary") == 0) {
 				List<ReportSelectedField> tempFields = new LinkedList<ReportSelectedField>();
 				tempFields.addAll(wiz.getReportSelectedFields());
 				refData.put("selectedFields", tempFields);
 				// Clear out the selected fields because some items do not post back correctly
 				wiz.getReportSelectedFields().clear();
 				
 				refData.put("reportChartSettings", wiz.getReportChartSettings());
 			} else {
 				List<ReportGroupByField> tempColumns = new LinkedList<ReportGroupByField>();
 				tempColumns.addAll(wiz.getReportCrossTabFields().getReportCrossTabColumns());
 				refData.put("matrixColumns", tempColumns);
 				wiz.getReportCrossTabFields().getReportCrossTabColumns().clear();
 				
 				List<ReportGroupByField> tempRows = new LinkedList<ReportGroupByField>();
 				tempRows.addAll(wiz.getReportCrossTabFields().getReportCrossTabRows());
 				refData.put("matrixRows", tempRows);
 				wiz.getReportCrossTabFields().getReportCrossTabRows().clear();
 				
 				refData.put("matrixMeasure", wiz.getReportCrossTabFields().getReportCrossTabMeasure());
 				refData.put("matrixOperation", wiz.getReportCrossTabFields().getReportCrossTabOperation());
 			}
 		}
 
 		// filter screen
 		if(page==3) {
 			ReportDataSubSource rdss = reportSubSourceService.find(wiz.getSubSourceId());
 			List<ReportFieldGroup>    lrfg = reportFieldGroupService.readFieldGroupBySubSourceId(rdss.getId());
 			wiz.setFieldGroups(lrfg);
 		
			refData.put("fieldGroups", wiz.getFieldGroups());
		    refData.put("customFilters", wiz.getDataSubSource().getReportCustomFilterDefinitions());
 		    
 			List<ReportFilter> tempFilters = new LinkedList<ReportFilter>();
 			tempFilters.addAll(wiz.getReportFilters());
 			refData.put("selectedFilters", tempFilters);
 			// Clear out the selected fields because some items do not post back correctly
 			wiz.getReportFilters().clear();
 			
 		    refData.put("rowCount", wiz.getRowCount());			
 		}
 		
 		// run a saved report
 		if (page==7) {
 			//			wiz.setReportPath("/Reports/Clementine/" + wiz.getReportName().replace(" ", "_"));
 			refData.put("reportPath", wiz.getReportPath());
 		}
 
 		// running the report
 		if (page==8) {
 			// if there are no fields selected, select the defaults
 			if (wiz.getReportSelectedFields().size() == 0)
 				wiz.populateDefaultReportFields();
 
 //		 	 SuppressWarnings("unused")
 			
 			DynamicReport dr = reportGenerator.Generate(wiz, jdbcDataSource, reportFieldService, reportCustomFilterDefinitionService);
 			//String query = dr.getQuery().getText();
 
 			//
 			// execute the query and pass it to generateJasperPrint
 			//Connection connection = jdbcDataSource.getConnection();
 			//Statement statement = connection.createStatement();
 
 			File tempFile = File.createTempFile("wiz", ".jrxml");
 			logger.info("Temp File: " + tempFile);
 			DynamicJasperHelper.generateJRXML(dr,new ClassicLayoutManager(), reportGenerator.getParams(), null, tempFile.getPath());
 
 		    //
 		    // modify the jrxml
 		    ModifyReportJRXML reportXMLModifier = new ModifyReportJRXML(wiz, reportFieldService);
 		    
 		    // DJ adds a dataset that causes an error on the matrix reports so we need to remove it 
 		    if (wiz.getReportType().compareToIgnoreCase("matrix") == 0)
 		    	reportXMLModifier.removeCrossTabDataSubset(tempFile.getPath());
 			
 		    // add the summary info/totals to the report - DJ only allows one per column and we need to allow multiple so 
 			// 		we are altering the XML directly to add the summary calculations to the jasper report,
 		    //		this also handles adding the calculations to the groups created by DJ. 	
 			if (wiz.HasSummaryFields() == true){
 				reportXMLModifier.AddGroupSummaryInfo(tempFile.getPath());
 			    reportXMLModifier.AddReportSummaryInfo(tempFile.getPath());
 			}
 			    
 			//
 			// save the report to the server
 			reportGenerator.put(ResourceDescriptor.TYPE_REPORTUNIT, tempFile.getName(), tempFile.getName(), tempFile.getName(), wiz.getTempFolderPath(), tempFile,reportGenerator.getParams(), wiz.getDataSubSource().getJasperDatasourceName());
 
 			wiz.setReportPath(wiz.getTempFolderPath() + "/" + tempFile.getName());
 			refData.put("reportPath", wiz.getReportPath());
 
 			tempFile.delete();
 		}
 
 		return refData;
 	}
 
 	protected void saveReport(ReportWizard wiz) throws Exception {
 
 		//
 		// First we must generate a jrxml file
 		//
 
 		DynamicReport dr = reportGenerator.Generate(wiz, jdbcDataSource, reportFieldService, reportCustomFilterDefinitionService);
 
 		File tempFile = File.createTempFile("wiz", ".jrxml");
 		logger.info("Temp File: " + tempFile);
 		DynamicJasperHelper.generateJRXML(dr,new ClassicLayoutManager(), reportGenerator.getParams(), null, tempFile.getPath());
 
 		//
 	    // modify the jrxml
 	    ModifyReportJRXML reportXMLModifier = new ModifyReportJRXML(wiz, reportFieldService);
 	    
 	    // DJ adds a dataset that causes an error on the matrix reports so we need to remove it 
 	    if (wiz.getReportType().compareToIgnoreCase("matrix") == 0)
 	    	reportXMLModifier.removeCrossTabDataSubset(tempFile.getPath());
 		
 	    // add the summary info/totals to the report - DJ only allows one per column and we need to allow multiple so 
 		// 		we are altering the XML directly to add the summary calculations to the jasper report,
 	    //		this also handles adding the calculations to the groups created by DJ. 	
 		if (wiz.HasSummaryFields() == true){
 			reportXMLModifier.AddGroupSummaryInfo(tempFile.getPath());
 		    reportXMLModifier.AddReportSummaryInfo(tempFile.getPath());
 		}
 		
 		String reportTitle = wiz.getDataSubSource().getDisplayName() + " Custom Report";
 		if (wiz.getReportName() != null && wiz.getReportName().length() > 0)
 			reportTitle = wiz.getReportName();
 
 		String reportComment = wiz.getDataSubSource().getDisplayName() + " Custom Report";
 		if (wiz.getReportComment() != null && wiz.getReportComment().length() > 0)
 			reportComment = wiz.getReportComment();
 
 		reportGenerator.put(ResourceDescriptor.TYPE_REPORTUNIT, reportTitle.replace(" ", "_").replace("'", "").replace("\"", ""), reportTitle, reportComment,wiz.getReportPath(),tempFile, reportGenerator.getParams(), wiz.getDataSubSource().getJasperDatasourceName());
 
 		//    		reportGenerator.put(ResourceDescriptor.TYPE_REPORTUNIT, reportTitle.replace(" ", "_"), reportTitle, reportComment, wiz.getReportPath(),tempFile, reportGenerator.getParams());
 
 
 		// delete the temporary file
 		tempFile.delete();
 	}
 
 	public void setDynamicView(DynamicReportView dynamicView) {
 		this.dynamicView = dynamicView;
 	}
 
 	public void setJdbcDataSource(DataSource jdbcDataSource) {
 		this.jdbcDataSource = jdbcDataSource;
 	}
 
 	public void setReportFieldGroupService(
 			ReportFieldGroupService reportFieldGroupService) {
 		this.reportFieldGroupService = reportFieldGroupService;
 	}
 
 	public void setReportFieldService(ReportFieldService reportFieldService) {
 		this.reportFieldService = reportFieldService;
 	}
 
 	public void setReportGenerator(ReportGenerator reportGenerator) {
 		this.reportGenerator = reportGenerator;
 	}
 
 	public void setReportSourceService(ReportSourceService reportSourceService) {
 		this.reportSourceService = reportSourceService;
 	}
 
 	public void setReportSubSourceService(
 			ReportSubSourceService reportSubSourceService) {
 		this.reportSubSourceService = reportSubSourceService;
 	}
 
 	public void setReportUnitDataSourceURI(String reportUnitDataSourceURI) {
 		this.reportUnitDataSourceURI = reportUnitDataSourceURI;
 	}
 
 	public void setReportWizardService(ReportWizardService reportWizardService) {
 		this.reportWizardService = reportWizardService;
 	}
 
 	public void setSessionService(SessionService sessionService) {
 		this.sessionService = sessionService;
 	}
 
 	@Override
 	protected ModelAndView showForm(HttpServletRequest request,
 			HttpServletResponse response, BindException errors)
 	throws Exception {
 
 		return showPage(request, errors, getInitialPage(request, errors
 				.getTarget()));
 	}
 
 	public void setReportCustomFilterDefinitionService(
 			ReportCustomFilterDefinitionService reportCustomFilterDefinitionService) {
 		this.reportCustomFilterDefinitionService = reportCustomFilterDefinitionService;
 	}
 
 	public ReportCustomFilterDefinitionService getReportCustomFilterDefinitionService() {
 		return reportCustomFilterDefinitionService;
 	}
 
 	public void setJasperServerService(JasperServerService jss) {
 		jasperServerService = jss;
 	}
 
 	public UserDetailsService getUserDetailsService() {
 		return userDetailsService;
 	}
 
 	public void setUserDetailsService(UserDetailsService userDetailsService) {
 		this.userDetailsService = userDetailsService;
 	}
 }
