 package com.wgs.cucumber.python.run;
 
 import com.intellij.execution.Location;
 import com.intellij.execution.RunnerAndConfigurationSettings;
 import com.intellij.execution.actions.ConfigurationContext;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.util.PsiTreeUtil;
 import com.jetbrains.django.testRunner.DjangoTestsConfigurationProducer;
 import com.jetbrains.django.testRunner.DjangoTestsRunConfiguration;
 import com.jetbrains.python.buildout.BuildoutFacet;
 import com.jetbrains.python.buildout.config.psi.BuildoutPsiUtil;
 import com.jetbrains.python.buildout.config.psi.impl.BuildoutCfgSection;
 import com.jetbrains.python.psi.PyUtil;
 import org.jetbrains.plugins.cucumber.psi.GherkinFile;
 import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;
 import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
 

 public class CucumberDjangoTestsConfigurationProducer extends DjangoTestsConfigurationProducer {
 
     private GherkinPsiElement myGherkinSourceElement;
 
     @Override
     public PsiElement getSourceElement() {
         return myGherkinSourceElement;
     }
 
     @Override
     protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
 
         PsiElement element = location.getPsiElement();
         GherkinPsiElement gherkinPsiElement;
         if (element instanceof GherkinFile) {
             gherkinPsiElement = PsiTreeUtil.getChildOfType(element, GherkinPsiElement.class);
         } else {
             gherkinPsiElement = PsiTreeUtil.getParentOfType(element, GherkinPsiElement.class);
         }
 
         if (gherkinPsiElement != null) {
             myGherkinSourceElement = gherkinPsiElement;
             return createConfigurationByGherkinElement(gherkinPsiElement, context);
         } else {
             return null;
         }
     }
 
     private int getScenarioIndex(GherkinScenario scenario) {
         int scenarioIndex = 0;
         GherkinScenario currentScenario = PsiTreeUtil.getPrevSiblingOfType(scenario, GherkinScenario.class);
         while (currentScenario != null) {
             currentScenario = PsiTreeUtil.getPrevSiblingOfType(currentScenario, GherkinScenario.class);
             scenarioIndex++;
         }
 
         return scenarioIndex;
     }
 
     private String computeFeatureClassName(String featureFileName) {
         String[] featureComponents = featureFileName.split("\\.");
 
         return featureComponents[0];
     }
 
     private RunnerAndConfigurationSettings createConfigurationByGherkinElement(GherkinPsiElement element, ConfigurationContext context) {
        // not very usefull for our use cases
        String appName = "";
 
         Module module = ModuleUtil.findModuleForPsiElement(element);
 
         PsiFile featureFileElement = PsiTreeUtil.getParentOfType(element, PsiFile.class);
         RunnerAndConfigurationSettings result = null;
         if (featureFileElement != null) {
 
             result = cloneTemplateConfiguration(context.getProject(), context);
 
             String target = computeTargetForGherkinElement(featureFileElement, element);
 
             DjangoTestsRunConfiguration configuration = (DjangoTestsRunConfiguration) result.getConfiguration();
 
             configuration.setName((new StringBuilder()).append("Cucumber: ").append(target).toString());
             configuration.setUseModuleSdk(true);
             configuration.setModule(module);
             configuration.setTarget(target);
             setDefaultSettingsFile(configuration, appName);
         }
         return result;
     }
 
     private String computeTargetForGherkinElement(PsiFile featureFile, GherkinPsiElement gherkinPsiElement) {
         StringBuilder builder = new StringBuilder();
        GherkinScenario scenario = null;
 
        //TODO: fixme Magic String
         builder.append("bdd.").append(computeFeatureClassName(featureFile.getName()));
        GherkinScenario parentScenario = PsiTreeUtil.getParentOfType(gherkinPsiElement, GherkinScenario.class);

        if (parentScenario != null) {
            scenario = parentScenario;
        }
        else if (gherkinPsiElement instanceof GherkinScenario) {
            scenario = (GherkinScenario)gherkinPsiElement;
        }
 
        //TODO: fixme Magic String
        if (scenario != null) {
            builder.append(".test_scenario_").append(getScenarioIndex(scenario));
         }
 
         return builder.toString();
     }
 
     private static void setDefaultSettingsFile(DjangoTestsRunConfiguration configuration, String appName) {
         BuildoutFacet buildoutFacet = BuildoutFacet.getInstance(configuration.getModule());
         if (buildoutFacet != null) {
             com.jetbrains.python.buildout.config.psi.impl.BuildoutCfgFile configFile = buildoutFacet.getConfigPsiFile();
             if (configFile != null) {
                 BuildoutCfgSection section = BuildoutPsiUtil.getDjangoSection(configFile);
                 if (section != null && appName.equals(section.getOptionValue("test"))) {
                     String projectName = section.getOptionValue("project");
                     String settingsFileName = section.getOptionValue("settings");
                     if (projectName != null && settingsFileName != null) {
                         VirtualFile settingsFile = PyUtil.findInRoots(configuration.getModule(), (new StringBuilder()).append(projectName).append("/").append(settingsFileName).append(".py").toString());
                         if (settingsFile != null)
                             configuration.setSettingsFile(settingsFile.getPath());
                     }
                 }
             }
         }
     }
 
 
 
 }
