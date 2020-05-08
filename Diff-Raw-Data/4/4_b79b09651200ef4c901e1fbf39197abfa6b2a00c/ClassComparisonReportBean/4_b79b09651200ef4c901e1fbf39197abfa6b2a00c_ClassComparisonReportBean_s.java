 package gov.nih.nci.eagle.web.reports;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import gov.nih.nci.caintegrator.analysis.messaging.ClassComparisonResultEntry;
 
 public class ClassComparisonReportBean implements ReportBean{
 
     private ClassComparisonResultEntry entry;
     private String geneSymbol;
     
     public ClassComparisonReportBean(ClassComparisonResultEntry entry, String hugoGeneSymbol) {
         this.entry = entry;
         this.geneSymbol = hugoGeneSymbol;
     }
     
     public String getReporterId() {
         return entry.getReporterId();
     }
     
     public Double getPvalue() {
         return entry.getPvalue();
     }
     
     public Double getMeanBaselineGrp() {
         return entry.getMeanBaselineGrp();
     }
     
     public Double getMeanGrp1() {
         return entry.getMeanGrp1();
     }
     
    public Double getAbsoluteFoldChange() {
        return entry.getAbsoluteFoldChange();
     }
     
     public String getGeneSymbol() {
         return geneSymbol;
     }
     
     public List getRow()	{
     	List row = new ArrayList();
     	row.add(entry.getReporterId());
     	row.add(entry.getPvalue());
     	row.add(entry.getMeanBaselineGrp());
     	row.add(entry.getMeanGrp1());
     	row.add(entry.getFoldChange());
     	row.add(this.getGeneSymbol());
     	return row; 
     }
     
     public List getRowLabels()	{
     	//this is here only to keep the CSV column config in one class
        	List row = new ArrayList();
     	row.add("Reporter");
     	row.add("p-value");
     	row.add("mean_baseline");
     	row.add("mean_group1");
     	row.add("fold change");
     	row.add("gene symbol");
     	return row; 
     }
 
 }
