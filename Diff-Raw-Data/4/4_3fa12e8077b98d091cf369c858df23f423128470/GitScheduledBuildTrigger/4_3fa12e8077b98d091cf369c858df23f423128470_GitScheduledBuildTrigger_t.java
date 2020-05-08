 package com.maxifier.teamcity.trigger;
 
 import jetbrains.buildServer.BuildAgent;
 import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
 import jetbrains.buildServer.buildTriggers.BuildTriggerException;
 import jetbrains.buildServer.buildTriggers.BuildTriggerService;
 import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
 import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
 import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
 import jetbrains.buildServer.buildTriggers.scheduler.CronExpression;
 import jetbrains.buildServer.buildTriggers.scheduler.CronFieldInfo;
 import jetbrains.buildServer.buildTriggers.scheduler.CronParseException;
 import jetbrains.buildServer.buildTriggers.scheduler.CronScheduler;
 import jetbrains.buildServer.buildTriggers.scheduler.DailyScheduling;
 import jetbrains.buildServer.buildTriggers.scheduler.SchedulerBuildTriggerService;
 import jetbrains.buildServer.buildTriggers.scheduler.SchedulingPolicy;
 import jetbrains.buildServer.buildTriggers.scheduler.Time;
 import jetbrains.buildServer.buildTriggers.scheduler.WeeklyScheduling;
 import jetbrains.buildServer.buildTriggers.vcs.VcsBuildTriggerService;
 import jetbrains.buildServer.serverSide.BatchTrigger;
 import jetbrains.buildServer.serverSide.BranchEx;
 import jetbrains.buildServer.serverSide.BranchesPolicy;
 import jetbrains.buildServer.serverSide.BuildCustomizer;
 import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
 import jetbrains.buildServer.serverSide.BuildServerAdapter;
 import jetbrains.buildServer.serverSide.BuildServerListener;
 import jetbrains.buildServer.serverSide.BuildTypeEx;
 import jetbrains.buildServer.serverSide.ChangeDescriptor;
 import jetbrains.buildServer.serverSide.InvalidProperty;
 import jetbrains.buildServer.serverSide.PropertiesProcessor;
 import jetbrains.buildServer.serverSide.SBuildAgent;
 import jetbrains.buildServer.serverSide.TriggerTask;
 import jetbrains.buildServer.util.EventDispatcher;
 import jetbrains.buildServer.util.SystemTimeService;
 import jetbrains.buildServer.util.TimeService;
 import jetbrains.buildServer.vcs.SVcsModification;
 import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
 import jetbrains.buildServer.web.openapi.PluginDescriptor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author aleksey.didik@maxifier.com (Aleksey Didik)
  */
 public class GitScheduledBuildTrigger extends BuildTriggerService {
 
 
     public static final String BRANCHES = "branches";
     public static final String BUILD_DEFAULT = "buildDefault";
 
     private final PluginDescriptor pluginDescriptor;
     private final BatchTrigger batchTrigger;
     private final BuildCustomizerFactory buildCustomizerFactory;
     private final TimeService myTimeService;
     private long myServerStartupTime = 0;
 
 
     private SchedulerBuildTriggerService delegate;
 
     public GitScheduledBuildTrigger(PluginDescriptor pluginDescriptor,
                                     EventDispatcher<BuildServerListener> eventDispatcher,
                                     BatchTrigger batchTrigger,
                                     BuildCustomizerFactory buildCustomizerFactory) {
         this.pluginDescriptor = pluginDescriptor;
         this.batchTrigger = batchTrigger;
         this.buildCustomizerFactory = buildCustomizerFactory;
         this.myTimeService = SystemTimeService.getInstance();
         eventDispatcher.addListener(new BuildServerAdapter() {
             @Override
             public void serverStartup() {
                 myServerStartupTime = myTimeService.now();
             }
         });
         delegate = new SchedulerBuildTriggerService(eventDispatcher,
                 this.batchTrigger, this.buildCustomizerFactory);
     }
 
     @Override
     public String getName() {
         return "gitSchedulingTrigger";
     }
 
     @Override
     public String getDisplayName() {
         return "Git Scheduling Trigger";
     }
 
     public String describeTrigger(BuildTriggerDescriptor buildTriggerDescriptor) {
        String branches = buildTriggerDescriptor.getProperties().get(BRANCHES);
         return String.format("%s%n" +
                 "Default branch will %s" +
                 "%nList of git branches: %s",
                 delegate.describeTrigger(buildTriggerDescriptor),
                 isTrue(buildTriggerDescriptor.getProperties().get(BUILD_DEFAULT)) ? "be triggered" : "not be triggered",
                branches != null ?  branches : "none");
     }
 
     @Override
     public String getEditParametersUrl() {
         return pluginDescriptor.getPluginResourcesPath("editGitSchedulingTrigger.jsp");
     }
 
     @Override
     public boolean isMultipleTriggersPerBuildTypeAllowed() {
         return true;
     }
 
     @Override
     public BuildTriggeringPolicy getBuildTriggeringPolicy() {
         return new PolledBuildTrigger() {
             @Override
             public void triggerBuild(PolledTriggerContext polledTriggerContext) throws BuildTriggerException {
                 //ugly hack, of course :)
                 BuildTypeEx buildType = (BuildTypeEx) polledTriggerContext.getBuildType();
                 BuildTriggerDescriptor triggerDescriptor = polledTriggerContext.getTriggerDescriptor();
                 Map<String, String> properties = triggerDescriptor.getProperties();
                 SchedulingPolicy schedulingPolicy = createSchedulingPolicy(properties);
                 Date date = polledTriggerContext.getPreviousCallTime();
                 if (date == null) {
                     return;
                 }
                 long prevCallTime = date.getTime();
                 if (prevCallTime < myServerStartupTime) {
                     return;
                 }
                 long schedulingTime = schedulingPolicy.getScheduledTime(prevCallTime);
                 if (schedulingTime > 0L && myTimeService.now() >= schedulingTime) {
                     if (isTrue(properties.get(BUILD_DEFAULT))) {
                         add2Queue(polledTriggerContext, buildType.getBranch("<default>"));
                     }
                     //prepare branches
                     List<BranchEx> branches = buildType.getBranches(BranchesPolicy.ALL_BRANCES, false);
                     String[] requestedBranches = properties.get(BRANCHES).split("[;|,]]");
                     for (BranchEx branch : branches) {
                         if (!branch.isDefaultBranch() && isRequested(branch.getName(), requestedBranches)) {
                             add2Queue(polledTriggerContext, branch);
                         }
                     }
 
                 }
             }
 
             private void add2Queue(PolledTriggerContext polledTriggerContext, BranchEx branch) {
                 BuildTypeEx buildType = (BuildTypeEx) polledTriggerContext.getBuildType();
                 BuildTriggerDescriptor triggerDescriptor = polledTriggerContext.getTriggerDescriptor();
                 Map<String, String> properties = triggerDescriptor.getProperties();
 
                 //check pending changes
                 if (isTriggerIfPendingChanges(properties) && !pendingChanges(buildType, branch, properties)) {
                     return;
                 }
                 //new build customizer, set up desired branch name if not default branch it is.
                 BuildCustomizer buildCustomizer
                         = buildCustomizerFactory.createBuildCustomizer(buildType, null);
                 if (!branch.isDefaultBranch()) {
                     buildCustomizer.setDesiredBranchName(branch.getName());
                 }
                 //clean checkout if requested.
                 if (isEnforceCleanCheckout(properties)) {
                     buildCustomizer.setCleanSources(true);
                 }
                 //add2Queue
                 List<TriggerTask> tasks = new LinkedList<TriggerTask>();
                 if (isTriggerOnAllCompatibleAgents(properties)) {
                     for (BuildAgent buildAgent : buildType.getCanRunAndCompatibleAgents(false)) {
                         TriggerTask task = batchTrigger.newTriggerTask(buildCustomizer.createPromotion());
                         task.setRunOnAgent((SBuildAgent) buildAgent);
                         tasks.add(task);
                     }
                 } else {
                     tasks.add(batchTrigger.newTriggerTask(buildCustomizer.createPromotion()));
                 }
                 batchTrigger.processTasks(tasks, getDisplayName());
                 polledTriggerContext.getCustomDataStorage().putValues(properties);
 
             }
 
             private boolean isRequested(String branchName, String[] requestedBranches) {
                 for (String requestedBranch : requestedBranches) {
                     if (requestedBranch.trim().contains(branchName)) {
                         return true;
                     }
                 }
                 return false;
             }
 
             private boolean pendingChanges(BuildTypeEx buildType, BranchEx branch, Map<String, String> properties) {
                 List<ChangeDescriptor> detectedChanges
                         = branch.getDetectedChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, null);
                 List<SVcsModification> vcsModifications = new ArrayList<SVcsModification>(detectedChanges.size());
                 for (ChangeDescriptor detectedChange : detectedChanges) {
                     SVcsModification relatedVcsChange = detectedChange.getRelatedVcsChange();
                     if (relatedVcsChange != null) {
                         vcsModifications.add(relatedVcsChange);
                     }
                 }
                 VcsBuildTriggerService.filterModificationsByTriggerRules(vcsModifications,
                         buildType.getDependencyGraph().getNodes(), properties.get("triggerRules"));
                 return !vcsModifications.isEmpty();
             }
 
 
             SchedulingPolicy createSchedulingPolicy(Map<String, String> properties) throws BuildTriggerException {
                 String schedulingPolicy = properties.get("schedulingPolicy");
                 try {
                     if ("daily".equals(schedulingPolicy)) {
                         return new DailyScheduling(createTime(properties));
                     }
                     if ("weekly".equals(schedulingPolicy)) {
                         String day = properties.get("dayOfWeek");
                         if (day == null) {
                             day = "Sunday";
                         }
                         return new WeeklyScheduling(createTime(properties), WeeklyScheduling.getDayByName(day));
                     }
                     if ("cron".equals(schedulingPolicy)) {
 
                         return new CronScheduler(getCronExpression(properties));
                     } else {
                         throw new BuildTriggerException("Unknown scheduling policy: " + properties);
                     }
                 } catch (Exception e) {
                     throw new BuildTriggerException("Failed to create scheduling policy from properties: " + properties + ", error: " + e.toString());
                 }
             }
 
             private CronExpression getCronExpression(Map<String, String> map)
                     throws CronParseException {
                 return CronExpression.createCronExpression(extractCronParameters(map));
             }
 
             private Map extractCronParameters(Map<String, String> properties) {
                 HashMap hashmap = new HashMap();
                 CronFieldInfo acronfieldinfo[] = CronFieldInfo.values();
                 for (CronFieldInfo cronfieldinfo : acronfieldinfo) {
                     String s = properties.get("cronExpression_" + cronfieldinfo.getKey());
                     hashmap.put(cronfieldinfo.getKey(), s);
                 }
                 return hashmap;
             }
 
 
             private boolean isTriggerIfPendingChanges(Map<String, String> props) {
                 String property = props.get("triggerBuildWithPendingChangesOnly");
                 return isTrue(property);
             }
 
 
             private boolean isEnforceCleanCheckout(Map<String, String> props) {
                 return isTrue(props.get("enforceCleanCheckout"));
             }
 
             private boolean isTriggerOnAllCompatibleAgents(Map<String, String> props) {
                 return isTrue(props.get("triggerBuildOnAllCompatibleAgents"));
             }
 
             private Time createTime(Map map) {
                 return new Time(parseInt((String) map.get("hour")), parseInt((String) map.get("minute")));
             }
 
             private int parseInt(String s) {
                 return Integer.parseInt(s);
             }
 
         };
 
 
     }
 
     private boolean isTrue(String property) {
         return Boolean.parseBoolean(property) || "yes".equalsIgnoreCase(property);
     }
 
 
     @Override
     public PropertiesProcessor getTriggerPropertiesProcessor() {
         return new PropertiesProcessor() {
             @Override
             public Collection<InvalidProperty> process(Map<String, String> properties) {
                 //TODO process
                 Collection<InvalidProperty> invalid = delegate.getTriggerPropertiesProcessor().process(properties);
                 return invalid;
             }
         };
     }
 
     @Override
     public Map<String, String> getDefaultTriggerProperties() {
         Map<String, String> defaultProps = delegate.getDefaultTriggerProperties();
         defaultProps.put(BRANCHES, "");
         defaultProps.put(BUILD_DEFAULT, "true");
         return defaultProps;
     }
 
 
 }
