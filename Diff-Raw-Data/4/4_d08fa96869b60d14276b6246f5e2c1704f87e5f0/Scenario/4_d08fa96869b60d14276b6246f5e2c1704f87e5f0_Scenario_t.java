 package org.jbehave.scenario;
 
 import org.jbehave.Configuration;
 import org.jbehave.scenario.steps.Steps;
 import org.junit.Test;
 
 /**
  * <p>Extend this class to run your scenario. Call the class after your
  * scenario, eg: "ICanLogin".
  * 
 * <p>The Scenario should be in a matching text file in the same place, 
 * eg "i_can_login".
  * 
  * <p>Write some steps in your scenario, starting each new step with
  * Given, When, Then or And.
  * 
  * <p>Then look at the Steps class.
  */
 public abstract class Scenario {
 
     private final Steps[] candidateSteps;
     private final ScenarioRunner scenarioRunner;
     private final Configuration configuration;
 
     public Scenario(Steps... candidateSteps) {
         this(new PropertyBasedConfiguration(), candidateSteps);
     }
 
     public Scenario(Configuration configuration, Steps... candidateSteps) {
         this(new ScenarioRunner(), configuration, candidateSteps);
     }
     
     
     public Scenario(ScenarioRunner scenarioRunner, Configuration configuration, Steps... candidateSteps) {
         this.configuration = configuration;
         this.scenarioRunner = scenarioRunner;
         this.candidateSteps = candidateSteps;
     }
 
     @Test
     public void run() throws Throwable {
         StoryDefinition story = configuration.forDefiningScenarios().loadScenarioDefinitionsFor(this.getClass());
         scenarioRunner.run(story, configuration, candidateSteps);
     }
 }
