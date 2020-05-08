 package org.etp.portalKit.powerbuild.logic;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.type.TypeReference;
 import org.etp.portalKit.common.service.DeployService;
 import org.etp.portalKit.common.service.PropertiesManager;
 import org.etp.portalKit.common.util.CommandResult;
 import org.etp.portalKit.common.util.JSONUtils;
 import org.etp.portalKit.powerbuild.bean.BuildInformation;
 import org.etp.portalKit.powerbuild.bean.BuildResult;
 import org.etp.portalKit.powerbuild.bean.DeployInformation;
 import org.etp.portalKit.powerbuild.bean.DeployType;
 import org.etp.portalKit.powerbuild.bean.ExecuteCommand;
 import org.etp.portalKit.powerbuild.bean.ExecuteMultiCommand;
 import org.etp.portalKit.powerbuild.bean.ExecuteSingleCommand;
 import org.etp.portalKit.powerbuild.service.BuildListProvider;
 import org.etp.portalKit.powerbuild.service.ExecuteType;
 import org.etp.portalKit.powerbuild.service.MavenExecutor;
 import org.etp.portalKit.setting.bean.SettingsCommand;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.stereotype.Component;
 
 /**
  * The purpose of this class is
  */
 @Component(value = "buildLogic")
 public class PowerBuildLogic {
 
     @Resource(name = "pathMatchingResourcePatternResolver")
     private PathMatchingResourcePatternResolver pathResolver;
 
     @Resource(name = "customizedBuildListProvider")
     private BuildListProvider buildListProvider;
 
     @Resource(name = "propertiesManager")
     private PropertiesManager prop;
 
     @Resource(name = "mavenExecutor")
     private MavenExecutor executor;
 
     @Resource(name = "deployService")
     private DeployService deployService;
 
     private String ENVIRONMENT_DEPLOY_JSON = "powerbuild/deployInformation.json";
 
     private DeployInformation deployInformation;
 
     /**
      * initialize the basedListtree
      */
     @PostConstruct
     public void initCommbuildList() {
         deployInformation = JSONUtils.fromJSONResource(pathResolver.getResource(ENVIRONMENT_DEPLOY_JSON),
                 new TypeReference<DeployInformation>() {
                     //            
                 });
     }
 
     private String checkDeployPath() {
         String deployPath = prop.get(SettingsCommand.TOMCAT_WEBAPPS_PATH);
         if (StringUtils.isBlank(deployPath)) {
             throw new RuntimeException("You haven't deploy path setted.");
         }
         return deployPath;
     }
 
     private String checkDesignPath() {
         String path = prop.get(SettingsCommand.PORTAL_TEAM_PATH);
         if (StringUtils.isBlank(path)) {
             throw new RuntimeException("You haven't design path setted.");
         }
         return path;
     }
 
     private boolean checkCanBeDeployed(String absolutePath) {
         File file = new File(absolutePath);
         File[] files = file.listFiles(new FileFilter() {
             @Override
             public boolean accept(File arg0) {
                 return arg0.isDirectory() && "target".equals(arg0.getName());
             }
         });
         if (files.length == 0) {
             return false;
         }
         File target = new File(absolutePath, "target");
         File[] wars = target.listFiles(new FileFilter() {
             @Override
             public boolean accept(File arg0) {
                 return arg0.isFile() && arg0.getName().endsWith(".war");
             }
         });
         if (wars.length == 0) {
             return false;
         }
         return true;
     }
 
     private ExecuteType getExecuteType(ExecuteCommand cmd) {
         ExecuteType type = null;
         if (cmd.isNeedBuild() && cmd.isNeedTest()) {
             type = ExecuteType.COMPILE_TEST;
         } else if (cmd.isNeedBuild()) {
             type = ExecuteType.COMPILE;
         } else if (cmd.isNeedTest()) {
             type = ExecuteType.TEST;
         }
         return type;
     }
 
     /**
      * Execute specified command to the project which given
      * <code>absolutePath</code> indicates. There are four kinds of
      * command, compile, test, compile_test, deploy.
      * 
      * @param absolutePath absolute path of project.
      * @param cmd ExecuteCommand
      * @return BuildResult
      */
     public BuildResult executeCommand(String absolutePath, ExecuteSingleCommand cmd) {
         BuildResult br = new BuildResult();
         ExecuteType type = getExecuteType(cmd);
         if ((type == null) && !cmd.isNeedDeploy()) {
             throw new RuntimeException("You have to choose at least one option.");
         }
         if (type != null) {
             CommandResult cr = executor.exec(absolutePath, type);
             br.setCommandResult(cr);
             if (!br.isSuccess()) {
                 return br;
             }
         }
 
         if (cmd.isNeedDeploy()) {
             String deployPath = checkDeployPath();
             if (checkCanBeDeployed(absolutePath)) {
                 br.setDeployed(deployService.deployFromFolder(absolutePath, deployPath));
             } else {
                 br.setDeployed(true);
             }
         }
         return br;
     }
 
     private List<String> getDeploySet(DeployType deployType) {
         String basePath = checkDesignPath();
         List<String> deploySet = new ArrayList<String>();
         switch (deployType) {
         case REFERENCE_PORTAL:
             for (Map<String, String> fw : deployInformation.getFramework()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
             for (Map<String, String> fw : deployInformation.getReferencePortal()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
             break;
         case MULTISCREEN_PORTAL:
             for (Map<String, String> fw : deployInformation.getFramework()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
             for (Map<String, String> fw : deployInformation.getMultiscreenPortal()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
             break;
         }
         return deploySet;
     }
 
     /**
      * Execute compile, test, deploy command to a specified type such
      * as <code>REFERENCE_PORTAL</code>,
      * <code>MULTISCREEN_PORTAL</code>
      * 
      * @param deployType <code>REFERENCE_PORTAL</code>,
      *            <code>MULTISCREEN_PORTAL</code>
      * @param cmd ExecuteMultiCommand
      * @return BuildResult
      */
     public BuildResult executeWithType(DeployType deployType, ExecuteMultiCommand cmd) {
         String deployPath = checkDeployPath();
         String basePath = checkDesignPath();
         ExecuteType executeType = getExecuteType(cmd);
         if ((executeType == null) && !cmd.isNeedDeploy()) {
             throw new RuntimeException("You have to choose at least one option.");
         }
         BuildResult result = new BuildResult();
         if (executeType != null) {
             result.setCommandResult(executor.exec(new File(basePath, "design").getAbsolutePath(), executeType));
             if (!result.isSuccess()) {
                 return result;
             }
         }
        List<String> deploySet = getDeploySet(deployType);

         if (cmd.isNeedDeploy()) {
             for (String abs : deploySet) {
                 if (checkCanBeDeployed(abs)) {
                     result.setDeployed(deployService.deployFromFolder(abs, deployPath));
                 } else {
                     result.setDeployed(true);
                 }
             }
         }
         return result;
     }
 
     /**
      * Get information which build page needed.
      * 
      * @return BuildInformation
      */
     public BuildInformation getBuildInformation() {
         Map<String, List<String>> deployInfo = new HashMap<String, List<String>>();
         List<String> refApps = new ArrayList<String>();
         for (Map<String, String> fw : deployInformation.getFramework()) {
             refApps.add(fw.get("packageName"));
         }
         for (Map<String, String> rp : deployInformation.getReferencePortal()) {
             refApps.add(rp.get("packageName"));
         }
         deployInfo.put("referencePortal", refApps);
         List<String> multi = new ArrayList<String>();
         for (Map<String, String> fw : deployInformation.getFramework()) {
             multi.add(fw.get("packageName"));
         }
         for (Map<String, String> mp : deployInformation.getMultiscreenPortal()) {
             multi.add(mp.get("packageName"));
         }
         deployInfo.put("multiscreenPortal", multi);
         return new BuildInformation(buildListProvider.retrieveDirTrees(), deployInfo);
     }
 }
