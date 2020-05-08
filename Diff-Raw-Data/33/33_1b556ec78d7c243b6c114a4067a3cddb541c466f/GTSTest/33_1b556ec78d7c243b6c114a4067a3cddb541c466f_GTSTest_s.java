 package org.cagrid.gts.test;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.ExamReactorStrategy;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
 
 @RunWith(JUnit4TestRunner.class)
 @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
 public class GTSTest extends CaGridTestSupport {
 


     @Override
     @Configuration
     public Option[] config() {
        Option[] options = new Option[] { 
                //put Custom stuff here
         };
         return CaGridTestSupport.concatAll(super.config(), options);
     }
 
     @Test
     public void testGTS() throws Exception {
         // System.err.println(executeCommand("features:addurl " + gtsFeaturesURL));
         System.err.println(executeCommand("features:listurl"));
        //System.err.println(executeCommand("features:list"));
        
        //installAndAssertFeature("cagrid-third-party");
        //installAndAssertFeature("wsrf-draft");
        installAndAssertFeature("cagrid-gts");
         assertBundleInstalled("cagrid-gts-service");
     }
 
 }
