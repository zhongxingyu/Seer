 package org.whitesource.bamboo.agent;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.mockito.Mockito.mock;
 
 import java.io.File;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.whitesource.agent.api.model.AgentProjectInfo;
 
 import com.atlassian.bamboo.build.logger.BuildLogger;
 
 public class GenericOssInfoExtractorTest
 {
     private static BuildLogger buildLogger;
     private static File testDirectory;
     protected static final String PROJECT_NAME = "TestPojectName";
     protected static final String PROJECT_TOKEN = "TestProjectToken";
     protected static final String PATTERN_NONE = "";
     protected static final String PATTERN_ALL = "lib/*.jar";
    protected static final int NUM_ALL = 9; // REVIEW: maybe derive this number dynamically in setUp(), or maybe not?
     protected static final String PATTERN_WSS = "lib/wss*.jar";
     protected static final int NUM_WSS = 3;
     protected static final String dependenciesMismatch = "Number of dependencies doesn't match, expected:<[%s]> but was:<[%s]> - have you added/removed any dependencies?";
 
     @Before
     public void setUp() throws Exception
     {
         buildLogger = mock(BuildLogger.class);
        testDirectory = new File("target/test-jars");
     }
 
     @Test
     public void testExtractOssInfoIsDirectory()
     {
         assertThat(testDirectory).isDirectory();
     }
 
     @Test
     public void testExtractOssInfoDefault()
     {
         BaseOssInfoExtractor extractor = new GenericOssInfoExtractor(PROJECT_NAME, PROJECT_TOKEN, PATTERN_ALL,
                 PATTERN_NONE, testDirectory, buildLogger);
         Collection<AgentProjectInfo> projectInfos = extractor.extract();
         assertThat(projectInfos.size()).isEqualTo(1);
         int actual = projectInfos.iterator().next().getDependencies().size();
         assertThat(actual).overridingErrorMessage(dependenciesMismatch, NUM_ALL, actual).isEqualTo(NUM_ALL);
     }
 
     @Test
     public void testExtractOssInfoDefaultWithIncludes()
     {
         BaseOssInfoExtractor extractor = new GenericOssInfoExtractor(PROJECT_NAME, PROJECT_TOKEN, PATTERN_WSS,
                 PATTERN_NONE, testDirectory, buildLogger);
         Collection<AgentProjectInfo> projectInfos = extractor.extract();
         assertThat(projectInfos.size()).isEqualTo(1);
         int actual = projectInfos.iterator().next().getDependencies().size();
         assertThat(actual).overridingErrorMessage(dependenciesMismatch, NUM_WSS, actual).isEqualTo(NUM_WSS);
     }
 
     @Test
     public void testExtractOssInfoDefaultWithExcludes()
     {
         BaseOssInfoExtractor extractor = new GenericOssInfoExtractor(PROJECT_NAME, PROJECT_TOKEN, PATTERN_ALL,
                 PATTERN_WSS, testDirectory, buildLogger);
         Collection<AgentProjectInfo> projectInfos = extractor.extract();
         assertThat(projectInfos.size()).isEqualTo(1);
         int actual = projectInfos.iterator().next().getDependencies().size();
         assertThat(actual).overridingErrorMessage(dependenciesMismatch, NUM_ALL - NUM_WSS, actual).isEqualTo(
                 NUM_ALL - NUM_WSS);
     }
 }
