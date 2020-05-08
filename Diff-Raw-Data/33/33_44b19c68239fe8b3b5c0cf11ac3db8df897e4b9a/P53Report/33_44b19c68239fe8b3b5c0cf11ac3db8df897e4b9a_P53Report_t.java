 package gov.nih.nci.caintegrator.application.report;
 
 import gov.nih.nci.caintegrator.application.bean.LevelOfExpressionIHCFindingReportBean;
 import gov.nih.nci.caintegrator.application.bean.P53FindingReportBean;
 import gov.nih.nci.caintegrator.application.util.PatientComparator;
 import gov.nih.nci.caintegrator.application.util.TimepointStringComparator;
 import gov.nih.nci.caintegrator.domain.finding.mutation.p53.bean.P53MutationFinding;
 import gov.nih.nci.caintegrator.domain.finding.protein.ihc.bean.LevelOfExpressionIHCFinding;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.caintegrator.studyQueryService.dto.ihc.LevelOfExpressionIHCFindingCriteria;
 import gov.nih.nci.caintegrator.studyQueryService.dto.p53.P53FindingCriteria;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 public class P53Report {
 	
 	/**
 	 * public constructor
 	 */
 	public P53Report() {}
 	
 	/**
      * @param finding
      * @param filterMapParams
      * @return Document
      */
     public static Document getReportXML(Finding finding, Map filterMapParams) {
 
         Document document = DocumentHelper.createDocument();
 
             Element report = document.addElement( "Report" );
             Element cell = null;
             Element data = null;
             Element dataRow = null;
             //ADD BASIC ATTRIBUTED
             report.addAttribute("reportType", "p53Mutation");
             report.addAttribute("groupBy", "none");
             String queryName = finding.getTaskId();        
             report.addAttribute("queryName", queryName);
             report.addAttribute("sessionId", "the session id");         
             
             StringBuffer sb = new StringBuffer();
              
             /* *************************************************************************************************
              * 
              * PROCESS QUERY DETAILS
              * 
              * *************************************************************************************************             * 
              */
             P53FindingCriteria criteria = (P53FindingCriteria)finding.getQueryDTO();
             ArrayList<String> queryDetails = new ArrayList();
             if(criteria!=null){
                 String tmp = ""; 
                 ArrayList<String> tmpCollection = new ArrayList<String>();
                 tmp = criteria.getQueryName()!=null ? criteria.getQueryName() : "";
                 queryDetails.add("Query Name: " + tmp);
                 tmp = "";
                 
                 if(criteria.getSpecimenCriteria()!=null && 
                         criteria.getSpecimenCriteria().getTimeCourseCollection()!=null && 
                         !criteria.getSpecimenCriteria().getTimeCourseCollection().isEmpty()){
                     Set<String> timepoints = criteria.getSpecimenCriteria().getTimeCourseCollection();                    
                     tmpCollection = new ArrayList<String>(timepoints);
                     TimepointStringComparator ts = new TimepointStringComparator();
                     Collections.sort(tmpCollection, ts);
                     for(String tc : tmpCollection){
                         if(tc != tmpCollection.get(0)){
                             tmp += ", " + tc;
                         }
                         else{
                             tmp += tc;
                         }
                     }
                 }
                 queryDetails.add("Timepoint(s): " + tmp);
                 tmpCollection.clear();
                 tmp = "";                
                 if(criteria.getMutationStatusCollection()!=null && !criteria.getMutationStatusCollection().isEmpty()){
                     Set<String> mutationStatus = (Set<String>) criteria.getMutationStatusCollection();                    
                     tmpCollection = new ArrayList<String>(mutationStatus);
                     for(String in : tmpCollection){
                         if(in != tmpCollection.get(0)){
                             tmp += ", " + in;
                         }
                         else{
                             tmp += in;
                         }
                     }
                    
                 }
                 queryDetails.add("Mutation Status: " + tmp);
                 tmpCollection.clear();
                 tmp = "";    
                              
                 if(criteria.getMutationTypeCollection()!=null && !criteria.getMutationTypeCollection().isEmpty()){
                     Set<String> mutationType = (Set<String>) criteria.getMutationTypeCollection();                    
                     tmpCollection = new ArrayList<String>(mutationType);
                     for(String lc : tmpCollection){
                         if(lc != tmpCollection.get(0)){
                             tmp += ", " + lc;
                         }
                         else{
                             tmp += lc;
                         }
                     }
                    
                 }
                 queryDetails.add("Mutation Type: " + tmp); 
                 tmpCollection.clear();
                 tmp = "";
             }
             
             String qd = "";
             for(String q : queryDetails){
                 qd += q + " ||| ";
             }
             
             /* **************************************************************************************************************
              * 
              * BUILD REPORT AS XML
              * 
              * **************************************************************************************************************
              */
             
             //GET FINDINGS, CAST TO APPROPRIATE FINDINGS AND ADD TO ARRAYLIST (RESULTS)
             ArrayList dFindings = new ArrayList(finding.getDomainFindings());
             ArrayList<P53MutationFinding> domainFindings = new ArrayList<P53MutationFinding>(dFindings);
             ArrayList<P53FindingReportBean> results = new ArrayList<P53FindingReportBean>();
             for(P53MutationFinding p53f : domainFindings)  {
             	P53FindingReportBean reportBean = new P53FindingReportBean(p53f);
                 results.add(reportBean);
             }
             
             
             if(!results.isEmpty())  {      	
                 
                 
                 //SORT THE ARRAYLIST(RESULTS) BY PATIENT DID
             	
                 PatientComparator p = new PatientComparator();
                 Collections.sort(results, p);
                
                 
                 //CREATE A HASHMAP SORTED BY PATIENT DID AS THE KEY AND THE ARRAYLIST OF REPORTBEANS AS THE VALUE                
                  
               //  Map<String,ArrayList<P53FindingReportBean>> reportBeanMap = new HashMap<String,ArrayList<P53FindingReportBean>>();
                 Map<String,ArrayList<P53FindingReportBean>> reportBeanMap = new HashMap<String,ArrayList<P53FindingReportBean>>();
                 
                 int j= results.size();
                 for(int i =0; i<results.size();i++){
                     boolean found = false;
                     Set<String> keys = reportBeanMap.keySet();
                     int key = keys.size();
                     if(!keys.isEmpty()){
                         for(String k:keys){
                             String s = results.get(i).getSpecimenIdentifier();
                             if(s.equals(k)){
                                 found = true;
                                 reportBeanMap.get(k).add(results.get(i));
                                 break;
                             }
                         }
                         if(!found){
                             reportBeanMap.put(results.get(i).getSpecimenIdentifier(),new ArrayList<P53FindingReportBean>());
                             reportBeanMap.get(results.get(i).getSpecimenIdentifier()).add(results.get(i));
                         }
                     }
                     else{
                         reportBeanMap.put(results.get(i).getSpecimenIdentifier(),new ArrayList<P53FindingReportBean>());
                         reportBeanMap.get(results.get(i).getSpecimenIdentifier()).add(results.get(i));
                     }
 //                   
                          
                 }
                  
                 
                 //IF THE USER SELECTED TIMEPOINTS FOR WHICH THAT PATIENT DID DID NOT HAVE DATA, CREATE NULL BEANS SO AS TO RENDER A READABLE REPORT
                 Set<String> b = reportBeanMap.keySet();
                         for(String g: b){
                             while(reportBeanMap.get(g).size()<(reportBeanMap.get(g).get(0).getTimepointHeaders(criteria).size())){
                                 reportBeanMap.get(g).add(new P53FindingReportBean(new P53MutationFinding()));
                             }
                         }
                 
                 //ADD QUERY DETAILS      
                 Element details = report.addElement("Query_details");                
                 cell = details.addElement("Data");
                 cell.addText(qd);
                 cell = null;
                 
                 //ADD HEADERS THAT ARE TIMEPOINT-INDEPENDENT (PATIENT)
                 Element headerRow = report.addElement("Row").addAttribute("name", "headerRow");
                 ArrayList<String> ntpHeaders = results.get(0).getNonTimepointHeaders();
                 for(String ntpHeader : ntpHeaders){
                 	
                 	// on the report page, should not display labtrak id
                     if(!ntpHeader.equalsIgnoreCase("LabTrak ID")){
                       cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "ntpHeader");
                       data = cell.addElement("Data").addAttribute("type", "header").addText(ntpHeader);
                       data = null;
                       cell = null;    
                     }
                 }
                 
                 //ADD HEADERS THAT ARE TIMEPOINT DEPENDENT
                 ArrayList<String> headers = results.get(0).getHeaders();                
                 for(String header : headers){   
                 	
                     cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
                         data = cell.addElement("Data").addAttribute("type", "header").addText(header);
                         data = null;
                         cell = null;                
                 }
               
                 
                 //ADD TIMEPOINT SUBHEADERS
                 ArrayList<String> tpHeaders = results.get(0).getTimepointHeaders(criteria);
                
                 TimepointStringComparator ts = new TimepointStringComparator();
                 Collections.sort(tpHeaders,ts);   
                 Element tpHeaderRow = report.addElement("Row").addAttribute("name", "tpHeaderRow");
             
                	 // this is leave the first space empty
                   
                    cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "ntpHeader");
                    data = cell.addElement("Data").addAttribute("type", "header").addText("");
                    System.out.println(")))"+data.toString());
                     data = null;
                    cell = null;    
                    
             
                for(int i=0; i<tpHeaders.size();i++){                	
                	
                	
                    cell = tpHeaderRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "firstTP").addAttribute("group", "tpHeader");                       
                     data = cell.addElement("Data").addAttribute("type", "header").addText(tpHeaders.get(i));                    
                     data = null;
                     cell = null;
                 }
              
                   Set<String> keys = reportBeanMap.keySet();   
                   
                  
                 // ADD DATA ROWS           
                    for(String key : keys) {
                 	   
                 	  String tp = reportBeanMap.get(key).get(0).getTimepoint();
                 	  Collection<String>   mutationStatusCollection = criteria.getMutationStatusCollection();
                 	  Collection<String>   mutationTypeCollection = criteria.getMutationTypeCollection();
                  	 
                 	  String mutationStatus = reportBeanMap.get(key).get(0).getMutationStatus();
                 	  String mutationType = reportBeanMap.get(key).get(0).getMutationType();
                 	   
                 	  if(tpHeaders.contains(tp) 
                 			  && ( mutationStatusCollection ==null || (mutationStatusCollection!= null &&mutationStatusCollection.contains(mutationStatus)))
                 		      && ( mutationTypeCollection ==null || (mutationTypeCollection!= null &&mutationTypeCollection.contains(mutationType)))) {               		  
                 		  
                 	   
                                       dataRow = report.addElement("Row").addAttribute("name", "dataRow");                         
                                        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "sample").addAttribute("group", "data");
                                            data = cell.addElement("Data").addAttribute("type", "selectable").addText(reportBeanMap.get(key).get(0).getPatientDID());
                                            data = null;
                                        cell = null;                                               
                                       
                                        
                                         //GRAB EACH REPORT BEAN IN EACH ARRAYLIST AND MATCH UP TO THE APPROPRIATE TIMEPOINT AS A MAP WITH THE TIMEPOINT AS KEY AND REPORTBEAN THE VALUE
                                         ArrayList<P53FindingReportBean> myList = reportBeanMap.get(key);
                                         Map<String,P53FindingReportBean> myMap = new HashMap<String,P53FindingReportBean>();
                                         ArrayList<P53FindingReportBean> mySortedMap = new ArrayList<P53FindingReportBean>();
                                         
                                         for(P53FindingReportBean ggg : myList){
                                             for(int i=0; i<tpHeaders.size();i++){
                                                 if(ggg.getTimepoint().equalsIgnoreCase(tpHeaders.get(i))){
                                                     myMap.put(tpHeaders.get(i),ggg);
                                                     break;
                                                 }
                                                 else if(ggg.getTimepoint().equals("--")){
                                                     if(!myMap.containsKey(tpHeaders.get(i))){
                                                         myMap.put(tpHeaders.get(i),ggg);
                                                         break;
                                                     }                                                    
                                                 }
                                             }
                                           }
                                         
                                         //SORT MAP BY TIMEPOINT SO THAT THE REPORT BEAN DATA CAN EASILY BE DISPLAYED UNDER THE APPROPRIATE TIEMPOINT
                                         for(int i=0; i<tpHeaders.size();i++){
                                             for(String k : myMap.keySet()){
                                                 if(k.equalsIgnoreCase(tpHeaders.get(i))){
                                                     mySortedMap.add(myMap.get(k));
                                                 }
                                             }
                                         }
                                             
                                       
                                         //ITERATE OVER THE MAP FOR EACH DATA FIELD WITH ITS CORRESPONDING TIMEPOINT AND BUILD DATA ROWS
                                         for(P53FindingReportBean reportBean : mySortedMap) {
                                                 
                                         	cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getMutationStatus());                                        
                                             data = null;
                                             cell = null;
                                         }
                                         for(P53FindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getMutationType());
                                             data = null;
                                             cell = null;
                                            
                                        }
                                        for(P53FindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getExonOrIntronLocation());
                                             data = null;
                                             cell = null;
                                           
                                            
                                        }
                                         for(P53FindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getBaseChange());
                                             data = null;
                                             cell = null;
                                            
                                         }
                                         for(P53FindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getCodonAminoacidChange());
                                             data = null;
                                             cell = null;
                                            
                                             }
                                         for(P53FindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getProteinStructuralDomain());
                                             data = null;
                                             cell = null;
                                            
                                             }
                                     }
                 	       } 
                                 
             }
             
             else {
                 //TODO: handle this error
                 sb.append("<br><Br>Level of Expression is empty<br>");
             }
             
             return document;
     }
     
     public static HSSFWorkbook getReportExcel(Finding finding, HashMap map) {
         HSSFWorkbook wb = new HSSFWorkbook();
         HSSFSheet sheet = wb.createSheet(finding.getTaskId());
         
         ArrayList dFindings = new ArrayList(finding.getDomainFindings());
         ArrayList<P53MutationFinding> domainFindings = new ArrayList<P53MutationFinding>(dFindings);
         ArrayList<P53FindingReportBean> results = new ArrayList<P53FindingReportBean>();
         for(P53MutationFinding p53f : domainFindings)  {
         	P53FindingReportBean reportBean = new P53FindingReportBean(p53f);
             results.add(reportBean);
         }
         P53FindingCriteria criteria = (P53FindingCriteria)finding.getQueryDTO();
         
         if(!results.isEmpty())  {
             
             //SORT THE ARRAYLIST(RESULTS) BY PATIENT DID
            PatientComparator p = new PatientComparator();
            Collections.sort(results, p);
             
            
          
          
             
             //CREATE A HASHMAP SORTED BY PATIENT DID AS THE KEY AND THE ARRAYLIST OF REPORTBEANS AS THE VALUE                
              
             Map<String,ArrayList<P53FindingReportBean>> reportBeanMap = new HashMap<String,ArrayList<P53FindingReportBean>>();
             
             for(int i =0; i<results.size();i++){
                 if(i==0){
                     reportBeanMap.put(results.get(i).getSpecimenIdentifier(),new ArrayList<P53FindingReportBean>());
                     reportBeanMap.get(results.get(i).getSpecimenIdentifier()).add(results.get(i));                        
                 }
                 else if(!results.get(i).getSpecimenIdentifier().equalsIgnoreCase(results.get(i-1).getSpecimenIdentifier())){ 
                     reportBeanMap.put(results.get(i).getSpecimenIdentifier(),new ArrayList<P53FindingReportBean>());
                     reportBeanMap.get(results.get(i).getSpecimenIdentifier()).add(results.get(i));
                 }
                 else if(results.get(i).getSpecimenIdentifier().equalsIgnoreCase(results.get(i-1).getSpecimenIdentifier())){ 
                     reportBeanMap.put(results.get(i).getSpecimenIdentifier(),new ArrayList<P53FindingReportBean>());
                     reportBeanMap.get(results.get(i).getSpecimenIdentifier()).add(results.get(i));
                 }
                 else{
                     reportBeanMap.get(results.get(i-1).getSpecimenIdentifier()).add(results.get(i));                        
                 }
             }
             
           
             //IF THE USER SELECTED TIMEPOINTS FOR WHICH THAT PATIENT DID DID NOT HAVE DATA, CREATE NULL BEANS SO AS TO RENDER A READABLE REPORT
             Set<String> b = reportBeanMap.keySet();
                     for(String g: b){
                         while(reportBeanMap.get(g).size()<(reportBeanMap.get(g).get(0).getTimepointHeaders(criteria).size())){
                             reportBeanMap.get(g).add(new P53FindingReportBean(new P53MutationFinding()));
                         }
                     }
                     
             ArrayList<String> ntpHeaders = results.get(0).getNonTimepointHeaders();            
             HSSFRow row = sheet.createRow((short) 0);
             
             
             //ADD HEADERS THAT ARE TIMEPOINT DEPENDENT
             ArrayList<String> headers = results.get(0).getHeaders(); 
             
             ArrayList<String> tpHeaders = results.get(0).getTimepointHeaders(criteria);
             TimepointStringComparator ts = new TimepointStringComparator();
             Collections.sort(tpHeaders,ts);
             ArrayList<String> combinedHeaders = new ArrayList<String>(); 
             for(int i=0; i<headers.size();i++){
                 for(int j=0; j<tpHeaders.size();j++){
                     combinedHeaders.add(headers.get(i)+tpHeaders.get(j));
                 }
             }
             
             ntpHeaders.addAll(combinedHeaders);
             
             for(int i = 0; i<ntpHeaders.size();i++){
                 HSSFCell cell = row.createCell((short) i);
                 cell.setCellValue(ntpHeaders.get(i));                   
             }
             
             row = null;
             HSSFCell dataCell = null;
             Set<String> keysSet = reportBeanMap.keySet(); 
             ArrayList<String> keys = new ArrayList<String>(keysSet);
             int u=0;
             // ADD DATA ROWS           
                for(int i=0;i<keys.size();i++) {  
             	      String tp = reportBeanMap.get(keys.get(i)).get(0).getTimepoint();    
             	      Collection<String>   mutationStatusCollection = criteria.getMutationStatusCollection();
                 	  Collection<String>   mutationTypeCollection = criteria.getMutationTypeCollection();
                  	 
                 	  String mutationStatus=reportBeanMap.get(keys.get(i)).get(0).getMutationStatus();
                 	  String mutationType=reportBeanMap.get(keys.get(i)).get(0).getMutationType();
                 	
             	      if(tpHeaders.contains(tp) 
                 			  && ( mutationStatusCollection ==null || (mutationStatusCollection!= null &&mutationStatusCollection.contains(mutationStatus)))
                 		      && ( mutationTypeCollection ==null || (mutationTypeCollection!= null &&mutationTypeCollection.contains(mutationType)))) {
                     		  
                 		  
                                    
                                    sheet.createFreezePane( 0, 1, 0, 1 );
                                    row = sheet.createRow((short) u + 1); 
                                    dataCell = row.createCell((short) 0);
                                    dataCell.setCellValue(reportBeanMap.get(keys.get(i)).get(0).getPatientDID());
                                    
                                    dataCell = row.createCell((short) 1);
                                    dataCell.setCellValue(reportBeanMap.get(keys.get(i)).get(0).getSpecimenIdentifier());
                                    
                                   
                
                                     //GRAB EACH REPORT BEAN IN EACH ARRAYLIST AND MATCH UP TO THE APPROPRIATE TIMEPOINT AS A MAP WITH THE TIMEPOINT AS KEY AND REPORTBEAN THE VALUE
                                     ArrayList<P53FindingReportBean> myList = reportBeanMap.get(keys.get(i));
                                     Map<String,P53FindingReportBean> myMap = new HashMap<String,P53FindingReportBean>();
                                     ArrayList<P53FindingReportBean> mySortedMap = new ArrayList<P53FindingReportBean>();
                                     
                                     for(P53FindingReportBean ggg : myList){
                                         for(int j=0; j<tpHeaders.size();j++){
                                             if(ggg.getTimepoint().equalsIgnoreCase(tpHeaders.get(j))){
                                                 myMap.put(tpHeaders.get(j),ggg);
                                                 break;
                                             }
                                             else if(ggg.getTimepoint().equals("--")){
                                                 if(!myMap.containsKey(tpHeaders.get(j))){
                                                     myMap.put(tpHeaders.get(j),ggg);
                                                     break;
                                                 }                                                    
                                             }
                                         }
                                     }
                                     
                                     //SORT MAP BY TIMEPOINT SO THAT THE REPORT BEAN DATA CAN EASILY BE DISPLAYED UNDER THE APPROPRIATE TIEMPOINT
                                     for(int t=0; t<tpHeaders.size();t++){
                                         for(String k : myMap.keySet()){
                                             if(k.equalsIgnoreCase(tpHeaders.get(t))){
                                                 mySortedMap.add(myMap.get(k));
                                             }
                                         }
                                     }
                                     
                                   
                                         
                                    int counter = 2;
                                     //ITERATE OVER THE MAP FOR EACH DATA FIELD WITH ITS CORRESPONDING TIMEPOINT AND BUILD DATA ROWS
                                   
                                     for(P53FindingReportBean reportBean : mySortedMap) {                                                   
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getMutationStatus());                                        
                                     }
                                     for(P53FindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getMutationType()); 
                                     }
                                     for(P53FindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getExonOrIntronLocation()); 
                                     }
                                     for(P53FindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getBaseChange()); 
                                     }
                                     for(P53FindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getCodonAminoacidChange()); 
                                     }
                                     for(P53FindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getProteinStructuralDomain()); 
                                     }
                                     u++;
                    
                              } 
                    }
         
         }
         
         else {
             //TODO: handle this error
            
         }
             
        
 
         
         return wb;
     }
 
 }
 
 
 
 
 
 
