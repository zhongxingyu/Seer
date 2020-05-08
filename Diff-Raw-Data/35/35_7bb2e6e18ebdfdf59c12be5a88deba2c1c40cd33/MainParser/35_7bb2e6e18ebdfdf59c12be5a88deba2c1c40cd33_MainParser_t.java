 package de.hswt.hrm.report.latex;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.util.Collection;
 import java.util.Map;
 
import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.component.model.Attribute;
import de.hswt.hrm.component.service.ComponentService;
 import de.hswt.hrm.inspection.model.Inspection;
import de.hswt.hrm.inspection.model.Performance;
 import de.hswt.hrm.inspection.model.PhysicalRating;
 import de.hswt.hrm.report.latex.collectors.TargetPerformancePhotoStates;
import de.hswt.hrm.inspection.service.InspectionService;
 
 public class MainParser {
 
     private FileWriter fileWriter;
 
     /*
      * initialized with constructor params
      */
     private Collection<Map<Attribute, String>> componentAttributes;
     private String path;
    private Collection<Performance> componentsTable;
     private Inspection inspection;
     private Collection<PhysicalRating> ratings;
    private ComponentService compService;
    private InspectionService insService;
 
     /*
      * Constructor
      */
     public MainParser(String path, Inspection inspection,
             Collection<TargetPerformancePhotoStates> componentsTable,
             Collection<Map<Attribute, String>> componentAttributes,
            Collection<PhysicalRating> ratings, InspectionService insService,
            ComponentService compService) throws DatabaseException {
         this.path = path;

        this.componentsTable = insService.findPerformance(inspection);
         this.inspection = inspection;
         this.componentAttributes = componentAttributes;
        this.ratings = insService.findPhysicalRating(inspection);
        this.compService = compService;
        this.insService = insService;
 
     }
 
    public void parse() throws IOException, DatabaseException {
         path = FileSystems.getDefault().getPath(path, "inputs").toString();
         fileWriter = new FileWriter();
         fileWriter.writeToFile(path, "overallevaluation.tex", new OverallEvaluationParser(
                 inspection).parse());
         fileWriter.writeToFile(path, "physicalvaluation.tex", new PhysicalEvaluationParser(path,
                 ratings, inspection).parse());
         fileWriter.writeToFile(path, "reportdata.tex",
                 new ReportDataParser(path, inspection).parse());
         fileWriter.writeToFile(path, "targetperformancecomparison.tex",
                new TargetPerformanceComparisonParser(path, componentsTable, insService,
                        inspection, compService).parse());
         fileWriter.writeToFile(path, "components.tex", new ComponentsParser(path, inspection,
                 componentAttributes).parse());
 
     }
 }
