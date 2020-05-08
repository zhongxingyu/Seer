 package org.katta.labs.metrics.toxicity;
 
 import org.katta.labs.metrics.toxicity.check.Check;
 import org.katta.labs.metrics.toxicity.check.Checks;
 import org.katta.labs.metrics.toxicity.domain.Checkstyle;
 import org.katta.labs.metrics.toxicity.util.FileUtil;
 import org.katta.labs.metrics.toxicity.util.JAXBUtil;
 import org.katta.labs.metrics.toxicity.util.StringUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class ToxicityCalculator {
 
     private String checkstyleFilePath;
 
     public ToxicityCalculator(String checkstyleFilePath) {
         this.checkstyleFilePath = checkstyleFilePath;
     }
 
     public static void main(String... args) {
         if (args == null || args.length != 2) {
             System.out.println("Usage: toxicity.jar <checkstyleFilePath> <outputCSVFilePath>");
            System.out.println("Calculates the toxicity of your code based on the checkstyle report generated for your code.");
            System.out.println("To calculate a suitable Checkstyle report use:");
            System.out.println("   checkstyle-all.jar -c toxicity/checks.xml -f xml -o <checkstyleFilePath> -r <javaSourcePath>");
             System.exit(1);
            return;
         }
 
         ToxicityCalculator calculator = new ToxicityCalculator(args[0]);
         Map<String, Map<String, Double>> values = calculator.calculate();
 
         System.out.println(calculator.summary(values));
         FileUtil.write(args[1], calculator.toCsv(values));
     }
 
 
     public Map<String, Map<String, Double>> calculate() {
         Checkstyle checkstyle = loadCheckstyle(checkstyleFilePath);
         return checkstyle.getFiles().calculateToxicValue();
     }
 
     String summary(Map<String, Map<String, Double>> toxicValues) {
         Checks allChecks = Checks.all();
         double totalToxicity = 0.0;
         for (Map<String, Double> values : toxicValues.values()) {
             for (String checkName : values.keySet()) {
                 Double toxicity = values.get(checkName);
                 allChecks.find(checkName).addToxicValue(toxicity);
                 totalToxicity += toxicity;
             }
         }
         return allChecks.toString() + "\nTotal Toxicity :" + totalToxicity;
     }
 
     String toCsv(Map<String, Map<String, Double>> toxicValues) {
 
         StringBuilder csv = new StringBuilder();
 
         Checks checks = Checks.all();
         csv.append("FileName,").append(checks.toCSV()).append("\n");
 
         for (String file : toxicValues.keySet()) {
             List<String> values = new ArrayList<String>();
             values.add(file);
 
             for (Check check : checks) {
                 values.add(toxicValues.get(file).get(check.getName()).toString());
             }
             csv.append(StringUtil.join(",", values)).append("\n");
         }
         return csv.toString();
 
     }
 
     private Checkstyle loadCheckstyle(String filePath) {
         return JAXBUtil.load(Checkstyle.class, filePath);
     }
 
 }
