 /**
  * Copyright (C) 2012 White Source Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.whitesource.teamcity.agent;
 
 import com.intellij.openapi.diagnostic.Logger;
 import jetbrains.buildServer.ExtensionHolder;
 import jetbrains.buildServer.agent.*;
 import jetbrains.buildServer.log.Loggers;
 import jetbrains.buildServer.util.ArchiveUtil;
 import jetbrains.buildServer.util.EventDispatcher;
 import jetbrains.buildServer.util.FileUtil;
 import jetbrains.buildServer.util.StringUtil;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.jetbrains.annotations.NotNull;
 import org.springframework.util.CollectionUtils;
 import org.whitesource.agent.api.dispatch.CheckPoliciesResult;
 import org.whitesource.agent.api.dispatch.UpdateInventoryResult;
 import org.whitesource.agent.api.model.AgentProjectInfo;
 import org.whitesource.agent.api.model.DependencyInfo;
 import org.whitesource.api.client.WhitesourceService;
 import org.whitesource.api.client.WssServiceException;
 import org.whitesource.teamcity.common.Constants;
 import org.whitesource.teamcity.common.WssUtils;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipOutputStream;
 
 /**
  * @author Edo.Shor
  */
 public class WhitesourceLifeCycleListener extends AgentLifeCycleAdapter {
 
     /* --- Static members --- */
 
     private static final String LOG_COMPONENT = "LifeCycleListener";
 
     private ExtensionHolder extensionHolder;
 
     /* --- Constructors --- */
 
     /**
      * Constructor
      *
      * @param eventDispatcher
      * @param extensionHolder
      */
     public WhitesourceLifeCycleListener(@NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher,
                                         @NotNull final ExtensionHolder extensionHolder) {
         this.extensionHolder = extensionHolder;
         eventDispatcher.addListener(this);
     }
 
     @Override
     public void agentInitialized(@NotNull BuildAgent agent) {
         super.agentInitialized(agent);
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "initialized"));
     }
 
     /* --- Interface implementation methods --- */
 
     @Override
     public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
         super.beforeRunnerStart(runner);
 
         if (shouldUpdate(runner)) {
             Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "before runner start "
                     + runner.getBuild().getProjectName() + " type " + runner.getName()));
         }
     }
 
     @Override
     public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
         super.runnerFinished(runner, status);
 
         AgentRunningBuild build = runner.getBuild();
 
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "runner finished "
                 + build.getProjectName() + " type " + runner.getName()));
 
         if (!shouldUpdate(runner)) {
             return; // no need to update white source...
         }
 
         final BuildProgressLogger buildLogger = build.getBuildLogger();
         buildLogger.message("Updating White Source");
 
         // make sure we have an organization token
         String orgToken = runner.getRunnerParameters().get(Constants.RUNNER_OVERRIDE_ORGANIZATION_TOKEN);
         if (StringUtil.isEmptyOrSpaces(orgToken)) {
             orgToken = runner.getRunnerParameters().get(Constants.RUNNER_ORGANIZATION_TOKEN);
         }
         if (StringUtil.isEmptyOrSpaces(orgToken)) {
             stopBuildOnError((AgentRunningBuildEx) build,
                     new IllegalStateException("Empty organization token. " +
                             "Please make sure an organization token is defined for this runner"));
             return;
         }
 
         // should we check policies first ?
         boolean shouldCheckPolicies = false;
         String overrideCheckPolicies = runner.getRunnerParameters().get(Constants.RUNNER_OVERRIDE_CHECK_POLICIES);
        if ("global".equals(overrideCheckPolicies)) {
             shouldCheckPolicies = Boolean.parseBoolean(runner.getRunnerParameters().get(Constants.RUNNER_CHECK_POLICIES));
         } else {
             shouldCheckPolicies = "enabled".equals(overrideCheckPolicies);
         }
 
         // collect OSS usage information
         buildLogger.message("Collecting OSS usage information");
         BaseOssInfoExtractor extractor = null;
         if (WssUtils.isMavenRunType(runner.getRunType())) {
             extractor = new MavenOssInfoExtractor(runner);
         } else {
             extractor = new GenericOssInfoExtractor(runner);
         }
         Collection<AgentProjectInfo> projectInfos = extractor.extract();
         debugAgentProjectInfos(projectInfos);
 
         // send to white source
         if (CollectionUtils.isEmpty(projectInfos)) {
             buildLogger.message("No open source information found.");
         } else {
             WhitesourceService service = createServiceClient(runner);
             try{
                 if (shouldCheckPolicies) {
                     buildLogger.message("Checking policies");
                     CheckPoliciesResult result = service.checkPolicies(orgToken, projectInfos);
                     policyCheckReport(runner, result);
                     if (result.hasRejections()) {
                         stopBuild((AgentRunningBuildEx) build, "Open source rejected by organization policies.");
                     } else {
                         buildLogger.message("All dependencies conform with open source policies.");
                         sendUpdate(orgToken, projectInfos, service, buildLogger);
                     }
                 } else {
                     sendUpdate(orgToken, projectInfos, service, buildLogger);
                 }
             } catch (WssServiceException e) {
                 stopBuildOnError((AgentRunningBuildEx) build, e);
             } catch (IOException e) {
                 stopBuildOnError((AgentRunningBuildEx) build, e);
             } catch (RuntimeException e) {
                 Loggers.AGENT.error(WssUtils.logMsg(LOG_COMPONENT, "Runtime Error"), e);
                 stopBuildOnError((AgentRunningBuildEx) build, e);
             } finally {
                 service.shutdown();
             }
         }
     }
 
     private void policyCheckReport(BuildRunnerContext runner, CheckPoliciesResult result) throws IOException {
         AgentRunningBuild build = runner.getBuild();
         File reportDir = new File(build.getBuildTempDirectory(), "whitesource");
         reportDir.mkdirs();
 
         // generate report
         Velocity.setProperty(Velocity.RESOURCE_LOADER, "classpath");
         Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
         Velocity.init();
 
         VelocityContext context = new VelocityContext();
         context.put("buildName", build.getProjectName());
         context.put("buildNumber", build.getBuildNumber());
         context.put("creationTime", SimpleDateFormat.getInstance().format(new Date()));
         context.put("result", result);
         context.put("hasRejections", result.hasRejections());
 
         FileWriter fw = new FileWriter(new File(reportDir, "index.html"));
         Velocity.mergeTemplate("templates/policy-check.vm", "UTF-8", context, fw);
         fw.flush();
         fw.close();
 
         // copy report resources
         File resource = new File(reportDir, "wss.css");
         FileUtil.copyResourceIfNotExists(getClass(), "/templates/wss.css", resource);
 
         // pack report and send to server
         File reportArchive = new File(build.getBuildTempDirectory(), "whitesource.zip");
         ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(reportArchive));
         ArchiveUtil.packZip(reportDir, zos);
         ArtifactsPublisher publisher = extensionHolder.getExtensions(ArtifactsPublisher.class).iterator().next();
         Map<File, String> artifactsToPublish = new HashMap<File, String>();
         artifactsToPublish.put(reportArchive, "");
         publisher.publishFiles(artifactsToPublish);
     }
 
     /* --- Private methods --- */
 
     private boolean shouldUpdate(BuildRunnerContext runner) {
         String shouldUpdate = runner.getRunnerParameters().get(Constants.RUNNER_DO_UPDATE);
         return !StringUtil.isEmptyOrSpaces(shouldUpdate) && Boolean.valueOf(shouldUpdate);
     }
 
     private WhitesourceService createServiceClient(BuildRunnerContext runner) {
         String serviceUrl = runner.getRunnerParameters().get(Constants.RUNNER_SERVICE_URL);
         WhitesourceService service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION, serviceUrl);
 
         String proxyHost = runner.getRunnerParameters().get(Constants.RUNNER_PROXY_HOST);
         if (!StringUtil.isEmptyOrSpaces(proxyHost)) {
             int port = Integer.parseInt(runner.getRunnerParameters().get(Constants.RUNNER_PROXY_PORT));
             String username = runner.getRunnerParameters().get(Constants.RUNNER_PROXY_USERNAME);
             String password = runner.getRunnerParameters().get(Constants.RUNNER_PROXY_PASSWORD);
             service.getClient().setProxy(proxyHost, port, username, password);
         }
 
         return service;
     }
 
     private void sendUpdate(String orgToken, Collection<AgentProjectInfo> projectInfos,
                             WhitesourceService service, BuildProgressLogger buildLogger)
             throws WssServiceException {
 
         buildLogger.message("Sending to White Source");
         UpdateInventoryResult updateResult = service.update(orgToken, projectInfos);
         logUpdateResult(updateResult, buildLogger);
     }
 
     private void logUpdateResult(UpdateInventoryResult result, BuildProgressLogger logger) {
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "update success"));
 
         logger.message("White Source update results: ");
         logger.message("White Source organization: " + result.getOrganization());
         logger.message(result.getCreatedProjects().size() + " Newly created projects:");
         logger.message(StringUtil.join(result.getCreatedProjects(), ","));
         logger.message(result.getUpdatedProjects().size() + " existing projects were updated:");
         logger.message(StringUtil.join(result.getUpdatedProjects(), ","));
     }
 
     private void stopBuildOnError(AgentRunningBuildEx build, Exception e) {
         Loggers.AGENT.warn(WssUtils.logMsg(LOG_COMPONENT, "Stopping build"), e);
 
         BuildProgressLogger logger = build.getBuildLogger();
         String errorMessage = e.getLocalizedMessage();
         logger.buildFailureDescription(errorMessage);
         logger.exception(e);
         logger.flush();
         build.stopBuild(errorMessage);
     }
 
     private void stopBuild(AgentRunningBuildEx build, String message) {
         Loggers.AGENT.warn(WssUtils.logMsg(LOG_COMPONENT, "Stopping build: + message"));
 
         BuildProgressLogger logger = build.getBuildLogger();
         logger.buildFailureDescription(message);
         logger.flush();
         build.stopBuild(message);
     }
 
     private void debugAgentProjectInfos(Collection<AgentProjectInfo> projectInfos) {
         final Logger log = Loggers.AGENT;
 
         log.info("----------------- dumping projectInfos -----------------");
         log.info("Total number of projects : " + projectInfos.size());
         for (AgentProjectInfo projectInfo : projectInfos) {
             log.info("Project coordiantes: " + projectInfo.getCoordinates());
             log.info("Project parent coordiantes: " + projectInfo.getParentCoordinates());
             log.info("total # of dependencies: " + projectInfo.getDependencies().size());
             for (DependencyInfo info :  projectInfo.getDependencies()) {
                 log.info(info + " SHA-1: " + info.getSha1());
             }
         }
         log.info("----------------- dump finished -----------------");
 
     }
 }
