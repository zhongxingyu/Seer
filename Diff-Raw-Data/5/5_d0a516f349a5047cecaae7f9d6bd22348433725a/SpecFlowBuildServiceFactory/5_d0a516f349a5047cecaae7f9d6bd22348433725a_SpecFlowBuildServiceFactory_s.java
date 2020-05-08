 package specflow.agent;
 
 import jetbrains.buildServer.RunBuildException;
 import jetbrains.buildServer.agent.*;
 import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
 import jetbrains.buildServer.agent.inspections.InspectionReporter;
 import org.jetbrains.annotations.NotNull;
 
 public class SpecFlowBuildServiceFactory implements AgentBuildRunner, AgentBuildRunnerInfo {
   @NotNull
   public String getType() {
    return SpecFlowConstants.RUNNER_TYPE;
   }
 
   public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
    return agentConfiguration.getSystemInfo().isWindows();
   }
 
   @NotNull
   @Override
   public BuildProcess createBuildProcess(@NotNull AgentRunningBuild agentRunningBuild,
                                          @NotNull BuildRunnerContext buildRunnerContext)
           throws RunBuildException {
       return new SpecFlowBuildProcess(agentRunningBuild);
   }
 
   @NotNull
   @Override
   public AgentBuildRunnerInfo getRunnerInfo() {
       return this;
   }
 }
