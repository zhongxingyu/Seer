 package fr.xebia.training.jbehave.part4;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jbehave.core.configuration.Configuration;
 import org.jbehave.core.configuration.MostUsefulConfiguration;
 import org.jbehave.core.failures.FailingUponPendingStep;
 import org.jbehave.core.io.CodeLocations;
 import org.jbehave.core.io.LoadFromClasspath;
 import org.jbehave.core.io.LoadFromRelativeFile;
 import org.jbehave.core.junit.JUnitStories;
 import org.jbehave.core.reporters.StoryReporterBuilder;
 import org.jbehave.core.reporters.StoryReporterBuilder.Format;
 import org.jbehave.core.steps.CandidateSteps;
 import org.jbehave.core.steps.InstanceStepsFactory;
 
 import fr.xebia.training.jbehave.part4.finished.P4FTestBoardStep;
 
 public class P4TestBoard extends JUnitStories {
 
 	public Configuration configuration() {
         return new MostUsefulConfiguration()
         	.usePendingStepStrategy(new FailingUponPendingStep())
         	.useStoryLoader(new LoadFromRelativeFile(CodeLocations.codeLocationFromPath("src/test/resources/part4")))
             .useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats().withFormats(Format.CONSOLE)); 
     }
  
     @Override
     public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new P4TestBoardStep()).createCandidateSteps();
     }
 
 	@Override
 	protected List<String> storyPaths() {
 		List<String> stories = new ArrayList<String>();
 		stories.add("test_board_with_one_cell.story");
 		stories.add("test_board_with_many_cell.story");
 		stories.add("test_board_with_many_cell_with_alias.story");
 		stories.add("test_empty_board.story");
 		return stories;
 	}
 
     
 	
 	
 }
 
