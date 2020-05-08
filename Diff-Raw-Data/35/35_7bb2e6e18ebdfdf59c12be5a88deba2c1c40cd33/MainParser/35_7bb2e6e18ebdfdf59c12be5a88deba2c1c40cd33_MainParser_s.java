 package de.hswt.hrm.report.latex;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.util.Collection;
 import java.util.Map;
 
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.PhysicalRating;
 import de.hswt.hrm.report.latex.collectors.TargetPerformancePhotoStates;
 
 public class MainParser {
 
     private FileWriter fileWriter;
 
     /*
      * initialized with constructor params
      */
     private Collection<Map<Attribute, String>> componentAttributes;
     private String path;
    private Collection<TargetPerformancePhotoStates> componentsTable;
     private Inspection inspection;
     private Collection<PhysicalRating> ratings;
 
     /*
      * Constructor
      */
     public MainParser(String path, Inspection inspection,
             Collection<TargetPerformancePhotoStates> componentsTable,
             Collection<Map<Attribute, String>> componentAttributes,
            Collection<PhysicalRating> ratings) {
         this.path = path;
        this.componentsTable = componentsTable;
         this.inspection = inspection;
         this.componentAttributes = componentAttributes;
        this.ratings = ratings;
 
     }
 
    public void parse() throws IOException {
         path = FileSystems.getDefault().getPath(path, "inputs").toString();
         fileWriter = new FileWriter();
         fileWriter.writeToFile(path, "overallevaluation.tex", new OverallEvaluationParser(
                 inspection).parse());
         fileWriter.writeToFile(path, "physicalvaluation.tex", new PhysicalEvaluationParser(path,
                 ratings, inspection).parse());
         fileWriter.writeToFile(path, "reportdata.tex",
                 new ReportDataParser(path, inspection).parse());
         fileWriter.writeToFile(path, "targetperformancecomparison.tex",
                new TargetPerformanceComparisonParser(path, componentsTable).parse());
         fileWriter.writeToFile(path, "components.tex", new ComponentsParser(path, inspection,
                 componentAttributes).parse());
 
     }

 }
