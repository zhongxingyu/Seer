 package hudson.plugins.coverage.model;
 
 import hudson.plugins.coverage.model.measurements.BranchCoverage;
 import hudson.plugins.coverage.model.measurements.LineCoverage;
 
 /**
  * The standard model for calculating project level results.
  *
  * @author Stephen Connolly
  * @since 26-Jun-2008 23:57:28
  */
 public final class StandardModel implements Model {
 
     private StandardModel() {
     }
 
     public static StandardModel getInstance() {
        return SingletonHolder.INSTANCE;
     }
 
     public void apply(Instance instance) {
         int lineCount = 0;
         int lineCover = 0;
         int branchCount = 0;
         int branchCover = 0;
         for (Element element : instance.getChildElements()) {
             for (Instance child : instance.getChildren(element).values()) {
                 LineCoverage lineCoverage = (LineCoverage) child.getMeasurement(Metric.LINE_COVERAGE);
                 if (lineCoverage != null) {
                     lineCount += lineCoverage.getCount();
                     lineCover += lineCoverage.getCover();
                 }
                 BranchCoverage branchCoverage = (BranchCoverage) child.getMeasurement(Metric.BRANCH_COVERAGE);
                 if (branchCoverage != null) {
                     branchCount += branchCoverage.getCount();
                     branchCover += branchCoverage.getCover();
                 }
             }
         }
         instance.setMeasurement(Metric.LINE_COVERAGE, new LineCoverage(lineCount, lineCover));
         instance.setMeasurement(Metric.BRANCH_COVERAGE, new BranchCoverage(branchCount, branchCover));
     }
 
    private static final class SingletonHolder {
         private static final StandardModel INSTANCE = new StandardModel();
     }
 }
