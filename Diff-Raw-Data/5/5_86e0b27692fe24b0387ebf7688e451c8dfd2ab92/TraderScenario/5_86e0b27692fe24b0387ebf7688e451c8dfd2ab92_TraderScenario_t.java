 package org.jbehave.examples.trader;
 
 import org.jbehave.scenario.JUnitScenario;
 import org.jbehave.scenario.PropertyBasedConfiguration;
 import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
 import org.jbehave.scenario.parser.PatternScenarioParser;
 import org.jbehave.scenario.parser.ScenarioDefiner;
 import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
 import org.jbehave.scenario.reporters.CollectingScenarioReporter;
 import org.jbehave.scenario.reporters.FilePrintStreamFactory;
 import org.jbehave.scenario.reporters.HtmlPrintStreamScenarioReporter;
 import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;
 import org.jbehave.scenario.reporters.ScenarioReporter;
 
 public class TraderScenario extends JUnitScenario {
 
    public TraderScenario(final Class<?> thisClass) {
         super(new PropertyBasedConfiguration() {
             @Override
             public ScenarioDefiner forDefiningScenarios() {
                 return new ClasspathScenarioDefiner(new UnderscoredCamelCaseResolver(".scenario"), new PatternScenarioParser(this));
             }
 
             @Override
             public ScenarioReporter forReportingScenarios() {
                 return new CollectingScenarioReporter(new PrintStreamScenarioReporter(), new HtmlPrintStreamScenarioReporter(new FilePrintStreamFactory(thisClass)));
             }
 
         }, new TraderSteps());
     }
 
 }
