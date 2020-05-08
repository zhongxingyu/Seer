 /**
  *
  */
 package org.diveintojee.poc.stories;
 
 import org.diveintojee.poc.steps.ContinuitySteps;
 import org.diveintojee.poc.steps.Exchange;
import org.diveintojee.poc.web.SearchClassifiedsResource;
 import org.jbehave.core.io.CodeLocations;
 import org.jbehave.core.io.StoryFinder;
 import org.jbehave.core.steps.InjectableStepsFactory;
 import org.jbehave.core.steps.InstanceStepsFactory;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author louis.gueye@gmail.com
  */
 public class ContinuityStory extends AbstractJUnitStories {
 
     @Override
     public InjectableStepsFactory stepsFactory() {
         return new InstanceStepsFactory(configuration(), new ContinuitySteps(new Exchange()));
     }
 
     @Override
     protected List<String> storyPaths() {
        List<String> paths = new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(
                SearchClassifiedsResource.class).getFile(),
                 Arrays.asList("**/continuity.story"), null);
         return paths;
     }
 }
