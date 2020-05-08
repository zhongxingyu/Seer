 package net.adamcin.jenkins.granite;
 
 import hudson.Extension;
 import hudson.console.ConsoleAnnotator;
 import hudson.console.ConsoleAnnotatorFactory;
 import hudson.model.Run;
 import org.jvnet.hudson.plugins.collapsingconsolesections.CollapsingSectionAnnotator;
 import org.jvnet.hudson.plugins.collapsingconsolesections.SectionDefinition;
 
 @Extension
 public class GraniteAnnotatorFactory extends ConsoleAnnotatorFactory<Class<Run>> {
 
     @Override
     public ConsoleAnnotator newInstance(Class<Run> context) {
         SectionDefinition uninstallSection = new SectionDefinition(
                 "[Granite] Uninstalling package",
                "M Uninstalling package from snapshot .*",
                "M Package imported.");
         SectionDefinition installSection = new SectionDefinition(
                 "[Granite] Installing package",
                "M Creating snapshot for package .*",
                "M Package imported.");
         return new CollapsingSectionAnnotator(uninstallSection, installSection);
     }
 }
