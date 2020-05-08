 package ro.utcluj.ecsb;
 
 import org.apache.log4j.Logger;
 import ro.utcluj.ecsb.utils.EcsbClassifiers;
 import ro.utcluj.ecsb.utils.EcsbFactory;
 import ro.utcluj.ecsb.utils.EcsbUtils;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.Properties;
 
 public class DecsbTests {
     public static void main(String[] args) {
         try {
             boolean distributed = false;
             if (args.length > 0) {
                 distributed = true;
                 ECSB.CONF_PATH = args[0];
             }
             for (EcsbClassifiers classifier : EcsbClassifiers.values()) {
                 Logger.getLogger(DecsbTests.class).info(classifier.getClassName() + " - starting time:" + new Timestamp(System.currentTimeMillis()));
                 Properties configuration = EcsbUtils.loadConfiguration(distributed, ECSB.CONF_PATH + "decsb.properties");
                 overwriteConfiguration(configuration, classifier);

                EcsbUtils.initLogger(distributed, "nd_" + classifier.getClassName() + ".txt");
                 final ECSB ecsb = new EcsbFactory(configuration).setUpECSB(distributed);
                 ecsb.runEvolutionaryCostSensitiveBalancing();
                 Logger.getLogger(DecsbTests.class).info(classifier.getClassName() + " - end time:" + new Timestamp(System.currentTimeMillis()));
             }
         } catch (IOException e) {
             Logger.getLogger("ECSBLog").error("Unable to load configuration file.");
         }
 
     }
 
     private static void overwriteConfiguration(Properties configuration, EcsbClassifiers classifier) {
 
         configuration.setProperty("base_classifier", classifier.getClassName());
         int splitsNumber;
         if (configuration.getProperty("dataset_path").contains("sick")) {
             if (classifier.equals(EcsbClassifiers.IBk) || classifier.equals(EcsbClassifiers.J48)) {
                 splitsNumber = 3;
             } else if (classifier.equals(EcsbClassifiers.AdaBoostM1)) {
                 splitsNumber = 6;
             } else {
                 splitsNumber = 8;
 
             }
         } else {
             if (classifier.equals(EcsbClassifiers.IBk) || classifier.equals(EcsbClassifiers.J48)) {
                 splitsNumber = 4;
             } else if (classifier.equals(EcsbClassifiers.AdaBoostM1)) {
                 splitsNumber = 7;
             } else {
                 splitsNumber = 10;
 
             }
         }
         configuration.setProperty("train.splits.number", String.valueOf(splitsNumber));
     }
 }
