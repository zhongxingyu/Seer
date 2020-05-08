 package gov.nih.nci.eagle.web.reports;
 
 import gov.nih.nci.caintegrator.analysis.messaging.FTestResultEntry;
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelResult;
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelResultEntry;
 import gov.nih.nci.caintegrator.application.configuration.SpringContext;
 import gov.nih.nci.caintegrator.domain.annotation.gene.bean.GeneBiomarker;
 import gov.nih.nci.caintegrator.service.findings.FTestFinding;
 import gov.nih.nci.caintegrator.service.findings.GeneralizedLinearModelFinding;
 import gov.nih.nci.eagle.query.dto.ClassComparisonQueryDTOImpl;
 import gov.nih.nci.eagle.util.FTestComparator;
 import gov.nih.nci.eagle.util.PatientGroupManager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletResponse;
 
 public class GLMReport {
 
     private GeneralizedLinearModelFinding finding;
     private Boolean sortAscending;
     private FTestComparator sortComparator;
     private List reportBeans;
 
     private Map<String, List> patientInfoMap;
     private String sortedBy;
 
 
     public GLMReport(GeneralizedLinearModelFinding finding) {
 
         this.finding = finding;
         sortAscending = true;
         sortComparator = new FTestComparator("pvalues[0]", sortAscending);
        sortedBy = "groupName_0";
         reportBeans = new ArrayList<GLMReportBean>();
 
         for (GeneralizedLinearModelResultEntry entry : finding.getResultEntries()) {
             GLMReportBean bean = new GLMReportBean(entry, ((GeneBiomarker)finding
                     .getReporterAnnotationsMap().get(entry.getReporterId()))
                     .getHugoGeneSymbol());
             reportBeans.add(bean);
         }
         
         patientInfoMap = new HashMap<String, List>();
         PatientGroupManager man = (PatientGroupManager)SpringContext.getBean("patientManager");
         for(String groupName : (List<String>)getBaselineGroups()) {
             List patients = getQueryDTO().getBaselineGroupMap().get(groupName);
             List patientInfo = man.getPatientInfo(patients);
             patientInfoMap.put(groupName, patientInfo);
         }
         for(String groupName : (List<String>)getComparisonGroups()) {
             List patients = getQueryDTO().getComparisonGroupsMap().get(groupName);
             List patientInfo = man.getPatientInfo(patients);
             patientInfoMap.put(groupName, patientInfo);
         }
     }
 
     public ClassComparisonQueryDTOImpl getQueryDTO() {
         return (ClassComparisonQueryDTOImpl)finding.getTask().getQueryDTO();
     }
     
     public List getBaselineGroups() {
         return new ArrayList(getQueryDTO().getBaselineGroupMap().keySet());
     }
 
     public List getComparisonGroups() {
         return new ArrayList(getQueryDTO().getComparisonGroupsMap().keySet());
     }
     
     public Collection getReportBeans() {
         Collections.sort(reportBeans, sortComparator);
         return reportBeans;
     }
 
     public int getNumberGroups() {
     	return (finding.getGroupNames() != null ? finding.getGroupNames().size() : 0);
     }
 
     public Collection getGroupNames() {
     	List<String> gnames = finding.getGroupNames();
     	List<String> newgnames = new ArrayList<String>();
     	for(String s : gnames)	{
     		newgnames.add(s.replace("_after_adjustment", " (after adjustment)").replaceAll("_before_adjustment", " (before adjustment)"));
     	}
     	Collections.sort(newgnames);
     	return newgnames;
     }
 
     public void sortDataList(ActionEvent event) {
         String sortFieldAttribute = getAttribute(event, "sortField");
 
        //js doesnt like the [] notation, so had to use another Att with an underscore
        sortedBy = getAttribute(event, "sortedBy") != null ? getAttribute(event, "sortedBy") : sortFieldAttribute.replace("[", "_").replace("]", "");
     
         // Get and set sort field and sort order.
         // Get and set sort field and sort order.
         if (sortFieldAttribute != null
                 && sortFieldAttribute.equals(sortComparator.getField())) {
             sortAscending = !sortAscending;
         } else {
             sortAscending = true;
         }
         if (sortFieldAttribute != null) {
             sortComparator = new FTestComparator(sortFieldAttribute,
                     sortAscending);
         }
 
     }
 
     private static String getAttribute(ActionEvent event, String name) {
         return (String) event.getComponent().getAttributes().get(name);
     }
 
     public boolean getSortAscending() {
         return sortAscending;
     }
 
     public void setSortAscending(boolean sortAscending) {
         this.sortAscending = sortAscending;
     }
     
     public void generateCSV(ActionEvent event)	{
     	Collection reportBeans = this.getReportBeans();
     	List<List> csv = new ArrayList<List>();
     	
    		for(GLMReportBean rb : (List<GLMReportBean>)reportBeans){
    			if(csv.size()==0){
    				List headers = rb.getRowLabels();
    				//since the columns are dynamic based on the # of groups, we need to overwrite some of the header with
    				//values from the Report, as the ReportBean doesnt have this info
    				for(int i=0; i<this.getGroupNames().size(); i++){
    					
    					try {
 						headers.set(i+1, this.getGroupNames().toArray()[i].toString() + ""); 
 						//overwrite, with an offset of 2
 					} catch (RuntimeException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
    				
    				}
    				csv.add(headers);
    			}
    			csv.add(rb.getRow());
 		}
 		
         FacesContext facesContext = FacesContext.getCurrentInstance();
         HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
 		try {
 			CSVUtil.renderCSV(response, csv);
 		} catch (Exception e) {
 			// TODO: handle exception
 		} finally	{
 			FacesContext.getCurrentInstance().responseComplete();
 		}
 
     }
 
 	public Map<String, List> getPatientInfoMap() {
 		return patientInfoMap;
 	}
 
 	public void setPatientInfoMap(Map<String, List> patientInfoMap) {
 		this.patientInfoMap = patientInfoMap;
 	}
 
 	public String getSortedBy() {
 		return sortedBy;
 	}
 
 	public void setSortedBy(String sortedBy) {
 		this.sortedBy = sortedBy;
 	}
 
 }
