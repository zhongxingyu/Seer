 /*
  * Sonar-SonarJ-Plugin
  * Open source plugin for Sonar
  * Copyright (C) 2009, 2010 hello2morrow GmbH
  * mailto: info AT hello2morrow DOT com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package com.hello2morrow.sonarplugin;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import com.hello2morrow.sonarplugin.xsd.XsdBuildUnits;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.batch.maven.DependsUponMavenPlugin;
 import org.sonar.api.batch.maven.MavenPluginHandler;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.JavaFile;
 import org.sonar.api.resources.JavaPackage;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.api.rules.Rule;
 import org.sonar.api.rules.RulePriority;
 import org.sonar.api.rules.RulesManager;
 import org.sonar.api.rules.Violation;
 
 import com.hello2morrow.sonarplugin.xsd.ReportContext;
 import com.hello2morrow.sonarplugin.xsd.XsdArchitectureViolation;
 import com.hello2morrow.sonarplugin.xsd.XsdAttribute;
 import com.hello2morrow.sonarplugin.xsd.XsdAttributeCategory;
 import com.hello2morrow.sonarplugin.xsd.XsdAttributeRoot;
 import com.hello2morrow.sonarplugin.xsd.XsdCycleGroup;
 import com.hello2morrow.sonarplugin.xsd.XsdCycleGroups;
 import com.hello2morrow.sonarplugin.xsd.XsdCyclePath;
 import com.hello2morrow.sonarplugin.xsd.XsdDependencyProblem;
 import com.hello2morrow.sonarplugin.xsd.XsdElementProblem;
 import com.hello2morrow.sonarplugin.xsd.XsdPosition;
 import com.hello2morrow.sonarplugin.xsd.XsdProblemCategory;
 import com.hello2morrow.sonarplugin.xsd.XsdProjects;
 import com.hello2morrow.sonarplugin.xsd.XsdTask;
 import com.hello2morrow.sonarplugin.xsd.XsdTasks;
 import com.hello2morrow.sonarplugin.xsd.XsdTypeRelation;
 import com.hello2morrow.sonarplugin.xsd.XsdViolations;
 import com.hello2morrow.sonarplugin.xsd.XsdWarning;
 import com.hello2morrow.sonarplugin.xsd.XsdWarnings;
 import com.hello2morrow.sonarplugin.xsd.XsdWarningsByAttribute;
 import com.hello2morrow.sonarplugin.xsd.XsdWarningsByAttributeGroup;
 
 public final class SonarJSensor implements Sensor
 {
     public static final String COST_PER_INDEX_POINT = "sonarj.index_point_cost";
 
     private static final Logger LOG = LoggerFactory.getLogger(SonarJSensor.class);
 
     private static final String REPORT_DIR = "sonarj-sonar-plugin";
     private static final String REPORT_NAME = "sonarj-report.xml";
 
     private static final String ACD = "Average component dependency (ACD)";
     private static final String NCCD = "Normalized cumulative component dependency (NCCD)";
     private static final String INTERNAL_PACKAGES = "Number of internal packages";
     private static final String INSTRUCTIONS = "Number of instructions";
     private static final String UNASSIGNED_TYPES = "Number of unassigned types";
     private static final String VIOLATING_DEPENDENCIES = "Number of violating type dependencies";
     private static final String VIOLATING_TYPES = "Number of violating types";
     private static final String TYPE_DEPENDENCIES = "Number of type dependencies (all)";
     private static final String JAVA_FILES = "Number of Java source files (non-excluded)";
     private static final String IGNORED_VIOLATIONS = "Number of ignored violations";
     private static final String IGNORED_WARNINGS = "Number of ignored warnings";
     private static final String TASKS = "Number of tasks";
     private static final String ALL_WARNINGS = "Number of warnings (all)";
     private static final String CYCLE_WARNINGS = "Number of warnings (cyclic)";
     private static final String THRESHOLD_WARNINGS = "Number of warnings (thresholds)";
     private static final String WORKSPACE_WARNINGS = "Number of warnings (workspace)";
     private static final String DUPLICATE_WARNINGS = "Number of warnings (duplicate code blocks)";
     private static final String EROSION_REFS = "Structural erosion - reference level";
     private static final String EROSION_TYPES = "Structural erosion - type level";
     private static final String INTERNAL_TYPES = "Number of internal types (all)";
     private static final String STUCTURAL_DEBT_INDEX = "Structural debt (index)";
 
     private Map<String, Number> projectMetrics;
     private SensorContext sensorContext;
     private RulesManager rulesManager;
     private RulesProfile rulesProfile;
    private double indexCost = 12.0;
 
     protected static ReportContext readSonarjReport(String fileName, String packaging)
     {
         ReportContext result = null;
         InputStream input = null;
         ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
 
         try
         {
             Thread.currentThread().setContextClassLoader(SonarJSensor.class.getClassLoader());
             JAXBContext context = JAXBContext.newInstance("com.hello2morrow.sonarplugin.xsd");
             Unmarshaller u = context.createUnmarshaller();
 
             input = new FileInputStream(fileName);
             result = (ReportContext) u.unmarshal(input);
         }
         catch (JAXBException e)
         {
             LOG.error("JAXB Problem in " + fileName, e);
         }
         catch (FileNotFoundException e)
         {
             if (!packaging.equalsIgnoreCase("pom"))
             {
                 LOG.warn("Cannot open SonarJ report: " + fileName + ".");
                 LOG.warn("  Did you run the maven sonarj goal before with the POM option <prepareForSonar>true</prepareForSonar> " +
                 		"or with the commandline option -Dsonarj.prepareForSonar=true?");
                 LOG.warn("  Is the project part of the SonarJ architecture description?");
                 LOG.warn("  Did you set the 'aggregate' to true (must be false)?");
             }
         }
         finally
         {
             Thread.currentThread().setContextClassLoader(defaultClassLoader);
             if (input != null)
             {
                 try
                 {
                     input.close();
                 }
                 catch (IOException e)
                 {
                     LOG.error("Cannot close " + fileName, e);
                 }
             }
         }
         return result;
     }
 
     public SonarJSensor(Configuration config, RulesManager rulesManager, RulesProfile rulesProfile)
     {
        indexCost = config.getDouble(COST_PER_INDEX_POINT, 12.0);
         this.rulesManager = rulesManager;
         this.rulesProfile = rulesProfile;
         if (rulesManager == null)
         {
             LOG.warn("No RulesManager provided to sensor");
         }
         if (rulesProfile == null)
         {
             LOG.warn("No RulesProfile given to sensor");
         }
     }
 
     public boolean shouldExecuteOnProject(Project project)
     {
         return true;
     }
 
     private Map<String, Number> readAttributes(XsdAttributeRoot root)
     {
         Map<String, Number> result = new HashMap<String, Number>();
 
         for (XsdAttributeCategory cat : root.getAttributeCategory())
         {
             for (XsdAttribute attr : cat.getAttribute())
             {
                 String attrName = attr.getName();
                 String value = attr.getValue();
 
                 try
                 {
                     if (value.indexOf('.') >= 0)
                     {
                         result.put(attrName, Double.valueOf(value));
                     }
                     else if (value.indexOf(':') == -1)
                     {
                         result.put(attrName, Integer.valueOf(value));
                     }
                 }
                 catch (NumberFormatException e)
                 {
                     // Ignore this value
                 }
             }
         }
         return result;
     }
 
     private double getProjectMetric(String key)
     {
         Number num = projectMetrics.get(key);
 
         if (num == null)
         {
             LOG.error("Cannot find metric <" + key + "> in generated report");
             return 0.0;
         }
         return num.doubleValue();
     }
 
     private Measure saveMeasure(String key, Metric metric, int precision)
     {
         double value = getProjectMetric(key);
 
         return saveMeasure(metric, value, precision);
     }
 
     private Measure saveMeasure(Metric metric, double value, int precision)
     {
         Measure m = new Measure(metric, value, precision);
 
         sensorContext.saveMeasure(m);
         return m;
     }
     
     private String getAttribute(List<XsdAttribute> map, String name)
     {
         String value = null;
 
         for (XsdAttribute attr : map)
         {
             if (attr.getName().equals(name))
             {
                 value = attr.getValue();
                 break;
             }
         }
         return value;
     }
 
     @SuppressWarnings("unchecked")
     private void analyseCycleGroups(ReportContext report, Number internalPackages, String buildUnitName)
     {
         XsdCycleGroups cycleGroups = report.getCycleGroups();
         double cyclicity = 0;
         double biggestCycleGroupSize = 0;
         double cyclicPackages = 0;
 
         for (XsdCycleGroup group : cycleGroups.getCycleGroup())
         {
             if (group.getNamedElementGroup().equals("Physical build unit package"))
             {
                 if (getBuildUnitName(group).equals(buildUnitName))
                 {
                     int groupSize = group.getCyclePath().size();
                     cyclicPackages += groupSize;
                     cyclicity += groupSize * groupSize;
                     if (groupSize > biggestCycleGroupSize)
                     {
                         biggestCycleGroupSize = groupSize;
                     }
                     handlePackageCycleGroup(group);
                 }
             }
         }
         saveMeasure(SonarJMetrics.BIGGEST_CYCLE_GROUP, biggestCycleGroupSize, 0);
         saveMeasure(SonarJMetrics.CYCLICITY, cyclicity, 0);
         saveMeasure(SonarJMetrics.CYCLIC_PACKAGES, cyclicPackages, 0);
 
         double relativeCyclicity = 100.0 * Math.sqrt(cyclicity) / internalPackages.doubleValue();
         double relativeCyclicPackages = 100.0 * cyclicPackages / internalPackages.doubleValue();
 
         saveMeasure(SonarJMetrics.RELATIVE_CYCLICITY, relativeCyclicity, 1);
         saveMeasure(SonarJMetrics.INTERNAL_PACKAGES, internalPackages.doubleValue(), 0);
         saveMeasure(SonarJMetrics.CYCLIC_PACKAGES_PERCENT, relativeCyclicPackages, 1);
     }
 
     @SuppressWarnings("unchecked")
     private void saveViolation(Rule rule, RulePriority priority, String fqName, int line, String msg)
     {
         Resource javaFile = sensorContext.getResource(new JavaFile(fqName));
 
         if (javaFile == null)
         {
             LOG.error("Cannot obtain resource " + fqName);
         }
         else
         {
             Violation v = new Violation(rule, javaFile);
 
             v.setMessage(msg);
             v.setLineId(line);
             v.setPriority(priority);
             sensorContext.saveViolation(v);
         }
     }
 
     private String getBuildUnitName(XsdCycleGroup group)
     {
         if (group.getParent().equals("(Default Build Unit)"))
         {
             return group.getElementScope();
         }
         return group.getParent();
     }
 
     private String getBuildUnitName(String fqName)
     {
         String buName = "<UNKNOWN>";
 
         if (fqName != null)
         {
             int colonPos = fqName.indexOf("::");
 
             if (colonPos != -1)
             {
                 buName = fqName.substring(colonPos + 2);
                 if (buName.equals("(Default Build Unit)"))
                 {
                     // Compatibility with old SonarJ versions
                     buName = fqName.substring(0, colonPos);
                 }
             }
         }
         return buName;
     }
 
     private static String relativeFileNameToFqName(String fileName)
     {
         int lastDot = fileName.lastIndexOf('.');
 
         return fileName.substring(0, lastDot).replace('/', '.');
     }
 
     private void handlePackageCycleGroup(XsdCycleGroup group)
     {
         Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.CYCLE_GROUP_RULE_KEY);
         ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.CYCLE_GROUP_RULE_KEY);
 
         if (rule != null && activeRule != null)
         {
             for (XsdCyclePath pathElement : group.getCyclePath())
             {
                 String fqName = pathElement.getParent();
                 Resource javaPackage = sensorContext.getResource(new JavaPackage(fqName));
 
                 if (javaPackage == null)
                 {
                     LOG.error("Cannot obtain resource " + fqName);
                 }
                 else
                 {
                     Violation v = new Violation(rule, javaPackage);
 
                     v.setMessage("Package participates in a cycle group");
                     v.setLineId(1);
                     v.setPriority(activeRule.getPriority());
                     sensorContext.saveViolation(v);
                 }
             }
         }
     }
 
     private int handleArchitectureViolations(XsdViolations violations, String buildUnitName)
     {
         Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.ARCH_RULE_KEY);
         ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.ARCH_RULE_KEY);
         int count = 0;
 
         for (XsdArchitectureViolation violation : violations.getArchitectureViolations())
         {
             String toName = getAttribute(violation.getArchitectureViolation().getAttribute(), "To");
             String toElemType = getAttribute(violation.getArchitectureViolation().getAttribute(), "To element type").toLowerCase();
             String target = toElemType + ' ' + toName;
 
             for (XsdTypeRelation rel : violation.getTypeRelation())
             {
                 String toType = getAttribute(rel.getAttribute(), "To");
                 String msg = "Type " + toType + " from " + target + " must not be used from here";
                 String bu = getAttribute(rel.getAttribute(), "From build unit");
 
                 bu = getBuildUnitName(bu);
                 if (bu.equals(buildUnitName))
                 {
                     for (XsdPosition pos : rel.getPosition())
                     {
                         if (rule != null && activeRule != null)
                         {
                             String relFileName = pos.getFile();
 
                             if (relFileName != null)
                             {
                                 String fqName = relativeFileNameToFqName(relFileName);
                                 saveViolation(rule, activeRule.getPriority(), fqName, Integer.valueOf(pos.getLine()), msg);
                             }
                         }
                         count++;
                     }
                 }
             }
 
         }
         if (rule == null)
         {
             LOG.error("SonarJ architecture rule not found");
         }
         else if (activeRule == null)
         {
             LOG.warn("SonarJ architecture rule deactivated");
         }
         return count;
     }
 
     private String getRuleKey(String attributeGroup)
     {
         if (attributeGroup.equals("Duplicate code"))
         {
             return SonarJPluginBase.DUPLICATE_RULE_KEY;
         }
         if (attributeGroup.equals("Workspace"))
         {
             return SonarJPluginBase.WORKSPACE_RULE_KEY;
         }
         if (attributeGroup.equals("Threshold"))
         {
             return SonarJPluginBase.THRESHOLD_RULE_KEY;
         }
         return null;
     }
 
     private void handleWarnings(XsdWarnings warnings, String buildUnitName)
     {
         for (XsdWarningsByAttributeGroup warningGroup : warnings.getWarningsByAttributeGroup())
         {
             String key = getRuleKey(warningGroup.getAttributeGroup());
             if (key == null)
             {
                 continue;
             }
             Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, key);
             ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, key);
             if (rule == null)
             {
                 LOG.error("SonarJ threshold rule not found");
                 continue;
             }
             if (activeRule == null)
             {
                 LOG.info("SonarJ threshold rule deactivated");
                 continue;
             }
             for (XsdWarningsByAttribute warningByAttribute : warningGroup.getWarningsByAttribute())
             {
                 String attrName = warningByAttribute.getAttributeName();
 
                 for (XsdWarning warning : warningByAttribute.getWarning())
                 {
                     String msg = attrName + "=" + getAttribute(warning.getAttribute(), "Attribute value");
                     String bu = getAttribute(warning.getAttribute(), "Build unit");
 
                     bu = getBuildUnitName(bu);
                     if (bu.equals(buildUnitName))
                     {
                         if (warning.getPosition().size() > 0)
                         {
                             for (XsdPosition pos : warning.getPosition())
                             {
                                 String relFileName = pos.getFile();
 
                                 if (relFileName != null)
                                 {
                                     String fqName = relativeFileNameToFqName(relFileName);
 
                                     saveViolation(rule, activeRule.getPriority(), fqName, Integer.valueOf(pos.getLine()), msg);
                                 }
                             }
                         }
                         else
                         {
                             String elemType = getAttribute(warning.getAttribute(), "Element type");
 
                             if (elemType.equals("Class file") || elemType.equals("Source file"))
                             {
                                 // Attach a violation at line 1
                                 String fileName = getAttribute(warning.getAttribute(), "Element");
                                 String fqName = fileName.substring(0, fileName.lastIndexOf('.')).replace('/', '.');
 
                                 saveViolation(rule, activeRule.getPriority(), fqName, 1, msg);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private String handleDescription(String descr)
     {
         if (descr.startsWith("Fix warning"))
         {
             // TODO: handle ascending metrics correctly (99% are descending)
             return "Reduce" + descr.substring(descr.indexOf(':') + 1).toLowerCase();
         }
         if (descr.startsWith("Cut type"))
         {
             String toType = descr.substring(descr.indexOf("to "));
 
             return "Cut dependency " + toType;
         }
         if (descr.startsWith("Move type"))
         {
             String to = descr.substring(descr.indexOf("to "));
 
             return "Move " + to;
         }
         return descr;
     }
 
     private int handleTasks(XsdTasks tasks, String buildUnitName)
     {
         Map<String, RulePriority> priorityMap = new HashMap<String, RulePriority>();
 
         Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.TASK_RULE_KEY);
         int count = 0;
 
         if (rule == null)
         {
             LOG.error("SonarJ task rule not found");
             return 0;
         }
 
         ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.TASK_RULE_KEY);
 
         if (activeRule == null)
         {
             LOG.info("SonarJ task rule not activated");
         }
 
         priorityMap.put("Low", RulePriority.INFO);
         priorityMap.put("Medium", RulePriority.MINOR);
         priorityMap.put("High", RulePriority.MAJOR);
 
         for (XsdTask task : tasks.getTask())
         {
             String bu = getAttribute(task.getAttribute(), "Build unit");
 
             bu = getBuildUnitName(bu);
             if (bu.equals(buildUnitName))
             {
                 String priority = getAttribute(task.getAttribute(), "Priority");
                 String description = getAttribute(task.getAttribute(), "Description");
                 String assignedTo = getAttribute(task.getAttribute(), "Assigned to");
 
                 description = handleDescription(description); // This should not be needed, but the current description sucks
 
                 int index = description.indexOf(" package");
 
                 if (index > 0 && index < 8)
                 {
                     // Package refactorings won't get markers - this would create to many non relevant markers
                     count++;
                 }
                 else
                 {
                     if (assignedTo != null)
                     {
                         assignedTo = '[' + StringUtils.trim(assignedTo) + ']';
                         if (assignedTo.length() > 2)
                         {
                             description += ' ' + assignedTo;
                         }
                     }
                     for (XsdPosition pos : task.getPosition())
                     {
                         String relFileName = pos.getFile();
 
                         if (relFileName != null)
                         {
                             String fqName = relativeFileNameToFqName(relFileName);
                             int line = Integer.valueOf(pos.getLine());
 
                             if (line == 0)
                             {
                                 line = 1;
                             }
                             if (activeRule != null)
                             {
                                 saveViolation(rule, priorityMap.get(priority), fqName, line, description);
                             }
                         }
                         count++;
                     }
                 }
             }
         }
         return count;
     }
 
     private void addArchitectureMeasures(ReportContext report, String buildUnitName)
     {
         double types = saveMeasure(INTERNAL_TYPES, SonarJMetrics.INTERNAL_TYPES, 0).getValue();
         Measure unassignedTypes = saveMeasure(UNASSIGNED_TYPES, SonarJMetrics.UNASSIGNED_TYPES, 0);
         Measure violatingTypes = saveMeasure(VIOLATING_TYPES, SonarJMetrics.VIOLATING_TYPES, 0);
         saveMeasure(VIOLATING_DEPENDENCIES, SonarJMetrics.VIOLATING_DEPENDENCIES, 0);
         saveMeasure(TASKS, SonarJMetrics.TASKS, 0);
         saveMeasure(THRESHOLD_WARNINGS, SonarJMetrics.THRESHOLD_WARNINGS, 0);
         saveMeasure(WORKSPACE_WARNINGS, SonarJMetrics.WORKSPACE_WARNINGS, 0);
         saveMeasure(IGNORED_VIOLATIONS, SonarJMetrics.IGNORED_VIOLATONS, 0);
         saveMeasure(IGNORED_WARNINGS, SonarJMetrics.IGNORED_WARNINGS, 0);
         saveMeasure(DUPLICATE_WARNINGS, SonarJMetrics.DUPLICATE_WARNINGS, 0);
         saveMeasure(CYCLE_WARNINGS, SonarJMetrics.CYCLE_WARNINGS, 0);
         saveMeasure(ALL_WARNINGS, SonarJMetrics.ALL_WARNINGS, 0);
 
         assert types >= 1.0 : "Project must not be empty !";
 
         double violatingTypesPercent = 100.0 * violatingTypes.getValue() / types;
         double unassignedTypesPercent = 100.0 * unassignedTypes.getValue() / types;
 
         saveMeasure(SonarJMetrics.VIOLATING_TYPES_PERCENT, violatingTypesPercent, 1);
         saveMeasure(SonarJMetrics.UNASSIGNED_TYPES_PERCENT, unassignedTypesPercent, 1);
 
         XsdViolations violations = report.getViolations();
 
         double violating_refs = 0;
         double task_refs = 0;
 
         if (rulesManager != null && rulesProfile != null)
         {
             violating_refs = handleArchitectureViolations(violations, buildUnitName);
             handleWarnings(report.getWarnings(), buildUnitName);
             task_refs = handleTasks(report.getTasks(), buildUnitName);
         }
         saveMeasure(SonarJMetrics.ARCHITECTURE_VIOLATIONS, violating_refs, 0);
         saveMeasure(SonarJMetrics.TASK_REFS, task_refs, 0);
     }
 
     private void analyse(IProject project, XsdAttributeRoot xsdBuildUnit, String buildUnitName, ReportContext report, boolean isMultiModuleProject)
     {
         LOG.info("Adding measures for " + project.getName());
 
         projectMetrics = readAttributes(xsdBuildUnit);
 
         Number internalPackages = projectMetrics.get(INTERNAL_PACKAGES);
 
         if (internalPackages.intValue() == 0)
         {
             LOG.warn("No classes found in project " + project.getName());
             return;
         }
 
         double acd = projectMetrics.get(ACD).doubleValue();
         double nccd = projectMetrics.get(NCCD).doubleValue();
 
         saveMeasure(ACD, SonarJMetrics.ACD, 1);
         saveMeasure(NCCD, SonarJMetrics.NCCD, 1);
         saveMeasure(INSTRUCTIONS, SonarJMetrics.INSTRUCTIONS, 0);
         saveMeasure(JAVA_FILES, SonarJMetrics.JAVA_FILES, 0);
         saveMeasure(TYPE_DEPENDENCIES, SonarJMetrics.TYPE_DEPENDENCIES, 0);
         saveMeasure(EROSION_REFS, SonarJMetrics.EROSION_REFS, 0);
         saveMeasure(EROSION_TYPES, SonarJMetrics.EROSION_TYPES, 0);
         Number structuralDebtIndex = saveMeasure(STUCTURAL_DEBT_INDEX, SonarJMetrics.EROSION_INDEX, 0).getValue();
 
         if (indexCost > 0) {
             double structuralDebtCost = structuralDebtIndex.doubleValue() * indexCost;
             saveMeasure(SonarJMetrics.EROSION_COST, structuralDebtCost, 0);
         }
 
         analyseCycleGroups(report, internalPackages, buildUnitName);
         if (projectMetrics.get(UNASSIGNED_TYPES) != null)
         {
             LOG.info("Adding architecture measures for " + project.getName());
             addArchitectureMeasures(report, buildUnitName);
         }
 
         AlertDecorator.setAlertLevels(new SensorProjectContext(sensorContext));
     }
 
     protected void analyse(IProject project, SensorContext sensorContext, ReportContext report)
     {
         this.sensorContext = sensorContext;
         
         XsdBuildUnits buildUnits = report.getBuildUnits();
         List<XsdAttributeRoot> buildUnitList = buildUnits.getBuildUnit();
 
         if (buildUnitList.size() > 1)
         {
             String longName = project.getArtifactId() + "[" + project.getGroupId() + "]";
             String longName2 = project.getGroupId() + ':' + project.getArtifactId();
 
             for (XsdAttributeRoot sonarBuildUnit : buildUnitList)
             {
                 String buName = sonarBuildUnit.getName();
 
                 buName = getBuildUnitName(buName);
                 if (buName.equals(project.getArtifactId()) || buName.equals(longName) || buName.equals(longName2))
                 {
                     analyse(project, sonarBuildUnit, buName, report, true);
                     break;
                 }
                 else
                 {
                     if (buName.startsWith("...") && longName2.endsWith(buName.substring(2)))
                     {
                         analyse(project, sonarBuildUnit, buName, report, true);
                         break;
                     }
                 }
             }
         }
         else
         {
             String buName = buildUnitList.get(0).getName();
 
             buName = getBuildUnitName(buName);
             analyse(project, buildUnitList.get(0), buName, report, false);
         }
     }
 
     public void analyse(Project project, SensorContext sensorContext)
     {
 
         LOG.info("------------------------------------------------------------------------");
         LOG.info("Execute sonar-sonarj-plugin for " + project.getName());
         LOG.info("------------------------------------------------------------------------");
         ReportContext report = readSonarjReport(getReportFileName(project), project.getPackaging());
 
         if (report != null)
         {
             analyse(new ProjectDelegate(project), sensorContext, report);
         }
     }
 
     public final String getReportFileName(Project project)
     {
         return project.getFileSystem().getBuildDir().getPath() + '/' + REPORT_DIR + '/' + REPORT_NAME;
     }
 }
